/**
 * 
 */
package com.ems.gateway;

import org.junit.Ignore;

import com.ems.ssl.SSLSessionManager;

/**
 * @author Sreedhar
 *
 */
@Ignore
public class GatewayCommandsTest {
  
  /**
   * 
   */
  public GatewayCommandsTest() {
  
    // TODO Auto-generated constructor stub
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
  }
  
  //it sends a 4 byte array
  public static final byte[] intToByteArray(int value) {

    return new byte[] {
            (byte)((value & 0xFF000000) >> 24),
            (byte)((value & 0xFF0000) >> 16),
            (byte)((value & 0xFF00) >> 8),
            (byte) (value & 0xFF)};
    
  } //end of method intToByteArray
  
  public void sendGatewayInfoReq(String ip) {
    
    byte[] dataPacket = new byte[0];
    
    byte[] gwPkt = new byte[5 + dataPacket.length];
    gwPkt[0] = 2; //(byte)ServerConstants.GATEWAY_CMD_INFO;
    int seqNo = 1;
    byte[] seqNoArr = intToByteArray(seqNo);
    System.arraycopy(seqNoArr, 0, gwPkt, 1, seqNoArr.length);
    System.arraycopy(dataPacket, 0, gwPkt, 5, dataPacket.length);
    
    GatewayPacket gwPktObj = new GatewayPacket();
    gwPktObj.addGatewayCommandData(gwPkt);    
    byte[] gwPacket = gwPktObj.getGatewayPacket();
    
    SSLSessionManager sslMgr = SSLSessionManager.getInstance();
    try {
      sslMgr.sendCommandToGateway(1, gwPacket);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
  } //end of method sendGatewayInfoReq
  
}
