/**
 * 
 */
package com.communicator.manager;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CommonStateUtils;
import com.communicator.job.BulkSyncUpJob;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SchedulerManager;

@Service("migrationManager")
public class MigrationManager {
	
	static final Logger logger = Logger.getLogger(MigrationManager.class
			.getName());
	
	@Resource
	SecureCloudConnectionTemplate securecloudConnectionTemplate;
	
	@Resource
	CloudManager cloudManager;
	@Resource
	ServerInfoManager serverInfoManager;
	@Resource
	ReplicaServerInfoManager replicaServerInfoManager;
	
	Scheduler sched = null ;
	
	
	public void runMigrationOrSync() throws SchedulerException
	{
		String IN_PROGRESS_LOG =  " migration in progress." ;
		String jobName =null ;
		String nextState= null ;
		
		if(replicaServerInfoManager.getCurrentEmStatus()== EmStatus.SPPA)
		{
			if(replicaServerInfoManager.getCurrentMigrationStateAttempts().longValue() > CommunicatorConstant.errorToleranceCount.longValue() )
			{
				logger.info("Attempts for completing the " + replicaServerInfoManager.getCurrentMigrationState().getName() + " State has lapsed more than 3 times. Migration cannot progress. Contact Adminstrator.");
				String state = CommonStateUtils.getFailState(replicaServerInfoManager.getCurrentMigrationState()) ;
				if(state!=null)
				{
					setReplicaMigrationFlagOnCloud(state);
				}
				
				return ;
			}
			
			switch(replicaServerInfoManager.getCurrentMigrationState())
			{
			
			//Sync failed case
			case SYNC_FAILED:
			// Sync Ready case
			case SYNC_READY:
				if(!replicaServerInfoManager.getIsSyncPaused()) { 
					cloudManager.sendDataSPPA();
				}
				else {
					logger.info("Sync Paused!!");
				}
				break;
				
			//Migration case
			case MIGRATION_READY:
				cloudManager.doDataMigration() ;
				break;
		
				
			//Start Cases
			case ENERGY_CONSUMPTION_DAILY_START:
				jobName = CommonStateUtils.energyDailyTableName ;
				 nextState = DatabaseState.ENERGY_CONSUMPTION_DAILY_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case ENERGY_CONSUMPTION_HOURLY_START:
				jobName = CommonStateUtils.energyHourlyTableName;
				nextState = DatabaseState.ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case ENERGY_CONSUMPTION_START:
				jobName = CommonStateUtils.energyTableName;
				nextState= DatabaseState.ENERGY_CONSUMPTION_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case MOTION_BIT_START:
				jobName = CommonStateUtils.motionBitTableName;
				nextState = DatabaseState.MOTION_BIT_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			
				
			// Success cases
			case ENERGY_CONSUMPTION_DAILY_SUCCESS:
				logger.info("Daily energy data Migration successfull.") ;
				setReplicaMigrationFlagOnCloud(DatabaseState.MOTION_BIT_START.getName()) ;	
				break;
			case ENERGY_CONSUMPTION_HOURLY_SUCCESS:
				logger.info("Hourly energy data Migration successfull.") ;
				setReplicaMigrationFlagOnCloud(DatabaseState.ENERGY_CONSUMPTION_DAILY_START.getName()) ;	
				break;		
			case ENERGY_CONSUMPTION_SUCCESS:
				logger.info("Minutes energy data Migration successfull.") ;
				setReplicaMigrationFlagOnCloud(DatabaseState.ENERGY_CONSUMPTION_HOURLY_START.getName()) ;	
				break;	
			case MIGRATION_SUCCESS:
				logger.info("Initial Migration successfull.") ;
				setReplicaMigrationFlagOnCloud(DatabaseState.ENERGY_CONSUMPTION_START.getName()) ;
				break;					
			case MOTION_BIT_SUCCESS:
				logger.info("Motion bit Migration successfull.") ;
				setReplicaMigrationFlagOnCloud(DatabaseState.SYNC_READY.getName()) ;
				break;
				
			// Progress Cases
			case ENERGY_CONSUMPTION_DAILY_IN_PROGRESS:
				logger.info("Daily Energy data" +IN_PROGRESS_LOG) ;
				//check if running or not. if not start again
				jobName = CommonStateUtils.energyDailyTableName ;
				 nextState = DatabaseState.ENERGY_CONSUMPTION_DAILY_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case ENERGY_CONSUMPTION_IN_PROGRESS:
				logger.info("Minutes Energy data" +IN_PROGRESS_LOG) ;
				//check if running or not. if not start again
				jobName = CommonStateUtils.energyTableName;
				nextState= DatabaseState.ENERGY_CONSUMPTION_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS:
				logger.info("Hourly Energy data" +IN_PROGRESS_LOG) ;
				//check if running or not. if not start again
				jobName = CommonStateUtils.energyHourlyTableName;
				nextState = DatabaseState.ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case MOTION_BIT_IN_PROGRESS:
				logger.info("Motion bit data" +IN_PROGRESS_LOG) ;
				//check if running or not. if not start again
				jobName = CommonStateUtils.motionBitTableName;
				nextState = DatabaseState.MOTION_BIT_IN_PROGRESS.getName() ;
				createAndFireBulkJob(jobName ,nextState) ;
				break;
			case MIGRATION_IN_PROGRESS:
				logger.info("Initial Migration is in Progress.") ;
				break;
		
				
			// Failure Cases
			case ENERGY_CONSUMPTION_DAILY_FAIL:
			case ENERGY_CONSUMPTION_HOURLY_FAIL:
			case ENERGY_CONSUMPTION_FAIL:
			case MOTION_BIT_FAIL:
				logger.info("Stopping job related to status :- "+ replicaServerInfoManager.getCurrentMigrationState());
				sched.deleteJob(new JobKey(CommonStateUtils.getTableNameAccordingToState(replicaServerInfoManager.getCurrentMigrationState())));
				break;
			
			//Miscellaneous cases
			case MIGRATION_FAIL:
				logger.info("Migration step have failed due to some reason. Please contact Admin.") ;
				break;
			case REPLICA_UNREACHABLE:
				logger.info("Em Was able to re establish connection with replica server. Resetting the flag to start process from where it was left.") ;
				setPreviousMigrationFlagOnCloud() ;
				break;
			case NOT_MIGRATED:
				logger.info("Em is NOT_MIGRATED. Not a valid state according to workflow.") ;
				break;
			case REMIGRATION_REQUIRED:
				logger.info("Remigration required. Nothing to sync.");
				break;
			default:
				logger.error("Communicator cannot understand " +replicaServerInfoManager.getCurrentMigrationState()+" state. Contact Adminstrator.");
			break;

			}
		}
	}
	// this function set state on cloud through replica server
	public  Boolean setReplicaMigrationFlagOnCloud(String migrationStatus )
	{
		try{
				synchronized (CommunicatorConstant.setMigrationFlagService)
				{
					logger.info(CommunicatorConstant.setMigrationFlagService+serverInfoManager.getMacAddress()+"/"+migrationStatus);
					
					CloudHttpResponse response = securecloudConnectionTemplate.executeGet(CommunicatorConstant.setMigrationFlagService+serverInfoManager.getMacAddress()+"/"+migrationStatus ,replicaServerInfoManager.getReplicaServerIP());
					int state= response.getStatus();
					if(state!=200)
					{
						logger.error("Error while communicating migration state "+migrationStatus +" to replica server. Please contact Administrator") ;
						return false ;
					}
				}
			} catch(Exception ex)
			{
				logger.error("Error while communicating migration state "+migrationStatus +" to replica server. Please contact Administrator") ;
				logger.error(ex.getMessage(),ex) ;
				return false ;
			}
		return true ;
	}
	//This function directly set state on Ecloud
	public  Boolean setReplicaMigrationFlagDirectECloud(String migrationStatus )
	{
		try{
				synchronized (CommunicatorConstant.setMigrationFlagService)
				{
					logger.info(CommunicatorConstant.setMigrationFlagOnEcloudService+serverInfoManager.getMacAddress()+"/"+migrationStatus);
					
					CloudHttpResponse response = securecloudConnectionTemplate.executePost(CommunicatorConstant.setMigrationFlagOnEcloudService+serverInfoManager.getMacAddress()+"/"+migrationStatus + "/-1","Dummy post",serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
					int state= response.getStatus();
					if(state!=200)
					{
						logger.error("Error while communicating migration state "+migrationStatus +" to replica server. Please contact Administrator State Returned :-" + state) ;
						return false ;
					}
				}
			} catch(Exception ex)
			{
				logger.error("Error while communicating migration state "+migrationStatus +" to replica server. Please contact Administrator") ;
				logger.error(ex.getMessage(),ex) ;
				return false ;
			}
		return true ;
	}
	public  Boolean setPreviousMigrationFlagOnCloud()
	{
		try{
				
					logger.info(CommunicatorConstant.setPreviousMigrationFlagService+serverInfoManager.getMacAddress());
					
					CloudHttpResponse response = securecloudConnectionTemplate.executeGet(CommunicatorConstant.setPreviousMigrationFlagService+serverInfoManager.getMacAddress(), serverInfoManager.getHost()  );
					int state= response.getStatus();
					if(state!=200)
					{
						logger.error("Error while communicating  to cloud server. Please contact Administrator. State Returned :-" + state) ;
						return false ;
					}
				
			} catch(Exception ex)
			{
				logger.error("Error while communicating migration state to cloud server. Please contact Administrator") ;
				logger.error(ex.getMessage(),ex) ;
				return false ;
			}
		return true ;
	}
	private void createNewBulkSyncUpJob(String tableName) throws SchedulerException {
		JobDetail BulkJob = newJob(BulkSyncUpJob.class)
								.withIdentity(tableName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName())
				.usingJobData("tableName", tableName)
				.build();
		SimpleTrigger BulkTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(
						tableName+"_trigger",
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName()).startNow().build();

		SchedulerManager.getInstance().getScheduler()
				.scheduleJob(BulkJob, BulkTrigger);
	}
	private void createAndFireBulkJob(String jobName , String state)
	{
		sched = SchedulerManager.getInstance().getScheduler();
		try {
			if (!sched.checkExists(new JobKey(jobName, sched.getSchedulerName()))) {
				setReplicaMigrationFlagOnCloud(state) ;
				createNewBulkSyncUpJob(jobName);	
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
