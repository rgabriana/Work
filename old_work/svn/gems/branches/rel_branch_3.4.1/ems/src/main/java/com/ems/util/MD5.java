package com.ems.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	private MD5() {}	

	/**
	 * MD5
	 * @param clear
	 * @return
	 */
	public static String hash(String clear) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] b = md.digest(clear.getBytes());

			int size = b.length;
			StringBuffer h = new StringBuffer(size);
			for (int i = 0; i < size; i++) {
				int u = b[i] & 255; // unsigned conversion
				if (u < 16) {
					h.append("0" + Integer.toHexString(u));
				} else {
					h.append(Integer.toHexString(u));
				}
			}
			return h.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error generating MD5 hash.");
		}
	}
	
	public static String getMD5Checksum(String filename) {
		String result = "";
		try {
			byte[] b = createChecksum(filename);
			for (int i = 0; i < b.length; i++) {
				result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
			}
		} catch (Exception e) {
			System.out.println("Error generating md5sum for : " + filename);
		}
		return result;
	}

	public static byte[] createChecksum(String filename) {
		try {
			InputStream fis = new FileInputStream(filename);
	
			byte[] buffer = new byte[10240];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			fis.close();
			return complete.digest();
		} catch (Exception e) {
			System.out.println("Error generating md5sum for : " + filename);
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(hash("super"));
	}
}
