package com.communicator.job;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communicator.CommunicatorEntryPoint;
import com.communicator.dao.CloudConfigDao;
import com.communicator.dao.SyncTasksDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SpringContext;
import com.communicator.util.SyncTasks;

public class CleanupJob implements Job {
	
	Logger logger = Logger.getLogger(CleanupJob.class.getName());
	SyncTasksDao syncTasksDao;
	CloudConfigDao cloudConfigDao;
	SystemConfigDao systemConfigDao;
	
	public CleanupJob() {
		syncTasksDao = (SyncTasksDao) SpringContext.getBean("syncTasksDao");
		cloudConfigDao = (CloudConfigDao)SpringContext.getBean("cloudConfigDao");
		systemConfigDao = (SystemConfigDao)SpringContext.getBean("systemConfigDao");
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Starting clean up job");
		SyncTasks st = syncTasksDao.getOldestSyncTaskBeforeDate(30);
		try{
			while (st != null) {
				logger.info("removing file " + st.getFilename());
	    		File file = new File(st.getFilename());
	    		if(!file.exists() || file.delete()){
	    			logger.info(st.getFilename() + " is cleaned!!");
	    			syncTasksDao.deleteSyncTasksByLastWalId(st.getLastWalId());
	    		}
	    		st = syncTasksDao.getOldestSyncTaskBeforeDate(5);
			}
			
		}catch(Exception e){
    		logger.error(e.getMessage(), e);
    	}
		try {
			Date lastSyncTime = new Date(Long.parseLong(cloudConfigDao.getCloudConfigValue(CommunicatorConstant.sysConfigSuccessfulSyncTime)));
			Date remigrationDelayRequired = new Date(new Date().getTime() - CommunicatorEntryPoint.remigrationDelay);
			logger.info("Last successfull sync at " + lastSyncTime + " and remigration delay limit is " + remigrationDelayRequired);
			if(lastSyncTime.before(remigrationDelayRequired)) {
				logger.warn("Setting flag to Remigration Required.");
				systemConfigDao.doBeforeMigrationcleanUp();
				cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigRemigrationRequired, "1");
				CommunicatorEntryPoint.remigrationRequired = true;
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}

}
