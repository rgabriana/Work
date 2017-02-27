package com.ems.BarionetTCPServer;

// TCP Server for sending Barionet statechange commands

import com.ems.XMLParser.*;

//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
import java.util.*;
//import java.util.regex.Pattern;
import java.net.*;
import java.io.*;

public class BarionetTCPServer extends Thread{
	
	InetAddress ip;
	String mac;
	
	public BarionetTCPServer(InetAddress ip, String mac)
	{
		this.ip = ip;
		this.mac = mac;
	}
	
	
	public void run()
	{
		InputStream in = null;
		BufferedReader bf = null;
		BufferedReader bfs = null;
		ServerSocket barionetSocket = null;
		Socket socket = null;
		try{
		XMLParser xml = new XMLParser("BarionetMacPortMapping.xml");
		Map<String,String> macToPortMapping = xml.returnMacAndIOList();
		barionetSocket = new ServerSocket(12302, 50, ip);
		System.out.println("Listening on socket created on 12302 from" + ip + "server accepts commands sent from EM");
		socket = barionetSocket.accept();
		System.out.println("Starting TCP Server" + ip);
		System.out.println(socket.isConnected());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		bfs = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line;
		if(socket.isConnected())
		{
			while ((line=bfs.readLine())!=null)
			{
				in = new FileInputStream(new File (mac+".cfg"));
				bf = new BufferedReader(new InputStreamReader(in));
				//System.out.println(line.equals("version"+"\r\n"));
				if(line.startsWith("iolist"))
			    {
			    	System.out.println("Command from EM Iolist ->" +"\n");
				    out.writeBytes(macToPortMapping.get(mac) + "\n"); 
			    }
				if(line.startsWith("version"))
				{
					System.out.println("Command from EM version->"+ "\n");
					out.writeBytes("version,BARIONET 1.3"+"\n");
				}
			    
			    System.out.println("Read Data from File");
				while((line = bf.readLine()) != null)
				{
					if(!line.trim().isEmpty()){
				 out.writeBytes(line+"\n");
				 out.flush();}
				}
				//out.close();
			}
		}
		else
		{
			System.out.println("Server not listening on 12302 for this " + ip);
		}
		Thread.sleep(10000);
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/*public static void main(String[] args)
	{
		try{
		XMLParser xml = new XMLParser("BarionetMacPortMapping.xml");
		Map<String,String> macToPort = xml.returnMacAndIOList();
		String[] macStrings = xml.getMacs();
		InetAddress localAddress;
		String line;
		int i=0;
		
		NetworkInterface networkInterface = NetworkInterface.getByName("eth1");
		Enumeration <NetworkInterface> networkInterfaces = networkInterface.getSubInterfaces();
		
		while(networkInterfaces.hasMoreElements())
		{
			NetworkInterface Iface = (NetworkInterface) networkInterfaces.nextElement(); 
			Enumeration <InetAddress>  IPAddresses = Iface.getInetAddresses();
			while(IPAddresses.hasMoreElements())
			{
				localAddress = IPAddresses.nextElement();
				BarionetTCPServer tcp = new BarionetTCPServer(localAddress,macStrings[i]);
				tcp.start();
				i++;
			}
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}*/

}
