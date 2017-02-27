package com.communicator.util;

import java.security.MessageDigest;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.communicator.service.EmManager;

public class Globals {
	
	public static final Logger logger = Logger.getLogger(Globals.class.getName());
	
	// communication error state
	final public static int state_comm_success				=	100;	
	final public static int state_comm_url_failed			=	101;
	final public static int state_comm_connect_failed		=	102;
	final public static int state_comm_init_failed			=	103;
	final public static int state_comm_send_failed			=	104;
	final public static int state_comm_read_failed			=	105;
	
	
	public static final String CONNECTION_FAILURE = "CONNECTION_FAILURE";
	
	
	public static boolean uem_enable = false; 
	public static String uem_ip = "";
	public static String uem_username = "";
	public static String uem_password = "";
	public static String appVersion = "";
	
	final public static String HTTPS = "https://";
	public static String  loginUrl= "/uem/em.action" ;
	
	public static String  uemPollUrl= "/uem/services/org/communicate/em/poll" ;
	
	public static String sessionId = "";
	
	public static String  emLoginUrl="https://localhost/ems/wsaction.action" ;
	public static String emSessionId = "";
	
	public static String getUemInfoUrl = "https://localhost/ems/services/uem/getUemInfo";
	
	public static String getSensorInfo = "https://localhost/ems/services/uem/getSensorInfo/";
	
	public static String getAllSensors = "https://localhost/ems/services/uem/getAllSensors/";
	
	public static String getEmFacilityTree = "https://localhost/ems/services/uem/getEmFacilityTreeForUem";
	
	public static String getFloorPlan = "https://localhost/ems/services/uem/getFloorPlan/";
	
	public static String setPeriodicAndRealTimeHB= "https://localhost/ems/services/uem/setPeriodicAndRealTimeHB/";
	
	public static String addUpdateUEMGatewayURL = "https://localhost/ems/services/uem/add/";
	
	public static String setDimLevelURL = "https://localhost/ems/services/org/fixture/setDimLevel/";
	
	public static String getDimLevelsAndConnectivityURL = "https://localhost//ems/services/uem/getDimLevelsAndLastConnectivity";
	
	public static int emConnectionTimeout = 60 * 1000;
	
	public static int uemConnectionTimeout = 300 * 1000;
	
	private static ApplicationContext springContext =  SpringAppContext.getContext() ;
	
	private static String getSSLAuthKey(String authKey, String gwMac) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.reset();
		byte[] hardCodedArr = "EnlightedAuthKey".getBytes();
		md.update(hardCodedArr);
		byte[] keyArr = authKey.getBytes();
		md.update(keyArr);
		byte[] gwMacArr = gwMac.replaceAll(":", "").getBytes();
		md.update(gwMacArr);
		byte[] digest = md.digest();
		return getByteString(digest);
	}
	
	private static String getByteString(byte[] bytes) {
		StringBuffer oBuffer = new StringBuffer();
		int noOfBytes = bytes.length;
		for (int i = 0; i < noOfBytes; i++) {
			oBuffer.append(String.format("%x", bytes[i]));
		}
		return oBuffer.toString();
	}
	
	public static String getPassword() {
		try {
			return getSSLAuthKey(uem_password, uem_username);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getUsername() {
		return uem_username.replaceAll(":", "");
	}
	
	
	
	
}

