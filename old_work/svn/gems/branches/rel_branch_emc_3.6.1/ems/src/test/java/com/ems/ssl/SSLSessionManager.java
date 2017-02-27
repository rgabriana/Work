/**
 * 
 */
package com.ems.ssl;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @author Sreedhar
 * class to maintain SSL sessions to all gateways
 */
public class SSLSessionManager {
    
public static final int GW_SSL_PORT = 995;
  
  public static String TLS_PROTOCOL_VERSION = "TLSv1";
	
  public static String[] TLS_CIPHER_SUITES = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
  
  public static final int GW_UDP_PORT = 8085;
  
  private static SSLSessionManager instance = null;  
  private static SSLSocketFactory tlsSocketFactory = null;
  
  //gesm ip address of building side to connect to gateways
  private String bldgGemsIp = "localhost";
  
  private static int sslConnectTimeout = 10; // in seconds
  private static int sslReadTimeout = 6 * 60; //in seconds
  
  //hash map to hold all the SSL sessions to gateways
  private static ConcurrentHashMap<Long, SSLConnection> gwSslSessionMap = 
    new ConcurrentHashMap<Long, SSLConnection>();

  /**
   * 
   */
  private SSLSessionManager() {
    // TODO Auto-generated constructor stub
        
    initSSL();
    bldgGemsIp = getIp4Address("eth1");
 
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
  
  public void removeSSLConnection(long gwId) {
    
    SSLConnection sslConn = gwSslSessionMap.remove(gwId);
    if(sslConn != null) {
      sslConn.terminateConnection();
    }
    
  } //end of method removeSSLConnection
   
  //function to get the ssl connection from cache. if it is not in the cache, 
  //create the new connection and return
  private SSLConnection getSSLConnection(long gwId) throws SSLException {
    
    SSLConnection sslConn = gwSslSessionMap.get(gwId);
    if(sslConn == null) {
      //SSL connection is not in the cache, create one     
      sslConn = createSSLConnection(gwId);      
    }
    return sslConn;
    
  } //end of method getSSLConnection
  
  public void reinitiateConnection(long gwId) {
        
    gwSslSessionMap.remove(new Long(gwId));
     
    try {
      createSSLConnection(gwId);
    } 
    catch(Exception e) {
    }
  	
  } //end of method reinitiateConnection
  
  //function to send a particular command to a gateway
  public void sendCommandToGateway(long gwId, byte[] pkt) throws SSLException {
    
    //get the ssl session from the cache
    SSLConnection sslConn = getSSLConnection(gwId);
    try {
      sslConn.sendCommand(pkt);
    }
    catch(Exception e) {
      sslConn = null;     
      //connection is broken reinitiate it
      reinitiateConnection(gwId);     
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
	SSLContext tlsSc = SSLContext.getInstance(TLS_PROTOCOL_VERSION);						
	tlsSc.init(null, new TrustManager[] { new SSLTrustManager()}, secRand); 
	tlsSocketFactory = tlsSc.getSocketFactory();
      }
      catch(Exception e) {	
	e.printStackTrace();
      }
    }
    
  } //end of method initSSL
    
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
      sslConn.sendCommand(key);
      
      byte[] rcvdPkt = sslConn.getInitialResponse();
      if(rcvdPkt == null) {
      	return false;
      }
      byte[] rcvdKey = new byte[20];
      System.arraycopy(rcvdPkt, 0, rcvdKey, 0, 20);
  
      if(Arrays.equals(key, rcvdKey)) {	
      	return true;
      }      
    }
    catch(Exception e) {
      e.printStackTrace();
    }	
    return false;
    
  } //end of method authenticateGwSSL
  
  //function to create the SSL session object for a gateway
  private synchronized SSLConnection createSSLConnection(long gwId) throws SSLException {

    String key = ""; //wireless encryption key
    String gwIp = ""; //gateway ip
    String macAddr = ""; //gateway mac adress
    
    if(gwSslSessionMap.containsKey(gwId)) {
      return gwSslSessionMap.get(gwId);
    }
   
    if (key == null || key.length() == 0) {
      String msg = gwIp + "(create): SSL Authentication Key is NULL.";     
     
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
      InetSocketAddress address = new InetSocketAddress(gwIp, GW_UDP_PORT);
      socket.connect(address, sslConnectTimeout * 1000); // connect with timeout 
     
      socket.setSoTimeout(sslReadTimeout * 1000); // set read(I/O) timeout
      socket.setEnabledCipherSuites(TLS_CIPHER_SUITES);
      socket.setEnabledProtocols(new String[] {TLS_PROTOCOL_VERSION});

      socket.startHandshake();
     
      SSLConnection sslConn = new SSLConnection(socket, gwIp, gwId);
      socketCreated = true;
      //socket connection is established, authenticate gateway
      
      if (!authenticateGwSSL(sslConn, key, macAddr)) {
      	String msg = gwIp + "(create): SSL Authentication Failed to gw";
      	//raise an authentication failure event
      	throw new SSLException(msg);
      }            
      //start a separate thread to listen for packets from this gateway 
      sslConn.listenForPackets();
      
      //store the SSL session in the cache     
      gwSslSessionMap.put(gwId, sslConn);
      return sslConn;
    }
    catch(SSLException ex) {
      throw ex;
    }
    catch(NoRouteToHostException nrthe) {      
      throw new SSLException(nrthe.getMessage());
    }
    catch(Exception ex) {
      throw new SSLException(ex.getMessage());
    } finally {
      if(!socketCreated) {
      	//raise an event in case of SSL socket creation failed      	
      }
    }

  } //end of method createSSLConnection

} //end of class SSLSessionManager
