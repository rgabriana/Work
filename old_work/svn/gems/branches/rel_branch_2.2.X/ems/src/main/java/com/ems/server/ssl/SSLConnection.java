/**
 * 
 */
package com.ems.server.ssl;

import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

import com.ems.server.device.DeviceListener;
import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.util.ServerUtil;

/**
 * @author Sreedhar
 * 
 */
public class SSLConnection {

  private SSLSocket sslSock = null;
  private String gwIp;
  private long gwId;
  private boolean listenForPackets = true;
  
  private static Logger sslLogger = Logger.getLogger("SSLLogger");
  private static Logger pktLogger = Logger.getLogger("PKTLogger");
  
  /**
   * 
   */
  public SSLConnection() {
    // TODO Auto-generated constructor stub
  }
  
  public SSLConnection(SSLSocket socket, String ip, long gwId) {
    
    sslSock = socket; 
    gwIp = ip;
    this.gwId = gwId;
    
  } //end of constructor
  
  public SSLSocket getSocket() {
    
    return sslSock;
    
  } //end of method getSocket
  
  public void terminateConnection() {
    
    //listenForPackets = false;
    try {
    	sslSock.close();
    }
    catch(Exception e) {
    	sslLogger.error(gwIp + ": unable to close the SSL socket");
    }    
    
  } //end of method terminateConnection
  
  public String getIpAddress() {
    
    return gwIp;
    
  } //end of method getIpAddress
  
  public byte[] getInitialResponse() {
  	
  	byte[] rcvdByteArr = new byte[20];
  	int len = 0;  
    try {
    	while(true) {
    		InputStream in = sslSock.getInputStream();
    		len = in.read(rcvdByteArr);
    		if(len == 0) {
    			ServerUtil.sleepMilli(10);
    			continue;
    		} else if(len == -1) {
    			//TODO
    		}
    		//sslLogger.info("received key - " + ServerUtil.getLogPacket(rcvdByteArr));
    		return rcvdByteArr;
    	}
    }
    catch(Exception e) {
    	e.printStackTrace();    	
    }
    return null;
    
  } //end of method getInitialResponse
  
  public void listenForPackets() {
    
  	listenForPackets = true;
    new Thread("SSL Listener " + gwIp) {
      //thread to receive the packets from the gateway
      public void run() {

      	byte[] buffer = new byte[1024];
      	while(listenForPackets) {
      		int len = 0;  
      		try {      
      			InputStream in = sslSock.getInputStream();      
      			len = in.read(buffer);
      			if(len == 0) {
      				//System.out.println("received nothing");
      				//sleep for 10 milli seconds to avoid busy loop in case socket is returning immediately
      				ServerUtil.sleepMilli(10);
      				continue;
      			} else if(len == -1) {
      				sslLogger.error(gwIp + ": detected end of stream");
      				listenForPackets = false;
      			} else {
      				processData(buffer, len);
      			}
      		}    
      		catch(SocketTimeoutException ste) {
      			sslLogger.error(gwIp + "(listenForPackets - ste): " + ste.getMessage());
      			listenForPackets = false;
      		}
      		catch(SocketException se) {
      			sslLogger.error(gwIp + "(listenForPackets - se): " + se.getMessage());
      			listenForPackets = false;
      		}
      		catch(Exception e) {
      			sslLogger.error(gwIp + "(listenForPackets - e): " + e.getMessage());
      			listenForPackets = false;
      		}
      		finally {
      			if(!listenForPackets) {
      				terminateConnection();
      				try {
      				    SSLSessionManager.getInstance().reinitiateConnection(gwId);
      				} catch(Exception e) {
      				    e.printStackTrace();
      				}
      			}
      		}
      	}
      }
      
    }.start();
    
  } //end of method listenForPackets
  
  int rcvdHeaderLen = 0;
  int expLen = 0;
  int currLen = 0;
  byte[] rcvdPkt = null;  
  byte firstLen = 0;
  
  private void processData(byte[] data, int len) {
    
    int currBuffPos = 0;
    boolean poolPkt = true;
    for(; len > 0; ) {
      if(rcvdHeaderLen == 4) {
      	//header is received
      	int left = expLen - currLen;
      	if(sslLogger.isDebugEnabled()) {
      	  sslLogger.debug("no. of bytes to copy - " + left);
      	}
      	int toCopy = len > left ? left : len;
      	System.arraycopy(data, currBuffPos, rcvdPkt, currLen + 4, toCopy);
      	len -= toCopy;
      	currBuffPos += toCopy;
      	currLen += toCopy;
	
      	if(currLen == expLen) {
      	  //got the full packet, send for processing it
      		if (pktLogger.isInfoEnabled()) {
          	    pktLogger.info(gwId + " " + gwIp + ": received command - " + ServerUtil.getLogPacket(rcvdPkt));
      		}
      	  if(sslLogger.isInfoEnabled()) {
      	    sslLogger.info(gwId + " " + gwIp + ": received command - " + ServerUtil.getLogPacket(rcvdPkt));
      	  }
      	  if(sslLogger.isDebugEnabled()) {
      	    sslLogger.debug(gwIp + ": received " + expLen + " bytes");
      	  }
      	  ZigbeeDeviceImpl.getInstance().getDeviceListener().addGatewayResponse(rcvdPkt, gwIp, poolPkt);      		
      	  rcvdHeaderLen = 0;
      	  expLen = 0;
      	  currLen = 0;
      	}
      } else {
      	//we don't have the header yet
      	byte c = data[currBuffPos++];
      	len--;
      	switch(rcvdHeaderLen) {
      		case 0:
      			//first byte e
      			if(c == 0x65) {
      				rcvdHeaderLen = 1;
      			}
      			break;
      		case 1:
      			//second byte s
      			if(c == 0x73) {
      				rcvdHeaderLen = 2;      				
      			} else {
      				rcvdHeaderLen = 0;		  
      			}
      			break;
      		case 2:
      			//first byte of len
      			firstLen = c;
      			expLen = c << 8;      			
      			rcvdHeaderLen = 3;
      			break;
      		case 3:
      			//second byte of len
      			expLen |= (c & 0xFF) - 4;
      			if(sslLogger.isDebugEnabled()) {
      			  sslLogger.debug("expected length of the packet -- " + expLen);
      			}
      			rcvdHeaderLen = 4;
      			currLen = 0;
      			poolPkt = true;
      			if(expLen > 128) {
      			  //pool pkts are of size 128. So, create a temporary packet for length of
      			  //more than 128
      			  rcvdPkt = new byte[expLen + 4];
    			  poolPkt = false;
      			} else {
      			  rcvdPkt = DeviceListener.getPacket();
      			  if(rcvdPkt == null) {
      			    //pool is exhausted. so create a temporary packet which will be garbage collected
      			    rcvdPkt = new byte[expLen + 4];
      			    poolPkt = false;
      			  }
      			}
      			rcvdPkt[0] = 0x65;
      			rcvdPkt[1] = 0x73;
      			rcvdPkt[2] = firstLen;
      			rcvdPkt[3] = c;      			
      			break;
      	}
      }
    }
    
  } //end of method processData
  
  public void sendCommand(byte[] packet) throws Exception {
	  if (pktLogger.isInfoEnabled()) {
	      pktLogger.info(gwId + " " + gwIp + ": sending command - " + ServerUtil.getLogPacket(packet));
	  }
    if(sslLogger.isInfoEnabled()) {
      sslLogger.info(gwId + " " + gwIp + ": sending command - " + ServerUtil.getLogPacket(packet));
    }
    try {
      sslSock.getOutputStream().write(packet);
    }
    catch(Exception e) {
      //not able to write on to the socket
    	sslLogger.error(gwIp + "(sendCommand):" + e.getMessage());
    	if(e.getMessage().contains("Socket is closed")) {
    		listenForPackets = false;
    		terminateConnection();
    	}
      throw e;
    }
     
  } //end of method sendCommand
  
} //end of class SSLConnection
