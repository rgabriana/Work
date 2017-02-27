/**
 * 
 */
package com.ems.server.upgrade;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.server.ServerConstants;
import com.ems.server.device.GatewayImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.GatewayManager;

/**
 * @author Lélio
 * 
 * This worker upgrades a gateway.
 * It sends a XFER_INIT command to each gateway with the filename of the firmware image. 
 * If the gateway understands this command, it stores the filename and send a XFER_INIT_OK ack 
 * to the EM. When the EM receives XFER_INIT_OK, it starts to stream the image to the gateway 
 * through the SSL connection using the XFER command. 
 * Once the image has been transferred, the EM send a CMD_IMG_UPGRADE command to the gateway 
 * with the filename and its SHA1 sum.
 * If the gateway is old and does not understand the XFER_INIT command, it will ignore it and 
 * will not send a XFER_INIT_OK ack.
 * If the EM does not receive XFER_INIT_OK after XFER_INIT_OK_TIMEOUT seconds, it fallbacks 
 * to the previous upgrade scheme (scp) by sending a regular CMD_IMG_UPGRADE command 
 * with the full path of the image.
 *
 */
public class GatewayImageUpgradeXferWorker extends Thread {

	private static final Logger logger = Logger.getLogger("ImageUpgrade");

	private GatewayManager gwMgr = null;
	private EventsAndFaultManager eventMgr = null;
	private FirmwareUpgradeManager firmUpMgr = null;

	private GWFirmware gwFirmware = null;
	private ImageUpgradeDBJob job = null;
	private Gateway gw = null;

	private static final int CHUNK_SIZE = 32000;
	
	private static final int XFER_INIT_OK_TIMEOUT = 20;

	private static final short GW_UPGRADE_RESP_OK = 0;
	private static final short GW_UPGRADE_FAILED = 2;
	private static final short GW_UPGRADE_IN_PROGRESS = 3;
	private static final short GW_UPGRADE_XFER_OK = 4;
	private static final short GW_UPGRADE_XFER_INVALID_FN = 5;

	public BlockingQueue<Integer> ackQueue = new LinkedBlockingQueue<Integer>();

  
	public GatewayImageUpgradeXferWorker(Gateway gw, ImageUpgradeDBJob job, GWFirmware gwFirmware) {

		this.job = job;
		this.gw = gw;
		this.gwFirmware = gwFirmware;

		gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
		eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
		firmUpMgr = (FirmwareUpgradeManager)SpringContext.getBean("firmwareUpgradeManager");

	} //end of constructor

	private void evtUpgradeFailed(Integer gwUpgrStatus) {
		String status = "file transfer";
		if(gwUpgrStatus != null) {
			if (gwUpgrStatus == GW_UPGRADE_FAILED) {
				status = "upgrade";
			}
			else if (gwUpgrStatus == GW_UPGRADE_XFER_INVALID_FN) {
				status = "invalid filename";
			}
		}
		String description = "image upgrade failed on gateway " + gw.getId() + 
				" failed for " + status + " (received ack " + gwUpgrStatus + ").";
		logger.info(description);
		firmUpMgr.finishDeviceUpgrade(job.getId(), gw.getId(), ServerConstants.IMG_UP_STATUS_FAIL, 1, description, "");
		gwMgr.setImageUpgradeStatus(gw.getId(), ServerConstants.IMG_UP_STATUS_FAIL);
		eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR); 
	}

	private void evtUpgradeSuccess(long startTime) {
		logger.info("Time taken to transfer the file on gateway " + gw.getId() + 
				" is " + (System.currentTimeMillis() - startTime));
		String description = "Image upgrade of Gateway with image " + gwFirmware.fileName + " successful";
		firmUpMgr.finishDeviceUpgrade(job.getId(), new Long(gw.getId()), 
				ServerConstants.IMG_UP_STATUS_SUCCESS, 1, description, gw.getVersion());			    
		gwMgr.setImageUpgradeStatus(gw.getId(), ServerConstants.IMG_UP_STATUS_SUCCESS);
		eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR);
	}

	public void run() {
		String description = null;	 
		boolean failure = false;
		logger.info("Launching new-protocol gateway upgrade (gw " + gw.getId() + ")"); 

		//command is sent so changing the status to in progress
		Long[] temp = { gw.getId() };
		firmUpMgr.startDeviceUpgrade(job.getId(), temp);
		description = "Image upgrade of Gateway " + gw.getId() +" with image " + gwFirmware.fileName + "is in progress";
		gwMgr.setImageUpgradeStatus(gw.getId(), ServerConstants.IMG_UP_STATUS_INPROGRESS);
		eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR);

		while(true) {
			synchronized(ImageUpgradeSO.gwThreadLock) {
				if(!ImageUpgradeSO.getInstance().gwUpgrMap.containsKey(gw.getId()) &&
						!ImageUpgradeSO.getInstance().gwXferUpgrMap.containsKey(gw.getId()) &&
						!ImageUpgradeSO.getInstance().gwFixtUpgrMap.containsKey(gw.getId()) &&
						!ImageUpgradeSO.getInstance().gwWdsUpgrMap.containsKey(gw.getId())) {
					ImageUpgradeSO.getInstance().gwXferUpgrMap.put((long) gw.getId(), this);
					break;
				}
			}
			logger.info(gw.getId() + ": other image upgrade job is running on this gw");
			ServerUtil.sleep(30);
		}

		// First, we send a XFER_INIT command. If we don't receive a proper ack, 
		// we'll switch to the old fashioned upgrade.
		GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_FILE_XFER_INIT, 
				gw, gwFirmware.xferInitPkt, false);

		// The ack codes are put in a blockingqueue by the ImageUpradeSO thread
		Integer gwUpgrStatus;
		try {
			gwUpgrStatus = ackQueue.poll(XFER_INIT_OK_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			gwUpgrStatus = null;
		}

		// At this point, we either have no ack and the new protocol is not supported,
		// or a XFER_INIT_OK ack and we will start to stream the firmware

		long startTime = System.currentTimeMillis();
		
		// No need to stream the image to all the gateways at the same time, so we use
		// a semaphore
		Semaphore smp = ImageUpgradeSO.getInstance().getGtwSemaphore();
		smp.acquireUninterruptibly();
		
		// Beware, gwUpgrStatus is an Integer, hence we must check it is not null
		// (or this will raise a NullPointerException)
		if (gwUpgrStatus == null) {
			// The GW has not send a cmd_upgrade_ack: it did not understand the
			// command -> xfer_init is not supported.
			logger.info(gw.getId() + ": No ack for gw xfer_init; fallback to old style upgrade");
			gwLaunchUpgrade();
		}
		else if (gwUpgrStatus == GW_UPGRADE_XFER_OK) {
			if (!gwImageUpload()) {
				failure = true;
				
				// The transfer was interrupted, an ack is probably pending
				gwUpgrStatus = ackQueue.poll();
				evtUpgradeFailed(gwUpgrStatus);
			}
			else {
				gwLaunchNewUpgrade();
			}
		}
		else if (gwUpgrStatus == GW_UPGRADE_IN_PROGRESS) {
			logger.info("Update is already in progress for gw " + gw.getId());
			failure = true;
		}
		else {
			evtUpgradeFailed(gwUpgrStatus);
			failure = true;
		}
		
		smp.release();

		if (!failure) {
			try {
				gwUpgrStatus = ackQueue.poll(4, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
				gwUpgrStatus = null;
			}

			if (gwUpgrStatus != null && gwUpgrStatus == GW_UPGRADE_RESP_OK) {
				evtUpgradeSuccess(startTime);
			}
			else { // no ack or invalid ack
				evtUpgradeFailed(gwUpgrStatus);
			}
		}

		// TODO: think about this. If the upgrade is in progress (somewhere else), 
		// do we really want to clear this map? If we don't, we might just get stuck.
		synchronized(ImageUpgradeSO.gwThreadLock) {
			ImageUpgradeSO.getInstance().gwXferUpgrMap.remove((long) gw.getId());
		}

	} //end of method run

	/**
	 * Start the transfer of a file to the gw.
	 * @return true if the file was transferred, false if transfer was interrupted.
	 */
	private boolean gwImageUpload() {
		for (int offset = 0; offset <= gwFirmware.fileSize; offset += CHUNK_SIZE) {
			if (!ackQueue.isEmpty()) { 
				// We received an ack in the middle of a transfer, the gateway might 
				// want to stop the transfer: abort (no space left or other reason)
				return false;
			}
			byte[] dataPacket = gwDataPacket(offset);

			GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_FILE_XFER, 
						this.gw, dataPacket, false);	
		}

		String description = "File " + gwFirmware.fileName + " uploaded";
		eventMgr.addEvent(this.gw, description, EventsAndFault.GW_IMG_UP_STR);
		return true;
	} // end of method gwImageUpload

	/**
	 * Send a new protocol CMD_IMAGE_UPGRADE command (filename instead of filepath)
	 */
	private void gwLaunchNewUpgrade() {
		GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_UPGRADE_CMD, 
				gw, gwFirmware.newUpgPkt, false);	
	} // end of method gwLaunchNewUpgrade
	
	/**
	 * Send an old protocol CMD_IMAGE_UPGRADE command (filepath to be scp-ed)
	 */
	private void gwLaunchUpgrade() {
		GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_UPGRADE_CMD, 
				gw, gwFirmware.standardUpgPkt, false);	
	} // end of method gwLaunchUpgrade

	/**
	 * Return a full xfer packet with header and payload, of maximum size CHUNK_SIZE
	 * and containing the firmware bytes [offset:offset+CHUNK_SIZE]
	 * @param offset
	 * @return
	 */
	private byte[] gwDataPacket(int offset) {
		int dataChunkSize = Math.min(CHUNK_SIZE, gwFirmware.fileSize - offset); 

		byte[] pkt = new byte[4 + 4 + dataChunkSize];
		byte[] offsetArr = ServerUtil.intToByteArray(offset);
		byte[] dataLenArr = ServerUtil.intToByteArray(dataChunkSize);

		System.arraycopy(offsetArr, 0, pkt, 0, 4);
		System.arraycopy(dataLenArr, 0, pkt, 4, 4);
		System.arraycopy(gwFirmware.fileArray, offset, pkt, 8, dataChunkSize);
		
		return pkt;
	} // end of method gwDataPacket

	public void gwCancelFileUpload(Gateway gateway, byte[] packet) {


	} //end of method gwCancelFileUpload


}