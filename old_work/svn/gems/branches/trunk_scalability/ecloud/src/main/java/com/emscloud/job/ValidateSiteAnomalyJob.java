package com.emscloud.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ValidateSiteAnomalyJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		System.out.println("Validate Site Anomaly Job Called");
	}

}
