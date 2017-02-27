/**
 * 
 */
package com.ems.server.device.plugload;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.PlugloadCache;
import com.ems.model.EventsAndFault;
import com.ems.model.Plugload;
import com.ems.server.GatewayInfo;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.CommandScheduler;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.service.PMStatsProcessorService;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
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
	private EventsAndFaultManager eventMgr = null;

	public PlugloadResponsePacket(byte[] plPkt, String gwIp, long gwId) {

		this.pkt = plPkt;
		this.gwIp = gwIp;
		this.gwId = gwId;
		plMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");
		eventMgr = (EventsAndFaultManager) SpringContext.getBean("eventsAndFaultManager");

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
		//long plId = plugload.getId();
		switch (msgType) {
		case ServerConstants.PL_REPT_PM_DATA_MSG_TYPE:	  	  
  		if (plugload.getState().equals(ServerConstants.FIXTURE_STATE_DELETED_STR)) {
  			String sMsg = snapAddr + ": Plugload in deleted state, ignoring the pms stats";
  			logger.info(sMsg);
  			GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gwId);
  			if(gwInfo != null && gwInfo.getOperationalMode() == GatewayInfo.GW_NORMAL_MODE) {       			
  				eventMgr.addEvent(plugload, sMsg, EventsAndFault.WIRELESS_PARAMS_MISMATCH_STR);  				    				       		
  	    }
  			pkt = null;
				plugload = null;
				return; 
  		}
  		PerfSO.getInstance().updatePlugloadStats(plugload, pkt, gwId);      	  
  		break;
		case ServerConstants.NODE_INFO_MSG_TYPE:
			PlugloadImpl.getInstance().nodeBootInfo(plugload, pkt, gwId);
			break;
		case ServerConstants.SU_CMD_REQ_DETAIL_CONFIG_CRC_RESP:
    	PlugloadImpl.getInstance().receivedGroupChecksums(plugload, pkt);
    	break;
		case ServerConstants.GET_STATUS_MSG_TYPE:
  	  getCurrentState(plugload, pkt, gwId);      	  
  	  break;
		case ServerConstants.RESEND_REQUEST:
  	  ImageUpgradeSO.getInstance().missingPacketRequest(plugload, pkt, gwId);
  	  break;
  	case ServerConstants.ABORT_ISP_OPCODE:
  	  ImageUpgradeSO.getInstance().cancelFileUpload(plugload, pkt, gwId);
  	  break;
		case ServerConstants.ACK_TO_MSG:
			int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
			if ((pkt[msgStartPos] & 0xFF) == ServerConstants.ISP_INIT_ACK_OPCODE) {
				ImageUpgradeSO.getInstance().ackImageUploadStart(plugload, gwId);
			} else {
				int ackToMsg = pkt[msgStartPos] & 0xFF;
				if (logger.isDebugEnabled()) {
					logger.debug(plugload.getId() + ": ack packet(" + ackToMsg + ") -- " + ServerUtil.getLogPacket(pkt));
				}
				CommandScheduler.getInstance().gotAck(pkt, plugload, gwId);
				switch (ackToMsg) {
				case ServerConstants.SU_SET_APPLY_WIRELESS_CMD:
					PlugloadImpl.getInstance().plugLoadWirelessAckStatus(plugload, true);
					break;
				case ServerConstants.SU_CMD_JOIN_GRP:
		    	DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(plugload, ServerConstants.ACK_TO_MSG);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " joined group");
		    	}
		    	break;
		  	case ServerConstants.SU_CMD_LEAVE_GRP:
		    	DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(plugload, ServerConstants.ACK_TO_MSG);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " left group");
		    	}
		    	break;
		    case ServerConstants.SU_CMD_REQ_REST_GRP:
		    	DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(plugload, ServerConstants.ACK_TO_MSG);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " rest group successfull");
		    	}
		    	break;
		    case ServerConstants.CMD_SET_SWITCH_GRP_PARMS:
		    	DeviceServiceImpl.getInstance().suWirelessGrpConfigChangeAckStatus(plugload, true);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " switch group sync'd");
		    	}
		    	break;
		    case ServerConstants.CMD_SET_SWITCH_GRP_WDS:
		    	DeviceServiceImpl.getInstance().suWirelessGrpConfigChangeAckStatus(plugload, true);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " wds group sync'd");
		    	}
		    	break;
		    case ServerConstants.CMD_SWITCH_GRP_DEL_WDS:
		    	DeviceServiceImpl.getInstance().suWirelessGrpConfigChangeAckStatus(plugload, true);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " wds removed");
		    	}
		    	break;
		    case ServerConstants.ENABLE_HOPPER_MSG_TYPE:
		    	plMgr.updateHopperState(plugload.getId(), 1);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " hopper enabled");
		    	}
		    	break;
		    case ServerConstants.DISABLE_HOPPER_MSG_TYPE:
		    	plMgr.updateHopperState(plugload.getId(), 0);
		    	if(logger.isInfoEnabled()) {
		    		logger.info(plugload.getId() + " hopper disabled");
		    	}
		    	break;
				default:
					break;
				}
			}
			break;
		case ServerConstants.NACK_TO_MSG:
			msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
			if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
				msgStartPos = 3;
			}
			if ((pkt[msgStartPos] & 0xFF) != ServerConstants.ISP_INIT_ACK_OPCODE) {
				int nackToMsg = pkt[msgStartPos] & 0xFF;
				if (logger.isDebugEnabled()) {
					logger.debug(plugload.getId() + ": NACK packet(" + nackToMsg + ") -- " + ServerUtil.getLogPacket(pkt));
				}	
				CommandScheduler.getInstance().gotNack(pkt, plugload, gwId);
				switch(nackToMsg) {
				case ServerConstants.SU_CMD_JOIN_GRP:
					DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(plugload, ServerConstants.NACK_TO_MSG);
					if(logger.isInfoEnabled()) {
						logger.info(plugload.getId() + " joined group");
					}	
					break;
				case ServerConstants.CMD_MOTION_GRP_APPLY_ACTION:
					logger.warn(plugload.getId() + " " + plugload.getSnapAddress() + " motion group configuration not support!");
					break;
				}
			}
			break;
		default:
			break;
		}
		
	} //end of processResponse
		
	public void getCurrentState(Plugload plugload, byte[] packet, long gwId) {

    if (plMgr == null) {
    	plMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");
    }

    String plugloadName = plugload.getName();
    if(logger.isInfoEnabled()) {
    	logger.info(plugloadName + ": fx stats- " + ServerUtil.getLogPacket(packet));
    }

    int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS;
    
    byte currState = packet[pktIndex++]; // 4th
    byte currVolt = packet[pktIndex++]; // 5th
    byte currTemp = packet[pktIndex++]; // 6th
    byte currTempPrecision = 0;
    byte[] tempShortByteArr = new byte[2];
    // amb light bytes 7,8    
    pktIndex += 2;
        
    StringBuffer sb = new StringBuffer();
    sb.append("Plugload[");
    sb.append(plugloadName);
    sb.append("]: State=");
    sb.append(currState); // state
    sb.append(",Voltage=");
    sb.append(currVolt);
    sb.append(",Temp=");
    sb.append(currTemp);

    // motion secs ago bytes 9,10
    System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
    pktIndex += 2;
    int motionSecAgo = ServerUtil.byteArrayToShort(tempShortByteArr);    
    sb.append(",MotionSecAgo=");
    sb.append(motionSecAgo);

    // current time bytes 11,12,13, 14
    byte[] tempIntByteArr = new byte[4];
    System.arraycopy(packet, pktIndex, tempIntByteArr, 0, tempIntByteArr.length);
    pktIndex += 4;
    Calendar cal = Calendar.getInstance();
    int gmtOffset = (int) (cal.getTimeZone().getOffset(System.currentTimeMillis()));
    long currentTime = (ServerUtil.intByteArrayToLong(tempIntByteArr) * 60 * 1000) - gmtOffset;
    sb.append(",currentTime=");
    sb.append(currentTime + "(" + new Date(currentTime) + ")");

    // up time bytes 15,16,17,18
    System.arraycopy(packet, pktIndex, tempIntByteArr, 0, tempIntByteArr.length);
    pktIndex += 4;
    long upTime = ServerUtil.intByteArrayToLong(tempIntByteArr);    
    sb.append(",uptime=");
    sb.append(upTime);
    // byte 19 global profile checksum
    short gPrChecksum = packet[pktIndex++];
    sb.append(",global Profile checksum=");
    sb.append(gPrChecksum);
    plugload.setGlobalProfileChecksum(gPrChecksum);
 
    // byte 20 profile checksum
    short sPrChecksum = packet[pktIndex++];
    sb.append(",scheduled Profile checksum=");
    sb.append(sPrChecksum);
    plugload.setScheduledProfileChecksum((int)sPrChecksum);

    // bytes 21, 22 to off timer
    pktIndex += 2;
    // bytes 23,24 energy ticks    
    System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
    pktIndex += 2;
    int pulses = ServerUtil.byteArrayToShort(tempShortByteArr);    
    // bytes 25, 26, 27, 28 time in milli sec from last instant power reading
    System.arraycopy(packet, pktIndex, tempIntByteArr, 0, tempIntByteArr.length);
    pktIndex += 4;
    long pulsesDuration = ServerUtil.intByteArrayToLong(tempIntByteArr);
    sb.append(",managed load duration=");
    sb.append(pulsesDuration);
    // bytes 29, 30 calib value    
    pktIndex += 2;    
    if(currVolt == 0){ 
    	plugload.setManagedLoad((float)0.0);
    	sb.append(",managed load=");
    	sb.append(0);
    } else if(pulses == PMStatsProcessorService.SHORT_MINUS_ONE) {
    	sb.append(",managed load=-1");
    } else {			
			plugload.setManagedLoad((float)pulses);
			sb.append(",managed load=");
			sb.append(pulses);
		} 

    // byte 31 is hopper
    int isHopper = packet[pktIndex++];
    sb.append(",hopper=");
    sb.append(isHopper);
    plugload.setIsHopper(isHopper);
		    
    //group id
    int groupId = packet[pktIndex++];
    sb.append(",profile group id=");
    sb.append(groupId);
       
    //utc time in seconds
    pktIndex += 4;           
    //UTC (GMT) 100ths portion of time (0-99) 
    pktIndex += 2;            
    //Ambient Calibration level 
    pktIndex += 2;    
    // The tenths precision of the current 
    currTempPrecision = packet[pktIndex++];
    
    // one of the following: auto(101), baseline(102),  or bypass(103)
    sb.append(",current mode=");
    sb.append(packet[pktIndex++]);
    
    // current profile type See profile_types_t, values are:
    //   WEEK_DAY_PROFILE(0), WEEK_END_PROFILE(1),
    //   OVERRIDE_PROFILE(2), and EXT_OVERRIDE_PROFILE(3)
    sb.append(",current profile type=");
    sb.append(packet[pktIndex++]);
    
    //current profile period of day type or override type.
    // when curr_prof_type is WEEK_DAY_PROFILE(0) or
    //      WEEK_END_PROFILE(1), the values are:
    //   MORNING_PROFILE(0), DAY_PROFILE(1), EVENING_PROFILE(2),
    //   NIGHT_PROFILE(3),
    // when curr_prof_type is OVERRIDE_PROFILE(2) or
    //      EXT_OVERRIDE_PROFILE(3), the values are:
    //   OVERRIDE_PROFILE_NONE(0), OVERRIDE_PROFILE_1(1),
    //   OVERRIDE_PROFILE_2(2), OVERRIDE_PROFILE_3(3), or
    //   OVERRIDE_PROFILE_4(4)
    sb.append(",current period type=");
    sb.append(packet[pktIndex++]);
    
    // local motion seconds ago how long ago in secs that motion was detected locally
    //   a value of -1 means motion not detected since boot    
    System.arraycopy(packet, pktIndex, tempIntByteArr, 0, tempIntByteArr.length);
    pktIndex += 4;
    long localMotionSecsAgo = ServerUtil.intByteArrayToLong(tempIntByteArr);    
    sb.append(",local motion secs ago=");
    sb.append(localMotionSecsAgo);
    
    //motion window expire number of seconds before motion window expires (or -1 if none)    
    System.arraycopy(packet, pktIndex, tempIntByteArr, 0, tempIntByteArr.length);
    pktIndex += 4;
    sb.append(",motion window expiry=");
    sb.append(ServerUtil.intByteArrayToLong(tempIntByteArr));
    
    // unmanaged load instantaneous power in watts (on unswitched port) (-1 is error)
    System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
    pktIndex += 2;
    int unmanagedLoad = ServerUtil.byteArrayToShort(tempShortByteArr);
    sb.append(",unmanaged load=");
    sb.append(unmanagedLoad);
    plugload.setUnmanagedLoad((float)unmanagedLoad);
    
    //extended checksum of global profile
    System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
    pktIndex += 2;
    int extGlPrChecksum = ServerUtil.byteArrayToShort(tempShortByteArr);
    sb.append(",extended global profile checksum=");
    sb.append(extGlPrChecksum);
    
    //extended checksum of schedule profile
    System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
    pktIndex += 2;
    int extSchedPrChecksum = ServerUtil.byteArrayToShort(tempShortByteArr);
    sb.append(",extended scheduled profile checksum=");
    sb.append(extSchedPrChecksum);
    
    sb.append(", local advanced profile checksum=");    
    sb.append(PlugloadImpl.getInstance().getPlugloadGlobalchecksum(plugload));
    sb.append(",local Profile checksum=");
    sb.append(PlugloadImpl.getInstance().getPlugloadScheduledChecksum(plugload));
    
    // remote motion seconds ago how long ago in secs that motion was detected remotely
    //   a value of -1 means motion not detected since boot    
    System.arraycopy(packet, pktIndex, tempIntByteArr, 0, tempIntByteArr.length);
    pktIndex += 4;
    long remoteMotionSecsAgo = ServerUtil.intByteArrayToLong(tempIntByteArr);
    sb.append(",remote motion secs ago=");
    sb.append(remoteMotionSecsAgo);
    
    logger.info(sb.toString());
  
    // update the plugload
    try {
    	if (motionSecAgo < 65535) {
    		plugload.setLastOccupancySeen(new Integer(motionSecAgo));
    	} else {
    		//get the value from local and remote motion seconds ago fields
    		if(remoteMotionSecsAgo < localMotionSecsAgo) {
    			plugload.setLastOccupancySeen((int)remoteMotionSecsAgo);
    		} else if(localMotionSecsAgo < 4294967295L){
    			plugload.setLastOccupancySeen((int)localMotionSecsAgo);
    		}
    	}
    	plugload.setCurrentState(ServerUtil.getCurrentState(currState));
    	plugload.setAvgVolts((float)currVolt);
    	Float plTemp = new Float (((new Float(currTemp + "." + currTempPrecision)) * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU2());
    	plugload.setAvgTemperature(plTemp);
    	plugload.setSecGwId(gwId);
    	GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gwId);
    	if(gwInfo != null) {
    		plugload.setGateway(gwInfo.getGw());
    	}
    	plugload.setLastConnectivityAt(new Date());
    	plMgr.updateRealtimeStats(plugload);

    	// update the commission status, location if plugload is in discovered state
    	if (plugload.getState().equals(ServerConstants.PLUGLOAD_STATE_DISCOVER_STR) || 
    			plugload.getState().equals(ServerConstants.PLUGLOAD_STATE_PLACED_STR)) {
    		// update the fixture state to commissioned
    		plugload.setState(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
    		PlugloadCache.getInstance().getDevice(plugload).setDeviceState(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
    		plMgr.updateState(plugload);        
    	}
    } catch (Exception ex) {
        ex.printStackTrace();
    }

	} // end of method getCurrentState
	
}
