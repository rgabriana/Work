package com.ems.uem.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.server.SchedulerManager;
import com.ems.uem.scheduler.UEMScheduler;

@Service("uemSchedulerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UEMSchedulerManager {
	private static final Logger logger = Logger.getLogger("WSLogger");
	public boolean addUEMSchedulerJob(String userPath)
	{	
		logger.info("Activating GLEM scheduler job");
		//String version="";
		JobDetail job = newJob(UEMScheduler.class)
	    .withIdentity("uemservicejob1", "uemjobgroup")	    
	    .build();
		
		//version = ServerMain.getInstance().getGemsVersion();
		
		Trigger trigger = newTrigger()
	    .withIdentity("uemservicejobtrigger1", "uemjobgroup")	    
	    .startNow()
	    .withSchedule(simpleSchedule()
	            .withIntervalInSeconds(300)
	            .repeatForever())
	    .build();
		
		try {
			// Add the job only if it does not exist
			if (SchedulerManager.getInstance().getScheduler().checkExists(
					new JobKey("uemservicejob1", "uemjobgroup")) == false) {
				SchedulerManager.getInstance().getScheduler().scheduleJob(job,
						trigger);
			}
		} catch (SchedulerException e) {
			logger.error(e);
		}
		
		
		return true;
		
	}
	

}
