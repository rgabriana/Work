package com.emscloud.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.job.EnergyAggregationJob;
import com.emscloud.job.GenerateBillJob;
import com.emscloud.util.SchedulerManager;

@Service("billTaskManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BillTaskManager {
	static final Logger logger = Logger.getLogger("CloudBilling");
	@Autowired
	private MessageSource messageSource;
	public void processBillTasks() throws SchedulerException {
		
		String cronStatment = messageSource.getMessage("cron.statement", null,null);
		System.out.println("cron statement - " + cronStatment);
			try {
					Date startDate = new Date();
					String generateMonthlyBillJobName = "job_monthly_bill_generate";
					String generateMonthlyBillTriggerName = "trigger_monthly_bill_generate";
					
					// Delete the older Quartz job and create a new one
		        	if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(generateMonthlyBillJobName,	SchedulerManager.getInstance().getScheduler().getSchedulerName()))) {
		        		if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(generateMonthlyBillJobName,	SchedulerManager.getInstance().getScheduler().getSchedulerName())) == false)
		        			//System.out.println("Failed to delete Quartz job" + generateMonthlyBillJobName);
		        			logger.debug("Failed to delete Quartz job" + generateMonthlyBillJobName);
		        	}
		        	
		        	// Create quartz job
					JobDetail billJob = newJob(GenerateBillJob.class)
							.withIdentity(generateMonthlyBillJobName,
					SchedulerManager.getInstance().getScheduler()
							.getSchedulerName())
							.build();
					
					//System.out.println("Scheduler NAME " + SchedulerManager.getInstance().getScheduler().getSchedulerName());
					//.withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(4, 03, 00)).build();
					// Create Quartz trigger
					CronTrigger billGenerateTrigger = (CronTrigger) newTrigger()
							.withIdentity(generateMonthlyBillTriggerName,SchedulerManager.getInstance().getScheduler().getSchedulerName())
							.withSchedule(CronScheduleBuilder.cronSchedule(cronStatment))
							.startAt(startDate)
							.build();
					
					SchedulerManager.getInstance().getScheduler()
							.scheduleJob(billJob, billGenerateTrigger);				
					logger.info("Billing job has been Scheduled");					
					
					/*try {
						new EnergyAggregationJob();
						System.out.println("aggregation job scheduled");
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}*/

			} catch (IllegalArgumentException e) {
				logger.error(e.toString());
			}
		}

}
