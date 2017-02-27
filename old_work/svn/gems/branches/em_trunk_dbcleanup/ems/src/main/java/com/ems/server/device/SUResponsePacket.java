package com.ems.server.device;

import java.util.Date;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.MotionBitRecord;
import com.ems.server.GwStatsSO;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.MotionBitRecordManager;

public class SUResponsePacket {

  private byte[] pkt = null;
  private String gwIp = null;
  private long gwId = -1;
  
  private FixtureManager fixtureMgr = null;
  private MotionBitRecordManager motionBitRecordMgr = null;
  
  private static final Logger logger = Logger.getLogger("CommLog");
  private static Logger profileLogger = Logger.getLogger("ProfileLogger");
  
  public SUResponsePacket(byte[] suPkt, String gwIp, long gwId) {
    
    this.pkt = suPkt;
    this.gwIp = gwIp;
    this.gwId = gwId;
    
    fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
    motionBitRecordMgr = (MotionBitRecordManager) SpringContext.getBean("motionBitRecordManager");
    
    
  } //end of constructor
  
  public void finalize() {
    
    pkt = null;
    gwIp = null;
    
  }
  /*
   * function to return the message type of the response packet
   */
  private int getMessageType() {
    
    int msgTypePos = ServerConstants.RES_CMD_PKT_MSG_TYPE_POS;       
    if(pkt[0] == ServerConstants.FRAME_START_MARKER) { //old packet
      msgTypePos = 2;
    }
    if(pkt[1] == 1) { //1.2 packet
      msgTypePos -= 3;
    }
    int msgType = (pkt[msgTypePos] & 0xFF);
    return msgType;
    
  } //end of method getMessageType
  
  /*
   * returns the string format of 3 byte mac address of su
   */
  private String getSUAddress() {
    
    //byte[] snapByteArr = { pkt[8], pkt[9], pkt[10] };    
    return ServerUtil.getSnapAddr(pkt[8], pkt[9], pkt[10]); 
    
  } //end of method getSUAddress
  
  /* 
   * function to return the data part of the packet
   */
  private byte[] getDataPacket() {
    
    int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
    if(pkt[0] == ServerConstants.FRAME_START_MARKER) { //old packet
      index = 3;
    }
    int pktLen = pkt.length;
    byte[] data = new byte[pktLen - index];
    System.arraycopy(pkt, index, data, 0, data.length);
    return data;
      
  } //end of method getDataPacket
  
  private long getTransactionId() {
    
    byte[] seqNoArr = new byte[4];
    System.arraycopy(pkt, 4, seqNoArr, 0, seqNoArr.length);
    long seqNo = ServerUtil.byteArrayToInt(seqNoArr);
    return seqNo;
    
  } //end of method getTransactionId
  
  //this is the function to parse the packet received from SU
  public void processResponse() {
        
    String snapAddr = getSUAddress();
    int msgType = getMessageType();    
    //when the SU is discovered first time, fixture is not there in the database
    //so call discovery class without checking for fixture object
    if(msgType == ServerConstants.SU_DISCOVERY_TYPE) {
      DiscoverySO.getInstance().discoveryData(snapAddr, pkt, gwIp);
      pkt = null;
      return;
    }
    Fixture fixture =  FixtureCache.getInstance().getDeviceFixture(snapAddr);
    if(fixture == null) {
      logger.error(snapAddr + ": There is no fixture, ignoring the node pkt - " + ServerUtil.getLogPacket(pkt));	
      pkt = null;
      return;
    }
    fixture.setLastConnectivityAt(new Date());
    long fixtureId = fixture.getId();    
    switch (msgType){      	
    	case ServerConstants.REPT_PM_DATA_MSG_TYPE:	  	  
      	  PerfSO.getInstance().updateStatsFromZigbee(fixture, pkt, gwId);      	  
      	  break;
      	case ServerConstants.REPT_MOTION_MSG_TYPE:
      	  DiscoverySO.getInstance().motionEvent(fixture, pkt);
      	  break;
      	case ServerConstants.RESEND_REQUEST:
      	  ImageUpgradeSO.getInstance().missingPacketRequest(fixture, pkt, gwId);
      	  break;
      	case ServerConstants.ABORT_ISP_OPCODE:
      	  ImageUpgradeSO.getInstance().cancelFileUpload(fixture, pkt, gwId);
      	  break;
      	case ServerConstants.ACK_TO_MSG:
      	  int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
      	  if(pkt[0] == ServerConstants.FRAME_START_MARKER) { //old packet
      	    msgStartPos = 3;
      	  }
      	  if ((pkt[msgStartPos] & 0xFF) == ServerConstants.ISP_INIT_ACK_OPCODE) {		
      	    ImageUpgradeSO.getInstance().ackImageUploadStart(fixture, gwId);
      	  } else {
      	    int ackToMsg = pkt[msgStartPos] & 0xFF;
      	    if(logger.isDebugEnabled()) {
      	      logger.debug(fixture.getId() + ": ack packet(" + ackToMsg + ") -- " + 
      		ServerUtil.getLogPacket(pkt));
      	    }
      	    //ServerMain.getInstance().ackDeviceMessage(fixture, pkt, gwId);
      	    CommandScheduler.getInstance().gotAck(pkt, fixture, gwId);      	    
      	    switch(ackToMsg) {
      	    case ServerConstants.SU_SET_WIRELESS_CMD:
      	      //ack came for the change wireless params
      	      DeviceServiceImpl.getInstance().applyWireless(fixture);
      	      //change the commission state = COMMISSION_STATUS_WIRELESS
      	      fixtureMgr.updateCommissionStatus(fixtureId, 
      		  ServerConstants.COMMISSION_STATUS_WIRELESS);
      	      break;
      	    case ServerConstants.SU_APPLY_WIRELESS_CMD:
      	      //ack came for the applying the wireless params
      	      DeviceServiceImpl.getInstance().suWirelessChangeAckStatus(fixture, true);
      	      GatewayImpl.getInstance().receivedSuWirelessChangeAck(fixture);
      	      break;
      	    case ServerConstants.SU_SET_APPLY_WIRELESS_CMD:
      	      DiscoverySO.getInstance().receivedHopperWirelessParamsAck(fixture.getId());
      	      GwStatsSO.getInstance().receivedHopperWirelessParamsAck(fixture.getId());
      	      break;
      	    case ServerConstants.SET_LIGHT_LEVEL_MSG_TYPE:
      	      //may be bacnet service is waiting for this. inform
      	      BacnetService.getInstance().receivedDimAck(fixture);
      	      break;
      	    case ServerConstants.SU_CMD_JOIN_GRP:
      	    	DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(fixture, true);
      	    	logger.info(fixture.getId() + " joined group");
      	    	break;
      	    case ServerConstants.SU_CMD_LEAVE_GRP:
      	    	DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(fixture, true);
      	    	logger.info(fixture.getId() + " left group");
      	    	break;
      	    case ServerConstants.SU_CMD_REQ_REST_GRP:
      	    	DeviceServiceImpl.getInstance().suWirelessGrpChangeAckStatus(fixture, true);
      	    	logger.info(fixture.getId() + " rest group successfull");
      	    	break;
            case ServerConstants.CMD_SET_SWITCH_GRP_PARMS:
                DeviceServiceImpl.getInstance().suWirelessGrpConfigChangeAckStatus(fixture, true);
                logger.info(fixture.getId() + " switch group sync'd");
                break;
            case ServerConstants.CMD_SET_SWITCH_GRP_WDS:
                DeviceServiceImpl.getInstance().suWirelessGrpConfigChangeAckStatus(fixture, true);
                logger.info(fixture.getId() + " wds group sync'd");
                break;
            case ServerConstants.CMD_SWITCH_GRP_DEL_WDS:
                DeviceServiceImpl.getInstance().suWirelessGrpConfigChangeAckStatus(fixture, true);
                logger.info(fixture.getId() + " wds removed");
                break;
      	    default:
      	      break;
      	    }      	    
      	  }      	  
      	  break;
        case ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE:
          if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(snapAddr + " received profile download msg (" + msgType + ")\n" + ServerUtil.getLogPacket(pkt));
          }
          FixtureCache.getInstance().getProfileFromSU(fixtureId, pkt);
          break;
      	case ServerConstants.GET_STATUS_MSG_TYPE:
      	  ServerMain.getInstance().getCurrentState(fixture, pkt, gwId);      	  
      	  break;	   
      	case ServerConstants.GET_VERSION_MSG_TYPE:
      	  ImageUpgradeSO.getInstance().currentNodeVersion(fixture, pkt, gwId);    	  
      	  break;
      	case ServerConstants.NODE_INFO_MSG_TYPE:
      	  DeviceServiceImpl.getInstance().nodeBootInfo(fixture, pkt, gwId);      	  
      	  break;
      	case ServerConstants.SU_RDB_MSG_TYPE:
      	  DeviceServiceImpl.getInstance().logRDBMessages(snapAddr, pkt);
      	  break;
      	case ServerConstants.SU_EVENT_MSG_TYPE:
      	  DeviceServiceImpl.getInstance().handleSUEvent(fixtureId, pkt);
      	  break;      	
        case ServerConstants.SU_TRIGGER_MOTION_BIT_MSG_TYPE:
            long currTime = System.currentTimeMillis();
            logger.debug("Processing motion bit pkt: ");
            int seqNo = ServerUtil.extractIntFromByteArray(pkt, 4);
            DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
            if (device != null && device.getLastMBitSeqNo() == seqNo) {
                // duplicate packet ignore it
                logger.debug(fixture.getId() + " seq: " + seqNo + ", duplicate motion bit received! " + (System.currentTimeMillis() - currTime));
                pkt = null;
                fixture = null;
                return;
            } else {
                byte motion_bit_info = (byte)(pkt[12] & 0xFF);
                int bitmask = pkt[13] & 0xFF;
                
                device.setLastMBitSeqNo(seqNo);
                logger.debug(fixture.getId() + " seq: " + seqNo + ", motion_bit_info: " + (motion_bit_info & 0xFF) + ", mask: " + (bitmask & 0xFF) + ", motion bit recived " + ServerUtil.getLogPacket(pkt));
                MotionBitRecord mbr = new MotionBitRecord();
                mbr.setFixtureId(fixture.getId());
                mbr.setCaptureAt(new Date());
                mbr.setMotionBitsLevel(((motion_bit_info >> 4) & 0x0f));
                //add the frequency
                mbr.setMotionBitsFrequency(((motion_bit_info) &  0x0F));
                if (bitmask > 0) {
                    int pos = 14;
                    int length = bitmask * 4;
                    StringBuffer sb = new StringBuffer();
                    for(int i = 0; i < length; i++) {
                        sb.append(String.format("%02x", pkt[pos]));
                        pos++;
                    }
                    mbr.setMotionBits(sb.toString());
                    sb = null;
                }
                motionBitRecordMgr.save(mbr);
                logger.debug(fixture.getId() + " seq: " + seqNo + ", Processed motion bit pkt: " + (System.currentTimeMillis() - currTime));
            }
            //DeviceServiceImpl.getInstance().suWirelessMotionBitAckStatus(fixture, true);
            break;
      	default:
      	  logger.error(snapAddr + ": message unknown - " + msgType);
      	  break;
    }
    pkt = null;
    fixture = null;
  
  } //end of method processResponse  
  
} //end of class SUResponsePacket
