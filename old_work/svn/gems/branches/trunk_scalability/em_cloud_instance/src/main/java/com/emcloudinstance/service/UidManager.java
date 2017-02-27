package com.emcloudinstance.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;

import com.emcloudinstance.util.SchedulerManager;




public class UidManager {
	
		
	public static void startUidJob() {
		try {
		// Create quartz job
        JobDetail uidJob = newJob(UidJob.class)
                .withIdentity("UidJobName", SchedulerManager.getInstance().getScheduler().getSchedulerName())
                .build();

        // Create Quartz trigger
        SimpleTrigger uidTrigger = (SimpleTrigger) newTrigger() 
                .withIdentity("UidTriggerName", SchedulerManager.getInstance().getScheduler().getSchedulerName())
                .startNow()
                .withSchedule(simpleSchedule()
                .withIntervalInSeconds(120)
            	.repeatForever())
                .build();
        
        SchedulerManager.getInstance().getScheduler().scheduleJob(uidJob, uidTrigger);	
		
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
}
