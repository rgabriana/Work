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

	
	public static byte[] getMacAddressForIp(String ip)
	{
		
		try {
	 
			InetAddress add = InetAddress.getByName(ip);
			NetworkInterface network = NetworkInterface.getByInetAddress(add);
			byte[] mac = new byte[5] ;
		    mac = network.getHardwareAddress();	 
			/*String sb =null ;
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));		
			}*/
			//sb= macBytetoString(':',mac) ;
			
		
			return mac ;
		
		} catch (UnknownHostException e) {
	 
			e.printStackTrace();
	 
		} catch (SocketException e){
	 
			e.printStackTrace();
	 
		}
		return null ;
	 
	   }
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
	//used for mac address
	public static String macBytetoString(char ch ,byte[] macAddress) {
		StringBuffer sb = new StringBuffer( 17 );
		for ( int i=44; i>=0; i-=4 ) {
			int nibble =  ((int)( byte2Long(macAddress) >>> i )) & 0xf;
			char nibbleChar = (char)( nibble > 9 ? nibble + ('A'-10) : nibble + '0' );
			sb.append( nibbleChar );
			if ( (i & 0x7) == 0 && i != 0 ) {
				sb.append( ch );
			}
		}
		return sb.toString();		
	}
	//used for mac address
	public static long byte2Long(byte addr[]) {
	    long address = 0;
		if (addr != null) {
		    if (addr.length == 6) {
			address = unsignedByteToLong(addr[5]);
			address |= (unsignedByteToLong(addr[4]) << 8);
			address |= (unsignedByteToLong(addr[3]) << 16);
			address |= (unsignedByteToLong(addr[2]) << 24);
			address |= (unsignedByteToLong(addr[1]) << 32);
			address |= (unsignedByteToLong(addr[0]) << 40);
		    } 
		} 
		return address;
	}	
	//used for mac address
	public static long unsignedByteToLong(byte b) {
	    return (long) b & 0xFF;
	}
	
	}

