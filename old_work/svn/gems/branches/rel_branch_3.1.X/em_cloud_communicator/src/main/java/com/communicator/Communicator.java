package com.communicator;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

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
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringAppContext;

public class Communicator {

	private static long sleepTime = 300 * 1000;
	public static final Logger logger = Logger.getLogger(Communicator.class.getName());
	private static ApplicationContext springContext = null ;
	
	   
	public static void main(String[] args) {
	
			// Some of the URL require certificate authentication, on which server issues a renegotiation request
			// Clients need to honor this renegotiation with the server.
			System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
			
			Random rndGenerator = new Random();
			try {
				Thread.sleep(rndGenerator.nextInt(new Integer((int) sleepTime - 60000)));
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}
	            
			logger.info("--------------------COMMUNICATOR IS STARTING AT "+ new Date() + "----------------------------------");
			//Initialize the logging system
			BasicConfigurator.configure();
			
			//Initialize the Spring factory
			SpringAppContext.init();
			springContext = SpringAppContext.getContext();
			
			SecureCloudConnectionTemplate secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)springContext.getBean("secureCloudConnectionTemplate");
			InitializeSecureConnection.init(secureCloudConnectionTemplate);
			
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
        
	}

	