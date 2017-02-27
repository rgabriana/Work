/**
 * 
 */
package com.ems.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.FixtureCache;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.GwStats;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.GwStatsManager;
import com.ems.service.GatewayManager;

/**
 * @author sreedhar
 *
 */
public class GwStatsSO {

  private static GwStatsSO instance = null;
      
  private Timer gwStatsTimer = new Timer("Gateway Stats Buckets Timer", true);
  private int gwBucketInterval = 5 * 60 * 1000; //5 minutes
  private int gwPingInterval = 30 * 1000; //30 seconds
  private long gwPingCounter = 0;
  
  private Timer gwPingTimer = new Timer("Gateway Ping Timer", true);
  
  private static final Logger logger = Logger.getLogger("Perf");
  
  private GatewayManager gwMgr = null;
  private GwStatsManager gwStatsMgr = null;
  private EventsAndFaultManager eventMgr = null;
  private FixtureManager fixtureMgr = null;
  
  /**
   * 
   */
  private GwStatsSO() {
    
    gwStatsMgr = (GwStatsManager)SpringContext.getBean("gwStatsManager");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
    loadLastGwStatsFromDB();
    //startGwStatsBucketsTask();
    startGwPingTask();
    
  } //end of constructor

  public static GwStatsSO getInstance() {
    
    if(instance == null) {
      synchronized(GwStatsSO.class) {
	if(instance == null) {
	  instance = new GwStatsSO();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  private void loadLastGwStatsFromDB() {
    
    List<GwStats> gwStatsList = gwStatsMgr.loadLastGwStatsFromDB();
    Iterator<GwStats> gwStatsIter = gwStatsList.iterator();
    GwStats gwStats = null;
    
    while(gwStatsIter.hasNext()) {
      try {
	gwStats = gwStatsIter.next();
	long gwId = gwStats.getGwId();      
	GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gwId);
	gwInfo.setUptime(0);
	gwInfo.setPktsFromGems(0);
	gwInfo.setPktsFromNodes(0);
	gwInfo.setPktsToGems(0);
	gwInfo.setPktsToNodes(0);	
	gwInfo.setUptime(gwStats.getUptime());	
	gwInfo.setPktsFromGems(gwStats.getNoPktsFromGems());
	gwInfo.setPktsFromNodes(gwStats.getNoPktsFromNodes());	
	gwInfo.setPktsToGems(gwStats.getNoPktsToGems());	
	gwInfo.setPktsToNodes(gwStats.getNoPktsToNodes());	
      }
      catch(Exception ex) {
	logger.error(gwStats.getId() + ": Error retrieving the current stats from db - " + ex.getMessage());
      }
    }   
    
  } //end of method loadLastGwStatsFromDB
  
  private void startGwPingTask() {
    
    new Thread() {
      public void run() {
	try {	   
	  if(logger.isDebugEnabled()) {
	    logger.debug("starting the Gw ping task");
	  }
	  GwPingTask gwPingTask = new GwPingTask(); 	    
	  gwPingTimer.scheduleAtFixedRate(gwPingTask, 0, gwPingInterval);	    
	}
	catch(Exception ex) {
	  ex.printStackTrace();
	}	
	
      } //end of method run
    }.start();
    
  } //end of method startGwPingTask
    
  private void startGwStatsBucketsTask() {
    
    new Thread() {
      public void run() {
	
	while(true) {
	  try {	     
	    if(ServerUtil.getCurrentMin() % 5 == 0) { //start at the 5th/10th/15th... minute
	      if(logger.isDebugEnabled()) {
		logger.debug("starting the Gw Stats buckets task");
	      }
	      GwStatsCollectionTask gwStatsCollectionTask = new GwStatsCollectionTask(); 	    
	      gwStatsTimer.scheduleAtFixedRate(gwStatsCollectionTask, 0, gwBucketInterval);
	      return;
	    }
	    Thread.sleep(1000); //sleep for 1 sec
	  }
	  catch(Exception ex) {
	    ex.printStackTrace();
	  }
	}	
	
      } //end of method run
    }.start();
    
  } //end of method startGwStatsBucketsTask
  
  public class GwStatsCollectionTask extends TimerTask{
    
    public void run() {

      try {
	List<Gateway> gwList = gwMgr.loadAllGateways();
	Iterator<Gateway> gwIter = gwList.iterator();
	Gateway gw = null;
	while(gwIter.hasNext()) {
	  gw = gwIter.next();
	  GatewayImpl.getInstance().sendGatewayInfoReq(gw);
	}
      }
      catch(Exception ex) {
	ex.printStackTrace();
      }
	
    } //end of method run
 
  } //end of class GwStatsCollectionTask
  
  public class GwPingTask extends TimerTask{
    
    public void run() {

      gwPingCounter++;
      try {
	List<Gateway> gwList = gwMgr.loadAllGateways();
	Iterator<Gateway> gwIter = gwList.iterator();
	Gateway gw = null;
	while(gwIter.hasNext()) {
	  gw = gwIter.next();
	  if(!gw.isCommissioned()) {
	    continue;
	  }
	  //ping the gateway. It was required for 1.x nodes. 1.x gateway used to hang when it gets
	  //back to back packets. It was getting recovered through ping.
	  if(ServerUtil.compareVersion(gw.getApp2Version(), "2.0") < 0) {
	    if(gwPingCounter % 10 != 0) {
	      continue;
	    }
	    Process pingProcess = null;
	    try {	      
	      pingProcess = Runtime.getRuntime().exec("ping -c3 " + gw.getIpAddress());	      
	      while(true) {
		try {
		  ServerUtil.sleepMilli(500);
		  pingProcess.exitValue();
		  break;
		}
		catch(Exception e) {
		  //e.printStackTrace();
		}
	      }	      
	    }
	    catch(Exception e){	     
	      e.printStackTrace();
	    }
	    finally {
	      if(pingProcess != null) {		
		pingProcess.getInputStream().close();
		pingProcess.getOutputStream().close();
		pingProcess.getErrorStream().close();
		pingProcess.destroy();
	      }
	    }
	  }
	  /* not pinging to 2.0 gateways
	  boolean pingStatus = InetAddress.getByName(gw.getIpAddress()).isReachable(10);
	  if(pingStatus) {
	    if(logger.isDebugEnabled()) {
	      logger.debug(gw.getIpAddress() + ": is reachable");
	    }
	    //if the gateway is reachable make sure that SSL connection is up
	    SSLSessionManager.getInstance().checkSSLConnection(gw.getId());
	  } else {
	    logger.error(gw.getIpAddress() + ": is not reachable");
	    //remove the ssl connection from the cache
	    SSLSessionManager.getInstance().removeSSLConnection(gw.getId());
	  }
	  */
	}
      }
      catch(Exception ex) {
	ex.printStackTrace();
      }
	
    } //end of method run
 
  } //end of class GwPingTask
  
  public void parseGwInfo(long gwId, byte[] pkt, String gwIp) {
    
    GatewayInfo gwInfo = GatewayImpl.getInstance().getGatewayInfo(gwIp);
    
	  // Question: Do we need a separate cmd for commissioning?
//    if (!gwMgr.isCommissioned(gwId)) {
//    	gwMgr.setCommissionStatus(gwId);
//    }
    //logger.info(gwId + ": gw stats pkt -" + pkt);
    if(logger.isDebugEnabled()) {
      logger.debug(gwId + ": gw stats pkt - " + ServerUtil.getLogPacket(pkt));
    }
    byte[] tempByteArr = new byte[4];
    
    //update the gateway info parameters
    final Gateway gw = gwMgr.loadGateway(gwId);
    //clear the unreachable alarm
    eventMgr.clearAlarm(gw, EventsAndFault.GW_REACHABLILITY_FAILURE);
    /*
    //byte 0 is message type
    //byte 1, 2, 3, 4 seq no
    //bytes 5, 6, 7, 8, 9, 10 is mac
    //bytes 11, 12, 13, 14 is ip
    gw.setIpAddress(gwIp);
    //bytes 15, 16 ,17, 18 is net mask
    System.arraycopy(pkt, 15, tempByteArr, 0, tempByteArr.length);
    String subnetMask = ServerUtil.convertByteArrToIp(tempByteArr);
    logger.info(gwId + ": subnet mask -- " + subnetMask);
    gw.setSubnetMask(subnetMask);
    //bytes 19, 20, 21, 22 is default gw
    System.arraycopy(pkt, 19, tempByteArr, 0, tempByteArr.length);
    String defaultGw = ServerUtil.convertByteArrToIp(tempByteArr);
    logger.info(gwId + ": default gw -- " + defaultGw);
    gw.setDefaultGw(defaultGw);
    */
    //bytes 23 is channel
    int rcvdChannel = (int)pkt[23];
    //gw.setChannel((int)pkt[23]);
    //byte 24 is radio rate
    int rcvdRadioRate = (int)pkt[24];
    //gw.setWirelessRadiorate((int)pkt[24]);
    //bytes 25 to 41 is key
    byte[] keyArr = new byte[17];
    System.arraycopy(pkt, 25, keyArr, 0, keyArr.length);
    String rcvdKey = new String(keyArr, 0, 16);

    //gw.setWirelessEncryptKey(key);
    //bytes 42, 43 is network id
    byte[] netIdArr = new byte[2];
    System.arraycopy(pkt, 42, netIdArr, 0, netIdArr.length);
    int rcvdNetId = ServerUtil.byteArrayToShort(netIdArr);
    
    //gw.setWirelessNetworkId(netId);    
    //gwMgr.saveGatewayInfo(gw);
    
    //GatewayInfo gwInfo = GatewayImpl.getInstance().getGatewayInfo(gwIp);
    //if gateway is not in commissioning/discovery mode   
    if(gw.isCommissioned() && gwInfo.getOperationalMode() != GatewayInfo.GW_COMMISSIONING_MODE && 
    		gwInfo.getOperationalMode() != GatewayInfo.GW_DISCOVERY_MODE) {
    	//generate an event if received parameters are different from the configured values
    	if(gw.getChannel() != rcvdChannel || !gw.getWirelessEncryptKey().equals(rcvdKey) ||
    			gw.getWirelessNetworkId() != rcvdNetId || gw.getWirelessRadiorate() != rcvdRadioRate) {
    	  if(logger.isInfoEnabled()) {
    	    logger.info(gwId + ": received -- " + rcvdChannel + " " + rcvdNetId + " " + 
    		rcvdRadioRate);
    	    logger.info(gwId + ": configured -- " + gw.getChannel() + " " + 
    		gw.getWirelessNetworkId() + " " + gw.getWirelessRadiorate());
    	  }
    	  eventMgr.addAlarm(gw, "Configured and Current wireless parameters are different", 
    	      EventsAndFault.GW_CONFIG_FAILURE);
    	  new Thread() {
    	    public void run() {
    	      //move the hoppers as well
    	      moveHoppersToConfiguredWireless(gw);
    	      //send a gateway update command
    	      GatewayImpl.getInstance().changeWirelessParams(gw);    
    	    }
    	  }.start();    	  		
    	}
    }
    //bytes 44, 45, 46, 47 is uptime
    System.arraycopy(pkt, 44, tempByteArr, 0, tempByteArr.length);
    long uptime = ServerUtil.intByteArrayToLong(tempByteArr);
    if(logger.isInfoEnabled()) {
      logger.info(gwIp + ": gw up time -- " + uptime);
    }
    
    //bytes 48, 49, 50, 51 is pkts from gems
    System.arraycopy(pkt, 48, tempByteArr, 0, tempByteArr.length);
    long pktsFromGems = ServerUtil.intByteArrayToLong(tempByteArr);
    if(logger.isDebugEnabled()) {
      logger.debug(gwIp + ": pkts from gems -- " + pktsFromGems);
    }
    
    //bytes 52, 53, 54, 55 is pkts to gems
    System.arraycopy(pkt, 52, tempByteArr, 0, tempByteArr.length);
    long pktsToGems = ServerUtil.intByteArrayToLong(tempByteArr);
    if(logger.isDebugEnabled()) {
      logger.debug(gwIp + ": pkts to gems -- " + pktsToGems);
    }
    
    //bytes 56, 57, 58, 59 is pkts to nodes
    System.arraycopy(pkt, 56, tempByteArr, 0, tempByteArr.length);
    long pktsToNodes = ServerUtil.intByteArrayToLong(tempByteArr);
    if(logger.isDebugEnabled()) {
      logger.debug(gwIp + ": pkts to node -- " + pktsToNodes);
    }
    
    //bytes 60, 61, 62, 63 is pkts from nodes
    System.arraycopy(pkt, 60, tempByteArr, 0, tempByteArr.length);
    long pktsFromNodes = ServerUtil.intByteArrayToLong(tempByteArr);
    if(logger.isDebugEnabled()) {
      logger.debug(gwIp + ": pkts from nodes -- " + pktsFromNodes);
    }
        
    if(pkt.length > 64) {
      int i = 64;
      //byte 64
      int releaseNo = pkt[i++];    
      //bytes 65, 66 build no
      byte[] tempShortArr = new byte[2];
      System.arraycopy(pkt, i, tempShortArr, 0, tempShortArr.length);
      int buildNo = ServerUtil.byteArrayToShort(tempShortArr);
      i += 2;    
      //byte 67 major
      int majorVer = pkt[i++];
      //byte 68 minor
      int minorVer = pkt[i++];
      //byte 69 app id
      int currApp = pkt[i++];      
      String gwVer1 = majorVer + "." + minorVer + "." + releaseNo + " b" + buildNo;
            
      //byte 70 other app release no.
      releaseNo = pkt[i++];      
      //byte 71, 72 build no. of other app
      System.arraycopy(pkt, i, tempShortArr, 0, tempShortArr.length);
      buildNo = ServerUtil.byteArrayToShort(tempShortArr);
      i += 2;      
      //byte 73 major of other app
      majorVer = pkt[i++];
      //byte 74 minor of other app
      minorVer = pkt[i++];
      //build no is not added as it is coming as 0
      String gwVer2 = majorVer + "." + minorVer + "." + releaseNo + " b" + buildNo;
      
      //byte 75 major of boot loader
      majorVer = pkt[i++];
      //byte 76 minor of boot loader
      minorVer = pkt[i++];
      String bootLoaderVer = majorVer + "." + minorVer; 
   
      if(currApp == 2) {            
        gw.setVersion(gwVer1);
        gw.setApp1Version(gwVer2);
      } else {
        gw.setApp1Version(gwVer1);   
        gw.setVersion(gwVer2);
      }     
      gw.setBootLoaderVersion(bootLoaderVer);
      //gwMgr.setVersions(gw);
    }
        
    long lastUptime = gwInfo.getUptime();
    if(lastUptime > uptime) { //gw rebooted
      gwInfo.setUptime(0);
      gwInfo.setPktsFromGems(0);
      gwInfo.setPktsFromNodes(0);
      gwInfo.setPktsToGems(0);
      gwInfo.setPktsToNodes(0);
    }
    
    //update the gateway with the current stats
    GwStats currGwStats = new GwStats();
    currGwStats.setGwId(gwId);
    currGwStats.setNoPktsFromGems(pktsFromGems);
    currGwStats.setNoPktsFromNodes(pktsFromNodes);
    currGwStats.setNoPktsToGems(pktsToGems);
    currGwStats.setNoPktsToNodes(pktsToNodes);
    currGwStats.setUptime(uptime);
    //update version in the same call
    gwStatsMgr.updateCurrentGwStats(currGwStats, gw);
    
    long currentMillis = System.currentTimeMillis();
    if(currentMillis - gwInfo.getLastStatsTime().getTime() < PerfSO.FIVE_MINUTE_INTERVAL) {
      return;
    }
    gwInfo.setLastStatsTime(new Date(currentMillis));
    
    //add the gw stats deltas to the db
    GwStats deltaGwStats = new GwStats();
    deltaGwStats.setCaptureAt(new Date());
    deltaGwStats.setGwId(gwId);    
    deltaGwStats.setNoPktsFromGems(pktsFromGems - gwInfo.getPktsFromGems());
    deltaGwStats.setNoPktsFromNodes(pktsFromNodes - gwInfo.getPktsFromNodes());
    deltaGwStats.setNoPktsToGems(pktsToGems - gwInfo.getPktsToGems());
    deltaGwStats.setNoPktsToNodes(pktsToNodes - gwInfo.getPktsToNodes());    
    deltaGwStats.setUptime(uptime);
    gwStatsMgr.save(deltaGwStats);  
        
    //update the cache with current stats
    gwInfo.setUptime(uptime);
    gwInfo.setPktsFromGems(pktsFromGems);
    gwInfo.setPktsFromNodes(pktsFromNodes);
    gwInfo.setPktsToGems(pktsToGems);
    gwInfo.setPktsToNodes(pktsToNodes);

  } //end of method parseGwInfo
  
  private ConcurrentHashMap<Long, Boolean> hopperAckMap = new ConcurrentHashMap<Long, Boolean>();
  
  private void moveHoppersToConfiguredWireless(Gateway gw) {
    
    List<Fixture> hopperList = fixtureMgr.loadHoppersBySecGwId(gw.getId());
    if(hopperList == null) {
      //there are no hoppers on this gateway
      return;
    }
    ArrayList<Integer> hopperArrList = new ArrayList<Integer>();    
    Iterator<Fixture> iter = hopperList.iterator();
    hopperAckMap.clear();
    while(iter.hasNext()) {
      Fixture hopper = iter.next();
      if(ServerUtil.compareVersion(hopper.getVersion(), "2.1") >= 0) {
        hopperArrList.add(hopper.getId().intValue());  	
        hopperAckMap.put(hopper.getId(), false);
      }
    }
    int[] fixtArr = new int[hopperArrList.size()];
    for(int k = 0; k < hopperArrList.size(); k++) {
      fixtArr[k] = hopperArrList.get(k);
    }    
    DeviceServiceImpl.getInstance().setApplyWirelessCustom(fixtArr, gw, (short)0, (byte)1);      
    
    //wait for 4 sec for the acks      
    for(int m = 0; m < 4; m++) {
      ServerUtil.sleep(1);	
      //verify that acks have come from hoppers	   
      Iterator<Long> hopperAckIter = hopperAckMap.keySet().iterator();
      while(hopperAckIter.hasNext()) {
	long hopperId = hopperAckIter.next();
	if(hopperAckMap.get(hopperId) == true) {
	  //ack is received
	  hopperAckIter.remove();
	}
      }
      if(hopperAckMap.size() == 0) {
	break;
      }
    }
    //check for any missing acks for hoppers
    if(hopperAckMap.size() > 0) {
      Iterator<Long> hopperAckIter = hopperAckMap.keySet().iterator();
      while(hopperAckIter.hasNext()) {
	long hopperId = hopperAckIter.next();
	if(hopperAckMap.get(hopperId) == true) {
	  //ack is received
	  hopperAckIter.remove();
	} else {
	  //ack is not received, raise an event
	  Fixture hopper = FixtureCache.getInstance().getDevice(hopperId).getFixture();
	  eventMgr.addEvent(hopper, ServerConstants.hopperConfErrStr, EventsAndFault.GW_CONFIG_FAILURE);
	}
      }
    }    
                
  } //end of method moveHoppersToConfiguredWireless
 
  public void receivedHopperWirelessParamsAck(long hopperId) {
    
    if(hopperAckMap != null && hopperAckMap.containsKey(hopperId)) {
      hopperAckMap.put(hopperId, true);
    }
    
  } //end of method receivedHopperWirelessParamsAck  

} //end of class GwStatsSO
