/**
 * 
 */
package com.ems.server.device;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.FixtureCache;
import com.ems.cache.BallastCache;
import com.ems.model.Ballast;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Groups;
import com.ems.model.Profile;
import com.ems.model.ProfileHandler;
import com.ems.model.SceneLevel;
import com.ems.cache.DeviceInfo;
import com.ems.server.PerfSO;
import com.ems.server.RemoteDebugging;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.service.PMStatsProcessorService;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.EmsThreadPool;
import com.ems.server.util.ServerUtil;
import com.ems.service.EmsAuditService;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.ProfileManager;
import com.ems.service.SwitchManager;
import com.ems.vo.model.VoltPowerCurveValue;

/**
 * @author
 * 
 */
public class DeviceServiceImpl implements DeviceService {

    public static int UNICAST_PKTS_DELAY = 50;
    public static int NO_OF_MULTICAST_TARGETS = 10;
    public static String DEFAULT_AES_KEY = "enLightedWorkNow";
    public static int MULTICAST_INTER_PKT_DELAY = 300; // in milli secs
    public static int MULTICAST_INTER_PKT_DELAY2 = 75; // in milli secs

    public static int VALIDATION_NO_OF_RETRIES = 6;
    public static byte HOPPER_TX_POWER = 0;

    private static Logger m_rdbLogger = Logger.getLogger(RemoteDebugging.class.getName());
    private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");
    private static Logger profileLogger = Logger.getLogger("ProfileLogger");

    /*
     * singleton object instance
     */
    private static DeviceServiceImpl instance = null;

    private FixtureManager fixtureMgr = null;
    private EventsAndFaultManager eventMgr = null;
    private ProfileManager profileMgr = null;
    private GroupManager groupMgr = null;
    private EmsAuditService emsAuditMgr = null;
    private SwitchManager switchMgr = null;

    private GatewayManager gwMgr = null;

    // Thread pool for profile sync packets
    private int noOfProfileSyncProcessThreads = 1;
    private EmsThreadPool profileSyncProcessThPool = null;

    /**
   * 
   */
    private DeviceServiceImpl() {
        // TODO Auto-generated constructor stub

        fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        groupMgr = (GroupManager) SpringContext.getBean("groupManager");
        eventMgr = (EventsAndFaultManager) SpringContext.getBean("eventsAndFaultManager");
        profileMgr = (ProfileManager) SpringContext.getBean("profileManager");
        gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        emsAuditMgr = (EmsAuditService) SpringContext.getBean("emsAuditService");

        profileSyncProcessThPool = new EmsThreadPool(noOfProfileSyncProcessThreads, "ProfileSyncThread");

    } // end of constructor

    /*
     * Singleton method to make sure that only one instance of the DeviceServiceImpl exists
     */
    public static DeviceServiceImpl getInstance() {

        if (instance == null) {
            synchronized (DeviceServiceImpl.class) {
                if (instance == null) {
                    instance = new DeviceServiceImpl();
                }
            }
        }
        return instance;

    } // end of method getInstance

    public void setNoOfMulticastTargets(int noOfTargets) {

        NO_OF_MULTICAST_TARGETS = noOfTargets;

    } // end of method setNoOfMulticastTargets

    public void setMulticastInterPktDelay(int milliSec) {

        MULTICAST_INTER_PKT_DELAY = milliSec;

    } // end of method setMulticastInterPktDelay
    
    public void setHopperTxPower(int txPower) {
      
      HOPPER_TX_POWER = (byte)txPower;
      
    } //end of method setHopperTxPower
        
    public void setMulticastInterPktDelay2(int milliSec) {
    	
    	MULTICAST_INTER_PKT_DELAY2 = milliSec;
    	
    } //end of method setMulticastInterPktDelay2

    public void setUnicastPktsDelay(int millis) {

        UNICAST_PKTS_DELAY = millis;

    } // end of method setUnicastPktsDelay

    private void setFixtureState(int[] fixtureArr, int state) {

        try {
            byte[] level = ServerUtil.intToByteArray(state);
            byte[] timeA = ServerUtil.intToByteArray(0);

            byte[] dataPacket = new byte[level.length + timeA.length];
            System.arraycopy(level, 0, dataPacket, 0, level.length); // Add the file size MSB first
            System.arraycopy(timeA, 0, dataPacket, level.length, timeA.length);
            // multicastFixtures(fixtureArr, dataPacket, ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE, true);
            CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket,
                    ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE, true, MULTICAST_INTER_PKT_DELAY);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error("error in setFixtureState - " + ex.getMessage());
        }

    } // end of method setFixtureState

    public void setAutoState(int[] fixtureArr) {

        setFixtureState(fixtureArr, ServerConstants.AUTO_STATE_ENUM);
        // int noOfFixtures = fixtureArr.length;
        // for(int i = 0; i < noOfFixtures; i++) {
        // setFixtureState(fixtureArr[i], ServerConstants.AUTO_STATE_ENUM);
        // }

    } // end of method setAutoState

    public void setBaselineState(int[] fixtureArr) {

        setFixtureState(fixtureArr, ServerConstants.BASELINE_STATE_ENUM);
        // int noOfFixtures = fixtureArr.length;
        // for(int i = 0; i < noOfFixtures; i++) {
        // setFixtureState(fixtureArr[i], ServerConstants.BASELINE_STATE_ENUM);
        // }

    } // end of method setBaselineState

    public void getRealTimeStatus(long floorId) {

        fixtureMgr.adjustLastOccupancyTime(floorId, 6);
        List<Fixture> fixtList = fixtureMgr.loadFixtureByFloorId(floorId);
        int[] fixtureArr = new int[fixtList.size()];

        int noOfFixtures = fixtList.size();
        for (int i = 0; i < noOfFixtures; i++) {
            Fixture fixture = fixtList.get(i);
            // Symptom: Firing realtime on discovered fixture via gateway on default params updates the state of the
            // fixture(s) from DISCOVERED TO
            // COMMISSIONED.
            // Resolution: Only allow real time status of commissioned fixtures.
            if (fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
                fixtureArr[i] = fixture.getId().intValue();
            }
        }

        byte[] dataPacket = new byte[0];
        // multicastFixtures(fixtureArr, dataPacket, ServerConstants.GET_STATUS_MSG_TYPE, false);
        CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, ServerConstants.GET_STATUS_MSG_TYPE, false,
                MULTICAST_INTER_PKT_DELAY);

        // Fetch real time stats for Gateways
        List<Gateway> gwList = gwMgr.loadFloorGateways(floorId);
        for (int i = 0; i < gwList.size(); i++) {
            Gateway oGW = gwList.get(i);
            if (oGW != null) {
                gwMgr.getRealtimeStats(oGW.getIpAddress());
            }
        }
    } // end of method getRealTimeStatus
    
    private boolean backdoorCommands(int[] fixtureArr, int time) {
      
      ProfileHandler oNewPFH = null;
      switch(time) {
      case 1711:
      	// go to debug mode
        debugFixture(fixtureArr[0]);
        return true;
      case 1773:
      	// Global Profile      	
      	downloadProfileFromSU(fixtureArr[0], 1);
      	waitForProfilePkt(fixtureArr[0]);
      	DeviceInfo device = FixtureCache.getInstance().getDevice((long) fixtureArr[0]);
      	if (device != null) {
          byte[] pfpkt = device.getProfilePkt((long) fixtureArr[0]);
          if (pfpkt != null) {
            oNewPFH = new ProfileHandler();
            assembleNewProfile((long) fixtureArr[0], 1, pfpkt, oNewPFH, device);
          }
      	}
      	return true;
      case 1774:
      	// Weekday
      	downloadProfileFromSU(fixtureArr[0], 2);
      	waitForProfilePkt(fixtureArr[0]);
      	device = FixtureCache.getInstance().getDevice((long) fixtureArr[0]);
      	if (device != null) {
          byte[] pfpkt = device.getProfilePkt((long) fixtureArr[0]);
          if (pfpkt != null) {
            oNewPFH = new ProfileHandler();
            assembleNewProfile((long) fixtureArr[0], 2, pfpkt, oNewPFH, device);
          }
      	}
      	return true;
      case 1775:
      	// Weekend
      	downloadProfileFromSU(fixtureArr[0], 3);
      	waitForProfilePkt(fixtureArr[0]);
      	device = FixtureCache.getInstance().getDevice((long) fixtureArr[0]);
      	if (device != null) {
          byte[] pfpkt = device.getProfilePkt((long) fixtureArr[0]);
          if (pfpkt != null) {
            oNewPFH = new ProfileHandler();
            assembleNewProfile((long) fixtureArr[0], 3, pfpkt, oNewPFH, device);
          }
      	}
      	return true;
      case 1776:
      	// Holiday
      	downloadProfileFromSU(fixtureArr[0], 4);
      	waitForProfilePkt(fixtureArr[0]);
      	device = FixtureCache.getInstance().getDevice((long) fixtureArr[0]);
      	if (device != null) {
          byte[] pfpkt = device.getProfilePkt((long) fixtureArr[0]);
          if (pfpkt != null) {
            oNewPFH = new ProfileHandler();
            assembleNewProfile((long) fixtureArr[0], 4, pfpkt, oNewPFH, device);
          }
      	}
      	return true;
      }
      
      byte[] dataPacket = null;
      int msgType = 0;
      switch (time) {
      case 1770:
          dataPacket = new byte[1];
          dataPacket[0] = (byte) 0;
          msgType = ServerConstants.REBOOT_MSG_TYPE;
          break;
      case 1771:
          dataPacket = new byte[1];
          dataPacket[0] = (byte) 1;
          msgType = ServerConstants.REBOOT_MSG_TYPE;
          break;
      case 1772:
          dataPacket = new byte[1];
          dataPacket[0] = (byte) 2;
          msgType = ServerConstants.REBOOT_MSG_TYPE;
          break;
      case 1717:
          dataPacket = new byte[0];
          msgType = ServerConstants.MANUAL_CALIB_MSG_TYPE;
          break;
      case 1808:
          dataPacket = new byte[0];
          msgType = ServerConstants.ENABLE_HOPPER_MSG_TYPE;
          break;
      case 1809:
          dataPacket = new byte[0];
          msgType = ServerConstants.DISABLE_HOPPER_MSG_TYPE;
          break;
      default:
      	return false;
      }
      CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, msgType, true,
          DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY);      
      return true;
      
    } //end of method backdoorCommands

    public void dimFixtures(int[] fixtureArr, int percentage, int time) {

      if(percentage == -100) {
        //if the percentage is -100, it has to be absolute off
        absoluteDimFixtures(fixtureArr, percentage, time);
        return;
      }
        if (percentage == 0) {
            // no need to send the command to fixture as there is no change required
            return;
        }
        if(percentage == 100 && backdoorCommands(fixtureArr, time)) {
          return;
        }        
    
        byte[] dataPacket = null;
        int msgType = 0;
        boolean negativePerc = false;
        if (percentage < 0) {
          percentage = percentage * -1;
          negativePerc = true;
        }
        byte[] level = ServerUtil.intToByteArray(percentage);
        if (negativePerc) {
          level[0] = -128;
        }
        byte[] timeA = ServerUtil.intToByteArray(time * 60);
        dataPacket = new byte[level.length + timeA.length];
        System.arraycopy(level, 0, dataPacket, 0, level.length); // Add the file size MSB first
        System.arraycopy(timeA, 0, dataPacket, level.length, timeA.length);
        msgType = ServerConstants.SET_LIGHT_LEVEL_MSG_TYPE;
        
        // multicastFixtures(fixtureArr, dataPacket, msgType, true);
        CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, msgType, true,
                DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY);

    } // end of method dimFixtures
    
    private byte DEFAULT_SU_HOP_COUNT = 3;
    
    public void setDefaultSUHopCount(byte hopCount) {
    	
    	DEFAULT_SU_HOP_COUNT = hopCount;
    	
    } //end of method setDefaultSUHopCount

    // function to enable/disable hopper functionality on SU
    public void enableHopper(long fixtureId, boolean enable) {

    	Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
      if (fixture == null) {
          // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
          fixtureLogger.error(fixtureId + ": There is no Fixture");
          return;
      }
        int[] fixtureArr = new int[1];
        fixtureArr[0] = (int) fixtureId;
        int dataLen = 0;
        /*TODO will be done at a later time
        if(enable && ServerUtil.compareVersion(fixture.getVersion(), "2.0") >= 0) {
        	dataLen = 1;
        } */
        byte[] dataPacket = new byte[dataLen];
        if(dataLen == 1) {
        	dataPacket[0] = DEFAULT_SU_HOP_COUNT; //TODO this has to come from GUI
        }
        int msgType = ServerConstants.ENABLE_HOPPER_MSG_TYPE;
        if (!enable) {
            msgType = ServerConstants.DISABLE_HOPPER_MSG_TYPE;
        }
        // multicastFixtures(fixtureArr, dataPacket, msgType, true);
        CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, msgType, true, MULTICAST_INTER_PKT_DELAY);

    } // end of method enableHopper

    private static int nextSeq = 1;

    public static int getNextSeqNo() {

        // TODO: Use timestamp to generate really unique TXID and maybe serially as well;
        if (nextSeq == 1) {
            Random randomGenerator = new Random();
            nextSeq = randomGenerator.nextInt(30000);
        }
        return nextSeq++ % 30000 + 1;

    } // end of method getNextSeqNo

    public void rebootFixture(long fixtureId, int app) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        try {
            if (ServerUtil.compareVersion(fixture.getVersion(), "1.2") < 0) {// old fixture no support
                return;
            }
            // Toggle application reboot based on the current application.
            if (fixture.getCurrApp() == 1)
                app = 2;
            else
                app = 1;

            byte[] dataPacket = new byte[1];
            dataPacket[0] = (byte) app;
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.REBOOT_MSG_TYPE, true,
                    UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.REBOOT_MSG_TYPE, dataPacket, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error(fixtureId + ": error in rebootFixture- " + ex.getMessage());
        }

    } // end of method rebootFixture

    public void fillHeader(byte[] header, int dataLen, int msgType, String version) {

        int i = 0;
        int headerLen = header.length;
        if (ServerUtil.compareVersion(version, "1.2") < 0) {
            // start marker(1), len(1), msg type(1), command, end marker(1)
            header[i++] = ServerConstants.FRAME_START_MARKER; // 0x57
            header[i++] = (byte) (dataLen + 4);
        } else {
            // start marker(1), protocol version(1), len(2), seq no (4), msg type(1),
            // command, end marker(1)
            header[i++] = ServerConstants.FRAME_NEW_START_MARKER; // 0x58
            if (ServerUtil.compareVersion(version, "1.3") < 0) {
                header[i++] = 1; // protocol version
            } else { // node version is 1.3
                header[i++] = 2; // protocol version
            }
            byte[] lenByteArr = ServerUtil.shortToByteArray(dataLen + headerLen + 1);
            System.arraycopy(lenByteArr, 0, header, i++, lenByteArr.length);
            i++;
            int txId = getNextSeqNo();
            byte[] seqByteArr = ServerUtil.intToByteArray(txId);
            System.arraycopy(seqByteArr, 0, header, i++, seqByteArr.length);
            i += 3;
        }
        header[headerLen - 1] = (byte) msgType;

    } // end of method fillHeader

    public byte[] getMulticastHeader(int dataLen, int msgType, byte[] snapAddr) {

        byte[] header = new byte[9 + 2 + snapAddr.length];
        fillHeader(header, dataLen, msgType, "1.3");
        int i = 8;
        /* this is for multi cast */
        header[i++] = 0x5a; // indicating multi cast
        int noOfTargets = snapAddr.length / 3;
        header[i++] = (byte) noOfTargets;
        System.arraycopy(snapAddr, 0, header, i, snapAddr.length);
        i += noOfTargets * 3;
        return header;

    } // end of method getMulticastHeader

    public byte[] getHeader(int dataLen, int msgType, String version, String snapAddr) {

        byte[] header = null;
        int i = 0;
        if (ServerUtil.compareVersion(version, "1.2") < 0) {
            // start marker(1), len(1), msg type(1), command, end marker(1)
            header = new byte[3];
            fillHeader(header, dataLen, msgType, version);
        } else {
            if (ServerUtil.compareVersion(version, "1.3") < 0) {
                // start marker(1), protocol version(1), len(2), seq no (4), msg type(1),
                // command, end marker(1)
                header = new byte[9];
                fillHeader(header, dataLen, msgType, version);
            } else { // this is for 1.3
                // start marker(1), protocol version(1), len(2), seq no (4), msg type(1),
                // command, end marker(1)
                int headerLen = 9;
                if (snapAddr != null) {
                    headerLen = 14;
                }
                header = new byte[headerLen];
                fillHeader(header, dataLen, msgType, version);
                if (snapAddr != null) {
                    /* this is for multi cast */
                    i = 8;
                    header[i++] = 0x5a; // indicating multi cast
                    header[i++] = (byte) 1; // single target
                    byte[] snapByteArr = ServerUtil.getSnapAddr(snapAddr);
                    System.arraycopy(snapByteArr, 0, header, i++, snapByteArr.length);
                    i += 2;
                }
            }
        }
        return header;

    } // end of method getHeader

    /**
     * Waits for Profile Packets
     * 
     * @param fixtureId
     */
    public void waitForProfilePkt(long fixtureId) {

        ServerUtil.sleepMilli(200);
        int retries = 15; // retry for 3 sec. so 15 times
        while (--retries > 0) {
            if (FixtureCache.getInstance().isPFPktMarked(new Long(fixtureId))) {
                // no pending tasks.
              if(profileLogger.isDebugEnabled()) {
                profileLogger.debug(fixtureId + ": profile pkt received.");
              }
                break;
            }
            if(profileLogger.isDebugEnabled()) {
              profileLogger.debug(fixtureId + ": waiting for profile packet...");
            }
            ServerUtil.sleepMilli(200);
        }
    } // end of method waitForProfilePkt

    private void debugFixture(final long fixtureId) {

        new Thread() {
            public void run() {
                long startTime = System.currentTimeMillis();
                long fiveMin = 5 * 60 * 1000;
                while ((System.currentTimeMillis() - startTime) < fiveMin) {
                    getCurrentState(fixtureId);
                    ServerUtil.sleep(10);
                }
            }
        }.start();

    } // end of method debugFixture

    public void absoluteDimFixture(int fixtureId, int percentage, int time) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        try {
            byte[] level = ServerUtil.intToByteArray(percentage);
            byte[] timeA = ServerUtil.intToByteArray(time * 60);

            byte[] dataPacket = new byte[level.length + timeA.length];
            System.arraycopy(level, 0, dataPacket, 0, level.length);
            System.arraycopy(timeA, 0, dataPacket, level.length, timeA.length);
            CommandScheduler.getInstance().addCommand(fixture, dataPacket,
                    ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE, true, UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE, dataPacket, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error(fixtureId + ": error in absoluteDimFixture- " + ex.getMessage());
        }

    } // end of method absoluteDimFixture

    public void absoluteDimFixtures(int[] fixtureArr, int percentage, int time) {

      if(percentage == 100 && backdoorCommands(fixtureArr, time)) {
        return;
      }
      
        try {
        	
            byte[] level = ServerUtil.intToByteArray(percentage);
            byte[] timeA = ServerUtil.intToByteArray(time * 60);

            byte[] dataPacket = new byte[level.length + timeA.length];
            System.arraycopy(level, 0, dataPacket, 0, level.length);
            System.arraycopy(timeA, 0, dataPacket, level.length, timeA.length);
            CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket,
                    ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE, true, DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY);
            // sendPacket(fixture, ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE, dataPacket, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error("error in absoluteDimFixtures- " + ex.getMessage());
        }

    } // end of method absoluteDimFixtures

    public void sendMulticastPacket(ArrayList<Fixture> fixtureList, int msgType, byte[] dataPacket, boolean retryReq,
            int sleepMillis) {

        long gwId = fixtureList.get(0).getSecGwId();
        Gateway gw = gwMgr.loadGateway(gwId);
        if (gw == null) {
            // System.out.println(gwId + ": there is no gateway with the id");
            fixtureLogger.error(gwId + ": There is no Gateway");
            return;
        }

        int noOfFixtures = fixtureList.size();
        byte[] snapAddr = new byte[noOfFixtures * 3];
        Fixture fixture = null;
        for (int i = 0; i < noOfFixtures; i++) {
            fixture = fixtureList.get(i);
            System.arraycopy(ServerUtil.getSnapAddr(fixture.getSnapAddress()), 0, snapAddr, i * 3, 3);
        }

        byte[] header = getMulticastHeader(dataPacket.length, msgType, snapAddr);

        byte[] packet = new byte[header.length + dataPacket.length + 1];
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(dataPacket, 0, packet, header.length, dataPacket.length);
        packet[packet.length - 1] = ServerConstants.FRAME_END_MARKER;

        // YGC: Handling multicast fixtures audit record entry.
        if (msgType != ServerConstants.IMAGE_UPGRADE_MSG_TYPE) {
            for (int i = 0; i < noOfFixtures; i++) {
                fixture = fixtureList.get(i);
                emsAuditMgr.insertAuditRecord(ServerUtil.getTxnId(packet), fixture.getId(), fixture.getFixtureName(),
                        ServerConstants.DEVICE_FIXTURE, msgType);
            }
        }

        if(fixtureLogger.isDebugEnabled()) {
          fixtureLogger.debug("multicast packet: " + ServerUtil.getLogPacket(packet));
        }
        //ServerUtil.logPacket("multicast packet", packet, fixtureLogger);

        if (retryReq) {
            if (msgType == ServerConstants.SET_VALIDATION_MSG_TYPE) {
                ServerMain.getInstance().addGatewayCommand(gw, packet, fixtureList, VALIDATION_NO_OF_RETRIES);
            } else {
                ServerMain.getInstance().addGatewayCommand(gw, packet, fixtureList, DeviceInfo.getNoOfRetries());
            }
        }
        if (gw.getGatewayType() == ServerConstants.ZIGBEE_GW_SU) {
            GatewayComm.getInstance().sendNodeDataToGateway(gwId, gw.getIpAddress(), packet);
        }
        ServerUtil.sleepMilli(sleepMillis);

    } // end of method sendMulticastPacket

    public void dimFixture(int fixtureId, int percentage, int time) {

        if (percentage == 0) {
            // no need to send the command to fixture as there is no change required
            return;
        }
        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println(fixtureId + ": There is no Fixture");
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        if (!fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
            // System.out.println(fixtureId + ": Fixture is not commissioned");
            fixtureLogger.error(fixtureId + ": Fixture is not commissioned");
            return;
        }
        if (time == 1711) {
            // go to debug mode
            debugFixture(fixtureId);
            return;
        }
        if (time == 1770) {
            rebootFixture(fixtureId, 0);
            return;
        }
        if (time == 1771) {
            rebootFixture(fixtureId, 1);
            return;
        }
        if (time == 1772) {
            rebootFixture(fixtureId, 2);
            return;
        }
        if (time == 1717) {
            manualCalib(fixtureId);
            return;
        }
        if (time == 1773) {
            // Global Profile
            downloadProfileFromSU(fixtureId, 1);
            return;
        }
        if (time == 1774) {
            // Weekday
            downloadProfileFromSU(fixtureId, 2);
            return;
        }
        if (time == 1775) {
            // Weekend
            downloadProfileFromSU(fixtureId, 3);
            return;
        }
        if (time == 1776) {
            // Holiday
            downloadProfileFromSU(fixtureId, 4);
            return;
        }

        try {
            boolean negativePerc = false;
            if (percentage < 0) {
                percentage = percentage * -1;
                negativePerc = true;
            }
            byte[] level = ServerUtil.intToByteArray(percentage);
            if (negativePerc) {
                level[0] = -128;
            }
            byte[] timeA = ServerUtil.intToByteArray(time * 60);
            byte[] dataPacket = new byte[level.length + timeA.length];
            System.arraycopy(level, 0, dataPacket, 0, level.length); // Add the file size MSB first
            System.arraycopy(timeA, 0, dataPacket, level.length, timeA.length);
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.SET_LIGHT_LEVEL_MSG_TYPE,
                    true, UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.SET_LIGHT_LEVEL_MSG_TYPE, dataPacket, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
        	fixtureLogger.error(fixtureId + ": error in dimFixture- " + ex.getMessage());
        }

    } // end of method dimFixture

    /**
     * startRemoteDebug - Initiates a remote debug call on the given fixtureid for the specified top level application
     * and the sub level application.
     * 
     * @param fixtureId
     *            as Integer
     * @param rootcategory
     *            as Integer (will be left shifted by the rootcategory value to match SU counting
     * @param subcategory
     *            as Integer (will be left shifted by the subcategory value, to match SU counting
     */
    public void startRemoteDebug(int fixtureId, int rootcategory, int subcategory) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            m_rdbLogger.warn("There is no Fixture with the fixture Id: " + fixtureId);
            return;
        }
        try {
            m_rdbLogger.info("[" + fixture.getFixtureName() + "::" + RemoteDebugging.getApplication(rootcategory) + ":"
                    + RemoteDebugging.getSubApplication(rootcategory, subcategory) + "] Sending RDB message...");
            // NOTE: The su starts counting in the multiples of 2
            rootcategory = 0x1 << rootcategory;
            subcategory = 0x1 << subcategory;

            byte[] application = ServerUtil.intToByteArray(rootcategory);
            byte[] appSubSection = ServerUtil.intToByteArray(subcategory);

            byte[] dataPacket = new byte[application.length + appSubSection.length];
            System.arraycopy(application, 0, dataPacket, 0, application.length);
            System.arraycopy(appSubSection, 0, dataPacket, application.length, appSubSection.length);
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, RemoteDebugging.START_REMOTE_DEBUG, true,
                    UNICAST_PKTS_DELAY);
            // sendPacket(fixture, RemoteDebugging.START_REMOTE_DEBUG, dataPacket, false);
        } catch (Exception ex) {
            m_rdbLogger.fatal(ex.getMessage());
            ex.printStackTrace();
        }

    } // end of method startRemoteDebug

    /**
     * logRDBMessage - acts as hooks for receiving RDB messages for a particular application. The logs will be printed
     * for a min after it the RDB gets triggered.
     * 
     * @param snapAddr
     *            as String
     * @param packet
     *            as byte[] (includes the message)
     */
    public void logRDBMessages(String snapAddr, byte[] packet) {
        StringBuffer sBuf = new StringBuffer();
        // Fetch App and SUBAPP from the packet...
        if(fixtureLogger.isDebugEnabled()) {
          fixtureLogger.debug(snapAddr + ": rdb packet -- " + ServerUtil.getLogPacket(packet));
        }
        int application = (int) (packet[RemoteDebugging.RDB_PKT_APP_NO] & 0xFF) >> 0x1;
        int subapplication = (int) (packet[RemoteDebugging.RDB_PKT_SUBAPP_NO] & 0xFF) >> 0x1;
        sBuf.append("[").append(snapAddr).append("::").append(RemoteDebugging.getApplication(application)).append(":")
                .append(RemoteDebugging.getSubApplication(application, subapplication)).append("] ");
        if (packet.length == RemoteDebugging.RDB_PKT_EXPTECTED_TOTAL_MSG_LENGTH) {
            for (int count = RemoteDebugging.RDB_PKT_MSG_START; count < RemoteDebugging.RDB_PKT_MSG_LENGTH - 1; count++) {
                sBuf.append((char) packet[count]);
            }
        } else {
            for (int count = RemoteDebugging.RDB_PKT_MSG_START; count < packet.length - 1; count++) {
                sBuf.append((char) packet[count]);
            }
        }
        if(m_rdbLogger.isInfoEnabled()) {
          m_rdbLogger.info(sBuf.toString());
        }
    } // end of method logRDBMessages
    
    public void getMulticastCurrentVersion(int[] fixArr) {
    	
    	try {
    		byte[] dataPacket = new byte[0];
    		CommandScheduler.getInstance().addCommand(fixArr, dataPacket, 
    				ServerConstants.GET_VERSION_MSG_TYPE, true, MULTICAST_INTER_PKT_DELAY);
    	} catch (Exception ex) {
    		fixtureLogger.error("error in getCurrentVersion- " + ex.getMessage());
    	}
    	 
    } //end of method getMulticastCurrentVersion

    public void getCurrentVersion(long fixtureId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        try {
            byte[] dataPacket = new byte[0];
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.GET_VERSION_MSG_TYPE, true,
                    UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.GET_VERSION_MSG_TYPE, dataPacket, false);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error(fixtureId + ": error in getCurrentVersion- " + ex.getMessage());
        }

    } // end of method getCurrentVersion

    public void getCurrentState(long fixtureId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        try {
            byte[] dataPacket = new byte[0];
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.GET_STATUS_MSG_TYPE, false,
                    UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.GET_STATUS_MSG_TYPE, dataPacket, false);
            // ServerUtil.sleepMilli(200);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error(fixtureId + ": error in getCurrentState- " + ex.getMessage());
        }

    } // end of method getCurrentState

    public void manualCalib(long fixtureId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        try {
            byte[] dataPacket = new byte[0];
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.MANUAL_CALIB_MSG_TYPE, true,
                    UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.MANUAL_CALIB_MSG_TYPE, dataPacket, true);
            // ServerUtil.sleepMilli(200);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error(fixtureId + ": error in manualCalib- " + ex.getMessage());
        }

    } // end of method manualCalib

    public void sendValidationCmd(int fixtureId, int cmd) {

        sendValidationCmd(fixtureId, cmd, ServerConstants.VAL_DETECT_MOTION_LIGHT_TYPE);

    } // end of method sendValidationCmd

    // this is the validation command to be sent to multiple fixtures
    public void sendValidationCmd(List<Fixture> fixtureList, int[] fixtureArr, int cmd, int detectType, 
    		Gateway gw) {

        int noOfFixtures = fixtureList.size();
        int msgType = ServerConstants.SET_VALIDATION_MSG_TYPE;
        // fixtureMgr.updateLastCommandSent(fixtureArr, msgType);
        int timeout = 60 * 60; // 1 hour

        byte[] cmdArr = ServerUtil.intToByteArray(cmd);

        byte wiringCheck = 0;
        if (detectType == 4) { // 4 is wiring option selected in the GUI
            wiringCheck = 1;
            detectType = 3;
        }
        byte[] shortArr = ServerUtil.shortToByteArray((short) DiscoverySO.getValidationTargetAmbLight());
        byte[] delayArr = { (byte) detectType, 5, shortArr[0], shortArr[1] };
        byte[] timeoutArr = ServerUtil.intToByteArray(timeout);

        short targetRelAmbLight = (short) DiscoverySO.getValidationTargetRelativeAmbLight();
        if(ServerUtil.compareVersion(gw.getApp2Version(), "2.0") >= 0) {
        	targetRelAmbLight = (short) DiscoverySO.getValidationTargetRelativeAmbLight_2();
        }
        byte[] relAmbLightTargetArr = ServerUtil.shortToByteArray(targetRelAmbLight);
        byte maxEnergyReading = (byte) DiscoverySO.getValidationMaxEnergyReading();

        byte[] dataPacket = new byte[cmdArr.length + delayArr.length + timeoutArr.length + 1
                + relAmbLightTargetArr.length + 1];
        int pktPos = 0;
        System.arraycopy(cmdArr, 0, dataPacket, pktPos, cmdArr.length);
        pktPos += 4;
        System.arraycopy(delayArr, 0, dataPacket, pktPos, delayArr.length);
        pktPos += 4;
        System.arraycopy(timeoutArr, 0, dataPacket, pktPos, timeoutArr.length);
        pktPos += 4;
        dataPacket[pktPos++] = wiringCheck;
        System.arraycopy(relAmbLightTargetArr, 0, dataPacket, pktPos, relAmbLightTargetArr.length);
        pktPos += 2;
        dataPacket[pktPos++] = maxEnergyReading;

        CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, msgType, true, MULTICAST_INTER_PKT_DELAY);

    } // end of method sendValidationCmd

    public void sendValidationCmd(int fixtureId, int cmd, int detectType) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        int inter_step_delay = 200; // 200 milli sec
        int timeout = 60 * 60; // 1 hour
        try {
            byte[] cmdArr = ServerUtil.intToByteArray(cmd);
            // byte[] delayArr = ServerUtil.intToByteArray(inter_step_delay);
            byte[] shortArr = ServerUtil.shortToByteArray((short) DiscoverySO.getValidationTargetAmbLight());
            byte[] delayArr = { (byte) detectType, 5, shortArr[0], shortArr[1] };
            byte[] timeoutArr = ServerUtil.intToByteArray(timeout);

            byte[] dataPacket = new byte[cmdArr.length + delayArr.length + timeoutArr.length];
            System.arraycopy(cmdArr, 0, dataPacket, 0, cmdArr.length);
            System.arraycopy(delayArr, 0, dataPacket, cmdArr.length, delayArr.length);
            System.arraycopy(timeoutArr, 0, dataPacket, cmdArr.length + delayArr.length, timeoutArr.length);
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.SET_VALIDATION_MSG_TYPE,
                    true, UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.SET_VALIDATION_MSG_TYPE, dataPacket, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
            fixtureLogger.error(fixtureId + ": error in sendValidationcmd- " + ex.getMessage());
        }

    } // end of method sendValidationCmd

    public void applyWireless(Fixture fixture) {

        byte[] dataPkt = new byte[1];
        dataPkt[0] = (byte) 15; // default delay of 15 sec
        // sendPacket(fixture, ServerConstants.SU_APPLY_WIRELESS_CMD, dataPkt, true);
        CommandScheduler.getInstance().addCommand(fixture, dataPkt, ServerConstants.SU_APPLY_WIRELESS_CMD, true,
                UNICAST_PKTS_DELAY);

    } // end of method applyWireless

    // function to set wireless params on a single fixture
    public void setWirelessParams(long fixtureId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        long gwId = fixture.getSecGwId();
        setWirelessParams(fixtureId, gwId);

    } // end of method setWirelessParams

    class LockObj {

        boolean gotAck = false;

    } // end of class LockObj

    HashMap<Long, LockObj> lockHashMap = new HashMap<Long, LockObj>();
    Map<Long, LockObj> grplockHashMap = new HashMap<Long, LockObj>();
    Map<Long, LockObj> mbitlockHashMap = new HashMap<Long, LockObj>();
    Map<Long, LockObj> grpconfiglockHashMap = new HashMap<Long, LockObj>();

    /**
     * Get Ack
     * 
     * @param fixture
     * @return
     */
    public synchronized boolean getSuWirelessChangeAckStatus(Fixture fixture) {
        boolean bStatus = false;
        LockObj lock = lockHashMap.get(fixture.getId());
        if (lock == null) {
            return bStatus;
        }
        bStatus = lock.gotAck;
        lock = null;
        lockHashMap.remove(fixture.getId());
        return bStatus;
    }

    public void suWirelessChangeAckStatus(Fixture fixture, boolean ackRcvd) {

        if (ackRcvd && fixture.getState().equals(ServerConstants.FIXTURE_STATE_DISCOVER_STR)) {
            // no one is waiting for this ack so it must be the commissioning request
            fixture.setState(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
            DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
            device.setFirstStatsAfterCommission(true);
            if(fixtureLogger.isInfoEnabled()) {
              fixtureLogger.info(fixture.getId() + ": changing state to commissioned");
            }
            fixtureMgr.updateState(fixture);
            return;
        }
        // lockHashMap is used only in case of deletion of sensors
        LockObj lock = lockHashMap.get(fixture.getId());
        if (lock == null) {
            return;
        }
        lock.gotAck = ackRcvd;
        try {
            synchronized (lock) {
                lock.notify();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            fixtureLogger.error("Error in notifying in receivedSuWirelessChangeAck- " + e.getMessage());
        }

    } // end of method suWirelessChangeAckStatus

    public void setWirelessFactoryDefaults(Fixture fixture, boolean track) {

        byte[] dataPkt = new byte[22];
        int i = 0;
        dataPkt[i++] = 4;
        dataPkt[i++] = (byte) DiscoverySO.getDefaultRadioRate();
        // network id
        byte[] tempShortArr = ServerUtil.shortToByteArray(0x6854);
        System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
        i += 2;
        String key = DEFAULT_AES_KEY;
        byte[] keyArr = new byte[17];
        System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
        keyArr[key.length()] = 0;
        System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
        i += 17;
        dataPkt[i++] = 2; // encryption type
        // sendPacket(fixture, ServerConstants.SU_SET_WIRELESS_CMD, dataPkt, true);
        CommandScheduler.getInstance().addCommand(fixture, dataPkt, ServerConstants.SU_SET_WIRELESS_CMD, true,
                UNICAST_PKTS_DELAY);
        if (!track) {
            return;
        }
        LockObj lock = new LockObj();
        lockHashMap.put(fixture.getId(), lock);
        synchronized (lock) {
            try {
                // wait for 30 seconds. it is for safe side. should be notified much before that
                lock.wait(30 * 1000);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        if(fixtureLogger.isDebugEnabled()) {
          fixtureLogger.debug(fixture.getId() + ": wireless factory default Ack status (" + lock.gotAck + "), track (" + track + ")");
        }
        if (!lock.gotAck) {
            fixtureLogger.error(Thread.currentThread().getName() + "=> " + fixture.getId()
                    + ": No ack for factory before deletion");
        }

    } // end of method setWirelessFactoryDefaults
        
    //function to be called with all the fixtures to be hoppers or non hoppers because
    //it is a multi-cast command receiving the same data packet. 
    public void setApplyWirelessDefaults(int[] fixtureArr, Gateway gw, short timeDelay,
	byte isHopper) {
      
      byte[] dataPkt = new byte[29];      
      int i = 0;
      dataPkt[i++] = 0x01; //sub type
      dataPkt[i++] = 4; //channel
      dataPkt[i++] = (byte) DiscoverySO.getDefaultRadioRate();
      // network id
      byte[] tempShortArr = ServerUtil.shortToByteArray(0x6854);
      System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
      i += 2;
      String key = DEFAULT_AES_KEY;
      byte[] keyArr = new byte[17];
      System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
      keyArr[key.length()] = 0;
      System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
      i += 17;
      dataPkt[i++] = 2; // encryption type     
      dataPkt[i++] = isHopper; //hopper enabled
      dataPkt[i++] = DEFAULT_SU_HOP_COUNT; //default hop count
      byte[] timeArr = ServerUtil.shortToByteArray(timeDelay);
      System.arraycopy(timeArr, 0, dataPkt, i, timeArr.length);
      i += 2;
      dataPkt[i++] = HOPPER_TX_POWER; //transmit power
      dataPkt[i++] = 1; //cca mode congestion control
      CommandScheduler.getInstance().addCommand(fixtureArr, dataPkt, 
	  ServerConstants.SU_SET_APPLY_WIRELESS_CMD, true, 
	  DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY);
      
    } //end of method setApplyWirelessParams
    
    //function to be called with all the fixtures to be hoppers or non hoppers because
    //it is a multi-cast command receiving the same data packet.
    public void setApplyWirelessCustom(int[] fixtureArr, Gateway gw, short timeDelay,
	byte isHopper) {
      
      byte[] dataPkt = new byte[29];      
      int i = 0;
      dataPkt[i++] = 0x01; //sub type
      dataPkt[i++] = gw.getChannel().byteValue(); //channel
      dataPkt[i++] = gw.getWirelessRadiorate().byteValue();
      // network id
      byte[] tempShortArr = ServerUtil.shortToByteArray(gw.getWirelessNetworkId());
      System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
      i += 2;
      String key = gw.getWirelessEncryptKey();
      if(key.equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        key = ServerConstants.DEF_WLESS_SECURITY_KEY;
      }
      byte[] keyArr = new byte[17];
      System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
      keyArr[key.length()] = 0;
      System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
      i += 17;
      dataPkt[i++] = gw.getWirelessEncryptType().byteValue();  
      dataPkt[i++] = isHopper; //hopper enabled
      dataPkt[i++] = DEFAULT_SU_HOP_COUNT; //default hop count
      byte[] timeArr = ServerUtil.shortToByteArray(timeDelay);
      System.arraycopy(timeArr, 0, dataPkt, i, timeArr.length);
      i += 2;
      dataPkt[i++] = HOPPER_TX_POWER; //transmit power
      dataPkt[i++] = 1; //cca mode congestion control
      CommandScheduler.getInstance().addCommand(fixtureArr, dataPkt, 
	  ServerConstants.SU_SET_APPLY_WIRELESS_CMD, true, 
	  DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY);
      
    } //end of method setApplyWirelessCustom

    // function to set channel on a single fixture via a specific gateway
    public void setWirelessParams(long fixtureId, long gatewayId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }

        Gateway gw = gwMgr.loadGateway(gatewayId);
        byte[] dataPkt = new byte[22];
        int i = 0;
        dataPkt[i++] = gw.getChannel().byteValue();
        // System.out.println("gw channel -- " + gw.getChannel().byteValue());
        if(fixtureLogger.isDebugEnabled()) {
          fixtureLogger.debug(fixtureId + ": gw channel -- " + gw.getChannel().byteValue());
        }
        dataPkt[i++] = gw.getWirelessRadiorate().byteValue();
        byte[] tempShortArr = ServerUtil.shortToByteArray(gw.getWirelessNetworkId());
        System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
        i += 2;
        String key = gw.getWirelessEncryptKey();
        if(key.equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
          key = ServerConstants.DEF_WLESS_SECURITY_KEY;
        }
        byte[] keyArr = new byte[17];
        System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
        keyArr[key.length()] = 0;
        System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
        i += 17;
        if(fixtureLogger.isDebugEnabled()) {
          fixtureLogger.debug("encryption type == " + gw.getWirelessEncryptType().byteValue());
        }
        byte keyType = gw.getWirelessEncryptType().byteValue();
        if (keyType == 1) {
            keyType = 2;
        }
        dataPkt[i++] = keyType;
        CommandScheduler.getInstance().addCommand(fixture, dataPkt, ServerConstants.SU_SET_WIRELESS_CMD, true,
                UNICAST_PKTS_DELAY);
        // sendPacket(fixture, ServerConstants.SU_SET_WIRELESS_CMD, dataPkt, true);

    } // end of method setWirelessParams

    private void sendFixtureHwFaultAcknowledgement(Fixture fixture, int subEvtType, int txId) {

        try {
            byte[] dataPacket = new byte[9];
            int pktIndex = 0;
            ServerUtil.fillIntInByteArray(ServerConstants.EVENT_HARDWARE_FAILURE, dataPacket, pktIndex);
            pktIndex += 4;
            dataPacket[pktIndex++] = (byte) subEvtType;
            ServerUtil.fillIntInByteArray(txId, dataPacket, pktIndex);
            if(fixtureLogger.isInfoEnabled()) {
              fixtureLogger.info(fixture.getId() + ": hw event ack- " + ServerUtil.getLogPacket(dataPacket));
            }
            // send the acknowledgement
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.SET_ACK_SU, false,
                    UNICAST_PKTS_DELAY);
            if(fixtureLogger.isInfoEnabled()) {
              fixtureLogger.info(fixture.getId() + ": Sent hw event Ack");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method sendFixtureHwFaultAcknowledgement

    private void sendFixtureFaultAcknowledgement(Fixture fixture, int eventType, int txId) {

        int pktIndex = 0;
        byte[] dataPacket = new byte[8];
        try {
            ServerUtil.fillIntInByteArray(eventType, dataPacket, pktIndex);
            pktIndex += 4;
            ServerUtil.fillIntInByteArray(txId, dataPacket, pktIndex);
            pktIndex += 4;
            if(fixtureLogger.isInfoEnabled()) {
              fixtureLogger.info(fixture.getId() + ": event ack- " + ServerUtil.getLogPacket(dataPacket));
            }
            // send the acknowledgment
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.SET_ACK_SU, false,
                    UNICAST_PKTS_DELAY);
            if(fixtureLogger.isInfoEnabled()) {
              fixtureLogger.info(fixture.getId() + ": Sent event Ack");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method sendFixtureFaultAcknowledgement
    
    public byte[] getCurrentTimePacket(int seqNo) {
    	
    	Calendar cal = Calendar.getInstance();

      int currentTime = (int) (System.currentTimeMillis() / (1000 * 60));
      int dst = cal.get(Calendar.DST_OFFSET) / (1000 * 60);
      int gmtOffset = (int) (cal.getTimeZone().getOffset(System.currentTimeMillis()) / (1000 * 60)) - dst;

      try {       
          // current time, offset from gmt, daylight saving (+/-60), seq no.
          byte[] dataPacket = new byte[16];
          int pktIndex = 0;
          // 4 bytes for current time
          ServerUtil.fillIntInByteArray(currentTime, dataPacket, pktIndex);
          pktIndex += 4;
          // 4 bytes for gmt offset
          ServerUtil.fillIntInByteArray(gmtOffset, dataPacket, pktIndex);
          pktIndex += 4;
          // 4 bytes for dst
          ServerUtil.fillIntInByteArray(dst, dataPacket, pktIndex);
          pktIndex += 4;
          // 4 bytes of seq no          
          ServerUtil.fillIntInByteArray(seqNo, dataPacket, pktIndex);          
          pktIndex += 4;
          return dataPacket;        
      } catch (Exception e) {
          e.printStackTrace();
      }
      return null;
      
    } //end of method getCurrentTimePacket

    public void setCurrentTime(Fixture fixture, int seqNo) {
      
      DeviceInfo device = null;
        try {            
            byte[] dataPacket = new byte[0];
            device = FixtureCache.getInstance().getDevice(fixture);
            if(device != null) {
            	device.setLastDateSyncSeqNo(seqNo);  
            	if(device.isLastDateSyncPending()) {
            		//there is already a time command pending on this fixture. so don't schedule
            		return;
            	}
            	device.setLastDateSyncPending(true);
            }
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.SET_CURRENT_TIME, true,
                    UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.SET_CURRENT_TIME, dataPacket, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
          fixture = null;
          device = null;
        }

    } // end of method setCurrentTime

    public static void main(String args[]) {

      if(m_rdbLogger.isDebugEnabled()) {
        m_rdbLogger.debug("inside the main");
      }
        // DeviceServiceImpl impl = new DeviceServiceImpl();
        // impl.discover();

        m_rdbLogger.debug("current time -- " + System.currentTimeMillis());

        byte by = 2;
        String s = "\\x" + by;
        if(m_rdbLogger.isDebugEnabled()) {
          m_rdbLogger.debug("length -- " + s.getBytes().length);
        }
        int bytesRead = -1;
        if ((bytesRead = -1) != -1) {
          if(m_rdbLogger.isDebugEnabled()) {
            m_rdbLogger.debug("not -1");
          }
        } else {
          if(m_rdbLogger.isDebugEnabled()) {
            m_rdbLogger.debug("-1");
          }
        }

    } // end of method main

    public void setProfileSchedule(String id) {

        // System.out.println("setProfileSchedule called");
      if(fixtureLogger.isDebugEnabled()) {
        fixtureLogger.debug("setProfileSchedule called");
      }
        if (fixtureMgr == null) {
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        }
        List<Fixture> fixtureList = fixtureMgr.loadAllFixtures();
        Iterator<Fixture> fixtureIter = fixtureList.iterator();
        while (fixtureIter.hasNext()) {
            Fixture fixture = (Fixture) fixtureIter.next();
            // setProfileSchedule(fixture.getId().intValue());
        }

    } // end of method setProfileSchedule

    private byte[] getGlobalProfileByteArrayByGroupId(long groupId) {
        ProfileHandler prHand = fixtureMgr.getProfileHandlerByGroupId(groupId);
        return prHand.getGlobalProfileByteArray();
    } // end of method getGlobalProfileByteArrayByGroupId

    private byte[] getGlobalProfileByteArray(long fixtureId) {

        ProfileHandler prHand = fixtureMgr.getProfileHandlerByFixtureId(fixtureId);
        return prHand.getGlobalProfileByteArray();

    } // end of method getGlobalProfileByteArray

    public void setGlobalProfile(long fixtureId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        // Update the Global Profile flag
        fixtureMgr.enablePushGlobalProfileForFixture(fixture.getId());

        ProfileHandler prHand = fixtureMgr.getProfileHandlerByFixtureId(fixtureId);

        byte[] gPrByteArr = getGlobalProfileByteArray(fixtureId);
        byte[] profileByteArr = new byte[gPrByteArr.length + 1];
        System.arraycopy(gPrByteArr, 0, profileByteArr, 1, gPrByteArr.length);
        byte gChecksum = ServerUtil.computeChecksum(gPrByteArr);
        profileByteArr[0] = gChecksum;
        if(profileLogger.isDebugEnabled()) {
          profileLogger.debug(fixtureId + ": Sending Global profile, PFHID: " + prHand.getId() + " - chksum: ("
                + prHand.getGlobalProfileChecksum() + ") => " + ServerUtil.getLogPacket(profileByteArr));
        }
        CommandScheduler.getInstance().addCommand(fixture, profileByteArr, ServerConstants.SET_PROFILE_ADV_MSG_TYPE,
                true, UNICAST_PKTS_DELAY);
        // sendPacket(fixture, ServerConstants.SET_PROFILE_ADV_MSG_TYPE, profileByteArr, true);
        // ServerUtil.sleepMilli(UNICAST_PKTS_DELAY);

    } // end of method setGlobalProfile

    public void sendDefaultProfile(int fixtureId) {

        ProfileHandler defaultPrHand = null;
        sendProfileToFixture(defaultPrHand, fixtureId, ServerConstants.SET_PROFILE_MSG_TYPE);

    } // end of method sendDefaultProfile

    public byte calculateGlobalProfileChecksum(Fixture fixture) {
        byte[] profileByteArray = null;
        if (fixture.getGroupId() == 0) {
            // Custom
            profileByteArray = getGlobalProfileByteArray(fixture.getId());
        } else {
            profileByteArray = getGlobalProfileByteArrayByGroupId(fixture.getGroupId());
        }
        return ServerUtil.computeChecksum(profileByteArray);
    } // end of method calculateGlobalProfileChecksum

    public byte calculateScheduledProfileChecksum(Fixture fixture) {
        byte[] profileByteArr = null;
        if (fixture.getGroupId() == 0) {
            // Custom
            long prHandId = fixture.getProfileHandler().getId();
            ProfileHandler prHand = profileMgr.getProfileHandlerById(prHandId);
            profileByteArr = prHand.getScheduledProfileByteArray();
        } else {
            ProfileHandler prHand = fixtureMgr.getProfileHandlerByGroupId(fixture.getGroupId());
            profileByteArr = prHand.getScheduledProfileByteArray();
        }
        byte profileChecksum = ServerUtil.computeChecksum(profileByteArr);
        return profileChecksum;
    } // end of method calculateScheduledProfileChecksum

    /**
     * Returns the profiles group id
     * 
     * @param fixture
     * @return value of the profile group id, zero if custom.
     */
    public byte getProfileGroupId(Fixture fixture) {
        if (fixture.getGroupId() == 0) {
            // Custom
            return 0;
        } else {
            ProfileHandler prHand = fixtureMgr.getProfileHandlerByGroupId(fixture.getGroupId());
            return prHand.getProfileGroupId();
        }
    } // end of method getProfileGroupId

    public void nodeBootInfo(Fixture fixture, byte[] packet, long gwId) {

      if(fixtureLogger.isDebugEnabled()) {
        fixtureLogger.debug(fixture.getId() + ":node boot info packet - " + ServerUtil.getLogPacket(packet));
      }
        /*
         * typedef struct node_info { unsigned char g_profile_checksum; unsigned char s_profile_checksum; unsigned char
         * boot_loader_version_MJ; unsigned char boot_loader_version_MN; version_info_t version; unsigned char
         * is_bypass_on; resetReason reset_reason; plc_info_t plc_info; unsigned char zgwIP[3]; // zigbee gateway ID
         * 00.01.0c etc will modify PING to send it short calibValue; }node_info_t;
         */
        
        DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
        if (device != null) {
            device.setBootTime(new Date());
            device.setUptime(1);
            //SU1 sends starting seq as 0 and SU2 sends starting seq as 1
            //to work with both, last seq no. is set to -2.
            device.setLastStatsSeqNo(-2); 
        }
        setCurrentTime(fixture, 0);
        int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (ServerUtil.compareVersion(fixture.getVersion(), "1.2") < 0) { // old packet
            pktIndex = 3;
        }
        byte gProfileChecksum = packet[pktIndex++]; // 4th
        byte profileChecksum = packet[pktIndex++]; // 5th

        byte bootLoaderMajorVer = packet[pktIndex++]; // 6th
        byte bootLoaderMinorVer = packet[pktIndex++]; // 7th
        String bootLoaderVer = bootLoaderMajorVer + "." + bootLoaderMinorVer;
        // System.out.println(fixtureId + ": boot loader ver -- " + bootLoaderVer);
        if(fixtureLogger.isDebugEnabled()) {
          fixtureLogger.debug(fixture.getId() + ": boot loader ver -- " + bootLoaderVer);
        }
        // if version mismatch schedule an image upload
        // 8 to 13 are version related
        byte releaseNo = packet[pktIndex++];
        byte[] revisionArr = { packet[pktIndex++], packet[pktIndex++] };
        short currRevision = (short) ServerUtil.byteArrayToShort(revisionArr);

        byte major = packet[pktIndex++];
        byte minor = packet[pktIndex++];
        byte appId = packet[pktIndex++];

        String currVersion = major + "." + minor + "." + releaseNo + " b" + currRevision;
        // pktIndex += 6;
        byte bypassOn = packet[pktIndex++]; // 14th
        byte resetReason = packet[pktIndex++]; // 15th
        // System.out.println("reset reason -- " + resetReason);
        if(fixtureLogger.isInfoEnabled()) {
          fixtureLogger.info(fixture.getId() + ": reset reason -- " + resetReason);
        }
        // plc info
        pktIndex += 18;
        // zigbee network id
        pktIndex += 3;
        // calib value
        pktIndex += 2;
        String otherVersion = "Unknown";
        byte profileGroupId = 0;
        short otherRevision = 0;
        String cuVersion = "";
        int imgUpgrStatus = 0;
        if (ServerUtil.compareVersion(fixture.getVersion(), "1.2") >= 0) {
            byte otherMajorVer = packet[pktIndex++];
            byte otherMinorVer = packet[pktIndex++];
            otherVersion = otherMajorVer + "." + otherMinorVer;
            // cu version
            byte[] cuVerArr = { packet[pktIndex++], packet[pktIndex++] };
            cuVersion = "" + (short) ServerUtil.byteArrayToShort(cuVerArr);
            //image upgrade pending status
            imgUpgrStatus = packet[pktIndex++];
            // reserved
            pktIndex += 1;
            if (pktIndex + 4 < packet.length) {
                byte[] otherRevisionArr = { packet[pktIndex++], packet[pktIndex++] };
                otherRevision = (short) ServerUtil.byteArrayToShort(otherRevisionArr);
                // Fetch the profile group id from the SU.
                profileGroupId = packet[pktIndex++];
                // other version release no.
                otherVersion += "." + packet[pktIndex++];
            }
            otherVersion += " b" + otherRevision;
            // System.out.println(fixtureId + ": app1 version -- " + firmwareVersion);
            if(fixtureLogger.isDebugEnabled()) {
              fixtureLogger.debug(fixture.getId() + ": other version -- " + otherVersion);
            }
        }
        if(fixtureLogger.isInfoEnabled()) {
          fixtureLogger.info(fixture.getId() + ": image(CurrVer= " + currVersion + ", OtherVer= " + otherVersion
                + ") booted to app" + appId);
        }
        String upgrStatus = ImageUpgradeSO.getInstance().nodeRebooted(fixture, currRevision, 
            otherRevision, appId, gwId, imgUpgrStatus);
        String app2Version = currVersion;
        String app1Version = otherVersion;
        if (ServerUtil.compareVersion(currVersion, "2.0") >= 0 ||
        	ServerUtil.compareVersion(otherVersion, "2.0") >= 0) {
        	//2.0 nodes always app2 version is current version so no need to do anything
        } else {
        		if(appId == 1) { // su is in app1
        			app2Version = otherVersion;
        			app1Version = currVersion;
        		}
        }
        if (ServerUtil.compareVersion(currVersion, ServerMain.getInstance().getGemsVersion()) == 0) {
            // reset the version synced flag.
            fixture.setVersion(app2Version);
            fixture.setVersionSynced(0);
            fixtureMgr.updateFixtureVersionSyncedState(fixture);
        }        
        try {
            handleProfileMismatch(fixture, gProfileChecksum, profileChecksum, profileGroupId,
                    ServerConstants.NODE_INFO_MSG_TYPE);
        } catch (Exception e) {
            profileLogger.error(fixture.getId() + ": error in handling profile mismatch");
        }
        fixture.setVersion(app2Version);
        fixture.setBootLoaderVersion(bootLoaderVer);
        fixture.setFirmwareVersion(app1Version);
        fixture.setSecGwId(gwId);
        fixture.setCurrApp((short) appId);
        fixture.setCuVersion(cuVersion);        
        if(!upgrStatus.equals(ServerConstants.IMG_UP_STATUS_NOT_PENDING)) {
          fixture.setUpgradeStatus(upgrStatus);
        }
        fixture.setLastConnectivityAt(new Date());
        fixtureMgr.updateBootInfo(fixture, upgrStatus);

    } // end of method nodeBootInfo

    public void initiateProfileSyncActivity(Fixture fixture, byte gProfChecksum, byte sProfChecksum,
            byte profileGroupId, int msgType) {
        long fixtureId = fixture.getId();
        String sResolutionComments = "";
        String sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH;
        String sMsgType = Integer.toHexString(msgType);
        if(profileLogger.isDebugEnabled()) {
          profileLogger.debug(fixtureId + ": (" + sMsgType + ") profile group Id from fixture -- " + profileGroupId);
        }
        byte bFixtureProfileGroupId = getProfileGroupId(fixture);
        if (profileGroupId != bFixtureProfileGroupId) {
          if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": (" + sMsgType + ") fetched groupId from fixture -- "
                    + bFixtureProfileGroupId);
          }
        }

        if(profileLogger.isDebugEnabled()) {
          profileLogger.debug(fixtureId + ": (" + sMsgType + ") profile checksum from fixture -- " + sProfChecksum);
        }
        byte calcSchedPrChecksum = calculateScheduledProfileChecksum(fixture);
        if (sProfChecksum != calcSchedPrChecksum) {
          if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": (" + sMsgType + ") calc schedvprofile checksum -- "
                    + calcSchedPrChecksum);
          }
        }
        if(profileLogger.isDebugEnabled()) {
          profileLogger.debug(fixtureId + ": (" + sMsgType + 
              ") global profile checksum from fixture -- " + gProfChecksum);
        }
        byte calcGlobalPrChecksum = calculateGlobalProfileChecksum(fixture);
        if (gProfChecksum != calcGlobalPrChecksum) {
          if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": (" + sMsgType + ") calculated global profile checksum -- "
                    + calcGlobalPrChecksum);
          }
        }

        // Check for the user action first, if this is still set then we have to push the profile to the SU.
        if (fixture.isPushProfile() || fixture.isPushGlobalProfile()) {
            // an alarm should be raised
            sEventType = EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION;
            eventMgr.addAlarm(fixture, "Fixture profile needs to be pushed", sEventType, EventsAndFault.MAJOR_SEV_STR);
            sResolutionComments = "Profile pushed to SU";
            if(profileLogger.isDebugEnabled()) {
              profileLogger.debug(fixtureId + ": (" + sMsgType + ") Profile needs to be pushed to SU.");
            }
            sendCustomProfile(fixture.getId());
            setGlobalProfile(fixture.getId());
        } else {
            // User push action is NOT set, now the verification begins...
            boolean bCheckProfileChecksum = true;
            // profileGroupId != 0: indicates that the SU is currently associated with some group profile.
            if (profileGroupId != 0) {
                boolean bAssociationCheckRequired = true;
                // Check if it matches any groups...
                if (fixture.getGroupId() != 0) {
                    // Fixture is associated with some group already, verify if its the same.
                    ProfileHandler oGroupPFH = fixtureMgr.getProfileHandlerByGroupId(fixture.getGroupId());
                    if (oGroupPFH != null && oGroupPFH.getProfileGroupId() == profileGroupId) {
                        bAssociationCheckRequired = false;
                        bCheckProfileChecksum = false;
                        if ((sProfChecksum != calcSchedPrChecksum) || (gProfChecksum != calcGlobalPrChecksum)) {
                            sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION;
                            eventMgr.addAlarm(
                                    fixture,
                                    "Fixture "
                                            + fixture.getFixtureName()
                                            + " - ("
                                            + fixture.getVersion()
                                            + ") profile mismatch. Group Id matches but checksum mismatches, needs user action!",
                                    sEventType, EventsAndFault.MAJOR_SEV_STR);
                            if(profileLogger.isInfoEnabled()) {
                              profileLogger.info(fixtureId + ": (" + sMsgType
                                    + ") Group Id matches but checksum mismatches, needs user action on "
                                    + fixture.getFixtureName() + " - (" + fixture.getVersion() + ")");
                            }
                            sResolutionComments += "*** Fixture profile checksum mismatched, need user action! *** ";
                            if (ServerUtil.compareVersion(fixture.getVersion(), ServerMain.getInstance()
                                    .getGemsVersion()) != 0) {
                                fixture.setVersionSynced(1);
                                fixtureMgr.updateFixtureVersionSyncedState(fixture);
                            }
                        }
                    }
                }
                if (bAssociationCheckRequired) {
                    sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH;
                    eventMgr.addAlarm(fixture, "Fixture profile mismatch", sEventType, EventsAndFault.MAJOR_SEV_STR);
                    if(profileLogger.isDebugEnabled()) {
                      profileLogger.debug(fixtureId + ": (" + sMsgType
                            + ") Check if Profile matches any of the groups profiles.");
                    }
                    Long groupId = associateFixtureWithGroupProfileIfMatches(fixtureId, profileGroupId);
                    // The SU should match one of the other profile group in this case, so groupId should never come 0
                    // here
                    if (groupId != 0L) {
                        sResolutionComments = "Profile now associated with group " + groupId.longValue();
                        if(profileLogger.isDebugEnabled()) {
                          profileLogger.debug(fixtureId + ": (" + sMsgType + ") associated with group. "
                                + groupId.longValue());
                        }
                        bCheckProfileChecksum = false;
                    } else {
                        profileLogger
                                .error(fixtureId
                                        + ": Profile should have matched one of the groups, "
                                        + "but since it didn't we are going to download it based on matching the checksums with the fixture custom profile.");
                    }
                }
            }

            if (bCheckProfileChecksum) {
                // This is custom profile, match the checksum's with our fixture profile handler.
                if ((sProfChecksum != calcSchedPrChecksum) || (gProfChecksum != calcGlobalPrChecksum)) {
                    sResolutionComments += "*** Fixture profile checksum mismatched, needs download! *** ";
                    DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
                    ProfileHandler oFixturePFH = fixtureMgr.getProfileHandlerByFixtureId(fixture.getId());
                    ProfileHandler oNewPFH = new ProfileHandler();
                    oNewPFH.create();
                    oNewPFH.copyFrom(oFixturePFH);
                    if(profileLogger.isDebugEnabled()) {
                      profileLogger.debug(fixtureId + ": (" + sMsgType + ") Profile needs to be downloaded from SU.");
                    }
                    downloadScheduleProfile(fixtureId, oNewPFH, device);
                    byte[] profileCSArr = oNewPFH.getScheduledProfileByteArray();
                    if (profileCSArr != null) {
                        oNewPFH.setProfileChecksum((short) ServerUtil.computeChecksum(profileCSArr));
                    }
                    sResolutionComments += "Profile downloaded from SU.";
                    if(profileLogger.isDebugEnabled()) {
                      profileLogger.debug(fixtureId + ": Global Profile needs to be downloaded from SU.");
                    }
                    downloadGlobalProfile(fixtureId, oNewPFH, device);
                    byte[] profileGCSArr = oNewPFH.getGlobalProfileByteArray();
                    if (profileGCSArr != null) {
                        oNewPFH.setGlobalProfileChecksum((short) ServerUtil.computeChecksum(profileGCSArr));
                    }
                    sResolutionComments += ", Global Profile downloaded from SU.";
                    if(profileLogger.isDebugEnabled()) {
                      profileLogger.debug(fixtureId + ": Saving downloaded Fixture Profile to profile Id: "
                            + oNewPFH.getId());
                    }
                    fixtureMgr.syncFixtureProfile(oNewPFH, fixtureId);
                    oNewPFH = null;
                }
                if (!sResolutionComments.equals("")) {
                    resetProfileMisMatchEvent(fixture, sResolutionComments, sEventType);
                }
            }
        }
    } // end of method

    /**
     * This will run in the context of the thread pool., this will handle the senarios where async events coming in
     * short burst are handled in controlled thread pool
     * 
     * @param fixture
     * @param gProfChecksum
     * @param sProfChecksum
     * @param profileGroupId
     * @param msgType
     */
    public void handleProfileMismatch(Fixture fixture, byte gProfChecksum, byte sProfChecksum, byte profileGroupId,
            int msgType) {
    	//if the profile sync work is submitted in the last 5 minutes, don't add now.
    	DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);    
    	if(device != null && (System.currentTimeMillis() - device.getLastProfileSyncTime().getTime() 
    			< PerfSO.FIVE_MINUTE_INTERVAL)) {
    	  if(profileLogger.isInfoEnabled()) {
    		profileLogger.info(fixture.getId() + ": Ignoring the profile sync as it was scheduled in the last 5 minutes");
    	  }
    		return;
    	}
    	device.setLastProfileSyncTime(new Date(System.currentTimeMillis()));
        ProfileSyncWork oProfileSyncWork = new ProfileSyncWork(fixture, gProfChecksum, sProfChecksum, profileGroupId,
                msgType);
        profileSyncProcessThPool.addWork(oProfileSyncWork);
    }

    private void resetProfileMisMatchEvent(Fixture fixture, String sResolutionComments, String sType) {
        List<EventsAndFault> oEventsAndFaultsList = eventMgr.getEventsAndFaultsByFixtureId(fixture.getId());
        Iterator<EventsAndFault> itr = oEventsAndFaultsList.iterator();
        while (itr.hasNext()) {
            EventsAndFault oEventsAndFault = (EventsAndFault) itr.next();
            if (oEventsAndFault != null && oEventsAndFault.getEventType().equals(sType)) {
                oEventsAndFault.setActive(false);
                oEventsAndFault.setResolutionComments(sResolutionComments);
                oEventsAndFault.setResolvedOn(new Date(System.currentTimeMillis()));
                eventMgr.save(oEventsAndFault);
            }
        }

    }

    /**
     * Download sequencially the weekday, weekend and the holiday schedule profiles.
     * 
     * @param fixtureId
     * @param oNewPFH
     * @param device
     */
    public void downloadScheduleProfile(long fixtureId, ProfileHandler oNewPFH, DeviceInfo device) {
        downloadProfileFromSU(fixtureId, 2);
        waitForProfilePkt(fixtureId);
        if (device != null) {
            byte[] pfpkt = device.getProfilePkt(fixtureId);
            if (pfpkt != null)
                assembleNewProfile(fixtureId, 2, pfpkt, oNewPFH, device);

        }
        downloadProfileFromSU(fixtureId, 3);
        waitForProfilePkt(fixtureId);
        if (device != null) {
            byte[] pfpkt = device.getProfilePkt(fixtureId);
            if (pfpkt != null)
                assembleNewProfile(fixtureId, 3, pfpkt, oNewPFH, device);
        }
        downloadProfileFromSU(fixtureId, 4);
        waitForProfilePkt(fixtureId);
        if (device != null) {
            byte[] pfpkt = device.getProfilePkt(fixtureId);
            if (pfpkt != null)
                assembleNewProfile(fixtureId, 4, pfpkt, oNewPFH, device);
        }
    }

    /**
     * Download global (advance) profile details
     * 
     * @param fixtureId
     * @param oNewPFH
     * @param device
     */
    public void downloadGlobalProfile(long fixtureId, ProfileHandler oNewPFH, DeviceInfo device) {
        // Download it from the SU.
        downloadProfileFromSU(fixtureId, 1);
        waitForProfilePkt(fixtureId);
        if (device != null) {
            byte[] pfpkt = device.getProfilePkt(fixtureId);
            if (pfpkt != null)
                assembleNewProfile(fixtureId, 1, pfpkt, oNewPFH, device);
        }
    }

    public void handleSUEvent(long fixtureId, byte[] packet) {

      if(fixtureLogger.isDebugEnabled()) {
        fixtureLogger.debug(fixtureId + ": SU event packet " + ServerUtil.getLogPacket(packet));
      }
        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("No fixture exists with this address " + fixtureId);
            fixtureLogger.error(fixtureId + ": There is no Fixture");
            return;
        }
        // 0 to 7th bytes are header
        // 8th, 9th, 10th is snap address
        // 11th is message type
        // 12th, 13th, 14th, 15th bytes is the event type
        int i = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        byte evtTypeArr[] = { packet[i++], packet[i++], packet[i++], packet[i++] };
        int eventType = ServerUtil.byteArrayToInt(evtTypeArr);
        int evtTxId = ServerUtil.getTxnId(packet);
        switch (eventType) {
        case ServerConstants.EVENT_BYPASS:
            fixtureMgr.updateCurrentState(fixtureId, ServerConstants.CURR_STATE_DISABLED_STR);
            if (isFixtureOfVersion2AndGreater(fixture)) {
                sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            }
            break;
        case ServerConstants.EVENT_BAD_PROFILE_RAM:
            // an alarm should be raised
            eventMgr.addAlarm(fixture, "Bad profile in RAM", EventsAndFault.FIXTURE_BAD_PROFILE,
                    EventsAndFault.MAJOR_SEV_STR);
            if (isFixtureOfVersion2AndGreater(fixture)) {
                sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            }
            break;
        case ServerConstants.EVENT_BAD_PROFILE_ROM:
            // an alarm should be raised
            eventMgr.addAlarm(fixture, "Bad profile in ROM", EventsAndFault.FIXTURE_BAD_PROFILE,
                    EventsAndFault.MAJOR_SEV_STR);
            if (isFixtureOfVersion2AndGreater(fixture)) {
                sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            }
            break;
        case ServerConstants.EVENT_CU_COMM_FAILURE:
            // an alarm should be raised
            eventMgr.addAlarm(fixture, "CU communication failure", EventsAndFault.FIXTURE_CU_FAILURE,
                    EventsAndFault.MAJOR_SEV_STR);
            if (isFixtureOfVersion2AndGreater(fixture)) {
                sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            }
            break;
        case ServerConstants.BAD_MD5SUM:
            // an alarm should be raised
            // byte 16 is app which has the problem
            byte app = packet[i++];
            String imageStr = "Application";
            if (app == 1) {
                imageStr = "Firmware";
            }
            eventMgr.addAlarm(fixture, imageStr + " bad checksum", EventsAndFault.FIXTURE_IMG_CHECKSUM_FAILURE,
                    EventsAndFault.MAJOR_SEV_STR);
            if (isFixtureOfVersion2AndGreater(fixture)) {
                sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            }
            break;
        case ServerConstants.EVENT_DIMMING:
            // an event should be raised
            fixtureMgr.updateCommissionStatus(fixtureId, ServerConstants.COMMISSION_STATUS_DIMMING);
            if (isFixtureOfVersion2AndGreater(fixture)) {
                sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            }
            break;
        case ServerConstants.EVENT_HARDWARE_FAILURE:
            // If the event type is hardware failure than the first byte after that tells about if the failure is in
            // Init, Post or runtime. Then the 4 bytes after that tells about the exact nature of failure

            byte faultStatus = packet[i++];
            String faultTime = "Init";
            if (faultStatus == 1) {
                faultTime = "POST";
            } else if (faultStatus == 2) {
                faultTime = "Runtime";
            }
            byte[] faultType = { packet[i++], packet[i++], packet[i++], packet[i++] };
            // an alarm should be raised
            eventMgr.addAlarm(fixture,
                    "Fixture Hardware failure during " + faultTime + "-" + ServerUtil.byteArrayToInt(faultType),
                    EventsAndFault.FIXTURE_HARDWARE_FAILUE, EventsAndFault.MAJOR_SEV_STR);
            // Send the acknowledgment if the Fixture is greater than 1.0 version
            sendFixtureHwFaultAcknowledgement(fixture, faultStatus, evtTxId);
            break;
        case ServerConstants.EVENT_TOO_HOT: {
            // The too hot event, The 4 bytes after event type tell about the temperature

            byte[] temperatureArr = { packet[i++], packet[i++], packet[i++], packet[i++] };
            int temp = ServerUtil.byteArrayToInt(temperatureArr);
            // an alarm should be raised
            eventMgr.addAlarm(fixture, "Fixture too Hot. Temperature: " + temp, EventsAndFault.FIXTURE_TOO_HOT,
                    EventsAndFault.MAJOR_SEV_STR);
            // Send the acknowledgment if the Fixture is greater than 1.0 version
            sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            break;
        }
        case ServerConstants.EVENT_CPU_TOO_HIGH: {
            // High CPU usage event. The 2 bytes after the event type tell the CPU usage level
            byte[] usage = { packet[i++], packet[i++] };
            // an alarm should be raised
            eventMgr.addAlarm(fixture, "Fixture CPU usage is high. Usage: " + ServerUtil.byteArrayToShort(usage),
                    EventsAndFault.FIXTURE_CPU_USAGE_HIGH, EventsAndFault.MAJOR_SEV_STR);
            // Send the acknowledgment if the Fixture is greater than 1.0 version
            sendFixtureFaultAcknowledgement(fixture, eventType, evtTxId);
            break;
        }
        default:
        if(fixtureLogger.isInfoEnabled()) {
            fixtureLogger.info(fixture.getId() + ": Unknown event received - " + eventType);
        }
            break;
        }
        packet = null;

    }// end of method handleSUEvent

    /**
     * This will return if the fixture is of version 2 or greater
     * 
     * @param fixture
     */
    private boolean isFixtureOfVersion2AndGreater(Fixture fixture) {
        // Get the first string of version
        if (fixture.getVersion() != null) {
            int version = Integer.parseInt(fixture.getVersion().substring(0, 1));
            if (version >= 2) {
                return true;
            } else {
                return false;
            }

        } else {
            fixtureLogger.error("Fixture has a null version, which should not happen. Fixture id:" + fixture.getId());
            return false;
        }
    }

    private void sendProfileToFixture(ProfileHandler prHand, long fixtureId, int msgType) {

        // System.out.println("inside the sendprofiletofixture ");
      if(profileLogger.isDebugEnabled()) {
        profileLogger.debug(fixtureId + ": inside the sendprofiletofixture");
      }
        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            profileLogger.error(fixtureId + ": There is no Fixture");
            return;
        }

        fixtureMgr.enablePushProfileForFixture(fixture.getId());
        Profile dayProfile = prHand.getDayProfile();
        Profile evenProfile = prHand.getEveningProfile();
        Profile nightProfile = prHand.getNightProfile();
        Profile mornProfile = prHand.getMorningProfile();

        Profile dayHolProfile = prHand.getDayProfileHoliday();
        Profile evenHolProfile = prHand.getEveningProfileHoliday();
        Profile nightHolProfile = prHand.getNightProfileHoliday();
        Profile mornHolProfile = prHand.getMorningProfileHoliday();

        Profile dayWeekEndProfile = prHand.getDayProfileWeekEnd();
        Profile evenWeekEndProfile = prHand.getEveningProfileWeekEnd();
        Profile nightWeekEndProfile = prHand.getNightProfileWeekEnd();
        Profile mornWeekEndProfile = prHand.getMorningProfileWeekEnd();

        DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
        try {

            // weekDayProfile
            byte[] weekDayByteArr = prHand.getProfileByteArray(mornProfile, dayProfile, evenProfile, nightProfile);
            // checksum
            byte[] profileArray = new byte[weekDayByteArr.length + 1];
            profileArray[0] = ServerConstants.WEEK_DAY_PROFILE;
            System.arraycopy(weekDayByteArr, 0, profileArray, 1, weekDayByteArr.length);
            byte prChecksum = ServerUtil.computeChecksum(profileArray);

            byte[] dataPacket = new byte[profileArray.length + 1];
            dataPacket[0] = prChecksum;
            System.arraycopy(profileArray, 0, dataPacket, 1, profileArray.length);
            if(profileLogger.isDebugEnabled()) {
              profileLogger.debug(fixture.getId() + ": Sending Weekday profile, PFHID: " + prHand.getId()
                    + " - chksum: (" + prHand.getProfileChecksum() + ") => " + ServerUtil.getLogPacket(dataPacket));
            }

            long seqNo = CommandScheduler.getInstance().addCommand(fixture, dataPacket, msgType, true, UNICAST_PKTS_DELAY);
            if(device != null) {
              device.clearPushProfileFlag();
              device.addProfileSeqNo(seqNo);
            }            
            ServerUtil.sleepMilli(UNICAST_PKTS_DELAY);

            // weekend profile
            byte[] weekEndByteArr = prHand.getProfileByteArray(mornWeekEndProfile, dayWeekEndProfile,
                    evenWeekEndProfile, nightWeekEndProfile);
            // checksum
            profileArray = new byte[weekEndByteArr.length + 1];
            profileArray[0] = ServerConstants.WEEK_END_PROFILE;
            System.arraycopy(weekEndByteArr, 0, profileArray, 1, weekEndByteArr.length);
            prChecksum = ServerUtil.computeChecksum(profileArray);

            dataPacket = new byte[profileArray.length + 1];
            dataPacket[0] = prChecksum;
            System.arraycopy(profileArray, 0, dataPacket, 1, profileArray.length);
            if(profileLogger.isDebugEnabled()) {
              profileLogger.debug(fixture.getId() + ": Sending Weekend profile, PFHID: " + prHand.getId()
                    + " - chksum: (" + prHand.getProfileChecksum() + ") => " + ServerUtil.getLogPacket(dataPacket));
            }

            seqNo = CommandScheduler.getInstance().addCommand(fixture, dataPacket, msgType, true, UNICAST_PKTS_DELAY);
            if(device != null) {              
              device.addProfileSeqNo(seqNo);
            }            
            ServerUtil.sleepMilli(UNICAST_PKTS_DELAY);

            // holiday profile
            int[] fixtureArr = new int[1];
            fixtureArr[0] = fixture.getId().intValue();
            //fixtureMgr.updateLastCommandSent(fixtureArr, msgType);
            byte[] holidayByteArr = prHand.getProfileByteArray(mornHolProfile, dayHolProfile, evenHolProfile,
                    nightHolProfile);
            // checksum
            profileArray = new byte[holidayByteArr.length + 1];
            profileArray[0] = ServerConstants.HOLIDAY_PROFILE;
            System.arraycopy(holidayByteArr, 0, profileArray, 1, holidayByteArr.length);
            prChecksum = ServerUtil.computeChecksum(profileArray);

            dataPacket = new byte[profileArray.length + 1];
            dataPacket[0] = prChecksum;
            System.arraycopy(profileArray, 0, dataPacket, 1, profileArray.length);
            if(profileLogger.isDebugEnabled()) {
              profileLogger.debug(fixture.getId() + ": Sending Holiday profile, PFHID: " + prHand.getId()
                    + " - chksum: (" + prHand.getProfileChecksum() + ") => " + ServerUtil.getLogPacket(dataPacket));
            }
            seqNo = CommandScheduler.getInstance().addCommand(fixture, dataPacket, msgType, true, UNICAST_PKTS_DELAY);
            if(device != null) {              
              device.addProfileSeqNo(seqNo);
            } 
            ServerUtil.sleepMilli(UNICAST_PKTS_DELAY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method sendProfileToFixture

    public void sendCustomProfile(final long fixtureId) {

        // System.out.println("fixture id in sendProfile -- " + fixtureId);
      if(profileLogger.isDebugEnabled()) {
        profileLogger.debug(fixtureId + ": inisde sendCustomProfile");
      }
        if (fixtureMgr == null) {
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        }
        ProfileHandler prHand = fixtureMgr.getProfileHandlerByFixtureId((long) fixtureId);
        sendProfileToFixture(prHand, fixtureId, ServerConstants.SET_PROFILE_MSG_TYPE);

    } // end of method sendCustomProfile

    public void sendCustomProfile(final ProfileHandler prHand, final long fixtureId) {
        // System.out.println("fixture id in sendProfile -- " + fixtureId);
      if(profileLogger.isDebugEnabled()) {
        profileLogger.debug(fixtureId + ": in sendCustomProfile");
      }
        sendProfileToFixture(prHand, fixtureId, ServerConstants.SET_PROFILE_MSG_TYPE);
    } // end of method sendCustomProfile

    public void getFixtureCurrentStatus(long fixtureId) {

        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        getCurrentState(fixtureId);

    } // end of method getFixtureCurrentStatus

    public void updateGroupProfile(ProfileHandler prHand, long groupId) {
        if (fixtureMgr == null)
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureGroupManager");

        List<Fixture> fixtList = fixtureMgr.loadFixtureByGroupId(groupId);

        if (fixtList != null) {
            Iterator<Fixture> iter = fixtList.iterator();
            Fixture fixture = null;
            while (iter.hasNext()) {
                fixture = iter.next();
                if (fixture == null) {
                    continue;
                }
                ProfileHandler fxPFHandler = fixtureMgr.getProfileHandlerByFixtureId(fixture.getId());
                if (fxPFHandler != null) {
                    // System.out.println("updateGroupProfile - Fixture (" + fixture.getId() + "), FxPFID: (" +
                    // fxPFHandler.getId() + "), Group (" + groupId + "), GpPFID: (" + prHand.getId() + ")");
                  if(profileLogger.isDebugEnabled()) {
                    profileLogger.debug("updateGroupProfile - Fixture (" + fixture.getId() + "), FxPFID: ("
                            + fxPFHandler.getId() + "), Group (" + groupId + "), GpPFID: (" + prHand.getId() + ")");
                  }
                    fxPFHandler.copyFrom(prHand);
                    fixtureMgr.updateProfileHandler(fxPFHandler);
                    //enabled fixture profile push 
                    fixtureMgr.enablePushProfileForFixture(fixture.getId());
                    // #1335: Don't send the profile immediately when working with group profile push, just enable the push flag and let the
                    // profile get sync'd during PM stats
                    //sendProfileToFixture(prHand, fixture.getId().intValue(), ServerConstants.SET_PROFILE_MSG_TYPE);
                }
            }
        }

    } // end of method updateGroupProfile

    public void updateAdvanceGroupProfile(ProfileHandler prHand, long groupId) {
        if (fixtureMgr == null)
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureGroupManager");

        List<Fixture> fixtList = fixtureMgr.loadFixtureByGroupId(groupId);

        if (fixtList != null) {
            Iterator<Fixture> iter = fixtList.iterator();
            Fixture fixture = null;
            while (iter.hasNext()) {
                fixture = iter.next();
                if (fixture == null) {
                    continue;
                }
                ProfileHandler fxPFHandler = fixtureMgr.getProfileHandlerByFixtureId(fixture.getId());
                if (fxPFHandler != null) {
                    // System.out.println("updateGroupProfile - Fixture (" + fixture.getId() + "), FxPFID: (" +
                    // fxPFHandler.getId() + "), Group (" + groupId + "), GpPFID: (" + prHand.getId() + ")");
                  if(profileLogger.isDebugEnabled()) {
                    profileLogger.debug(fixture.getId() + ": updateGroupProfile, FxPFID: (" + fxPFHandler.getId()
                            + "), Group (" + groupId + "), GpPFID: (" + prHand.getId() + ")");
                  }
                    fxPFHandler.copyFrom(prHand);
                    fixtureMgr.updateProfileHandler(fxPFHandler);
                    // Update the Global Profile flag
                    fixtureMgr.enablePushGlobalProfileForFixture(fixture.getId());
                    // #1335: Don't send the profile immediately when working with group profile push, just enable the push flag and let the
                    // profile get sync'd during PM stats
                    //setGlobalProfile(fixture.getId().longValue());
                }
            }
        }

    } // end of method updateAdvanceGroupProfile

    public void updateGroupProfile(final long groupId, final long fixtureId) {
        ProfileHandler prHand = fixtureMgr.getProfileHandlerByGroupId(groupId);
        // System.out.println("Pushing profile " + prHand.getId() + " to fixture: " + fixtureId);
        if(profileLogger.isDebugEnabled()) {
          profileLogger.debug(fixtureId + ": Pushing profile " + prHand.getId() + " to fixture, scheduled");
        }
        //enabled fixture profile push 
        fixtureMgr.enablePushProfileForFixture(fixtureId);
        // Update the Global Profile flag
        fixtureMgr.enablePushGlobalProfileForFixture(fixtureId);
        // #1335: Don't send the profile immediately when working with group profile push, just enable the push flag and let the
        // profile get sync'd during PM stats
        /*
        sendProfileToFixture(prHand, fixtureId, ServerConstants.SET_PROFILE_MSG_TYPE);
        // Also send the Global profile
        setGlobalProfile(fixtureId);
        */
    } // end of method updateGroupProfile

    public void cancelDR() {

        eventMgr.addEvent("DR Condition cancelled", EventsAndFault.DR_EVENT_STR, EventsAndFault.INFO_SEV_STR);
        new Thread() {
            public void run() {
                List<Fixture> fixtureList = fixtureMgr.getAllCommissionedFixtureList();
                int noOfFixtures = fixtureList.size();
                int[] fixtArr = new int[noOfFixtures];
                for (int i = 0; i < noOfFixtures; i++) {
                    int fixtureId = fixtureList.get(i).getId().intValue();
                    fixtArr[i] = fixtureId;
                    // setFixtureState(fixtureId, ServerConstants.AUTO_STATE_ENUM);
                }
                setFixtureState(fixtArr, ServerConstants.AUTO_STATE_ENUM);
            }
        }.start();

    } // end of method cancelDR

    public void executeDR(int percentage, int duration) {

        eventMgr.addEvent("DR Condition Initiated", EventsAndFault.DR_EVENT_STR, EventsAndFault.INFO_SEV_STR);
        List<Fixture> fixtureList = fixtureMgr.getAllCommissionedFixtureList();
        int noOfFixtures = fixtureList.size();
        int[] fixtureIdArr = new int[noOfFixtures];
        for (int i = 0; i < noOfFixtures; i++) {
            int fixtureId = fixtureList.get(i).getId().intValue();
            fixtureIdArr[i] = fixtureId;
            // dimFixture(fixtureId, -1 * percentage, duration);
        }
        dimFixtures(fixtureIdArr, -1 * percentage, duration);

    } // end of method executeDR

    /**
     * Dim all the fixture within the group
     * 
     * @param groupId
     * @param percentage
     * @param duration
     */
    public void dimFixturesByGroup(long groupId, int percentage, int duration) {
        if (fixtureMgr == null)
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureGroupManager");

        List<Fixture> fixtList = fixtureMgr.loadFixtureByGroupId(groupId);
        if (fixtList != null) {
            int[] fixtureList = new int[fixtList.size()];
            int count = 0;
            Iterator<Fixture> iter = fixtList.iterator();
            Fixture fixture = null;
            while (iter.hasNext()) {
                fixture = iter.next();
                if (fixture == null) {
                    continue;
                }
                fixtureList[count++] = fixture.getId().intValue();
            }
            if (fixtureList.length > 0) {
              if(fixtureLogger.isInfoEnabled()) {
                fixtureLogger.info("List of Fixtures: " + fixtureList.length + " " + percentage + "%" + ", duration: "
                        + duration);
              }
                dimFixtures(fixtureList, -1 * percentage, duration);
            }
        }
    }

    /**
     * Fetch fixtures by scene id and switch id and control them
     * @param switchId switch identifier
     * @param sceneId scene identifier
     * @param mode percentage 
     * @param duration time in minutes
     */
    public void dimFixturesBySceneOfSwitch(long switchId, long sceneId, int mode, int duration) {
        if (fixtureMgr == null)
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureGroupManager");

        if (switchMgr == null)
            switchMgr = (SwitchManager) SpringContext.getBean("switchManager");

        List<SceneLevel> oSceneLevelList = switchMgr.loadLevelsBySwitchAndSceneId(switchId, sceneId);

        if (oSceneLevelList != null && oSceneLevelList.size() > 0) {
            int[] fixtureList = new int[oSceneLevelList.size()];
            int count = 0;
            Iterator<SceneLevel> iter = oSceneLevelList.iterator();
            SceneLevel oSceneLevel = null;
            while (iter.hasNext()) {
                oSceneLevel = iter.next();
                if (oSceneLevel == null) {
                    continue;
                }
                // Optimize
                if (mode == 100 || mode == 0 || mode == 101) {
                    fixtureList[count++] = oSceneLevel.getFixtureId().intValue();
                } else {
                    absoluteDimFixture(oSceneLevel.getFixtureId().intValue(), oSceneLevel.getLightLevel().intValue(),
                            duration);
                }
            }
            if (mode == 100 || mode == 0) {
                absoluteDimFixtures(fixtureList, mode, duration);
            } else if (mode == 101) {
                setAutoState(fixtureList);
            }
        }
    }


    /**
     * Dim fixture based on switch id
     * @param switchId
     * @param mode
     * @param duration
     */
    public void dimFixturesBySwitch(long switchId, int mode, int duration) {
        if (fixtureMgr == null)
            fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureGroupManager");

        if (switchMgr == null)
            switchMgr = (SwitchManager) SpringContext.getBean("switchManager");

        List<Fixture> oFixtureList = switchMgr.loadFixturesBySwitchId(switchId);

        if (oFixtureList != null && oFixtureList.size() > 0) {
            int[] fixtureList = new int[oFixtureList.size()];
            int count = 0;
            Iterator<Fixture> iter = oFixtureList.iterator();
            Fixture oFixture = null;
            while (iter.hasNext()) {
            	oFixture = iter.next();
                if (oFixture == null) {
                    continue;
                }
                fixtureList[count++] = oFixture.getId().intValue();
            }
            if (mode == 101) {
                setAutoState(fixtureList);
            } else {
                absoluteDimFixtures(fixtureList, mode, duration);
            }
        }
    }

    /**
     * Initiate discovery of PLC sU nodes...
     */
    public void discover() {

        try {
            // TODO message type for discovering plc nodes has to be changed
            byte[] header = getHeader(0, ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE, "1.2", null);
            byte[] packet = new byte[header.length + 1];
            System.arraycopy(header, 0, packet, 0, header.length);
            packet[packet.length - 1] = ServerConstants.FRAME_END_MARKER;
            // plcDeviceImpl.discoverSUnodes(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method discover

    /**
     * Check if the profile group id of any group match with the profile group id of this incoming fixture packet.
     * 
     * @param fixtureId
     * @param profileGroupId
     * @return groupId if matches, else 0.
     */
    public Long associateFixtureWithGroupProfileIfMatches(long fixtureId, byte profileGroupId) {
        List<Groups> oGroupList = groupMgr.loadAllGroups();
        if (oGroupList != null) {
            Iterator<Groups> itr = oGroupList.iterator();
            while (itr.hasNext()) {
                Groups oGroup = (Groups) itr.next();
                if (oGroup != null) {
                    if (oGroup.getProfileHandler().getProfileGroupId().byteValue() == profileGroupId) {
                        fixtureMgr.assignGroupProfileToFixtureProfile(fixtureId, oGroup.getId());
                        return oGroup.getId();
                    }
                }
            }
        }
        return 0L;
    }

    /**
     * Download Profile From SU
     */
    public void downloadProfileFromSU(long fixtureId, int profileID) {
        Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
        if (fixture == null) {
          if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": fixture not found.");
          }
            return;
        }
        try {
            // old fixture no support
            if (ServerUtil.compareVersion(fixture.getVersion(), "1.2") < 0) {
                return;
            }

            byte[] dataPacket = new byte[1];
            dataPacket[0] = (byte) profileID;
            CommandScheduler.getInstance().addCommand(fixture, dataPacket, ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE,
                    false, UNICAST_PKTS_DELAY);
            // sendPacket(fixture, ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE,
            // dataPacket, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Assembly process for the profile Handler download from SU
     * 
     * @param fixtureId
     * @param subType
     * @param pkt
     * @param pfHandler
     */
    public void assembleNewProfile(long fixtureId, int subMsgType, byte[] ackPkt, ProfileHandler oNewPFH,
            DeviceInfo device) {
        switch (subMsgType) {
        case 1:
        if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": Global profile.");
        }
            oNewPFH.setAdvanceProfile(ackPkt);
            break;
        case 2:
        if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": Weekday profile.");
        }
            oNewPFH.setWeekdayProfileFromByteArray(ackPkt);
            break;
        case 3:
        if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": Weekend profile.");
        }
            oNewPFH.setWeekendProfileFromByteArray(ackPkt);
            break;
        case 4:
        if(profileLogger.isDebugEnabled()) {
            profileLogger.debug(fixtureId + ": Holiday profile.");
        }
            oNewPFH.setHolidayProfileFromByteArray(ackPkt);
            break;
        }
        ackPkt = null;
        device.removeProfilePkt(fixtureId);
    }

    /**
     * 
     * @param txnId
     * @param attempts
     * @param status
     */
    public synchronized void updateAuditRecord(long txnId, int attempts, int status) {
        emsAuditMgr.updateAuditRecord(txnId, attempts, status);
    }

    /**
     * 
     * @param txnId
     * @param deviceId
     * @param attempts
     * @param status
     */
    public synchronized void updateAuditRecord(long txnId, long deviceId, int attempts, int status) {
        emsAuditMgr.updateAuditRecord(txnId, deviceId, attempts, status);
    }

    /**
     * Puts profile sync work in thread pool. Rather than in individual thread.
     */
    public class ProfileSyncWork implements Runnable {
        private Fixture fixture;
        private byte gProfChecksum;
        private byte sProfChecksum;
        private byte profileGroupId;
        private int msgType;

        public ProfileSyncWork(Fixture fixture, byte gProfChecksum, byte sProfileChecksum, byte profileGroupId,
                int msgType) {
            this.fixture = fixture;
            this.gProfChecksum = gProfChecksum;
            this.sProfChecksum = sProfileChecksum;
            this.profileGroupId = profileGroupId;
            this.msgType = msgType;
        }

        public void run() {
            try {
                Thread.sleep(50);
                initiateProfileSyncActivity(fixture, gProfChecksum, sProfChecksum, profileGroupId, msgType);
            } catch (InterruptedException ie) {
              if(profileLogger.isDebugEnabled()) {
                profileLogger.debug("interrupted activity.");
              }
            }
        }
    }
    
    public Map<Long, List<Long>> grpProcessingMap = new HashMap<Long, List<Long>>();
    
    /**
     * Send SU Group commands
     * 
     * @param fixtureArr
     *            : List of fixture ids
     * @param msgType:
     *            cmd msg type (such as join group, leave group etc;)
     * @param gType
     *            group type (such as 1 for Motion Group etc;)
     * @param groupNo
     *            (unique no to identify this group
     */
    public void sendSUGroupCommand(int[] fixtureArr, int msgType, byte gType, int groupNo) {
        try {
//            byte groupVersion = 1;
//            byte groupType = gType;
//
//            byte[] dataPacket = new byte[] { (byte) ((groupVersion << 4) + groupType), (byte) ((groupNo >> 16) & 0xFF),
//                    (byte) ((groupNo >> 8) & 0xFF), (byte) (groupNo & 0xFF) };
            fixtureLogger.debug(fixtureArr[0] + ": request to join group " + groupNo);
            byte[] dataPacket = ServerUtil.intToByteArray(groupNo);

            LockObj lock = new LockObj();
            grplockHashMap.put(new Long(fixtureArr[0]), lock);
            CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, msgType, true,
                    MULTICAST_INTER_PKT_DELAY);
            synchronized (lock) {
                try {
                    // wait for 5 seconds. it is for safe side. should be notified before that
                    lock.wait(5 * 1000);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            if (!lock.gotAck) {
                fixtureLogger.error(Thread.currentThread().getName() + "=> " + fixtureArr[0]
                        + ": No ack for group update");
            }
        } catch (Exception ex) {
            fixtureLogger.error("error in sendSUGroupCommand - " + ex.getMessage());
        }
    }
    
    public void suWirelessGrpChangeAckStatus(Fixture fixture, boolean ackRcvd) {

        // grplockHashMap is used only in case of group related changes on SU
        LockObj lock = grplockHashMap.get(fixture.getId());
        if (lock == null) {
            return;
        }
        lock.gotAck = ackRcvd;
        try {
            synchronized (lock) {
                lock.notify();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            fixtureLogger.error("Error in notifying in receivedSuWirelessGrpChangeAck- " + e.getMessage());
        }

    } // end of method suWirelessChangeAckStatus
    
    public synchronized boolean getSuWirelessGrpChangeAckStatus(Fixture fixture) {
        boolean bStatus = false;
        LockObj lock = grplockHashMap.get(fixture.getId());
        if (lock == null) {
            return bStatus;
        }
        bStatus = lock.gotAck;
        lock = null;
        grplockHashMap.remove(fixture.getId());
        return bStatus;
    }

    /**
     * Based on group Fixture
     * @param fixture
     * @return
     */
    public synchronized boolean getSuWirelessGrpChangeAckStatus(GemsGroupFixture groupfixture) {
        boolean bStatus = false;
        LockObj lock = grplockHashMap.get(groupfixture.getFixture().getId());
        if (lock == null) {
            return bStatus;
        }
        bStatus = lock.gotAck;
        lock = null;
        grplockHashMap.remove(groupfixture.getFixture().getId());
        return bStatus;
    }

    /**
     * Based on the Motion bits schedule, returns the byte array that needs to be sent to SU 
     * @param startTime start time (currently, its now)
     * @param endTime (time at which motion bits should be stopped)
     * @return byte array
     */
    public byte[] getMotionBitsTimeIntervals(Date startTime, Date endTime) {
        Calendar cal = Calendar.getInstance();
        int start = (int) (startTime.getTime() / (1000 * 60));
        int end = (int) (endTime.getTime() / (1000 * 60));
        int dst = cal.get(Calendar.DST_OFFSET) / (1000 * 60);
        int gmtOffset = (int) (cal.getTimeZone().getOffset(System.currentTimeMillis()) / (1000 * 60)) - dst;

        try {
            // current time, offset from gmt, daylight saving (+/-60), seq no.
            byte[] dataPacket = new byte[16];
            int pktIndex = 0;
            // 4 bytes for start time
            ServerUtil.fillIntInByteArray(start, dataPacket, pktIndex);
            pktIndex += 4;
            // 4 bytes for end time
            ServerUtil.fillIntInByteArray(end, dataPacket, pktIndex);
            pktIndex += 4;
            // 4 bytes for gmt offset
            ServerUtil.fillIntInByteArray(gmtOffset, dataPacket, pktIndex);
            pktIndex += 4;
            // 4 bytes for dst
            ServerUtil.fillIntInByteArray(dst, dataPacket, pktIndex);
            pktIndex += 4;
            return dataPacket;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     * @param fixtureArr fixtures participating in the motion bit play
     * @param msgType 0xda
     * @param no_of_bits 1 or 2 bits
     * @param startTime: NOW
     * @param endTime: Date time at which the motion bits sending should stop.
     * @param frequency interval at which su will be sending motion bits info (1 - 4)
     */
    public void sendMotionBitCommand(int[] fixtureArr, int msgType, byte no_of_bits, byte frequency,
            byte motion_detection_interval, Date startTime, Date endTime) {
        try {
            byte[] dataPacket = new byte[18];
            byte[] header = new byte[] { (byte) ((no_of_bits << 4) + frequency), (byte) motion_detection_interval };
            byte[] mbitSchedule = getMotionBitsTimeIntervals(startTime, endTime);

            System.arraycopy(header, 0, dataPacket, 0, header.length);
            System.arraycopy(mbitSchedule, 0, dataPacket, header.length, mbitSchedule.length);

            LockObj lock = new LockObj();
            mbitlockHashMap.put(new Long(fixtureArr[0]), lock);
            CommandScheduler.getInstance().addCommand(fixtureArr, dataPacket, msgType, true, MULTICAST_INTER_PKT_DELAY);
            System.out.println("Msg: " + (msgType & 0xff) + ", " + ServerUtil.getLogPacket(dataPacket));
            synchronized (lock) {
                try {
                    // wait for 5 seconds. it is for safe side. should be notified before that
                    lock.wait(5 * 1000);
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            if (!lock.gotAck) {
                fixtureLogger.error(Thread.currentThread().getName() + "=> " + fixtureArr[0]
                        + ": No ack for motion bit action");
            }
        } catch (Exception ex) {
            fixtureLogger.error("error in sendMotionBitcommand - " + ex.getMessage());
        }
    }


    /**
     * Notified when the motion bit ack is going per fixture basis.
     * @param fixture
     * @return true or false
     */
    public void suWirelessMotionBitAckStatus(Fixture fixture, boolean ackRcvd) {
        // grplockHashMap is used only in case of group related changes on SU
        LockObj lock = mbitlockHashMap.get(fixture.getId());
        if (lock == null) {
            return;
        }
        lock.gotAck = ackRcvd;
        try {
            synchronized (lock) {
                lock.notify();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            fixtureLogger.error("Error in notifying in suWirelessMotionBitAckStatus - " + e.getMessage());
        }
    }
        
    HashMap<Long, HashMap<Double, VoltPowerCurveValue>> swCalibHashMap = 
	new HashMap<Long, HashMap<Double, VoltPowerCurveValue>>();
    HashMap<Long, LockObj> calibLockMap = new HashMap<Long, LockObj>();
        
    private void printVoltPowerCurveMap(long fixtureId, Collection<VoltPowerCurveValue> curveMap) {
      
      Iterator<VoltPowerCurveValue> iter = curveMap.iterator();
      while(iter.hasNext()) {
	VoltPowerCurveValue curveVal = iter.next();
	fixtureLogger.debug(fixtureId + ": " + curveVal.getVolts() + " => " + curveVal.getAverageLoad() + 
	    ", " + curveVal.getCurveValue());
      }
      
    } //end of method printVoltPowerCurveMap
    
    /*
    public Collection<VoltPowerCurveValue> calibrateSwMetering(long fixtureId) {
            
      System.out.println("inside the calibrateSwMetering");
      if(calibLockMap.containsKey(fixtureId)) {
	return null; //calibration is already in progress	
      }  
      LockObj lock = new LockObj();
      calibLockMap.put(fixtureId, lock);      
      if(!swCalibHashMap.containsKey(fixtureId)) {
	swCalibHashMap.put(fixtureId, new HashMap<Double, VoltPowerCurveValue>());
      }
      //iterate through manual dimming and fx status for voltage levels in the step of 5
      for(int v = 0; v <=100; v += 5) {
	
	System.out.println(fixtureId + ": calibrating at " + v);
	absoluteDimFixture((int)fixtureId, v, 100);
	ServerUtil.sleep(2);
	lock.gotAck = false;
	for(int k = 0; k < 3; k++) {
	  getCurrentState(fixtureId);
	  ServerUtil.sleep(2);
	  synchronized(swCalibHashMap) {
	    try {
	      swCalibHashMap.wait(1000);
	    }
	    catch(Exception ex) {
	      ex.printStackTrace();
	    }
	  }
	  if(lock.gotAck) {
	    break;
	  }
	}
      }     
      Collection<VoltPowerCurveValue> curveMap = swCalibHashMap.get(fixtureId).values();
      Ballast ballast = ServerMain.getInstance().getDevice(fixtureId).getFixture().getBallast();
      calculateCurveValues(curveMap, ballast);
      printVoltPowerCurveMap(fixtureId, curveMap);
      //add this volt power curve to the database      
      BallastCache.getInstance().addVoltPowerCurveMap(ballast, curveMap);
      calibLockMap.remove(fixtureId);
      return curveMap;
      
    } //end of method calibrateSwMetering
    */
    private void calculateCurveValues(Collection<VoltPowerCurveValue> curveMap, Ballast ballast) {
      
      Iterator<VoltPowerCurveValue> iter = curveMap.iterator();
      while(iter.hasNext()) {
	VoltPowerCurveValue curveVal = iter.next();
	double doubleCurveVal =  (curveVal.getAverageLoad() * 100) / 
	    (ballast.getBallastFactor() * ballast.getWattage() *  ballast.getLampNum());	    
	curveVal.setCurveValue(doubleCurveVal);
      }
      
    } //end of method calculateCurveValues
    
    public Collection<VoltPowerCurveValue> calibrateSwMetering(Long[] fixtureIds) throws Exception {
      
      //System.out.println("inside the calibrateSwMetering");      
      //this is not synchronized. so if multiple people try at the same time, this might have problem
      for(int i = 0; i < fixtureIds.length; i++) {
	long fixtureId = fixtureIds[i];
	if(calibLockMap.containsKey(fixtureId)) {
	  throw new Exception(fixtureId + ": calibration is already in progress");
	  //continue; //calibration is already in progress	
	}
      }
      
      for(int i = 0; i < fixtureIds.length; i++) {
	long fixtureId = fixtureIds[i];
	LockObj lock = new LockObj();
	calibLockMap.put(fixtureId, lock);
	if(!swCalibHashMap.containsKey(fixtureIds)) {
	  swCalibHashMap.put(fixtureId, new HashMap<Double, VoltPowerCurveValue>());
	}
	//iterate through manual dimming and fx status for voltage levels in the step of 5
	for(int v = 100; v > 0; v -= 5) {
	  System.out.println(fixtureId + ": calibrating at " + v);
	  absoluteDimFixture((int)fixtureId, v, 100);
	  ServerUtil.sleep(4);
	  lock.gotAck = false;
	  for(int k = 0; k < 3; k++) {
	    getCurrentState(fixtureId);
	    ServerUtil.sleep(2);
	    synchronized(swCalibHashMap) {
	      try {
		swCalibHashMap.wait(1000);
	      }
	      catch(Exception ex) {
		ex.printStackTrace();
	      }
	    }
	    if(lock.gotAck) {
	      break;
	    }
	  }
	}  
	calibLockMap.remove(fixtureId);
      }
      Ballast ballast = FixtureCache.getInstance().getDevice(fixtureIds[0]).getFixture().getBallast();      
      HashMap<Double, VoltPowerCurveValue> calibCurveMap = new HashMap<Double, VoltPowerCurveValue>();
      for(int k = 0; k < fixtureIds.length; k++) {
	HashMap<Double, VoltPowerCurveValue> curveMap = swCalibHashMap.get(fixtureIds[k]);
	//printVoltPowerCurveMap(fixtureIds[k], curveMap.values());
	Iterator<Double> iter = curveMap.keySet().iterator();
	while(iter.hasNext()) {
	  double doubleVolt = iter.next();
	  VoltPowerCurveValue val = calibCurveMap.get(doubleVolt);
	  if(val == null) {
	    val = new VoltPowerCurveValue();
	    val.setVolts(doubleVolt);
	    calibCurveMap.put(doubleVolt, val);
	  }
	  val.addLoad(curveMap.get(doubleVolt).getLoad());	  
	}
      }
      calculateCurveValues(calibCurveMap.values(), ballast);
      printVoltPowerCurveMap(fixtureIds[0], calibCurveMap.values());
      //add this volt power curve to the database
      BallastCache.getInstance().addVoltPowerCurveMap(ballast, calibCurveMap.values());
      return calibCurveMap.values();
      
    } //end of method calibrateSwMetering
        
    public void receivedFxStats(Fixture fixture, int volts, int watts) {
      
      long fixtureId = fixture.getId();
      LockObj lock = calibLockMap.get(fixtureId);
      if(lock == null) {
	return;
      }
      if(volts > 0 && (watts == 0 || watts == PMStatsProcessorService.SHORT_MINUS_ONE)) { 
	//ignore the fx stats if it is 0 or -1 
	return;
      }
      lock.gotAck = true;              
      fixtureLogger.debug(fixtureId + ": " + volts + "(volts) --> " + watts + "(watts)");
      VoltPowerCurveValue curveVal = new VoltPowerCurveValue();
      Double voltDouble = (double)volts/10;
      double wattsDouble = watts / fixture.getNoOfFixtures();
      curveVal.setVolts(voltDouble);
      curveVal.addLoad(wattsDouble);
      swCalibHashMap.get(fixtureId).put(voltDouble, curveVal);
      
      if(volts == 5) {
	VoltPowerCurveValue curveVal1 = new VoltPowerCurveValue();
	Double voltDouble1 = (double)0;	      
	curveVal1.setVolts(voltDouble1);
	curveVal1.addLoad(wattsDouble);
	swCalibHashMap.get(fixtureId).put(voltDouble1, curveVal1);
      }
      synchronized(swCalibHashMap) {
	swCalibHashMap.notify();
      }
      
    } //end of method receivedFxStats
    
    public void sendSwitchGroupConfiguration(int[] fixtureArr, byte[] config, int msgType) {
        try {
            LockObj lock = new LockObj();
            grpconfiglockHashMap.put(new Long(fixtureArr[0]), lock);
            CommandScheduler.getInstance().addCommand(fixtureArr, config, msgType, true, MULTICAST_INTER_PKT_DELAY);
            System.out.println("Msg: " + (msgType & 0xff) + ", " + ServerUtil.getLogPacket(config));
            synchronized (lock) {
                try {
                    // wait for 5 seconds. it is for safe side. should be notified before that
                    lock.wait(5 * 1000);
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            if (!lock.gotAck) {
                fixtureLogger.error(Thread.currentThread().getName() + "=> " + fixtureArr[0]
                        + ": No ack for switch group configuration");
            }
        } catch (Exception ex) {
            fixtureLogger.error("error in sendSwitchGroupConfiguration - " + ex.getMessage());
        }
    }
    
    public void sendWdsGroupConfiguration(int[] fixtureArr, byte[] config, int msgType) {
        try {
            LockObj lock = new LockObj();
            grpconfiglockHashMap.put(new Long(fixtureArr[0]), lock); // TODO: Review locking
            CommandScheduler.getInstance().addCommand(fixtureArr, config, msgType, true, MULTICAST_INTER_PKT_DELAY);
            System.out.println("Msg: " + (msgType & 0xff) + ", " + ServerUtil.getLogPacket(config));
            synchronized (lock) {
                try {
                    // wait for 5 seconds. it is for safe side. should be notified before that
                    lock.wait(5 * 1000);
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            if (!lock.gotAck) {
                fixtureLogger.error(Thread.currentThread().getName() + "=> " + fixtureArr[0]
                        + ": No ack for switch group configuration");
            }
        } catch (Exception ex) {
            fixtureLogger.error("error in sendSwitchGroupConfiguration - " + ex.getMessage());
        }
    }

    public void suWirelessGrpConfigChangeAckStatus(Fixture fixture, boolean ackRcvd) {

        // grplockHashMap is used only in case of group related changes on SU
        LockObj lock = grpconfiglockHashMap.get(fixture.getId());
        if (lock == null) {
            return;
        }
        lock.gotAck = ackRcvd;
        try {
            synchronized (lock) {
                lock.notify();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            fixtureLogger.error("Error in notifying in suWirelessGrpConfigChangeAckStatus- " + e.getMessage());
        }

    } // end of method suWirelessChangeAckStatus

    public synchronized boolean getSuWirelessGrpConfigChangeAckStatus(Long groupfixtureId) {
        boolean bStatus = false;
        LockObj lock = grpconfiglockHashMap.get(groupfixtureId);
        if (lock == null) {
            return bStatus;
        }
        bStatus = lock.gotAck;
        lock = null;
        grpconfiglockHashMap.remove(groupfixtureId);
        return bStatus;
    }
    
    public void sendUTCTime(Gateway gw) {

    	Calendar cal = Calendar.getInstance();
    	    	
    	long currentMillis = System.currentTimeMillis();
    	int currentTimeSec = (int) (currentMillis / 1000);
    	short milliSec = (short) (currentMillis % 1000);
    	short hundredthSec = (short) (milliSec / 10);
    	byte dst = (byte) (cal.get(Calendar.DST_OFFSET) / (1000 * 60));
    	short gmtOffset = (short) (cal.getTimeZone().getOffset(currentMillis) / 1000 / 60 - dst);
    	fixtureLogger.debug("sending utc time => " + currentMillis + "(ms) " + currentTimeSec + " (s) " + 
    			hundredthSec +  "(hs) " + gmtOffset + " (offset)");
    	    	
    	try {       
    		byte[] dataPacket = new byte[9];
    		int pktIndex = 0;
    		// 4 bytes for utc time in sec since jan 1, 1970        
    		ServerUtil.fillIntInByteArray(currentTimeSec, dataPacket, pktIndex);
    		pktIndex += 4;
    		// 2 bytes for utc hundredths of sec (0-99)
    		ServerUtil.fillShortInByteArray(hundredthSec, dataPacket, pktIndex);
    		pktIndex += 2;
    		// 2 bytes for offset of time zone in minutes ((-11 - +14)*60)
    		ServerUtil.fillShortInByteArray(gmtOffset, dataPacket, pktIndex);
    		pktIndex += 2;
    		// 1 byte for DST 0 when DST not presently observed or 60 when observed
    		dataPacket[pktIndex++] = dst;        
    		
    		byte[] header = getHeader(dataPacket.length, ServerConstants.CMD_SET_UTC_TIME, "1.2", null);
    		byte[] pkt = new byte[header.length + dataPacket.length + 1];
    		System.arraycopy(header, 0, pkt, 0, header.length);
    		pkt[pkt.length - 1] = ServerConstants.FRAME_END_MARKER;   		
    		System.arraycopy(dataPacket, 0, pkt, header.length, dataPacket.length);
    		GatewayComm.getInstance().sendNodeDataToGateway(gw.getId(), gw.getIpAddress(), pkt);    
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	     
    } //end of method sendUTCTime

} // end of class DeviceServiceImpl