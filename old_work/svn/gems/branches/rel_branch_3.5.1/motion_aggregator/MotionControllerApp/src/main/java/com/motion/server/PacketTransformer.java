package com.motion.server;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.motion.dao.FixtureDao;
import com.motion.dao.MotionPacketDao;
import com.motion.utils.CommonUtils;
import com.motion.utils.MotionAppConstants;
import com.motion.utils.SpringContext;

public class PacketTransformer implements Job {
	public static final Logger logger = Logger
			.getLogger(PacketTransformer.class.getName());
	private BlockingQueue<byte[]> workQueue = null;
	
	private static Long processed = 0l;
	private static Long incrementCount = 1l;
	private UdpListener udpListener = null;
	private MotionPacketDao motionPacketDao = null;
	private FixtureDao fixtureDao = null;

	public PacketTransformer() {
		udpListener = (UdpListener) SpringContext.getBean("udpListener");
		motionPacketDao = (MotionPacketDao) SpringContext
				.getBean("motionPacketDao");
		fixtureDao = (FixtureDao) SpringContext.getBean("fixtureDao");
		init();
	}

	public void init() {
		this.workQueue = udpListener.getWorkQueue();

	}

	public void startTranforming() {
		logger.info("Motion packet transformer started");
		while (true) {
			try {

				byte[] data = workQueue.take();
				long startTime = System.currentTimeMillis();
				byte[] pktLenArr = { data[3], data[4] };
				int pktLen = CommonUtils.byteArrayToShort(pktLenArr);
				byte[] actualData = new byte[pktLen];
				System.arraycopy(data, 0, actualData, 0, actualData.length);
				if (!checkDataValidity(actualData, pktLen)) {
					logger.error("Packet got was not valid or matching checksum. Packet is :- "
							+ new String(data));
					logger.info("Moving on to next packet..");
					continue;

				}
				HashMap<String, String> list = splitAndTranformData(actualData);
				motionPacketDao.saveMotionPacket(list);
				processed = processed + incrementCount;
				logger.debug("Motion packet processed till now :-"
						+ processed.toString()
						+ " Time Taken to processes last packet:-"
						+ (System.currentTimeMillis() - startTime) + " ms");
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public HashMap<String, String> splitAndTranformData(byte[] data) {
		
		HashMap<String, String> newData = new HashMap<String, String>();
		try {
		byte[] macArr = new byte[6];
		System.arraycopy(data, 5, macArr, 0, macArr.length);
		byte[] blobArr = new byte[4];
		System.arraycopy(data, 11, blobArr, 0, blobArr.length);
		byte[] localX = new byte[4];
		System.arraycopy(data, 15, localX, 0, localX.length);
		byte[] localY = new byte[4];
		System.arraycopy(data, 19, localY, 0, localY.length);
		newData.put("mac", parseMac(macArr));
		newData.put("blob_id",
				Integer.toString(CommonUtils.byteArrayToInt(blobArr)));
		newData.put("local_x",
				Integer.toString(CommonUtils.byteArrayToInt(localX)));
		newData.put("local_y",
				Integer.toString(CommonUtils.byteArrayToInt(localY)));
		newData.put("fixture_id",
				fixtureDao.getFixtureIdFromMac(newData.get("mac")));
		Integer x = Integer.parseInt(fixtureDao.getFixtureXbyId(newData
				.get("fixture_id"))) + Integer.parseInt(newData.get("local_x"));
		newData.put("global_x", Integer.toString(x));
		Integer y = Integer.parseInt(fixtureDao.getFixtureYbyId(newData
				.get("fixture_id"))) + Integer.parseInt(newData.get("local_y"));
		newData.put("global_y", Integer.toString(y));
		logger.debug("mac = " + newData.get("mac") + " blob_id = "
				+ newData.get("blob_id") + " local_x ="
				+ newData.get("local_x") + " local_y ="
				+ newData.get("local_y") + "global_x ="
				+ newData.get("global_x") + "global_y ="
				+ newData.get("global_y"));
	
			MotionAppConstants.displayDataSendQueue.put(newData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage()) ;
			newData = null ;
		}
		return newData;
		
	}

	public String parseMac(byte[] mac) {
		if (mac == null)
			return null;

		StringBuilder sb = new StringBuilder(18);
		for (byte b : mac) {
			if (sb.length() > 0)
				sb.append(':');
			sb.append(String.format("%02x", b));
		}
		logger.debug("Mac parsed :- " + sb.toString());
		return sb.toString();
	}

	public Boolean checkDataValidity(byte[] data, Integer length) {
		Boolean status = false;
		byte[] startByteArr = new byte[3];
		byte[] endByteArr = new byte[3];
		System.arraycopy(data, 0, startByteArr, 0, startByteArr.length);
		System.arraycopy(data, data.length - 3, endByteArr, 0,
				endByteArr.length);
		logger.debug("Packet is :-" + new String(data));
		logger.debug("Header is :- " + new String(startByteArr)
				+ " Footer is :- " + new String(endByteArr)
				+ " Length of the packet give is :- " + length
				+ " Length calculated is :- " + data.length);
		if (new String(startByteArr)
				.equalsIgnoreCase(MotionAppConstants.MOTION_PACKET_HEADER)) {
			if (new String(endByteArr)
					.equalsIgnoreCase(MotionAppConstants.MOTION_PACKET_FOOTER)) {
				if (length.intValue() == data.length) {
					status = true;
				}
			}
		}
		return status;
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		init();
		logger.info("Starting Motion packet Transformer....");
		startTranforming();

	}

}
