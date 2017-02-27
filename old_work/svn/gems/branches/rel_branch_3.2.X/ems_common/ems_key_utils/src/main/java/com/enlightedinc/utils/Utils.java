package com.enlightedinc.utils;

public class Utils
{
	 /**Function convert to String
	  *
	 */
	public static String asHex (byte buf[]) {
	   StringBuffer strbuf = new StringBuffer(buf.length * 2);
	   int i;

	   for (i = 0; i < buf.length; i++) {
	    if (((int) buf[i] & 0xff) < 0x10)
		    strbuf.append("0");

	    strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
	   }

	   return strbuf.toString();
	}
	
	
	public static byte[] fromHexString(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

}