package com.ems.BarionetDiscovery;

//Server Program listening on ports 30718 and 30719

import com.ems.XMLParser.*;
import com.ems.BarionetTCPServer.*;

//import java.io.IOException;
//import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.net.*;
//import java.io.*;

public class BarionetDiscoverySimulator extends Thread {
	
	//InetAddress localAddress = null;
	//int portnumber = 30718;
	//InetAddress[] addresses = new InetAddress[10];
	//int iCount=0;
	
    public void run()
    {
      	
	    DatagramSocket socket = null;
		byte[] buf = new byte[256];
		try{		
		socket = new DatagramSocket(30718);	
		socket.setReceiveBufferSize(2048);
		DatagramPacket packet = new DatagramPacket(buf,buf.length);
              while(true){
		socket.receive(packet);
		//Received broadcast from EM
		InetAddress address = packet.getAddress();
		int port = packet.getPort();
		//byte[] data = packet.getData();
		System.out.println(address + "---" + port);
		//System.out.println(addresses[1]);
		//socket.close();
		// Now Send the data back
		sendData(address);
              } 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
    }

	public void sendData(InetAddress address) {
	
		byte[] barixDeviceDetails = prepareResponseBytes();
		byte[] responseByteData = new byte[256];
		int i=0;
		//int j=0;
		//InetAddress[] barionetServerIps = new InetAddress[4];
		System.arraycopy(barixDeviceDetails, 0, responseByteData, 0, barixDeviceDetails.length);
		try {
			//PrintWriter writer = new PrintWriter("ipToMacMapping.txt","UTF-8");
			NetworkInterface networkInterface = NetworkInterface.getByName("eth1");
			Enumeration <NetworkInterface> networkInterfaces = networkInterface.getSubInterfaces();
			XMLParser xml = new XMLParser("BarionetMacPortMapping.xml");
			//Map<String,String> macToPort = xml.returnMacAndIOList();
			String[] macStrings = xml.getMacs();
			
			while(networkInterfaces.hasMoreElements())
			{
				NetworkInterface Iface = (NetworkInterface) networkInterfaces.nextElement(); 
				Enumeration <InetAddress>  IPAddresses = Iface.getInetAddresses();
				while(IPAddresses.hasMoreElements())
				{
					InetAddress localAddress = IPAddresses.nextElement();
					System.out.println("IP Address 1- > "+ localAddress);
					byte[] mac = macAddressGenerator(macStrings[i]);
					System.arraycopy(mac, 0, responseByteData, 5, mac.length);
					byte[] ip = localAddress.getAddress();
					System.arraycopy(ip, 0, responseByteData, 11, ip.length);
					if(!localAddress.isLoopbackAddress() && (localAddress.getAddress().length == 4)){
					//BarionetTCPServer tcp = new BarionetTCPServer(localAddress,macStrings[i]);
					//tcp.start();
					System.out.println("Sending response to discovery to " + address + ", from: " + localAddress + " Mac Address - >" + macStrings[i]);
					DatagramSocket brSockConn = new DatagramSocket(30719, localAddress);
					brSockConn.send(new DatagramPacket(responseByteData, 0, responseByteData.length, new InetSocketAddress(address, 30718)));
                                   System.out.println("Data Sent");
					//writer.println(macStrings[i]+" "+localAddress.toString());
					Thread.sleep(2000);
                    i= i+1;
					brSockConn.close();
					
					}
					//writer.flush();
				}
				//writer.close();
			}
				//System.out.println("Sending response: " + address);
				// Now Send the data back
				
			}catch(Exception e) {
				
			}
	}
	
	
	public byte[] convertIntToByte(int[] paramArrayOfInt) {
		int i = paramArrayOfInt.length;
		byte[] arrayOfByte = new byte[i];
		for (int j = 0; j < i; j++) {
			if (paramArrayOfInt[j] > 127) {
				paramArrayOfInt[j] -= 256;
			}
			arrayOfByte[j] = Integer.valueOf(paramArrayOfInt[j] & 0xFF)
					.byteValue();
		}
		return arrayOfByte;
	}
	
	public byte[] prepareResponseBytes()
	{
		int[] barixID = { 129, 136, 83, 129 };
		byte[] barixIDByte = convertIntToByte(barixID);
		byte byteGetResponse = -127;
		//int intGetResponse = 129;
		byte byteSetResponse = -126;
		//int intSetResponse = 130;
		byte[] BarixBytes = new byte[4];
		System.arraycopy(barixIDByte, 0, BarixBytes, 0, barixIDByte.length);
		byte[] responseBytes1 = new byte[5];
		byte[] responseBytes2 = new byte[5];
		System.arraycopy(BarixBytes, 0, responseBytes1, 0, BarixBytes.length);
		System.arraycopy(BarixBytes, 0, responseBytes2, 0, BarixBytes.length);
		responseBytes1[4] = byteGetResponse;
		responseBytes2[4] = byteSetResponse;
		return responseBytes2;
	}
	
	public byte[] macAddressGenerator(String s)
	{
		int i = 0;
		String[] mac = s.split("-");
		byte[] macAddress = new byte[6];
		for(i = 0; i < mac.length; i++) {
	        macAddress[i] = Integer.decode("0x"+ mac[i]).byteValue();
	        //System.out.println(macAddress[i]);
	    }
		
		return macAddress;
	}
    
    public static void main(String[] args)
    {
    	int k = 0;
    	NetworkInterface networkInterface1 = null;
		try {
			networkInterface1 = NetworkInterface.getByName("eth1");
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Enumeration <NetworkInterface> networkInterfaces1 = networkInterface1.getSubInterfaces();
		XMLParser xml1 = new XMLParser("BarionetMacPortMapping.xml");
		String[] macStrings1 = xml1.getMacs();
		while(networkInterfaces1.hasMoreElements())
		{
			NetworkInterface Iface1 = (NetworkInterface) networkInterfaces1.nextElement(); 
			Enumeration <InetAddress>  IPAddresses1 = Iface1.getInetAddresses();
			while(IPAddresses1.hasMoreElements())
			{
				InetAddress localAddress1 = IPAddresses1.nextElement();
				System.out.println("TCP IP Address - > "+ localAddress1);
				
				BarionetTCPServer tcp = new BarionetTCPServer(localAddress1,macStrings1[k]);
				tcp.start();
				k= k+1;
			}
		}
    	
    	BarionetDiscoverySimulator s = new BarionetDiscoverySimulator();
    	s.start();
    }
    
}
