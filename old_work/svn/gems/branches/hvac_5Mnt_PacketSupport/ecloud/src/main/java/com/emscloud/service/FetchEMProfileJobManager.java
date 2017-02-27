package com.emscloud.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.action.SpringContext;
import com.emscloud.job.CreateUEMProfileJob;
import com.emscloud.job.FetchEMProfileJob;
import com.emscloud.job.ProfileGroupSyncJob;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.util.SchedulerManager;

@Service("fetchEMProfileJobManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FetchEMProfileJobManager {
   
    JobDetail fetchEMProfileJob;
    JobDetail createUEMProfileJob;
    JobDetail profileGroupSyncJob;
    Scheduler sched = SchedulerManager.getInstance().getScheduler();
    static final Logger logger = Logger.getLogger("CloudProfile");
    Properties prop = new Properties();
	public void scheduleProfileFetch() throws SchedulerException {
	   String jobName = "fetchEMProfileJob";
	   String triggerName = "fetchEMProfileTrigger";
	    try {
	    	
	    	if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(jobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(jobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					logger.debug("Failed to delete Quartz job" + jobName);
			}
	    	
	        String cronstatement="0 0 0/1 * * ?"; // Default cron statement to run the job hourly
	        SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
	        if (systemConfigurationManager != null) {
	            SystemConfiguration cronSetting = systemConfigurationManager.loadConfigByName("DOWNLOAD_PROFILE_CRON_SETTING");
	            cronstatement = cronSetting.getValue();
            }
	        
	        fetchEMProfileJob = newJob(FetchEMProfileJob.class)
                    .withIdentity(jobName, 
                            sched.getSchedulerName())
                    .build();
	        CronTrigger fetchEMProfileTrigger = (CronTrigger) newTrigger()
                    .withIdentity(triggerName,SchedulerManager.getInstance().getScheduler().getSchedulerName())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronstatement))
                    .startNow()
                    .build();
            
            SchedulerManager.getInstance().getScheduler()
                    .scheduleJob(fetchEMProfileJob, fetchEMProfileTrigger); 
        } catch (SchedulerException e) {
            logger.error(e.toString());
        } 
	}
	
	public void initiateProfileFetch() throws SchedulerException {
		
		String jobName = "createUEMProfileJob";
		String triggerName = "initiateProfileFetchTrigger";
        try {
        	
        	if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(jobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(jobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					logger.debug("Failed to delete Quartz job" + jobName);
			}
        	
        	
            createUEMProfileJob = newJob(CreateUEMProfileJob.class)
                    .withIdentity(jobName, 
                            sched.getSchedulerName())
                    .build();
            
            SimpleTrigger trigger = (SimpleTrigger) newTrigger()
                    .withIdentity(triggerName, "group1")
                    .startAt(DateBuilder.futureDate(15, IntervalUnit.SECOND)) 
                    .forJob(createUEMProfileJob) 
                    .build();
            
            SchedulerManager.getInstance().getScheduler()
                    .scheduleJob(createUEMProfileJob, trigger); 
        } catch (SchedulerException e) {
            logger.error(e.toString());
        } 
    }
	
	public void syncUEMProfileToEM() throws SchedulerException {
		
		String jobName = "profileGroupSyncJob";
		String triggerName = "syncUEMProfileTrigger";
        
        try {
        	
        	if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(jobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(jobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					logger.debug("Failed to delete Quartz job" + jobName);
			}
        	
            String cronstatement="0 0/15 * * * ?"; // Default cron statement to run the job hourly
            SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
            if (systemConfigurationManager != null) {
                SystemConfiguration cronSetting = systemConfigurationManager.loadConfigByName("SYNC_PROFILE_CRON_SETTING");
                cronstatement = cronSetting.getValue();
            }
            
            profileGroupSyncJob = newJob(ProfileGroupSyncJob.class)
                    .withIdentity(jobName, 
                            sched.getSchedulerName())
                    .build();
            CronTrigger syncUEMProfileTrigger = (CronTrigger) newTrigger()
                    .withIdentity(triggerName,SchedulerManager.getInstance().getScheduler().getSchedulerName())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronstatement))
                    .startNow()
                    .build();
            
            SchedulerManager.getInstance().getScheduler()
                    .scheduleJob(profileGroupSyncJob, syncUEMProfileTrigger); 
        } catch (SchedulerException e) {
            logger.error(e.toString(),e);
        } 
    }
}