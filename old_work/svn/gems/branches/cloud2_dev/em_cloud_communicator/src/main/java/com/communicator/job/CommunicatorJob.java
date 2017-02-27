package com.communicator.job;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communicator.dao.SystemConfigDao;
import com.communicator.manager.CallHomeManager;
import com.communicator.manager.MigrationManager;
import com.communicator.manager.ReplicaServerInfoManager;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringContext;

public class CommunicatorJob implements Job {
	
	static final Logger logger = Logger.getLogger(CommunicatorJob.class.getName());
	
	static Boolean isCleanUpDoneWhenCloudDisabled = false;
	static int numberofFailedAttempts = 0;
	
	ServerInfoManager serverInfoManager;
    CallHomeManager callHomeManager;
    MigrationManager migrationManager;
    ReplicaServerInfoManager replicaServerInfoManager;
    SystemConfigDao systemConfigDao;
    SecureCloudConnectionTemplate secureCloudConnectionTemplate;
    
	
	public CommunicatorJob() {
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
	    callHomeManager = (CallHomeManager)SpringContext.getBean("callHomeManager");
	    migrationManager = (MigrationManager)SpringContext.getBean("migrationManager");
	    replicaServerInfoManager = (ReplicaServerInfoManager)SpringContext.getBean("replicaServerInfoManager");
	    systemConfigDao = (SystemConfigDao)SpringContext.getBean("systemConfigDao");
	    secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)SpringContext.getBean("secureCloudConnectionTemplate");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//logger.info("Testing:::" + isCleanUpDoneWhenCloudDisabled + " " + numberofFailedAttempts);
		try {
			List<JobExecutionContext> jobs = SchedulerManager.getInstance().getScheduler().getCurrentlyExecutingJobs();
			for (JobExecutionContext job : jobs) {
	            if (job.getTrigger().equals(context.getTrigger()) && !job.getFireInstanceId().equals(context.getFireInstanceId())) {
	            	numberofFailedAttempts++;
	            	if(numberofFailedAttempts > 15) {
	            		System.exit(0);
	            	}
	                logger.info(numberofFailedAttempts + ": There's another instance running, so leaving" + this);
	                return;
	            }
	        }
			numberofFailedAttempts = 0;
			if(systemConfigDao.isCloudEnabled().equalsIgnoreCase("0"))
			{
				logger.info("EM is not cloud enabled. Contact Administrator");
				if(!isCleanUpDoneWhenCloudDisabled)
				{
					systemConfigDao.doBeforeMigrationcleanUp() ;
					isCleanUpDoneWhenCloudDisabled = true;
				}
			} else {
				// If cloud communication is toggled
				isCleanUpDoneWhenCloudDisabled = false;
				serverInfoManager.init();
				String status = serverInfoManager.getCloudSyncType();
					if("1".equals(status)) {
						callHomeManager.sendDataCallHome();
					}
					else if ("2".equals(status)) {
						if(!InitializeSecureConnection.getIsSppaCertificateInitialized())
						{
							InitializeSecureConnection.init(secureCloudConnectionTemplate);
							
						}
						callHomeManager.sendDataCallHome();
						if(replicaServerInfoManager.getReplicaConnectivity())
						{
							migrationManager.runMigrationOrSync() ;
						}else
						{
							migrationManager.setReplicaMigrationFlagDirectECloud( DatabaseState.REPLICA_UNREACHABLE.getName());
						}
					}	
				 else {
					serverInfoManager.init();
				}
			}
		} catch (SecurityException e) {
			logger.error(e.toString());
		} catch(Exception e) {
			logger.error(e.toString());
		}
	}

}
