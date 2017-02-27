package com.communicator.job;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CommonStateUtils;
import com.communicator.dao.CloudConfigDao;
import com.communicator.dao.EnergySyncUpDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.manager.MigrationManager;
import com.communicator.manager.ReplicaServerInfoManager;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SpringContext;

public class BulkSyncUpJob  implements Job {

	SecureCloudConnectionTemplate secureCloudConnectionTemplate;
	ServerInfoManager serverInfoManager;
	ReplicaServerInfoManager  replicaServerInfoManager ;
	EnergySyncUpDao energySyncUpDao ;
	SystemConfigDao systemConfigDao ;
	MigrationManager migrationManager ;
	String tableName ;
	Boolean remigration;
	CloudConfigDao cloudConfigDao;
	
	
	static final Logger logger = Logger.getLogger(BulkSyncUpJob.class.getName());
	
	public BulkSyncUpJob(){
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
		systemConfigDao = (SystemConfigDao)SpringContext.getBean("systemConfigDao");
		replicaServerInfoManager = (ReplicaServerInfoManager)SpringContext.getBean("replicaServerInfoManager");
		energySyncUpDao = (EnergySyncUpDao)SpringContext.getBean("energySyncUpDao");
		migrationManager = (MigrationManager)SpringContext.getBean("migrationManager");
		secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)SpringContext.getBean("secureCloudConnectionTemplate");
		cloudConfigDao = (CloudConfigDao) SpringContext.getBean("cloudConfigDao");
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		logger.info("Starting new "+ tableName +" job event at "  + new Date());
		if(!energySyncUpDao.isTabelEmpty(tableName))
		{
			// check a corner case where fixture might have been configured 
			//after migration process started but stopped before completing migration
			// just check whether the min id of table is equal to the  min id of the record_id in wal logs for this table.
			//if yes then no need to go ahead put success state and move on wal will sync all the data.
			if(systemConfigDao.getTableLocalMinId(tableName).longValue() == systemConfigDao.getWalMinRecordIdForTable(tableName).longValue())
			{
				logger.info(tableName +" table was empty at the time of putting the triggers stopping the job. Data is present in wal logs.");
				putsuccessState() ;
				return ;
			}
			if(remigration != null && remigration) {
				
				while(keepRunning())
				{
					logger.info("Starting new "+ tableName +" sync event at "  + new Date());
					Long lastDataId = getMaxSyncedId(tableName) ;
					try {
										 
						ByteArrayOutputStream baos_energy = energySyncUpDao.getBulkDataForRestrictedRemigration(tableName, lastDataId, CommunicatorConstant.recordsCount, systemConfigDao.getLastIdBeforeTriggers(tableName)) ;
						
						MultipartEntity parts = new MultipartEntity();
						ByteArrayBody bytearray = new ByteArrayBody(baos_energy.toByteArray(), "energy");
						parts.addPart("other", bytearray);
						CloudHttpResponse response = secureCloudConnectionTemplate.executePost(CommunicatorConstant.SyncBulkDataRestrictedRemigrationService+ serverInfoManager.getMacAddress()+"/"+tableName, parts, serverInfoManager.getReplicaServerIP());

						logger.info("status " + response.getStatus());
						if(response.getStatus() == 200) {
							Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
							cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime, ((Long)(new Date().getTime())).toString());
						} else if(response.getStatus() == 503)
						{
							return ;
						}
						else {
							logger.info(tableName + " SYNC FAILED!!!!! ERROR on server");
							if(putFailorInProgressState())
							{
								return ;
							}
						}
						
					} catch (Exception e) {
						logger.error( e.getMessage(), e);
						if(putFailorInProgressState())
						{
							return ;
						}
			
					}
				}

			}
			else {
				while(keepRunning())
				{
					logger.info("Starting new "+ tableName +" sync event at "  + new Date());
					Long remoteMinId = getLastSyncedId(tableName) ;
					try {
										 
						ByteArrayOutputStream baos_energy = energySyncUpDao.getBulkData(tableName, remoteMinId, CommunicatorConstant.recordsCount) ;
						
						MultipartEntity parts = new MultipartEntity();
						ByteArrayBody bytearray = new ByteArrayBody(baos_energy.toByteArray(), "energy");
						parts.addPart("other", bytearray);
						CloudHttpResponse response = secureCloudConnectionTemplate.executePost(CommunicatorConstant.SyncBulkDataService+ serverInfoManager.getMacAddress()+"/"+tableName, parts, serverInfoManager.getReplicaServerIP());

						logger.info("status " + response.getStatus());
						if(response.getStatus() == 200) {
							Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
							cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime, ((Long)(new Date().getTime())).toString());
						} else if(response.getStatus() == 503)
						{
							return ;
						}
						else {
							logger.info(tableName + " SYNC FAILED!!!!! ERROR on server");
							if(putFailorInProgressState())
							{
								return ;
							}
						}
						
					} catch (Exception e) {
						logger.error( e.getMessage(), e);
						if(putFailorInProgressState())
						{
							return ;
						}
			
					}
				}
			}
			
			if(!keepRunning()) {	
				logger.info(tableName + " data catch up of historic data completed. Stopping the Energy Syncup thread.") ;
				putsuccessState() ; 
				return ;
			}
			
			
		}
		else {
			try {
				Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
				logger.info(tableName +" table is empty stoping the job");
				putsuccessState() ;
				return ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
		}
		
		
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	private Long getLastSyncedId(String tableName)
	{
		Long result = replicaServerInfoManager.getTableLastIdSynced(tableName).longValue() ;
		if(result.longValue()!=-1l)
		{
			return result ;
		}else
		{
			return (systemConfigDao.getLastIdBeforeTriggers(tableName) + 1);
		}
	}
	
	private Long getMaxSyncedId(String tableName)
	{
		Long result = replicaServerInfoManager.getTableMaxIdSynced(tableName).longValue() ;
		if(result.longValue() != -1l)
		{
			return result ;
		}else
		{
			return systemConfigDao.getTableLocalMinId(tableName) - 1;
		}
	}
	
	
	public boolean putFailorInProgressState()
	{
		boolean flag = false ;
		try {
			Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
			if(replicaServerInfoManager.getCurrentMigrationStateAttempts().longValue() > CommunicatorConstant.errorToleranceCount.longValue())
			{ 
				migrationManager.setReplicaMigrationFlagOnCloud(CommonStateUtils.getStateFailAccordingToTableName(tableName, remigration));
				flag = true ;
			}
			else
			{
				migrationManager.setReplicaMigrationFlagOnCloud(CommonStateUtils.getStateInProgressAccordingToTableName(tableName, remigration));
				flag = false;
			}	
			} catch (Exception e) {
				logger.error(e.getMessage() , e);
			}
		return flag;
	}
	
	private void putsuccessState()
	{
		try {
		migrationManager.setReplicaMigrationFlagOnCloud(CommonStateUtils.getStateSuccessAccordingToTableName(tableName, remigration)) ;
			Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
		} catch (Exception e) {
			logger.error(e.getMessage() , e);
		}
	}
	
	
	private boolean keepRunning() {
		if(remigration  != null && remigration) {
			Long maxIdSynced = replicaServerInfoManager.getTableMaxIdSynced(tableName);
			Long maxIdToBeSynced = systemConfigDao.getLastIdBeforeTriggers(tableName);
			logger.info("maxIdSynced=" + maxIdSynced + " and maxIdToBeSynced=" + maxIdToBeSynced + " for table " + tableName);
			if(maxIdSynced == -1L) {
				maxIdSynced = 0L;
			}
			return (maxIdToBeSynced.longValue() > 0 && maxIdSynced.longValue() < maxIdToBeSynced.longValue() && dataExistsBetweenIds(maxIdSynced, maxIdToBeSynced));
		}
		else {
			Long minIdSynced = replicaServerInfoManager.getTableLastIdSynced(tableName);
			Long minIdToBeSynced = systemConfigDao.getTableLocalMinId(tableName);
			logger.info("minIdSynced=" + minIdSynced + " and minIdToBeSynced=" + minIdToBeSynced + " for table " + tableName);
			return (minIdToBeSynced.longValue() > 0 && (minIdSynced.longValue() == -1 || minIdSynced.longValue() > minIdToBeSynced.longValue()));
		}
		
	}
	
	public boolean dataExistsBetweenIds(Long minId, Long maxId) {
		return systemConfigDao.dataExistsBetweenIds(tableName, minId, maxId);
	}
	
	
	public Boolean getRemigration() {
		return remigration;
	}
	
	
	public void setRemigration(Boolean remigration) {
		this.remigration = remigration;
	}
}


