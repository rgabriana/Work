/**
 * 
 */
package com.ems.server.device;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.FixtureCache;
import com.ems.model.Area;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.SystemConfiguration;
import com.ems.server.EmsShutdownObserver;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.util.EmsThreadPool;
import com.ems.server.util.ServerUtil;
import com.ems.service.AreaManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.SystemConfigurationManager;

/**
 * @author
 * 
 */
public class BacnetService implements EmsShutdownObserver {

    private static BacnetService instance = null;

    public static int BACNET_SERVER_PORT = 51368;
    public static int BACNET_GEMS_LISTENER_PORT = 51367;

    private EmsThreadPool threadPool = null;
    private BacnetListenerDaemon bacnetListenerDaemon = null;

    private FixtureManager fixtureMgr = null;
    private GatewayManager gwMgr = null;
    private AreaManager areaMgr = null;
    private EventsAndFaultManager eventMgr = null;

    static final Logger logger = Logger.getLogger("BacnetLog");

    public static final int BACNET_CMD_TYPE_REQ = 1;
    public static final int BACNET_CMD_TYPE_RESP = 2;

    public static final int BACNET_MSGTYPE_SU_DISCOVER = 0x1;
    public static final int BACNET_MSGTYPE_SU_DATA_MAC = 0x2;
    public static final int BACNET_MSGTYPE_SU_DATA_ID = 0x3;
    public static final int BACNET_MSGTYPE_SU_DIM_MAC = 0x4;
    public static final int BACNET_MSGTYPE_SU_DIM_ID = 0x5;
    
    public static final int BACNET_MSGTYPE_GW_DISCOVER = 0x6;
    public static final int BACNET_MSGTYPE_GW_SU_DISCOVER = 0x7;
    public static final int BACNET_MSGTYPE_GW_SU_DATA_ID_LIST = 0x8;
    public static final int BACNET_MSGTYPE_GW_DATA = 0x9;
    public static final int BACNET_MSGTYPE_SU_DETAILS_ID = 0xa;
    

    public static final int BACNET_MSGTYPE_GEMS_DATA = 0x81;
    public static final int BACNET_MSGTYPE_DR = 0x82;
    public static final int BACNET_MSGTYPE_DR_STATUS = 0x83;

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

    public static final byte FIXTURE_STATE_DISCOVERED = 1;
    public static final byte FIXTURE_STATE_COMMISSIONED = 2;
    public static final byte FIXTURE_STATE_DELETED = 3;

    private static final int VERSION_LEN = 16;

    /**
   * 
   */
    private BacnetService() {

        // logger.debug("Constructor of bacnet service");
        // TODO Auto-generated constructor stub
        eventMgr = (EventsAndFaultManager) SpringContext.getBean("eventsAndFaultManager");
        gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        areaMgr =(AreaManager) SpringContext.getBean("areaManager");
        if(isBacnetEnabled())
        {
	        int noOfThreads = 2; // ServerMain.getInstance().getNoOfCmdRespListenerThreads();
	        threadPool = new EmsThreadPool(noOfThreads, "BacnetService");
	        bacnetListenerDaemon = new BacnetListenerDaemon();
	        bacnetListenerDaemon.start();
	        ServerUtil.sleep(1);
	        startBacnetService();
	        // ServerMain.getInstance().addShutdownObserver(bacnetListenerDaemon);
        }
    } // end of constructor BacnetService

    public static BacnetService getInstance() {

        if (instance == null) {
            synchronized (BacnetService.class) {
                if (instance == null) {
                    instance = new BacnetService();
                }
            }
        }
        return instance;

    } // end of method getInstance

    private void startBacnetService() {
    	
        try {
            Process bacnetProcess = Runtime.getRuntime().exec("/var/lib/tomcat6/Enlighted/bacnet restart");
            String desc = "Bacnet service started";
            if(logger.isInfoEnabled()) {
              logger.info(desc);
            }
            eventMgr.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
        } catch (Exception e) {
            logger.error("Bacnet process could not be started " + e.getMessage());
        }
        

    } // end of method startBacnetService
    /**
     *  Enabling or disabling cron job for bacnet services. 
     */
    private boolean isBacnetEnabled()
    {
    	Boolean result =false ;
    	SystemConfigurationManager systemConfigurationManager =(SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");	
    	String bacnet_entry = systemConfigurationManager.loadConfigByName("menu.bacnet.show").getValue() ;    	
    	if(bacnet_entry.equalsIgnoreCase("true"))
    	{
    		result= true ;		
    	}
    	else 
    	{
    		result= false ;
    		 try {
    	            Process bacnetProcess = Runtime.getRuntime().exec("/var/lib/tomcat6/Enlighted/bacnet stop");
    	            int status = bacnetProcess.waitFor();
    	            String desc = "Bacnet service stopped: Status  " + status;
    	            if(logger.isInfoEnabled()) {
    	              logger.info(desc);
    	            }
    	        } catch (Exception e) {
    	            logger.error("Bacnet process could not be stopped " + e.getMessage());
    	        }

    	}
    	
		return result ;
    }

    private void stopBacnetService() {

        try {
            Process bacnetProcess = Runtime.getRuntime().exec("/var/lib/tomcat6/Enlighted/bacnet stop");
            int status = bacnetProcess.waitFor();
            String desc = "Bacnet service stopped: Status  " + status;
            if(logger.isInfoEnabled()) {
              logger.info(desc);
            }
            eventMgr.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
        } catch (Exception e) {
            logger.error("Bacnet process could not be stopped " + e.getMessage());
        }

    } // end of method startBacnetService

    public Properties getBacnetConfiguration() {

        Properties bacnetProp = new Properties();
        SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext
                .getBean("systemConfigurationManager");
        SystemConfiguration tempConfig = sysMgr.loadConfigByName("bacnet.vendor_id");
        if (tempConfig != null) {
          if(logger.isDebugEnabled()) {
            logger.debug("from database bacnet vendor id -- " + tempConfig.getValue());
          }
            bacnetProp.put("VendorId", tempConfig.getValue());
        }
        tempConfig = sysMgr.loadConfigByName("bacnet.server_port");
        if (tempConfig != null) {
          if(logger.isDebugEnabled()) {
            logger.debug("from database bacnet server port -- " + tempConfig.getValue());
          }
            bacnetProp.put("ListenPort", tempConfig.getValue());
        }
        tempConfig = sysMgr.loadConfigByName("bacnet.network_id");
        if (tempConfig != null) {
          if(logger.isDebugEnabled()) {
            logger.debug("from database bacnet network id -- " + tempConfig.getValue());
          }
            bacnetProp.put("NetworkId", tempConfig.getValue());
        }
        tempConfig = sysMgr.loadConfigByName("bacnet.max_APDU_length");
        if (tempConfig != null) {
          if(logger.isDebugEnabled()) {
            logger.debug("from database bacnet max APDU length -- " + tempConfig.getValue());
          }
            bacnetProp.put("MaxAPDU", tempConfig.getValue());
        }
        tempConfig = sysMgr.loadConfigByName("bacnet.APDU_timeout");
        if (tempConfig != null) {
          if(logger.isDebugEnabled()) {
            logger.debug("from database bacnet APDU timeout -- " + tempConfig.getValue());
          }
            int timeOut = Integer.parseInt(tempConfig.getValue()) / 1000;
            bacnetProp.put("APDUTimeout", Integer.toString(timeOut));
        }
        tempConfig = sysMgr.loadConfigByName("bacnet.device_base_instance");
        if (tempConfig != null) {
          if(logger.isDebugEnabled()) {
            logger.debug("from database bacnet device base instance -- " + tempConfig.getValue());
          }
            bacnetProp.put("DeviceBaseInstance", tempConfig.getValue());
        }
        return bacnetProp;

    } // end of method getBacnetConfiguration

    private boolean checkPortAvailable(int port) {

        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(port, InetAddress.getLocalHost());
            ds.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            logger.error("Error in check port availability: " + e.getMessage());
        } finally {
            if (ds != null) {
                try {
                    ds.close();
                } catch (Exception e) {
                    /* should not be thrown */
                }
            }
        }
        return false;

    } // end of method checkPortAvailable

    public void setBacnetConfiguration(Properties prop) throws Exception {

        String bacnetFile = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/bacnet.conf";
        boolean portValid = true;
        String desc = "Bacnet configuration changed - " + prop.toString();
        try {
            SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext
                    .getBean("systemConfigurationManager");
            SystemConfiguration tempConfig = null;
            if (prop.containsKey("VendorId")) {
              if(logger.isInfoEnabled()) {
                logger.info("vendor id from gui - " + prop.getProperty("VendorId"));
              }
                tempConfig = sysMgr.loadConfigByName("bacnet.vendor_id");
                tempConfig.setValue(prop.getProperty("VendorId"));
                sysMgr.save(tempConfig);
            }

            if (prop.containsKey("ListenPort")) {
                int newPort = Integer.parseInt(prop.getProperty("ListenPort"));
                if(logger.isInfoEnabled()) {
                  logger.info("server port from gui - " + newPort);
                }
                tempConfig = sysMgr.loadConfigByName("bacnet.server_port");
                if (!tempConfig.getValue().equals(prop.getProperty("ListenPort"))) {
                    // port is changed
                    portValid = checkPortAvailable(newPort);
                }
                if (portValid) {
                    tempConfig.setValue(prop.getProperty("ListenPort"));
                    sysMgr.save(tempConfig);
                } else {
                    prop.put("ListenPort", tempConfig.getValue());
                }
            }

            if (prop.containsKey("NetworkId")) {
              if(logger.isInfoEnabled()) {
                logger.info("network id from gui - " + prop.getProperty("NetworkId"));
              }
                tempConfig = sysMgr.loadConfigByName("bacnet.network_id");
                tempConfig.setValue(prop.getProperty("NetworkId"));
                sysMgr.save(tempConfig);
            }

            if (prop.containsKey("MaxAPDU")) {
              if(logger.isInfoEnabled()) {
                logger.info("max APDU from gui - " + prop.getProperty("MaxAPDU"));
              }
                tempConfig = sysMgr.loadConfigByName("bacnet.max_APDU_length");
                tempConfig.setValue(prop.getProperty("MaxAPDU"));
                sysMgr.save(tempConfig);
            }

            if (prop.containsKey("APDUTimeout")) {
                int timeOut = Integer.parseInt(prop.getProperty("APDUTimeout"));
                if(logger.isInfoEnabled()) {
                  logger.info("APDU timeout from gui - " + timeOut);
                }
                timeOut *= 1000;
                tempConfig = sysMgr.loadConfigByName("bacnet.APDU_timeout");
                tempConfig.setValue(Integer.toString(timeOut));
                sysMgr.save(tempConfig);
                prop.put("APDUTimeout", Integer.toString(timeOut));
            }

            if (prop.containsKey("DeviceBaseInstance")) {
              if(logger.isInfoEnabled()) {
                logger.info("device base instance from gui - " + prop.getProperty("DeviceBaseInstance"));
              }
                tempConfig = sysMgr.loadConfigByName("bacnet.device_base_instance");
                tempConfig.setValue(prop.getProperty("DeviceBaseInstance"));
                sysMgr.save(tempConfig);
            }
            FileOutputStream outStream = new FileOutputStream(bacnetFile);
            prop.store(outStream, "Bacnet configuration properties");
            eventMgr.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
            startBacnetService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!portValid) {
            throw new Exception("Bacnet server port is not available.");
        }

    } // end of method setBacnetConfiguration

    // this is the work class for handling packets received from Bacnet
    public class BacnetResponseWork implements Runnable {

        byte[] packet = null;

        public BacnetResponseWork(byte[] pkt) {

            this.packet = pkt;

        } // end of method GwResponseWork

        public void run() {

            try {
                processBacnetPacket(packet);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end of method run

    } // end of class BacnetResponseWork

    private void fillBacnetHeader(byte[] bacnetResp, byte msgType, byte result) {

        bacnetResp[BACNET_MSG_TYPE_POS] = BACNET_CMD_TYPE_RESP;
        bacnetResp[BACNET_MSG_VERSION_POS] = BACNET_CMD_VER1; // version
        // length 2 bytes
        ServerUtil.fillShortInByteArray(bacnetResp.length, bacnetResp, BACNET_MSG_LEN_POS);
        // transaction id 4 bytes
        ServerUtil.fillIntInByteArray(DeviceServiceImpl.getNextSeqNo(), bacnetResp, BACNET_MSG_SEQ_NO_POS);
        bacnetResp[BACNET_MSG_CMD_POS] = msgType; // command
        bacnetResp[BACNET_RES_MSG_RESULT_POS] = result; // result
        // reserved 2 bytes
        ServerUtil.fillShortInByteArray(0, bacnetResp, BACNET_MSG_RESERVED_POS);

    } // end of method fillBacnetHeader

    public class SUDataResponse {

        String version;
        byte lightStatus;
        int load;
        byte occStatus;

    } // end of class SUDataResponse

    public int fillSUData(byte[] responsePkt, int fillPos, Fixture fixture) {

        String fixtureState = fixture.getState();
        byte state = FIXTURE_STATE_COMMISSIONED;
        if (fixtureState.equals(ServerConstants.FIXTURE_STATE_DISCOVER_STR)) {
            state = FIXTURE_STATE_DISCOVERED;
        } else if (fixtureState.equals(ServerConstants.FIXTURE_STATE_DELETED_STR)) {
            state = FIXTURE_STATE_DELETED;
        }

        String version = fixture.getVersion();
        byte[] verArr = version.getBytes();
        int noOfVerBytes = verArr.length;
        int load = fixture.getWattage();
        byte lightStatus = (byte) (fixture.getDimmerControl() > 0 ? 1 : 0);
        byte occStatus = (byte) (fixture.getLastOccupancySeen() <= 300 ? 1 : 0);
        short dimValue = fixture.getDimmerControl().shortValue();
        short temperature = fixture.getAvgTemperature();

        // fixture state 1 byte
        responsePkt[fillPos++] = state;
        // version 16 bytes
        System.arraycopy(verArr, 0, responsePkt, fillPos, noOfVerBytes);
        for (int j = noOfVerBytes; j < VERSION_LEN; j++) {
            responsePkt[fillPos + j] = 0;
        }
        fillPos += VERSION_LEN;
        // light status 1 byte
        responsePkt[fillPos++] = lightStatus;
        // load 2 bytes
        ServerUtil.fillShortInByteArray(load, responsePkt, fillPos);
        fillPos += 2;
        // occupancy status 1 byte
        responsePkt[fillPos++] = occStatus;
        // dim value 2 bytes
        ServerUtil.fillShortInByteArray(dimValue, responsePkt, fillPos);
        fillPos += 2;
        // temperature 2 bytes
        ServerUtil.fillShortInByteArray(temperature, responsePkt, fillPos);
        fillPos += 2;
        return fillPos;

    } // end of method fillSUData
    
    public byte[] getSUNames(Fixture fixture) {
      
      //sensor name
      String fixtName = fixture.getFixtureName();
      byte[] fixtureNameArr = fixtName.getBytes();          
      int fixtNameLen = fixtureNameArr.length;
      
      //location
      String location = fixture.getLocation();
      byte[] locationArr = location.getBytes();
      int locLen = locationArr.length;
      
      //area name
      Area area = fixture.getArea();
      String areaName = "";
      if(area != null) {
	try {
	  areaName = areaMgr.getAreaById(area.getId()).getName();
	}
	catch(Exception e){
	  e.printStackTrace();
	}
      }               
      byte[] areaNameArr = areaName.getBytes();
      int areaLen = areaNameArr.length;      
      
      int namesLen = fixtNameLen + locLen + areaLen + 6;
      byte[] namesArr = new byte[namesLen];
      
      //fixture name
      if(fixtNameLen > 127) {
	fixtNameLen = fixtNameLen - 256;
      }	 
      int fillPos = 0;
      namesArr[fillPos++] = (byte)fixtNameLen;
      System.arraycopy(fixtureNameArr, 0, namesArr, fillPos, fixtNameLen);
      fillPos += fixtureNameArr.length;
      namesArr[fillPos++] = 0;
      	
      //location      
      if(locLen > 127) {
	locLen = locLen - 256;
      }
      namesArr[fillPos++] = (byte)locLen;          
      System.arraycopy(locationArr, 0, namesArr, fillPos, locLen);
      fillPos += locLen;
      namesArr[fillPos++] = 0;          
      	
      //area name      
      if(areaLen > 127) {
	areaLen = areaLen - 256;
      }
      namesArr[fillPos++] = (byte)areaLen;
      System.arraycopy(areaNameArr, 0, namesArr, fillPos, areaLen);
      fillPos += areaLen;  
      namesArr[fillPos++] = 0;
      return namesArr;
      
  } // end of method fillSUDetails

    // this is the function to parse the packet received from the bacnet
    public void processBacnetPacket(byte[] bacnetPkt) {

        if (fixtureMgr == null) {
            try {
                fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
            } catch (NullPointerException npe) {
            }
        }
        byte[] responsePkt = null;
        try {
            // parse bacnet packet
            int msgType = (bacnetPkt[BACNET_MSG_CMD_POS] & 0xFF);
            switch (msgType) {
            case BACNET_MSGTYPE_SU_DISCOVER:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet discover packet - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                List<Fixture> fixtureList = fixtureMgr.getAllCommissionedFixtureList();
                int noOfDevices = 0;
                if (fixtureList != null) {
                    noOfDevices = fixtureList.size();
                }
                responsePkt = new byte[BACNET_HEADER_LEN + 2 + noOfDevices * 8];
                byte result = BACNET_RES_SUCCESS;
                if (noOfDevices == 0) {
                    result = BACNET_RES_NO_DEVICES;
                }
                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DISCOVER, result);
                if(logger.isInfoEnabled()) {
                  logger.info("bacnet: no. of commssioned fixtures - " + noOfDevices);
                }
                // add no. of devices to response packet
                ServerUtil.fillShortInByteArray(noOfDevices, responsePkt, BACNET_DATA_POS);
                // add devices to response packet
                if (noOfDevices != 0) {
                    Iterator<Fixture> fixtureIter = fixtureList.iterator();
                    int i = BACNET_DATA_POS + 2;
                    while (fixtureIter.hasNext()) {
                        Fixture fixt = fixtureIter.next();
                        ServerUtil.fillIntInByteArray(fixt.getId().intValue(), responsePkt, i);
                        i += 4;
                        byte[] snapByteArr = ServerUtil.getSnapAddr(fixt.getSnapAddress());
                        System.arraycopy(snapByteArr, 0, responsePkt, i, snapByteArr.length);
                        i += 3;
                        if (fixt.getState().equals(ServerConstants.FIXTURE_STATE_DISCOVER_STR)) {
                            responsePkt[i++] = FIXTURE_STATE_DISCOVERED;
                        } else if (fixt.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
                            responsePkt[i++] = FIXTURE_STATE_COMMISSIONED;
                        } else { // fixture is deleted
                            responsePkt[i++] = FIXTURE_STATE_DELETED;
                        }
                    }
                }
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_SU_DATA_MAC:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet su data packet(id) - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                int noOfNodesReq = ServerUtil.extractShortFromByteArray(bacnetPkt, BACNET_DATA_POS);
                int addrPos = BACNET_DATA_POS + 2;
                responsePkt = new byte[BACNET_HEADER_LEN + 2 + (noOfNodesReq * 33)];
                int fillPos = BACNET_DATA_POS;
                // add no. of devices to the response packet
                ServerUtil.fillShortInByteArray(noOfNodesReq, responsePkt, fillPos);
                fillPos += 2;
                int noOfSuccessDevices = 0;
                for (int k = 0; k < noOfNodesReq; k++) {
                    byte[] snapAddrArr = { bacnetPkt[addrPos], bacnetPkt[addrPos + 1], bacnetPkt[addrPos + 2] };
                    addrPos += 3;
                    String snapAddr = ServerUtil.getSnapAddr(snapAddrArr);
                    Fixture fixture = FixtureCache.getInstance().getDeviceFixture(snapAddr);
                    if (fixture == null) {
                        responsePkt[fillPos++] = BACNET_RES_DEVICE_NOT_FOUND;
                        ServerUtil.fillIntInByteArray(0, responsePkt, fillPos);
                    } else {
                        // result 1 byte
                        responsePkt[fillPos++] = BACNET_RES_SUCCESS;
                        ServerUtil.fillLongAsIntInByteArray(fixture.getId(), responsePkt, fillPos);
                        noOfSuccessDevices++;
                    }
                    fillPos += 4;
                    // address 3 bytes
                    System.arraycopy(snapAddrArr, 0, responsePkt, fillPos, 3);
                    fillPos += 3;
                    if (fixture == null) {
                        // fill zeros
                        for (int i = 0; i < 19; i++) {
                            responsePkt[fillPos++] = 0;
                        }
                    } else {
                        fillPos = fillSUData(responsePkt, fillPos, fixture);
                    }
                }
                result = BACNET_RES_SUCCESS;
                if (noOfSuccessDevices == 0) {
                    result = BACNET_RES_NO_DEVICES;
                } else if (noOfSuccessDevices < noOfNodesReq) {
                    result = BACNET_RES_PARTIAL;
                }

                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DATA_MAC, result);
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_SU_DATA_ID:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet su data packet(id) - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                noOfNodesReq = ServerUtil.extractShortFromByteArray(bacnetPkt, BACNET_DATA_POS);
                addrPos = BACNET_DATA_POS + 2;
                responsePkt = new byte[BACNET_HEADER_LEN + 2 + (noOfNodesReq * 33)];
                fillPos = BACNET_DATA_POS;
                // add no. of devices to the response packet
                ServerUtil.fillShortInByteArray(noOfNodesReq, responsePkt, fillPos);
                fillPos += 2;
                noOfSuccessDevices = 0;
                for (int k = 0; k < noOfNodesReq; k++) {
                    long nodeId = ServerUtil.extractIntAsLongFromByteArray(bacnetPkt, addrPos);
                    addrPos += 4;
                    Fixture fixture = fixtureMgr.getFixtureById(nodeId);
                    if (fixture == null) {
                        responsePkt[fillPos++] = BACNET_RES_DEVICE_NOT_FOUND;
                    } else {
                        // result 1 byte
                        responsePkt[fillPos++] = BACNET_RES_SUCCESS;
                        noOfSuccessDevices++;
                    }
                    ServerUtil.fillLongAsIntInByteArray(nodeId, responsePkt, fillPos);
                    fillPos += 4;
                    if (fixture == null) {
                        // fill zeros
                        for (int i = 0; i < 22; i++) {
                            responsePkt[fillPos++] = 0;
                        }
                    } else {
                        // address 3 bytes
                        String snapAddr = fixture.getSnapAddress();
                        System.arraycopy(ServerUtil.getSnapAddr(snapAddr), 0, responsePkt, fillPos, 3);
                        fillPos += 3;
                        fillPos = fillSUData(responsePkt, fillPos, fixture);
                    }
                }
                result = BACNET_RES_SUCCESS;
                if (noOfSuccessDevices == 0) {
                    result = BACNET_RES_NO_DEVICES;
                } else if (noOfSuccessDevices < noOfNodesReq) {
                    result = BACNET_RES_PARTIAL;
                }
                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DATA_ID, result);
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_SU_DIM_MAC:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet su dim packet(mac) - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                // assumption here is at a time bacnet can send only one device and that to blocking
                int pktPos = BACNET_DATA_POS + 2;
                byte[] snapAddrArr = { bacnetPkt[pktPos++], bacnetPkt[pktPos++], bacnetPkt[pktPos++] };
                int dimType = bacnetPkt[pktPos++];
                int percentage = ServerUtil.extractShortFromByteArray(bacnetPkt, pktPos);
                pktPos += 2;
                String snapAddr = ServerUtil.getSnapAddr(snapAddrArr);
                Fixture fixture = fixtureMgr.getFixtureBySnapAddr(snapAddr);
                result = BACNET_RES_SUCCESS;
                if (fixture == null) {
                    result = BACNET_RES_NO_DEVICES;
                    dimResult = BACNET_RES_DEVICE_NOT_FOUND;
                }

                responsePkt = new byte[BACNET_HEADER_LEN + 2 + 4];
                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DIM_MAC, result);
                fillPos = BACNET_DATA_POS;
                ServerUtil.fillShortInByteArray(1, responsePkt, fillPos);
                fillPos += 2;
                // result
                if (dimResult != BACNET_RES_DEVICE_NOT_FOUND) {
                    dimResult = BACNET_RES_DIM_TIMEOUT;
                    // fixture is not found so send the error response
                    bacnetDimFixture = fixture;
                    if (dimType == 1) { // relative dimming
                        DeviceServiceImpl.getInstance().dimFixture(fixture.getId().intValue(), percentage, 0);
                    } else { // absolute dimming
                        fixtureMgr.dimFixture(fixture.getId().intValue(), percentage, 0);
                    }
                    try {
                        bacnetDimLock.wait(3 * 1000);
                    } catch (Exception ex) {
                        logger.error(fixture.getFixtureName() + ": no response for bacnet dim command");
                    }
                }
                responsePkt[fillPos++] = dimResult;
                // address 3 bytes
                System.arraycopy(snapAddrArr, 0, responsePkt, fillPos, 3);
                fillPos += 3;
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_SU_DIM_ID:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet su dim packet(id)  - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                // assumption here is at a time bacnet can send only one device and that to blocking
                pktPos = BACNET_DATA_POS + 2;
                long nodeId = ServerUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
                pktPos += 4;
                dimType = bacnetPkt[pktPos++];
                percentage = ServerUtil.extractShortFromByteArray(bacnetPkt, pktPos);
                pktPos += 2;
                fixture = fixtureMgr.getFixtureById(nodeId);
                result = BACNET_RES_SUCCESS;
                if (fixture == null) {
                    result = BACNET_RES_NO_DEVICES;
                    dimResult = BACNET_RES_DEVICE_NOT_FOUND;
                }

                responsePkt = new byte[BACNET_HEADER_LEN + 2 + 5];
                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DIM_ID, result);
                fillPos = BACNET_DATA_POS;
                ServerUtil.fillShortInByteArray(1, responsePkt, fillPos);
                fillPos += 2;
                // result
                if (dimResult != BACNET_RES_DEVICE_NOT_FOUND) {
                    dimResult = BACNET_RES_DIM_TIMEOUT;
                    // fixture is not found so send the error response
                    bacnetDimFixture = fixture;
                    if (dimType == 1) { // relative dimming
                        DeviceServiceImpl.getInstance().dimFixture(fixture.getId().intValue(), percentage, 0);
                    } else { // absolute dimming
                        fixtureMgr.dimFixture(fixture.getId().intValue(), percentage, 0);
                    }
                    try {
                        bacnetDimLock.wait(3 * 1000);
                    } catch (Exception ex) {
                        logger.error(fixture.getFixtureName() + ": no response for bacnet dim command");
                    }
                }
                responsePkt[fillPos++] = dimResult;
                // id 4 bytes
                ServerUtil.fillLongAsIntInByteArray(nodeId, responsePkt, fillPos);
                fillPos += 4;
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_GEMS_DATA:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet gems data - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                responsePkt = new byte[BACNET_HEADER_LEN + VERSION_LEN];
                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_GEMS_DATA, BACNET_RES_SUCCESS);
                fillPos = BACNET_DATA_POS;
                String gemsVer = ServerMain.getInstance().getGemsVersion() + "."
                        + ServerMain.getInstance().getGemsBuildVersion();
                byte[] verBytes = gemsVer.getBytes();
                System.arraycopy(verBytes, 0, responsePkt, fillPos, verBytes.length);
                for (int i = verBytes.length; i < VERSION_LEN; i++) {
                    responsePkt[fillPos + i] = 0;
                }
                fillPos += VERSION_LEN;
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_DR:
            if(logger.isInfoEnabled()) {
                logger.info("bacnet DR command - " + ServerUtil.getLogPacket(bacnetPkt));
            }
                pktPos = BACNET_DATA_POS;
                byte enabled = bacnetPkt[pktPos++];
                if (enabled == 1) { // DR enable
                    percentage = ServerUtil.extractShortFromByteArray(bacnetPkt, pktPos);
                    pktPos += 2;
                    // int duration = 60;
                    int duration = ServerUtil.extractShortFromByteArray(bacnetPkt, pktPos);
                    pktPos += 2;
                    DeviceServiceImpl.getInstance().executeDR(percentage, duration);
                } else {
                    DeviceServiceImpl.getInstance().cancelDR();
                }

                result = BACNET_RES_SUCCESS;
                responsePkt = new byte[BACNET_HEADER_LEN];
                fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_DR, result);
                sendBacnetPacket(responsePkt);
                break;
            case BACNET_MSGTYPE_GW_DISCOVER:
            if(logger.isInfoEnabled()) {
            	logger.info("bacnet gateway discover packet - " + ServerUtil.getLogPacket(bacnetPkt));
            }
            	sendGwDiscoveryResponse();
            	break;
            case BACNET_MSGTYPE_GW_SU_DISCOVER:
            if(logger.isInfoEnabled()) {
            	logger.info("bacnet get commissioned sensors of gw packet - " + ServerUtil.getLogPacket(bacnetPkt));
            }
            	pktPos = BACNET_DATA_POS + 2;
                long gwId = ServerUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
                pktPos += 4;
            	sendGwCommissionedSensorsResponse(gwId);
            	break;
            case BACNET_MSGTYPE_GW_SU_DATA_ID_LIST:
            if(logger.isInfoEnabled()) {
            	logger.info("bacnet get commissioned sensors details of gw packet - " + 
            	    ServerUtil.getLogPacket(bacnetPkt));
            }
        	pktPos = BACNET_DATA_POS + 2;
        	gwId = ServerUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
        	pktPos += 4;
        	sendGwCommissionedSensorsDetailsResponse(gwId);
        	break;            	
            case BACNET_MSGTYPE_GW_DATA:
            if(logger.isInfoEnabled()) {
            	logger.info("bacnet gw data packet(id) - " + ServerUtil.getLogPacket(bacnetPkt));
            }
            	pktPos = BACNET_DATA_POS + 2;
        	gwId = ServerUtil.extractIntAsLongFromByteArray(bacnetPkt, pktPos);
        	pktPos += 4;
            	sendGwDetailsResponse(gwId);
            	break;
            case BACNET_MSGTYPE_SU_DETAILS_ID:
            if(logger.isInfoEnabled()) {
            	logger.info("bacnet su detaiks packet(id) - " + ServerUtil.getLogPacket(bacnetPkt));
            }
            	noOfNodesReq = ServerUtil.extractShortFromByteArray(bacnetPkt, BACNET_DATA_POS);
            	addrPos = BACNET_DATA_POS + 2;   	
            	nodeId = ServerUtil.extractIntAsLongFromByteArray(bacnetPkt, addrPos);
            	fixture = fixtureMgr.getFixtureById(nodeId);            	
            	int namesLen = 6;
            	byte[] namesArr = null;
            	if (fixture != null) {           	 
            	  namesArr = getSUNames(fixture);
            	  namesLen = namesArr.length;
            	} 
            	responsePkt = new byte[BACNET_HEADER_LEN + 2 + 33 + namesLen];            	
            	fillPos = BACNET_DATA_POS;
            	// add no. of devices to the response packet
            	ServerUtil.fillShortInByteArray(1, responsePkt, fillPos);
            	fillPos += 2;   
            	noOfSuccessDevices = 0;
            	if (fixture == null) {
            	  responsePkt[fillPos++] = BACNET_RES_DEVICE_NOT_FOUND;
            	} else {
            	  // result 1 byte
            	  responsePkt[fillPos++] = BACNET_RES_SUCCESS;
            	  noOfSuccessDevices++;            	  
            	} 
            	ServerUtil.fillLongAsIntInByteArray(nodeId, responsePkt, fillPos);
            	fillPos += 4;
            	if (fixture == null) {
            	  // fill zeros
            	  for (int i = 0; i < 28; i++) {
            	    responsePkt[fillPos++] = 0;
            	  }
            	} else {
            	  // address 3 bytes
            	  snapAddr = fixture.getSnapAddress();
            	  System.arraycopy(ServerUtil.getSnapAddr(snapAddr), 0, responsePkt, fillPos, 3);
            	  fillPos += 3;
            	  fillPos = fillSUData(responsePkt, fillPos, fixture);           	  
            	  System.arraycopy(namesArr, 0, responsePkt, fillPos, namesArr.length);                    
            	}            	
            	result = BACNET_RES_SUCCESS;
            	if (noOfSuccessDevices == 0) {
            	  result = BACNET_RES_NO_DEVICES;
            	} else if (noOfSuccessDevices < noOfNodesReq) {
            	  result = BACNET_RES_PARTIAL;
            	}
            	fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DATA_ID, result);
            	sendBacnetPacket(responsePkt);
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
    
    private void sendGwDetailsResponse(long gwId) {
            
      byte[] responsePkt = new byte[BACNET_HEADER_LEN + 2 + 33];
      int fillPos = BACNET_DATA_POS;
      // add no. of devices to the response packet
      ServerUtil.fillShortInByteArray(1, responsePkt, fillPos);
      fillPos += 2;
      int noOfSuccessDevices = 0;
      Gateway gw = null;
      try {
	gw = gwMgr.loadGateway(gwId);
      }
      catch(Exception e) {	
	e.printStackTrace();
      }
      if (gw == null) {	
	responsePkt[fillPos++] = BACNET_RES_DEVICE_NOT_FOUND;
      } else {
	// result 1 byte
	responsePkt[fillPos++] = BACNET_RES_SUCCESS;
	noOfSuccessDevices++;
      }
      ServerUtil.fillLongAsIntInByteArray(gwId, responsePkt, fillPos);
      fillPos += 4;
      if (gw == null) {
	// fill zeros
	for (int i = 0; i < 22; i++) {
	  responsePkt[fillPos++] = 0;
	}
      } else {
	// address 3 bytes
	String snapAddr = gw.getSnapAddress();
	System.arraycopy(ServerUtil.getSnapAddr(snapAddr), 0, responsePkt, fillPos, 3);
	fillPos += 3;
	if(gw.isCommissioned()) {
	  responsePkt[fillPos++] = FIXTURE_STATE_COMMISSIONED;
	} else {
	  responsePkt[fillPos++] = FIXTURE_STATE_DISCOVERED;
	}
	// version 16 bytes
	String version = gw.getApp2Version();
        byte[] verArr = version.getBytes();
        int noOfVerBytes = verArr.length;
        System.arraycopy(verArr, 0, responsePkt, fillPos, noOfVerBytes);
        for (int j = noOfVerBytes; j < VERSION_LEN; j++) {
            responsePkt[fillPos + j] = 0;
        }
        fillPos += VERSION_LEN;      
      }
      byte result = BACNET_RES_SUCCESS;
      if (noOfSuccessDevices == 0) {
          result = BACNET_RES_NO_DEVICES;
      } else if (noOfSuccessDevices < 1) {
          result = BACNET_RES_PARTIAL;
      }
      fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DATA_ID, result);
      sendBacnetPacket(responsePkt);
      
    } //end of method sendGwDetailsResponse
        
    private void sendGwDiscoveryResponse() {
            
      List<Gateway> gwList = gwMgr.loadCommissionedGateways();
      int noOfDevices = 0;
      if (gwList != null) {
          noOfDevices = gwList.size();
      }
      byte[] responsePkt = new byte[BACNET_HEADER_LEN + 2 + noOfDevices * 8];
      byte result = BACNET_RES_SUCCESS;
      if (noOfDevices == 0) {
          result = BACNET_RES_NO_DEVICES;
      }
      fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_GW_DISCOVER, result);
      if(logger.isInfoEnabled()) {
	logger.info("bacnet: no. of commssioned gateways - " + noOfDevices);
      }
      // add no. of devices to response packet
      ServerUtil.fillShortInByteArray(noOfDevices, responsePkt, BACNET_DATA_POS);
      // add devices to response packet
      if (noOfDevices != 0) {
          Iterator<Gateway> gwIter = gwList.iterator();
          int i = BACNET_DATA_POS + 2;
          while (gwIter.hasNext()) {
              Gateway gw = gwIter.next();
              ServerUtil.fillIntInByteArray(gw.getId().intValue(), responsePkt, i);
              i += 4;
              byte[] snapByteArr = ServerUtil.getSnapAddr(gw.getSnapAddress());
              System.arraycopy(snapByteArr, 0, responsePkt, i, snapByteArr.length);
              i += 3;
              responsePkt[i++] = FIXTURE_STATE_COMMISSIONED;               
          }
      }
      sendBacnetPacket(responsePkt);
      
    } //end of method sendGwDiscoveryResponse
    
    private void sendGwCommissionedSensorsDetailsResponse(long gwId) {
            
      List<Fixture> fixtureList = fixtureMgr.getAllCommissionedFixturesBySecGwId(gwId);
      int noOfDevices = 0;
      if (fixtureList != null) {
          noOfDevices = fixtureList.size();
      }
      
      byte[] responsePkt = new byte[BACNET_HEADER_LEN + 2 + (noOfDevices * 33)];
      int fillPos = BACNET_DATA_POS;
      // add no. of devices to the response packet
      ServerUtil.fillShortInByteArray(noOfDevices, responsePkt, fillPos);
      fillPos += 2;
      int noOfSuccessDevices = 0;
      for (int k = 0; k < noOfDevices; k++) {
	Fixture fixture = fixtureList.get(k);	
	// result 1 byte
	responsePkt[fillPos++] = BACNET_RES_SUCCESS;
	noOfSuccessDevices++;	
	ServerUtil.fillLongAsIntInByteArray(fixture.getId(), responsePkt, fillPos);
	fillPos += 4;
	// address 3 bytes
	String snapAddr = fixture.getSnapAddress();
	System.arraycopy(ServerUtil.getSnapAddr(snapAddr), 0, responsePkt, fillPos, 3);
	fillPos += 3;
	fillPos = fillSUData(responsePkt, fillPos, fixture);	
      }
      byte result = BACNET_RES_SUCCESS;
      if (noOfSuccessDevices == 0) {
          result = BACNET_RES_NO_DEVICES;
      } else if (noOfSuccessDevices < noOfDevices) {
          result = BACNET_RES_PARTIAL;
      }
      fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_SU_DATA_ID, result);   
      sendBacnetPacket(responsePkt);
      
    } //end of method sendGwCommissionedSensorsDetailsResponse
    
    private void sendGwCommissionedSensorsResponse(long gwId) {
      
      List<Fixture> fixtureList = fixtureMgr.getAllCommissionedFixturesBySecGwId(gwId);
      int noOfDevices = 0;
      if (fixtureList != null) {
          noOfDevices = fixtureList.size();
      }
      byte[] responsePkt = new byte[BACNET_HEADER_LEN + 2 + noOfDevices * 8];
      byte result = BACNET_RES_SUCCESS;
      if (noOfDevices == 0) {
          result = BACNET_RES_NO_DEVICES;
      }
      fillBacnetHeader(responsePkt, (byte) BACNET_MSGTYPE_GW_SU_DISCOVER, result);
      if(logger.isInfoEnabled()) {
	logger.info("no. of commssioned fixtures associated with gateway - " + gwId + " are " + noOfDevices);
      }
      // add no. of devices to response packet
      ServerUtil.fillShortInByteArray(noOfDevices, responsePkt, BACNET_DATA_POS);
      // add devices to response packet
      if (noOfDevices != 0) {
          Iterator<Fixture> fixtureIter = fixtureList.iterator();
          int i = BACNET_DATA_POS + 2;
          while (fixtureIter.hasNext()) {
              Fixture fixt = fixtureIter.next();
              ServerUtil.fillIntInByteArray(fixt.getId().intValue(), responsePkt, i);
              i += 4;
              byte[] snapByteArr = ServerUtil.getSnapAddr(fixt.getSnapAddress());
              System.arraycopy(snapByteArr, 0, responsePkt, i, snapByteArr.length);
              i += 3;
              responsePkt[i++] = FIXTURE_STATE_COMMISSIONED;            
          }
      }
      sendBacnetPacket(responsePkt);
      
    } //end of method sendGwCommissionedSensorsResponse

    private byte dimResult = BACNET_RES_DIM_TIMEOUT;
    private Object bacnetDimLock = new Object();
    private Fixture bacnetDimFixture = null;

    public void receivedDimAck(Fixture fixture) {

        if (bacnetDimFixture == null || bacnetDimFixture.getId().longValue() != fixture.getId().longValue()) {
            return;
        }
        try {
            synchronized (bacnetDimLock) {
                bacnetDimLock.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in notifying in receivedGwWirelessChangeAck");
        }
        dimResult = BACNET_RES_SUCCESS;

    } // end of method receivedDimAck

    public void sendBacnetPacket(byte[] pkt) {

      if(logger.isInfoEnabled()) {
        logger.info("bacnet response packet - " + ServerUtil.getLogPacket(pkt));
      }

        int port = BACNET_SERVER_PORT;
        DatagramSocket gwSockConn = null;
        try {
            gwSockConn = new DatagramSocket();
            gwSockConn.send(new DatagramPacket(pkt, 0, pkt.length, new InetSocketAddress("127.0.0.1", port)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (gwSockConn != null) {
                try {
                    gwSockConn.close();
                } catch (Exception ex) {
                }
            }
        }

    } // end of method sendBacnetPacket

    // this is the daemon class listening for packets from bacnet
    public class BacnetListenerDaemon extends Thread {

        byte[] rcvByteArr = new byte[128];
        DatagramSocket serverSocket = null;

        public BacnetListenerDaemon() {
        }

        public void run() {

          if(logger.isInfoEnabled()) {
            logger.info("run of backnet ");
          }
            try {
                serverSocket = new DatagramSocket(BACNET_GEMS_LISTENER_PORT, InetAddress.getLocalHost());
                serverSocket.setReceiveBufferSize(2048);
                if(logger.isDebugEnabled()) {
                  logger.debug("bacnet listener socket started");
                }
                while (ServerMain.getInstance().isRunning()) {
                    try {
                        DatagramPacket dp = new DatagramPacket(rcvByteArr, rcvByteArr.length);
                        serverSocket.receive(dp);
                        if(logger.isInfoEnabled()) {
                          logger.info("bacnet packet - " + ServerUtil.getLogPacket(rcvByteArr));
                        }
                        int pktLen = ServerUtil.extractShortFromByteArray(rcvByteArr, BACNET_MSG_LEN_POS);
                        byte[] pkt = new byte[pktLen];
                        System.arraycopy(rcvByteArr, 0, pkt, 0, pktLen);
                        BacnetResponseWork work = new BacnetResponseWork(pkt);
                        threadPool.addWork(work);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end of method run

        public void cleanUp() {
          if(logger.isInfoEnabled()) {
            logger.info("closing bacnet port ");
          }
            try {
                // serverSocket.disconnect();
                serverSocket.close();
                if(logger.isInfoEnabled()) {
                  logger.info("Closed the bacnet socket");
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }

        }

    } // end of class BacnetListenerDaemon

    // this is not being used right now
    public class BacnetResponse {

        int msgType;
        byte[] bacnetResp = null;

        public BacnetResponse(int msg, int noOfDevices) {

            int len = BACNET_HEADER_LEN + 2;
            switch (msg) {
            case BACNET_MSGTYPE_SU_DISCOVER:
                len += noOfDevices * 3;
                break;
            case BACNET_MSGTYPE_SU_DATA_MAC:
                len += noOfDevices * 19;
                break;
            }
            bacnetResp = new byte[len];
            fillBacnetHeader(bacnetResp, (byte) msg, (byte) 0);

        } // end of constructor

        public byte[] getBacnetResponse() {

            return bacnetResp;

        } // end of method getBacnetResponse

    } // end of class BacnetResponse

    public void cleanUp() {

        try {
            stopBacnetService();
            if(logger.isDebugEnabled()) {
              logger.debug("bacnet services stopped");
            }
            bacnetListenerDaemon.cleanUp();

            if(logger.isInfoEnabled()) {
              logger.info("Bacnet services stopped succcessfully");
            }
        } catch (Throwable th) {

        } finally {
          if(logger.isDebugEnabled()) {
            logger.debug("bacnet thread pool stopped");
          }

        }
    }

} // end of class BacnetService
