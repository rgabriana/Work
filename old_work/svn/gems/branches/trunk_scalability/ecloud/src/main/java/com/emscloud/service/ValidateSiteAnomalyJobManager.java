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

import com.emscloud.job.ValidateSiteAnomalyJob;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.util.SchedulerManager;

@Service("validateSiteAnomalyJobManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ValidateSiteAnomalyJobManager {
	 static final Logger logger = Logger.getLogger(ValidateSiteAnomalyJobManager.class.getName());
	
	 JobDetail validateSiteAnomalyJob;
	 
	 Scheduler sched = SchedulerManager.getInstance().getScheduler();
	 
	 @Resource
	 SystemConfigurationManager systemConfigurationManager;
	 
	 public void startValidateSiteAnomalyJob() {

			String syncJobName = "validateSiteAnomalyJob";
			String syncTriggerName = "validateSiteAnomalyJobTrigger";
			String cron15MinDefault = "0 0 2 * * ?";
			
			try {
				// check if job exist, if not create.
				if (!sched.checkExists(new JobKey(syncJobName, sched
						.getSchedulerName()))) {
					// Default cron statement to run the job every 15 min

					String cronstatement = cron15MinDefault;
					try {
						if (systemConfigurationManager != null) {
							SystemConfiguration cronSetting = systemConfigurationManager
									.loadConfigByName("VALIDATE.SITE.ANOMALY.CRON");
							if (cronSetting.getValue() != null
									|| !cronSetting.getValue().isEmpty()) {
								cronstatement = cronSetting.getValue();
							} else {
								cronstatement = cron15MinDefault;
							}
						}
					} catch (Exception e) {
						cronstatement = cron15MinDefault;
					}
					// create job
					validateSiteAnomalyJob = newJob(ValidateSiteAnomalyJob.class)
							.withIdentity(
									syncJobName,
									SchedulerManager.getInstance().getScheduler()
											.getSchedulerName()).build();
					// create trigger
					CronTrigger sync15minJobTrigger = (CronTrigger) newTrigger()
							.withIdentity(
									syncTriggerName,
									SchedulerManager.getInstance().getScheduler()
											.getSchedulerName())
							.withSchedule(
									CronScheduleBuilder.cronSchedule(cronstatement))
							.startNow().build();

					// schedule job
					SchedulerManager.getInstance().getScheduler()
							.scheduleJob(validateSiteAnomalyJob, sync15minJobTrigger);
				}

			} catch (SchedulerException e) {
				logger.error(e.toString());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
}
