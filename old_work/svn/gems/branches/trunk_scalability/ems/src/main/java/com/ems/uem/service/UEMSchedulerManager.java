package com.ems.uem.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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
	
	public boolean addUEMSchedulerJob(String userPath)
	{		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;
		
	}
	

}
