package com.enlightedinc.hvac.utils;

public class Globals {

	public static int state;
	
	// communication error state
	final public static int state_comm_success				=	100;	
	final public static int state_comm_url_failed			=	101;
	final public static int state_comm_connect_failed		=	102;
	final public static int state_comm_init_failed			=	103;
	final public static int state_comm_send_failed			=	104;
	final public static int state_comm_read_failed			=	105;
	
	public static String em_ip = "";
	public static String em_username = "";
	public static String em_password = "";
	
	public static boolean enabledAvgTemperature = false;
	public static boolean enabledAmbientLight = false;
	public static boolean enabledSensorPower = false;
	public static boolean enabledMotionBits = false;
	public static boolean enabledDimLevels = false;
	public static boolean enabledFixtureOutages = false;
	public static boolean enabledLastOccupancy = false;
	public static boolean enableStatsPolling = false;

	public static String buffer			= "";
	
	public static String propFile = "" ;
	final public static String HTTPS = "https://";
	public static String  loginUrl= "/ems/wsaction.action" ;
	
	public static String sessionId = "";
	
	
	
	public static String getAvgTemperatureURL = "/ems/services/org/ec/getAvgTemperature/"; // + {lastSyncId}/{toSyncId}
	public static String getAmbientLightURL = "/ems/services/org/ec/getAmbientLight/"; // + {lastSyncId}/{toSyncId}
	public static String getSensorPowerURL = "/ems/services/org/ec/getSensorPower/"; // + {lastSyncId}/{toSyncId}
	public static String getMotionBitsURL = "/ems/services/org/ec/getMotionBits/"; // + {lastSyncId}/{toSyncId}
	
	public static String getDimLevelsURL = "/ems/services/org/fixture/getDimLevels";
	public static String getFixtureOutagesURL = "/ems/services/org/fixture/getFixtureOutages";
	public static String getLastOccupancyURL = "/ems/services/org/fixture/getLastOccupancy";
	
	public static String setDimLevelURL = "/ems/services/org/fixture/setDimLevel";
	public static String getRealTimeStatsURL = "/ems/services/org/fixture/getRealTimeStats";
	
	public static long emPollingInterval = 300000; 
	public static long emStatsPollingInterval = 10000;
	
}
