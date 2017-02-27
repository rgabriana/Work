/**
 * 
 */
package com.ems.server.discovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.ems.server.GatewayInfo;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureDistancesService;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.InventoryDeviceService;
import com.ems.service.ProfileManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.action.SpringContext;
import com.ems.model.Ballast;
import com.ems.model.Building;
import com.ems.model.Bulb;
import com.ems.model.Campus;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.FixtureDistances;
import com.ems.model.Floor;
import com.ems.model.Groups;
import com.ems.model.InventoryDevice;
import com.ems.model.ProfileHandler;
import com.ems.model.SystemConfiguration;

/**
 * @author EMS
 *
 */
public class DiscoverySO {

  private static int DISCOVERY_RETRY_INTERVAL = 10; //10 secs
  private static int DEFAULT_RADIO_RATE = 2;
  private static int DISCOVERY_MAX_NO_OF_ALLOWED_SENSORS_PER_GATEWAY = 100;
  private static int DISCOVERY_MAX_TIME = 180 * 1000; //3 min. it is in sec
  private static int VALIDATION_TARGET_AMB_LIGHT = 9990;
  private static int VALIDATION_TARGET_REL_AMB_LIGHT = 200;
  private static int VALIDATION_TARGET_REL_AMB_LIGHT_2 = 500;
  private static int VALIDATION_MAX_ENERGY_READING = 40;
  private static short DEFAULT_FIXTURE_VOLTAGE = 277;
  private static int VALIDATION_INACTIVITY_TIME = 15 * 60 * 1000; //15 minutes it is in sec
  
  public static int DISC_STATUS_SUCCESS = 1;  
  public static int DISC_STATUS_STARTED = 2;
  public static int DISC_STATUS_INPROGRESS = 3;
  public static int DISC_ERROR_INPROGRESS = 4;
  public static int DISC_ERROR_GW_CH_CHANGE_DEF = 5;
  public static int DISC_ERROR_TIMED_OUT = 6;
  public static int DISC_ERROR_GW_CH_CHANGE_CUSTOM = 7;  
  
  public static int COMM_STATUS_SUCCESS = 8;
  public static int COMM_STATUS_STARTED = 9;
  public static int COMM_STATUS_INPROGRESS = 10;
  public static int COMM_STATUS_FAIL = 11;
  public static int COMM_ERROR_INPROGRESS = 12;
  public static int COMM_ERROR_GW_CH_CHANGE_DEF = 13;
  public static int COMM_ERROR_GW_CH_CHANGE_CUSTOM = 14;
  public static int COMM_ERROR_INACTIVE_TIMED_OUT = 15;
  public static int COMM_ERROR_INACTIVE_TIMED_OUT_GW_CH_CHANGE_CUSTOM = 16;
  
  private static String INSTALL_STATE_READY= "READY";
  private static String INSTALL_STATE_DISCOVERY = "DISCOVERY";
  private static String INSTALL_STATE_COMMISSIONING = "COMMISSIONING";

  private static DiscoverySO instance = null;
  
  private HashMap<String, InventoryDevice> deviceMap = new HashMap<String, InventoryDevice>();
  private HashMap<Long, Long> floorUpTimeMap = new HashMap<Long, Long>();
  
  //private Timer discoveryTimer = new Timer("Discovery Thread", true);
  //private int discoverInterval = 5 * 60 * 1000; //5 minutes
  //private int discoverInitialDelay = 30 * 1000; // 30 seconds
  
  private int lastDiscoverFloorId = 0;
  private InventoryDeviceService inventoryService = null;
  private FixtureManager fixtureMgr = null;
  private FixtureDistancesService fdService = null;
  private FloorManager floorMgr = null;
  private BuildingManager buildingMgr = null;
  private CampusManager campusMgr = null;
  private ProfileManager profileMgr = null;
  private GatewayManager gwMgr = null;
  private GroupManager groupMgr = null;
  SystemConfigurationManager sysMgr = null;
  private EventsAndFaultManager eventMgr = null;
  
  private long lastValActivityTime = 0;  
  private int noOfValidatedFixtures = 0;
  private Boolean distanceDiscProgress = false;
  private long m_iCurrentFloorId = 1;
  
  private static final Logger logger = Logger.getLogger("Discovery");
     
  /**
   * 
   */
  private DiscoverySO() {
    
    // TODO Auto-generated constructor stub
    init();
    
    //DiscoveryTask discoverTask = new DiscoveryTask(); 
    //discoveryTimer.schedule(discoverTask, discoverInitialDelay, discoverInterval);
    
  } //end of constructor
  
  public static DiscoverySO getInstance() {
    
    if(instance == null) {
      synchronized(DiscoverySO.class) {
	if(instance == null) {
	  instance = new DiscoverySO();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  public void readDiscoveryParams() {
      
    SystemConfiguration tempConfig = sysMgr.loadConfigByName("discovery.retry_interval");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database discovery retry time -- " + tempConfig.getValue());
      }
      DISCOVERY_RETRY_INTERVAL = Integer.parseInt(tempConfig.getValue());
    }
    SystemConfiguration oConfig = sysMgr.loadConfigByName("discovery.max_no_install_sensors");
    if (oConfig != null) {
      if(logger.isDebugEnabled()) {
        logger.debug("from database max number of installed sensors allowed -- " + oConfig.getValue());
      }
        DISCOVERY_MAX_NO_OF_ALLOWED_SENSORS_PER_GATEWAY = Integer.parseInt(oConfig.getValue());
    }
    
    tempConfig = sysMgr.loadConfigByName("default.radio_rate");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database default radio rate -- " + tempConfig.getValue());
      }
      DEFAULT_RADIO_RATE = Integer.parseInt(tempConfig.getValue());      
    }
    tempConfig = sysMgr.loadConfigByName("discovery.max_time");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database discovery max time -- " + tempConfig.getValue());
      }
      DISCOVERY_MAX_TIME = Integer.parseInt(tempConfig.getValue()) * 1000;      
    } 
    tempConfig = sysMgr.loadConfigByName("discovery.validationTargetAmbLight");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database discovery validation target amb light -- " + tempConfig.getValue());
      }
      VALIDATION_TARGET_AMB_LIGHT = Integer.parseInt(tempConfig.getValue());      
    }
    tempConfig = sysMgr.loadConfigByName("discovery.validationTargetRelAmbLight");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database discovery validation target rel amb light -- " + tempConfig.getValue());
      }
      VALIDATION_TARGET_REL_AMB_LIGHT = Integer.parseInt(tempConfig.getValue());      
    }
    tempConfig = sysMgr.loadConfigByName("discovery.validationMaxEnergyPercentReading");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database discovery validation max energy percentage reading -- " + 
	  tempConfig.getValue());
      }
      VALIDATION_MAX_ENERGY_READING = Integer.parseInt(tempConfig.getValue());      
    }
    tempConfig = sysMgr.loadConfigByName("fixture.default_voltage");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database fixture default fixture voltage -- " + tempConfig.getValue());
      }
      DEFAULT_FIXTURE_VOLTAGE = Short.parseShort(tempConfig.getValue());      
    }
    tempConfig = sysMgr.loadConfigByName("commissioning.inactivity_timeout");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
	logger.debug("from database commissioning inactivity timeout -- " + tempConfig.getValue());
      }
      VALIDATION_INACTIVITY_TIME = Integer.parseInt(tempConfig.getValue()) * 1000;      
    }
    tempConfig = sysMgr.loadConfigByName("discovery.validationTargetRelAmbLight_2");
    if(tempConfig != null) {
      if(logger.isDebugEnabled()) {
    	logger.debug("from database discovery validation target rel amb light 2 -- " + tempConfig.getValue());
      }
    	VALIDATION_TARGET_REL_AMB_LIGHT_2 = Integer.parseInt(tempConfig.getValue());      
    }
        
  } //end of method readDiscoveryParams
  
  public static int getValidationTargetAmbLight() {
    
    return VALIDATION_TARGET_AMB_LIGHT;
    
  } //end of method getValidationTargetAmbLight
  
  public static int getValidationTargetRelativeAmbLight() {
    
    return VALIDATION_TARGET_REL_AMB_LIGHT;
    
  } //end of method getValidationTargetRelativeAmbLight
  
  public static int getValidationTargetRelativeAmbLight_2() {
    
    return VALIDATION_TARGET_REL_AMB_LIGHT_2;
    
  } //end of method getValidationTargetRelativeAmbLight_2
  
  public static int getValidationMaxEnergyReading() {
    
    return VALIDATION_MAX_ENERGY_READING;
    
  } //end of method getValidationTargetRelativeAmbLight

  public int getMaxNoOfAllowedSensors() {
	  return DISCOVERY_MAX_NO_OF_ALLOWED_SENSORS_PER_GATEWAY;
  }
  
  public static int getDefaultRadioRate() {
    
    return DEFAULT_RADIO_RATE;
    
  } //end of method getDefaultRadioRate
  
  private void init() {
    
    if(inventoryService == null) {
      inventoryService = (InventoryDeviceService)SpringContext.getBean("inventoryDeviceService");
    }
    if(fixtureMgr == null) {
      fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
    }
    if(floorMgr == null) {
      floorMgr = (FloorManager)SpringContext.getBean("floorManager");
    }
    if(buildingMgr == null) {
      buildingMgr = (BuildingManager)SpringContext.getBean("buildingManager");
    }
    if(campusMgr == null) {
      campusMgr = (CampusManager)SpringContext.getBean("campusManager");
    }
    if(profileMgr == null) {
      profileMgr = (ProfileManager)SpringContext.getBean("profileManager");
    }
    if(fdService == null) {
      fdService = (FixtureDistancesService)SpringContext.getBean("fixtureDistancesService");
    }
    if(gwMgr == null) {
      gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    }
    if(groupMgr == null) {
      groupMgr = (GroupManager)SpringContext.getBean("groupManager");
    }
    if(sysMgr == null) {
      sysMgr = (SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
    }
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    //System.out.println("inside the init");
    if(logger.isDebugEnabled()) {
      logger.debug("inside the init");
    }
    //retrieve the lastDiscoverFloorId from the database InventoryDevice table
    //so that for the new discoverd devices, correct dummy floor id can be assigned.
    lastDiscoverFloorId = 0;
    //load the existing inventroy devices
    List<InventoryDevice> deviceList = inventoryService.loadAllInventoryDeviceByType(ServerConstants.DEVICE_FIXTURE);
    if(deviceList != null) {
      //System.out.println("no. of fixtures in the db in init " + deviceList.size());
      if(logger.isDebugEnabled()) {
	logger.debug("no. of fixtures in the db in init " + deviceList.size());
      }
      Iterator iter = deviceList.iterator();
      while(iter.hasNext()) {
        InventoryDevice device = (InventoryDevice)iter.next();      
        deviceMap.put(device.getSnapAddr(), device);
      }
    } else {
      //System.out.println("device list in init is null");
      if(logger.isDebugEnabled()) {
	logger.debug("device list in init is null");
      }
    }
  } //end of method init
    
  public boolean commissionFixture(Long id) {
    
    lastValActivityTime = System.currentTimeMillis();
    //System.out.println("commissionFixture called -- " + id.intValue());
    if(logger.isInfoEnabled()) {
      logger.info(id.longValue() + ": commissionFixture called");
    }
    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
    deviceImpl.setWirelessParams(id);
    return true;
    
  } //end of method validateFixture

  public boolean commissionFixture(long fixtureId, long gatewayId) {
	    
    lastValActivityTime = System.currentTimeMillis();
	    //System.out.println("commissionFixture called -- " + id.intValue());
    if(logger.isInfoEnabled()) {
	    logger.info(fixtureId + ": commissionFixture called, via gateway: " + gatewayId);
    }
	    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
	    deviceImpl.setWirelessParams(fixtureId, gatewayId);
	    return true;
	    
  } //end of method validateFixture

  public boolean validateFixture(Long id) {
    
    //System.out.println("validateFixture called -- " + id.intValue());
    if(logger.isInfoEnabled()) {
      logger.info(id.longValue() + ": validateFixture called");
    }
    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
    deviceImpl.sendValidationCmd(id.intValue(), 0);
        
    return true;
    
  } //end of method validateFixture
    
  
  public boolean isDistanceDiscProgress() {
    
    return distanceDiscProgress;
    
  } //end of method isDistanceDiscProgress
  
  private String currDistDiscFixture = "";
  private int noOfLigthLevelResp = 0;
  
  private HashMap baseFixtureLightMap = new HashMap();
  
  public void fixtureDistance(String snapAddr, short light) {
    
    noOfLigthLevelResp++;
    if(currDistDiscFixture != null) {
      //this is light level when one of the fixture is on
      FixtureDistances fd = new FixtureDistances();
      fd.setDstFixture(snapAddr);
      fd.setLightLevel((int)light);
      fd.setSrcFixture(currDistDiscFixture);
      //System.out.println("fd - " + currDistDiscFixture + "-" + snapAddr + "-" + light);
      if(logger.isInfoEnabled()) {
	logger.info("fd - " + currDistDiscFixture + "-" + snapAddr + "-" + light);
      }
      fdService.addFixtureDistance(fd);
    } else {
      //this is the light level when all the fixtures are off
      Fixture fixture = fixtureMgr.getFixtureBySnapAddr(snapAddr);
      //System.out.println("base light level of " + snapAddr + " is " + light);
      if(logger.isInfoEnabled()) {
	logger.info(snapAddr + ": base light level - " + light);
      }
      baseFixtureLightMap.put(fixture.getId(), "" + light);
    }
    
  } //end of method fixtureDistance
  
  private void findOutDistances(final int floorId) {
  
    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
        	
    //first clear all pending acks.	
    //ServerMain.getInstance().clearAllPendingRequests();
    fdService.removeAllFixtureDistances();
			
    //get all non validated fixtures
    List fixtureList = fixtureMgr.getUnValidatedFixtureList(floorId);
    Iterator<Fixture> iter = fixtureList.iterator();
    Fixture fixture = null;
    int totalFixtures = fixtureList.size();

    //first send manual mode off to all the fixtures
    //System.out.println("sending manual mode off"); //it should be minimum light level
    if(logger.isInfoEnabled()) {
      logger.info("sending manual mode off");
    }
    int[] fixtureArr = new int[totalFixtures];
    int i = 0;
    while(iter.hasNext()) {
      fixture = iter.next();
      fixtureArr[i++] = fixture.getId().intValue();
      //deviceImpl.dimFixture(fixture.getId().intValue(), 1, 20);
    }
    deviceImpl.dimFixtures(fixtureArr, 1, 20);
    //Make sure all the fixtures received the manual mode off command 
    ServerUtil.sleep(3);
    //System.out.println("manual mode off finished");
    if(logger.isInfoEnabled()) {
      logger.info("manual mode off finished");
    }
    //get light levels of all fixtures to get the difference in ligth levels
    iter = fixtureList.iterator();	
    //send the get light level to all the fixtures	
    //System.out.println("sending get light level");
    if(logger.isInfoEnabled()) {
      logger.info("sending get light level");
    }
    currDistDiscFixture = null;
    while(iter.hasNext()) {
      fixture = iter.next();	  
      deviceImpl.getCurrentState(fixture.getId());      
    }	
    //make sure that all the devices returned light level
    ServerUtil.sleep(5);
    //we have to receive light  level from all the fixtures
    if(noOfLigthLevelResp < (totalFixtures)) {
      ServerUtil.sleep(5);
    }
    
    //clear all pending acks before proceeding with next level
    //ServerMain.getInstance().clearAllPendingRequests();
    iter = fixtureList.iterator();    
    //next send manual mode on to all the fixtures one by one 
    while(iter.hasNext()) {
      fixture = iter.next();
      //manual mode on to one fixture
      deviceImpl.dimFixture(fixture.getId().intValue(), 100, 20);
      currDistDiscFixture = fixture.getSnapAddress();
      ServerUtil.sleep(4); //sleep 2 sec	  
      Iterator<Fixture> innerIter = fixtureList.iterator();
      Fixture innerFixture = null;
      //get current state from all fixtures
      noOfLigthLevelResp = 0;
      while(innerIter.hasNext()) {
	innerFixture = innerIter.next();	    
	deviceImpl.getCurrentState(innerFixture.getId());
	//ServerUtil.sleepMilli(200);
      }
      //make sure that all the devices returned light level
      String fileLocation = ServerMain.getInstance().getTomcatLocation() + "location2";
      if(!new File(fileLocation).exists()) {
	ServerUtil.sleep(5);
      }
      //we have to receive light  level from all the fixtures
      if(noOfLigthLevelResp < totalFixtures) {
	ServerUtil.sleep(5);
      }
      
      //manual mode off to the current fixture
      deviceImpl.dimFixture(fixture.getId().intValue(), 1, 20);
    }		
    ServerUtil.sleep(3);
    //last send manual mode on to all the fixtures
    //System.out.println("sending manual mode on");
    if(logger.isInfoEnabled()) {
      logger.info("sending manual mode on");
    }
    iter = fixtureList.iterator();
    while(iter.hasNext()) {
      fixture = iter.next();	  
      deviceImpl.dimFixture(fixture.getId().intValue(), 100, 1);            
    }
    //System.out.println("manual mode on finished");
    if(logger.isInfoEnabled()) {
      logger.info("manual mode on finished");
    }
    //System.out.println("finished distance discovery");
    if(logger.isInfoEnabled()) {
      logger.info("finished distance discovery");
    }
    //autoPlace = false;
    //calculate distances
    calculateDistances(1);
  
  } //end of method findOutDistances
  
  boolean autoPlace = false;
  
  public void startDistanceDiscovery(final int floorId) {
    
    //System.out.println("startDistanceDiscovery called - " + floorId);
    if(logger.isDebugEnabled()) {
      logger.debug(floorId + ": startDistanceDiscovery called");
    }
//    synchronized(distanceDiscProgress) {
//      if(distanceDiscProgress) {
//	return;
//      }
//      distanceDiscProgress = true;
//    }
    if(autoPlace) {
      //System.out.println("auto place is already in progress");
      logger.error("auto place is already in progress");
      return;
    }
    noOfValidatedFixtures = 0;
    currValMap.clear();
    pinnedFixtures.clear();
    autoPlace = true;
    new Thread() {
      
      public void run() {
	
	DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
	//List fixtureList = fixtureMgr.loadFixtureByFloorId(new Long(floorId));
	List fixtureList = fixtureMgr.getUnValidatedFixtureList(floorId);
	Iterator iter = fixtureList.iterator();
	Fixture fixture = null;	
	while(iter.hasNext()) {
	  fixture = (Fixture)iter.next();
	  deviceImpl.sendValidationCmd(fixture.getId().intValue(), 0);
	}
	
      } //end of method run
      
    }.start();
        
  } //end of method startDistanceDiscovery
  
  private ArrayList distanceList = new ArrayList();
      
  private void calculateDistances(int id) {
    
    //System.out.println("calculating distances");
    if(logger.isDebugEnabled()) {
      logger.debug("calculating distances");
    }
    List<Fixture> fixtureList = fixtureMgr.getUnValidatedFixtureList(id);
    Iterator<Fixture> fixtureIter = fixtureList.iterator();
    Fixture fixture = null;
    while(fixtureIter.hasNext()) {
      //for each fixture in the floor, get the distances from other fixtures
      fixture = fixtureIter.next();
      //System.out.println("requesting distances for fixture - " + fixture.getSnapAddress());
      if(logger.isDebugEnabled()) {
	logger.debug("requesting distances for fixture - " + fixture.getSnapAddress());
      }
      List<FixtureDistances> fdList = fdService.getFixtureDistances(fixture.getSnapAddress());
      if(fdList == null) {
	continue;
      }
      Iterator<FixtureDistances> iter = fdList.iterator();
      FixtureDistances fd = null;
      HashMap distanceMap = new HashMap();
      while(iter.hasNext()) {
	fd = iter.next();
	Fixture dstFixture = fixtureMgr.getFixtureBySnapAddr(fd.getDstFixture());
	if(dstFixture.getSnapAddress().equals(fixture.getSnapAddress())) {
	  continue;
	}
	int lightLevel = fd.getLightLevel().intValue();
	int baseLevel = 0;
	if(baseFixtureLightMap.containsKey(dstFixture.getId())){
	  baseLevel = Integer.parseInt(baseFixtureLightMap.get(dstFixture.getId()).toString());
	}
	//int diffLight = fd.getLightLevel().intValue() - Integer.parseInt(
	  //  baseFixtureLightMap.get(dstFixture.getId()).toString());
	int diffLight = lightLevel - baseLevel;
        //System.out.println("fd- " + fixture.getSnapAddress() + " " + 
          //  fd.getDstFixture() + " " + fd.getLightLevel() + " " + 
            //diffLight);
	if(logger.isInfoEnabled()) {
	  logger.info("fd- " + fixture.getSnapAddress() + " " + fd.getDstFixture() + " " + 
            fd.getLightLevel() + " " + diffLight);
	}
        float distance = -1;
        if(diffLight > 20) {          
          if(diffLight < 50) {
            distance = 10;
          } else if(diffLight < 75) {
            distance = 8;
          } else  if(diffLight < 100) {
            distance = 6;
          } else if(diffLight < 125) {
            distance = 4;
          } else if(diffLight < 150) {
            distance = 2;
          } else {
            distance = 1;
          }
          String distanceStr = fixture.getSnapAddress() + " " + fd.getDstFixture() +
          	" " + distance * 15;
          if(!distanceList.contains(distanceStr)) {
            distanceList.add(distanceStr);
          }
        }        
      }
    }
    
  } //end of method calculateDistances
  
  private void readLocationFile(int valFixtureNo) {
    
    String fileLocation = ServerMain.getInstance().getTomcatLocation() + "location" + valFixtureNo;
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(fileLocation));
      String line = "";
      int count = 0;
      while((line = br.readLine()) != null) {
	//System.out.println("location line = " + line);
	if(logger.isInfoEnabled()) {
	  logger.info("location line = " + line);
	}
	StringTokenizer st = new StringTokenizer(line, ",");
	String snapAddr = null;
	int x = 0;
	int y = 0;
	if(st.hasMoreTokens()) {
	  snapAddr = st.nextToken();
	}
	if(st.hasMoreTokens()) {
	  x = Integer.parseInt(st.nextToken());
	}
	if(st.hasMoreTokens()) {
	  y = Integer.parseInt(st.nextToken());
	}
	if(pinnedFixtures.contains(snapAddr)) {
	  continue;
	}
	Fixture fixture = fixtureMgr.getFixtureBySnapAddr(snapAddr);
	//fixture.setXaxis(x);
	//fixture.setYaxis(y);
	if(fixture != null) {
	  fixtureMgr.updatePosition(fixture.getId(), x, y, 
	      ServerConstants.FIXTURE_STATE_PLACED_STR);
	}
	count++;	
	ServerUtil.sleep(2);	
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    } 
    finally {
      if(br != null) {
	try {
	  br.close();
	}
	catch(Exception e1) {}
      }
    }
    
  } //end of method readLocationFile
  
//  public void startDistanceDiscovery1() {
//    
//    //System.out.println("startDistanceDiscovery called");
//    logger.debug("startDistanceDiscovery called");
//    List fixtureList = fixtureMgr.getAllFixtures();
//    Iterator iter = fixtureList.iterator();
//    //System.out.println("no. of fixtures -- " + fixtureList.size());
//    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
//    Fixture fixture = null;
//    //first pass
//    while(iter.hasNext()) {
//      fixture = (Fixture)iter.next();
//      //System.out.println("fixture id in start distance discovery -- " + fixture.getId());
//      deviceImpl.sendDiscoverMode(fixture.getId().intValue(), 0, 300, 300);      
//      //ServerUtil.sleepMilli(100);
//    }
//    //System.out.println("finished first pass");
//    logger.debug("finished first pass");
//    ServerUtil.sleep(5);
//    //second pass
//    iter = fixtureList.iterator();    
//    while(iter.hasNext()) {
//      fixture = (Fixture)iter.next();
//      deviceImpl.sendDiscoverMode(fixture.getId().intValue(), 1, 300, 300);    
//      ServerUtil.sleep(3);
//      //third pass
//      deviceImpl.sendDiscoverMode(fixture.getId().intValue(), 2, 300, 300);
//    }
//    //System.out.println("finished third pass");
//    logger.info("finished third pass");
//    //fourth pass   
//    ServerUtil.sleep(5);
//    fixtureList = fixtureMgr.getSortedFixtures();
//    iter = fixtureList.iterator();    
//    while(iter.hasNext()) {
//      fixture = (Fixture)iter.next();
//      //System.out.println("fixture id - " + fixture.getId());
//      logger.debug("fixture id - " + fixture.getId());
//      deviceImpl.sendDiscoverMode(fixture.getId().intValue(), 3, 300, 300); 
//      //ServerUtil.sleepMilli(100);
//    }
//    //System.out.println("finished fourth pass");
//    logger.info("finished fourth pass");
//   
//  } //end of method startDistanceDiscovery
  
  class LockObj {
    
    boolean gotAck = false;
    Object gwNetDisLock = new Object();
        
  } //end of class LockObj
  
  //assuming that at a time only one gateway will be in network discovery/commissioning phase
  //if more gateways are operated in parallel then these locks should be per gateway basis
  //using the above LockObj put into a hash map.
  private boolean gotGwAck = false;
  public void receivedGwWirelessChangeAck(long gwId) {
    
    gotGwAck = true;
    try {
      synchronized(gwNetDisLock) {
	gwNetDisLock.notify();
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      logger.error("Error in notifying in receivedGwWirelessChangeAck");
    }
    
  } //end of method receivedGwWirelessChangeAck
  
  //this is called when individual commission is called
  public int startValidation(long fixtureId, int type) {
    
    Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
    ArrayList<Fixture> fixtureList = new ArrayList<Fixture>();
    fixtureList.add(fixture);
    return startValidation(fixture.getSecGwId(), fixtureList, type);
    
  } //end of method startValidation
    
  private String installState = INSTALL_STATE_READY;
  private int commStatus = 0;
 
  public int getCommissioningStatus() {
  
    return commStatus;
    
  } //end of method getCommissioningStatus
  
  //this is called when bulk commission is clicked
  public int startValidation(final long gatewayId, final List<Fixture> fixtureList, 
      final int type) {
    
    if(logger.isDebugEnabled()) {
      logger.debug(gatewayId + ": startValidation called");
    }
    if(installState.equals(INSTALL_STATE_DISCOVERY)) {
      logger.error("Discovery is in progress");
      return DISC_ERROR_INPROGRESS;
    } else if(installState.equals(INSTALL_STATE_COMMISSIONING)) {
      logger.error("Commissioning is in progress");
      return COMM_ERROR_INPROGRESS;
    } 
    installState = INSTALL_STATE_COMMISSIONING;
    commStatus = COMM_STATUS_STARTED;
    noOfValidatedFixtures = 0;
    currValMap.clear();
    lastValActivityTime = System.currentTimeMillis();
    
    new Thread() {
      public void run() {
			
	Gateway gw = gwMgr.loadGateway(gatewayId); 
	if (gw == null) {
	  return;
	}	
	if(fixtureList == null || fixtureList.size() == 0) {
	  //no uncommissioned fixtures
	  return;
	}
	//generate commissioning start event
	eventMgr.addEvent(gw, "Commissioning started", EventsAndFault.COMMISSION_EVENT_STR, 
	    EventsAndFault.INFO_SEV_STR);
	int noOfFixtures = fixtureList.size();
	int[] fixtureArr = new int[noOfFixtures];
	for(int i = 0; i < noOfFixtures; i++) {
	  fixtureArr[i] = fixtureList.get(i).getId().intValue();
	}

	fixtureMgr.updateCommissionStatus(fixtureArr, ServerConstants.COMMISSION_STATUS_UNKNOWN);

	//move the gateway to the factory defaults	
	GatewayImpl.getInstance().setWirelessFactoryDefaults(gw);
	//wait for the ack to get back from the gw for the wireless parameters change
	gotGwAck = false;    
	synchronized(gwNetDisLock) {
	  try {	
	    gwNetDisLock.wait(3000); //wait for 3 sec		
	  }
	  catch(Exception e) {
	    e.printStackTrace();
	  }
	}
	if(!gotGwAck) {
	  //System.out.println(gw.getId() + ": No ack for factory before validation");
	  logger.error(gw.getId() + ": No ack for factory before validation");
	  //generate commission fail event	
	  eventMgr.addEvent(gw, "Commissioning failed: GW didn't move to default channel", 
	      EventsAndFault.COMMISSION_EVENT_STR, EventsAndFault.MAJOR_SEV_STR);
	  commStatus = COMM_ERROR_GW_CH_CHANGE_DEF;
	  installState = INSTALL_STATE_READY;
	  return;
	}
	//update the gateway operational mode in gateway info
  GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gw.getId());
  if(gwInfo != null) {
  	gwInfo.setOperationalMode(GatewayInfo.GW_COMMISSIONING_MODE);
  }
	commStatus = COMM_STATUS_INPROGRESS;
	if(logger.isDebugEnabled()) {
	  logger.debug("validation type -- " + type);
	}
	DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
	deviceImpl.sendValidationCmd(fixtureList, fixtureArr, 0, type, gw);
	while(installState.equals(INSTALL_STATE_COMMISSIONING)) {	  
	  if(System.currentTimeMillis() - lastValActivityTime < VALIDATION_INACTIVITY_TIME) {
	    ServerUtil.sleepMilli(500);
	    continue;
	  }
	  //validation is inactive for more than an configured time
	  commStatus = COMM_ERROR_INACTIVE_TIMED_OUT;
	  String desc = "Commissioning timed out due to inactivity";
	  logger.error(gw.getId() + ": " + desc);
	  //generate commission timed out event	
	  eventMgr.addEvent(gw, desc, EventsAndFault.COMMISSION_EVENT_STR, 
	      EventsAndFault.MAJOR_SEV_STR);
	  //move the gateway out of default
	  GatewayImpl.getInstance().changeWirelessParams(gw);
	  //wait for the ack to get back from the gw for the wireless parameters change
	  gotGwAck = false;
	  synchronized(gwNetDisLock) {
	    try {
	      gwNetDisLock.wait(3000); //wait for 3 sec	    
	    }
	    catch(Exception e) {
	      e.printStackTrace();
	    }
	  }
	  //update the gateway operational mode in gateway info
    gwInfo = ServerMain.getInstance().getGatewayInfo(gw.getId());
    if(gwInfo != null) {
    	gwInfo.setOperationalMode(GatewayInfo.GW_NORMAL_MODE);
    }
      installState = INSTALL_STATE_READY;
	  if(!gotGwAck) {
	    logger.error(gw.getId() + ": No ack for change wireless after commissioning timed out");
	    commStatus = COMM_ERROR_INACTIVE_TIMED_OUT_GW_CH_CHANGE_CUSTOM;        
	  } else {
	  	//got the ack, gateway moved to original channel check for any errors
	    checkCommissioningErrors(gw.getId());
	  }
	  return;
	}
	
	/*Iterator<Fixture> iter = fixtureList.iterator();
	Fixture fixture = null;	
	while(iter.hasNext()) {
	  fixture = iter.next();
	  //System.out.println("type -- " + type);
	  logger.debug("validation type -- " + type);
	  deviceImpl.sendValidationCmd(fixture.getId().intValue(), 0, type);
	}*/
	
	//start the timer to monitor the client validation activity. if validation is not
	//complete and GUI is still active, send validation cmd to fixtures again
//	Timer validationTimer = new Timer();
//	int validateTimeout = 60 * 60 * 1000; // 1 hr
//	validationTimer.schedule(new TimerTask() {
//	  public void run() {
//	    int hrMilliSec = 60 * 60 * 1000;
//	    if((System.currentTimeMillis() - lastValActivityTime) < hrMilliSec) {
//	      startValidation(floorId);
//	    }
//	  }
//	}, 0, validateTimeout);      
	
      } //end of method run
      
    }.start();
    return COMM_STATUS_STARTED;
    
  } //end of method startValidation
    
  public void startIdentification(int fixtureId) {
    
    //System.out.println("startIdentification called");
    if(logger.isDebugEnabled()) {
      logger.debug("startIdentification called");
    }
    lastValActivityTime = System.currentTimeMillis();
    //after this call, person can identify the fixture. icon can be dropped to the
    //appropriate location
    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
    deviceImpl.sendValidationCmd(fixtureId, 1);
        
  } //end of method startIdentification
    
  //this is a map of fixtures with current motion  
  HashMap<Long, String>currValMap = new HashMap<Long, String>();
  
  public synchronized void motionEvent(Fixture fixture, byte[] pkt) {
    
    if(logger.isDebugEnabled()) {
      logger.debug(fixture.getId() + ": occupancy event packet - " + ServerUtil.getLogPacket(pkt));
    }
    int bytePos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
    if(pkt[0] == ServerConstants.FRAME_START_MARKER) { //old packet
      bytePos = 3;
    }
    byte motionByte = pkt[bytePos];   
    String name = fixture.getFixtureName();
    //String snapAddr = fixture.getSnapAddress();
    //System.out.println("motion observed on " + name); 
    if(logger.isInfoEnabled()) {
      logger.info("motion observed on " + name);
    }
    if(motionByte == 1) { //motion sensed
      if(!currValMap.containsKey(fixture.getId())) {
          fixtureMgr.updateCommissionStatus(fixture.getId(), ServerConstants.COMMISSION_STATUS_MOTION);    
	currValMap.put(fixture.getId(), fixture.getFixtureName());	
      }
    } else { //motion not sensed
      if(currValMap.containsKey(fixture.getId())) {
	currValMap.remove(fixture.getId());	
      }
    }
    
  } //end of method motionEvent
  
  public List<String> getCurrentValPresenceList(int floorId) {
    
    //System.out.println("getCurrentValPresenceList called size " + currValMap.size());
    if(logger.isDebugEnabled()) {
      logger.debug("getCurrentValPresenceList called size " + currValMap.size());
    }
    //lastValActivityTime = System.currentTimeMillis();
    return new ArrayList<String>(currValMap.values());
    
  } //end of method getCurrentValPresenceList
  
  public void discoveryData(String snapAddr, byte[] pkt, String gwIp) {
    
    if(!networkDiscovery) {
      if(logger.isInfoEnabled()) {
	logger.info(snapAddr + ": Discovery not in progress. Ignoring the discovery packet");
      }
      return;
    }
    if(logger.isDebugEnabled()) {
      logger.debug(snapAddr + ": discovery response -- " + ServerUtil.getLogPacket(pkt));
    }
    //ServerUtil.logPacket(snapAddr + ": discovery response -- ", pkt, logger);
    Fixture fixture = fixtureMgr.getFixtureBySnapAddr(snapAddr);
    if(fixture != null && fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
      //fixture is already commissioned. so, ignore this
      fixture = null;
      return;
    }
    if(fixture == null) {
      //fixture is not discovered
      fixture = fixtureMgr.getDeletedFixtureBySnapAddr(snapAddr);      
    }
    if(fixture == null) {
      //fixture is not in the database
      fixture = new Fixture();
    } else {
      if(logger.isInfoEnabled()) {
	logger.info(snapAddr + ": fixture is in deleted or discovered state");
      }
    }
 
    Ballast ballast = fixtureMgr.getBallastById((long)9);
    fixture.setBallast(ballast);
    Bulb bulb = fixtureMgr.getBulbById((long)1);
    fixture.setNoOfFixtures(1);
    fixture.setBulb(bulb);
    fixture.setLastOccupancySeen(0);
    fixture.setNoOfBulbs(ballast.getLampNum());
    fixture.setBulbWattage(ballast.getWattage());
    fixture.setWattage(ballast.getWattage() * ballast.getLampNum());
    fixture.setBulbManufacturer(bulb.getManufacturer());    
    fixture.setBallastManufacturer(ballast.getBallastManufacturer());
    fixture.setBaselinePower(new BigDecimal(0));
    fixture.setDimmerControl(0);
    fixture.setVoltage(DEFAULT_FIXTURE_VOLTAGE);
    fixture.setXaxis(0);
    fixture.setYaxis(0);
    fixture.setUpgradeStatus("");
    
    // ServerConstants.DEFAULT_PROFILE is the default
    Long groupDefaultId = 1L;
    Groups oGroups = groupMgr.getGroupByName(ServerConstants.DEFAULT_PROFILE);
    if (oGroups != null) {
      groupDefaultId = oGroups.getId();
    }
    Long phID = profileMgr.getProfileHandlerIDByGroupName(ServerConstants.DEFAULT_PROFILE);
    ProfileHandler fixtureProfile = profileMgr.getProfileHandlerById(phID);
    if (fixtureProfile == null) {
      phID = 1L;
      fixtureProfile = profileMgr.getProfileHandlerById(new Long(1));
    }
      
    ProfileHandler fixtureProfileCopy = fixtureProfile.copy(); 
    // We want a new profile copied from default open office for our new fixture.
    profileMgr.saveProfileHandler(fixtureProfileCopy);
    fixture.setProfileHandler(fixtureProfileCopy);
    fixture.setProfileChecksum(fixtureProfileCopy.getProfileChecksum());
    fixture.setGlobalProfileChecksum(fixtureProfileCopy.getGlobalProfileChecksum());
    fixture.setCurrentProfile(ServerConstants.DEFAULT_PROFILE);
    fixture.setOriginalProfileFrom(ServerConstants.DEFAULT_PROFILE);   
    
    fixture.setBulbLife((double)100);
    fixture.setActive(true);
    fixture.setLastConnectivityAt(new Date());
    fixture.setLastStatsRcvdTime(new Date());
    fixture.setGroupId(groupDefaultId);
     
    fixture.setMacAddress(snapAddr);
    fixture.setSnapAddress(snapAddr);
    fixture.setCommType(ServerConstants.COMM_TYPE_ZIGBEE);
    
    String location = "";
    Gateway gw = gwMgr.getGatewayByIp(gwIp);
    try {
      Floor floor = floorMgr.getFloorById(gw.getFloor().getId());
      fixture.setFloor(floor);      
      location = floor.getName();
      long buildingId = floor.getBuilding().getId();
      Building building = buildingMgr.getBuildingById(buildingId);
      fixture.setBuildingId(buildingId);
      location = building.getName() + "->" + location;
      long campusId = building.getCampus().getId();
      Campus campus = campusMgr.loadCampusById(campusId);
      fixture.setCampusId(campusId);
      location = campus.getName() + "->" + location;
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }    
    fixture.setLocation(location);
    String fixtureName = "Sensor" + ServerUtil.generateName(snapAddr);
    fixture.setSensorId(fixtureName);
    fixture.setFixtureName(fixtureName);
    
    fixture.setArea(null);
    fixture.setSubArea(null);
     
    int i = ServerConstants.RES_CMD_PKT_MSG_START_POS;
    //12th, 13th bytes are net id
    byte tempShortArr[] = { pkt[i++], pkt[i++] };
    
    //14th  channel
    fixture.setChannel((int)pkt[i++]);
        
    //15th, 16th, 17th, 18th zigbee version
    String zigbeeVer = "" + pkt[i++];
    zigbeeVer += "." + pkt[i++];
    byte [] zigbeeBuildNo = { pkt[i++], pkt[i++] };
    zigbeeVer += "." + ServerUtil.byteArrayToShort(zigbeeBuildNo);
        
    //19th, 20th app1 version
    String app1Ver = "" + pkt[i++] + "." + pkt[i++];    
    String modelNo = "";
    if(pkt.length > 21) {
      //21th, 22th app2 version
      String version = "" + pkt[i++];    
      version += "." + pkt[i++];
    
      //23th, 24st boot loader version
      String bootLoaderVer = "" + pkt[i++] + "." + pkt[i++];
      fixture.setBootLoaderVersion(bootLoaderVer);
      //25nd, 26rd, 27th, 28th  app2 build no
      byte[] app2ReleaseNo = { pkt[i++], pkt[i++], pkt[i++], pkt[i++] };
      version += "." + ServerUtil.byteArrayToInt(app2ReleaseNo);      
    
      //29th, 30th, 31th, 32th app1 build no
      int app1ReleaseNo = ServerUtil.extractIntFromByteArray(pkt, 29);
      app1Ver += "." + app1ReleaseNo;
      i += 4;
      //33th, 34th cu version no
      byte[] cuVerArr = { pkt[i++], pkt[i++] };
      fixture.setCuVersion("" + (short)ServerUtil.byteArrayToShort(cuVerArr));
      if(pkt.length > 36) {
      	//35th, 36th svn tag no. of app2
      	byte[] app2BuildNo = { pkt[i++], pkt[i++] };
      	version += " b" + ServerUtil.byteArrayToShort(app2BuildNo);
      }
      fixture.setVersion(version);
      if(pkt.length > 38) {
      	//37th is current app
      	byte appId = pkt[i++];
      	if(logger.isDebugEnabled()) {
      	  logger.debug(snapAddr + ": Current app during dicovery -- " + appId);
      	}
      	fixture.setCurrApp((short)appId);
      }
      if(pkt.length > 39) {
      	//38, 39 app1 svn tag no
      	byte[] app1BuildNo = { pkt[i++], pkt[i++] };
      	app1Ver += " b" + ServerUtil.byteArrayToShort(app1BuildNo);
      	
      	//40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 model no.
      	byte[] modelByteArr = new byte[12];      	
      	System.arraycopy(pkt, i, modelByteArr, 0, modelByteArr.length);
      	int modelLen = 0;
      	for(int m = 0; m < 12; m++) {
      		if(modelByteArr[m] == 0x0) {
      			modelLen = m;
      			break;
      		}
      	}
      	i += 12;
      	if(modelLen > 0) {
      		modelNo = new String(modelByteArr, 0, modelLen);
      	}
      	fixture.setModelNo(modelNo);
      	if(logger.isDebugEnabled()) {
      	  logger.debug("model no -- " + modelNo);
      	}
      }
      fixture.setFirmwareVersion(app1Ver);
    } else {
      fixture.setVersion(zigbeeVer);
    }    
    fixture.setGateway(gw);
    fixture.setSecGwId(gw.getId());
    fixture.setState(ServerConstants.FIXTURE_STATE_DISCOVER_STR);
    fixture.setIsHopper(0);
    fixture.setVersionSynced(0);
    fixture.setCommissionStatus(ServerConstants.COMMISSION_STATUS_UNKNOWN);
           
    synchronized(fixtureMgr) {
    	try {
      fixtureMgr.save(fixture);
      ServerMain.getInstance().invalidateDeviceCache(fixture.getId());
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
  } //end of method discoveryData

  	/**
  	 * Discover PLC devices via gateway
  	 * @param gatewayAddr - Gateway ip address
  	 * @param plcnodeAddr - PLC SU ip address
  	 * @param plcnodeMac - PLC SU Mac address (unique and constant)
  	 * @param version - PLC SU version
  	 */
	public void discoveryPLCNodes(String gatewayAddr, String plcnodeAddr, String plcnodeMacAddr,
			String version) {
		// TODO: Need to check gatewayAddr in gateway table and if not present add this to gateway entry. As for now go ahead with
		// just the SU discovery.
		
		Fixture fixture = fixtureMgr.getFixtureByMacAddr(plcnodeMacAddr);
		if (fixture != null) {
			//System.out.println("Fixture already comissioned, updating...");
		  if(logger.isInfoEnabled()) {
			logger.info("Fixture already comissioned, updating...");
		  }
			// it is already discovered, so update its IP and version.
			fixtureMgr.updateFixturePlc(fixture.getId(), plcnodeAddr, version);
			return;
		}
		
		InventoryDevice device = inventoryService
				.getInventoryDeviceByMacAddr(plcnodeMacAddr);
		if (device == null) {
			//System.out.println("Creating new device in inventory...");
		  if(logger.isDebugEnabled()) {
			logger.debug("Creating new device in inventory...");
		  }
			// create the device
			device = new InventoryDevice();
		} else {
		  //System.out.println("Updating existing inventory device which is still not commissioned.");
		  if(logger.isDebugEnabled()) {
		    logger.debug("Updating existing inventory device which is still not commissioned.");
		  }
		}
		device.setSnapAddr(" ");
		device.setMacAddr(plcnodeMacAddr);
		device.setIpAddress(plcnodeAddr);
		device.setCommType(ServerConstants.COMM_TYPE_PLC);
	    device.setDeviceType(ServerConstants.DEVICE_FIXTURE);
		device.setFloorId(m_iCurrentFloorId); // The current floor for which the discovery is on. 
		device.setDeviceName("Sensor" + ServerUtil.generateName(plcnodeMacAddr));
		device.setVersion(version);
		inventoryService.addInventoryDevice(device);
		
		deviceMap.put(plcnodeMacAddr, device);
	} // end of method discoveryPLCNodes

  int baseX = 0;
  int baseY = 0;
  
  ArrayList pinnedFixtures = new ArrayList();
  
  public void validationFinished(int fixtureId) {
    
    //System.out.println("validationFinished called for fixture " + fixtureId);
    if(logger.isInfoEnabled()) {
      logger.info(fixtureId + ": validationFinished called");
    }
    noOfValidatedFixtures++;
    lastValActivityTime = System.currentTimeMillis();
    DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
    deviceImpl.sendValidationCmd(fixtureId, 2);
    
    final Fixture fixture = fixtureMgr.getFixtureById(new Long(fixtureId));    
    synchronized(currValMap) {
      currValMap.remove(fixture.getId());
    }
    if(!autoPlace) {
      //state is changed after receiving the ack for apply wireless params
      //fixture.setState(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
      //fixtureMgr.updateState(fixture);
      return;
    } else {
      pinnedFixtures.add(fixture.getSnapAddress());
    }
    //System.out.println("after pinning the fixture");
    if(logger.isDebugEnabled()) {
      logger.debug("after pinning the fixture");
    }
    if(noOfValidatedFixtures == 1) {
      //first fixture validated.
      baseX = fixture.getXaxis().intValue();
      baseY = fixture.getYaxis().intValue();
    }
    //System.out.println("after base coordinates");
    if(logger.isDebugEnabled()) {
      logger.debug("after base coordinates");
    }
    if(noOfValidatedFixtures < 2) {
      return;      
    } 
    distanceDiscProgress = true;
    //run through the algorithm to get the coordinates 
    new Thread() {
      public void run() {	
	if(noOfValidatedFixtures == 2) {
	  findOutDistances(fixture.getFloor().getId().intValue());
	}
	//prepare the input file for algorithm	
	prepareFixtureDistanceFile();
	//call the algorithm
	callAutoMap();
	//read the output	
	readLocationFile(noOfValidatedFixtures);
	distanceDiscProgress = false;
      }
    }.start();    
    
  } //end of method validationFinished
  
  private static void readErrorStream(final Process process) {
    
    new Thread() {
      public void run() {
	BufferedReader br = null;
	try {
	  ServerUtil.sleep(1);
	  br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	  String line = "";
	  StringTokenizer st = null;
	  //System.out.println("started reading error of auto map");
	  if(logger.isDebugEnabled()) {
	    logger.debug("started reading error of auto map");
	  }
	  while(true) {
	    line = br.readLine();
	    if(line == null) {
	      break;
	    }
	    //System.out.println("error line - " + line);	
	    if(logger.isInfoEnabled()) {
	      logger.info("error line - " + line);
	    }
	  }
	  //System.out.println("done with reading the error of auto map");
	  if(logger.isDebugEnabled()) {
	    logger.debug("done with reading the error of auto map");
	  }
	}
	catch(Exception e) {
	  e.printStackTrace();
	}
	finally {
	  if(br != null) {
	    try {	
	      br.close();
	    }
	    catch(Exception e) {}
	  }
	}
      }
    }.start();
    
  } //end of method readErrorStream
  
  private void readLocationStream(final Process process) {
    
    new Thread() {
      public void run() {
	BufferedReader br = null;
	try {
	  ServerUtil.sleep(1);
	  br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	  String line = "";
	  StringTokenizer st = null;
	  //System.out.println("started reading output of auto map");
	  if(logger.isDebugEnabled()) {
	    logger.debug("started reading output of auto map");
	  }
	  while(true) {
	    line = br.readLine();
	    if(line == null) {
	      break;
	    }
	    //System.out.println("fixture location - " + line);
	    if(logger.isInfoEnabled()) {
	      logger.info("fixture location - " + line);
	    }
	    if(line.trim().length() == 0) {
	      continue;
	    }
	    if(line.startsWith("**")) {
	      //some error
	      continue;
	    }	    
	    st = new StringTokenizer(line, ",");
	    String snapAddr = null;
	    int x = 0;
	    int y = 0;
	    if(st.hasMoreTokens()) {
	      snapAddr = st.nextToken();
	    }
	    if(st.hasMoreTokens()) {
	      x = (int)(Float.parseFloat(st.nextToken().trim()));
	    }
	    if(st.hasMoreTokens()) {
	      y = (int)(Float.parseFloat(st.nextToken().trim()));
	    }
	    if(x != -1 && y != -1) {
	      Fixture fixture = fixtureMgr.getFixtureBySnapAddr(snapAddr);
	      if(fixture != null) {
		//fixtureMgr.updatePosition(fixture.getId(), baseX + x, baseY + y);
		fixtureMgr.updatePosition(fixture.getId(), x, y, 
		    ServerConstants.FIXTURE_STATE_PLACED_STR);
	      }
	    }
	    
	  }	  
	  //System.out.println("done with reading the output of auto map");
	  if(logger.isDebugEnabled()) {
	    logger.debug("done with reading the output of auto map");
	  }
	}
	catch(Exception e) {
	  e.printStackTrace();
	}
	finally {
	  if(br != null) {
	    try {	
	      br.close();
	    }
	    catch(Exception e) {}
	  }
	}
      }
    }.start();
    
  } //end of method readLocationStream
  
  private boolean networkDiscovery = false;
  private boolean discoveryFinished = true;
  public static Object gwNetDisLock = new Object();
  private int discoveryStatus = DISC_STATUS_SUCCESS;
   
  public int getDiscoveryStatus() {
    
    if(discoveryFinished) {
      return discoveryStatus;
    }
    return DISC_STATUS_INPROGRESS;
    
  } //end of method getDiscoveryStatus
  
  public int startNetworkDiscovery(final long floorId, final long gatewayId) {
    
    if(logger.isDebugEnabled()) {
      logger.debug(gatewayId + ": startNetworkDiscovery called");
    }
    readDiscoveryParams();
    if(installState.equals(INSTALL_STATE_COMMISSIONING)) {
      logger.error("Commissioning is in progress");
      return COMM_ERROR_INPROGRESS;
    } else if(installState.equals(INSTALL_STATE_DISCOVERY)) {
      logger.error("Discovery is in progress");
      return DISC_ERROR_INPROGRESS;
    }
    installState = INSTALL_STATE_DISCOVERY;
    new Thread() {
      
      public void run() {
		
	if(networkDiscovery) {
	  //it is already in progress
	  return;
	}
	discoveryFinished = false;
	networkDiscovery = true;	
	//move the gateway to the factory defaults
	Gateway gw = gwMgr.loadGateway(gatewayId);
	//generate discovery start event	
	eventMgr.addEvent(gw, "Discovery started", EventsAndFault.DISCOVER_EVENT_STR, 
	    EventsAndFault.INFO_SEV_STR);
	if(logger.isDebugEnabled()) {
	  logger.debug(gatewayId + ": 1. discovery started sending factory settings to gw");
	}
	GatewayImpl.getInstance().setWirelessFactoryDefaults(gw);
	//wait for the ack to get back from the gw for the wireless params change
	gotGwAck = false;
	synchronized(gwNetDisLock) {
	  try {
	    gwNetDisLock.wait(3000); //wait for 3 sec	    
	  }
	  catch(Exception e) {
	    e.printStackTrace();    
	  }
	}	
	if(!gotGwAck) {
	  //System.out.println(gw.getId() + ": No ack for factory before discovery");
	  logger.error(gw.getId() + ": No ack for factory before discovery");
	  networkDiscovery = false;
	  discoveryFinished = true;
	  discoveryStatus = DISC_ERROR_GW_CH_CHANGE_DEF;
	  // reset this state, otherwise we won't be able to initiate discovery after sorting out gateway issues.
	  installState = INSTALL_STATE_READY;
	  //generate discovery failure event
	  eventMgr.addEvent(gw, "Discovery failed: GW didn't move to default channel", 
	      EventsAndFault.DISCOVER_EVENT_STR, EventsAndFault.MAJOR_SEV_STR);
	  return;
	}
	//update the gateway operational mode in gateway info
  GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gw.getId());
  if(gwInfo != null) {
  	gwInfo.setOperationalMode(GatewayInfo.GW_DISCOVERY_MODE);
  }
	//ServerUtil.sleep(1);
	m_iCurrentFloorId = floorId;
	long startTime = System.currentTimeMillis();
	while(networkDiscovery) {
	  int noOfFixt = 0;
	  int noOfInstSensors = 0;
	  long currentTime = System.currentTimeMillis();
	  if((currentTime - startTime) >= DISCOVERY_MAX_TIME) {
	    if(logger.isInfoEnabled()) {
	      logger.info(gw.getId() + ": Discovery timed out");
	    }
	    //generate discovery event
	    String desc = "Discovery timed out: " + noOfInstSensors + " fixtures installed," +
	    	noOfFixt + " fixtures discovered";
	    if(noOfFixt > 0) {
	      desc += noOfFixt + " fixtures discovered";
	    }
	    eventMgr.addEvent(gw, desc, 
		EventsAndFault.DISCOVER_EVENT_STR, EventsAndFault.MINOR_SEV_STR);
	    networkDiscovery = false;
	    discoveryStatus = DISC_ERROR_TIMED_OUT;
	    break;
	  }
	  if(logger.isDebugEnabled()) {
	    logger.debug(gatewayId + ": 2. sending discover request");
	  }
	  //we are sending discovery request 4 times
	  for(int k = 0; k < 4; k++) {
	    if(k != 0) {
	      ServerUtil.sleepMilli(50);
	    }
	    if (gatewayId == 0) {
	      ZigbeeDeviceImpl.getInstance().discover(floorId);
	    } else {	    
	      ZigbeeDeviceImpl.getInstance().discover(floorId, gatewayId);
	    }	    
	  }
	  for(int s = 0; s < DISCOVERY_RETRY_INTERVAL; s++) {
	    ServerUtil.sleep(1);
	    if(!networkDiscovery) {
	      break;
	    }
	  }
	  if(!networkDiscovery) {
	    break;
	  }
	  //addFixtures();
	  // 16 Feb 2011: Need to check the number of fixture based on the gateway id, need to stop
	  // the search if the number of expected fixtures are matching the number of discovered fixtures
	  // for this gateway.
	  List<Fixture> listOfFixtures = fixtureMgr.loadAllFixtureByGatewayId(gatewayId);
	  // Avoid a Null pointer exception here, since for a fresh install the fixtures will be 0.
	  noOfFixt = (listOfFixtures != null) ? listOfFixtures.size() : 0;
	  //System.out.println("no. of fixtures -- " + noOfFixt + ", associated with the gateway: " + gatewayId);
	  if(logger.isDebugEnabled()) {
	    logger.debug(gatewayId + ": 3. no. of fixtures associated -- " + noOfFixt);
	  }
	  try {
	    if (gw != null) {
	      noOfInstSensors = gw.getNoOfSensors();
	    }
	  }
	  catch(Exception  ex) {
	    ex.printStackTrace();
	  }
	  //System.out.println("no. of installed fixtures == " + noOfInstSensors + " for gateway: " + gatewayId);
	  if(logger.isDebugEnabled()) {
	    logger.debug(gatewayId + ": 4. no. of installed fixtures == " + noOfInstSensors);
	  }
	  if(noOfFixt >= noOfInstSensors) {
	    networkDiscovery = false;
	    discoveryStatus = DISC_STATUS_SUCCESS;
	    //all fixtures are discovered, generate discovery event
	    eventMgr.addEvent(gw, "Discovery successful: " + noOfInstSensors + 
		" fixtures installed, " + noOfFixt + " fixtures discovered", 
		EventsAndFault.DISCOVER_EVENT_STR, EventsAndFault.INFO_SEV_STR);
	  }
	}
	//put back the gateway into its configured parameters
	gw = gwMgr.loadGateway(gatewayId);	
	if(logger.isDebugEnabled()) {
	  logger.debug(gatewayId + ": 5. discovery finished, sending change channel to gw");
	}
	GatewayImpl.getInstance().changeWirelessParams(gw);
	//wait for the ack to get back from the gw for the wireless params change
	gotGwAck = false;
	synchronized(gwNetDisLock) {
	  try {
	    gwNetDisLock.wait(3000); //wait for 3 sec	    
	  }
	  catch(Exception e) {
	    e.printStackTrace();
	  }
	}
	//update the gateway operational mode in gateway info
  gwInfo = ServerMain.getInstance().getGatewayInfo(gw.getId());
  if(gwInfo != null) {
  	gwInfo.setOperationalMode(GatewayInfo.GW_NORMAL_MODE);
  }
	if(!gotGwAck) {
	  //System.out.println(gw.getId() + ": No ack for change wireless after discovery");
	  logger.error(gw.getId() + ": No ack for change wireless after discovery");
	  discoveryStatus = DISC_ERROR_GW_CH_CHANGE_CUSTOM;
	}
	discoveryFinished = true;
	installState = INSTALL_STATE_READY;
		
      } //end of method run
      
    }.start();
    return DISC_STATUS_STARTED;
    
  } //end of method startNetworkDiscovery

  public void cancelNetworkDiscovery() {
    
    networkDiscovery = false;
     
  } //end of method cancelNetworkDiscovery
  
  public boolean isDiscoveryInProgress() {
	  return !discoveryFinished;
  }

  private void callAutoMap() {
        
    String autoMapExe = ServerMain.getInstance().getTomcatLocation() + "map.exe";
    String distFile = "webapps\\ems\\fixtDist";
    String cmd[] = { "cmd", "/C", "\"" + autoMapExe + "\"", "-f", distFile };
    
    String sOSName = System.getProperty("os.name").toUpperCase();
    if (!sOSName.contains("WINDOWS")) {
      //System.out.println("OS: " + sOSName);
      if(logger.isDebugEnabled()) {
	logger.debug("OS: " + sOSName);
      }
      autoMapExe = ServerMain.getInstance().getTomcatLocation() + "map.bin";
      distFile = "webapps" + File.separator + "ems" + File.separator + "fixtDist";
      cmd = new String[] { "\"" + autoMapExe + "\"", "-f", distFile };
    }
    //System.out.println("auto map - " + autoMapExe + ", cmd: " + cmd.toString());
    if(logger.isDebugEnabled()) {
      logger.debug("auto map - " + autoMapExe + ", cmd: " + cmd.toString());
    }
        
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      String fileLocation = ServerMain.getInstance().getTomcatLocation() + "location2";
      if(!new File(fileLocation).exists()) {
	readLocationStream(process);
      }
      readErrorStream(process);
      process.waitFor();
      //System.out.println("auto map is done");
      if(logger.isInfoEnabled()) {
	logger.info("auto map is done");
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
    } finally {
    	cmd = null;
    }
        
  } //end of method callAutoMap
  
  private void prepareFixtureDistanceFile() {
    
    String fileLocation = ServerMain.getInstance().getTomcatLocation() + "fixtDist";    
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(fileLocation));
      List<Fixture> fixtureList = fixtureMgr.loadFixtureByFloorId((long)1);
      Iterator<Fixture> iter = fixtureList.iterator();
      Fixture fixture = null;      
      while(iter.hasNext()) {
	fixture = iter.next();
	if(fixture.getState().equals(ServerConstants.FIXTURE_STATE_VALIDATED_STR) || 
	    fixture.getState().equals(ServerConstants.FIXTURE_STATE_PLACED_STR)) {
	  //int fixtX = fixture.getXaxis() - baseX;
	  //int fixtY = fixture.getYaxis() - baseY;
	  int fixtX = fixture.getXaxis();
	  int fixtY = fixture.getYaxis();
	  bw.write(fixture.getSnapAddress() + " " + fixtX + " " + fixtY + "\n");
	} else {
	  bw.write(fixture.getSnapAddress() + " -1 -1\n");
	}
      }
      bw.write("\n");
      
      //write the distances now
      int noOfDistances = distanceList.size();
      for(int i = 0; i < noOfDistances; i++) {
	bw.write(distanceList.get(i) + "\n");
      }
      bw.flush();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      if(bw != null) {
	try {
	  bw.close();	  
	}
	catch(Exception e) {}
      }
    }
    
  } //end of method prepareFixtureDistanceFile
    
  public boolean isDistanceDiscoveryInProgress() {
    
    return distanceDiscProgress;
    
  } //end of method isDistanceDiscoveryInProgress
  
  public void autoPlaceFinished(long floorId) {

    //System.out.println("autoPlaceFinished on floor " + floorId);
    if(logger.isDebugEnabled()) {
      logger.debug(floorId + ": autoPlaceFinished on floor ");
    }
    autoPlace = false;

  } //end of method autoPlaceFinished
  
  public static void main(String args[]) {
    
//    Point firstPoint = new Point();
//    firstPoint.setLocation(455, 141);
//    System.out.println("distance -- " + firstPoint.distance(425, 197));
    
    String location = "C:\\Program Files\\apache\\apache-tomcat-6.0.26\\webapps\\ems\\map.exe";
    
    String cmd[] = { "cmd", "/C", location, "-f", "../webapps/ems/map"};
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      readErrorStream(process);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

   /**
     *  Once the gateway comes out of the default channel and moves in to the custom channel,
     *  only then will  fire fxstats on custom channel
     * @param gwId
     */
  private void checkCommissioningErrors(final long gwId) {
    
    new Thread("CommissiongErrorCheck") {
      public void run() {
	
	ServerUtil.sleep(2);
	//get the fixtures for which wireless apply ack didn't come
	List<Fixture> errFixtureList = fixtureMgr.getUnCommissionedFixtureList(gwId);	
	Iterator<Fixture> fixtureIter = errFixtureList.iterator();
	Fixture fixture = null;
	while(fixtureIter.hasNext()) {
	  fixture = fixtureIter.next();
	  if((fixture.getCommissionStatus().intValue() &
	      ServerConstants.COMMISSION_STATUS_WIRELESS) == ServerConstants.COMMISSION_STATUS_WIRELESS) {
	    if(logger.isInfoEnabled()) {
	      logger.info(fixture.getFixtureName() + ": check commission status");
	    }
	    DeviceServiceImpl.getInstance().getCurrentState(fixture.getId());
	  }
	}	
      } //end of run method
      
    }.start();
    
  } //end of method checkCommissioningErrors
  
  public int finishCommissioning(long gwId, List<Fixture> fixtureIdList) {
    
    if(logger.isDebugEnabled()) {
      logger.debug(gwId + ": finishCommissioning");
    }
    final Gateway gw = gwMgr.loadGateway(gwId);
    if(gw == null) {
      installState = INSTALL_STATE_READY;
      return COMM_STATUS_FAIL;
    }
    //generate commission finish event	
    String desc = "Commissioning ";      
    //at the end of commissioning always send auto command to all the fixtures
    //which are not yet commissioned.    
    try {
      if(fixtureIdList != null && fixtureIdList.size() > 0) {
	//there are un commissioned fixtures
	int noOfFixtures = fixtureIdList.size();
	desc += "aborted: " + noOfFixtures + " not commissioned";
	eventMgr.addEvent(gw, desc, EventsAndFault.COMMISSION_EVENT_STR, 
	    EventsAndFault.MAJOR_SEV_STR);
	int[] fixtureArr = new int[noOfFixtures];
	for(int i = 0; i < noOfFixtures; i++) {
	  fixtureArr[i] = ((Fixture)fixtureIdList.get(i)).getId().intValue();	  
	}
	DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
	deviceImpl.setAutoState(fixtureArr);
      } else {
	desc += "successful";
	eventMgr.addEvent(gw, desc, EventsAndFault.COMMISSION_EVENT_STR, 
	    EventsAndFault.INFO_SEV_STR);
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      logger.error("could not send auto :" + e.getMessage());
    }
    //put the gateway back to its original wireless settings   
    ServerUtil.sleep(5);
    GatewayImpl.getInstance().changeWirelessParams(gw);
    //wait for the ack to get back from the gw for the wireless parameters change
    gotGwAck = false;
    synchronized(gwNetDisLock) {
      try {
	gwNetDisLock.wait(3000); //wait for 3 sec	    
      }
      catch(Exception e) {
	e.printStackTrace();
      }
    }
    installState = INSTALL_STATE_READY;
    //update the gateway operational mode in gateway info
    GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gw.getId());
    if(gwInfo != null) {
    	gwInfo.setOperationalMode(GatewayInfo.GW_NORMAL_MODE);
    }
    if(!gotGwAck) {
      //System.out.println(gw.getId() + ": No ack for change wireless after discovery");
      logger.error(gw.getId() + ": No ack for change wireless after commissioning");
      commStatus = COMM_ERROR_GW_CH_CHANGE_CUSTOM;
    } else {
      commStatus = COMM_STATUS_SUCCESS;      
      checkCommissioningErrors(gw.getId());
    }
    return commStatus;
    
  } //end of method finishCommissioning
  
} //end of class DiscoverySO
