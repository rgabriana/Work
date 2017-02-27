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
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;

/**
 * @author Sreedhar
 * this class takes care of upgrading the fixtures
 * fixtures belonging to different gateways are upgraded in parallel as they
 * are run in different threads
 *
 */
public class FixtureImageUpgradeWorker extends Thread {
    
  private static final Logger logger = Logger.getLogger("ImageUpgrade");
  
  private FixtureManager fixtureMgr = null;
  private EventsAndFaultManager eventMgr = null;
  private FirmwareUpgradeManager firmUpMgr = null;
  private GatewayManager gwMgr = null;
  
  private int[] devices; //array of fixtures which are upgraded by this worker
  private String fileName; //image file name
  private int targetBuildNo; //retrieved from the file name
  
  private ImageUpgradeDBJob dbJob = null;
  private int noOfSusSuccess = 0;
  
  private int appType;
  private byte[] fileArray = null; //array of binary data of the image file
  
  //hash map of gateway threads
  private HashMap<Long, GwFixtImgUpgThread> gwFixUpgThrMap = new HashMap<Long, 
  	GwFixtImgUpgThread>();  
       
  //structure to hold all the image upgrade details per fixture
  class FixtImgUpgrDetails {
    
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
    
  } //end of class FixtImgUpgrDetails

  /**
   * 
   */
  public FixtureImageUpgradeWorker(ImageUpgradeJob job) {
    
    this.devices = job.getDeviceIds();
    this.fileName = job.getFileName();
    this.dbJob = job.getImageUpgradeDBJob();
     
    fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    firmUpMgr = (FirmwareUpgradeManager)SpringContext.getBean("firmwareUpgradeManager");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    
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
  		logger.error("Error in executing the fixture image upgrade work " + ex.getMessage());
  	}
    
  } //end of method run
     
  public void startImageUpload(int[] fixtureIds, String fileName) {
         
    logger.info("fixture upgrade request on " + fixtureIds.length + 
	" using " + fileName);
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
    if(fileName.toLowerCase().indexOf("su_firm") != -1) {
      //app1 upgrade
      appType = ServerConstants.ISP_APP1_INIT_OPCODE;
    }   
    
    int noOfFixtures = fixtureIds.length;    
    long startTime = System.currentTimeMillis();    
        
    //group fixtures based on the gateway with which they are communicating
    HashMap<Long, ArrayList<Fixture>>gwFixtureMap = new HashMap<Long, ArrayList<Fixture>>();
    for(int i = 0; i < noOfFixtures; i++) {
      Fixture fixture = fixtureMgr.getFixtureById(fixtureIds[i]);   
      if(fixture == null) {
      	logger.info(fixtureIds[i] + ": There is no Fixture");
        continue;
      }      
      Long gwId = fixture.getSecGwId();
      if(!gwFixtureMap.containsKey(gwId)) {
      	gwFixtureMap.put(gwId, new ArrayList<Fixture>());	
      }
      gwFixtureMap.get(gwId).add(fixture);      
    }
    
    ImageUpgradeSO.readUpgradeProperties();
    Iterator<Long> gwIter = gwFixtureMap.keySet().iterator();    
    
    //for each gateway in the gateway map, create a thread and start it so that all the
    //gateway threads run in parallel
    while(gwIter.hasNext()) {
      Long gwId = gwIter.next();
      ArrayList<Fixture> fixtureList = gwFixtureMap.get(gwId);
      GwFixtImgUpgThread thread = new GwFixtImgUpgThread(gwId, fixtureList, this);  
      gwFixUpgThrMap.put(gwId, thread);
      thread.start();
    }    
    //waiting for all the gateway threads to finish
    while(gwFixUpgThrMap.size() > 0) {
      ServerUtil.sleep(5);
    }
    logger.info("Time taken to upload to all fixtures -- " + 
    		(System.currentTimeMillis() - startTime));
        
  } //end of method startImageUpload
  
  //class to handle upgrade of all the fixtures that belong to one gateway
  //there will be one thread per gateway
  class GwFixtImgUpgThread extends Thread {
    
    private long gwId;
    //list of fixtures that are part of the image upgrade
    private ArrayList<Fixture> fixtureList;
    
    //fixture list maintained for acks. these are fixtures for which acks are expected
    private ArrayList<Long> fixtureAckList = new ArrayList<Long>();

    //this list contain fixtures for which image upgrade is pending
    //i.e. image upgrade is started and not yet finished
    //TODO remove this and reuse fixtureInProgressMap for this purpose
    private ArrayList<Long> imgUpPendingFixList = new ArrayList<Long>();
    
    //it is a list of missing offset (multiple of 64)
    private LinkedList<Integer> missingOffsetList = new LinkedList<Integer>();
    
    //cache to hold image upgrade details of all fixtures
    private Map<Long, FixtImgUpgrDetails> fixtureInProgressMap = Collections.synchronizedMap(
    		new HashMap<Long, FixtImgUpgrDetails>());  
    
    private int initAttempts = 2;
    private boolean inProgress = false;
    private boolean bucket8kFinished = false; //bucket is 8k
    private int lastBucket = 0;
    private boolean broadcastUpgr = true;
    private int currRetryAttempts = 0; //no. of attempts 
    private int maxAttempts = 2;
    
    //this cache is used to hold fixtures pre 1.4 as pre 1.4 fixtures have to be
    //upgraded 3 at a time.
    private ArrayList<Fixture>oldFixtureMulticastList = new ArrayList<Fixture>();
    
    private FixtureImageUpgradeWorker worker = null;
    
    //this oldFixtureList is for upgrading 1.3.x SUs in the old way
    ArrayList<Fixture> oldFixtureList = new ArrayList<Fixture>();
    ArrayList<Fixture> newFixtureList = new ArrayList<Fixture>();
    
    int interPktDelay = ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY_2;
    Gateway gw = null;
    
    public GwFixtImgUpgThread(long gwId, ArrayList<Fixture> fixtureList, 
    		FixtureImageUpgradeWorker worker) {
      
      this.gwId = gwId;
      this.fixtureList = fixtureList;
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
      			ImageUpgradeSO.getInstance().gwFixtUpgrMap.put(gwId, worker);
      			break;
      		}
      	}
      	logger.info(gwId + ": other image upgrade job is running on this gw");
      	ServerUtil.sleep(10);
      }
      int noOfFixtures = fixtureList.size();
      //create details object for each fixture in the fixture list      
      for(int i = 0; i < noOfFixtures; i++) {	
      	Fixture fixture = fixtureList.get(i);
      	FixtImgUpgrDetails details = new FixtImgUpgrDetails();
      	details.appUpgraded = appType;
      	details.targetVersion = targetBuildNo;
      	details.status = ServerConstants.IMG_UP_STATUS_SCHEDULED;
      	details.fileName = fileName;
      	details.noOfAttempts = 0;
      	fixtureInProgressMap.put(fixture.getId(), details);
      	if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
      		if(ServerUtil.compareVersion(fixture.getVersion(), "1.4.1") < 0) {
      			oldFixtureList.add(fixture);	  
      		} else {
      			newFixtureList.add(fixture);
      		}
      	} else {
      		//if the su is of new hardware, don't check for firmware version as 
      		//it could be 0. It should be always added to new fixture list
      		if(ServerUtil.compareVersion(fixture.getVersion(), "2.0") >= 0) {
      			newFixtureList.add(fixture);
      		} else { //old hardware
      			if(ServerUtil.compareVersion(fixture.getFirmwareVersion(), "1.4.1") < 0) {
      				oldFixtureList.add(fixture);	  
      			} else {
      				newFixtureList.add(fixture);
      			}
      		}
      	}	
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
      		if(fixtureInProgressMap.size() == 0) {
      			//all fixtures are done with upgrade successfully
      			logger.info(gwId + ": all the fixtures are done no need to retry");
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
      		//all fixtures are successfully upgraded
      		return;
      	}
      	//done with all the attempts
      	//wait for another couple of minutes as sensors are taking time to boot in 2.0
      	for(int i = 0; i < 24; i++) {
      		if(fixtureInProgressMap.size() == 0) {
      			//all fixtures are done with upgrade successfully or aborted
      			logger.info(gwId + ": all the fixtures are done no need to wait");      			
      			break;
      		}  
      		ServerUtil.sleep(5);
      	}
      	//go through the fixtures and mark them as failed
      	for(int i = 0; i < noOfFixtures; i++) {
      		Fixture fixture = fixtureList.get(i);
      		FixtImgUpgrDetails details = fixtureInProgressMap.get(fixture.getId());
      		if(details == null) {
      			//this fixture is already removed from the map. it is successful
      			continue;
      		}	
      		String description = "";
      		if(details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
      			//image upgrade didn't start on this fixture  
      			description = "Image upgrade of Application with image " + fileName + " didn't start";
      			if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
      				description = "Image upgrade of Firmware with image " + fileName + " didn't start";
      			}  
      		} else if(details.status.equals(ServerConstants.IMG_UP_STATUS_INPROGRESS)) {	  
      			description = "Image upgrade of Application with image " + fileName + " didn't finish";
      			if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
      				description = "Image upgrade of Firmware with image " + fileName + " didn't finish";
      			}  
      		} else {
      			//it is failed
      			if(details.description == null || details.description.equals("")) {
      				description = "Image upgrade of Application with image " + fileName + " aborted";
      				if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
      					description = "Image upgrade of Firmware with image " + fileName + " aborted";
      				}
      			} else {
      				description = details.description;
      			}
      		}
      		logger.error(fixture.getId() + ": " + description);
      		//update the device status as failed in the database
      		fixtureMgr.setImageUpgradeStatus(fixture.getId(), ServerConstants.IMG_UP_STATUS_FAIL);
      		firmUpMgr.finishDeviceUpgrade(dbJob.getId(), fixture.getId(), 
      				ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description, "");
      		eventMgr.addEvent(fixture, description, EventsAndFault.FIXTURE_IMG_UP_STR);
      		fixtureInProgressMap.remove(fixture.getId());
      	}
      }
      catch(Exception e) {
      	e.printStackTrace();
      }
      finally {
      	gwFixUpgThrMap.remove(gwId);
      	ImageUpgradeSO.getInstance().gwFixtUpgrMap.remove(gwId);
      }
      
    } //end of method run
    
    private void initiateImageUpgrade() {
      
    	//iterator through new fixture list to remove the successful fixtures
      Iterator<Fixture> newFixtIter = newFixtureList.iterator();
      while(newFixtIter.hasNext()) {
      	Fixture fixture = newFixtIter.next();
      	FixtImgUpgrDetails details = fixtureInProgressMap.get(fixture.getId());
      	if(details == null || details.status.equals(ServerConstants.IMG_UP_STATUS_SUCCESS)) {
      		//it is already removed or it was successful
      		newFixtIter.remove();
      	}	
      }
      //iterator through old fixture list to remove the successful fixtures
      Iterator<Fixture> oldFixtIter = oldFixtureList.iterator();
      while(oldFixtIter.hasNext()) {
      	Fixture fixture = oldFixtIter.next();
      	FixtImgUpgrDetails details = fixtureInProgressMap.get(fixture.getId());
      	if(details == null || details.status.equals(ServerConstants.IMG_UP_STATUS_SUCCESS)) {
      		//it is already removed or it was successful
      		oldFixtIter.remove();
      	}	
      }
           
      //upgrade new fixtures 
      broadcastUpgr = true;
      if(newFixtureList.size() > 0) {
      	startMulticastImageUpload(newFixtureList, appType);
      }
      
      //upgrade old fixtures
      broadcastUpgr = false;
      fixtureAckList.clear();
      imgUpPendingFixList.clear();
      missingPktSeqMap.clear();
      //missingPktCountMap.clear();
      int noOfOldFixtures = oldFixtureList.size();      
      for(int i = 0; i < noOfOldFixtures; i++) {
      	oldFixtureMulticastList.add(oldFixtureList.get(i));	
      	fixtureAckList.add(oldFixtureList.get(i).getId());
      	if((i == (noOfOldFixtures - 1)) || ((i + 1) % 3 == 0)) {	
      		startMulticastImageUpload(oldFixtureMulticastList, appType);
      		oldFixtureMulticastList.clear();
      		imgUpPendingFixList.clear();
      		fixtureAckList.clear();
      	}
      }      
      
      //image upload is complete. give 45 seconds for fixtures to send node boot info
      boolean allFixturesDone = false;
      for(int i = 0; i < 9; i++) {
      	if(fixtureInProgressMap.size() == 0) {
      		//all fixtures are done with upgrade successfully
      		logger.info(gwId + ": all fixtures are done no need to wait for node boot info");
      		allFixturesDone = true;
      		break;
      	}
      	ServerUtil.sleep(5);	  
      }
      
      if(allFixturesDone) {
      	return;
      }
      //go through the fixtures and process them as required
      int noOfFixtures = fixtureList.size();  
      ArrayList<Long> fixIdList = new ArrayList<Long>();
      for(int i = 0; i < noOfFixtures; i++) {
      	Fixture fixture = fixtureList.get(i);
      	FixtImgUpgrDetails details = fixtureInProgressMap.get(fixture.getId());
      	if(details == null) {
      		//this fixture is already removed from the map as it is successful
      		continue;
      	}
      	if(details.status.equals(ServerConstants.IMG_UP_STATUS_INPROGRESS)) {
      		//image upgrade status is in progress. retrieve the version and check
      		//DeviceServiceImpl.getInstance().getCurrentVersion(fixture);
      		fixIdList.add(fixture.getId());
      		//ServerUtil.sleepMilli(ImageUpgradeSO.IMG_UP_INTER_PKT_DELAY);
      	}	
      }      
      if(fixIdList.size() > 0) {
      	int[] fixArr = new int[fixIdList.size()];
      	for(int i = 0; i < fixArr.length; i++) {
      		fixArr[i] = fixIdList.get(i).intValue();
      	}
      	DeviceServiceImpl.getInstance().getMulticastCurrentVersion(fixArr);
      }
	
    } //end of method initiateImageUpgrade
    
    //private Object initAckObj = new Object();
    
    public void startMulticastImageUpload(ArrayList<Fixture> fixtureList, int imgType) {
      
      //send init packet to the fixture    
      missingOffsetList.clear();     
      initAttempts = 2;      
      String msg = ": image upgrade initiated on fixtures - ";    
      int noOfFixtures = fixtureList.size();
      Fixture fixture = null;
      Long[] fixtureIds = new Long[noOfFixtures];
      for(int i = 0; i < noOfFixtures; i++) {
        if(i > 0) {
          msg += ", ";
        }
        fixture = fixtureList.get(i); 
        fixtureAckList.add(fixture.getId());
        msg += fixture.getFixtureName();        
        String description = "Image upgrade of Application with image " + fileName + " initiated";
        if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
          description = "Image upgrade of Firmware with image " + fileName + " initiated";
        }        
        eventMgr.addEvent(fixture, description, EventsAndFault.FIXTURE_IMG_UP_STR);
        fixtureIds[i] = fixture.getId();
        FixtImgUpgrDetails details = fixtureInProgressMap.get(fixture.getId());	  
        if(details != null) {
          details.noOfAttempts++;
        }
      }
      if(currRetryAttempts == 0) {
      	//update the device status as in progress in the db only in the first attempt
      	firmUpMgr.startDeviceUpgrade(dbJob.getId(), fixtureIds);
      }
      logger.debug(gwId + msg + " at " + new Date());
      //send the init packet to all the fixtures two times
      while(initAttempts > 0) {        
        try {	
          multicastInitPacket(fileArray.length, fixtureList, imgType, interPktDelay);	
        }
        catch(Exception ex) {
          ex.printStackTrace();
        }
        //wait for some time so that fixture get the init request
//        try {
//        	initAckObj.wait(15);
//        }
//        catch(Exception ex) {
//        }
        ServerUtil.sleep(15);
        initAttempts--;
        if(inProgress) {
          //ack is received from all fixtures or ready to send the file
          break;
        }
      }
      
      //if there are any pending fixtures, proceed
      if(imgUpPendingFixList.size() > 0) {
      	inProgress = true;
      } else {
      	// no fixture has returned ack for initiate image upgrade
      	return;
      }
    
      logger.info(gwId+ ": no. of pending fixtures- " + imgUpPendingFixList.size());
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
      fixtureAckList.clear();
      fixtureAckList.addAll(imgUpPendingFixList);	
      while(offset <= fileSize && inProgress) {
      	if(sectorSize > 8192) { //8k boundary is crossed
      		offset = offset - (offset % 8192);
      	}
      	int remainingSize = fileSize - offset;	  
      	if(remainingSize <= 0) {
      		//at the end also,  missing packets
      		sendMissingPkts(fixture, dataSize);
      		logger.info(gwId + ": time taken to upload on all fixtures- " +
      				(System.currentTimeMillis() - startTime));
      		inProgress = false;
      	  int noOfFailedFixtures = fixtureAckList.size();
      	  for(int i = 0; i < noOfFailedFixtures; i++) {
      	    logger.error(fixtureAckList.get(i) + ": image upgrade failed to get the ack for end of file");	      
      	    imgUpPendingFixList.remove(fixtureAckList.get(i));
      	  }
      	  break;
      	}
  	    
      	//after 8k wait for missing packet requests for 2 sec and proceed     
      	if(offset > 0 && offset % (8 * 1024) == 0) {	
      		//proceed with the retransmission of missing packets and go through remaining file
      		sendMissingPkts(fixture, dataSize);
      		sectorSize = 0;
      		int noOfFailedFixtures = fixtureAckList.size();
      		for(int i = 0; i < noOfFailedFixtures; i++) {
      			long failedFixtId = fixtureAckList.get(i);
      			logger.error(failedFixtId + ": image upgrade failed as ack is missed");	    
      			imgUpPendingFixList.remove(failedFixtId);
      		}
      		fixtureAckList.clear();
      		fixtureAckList.addAll(imgUpPendingFixList);	    
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
    
    public void ackImageUploadStart(final Fixture fixture) {
      
      final long fixtureId = fixture.getId();
      logger.debug(fixtureId + ": got ack for image upload start");   
      if(!fixtureAckList.contains(fixtureId) && !imgUpPendingFixList.contains(fixtureId)) {      
        logger.error(fixtureId + ":fixture is not in the upgrade list. so, ignoring the ack");
        return;
      }
      synchronized(ackLock) {
        if(inProgress) {
          //already file transfer is in progress so ignore
          logger.debug(fixtureId + ":file transfer is already in progress. so ignoring the ack");
          return;
        }
        FixtImgUpgrDetails details = fixtureInProgressMap.get(fixtureId);    
        if(details != null && details.status.equals(ServerConstants.IMG_UP_STATUS_SCHEDULED)) {
          fixtureMgr.setImageUpgradeStatus(fixtureId, ServerConstants.IMG_UP_STATUS_INPROGRESS);
          details.status = ServerConstants.IMG_UP_STATUS_INPROGRESS;
          String description = "Image upgrade of Application with image " + details.fileName + " started";
          if(appType == ServerConstants.ISP_APP1_INIT_OPCODE) {
            description = "Image upgrade of Firmware with image " + details.fileName + " started";
          }
          eventMgr.addEvent(fixture, description, EventsAndFault.FIXTURE_IMG_UP_STR); 
        }    
        if(!imgUpPendingFixList.contains(fixtureId)) {
          //adding the fixture to pending list and removing it from ack list
          imgUpPendingFixList.add(new Long(fixtureId));
          fixtureAckList.remove(fixtureId);
        }
        if(initAttempts > 0 && fixtureAckList.size() > 0) {
          //init ack didn't come from all the fixtures and there are still init packet
          //attempts left so don't start file upload yet
          return;
        } else {
          //set the in progress flag to true so the file transfer starts
          inProgress = true;
          //initAckObj.notify();
        }
      }                
      
    } //end of method ackImageUploadStart
    
    private void sendMissingPkts(Fixture fixture, int dataSize) {
      
      bucket8kFinished = false;
      lastBucket++;
      long fixtureId = fixture.getId();
      int fileSize = fileArray.length;    
      long waitTime = 3 * 60 * 1000;
      long startWaitingTime = System.currentTimeMillis();
      logger.info(gwId + ": no. of nodes to expect ack from- " + fixtureAckList.size());
      while(true) {         
        if(!inProgress) { //upgrade is aborted/finished	
          break;
        }        
        if(fixtureAckList.size() == 0) { 
          //bucket finished message came from all fixtures
          logger.debug(gwId + ": positive ack came in " + 
              (System.currentTimeMillis() - startWaitingTime) + " ms");
          break;
        }      
        if(imgUpPendingFixList.size() == 0) {
          //image upgrade finished on all the fixtures. may be final acks didn't come
          //but they might have aborted or rebooted successfully
          inProgress = false;
          break;
        }
        if(System.currentTimeMillis() - startWaitingTime > waitTime) {
          if(fixtureAckList.size() == imgUpPendingFixList.size()) {
            logger.error(gwId + ": Aborting as no packets received from all fixtures");
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
              logger.debug(gwId + ":last packet requested filesize - " + fileSize + 
        	  " datasize - " +dataSize);
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
     
    public synchronized void missingPacketRequest(Fixture fixture, byte[] packet) {
          
      logger.debug(fixture.getId() + ": missing packet request - " + ServerUtil.getLogPacket(packet));
      if(!inProgress) { //upgrade is aborted
        return;
      }
         
      if(!imgUpPendingFixList.contains(fixture.getId())) {
      	return; //this fixture is not in the pending list so ignoring the missing packet request
      }
      short index = 0;
      short bitMap = 0;
      int baseOffset = 512;
      int missingOffset = 0;
      byte startMarker = packet[0];    
      int noOfMissingPkts = packet[ServerConstants.RES_CMD_PKT_MSG_START_POS];
      int offsetPos = packet.length - 5;
      if(startMarker == ServerConstants.FRAME_START_MARKER) { //old packet
        noOfMissingPkts = packet[3];
        offsetPos = 52;
      }
      if(noOfMissingPkts == 0) {
        byte[] tempArr = { packet[offsetPos], packet[offsetPos + 1], packet[offsetPos + 2], 
  	  packet[offsetPos + 3] };
        int receivedOffset = ServerUtil.byteArrayToInt(tempArr);
        if(receivedOffset == lastBucket) {
          if(!bucket8kFinished) {
            fixtureAckList.remove(fixture.getId());
          } else {
            //this is a ack packet came after bucket is finished
            return;
          }
          if(fixtureAckList.size() == 0) { 
            //0 buckets came from all the fixtures
            bucket8kFinished = true;
          }
          int lastBucketIndex = (fileArray.length / 8192) - 1;
          if(fileArray.length % 8192 > 0) {
            lastBucketIndex++;
          }
          if(lastBucket == lastBucketIndex) {
            logger.info(fixture.getId() + ": file transfer finished");
            imgUpPendingFixList.remove(fixture.getId());	  
          }
        }      
        return;
      }    
      //missing pkt seq no. 
      byte[] seqTempArr = { packet[ServerConstants.CMD_PKT_TX_ID_POS], packet[ServerConstants.CMD_PKT_TX_ID_POS + 1], 
      		packet[ServerConstants.CMD_PKT_TX_ID_POS + 2], packet[ServerConstants.CMD_PKT_TX_ID_POS + 3] };
      long seqNo = ServerUtil.intByteArrayToLong(seqTempArr);   
      if(missingPktSeqMap.containsKey(fixture.getId())) {
      	if(seqNo == missingPktSeqMap.get(fixture.getId())) {
      		//same packet is received. probably it is hopped duplicate packet. so ignoring the the packet
      		return; //not processing again as it is processed earlier
      	}    		
      }
      missingPktSeqMap.put(fixture.getId(), seqNo);      
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
        		logger.debug(fixture.getId() + ": requested offset(adding) -- " + missingOffset);
        		noOf64BytePkts++;
        		synchronized(missingOffsetList) {
        			if(!missingOffsetList.contains(new Integer(missingOffset))) {
        				missingOffsetList.add(new Integer(missingOffset));
        			}
        		}
        	}
        }
      } 
      logger.debug(fixture.getId() + ": no. of missing - " + noOf64BytePkts + ", last bucket - " + lastBucket);
      if(noOf64BytePkts == 128) { //ignoring the node if it is asking for entire 8k chunk
      	logger.error(fixture.getId() + ": requesting for 8k chunk, ignoring the sensor");
      	imgUpPendingFixList.remove(fixture.getId());
      	fixtureAckList.remove(fixture.getId());
      	return;
      } 
      /*
      if(missingPktCountMap.containsKey(fixture.getId()) && noOf64BytePkts >= 32) {
      	if(noOf64BytePkts == missingPktCountMap.get(fixture.getId())) {
      		//subsequent missing packet request also have same no. of missing packets and at least 32 packets are missed      	
      		logger.error(fixture.getId() + ": requesting for " + noOf64BytePkts + "again. ignoring the sensor");
      		imgUpPendingFixList.remove(fixture.getId());
      		fixtureAckList.remove(fixture.getId());
      		return;      		
      	}
      } 
      missingPktCountMap.put(fixture.getId(), noOf64BytePkts);
      */
    	             
    } //end of method missingPacketRequest
    
    public void cancelFileUpload(Fixture fixture, byte[] packet) {
      
      logger.error(fixture.getId() + ": abort message - " + ServerUtil.getLogPacket(packet));    
      //for point to point this inProgress should be false
      fixtureAckList.remove(fixture.getId());
      imgUpPendingFixList.remove(fixture.getId());
      FixtImgUpgrDetails details = fixtureInProgressMap.get(fixture.getId());
      //String description = "Fixture upgrade aborted";
      if(details != null) {
        //fixtureInProgressMap.remove(fixture.getId());
      	details.status = ServerConstants.IMG_UP_STATUS_FAIL;
      	details.description = "Image upgrade of Application with image " + details.fileName + " aborted";
      	if(details.appUpgraded == ServerConstants.ISP_APP1_INIT_OPCODE) {
      		details.description = "Image upgrade of Firmware with image " + details.fileName + " aborted";
      	}
//        if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) {
//          description = "Image upgrade of Application with image " + details.fileName + " aborted";
//        } else {
//          description = "Image upgrade of Firmware with image " + details.fileName + " aborted";
//        }
//        firmUpMgr.finishDeviceUpgrade(dbJob.getId(), fixture.getId(), 
//            ServerConstants.IMG_UP_STATUS_FAIL, details.noOfAttempts, description);
      }
//      fixtureMgr.setImageUpgradeStatus(fixture.getId(), ServerConstants.IMG_UP_STATUS_FAIL);
//      eventMgr.addEvent(fixture, description, 
//  	  EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR);
      if(imgUpPendingFixList.size() == 0) { //abort came/image upload finished on all fixtures
        inProgress = false;
      }
      
    } //end of method cancelFileUpload
    
    //this is in response to get current version    
    public void currentNodeVersion(Fixture fixture, byte[] packet) {
      
      long fixtureId = fixture.getId();
      logger.debug(fixtureId + ": current node version - " + 
	  ServerUtil.getLogPacket(packet));
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
      logger.info(fixtureId + ": image(" + swVersion + ") booted to app" + appId);
           
      //if(major == majorVer && minor == minorVer && appId == 2) {
      fixture.setLastConnectivityAt(new Date());
      if(appId == 2) {
        logger.info(fixtureId + " Image upgrade successfull");
        fixtureMgr.updateVersion(swVersion, fixtureId, gwId);
      } else {        
        logger.info(fixtureId + " Image upgrade failed");
        fixtureMgr.updateFirmwareVersion(swVersion, fixtureId, gwId);
      }
      FixtImgUpgrDetails details = fixtureInProgressMap.get(fixtureId);
      if(details == null) {
      	return;
      }
      //current version is not returning app1 version so only invoke the update
      //function for app2 upgrade
      if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) {
      	updateUpgradeCompleteStatus(fixture, details, appId, revision, 0, 0, swVersion);
      }
      
    } //end of method currentNodeVersion
    
    private String updateUpgradeCompleteStatus(Fixture fixture, FixtImgUpgrDetails details, 
    		byte appId, int currVer, int otherVer, int imgUpgrStatus, String version) {
      
      boolean imageUpgradeSuccess = false;
      String imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_FAIL;
      if(currRetryAttempts < (maxAttempts - 1)) {
	imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_INPROGRESS;
      }
      //String description = "";
      //for app2 upgrade, app2 is erased. so, current app 2 means it booted properly with
      //new image
      if(details.appUpgraded == ServerConstants.ISP_APP2_INIT_OPCODE) { //app2 is upgraded
      	details.description = "Image upgrade with image " + details.fileName;
        if(appId == 2) { //booted to app2        	 
        	if(ServerUtil.compareVersion(gw.getApp2Version(), "2.0") >= 0) {
        		if(imgUpgrStatus == 0) {
        			if(details.targetVersion == currVer) {        		
        				imageUpgradeSuccess = true;
        			} else {
        				details.description += " failed as SU rebooted with old image(" + currVer + ")";
        			}
        		} else {
        			details.description += " failed as SU rebooted during upgrade";
        		}
        	} else {
        		imageUpgradeSuccess = true;
          }
        } else {
        	details.description += " failed as SU rebooted with Other image";
          logger.error(fixture.getId() + ":" + details.description);
        }
      } else { 
      	details.description = "Image upgrade of Firmware with image " + details.fileName;
        //for app1 upgrade, app1 is erased, so if the other revision no. is same as 
        //target version then upgrade is successful.
        if(details.targetVersion == otherVer) {
          imageUpgradeSuccess = true;
        }
      }
      
      if(imageUpgradeSuccess) {
      	details.status = ServerConstants.IMG_UP_STATUS_SUCCESS;
      	details.description += " successful";
      	fixtureInProgressMap.remove(fixture.getId());
        logger.info(fixture.getId() + ":image upgrade successful and node rebooted. New version : " + fixture.getVersion() + "," + fixture.getFirmwareVersion());
        noOfSusSuccess++;
        fixtureMgr.setImageUpgradeStatus(fixture.getId(), ServerConstants.IMG_UP_STATUS_SUCCESS);
        eventMgr.addEvent(fixture, details.description, EventsAndFault.FIXTURE_IMG_UP_STR);	
        firmUpMgr.finishDeviceUpgrade(dbJob.getId(), fixture.getId(), 
            ServerConstants.IMG_UP_STATUS_SUCCESS, details.noOfAttempts, details.description, version);
        imgUpgrStatusStr = ServerConstants.IMG_UP_STATUS_SUCCESS;
      } else {
      	details.status = ServerConstants.IMG_UP_STATUS_FAIL;
      	//details.description = description + " failed";        
      }
      return imgUpgrStatusStr;
      
    } //end of method updateUpgradeCompleteStatus
    
    public String nodeRebooted(Fixture fixture, short currVer, short otherVer, byte appId, int imgUpgrStatus, String version) {
      
      long fixtureId = fixture.getId();
         
      //image upgrade was pending on this fixture. abort it
      imgUpPendingFixList.remove(new Long(fixtureId));
      if(imgUpPendingFixList.size() == 0) {
        inProgress = false;
      }
      if(!fixtureInProgressMap.containsKey(fixtureId)) {
        return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
      }            
      FixtImgUpgrDetails details = fixtureInProgressMap.get(fixtureId);
      if(details.appUpgraded == 3) {
        return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
      }
      return updateUpgradeCompleteStatus(fixture, details, appId, currVer, otherVer, imgUpgrStatus, version);
          
    } //end of method nodeRebooted
    
    private void sendImagePacket(long gwId, int offset, int remainingSize, 
    		boolean missing, int retransmits, int interPktDelay) {
	    
      if(retransmits > 10) {
      	retransmits = 10;
      } else if(retransmits < 1) {
      	retransmits = 1;
      }
      if(!broadcastUpgr) {
      	//for old upgrades, it is sent only once
      	retransmits = 1;
      }
      for(int i = 0; i < retransmits; i++) {
      	if(missing) {
      		logger.debug(gwId + ":sending missing img packet(" + (i+1) + "): " + offset);
      	} else {
      		logger.debug(gwId + ":sending img packet(" + (i+1) + "): " + offset);
      	}
      	if(broadcastUpgr) {
      		broadcastDataPacket(offset, remainingSize, gwId);
      	} else {
      		multicastDataPacket(offset, remainingSize, oldFixtureMulticastList);
      	}
      	ServerUtil.sleepMilli(interPktDelay/ImageUpgradeSO.IMG_UP_NO_RETRANSMITS);
      }
      
    } //end of method sendImagePacket
    
  } //end of class GwFixtImgUpgThread  
  
  private void multicastInitPacket(int fileSize, ArrayList<Fixture> fixtureList, 
      int imageType, int interPktDelay) {
    
    int i = 0;        
    //image type(1), packet size(4), file size(4)
    byte[] dataPacket = new byte[9];
    dataPacket[i++] = (byte)imageType;    
    if(fixtureList.get(0).getCommType() == ServerConstants.COMM_TYPE_ZIGBEE) {
      dataPacket[i++] = 0x00;
      dataPacket[i++] = 0x00;
      dataPacket[i++] = 0x00;
      dataPacket[i++] = (byte)ImageUpgradeSO.IMG_UP_ZIGBEE_PKT_SIZE;
    }
    byte[] sizeArray = ServerUtil.intToByteArray(fileSize);
    System.arraycopy(sizeArray, 0, dataPacket, i, sizeArray.length); // Add the file size MSB first
    ArrayList<Fixture> tempList = new ArrayList<Fixture>();
    int noOfFixtures = fixtureList.size();
    for(int k = 0; k < noOfFixtures; k++) {
      tempList.add(fixtureList.get(k));
      if((k == (noOfFixtures - 1)) || ((k + 1) % ImageUpgradeSO.IMG_UP_NO_MULTICAST_TARGETS == 0)) {
      	DeviceServiceImpl.getInstance().sendMulticastPacket(tempList,
      			ServerConstants.IMAGE_UPGRADE_MSG_TYPE, dataPacket, false, 0);
      	tempList.clear();
      	ServerUtil.sleepMilli(200);	
      }      
    }    
       
  } //end of method multicastInitPacket  
  
  private void multicastDataPacket(int offset, int dataSize, ArrayList<Fixture> fixtureList) {
    
    int chunkSize = ImageUpgradeSO.IMG_UP_ZIGBEE_PKT_SIZE; //64 should be dataSize.
        
    //data packet = offset, file data, checksum
    byte[] dataArray = new byte[chunkSize + 4];    
    byte[] offsetArray = ServerUtil.intToByteArray(offset);
    System.arraycopy(offsetArray, 0, dataArray, 0, offsetArray.length); // Add the file size MSB first
    System.arraycopy(fileArray, offset, dataArray, offsetArray.length, dataSize);    
    int remBytes = chunkSize - dataSize;
    while(remBytes > 0) {      
      --remBytes;
      //packet[i + 4 + dataSize + remBytes] = 0;
      if(remBytes % 2 == 0) {
      	dataArray[dataSize + 4 + remBytes] = 0x55;
      } else {
      	dataArray[dataSize + 4 + remBytes] = 0x66;
      }
    }        
        
    byte[] dataPacket = new byte[dataArray.length + 2];
    dataPacket[0] = (byte)ServerConstants.ISP_DATA_OPCODE;
    System.arraycopy(dataArray, 0, dataPacket, 1, dataArray.length);
    dataPacket[dataPacket.length - 1] = ServerUtil.computeChecksum(dataArray);
    
    DeviceServiceImpl.getInstance().sendMulticastPacket(fixtureList,
    		ServerConstants.IMAGE_UPGRADE_MSG_TYPE, dataPacket, false, 0);
   
  } //end of method multicastDataPacket
  
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
  
  public void cancelFileUpload(Fixture fixture, byte[] packet, long gwId) {
    
    if(gwFixUpgThrMap.containsKey(gwId)) {
      gwFixUpgThrMap.get(gwId).cancelFileUpload(fixture, packet);
    }
    
  } //end of method cancelFileUpload
  
  //this is called from node boot info
  public String nodeRebooted(Fixture fixture, short currVer, short otherVer, byte appId,
      long gwId, int imgUpgrStatus, String version) {
    
    if(gwFixUpgThrMap.containsKey(gwId)) {
      return gwFixUpgThrMap.get(gwId).nodeRebooted(fixture, currVer, otherVer, appId, imgUpgrStatus, version);
    }
    return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
        
  } //end of method nodeRebooted
  
  public void ackImageUploadStart(final Fixture fixture, long gwId) {
    
    if(gwFixUpgThrMap.containsKey(gwId)) {
      gwFixUpgThrMap.get(gwId).ackImageUploadStart(fixture);
    }
    
  } //end of method ackImageUploadStart
  
  public synchronized void missingPacketRequest(Fixture fixt, byte[] packet, long gwId) {
    
    if(gwFixUpgThrMap.containsKey(gwId)) {
      gwFixUpgThrMap.get(gwId).missingPacketRequest(fixt, packet);
    }
    
  } //end of method missingPacketRequest
  
  //this is in response to get current version
  public void currentNodeVersion(Fixture fixture, byte[] packet, long gwId) {
    
    if(gwFixUpgThrMap.containsKey(gwId)) {
      gwFixUpgThrMap.get(gwId).currentNodeVersion(fixture, packet);
    }
    
  } //end of method currentNodeVersion
  
} //end of class FixtureImageUpgradeWorker
