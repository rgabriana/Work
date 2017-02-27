package com.ems.occengine.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class CommonUtils {

	public static final Logger logger = Logger.getLogger(CommonUtils.class
			.getName());
	private static Properties configProperties = new Properties();
	public static final int SU_HB_FREQUENCY = 30;

	public static final int byteArrayToShort(byte[] b) {

		int l = 0;
		l |= b[0] & 0xff;
		l = (l << 8) | b[1] & 0xff;
		return l;

	}

	public static final int extractShortFromByteArray(byte[] b, int startPos) {

		int l = 0;
		l |= b[startPos] & 0xff;
		l = (l << 8) | b[startPos + 1] & 0xff;
		return l;

	}

	// it expects the length of array to be 4
	public static final int byteArrayToInt(byte[] b) {

		int l = 0;
		l |= b[0] & 0xff;
		l = (l << 8) | b[1] & 0xff;
		l = (l << 8) | b[2] & 0xff;
		l = (l << 8) | b[3] & 0xff;
		return l;

	}

	public static final long intByteArrayToLong(byte[] b) {

		long l = 0;
		l |= b[0] & 0xff;
		l = (l << 8) | b[1] & 0xff;
		l = (l << 8) | b[2] & 0xff;
		l = (l << 8) | b[3] & 0xff;
		return l;
	}

	public static String getSnapAddr(byte addr1, byte addr2, byte addr3) {

		StringBuffer sb = new StringBuffer();
		short tempShort = 0;

		if (addr1 < 0) {
			tempShort = (short) (256 + addr1);
		} else {
			tempShort = addr1;
		}
		sb.append(Integer.toString(tempShort, 16));
		sb.append(":");

		if (addr2 < 0) {
			tempShort = (short) (256 + addr2);
		} else {
			tempShort = addr2;
		}
		sb.append(Integer.toString(tempShort, 16));
		sb.append(":");

		if (addr3 < 0) {
			tempShort = (short) (256 + addr3);
		} else {
			tempShort = addr3;
		}
		sb.append(Integer.toString(tempShort, 16));

		return sb.toString();
	}

	public static String getPacket(byte[] packet) {
		StringBuffer oBuffer = new StringBuffer();
		int noOfBytes = packet.length;
		for (int i = 0; i < noOfBytes; i++) {
			oBuffer.append(String.format("%x ", packet[i]));
		}
		return oBuffer.toString();
	}

	public static boolean loadPropertyFile(String filePath) {
		boolean bResult = false;
		FileInputStream file;
		String path = filePath;
		try {
			file = new FileInputStream(path);
			configProperties.load(file);
			System.out.println(configProperties.toString());
			file.close();
			bResult = true;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return bResult;
	}

	public static String getPropertyWithName(String PropertyName) {
		return configProperties.getProperty(PropertyName);
	}
}
