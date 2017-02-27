package com.ems.service;



import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ems.action.SpringContext;


public class EmailNotificationOneHourSchedulerJob implements Job {
	
	public static final Logger logger = Logger.getLogger("emailNotificationLogger");
	
	EmailNotificationOneHourManager emailNotificationOneHourManager;

	public EmailNotificationOneHourSchedulerJob() {
		emailNotificationOneHourManager = (EmailNotificationOneHourManager) SpringContext.getBean("emailNotificationOneHourManager");
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		try {
			if (emailNotificationOneHourManager.isRunning() == false) {
				if(logger.isInfoEnabled()) {
					logger.info(context.getFireTime()
							+ ": starting new Email Notification One Hour Scheduler Job"
							+ " at " + Calendar.getInstance().getTime().toString());
				}
				emailNotificationOneHourManager.sendOneHourNotificationEmail();
			}else {
				if(logger.isInfoEnabled()) {
					logger.info(context.getFireTime()
							+ ": previous job still running  " + context.getPreviousFireTime());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(logger.isInfoEnabled()) {
				logger.info(context.getFireTime() + " done... ("
						+ (System.currentTimeMillis() - startTime) + ")");
			}
		}

	}

	

}
