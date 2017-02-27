package com.emscloud.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.action.SpringContext;
import com.emscloud.service.ProfileSyncManager;

public class CreateUEMProfileJob implements Job {
	
	ProfileSyncManager profileSyncManager;
	public static final Logger logger = Logger.getLogger("CloudProfile");
	
	public CreateUEMProfileJob() {
	    profileSyncManager = (ProfileSyncManager) SpringContext.getBean("profileSyncManager");
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
	    try
        {
	        //System.out.println("Create EM Profile Job Started......");
	        logger.info("Create EM Profile Job Started......");
	        boolean isDefaultProfileCreated = profileSyncManager.createDefaultProfilesInUEM();
	        if(isDefaultProfileCreated)
	        {
	            //Download For ALL Mapped EM Instances
	            Long emId= (long) 0;
	            profileSyncManager.downloadDerivedEMTemplatesToUEM(emId);
	            profileSyncManager.downloadDerivedEMProfilesToUEM(emId);
	        }
        }catch(Exception e){
            logger.error("Failed to create Default 17 profiles in UEM" + e.getMessage());
            e.printStackTrace();
        }
	}
}
