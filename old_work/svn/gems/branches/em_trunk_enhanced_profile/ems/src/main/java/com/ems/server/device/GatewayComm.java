/**
 * 
 */
package com.ems.server.device;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import com.ems.action.SpringContext;
import com.ems.model.Gateway;
import com.ems.server.ServerConstants;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;

/**
 * @author Sreedhar
 *
 */
public class GatewayComm {

  private static GatewayComm instance = null;
  
  private GatewayManager gwMgr = null;
  
  private boolean sslEnabled = true;

  /**
   * 
   */
  private GatewayComm() {
    
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    
  } //end of constructor GatewayComm

  public static GatewayComm getInstance() {
    
    if(instance == null) {
      synchronized(GatewayComm.class) {
	if(instance == null) {
	  instance = new GatewayComm();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  public void setSSLEnabled(boolean bool) {
  	sslEnabled = bool;
  }
  
  public boolean isSSLEnabled() {
  	return sslEnabled;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  } //end of method main
  
  //method to send the packet to a specific enlighted gateway with target as gateway
  public void sendDataToGateway(long gwId, byte[] packet) {
 
    Gateway gw = gwMgr.loadGateway(gwId);
    if(gw == null) {
      return;
    }
    GatewayPacket gwPktObj = new GatewayPacket();
    gwPktObj.addGatewayCommandData(packet);    
    byte[] gwPacket = gwPktObj.getGatewayPacket();
    sendDataPktToGateway(gw, gwPacket);
  
  } //end of method sendDataToGateway
  
  private void sendDataPktToGateway(Gateway gw, byte[] gwPacket) {
    
    //if the gateway version is not known, first try to use SSL if it fails, use UDP   
    if(sslEnabled) {
      if(gw.getApp2Version() == null || gw.getApp2Version().equals("")) {
	try {
	  SSLSessionManager.getInstance().sendCommandToGateway(gw, gwPacket);
	}
	catch(Exception e) {
	  e.printStackTrace();
	  System.err.println("could not send the packet over SSL: " + e.getMessage());
	  //failed with SSL. so probably it is old gateway use UDP
	  sendDataToGwUDP(gw.getIpAddress(), gwPacket);
	}
	return;
      }    
      //if version is 2.0 use SSL other wise use UDP
      try {
	if(ServerUtil.isSSLSupported(gw)) {
	  SSLSessionManager.getInstance().sendCommandToGateway(gw, gwPacket);
	} else {
	  sendDataToGwUDP(gw.getIpAddress(), gwPacket);
	}
      }
      catch(Exception ex) {
	System.err.println("could not send the gateway packet: " + ex.getMessage());
      }
    } else {
      sendDataToGwUDP(gw.getIpAddress(), gwPacket);
    }
    
  } //end of method sendDataPktToGateway
  
  //method to send the packet to a specific gateway with target as node
  public void sendNodeDataToGateway(long gwId, String gwIp, byte[] packet) {
 
    Gateway gw = gwMgr.loadGateway(gwId);
    if(gw == null) {
      return;
    }
    GatewayPacket gwPktObj = new GatewayPacket();
    gwPktObj.addSUCommandData(packet);    
    byte[] gwPacket = gwPktObj.getGatewayPacket();
    sendDataPktToGateway(gw, gwPacket);   
    
  } //end of method sendNodeDatatoGateway

  public void sendWDSDataToGateway(long gwId, String gwIp, byte[] packet) {
      
      Gateway gw = gwMgr.loadGateway(gwId);
      if(gw == null) {
        return;
      }
      GatewayPacket gwPktObj = new GatewayPacket();
      gwPktObj.addWDSCommandData(packet);    
      byte[] gwPacket = gwPktObj.getGatewayPacket();
      sendDataPktToGateway(gw, gwPacket);   
      
    } //end of method sendNodeDatatoGateway

  
  //this function is used to send the packet to gateway using UDP
  private void sendDataToGwUDP(String gwIp, byte[] data) {
      // TODO: Remove Debugging
      System.out.println("[Sending] : " + ServerUtil.getLogPacket(data));

    //gwIp = "192.168.96.206";
    int port = ServerConstants.GW_UDP_PORT;
    DatagramSocket gwSockConn = null;
    try {
      gwSockConn = new DatagramSocket();
      //ServerUtil.logPacket("to gw( " + gwIp + "): ", data, fixtureLogger);
      gwSockConn.send(new DatagramPacket(data, 0, data.length, new InetSocketAddress(gwIp, port)));      
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {      
      if(gwSockConn != null) {
	try {
	  gwSockConn.close();
	}
	catch(Exception ex) {	  
	}
      }     
    }
	
  } //end of method sendDataToGw
  
} //end of class GatewayComm
