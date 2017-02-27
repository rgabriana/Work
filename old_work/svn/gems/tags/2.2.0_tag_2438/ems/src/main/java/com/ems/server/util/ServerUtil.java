/**
 * 
 */
package com.ems.server.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.server.ServerConstants;

/**
 * @author EMS
 *
 */
public class ServerUtil {

  /**
   * 
   */
  public ServerUtil() {
    // TODO Auto-generated constructor stub
  }

  public static byte computeChecksum(byte[] array) {
    
    int noOfBytes = array.length;
    byte checksum = 0;
    for(int i = 0; i < noOfBytes; i++) {
      checksum ^= array[i];
    }
    return checksum;
    
  } //end of method computeChecksum
    
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
  
  //this expects the length of array to be 4
  public static final long extractIntAsLongFromByteArray(byte[] b, int startPos) {
   
    long l = 0;
    l |= b[startPos] & 0xff;
    l  = (l<<8) | b[startPos + 1] & 0xff;
    l  = (l<<8) | b[startPos + 2] & 0xff;
    l  = (l<<8) | b[startPos + 3] & 0xff;
    return l;
    
  } //end of method intByteArrayToLong
  
  //it fills 4 bytes in passed array
  public static final void fillLongAsIntInByteArray(long value, byte[] byteArr, int startPos) {

    byteArr[startPos] = (byte)((value & 0xFF000000) >> 24);
    byteArr[startPos + 1] = (byte)((value & 0xFF0000) >> 16);
    byteArr[startPos + 2] = (byte)((value & 0xFF00) >> 8);
    byteArr[startPos + 3] = (byte) (value & 0xFF);
        
  } //end of method fillIntInByteArray

  //it sends a 4 byte array
  public static final byte[] intToByteArray(int value) {

    return new byte[] {
            (byte)((value & 0xFF000000) >> 24),
            (byte)((value & 0xFF0000) >> 16),
            (byte)((value & 0xFF00) >> 8),
            (byte) (value & 0xFF)};
    
  } //end of method intToByteArray
  
  //it expects the length of array to be 8
  public static final long extractLongFromByteArray(byte [] b, int startPos) {
  
    long l = 0;
    l |= b[startPos] & 0xff;
    l  = (l<<8) | b[startPos + 1] & 0xff;
    l  = (l<<8) | b[startPos + 2] & 0xff;
    l  = (l<<8) | b[startPos + 3] & 0xff;
    l  = (l<<8) | b[startPos + 4] & 0xff;
    l  = (l<<8) | b[startPos + 5] & 0xff;
    l  = (l<<8) | b[startPos + 6] & 0xff;
    l  = (l<<8) | b[startPos + 7] & 0xff;
    return l;
    
  }
  
  //it expects the length of array to be 8
  public static final long byteArrayToLong(byte [] b) {
    
    long l = 0;
    l |= b[0] & 0xff;
    l  = (l<<8) | b[1] & 0xff;
    l  = (l<<8) | b[2] & 0xff;
    l  = (l<<8) | b[3] & 0xff;
    l  = (l<<8) | b[4] & 0xff;
    l  = (l<<8) | b[5] & 0xff;
    l  = (l<<8) | b[6] & 0xff;
    l  = (l<<8) | b[7] & 0xff;
    return l;
    
  } //end of method byteArrayToLong
  
  //this expects the length of array to be 4
  public static final long intByteArrayToLong(byte[] b) {
   
    long l = 0;
    l |= b[0] & 0xff;
    l  = (l<<8) | b[1] & 0xff;
    l  = (l<<8) | b[2] & 0xff;
    l  = (l<<8) | b[3] & 0xff;
    return l;
    
  } //end of method intByteArrayToLong
  
  //it expects the length of array to be 4
  public static final int byteArrayToInt(byte [] b) {
    
    int l = 0;
    l |= b[0] & 0xff;
    l  = (l<<8) | b[1] & 0xff;
    l  = (l<<8) | b[2] & 0xff;
    l  = (l<<8) | b[3] & 0xff;
    return l;
    
  } //end of method byteArrayToInt
  
  //it expects the length of array to be 4
  public static final int extractIntFromByteArray(byte [] b, int startPos) {
    
    int l = 0;
    l |= b[startPos] & 0xff;
    l  = (l<<8) | b[startPos + 1] & 0xff;
    l  = (l<<8) | b[startPos + 2] & 0xff;
    l  = (l<<8) | b[startPos + 3] & 0xff;
    return l;
    
  } //end of method byteArrayToInt

  public static String getSnapAddr(byte[] addr) {
        
    int len = addr.length;
    if(len < 3) {
      return null;
    } 
    StringBuffer sb = new StringBuffer();
    short tempShort = 0;    
    for(int i = 0; i < len; i++) {
      if(i > 0) {
	sb.append(":");
      }
      if(addr[i] < 0) {
	tempShort = (short)(256 + addr[i]);
      } else {
	tempShort = addr[i];
      }
      sb.append(Integer.toString(tempShort, 16));      
    }
    return sb.toString();
    
//    if(addr[len - 1] < 0) {
//      tempShort = (short)(256 + addr[len - 1]);
//    }
//    return Integer.toString(addr[len - 3], 16) + ":" + Integer.toString(addr[len - 2], 16) + 
//    	":" + Integer.toString(tempShort,16); 
//        
  } //end of method getSnapAddr
  
  public static String getSnapAddr(byte addr1, byte addr2, byte addr3) {
        
    StringBuffer sb = new StringBuffer();
    short tempShort = 0; 
    
    if(addr1 < 0) {	
      tempShort = (short)(256 + addr1);
    } else {
      tempShort = addr1;
    }
    sb.append(Integer.toString(tempShort, 16));
    sb.append(":");
    
    if(addr2 < 0) {
      tempShort = (short)(256 + addr2);
    } else {
      tempShort = addr2;
    }
    sb.append(Integer.toString(tempShort, 16));
    sb.append(":");
    
    if(addr3 < 0) {	
      tempShort = (short)(256 + addr3);
    } else {
      tempShort = addr3;
    }
    sb.append(Integer.toString(tempShort, 16));    
    
    return sb.toString();
  
  } //end of method getSnapAddr
  
  /**
   * Converts a integer to IP address string
   * @param ip
   * @return String representation of IP address...
   */
  public static String intToIpAddress(int ip) {
      return ((ip >> 24 ) & 0xFF) + "." +
             ((ip >> 16 ) & 0xFF) + "." +
             ((ip >>  8 ) & 0xFF) + "." +
             ( ip        & 0xFF);
  }
  public static short[] getShortSnapAddr(String addr) {
        
    short[] addrArr = new short[3];
    StringTokenizer st = new StringTokenizer(addr, ":");
    //System.out.println("addr - " + addr);
    int i = 0;
    if(st.countTokens() == 3) {
      while(st.hasMoreTokens()) {
	addrArr[i++] = Short.parseShort(st.nextToken(), 16);
      }
    }
    return addrArr;
    
  } //end of method getShortSnapAddr
  
  public static byte[] getMultiCastAddr(String addr) {
    
    byte[] multiAddr = new byte[2];
    StringTokenizer st = new StringTokenizer(addr, ":");    
    int i = 0;
    if(st.countTokens() == 2) {
      while(st.hasMoreTokens()) {
	multiAddr[i++] = Byte.parseByte(st.nextToken(), 16);		
      }
    }
    return multiAddr;
    
  } //end of method getMultiCastAddr
  
  public static byte[] getSnapAddr(String addr) {
    
    try {
      byte[] addrArr = new byte[3];
      StringTokenizer st = new StringTokenizer(addr, ":");
      //System.out.println("addr - " + addr);
      int i = 0;
      short tempShort = 0;
      if(st.countTokens() == 3) {
	while(st.hasMoreTokens()) {
	  tempShort = Short.parseShort(st.nextToken(), 16);
	  if(tempShort > 127) {
	    tempShort = (short)(tempShort - 256);
	  } 
	  addrArr[i++] = (byte)tempShort;
	  //addrArr[i++] = Byte.parseByte(st.nextToken());
	}
	return addrArr;
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return null;
    
  } //end of method getSnapAddr
  
  //it sends a 2 byte array
  public static final byte[] shortToByteArray(int value) {

    return new byte[] {            
            (byte)((value & 0xFF00) >> 8),
            (byte) (value & 0xFF)};
    
  } //end of method shortToByteArray
  
  public static final byte[] shortToByteArray(short value) {

    return new byte[] {            
            (byte)((value & 0xFF00) >> 8),
            (byte) (value & 0xFF)};
    
  } //end of method shortToByteArray
  
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
    
  public static void logPacket(String label, byte[] packet, Logger logger) {
    
    logger.debug(label + ": " + getLogPacket(packet));
        
  } //end of method logPacket
  
  public static String getLogPacket(byte[] packet) {
    
    int noOfBytes = packet.length;
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < noOfBytes; i++) {
      sb.append(String.format("%x", packet[i]));
      sb.append(' ');
    }
    return sb.toString();
    
  } // end of method getLogPacket
  
  public static int getCurrentMin() {
        
    Calendar cal = Calendar.getInstance();
    return cal.get(Calendar.MINUTE);
    
  } //end of method getCurrentMin
  
  public static int getCurrentHour() {
    
    Calendar cal = Calendar.getInstance();
    return cal.get(Calendar.HOUR_OF_DAY);
    
  } //end of method getCurrentHour
    
  //it expects the length of array to be 2
  public static final int byteArrayToShort(byte [] b) {
    
    int l = 0;
    l |= b[0] & 0xff;
    l  = (l<< 8) | b[1] & 0xff;
    return l;
    
  } //end of method byteArrayToShort
  
  //it extracts 2 bytes from the start position
  public static final int extractShortFromByteArray(byte [] b, int startPos) {
    
    int l = 0;
    l |= b[startPos] & 0xff;
    l  = (l<< 8) | b[startPos + 1] & 0xff;
    return l;
    
  } //end of method extractShortFromByteArray
  
  public static Boolean isBitSet(short b, int bit) {
    
    return (b & (1 << bit)) != 0;
    
  } //end of method isBitSet 
  
  /*
   * if ver1 == ver2 return 0
   * if ver1 > ver2 return +ve
   * if ver1 < ver2 return -ve
   */
  public static int compareVersion(String ver1, String ver2) {
    
    if(ver1 == null && ver2 == null) {
      return 0;
    }
    if(ver2 == null) {
      return 1;
    }
    if(ver1 == null) {
      return -1;
    }
    ver1 = ver1.split("b")[0].trim();
    ver2 = ver2.split("b")[0].trim();
    String s1 = normalisedVersion(ver1, ".", 4); 
    String s2 = normalisedVersion(ver2, ".", 4); 
    int cmp = s1.compareTo(s2);    
    return cmp;
    
  } //end of method compareVersion

  public static String normalisedVersion(String version, String sep, int maxWidth) { 
  
    String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version); 
    StringBuilder sb = new StringBuilder(); 
    for (String s : split) { 
        sb.append(String.format("%" + maxWidth + 's', s)); 
    } 
    return sb.toString();
    
  } //end of method normalisedVersion
  
  public static String getCommandString(int msgType) {
    
    switch(msgType) {
    case ServerConstants.SET_LIGHT_LEVEL_MSG_TYPE:
      return ServerConstants.SET_LIGHT_LEVEL_STR;   
    case ServerConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE:
      return ServerConstants.SET_ABS_LIGHT_LEVEL_STR;
    case ServerConstants.SET_PROFILE_ADV_MSG_TYPE:
      return ServerConstants.SET_PROFILE_ADV_STR;
    case ServerConstants.SET_PROFILE_MSG_TYPE:
      return ServerConstants.SET_PROFILE_STR;
    case ServerConstants.SET_VALIDATION_MSG_TYPE:
      return ServerConstants.SET_VALIDATION_STR;
    case ServerConstants.SET_LIGHT_STATE_MSG_TYPE:
      return ServerConstants.SET_LIGHT_STATE_STR;
    case ServerConstants.SET_DISC_MODE_MSG_TYPE:
      return ServerConstants.SET_DISC_MODE_MSG_STR;
    case ServerConstants.SU_APPLY_WIRELESS_CMD:
      return ServerConstants.SU_APPLY_WIRELESS_STR;
    case ServerConstants.SU_SET_WIRELESS_CMD:
      return ServerConstants.SU_SET_WIRELESS_STR;
    case ServerConstants.REBOOT_MSG_TYPE:
      return ServerConstants.SU_REBOOT_STR;
    case ServerConstants.MANUAL_CALIB_MSG_TYPE:
      return ServerConstants.SU_MANUAL_CALIB_STR;
    default:
      return "Unknown";
    }
    
  } //end of method getCommandString
  
  public static String getCurrentState(int currState) {
    
    switch(currState) {
    case ServerConstants.CURR_STATE_AMB_ON:
      return ServerConstants.CURR_STATE_AUTO_STR;	
    case ServerConstants.CURR_STATE_BASELINE:
      return ServerConstants.CURR_STATE_BASELINE_STR;
    case ServerConstants.CURR_STATE_DISC:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_INIT:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_MANUAL:
      return ServerConstants.CURR_STATE_MANUAL_STR;
    case ServerConstants.CURR_STATE_NORMAL_HIGH:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_NORMAL_LOW:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_OCC_OFF:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_OCC_ON:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_VALIDATION:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_GOTOAMBHI:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_GOTOAMBLO:
      return ServerConstants.CURR_STATE_AUTO_STR;
    case ServerConstants.CURR_STATE_BYPASS:
      return ServerConstants.CURR_STATE_DISABLED_STR;
    default:
      return ServerConstants.CURR_STATE_UNKNOWN_STR;
    }
    
  } //end of method getCurrentState

  /**
   * Get's the transaction id from packet
   * @param packet
   * @return txnId
   */
  public static int getTxnId(byte[] packet) {
	  
  	byte[] seqNoArr = new byte[4];    
	  System.arraycopy(packet, ServerConstants.CMD_PKT_TX_ID_POS, seqNoArr, 0, seqNoArr.length);
	  int seqNo = byteArrayToInt(seqNoArr);
	  return seqNo;
	  
  } //end of method getTxnId 

  public static String convertByteArrToIp(byte[] ipArr) {
    
    StringBuffer sb = new StringBuffer();    
    short tempShort = 0;
    for(int i = 0; i < 4; i++) {      
      if(i > 0) {
	sb.append(".");
      }
      if(ipArr[i] < 0) {
	tempShort = (short)(256 + ipArr[i]);
      } else {
	tempShort = ipArr[i];
      }
      sb.append(tempShort);     
    }
    return sb.toString();
    
  } //end of method convertByteArrToIp
  
  public static String convertByteArrToKey(byte[] keyArr) {
    
    String key = "";
    return key;
    
  } //end of method convertByteArrToKey
  
  public static String getIPFromMac(String sMacAddress) {
	  String sIP = "10.96.";
	  String sHost1 = "1";
	  String sHost2 = "255";
	  if (sMacAddress.indexOf(":") != -1)
		  sMacAddress = sMacAddress.replaceAll(":", "");
	  
	  if (sMacAddress.length() == 12) {
		  sHost1 = sMacAddress.substring(8, sMacAddress.length()-2);
		  sHost2 = sMacAddress.substring(10, sMacAddress.length());
		  try {
			  sIP = sIP + Integer.parseInt(sHost1,16) + "." + Integer.parseInt(sHost2,16);
		  }catch(NumberFormatException nfe) {
			  sIP = "10.96.1.1";
		  }
	  } else {
		  sIP += "1.1";
	  }
	  return sIP;
  }

  public static String getSNAPFromMac(String sMacAddress) {
	  String sSNAP = "00:00:01";
	  if (sMacAddress.indexOf(":") != -1)
		  sMacAddress = sMacAddress.replaceAll(":", "");

	  if (sMacAddress.length() == 12) {
		  sSNAP = sMacAddress.substring(6, sMacAddress.length()-4);
		  sSNAP += ":";
		  sSNAP += sMacAddress.substring(8, sMacAddress.length()-2);
		  sSNAP += ":";
		  sSNAP += sMacAddress.substring(10, sMacAddress.length());
	  }
	  return sSNAP;
  }


  //function to convert mac address string to byte array
  public static byte[] convertMacToByteArr(String macAddr) {
    
    byte[] macByteArr = new byte[6];
    StringTokenizer st = new StringTokenizer(macAddr, ":");
    String token = null;
    short tempShort = 0;
    int i = 0;
    while(st.hasMoreTokens()) {
      token = st.nextToken();
      tempShort = Short.parseShort(token, 16);
      if(tempShort > 127) {
	tempShort = (short)(tempShort - 256);
      }
      macByteArr[i++] = (byte)tempShort;
      //System.out.println(macByteArr[i++]);        
    }
    return macByteArr;
    
  } //end of method convertMacToByteArr
  
  public static String generateName(String snapAddr) {
    StringTokenizer st = new StringTokenizer(snapAddr, ":");
    String name = "";
    String token = "";
    while(st.hasMoreTokens()) {
      token = st.nextToken();
      if (token.length() == 1)
    	  token = "0" + token;
      name += token;
    }
    return name;
  } //end of method generateName
  
  public static boolean isNewCU(Fixture fixture) {
    
    String version = fixture.getVersion();
    if(version == null || compareVersion(version, "2.0") < 0) {
      return false;
    }
    //2.0 SU
    String cuVersion = fixture.getCuVersion();
    if(cuVersion == null) {
      return false;
    }
    int cuVerInt = Integer.parseInt(cuVersion);
    if(cuVerInt >= 32 || cuVerInt == 0) {
      return true;
    }
    return false;
    
  } //end of method isNewCU
  
  //this method returns whether SSL is supported by the gateway based on version
  public static boolean isSSLSupported(Gateway gw) {
    if (gw != null) {
        if(ServerUtil.compareVersion(gw.getApp2Version(), "2.0") >= 0) {
          return true;
        }
    }
    return false;
    
  } //end of method isSSLSupported

  public static byte[] createSHA1Checksum(String filename) {
  	
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1"); 
	    md.reset();
			
	    InputStream fis = new FileInputStream(filename);	
			byte[] buffer = new byte[10240];			
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					md.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			fis.close();
			return md.digest();
		} catch (Exception e) {
			System.out.println("Error generating SHA1sum for : " + filename);
		}
		return null;
		
	} //end of method createSHA1Checksum
  
  public static void main(String args[]) {
     /*
    String mac = "68:54:f5:00:03:46";
    byte[] macByteArr = convertMacToByteArr(mac);
    
    byte[] arr = {0, 1, 2, 0};
    System.out.println(computeChecksum(arr));
    */
  	
  	System.out.println(compareVersion("2.0.0 b497", "1.4.1"));
  }
  
  public static final byte[] longToByteArray(long value) {
      byte[] buff = new byte[ 8 ];

      buff[0] = (byte)(value >>> 56);
      buff[1] = (byte)(value >>> 48);
      buff[2] = (byte)(value >>> 40);
      buff[3] = (byte)(value >>> 32);
      buff[4] = (byte)(value >>> 24);
      buff[5] = (byte)(value >>> 16);
      buff[6] = (byte)(value >>>  8);
      buff[7] = (byte)(value >>>  0);
      return buff;
  } //end of method LongToByteArray

  
} //end of class ServerUtil
