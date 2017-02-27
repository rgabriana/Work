package com.emscloud.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.job.SiteAnomalyValidationJob;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.util.SchedulerManager;

@Service("siteAnomalyValidationJobManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SiteAnomalyValidationJobManager {
	 static final Logger logger = Logger.getLogger(SiteAnomalyValidationJobManager.class.getName());
	
	 JobDetail validateSiteAnomalyJob;
	 
	 Scheduler sched = SchedulerManager.getInstance().getScheduler();
	 
	 @Resource
	 SystemConfigurationManager systemConfigurationManager;
	 
	 public void startValidateSiteAnomalyJob() {

			String validateSiteAnomalyJobName = "validateSiteAnomalyJob";
			String validateSiteAnomalyJobTriggerName = "validateSiteAnomalyJobTrigger";
			String cronSiteValidationNightlyJobDefault = "0 0 2 * * ?";
			try {
				
					// Delete the older Quartz job and create a new one
		        	if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(validateSiteAnomalyJobName,	SchedulerManager.getInstance().getScheduler().getSchedulerName()))) {
		        		if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(validateSiteAnomalyJobName,	SchedulerManager.getInstance().getScheduler().getSchedulerName())) == false)
		        			//System.out.println("Failed to delete Quartz job" + generateMonthlyBillJobName);
		        			logger.debug("Failed to delete Quartz job" + validateSiteAnomalyJobName);
		        	}
	        	
					// Default cron statement to run the job every 15 min
					String cronstatement = cronSiteValidationNightlyJobDefault;
					try {
						if (systemConfigurationManager != null) {
							SystemConfiguration cronSetting = systemConfigurationManager
									.loadConfigByName("VALIDATE.SITE.ANOMALY.CRON");
							if (cronSetting != null && cronSetting.getValue() != null
									&& !cronSetting.getValue().isEmpty()) {
								cronstatement = cronSetting.getValue();
								//it is disabled in the database so don't start the job
								if(cronstatement.equalsIgnoreCase("Disabled")) {
									return;
								}
							} else {								
								cronstatement = cronSiteValidationNightlyJobDefault;
							}
						}
					} catch (Exception e) {
						cronstatement = cronSiteValidationNightlyJobDefault;
					}
					// create job
					validateSiteAnomalyJob = newJob(SiteAnomalyValidationJob.class)
							.withIdentity(
									validateSiteAnomalyJobName,
									SchedulerManager.getInstance().getScheduler()
											.getSchedulerName()).build();
					// create trigger
					CronTrigger siteValidationJobTrigger = (CronTrigger) newTrigger()
							.withIdentity(
									validateSiteAnomalyJobTriggerName,
									SchedulerManager.getInstance().getScheduler()
											.getSchedulerName())
							.withSchedule(
									CronScheduleBuilder.cronSchedule(cronstatement))
							.startNow().build();

					// schedule job
					SchedulerManager.getInstance().getScheduler()
							.scheduleJob(validateSiteAnomalyJob, siteValidationJobTrigger);

			} catch (SchedulerException e) {
				logger.error(e.toString());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
}
