package com.emscloud.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.action.SpringContext;
import com.emscloud.service.ProfileSyncManager;

public class ProfileGroupSyncJob implements Job {
	
	ProfileSyncManager profileSyncManager;
	public static final Logger logger = Logger.getLogger(FetchEMProfileJob.class.getName());
	
	public ProfileGroupSyncJob() {
	    profileSyncManager = (ProfileSyncManager) SpringContext.getBean("profileSyncManager");
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
	    try
        {
	        logger.info("Sync EM Profile Job Started......");
	        profileSyncManager.syncProfileGroupsToEM();
        }catch(Exception e){
            logger.error("Profiles can not be downloaded completely from EM to UEM" + e.getMessage());
            e.printStackTrace();
        }
	}
}
