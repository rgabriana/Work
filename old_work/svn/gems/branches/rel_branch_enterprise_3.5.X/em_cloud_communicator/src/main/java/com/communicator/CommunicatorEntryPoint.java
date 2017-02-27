package com.communicator;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.communication.utils.JsonUtil;
import com.communicator.dao.CloudConfigDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.job.CleanupJob;
import com.communicator.job.CommunicatorJob;
import com.communicator.job.DataPrepJob;
import com.communicator.manager.EmManager;
import com.communicator.manager.ServerInfoManager;
import com.communicator.manager.UEMHeartBeatManager;
import com.communicator.uem.UemCommunicator;
import com.communicator.uem.UemParamType;
import com.communicator.uem.UemRequest;
import com.communicator.uem.UemResponse;
import com.communicator.util.Communicator;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringAppContext;
import com.communicator.util.SpringContext;

public class CommunicatorEntryPoint {

	private static long sleepTime = 300 * 1000;
	public static final Logger logger = Logger.getLogger(CommunicatorEntryPoint.class.getName());
	private static ApplicationContext springContext = null ;
	
	public static Communicator emCommunicator = new Communicator();
	
	public static UemCommunicator uemCommunicator = null;
	
	public static int commReset = 0;
	public static boolean remigrationRequired = false;
	public static long remigrationDelay = 180 * 24 * 60 * 60 * 1000L;	//180 days
	
	private static EmManager emManager = null;
	
	private static boolean uemStart = false;
	private static boolean cloudStart = false;
	
	private static ArrayList<com.communicator.uem.NameValue> responseNameValue = new ArrayList<com.communicator.uem.NameValue>();
	
	public static void main(String[] args) {
	
		// Some of the URL require certificate authentication, on which server issues a renegotiation request
		// Clients need to honor this renegotiation with the server.
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		
		Random rndGenerator = new Random();
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc;
			proc = rt.exec(System.getenv().get("OPT_ENLIGHTED")+"/communicator/last_communication_time.sh");
			proc.waitFor();
			Thread.sleep(rndGenerator.nextInt(new Integer((int) sleepTime - 60000)));
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
            
		logger.info("--------------------COMMUNICATOR IS STARTING AT "+ new Date() + "----------------------------------");
		//Initialize the logging system
		BasicConfigurator.configure();
		
		//Initialize the Spring factory
		SpringAppContext.init();
		springContext = SpringAppContext.getContext();
		
		Thread monitor = new Thread(new CommunicatorEntryPoint.CommunicationMonitor());
		monitor.start();
		
		Thread cloudStart = new Thread(new CommunicatorEntryPoint.CloudStart());
		cloudStart.start();
		
		Thread uemStart = new Thread(new CommunicatorEntryPoint.UemStart());
		uemStart.start();
			
	} 
	
	
	private static void poll() {
		UemRequest uemRequest = new UemRequest();
		uemRequest.setNameval(responseNameValue);
		String sb = uemCommunicator.webServicePostRequest(JsonUtil.getJSONString(uemRequest) ,CommunicatorConstant.HTTPS + CommunicatorConstant.uem_ip + CommunicatorConstant.uemPollUrl , CommunicatorConstant.uemConnectionTimeout, "text/plain", null);
		logger.info(CommunicatorConstant.HTTPS + CommunicatorConstant.uem_ip + CommunicatorConstant.uemPollUrl + "::" + sb);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb)) {
			try {
				Thread.sleep(60000);
				emManager.getUemInfo();
			} catch (InterruptedException e) {
				logger.error(e);
			}
			return;
		}
		else {
			process(sb);
		}
	}
	
	private static void process(String sb) {
		responseNameValue = new ArrayList<com.communicator.uem.NameValue>();
		
		com.communicator.uem.JsonUtil<UemResponse> jsonUtil = new com.communicator.uem.JsonUtil<UemResponse>();
		UemResponse uemResponse = jsonUtil.getUemResponseObject(sb, UemResponse.class);
		HashMap<UemParamType, String> respMap = uemResponse.getNameValueMap();

		if(respMap.containsKey(UemParamType.RequestType)) {
			try{
				if((UemParamType.valueOf(respMap.get(UemParamType.RequestType)).getName().equals(respMap.get(UemParamType.RequestType)))) {
					switch (UemParamType.valueOf(respMap.get(UemParamType.RequestType))) {
						case RequestFacilityTree: {
							String data = emManager.getEmFacilityTree(); //Get TreeNode
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, data));
							}
							break;
							
						}
						case AddUEMGateway: {
							String payload = respMap.get(UemParamType.PayLoad);
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.addUpdateUEMGateway(payloads[0],payloads[1], payloads[2]); //Get status. Accept host :::: port :::: key
							}
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, data));
							}
							break;								
						}
						case RequestAllSensors: {
							String data = emManager.getAllSensors(); //Get SensorList
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case RequestSensor : {
							String data = emManager.getSensorData(respMap.get(UemParamType.PayLoad));	//Get Sensor. Accept macAddress
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case RequestFloorPlan : {
							String data = emManager.getFloorPlan(Long.parseLong(respMap.get(UemParamType.PayLoad))); //Get planMap encoded. Accept em floor 1.
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case RequestDimLevelAndLastConnectivity : {
							String data = emManager.getDimLevelAndLastConnectivityAt(respMap.get(UemParamType.PayLoad)); //Get SensorList. Accept xml fixtures list with mac address.
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case SetOccChangeTrigger: {
							String payload = respMap.get(UemParamType.PayLoad);
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.setOccChangeTrigger(payloads[0], Short.parseShort(payloads[1]), Short.parseShort(payloads[2]), Short.parseShort(payloads[3])); //Get SensorList. Accept xml fixtures list with mac address :::: enable :::: triggerDelayTime ::::ack 
							}
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
							}
							break;
						}
						case SetDimLevel: {
							String payload = respMap.get(UemParamType.PayLoad);
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.setDimLevel(payloads[0],Integer.parseInt(payloads[1]), Integer.parseInt(payloads[2])); //Get SensorList. Accept xml fixtures list with mac address :::: percentage :::: time
							}
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
							}
							break;
						}
						default : {
							logger.warn("============No Task=======");
							responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
							responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "S"));
						}
					}
				}
			}
			catch (IllegalArgumentException e) {
				responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
				responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.SuccessAck, "E"));
				responseNameValue.add(new com.communicator.uem.NameValue(UemParamType.PayLoad, e.getMessage()));
				logger.error(e);
			}
		}
		
	}
	
	private static void connectToUEM() {
		SystemConfigDao sysConfigDao = (SystemConfigDao) SpringContext.getBean("systemConfigDao");
		String uem_enable = sysConfigDao.getSysConfigValue("uem.enable");
		String uem_pkt_forwarding_enable = sysConfigDao.getSysConfigValue("uem.pkt.forwarding.enable");
		
		if ("1".equals(uem_enable) && "1".equals(uem_pkt_forwarding_enable)) {
			logger.info("------------ Connecting to UEM ------------");
			UEMHeartBeatManager uemHeartbeatManager = (UEMHeartBeatManager) springContext
					.getBean("uemHeartbeatManager");

			uemCommunicator = UemCommunicator.getInstance();
			emManager = (EmManager) springContext.getBean("emManager");
			emManager.getUemInfo();

			uemHeartbeatManager.callUEM();

			while (true) {
				uemStart = true;
				try {
					commReset = 0;
					if (CommunicatorConstant.uem_ip != null
							&& !"".equals(CommunicatorConstant.uem_ip)
							&& CommunicatorConstant.uem_password != null
							&& !"".equals(CommunicatorConstant.uem_password)
							&& !" ".equals(CommunicatorConstant.uem_password)
							&& CommunicatorConstant.uem_username != null
							&& !"".equals(CommunicatorConstant.uem_username)) {
						uemHeartbeatManager.checkUpdateEMIp();
						poll();
					} else {
						logger.info("UEM location = "
								+ CommunicatorConstant.HTTPS
								+ CommunicatorConstant.uem_ip
								+ " and em is not registered.");
						Thread.sleep(60000);
						emManager.getUemInfo();
						uemHeartbeatManager.callUEM();
					}
				} catch (InterruptedException e) {
					logger.error(e);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}
	
	private static void connectToCloud() {
		SystemConfigDao sysConfigDao = (SystemConfigDao) SpringContext.getBean("systemConfigDao");		
		String cloud_enable = sysConfigDao.getSysConfigValue("enable.cloud.communication");
		
		if ("1".equals(cloud_enable)) {
			logger.info("------------ Connecting to Cloud ------------");
			CloudConfigDao cloudConfigDao = (CloudConfigDao) SpringContext.getBean("cloudConfigDao");
			if ("-1".equals(cloudConfigDao.getCloudConfigValue(CommunicatorConstant.sysConfigSuccessfulSyncTime))) 
			{
				cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime,
						((Long) (new Date().getTime())).toString());
			}

			remigrationRequired = "1".equals(cloudConfigDao.getCloudConfigValue(CommunicatorConstant.sysConfigRemigrationRequired));

			ServerInfoManager serverInfoManager = (ServerInfoManager) SpringContext.getBean("serverInfoManager");
			while (serverInfoManager.getCloudMode() == null) {
				serverInfoManager.init(true);
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
			cloudStart = true;

			if (serverInfoManager.getCloudMode()) {
				SecureCloudConnectionTemplate secureCloudConnectionTemplate = (SecureCloudConnectionTemplate) springContext
						.getBean("secureCloudConnectionTemplate");
				InitializeSecureConnection.init(secureCloudConnectionTemplate);
			}

			while (!serverInfoManager.getCloudMode()
					&& (CloudConnectionTemplate.key == null
							|| CloudConnectionTemplate.key.equals("") || CloudConnectionTemplate.key
								.equals("key"))) {
				serverInfoManager.init(true);
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}

			// Initialize quartz scheduler
			Scheduler sched = SchedulerManager.getInstance().getScheduler();

			try {
				JobDetail syncDataJob = newJob(DataPrepJob.class).withIdentity(
						"DataPrepJob", sched.getSchedulerName()).build();

				SimpleTrigger syncDataTrigger = (SimpleTrigger) newTrigger()
						.withIdentity("DataPrepTrigger",
								sched.getSchedulerName())
						.startNow()
						.withSchedule(
								SimpleScheduleBuilder.simpleSchedule()
										.withIntervalInMilliseconds(sleepTime)
										.repeatForever()).build();

				sched.scheduleJob(syncDataJob, syncDataTrigger);
			} catch (SchedulerException e) {
				logger.error(e.getMessage(), e);
			}

			JobDetail downloadJob;
			try {
				downloadJob = newJob(CommunicatorJob.class).withIdentity(
						"CommunicatorJob", sched.getSchedulerName()).build();

				SimpleTrigger downloadTrigger = (SimpleTrigger) newTrigger()
						.withIdentity("CommunicatorTrigger",
								sched.getSchedulerName())
						.startNow()
						.withSchedule(
								SimpleScheduleBuilder.simpleSchedule()
										.withIntervalInMilliseconds(sleepTime)
										.repeatForever()).build();

				sched.scheduleJob(downloadJob, downloadTrigger);
			} catch (SchedulerException e) {
				logger.error(e.getMessage(), e);
			}

			JobDetail cleanupJob;
			try {
				cleanupJob = newJob(CleanupJob.class).withIdentity(
						"CleanupJob", sched.getSchedulerName()).build();

				Date startAt = new Date((new Date()).getTime() + 10 * 60 * 1000);

				SimpleTrigger cleanupTrigger = (SimpleTrigger) newTrigger()
						.withIdentity("CleanupTrigger",
								sched.getSchedulerName())
						.startAt(startAt)
						.withSchedule(
								SimpleScheduleBuilder.simpleSchedule()
										.withIntervalInHours(24)
										// .withIntervalInMinutes(1)
										.repeatForever()).build();

				sched.scheduleJob(cleanupJob, cleanupTrigger);

				logger.info("Scheduled cleanup job to be run every 24 hours starting "
						+ startAt);
			} catch (SchedulerException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	static class CommunicationMonitor implements Runnable {

		@Override
		public void run() {
			while (true) {
				try{
					if(commReset > 12) {
						logger.error("Communicator is stuck for a long time. Killing the process.");
						System.exit(0);
					}
					commReset++;
					Thread.sleep(300000);
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
	
	static class CloudStart implements Runnable {

		@Override
		public void run() {
			while (!cloudStart) {
				try{
					connectToCloud();					
					Thread.sleep(300000);
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
	
	static class UemStart implements Runnable {
		@Override
		public void run() {
			while (!uemStart) {
				try{
					connectToUEM();					
					Thread.sleep(300000);
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e);
					try {Thread.sleep(120000);} catch (InterruptedException e1) {}
				}
			}
		}
	}
        
}

	