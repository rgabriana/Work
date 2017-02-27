package com.motion.server;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import com.motion.utils.CommonUtils;
import com.motion.utils.MotionAppConstants;
import com.motion.utils.SchedulerManager;
import com.motion.utils.SpringAppContext;




/**
 *  Main Class
 *
 */
public class ServerMain 
{
	public static final Logger logger = Logger.getLogger(ServerMain.class.getName());
	private static AbstractApplicationContext  springContext = null ;
    public static void main( String[] args )
    {
    	logger.info("--------------------MOTION CONTROLLER APP IS STARTING AT "+ new Date() + "----------------------------------");
    	//Initializing the properties
		logger.info("Initializing the properties.........") ;
		initProperties()  ;
    	
    	//Initialize the Spring factory
		SpringAppContext.init();
		springContext = SpringAppContext.getContext();
		springContext.registerShutdownHook();
		
		//Initialize quartz scheduler
		SchedulerManager.getInstance();
		
		logger.info("Starting Udp Listner........");
		UdpListener udpListener = (UdpListener)springContext.getBean("udpListener");		
		udpListener.startListening() ;
		
    }
    private static void initProperties() 
    {	
    	try {
		    	File configFile = new File(MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
		    	if(configFile.exists())
		    	{
					MotionAppConstants.LINE_WIDTH = CommonUtils.getPropertyWithName("lineWidth", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.LINE_PATTERN = CommonUtils.getPropertyWithName("linePattern", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.BLOB_RADIUS = CommonUtils.getPropertyWithName("blobRadius", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.DATA_SEND_DELAY = CommonUtils.getPropertyWithName("dataSendDelay", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.REMOTE_HOST = CommonUtils.getPropertyWithName("remoteIp", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.REMOTE_PORT = Integer.parseInt(CommonUtils.getPropertyWithName("remoteUdpPort", MotionAppConstants.CONFIG_PROPERTIES_PATH)) ;
					MotionAppConstants.FIXTURE_DETAILS = CommonUtils.getPropertyWithName("fixtureDetails", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.FLOOR_DETAILS = CommonUtils.getPropertyWithName("floorDetails", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
					MotionAppConstants.BLOB_UNIFICATION_FACTOR = CommonUtils.getPropertyWithName("blobUnificationFactor", MotionAppConstants.CONFIG_PROPERTIES_PATH) ;
		    	} else
		    	{
		    		logger.error("Config file missing. Application will exit as properties are not initialized.") ;
		    		System.exit(1);
		    	}
    	}catch (Exception e)
    	{
    		logger.error(e.getMessage() + "Application will exit") ;
    		System.exit(1);
    	}
    }
}
