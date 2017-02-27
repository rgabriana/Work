/**
 * 
 */
package com.ems.server.ssl;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.model.Gateway;
import com.ems.server.ServerConstants;
import com.ems.server.device.GatewayComm;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.GatewayManager;

/**
 * @author Sreedhar
 * class to maintain SSL sessions to all gateways
 */
public class SSLSessionManager {
    
  private static SSLSessionManager instance = null;  
  private static SSLSocketFactory tlsSocketFactory = null;
  
  //gesm ip address of building side to connect to gateways
  private String bldgGemsIp = "localhost";
  
  //private String defaultAuthKey = "enLightedWorkNow";
  
  private static Logger sslLogger = Logger.getLogger("SSLLogger");
  private GatewayManager gwMgr = null;
  
  private static int sslConnectTimeout = 10; // in seconds
  private static int sslReadTimeout = 6 * 60; //in seconds
  
  //hash map to hold all the SSL sessions to gateways
  private static ConcurrentHashMap<Long, SSLConnection> gwSslSessionMap = 
    new ConcurrentHashMap<Long, SSLConnection>();
  
  private EventsAndFaultManager eventMgr = null;
  
  /**
   * 
   */
  private SSLSessionManager() {
    // TODO Auto-generated constructor stub
    
    eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
    initSSL();
    bldgGemsIp = getIp4Address("eth1");
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    
    //this is done a seperate thread so that it will not delay the server startup
    if(!GatewayComm.getInstance().isSSLEnabled()) {
  		return;
  	}
    new Thread() {
      public void run() {
      	initializeGwSSLConnections();
      }
    }.start();    
    
  } //end of constructor
  
  public static SSLSessionManager getInstance() {
    
    if(instance == null) {
      synchronized(SSLSessionManager.class) {
	if(instance == null) {
	  instance = new SSLSessionManager();
	}
      }
    }
    return instance;
    
  } //end of method getInstance

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
        
    try {      
      //String gwIp = "10.10.10.48";      
      String gwIp = "10.10.10.38";
//      SSLSessionManager sslSessionMgr = SSLSessionManager.getInstance();
//      byte[] packet = { 0x58, 0x1, 0x0, 0x17, 0x0, 0x0, 0x1e, 0x5, 0x5a, 0x1, 0x0, 0x4,
//	  0x57, (byte)0xd6, 0x0, 0x0, 0x0, 0x64, 0x0, 0x0, 0xe, 0x10, 0x5e };
//      sslSessionMgr.sendCommandToGateway(1, packet);
//      
      getSSLAuthKey("enLightedWorkNow", "");
      
//      	MessageDigest md = MessageDigest.getInstance("SHA-1");      
//      	byte[] keyArr = defaultAuthKey.getBytes();
//      	ServerUtil.printPacket("key array -- ", keyArr);
//      	md.update(keyArr);
//      	ServerUtil.printPacket("digest -- ", md.digest());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
  } //end of method main
  
  public void removeSSLConnection(Gateway gw) {
    
    gwSslSessionMap.remove(gw.getId());
    
  } //end of method removeSSLConnection
  
  public void checkSSLConnection(long gwId) {
    
  	if(!GatewayComm.getInstance().isSSLEnabled()) {
  		return;
  	}
  	//sslLogger.info("inside the check ssl");
    Gateway gw = gwMgr.loadGateway(gwId);
    if(!ServerUtil.isSSLSupported(gw)) {
      return;
    }
    try {
      getSSLConnection(gw);
    }
    catch(SSLException ssle) {
      //ssle.printStackTrace();
      sslLogger.error(gw.getIpAddress() + "(checkSSLConnection): " + ssle.getMessage());
    }
            
  } //end of method checkSSLConnection
  
  //function to get the ssl connection from cache. if it is not in the cache, 
  //create the new connection and return
  private synchronized SSLConnection getSSLConnection(Gateway gw) throws SSLException {
    
    SSLConnection sslConn = gwSslSessionMap.get(gw.getId());
    if(sslConn == null) {
      //SSL connection is not in the cache, create one
    	sslLogger.debug(gw.getIpAddress() + ": could not retrieve the SSL connection from cache");
      sslConn = createSSLConnection(gw);      
    }
    return sslConn;
    
  } //end of method getSSLConnection
  
  public void reinitiateConnection(long gwId) {
        
    gwSslSessionMap.remove(new Long(gwId));
    Gateway gw = gwMgr.loadGateway(gwId);
    if(!ServerUtil.isSSLSupported(gw)) {
      return;
    }
  	try {
  		createSSLConnection(gw);
  	} 
  	catch(Exception e) {
  		sslLogger.error(gw.getIpAddress() + ": not able to reinitiate the connection");
  	}
  	
  } //end of method reinitiateConnection
  
  //function to send a particular command to a gateway
  public void sendCommandToGateway(Gateway gw, byte[] pkt) throws SSLException {
    
    //get the ssl session from the cache
    SSLConnection sslConn = getSSLConnection(gw);
    try {
      sslConn.sendCommand(pkt);
    }
    catch(Exception e) {
      sslConn = null;
      sslLogger.info(gw.getId() + ": SSL conn is broken - " + e.getMessage());
      //connection is broken reinitiate it
      reinitiateConnection(gw.getId());     
    }
    
  } //end of method sendCommandToGateway
      
  //function to get the ip v4 address of a particular interface
  private String getIp4Address(String interfaceName) {
    
    String ip = "localhost";
    try {
      NetworkInterface ni = NetworkInterface.getByName(interfaceName);
      //System.out.println("interface name - " + ni.getDisplayName());
      Enumeration<InetAddress> ipEnum = ni.getInetAddresses();      
      while(ipEnum.hasMoreElements()) {
	InetAddress iAddr = (InetAddress)ipEnum.nextElement();
	if(!(iAddr instanceof Inet6Address)) {
	  //System.out.println(" ip4 address -- " + iAddr.getHostAddress());
	  ip = iAddr.getHostAddress();
	}
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return ip;
    
  } //end of method getIp4Address

  //function to initialize the SSL
  private void initSSL() {
    
    if(tlsSocketFactory == null) {
      SecureRandom secRand = new SecureRandom();
      try {
	SSLContext tlsSc = SSLContext.getInstance(SSLConstants.TLS_PROTOCOL_VERSION);						
	tlsSc.init(null, new TrustManager[] { new SSLTrustManager()}, secRand); 
	tlsSocketFactory = tlsSc.getSocketFactory();
      }
      catch(Exception e) {
	sslLogger.error("error in initializing SSL" + e.getMessage());
	e.printStackTrace();
      }
    }
    
  } //end of method initSSL
  
  //this method is called at the startup of GEMS to establish connections
  //to all the gateways
  private void initializeGwSSLConnections() {
    
  	//sslLogger.info("inside the initial gw ssl connections");
    List<Gateway> gwList = gwMgr.loadAllGateways();
    Iterator<Gateway> gwIter = gwList.iterator();
    Gateway gw = null;
    while(gwIter.hasNext()) {
      gw = gwIter.next();
      try {
      	if(ServerUtil.isSSLSupported(gw)) {
      		getSSLConnection(gw);
      	}
      }
      catch(Exception e) {
	//System.err.println(gw.getIpAddress() + ": count not initialize SSL connection");
      	sslLogger.error(gw.getIpAddress() + ": could not initialize SSL connection");
      }
    }
    sslLogger.info("gateway ssl connections are established");
    
  } //end of method initializeGwSSLConnections
  
  //function to compute the key used for mutual authentication between GEMS and gateway  
  private static byte[] getSSLAuthKey(String authKey, String gwMac) throws Exception {
    
    MessageDigest md = MessageDigest.getInstance("SHA-1"); 
    md.reset();
    byte[] hardCodedArr = "EnlightedAuthKey".getBytes();    
    md.update(hardCodedArr);
    byte[] keyArr = authKey.getBytes();
    md.update(keyArr);
    byte[] gwMacArr = gwMac.replaceAll(":", "").getBytes(); //ServerUtil.getFixedLengthMacString(gwMac).getBytes();      
    md.update(gwMacArr);
    byte[] digest = md.digest();    
    return digest;
    
  } //end of method getSSLAuthKey
  
  //function to authenticate the Gateway SSL
  private boolean authenticateGwSSL(SSLConnection sslConn, String authKey, String gwMac) {
    
    try {
      byte[] key = getSSLAuthKey(authKey, gwMac);
      sslLogger.debug(sslConn.getIpAddress() + "(sent digest)- " +  
	  ServerUtil.getLogPacket(key));      
      
//      byte[] gwPkt = new byte[5 + key.length];
//      gwPkt[0] = (byte)ServerConstants.GATEWAY_AUTH_CMD;
//      int seqNo = 0; //seq no. is not required for this packet
//      byte[] seqNoArr = ServerUtil.intToByteArray(seqNo);
//      System.arraycopy(seqNoArr, 0, gwPkt, 1, seqNoArr.length);
//      System.arraycopy(key, 0, gwPkt, 5, key.length);
      
      sslConn.sendCommand(key);
      
      byte[] rcvdPkt = sslConn.getInitialResponse();
      if(rcvdPkt == null) {
      	return false;
      }
      byte[] rcvdKey = new byte[20];
      System.arraycopy(rcvdPkt, 0, rcvdKey, 0, 20);
      sslLogger.debug(sslConn.getIpAddress() + "(received digest) - " + 
      		ServerUtil.getLogPacket(rcvdKey));
      
      if(Arrays.equals(key, rcvdKey)) {
      	sslLogger.info(sslConn.getIpAddress() + "- authentication successfull");
      	return true;
      }
      sslLogger.error(sslConn.getIpAddress() + "- authentication failed");
    }
    catch(Exception e) {
      sslLogger.error(sslConn.getIpAddress() + "(auth): " + e.getMessage());
      e.printStackTrace();
    }	
    return false;
    
  } //end of method authenticateGwSSL
  
  //function to create the SSL session object for a gateway
  private synchronized SSLConnection createSSLConnection(Gateway gw) throws SSLException {

  	if(gwSslSessionMap.containsKey(gw.getId())) {
  		return gwSslSessionMap.get(gw.getId());
  	}
    String key = gw.getWirelessEncryptKey();
    String gwIp = gw.getIpAddress();
    if (key == null || key.length() == 0) {
      String msg = gwIp + "(create): SSL Authentication Key is NULL.";      
      sslLogger.error(msg);
      throw new SSLException(msg);
    }
    boolean socketCreated = false;
    try {      
      SSLSocket socket = (SSLSocket)tlsSocketFactory.createSocket();
        socket.setKeepAlive(true);
      //bind to the local eth1 interface address
      if(bldgGemsIp != null && bldgGemsIp.length() != 0) {
      	InetSocketAddress localAddress = new InetSocketAddress(bldgGemsIp, 0);
      	socket.bind(localAddress);
      }
      InetSocketAddress address = new InetSocketAddress(gwIp, ServerConstants.GW_UDP_PORT);
      socket.connect(address, sslConnectTimeout * 1000); // connect with timeout 
      sslLogger.debug(gwIp + ": after socket connect");
      socket.setSoTimeout(sslReadTimeout * 1000); // set read(I/O) timeout
      socket.setEnabledCipherSuites(SSLConstants.TLS_CIPHER_SUITES);
      socket.setEnabledProtocols(new String[] {SSLConstants.TLS_PROTOCOL_VERSION});

      socket.startHandshake();
      sslLogger.debug(gwIp + ": after handshake");
      SSLConnection sslConn = new SSLConnection(socket, gwIp, gw.getId());
      socketCreated = true;
      //socket connection is established, authenticate gateway
      sslLogger.debug(gwIp + ": created the ssl connection");
      if (!authenticateGwSSL(sslConn, key, gw.getMacAddress())) {
      	String msg = gwIp + "(create): SSL Authentication Failed to gw";
      	sslLogger.error(msg);
      	//raise an authentication failure event
      	eventMgr.addAlarm(gw, "Could not authenticate", 
      			EventsAndFault.GW_SSL_CONN_FAILURE, EventsAndFault.MAJOR_SEV_STR);
      	throw new SSLException(msg);
      }            
      //start a separate thread to listen for packets from this gateway 
      sslConn.listenForPackets();
      sslLogger.info(gwIp + ": keep alive -- " + socket.getKeepAlive());
      //store the SSL session in the cache     
      gwSslSessionMap.put(gw.getId(), sslConn);
      return sslConn;
    }
    catch(SSLException ex) {
      throw ex;
    }
    catch(NoRouteToHostException nrthe) {
      sslLogger.error(gwIp + ":" + nrthe.getMessage());
      throw new SSLException(nrthe.getMessage());
    }
    catch(Exception ex) {
      sslLogger.error(gwIp + "(createConnection): " + ex.getMessage());
      throw new SSLException(ex.getMessage());
    } finally {
      if(!socketCreated) {
      	//raise an event in case of SSL socket creation failed
      	eventMgr.addAlarm(gw, "Could not create secure connection", 
      			EventsAndFault.GW_SSL_CONN_FAILURE, EventsAndFault.MAJOR_SEV_STR);
      }
    }

  } //end of method createSSLConnection

} //end of class SSLSessionManager
