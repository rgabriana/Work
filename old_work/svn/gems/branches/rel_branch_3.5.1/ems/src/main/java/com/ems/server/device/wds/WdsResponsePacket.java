/**
 * 
 */
package com.ems.server.device.wds;

import java.util.Date;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Wds;
import com.ems.server.ServerConstants;
import com.ems.server.device.CommandScheduler;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.service.WdsManager;

/**
 * @author yogesh
 * 
 */
public class WdsResponsePacket {
    private static final Logger logger = Logger.getLogger("SwitchLogger");
    private byte[] pkt = null;
    private String gwIp = null;
    private long gwId = -1;
    private WdsManager wdsMgr = null;

    public WdsResponsePacket(byte[] suPkt, String gwIp, long gwId) {
        this.pkt = suPkt;
        this.gwIp = gwIp;
        this.gwId = gwId;
        wdsMgr = (WdsManager) SpringContext.getBean("wdsManager");
    }

    public void finalize() {
        pkt = null;
        gwIp = null;
    }

    /*
     * function to return the message type of the response packet
     */
    private int getMessageType() {
        int msgTypePos = ServerConstants.RES_CMD_PKT_MSG_TYPE_POS;
        if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            msgTypePos = 2;
        }
        if (pkt[1] == 1) { // 1.2 packet
            msgTypePos -= 3;
        }
        int msgType = (pkt[msgTypePos] & 0xFF);
        return msgType;
    }

    /*
     * returns the string format of 3 byte mac address of su
     */
    private String getWDSAddress() {
        return ServerUtil.getSnapAddr(pkt[8], pkt[9], pkt[10]);
    }

    /*
     * function to return the data part of the packet
     */
    private byte[] getDataPacket() {
        int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            index = 3;
        }
        int pktLen = pkt.length;
        byte[] data = new byte[pktLen - index];
        System.arraycopy(pkt, index, data, 0, data.length);
        return data;
    }

    private long getTransactionId() {
        byte[] seqNoArr = new byte[4];
        System.arraycopy(pkt, 4, seqNoArr, 0, seqNoArr.length);
        long seqNo = ServerUtil.byteArrayToInt(seqNoArr);
        return seqNo;
    }

    // this is the function to parse the packet received from SU
    public void processResponse() {
        String snapAddr = getWDSAddress();
        int msgType = getMessageType();
        // when the SU is discovered first time, fixture is not there in the database
        // so call discovery class without checking for fixture object
        if (msgType == ServerConstants.CMD_SWITCH_DISCOVER_RESPONSE) {
            DiscoverySO.getInstance().discoveryWDS(snapAddr, pkt, gwIp);
            pkt = null;
            return;
        }
        Wds oWds = wdsMgr.getWdsSwitchBySnapAddress(snapAddr);
        if (oWds == null) {
            logger.error(snapAddr + ": There is no ERC, ignoring the node pkt - " + ServerUtil.getLogPacket(pkt));
            pkt = null;
            return;
        }
        logger.info("WDS packet: " + ServerUtil.getLogPacket(pkt));
        long switchId = oWds.getId();
        switch (msgType) {
        case ServerConstants.RESEND_REQUEST:
      	  ImageUpgradeSO.getInstance().missingPacketRequest(oWds, pkt, gwId);
      	  break;
      	case ServerConstants.ABORT_ISP_OPCODE:
      	  ImageUpgradeSO.getInstance().cancelFileUpload(oWds, pkt, gwId);
      	  break;
      	case ServerConstants.NODE_INFO_MSG_TYPE:
      		ImageUpgradeSO.getInstance().nodeRebooted(oWds, pkt, gwId);      	  
      	  break;
      	case ServerConstants.GET_MANUF_INFO_RSP:
      		DeviceServiceImpl.getInstance().processManufacturingInfo(pkt);
      		break;
        case ServerConstants.ACK_TO_MSG:
            int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
            if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
                msgStartPos = 3;
            }
            if ((pkt[msgStartPos] & 0xFF) == ServerConstants.ISP_INIT_ACK_OPCODE) {		
        	    ImageUpgradeSO.getInstance().ackImageUploadStart(oWds, gwId);
        	  } else {
        	  	int ackToMsg = pkt[msgStartPos] & 0xFF;
        	  	if (logger.isDebugEnabled()) {
                logger.debug(oWds.getId() + ": ack packet(" + ackToMsg + ") -- " + ServerUtil.getLogPacket(pkt));
        	  	}        	 
        	  	// ServerMain.getInstance().ackDeviceMessage(fixture, pkt, gwId);
        	  	CommandScheduler.getInstance().gotAck(pkt, oWds, gwId);
        	  	switch (ackToMsg) {
        	  	case ServerConstants.CMD_SET_SWITCH_PARAMS:
                logger.info("WDS packet Ack for set switch param.");
                oWds.setState(ServerConstants.WDS_STATE_COMMISSIONED_STR);
                wdsMgr.updateState(oWds);
                // Set the WDS to commission mode.
                //WDSImpl.getInstance().applyWireless(switchId, oWds.getGatewayId());
                break;
        	  	case ServerConstants.CMD_APPLY_SWITCH_PARAMS:
                logger.info("(ERC Commissioned): ERC packet Ack for apply switch param.");
                break;
        	  	default:
                break;
        	  	}
        	  }
            break;
        case ServerConstants.WDS_BATTERY_LEVEL_MSG:
        	if(pkt.length==26)
        	{
        		int batteryVolt = ServerUtil.byteArrayToShort(new byte[]{pkt[23],pkt[24]});
        		wdsMgr.updateBatteryLevel(oWds,batteryVolt);
        	}	
        	break;
        default:
            break;
        }
    }
}
