/**
 * 
 */
package com.ems.server.device.plugload;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Plugload;
import com.ems.server.ServerConstants;
import com.ems.server.device.CommandScheduler;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.service.PlugloadManager;

/**
 * @author sreedhar.kamishetti
 * 
 */
public class PlugloadResponsePacket {

	private static final Logger logger = Logger.getLogger("PlugloadLogger");

	private byte[] pkt = null;
	private String gwIp = null;
	private long gwId = -1;

	private PlugloadManager plMgr = null;

	public PlugloadResponsePacket(byte[] plPkt, String gwIp, long gwId) {

		this.pkt = plPkt;
		this.gwIp = gwIp;
		this.gwId = gwId;
		plMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");

	} // end of constructor

	public void finalize() {

		pkt = null;
		gwIp = null;

	} // end of method finalize

	/*
	 * function to return the message type of the response packet
	 */
	private int getMessageType() {

		int msgTypePos = ServerConstants.RES_CMD_PKT_MSG_TYPE_POS;
		int msgType = (pkt[msgTypePos] & 0xFF);
		return msgType;

	} // end of method getMessageType

	/*
	 * returns the string format of 3 byte mac address of plugload
	 */
	private String getPlugloadAddress() {
		return ServerUtil.getSnapAddr(pkt[8], pkt[9], pkt[10]);
	}

	// this is the function to parse the packet received from plugload
	public void processResponse() {

		String snapAddr = getPlugloadAddress();
		int msgType = getMessageType();
		// when the SU is discovered first time, fixture is not there in the
		// database
		// so call discovery class without checking for fixture object
		if (msgType == ServerConstants.CMD_DISCOVERY_RESPONSE) {
			DiscoverySO.getInstance().plugloadDiscovery(snapAddr, pkt, gwIp);
			pkt = null;
			return;
		}

		Plugload plugload = plMgr.getPlugloadBySnapAddress(snapAddr);
		if (plugload == null) {
			logger.error(snapAddr
					+ ": There is no Plugload, ignoring the device pkt - "
					+ ServerUtil.getLogPacket(pkt));
			pkt = null;
			return;
		}
		logger.info("Plugload packet: " + ServerUtil.getLogPacket(pkt));
		long plId = plugload.getId();
		switch (msgType) {
		case ServerConstants.NODE_INFO_MSG_TYPE:
			PlugloadImpl.getInstance().nodeBootInfo(plugload, pkt, gwId);
			break;
		case ServerConstants.ACK_TO_MSG:
			int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
			if ((pkt[msgStartPos] & 0xFF) == ServerConstants.ISP_INIT_ACK_OPCODE) {
				ImageUpgradeSO.getInstance()
						.ackImageUploadStart(plugload, gwId);
			} else {
				int ackToMsg = pkt[msgStartPos] & 0xFF;
				if (logger.isDebugEnabled()) {
					logger.debug(plugload.getId() + ": ack packet(" + ackToMsg
							+ ") -- " + ServerUtil.getLogPacket(pkt));
				}
				CommandScheduler.getInstance().gotAck(pkt, plugload, gwId);
				switch (ackToMsg) {
				case ServerConstants.SU_SET_APPLY_WIRELESS_CMD:
					DeviceServiceImpl.getInstance().plugLoadWirelessAckStatus(plugload, true);
					break;
				default:
					break;
				}
			}
		default:
			break;
		}
	}
}
