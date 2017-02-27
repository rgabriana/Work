package com.emscloud.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.action.SpringContext;
import com.emscloud.service.ProfileSyncManager;

public class FetchEMProfileJob implements Job {
	
	ProfileSyncManager profileSyncManager;
	public static final Logger logger = Logger.getLogger("CloudProfile");
	
	public FetchEMProfileJob() {
	    profileSyncManager = (ProfileSyncManager) SpringContext.getBean("profileSyncManager");
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
	    try
        {
	        logger.info("Fetch EM Profile Job Started......");
	        //Download For ALL Mapped EM Instances
	        Long Id=(long) 0;
	        profileSyncManager.downloadDerivedEMTemplatesToUEM(Id);
	        profileSyncManager.downloadDerivedEMProfilesToUEM(Id);
            
        }catch(Exception e){
            logger.error("Profiles can not be downloaded completely from EM to UEM" + e.getMessage());
            e.printStackTrace();
        }
	}
}
