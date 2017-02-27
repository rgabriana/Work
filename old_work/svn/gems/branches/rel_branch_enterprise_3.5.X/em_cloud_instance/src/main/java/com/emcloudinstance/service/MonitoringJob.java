package com.emcloudinstance.service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emcloudinstance.util.SpringContext;

public class MonitoringJob implements Job{

	@Override
	public void execute(JobExecutionContext executionContext) throws JobExecutionException {
		MonitoringManager monitoringManager = (MonitoringManager)SpringContext.getBean("monitoringManager");
		String mac = executionContext.getJobDetail().getJobDataMap().getString("mac");
		monitoringManager.doMonitoring(mac);
	}

}
