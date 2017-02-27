/**
 * 
 */
package com.occengine.utils;

/**
 * @author sreedhar.kamishetti
 *
 */
public class OccUtil {

	/**
	 * 
	 */
	public OccUtil() {
		// TODO Auto-generated constructor stub
	}
	
	//it fills 2 bytes in passed array
  public static final void fillShortInByteArray(int value, byte[] byteArr, int startPos) {
    
    byteArr[startPos] = (byte)((value & 0xFF00) >> 8);
    byteArr[startPos + 1] = (byte) (value & 0xFF);
        
  } //end of method fillShortInByteArray
  
//it fills 4 bytes in passed array
  public static final void fillIntInByteArray(int value, byte[] byteArr, int startPos) {

    byteArr[startPos] = (byte)((value & 0xFF000000) >> 24);
    byteArr[startPos + 1] = (byte)((value & 0xFF0000) >> 16);
    byteArr[startPos + 2] = (byte)((value & 0xFF00) >> 8);
    byteArr[startPos + 3] = (byte) (value & 0xFF);
        
  } //end of method fillIntInByteArray
  
  //it fills 4 bytes in passed array
  public static final void fillLongAsIntInByteArray(long value, byte[] byteArr, int startPos) {

    byteArr[startPos] = (byte)((value & 0xFF000000) >> 24);
    byteArr[startPos + 1] = (byte)((value & 0xFF0000) >> 16);
    byteArr[startPos + 2] = (byte)((value & 0xFF00) >> 8);
    byteArr[startPos + 3] = (byte) (value & 0xFF);
        
  } //end of method fillIntInByteArray
  
  //it extracts 2 bytes from the start position
  public static final int extractShortFromByteArray(byte [] b, int startPos) {
    
    int l = 0;
    l |= b[startPos] & 0xff;
    l  = (l<< 8) | b[startPos + 1] & 0xff;
    return l;
    
  } //end of method extractShortFromByteArray
  
  //this expects the length of array to be 4
  public static final long extractIntAsLongFromByteArray(byte[] b, int startPos) {
   
    long l = 0;
    l |= b[startPos] & 0xff;
    l  = (l<<8) | b[startPos + 1] & 0xff;
    l  = (l<<8) | b[startPos + 2] & 0xff;
    l  = (l<<8) | b[startPos + 3] & 0xff;
    return l;
    
  } //end of method intByteArrayToLong
  
  public static String getLogPacket(byte[] packet) {
    
    int noOfBytes = packet.length;
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < noOfBytes; i++) {
      sb.append(String.format("%x", packet[i]));
      sb.append(' ');
    }
    return sb.toString();
    
  } // end of method getLogPacket
  
  public static void sleepMilli(int milli) {
    
    try {
      Thread.sleep(milli);
    }
    catch(Exception ex) {}
    
  } //end of method sleepMilli
  
  public static void sleep(int sec) {
    
    try {
      Thread.sleep(sec * 1000);
    }
    catch(Exception ex) {}
    
  } //end of method sleep
  

}
