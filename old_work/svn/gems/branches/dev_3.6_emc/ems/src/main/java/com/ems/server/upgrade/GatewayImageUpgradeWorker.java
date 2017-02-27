/**
 * 
 */
package com.ems.server.upgrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.server.ServerConstants;
import com.ems.server.device.GatewayImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.GatewayManager;

/**
 * This is a legacy class that was used when the connection with the gateway was in UDP.
 * No current (2015) gateway should use this code.
 * @author Sreedhar
 *
 */
public class GatewayImageUpgradeWorker extends Thread {
    
  private static final Logger logger = Logger.getLogger("ImageUpgrade");
  
  private GatewayManager gwMgr = null;
  private EventsAndFaultManager eventMgr = null;
  private FirmwareUpgradeManager firmUpMgr = null;
  
  private Long[] devices;
  private String fileName;
  private ImageUpgradeDBJob dbJob = null;
  private int noOfGwsSuccess = 0;
    
  private int appType;
  private byte[] gwFileArray = null; 
  private boolean inProgress = false;
  private boolean bucket8kFinished = false; //bucket is 8k
  private int lastBucket = 0;
  
  private ArrayList<Long> gwAckList = new ArrayList<Long>();
  private ArrayList<Long> imgUpPendingGwList = new ArrayList<Long>();
  
  //it is a list of missing offset (multiple of 64)
  private LinkedList<Integer> missingOffsetList = new LinkedList<Integer>();
  
  private Map<Long, GatewayImgUpgrDetails> gwInProgressMap = 
    Collections.synchronizedMap(new HashMap<Long, GatewayImgUpgrDetails>());
         
  class GatewayImgUpgrDetails {
    
    int targetVersion;
    int appUpgraded;
    String status;
    String fileName;
    Date StartTime;
    Date endTime;
    int noOfAttempts;
    
    boolean ackRcvd;
    boolean imgPending;
    
  } //end of class GatewayImgUpgrDetails

  /**
   * 
   */
  public GatewayImageUpgradeWorker(ImageUpgradeDBJob job) {
    
    this.devices = job.getDeviceIds();
    this.fileName = job.getImageName();
    this.dbJob = job;
        
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    firmUpMgr = (FirmwareUpgradeManager)SpringContext.getBean("firmwareUpgradeManager");
    
  } //end of constructor
  
  public void run() {
    
    //update the job status in database as in progress
    dbJob.setStatus(ServerConstants.IMG_UP_STATUS_INPROGRESS);
    dbJob.setStartTime(new Date());
    firmUpMgr.save(dbJob);
        
    gwStartImpageUpload(devices, fileName);
    
    //update the status in database
    dbJob.setStatus(ServerConstants.IMG_UP_STATUS_FAIL);
    if(devices.length == noOfGwsSuccess) {
      dbJob.setStatus(ServerConstants.IMG_UP_STATUS_SUCCESS);
    } else if(noOfGwsSuccess < devices.length){
      dbJob.setStatus(ServerConstants.IMG_UP_STATUS_PARTIAL);
    }
    dbJob.setEndTime(new Date());
    firmUpMgr.save(dbJob);
    
  } //end of method run
     
  private void gwStartImpageUpload(Long[] gwIds, String fileName) {

    ImageUpgradeSO.readUpgradeProperties();
    
    logger.info("gateway upgrade request on " + gwIds.length + " using " + 
	fileName);    
    //this is based on the file naming convention being followed
    int targetBuildNo = Integer.parseInt(fileName.substring(0, 
	fileName.indexOf("_")));

    appType = ServerConstants.ISP_APP2_INIT_OPCODE;
    if(fileName.toLowerCase().indexOf("gw_firm") != -1) {
      //app1 upgrade
      appType = ServerConstants.ISP_APP1_INIT_OPCODE;
    }
    String filePath = ImageUpgradeSO.getInstance().getImageLocation() + 
    	fileName;    
    try {
      gwFileArray = null;
      gwFileArray = ImageUpgradeSO.getBytesFromFile(new File(filePath));     
    }
    catch(Exception e) {
      e.printStackTrace();
    }  
    if(gwFileArray == null) {
      logger.error("failed to read the gateway file");
      return;
    }
    
    int noOfGws = gwIds.length;    
    long startTime = System.currentTimeMillis();
           
    for(int i = 0; i < noOfGws; i++) {     
      Gateway gw = gwMgr.loadGateway((long)devices[i]);   
      if(gw == null) {
	logger.info(devices[i] + ": There is no gateway");
	continue;
      }
      GatewayImgUpgrDetails details = new GatewayImgUpgrDetails();
      details.appUpgraded = appType;
      details.targetVersion = targetBuildNo;
      details.status = ServerConstants.IMG_UP_STATUS_SCHEDULED;
      details.fileName = fileName;
      gwInProgressMap.put(gw.getId(), details); 
      while(true) {
	synchronized(ImageUpgradeSO.gwThreadLock) {
	  if(!ImageUpgradeSO.getInstance().gwUpgrMap.containsKey(gwIds[i]) &&
  		  !ImageUpgradeSO.getInstance().gwXferUpgrMap.containsKey(gw.getId()) &&
	      !ImageUpgradeSO.getInstance().gwFixtUpgrMap.containsKey(gwIds[i]) &&
	      !ImageUpgradeSO.getInstance().gwWdsUpgrMap.containsKey(gwIds[i])) {
	    ImageUpgradeSO.getInstance().gwUpgrMap.put((long)gwIds[i], this);
	    break;
	  }
	}
	logger.info(gwIds[i] + ": other image upgrade job is running on this gw");
	ServerUtil.sleep(30);
      }
      details.StartTime = new Date();
      details.noOfAttempts = 1;
      //send init packet to the gateway    
      gwStartImageUpload(gw, appType);
      ImageUpgradeSO.getInstance().gwUpgrMap.remove((long)gwIds[i]);
    }
    for(int i = 0; i < noOfGws; i++) {        
      GatewayImgUpgrDetails details = gwInProgressMap.get(new Long(gwIds[i]));
      if(details != null && details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
	//image upgrade didn't start on this fixture
	logger.error(gwIds[i] + ": gw Image upgrade didn't start");
	gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_FAIL);
	String description = "gw Image upgrade of Application with image " + fileName + " didn't start";
	if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
	  description = "gw Image upgrade of Firmware with image " + fileName + " didn't start";
	}
	Gateway gw = gwMgr.loadGateway((long)gwIds[i]);
	eventMgr.addEvent(gw, description, 
		  EventsAndFault.GW_IMG_UP_STR);
	gwInProgressMap.remove(gwIds[i]);	
	//update the gateway image upgrade status in db     
	firmUpMgr.finishDeviceUpgrade(dbJob.getId(), (long)gwIds[i], 
	    ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description, "");
      }      
    }   
    logger.info("Time taken to upload to all gateways -- " + 
	(System.currentTimeMillis() - startTime));
    
  } //end of method gwStartImpageUpload
  
  private void gwStartImageUpload(Gateway gateway, int imgType) {
    
    //update the upgrade status of gateways as in progress
    Long[] deviceIds = { gateway.getId() };
    firmUpMgr.startDeviceUpgrade(dbJob.getId(), deviceIds);
    gwMgr.setImageUpgradeStatus(gateway.getId(), ServerConstants.IMG_UP_STATUS_INPROGRESS);
    
    //send init packet to the fixture    
    missingOffsetList.clear();      
    int retries = 3;
    ImageUpgradeSO.readUpgradeProperties();
    logger.debug(gateway.getId() + ": gw file size in sendStartImageUpload- " + 
	gwFileArray.length);
    //Gateway gateway = gwMgr.loadGateway(gwId);    
    byte[] initPacket = gwInitPacket(gwFileArray.length, imgType);   
    gwAckList.clear();
    while(--retries >= 0) {        
      try {	
	GatewayImpl.getInstance().sendGwPkt(ServerConstants.IMAGE_UPGRADE_MSG_TYPE, gateway, 
	    initPacket, false);	
      }
      catch(Exception ex) {
	ex.printStackTrace();
      }
      //wait for some time so that gateway gets the init request
      ServerUtil.sleep(15);
      if(inProgress) {
	//ack is received
	break;
      }
    }
    while(inProgress) {
      ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY);
    }
    
  } //end of method gwStartImageUpload

  private byte[] gwInitPacket(int fileSize, int imageType) {
  
    int i = 0;        
    //image type(1), packet size(4), file size(4)
    byte[] dataPacket = new byte[9];
    dataPacket[i++] = (byte)imageType;    
    dataPacket[i++] = 0x00;
    dataPacket[i++] = 0x00;
    dataPacket[i++] = 0x00;
    if(ImageUpgradeSO.IMG_UP_PLC_PKT_SIZE > 127) {
      dataPacket[i++] = (byte)(ImageUpgradeSO.IMG_UP_PLC_PKT_SIZE - 256);
    } else {
      dataPacket[i++] = (byte)ImageUpgradeSO.IMG_UP_PLC_PKT_SIZE;
    }    
    byte[] sizeArray = ServerUtil.intToByteArray(fileSize);
    System.arraycopy(sizeArray, 0, dataPacket, i, sizeArray.length); // Add the file size MSB first
      
    return dataPacket;
    
  } //end of method gwInitPacket
  
  public void gwAckImageUploadStart(final Gateway gateway) {
    
    final long gwId = gateway.getId();
    logger.debug(gateway.getIpAddress() + ": got ack for gw image upload start");    
    if(inProgress) {
      //already file transfer is in progress so ignore
      logger.debug(gwId + ":gw file transfer is already in progress. so ignoring the ack");
      return;
    }
    GatewayImgUpgrDetails details = gwInProgressMap.get(gwId);    
    if(details != null && details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
      details.status = ServerConstants.IMG_UP_STATUS_INPROGRESS;
      String description = "Image upgrade of Application with image " + details.fileName + " started";
      if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
        description = "Image upgrade of Firmware with image " + details.fileName + " started";
      }
      eventMgr.addEvent(gateway, description, 
  	  EventsAndFault.GW_IMG_UP_STR); 
    }    
    if(!imgUpPendingGwList.contains(new Long(gwId))) {
      imgUpPendingGwList.add(new Long(gwId));
      gwAckList.remove(gwId);
    }
    inProgress = true;
    logger.info("no. of pending gws- " + imgUpPendingGwList.size());
    new Thread() {
      public void run() {
	//ServerUtil.sleep(5); //5 sec sleep before file transfer is started
	long startTime = System.currentTimeMillis();
	int dataSize = ImageUpgradeSO.IMG_UP_PLC_PKT_SIZE;
	int fileSize = gwFileArray.length;
	//int fileSize = 8 * 1024;
	if(fileSize < dataSize) {
	  dataSize = fileSize;
	}    
	int offset = 0;    
	byte oosPkt[] = null;
	int sectorSize = 0;
	bucket8kFinished = false;
	lastBucket = -1;	
	gwAckList.addAll(imgUpPendingGwList);	
	while(offset <= fileSize && inProgress) {
	  if(sectorSize > 8192) { //8k boundary is crossed
	    offset = offset - (offset % 8192);
	  }
	  int remainingSize = fileSize - offset;	  
	  if(remainingSize <= 0) {
	    //at the end also,  missing packets
	    gwSendMissingPkts(gateway, dataSize);
	    logger.info("Time taken to upload on gateway " + gwId + " is " +
		(System.currentTimeMillis() - startTime));
	    inProgress = false;
	    int noOfFailedGws = gwAckList.size();
	    for(int i = 0; i < noOfFailedGws; i++) {	      
	      gwMgr.setImageUpgradeStatus(gwId, ServerConstants.IMG_UP_STATUS_FAIL);
	      imgUpPendingGwList.remove(gwAckList.get(i));
	      GatewayImgUpgrDetails details = gwInProgressMap.get(gwId);
	      if(details != null) {
		String description = "Image upgrade of Application with image " + details.fileName + " failed";
		if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
		  description = "Image upgrade of Firmware with image " + details.fileName + " failed";
		}
		eventMgr.addEvent(gateway, description, 
		    EventsAndFault.GW_IMG_UP_STR); 
		gwInProgressMap.remove(gwId);
		//update the gateway image upgrade status in db     
		firmUpMgr.finishDeviceUpgrade(dbJob.getId(), gateway.getId(), 
		    ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description, "");
	      }
	    }
	    return;
	  }
	    
	  //after 8k wait for missing packet requests for 2 sec and proceed     
	  if(offset > 0 && offset % (8 * 1024) == 0) {	
	    //proceed with the retransmission of missing packets and go through remaining file
	    gwSendMissingPkts(gateway, dataSize);
	    sectorSize = 0;	    
	    int noOfFailedFixtures = gwAckList.size();
	    for(int i = 0; i < noOfFailedFixtures; i++) {
	      gwMgr.setImageUpgradeStatus(gwAckList.get(i), 
		  ServerConstants.IMG_UP_STATUS_FAIL);	
	      //update the gateway image upgrade status in db     
	      firmUpMgr.updateDeviceStatus(dbJob.getId(), gateway.getId(), 
		  ServerConstants.IMG_UP_STATUS_FAIL);
	      imgUpPendingGwList.remove(gwAckList.get(i));
	      GatewayImgUpgrDetails details = gwInProgressMap.get(gwId);
	      if(details != null) {
		String description = "Image upgrade of Application with image " + details.fileName + " failed";
		if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
		  description = "Image upgrade of Firmware with image " + details.fileName + " failed";
		}
		eventMgr.addEvent(gateway, description, 
		    EventsAndFault.GW_IMG_UP_STR); 
		gwInProgressMap.remove(gwId);
		//update the gateway image upgrade status in db     
		firmUpMgr.finishDeviceUpgrade(dbJob.getId(), gateway.getId(), 
		    ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description, "");
	      }
	    }
	    gwAckList.clear();
	    if(!inProgress) {
	      break;
	    }
	    gwAckList.addAll(imgUpPendingGwList);	    
	  }      
	  if(remainingSize < dataSize) { //last packet
	    //dataSize = remainingSize;
	  } else {
	    remainingSize = dataSize;
	  }
	  /*test code 
	  if(offset == 192 || offset == (3 * 1024) || offset == (17 * 1024) ||
	      offset == 8128 || offset == 108480) {	
	    offset += dataSize;
	    sectorSize += dataSize;
	    continue;
	  }
	  if(offset == (7 * 1024) || offset == (2 * 1024)) {      
	    oosPkt = dataPacket;
	    offset += dataSize;
	    sectorSize += dataSize;
	    continue;
	  } 
	  /**/
	  logger.debug(gwId + ":gw sending data to node: " + offset);
	  byte[] dataPacket = gwDataPacket(offset, remainingSize);
	  GatewayImpl.getInstance().sendGwPkt(ServerConstants.IMAGE_UPGRADE_MSG_TYPE, 
	      gateway, dataPacket, false);	  
	  /* test code
	  if((offset == (9 * 1024) || offset == (4 * 1024)) && oosPkt != null) {      
	    System.out.println("sending data to node: " + offset);
	    //printPacket(oosPkt);
	    if(commType == ServerConstants.COMM_TYPE_ZIGBEE) {
	      ZigbeeDeviceImpl.getInstance().sendDatatoNode(fixtureId, oosPkt); 
	    } else {	    
	      GEMSGatewayImpl.getInstance().sendDatatoNode((int)fixtureId, oosPkt);
	    }	    	 
	    ServerUtil.sleepMilli(IMG_UP_INTER_PKT_DELAY);
	    oosPkt = null;
	  } 
	  /**/
	  offset += remainingSize;
	  sectorSize += remainingSize;
	  ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY);
	}	
      }
    }.start();            
    
  } //end of method gwAckImageUploadStart
  
  public void gwSendMissingPkts(Gateway gateway, int dataSize) {
    
    bucket8kFinished = false;
    lastBucket++;
    long gwId = gateway.getId();
    int fileSize = gwFileArray.length;    
    long waitTime = 5 * 60 * 1000;
    long startWaitingTime = System.currentTimeMillis();
    logger.info("no. of nodes to expect ack from- " + gwAckList.size());
    while(true) {         
      if(!inProgress) { //upgrade is aborted/finished	
	break;
      }
      //if(bucketFinished) { //bucket finished    
      if(gwAckList.size() == 0) { 
	//bucket finished message came from all gateways
	logger.debug(gwId + ": positive ack came in " + 
	    (System.currentTimeMillis() - startWaitingTime));
	break;
      }      
      if(System.currentTimeMillis() - startWaitingTime > waitTime) {
	if(gwAckList.size() == imgUpPendingGwList.size()) {
	  logger.error(gwId + ": Aborting as no packets received from gateways");
	  inProgress = false;
	}
	break;
      } 
      while(true) {
	try {
	  Integer offsetInt = null;
	  synchronized(missingOffsetList) {
	    if(missingOffsetList.size() == 0) {
	      break;
	    }
	    offsetInt = missingOffsetList.poll();
	  }
	  if(offsetInt == null) {
	    continue;
	  }
	  int offset = offsetInt.intValue();
	  logger.debug(gwId + ":gw requested offset(retrieving) -- " + offset);
	  //for the last packet it may not be 64 bytes
	  int remainingSize = fileSize - offset;
	  if(remainingSize <= 0) {
	    //offset more than the file size
	    continue;
	  }
	  if(remainingSize < dataSize) { //last packet
	    //dataSize = remainingSize;
	    logger.debug(gwId + ": gw last packet requested");
	  } else {
	    remainingSize = dataSize;
	  }
	  logger.debug(gwId + ":gw sending missing packet with offset: " + offset);
	  byte[] dataPacket = gwDataPacket(offset, remainingSize);
	  GatewayImpl.getInstance().sendGwPkt(ServerConstants.IMAGE_UPGRADE_MSG_TYPE, 
	      gateway, dataPacket, false);		
	  ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY);
	}
	catch(Exception e) {
	  logger.error(gwId + ": " + e.getMessage());	 
	}	
      } 
      ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY);
    }    
        
  } //end of method gwSendMissingPkts

  private byte[] gwDataPacket(int offset, int dataSize) {
    
    int chunkSize = ImageUpgradeSO.IMG_UP_PLC_PKT_SIZE;   
         
    //data packet = offset, file data, checksum
    byte[] dataArray = new byte[chunkSize + 4];    
    byte[] offsetArray = ServerUtil.intToByteArray(offset);
    System.arraycopy(offsetArray, 0, dataArray, 0, offsetArray.length); // Add the file size MSB first
    System.arraycopy(gwFileArray, offset, dataArray, offsetArray.length, dataSize);    
    int remBytes = chunkSize - dataSize;
    while(remBytes > 0) {      
      --remBytes;
      //packet[i + 4 + dataSize + remBytes] = 0;
      dataArray[dataSize + 4 + remBytes] = 0;
    }        
        
    byte[] dataPacket = new byte[dataArray.length + 2];
    dataPacket[0] = (byte)ServerConstants.ISP_DATA_OPCODE;
    System.arraycopy(dataArray, 0, dataPacket, 1, dataArray.length);
    dataPacket[dataPacket.length - 1] = ServerUtil.computeChecksum(dataArray);
   
    return dataPacket;
    
  } //end of method gwDataPacket
  
  public synchronized void gwMissingPacketRequest(Gateway gateway, byte[] packet) {
    
    logger.debug(gateway.getIpAddress() + ":gw missing packet request - " + ServerUtil.getLogPacket(packet));
    if(!inProgress) { //upgrade is aborted
      return;
    }
        
    short index = 0;
    short bitMap = 0;
    int baseOffset = 512;
    int missingOffset = 0;
    byte startMarker = packet[0];    
    //-3 is because  for gw packet there is no node id after message type
    int noOfMissingPkts = packet[ServerConstants.RES_CMD_PKT_MSG_START_POS - 3];	
    int offsetPos = packet.length - 5;
    if(startMarker == ServerConstants.FRAME_START_MARKER) { //old packet
      noOfMissingPkts = packet[3];
      offsetPos = 52;
    }
    logger.debug(gateway.getId() + ": gw no. of missing - " + noOfMissingPkts + 
	", last bucket - " + lastBucket);
    if(noOfMissingPkts == 0) {
      byte[] tempArr = { packet[offsetPos], packet[offsetPos + 1], packet[offsetPos + 2], 
	  packet[offsetPos + 3] };
      int receivedOffset = ServerUtil.byteArrayToInt(tempArr);
      logger.debug(gateway.getId() + ": gw received offset -- " + receivedOffset);
      if(receivedOffset == lastBucket) {
	if(!bucket8kFinished) {
	  gwAckList.remove(gateway.getId());
	} else {
	  //this is a ack packet came after bucket is finished
	  return;
	}
	if(gwAckList.size() == 0) { 
	  //0 buckets came from all the fixtures
	  bucket8kFinished = true;
	}
	int lastBucketIndex = (gwFileArray.length / 8192) - 1;
	if(gwFileArray.length % 8192 > 0) {
	  lastBucketIndex++;
	}
	if(lastBucket == lastBucketIndex) {
	  logger.info(gateway.getId() + ": gw file transfer finished");
	  imgUpPendingGwList.remove(gateway.getId());
	  gwMgr.setImageUpgradeStatus(gateway.getId(), 
	      ServerConstants.IMG_UP_STATUS_SUCCESS);
	  GatewayImgUpgrDetails details = gwInProgressMap.get(gateway.getId());
	  String description = "Fixture upgrade successful";
	  if(details != null) {
	    gwInProgressMap.remove(gateway.getId());
	    if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) {
	      description = "Image upgrade of Application with image " + details.fileName + " successful";
	    } else {
	      description = "Image upgrade of Firmware with image " + details.fileName + " successful";
	    }
	    noOfGwsSuccess++;
	    //update the gateway image upgrade status in db     
	    firmUpMgr.finishDeviceUpgrade(dbJob.getId(), gateway.getId(), 
		ServerConstants.IMG_UP_STATUS_SUCCESS, details.noOfAttempts, description, gateway.getVersion());	
	  }
	  eventMgr.addEvent(gateway, description, 
	      EventsAndFault.GW_IMG_UP_STR);
	}
      }      
      return;
    }    
    int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS + 1;   
    for(int k = 0; k < noOfMissingPkts; k++) {
      byte[] tempArr = { packet[pktIndex++], packet[pktIndex++]};
      index = (short)ServerUtil.byteArrayToShort(tempArr);
      //index = (short)(0x00FF & ((short)packet[pktIndex++]));
      bitMap = (short)(0x00FF & ((short)packet[pktIndex++]));      
      for(int j = 0; j < 8; j++) {
	if(!ServerUtil.isBitSet(bitMap, j)) {
	  missingOffset = (baseOffset * index) + j * 64;
	  logger.debug("gw requested offset(adding) -- " + missingOffset);
	  synchronized(missingOffsetList) {
	    if(!missingOffsetList.contains(new Integer(missingOffset))) {
	      missingOffsetList.add(new Integer(missingOffset));
	    }
	  }
	}
      }
    }    
           
  } //end of method gwMissingPacketRequest
  
  public void gwCancelFileUpload(Gateway gateway, byte[] packet) {
    
    logger.error(gateway.getIpAddress() + ":receieved abort message - " + ServerUtil.getLogPacket(packet));
    //for point to point this inProgress should be false
    gwAckList.remove(gateway.getId());
    imgUpPendingGwList.remove(gateway.getId());
    gwMgr.setImageUpgradeStatus(gateway.getId(), ServerConstants.IMG_UP_STATUS_FAIL);
    //update the gateway image upgrade status in db     
    firmUpMgr.updateDeviceStatus(dbJob.getId(), gateway.getId(), 
	ServerConstants.IMG_UP_STATUS_FAIL);
    if(imgUpPendingGwList.size() == 0) { //abort came/image upload finished on all fixtures
      inProgress = false;
    }
    GatewayImgUpgrDetails details = gwInProgressMap.get(gateway.getId());
    String description = "Gateway upgrade aborted";
    if(details != null) {
      gwInProgressMap.remove(gateway.getId());
      if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) {
	description = "Image upgrade of Application with image " + details.fileName + " aborted";
      } else {
	description = "Image upgrade of Firmware with image " + details.fileName + " aborted";
      }
      //update the gateway image upgrade status in db     
      firmUpMgr.finishDeviceUpgrade(dbJob.getId(), gateway.getId(), 
	  ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description, "");	
    }
    eventMgr.addEvent(gateway, description, 
	EventsAndFault.GW_IMG_UP_STR);
    
  } //end of method gwCancelFileUpload
  
} //end of class FixtureImageUpgradeWorker
