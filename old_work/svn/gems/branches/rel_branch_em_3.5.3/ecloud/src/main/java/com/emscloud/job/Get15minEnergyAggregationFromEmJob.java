package com.emscloud.job;



import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.action.SpringContext;
import com.emscloud.service.ECManager;


public class Get15minEnergyAggregationFromEmJob implements Job {
	public static final Logger logger = Logger.getLogger("StatsAgg");
	ECManager eCManager;

	public Get15minEnergyAggregationFromEmJob() {
		eCManager = (ECManager) SpringContext.getBean("eCManager");
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		try {
			if (eCManager.isRunning() == false) {
				if(logger.isInfoEnabled()) {
					logger.info(context.getFireTime()
							+ ": starting new 15 min energy sync on floor levels"
							+ " at " + Calendar.getInstance().getTime().toString());
				}
	
				eCManager.get15MinEnergySyncDataFromEm();
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
