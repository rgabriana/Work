package com.ems.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ems.server.SchedulerManager;


@Service("bacnetSchedulerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BacnetSchedulerManager {
	
	static final Logger logger = Logger.getLogger("BacnetLog");
	
	JobDetail bacnetSchedulerJob;
	
    public void startBacnetScheduler(){
    	//System.out.println("In startBacnetScheduler() start");
    	String cronstatement = "0 0/15 * * * ?"; // runs every 15 minutes every day
    	
    	String bacnetSchedulerJobName = "bacnetSchedulerJob";
		String bacnetSchedulerTriggerName = "bacnetSchedulerJobTrigger";
		
		try {
			// check if job exist, if not create.
			// Delete the older Quartz job and create a new one
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(bacnetSchedulerJobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(bacnetSchedulerJobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					logger.debug("Failed to delete Quartz job" + bacnetSchedulerJobName);
			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			//System.out.println("In startBacnetScheduler() exception 1");
		}
		
		
		try {
				
			// create job
			bacnetSchedulerJob = newJob(BacnetSchedulerJob.class)
					.withIdentity(
							bacnetSchedulerJobName,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName()).build();
			// create trigger
			CronTrigger bacnetSchedulerJobTrigger = (CronTrigger) newTrigger()
					.withIdentity(
							bacnetSchedulerTriggerName,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName())
					.withSchedule(
							CronScheduleBuilder.cronSchedule(cronstatement))
					.startNow().build();

			// schedule job
			SchedulerManager.getInstance().getScheduler()
					.scheduleJob(bacnetSchedulerJob, bacnetSchedulerJobTrigger);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			//System.out.println("In startBacnetScheduler() exception 2");
		}
		
		//System.out.println("In startBacnetScheduler() end");
			
	}
    
    public void stopBacnetScheduler(){
    	
    	//System.out.println("In stopBacnetScheduler() start");
    	
    	String bacnetSchedulerJobName = "bacnetSchedulerJob";
		
    	try {
			// check if job exist
			// Delete the older Quartz job
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(bacnetSchedulerJobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(bacnetSchedulerJobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					logger.debug("Failed to delete Quartz job" + bacnetSchedulerJobName);
			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			//System.out.println("In stopBacnetScheduler() exception");
		}
		
		//System.out.println("In stopBacnetScheduler() end");
    }

}
