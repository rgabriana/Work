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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ems.types.DeviceType;
import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.cache.PlugloadCache;
import com.ems.model.Device;
import com.ems.model.EventsAndFault;
import com.ems.model.FirmwareUpgrade;
import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.model.Plugload;
import com.ems.model.SystemConfiguration;
import com.ems.model.Wds;
import com.ems.mvc.util.EmsModeControl;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.upgrade.FixtureImageUpgradeWorker;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.ImageUpgradeJobManager;
import com.ems.service.PlugloadManager;
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
  
  private static int UPGRADE_ON_REBOOT_INTERVAL = 900;
  
  private static int IMG_UP_NO_TEST_RUNS = 20;
  private static String IMG_TEST_FILE = "110_su.bin";
  
  static final Logger logger = Logger.getLogger("ImageUpgrade");
  
  boolean debug = true;  
      
  private static ImageUpgradeSO instance = null;  
 
  private FirmwareUpgradeManager firmUpMgr = null;
  private FirmwareUpgradeScheduleManager firmUpSchedMgr = null;
  private ImageUpgradeJobManager imgUpgrJobMgr = null;
  private FixtureManager fixtureMgr = null;
  private GatewayManager gwMgr = null;
  private WdsManager wdsMgr = null;
  private PlugloadManager plugloadMgr = null;
  private EventsAndFaultManager eventMgr;
  
  private static String imageLocation = "";
  
  private byte[] fileArray = null;  
  private String currFileName = "";  
  
  private static boolean jobInProgress = false;
  
  private Map<Long, FixtImgUpgrDetails> fixtureInProgressMap = Collections.synchronizedMap(
    new HashMap<Long, FixtImgUpgrDetails>());
  
  private HashMap<Long, FixtureImageUpgradeWorker> upgrJobMap = new HashMap<Long, FixtureImageUpgradeWorker>();
  
  // Max number of concurrent gateway firmware uploads
  private Semaphore gtwSemaphore = new Semaphore(6);
  
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
    imgUpgrJobMgr = (ImageUpgradeJobManager)SpringContext.getBean("imageUpgradeJobManager");
    firmUpSchedMgr = (FirmwareUpgradeScheduleManager)SpringContext.getBean("firmwareUpgradeScheduleManager");
    fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    wdsMgr = (WdsManager)SpringContext.getBean("wdsManager");
    plugloadMgr = (PlugloadManager)SpringContext.getBean("plugloadManager");
   
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
  
  public Semaphore getGtwSemaphore() {
	  return gtwSemaphore;
  }
	  
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
  
  //TODO
  public void ackImageUploadStart(Plugload plugload, long gwId) {
    
    if(gwPlugloadUpgrMap.containsKey(gwId)) {
    	gwPlugloadUpgrMap.get(gwId).ackImageUploadStart(plugload, gwId);
    }
    
  } //end of method ackImageUploadStart
  
  public synchronized void missingPacketRequest(Plugload plugload, byte[] packet, long gwId) {
    
    if(gwPlugloadUpgrMap.containsKey(gwId)) {
    	gwPlugloadUpgrMap.get(gwId).missingPacketRequest(plugload, packet, gwId);
    }
    
  } //end of method missingPacketRequest
  
  //this is in response to get current version
  public void currentNodeVersion(Plugload plugload, byte[] packet, long gwId) {
        
    if(gwPlugloadUpgrMap.containsKey(gwId)) {
    	gwPlugloadUpgrMap.get(gwId).currentNodeVersion(plugload, packet, gwId);
    }
    
  } //end of method currentNodeVersion
    
  public void cancelFileUpload(Plugload plugload, byte[] packet, long gwId) {
    
    if(gwPlugloadUpgrMap.containsKey(gwId)) {
    	gwPlugloadUpgrMap.get(gwId).cancelFileUpload(plugload, packet, gwId);
    }
    
  } //end of method cancelFileUpload
      
  //this is called from node boot info
  public String nodeRebooted(Fixture fixture, short currVer, short otherVer, byte appId,
      long gwId, int imgUpgrStatus, String version) {
    
    if(gwFixtUpgrMap.containsKey(gwId)) {
      return gwFixtUpgrMap.get(gwId).nodeRebooted(fixture, currVer, otherVer, appId, gwId, imgUpgrStatus, version);
    }
    /* to support new image upgrade for sensors
    else {
    	//it didn't reboot because of image upgrade
    	boolean startUpgrThr = FixtureCache.getInstance().nodeRebooted(fixture.getId());
    	if(startUpgrThr) {
    		//start the timer
    		new Thread() {
    			public void run() {
    				ServerUtil.sleep(UPGRADE_ON_REBOOT_INTERVAL);
    				//clear the reboot list 
    				ArrayList<Long> nodeList = FixtureCache.getInstance().drainNodeRebootList();
    				//start the image upgrade on all the rebooted sensors
    				//find the image for all the fixtures and upgrade them using one file at a time
    				Iterator<Long> nodeIter = nodeList.iterator();
    				HashMap<String, StringBuffer> modelFixtureMap = new HashMap<String, StringBuffer>();
    				while(nodeIter.hasNext()) {
    					Long fixtureId = nodeIter.next();
    					Device dev = FixtureCache.getInstance().getCachedFixture(fixtureId);
    					String model = dev.getModelNo();
    					if(!modelFixtureMap.containsKey(model)) {
    						modelFixtureMap.put(model, new StringBuffer());
    						modelFixtureMap.get(model).append(fixtureId);
    					} else {
    						modelFixtureMap.get(model).append("," + fixtureId);
    					}
    					
    				}
    				
    				//group sensors based on image 
    				Iterator<String> modelIter = modelFixtureMap.keySet().iterator();    				
    				HashMap<String, StringBuffer> imgFixtureMap = new HashMap<String, StringBuffer>();
    				HashMap<String, FirmwareUpgradeSchedule> imgScheduleMap = new HashMap<String, FirmwareUpgradeSchedule>();
    				while(modelIter.hasNext()) {
    					String model = modelIter.next();
    					//get the image for this model
    					List<FirmwareUpgradeSchedule> scheduleList = firmUpSchedMgr.getAllActiveFirwareSchedules();
    					//find the correct image based on model
    					FirmwareUpgradeSchedule modelSchedule = null; //firmUpSchedMgr.getActiveFirmwareUpgradeSchedule(model);
    					Iterator<FirmwareUpgradeSchedule> schedListIter = scheduleList.iterator();    					
    					while(schedListIter.hasNext()) {
    						modelSchedule = schedListIter.next();
    						if(model.contains(modelSchedule.getModelNo())) {
    							break;
    						}
    					}
    					if(modelSchedule == null) {
    						continue;
    					}
    					if(!imgFixtureMap.containsKey(modelSchedule.getFileName())) {
    						imgFixtureMap.put(modelSchedule.getFileName(), modelFixtureMap.get(model));
    						imgScheduleMap.put(modelSchedule.getFileName(), modelSchedule);
    					} else {
    						imgFixtureMap.get(modelSchedule.getFileName()).append(","  + modelFixtureMap.get(model).toString());
    					}    					
    				}
    				//schedule a job to run for each image with the rebooted sensors
    				ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
    				Iterator<String> imgIter = imgFixtureMap.keySet().iterator();
    				while(imgIter.hasNext()) {
    					String imgFile = imgIter.next();
    					// Creates a Fixture upgrade job
    					ImageUpgradeDBJob fixtureJob = new ImageUpgradeDBJob();
    					fixtureJob.setDeviceType(DeviceType.Fixture.getName());		
    					fixtureJob.setExcludeList(null);
    					fixtureJob.setIncludeList(imgFixtureMap.get(imgFile).toString());
    					fixtureJob.setImageName(imgFile);
    					fixtureJob.setNoOfRetries(imgScheduleMap.get(imgFile).getRetries());
    					fixtureJob.setRetryInterval(imgScheduleMap.get(imgFile).getRetryInterval());
    					fixtureJob.setJobName(imgFile+ "_OnReboot");    					
							fixtureJob.setScheduledTime(new Date());    					
							jobList.add(fixtureJob);
    				}    				
    				startDeviceImageUpgrade(jobList);
    			}
    		}.start(); 	
    	}    	
    }
    */
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
      is.close();
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
    	is.close();
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
  	ImageUpgradeDBJob fixtureJob = new ImageUpgradeDBJob();
    fixtureJob.setDeviceType(DeviceType.Fixture.getName());
    //convert from int to Long[]
    Long[] deviceIds = new Long[fixtureIds.length];
    for(int i = 0; i < fixtureIds.length; i++) {
    	deviceIds[i] = (long)fixtureIds[i];
    }
    fixtureJob.setDeviceIds(deviceIds);
    fixtureJob.setImageName(IMG_TEST_FILE);    
    ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
    jobList.add(fixtureJob);
    startDeviceImageUpgrade(jobList);
    
  } //end of method testStartImageUpload
  
  private void startFixtureImageUpgrade(Long[] fixtureIds, String fileName) {
    
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
  
  private FirmUpgrJobTimeComparator timeComparator = new FirmUpgrJobTimeComparator();
  
  //comparator class to compare two image upgrade works that are added to the thread pool work queue
  public class FirmUpgrJobTimeComparator implements Comparator<ImageUpgradeWork> {
    
    @Override
    public int compare(ImageUpgradeWork work1, ImageUpgradeWork work2) {
      
      if(work1.scheduleTime.before(work2.scheduleTime)) {
      	return -1;
      } else {
      	return 1;
      }
      //return 0;
      
    } //end of method compare
    
  } //end of class FirmUpgrJobTimeComparator
  
  private PriorityBlockingQueue<ImageUpgradeWork> workQueue = new PriorityBlockingQueue<ImageUpgradeWork>(100, timeComparator);
  
  private ImageUpgradeThreadPoolExecutor imgUpgrThreadPool = new ImageUpgradeThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, workQueue);
  
  public class ImageUpgradeThreadPoolExecutor extends ThreadPoolExecutor {
        
  public ImageUpgradeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
    		PriorityBlockingQueue workQueue) { 
      
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue); 
      prestartAllCoreThreads();
      allowCoreThreadTimeOut(false);
      
    } //end of constructor
    
  } //end of class ImageUpgradeThreadPoolExecutor  
  
  //this map is used for currently running fixture upgrades per gateway
  public ConcurrentHashMap<Long, FixtureImageUpgradeWorker> gwFixtUpgrMap = 
    new ConcurrentHashMap<Long, FixtureImageUpgradeWorker>();

  //this map is used for currently running gateway upgrades per gateway (new protocol)
  public ConcurrentHashMap<Long, GatewayImageUpgradeXferWorker> gwXferUpgrMap = 
    new ConcurrentHashMap<Long, GatewayImageUpgradeXferWorker>();

  //this map is used for currently running gateway upgrades per gateway
  public ConcurrentHashMap<Long, GatewayImageUpgradeWorker> gwUpgrMap = 
    new ConcurrentHashMap<Long, GatewayImageUpgradeWorker>();
  
  //this map is used for currently running wds upgrades per gateway
  public ConcurrentHashMap<Long, WDSImageUpgradeWorker> gwWdsUpgrMap = 
    new ConcurrentHashMap<Long, WDSImageUpgradeWorker>();

  //this map is used for currently running plugload upgrades per gateway
  public ConcurrentHashMap<Long, PlugloadImageUpgradeWorker> gwPlugloadUpgrMap = 
    new ConcurrentHashMap<Long, PlugloadImageUpgradeWorker>();
  
  //this map is used to store in memory the firmwares
  public ConcurrentHashMap<ImageUpgradeDBJob, GWFirmware> gwFirmwares = 
    new ConcurrentHashMap<ImageUpgradeDBJob, GWFirmware>();
  
  static Object gwThreadLock = new Object();
    
  private void scheduleImageUpgradeJob(ImageUpgradeWork upgradeWork) {
  
  	imgUpgrThreadPool.execute(upgradeWork);
  	
  } //end of method scheduleImageUpgradeJob
  
  //allowing multiple image upgrade jobs at the same time  
  public class ImageUpgradeWork implements Runnable {
    
  	private Date scheduleTime = null;
    private ArrayList<ImageUpgradeDBJob> jobList = null;
    
    public ImageUpgradeWork(ArrayList<ImageUpgradeDBJob> jobList, Date schedTime) {
    
      this.jobList = jobList;
      scheduleTime = schedTime;
      
    } //end of constructor
    
    public void run() {
          	
		if(scheduleTime.after(new Date())) {
			//if the scheduled time has not reached go back to the queue
			logger.debug("work time " + scheduleTime.toString() + " has not yet come so rescheduling");
			ServerUtil.sleep(10);
			scheduleImageUpgradeJob(this);
			return;
		}
      int noOfJobs = jobList.size();
      logger.debug("upgw:Running ImageUpgradeSO");
      ImageUpgradeDBJob job = null;
      for(int i = 0; i < noOfJobs; i++) {
    	try {
	        job = jobList.get(i);   
	        if(abortedJobs.contains(job.getId())) {
	        	logger.debug(job.getId() + " job aborted so not scheduling");
	        	continue;
	        }
	        if(job.getDeviceType().equals(DeviceType.Gateway.getName())) {
	        	if(job.getImageName().toLowerCase().indexOf("gw.tar") != -1) {
            		startGWImageUpgrade(job, job.getDeviceIds(), ImageUpgradeSO.getInstance().getImageLocation() + job.getImageName());
            		continue;
	        	}
	          Thread gwUpgrThr = new GatewayImageUpgradeWorker(job);
	          gwUpgrThr.start();
	          try {
	            logger.info("waiting for the gw upgrade thread to finish");
	            gwUpgrThr.join();
	          }
	          catch(Exception e) {
	            e.printStackTrace();
	          }
	        } else if(job.getDeviceType().equals(DeviceType.WDS.getName())) {          
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
	        } else if(job.getDeviceType().equals(DeviceType.Plugload.getName())) {          
		          //upload to the plugload
		          Thread plugloadUpgrThr = new PlugloadImageUpgradeWorker(job);
		          plugloadUpgrThr.start();
		          try {
		          	logger.info("waiting for the plugload upgrade thread to finish");
		          	plugloadUpgrThr.join();
		          }
		          catch(Exception e) {
		          	e.printStackTrace();
		          }		        
	        } else {
	          if(job.getImageName().toLowerCase().indexOf("su_pyc") != -1) {
	            startFixtureImageUpgrade(job.getDeviceIds(), job.getImageName());
	            //EmsModeControl.resetMode();
	            continue;
	          }
	          if(abortedJobs.contains(job.getId())) {
	          	logger.debug(job.getId() + ": job is aborted");
	          	abortedJobs.remove(job.getId());
	          	continue;
	          }
	          if(job.getStopTime() != null && !job.getStopTime().after(new Date())) {
	          	//end time is already elapsed so cannot schedule now
	          	logger.debug(job.getId() + ": end time is already elapsed so cannot continue");
	          	continue;
	          }
	          FixtureImageUpgradeWorker upgrThr = new FixtureImageUpgradeWorker(job);
	          upgrJobMap.put(job.getId(), upgrThr);
	          upgrThr.start();
	          try {
	            logger.info("waiting for the fixture upgrade thread to finish");
	            upgrThr.join();	            
	          }
	          catch(Exception e) {
	            e.printStackTrace();
	          }
	          upgrJobMap.remove(job.getId());
	        }
	      }
    	catch (Exception e) {
    		e.printStackTrace();
    	}
      }
      EmsModeControl.resetMode();
    } //end of method run
    
  } //end of class ImageUpgradeWork

  ArrayList<Long> abortedJobs = new ArrayList<Long>();
    
  public void abortJob(long jobId) {
  	
  	abortedJobs.add(jobId);
  	if(upgrJobMap.containsKey(jobId)) {
  		abortedJobs.remove(jobId);
  		upgrJobMap.get(jobId).abortJob();
  	} else {
  		logger.debug(jobId + ": job does not exists in the cache");
  		ImageUpgradeDBJob dbJob = imgUpgrJobMgr.loadImageUpgradeJobById(jobId);
  		if(dbJob != null) {
  			dbJob.setStatus(ServerConstants.IMG_UP_STATUS_ABORTED);
  			dbJob.setEndTime(new Date());
  			firmUpMgr.save(dbJob);
  		}
  	}
  } //end of method cancelFirmwareUpgrade
  
  public void startDeviceImageUpgrade(ArrayList<ImageUpgradeDBJob> jobList) {

  	startDeviceImageUpgrade(jobList, new Date());
  	
  } //end of method startDeviceImageUpgrade
   
  public void startDeviceImageUpgrade(ArrayList<ImageUpgradeDBJob> jobList, Date scheduleTime) {
        
//    if(jobInProgress) {
//      //already file transfer is in progress so ignore
//      logger.info("file transfer is already in progress.");
//      return;
//    }
//    jobInProgress = true;

  	if(scheduleTime == null) {
  		scheduleTime = new Date();
  	}
    //add the job in the database
    int noOfJobs = jobList.size();
    ImageUpgradeDBJob job = null;
    for(int i = 0; i < noOfJobs; i++) {
      job = jobList.get(i);    
      if(job.getJobName() == null || job.getJobName().length() == 0) {
      	job.setJobName("ImageUpgrade_" + System.currentTimeMillis());
      }
      //job.setNoOfRetries(IMG_DEFAULT_FAIL_RETRIES);
      job.setScheduledTime(scheduleTime);
      job.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);
      
      String includeList = job.getIncludeList();
    	//Long deviceIds[] = job.getDeviceIds(); //from 3.2 it will be null
    	ArrayList<Long> deviceLongList = null;
    	if(includeList != null && !includeList.isEmpty()) {
    		//if the include list is not empty just upgrade them
    		deviceLongList = convertToLongsList(includeList);      		
    	}
    	
      //int noOfDevices = deviceLongList.size();
      if(job.getDeviceType().equals(DeviceType.Gateway.getName())) {
      	job = firmUpMgr.save(job);
      	//update the upgrade status of gateways as scheduled
      	//if include list empty work with entire list and remove exclude list
      	if(deviceLongList == null) {
      		//TODO in the next release
      	}
      	Iterator<Long> deviceIter = deviceLongList.iterator();
      	while(deviceIter.hasNext()) {
      		Long gwId = deviceIter.next();      
      		Gateway gw = gwMgr.loadGateway(gwId);   
      		if(gw == null) {
      			logger.info(gwId + ": There is no gateway");
      			continue;
      		}     	
      		gwMgr.setImageUpgradeStatus(gw.getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setDevice_type(DeviceType.Gateway.getName()); 
      		deviceStatus.setJobId(job.getId());
      		deviceStatus.setDeviceId(gw.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);	
      		deviceStatus = firmUpMgr.save(deviceStatus);
      	}
      } else if(job.getDeviceType().equals(DeviceType.WDS.getName())) {
      	job = firmUpMgr.save(job);
      	//update the upgrade status of switches as scheduled
      	//if include list empty work with entire list and remove exclude list
      	if(deviceLongList == null) {
      		//TODO in the next release
      	}
      	Iterator<Long> deviceIter = deviceLongList.iterator();
      	while(deviceIter.hasNext()) {
      		Long ercId = deviceIter.next();
      		Wds wds = wdsMgr.loadWdsById(ercId);   
      		if(wds == null) {
      			logger.info(ercId + ": There is no ERC");
      			continue;
      		} 
      		wdsMgr.setImageUpgradeStatus(wds.getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setDevice_type(DeviceType.WDS.getName()); 
      		deviceStatus.setJobId(job.getId());
      		deviceStatus.setDeviceId(wds.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);	
      		deviceStatus = firmUpMgr.save(deviceStatus);
      	}
      } else if(job.getDeviceType().equals(DeviceType.Plugload.getName())) {      	
      	job = firmUpMgr.save(job);      	
      	//update the upgrade status of plugloads as scheduled
      	int noOfDevices = job.getDeviceIds().length;	
      	for(int k = 0; k < noOfDevices; k++) {
      		Plugload device = plugloadMgr.getPlugloadById(job.getDeviceIds()[k]);   
      		if(device == null) {
      			logger.info(job.getDeviceIds()[k] + ": There is no Plugload");
      			continue;
      		}     
      		plugloadMgr.setImageUpgradeStatus(device.getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setDevice_type(DeviceType.Plugload.getName()); 
      		deviceStatus.setJobId(job.getId());
      		deviceStatus.setDeviceId(device.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		firmUpMgr.save(deviceStatus);
      	}
      } else {
      	job = firmUpMgr.save(job);
      	//update the upgrade status of fixtures as scheduled      	
      	
      	//if include list is empty work with entire list and remove exclude list
      	if(deviceLongList == null) {
      		//include list is empty
      		//get all the fixtures from the database and check for exclude list
      		HashMap<Long, DeviceInfo> deviceMap = FixtureCache.getInstance().getDeviceMap();
      		deviceLongList = new ArrayList<Long>(deviceMap.keySet());
      		if(job.getExcludeList() != null) {
      			ArrayList<Long> excludeList = convertToLongsList(job.getExcludeList());      		
      			deviceLongList.removeAll(excludeList);
      		}
      	}      	      	
      	Iterator<Long> iter = deviceLongList.iterator();
      	while(iter.hasNext()) {   
      		Long fixId = iter.next();
      		Fixture fixture = fixtureMgr.getFixtureById(fixId);   
      		if(fixture == null) {
      			logger.info(fixId + ": There is no Fixture");
      			iter.remove();
      			continue;
      		}     
      		//get the model no. for this image so that include sensors with that model no.
      		List<FirmwareUpgradeSchedule> fusList = firmUpSchedMgr.getFirmwareUpgradeSchedule(job.getImageName());
      		if(fusList != null && fusList.size() > 0) {
      			Iterator<FirmwareUpgradeSchedule> fusIter = fusList.iterator();      		
      			FirmwareUpgradeSchedule fus = fusList.get(0);
      		
      			String fixModel = "SU-2";
      			if(fixture.getModelNo() != null && !fixture.getModelNo().isEmpty()) {
      				fixModel = fixture.getModelNo();
      			}
      			if(!isModelApplicable(fixModel, fusIter)) {
      				//TODO update the status saying not applicable if it is part of include list. should not update the status if it 
      				//is part of all sensors
      				logger.debug(fixture.getId() + ": model is not applicable to the sensor so ignoring it");
      				continue;
      			}
      			/*
      			if(!fixModel.contains(fus.getModelNo())) {
      				continue;
      			} */
      			//assumption is that all the firmware upgrades related to that image are of the same version
      			if(ServerUtil.compareVersion(fixture.getVersion(), fus.getVersion()) >= 0) {
      				//sensor is already running with version later than the image version ignore it
      				logger.debug(fixture.getId() + ": sensor is already with the latest image so ignoring");
      				iter.remove();
      				continue;
      			}
      		}
      		fixtureMgr.setImageUpgradeStatus(fixture.getId(), ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		ImageUpgradeDeviceStatus deviceStatus = new ImageUpgradeDeviceStatus();
      		deviceStatus.setDevice_type(DeviceType.Fixture.getName()); 
      		deviceStatus.setJobId(job.getId());
      		deviceStatus.setDeviceId(fixture.getId());
      		deviceStatus.setNoOfAttempts(0);
      		deviceStatus.setStatus(ServerConstants.IMG_UP_STATUS_SCHEDULED);
      		firmUpMgr.save(deviceStatus);
      	}      	
      }  
      Long[] deviceIds = (Long[])deviceLongList.toArray(new Long[deviceLongList.size()]);
      job.setDeviceIds(deviceIds);
    	if(deviceIds.length == 0) {
    		//no sensors to upgrade. to remove the job
    		jobList.remove(job);
    		job.setStatus(ServerConstants.IMG_UP_STATUS_SUCCESS);      
    		job.setDescription("All Fixtures are up to date");
        firmUpMgr.save(job);   		
    	}
    }
    if(jobList.size() > 0) {
    	scheduleImageUpgradeJob(new ImageUpgradeWork(jobList, scheduleTime));
    }
    //jobInProgress = false;
    
  } //end of method startDeviceImage/upgrade
  
  private boolean isModelApplicable(String model, Iterator<FirmwareUpgradeSchedule> iter) {
  	  	
  	while(iter.hasNext()) {
  		if(model.contains(iter.next().getModelNo())) {
  			return true;
  		}
  	}
  	return false;
  	
  } //end of method isModelApplicable
  
  private ArrayList<Long> convertToLongsList(String str) {
  	
  	ArrayList<Long> longsList = new ArrayList<Long>();
		for (String s : str.split(",")) {
			longsList.add(new Long(s));
		}
		return longsList;
		
  } //end of method convertToLongsList
  
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
  
  private void upgradeRadio(Long[] fixtureIds, String fileName, int targetBuildNo) {
    
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
	    eventMgr.addEvent(fixt, desc, EventsAndFault.FIXTURE_IMG_UP_STR);
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
	    eventMgr.addEvent(fixt, desc, EventsAndFault.FIXTURE_IMG_UP_STR);
	  } else {
	    //not the last chunk but if it is failed, update the status and remove the fixture
	    //from the list
	    if(status.equals(ServerConstants.IMG_UP_STATUS_FAIL)) {
	      fixtureInProgressMap.remove(fixt.getId());
	      gwFixtureMap.get(gwId).remove(fixt);
	      fixtureMgr.setImageUpgradeStatus(fixt.getId(), status);
	      eventMgr.addEvent(fixt, desc, EventsAndFault.FIXTURE_IMG_UP_STR);
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
            
  	if(logger.isDebugEnabled()) {
  		logger.debug(gwId + ": got the ack for radio program " + ServerUtil.getLogPacket(pkt));
  	}
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
  
  public synchronized void gwMissingPacketRequest(Long gwId, byte[] packet) {
    
  	try {
  		GatewayImageUpgradeXferWorker worker = gwXferUpgrMap.get(gwId);
  		if (worker == null) {
  			logger.error(gwId + ": Received a gw missing packet request for a non running thread");
  			return;
  		}
  		worker.gwMissingPacketRequest(packet);
  	}
  	catch(Exception e) {
  		logger.error("not able to process the missing packet request", e);
  	}
   
  } //end of method gwMissingPacketRequest
  
  public void gwCancelFileUpload(Gateway gateway, byte[] packet) {
    
    if(gwUpgrMap.containsKey(gateway.getId())) {
      gwUpgrMap.get(gateway.getId()).gwCancelFileUpload(gateway, packet);
    }
    
  } //end of method gwCancelFileUpload
  
  public static Object gatewayUpgradeLock = new Object();
  private int gwUpgrStatus = 0;
  
  /**
   * New gateway upgrade will be a tar file, containing the gateway image and the gateway radio image.
   * @param fixtureIds
   * @param fileName
   */
	private void startGWImageUpgrade(ImageUpgradeDBJob dbJob, Long[] gwIds, String fileName) {
		
		GWFirmware gwFirmware;
		try {
			gwFirmware = new GWFirmware(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
			gwFirmware = null;
		}
		
		// Thread count is higher than semaphore count because the thread only dies when
		// it receives the upgrade ack (which happens ~2 minutes after the file has been ~
		// transfered, when the atmel upgrade succeeded) and we don't want to waste time.
		ExecutorService upgExecutor = Executors.newFixedThreadPool(15);
		
		for (int i = 0; i < gwIds.length; i++) {
			Gateway gw = gwMgr.loadGateway((long) gwIds[i]);
			if (gw == null) {
				logger.info(gwIds[i] + ": There is no gateway");
				continue;
			}
			
			if (gwFirmware == null) {
				String description = "The firmware could not be loaded into memory";
				firmUpMgr.finishDeviceUpgrade(dbJob.getId(), new Long(gwIds[i]), 
						ServerConstants.IMG_UP_STATUS_FAIL, 1, description, "");
			    gwMgr.setImageUpgradeStatus(gwIds[i], ServerConstants.IMG_UP_STATUS_FAIL);
			    eventMgr.addEvent(gw, description, EventsAndFault.GW_IMG_UP_STR); 
			}
			else {
				GatewayImageUpgradeXferWorker t = new GatewayImageUpgradeXferWorker(gw, dbJob, 
						gwFirmware);
				// this thread will add itself to the hashmap
				
			    upgExecutor.execute(t);
			}
		}
		
		/*
		 * Timeout to make sure we'll get out of here but the thread should never
		 * live for that long (all blocking calls have timeouts and they are parallel).
		 */
		try {
			upgExecutor.awaitTermination(3 * gwIds.length, TimeUnit.MINUTES);

		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("The new-protocol gw upgrade thread pool was interrupted."); 
		}
		finally {
		    dbJob.setEndTime(new Date());
		    firmUpMgr.save(dbJob);
		}
	    
		return;
	} // end of method startGWImageUpgrade

  /**
   * Receives Upgrade Ack from the Gateway and notifies the related working thread 
   * @param gwId
   * @param pkt
   */
	public void ackGatewayImageUpgrade(long gwId, byte[] pkt) {
		logger.debug(gwId + ": got the ack for gateway image upgrade "
				+ ServerUtil.getLogPacket(pkt));
		
		GatewayImageUpgradeXferWorker worker = gwXferUpgrMap.get(gwId);
		
		if (worker == null) {
			logger.error(gwId + ": Received a gw ack for a non running thread");
			return;
		}
		
		// Return value of the upgrade ack packet
		int pos = 5;
		gwUpgrStatus = pkt[pos];
		
		logger.debug(gwId + ": offering upgrade status ack " + gwUpgrStatus);
		worker.ackQueue.offer(gwUpgrStatus);

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
    
    int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS; 
    //bl major version
    pktIndex++;
    //bl minor version
    pktIndex++;
    //app major version
    byte major = packet[pktIndex++];
    //app minor version
    byte minor = packet[pktIndex++];
    //app bug fix
    byte releaseNo = packet[pktIndex++];
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
    
    String currVersion = major + "." + minor + "." + releaseNo + " b" + currVer;
    if(logger.isDebugEnabled()) {
    	logger.debug(wds.getId() + ": wds version -- " + currVersion);
    }
    wdsMgr.updateVersion(currVersion, wds.getId(), gwId);
    
    if(!gwWdsUpgrMap.containsKey(gwId)) {
    	return ServerConstants.IMG_UP_STATUS_NOT_PENDING;      
    }
    return gwWdsUpgrMap.get(gwId).nodeRebooted(wds, currVer, gwId, imageUpgStatus, currVersion);
    
  } //end of method nodeRebooted
    
  //this is called from node boot info
  public String nodeRebooted(Plugload plugload, short currVer, short otherVer, byte appId,
      long gwId, int imgUpgrStatus, String version) {
    
    if(gwPlugloadUpgrMap.containsKey(gwId)) {
      return gwPlugloadUpgrMap.get(gwId).nodeRebooted(plugload, currVer, otherVer, appId, gwId, imgUpgrStatus, version);
    } 
    /* to support new image upgrade for plugloads
    else {
    	//it didn't reboot because of image upgrade
    	boolean startUpgrThr = PlugloadCache.getInstance().nodeRebooted(plugload.getId());
    	if(startUpgrThr) {
    		//start the timer
    		new Thread() {
    			public void run() {
    				ServerUtil.sleep(UPGRADE_ON_REBOOT_INTERVAL);
    				//clear the reboot list 
    				ArrayList<Long> nodeList = PlugloadCache.getInstance().drainNodeRebootList();
    				//start the image upgrade on all the rebooted plugloads
    				//find the image for all the plugloads and upgrade them using one file at a time
    				Iterator<Long> nodeIter = nodeList.iterator();
    				HashMap<String, StringBuffer> modelPlugloadMap = new HashMap<String, StringBuffer>();
    				while(nodeIter.hasNext()) {
    					Long plugloadId = nodeIter.next();
    					Device dev = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
    					String model = dev.getModelNo();
    					if(!modelPlugloadMap.containsKey(model)) {
    						modelPlugloadMap.put(model, new StringBuffer());
    						modelPlugloadMap.get(model).append(plugloadId);
    					} else {
    						modelPlugloadMap.get(model).append("," + plugloadId);
    					}
    					
    				}
    				
    				//group plugloads based on image 
    				Iterator<String> modelIter = modelPlugloadMap.keySet().iterator();    				
    				HashMap<String, StringBuffer> imgPlugloadMap = new HashMap<String, StringBuffer>();
    				HashMap<String, FirmwareUpgradeSchedule> imgScheduleMap = new HashMap<String, FirmwareUpgradeSchedule>();
    				while(modelIter.hasNext()) {
    					String model = modelIter.next();
    					//get the image for this model
    					List<FirmwareUpgradeSchedule> scheduleList = firmUpSchedMgr.getAllActiveFirwareSchedules();
    					//find the correct image based on model
    					FirmwareUpgradeSchedule modelSchedule = null; 
    					Iterator<FirmwareUpgradeSchedule> schedListIter = scheduleList.iterator();    					
    					while(schedListIter.hasNext()) {
    						modelSchedule = schedListIter.next();
    						if(model.contains(modelSchedule.getModelNo())) {
    							break;
    						}
    					}
    					if(modelSchedule == null) {
    						continue;
    					}
    					if(!imgPlugloadMap.containsKey(modelSchedule.getFileName())) {
    						imgPlugloadMap.put(modelSchedule.getFileName(), modelPlugloadMap.get(model));
    						imgScheduleMap.put(modelSchedule.getFileName(), modelSchedule);
    					} else {
    						imgPlugloadMap.get(modelSchedule.getFileName()).append(","  + modelPlugloadMap.get(model).toString());
    					}    					
    				}
    				//schedule a job to run for each image with the rebooted plugloads
    				ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
    				Iterator<String> imgIter = imgPlugloadMap.keySet().iterator();
    				while(imgIter.hasNext()) {
    					String imgFile = imgIter.next();
    					// Creates a Fixture upgrade job
    					ImageUpgradeDBJob fixtureJob = new ImageUpgradeDBJob();
    					fixtureJob.setDeviceType(DeviceType.Fixture.getName());		
    					fixtureJob.setExcludeList(null);
    					fixtureJob.setIncludeList(imgPlugloadMap.get(imgFile).toString());
    					fixtureJob.setImageName(imgFile);
    					fixtureJob.setNoOfRetries(imgScheduleMap.get(imgFile).getRetries());
    					fixtureJob.setRetryInterval(imgScheduleMap.get(imgFile).getRetryInterval());
    					fixtureJob.setJobName(imgFile+ "_OnReboot");    					
							fixtureJob.setScheduledTime(new Date());    					
							jobList.add(fixtureJob);
    				}    				
    				startDeviceImageUpgrade(jobList);
    			}
    		}.start(); 	
    	}    	
    } */
    return ServerConstants.IMG_UP_STATUS_NOT_PENDING;
    
  } //end of method nodeRebooted
  
  public void cancelFileUpload(Wds wds, byte[] packet, long gwId) {
    
    if(gwWdsUpgrMap.containsKey(gwId)) {
      gwWdsUpgrMap.get(gwId).cancelFileUpload(wds, packet, gwId);
    }
    
  } //end of method cancelFileUpload
  
} //end of class ImageUpgradeSO
