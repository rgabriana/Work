/**
 * 
 */
package com.ems.server.device;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.server.GatewayInfo;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.util.ServerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;

/**
 * @author sreedhar
 * this class deals with all the functions for enlighted gateway
 */
public class GatewayImpl {

  private static GatewayImpl instance = null;
  
  private Timer gwHealthPollTimer = new Timer("GW Health Poll Timer", true);
  private int gwHealthPollInterval = 10 * 60 * 1000; //10 minutes
  
  private HashMap<String, Long> gwMap = new HashMap<String, Long>();
  
  private GatewayManager gwMgr = null;
  private FixtureManager fixtMgr = null;
  
  private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");

  /**
   * 
   */
  private GatewayImpl() {
    
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    fixtMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
    loadGateways();
    startGwHealthPollTask();
    
  } //end of constructor
  
  public static GatewayImpl getInstance() {
    
    if(instance == null) {
      synchronized(GatewayImpl.class) {
	if(instance == null) {
	  instance = new GatewayImpl();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  public void addGatewayInfo(String gwIp) {
    
    Gateway gw = gwMgr.getGatewayByIp(gwIp);   
    if(ServerMain.getInstance().addGatewayInfo(gw)) {    
      gwMap.put(gw.getIpAddress(), gw.getId());    
    }
    
  } //end of method reloadGatewayMap();
 
  private void loadGateways() {
    
    Iterator<GatewayInfo> gwIter = ServerMain.getInstance().getGwMap().values().iterator();
    GatewayInfo gwInfo = null;
    Gateway gw = null;
    while(gwIter.hasNext()) {
      gwInfo = gwIter.next();
      gw = gwInfo.getGw();
      gwMap.put(gw.getIpAddress(), gw.getId());
    }   
       
  } //end of method loadGateways  
    
  public GatewayInfo getGatewayInfo(String gwIp) {
    
    if(gwMap.containsKey(gwIp)) {
      return ServerMain.getInstance().getGatewayInfo(gwMap.get(gwIp));
    }
    return null;
    
  } //end of method getGatewayInfo
  
  private void startGwHealthPollTask() {
    
    GwHealthPollTask gwHealthPollTask = new GwHealthPollTask();
    if(fixtureLogger.isDebugEnabled()) {
      fixtureLogger.debug("starting gw health poller");
    }
    gwHealthPollTimer.scheduleAtFixedRate(gwHealthPollTask, 0, gwHealthPollInterval);
    
  } //end of method startGwHealthPollTask
  
  public class GwHealthPollTask extends TimerTask{
    
    public void run() {

      try {
	Iterator<String> gwIter = gwMap.keySet().iterator();
	String gwIp = null;
	while(gwIter.hasNext()) {
	  gwIp = gwIter.next();
	  GatewayInfo gwInfo = getGatewayInfo(gwIp);
	  if(gwInfo == null) {
	    //gateway info corresponding to this id is removed from cache. so, remove it from health poll map
	    gwIter.remove();
	    continue;
	  }
	  Date gwTime = gwInfo.getLastConnectivityAt();
	  if(gwTime != null && 
	      (new Date().getTime() - gwTime.getTime()) > PerfSO.TEN_MINUTE_INTERVAL ) {
	    //no packet from this gateway for 10 minutes send an health check packet
	    sendGatewayInfoReq(gwInfo.getGw());	    
	  }
	}
      }
      catch(Exception e) {
	e.printStackTrace();
      }
      
    } //end of method run
    
  } //end of class GwHealthPollTask
  
  //function to build the gateway packet pay load
  public void sendGwPkt(int msgType, Gateway gw, byte[] dataPacket, boolean retryReq) {
    
    byte[] gwPkt = new byte[5 + dataPacket.length];
    gwPkt[0] = (byte)msgType;
    int seqNo = DeviceServiceImpl.getNextSeqNo();
    byte[] seqNoArr = ServerUtil.intToByteArray(seqNo);
    System.arraycopy(seqNoArr, 0, gwPkt, 1, seqNoArr.length);
    System.arraycopy(dataPacket, 0, gwPkt, 5, dataPacket.length);
    SSLSessionManager.getInstance().checkSSLConnection(gw.getId());
    if(retryReq) {
      ServerMain.getInstance().addGatewayCommand(gw, gwPkt);
    }
//    fixtureLogger.debug("sendGwPkt => cmd: " + msgType + " channel: " + gw.getChannel() + " radio rate: "
//			+ gw.getWirelessRadiorate() + " network ID: "
//			+ gw.getWirelessNetworkId());
    if(fixtureLogger.isDebugEnabled()) {
      fixtureLogger.debug(gw.getIpAddress() + ": " + ServerUtil.getLogPacket(gwPkt));
    }
    GatewayComm.getInstance().sendDataToGateway(gw.getId(), gwPkt);
    
  } //end of method sendGwPkt
  
  public void sendGatewayInfoReq(Gateway gw) {
    
    byte[] dataPacket = new byte[0];
    sendGwPkt(ServerConstants.GATEWAY_CMD_INFO, gw, dataPacket, false);
	  
  } //end of method sendGatewayInfoReq
  
  public void sendGatewaySecurityCommand(Gateway gw) {
  	
  	byte[] dataPacket = { 0x7c, (0xa9 - 256), (0x99 - 256), 0x7d, 0x65, 0x6e, 0x6c, 0x69, 0x67 };
  	sendGwPkt(ServerConstants.GATEWAY_SECURITY_CMD, gw, dataPacket, true);
  	
  } //end of method sendGatewaySecurityString
  
  public void rebootGateway(Gateway gw) {
      
    byte[] dataPkt = new byte[1];
    dataPkt[0] = 2; //rebooting to app2       
    sendGwPkt(ServerConstants.GATEWAY_REBOOT_CMD, gw, dataPkt, true);
    
  } //end of method rebootGateway
  
  public void setWirelessFactoryDefaults(Gateway gw) {
    
    byte[] dataPkt = new byte[28];
    int i = 0;
    byte[] macArr = ServerUtil.convertMacToByteArr(gw.getMacAddress());
    System.arraycopy(macArr, 0, dataPkt, i, macArr.length);
    i += 6;
    dataPkt[i++] = 4;
    dataPkt[i++] = (byte)DiscoverySO.getDefaultRadioRate(); 
    //network id    
    byte[] tempShortArr = ServerUtil.shortToByteArray(0x6854);
    System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
    i += 2;
    String key = DeviceServiceImpl.DEFAULT_AES_KEY;
    byte[] keyArr = new byte[17];
    System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
    keyArr[key.length()] = 0;   
    System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
    i += 17;
    dataPkt[i++] = 2; //encryption type
    sendGwPkt(ServerConstants.GATEWAY_WIRELESS_CMD, gw, dataPkt, true);
    
  } //end of method setWirelessFactoryDefaults
    
  //function to change wireless params on gw
  //TODO need to figure out which params are changed and send them only
  public void changeWirelessParams(Gateway gw) {
       
    byte[] dataPkt = new byte[28];
    int i = 0;
    byte[] macArr = ServerUtil.convertMacToByteArr(gw.getMacAddress());
    System.arraycopy(macArr, 0, dataPkt, i, macArr.length);
    i += 6;
    dataPkt[i++] = gw.getChannel().byteValue();
    dataPkt[i++] = gw.getWirelessRadiorate().byteValue(); //-1; //radio rate -- no change so 0xff  
    //network id
    byte[] tempShortArr = ServerUtil.shortToByteArray(gw.getWirelessNetworkId());
    System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
    i += 2;
    String key = gw.getWirelessEncryptKey();
    if (key == null)
    	key = "";
    byte[] keyArr = new byte[17];
    System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
    keyArr[key.length()] = 0;   
    System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
    i += 17;
    byte keyType = gw.getWirelessEncryptType().byteValue();
    if(keyType == 1) {
      keyType = 2;
    }
    dataPkt[i++] = keyType;
    sendGwPkt(ServerConstants.GATEWAY_WIRELESS_CMD, gw, dataPkt, true);
    
  } //end of method changeWirelessParams
  
  private boolean gotGwAck = false;
  private Object suParamUpdLock = new Object();
  
  public void receivedSuWirelessChangeAck(Fixture fixture) {
    
    gotGwAck = true;
    //count for how many we got acks TODO
    try {
      synchronized(suParamUpdLock) {
	suParamUpdLock.notify();
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      fixtureLogger.error("Error in notifying in receivedGwWirelessChangeAck");
    }
    
  } //end of method receivedSuWirelessChangeAck
  
  public void updateGwAndSuParams(Gateway gw) {
    
    //first update all the nodes  
    List<Fixture> fixtureList = fixtMgr.loadAllFixtureBySecondaryGatewayId(gw.getId());
    Iterator<Fixture> iter = fixtureList.iterator();
    while(iter.hasNext()) {
      Fixture fixture = iter.next();
      gotGwAck = false;
      DeviceServiceImpl.getInstance().setWirelessParams(fixture.getId());
      ServerUtil.sleepMilli(75);
    }    
    //update gateway radio params
    changeWirelessParams(gw);
    
  } //end of method updateGwAndSuParams
   
  public void startScan(Gateway gw) { 
    
    byte[] dataPkt = new byte[4];
    dataPkt[0] = gw.getChannel().byteValue();
    dataPkt[1] = gw.getWirelessRadiorate().byteValue();
    dataPkt[2] = 3; //no. of time broadcast should be sent
    dataPkt[3] = 3; // no. of secs between broadcasts
    
    sendGwPkt(ServerConstants.GATEWAY_START_SCAN, gw, dataPkt, true);
    
  } //end of method startScan
  
  public void scanResults(byte[] pkt, long gwId) {
    
  } //end of method scanResults
  
} //end of class GatewayImpl
