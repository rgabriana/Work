package com.motion.utils;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MotionAppConstants {
	
	public static final String MOTION_PACKET_TRANSFORMER_JOB_NAME = "MotionPacketTransformer";
	public static final String MOTION_PACKET_HEADER="CMS" ;
	public static final String MOTION_PACKET_FOOTER="END" ;
	public static final String DISPLAY_DATA_SEND_JOB_NAME = "DisplayDataSendJob";
	public static BlockingQueue<HashMap<String, String>> displayDataSendQueue = new LinkedBlockingQueue<HashMap<String, String>> (1000);
	public static HashMap<String,String> blobColorMap= new HashMap<String,String>() ; 
	public static final String CONFIG_PROPERTIES_PATH = "./config.properties";
	
	
	//Variables initialized from config.properties file
	public static  String LINE_WIDTH = null;
	public static  String BLOB_RADIUS= null;
	public static  String LINE_PATTERN= null;
	public static Integer REMOTE_PORT = null ; 
	public static String REMOTE_HOST = null ;
	public static String DATA_SEND_DELAY = null ;
	public static String FIXTURE_DETAILS =null;
	public static String FLOOR_DETAILS = null;
	public static String BLOB_UNIFICATION_FACTOR = null ;

}
