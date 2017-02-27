package com.ems.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

public class HardwareInfoUtils {

	public static String convertByteArrToIp(byte[] ipArr) {

		StringBuffer sb = new StringBuffer();
		short tempShort = 0;
		for (int i = 0; i < 4; i++) {
			if (i > 0) {
				sb.append(".");
			}
			if (ipArr[i] < 0) {
				tempShort = (short) (256 + ipArr[i]);
			} else {
				tempShort = ipArr[i];
			}
			sb.append(tempShort);
		}
		return sb.toString();

	}

	/**
	 * Fetch the IP address given the interface.
	 * 
	 * @param sIface
	 * @return
	 */
	public static String getIpAddress(String sIface) {
		try {
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) e.nextElement();
				if (ni.getName().equals(sIface)) {
					Enumeration<InetAddress> e2 = ni.getInetAddresses();

					while (e2.hasMoreElements()) {
						InetAddress ip = (InetAddress) e2.nextElement();
						if (ip instanceof Inet4Address)
							return convertByteArrToIp(ip.getAddress());
					}
				}
				Enumeration<NetworkInterface> se = ni.getSubInterfaces();
				while (se.hasMoreElements()) {
					NetworkInterface sni = (NetworkInterface) se.nextElement();
					if (sni.getName().equals(sIface)) {
						Enumeration<InetAddress> e2 = sni.getInetAddresses();

						while (e2.hasMoreElements()) {
							InetAddress ip = (InetAddress) e2.nextElement();
							if (ip instanceof Inet4Address)
								return convertByteArrToIp(ip.getAddress());
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "127.0.0.1";
	}

	public static ArrayList<String> getMacAddress() {
		InetAddress ip;
		ArrayList<String> macList = new ArrayList<String>();

		try {
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				StringBuilder sb = new StringBuilder();
				NetworkInterface ni = (NetworkInterface) e.nextElement();
				byte[] mac = ni.getHardwareAddress();
				if (mac != null) {
					for (int i = 0; i < mac.length; i++) {
						sb.append(String.format("%02X%s", mac[i],
								(i < mac.length - 1) ? "-" : ""));
					}
				}
				macList.add(sb.toString());
			}
		} catch (SocketException e) {

			e.printStackTrace();

		}
		return macList;
	}

}
