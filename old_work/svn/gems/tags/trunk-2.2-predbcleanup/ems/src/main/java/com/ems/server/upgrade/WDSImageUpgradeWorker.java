/**
 * 
 */
package com.ems.server.upgrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.model.Wds;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.server.ServerConstants;
import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.device.wds.WDSImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.GatewayManager;
import com.ems.service.WdsManager;

/**
 * @author Sreedhar
 * this class takes care of upgrading the wdss
 * wdss belonging to different gateways are upgraded in parallel as they
 * are run in different threads
 *
 */
public class WDSImageUpgradeWorker extends Thread {
    
  private static final Logger logger = Logger.getLogger("ImageUpgrade");
    
  private EventsAndFaultManager eventMgr = null;
  private FirmwareUpgradeManager firmUpMgr = null;
  private GatewayManager gwMgr = null;
  private WdsManager wdsMgr = null;
  
  private int[] devices; //array of wdss which are upgraded by this worker
  private String fileName; //image file name
  private int targetBuildNo; //retrieved from the file name
  
  private ImageUpgradeDBJob dbJob = null;
  private int noOfSusSuccess = 0;
  
  private int appType;
  private byte[] fileArray = null; //array of binary data of the image file
  
  //hash map of gateway threads
  private HashMap<Long, GwWdsImgUpgThread> gwWdsUpgThrMap = new HashMap<Long, GwWdsImgUpgThread>();  
       
  //structure to hold all the image upgrade details per wds
  class WdsImgUpgrDetails {
    
    int targetVersion;
    int appUpgraded;
    String status; //scheduled, in progress, success, fail
    String fileName;
    Date startTime;
    Date endTime;
    int noOfAttempts;
    String description;

    boolean ackRcvd;
    boolean imgPending;
    
  } //end of class WdsImgUpgrDetails

  /**
   * 
   */
  public WDSImageUpgradeWorker(ImageUpgradeJob job) {
    
    this.devices = job.getDeviceIds();
    this.fileName = job.getFileName();
    this.dbJob = job.getImageUpgradeDBJob();
        
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    firmUpMgr = (FirmwareUpgradeManager)SpringContext.getBean("firmwareUpgradeManager");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    wdsMgr = (WdsManager)SpringContext.getBean("wdsManager");
    
  } //end of constructor
  
  public void run() {
    
  	try {
      //update the job status in database as in progress
      dbJob.setStatus(ServerConstants.IMG_UP_STATUS_INPROGRESS);
      dbJob.setStartTime(new Date());
      firmUpMgr.save(dbJob);
      
      startImageUpload(devices, fileName);
      
      //update the status in database
      if(noOfSusSuccess == 0) {
        dbJob.setStatus(ServerConstants.IMG_UP_STATUS_FAIL);
      } else if(devices.length == noOfSusSuccess) {    
        dbJob.setStatus(ServerConstants.IMG_UP_STATUS_SUCCESS);
      } else {
        dbJob.setStatus(ServerConstants.IMG_UP_STATUS_PARTIAL);
      }
      dbJob.setEndTime(new Date());
      firmUpMgr.save(dbJob);    
  	}
  	catch(Exception ex) {
  		logger.error("Error in executing the wds image upgrade work " + ex.getMessage());
  	}
    
  } //end of method run
     
  public void startImageUpload(int[] wdsIds, String fileName) {
         
    logger.info("wds upgrade request on " + wdsIds.length + " using " + fileName);
    String filePath = ImageUpgradeSO.getInstance().getImageLocation() + fileName;    
    try {
      fileArray = null;
      fileArray = ImageUpgradeSO.getBytesFromFile(new File(filePath));     
    }
    catch(Exception e) {
      e.printStackTrace();
    }  
    if(fileArray == null) {
      logger.error("failed to read the file");
      return;
    }
    logger.debug("file size - " + fileArray.length);

    //this is based on the file naming convention being followed
    targetBuildNo = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
    appType = ServerConstants.ISP_APP2_INIT_OPCODE;
       
    int noOfFixtures = wdsIds.length;    
    long startTime = System.currentTimeMillis();    
        
    //group fixtures based on the gateway with which they are communicating
    HashMap<Long, ArrayList<Wds>>gwWdsMap = new HashMap<Long, ArrayList<Wds>>();
    for(int i = 0; i < noOfFixtures; i++) {
      Wds wds = wdsMgr.getWdsSwitchById((long)wdsIds[i]);   
      if(wds == null) {
      	logger.info(wdsIds[i] + ": There is no Wds");
        continue;
      }      
      Long gwId = wds.getGatewayId();
      if(!gwWdsMap.containsKey(gwId)) {
      	gwWdsMap.put(gwId, new ArrayList<Wds>());	
      }
      gwWdsMap.get(gwId).add(wds);      
    }
    
    ImageUpgradeSO.readUpgradeProperties();
    Iterator<Long> gwIter = gwWdsMap.keySet().iterator();    
    
    //for each gateway in the gateway map, create a thread and start it so that all the
    //gateway threads run in parallel
    while(gwIter.hasNext()) {
      Long gwId = gwIter.next();
      ArrayList<Wds> wdsList = gwWdsMap.get(gwId);
      GwWdsImgUpgThread thread = new GwWdsImgUpgThread(gwId, wdsList, this);  
      gwWdsUpgThrMap.put(gwId, thread);
      thread.start();
    }    
    //waiting for all the gateway threads to finish
    while(gwWdsUpgThrMap.size() > 0) {
      ServerUtil.sleep(5);
    }
    logger.info("Time taken to upload to all wds -- " + (System.currentTimeMillis() - startTime));
        
  } //end of method startImageUpload
  
  //class to handle upgrade of all the fixtures that belong to one gateway
  //there will be one thread per gateway
  class GwWdsImgUpgThread extends Thread {
    
    private long gwId;
   
    //wds list maintained for acks. these are fixtures for which acks are expected
    private ArrayList<Long> wdsAckList = new ArrayList<Long>();

    //this list contain wds for which image upgrade is pending
    //i.e. image upgrade is started and not yet finished
    //TODO remove this and reuse wdsInProgressMap for this purpose
    private ArrayList<Long> imgUpPendingWdsList = new ArrayList<Long>();
    
    //it is a list of missing offset (multiple of 64)
    private LinkedList<Integer> missingOffsetList = new LinkedList<Integer>();
    
    //cache to hold image upgrade details of all wds
    private Map<Long, WdsImgUpgrDetails> wdsInProgressMap = Collections.synchronizedMap(
    		new HashMap<Long, WdsImgUpgrDetails>());  
    
    private int initAttempts = 2;
    private boolean inProgress = false;
    private boolean bucket8kFinished = false; //bucket is 8k
    private int lastBucket = 0;    
    private int currRetryAttempts = 0; //no. of attempts 
    private int maxAttempts = 2;
       
    private WDSImageUpgradeWorker worker = null;
        
    //list of wds that are part of the image upgrade
    ArrayList<Wds> wdsList = new ArrayList<Wds>();
    
    int interPktDelay = ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY_2;
    Gateway gw = null;
    
    public GwWdsImgUpgThread(long gwId, ArrayList<Wds> wdsList, WDSImageUpgradeWorker worker) {
      
      this.gwId = gwId;
      this.wdsList = wdsList;
      this.worker = worker;
      this.maxAttempts = ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES + 1;
      
    } //end of constructor
    
    public void run() {
      
    	gw = gwMgr.loadGateway(gwId);
    	interPktDelay = ImageUpgradeSO.getImageUpgradeInterPacketDelay(gw.getApp2Version());
      //if there are any other image upgrade jobs running on the same gateway
      //wait for them to be finished
      while(true) {
      	synchronized(ImageUpgradeSO.gwThreadLock) {
      		if(!ImageUpgradeSO.getInstance().gwFixtUpgrMap.containsKey(gwId) &&
      				!ImageUpgradeSO.getInstance().gwUpgrMap.containsKey(gwId) &&
      				!ImageUpgradeSO.getInstance().gwWdsUpgrMap.containsKey(gwId)) {
      			ImageUpgradeSO.getInstance().gwWdsUpgrMap.put(gwId, worker);
      			break;
      		}
      	}
      	logger.info(gwId + ": other image upgrade job is running on this gw");
      	ServerUtil.sleep(10);
      }
      int noOfWdss = wdsList.size();
      //create details object for each wds in the wds list      
      for(int i = 0; i < noOfWdss; i++) {	
      	Wds wds = wdsList.get(i);
      	WdsImgUpgrDetails details = new WdsImgUpgrDetails();
      	details.appUpgraded = appType;
      	details.targetVersion = targetBuildNo;
      	details.status = ServerConstants.IMG_UP_STATUS_SCHEDULED;
      	details.fileName = fileName;
      	details.noOfAttempts = 0;
      	wdsInProgressMap.put(wds.getId(), details);      	      	
      }
      
      //try for max retry attempts
      boolean retryReq = true;
      while(currRetryAttempts < maxAttempts) {
      	try {
      		initiateImageUpgrade();
      	}
      	catch(Exception e) {
      		e.printStackTrace();
      	}
      	retryReq = true;
      	//give 15 seconds for fixtures to return the current version
      	for(int i = 0; i < 3; i++) {
      		if(wdsInProgressMap.size() == 0) {
      			//all fixtures are done with upgrade successfully
      			logger.info(gwId + ": all the wds are done no need to retry");
      			retryReq = false;
      			break;
      		}  
      		ServerUtil.sleep(5);
      	}
      	if(!retryReq) {
      		break;
      	}
      	currRetryAttempts++;
      }
      
      try {
      	if(!retryReq) {
      		//all wds are successfully upgraded
      		return;
      	}
      	//done with all the attempts
      	//wait for another couple of minutes as wds are taking time to boot in 2.0
      	for(int i = 0; i < 24; i++) {
      		if(wdsInProgressMap.size() == 0) {
      			//all wds are done with upgrade successfully or aborted
      			logger.info(gwId + ": all the wds are done no need to wait");      			
      			break;
      		}  
      		ServerUtil.sleep(5);
      	}
      	//go through the wds and mark them as failed
      	for(int i = 0; i < noOfWdss; i++) {
      		Wds wds = wdsList.get(i);
      		WdsImgUpgrDetails details = wdsInProgressMap.get(wds.getId());
      		if(details == null) {
      			//this wds is already removed from the map. it is successful
      			continue;
      		}	
      		String description = "";
      		if(details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
      			//image upgrade didn't start on this wds  
      			description = "Image upgrade of Application with image " + fileName + " didn't start";      			  
      		} else if(details.status.equals(ServerConstants.IMG_UP_STATUS_INPROGRESS)) {	  
      			description = "Image upgrade of Application with image " + fileName + " didn't finish";      			 
      		} else {
      			//it is failed
      			if(details.description == null || details.description.equals("")) {
      				description = "Image upgrade of Application with image " + fileName + " aborted";      				
      			} else {
      				description = details.description;
      			}
      		}
      		logger.error(wds.getId() + ": " + description);
      		//update the device status as failed in the database
      		wdsMgr.setImageUpgradeStatus(wds.getId(), ServerConstants.IMG_UP_STATUS_FAIL);
      		firmUpMgr.finishDeviceUpgrade(dbJob.getId(), wds.getId(), 
      				ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description);
      		//TODO
      		//      		eventMgr.addEvent(wds, description, 
//      				EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR);
      		wdsInProgressMap.remove(wds.getId());
      	}
      }
      catch(Exception e) {
      	e.printStackTrace();
      }
      finally {
      	gwWdsUpgThrMap.remove(gwId);
      	ImageUpgradeSO.getInstance().gwWdsUpgrMap.remove(gwId);
      }
      
    } //end of method run
    
    private void initiateImageUpgrade() {
      
    	//iterator through wds list to remove the successful wds
      Iterator<Wds> wdsIter = wdsList.iterator();
      while(wdsIter.hasNext()) {
      	Wds wds = wdsIter.next();
      	WdsImgUpgrDetails details = wdsInProgressMap.get(wds.getId());
      	if(details == null || details.status.equals(ServerConstants.IMG_UP_STATUS_SUCCESS)) {
      		//it is already removed or it was successful
      		wdsIter.remove();
      	}	
      }
                 
      //upgrade wdss     
      if(wdsList.size() > 0) {
      	startMulticastImageUpload(wdsList, appType);
      }
            
      //image upload is complete. give 45 seconds for fixtures to send node boot info
      boolean allWdssDone = false;
      for(int i = 0; i < 9; i++) {
      	if(wdsInProgressMap.size() == 0) {
      		//all fixtures are done with upgrade successfully
      		logger.info(gwId + ": all wdss are done no need to wait for node boot info");
      		allWdssDone = true;
      		break;
      	}
      	ServerUtil.sleep(5);	  
      }
           	
    } //end of method initiateImageUpgrade
    
    //private Object initAckObj = new Object();
    
    public void startMulticastImageUpload(ArrayList<Wds> wdsList, int imageType) {
      
      //send init packet to the wds    
      missingOffsetList.clear();     
      initAttempts = 2;      
      String msg = ": image upgrade initiated on wdss - ";    
      int noOfWdss = wdsList.size();
      Wds wds = null;
      Long[] wdsIds = new Long[noOfWdss];
      for(int i = 0; i < noOfWdss; i++) {
        if(i > 0) {
          msg += ", ";
        }
        wds = wdsList.get(i); 
        wdsAckList.add(wds.getId());
        msg += wds.getName();        
        String description = "Image upgrade of Application with image " + fileName + " initiated";               
        //TODO
        //eventMgr.addEvent(wds, description, EventsAndFault.FIXTURE_IMG_UP_STR, 
        	//	EventsAndFault.INFO_SEV_STR);
        wdsIds[i] = wds.getId();
        WdsImgUpgrDetails details = wdsInProgressMap.get(wds.getId());	  
        if(details != null) {
          details.noOfAttempts++;
        }
      }
      if(currRetryAttempts == 0) {
      	//update the device status as in progress in the db only in the first attempt
      	firmUpMgr.startDeviceUpgrade(dbJob.getId(), wdsIds);
      }
      logger.debug(gwId + msg + " at " + new Date());
      //send the init packet to all the fixtures two times
      while(initAttempts > 0) {        
        try {	
          multicastInitPacket(fileArray.length, wdsList, imageType, interPktDelay);	
        }
        catch(Exception ex) {
          ex.printStackTrace();
        }
        ServerUtil.sleep(15);
        initAttempts--;
        if(inProgress) {
          //ack is received from all fixtures or ready to send the file
          break;
        }
      }
      
      //if there are any pending fixtures, proceed
      if(imgUpPendingWdsList.size() > 0) {
      	inProgress = true;
      } else {
      	// no wds has returned ack for initiate image upgrade
      	return;
      }
    
      logger.info(gwId+ ": no. of pending fixtures- " + imgUpPendingWdsList.size());
      long startTime = System.currentTimeMillis();
      int dataSize = ImageUpgradeSO.IMG_UP_ZIGBEE_PKT_SIZE;
      int fileSize = fileArray.length;	
      if(fileSize < dataSize) {
      	dataSize = fileSize;
      }    
      int offset = 0;    
      byte oosPkt[] = null;
      int sectorSize = 0;
      bucket8kFinished = false;
      lastBucket = -1;	
      wdsAckList.clear();
      wdsAckList.addAll(imgUpPendingWdsList);	
      while(offset <= fileSize && inProgress) {
      	if(sectorSize > 8192) { //8k boundary is crossed
      		offset = offset - (offset % 8192);
      	}
      	int remainingSize = fileSize - offset;	  
      	if(remainingSize <= 0) {
      		//at the end also,  missing packets
      		sendMissingPkts(wds, dataSize);
      		logger.info(gwId + ": time taken to upload on all wdss- " + (System.currentTimeMillis() - startTime));
      		inProgress = false;
      	  int noOfFailedWdss = wdsAckList.size();
      	  for(int i = 0; i < noOfFailedWdss; i++) {
      	    logger.error(wdsAckList.get(i) + ": image upgrade failed to get the ack for end of file");	      
      	    imgUpPendingWdsList.remove(wdsAckList.get(i));
      	  }
      	  break;
      	}
  	    
      	//after 8k wait for missing packet requests for 2 sec and proceed     
      	if(offset > 0 && offset % (8 * 1024) == 0) {	
      		//proceed with the retransmission of missing packets and go through remaining file
      		sendMissingPkts(wds, dataSize);
      		sectorSize = 0;
      		int noOfFailedFixtures = wdsAckList.size();
      		for(int i = 0; i < noOfFailedFixtures; i++) {
      			long failedWdsId = wdsAckList.get(i);
      			logger.error(failedWdsId + ": image upgrade failed as ack is missed");	    
      			imgUpPendingWdsList.remove(failedWdsId);
      		}
      		wdsAckList.clear();
      		wdsAckList.addAll(imgUpPendingWdsList);	    
      	}      
      	if(remainingSize < dataSize) { //last packet
      		//dataSize = remainingSize;
      	} else {
      		remainingSize = dataSize;
      	}
      	/*test code
				if(offset % 512 == 0) {
	  			offset += dataSize;
	  			sectorSize += dataSize;
	  			continue;
				}
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
      	if(inProgress) {
      		sendImagePacket(gwId, offset, remainingSize, false, ImageUpgradeSO.IMG_UP_NO_RETRANSMITS, interPktDelay);	  
      	}
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
      	//ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY/3);
      }	
            
    } //end of method startMulticastImageUpload
    
    private Object ackLock = new Object();    
    
    public void ackImageUploadStart(final Wds wds) {
      
      final long wdsId = wds.getId();
      logger.debug(wdsId + ": got ack for image upload start");   
      if(!wdsAckList.contains(wdsId) && !imgUpPendingWdsList.contains(wdsId)) {      
        logger.error(wdsId + ":wds is not in the upgrade list. so, ignoring the ack");
        return;
      }
      synchronized(ackLock) {
        if(inProgress) {
          //already file transfer is in progress so ignore
          logger.debug(wdsId + ":file transfer is already in progress. so ignoring the ack");
          return;
        }
        WdsImgUpgrDetails details = wdsInProgressMap.get(wdsId);    
        if(details != null && details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
          wdsMgr.setImageUpgradeStatus(wdsId, ServerConstants.IMG_UP_STATUS_INPROGRESS);
          details.status = ServerConstants.IMG_UP_STATUS_INPROGRESS;
          String description = "Image upgrade of Application with image " + details.fileName + " started";   
          //TODO
          //eventMgr.addEvent(wds, description, 
            //  EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.INFO_SEV_STR); 
        }    
        if(!imgUpPendingWdsList.contains(wdsId)) {
          //adding the wds to pending list and removing it from ack list
          imgUpPendingWdsList.add(new Long(wdsId));
          wdsAckList.remove(wdsId);
        }
        if(initAttempts > 0 && wdsAckList.size() > 0) {
          //init ack didn't come from all the wdss and there are still init packet
          //attempts left so don't start file upload yet
          return;
        } else {
          //set the in progress flag to true so the file transfer starts
          inProgress = true;
          //initAckObj.notify();
        }
      }                
      
    } //end of method ackImageUploadStart
    
    private void sendMissingPkts(Wds wds, int dataSize) {
      
      bucket8kFinished = false;
      lastBucket++;
      long fixtureId = wds.getId();
      int fileSize = fileArray.length;    
      long waitTime = 3 * 60 * 1000;
      long startWaitingTime = System.currentTimeMillis();
      logger.info(gwId + ": no. of nodes to expect ack from- " + wdsAckList.size());
      while(true) {         
        if(!inProgress) { //upgrade is aborted/finished	
          break;
        }        
        if(wdsAckList.size() == 0) { 
          //bucket finished message came from all fixtures
          logger.debug(gwId + ": positive ack came in " + 
              (System.currentTimeMillis() - startWaitingTime) + " ms");
          break;
        }      
        if(imgUpPendingWdsList.size() == 0) {
          //image upgrade finished on all the wdss. may be final acks didn't come
          //but they might have aborted or rebooted successfully
          inProgress = false;
          break;
        }
        if(System.currentTimeMillis() - startWaitingTime > waitTime) {
          if(wdsAckList.size() == imgUpPendingWdsList.size()) {
            logger.error(gwId + ": Aborting as no packets received from all wdss");
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
            logger.debug(gwId + ":requested offset(retrieving) -- " + offset);
            //for the last packet it may not be 64 bytes
            int remainingSize = fileSize - offset;
            if(remainingSize <= 0) {
              //offset more than the file size
              continue;
            }
            if(remainingSize < dataSize) { //last packet
              //dataSize = remainingSize;
              logger.debug(gwId + ":last packet requested filesize - " + fileSize + " datasize - " +dataSize);
            } else {
              remainingSize = dataSize;
            }
            sendImagePacket(gwId, offset, remainingSize, true, 1, interPktDelay);              	  
          }
          catch(Exception e) {
            logger.error(gwId + ": " + e.getMessage());
          }
        }
        ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY/3);
      }    
          
    } //end of method sendMissingPkts
     
    public synchronized void missingPacketRequest(Wds wds, byte[] packet) {
          
      logger.debug(wds.getId() + ": missing packet request - " + ServerUtil.getLogPacket(packet));
      if(!inProgress) { //upgrade is aborted
        return;
      }
         
      short index = 0;
      short bitMap = 0;
      int baseOffset = 512;
      int missingOffset = 0;
      byte startMarker = packet[0];    
      int noOfMissingPkts = packet[ServerConstants.RES_CMD_PKT_MSG_START_POS];
      int offsetPos = packet.length - 5;      
      logger.debug(wds.getId() + ": no. of missing - " + noOfMissingPkts + ", last bucket - " + lastBucket);
      if(noOfMissingPkts == 0) {
        byte[] tempArr = { packet[offsetPos], packet[offsetPos + 1], packet[offsetPos + 2], 
        		packet[offsetPos + 3] };
        int receivedOffset = ServerUtil.byteArrayToInt(tempArr);
        if(receivedOffset == lastBucket) {
          if(!bucket8kFinished) {
            wdsAckList.remove(wds.getId());
          } else {
            //this is a ack packet came after bucket is finished
            return;
          }
          if(wdsAckList.size() == 0) { 
            //0 buckets came from all the fixtures
            bucket8kFinished = true;
          }
          int lastBucketIndex = (fileArray.length / 8192) - 1;
          if(fileArray.length % 8192 > 0) {
            lastBucketIndex++;
          }
          if(lastBucket == lastBucketIndex) {
            logger.info(wds.getId() + ": file transfer finished");
            imgUpPendingWdsList.remove(wds.getId());	  
          }
        }      
        return;
      }    
      int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS + 1;
      if(startMarker == ServerConstants.FRAME_START_MARKER) { //old packet
        pktIndex = 4;
      }
      for(int k = 0; k < noOfMissingPkts; k++) {
        byte[] tempArr = { packet[pktIndex++], packet[pktIndex++]};
        index = (short)ServerUtil.byteArrayToShort(tempArr);
        //index = (short)(0x00FF & ((short)packet[pktIndex++]));
        bitMap = (short)(0x00FF & ((short)packet[pktIndex++]));      
        for(int j = 0; j < 8; j++) {
        	if(!ServerUtil.isBitSet(bitMap, j)) {
        		missingOffset = (baseOffset * index) + j * 64;
        		logger.debug(wds.getId() + ": requested offset(adding) -- " + missingOffset);
        		synchronized(missingOffsetList) {
        			if(!missingOffsetList.contains(new Integer(missingOffset))) {
        				missingOffsetList.add(new Integer(missingOffset));
        			}
        		}
        	}
        }
      }    
             
    } //end of method missingPacketRequest
    
    public void cancelFileUpload(Wds wds, byte[] packet) {
      
      logger.error(wds.getId() + ": abort message - " + ServerUtil.getLogPacket(packet));    
      //for point to point this inProgress should be false
      wdsAckList.remove(wds.getId());
      imgUpPendingWdsList.remove(wds.getId());
      WdsImgUpgrDetails details = wdsInProgressMap.get(wds.getId());      
      if(details != null) {        
      	details.status = ServerConstants.IMG_UP_STATUS_FAIL;
      	details.description = "Image upgrade of Application with image " + details.fileName + " aborted";
      }
      if(imgUpPendingWdsList.size() == 0) { //abort came/image upload finished on all wdss
        inProgress = false;
      }
      
    } //end of method cancelFileUpload
        
    private String updateUpgradeCompleteStatus(Wds wds, WdsImgUpgrDetails details, 
    		int currVer,int imgUpgrStatus) {
      
      boolean imageUpgradeSuccess = false;
      String imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_FAIL;
      if(currRetryAttempts < (maxAttempts - 1)) {
      	imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_INPROGRESS;
      }
      //String description = "";
      //for app2 upgrade, app2 is erased. so, current app 2 means it booted properly with
      //new image
      details.description = "Image upgrade with image " + details.fileName;              	 
      
      if(imgUpgrStatus == 0) {
      	if(details.targetVersion == currVer) {        		
      		imageUpgradeSuccess = true;
      	} else {
      		details.description += " failed as SU rebooted with old image(" + currVer + ")";
      	}
      } else {
      	details.description += " failed as SU rebooted during upgrade";
      }
          
      if(imageUpgradeSuccess) {
      	details.status = ServerConstants.IMG_UP_STATUS_SUCCESS;
      	details.description += " successful";
      	wdsInProgressMap.remove(wds.getId());
        logger.info(wds.getId() + ":image upgrade successful and wds rebooted.");
        noOfSusSuccess++;
        wdsMgr.setImageUpgradeStatus(wds.getId(), ServerConstants.IMG_UP_STATUS_SUCCESS);
        //TODO
        //eventMgr.addEvent(wds, details.description, 
        	//	EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.INFO_SEV_STR);	
        firmUpMgr.finishDeviceUpgrade(dbJob.getId(), wds.getId(), 
            ServerConstants.IMG_UP_STATUS_SUCCESS, details.noOfAttempts, details.description);
        imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_SUCCESS;
      } else {
      	details.status = ServerConstants.IMG_UP_STATUS_FAIL;      	        
      }
      return imgUpgrStatusStr;
      
    } //end of method updateUpgradeCompleteStatus
    
    public String nodeRebooted(Wds wds, short currVer, int imgUpgrStatus) {
      
      long wdsId = wds.getId();
         
      //image upgrade was pending on this wds. abort it
      imgUpPendingWdsList.remove(wdsId);
      if(imgUpPendingWdsList.size() == 0) {
        inProgress = false;
      }
      if(!wdsInProgressMap.containsKey(wdsId)) {
        return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
      }            
      WdsImgUpgrDetails details = wdsInProgressMap.get(wdsId);
      if(details.appUpgraded == 3) {
        return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
      }
      return updateUpgradeCompleteStatus(wds, details, currVer, imgUpgrStatus);
          
    } //end of method nodeRebooted
    
    private void sendImagePacket(long gwId, int offset, int remainingSize, 
    		boolean missing, int retransmits, int interPktDelay) {
	    
      if(retransmits > 10) {
      	retransmits = 10;
      } else if(retransmits < 1) {
      	retransmits = 1;
      }      
      for(int i = 0; i < retransmits; i++) {
      	if(missing) {
      		logger.debug(gwId + ":sending missing img packet(" + (i+1) + "): " + offset);
      	} else {
      		logger.debug(gwId + ":sending img packet(" + (i+1) + "): " + offset);
      	}
      	broadcastDataPacket(offset, remainingSize, gwId);      	
      	ServerUtil.sleepMilli(interPktDelay/ImageUpgradeSO.IMG_UP_NO_RETRANSMITS);
      }
      
    } //end of method sendImagePacket
    
  } //end of class GwFixtImgUpgThread  
  
  private void multicastInitPacket(int fileSize, ArrayList<Wds> wdsList, int imageType, int interPktDelay) {
    
    int i = 0;        
    //image type(1), packet size(4), file size(4)
    byte[] dataPacket = new byte[9];
    dataPacket[i++] = (byte)imageType;    
    dataPacket[i++] = 0x00;
    dataPacket[i++] = 0x00;
    dataPacket[i++] = 0x00;
    dataPacket[i++] = (byte)ImageUpgradeSO.IMG_UP_ZIGBEE_PKT_SIZE;    
    byte[] sizeArray = ServerUtil.intToByteArray(fileSize);
    System.arraycopy(sizeArray, 0, dataPacket, i, sizeArray.length); // Add the file size MSB first
    ArrayList<Wds> tempList = new ArrayList<Wds>();
    int noOfFixtures = wdsList.size();
    for(int k = 0; k < noOfFixtures; k++) {
      tempList.add(wdsList.get(k));
      if((k == (noOfFixtures - 1)) || ((k + 1) % ImageUpgradeSO.IMG_UP_NO_MULTICAST_TARGETS == 0)) {
      	WDSImpl.getInstance().sendMulticastPacket(tempList,
      			ServerConstants.IMAGE_UPGRADE_MSG_TYPE, dataPacket, false, 0);
      	tempList.clear();
      	ServerUtil.sleepMilli(200);	
      }      
    }    
       
  } //end of method multicastInitPacket  
    
  private void broadcastDataPacket(int offset, int dataSize, long gwId) {
    
    int chunkSize = ImageUpgradeSO.IMG_UP_ZIGBEE_PKT_SIZE; //64 should be dataSize.
        
    //data packet = offset, file data, checksum
    byte[] dataArray = new byte[chunkSize + 4 + 2];    
    ServerUtil.fillIntInByteArray(offset, dataArray, 1);    
    System.arraycopy(fileArray, offset, dataArray, 5, dataSize);    
    int remBytes = chunkSize - dataSize;
    while(remBytes > 0) {      
      --remBytes;      
      if(remBytes % 2 == 0) {
      	dataArray[dataSize + 5 + remBytes] = 0x55;
      } else {
      	dataArray[dataSize + 5 + remBytes] = 0x66;
      }
    }        
       
    dataArray[0] = 0;
    dataArray[dataArray.length - 1] = 0;
    dataArray[dataArray.length - 1] = ServerUtil.computeChecksum(dataArray);
    dataArray[0] = (byte)ServerConstants.ISP_DATA_OPCODE;
        
    ZigbeeDeviceImpl.getInstance().broadcastImageUprade(gwId, dataArray,
    		ServerConstants.IMAGE_UPGRADE_MSG_TYPE, fileArray.length);    
       
  } //end of method broadcastDataPacket
  
  public void cancelFileUpload(Wds wds, byte[] packet, long gwId) {
    
    if(gwWdsUpgThrMap.containsKey(gwId)) {
      gwWdsUpgThrMap.get(gwId).cancelFileUpload(wds, packet);
    }
    
  } //end of method cancelFileUpload
  
  //this is called from node boot info
  public String nodeRebooted(Wds wds, short currVer, long gwId, int imgUpgrStatus) {
    
    if(gwWdsUpgThrMap.containsKey(gwId)) {
      return gwWdsUpgThrMap.get(gwId).nodeRebooted(wds, currVer, imgUpgrStatus);
    }
    return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
        
  } //end of method nodeRebooted
  
  public void ackImageUploadStart(final Wds wds, long gwId) {
    
    if(gwWdsUpgThrMap.containsKey(gwId)) {
      gwWdsUpgThrMap.get(gwId).ackImageUploadStart(wds);
    }
    
  } //end of method ackImageUploadStart
  
  public synchronized void missingPacketRequest(Wds wds, byte[] packet, long gwId) {
    
    if(gwWdsUpgThrMap.containsKey(gwId)) {
      gwWdsUpgThrMap.get(gwId).missingPacketRequest(wds, packet);
    }
    
  } //end of method missingPacketRequest
    
} //end of class FixtureImageUpgradeWorker
