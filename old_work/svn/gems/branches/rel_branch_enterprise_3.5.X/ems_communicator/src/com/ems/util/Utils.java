package com.ems.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Utils {
	public static String getMacAddress( String macGenerationIp ) {
		InetAddress ip;
		StringBuilder sb = new StringBuilder();
		try {

			ip = InetAddress.getByName(macGenerationIp);

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			byte[] mac = network.getHardwareAddress();

			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i],
						(i < mac.length - 1) ? "-" : ""));
			}

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (SocketException e) {

			e.printStackTrace();

		}
		return sb.toString();
	}

}
