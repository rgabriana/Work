/**
 * 
 */
package com.ems.gateway;

/**
 * @author Sreedhar
 *
 */
public class GatewayPacket {

  private static byte GW_TARGET_FLAGS = 9; //flags 1001
  private static byte SU_TARGET_FLAGS = 24; //flags 11000
  
  public static final byte GW_MAGIC_FIRST = 'e';
  public static final byte GW_MAGIC_SECOND = 's';
  
  private byte[] gwHeader = { GW_MAGIC_FIRST, //magic
      GW_MAGIC_SECOND, //magic
      0, 9, //length
      SU_TARGET_FLAGS, //flags this need to be set based on gateway packet or SU packet
      0, 0, 0, 0 // hash key not used right now 
  };
  
  private byte[] gwPacket = null;

  /**
   * 
   */
  public GatewayPacket() {
    // TODO Auto-generated constructor stub   
  } //end of constructor
  
  //this function is to add the command targeted for gateway to the gateway packet
  public void addGatewayCommandData(byte[] data) {
    
    //correct the flags
    gwHeader[4] = GW_TARGET_FLAGS;
    //adjust the length in the header based on the data length   
    byte[] lenByteArr = shortToByteArray(data.length + gwHeader.length);
    System.arraycopy(lenByteArr, 0, gwHeader, 2, lenByteArr.length);
    
    gwPacket = new byte[gwHeader.length + data.length];
    System.arraycopy(gwHeader, 0, gwPacket, 0, gwHeader.length);
    System.arraycopy(data, 0, gwPacket, gwHeader.length, data.length);
    
  } //end of method addGatewayCommandData
  
  //this function is to add the command targeted for SU to the gateway packet
  public void addSUCommandData(byte[] data) {
    
    //correct the flags
    gwHeader[4] = SU_TARGET_FLAGS;
    //adjust the length in the header based on the data length   
    byte[] lenByteArr = shortToByteArray(data.length + gwHeader.length);
    System.arraycopy(lenByteArr, 0, gwHeader, 2, lenByteArr.length);
    
    gwPacket = new byte[gwHeader.length + data.length];
    System.arraycopy(gwHeader, 0, gwPacket, 0, gwHeader.length);
    System.arraycopy(data, 0, gwPacket, gwHeader.length, data.length);
    
  } //end of method addSUCommandData
  
  //it sends a 2 byte array
  public static final byte[] shortToByteArray(int value) {

    return new byte[] {            
            (byte)((value & 0xFF00) >> 8),
            (byte) (value & 0xFF)};
    
  } //end of method shortToByteArray
  
  public byte[] getGatewayPacket() {
    
    return gwPacket;
    
  } //end of method getGatewayPacket
  
} //end of class GatewayPacket
