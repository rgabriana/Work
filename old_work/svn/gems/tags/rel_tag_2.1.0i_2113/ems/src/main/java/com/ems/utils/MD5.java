package com.ems.utils;

import java.security.MessageDigest;


/**
 * Provides MD5 Hashing
 *
 * <p>
 * <b>Example:</b> 
 * <br />
 * <code>
 * &nbsp;&nbsp;String hashedString = MD5.hash("hash me");
 * </code>
 * <br /><br />
 * You can run it from the command line as follows:
 * <br />
 * <code>
 * <b>java</b> 
 */
public class MD5 {

	/**
	 * Returns the hashed value of <code>clear</code>.
	 */
	public static String hash(String clear) throws Exception {
		try {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] b = md.digest(clear.getBytes());

		int size = b.length;
		StringBuffer h = new StringBuffer(size);
		for (int i = 0; i < size; i++) {
			int u = b[i]&255; // unsigned conversion
			if (u<16) {
				h.append("0"+Integer.toHexString(u));
			} else {
				h.append(Integer.toHexString(u));
			}
		}
		return h.toString();
		} catch (Exception e) {
			throw new Exception(e);	
		}
	}

	/**
	 * Command line tool to get MD5 hash of strings.
	 */
	public static void main(String args[]) {
		try {
		} catch (Exception e) {
			usage();	
		}
	}

	/*
	 * Outputs usage info. To be used w/ the main method.
	 */
	private static void usage() {
	}
}
