package com.ems.uem.scheduler;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ems.uem.communication.UEMHeartBeatManager;

public class UEMScheduler implements Job{	 		
	private static final Logger logger = Logger.getLogger("WSLogger");

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("calling execute UEMscheduler");
		UEMHeartBeatManager.callUEM();
	}
}
