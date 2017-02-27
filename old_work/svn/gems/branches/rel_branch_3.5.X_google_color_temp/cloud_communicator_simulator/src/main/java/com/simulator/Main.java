package com.simulator;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;

import com.communication.template.CloudConnectionTemplate;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communicator.CommunicatorEntryPoint;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringAppContext;

public class Main {
	
	private static int sleepTime = 300 * 1000;
	static String configFile = "config.prop";
	
	private static ApplicationContext springContext = null ;
	
	public static final Logger logger = Logger.getLogger(Main.class.getName());
	
	public static Long lastKnownMaxWalId = -99L;
	public static Long lastComputedMaxWalId = -99L;
	
	public static Date lastKnownMaxCaptureAt = new Date();
	public static Date lastComputedMaxCaptureAt = new Date();
	
	public static Long lastKnownECMaxId = -1L;
	public static Long lastComputedECMaxId = -1L;
	
	
	public static String db;
	public static String masterIp;
	public static String replicaIp;
	public static String sensors;
	public static String appVersion;
	public static String emMac;
	public static Map<String, Long> fixtureList = new HashMap<String, Long>(); 
	
	public static void main(String[] args) {
		logger.info("Starting simulator for....");
		db = getPropertyWithName("dbName", configFile);
		logger.info("db = " + db);
		masterIp = getPropertyWithName("masterIp", configFile);
		logger.info("masterIp = " + masterIp);
		replicaIp = getPropertyWithName("replicaIp", configFile);
		logger.info("replicaIp = " + replicaIp);
		sensors = getPropertyWithName("sensors", configFile);
		logger.info("sensors = " + sensors);
		appVersion = getPropertyWithName("appVersion", configFile);
		logger.info("appVersion = " + appVersion);
		emMac = getPropertyWithName("emMac", configFile);
		logger.info("emMac = " + emMac);	
		
		//Initialize the logging system
		BasicConfigurator.configure();
		
		//Initialize the Spring factory
		SpringAppContext.init();
		springContext = SpringAppContext.getContext();
		Random rndGenerator = new Random();
		SecureCloudConnectionTemplate secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)springContext.getBean("secureCloudConnectionTemplate");
		InitializeSecureConnection.init(secureCloudConnectionTemplate);
		try {
			Thread.sleep(rndGenerator.nextInt(sleepTime));
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		lastKnownMaxWalId = getMaxWalId(replicaIp, db);
		
		String maxEcId = getPropertyWithName("ecMaxId", configFile);
		String maxCaptureAt = getPropertyWithName("maxCaptureAt", configFile);
		if(maxEcId != null && !"".equals(maxEcId)) {
			lastKnownECMaxId = Long.parseLong(maxEcId);
			lastKnownMaxCaptureAt = new Date(Long.parseLong(maxCaptureAt));
		}
		else {
			getMaxCaptureAtAndMaxId(replicaIp, db);
			setPropertyWithName("ecMaxId", lastKnownECMaxId.toString(), "maxCaptureAt", new Long(lastKnownMaxCaptureAt.getTime()).toString());
		}
		
		
		logger.info("max captureAt = " + lastKnownMaxCaptureAt);
        logger.info("max EC Id = " + lastKnownECMaxId);
		
		for (String mac: Arrays.asList(sensors.split(","))) {
			mac = mac.trim();
			fixtureList.put(mac, getSensorId(replicaIp, db, mac));
		}
		
		ServerInfoManager serverInfoManager = (ServerInfoManager)springContext.getBean("serverInfoManager");	    
	    
	    serverInfoManager.setAppVersion(appVersion);
	    serverInfoManager.setCloudSyncType("2");
	    serverInfoManager.setHost(masterIp);
	    serverInfoManager.setReplicaServerIP(replicaIp);
	    serverInfoManager.setMacAddress(emMac);
	    CloudConnectionTemplate.macId = emMac;
	    
	    Thread monitor = new Thread(new Main.CommunicationMonitor());
		monitor.start();

	    //Initialize quartz scheduler
		Scheduler sched = SchedulerManager.getInstance().getScheduler();
		
		JobDetail downloadJob;
		try {
			downloadJob = newJob(SimulatorCommunicatorJob.class)
					.withIdentity("SimulatorCommunicatorJob", 
							sched.getSchedulerName())
					.build();
			
			SimpleTrigger downloadTrigger = (SimpleTrigger) newTrigger()
					.withIdentity( "SimulatorCommunicatorTrigger",
							sched.getSchedulerName())
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							        .withIntervalInMilliseconds(sleepTime)
							        .repeatForever())
					        .build();
			
			sched.scheduleJob(downloadJob, downloadTrigger);
		} catch (SchedulerException e) {
			logger.error(e.toString());
		}
		
		
	}
	
	public static String getPropertyWithName(String PropertyName,
			String filePath) {
	  String result = null ;
	  if(new File(filePath).exists()){
		Properties mainProperties = new Properties();
		FileInputStream file = null;
		String path = filePath;
		try {
			file = new FileInputStream(path);
			mainProperties.load(file);
			result =mainProperties.getProperty(PropertyName);		
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			if(file != null) {
				try {
					file.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		if (result != null){
			return result.trim(); 
		}
		else {
			return null;
		}
		
	  }else
	  {
		  logger.error("File "+filePath +" does not exist");
		  return null;
	  }
		
	}
	
	
	public static void setPropertyWithName(String name1, String value1, String name2, String value2) {
		FileOutputStream out = null;
		FileInputStream in = null;
		
        try {
        	in = new FileInputStream(configFile);
        	Properties props = new Properties();
    		props.load(in);
    		
    		out = new FileOutputStream(configFile);
            props.setProperty(name1, value1);
            if(name2 != null) {
            	props.setProperty(name2, value2);
            }
            props.store(out, null);
       }
        catch (Exception e ) {
            logger.error(e.getMessage(), e);
        }
        finally{
        	if(in != null){
                try {
                    in.close();
                } 
                catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if(out != null){
                try {
                    out.close();
                } 
                catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
		
	}
	
	public static Long getSensorId(String replica, String dbName, String mac) {
		Connection connection = null;
        Statement selectStmt = null;
        Long id = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://" + replica + ":5432/" + dbName +"?characterEncoding=utf-8", "postgres", "postgres");
            //connection = DriverManager.getConnection("jdbc:postgresql://192.168.4.206/em_150_2261?characterEncoding=utf-8", "postgres", "postgres");
            selectStmt = connection.createStatement();
            ResultSet rs = selectStmt.executeQuery("SELECT id from device where mac_address = '" + mac + "'");
            while(rs.next())
            {
            	id = rs.getLong(1);
                logger.info("sensor id for " + mac + "  = " + id);
            }
        }
        catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }finally {
            try {
                selectStmt.close();
                connection.close();
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
        }
        return id;
	}
	
	public static Long getMaxWalId(String replica, String dbName) {
		Connection connection = null;
        Statement selectStmt = null;
        Long walId = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://" + replica + ":5432/" + dbName +"?characterEncoding=utf-8", "postgres", "postgres");
            //connection = DriverManager.getConnection("jdbc:postgresql://192.168.4.206/em_150_2261?characterEncoding=utf-8", "postgres", "postgres");
            selectStmt = connection.createStatement();
            ResultSet rs = selectStmt.executeQuery("SELECT val from cloud_config where name = 'lastWalSyncId'");
            while(rs.next())
            {
            	walId = rs.getLong(1);
                logger.info("max wal id = " + walId);
            }
        }
        catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }finally {
            try {
                selectStmt.close();
                connection.close();
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
        }
        return walId;
	}
	
	
	public static void getMaxCaptureAtAndMaxId(String replica, String dbName) {
		Connection connection = null;
        Statement selectStmt = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://" + replica + ":5432/" + dbName +"?characterEncoding=utf-8", "postgres", "postgres");
            //connection = DriverManager.getConnection("jdbc:postgresql://192.168.4.206/em_150_2261?characterEncoding=utf-8", "postgres", "postgres");
            selectStmt = connection.createStatement();
            ResultSet rs = selectStmt.executeQuery("SELECT max(capture_at), max(id) from energy_consumption");
            while(rs.next())
            {
            	lastKnownMaxCaptureAt = rs.getTimestamp(1);
            	lastKnownECMaxId = rs.getLong(2);
            }
        }
        catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }finally {
            try {
                selectStmt.close();
                connection.close();
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
        }
	}
	
	static class CommunicationMonitor implements Runnable {

		@Override
		public void run() {
			while (true) {
				try{
					if(CommunicatorEntryPoint.commReset > 12) {
						logger.error("Communicator is stuck for a long time. Killing the process.");
						System.exit(0);
					}
					CommunicatorEntryPoint.commReset++;
					Thread.sleep(300000);
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

}
