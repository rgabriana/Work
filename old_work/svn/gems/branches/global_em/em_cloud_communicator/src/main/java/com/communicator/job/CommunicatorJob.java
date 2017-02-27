package com.communicator.job;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communicator.CommunicatorEntryPoint;
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
	
	public static boolean callHomeSuccess = false;
	public static boolean syncReady = false;
	public static boolean healthBeacon = false;
	
	public static Thread callHomeThread = null;
	
	ServerInfoManager serverInfoManager;
    CallHomeManager callHomeManager;
    MigrationManager migrationManager;
    ReplicaServerInfoManager replicaServerInfoManager;
    SystemConfigDao systemConfigDao;
    SecureCloudConnectionTemplate secureCloudConnectionTemplate;
    
    private void initCallHome() {
    	if(callHomeThread == null) {
    		callHomeThread = new Thread(new CommunicatorJob.CallHomePoll());
    		callHomeThread.start();
    	}
    }
    
	
	public CommunicatorJob() {
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
	    callHomeManager = (CallHomeManager)SpringContext.getBean("callHomeManager");
	    migrationManager = (MigrationManager)SpringContext.getBean("migrationManager");
	    replicaServerInfoManager = (ReplicaServerInfoManager)SpringContext.getBean("replicaServerInfoManager");
	    systemConfigDao = (SystemConfigDao)SpringContext.getBean("systemConfigDao");
	    secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)SpringContext.getBean("secureCloudConnectionTemplate");
	    initCallHome();
	}
	
	class CallHomePoll implements Runnable {

		@Override
		public void run() {
			while (true) {
				try{
					while(true) {
						try {
							CommunicatorEntryPoint.commReset = 0;
							if(healthBeacon && ("1".equals(serverInfoManager.getCloudSyncType()) || "2".equals(serverInfoManager.getCloudSyncType())) ) {
								callHomeManager.sendDataCallHome();
								if(!syncReady && callHomeSuccess && "2".equals(serverInfoManager.getCloudSyncType())) {
									replicaCall();
								}
							}
							else {
								Thread.sleep(5000);
							}
						}
					 	catch (InterruptedException e) {
							logger.error(e);
							Thread.sleep(60000);
						}
						catch (Exception e) {
							logger.error(e);
							Thread.sleep(60000);
						}
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
			
		}
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
				healthBeacon = false;
				logger.info("EM is not cloud enabled. Contact Administrator");
				if(!isCleanUpDoneWhenCloudDisabled)
				{
					systemConfigDao.doBeforeMigrationcleanUp() ;
					isCleanUpDoneWhenCloudDisabled = true;
				}
			} else {
				// If cloud communication is toggled
				isCleanUpDoneWhenCloudDisabled = false;
				healthBeacon = true;
				//serverInfoManager.init();
				String status = serverInfoManager.getCloudSyncType();
				if ("2".equals(status) && callHomeSuccess) {
					if(syncReady) {
						replicaCall();
					}
				}
				else if (!callHomeSuccess) {
					serverInfoManager.init();
				}
			}
		} catch (SecurityException e) {
			logger.error(e.toString());
		} catch(Exception e) {
			logger.error(e.toString());
		}
	}
	
	private void replicaCall() throws SchedulerException {
		if(!InitializeSecureConnection.getIsSppaCertificateInitialized())
		{
			InitializeSecureConnection.init(secureCloudConnectionTemplate);
			
		}
		if(replicaServerInfoManager.getReplicaConnectivity())
		{
			migrationManager.runMigrationOrSync() ;
		}else
		{
			migrationManager.setReplicaMigrationFlagDirectECloud( DatabaseState.REPLICA_UNREACHABLE.getName());
		}
	}

}
