package com.ems.service;



import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ems.action.SpringContext;


public class ErcBatteryReportSchedulerJob implements Job {
	public static final Logger logger = Logger.getLogger("StatsAgg");
	WdsManager wdsManager;

	public ErcBatteryReportSchedulerJob() {
		wdsManager = (WdsManager) SpringContext.getBean("wdsManager");
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		try {
			if (wdsManager.isRunning() == false) {
				if(logger.isInfoEnabled()) {
					logger.info(context.getFireTime()
							+ ": starting new Erc Battery Report Scheduler Job"
							+ " at " + Calendar.getInstance().getTime().toString());
				}
	
				wdsManager.sendErcBatteryReportEmail();
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
