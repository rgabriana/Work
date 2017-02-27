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
			if(systemConfigDao.getTableLocalMinId(tableName).longValue()==systemConfigDao.getWalMinRecordIdForTable(tableName).longValue())
			{
				logger.info(tableName +" table was empty at the time of putting the triggers stopping the job. Data is present in wal logs.");
				putsuccessState() ;
				return ;
			}
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
			if(stoppingCriteria())
			{	
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
					// TODO Auto-generated catch block
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
			return systemConfigDao.getLastIdBeforeTriggers(tableName);
		}
	}
	public boolean putFailorInProgressState()
	{
		boolean flag = false ;
		try {
			Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
			if(replicaServerInfoManager.getCurrentMigrationStateAttempts().longValue() > CommunicatorConstant.errorToleranceCount.longValue())
			{ 
				migrationManager.setReplicaMigrationFlagOnCloud(CommonStateUtils.getStateFailAccordingToTableName(tableName));
				flag = true ;
			}
			else
			{
				migrationManager.setReplicaMigrationFlagOnCloud(CommonStateUtils.getStateInProgressAccordingToTableName(tableName));
				flag = false;
			}	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage() , e);
			}
		return flag;
	}
	private void putsuccessState()
	{
		try {
		migrationManager.setReplicaMigrationFlagOnCloud(CommonStateUtils.getStateSuccessAccordingToTableName(tableName)) ;
			Thread.sleep(CommunicatorConstant.bulkThreadSleepCount) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage() , e);
		}
	}
	private boolean keepRunning()
	{
		return replicaServerInfoManager.getTableLastIdSynced(tableName).longValue()!=systemConfigDao.getTableLocalMinId(tableName).longValue() ;
	}
	private boolean stoppingCriteria()
	{
		Long minId = replicaServerInfoManager.getTableLastIdSynced(tableName).longValue();
		logger.info(tableName + " sync done till " + minId);
		return minId == systemConfigDao.getTableLocalMinId(tableName).longValue() ;
	}
}


