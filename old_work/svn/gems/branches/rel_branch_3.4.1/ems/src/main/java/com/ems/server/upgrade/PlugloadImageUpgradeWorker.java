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
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.Plugload;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.device.plugload.PlugloadImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.GatewayManager;
import com.ems.service.PlugloadManager;

/**
 * @author Sreedhar
 * this class takes care of upgrading the fixtures
 * fixtures belonging to different gateways are upgraded in parallel as they
 * are run in different threads
 *
 */
public class PlugloadImageUpgradeWorker extends Thread {
    
  private static final Logger logger = Logger.getLogger("ImageUpgrade");
  
  private PlugloadManager plugloadMgr = null;
  private EventsAndFaultManager eventMgr = null;
  private FirmwareUpgradeManager firmUpMgr = null;
  private GatewayManager gwMgr = null;
  
  private Long[] plugloads; //array of plugloads which are upgraded by this worker
  private String fileName; //image file name
  private int targetBuildNo; //retrieved from the file name
  
  private ImageUpgradeDBJob dbJob = null;
  private int noOfSusSuccess = 0;
  
  private int appType;
  private byte[] fileArray = null; //array of binary data of the image file
  
  //hash map of gateway threads
  private HashMap<Long, GwPlugloadImgUpgThread> gwPlugloadUpgThrMap = new HashMap<Long, 
  	GwPlugloadImgUpgThread>();  
       
  //structure to hold all the image upgrade details per plugload
  class PlugloadImgUpgrDetails {
    
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
    
  } //end of class PlugloadImgUpgrDetails

  /**
   * 
   */
  public PlugloadImageUpgradeWorker(ImageUpgradeDBJob job) {
    
    this.plugloads = job.getDeviceIds();
    this.fileName = job.getImageName();
    this.dbJob = job;
     
    plugloadMgr = (PlugloadManager)SpringContext.getBean("plugloadManager");
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    firmUpMgr = (FirmwareUpgradeManager)SpringContext.getBean("firmwareUpgradeManager");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    
  } //end of constructor
  
  private boolean aborted = false;
  
  public void abortJob() {
  	
  	aborted = true;
  	dbJob.setStatus(ServerConstants.IMG_UP_STATUS_ABORTED);
		dbJob.setEndTime(new Date());
    firmUpMgr.save(dbJob);
  	
  } //end of method cancelJob
  
  public void run() {
    
  	try {
  		if(aborted) {
        return;
    	}
      //update the job status in database as in progress
      dbJob.setStatus(ServerConstants.IMG_UP_STATUS_INPROGRESS);
      dbJob.setStartTime(new Date());
      firmUpMgr.save(dbJob);
      
      startImageUpload(plugloads, fileName);
      
      if(aborted) {
        return;
    	}
      
      //update the status in database
      if(noOfSusSuccess == 0) {
        dbJob.setStatus(ServerConstants.IMG_UP_STATUS_FAIL);
      } else if(plugloads.length == noOfSusSuccess) {    
        dbJob.setStatus(ServerConstants.IMG_UP_STATUS_SUCCESS);
      } else {
        dbJob.setStatus(ServerConstants.IMG_UP_STATUS_PARTIAL);
      }
      dbJob.setEndTime(new Date());
      firmUpMgr.save(dbJob);    
  	}
  	catch(Exception ex) {
  		logger.error("Error in executing the plugload image upgrade work " + ex.getMessage());
  	}
    
  } //end of method run
     
  public void startImageUpload(Long[] plugloadIds, String fileName) {
      
  	if(logger.isInfoEnabled()) {
  		logger.info("plugload upgrade request on " + plugloads.length + " using " + fileName);
  	}
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
    if(logger.isDebugEnabled()) {
    	logger.debug("file size - " + fileArray.length);
    }

    //version is in the job @TODO check with this version
    //this is based on the file naming convention being followed
    targetBuildNo = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
    appType = ServerConstants.ISP_APP2_INIT_OPCODE;
    
    int noOfPlugloads = plugloadIds.length;    
    long startTime = System.currentTimeMillis();    
        
    //group fixtures based on the gateway with which they are communicating
    HashMap<Long, ArrayList<Plugload>>gwPlugloadMap = new HashMap<Long, ArrayList<Plugload>>();
    for(int i = 0; i < noOfPlugloads; i++) {
      Plugload plugload = plugloadMgr.getPlugloadById(plugloadIds[i]);   
      if(plugload == null) {
      	if(logger.isInfoEnabled()) {
      		logger.info(plugloads[i] + ": There is no Plugload");
      	}
        continue;
      }      
      Long gwId = plugload.getSecGwId();
      if(!gwPlugloadMap.containsKey(gwId)) {
      	gwPlugloadMap.put(gwId, new ArrayList<Plugload>());	
      }
      gwPlugloadMap.get(gwId).add(plugload);      
    }
    
    ImageUpgradeSO.readUpgradeProperties();
    Iterator<Long> gwIter = gwPlugloadMap.keySet().iterator();    
    
    //for each gateway in the gateway map, create a thread and start it so that all the
    //gateway threads run in parallel
    while(gwIter.hasNext()) {
      Long gwId = gwIter.next();
      ArrayList<Plugload> plugloadList = gwPlugloadMap.get(gwId);
      GwPlugloadImgUpgThread thread = new GwPlugloadImgUpgThread(gwId, plugloadList, this);  
      gwPlugloadUpgThrMap.put(gwId, thread);
      thread.start();
    }    
    //waiting for all the gateway threads to finish
    while(gwPlugloadUpgThrMap.size() > 0) {
      ServerUtil.sleep(5);
    }
    if(logger.isInfoEnabled()) {
    	logger.info("Time taken to upload to all plugloads -- " + (System.currentTimeMillis() - startTime));
    }
        
  } //end of method startImageUpload
  
  //class to handle upgrade of all the plugloads that belong to one gateway
  //there will be one thread per gateway
  class GwPlugloadImgUpgThread extends Thread {
    
    private long gwId;
    //list of plugloads that are part of the image upgrade
    private ArrayList<Plugload> plugloadList  = new ArrayList<Plugload>();
    
    //fixture list maintained for acks. these are plugload for which acks are expected
    private ArrayList<Long> plugloadAckList = new ArrayList<Long>();

    //this list contain plugloads for which image upgrade is pending
    //i.e. image upgrade is started and not yet finished
    //TODO remove this and reuse plugloadInProgressMap for this purpose
    private ArrayList<Long> imgUpPendingPlugloadList = new ArrayList<Long>();
    
    //it is a list of missing offset (multiple of 64)
    private LinkedList<Integer> missingOffsetList = new LinkedList<Integer>();
    
    //cache to hold image upgrade details of all devices
    private Map<Long, PlugloadImgUpgrDetails> plugloadInProgressMap = Collections.synchronizedMap(
    		new HashMap<Long, PlugloadImgUpgrDetails>());  
    
    private int initAttempts = 2;
    private boolean inProgress = false;
    private boolean bucket8kFinished = false; //bucket is 8k
    private int lastBucket = 0;    
    private int currRetryAttempts = 0; //no. of attempts 
    private int maxAttempts = 2;
    
    private PlugloadImageUpgradeWorker worker = null;
   
    int interPktDelay = ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY_2;
    Gateway gw = null;
    
    public GwPlugloadImgUpgThread(long gwId, ArrayList<Plugload> plugloadList, 
    		PlugloadImageUpgradeWorker worker) {
      
      this.gwId = gwId;
      this.plugloadList = plugloadList;
      this.worker = worker;
      this.maxAttempts = dbJob.getNoOfRetries(); //ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES + 1;
      
    } //end of constructor
    
    public void run() {
      
    	gw = gwMgr.loadGateway(gwId);
    	interPktDelay = ImageUpgradeSO.getImageUpgradeInterPacketDelay(gw.getApp2Version());
      //if there are any other image upgrade jobs running on the same gateway
      //wait for them to be finished
      while(true) {
      	if(aborted) {
      		return;
      	}
      	synchronized(ImageUpgradeSO.gwThreadLock) {
      		if(!ImageUpgradeSO.getInstance().gwFixtUpgrMap.containsKey(gwId) &&
      				!ImageUpgradeSO.getInstance().gwUpgrMap.containsKey(gwId) &&
      				!ImageUpgradeSO.getInstance().gwPlugloadUpgrMap.containsKey(gwId) &&
      				!ImageUpgradeSO.getInstance().gwWdsUpgrMap.containsKey(gwId)) {
      			ImageUpgradeSO.getInstance().gwPlugloadUpgrMap.put(gwId, worker);
      			break;
      		}
      	}
      	if(logger.isInfoEnabled()) {
      		logger.info(gwId + ": other image upgrade job is running on this gw");
      	}
      	ServerUtil.sleep(10);
      }
      int noOfPlugloads = plugloadList.size();
      //create details object for each plugload in the plugload list      
      for(int i = 0; i < noOfPlugloads; i++) {	
      	Plugload plugload = plugloadList.get(i);
      	PlugloadImgUpgrDetails details = new PlugloadImgUpgrDetails();
      	details.appUpgraded = appType;
      	details.targetVersion = targetBuildNo;
      	details.status = ServerConstants.IMG_UP_STATUS_SCHEDULED;
      	details.fileName = fileName;
      	details.noOfAttempts = 0;
      	plugloadInProgressMap.put(plugload.getId(), details);     	      
      }
      
      //try for max retry attempts
      boolean retryReq = true;
      while(currRetryAttempts < maxAttempts) {
      	if(aborted) {
      		break;
      	}
      	//stop the thread if it is past the stop time of job
      	if(dbJob.getStopTime() != null && !dbJob.getStopTime().after(new Date())) {
      		if(logger.isInfoEnabled()) {
      			logger.info(gwId + ": it is past the job's stop time");
      		}
    			break;
      	}
      	try {
      		if(currRetryAttempts > 0) { //if it is 2nd attempt
      			ServerUtil.sleep(dbJob.getRetryInterval());
      		}
      		initiateImageUpgrade();
      		if(aborted) {
      			break;
      		}
      	}
      	catch(Exception e) {
      		e.printStackTrace();
      	}
      	retryReq = true;
      	//give 15 seconds for fixtures to return the current version
      	for(int i = 0; i < 3; i++) {
      		if(plugloadInProgressMap.size() == 0) {
      			//all plugload are done with upgrade successfully
      			if(logger.isInfoEnabled()) {
      				logger.info(gwId + ": all the plugloads are done no need to retry");
      			}
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
      	if(currRetryAttempts < maxAttempts) {
      		//all attempts are not done but came out so either aborted, upgrade window completed or all succeeded
      		if(!retryReq) {
        		//all plugloads are successfully upgraded
        		return;
        	}
      	} else {
      		//done with all the attempts
      		//wait for another couple of minutes as sensors are taking time to boot in 2.0
      		for(int i = 0; i < 24; i++) {
      			if(plugloadInProgressMap.size() == 0) {
      				//all plugloads are done with upgrade successfully or aborted
      				if(logger.isInfoEnabled()) {
      					logger.info(gwId + ": all the plugloads are done no need to wait");
      				}
      				break;
      			}  
      			ServerUtil.sleep(5);
      		}
      	}
      	//go through the plugloads and mark them as failed
      	for(int i = 0; i < noOfPlugloads; i++) {
      		Plugload plugload = plugloadList.get(i);
      		PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugload.getId());
      		if(details == null) {
      			//this plugload is already removed from the map. it is successful
      			continue;
      		}	
      		String description = "";
      		if(details.status.equals(ServerConstants.IMG_UP_STATUS_ABORTED)) {
      			//image upgrade didn't start on this plugload  
      			description = "Image upgrade of Application with image " + fileName + " aborted";      		
      		} else if(details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
      			//image upgrade didn't start on this plugload  
      			description = "Image upgrade of Application with image " + fileName + " didn't start";      			
      		} else if(details.status.equals(ServerConstants.IMG_UP_STATUS_INPROGRESS)) {	  
      			description = "Image upgrade of Application with image " + fileName + " didn't finish";      			 
      		} else {
      			//it is failed
      			if(details.description == null || details.description.equals("")) {
      				description = "Image upgrade of Application with image " + fileName + " failed";      				
      			} else {
      				description = details.description;
      			}
      		}
      		logger.error(plugload.getId() + ": " + description);
      		//update the device status as failed in the database
      		if(!aborted) {
      			plugloadMgr.setImageUpgradeStatus(plugload.getId(), ServerConstants.IMG_UP_STATUS_FAIL);
      			firmUpMgr.finishDeviceUpgrade(dbJob.getId(), plugload.getId(), 
      					ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description, "");
      		} else {
      			plugloadMgr.setImageUpgradeStatus(plugload.getId(), ServerConstants.IMG_UP_STATUS_ABORTED);
      			firmUpMgr.finishDeviceUpgrade(dbJob.getId(), plugload.getId(), 
      					ServerConstants.IMG_UP_STATUS_ABORTED, details.noOfAttempts, description, "");
      		}
      		eventMgr.addEvent(plugload, description, EventsAndFault.FIXTURE_IMG_UP_STR);
      		plugloadInProgressMap.remove(plugload.getId());
      	}
      }
      catch(Exception e) {
      	e.printStackTrace();
      }
      finally {
      	gwPlugloadUpgThrMap.remove(gwId);
      	ImageUpgradeSO.getInstance().gwPlugloadUpgrMap.remove(gwId);
      }
      
    } //end of method run
    
    private void initiateImageUpgrade() {
      
    	//iterator through new plugload list to remove the successful fixtures
      Iterator<Plugload> plugloadIter = plugloadList.iterator();
      while(plugloadIter.hasNext()) {
      	Plugload plugload = plugloadIter.next();
      	PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugload.getId());
      	if(details == null || details.status.equals(ServerConstants.IMG_UP_STATUS_SUCCESS)) {
      		//it is already removed or it was successful
      		plugloadIter.remove();
      	}	
      }
           
      if(plugloadList.size() > 0) {
      	startMulticastImageUpload(plugloadList, appType);
      }
             
      //if the job is aborted update the status to aborted on all the in progress devices and move on
      if(aborted) {
      	Iterator<Long> inProgressIter = plugloadInProgressMap.keySet().iterator();
      	while(inProgressIter.hasNext()) {
      		Long fixtId = inProgressIter.next();
      		PlugloadImgUpgrDetails details = plugloadInProgressMap.get(fixtId);
      		details.status = ServerConstants.IMG_UP_STATUS_ABORTED;
      	}
      	return;
      }
      
      //image upload is complete. give 45 seconds for fixtures to send node boot info
      boolean allPlugloadsDone = false;
      for(int i = 0; i < 9; i++) {
      	if(plugloadInProgressMap.size() == 0) {
      		//all plugloads are done with upgrade successfully
      		if(logger.isInfoEnabled()) {
      			logger.info(gwId + ": all plugloads are done no need to wait for node boot info");
      		}
      		allPlugloadsDone = true;
      		break;
      	}
      	ServerUtil.sleep(5);	  
      }
      
      if(allPlugloadsDone) {
      	return;
      }
      //go through the plugloads and process them as required
      int noOfPlugloads = plugloadList.size();  
      ArrayList<Long> plugloadIdList = new ArrayList<Long>();
      for(int i = 0; i < noOfPlugloads; i++) {
      	Plugload plugload = plugloadList.get(i);
      	PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugload.getId());
      	if(details == null) {
      		//this plugload is already removed from the map as it is successful
      		continue;
      	}
      	if(details.status.equals(ServerConstants.IMG_UP_STATUS_INPROGRESS)) {
      		//image upgrade status is in progress. retrieve the version and check      		
      		plugloadIdList.add(plugload.getId());      		
      	}	
      }      
      if(plugloadIdList.size() > 0) {
      	int[] plugloadArr = new int[plugloadIdList.size()];
      	for(int i = 0; i < plugloadArr.length; i++) {
      		plugloadArr[i] = plugloadIdList.get(i).intValue();
      	}
      	DeviceServiceImpl.getInstance().getMulticastCurrentVersion(plugloadArr);
      }
	
    } //end of method initiateImageUpgrade
        
    public void startMulticastImageUpload(ArrayList<Plugload> plugloadList, int imgType) {
      
      //if the end time of the job has reached stop the upgrade rest of the plugloads will be upgraded next day
    	if(dbJob.getEndTime() != null && !new Date().before(dbJob.getEndTime())) {
    		return;
    	}
      
      //send init packet to the plugload    
      missingOffsetList.clear();     
      initAttempts = 2;      
      String msg = ": image upgrade initiated on plugloads - ";    
      int noOfPlugloads = plugloadList.size();
      Plugload plugload = null;
      Long[] plugloadIds = new Long[noOfPlugloads];
      for(int i = 0; i < noOfPlugloads; i++) {
        if(i > 0) {
          msg += ", ";
        }
        plugload = plugloadList.get(i); 
        plugloadAckList.add(plugload.getId());
        msg += plugload.getName();        
        String description = "Image upgrade of Application with image " + fileName + " initiated";              
        eventMgr.addEvent(plugload, description, EventsAndFault.FIXTURE_IMG_UP_STR);
        plugloadIds[i] = plugload.getId();
        PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugload.getId());	  
        if(details != null) {
          details.noOfAttempts++;
        }
      }
      if(currRetryAttempts == 0) {
      	//update the device status as in progress in the db only in the first attempt
      	firmUpMgr.startDeviceUpgrade(dbJob.getId(), plugloadIds);
      }
      if(logger.isDebugEnabled()) {
      	logger.debug(gwId + msg + " at " + new Date());
      }
      //send the init packet to all the fixtures two times
      while(initAttempts > 0) {        
        try {	
          multicastInitPacket(fileArray.length, plugloadList, imgType, interPktDelay);	
        }
        catch(Exception ex) {
          ex.printStackTrace();
        }
        ServerUtil.sleep(15);
        initAttempts--;
        if(inProgress) {
          //ack is received from all plugload or ready to send the file
          break;
        }
      }
      
      //if there are any pending plugloads, proceed
      if(imgUpPendingPlugloadList.size() > 0) {
      	inProgress = true;
      } else {
      	// no plugload has returned ack for initiate image upgrade
      	return;
      }
    
      if(logger.isInfoEnabled()) {
      	logger.info(gwId+ ": no. of pending plugloads- " + imgUpPendingPlugloadList.size());
      }
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
      plugloadAckList.clear();
      plugloadAckList.addAll(imgUpPendingPlugloadList);	
      while(offset <= fileSize && inProgress) {
      	if(aborted) {
      		inProgress = false;
      		break;
      	}
      	if(sectorSize > 8192) { //8k boundary is crossed
      		offset = offset - (offset % 8192);
      	}
      	int remainingSize = fileSize - offset;	  
      	if(remainingSize <= 0) {
      		//at the end also,  missing packets
      		sendMissingPkts(plugload, dataSize);
      		if(logger.isInfoEnabled()) {
      			logger.info(gwId + ": time taken to upload on all plugload - " +
      					(System.currentTimeMillis() - startTime));
      		}
      		inProgress = false;
      	  int noOfFailedPlugloads = plugloadAckList.size();
      	  for(int i = 0; i < noOfFailedPlugloads; i++) {
      	    logger.error(plugloadAckList.get(i) + ": image upgrade failed to get the ack for end of file");	      
      	    imgUpPendingPlugloadList.remove(plugloadAckList.get(i));
      	  }
      	  break;
      	}
  	    
      	//after 8k wait for missing packet requests for 2 sec and proceed     
      	if(offset > 0 && offset % (8 * 1024) == 0) {	
      		//proceed with the retransmission of missing packets and go through remaining file
      		sendMissingPkts(plugload, dataSize);
      		sectorSize = 0;
      		int noOfFailedPlugloads = plugloadAckList.size();
      		for(int i = 0; i < noOfFailedPlugloads; i++) {
      			long failedPlugloadId = plugloadAckList.get(i);
      			logger.error(failedPlugloadId + ": image upgrade failed as ack is missed");	    
      			imgUpPendingPlugloadList.remove(failedPlugloadId);
      		}
      		plugloadAckList.clear();
      		plugloadAckList.addAll(imgUpPendingPlugloadList);	    
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
      		sendImagePacket(gwId, offset, remainingSize, false,
      				ImageUpgradeSO.IMG_UP_NO_RETRANSMITS, interPktDelay);	  
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
    
    public void ackImageUploadStart(final Plugload plugload) {
      
      final long plugloadId = plugload.getId();
      if(logger.isDebugEnabled()) {
      	logger.debug(plugloadId + ": got ack for image upload start");
      }
      if(!plugloadAckList.contains(plugloadId) && !imgUpPendingPlugloadList.contains(plugloadId)) {      
        logger.error(plugloadId + ":plugload is not in the upgrade list. so, ignoring the ack");
        return;
      }
      synchronized(ackLock) {
        if(inProgress) {
          //already file transfer is in progress so ignore
        	if(logger.isDebugEnabled()) {
        		logger.debug(plugloadId + ":file transfer is already in progress. so ignoring the ack");
        	}
          return;
        }
        PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugloadId);    
        if(details != null && details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
          plugloadMgr.setImageUpgradeStatus(plugloadId, ServerConstants.IMG_UP_STATUS_INPROGRESS);
          details.status = ServerConstants.IMG_UP_STATUS_INPROGRESS;
          String description = "Image upgrade of Application with image " + details.fileName + " started";          
          eventMgr.addEvent(plugload, description, EventsAndFault.FIXTURE_IMG_UP_STR); 
        }    
        if(!imgUpPendingPlugloadList.contains(plugloadId)) {
          //adding the plugload to pending list and removing it from ack list
          imgUpPendingPlugloadList.add(new Long(plugloadId));
          plugloadAckList.remove(plugloadId);
        }
        if(initAttempts > 0 && plugloadAckList.size() > 0) {
          //init ack didn't come from all the plugloads and there are still init packet
          //attempts left so don't start file upload yet
          return;
        } else {
          //set the in progress flag to true so the file transfer starts
          inProgress = true;          
        }
      }                
      
    } //end of method ackImageUploadStart
    
    private void sendMissingPkts(Plugload plugload, int dataSize) {
      
      bucket8kFinished = false;
      lastBucket++;
      long plugloadId = plugload.getId();
      int fileSize = fileArray.length;    
      long waitTime = 3 * 60 * 1000;
      long startWaitingTime = System.currentTimeMillis();
      if(logger.isInfoEnabled()) {
      	logger.info(gwId + ": no. of nodes to expect ack from- " + plugloadAckList.size());
      }
      while(true) {         
        if(!inProgress) { //upgrade is aborted/finished	
          break;
        }        
        if(plugloadAckList.size() == 0) { 
          //bucket finished message came from all plugloads
        	if(logger.isDebugEnabled()) {
        		logger.debug(gwId + ": positive ack came in " + 
        				(System.currentTimeMillis() - startWaitingTime) + " ms");
        	}
          break;
        }      
        if(imgUpPendingPlugloadList.size() == 0) {
          //image upgrade finished on all the plugloads. may be final acks didn't come
          //but they might have aborted or rebooted successfully
          inProgress = false;
          break;
        }
        if(System.currentTimeMillis() - startWaitingTime > waitTime) {
          if(plugloadAckList.size() == imgUpPendingPlugloadList.size()) {
            logger.error(gwId + ": Aborting as no packets received from all plugloads");
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
            if(logger.isDebugEnabled()) {
            	logger.debug(gwId + ":requested offset(retrieving) -- " + offset);
            }
            //for the last packet it may not be 64 bytes
            int remainingSize = fileSize - offset;
            if(remainingSize <= 0) {
              //offset more than the file size
              continue;
            }
            if(remainingSize < dataSize) { //last packet
              //dataSize = remainingSize;
            	if(logger.isDebugEnabled()) {
              logger.debug(gwId + ":last packet requested filesize - " + fileSize + 
              		" datasize - " +dataSize);
            	}
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

    HashMap<Long, Long> missingPktSeqMap = new HashMap<Long, Long>();
    //HashMap<Long, Integer> missingPktCountMap = new HashMap<Long, Integer>();
     
    public synchronized void missingPacketRequest(Plugload plugload, byte[] packet) {
          
    	if(logger.isDebugEnabled()) {
    		logger.debug(plugload.getId() + ": missing packet request - " + ServerUtil.getLogPacket(packet));
    	}
      if(!inProgress) { //upgrade is aborted
        return;
      }
         
      if(!imgUpPendingPlugloadList.contains(plugload.getId())) {
      	return; //this plugload is not in the pending list so ignoring the missing packet request
      }
      short index = 0;
      short bitMap = 0;
      int baseOffset = 512;
      int missingOffset = 0;
      byte startMarker = packet[0];    
      int noOfMissingPkts = packet[ServerConstants.RES_CMD_PKT_MSG_START_POS];
      int offsetPos = packet.length - 5;      
      if(noOfMissingPkts == 0) {
        byte[] tempArr = { packet[offsetPos], packet[offsetPos + 1], packet[offsetPos + 2], 
        		packet[offsetPos + 3] };
        int receivedOffset = ServerUtil.byteArrayToInt(tempArr);
        if(receivedOffset == lastBucket) {
          if(!bucket8kFinished) {
            plugloadAckList.remove(plugload.getId());
          } else {
            //this is a ack packet came after bucket is finished
            return;
          }
          if(plugloadAckList.size() == 0) { 
            //0 buckets came from all the fixtures
            bucket8kFinished = true;
          }
          int lastBucketIndex = (fileArray.length / 8192) - 1;
          if(fileArray.length % 8192 > 0) {
            lastBucketIndex++;
          }
          if(lastBucket == lastBucketIndex) {
          	if(logger.isInfoEnabled()) {
          		logger.info(plugload.getId() + ": file transfer finished");
          	}
            imgUpPendingPlugloadList.remove(plugload.getId());	  
          }
        }      
        return;
      }    
      //missing pkt seq no. 
      byte[] seqTempArr = { packet[ServerConstants.CMD_PKT_TX_ID_POS], packet[ServerConstants.CMD_PKT_TX_ID_POS + 1], 
      		packet[ServerConstants.CMD_PKT_TX_ID_POS + 2], packet[ServerConstants.CMD_PKT_TX_ID_POS + 3] };
      long seqNo = ServerUtil.intByteArrayToLong(seqTempArr);   
      if(missingPktSeqMap.containsKey(plugload.getId())) {
      	if(seqNo == missingPktSeqMap.get(plugload.getId())) {
      		//same packet is received. probably it is hopped duplicate packet. so ignoring the the packet
      		return; //not processing again as it is processed earlier
      	}    		
      }
      missingPktSeqMap.put(plugload.getId(), seqNo);      
      int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS + 1;
      if(startMarker == ServerConstants.FRAME_START_MARKER) { //old packet
        pktIndex = 4;
      }
      int noOf64BytePkts = 0;
      for(int k = 0; k < noOfMissingPkts; k++) {
        byte[] tempArr = { packet[pktIndex++], packet[pktIndex++]};
        index = (short)ServerUtil.byteArrayToShort(tempArr);
        //index = (short)(0x00FF & ((short)packet[pktIndex++]));
        bitMap = (short)(0x00FF & ((short)packet[pktIndex++]));      
        for(int j = 0; j < 8; j++) {
        	if(!ServerUtil.isBitSet(bitMap, j)) {
        		missingOffset = (baseOffset * index) + j * 64;
        		if(logger.isDebugEnabled()) {
        			logger.debug(plugload.getId() + ": requested offset(adding) -- " + missingOffset);
        		}
        		noOf64BytePkts++;
        		synchronized(missingOffsetList) {
        			if(!missingOffsetList.contains(new Integer(missingOffset))) {
        				missingOffsetList.add(new Integer(missingOffset));
        			}
        		}
        	}
        }
      } 
      if(logger.isDebugEnabled()) {
      	logger.debug(plugload.getId() + ": no. of missing - " + noOf64BytePkts + ", last bucket - " + lastBucket);
      }
      if(noOf64BytePkts == 128) { //ignoring the node if it is asking for entire 8k chunk
      	logger.error(plugload.getId() + ": requesting for 8k chunk, ignoring the plugload");
      	imgUpPendingPlugloadList.remove(plugload.getId());
      	plugloadAckList.remove(plugload.getId());
      	return;
      } 
      /*
      if(missingPktCountMap.containsKey(plugload.getId()) && noOf64BytePkts >= 32) {
      	if(noOf64BytePkts == missingPktCountMap.get(plugload.getId())) {
      		//subsequent missing packet request also have same no. of missing packets and at least 32 packets are missed      	
      		logger.error(plugload.getId() + ": requesting for " + noOf64BytePkts + "again. ignoring the plugload");
      		imgUpPendingPlugloadList.remove(plugload.getId());
      		plugloadAckList.remove(plugload.getId());
      		return;      		
      	}
      } 
      missingPktCountMap.put(plugload.getId(), noOf64BytePkts);
      */
    	             
    } //end of method missingPacketRequest
    
    public void cancelFileUpload(Plugload plugload, byte[] packet) {
      
      logger.error(plugload.getId() + ": abort message - " + ServerUtil.getLogPacket(packet));    
      //for point to point this inProgress should be false
      plugloadAckList.remove(plugload.getId());
      imgUpPendingPlugloadList.remove(plugload.getId());
      PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugload.getId());
      
      if(details != null) {        
      	details.status = ServerConstants.IMG_UP_STATUS_FAIL;
      	details.description = "Image upgrade of Application with image " + details.fileName + " aborted";
      }
      if(imgUpPendingPlugloadList.size() == 0) { //abort came/image upload finished on all plugloads
        inProgress = false;
      }
      
    } //end of method cancelFileUpload
    
    //this is in response to get current version    
    public void currentNodeVersion(Plugload plugload, byte[] packet) {
      
      long plugloadId = plugload.getId();
      if(logger.isDebugEnabled()) {
      	logger.debug(plugloadId + ": current node version - " + ServerUtil.getLogPacket(packet));
      }
      int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS;
      //version is from 4th index to 9th index
      byte releaseNo = packet[pktIndex++];
      //byte day = packet[4];
      //byte year = packet[5];
          
      byte[] revisionArr = { packet[pktIndex++], packet[pktIndex++] }; //5, 6
      short revision = (short)ServerUtil.byteArrayToShort(revisionArr);
      
      byte major = packet[pktIndex++]; //7th
      byte minor = packet[pktIndex++]; //8th
      byte appId = packet[pktIndex++]; //9th
           
      String swVersion = major + "." + minor + "." + releaseNo + " b" + revision;    
      if(logger.isInfoEnabled()) {
      	logger.info(plugloadId + ": image(" + swVersion + ") booted to app" + appId);
      }
           
      //if(major == majorVer && minor == minorVer && appId == 2) {
      plugload.setLastConnectivityAt(new Date());
      if(appId == 2) {
      	if(logger.isInfoEnabled()) {
      		logger.info(plugloadId + " Image upgrade successfull");
      	}
        plugloadMgr.updateVersion(swVersion, plugloadId, gwId);
      } else {  
      	if(logger.isInfoEnabled()) {
      		logger.info(plugloadId + " Image upgrade failed");
      	}
        plugloadMgr.updateFirmwareVersion(swVersion, plugloadId, gwId);
      }
      PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugloadId);
      if(details == null) {
      	return;
      }
      //current version is not returning app1 version so only invoke the update
      //function for app2 upgrade
      if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) {
      	updateUpgradeCompleteStatus(plugload, details, appId, revision, 0, 0, swVersion);
      }
      
    } //end of method currentNodeVersion
    
    private String updateUpgradeCompleteStatus(Plugload plugload, PlugloadImgUpgrDetails details, 
    		byte appId, int currVer, int otherVer, int imgUpgrStatus, String version) {
      
      boolean imageUpgradeSuccess = false;
      String imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_FAIL;
      if(currRetryAttempts < (maxAttempts - 1)) {
      	imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_INPROGRESS;
      }
      //for app2 upgrade, app2 is erased. so, current app 2 means it booted properly with
      //new image
      if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) { //app2 is upgraded
      	details.description = "Image upgrade with image " + details.fileName;
        if(appId == 2) { //booted to app2        	 
        	if(imgUpgrStatus == 0) {
        		if(details.targetVersion == currVer) {        		
        			imageUpgradeSuccess = true;
        		} else {
        			details.description += " failed as plugload rebooted with old image(" + currVer + ")";
        		}
        	} else {
        		details.description += " failed as plugload rebooted during upgrade";
        	}        	
        } else {
        	details.description += " failed as plugload rebooted with Other image";
          logger.error(plugload.getId() + ":" + details.description);
        }
      }      
      if(imageUpgradeSuccess) {
      	details.status = ServerConstants.IMG_UP_STATUS_SUCCESS;
      	details.description += " successful";
      	plugloadInProgressMap.remove(plugload.getId());
      	if(logger.isInfoEnabled()) {
      		logger.info(plugload.getId() + ":image upgrade successful and plugload rebooted. New version : " + 
      				plugload.getVersion() + "," + plugload.getFirmwareVersion());
      	}
        noOfSusSuccess++;
        plugloadMgr.setImageUpgradeStatus(plugload.getId(), ServerConstants.IMG_UP_STATUS_SUCCESS);
        eventMgr.addEvent(plugload, details.description, EventsAndFault.FIXTURE_IMG_UP_STR);	
        firmUpMgr.finishDeviceUpgrade(dbJob.getId(), plugload.getId(), 
            ServerConstants.IMG_UP_STATUS_SUCCESS, details.noOfAttempts, details.description, version);
        imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_SUCCESS;
      } else {
      	details.status = ServerConstants.IMG_UP_STATUS_FAIL;      	        
      }
      return imgUpgrStatusStr;
      
    } //end of method updateUpgradeCompleteStatus
    
    public String nodeRebooted(Plugload plugload, short currVer, short otherVer, byte appId, int imgUpgrStatus, 
    		String version) {
      
      long plugloadId = plugload.getId();
         
      //image upgrade was pending on this plugload. abort it
      imgUpPendingPlugloadList.remove(new Long(plugloadId));
      if(imgUpPendingPlugloadList.size() == 0) {
        inProgress = false;
      }
      if(!plugloadInProgressMap.containsKey(plugloadId)) {
        return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
      }            
      PlugloadImgUpgrDetails details = plugloadInProgressMap.get(plugloadId);
      if(details.appUpgraded == 3) {
        return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
      }
      return updateUpgradeCompleteStatus(plugload, details, appId, currVer, otherVer, imgUpgrStatus, version);
          
    } //end of method nodeRebooted
    
    private void sendImagePacket(long gwId, int offset, int remainingSize, boolean missing, int retransmits, 
    		int interPktDelay) {
	    
      if(retransmits > 10) {
      	retransmits = 10;
      } else if(retransmits < 1) {
      	retransmits = 1;
      }
      
      for(int i = 0; i < retransmits; i++) {
      	if(missing) {
      		if(logger.isDebugEnabled()) {
      			logger.debug(gwId + ":sending missing img packet(" + (i+1) + "): " + offset);
      		}
      	} else {
      		if(logger.isDebugEnabled()) {
      			logger.debug(gwId + ":sending img packet(" + (i+1) + "): " + offset);
      		}
      	}
      	broadcastDataPacket(offset, remainingSize, gwId);      	
      	ServerUtil.sleepMilli(interPktDelay/ImageUpgradeSO.IMG_UP_NO_RETRANSMITS);
      }
      
    } //end of method sendImagePacket
    
  } //end of class GwFixtImgUpgThread  
  
  private void multicastInitPacket(int fileSize, ArrayList<Plugload> plugloadList, 
      int imageType, int interPktDelay) {
    
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
    ArrayList<Plugload> tempList = new ArrayList<Plugload>();
    int noOfPlugloads = plugloadList.size();
    for(int k = 0; k < noOfPlugloads; k++) {
      tempList.add(plugloadList.get(k));
      if((k == (noOfPlugloads - 1)) || ((k + 1) % ImageUpgradeSO.IMG_UP_NO_MULTICAST_TARGETS == 0)) {
      	PlugloadImpl.getInstance().sendMulticastPacket(tempList,
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
  
  public void cancelFileUpload(Plugload plugload, byte[] packet, long gwId) {
    
    if(gwPlugloadUpgThrMap.containsKey(gwId)) {
      gwPlugloadUpgThrMap.get(gwId).cancelFileUpload(plugload, packet);
    }
    
  } //end of method cancelFileUpload
  
  //this is called from node boot info
  public String nodeRebooted(Plugload plugload, short currVer, short otherVer, byte appId,
      long gwId, int imgUpgrStatus, String version) {
    
    if(gwPlugloadUpgThrMap.containsKey(gwId)) {
      return gwPlugloadUpgThrMap.get(gwId).nodeRebooted(plugload, currVer, otherVer, appId, imgUpgrStatus, version);
    }
    return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
        
  } //end of method nodeRebooted
  
  public void ackImageUploadStart(final Plugload plugload, long gwId) {
    
    if(gwPlugloadUpgThrMap.containsKey(gwId)) {
      gwPlugloadUpgThrMap.get(gwId).ackImageUploadStart(plugload);
    }
    
  } //end of method ackImageUploadStart
  
  public synchronized void missingPacketRequest(Plugload plugload, byte[] packet, long gwId) {
    
    if(gwPlugloadUpgThrMap.containsKey(gwId)) {
      gwPlugloadUpgThrMap.get(gwId).missingPacketRequest(plugload, packet);
    }
    
  } //end of method missingPacketRequest
  
  //this is in response to get current version
  public void currentNodeVersion(Plugload plugload, byte[] packet, long gwId) {
    
    if(gwPlugloadUpgThrMap.containsKey(gwId)) {
      gwPlugloadUpgThrMap.get(gwId).currentNodeVersion(plugload, packet);
    }
    
  } //end of method currentNodeVersion
  
} //end of class PlugloadImageUpgradeWorker
