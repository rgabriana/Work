package com.ems.service;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ems.action.SpringContext;


public class BacnetSchedulerJob implements Job {
	
	public static final Logger logger = Logger.getLogger("BacnetLog");
	
	BacnetManager bacnetManager;
	
	public BacnetSchedulerJob() {
		bacnetManager = (BacnetManager) SpringContext.getBean("bacnetManager");
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		//System.out.println("In BacnetSchedulerJob start:"+bacnetManager.isRunning());
		long startTime = System.currentTimeMillis();
		try {
			if (bacnetManager.isRunning() == false) {
				if(logger.isInfoEnabled()) {
					logger.info(context.getFireTime()
							+ ": starting Bacnet Scheduler Job"
							+ " at " + Calendar.getInstance().getTime().toString());
				}
	
				if(bacnetManager.isBacnetEnabled()){
	            	if(!bacnetManager.isBacnetServiceRunning()){
	            		bacnetManager.startBacnetService();
	            	}
	            }else{
	            	bacnetManager.stopBacnetService();
	            }
			}else {
				if(logger.isInfoEnabled()) {
					logger.info(context.getFireTime()
							+ ": previous job still running  " + context.getPreviousFireTime());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			//System.out.println("In BacnetSchedulerJob exception");
		} finally {
			if(logger.isInfoEnabled()) {
				logger.info(context.getFireTime() + " done... ("
						+ (System.currentTimeMillis() - startTime) + ")");
			}
		}
		//System.out.println("In BacnetSchedulerJob end");		
	}
}
