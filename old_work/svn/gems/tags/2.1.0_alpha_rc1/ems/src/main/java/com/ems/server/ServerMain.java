/**
 * 
 */
package com.ems.server;

import java.io.File;
import java.io.FileInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Company;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.Groups;
import com.ems.model.Profile;
import com.ems.model.ProfileConfiguration;
import com.ems.model.ProfileHandler;
import com.ems.model.SystemConfiguration;
import com.ems.model.WeekDay;
import com.ems.server.device.BacnetService;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayComm;
import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.device.GatewayImpl.GwHealthPollTask;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.EmsThreadPool;
import com.ems.server.util.ServerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
import com.ems.service.ProfileManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.AppContext;
import com.ems.utils.ArgumentUtils;

/**
 * @author EMS
 * 
 */
public class ServerMain {

    private static ServerMain instance = null;

    private static final Logger logger = Logger.getLogger("CommLog");
    private static Logger profileLogger = Logger.getLogger("ProfileLogger");
    private static Logger auditLogger = Logger.getLogger("AuditLogger");
    
    public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;

    private String tomcatLocation = null;
    private FixtureManager fixtureMgr = null;
    private GatewayManager gwMgr = null;

    // hash map to hold the devices in memory
    private HashMap<Long, DeviceInfo> deviceMap = new HashMap<Long, DeviceInfo>();

    // hash map to hold the gateways in memory
    private HashMap<Long, GatewayInfo> gwMap = new HashMap<Long, GatewayInfo>();

    // Thread pool for DB Updates on receiving acks
    private int noOfDBUpdateAckThreads = 15;
    private EmsThreadPool dbUpdateThPool = null;

    // no. of threads to be used for handling command responses from gw/su
    private int noOfCmdRespListenerThreads = 15;

    private int iSortPath = 0;

    private boolean bApplyECScalingFactor = true;
    private float iScaling_110_Factor = 0.0511f;
    private float iAdj_110_Factor = 6.9192f;
    private float iScaling_277_Factor = 1.4522f;
    private float iAdj_277_Factor = 12.754f;
    private float iScaling_240_Factor = 0.5f;
    private float iAdj_240_Factor = 0.0f;

    private boolean isRunning = false;
    private EmsShutdownHandler shutdownHandler = new EmsShutdownHandler();

    /**
   * 
   */
    private ServerMain() {

        // TODO Auto-generated constructor stub
        // start discovery and perf components
        // logger.debug("home dir -- " +System.getProperty("user.dir"));
        setRunning(true);

        try {

            BacnetService bacnetService = BacnetService.getInstance();
            // TODO: Rethink on backward compatibility support. To have one unique profile per group and fixture respectively.
//            profileLogger.debug("Checking whether profiles required upgrade...");
//            ProfileUpgradeTask oProfileUpgradeTask = new ProfileUpgradeTask();
//            oProfileUpgradeTask.start();
//            try {
//                oProfileUpgradeTask.join();
//            } catch (InterruptedException ie) {
//            } finally {
//                profileLogger.debug("Profiles upgrade done.");
//            }

            initializeDeviceMap();
            initializeGwMap();
            DiscoverySO.getInstance();
            PerfSO perfSo = PerfSO.getInstance();
            ImageUpgradeSO.getInstance();
            // CommandRetryThread retryThread = new CommandRetryThread();
            // retryThread.start();
            SSLSessionManager.getInstance();

            shutdownHandler.addShutdownObserver(bacnetService);
            shutdownHandler.addShutdownObserver(perfSo);
            // Called for testing simulation.
            // generateAuditLog();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of constructor

    /**
     * Generate Audit log - simulator -
     */
    private void generateAuditLog() {
        /*
         * EmsAuditService oEAS = null; if (oEAS == null) { oEAS = (EmsAuditService)
         * SpringContext.getBean("emsAuditService"); }
         * 
         * List<EmsAudit> oEASList = oEAS.loadAllFixtureAuditlogs(); if (oEASList == null) { String[] oAction = {
         * "Profile Change", "Relative Dimming", "Absolute Dimming", "Auto", "Baseline", "Schedule Profile",
         * "Advance Profile", "Fixture Configuration Change", "Gateway Configuration Change" }; String[] oStatus = {
         * "Success", "Fail", "In process", "Retrying" }; EmsAudit oEmsAudit = null; Random oRandom = new Random();
         * Calendar oNow = Calendar.getInstance(); for (int count = 0; count < 100; count++) { oEmsAudit = new
         * EmsAudit(); oRandom.setSeed(System.currentTimeMillis()); oEmsAudit.setTxnId(new Long(oRandom.nextInt(9999)));
         * oEmsAudit.setStartTime(oNow.getTime()); //oEmsAudit.setEndTime(oNow.getTime()); oEmsAudit.setDeviceId(new
         * Long(count)); oEmsAudit.setDeviceType(ServerConstants.DEVICE_FIXTURE);
         * oEmsAudit.setStatus(oStatus[oRandom.nextInt(oStatus.length)]);
         * oEmsAudit.setAction(oAction[oRandom.nextInt(oAction.length)]); oEAS.save(oEmsAudit); } } else {
         * Iterator<EmsAudit> oEASItr = oEASList.iterator(); while(oEASItr.hasNext()) {
         * auditLogger.debug(oEASItr.next().toString()); } }
         */
    }

    public HashMap getDeviceMap() {

        return deviceMap;

    } // end of method getDeviceMap

    public HashMap getGwMap() {

        return gwMap;

    } // end of method getGwMap

    private void initializeDeviceMap() {

        if (fixtureMgr == null) {
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        }
        List<Fixture> fixtureList = fixtureMgr.loadAllFixtures();
        if (fixtureList == null) {
            return;
        }
        DeviceInfo device = null;
        int noOfFixtures = fixtureList.size();
        Fixture fixture = null;
        for (int i = 0; i < noOfFixtures; i++) {
            fixture = fixtureList.get(i);
            device = new DeviceInfo(fixture);
            device.setDbId(fixture.getId());
            device.setLastStateRcvdTime(fixture.getLastStatsRcvdTime());
            addDevice(fixture.getId(), device);
        }

    } // end of method initializeDeviceMap

    public void updateGatewayInfo(long gwId) {

        Gateway gw = gwMgr.loadGateway(gwId);
        if (gw == null) {
            return;
        }
        GatewayInfo gwInfo = gwMap.get(gwId);
        if (gwInfo != null) {
            gwInfo.setGw(gw);
        }

    } // end of method updateGatewayInfo

    public boolean addGatewayInfo(Gateway gw) {

        // logger.debug("inside the addGatewayInfo");
        if (gw == null || gw.getId() == null) {
            // gateway is not added to database yet
            return false;
        }
        if (!gwMap.containsKey(gw.getId())) {
            GatewayInfo gwInfo = new GatewayInfo();
            gwInfo.setGw(gw);
            gwInfo.setLastConnectivityAt(gw.getLastConnectivityAt());
            gwInfo.setlastStatsRcvdAt(gw.getLastStatsRcvdTime());
            addGateway(gw.getId(), gwInfo);
        }
        return true;

    } // end of method addGatewayInfo

    private void initializeGwMap() {

        if (gwMgr == null) {
            gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        }
        List<Gateway> gwList = gwMgr.loadAllGateways();
        if (gwList == null) {
            return;
        }
        GatewayInfo gwInfo = null;
        int noOfGws = gwList.size();
        Gateway gw = null;
        for (int i = 0; i < noOfGws; i++) {
            gw = gwList.get(i);
            gwInfo = new GatewayInfo();
            gwInfo.setGw(gw);
            gwInfo.setLastConnectivityAt(gw.getLastConnectivityAt());
            gwInfo.setlastStatsRcvdAt(gw.getLastStatsRcvdTime());
            addGateway(gw.getId(), gwInfo);
        }

    } // end of method initializeGwMap

    public GatewayInfo getGatewayInfo(long gwId) {

        if (gwMap.containsKey(gwId)) {
            return gwMap.get(gwId);
        }
        return null;

    } // end of method getGatewayInfo

    public static ServerMain getInstance() {

        if (instance == null) {
            synchronized (ServerMain.class) {
                if (instance == null) {
                    instance = new ServerMain();
                }
            }
        }
        return instance;

    } // end of method getInstance
    
    //starting with 2.0.2 as it is the current version
    private String gemsVersion = "2.1";
    private String gemsBldNo = "0";

    public String getGemsVersion() {

    	return gemsVersion;
        
    } // end of method getGemsVersion

    public String getGemsBuildVersion() {

    	return gemsBldNo;
    	
    } //end of method getGemsBuildVersion
         
    private static boolean serverInitialized = false;
    
    public void setTomcatLocation(String location) {

        tomcatLocation = location;
        ZigbeeDeviceImpl.getInstance();
        // logger.debug("tomcat location -- " + tomcatLocation);
        String sImageUpgrade = AppContext.getProperty("upgrade.image.location");
        ImageUpgradeSO.getInstance().setFirmwareImageLocation(
                tomcatLocation + ".." + File.separator + ".." + File.separator + sImageUpgrade + File.separator);
        GwStatsSO.getInstance();
        // ZigbeeDeviceImpl.getInstance().initSnap(false);
        
        if(!serverInitialized) {
          //read gems version and build no.
          // EMSMANIFEST.MF is create from build system when ever a build is created.        	
          Properties p = new Properties();
          try {
            p.load(new FileInputStream(tomcatLocation + "/META-INF/MANIFEST.MF"));
          } catch (Exception ex) {
              ex.printStackTrace();
          }
          String verKeyStr = "Implementation-Version";            
          if (p.containsKey(verKeyStr)) {
              gemsVersion = p.getProperty(verKeyStr, "");
          }
                 
          String buildVersionStr = "Build-Version";          
          if (p.containsKey(buildVersionStr)) {
              gemsBldNo = p.getProperty(buildVersionStr, "");
          }
          
          //Set the task to read the database settings every 5 minutes
          Timer serverInitializationTimer = new Timer("Server Main Settings", true);
          ServerInitializationTask serverInitializationTask = new ServerInitializationTask();
          serverInitializationTimer.scheduleAtFixedRate(serverInitializationTask, 0, FIVE_MINUTE_INTERVAL);
          
          //read the gems build no.
          serverInitialized = true;
        }
    } // end of method setTomcatLocation
    
    private class ServerInitializationTask extends TimerTask{

		@Override
		public void run() {
			initializeSettings();			
		}
    	
    }
    
    public void initializeSettings(){
    	
    	try {
            // reading from the database
            SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext
                    .getBean("systemConfigurationManager");

            SystemConfiguration tempConfig = sysMgr.loadConfigByName("commandRetryDelay");
            logger.debug("from database command retry delay -- " + tempConfig.getValue());
            DeviceInfo.setCommandRetryDelay(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("commandNoOfRetries");
            logger.debug("from database command no. of retries -- " + tempConfig.getValue());
            DeviceInfo.setNoOfRetries(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("perf.pmStatsMode");
            logger.debug("from database pm stats mode -- " + tempConfig.getValue());
            PerfSO perfInstance = PerfSO.getInstance();
            perfInstance.setStatsMode(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("event.outageVolts");
            logger.debug("from database outage volts -- " + tempConfig.getValue());
            perfInstance.setOutageVolts(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("event.outageAmbLight");
            logger.debug("from database outage amb light -- " + tempConfig.getValue());
            perfInstance.setOutageAmbLight(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("perf.base_power_correction_percentage");
            logger.debug("from database base power correction percentage -- " + tempConfig.getValue());
            perfInstance.setBasePowerCorrectionPercentage(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("event.outage_detect_percentage");
            logger.debug("from database outage detection percentage -- " + tempConfig.getValue());
            perfInstance.setOutageDetectPercentage(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("event.fixture_outage_detect_watts");
            logger.debug("from database fixture outage detection watts -- " + tempConfig.getValue());
            perfInstance.setFixtureOutageDetectWatts(Integer.parseInt(tempConfig.getValue()));

            if (dbUpdateThPool == null) {
                tempConfig = sysMgr.loadConfigByName("cmd.ack_dbupdate_threads");
                logger.debug("from database command ack dbupdate no. of threads -- " + tempConfig.getValue());
                noOfDBUpdateAckThreads = Integer.parseInt(tempConfig.getValue());
                dbUpdateThPool = new EmsThreadPool(noOfDBUpdateAckThreads, "DBUpdateAckThread");
            }

            tempConfig = sysMgr.loadConfigByName("cmd.response_listener_threads");
            logger.debug("from database command response listener no. of threads -- " + tempConfig.getValue());
            noOfCmdRespListenerThreads = Integer.parseInt(tempConfig.getValue());

            DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();

            tempConfig = sysMgr.loadConfigByName("cmd.no_multicast_targets");
            logger.debug("from database command no. of multicast targets -- " + tempConfig.getValue());
            deviceImpl.setNoOfMulticastTargets(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("cmd.multicast_inter_pkt_delay");
            logger.debug("from database command multicast inter pkt delay -- " + tempConfig.getValue());
            deviceImpl.setMulticastInterPktDelay(Integer.parseInt(tempConfig.getValue()));
            
            tempConfig = sysMgr.loadConfigByName("cmd.multicast_inter_pkt_delay_2");
            if(tempConfig != null) {
            	logger.debug("from database command multicast inter pkt delay2 -- " + tempConfig.getValue());
            	deviceImpl.setMulticastInterPktDelay2(Integer.parseInt(tempConfig.getValue()));
            }

            tempConfig = sysMgr.loadConfigByName("cmd.unicast_inter_pkt_delay");
            logger.debug("from database command unicast inter pkt delay -- " + tempConfig.getValue());
            deviceImpl.setUnicastPktsDelay(Integer.parseInt(tempConfig.getValue()));

            tempConfig = sysMgr.loadConfigByName("fixture.sorting.path");
            logger.debug("from database fixture sorting path -- " + tempConfig.getValue());
            setSortPath(Integer.valueOf(tempConfig.getValue()).intValue());

            tempConfig = sysMgr.loadConfigByName("ec.apply.scaling.factor");
            logger.debug("from database ec apply scaling  factor -- " + tempConfig.getValue());
            setApplyECScalingFactor(Boolean.valueOf(tempConfig.getValue()).booleanValue());

            tempConfig = sysMgr.loadConfigByName("ec.scaling.for.277v");
            logger.debug("from database ec scaling factor 277v -- " + tempConfig.getValue());
            setScalingFactorFor277V(Float.valueOf(tempConfig.getValue()).floatValue());

            tempConfig = sysMgr.loadConfigByName("ec.adj.for.277v");
            logger.debug("from database ec adj factor 277v -- " + tempConfig.getValue());
            setAdjFactorFor277V(Float.valueOf(tempConfig.getValue()).floatValue());

            tempConfig = sysMgr.loadConfigByName("ec.scaling.for.240v");
            logger.debug("from database ec scaling factor 240v -- " + tempConfig.getValue());
            setScalingFactorFor240V(Float.valueOf(tempConfig.getValue()).floatValue());

            tempConfig = sysMgr.loadConfigByName("ec.adj.for.240v");
            logger.debug("from database ec adj factor 240v -- " + tempConfig.getValue());
            setAdjFactorFor240V(Float.valueOf(tempConfig.getValue()).floatValue());

            tempConfig = sysMgr.loadConfigByName("ec.scaling.for.110v");
            logger.debug("from database ec scaling factor 110v -- " + tempConfig.getValue());
            setScalingFactorFor110V(Float.valueOf(tempConfig.getValue()).floatValue());

            tempConfig = sysMgr.loadConfigByName("ec.adj.for.110v");
            logger.debug("from database ec adj factor 110v -- " + tempConfig.getValue());
            setAdjFactorFor110V(Float.valueOf(tempConfig.getValue()).floatValue());

            // reading discovery parameters also every time other parameters are read
            DiscoverySO.getInstance().readDiscoveryParams();
            
            // ssl enable flag
            tempConfig = sysMgr.loadConfigByName("ssl.enabled");
            if (tempConfig != null) {
            	logger.debug("from database ssl enabled -- " + tempConfig.getValue());
            	GatewayComm.getInstance().setSSLEnabled(
            			Boolean.valueOf(tempConfig.getValue()).booleanValue());
            }
            
            //default su hop count
            tempConfig = sysMgr.loadConfigByName("default_su_hop_count");
            if(tempConfig != null) {
            	logger.debug("from database default su hop count -- " + tempConfig.getValue());
            	DeviceServiceImpl.getInstance().setDefaultSUHopCount(
            			Byte.valueOf(tempConfig.getValue()).byteValue());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }    	
    }

    public int getNoOfCmdRespListenerThreads() {

        return noOfCmdRespListenerThreads;

    } // end of method getNoOfCmdRespListenerThreads

    public Properties readGemsProperties() {

        String sConfig = AppContext.getProperty("gems.config.location");
        String sConfigFile = tomcatLocation + ".." + File.separator + ".." + File.separator + sConfig + File.separator
                + "ems.properties";
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(sConfigFile));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return prop;

    } // end of method readGemsProperties

    public String getTomcatLocation() {

        return tomcatLocation;

    } // end of method getTomcatLocation

    public synchronized void addDevice(Long id, DeviceInfo device) {

        deviceMap.put(id, device);

    } // end of method addDevice

    public synchronized void addGateway(Long id, GatewayInfo gw) {

        gwMap.put(id, gw);

    } // end of method addGateway

    public void getCurrentState(Fixture fixture, byte[] packet, long gwId) {

        if (fixtureMgr == null) {
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        }

        String fixtureName = fixture.getFixtureName();
        logger.debug(fixtureName + ": CurrentState " + ServerUtil.getLogPacket(packet));

        int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (packet[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            pktIndex = 3;
        }
        byte currState = packet[pktIndex++]; // 4th
        byte currVolt = packet[pktIndex++]; // 5th
        byte currTemp = packet[pktIndex++]; // 6th
        byte[] tempShortByteArr = new byte[2];
        // amb light bytes 7,8
        System.arraycopy(packet, pktIndex++, tempShortByteArr, 0, tempShortByteArr.length);
        pktIndex++;
        short ambLight = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
        // logger.debug("current amb light -- " + ambLight);

        if (DiscoverySO.getInstance().isDistanceDiscProgress()) {
            // send the light level to Discovery class
            if (fixture.getCommType() == ServerConstants.COMM_TYPE_PLC) {
                DiscoverySO.getInstance().fixtureDistance(fixture.getIpAddress(), ambLight);
            } else {
                DiscoverySO.getInstance().fixtureDistance(fixture.getSnapAddress(), ambLight);
            }
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("Fixture[");
        sb.append(fixtureName);
        sb.append("]: State=");
        sb.append(currState); // state
        sb.append(",Voltage=");
        sb.append(currVolt);
        sb.append(",Temp=");
        sb.append(currTemp);

        sb.append(",AmbLight=");
        sb.append(ambLight);

        // motion secs ago bytes 9,10
        System.arraycopy(packet, pktIndex++, tempShortByteArr, 0, tempShortByteArr.length);
        pktIndex++;
        int motionSecAgo = ServerUtil.byteArrayToShort(tempShortByteArr);
        // logger.debug("motion sec ago  -- " + motionSecAgo);
        sb.append(",MotionSecAgo=");
        sb.append(motionSecAgo);

        // current time bytes 11,12,13, 14
        byte[] tempIntByteArr = new byte[4];
        System.arraycopy(packet, pktIndex++, tempIntByteArr, 0, tempIntByteArr.length);
        pktIndex += 3;
        Calendar cal = Calendar.getInstance();
        int gmtOffset = (int) (cal.getTimeZone().getOffset(System.currentTimeMillis()));
        long currentTime = (ServerUtil.intByteArrayToLong(tempIntByteArr) * 60 * 1000) - gmtOffset;
        // logger.debug("current time in mins -- " + currentTime);
        sb.append(",currentTime=");
        sb.append(currentTime + "(" + new Date(currentTime) + ")");

        // up time bytes 15,16,17,18
        System.arraycopy(packet, pktIndex++, tempIntByteArr, 0, tempIntByteArr.length);
        pktIndex += 3;
        long upTime = ServerUtil.intByteArrayToLong(tempIntByteArr);
        // logger.debug("uptime in secs -- " + upTime);
        sb.append(",uptime=");
        sb.append(upTime);

        if (packet.length > pktIndex) {
            // byte 19 global profile checksum
            short gPrChecksum = packet[pktIndex++];
            sb.append(",global Profile checksum=");
            sb.append(gPrChecksum);
            fixture.setGlobalProfileChecksum(gPrChecksum);
        }
        double avgPower = 0;
        if (packet.length > pktIndex) {
            // byte 20 profile checksum
            short sPrChecksum = packet[pktIndex++];
            sb.append(",scheduled Profile checksum=");
            sb.append(sPrChecksum);
            fixture.setProfileChecksum(sPrChecksum);

            // bytes 21, 22 to off timer
            pktIndex += 2;
            // bytes 23,24 energy ticks
            byte[] tempShortArr = new byte[2];
            System.arraycopy(packet, pktIndex, tempShortArr, 0, tempShortArr.length);
            pktIndex += 2;
            int pulses = ServerUtil.byteArrayToShort(tempShortArr);
            sb.append(", energy ticks=");
            sb.append(pulses);
            // bytes 25, 26, 27, 28 time in milli sec
            byte[] tempIntArr = new byte[4];
            System.arraycopy(packet, pktIndex, tempIntArr, 0, tempIntArr.length);
            pktIndex += 4;
            long pulsesDuration = ServerUtil.intByteArrayToLong(tempIntArr) / 1000;
            sb.append(", energy ticks duration=");
            sb.append(pulsesDuration);
            // bytes 29, 30 calib value
            System.arraycopy(packet, pktIndex, tempShortArr, 0, tempShortArr.length);
            pktIndex += 2;
            int calibValue = ServerUtil.byteArrayToShort(tempShortArr);
            sb.append(", energy calib value=");
            sb.append(calibValue);
            double energyConsumed = (double)pulses / 1000;
            if(fixture.getCuVersion().equals("32")) {           	            	        
              if(pulsesDuration > 0) {
        	avgPower = energyConsumed * 3600 / pulsesDuration;
        	fixture.setWattage((int) avgPower); 
              }
              sb.append(",energy consumed=");
              sb.append(energyConsumed);
            } else {
              if (pulsesDuration > 0) {
        	avgPower = PerfSO.getAvgPower(calibValue, pulses, (int) pulsesDuration, fixture);
        	energyConsumed = avgPower * pulsesDuration / 3600; // watts hr
        	fixture.setWattage((int) avgPower); 
        	sb.append(",energy consumed=");
                sb.append(energyConsumed);
              }
            }
            // end frame marker 5e should not be counted
            if ((packet.length - 1) > pktIndex) {
                // byte 31 is hopper
                int isHopper = packet[pktIndex++];
                sb.append(",hopper=");
                sb.append(isHopper);
                fixture.setIsHopper(isHopper);
            }
            // end frame marker 5e should not be counted
            if ((packet.length - 1) > pktIndex) {
                int groupId = packet[pktIndex++];
                sb.append(", profile group id=");
                sb.append(groupId);
            }
        }

        sb.append(", local advanced profile checksum=");
        sb.append(DeviceServiceImpl.getInstance().calculateGlobalProfileChecksum(fixture));
        sb.append(",local Profile checksum=");
        sb.append(DeviceServiceImpl.getInstance().calculateScheduledProfileChecksum(fixture));
        logger.info(sb.toString());

        // update the fixture
        try {
            fixture.setLightLevel(ambLight / 14);
            if (motionSecAgo != -1) {
                fixture.setLastOccupancySeen(new Integer(motionSecAgo));
            }
            fixture.setCurrentState(ServerUtil.getCurrentState(currState));
            fixture.setDimmerControl((int) currVolt);
            short fxTemp = 0;
            if(ServerUtil.compareVersion(fixture.getVersion(), "2.0") < 0) {
            	fxTemp = (short) ((currTemp * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU1());
            } else {
            	fxTemp = (short) (((currTemp) * 9 / 5) + 32);
            }
            fixture.setAvgTemperature(fxTemp);
            // long ballastId = fixture.getBallast().getId();
            // Ballast ballast = fixtureMgr.getBallastById(ballastId);
            // int watts = (int)PerfSO.getAvgPowerFromVolts(currVolt, ballast.getLampNum(),
            // ballast) * fixture.getNoOfFixtures();
            fixture.setSecGwId(gwId);
            fixtureMgr.updateRealtimeStats(fixture);

            // update the commission status, location if fixture is in discovered state
            if (fixture.getState().equals(ServerConstants.FIXTURE_STATE_DISCOVER_STR)) {
                // update the fixture state to commissioned
                fixture.setState(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
                fixtureMgr.updateState(fixture);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method getCurrentState

    public class MulticastCommand {

        Gateway gw;
        byte[] packet;

    }

    public void addGatewayCommand(Gateway gw, byte[] packet) {

        try {
            GatewayInfo gwInfo = null;
            if (!gwMap.containsKey(gw.getId())) {
                // logger.debug("adding gw to map");
                gwInfo = new GatewayInfo();
                gwInfo.setGw(gw);
                addGateway(gw.getId(), gwInfo);
            } else {
                gwInfo = gwMap.get(gw.getId());
            }
            gwInfo.addCmd(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method addGatewayCommand

    // add gateway command for retries with retries
    public void addGatewayCommand(Gateway gw, byte[] packet, ArrayList<Fixture> fixtureList, int noOfRetries) {

        try {
            GatewayInfo gwInfo = null;
            if (!gwMap.containsKey(gw.getId())) {
                // logger.debug("adding gw to map");
                gwInfo = new GatewayInfo();
                gwInfo.setGw(gw);
                addGateway(gw.getId(), gwInfo);
            } else {
                gwInfo = gwMap.get(gw.getId());
            }
            gwInfo.addCmd(packet, fixtureList, noOfRetries);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method addGatewayCommand

    // public void addDeviceCommand(Fixture fixture, byte[] packet) {
    //
    // try {
    // DeviceInfo device = null;
    // if(!deviceMap.containsKey(fixture.getId())) {
    // //logger.debug("adding device to map");
    // device = new DeviceInfo(fixture);
    // device.setDbId(fixture.getId());
    // device.setLastStateRcvdTime(fixture.getLastStatsRcvdTime());
    // addDevice(fixture.getId(), device);
    // } else {
    // device = deviceMap.get(fixture.getId());
    // }
    // device.addCmd(packet);
    // }
    // catch(Exception e) {
    // e.printStackTrace();
    // }
    //
    // } //end of method addDeviceCommand

    public void ackGatewayMessage(byte[] ackPkt, long gwId) {

        if (gwMap.containsKey(gwId)) {
            GatewayInfo gwInfo = gwMap.get(gwId);
            gwInfo.ackCmd(ackPkt);
        }

    } // end of method ackGatewayMessage

    public class DBUpdateOnAckWork implements Runnable {

        private Fixture fixture;
        private int msg;
        private long gwId;
        private Long cmdSeq;
        private int attempts;

        public DBUpdateOnAckWork(Fixture fixture, int msg, long gwId, Long cmdNo, int att) {

            this.fixture = fixture;
            this.msg = msg;
            this.gwId = gwId;
            this.cmdSeq = cmdNo;
            this.attempts = att;

        } // end of constructor

        public void run() {

            try {
                DeviceServiceImpl.getInstance().updateAuditRecord(cmdSeq.longValue(), fixture.getId(), attempts, 0); // success
                switch (msg) {
                case ServerConstants.SET_LIGHT_LEVEL_MSG_TYPE:
                	fixtureMgr.getCurrentDetails(fixture.getId());
                	break;              
                case ServerConstants.SET_VALIDATION_MSG_TYPE:
                	fixtureMgr.updateCommissionStatus(fixture.getId(), 
                			ServerConstants.COMMISSION_STATUS_COMMUNICATION);
                	break;                
                default:                    
                    break;
                }
            } catch (Exception ex) {
                logger.error(fixture.getId() + ": error updating the db on ack");
            }

        }

    } // end of class DBUpdateWork

    // public void ackDeviceMessage(Fixture fixture, byte[] ackPkt, long gwId) {
    //
    // if(gwMap.containsKey(gwId)) {
    // GatewayInfo gw = gwMap.get(gwId);
    // gw.ackCmd(ackPkt, fixture);
    // }
    // //ServerUtil.printPacket(ackPkt);
    // if(deviceMap.containsKey(fixture.getId())) {
    // DeviceInfo device = deviceMap.get(fixture.getId());
    // device.setLastGwId(gwId);
    // device.ackCmd(ackPkt);
    // }
    // int msg = (ackPkt[ServerConstants.RES_CMD_PKT_MSG_START_POS] & 0xFF);
    // // DBUpdateOnAckWork dbWork = new DBUpdateOnAckWork(fixture, msg, gwId);
    // // dbUpdateThPool.addWork(dbWork);
    //
    // } //end of method ackDeviceMessage

    public void addDbUpdateWork(DBUpdateOnAckWork dbWork) {

        dbUpdateThPool.addWork(dbWork);

    } // end of method addDbUpdateWork

    public boolean isGwTasksPending(Long gwId) {

        GatewayInfo gwInfo = gwMap.get(gwId);
        if (gwInfo != null) {
            if (gwInfo.getNoOfPendingCmds() > 0) {
                return true;
            }
        }
        return false;

    } // end of method isGwTasksPending

    // public boolean isTasksPending(Long fixId) {
    //
    // DeviceInfo device = deviceMap.get(fixId);
    // if(device != null) {
    // if(device.getNoOfPendingCmds() > 0) {
    // return true;
    // }
    // }
    // return false;
    //
    // } //end of method isTasksPending

    public boolean isPFPktMarked(Long fixId) {

        DeviceInfo device = deviceMap.get(fixId);
        if (device != null) {
            if (device.isPFPktMarked(fixId)) {
                return true;
            }
        }
        return false;
    } // end of method isPFPktPending

    public DeviceInfo getDevice(Long id) {

        if (deviceMap.containsKey(id)) {
            return deviceMap.get(id);
        }
        return null;

    } // end of method getDevice

    public DeviceInfo getDevice(Fixture fixture) {

        DeviceInfo device = null;
        try {
            if (!deviceMap.containsKey(fixture.getId())) {
                // logger.debug("adding device to map");
                device = new DeviceInfo(fixture);
                device.setDbId(fixture.getId());
                device.setLastStateRcvdTime(fixture.getLastStatsRcvdTime());
                device.setUptime(1);
                addDevice(fixture.getId(), device);
            } else {
                device = deviceMap.get(fixture.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return device;

    } // end of method getDevice

    // public void clearAllPendingRequests() {
    //
    // Iterator<Long> deviceIter = deviceMap.keySet().iterator();
    // Long deviceId = null;
    // DeviceInfo device = null;
    // while(deviceIter.hasNext()) {
    // deviceId = deviceIter.next();
    // device = deviceMap.get(deviceId);
    // device.clearAllPendingJobs();
    // }
    //
    // } //end of method clearAllPendingRequests

    // This thread loops through all the pending commands and retries if required and ack has
    // not been received in time.
    // public class CommandRetryThread extends Thread {
    //
    // public CommandRetryThread() {
    //
    // setName("Command Retry Thread");
    //
    // } //end of constructor
    //
    // public void run() {
    //
    // while(ServerMain.getInstance().isRunning()) {
    // //first try multicast retries on gateways
    // // try {
    // // Iterator<Long> gwIter = gwMap.keySet().iterator();
    // // Long gwId = null;
    // // GatewayInfo gw = null;
    // // while(gwIter.hasNext()) {
    // // gwId = gwIter.next();
    // // gw = gwMap.get(gwId);
    // // if(gw.getNoOfPendingCmds() == 0) {
    // // continue;
    // // }
    // // gw.retryCommands();
    // // }
    // // }
    // // catch(Exception e) {
    // // e.printStackTrace();
    // // }
    // //next try unicast retries on nodes
    // try {
    // Iterator<Long> deviceIter = deviceMap.keySet().iterator();
    // Long deviceId = null;
    // DeviceInfo device = null;
    // while(deviceIter.hasNext()) {
    // deviceId = deviceIter.next();
    // device = deviceMap.get(deviceId);
    // if(device.getNoOfPendingCmds() == 0) {
    // continue;
    // }
    // device.retryCommands();
    // }
    // }
    // catch(Exception e) {
    // e.printStackTrace();
    // }
    // ServerUtil.sleepMilli(DeviceServiceImpl.UNICAST_PKTS_DELAY * 3 / 2);
    // }
    //
    // } //end of method run
    //
    // } //end of class CommandRetryThread

    /**
     * Fetch profile from SU
     * 
     * @param fixtureId
     * @param packet
     */
    public void getProfileFromSU(long fixtureId, byte[] packet) {

        if (fixtureMgr == null) {
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        }
        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            logger.error(fixtureId + ": Fixture is unknown or it is not commissioned");
            return;
        }
        // String fixtureName = fixture.getFixtureName();
        // ServerUtil.printPacket(fixtureName + "- CurrentState ", packet);
        DeviceInfo dInfo = getDevice(fixture);
        if (dInfo != null) {
            dInfo.ackPfH(fixtureId, packet);
        }
    } // end of method getProfileFromSU

    /**
     * checks if there are any groups that are in 1.2 mode, if present, then creates seperate profile handler for them.
     * 
     * @author yogesh
     * 
     */
    public class ProfileUpgradeTask extends Thread {

        public ProfileUpgradeTask() {
            setName("Profile Upgrade");
        }

        public void run() {
            CompanyManager companyMgr = (CompanyManager) SpringContext.getBean("companyManager");

            List<Company> oCompanyList = companyMgr.getAllCompanies();
            if (oCompanyList == null || oCompanyList.size() < 1)
                return;
            Company company = oCompanyList.get(0);
            if (company == null || company.getCompletionStatus() != 3)
                return;

            SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext
                    .getBean("systemConfigurationManager");
            GroupManager groupMgr = (GroupManager) SpringContext.getBean("groupManager");
            ProfileManager profileMgr = (ProfileManager) SpringContext.getBean("profileManager");
            MetaDataManager metaDataMgr = (MetaDataManager) SpringContext.getBean("metaDataManager");

            if (fixtureMgr == null) {
                fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
            }

            HashMap<String, String> scMap = null;
            try {
                scMap = sysMgr.loadAllConfigMap();
            } catch (Exception e) {
                profileLogger.warn("Please run upgradeSQL script to update the existing database");
            }

            if (scMap == null)
                return;

            String groupList = getConfigurationValue(scMap, "default.", "metadata.areas");
            if (!ArgumentUtils.isNullOrEmpty(groupList)) {
                String[] groups = groupList.split(",");
                ProfileHandler groupProfileHandler = null;
                String groupName = "";
                String groupkey = "";
                // Group Id 1 is set to Default Profile.
                for (int i = 0; i < groups.length; i++) {
                    groupName = groups[i].trim();
                    Groups group = groupMgr.getGroupByName(groupName);
                    groupkey = groupName.replaceAll(" ", "").toLowerCase();
                    if (group != null && group.getProfileHandler().getId() == 1) {
                        if (groupName.equals(ServerConstants.DEFAULT_PROFILE)) {
                            groupProfileHandler = createProfile(scMap, profileMgr, metaDataMgr, "default.", group
                                    .getId().intValue(), false);
                            // Profile handler for default profile is always going to be 1, even though its group id is
                            // different.
                            ProfileHandler oDefaultProfileHandler = profileMgr.getProfileHandlerById(1L);
                            if (oDefaultProfileHandler == null) {
                                profileLogger
                                        .error("Default Profile Handler missing, Please use InstallSQL to install a new database.");
                                break;
                            }
                            oDefaultProfileHandler.copyFrom(groupProfileHandler);
                            profileMgr.saveProfileHandler(oDefaultProfileHandler);
                        } else {
                            groupkey = "default." + groupkey + ".";
                            groupProfileHandler = createProfile(scMap, profileMgr, metaDataMgr, groupkey, group.getId()
                                    .intValue(), true);
                        }
                        if (!groupName.equals(ServerConstants.DEFAULT_PROFILE)) {
                            group.setProfileHandler(groupProfileHandler);
                            group.setCompany(company);
                            metaDataMgr.saveOrUpdateGroup(group);
                            profileLogger.debug(groupName + " (" + group.getId() + ") updated with PFID ("
                                    + groupProfileHandler.getId() + ")");
                        }
                    }
                }
            }
            profileLogger.debug("Upgrading fixtures custom profile handlers to distinct profile handlers...");
            List<Fixture> oFixtureList = fixtureMgr.loadAllFixturesWithDefaultPFID();
            if (oFixtureList != null) {
                ProfileHandler oNewPFH = null;
                Iterator<Fixture> oFixItr = oFixtureList.iterator();
                while (oFixItr.hasNext()) {
                    Fixture oFixture = oFixItr.next();
                    if (oFixture != null) {
                        oNewPFH = createProfile(scMap, profileMgr, metaDataMgr, "default.", 1, true);
                        oFixture.setProfileHandler(oNewPFH);
                        fixtureMgr.upgradeFixtureProfile(oNewPFH, oFixture.getId());
                        profileLogger.debug(oFixture.getFixtureName() + " (" + oFixture.getId()
                                + ") updated with PFID (" + oNewPFH.getId() + ")");
                    }
                }
            }
        }

        private ProfileHandler createProfile(HashMap<String, String> scMap, ProfileManager profileMgr,
                MetaDataManager metaDataMgr, String strParamPrefix, int profileGroupId, boolean bSave) {
            String sParamPrefix = strParamPrefix;
            ProfileHandler profileHandler = new ProfileHandler();
            profileHandler.setDarkLux(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.dark_lux")));
            profileHandler.setNeighborLux(Integer
                    .valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.neighbor_lux")));
            profileHandler.setEnvelopeOnLevel(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.envelope_on_level")));
            profileHandler.setDropPercent(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.drop")));
            profileHandler.setRisePercent(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.rise")));
            profileHandler.setDimBackoffTime(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.dim_backoff_time")));
            profileHandler.setIntensityNormTime(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.intensity_norm_time")));
            profileHandler.setOnAmbLightLevel(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.on_amb_light_level")));
            profileHandler.setMinLevelBeforeOff(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.min_level_before_off")));
            profileHandler.setRelaysConnected(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.relays_connected")));
            profileHandler.setStandaloneMotionOverride(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.standalone_motion_override")));
            profileHandler.setDrReactivity(Byte
                    .valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.dr_reactivity")));
            profileHandler.setToOffLinger(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.to_off_linger")));
            profileHandler.setInitialOnLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.initial_on_level")));
            profileHandler.setInitialOnTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                    "pfh.initial_on_time")));
            profileHandler.setProfileGroupId((byte) profileGroupId);
            profileHandler.setProfileFlag((byte) 0);
            for (int i = 0; i < 12; i++) {
                Profile profile = new Profile();
                switch (i) {
                case 0:
                case 4:
                case 8:
                    sParamPrefix = strParamPrefix;
                    if (i == 4)
                        sParamPrefix += "weekend.";
                    else if (i == 8)
                        sParamPrefix += "holiday.";
                    profile.setMinLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.min_level")));
                    profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.on_level")));
                    profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.motion_detect_duration")));
                    profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.manual_override_duration")));
                    profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.motion_sensitivity")));
                    profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.ramp_up_time")));
                    profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.morning.ambient_sensitivity")));
                    if (i == 0)
                        profileHandler.setMorningProfile(profile);
                    else if (i == 4)
                        profileHandler.setMorningProfileWeekEnd(profile);
                    else if (i == 8)
                        profileHandler.setMorningProfileHoliday(profile);
                    break;
                case 1:
                case 5:
                case 9:
                    sParamPrefix = strParamPrefix;
                    if (i == 5)
                        sParamPrefix += "weekend.";
                    else if (i == 9)
                        sParamPrefix += "holiday.";
                    profile.setMinLevel(Long
                            .valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.day.min_level")));
                    profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.day.on_level")));
                    profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.day.motion_detect_duration")));
                    profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.day.manual_override_duration")));
                    profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.day.motion_sensitivity")));
                    profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.day.ramp_up_time")));
                    profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.day.ambient_sensitivity")));
                    if (i == 1)
                        profileHandler.setDayProfile(profile);
                    else if (i == 5)
                        profileHandler.setDayProfileWeekEnd(profile);
                    else if (i == 9)
                        profileHandler.setDayProfileHoliday(profile);
                    break;
                case 2:
                case 6:
                case 10:
                    sParamPrefix = strParamPrefix;
                    if (i == 6)
                        sParamPrefix += "weekend.";
                    else if (i == 10)
                        sParamPrefix += "holiday.";
                    profile.setMinLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.min_level")));
                    profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.on_level")));
                    profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.motion_detect_duration")));
                    profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.manual_override_duration")));
                    profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.motion_sensitivity")));
                    profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.ramp_up_time")));
                    profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.evening.ambient_sensitivity")));
                    if (i == 2)
                        profileHandler.setEveningProfile(profile);
                    else if (i == 6)
                        profileHandler.setEveningProfileWeekEnd(profile);
                    else if (i == 10)
                        profileHandler.setEveningProfileHoliday(profile);
                    break;
                case 3:
                case 7:
                case 11:
                    sParamPrefix = strParamPrefix;
                    if (i == 7)
                        sParamPrefix += "weekend.";
                    else if (i == 11)
                        sParamPrefix += "holiday.";
                    profile.setMinLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.night.min_level")));
                    profile.setOnLevel(Long
                            .valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.night.on_level")));
                    profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.night.motion_detect_duration")));
                    profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.night.manual_override_duration")));
                    profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.night.motion_sensitivity")));
                    profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.night.ramp_up_time")));
                    profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                            "profile.night.ambient_sensitivity")));
                    if (i == 3)
                        profileHandler.setNightProfile(profile);
                    else if (i == 7)
                        profileHandler.setNightProfileWeekEnd(profile);
                    else if (i == 11)
                        profileHandler.setNightProfileHoliday(profile);
                    break;
                }
                if (bSave)
                    profileMgr.saveProfile(profile);
            }
            ProfileConfiguration profileConfiguration = new ProfileConfiguration();
            profileConfiguration.setMorningTime(getConfigurationValue(scMap, sParamPrefix, "pfc.morning_time"));
            profileConfiguration.setDayTime(getConfigurationValue(scMap, sParamPrefix, "pfc.day_time"));
            profileConfiguration.setEveningTime(getConfigurationValue(scMap, sParamPrefix, "pfc.evening_time"));
            profileConfiguration.setNightTime(getConfigurationValue(scMap, sParamPrefix, "pfc.night_time"));
            if (bSave)
                profileMgr.saveProfileConfiguration(profileConfiguration); // Save profile Configuration

            saveDefaultWeekDays(scMap, profileConfiguration, metaDataMgr, bSave);

            // Set profile configuration to handler.
            profileHandler.setProfileConfiguration(profileConfiguration);

            if (bSave)
                profileMgr.saveProfileHandler(profileHandler);
            return profileHandler;
        }

        public void saveDefaultWeekDays(HashMap<String, String> scMap, ProfileConfiguration profileConfiguration,
                MetaDataManager metaDataManager, boolean bSave) {
            String weekDayList = getConfigurationValue(scMap, "default.", "metadata.weekday");
            String weekEndList = getConfigurationValue(scMap, "default.", "metadata.weekend");
            int order = 0;
            Set<WeekDay> oWeekDays = new HashSet<WeekDay>();
            if (!ArgumentUtils.isNullOrEmpty(weekDayList)) {
                String[] weekdays = weekDayList.split(",");
                for (int i = 0; i < weekdays.length; i++) {
                    WeekDay weekDay = new WeekDay();
                    weekDay.setDay(weekdays[i].trim());
                    weekDay.setProfileConfiguration(profileConfiguration);
                    weekDay.setShortOrder(++order);
                    weekDay.setType("weekday");
                    oWeekDays.add(weekDay);
                    if (bSave)
                        metaDataManager.saveOrUpdateWeekDay(weekDay);
                }
            }

            if (!ArgumentUtils.isNullOrEmpty(weekEndList)) {
                String[] weekends = weekEndList.split(",");
                for (int i = 0; i < weekends.length; i++) {
                    WeekDay weekDay = new WeekDay();
                    weekDay.setDay(weekends[i].trim());
                    weekDay.setProfileConfiguration(profileConfiguration);
                    weekDay.setShortOrder(++order);
                    weekDay.setType("weekend");
                    oWeekDays.add(weekDay);
                    if (bSave)
                        metaDataManager.saveOrUpdateWeekDay(weekDay);
                }
            }
            profileConfiguration.setWeekDays(oWeekDays);
        }

        /**
         * Returns default values, in case the values are not pre-populated in the database then makes sure that the
         * defaults are still returned.
         * 
         * @param scMap
         *            Map containing key-value pairs.
         * @param key
         *            "name of the configuration parameter"
         * @return value "value of the configuration parameter"
         */
        public String getConfigurationValue(HashMap<String, String> scMap, String sPrefix, String key) {
            String sValue = scMap.get(sPrefix + key);
            if (sValue == null || sValue.equals("")) {
                if (sPrefix.contains("weekend"))
                    sValue = scMap.get("default.weekend." + key);
                else if (sPrefix.contains("holiday"))
                    sValue = scMap.get("default.holiday." + key);
                else
                    sValue = scMap.get("default." + key);
                if (sValue == null || sValue.equals("")) {
                    if (key.endsWith("profile.morning.min_level"))
                        sValue = "0";
                    else if (key.endsWith("profile.morning.on_level"))
                        sValue = "75";
                    else if (key.endsWith("profile.morning.motion_detect_duration"))
                        sValue = "5";
                    else if (key.endsWith("profile.morning.manual_override_duration"))
                        sValue = "60";
                    else if (key.endsWith("profile.morning.motion_sensitivity"))
                        sValue = "1";
                    else if (key.endsWith("profile.morning.ramp_up_time"))
                        sValue = "0";
                    else if (key.endsWith("profile.morning.ambient_sensitivity"))
                        sValue = "5";

                    else if (key.endsWith("profile.day.min_level"))
                        sValue = "20";
                    else if (key.endsWith("profile.day.on_level"))
                        sValue = "75";
                    else if (key.endsWith("profile.day.motion_detect_duration"))
                        sValue = "15";
                    else if (key.endsWith("profile.day.manual_override_duration"))
                        sValue = "60";
                    else if (key.endsWith("profile.day.motion_sensitivity"))
                        sValue = "1";
                    else if (key.endsWith("profile.day.ramp_up_time"))
                        sValue = "0";
                    else if (key.endsWith("profile.day.ambient_sensitivity"))
                        sValue = "5";

                    else if (key.endsWith("profile.evening.min_level"))
                        sValue = "0";
                    else if (key.endsWith("profile.evening.on_level"))
                        sValue = "75";
                    else if (key.endsWith("profile.evening.motion_detect_duration"))
                        sValue = "5";
                    else if (key.endsWith("profile.evening.manual_override_duration"))
                        sValue = "60";
                    else if (key.endsWith("profile.evening.motion_sensitivity"))
                        sValue = "1";
                    else if (key.endsWith("profile.evening.ramp_up_time"))
                        sValue = "0";
                    else if (key.endsWith("profile.evening.ambient_sensitivity"))
                        sValue = "5";

                    else if (key.endsWith("profile.night.min_level"))
                        sValue = "0";
                    else if (key.endsWith("profile.night.on_level"))
                        sValue = "75";
                    else if (key.endsWith("profile.night.motion_detect_duration"))
                        sValue = "5";
                    else if (key.endsWith("profile.night.manual_override_duration"))
                        sValue = "60";
                    else if (key.endsWith("profile.night.motion_sensitivity"))
                        sValue = "1";
                    else if (key.endsWith("profile.night.ramp_up_time"))
                        sValue = "0";
                    else if (key.endsWith("profile.night.ambient_sensitivity"))
                        sValue = "5";

                    // Advance Global variables...
                    else if (key.equals("pfh.dark_lux"))
                        sValue = "20";
                    else if (key.equals("pfh.neighbor_lux"))
                        sValue = "200";
                    else if (key.equals("pfh.envelope_on_level"))
                        sValue = "50";
                    else if (key.equals("pfh.drop"))
                        sValue = "10";
                    else if (key.equals("pfh.rise"))
                        sValue = "20";
                    else if (key.equals("pfh.dim_backoff_time"))
                        sValue = "10";
                    else if (key.equals("pfh.intensity_norm_time"))
                        sValue = "10";
                    else if (key.equals("pfh.on_amb_light_level"))
                        sValue = "0";
                    else if (key.equals("pfh.min_level_before_off"))
                        sValue = "20";
                    else if (key.equals("pfh.relays_connected"))
                        sValue = "1";
                    else if (key.equals("pfh.standalone_motion_override"))
                        sValue = "0";
                    else if (key.equals("pfh.dr_reactivity"))
                        sValue = "0";
                    else if (key.equals("pfh.to_off_linger"))
                        sValue = "30";
                    else if (key.equals("pfh.initial_on_level"))
                        sValue = "50";
                    else if (key.equals("pfh.initial_on_time"))
                        sValue = "5";
                    else if (key.equals("pfh.profile_group_id"))
                        sValue = "1";

                    else if (key.equals("pfc.morning_time"))
                        sValue = "6:00 AM";
                    else if (key.equals("pfc.day_time"))
                        sValue = "9:00 AM";
                    else if (key.equals("pfc.evening_time"))
                        sValue = "6:00 PM";
                    else if (key.equals("pfc.night_time"))
                        sValue = "9:00 PM";
                    else
                        sValue = "0";
                }
            }
            return sValue;
        }
    } // end of ProfileUpgradeTask class

    /**
     * Fetch the IP address given the interface.
     * 
     * @param sIface
     * @return
     */
    public String getIpAddress(String sIface) {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                if (ni.getName().equals(sIface)) {
                    Enumeration<InetAddress> e2 = ni.getInetAddresses();

                    while (e2.hasMoreElements()) {
                        InetAddress ip = (InetAddress) e2.nextElement();
                        if (ip instanceof Inet4Address)
                            return ServerUtil.convertByteArrToIp(ip.getAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param iSortPath
     *            the iSortPath to set
     */
    public void setSortPath(int iSortPath) {
        this.iSortPath = iSortPath;
    }

    /**
     * @return the iSortPath
     */
    public int getSortPath() {
        return iSortPath;
    }

    /**
     * @param iScaling_277_Factor
     *            the iScaling_277_Factor to set
     */
    public void setScalingFactorFor277V(float iScaling_277_Factor) {
        this.iScaling_277_Factor = iScaling_277_Factor;
    }

    /**
     * @return the iScaling_277_Factor
     */
    public float getScalingFactorFor277V() {
        return iScaling_277_Factor;
    }

    /**
     * @param iScaling_240_Factor
     *            the iScaling_240_Factor to set
     */
    public void setScalingFactorFor240V(float iScaling_240_Factor) {
        this.iScaling_240_Factor = iScaling_240_Factor;
    }

    /**
     * @return the iScaling_240_Factor
     */
    public float getScalingFactorFor240V() {
        return iScaling_240_Factor;
    }

    /**
     * @param bApplyECScalingFactor
     *            the bApplyECScalingFactor to set
     */
    public void setApplyECScalingFactor(boolean bApplyECScalingFactor) {
        this.bApplyECScalingFactor = bApplyECScalingFactor;
    }

    /**
     * @return the bApplyECScalingFactor
     */
    public boolean isApplyECScalingFactor() {
        return bApplyECScalingFactor;
    }

    /**
     * @param iScaling_110_Factor
     *            the iScaling_110_Factor to set
     */
    public void setScalingFactorFor110V(float iScaling_110_Factor) {
        this.iScaling_110_Factor = iScaling_110_Factor;
    }

    /**
     * @return the iScaling_110_Factor
     */
    public float getScalingFactorFor110V() {
        return iScaling_110_Factor;
    }

    /**
     * @param iAdj_110_Factor
     *            the iAdj_110_Factor to set
     */
    public void setAdjFactorFor110V(float iAdj_110_Factor) {
        this.iAdj_110_Factor = iAdj_110_Factor;
    }

    /**
     * @return the iAdj_110_Factor
     */
    public float getAdjFactorFor110V() {
        return iAdj_110_Factor;
    }

    /**
     * @param iAdj_277_Factor
     *            the iAdj_277_Factor to set
     */
    public void setAdjFactorFor277V(float iAdj_277_Factor) {
        this.iAdj_277_Factor = iAdj_277_Factor;
    }

    /**
     * @return the iAdj_277_Factor
     */
    public float getAdjFactorFor277V() {
        return iAdj_277_Factor;
    }

    /**
     * @param iAdj_240_Factor
     *            the iAdj_240_Factor to set
     */
    public void setAdjFactorFor240V(float iAdj_240_Factor) {
        this.iAdj_240_Factor = iAdj_240_Factor;
    }

    /**
     * @return the iAdj_240_Factor
     */
    public float getAdjFactorFor240V() {
        return iAdj_240_Factor;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void addShutdownObserver(EmsShutdownObserver o) {
        shutdownHandler.addShutdownObserver(o);
    }

    public void cleanUp() {
        logger.debug("cleaning up all the services ");

        // All the threads should check this flag in their run method if they are in a while(true) loop
        setRunning(false);
        shutdownHandler.notifyObjservers();
        if (dbUpdateThPool != null) {
            dbUpdateThPool.stopThreads();
        }

        logger.debug("cleaning up done");
    }

} // end of class ServerMain
