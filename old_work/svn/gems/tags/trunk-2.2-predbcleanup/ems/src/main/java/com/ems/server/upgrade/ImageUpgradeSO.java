/**
 * 
 */
package com.ems.server.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ems.util.MD5;
import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.model.FirmwareUpgrade;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.model.SystemConfiguration;
import com.ems.model.Wds;
import com.ems.mvc.util.EmsModeControl;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.upgrade.FixtureImageUpgradeWorker;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.WdsManager;
import com.ems.server.ServerConstants;

import org.apache.log4j.Logger;

/**
 * @author EMS
 *
 */
public class ImageUpgradeSO {
  
  public static int IMG_UP_INTER_PKT_DELAY = 100; //in milli seconds
  public static int IMG_UP_INTER_PKT_DELAY_2 = 20;
  //private static int IMG_UP_INTER_BUCKET_DELAY = 10; //in sec
  
  public static int IMG_UP_NO_MULTICAST_TARGETS = 20;
  public static int IMG_UP_NO_RETRANSMITS = 2;
  public static int IMG_DEFAULT_FAIL_RETRIES = 1;
  
  public static int IMG_UP_ZIGBEE_PKT_SIZE = 64;
  public static int IMG_UP_PLC_PKT_SIZE = 192;
  private static int IMG_UP_RADIO_PKT_SIZE = 1024;
  
  private static short RADIO_FILE_CHUNK_SIZE = 8 * 1024;
  
  private static int IMG_UP_NO_TEST_RUNS = 20;
  private static String IMG_TEST_FILE = "110_su.bin";
  
  private EventsAndFaultManager eventMgr = null;
  
  static final Logger logger = Logger.getLogger("ImageUpgrade");
  
  boolean debug = true;  
      
  private static ImageUpgradeSO instance = null;  
  private FirmwareUpgradeManager firmUpMgr = null;
  private FixtureManager fixtureMgr = null;
  private GatewayManager gwMgr = null;
  private WdsManager wdsMgr = null;
  
  private static String imageLocation = "";
  
  private byte[] fileArray = null;  
  private String currFileName = "";  
  
  private static boolean jobInProgress = false;
  
  private Map<Long, FixtImgUpgrDetails> fixtureInProgressMap = Collections.synchronizedMap(
    new HashMap<Long, FixtImgUpgrDetails>());
  
  class FixtImgUpgrDetails {
    
    int targetVersion;
    int appUpgraded;
    String status;
    String fileName;
    
  } //end of class FixtImgUpgrDetails
     
  /**
   * 
   */
  private ImageUpgradeSO() {
    
    // TODO Auto-generated constructor stub
    firmUpMgr = (FirmwareUpgradeManager)SpringContext.getBean("firmwareUpgradeManager");
    fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    wdsMgr = (WdsManager)SpringContext.getBean("wdsManager");
   
  } //end of constructor
  
  public static ImageUpgradeSO getInstance() {
    
    if(instance == null) {
      synchronized(ImageUpgradeSO.class) {
	if(instance == null) {
	  instance = new ImageUpgradeSO();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  public String getImageLocation() {
    
    return imageLocation;
    
  } //end of method getImageLocation
  
  public static int getImageUpgradeInterPacketDelay(String gwVer) {
  	
  	if(ServerUtil.compareVersion(gwVer, "2.0") >= 0) {
  		return IMG_UP_INTER_PKT_DELAY_2;
  	}
  	return IMG_UP_INTER_PKT_DELAY;
  	
  } //end of method getImageUpgradeInterPacketDelay
    
  public static void readUpgradeProperties() {
       
    try {
      //reading from the database
      SystemConfigurationManager sysMgr = 
      		(SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
    
      SystemConfiguration tempConfig = sysMgr.loadConfigByName("imageUpgrade.zigbeePacketSize");
      if(tempConfig != null) {
      	logger.debug("from database zigbee size -- " + tempConfig.getValue());
      	IMG_UP_ZIGBEE_PKT_SIZE = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.interPacketDelay");
      if(tempConfig != null) {
      	logger.debug("from database inter packet delay -- " + tempConfig.getValue());
      	IMG_UP_INTER_PKT_DELAY = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.plcPacketSize");
      if(tempConfig != null) {
      	logger.debug("from database inter plc packet size -- " + tempConfig.getValue());
      	IMG_UP_PLC_PKT_SIZE = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.no_multicast_targets");
      if(tempConfig != null) {
      	logger.debug("from database no. of multicast target -- " + tempConfig.getValue());
      	IMG_UP_NO_MULTICAST_TARGETS = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.no_multicast_retransmits");
      if(tempConfig != null) {
      	logger.debug("from database no. of multicast retransmits -- " + tempConfig.getValue());
      	IMG_UP_NO_RETRANSMITS = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.default_fail_retries");
      if(tempConfig != null) {
      	logger.debug("from database no. of failure retries -- " + tempConfig.getValue());
      	IMG_DEFAULT_FAIL_RETRIES = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.no_test_runs");
      if(tempConfig != null) {
      	logger.debug("from database no. of test image uprade runs -- " + tempConfig.getValue());
      	IMG_UP_NO_TEST_RUNS = Integer.parseInt(tempConfig.getValue());
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.test_file");
      if(tempConfig != null) {
      	logger.debug("from database test file -- " + tempConfig.getValue());
      	IMG_TEST_FILE = tempConfig.getValue();
      }
      
      tempConfig = sysMgr.loadConfigByName("imageUpgrade.interPacketDelay_2");
      if(tempConfig != null) {
      	logger.debug("from database inter packet delay -- " + tempConfig.getValue());
      	IMG_UP_INTER_PKT_DELAY_2 = Integer.parseInt(tempConfig.getValue());
      }
      
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    
  } //end of method readUpgradeProperties
     
  public static boolean isInProgress() {
   
    return jobInProgress;
    
  } //end of method isInProgress
     
  public static void setFirmwareImageLocation(String location) {
    
    imageLocation = location;
    //logger.debug("images location -- " + imageLocation);
    
  } //end of method setFirmwareImageLocation
  
  public synchronized void missingPacketRequest(Fixture fixt, byte[] packet, long gwId) {
    
    if(gwFixtUpgrMap.containsKey(gwId)) {
      gwFixtUpgrMap.get(gwId).missingPacketRequest(fixt, packet, gwId);
    }
    
  } //end of method missingPacketRequest
  
  public void ackImageUploadStart(Fixture fixture, long gwId) {
    
    if(gwFixtUpgrMap.containsKey(gwId)) {
      gwFixtUpgrMap.get(gwId).ackImageUploadStart(fixture, gwId);
    }
    
  } //end of method ackImageUploadStart
      
  //this is called from node boot info
  public String nodeRebooted(Fixture fixture, short currVer, short otherVer, byte appId,
      long gwId, int imgUpgrStatus) {
    
    if(gwFixtUpgrMap.containsKey(gwId)) {
      return gwFixtUpgrMap.get(gwId).nodeRebooted(fixture, currVer, otherVer, appId, gwId, imgUpgrStatus);
    }
    return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
    
  } //end of method nodeRebooted
  
  //this is in response to get current version
  public void currentNodeVersion(Fixture fixture, byte[] packet, long gwId) {
        
    if(gwFixtUpgrMap.containsKey(gwId)) {
      gwFixtUpgrMap.get(gwId).currentNodeVersion(fixture, packet, gwId);
    }
    
  } //end of method currentNodeVersion
    
  public void cancelFileUpload(Fixture fixture, byte[] packet, long gwId) {
    
    if(gwFixtUpgrMap.containsKey(gwId)) {
      gwFixtUpgrMap.get(gwId).cancelFileUpload(fixture, packet, gwId);
    }
    
  } //end of method cancelFileUpload
  
  //this function will be called from the image upgrade page and this class.
  //reinit is true when called from image upgrade page and it will be false
  //when called from this class
  public synchronized void updateFile(boolean reinit) {
   
    FirmwareUpgrade firmUp = firmUpMgr.loadFirmwareUpgrade();
    String fileName = firmUp.getFileName();
    if(!currFileName.equals(fileName)) { //file is changed
      fileArray = null;
      currFileName = fileName;
    }    
    String filePath = imageLocation + fileName;    
    try {
      if(fileArray == null || reinit) {
	fileArray = getBytesFromFile(new File(filePath));
	if(fileName.toLowerCase().indexOf("firm") != -1) {
	  //appType = ServerConstants.ISP_APP1_INIT_OPCODE;
	}
      }      
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
  } //end of method updateFile
  
  public static byte[] getBytesFromFile(File file) throws IOException {
    
    InputStream is = new FileInputStream(file);
    // Get the size of the file
    long length = file.length();

    if (length > Integer.MAX_VALUE) {
        // File is too large
      throw new IOException("file is too large");
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int)length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
        throw new IOException("Could not completely read file "+file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
    
  } //end of method getBytesFromFile
      
  //this is for testing only. it is not used
  public void startImageUploadLoop(final int[] fixtureIds) {
   
  	new Thread() {
  		public void run() {
  			try {  				
  	  		for(int i = 0; i < IMG_UP_NO_TEST_RUNS; i++) {
  	  			testStartImageUpload(fixtureIds);
  	  			ServerUtil.sleep(180); //sleep for 180 seconds
  	  			DeviceServiceImpl.getInstance().rebootFixture(fixtureIds[0], 1);
  	  			ServerUtil.sleep(60);
  	  			DeviceServiceImpl.getInstance().rebootFixture(fixtureIds[0], 2);
  	  			ServerUtil.sleep(60);
  	  		}
  			}
  			catch(Exception ex) {
  				ex.printStackTrace();
  			}
  		}
  	}.start();  	
    
  } //end of method startImageUploadLoop
  
  public void testStartImageUpload(int[] fixtureIds) {
    
  	readUpgradeProperties();
    //startFixtureImageUpgrade(fixtureIds, "");
  	ImageUpgradeJob fixtureJob = new ImageUpgradeJob();
    fixtureJob.setDeviceType(ServerConstants.DEVICE_FIXTURE);
    fixtureJob.setDeviceIds(fixtureIds);
    fixtureJob.setFileName(IMG_TEST_FILE);    
    ArrayList<ImageUpgradeJob> jobList = new ArrayList<ImageUpgradeJob>();
    jobList.add(fixtureJob);
    startDeviceImageUpgrade(jobList);
    
  } //end of method testStartImageUpload
  
  private void startFixtureImageUpgrade(int[] fixtureIds, String fileName) {
    
    logger.info("fixture radio upgrade request on " + fixtureIds.length + 
	" using " + fileName);
    //this is based on the file naming convention being followed
    int targetBuildNo = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
    //check for radio upgrade
    if(fileName.toLowerCase().indexOf("su_pyc") != -1) {
      String filePath = getImageLocation() + fileName;    
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
      upgradeRadio(fixtureIds, fileName, targetBuildNo);
      return;
    }    
    	   
  } //end of method startFixtureImageUpgrade  
  private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(10);        
  private ThreadPoolExecutor imgUpgrThreadPool = new ThreadPoolExecutor(3, 3, 0, 
      TimeUnit.MILLISECONDS, workQueue);
  //this map is used for currently running fixture upgrades per gateway
  public ConcurrentHashMap<Long, FixtureImageUpgradeWorker> gwFixtUpgrMap = 
    new ConcurrentHashMap<Long, FixtureImageUpgradeWorker>();
  //this map is used for currently running gateway upgrades per gateway
  public ConcurrentHashMap<Long, GatewayImageUpgradeWorker> gwUpgrMap = 
    new ConcurrentHashMap<Long, GatewayImageUpgradeWorker>();
  
  //this map is used for currently running wds upgrades per gateway
  public ConcurrentHashMap<Long, WDSImageUpgradeWorker> gwWdsUpgrMap = 
    new ConcurrentHashMap<Long, WDSImageUpgradeWorker>();
  
  static Object gwThreadLock = new Object();
    
  //allowing multiple image upgrade jobs at the same time  
  public class ImageUpgradeWork implements Runnable {
    
    private ArrayList<ImageUpgradeJob> jobList = null;
    
    public ImageUpgradeWork(ArrayList<ImageUpgradeJob> jobList) {
    
      this.jobList = jobList;
      
    } //end of constructor
    
    public void run() {
      
      int noOfJobs = jobList.size();
      ImageUpgradeJob job = null;
      for(int i = 0; i < noOfJobs; i++) {
        job = jobList.get(i);    
        if(job.getDeviceType() == ServerConstants.DEVICE_GATEWAY) {
            if(job.getFileName().toLowerCase().indexOf("gw.tar") != -1) {
                startGWImageUpgrade(job.getImageUpgradeDBJob(), job.getDeviceIds(), ImageUpgradeSO.getInstance().getImageLocation() + job.getFileName());
                EmsModeControl.resetMode();
                return;
            }
          //upload to the gateways
          Thread gwUpgrThr = new GatewayImageUpgradeWorker(job);
          gwUpgrThr.start();
          try {
            logger.info("waiting for the gw upgrade thread to finish");
            gwUpgrThr.join();
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        } else if(job.getDeviceType() == ServerConstants.DEVICE_SWITCH) {          
          //upload to the wds
          Thread wdsUpgrThr = new WDSImageUpgradeWorker(job);
          wdsUpgrThr.start();
          try {
          	logger.info("waiting for the wds upgrade thread to finish");
          	wdsUpgrThr.join();
          }
          catch(Exception e) {
          	e.printStackTrace();
          }
        } else {
          if(job.getFileName().toLowerCase().indexOf("su_pyc") != -1) {
            startFixtureImageUpgrade(job.getDeviceIds(), job.getFileName());
            EmsModeControl.resetMode();
            return;
          }
          Thread upgrThr = new FixtureImageUpgradeWorker(job);
          upgrThr.start();
          try {
            logger.info("waiting for the upgrade thread to finish");
            upgrThr.join();
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
      EmsModeControl.resetMode();
    } //end of method run
    
  } //end of class ImageUpgradeWork
   
  public void startDeviceImageUpgrade(ArrayList<ImageUpgradeJob> jobList) {
        
//    if(jobInProgress) {
//      //already file transfer is in progress so ignore
//      logger.info("file transfer is already in progress.");
//      return;
//    }
//    jobInProgress = true;

    //add the job in the database
    int noOfJobs = jobList.size();
    ImageUpgradeJob job = null;
    for(int i = 0; i < noOfJobs; i++) {
      job = jobList.get(i);    
      ImageUpgradeDBJob dbJob = new ImageUpgradeDBJob();
      dbJob.setImageName(job.getFileName());
      dbJob.setJobName("ImageUpgrade_" + System.currentTimeMillis());
      dbJob.setNoOfRetries(IMG_DEFAULT_FAIL_RETRIES);
      dbJob.setScheduledTime(new Date());
      dbJob.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);
      int noOfDevices = job.getDeviceIds().length;
      if(job.getDeviceType() == ServerConstants.DEVICE_GATEWAY) {
      	dbJob.setDeviceType("gateway");    
      	dbJob = firmUpMgr.save(dbJob);
      	job.setImageUpgradeDBJob(dbJob);
	
      	//update the upgrade status of gateways as scheduled
      	for(int k = 0; k < noOfDevices; k++) {
      		Gateway gw = gwMgr.loadGateway((long)job.getDeviceIds()[k]);   
      		if(gw == null) {
      			logger.info(job.getDeviceIds()[k] + ": There is no gateway");
      			continue;
      		}     	
      		gwMgr.setImageUpgradeStatus(gw.getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setJobId(dbJob.getId());
      		deviceStatus.setDeviceId(gw.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);	
      		deviceStatus = firmUpMgr.save(deviceStatus);
      	}
      } else if(job.getDeviceType() == ServerConstants.DEVICE_SWITCH) {
      	dbJob.setDeviceType("switch");    
      	dbJob = firmUpMgr.save(dbJob);
      	job.setImageUpgradeDBJob(dbJob);
	
      	//update the upgrade status of switches as scheduled
      	for(int k = 0; k < noOfDevices; k++) {
      		Wds wds = wdsMgr.loadWdsById((long)job.getDeviceIds()[k]);   
      		if(wds == null) {
      			logger.info(job.getDeviceIds()[k] + ": There is no WDS");
      			continue;
      		} 
      		//TODO
      		//wdsMgr.setImageUpgradeStatus(wds).getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setJobId(dbJob.getId());
      		deviceStatus.setDeviceId(wds.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);	
      		deviceStatus = firmUpMgr.save(deviceStatus);
      	}
      } else {
      	dbJob.setDeviceType("fixture");
      	dbJob = firmUpMgr.save(dbJob);
      	job.setImageUpgradeDBJob(dbJob);
      	//update the upgrade status of fixtures as scheduled
      	int noOfFixtures = job.getDeviceIds().length;	
      	for(int k = 0; k < noOfFixtures; k++) {
      		Fixture fixture = fixtureMgr.getFixtureById(job.getDeviceIds()[k]);   
      		if(fixture == null) {
      			logger.info(job.getDeviceIds()[k] + ": There is no Fixture");
      			continue;
      		}     
      		fixtureMgr.setImageUpgradeStatus(fixture.getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setJobId(dbJob.getId());
      		deviceStatus.setDeviceId(fixture.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		firmUpMgr.save(deviceStatus);
      	}
      }      
    }
    imgUpgrThreadPool.execute(new ImageUpgradeWork(jobList));    
    //jobInProgress = false;
    
  } //end of method startDeviceImage/upgrade
  
  private byte pythonFileId = 0;
  
  private short sendRadioFile(long gwId, byte fileId, short startOffset) {
    
    Gateway gateway = gwMgr.loadGateway(gwId);
    int dataSize = IMG_UP_RADIO_PKT_SIZE;
    int fileSize = fileArray.length;
    long startTime = System.currentTimeMillis();      
    short offset = startOffset;   
    int endOffset = startOffset + RADIO_FILE_CHUNK_SIZE;
    if(fileSize < endOffset) {
      endOffset = fileSize;
    }
    while(offset < endOffset) {
      int remainingSize = fileSize - offset;	  
      if(remainingSize <= 0) {
	//end of file
	break;
      }
      if(remainingSize < dataSize) {
	dataSize = remainingSize;
      }
      byte[] radioPkt = new byte[7 + dataSize];
      int i = 0;
      //file id      
      radioPkt[i++] = fileId;    
      //offet      
      ServerUtil.fillShortInByteArray(offset, radioPkt, i);
      i +=2;
      //length of data
      ServerUtil.fillShortInByteArray(dataSize, radioPkt, i);
      i += 2;
      //length of file      
      ServerUtil.fillShortInByteArray(fileSize, radioPkt, i);
      i += 2;      
      System.arraycopy(fileArray, offset, radioPkt, 7, dataSize);
      ServerUtil.sleepMilli(IMG_UP_INTER_PKT_DELAY);
      logger.info(gwId + ": sending python file, offset - " + offset);
      GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_FILE_XFER, 
	      gateway, radioPkt, false);      
      offset += dataSize;
    }    
    logger.info(gwId + ": Time taken to upload radio file - " + (System.currentTimeMillis() - startTime));
    return (short)(endOffset - startOffset);
    
  } //end of method sendRadioFile
  
  private short radioDataSent = 0;
  private boolean lastRadioChunk = false;
  
  private void upgradeRadio(int[] fixtureIds, String fileName, int targetBuildNo) {
    
    HashMap<Long, ArrayList<Fixture>>gwFixtureMap = new HashMap<Long, ArrayList<Fixture>>();
    fixtureInProgressMap.clear();
    
    //group the fixtures into gateway buckets
    int noOfFixtures = fixtureIds.length;
    for(int k = 0; k < noOfFixtures; k++) {
      Fixture fixture = fixtureMgr.getFixtureById(fixtureIds[k]);   
      if(fixture == null) {
      	logger.info(fixtureIds[k] + ": There is no Fixture");
        continue;
      }     
      FixtImgUpgrDetails details = new FixtImgUpgrDetails();
      details.appUpgraded = 3;
      details.targetVersion = targetBuildNo;
      details.status = ServerConstants.IMG_UP_STATUS_SCHEDULED;
      details.fileName = fileName;
      fixtureInProgressMap.put(fixture.getId(), details);
      Long gwId = fixture.getSecGwId();
      if(!gwFixtureMap.containsKey(gwId)) {
	gwFixtureMap.put(gwId, new ArrayList<Fixture>());
      }
      gwFixtureMap.get(gwId).add(fixture);
    }    
    long startTime = System.currentTimeMillis();
    byte[] pgmDataPkt = new byte[8];    
    //iterate through the gateways 
    Iterator<Long> gwIter = gwFixtureMap.keySet().iterator();
    while(gwIter.hasNext()) {
      Long gwId = gwIter.next();
      //send in 8k chunks
      short startOffset = 0;
      int fileSize = fileArray.length;
      //try 2 times to transfer file
      Gateway gw = gwMgr.loadGateway(gwId);     
      //transfer/program a chunk of 8k
      while(startOffset < fileSize) {
	if(gwFixtureMap.get(gwId).size() == 0) {
	  //there are no fixtures to be programmed under this gateway. may be programming failed
	  //for prior chunk.
	  break;
	}
	//2 attempts to transfer the file 
	for(int l = 0; l < 2; l++) {
	  gotRadioAck = false;
	  //send the file(chunk) to this gateway
	  lastRadioChunk = false;
	  radioDataSent = sendRadioFile(gwId, ++pythonFileId, startOffset);  
	  if((startOffset + radioDataSent) == fileSize) {
	    //last chunk
	    lastRadioChunk = true;
	  }
	  //wait for the ack from gateway of file transfer completion    
	  synchronized(pythonUpgradeLock) {
	    try {	
	      pythonUpgradeLock.wait(60 * 1000); //wait for 1 minute		
	    }
	    catch(Exception e) {
	      e.printStackTrace();
	    }
	  }	
	  if(!gotRadioAck) {	
	    logger.error(gwId + ": No ack for radio file transfer (attempt " + (l + 1) + ")");
	    //ask for status		
	    GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_FILE_XFER_STATUS, gw, 
		new byte[0], false);
	    //wait for the ack from gateway of file transfer completion    
	    synchronized(pythonUpgradeLock) {
	      try {	
		pythonUpgradeLock.wait(60 * 1000); //wait for 1 minute		
	      }
	      catch(Exception e) {
		e.printStackTrace();
	      }
	    }
	  }
	  if(gotRadioAck) {
	    //got the successful ack so no need for retry
	    break;
	  }
	}
	if(!gotRadioAck) {
	  //file transfer to this gw is not successful, move to next gw
	  logger.error(gwId + ": No ack for radio file transfer");
	  break;
	}
	//file(chunk) transfer finished proceed with program
	ArrayList<Fixture> fixtureList = gwFixtureMap.get(gwId);
	int noOfGwFixtures = fixtureList.size();
	pgmDataPkt[3] = pythonFileId;
	//start offset
	ServerUtil.fillShortInByteArray(startOffset, pgmDataPkt, 4);
	//data size
	ServerUtil.fillShortInByteArray(radioDataSent, pgmDataPkt, 6);
	for(int k = 0; k < noOfGwFixtures; k++) {
	  Fixture fixt = fixtureList.get(k);
	  FixtImgUpgrDetails details = fixtureInProgressMap.get(fixt.getId());
	  if(details != null) {
	    details.status = ServerConstants.IMG_UP_STATUS_INPROGRESS;
	  }
	  if(startOffset == 0) { //programming first chunk
	    fixtureMgr.setImageUpgradeStatus(fixt.getId(), ServerConstants.IMG_UP_STATUS_INPROGRESS);
	    String desc = "Image upgrade of Radio with image " + details.fileName + " started";
	    eventMgr.addEvent(fixt, desc, EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR);
	  }
	  byte[] fixtSnap = ServerUtil.getSnapAddr(fixt.getSnapAddress());
	  System.arraycopy(fixtSnap, 0, pgmDataPkt, 0, 3);	
	  gotRadioAck = false;
	  GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_PGM_RADIO, gw, pgmDataPkt, true);
	  //wait for the ack from gateway of program completion   
	  synchronized(pythonUpgradeLock) {
	    try {	
	      pythonUpgradeLock.wait(5 * 60 * 1000); //wait for 5 min		
	    }
	    catch(Exception e) {
	      e.printStackTrace();
	    }
	  }	
	  if(!gotRadioAck) {
	    //no ack for radio programming, ask for the programming status	   
	    logger.error(fixt.getFixtureName() + ": No ack for radio program ask for status");
	    GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_PMG_RADIO_STATUS, gw, 
		fixtSnap, false);
	    //wait for the ack from gateway of program completion   
	    synchronized(pythonUpgradeLock) {
	      try {	
		pythonUpgradeLock.wait(60 * 1000); //wait for 1 min		
	      }
	      catch(Exception e) {
		e.printStackTrace();
	      }
	    }
	  }
	  String status = ServerConstants.IMG_UP_STATUS_FAIL;
	  if(gotRadioAck) {
	    //got the ack
	    status = ServerConstants.IMG_UP_STATUS_SUCCESS;
	  } 
	  String desc = "Image upgrade of Radio with image " + fileName + " " + status;	  
	  if((startOffset + radioDataSent) == fileSize) {
	    //last chunk
	    fixtureInProgressMap.remove(fixt.getId());
	    fixtureMgr.setImageUpgradeStatus(fixt.getId(), status);
	    eventMgr.addEvent(fixt, desc, EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR);
	  } else {
	    //not the last chunk but if it is failed, update the status and remove the fixture
	    //from the list
	    if(status.equals(ServerConstants.IMG_UP_STATUS_FAIL)) {
	      fixtureInProgressMap.remove(fixt.getId());
	      gwFixtureMap.get(gwId).remove(fixt);
	      fixtureMgr.setImageUpgradeStatus(fixt.getId(), status);
	      eventMgr.addEvent(fixt, desc, EventsAndFault.FIXTURE_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR);
	    }
	  }
	}
	startOffset += RADIO_FILE_CHUNK_SIZE;
      }
    }
    logger.info("Total time taken to upload radio file - " + (System.currentTimeMillis() - startTime));
        
  } //end of method upgradeRadio
  
  private boolean gotRadioAck = false; 
  public static Object pythonUpgradeLock = new Object();
  
  public void ackRadioFileTransfer(long gwId, byte[] pkt) {
    
    logger.debug(gwId + ": got the ack for radio file " + ServerUtil.getLogPacket(pkt));
    int totalSize = ServerUtil.extractShortFromByteArray(pkt, 5);
    logger.debug(gwId + ": received file size = " + totalSize);
    if(totalSize == radioDataSent) {
      gotRadioAck = true;
    }
    try {
      synchronized(pythonUpgradeLock) {
	pythonUpgradeLock.notify();
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      logger.error("Error in notifying in ackRadioFileTransfer");
    }
    
  } //end of method ackRadioFileTransfer
  
  public void ackRadioProgram(long gwId, byte[] pkt) {
            
    logger.debug(gwId + ": got the ack for radio program " + ServerUtil.getLogPacket(pkt));
    byte status = pkt[5];    
    //for in between chunks status > 0 is enough
    if(status > 3 || (status > 0 && !lastRadioChunk)) {      
      gotRadioAck = true;      
    }
    try {
      synchronized(pythonUpgradeLock) {
	pythonUpgradeLock.notify();
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      logger.error("Error in notifying in ackRadioProgram");
    }
    
  } //end of method ackRadioUpgrade
  
  public int getPacketSize(long fixtureId) {
    
    if(fixtureMgr.getCommType(fixtureId) == ServerConstants.COMM_TYPE_ZIGBEE) {
      return IMG_UP_ZIGBEE_PKT_SIZE;
    } else {
      return IMG_UP_PLC_PKT_SIZE;
    }
    
  } //end of method getPacketSize  
      
  public void gwAckImageUploadStart(final Gateway gateway) {
    
    if(gwUpgrMap.containsKey(gateway.getId())) {
      gwUpgrMap.get(gateway.getId()).gwAckImageUploadStart(gateway);
    }
    
  } //end of method gwAckImageUploadStart
  
  public synchronized void gwMissingPacketRequest(Gateway gateway, byte[] packet) {
    
    if(gwUpgrMap.containsKey(gateway.getId())) {
      gwUpgrMap.get(gateway.getId()).gwMissingPacketRequest(gateway, packet);
    }
           
  } //end of method gwMissingPacketRequest
  
  public void gwCancelFileUpload(Gateway gateway, byte[] packet) {
    
    if(gwUpgrMap.containsKey(gateway.getId())) {
      gwUpgrMap.get(gateway.getId()).gwCancelFileUpload(gateway, packet);
    }
    
  } //end of method gwCancelFileUpload
  
  private boolean gotGWUpgradeAck = false; 
  public static Object gatewayUpgradeLock = new Object();
  private int gwUpgrStatus = 0;

  /**
   * New gateway upgrade will be a tar file, containing the gateway image and the gateway radio image.
   * @param fixtureIds
   * @param fileName
   */
	private void startGWImageUpgrade(ImageUpgradeDBJob dbJob, int[] gwIds, String fileName) {
		long startTime = 0;
		String description = ""; 
		for (int i = 0; i < gwIds.length; i++) {
			Gateway gw = gwMgr.loadGateway((long) gwIds[i]);
			if (gw == null) {
				logger.info(gwIds[i] + ": There is no gateway");
				continue;
			}
			byte[] dataPkt = fileName.getBytes();
			// md5sum + 2 bytes for storing path length + actual file path
			byte[] upgradePkt = new byte[20 + 2 + dataPkt.length];
		    short pathLength = (short)dataPkt.length;
		    byte[] pathLengthArr = ServerUtil.shortToByteArray(pathLength);

			byte[] fileSHA1sumArr = ServerUtil.createSHA1Checksum(fileName);
			if (fileSHA1sumArr == null) {
				// Should not happen.
				fileSHA1sumArr = new byte[20];
				Arrays.fill(fileSHA1sumArr, (byte)'0');
			}
		    System.arraycopy(fileSHA1sumArr, 0, upgradePkt, 0, fileSHA1sumArr.length);
		    System.arraycopy(pathLengthArr, 0, upgradePkt, fileSHA1sumArr.length, pathLengthArr.length);
		    System.arraycopy(dataPkt, 0, upgradePkt, fileSHA1sumArr.length + pathLengthArr.length, dataPkt.length);

			logger.debug(gw.getIpAddress() + ": " + ServerUtil.getLogPacket(upgradePkt));
		    gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_SCHEDULED);
		    firmUpMgr.updateDeviceStatus(dbJob.getId(), new Long(gwIds[i]), ServerConstants.IMG_UP_STATUS_SCHEDULED);
			startTime = System.currentTimeMillis();
			gwUpgrStatus = -1;
			GatewayImpl.getInstance().sendGwPkt(ServerConstants.GATEWAY_UPGRADE_CMD, gw, upgradePkt, false);
			//command is sent so changing the status to in progress
			firmUpMgr.updateDeviceStatus(dbJob.getId(), new Long(gwIds[i]), ServerConstants.IMG_UP_STATUS_INPROGRESS);
			description = "Image upgrade of Gateway with image " + fileName + "is in progress";
		  gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_INPROGRESS);
			eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR, EventsAndFault.INFO_SEV_STR); 
			// wait for the ack from gateway of file transfer completion
			synchronized (gatewayUpgradeLock) {
				try {
					gatewayUpgradeLock.wait(300 * 1000); // wait for 5 minutes
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (!gotGWUpgradeAck) {
				logger.error(gwIds[i] + ": No ack for gw upgrade file transfer (attempt "
						+ (i + 1) + ")");
			    gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_FAIL);
			    firmUpMgr.updateDeviceStatus(dbJob.getId(), new Long(gwIds[i]), ServerConstants.IMG_UP_STATUS_FAIL);
				description = "gw Image upgrade of Firmware with image " + fileName + " didn't start";
				eventMgr.addEvent(gw, description, 
						  EventsAndFault.GW_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR);
			} else {
				if(gwUpgrStatus == 0) {
					//success
					logger.info("Time taken to transfer the file on gateway " + gwIds[i] + " is " + (System.currentTimeMillis() - startTime));
			    firmUpMgr.updateDeviceStatus(dbJob.getId(), new Long(gwIds[i]), ServerConstants.IMG_UP_STATUS_SUCCESS);
			    description = "Image upgrade of Gateway with image " + fileName + " successful";
			    gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_SUCCESS);
			    eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR, EventsAndFault.INFO_SEV_STR);
				} else {
					String status = "file transfer";
					if(gwUpgrStatus == 2) {
						status = "upgrade";
					}
					description = "image upgrade failed on gateway " + gwIds[i] + " failed for " + status;
			    logger.info(description);
			    firmUpMgr.updateDeviceStatus(dbJob.getId(), new Long(gwIds[i]), ServerConstants.IMG_UP_STATUS_FAIL);			    
			    gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_FAIL);
			    eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR, EventsAndFault.MAJOR_SEV_STR); 
				}
			}
		}
		return;
	} // end of method startGWImageUpgrade

  /**
   * Receives Ack from the Gateway and notifies the scheduler thread about the start of the gateway upgrade process...
   * @param gwId
   * @param pkt
   */
	public void ackGatewayImageUpgrade(long gwId, byte[] pkt) {
		logger.debug(gwId + ": got the ack for gateway image upgrade "
				+ ServerUtil.getLogPacket(pkt));
		
		int pos = 5;
		gwUpgrStatus = pkt[pos];
		gotGWUpgradeAck = true;
		try {
			synchronized (gatewayUpgradeLock) {
				gatewayUpgradeLock.notify();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in notifying in ackGatewayImageUpgrade");
		}
	} // end of method ackRadioFileTransfer
	
	public void ackImageUploadStart(Wds wds, long gwId) {
    
    if(gwWdsUpgrMap.containsKey(gwId)) {
      gwWdsUpgrMap.get(gwId).ackImageUploadStart(wds, gwId);
    }
    
  } //end of method ackImageUploadStart
  
  public synchronized void missingPacketRequest(Wds wds, byte[] packet, long gwId) {
    
    if(gwWdsUpgrMap.containsKey(gwId)) {
      gwWdsUpgrMap.get(gwId).missingPacketRequest(wds, packet, gwId);
    }
    
  } //end of method missingPacketRequest
  
  public String nodeRebooted(Wds wds, byte[] packet, long gwId) { 
  		//short currVer, short otherVer, byte appId, long gwId, int imgUpgrStatus) {
    
  	/*
  	 typedef struct node_info
{
    uint8_t        bl_major_version;
    uint8_t        bl_minor_version;
    version_info_t app_version;
    version_info_t firm_version;
    uint8_t        app_id; // currently not used
    uint8_t        reset_reason;
    uint8_t				 image_upgrade_status;
    uint8_t        model[SW_MODEL_SZ];
} __attribute__ ((packed)) node_info_t;

	typedef struct version_info
{
    uint8_t  major;
    uint8_t  minor;
    uint8_t  bugfix;
    uint16_t release;
} PACKED_STRUCT version_info_t;

  	 */
    if(!gwWdsUpgrMap.containsKey(gwId)) {
    	return ServerConstants.IMG_UP_STATUS_NOT_PENDING;      
    }
    int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS; 
    //bl major version
    pktIndex++;
    //bl minor version
    pktIndex++;
    //app major version
    pktIndex++;
    //app minor version
    pktIndex++;
    //app bug fix
    pktIndex++;
    //app release
    byte[] releaseArr= {packet[pktIndex++], packet[pktIndex++]};
    short currVer = (short) ServerUtil.byteArrayToShort(releaseArr);
    
    //firm version
    pktIndex += 5;
    //app id
    pktIndex++;
    //reset reason
    pktIndex++;
    //image upgrade status
    byte imageUpgStatus = packet[pktIndex++];
    //model
    pktIndex++;    
    
    return gwWdsUpgrMap.get(gwId).nodeRebooted(wds, currVer, gwId, imageUpgStatus);
    
  } //end of method nodeRebooted
  
  
  public void cancelFileUpload(Wds wds, byte[] packet, long gwId) {
    
    if(gwWdsUpgrMap.containsKey(gwId)) {
      gwWdsUpgrMap.get(gwId).cancelFileUpload(wds, packet, gwId);
    }
    
  } //end of method cancelFileUpload
  
} //end of class ImageUpgradeSO
