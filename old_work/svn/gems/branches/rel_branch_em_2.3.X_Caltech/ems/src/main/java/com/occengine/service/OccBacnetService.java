package com.occengine.service;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.dao.EnergyConsumptionDao;
import com.ems.dao.FixtureDao;
import com.ems.model.Fixture;
import com.ems.server.ServerMain;
import com.ems.server.device.BacnetService;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.occengine.OccupancyEngine;
import com.occengine.dao.ZoneDao;
import com.occengine.model.Zone;
import com.occengine.utils.OccUtil;

public class OccBacnetService {

	private static OccBacnetService instance = null;

  private ZoneDao zoneDao = null;
  private FixtureDao fixtureDao = null;
  private EnergyConsumptionDao energyConsumptionDao = null;
  
   static final Logger logger = Logger.getLogger("BacnetLog");

  public static final int BACNET_CMD_TYPE_REQ = 1;
  public static final int BACNET_CMD_TYPE_RESP = 2;

  public static final int BACNET_MSGTYPE_ZONE_DISCOVER = 0x61;  
  public static final int BACNET_MSGTYPE_ZONE_DATA = 0x62;
  public static final int BACNET_MSGTYPE_ZONE_STATUS = 0x63;
  public static final int BACNET_MSGTYPE_BMS_CLIENT_INFO = 0x64;

  public static final int BACNET_CMD_VER1 = 1;
  public static final int BACNET_HEADER_LEN = 12;

  // message format
  public static final int BACNET_MSG_TYPE_POS = 0; // 1 byte
  public static final int BACNET_MSG_VERSION_POS = 1; // 1 byte
  public static final int BACNET_MSG_LEN_POS = 2; // 2 bytes
  public static final int BACNET_MSG_SEQ_NO_POS = 4; // 4 bytes
  public static final int BACNET_MSG_CMD_POS = 8; // 1 byte
  public static final int BACNET_REQ_MSG_FLAGS_POS = 9; // 1byte
  public static final int BACNET_MSG_RESERVED_POS = 10; // 2 bytes
  public static final int BACNET_DATA_POS = 12;

  // response message format
  public static final int BACNET_RES_MSG_RESULT_POS = 9; // 1byte

  public static final byte BACNET_RES_SUCCESS = 0;
  public static final byte BACNET_RES_NO_DEVICES = 1;
  public static final byte BACNET_RES_PARTIAL = 2;
  public static final byte BACNET_RES_DEVICE_NOT_FOUND = 3;
  public static final byte BACNET_RES_DIM_TIMEOUT = 4;
  
  /**
 * 
 */
  private OccBacnetService() {

  	// TODO Auto-generated constructor stub     
  	if(zoneDao == null) {
  		zoneDao = (ZoneDao) SpringContext.getBean("zoneDao");
  	}
  	if(fixtureDao == null) {
  		fixtureDao = (FixtureDao)SpringContext.getBean("fixtureDao");
  	}
  	if(energyConsumptionDao == null) {
  		energyConsumptionDao = (EnergyConsumptionDao)SpringContext.getBean("energyConsumptionDao");
  	}
      
  } // end of constructor BacnetService

  public static OccBacnetService getInstance() {

  	if (instance == null) {
  		synchronized (OccBacnetService.class) {
  			if (instance == null) {
  				instance = new OccBacnetService();
  			}
  		}
  	}
  	return instance;

  } // end of method getInstance
  
  private static int nextSeq = 1;
  
  public static int getNextSeqNo() {

    // TODO: Use timestamp to generate really unique TXID and maybe serially as well;
    if (nextSeq == 1) {
    	Random randomGenerator = new Random();
    	nextSeq = randomGenerator.nextInt(30000);
    }
    return nextSeq++ % 30000 + 1;

  } // end of method getNextSeqNo

  private void fillBacnetHeader(byte[] bacnetResp, byte msgType, byte result) {

  	bacnetResp[BACNET_MSG_TYPE_POS] = BACNET_CMD_TYPE_RESP;
  	bacnetResp[BACNET_MSG_VERSION_POS] = BACNET_CMD_VER1; // version
  	// length 2 bytes
  	OccUtil.fillShortInByteArray(bacnetResp.length, bacnetResp, BACNET_MSG_LEN_POS);
  	// transaction id 4 bytes
  	OccUtil.fillIntInByteArray(getNextSeqNo(), bacnetResp, BACNET_MSG_SEQ_NO_POS);
  	bacnetResp[BACNET_MSG_CMD_POS] = msgType; // command
  	bacnetResp[BACNET_RES_MSG_RESULT_POS] = result; // result
  	// reserved 2 bytes
  	OccUtil.fillShortInByteArray(0, bacnetResp, BACNET_MSG_RESERVED_POS);

  } // end of method fillBacnetHeader
  
  public void processBacnetZoneDiscover(byte[] bacnetPkt) {
  	
  	byte[] responsePkt = null;
  	if(logger.isInfoEnabled()) {
    	logger.info("bacnet zone discover packet - " + OccUtil.getLogPacket(bacnetPkt));
    }
    List<Zone> zoneList = zoneDao.getAllZones();
    int noOfZones = 0;
    if (zoneList != null) {
    	noOfZones = zoneList.size();
    }
    responsePkt = new byte[BACNET_HEADER_LEN + 2 + noOfZones * 5];
    byte result = BACNET_RES_SUCCESS;
    if (noOfZones == 0) {
    	result = BACNET_RES_NO_DEVICES;
    }
    fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_ZONE_DISCOVER, result);
    if(logger.isInfoEnabled()) {
    	logger.info("bacnet: no. of zones - " + noOfZones);
    }
    //add no. of devices to response packet
    OccUtil.fillShortInByteArray(noOfZones, responsePkt, BACNET_DATA_POS);
    // add zones to response packet
    if (noOfZones != 0) {
    	Iterator<Zone> zoneIter = zoneList.iterator();
    	int i = BACNET_DATA_POS + 2;
    	while (zoneIter.hasNext()) {
    		Zone zone = zoneIter.next();
    		OccUtil.fillLongAsIntInByteArray(zone.getId(), responsePkt, i);
    		i += 4;          		
    		responsePkt[i++] = 0; //zone.getStatus().byteValue();          		                
    	}
    }
    BacnetService.getInstance().sendBacnetPacket(responsePkt);
  	
  } //end of method processBacnetZoneDiscover
  
  public void processBacnetZoneData(byte[] bacnetPkt) {
  	
  	 if(logger.isInfoEnabled()) {
     	logger.info("bacnet zone data - " + OccUtil.getLogPacket(bacnetPkt));
     }
     int pktPos = BACNET_DATA_POS + 2;
     long zoneId = OccUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
     pktPos += 4;
     sendZoneDataResponse(zoneId);
     
  } //end of method processBacnetZoneData
  
  public void processBmsClientInfo(byte[] bacnetPkt) {
  	
  	if(logger.isInfoEnabled()) {
    	logger.info("bacnet bms client info - " + OccUtil.getLogPacket(bacnetPkt));
    }
    int pktPos = BACNET_DATA_POS;
    long zoneId = OccUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
    pktPos += 4;
    byte rglValue = bacnetPkt[pktPos++];
    if(ServerMain.enableOccEngine) {
    	OccupancyEngine.getInstance().updateBmsClientInfo(zoneId, rglValue);
    }
        
  } //end of method processBmsClientInfo
  
  public void processBacnetZoneStatus(byte[] bacnetPkt) {
  	
  	if(logger.isInfoEnabled()) {
    	logger.info("bacnet zone status - " + OccUtil.getLogPacket(bacnetPkt));
    }
    int pktPos = BACNET_DATA_POS + 2;
    long zoneId = OccUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
    pktPos += 4;
    sendZoneStatusResponse(zoneId);
  	
  } //end of method processBacnetZoneStatus
       
  // this is the function to parse the packet received from the bacnet
  public void processBacnetPacket(byte[] bacnetPkt) {
  	
  	byte[] responsePkt = null;
  	try {
  		// parse bacnet packet
  		int msgType = (bacnetPkt[BACNET_MSG_CMD_POS] & 0xFF);
  		switch (msgType) {
  			case BACNET_MSGTYPE_ZONE_DISCOVER:
  				processBacnetZoneDiscover(bacnetPkt);
          break;
  			case BACNET_MSGTYPE_ZONE_DATA:
  				processBacnetZoneData(bacnetPkt);
          break;  
  			case BACNET_MSGTYPE_ZONE_STATUS:
  				processBacnetZoneStatus(bacnetPkt);          
          break; 
  			default:
          if(logger.isInfoEnabled()) {
          	logger.info("bacnet unknown message - " + msgType);
          }
          break;
  		}
  	} catch (Exception e) {
  		e.printStackTrace();
  	} finally {
  		bacnetPkt = null;
  		responsePkt = null;
  	}

  } // end of method processBacnetPacket
  
  private void sendZoneStatusResponse(long zoneId) {
  	
  	int statusPos = 0;
  	byte[] statusPkt = new byte[7];
  	// add no. of zones to the response packet
    OccUtil.fillShortInByteArray(1, statusPkt, statusPos);
    statusPos += 2;
    Zone zone = null;
    try {
    	zone = zoneDao.getZoneById(zoneId);
    }
    catch(Exception e) {	
    	e.printStackTrace();
    }    
    byte[] detailsPkt = null;
    byte result = BACNET_RES_SUCCESS;
    if (zone == null) {	
    	statusPkt[statusPos++] = BACNET_RES_DEVICE_NOT_FOUND;    
    	detailsPkt = new byte[0];
    	result = BACNET_RES_NO_DEVICES;
    } else {
    	//result 1 byte
    	statusPkt[statusPos++] = BACNET_RES_SUCCESS;   	
    }    
    OccUtil.fillLongAsIntInByteArray(zoneId, statusPkt, statusPos);
    statusPos += 4;
  	   
    if(zone != null) { 
    	detailsPkt = new byte[32];
    	int detailsPos = 0;
    	Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1980);
		long tempTime = cal.getTimeInMillis()/1000;
    	//last time stamp 4 bytes in sec
    	long timestamp = zone.getLastStatusUpdateTime() != null ? zone.getLastStatusUpdateTime().getTime() /1000 : tempTime; 
    	OccUtil.fillLongAsIntInByteArray(timestamp, detailsPkt, detailsPos);
    	detailsPos += 4;
    	//occupancy 1 byte
    	detailsPkt[detailsPos++] = zone.getOccStatus() != null ? zone.getOccStatus().byteValue() : -1;
    	//setback 1 byte
    	detailsPkt[detailsPos++] = zone.getSetback() != null ? zone.getSetback().byteValue(): -1;
    	//fan speed 1 byte
    	detailsPkt[detailsPos++] = zone.getFanSpeed() != null ? zone.getFanSpeed().byteValue() : -1;
    	//avg temperature 2 bytes    	
    	short tempTemp = zone.getAvgTemperature() != null ? (short)(zone.getAvgTemperature() * 100): -1;
    	OccUtil.fillShortInByteArray(tempTemp, detailsPkt, detailsPos);
    	detailsPos += 2;
    	//min temperature 2 bytes
    	tempTemp = zone.getMinTemperature() != null ? (short)(zone.getMinTemperature() * 100) : -1;
    	OccUtil.fillShortInByteArray(tempTemp, detailsPkt, detailsPos);
    	detailsPos += 2;
    	//max temperature 2 bytes
    	tempTemp = zone.getMaxTemperature() != null ? (short)(zone.getMaxTemperature() * 100) : -1;
    	OccUtil.fillShortInByteArray(tempTemp, detailsPkt, detailsPos);
    	detailsPos += 2;
    	//heart beat 1 byte
    	detailsPkt[detailsPos++] = zone.getHeartbeat() != null ? zone.getHeartbeat().byteValue() : -1;
    	//setback start time 4 bytes
    	long startTime = zone.getSetbackStartTime() != null ? zone.getSetbackStartTime().getTime() / 1000 : tempTime;
    	OccUtil.fillLongAsIntInByteArray(startTime, detailsPkt, detailsPos);
    	detailsPos += 4;
    	//setback end time 4 bytes
    	long endTime = zone.getSetbackEndTime() != null ? zone.getSetbackEndTime().getTime() / 1000 : tempTime;
    	OccUtil.fillLongAsIntInByteArray(endTime, detailsPkt, detailsPos);
    	detailsPos += 4;    	
    	//bms client heart beat time
    	long bmsClientHbTime = 0;
    	byte clientSetback = 0;
    	if(zone.getBmsClientLastHbTime() != null) {
    		bmsClientHbTime = zone.getBmsClientLastHbTime().getTime() / 1000;
      	clientSetback = zone.getLastBmsClientSetback() != null ? zone.getLastBmsClientSetback().byteValue() : 0;      	
    	}
    	OccUtil.fillLongAsIntInByteArray(bmsClientHbTime, detailsPkt, detailsPos);
    	detailsPos += 4;
    	detailsPkt[detailsPos++] = clientSetback;
    	long avgenergy = energyConsumptionDao.lastHourZoneEnergy(zoneId);
    	OccUtil.fillLongAsIntInByteArray(avgenergy, detailsPkt, detailsPos);
    	detailsPos += 4;
    	detailsPkt[detailsPos++] = fixtureDao.avgDimLevelByZone(zoneId).byteValue();
    }
    int fillPos = BACNET_DATA_POS;   
    byte[] responsePkt = new byte[BACNET_HEADER_LEN + statusPkt.length + detailsPkt.length];
    System.arraycopy(statusPkt, 0, responsePkt, fillPos, statusPkt.length);
    fillPos += statusPkt.length;
    System.arraycopy(detailsPkt, 0, responsePkt, fillPos, detailsPkt.length);
    fillPos += detailsPkt.length;        
    fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_ZONE_STATUS, result);
    BacnetService.getInstance().sendBacnetPacket(responsePkt);
    
  } //end of method sendZoneStatusResponse
  
  private void sendZoneDataResponse(long zoneId) {
       
  	int statusPos = 0;
  	byte[] statusPkt = new byte[7];
  	// add no. of zones to the response packet
    OccUtil.fillShortInByteArray(1, statusPkt, statusPos);
    statusPos += 2;
    Zone zone = null;
    try {
    	zone = zoneDao.getZoneById(zoneId);
    }
    catch(Exception e) {	
    	e.printStackTrace();
    }    
    byte[] detailsPkt = null;
    byte result = BACNET_RES_SUCCESS;
    if (zone == null) {	
    	statusPkt[statusPos++] = BACNET_RES_DEVICE_NOT_FOUND;    
    	detailsPkt = new byte[0];
    	result = BACNET_RES_NO_DEVICES;
    } else {
    	//result 1 byte
    	statusPkt[statusPos++] = BACNET_RES_SUCCESS;   	
    }    
    OccUtil.fillLongAsIntInByteArray(zoneId, statusPkt, statusPos);
    statusPos += 4;
  	   
    if(zone != null) {    	
    	//zone name
      String zoneName = zone.getName();
      byte[] zoneNameArr = zoneName.getBytes();          
      int zoneNameLen = zoneNameArr.length;
      
      //location
      int locationLen = 0;      
      detailsPkt = new byte[zoneNameLen + locationLen + 5]; //two length fields, two null terminating characters and status           
      int detailsPos = 0;      
      
      if(zoneNameLen > 127) {
      	zoneNameLen = zoneNameLen - 256;
      }
      if(locationLen > 127) {
      	locationLen = locationLen - 256;
      }
      
      //zone state
      detailsPkt[detailsPos++] = 0;
      //filling zone name
      detailsPkt[detailsPos++] = (byte)zoneNameLen;
      System.arraycopy(zoneNameArr, 0, detailsPkt, detailsPos, zoneNameLen);
      detailsPos += zoneNameArr.length;
      detailsPkt[detailsPos++] = 0;
            
      //filling zone location
      detailsPkt[detailsPos++] = (byte)locationLen;
      detailsPkt[detailsPos++] = 0;
           
    }
    int fillPos = BACNET_DATA_POS;   
    byte[] responsePkt = new byte[BACNET_HEADER_LEN + statusPkt.length + detailsPkt.length];
    System.arraycopy(statusPkt, 0, responsePkt, fillPos, statusPkt.length);
    fillPos += statusPkt.length;
    System.arraycopy(detailsPkt, 0, responsePkt, fillPos, detailsPkt.length);
    fillPos += detailsPkt.length;        
    fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_ZONE_DATA, result);
    BacnetService.getInstance().sendBacnetPacket(responsePkt);  
    
  } //end of method sendZoneDetailsResponse
  
  public void absoluteDimZone(int percentage, Long zone_id, int time) {
	  int[] fixtureList = fixtureDao.loadFixtureByZoneId(zone_id);
	  if(fixtureList.length == 1 && fixtureList[0] == -1) {
	  }
	  else {
		  DeviceServiceImpl deviceServiceImpl = DeviceServiceImpl.getInstance();
		  deviceServiceImpl.absoluteDimFixtures(fixtureList, percentage, time);
		  ServerUtil.sleep(2);
		  deviceServiceImpl.getCurrentState(fixtureList);
		  ServerUtil.sleepMilli(50);
		  deviceServiceImpl.getCurrentState(fixtureList);
	  }
  }
   
} // end of class BacnetService

