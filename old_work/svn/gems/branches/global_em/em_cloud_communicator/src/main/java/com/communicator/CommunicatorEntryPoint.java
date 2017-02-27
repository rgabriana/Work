package com.communicator;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communicator.job.CommunicatorJob;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.Communicator;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringAppContext;
import com.communicator.util.SpringContext;

public class CommunicatorEntryPoint {

	private static long sleepTime = 300 * 1000;
	public static final Logger logger = Logger.getLogger(CommunicatorEntryPoint.class.getName());
	private static ApplicationContext springContext = null ;
	
	public static Communicator emCommunicator = new Communicator();
	
	public static int commReset = 0;
	
	public static void main(String[] args) {
	
		// Some of the URL require certificate authentication, on which server issues a renegotiation request
		// Clients need to honor this renegotiation with the server.
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		
		Random rndGenerator = new Random();
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc;
			proc = rt.exec("/opt/enLighted/communicator/last_communication_time.sh");
			proc.waitFor();
			Thread.sleep(rndGenerator.nextInt(new Integer((int) sleepTime - 60000)));
		} catch (InterruptedException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		}
            
		logger.info("--------------------COMMUNICATOR IS STARTING AT "+ new Date() + "----------------------------------");
		//Initialize the logging system
		BasicConfigurator.configure();
		
		//Initialize the Spring factory
		SpringAppContext.init();
		springContext = SpringAppContext.getContext();
		
		Thread monitor = new Thread(new CommunicatorEntryPoint.CommunicationMonitor());
		monitor.start();
		
		ServerInfoManager serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
		while(serverInfoManager.getCloudMode() == null) {
			serverInfoManager.init();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(serverInfoManager.getCloudMode()) {
			SecureCloudConnectionTemplate secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)springContext.getBean("secureCloudConnectionTemplate");
			InitializeSecureConnection.init(secureCloudConnectionTemplate);
		}
		
		//Initialize quartz scheduler
		Scheduler sched = SchedulerManager.getInstance().getScheduler();
		
		JobDetail downloadJob;
		try {
			downloadJob = newJob(CommunicatorJob.class)
					.withIdentity("CommunicatorJob", 
							sched.getSchedulerName())
					.build();
			
			SimpleTrigger downloadTrigger = (SimpleTrigger) newTrigger()
					.withIdentity( "CommunicatorTrigger",
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
					logger.error(e);
				}
			}
		}
	}
        
}

	