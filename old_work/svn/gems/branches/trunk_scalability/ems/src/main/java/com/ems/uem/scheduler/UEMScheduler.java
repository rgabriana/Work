package com.ems.uem.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ems.uem.communication.UEMHeartBeatManager;

public class UEMScheduler implements Job{	 		

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub		
		//WebServices.callUEM();		
		UEMHeartBeatManager.callUEM();
	}
}
