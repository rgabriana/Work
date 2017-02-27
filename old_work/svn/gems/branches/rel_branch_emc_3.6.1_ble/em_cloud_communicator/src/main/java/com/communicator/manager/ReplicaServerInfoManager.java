package com.communicator.manager;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.CloudHttpResponse;
import com.communicator.util.CommunicatorConstant;

@Service("replicaServerInfoManager")
public class ReplicaServerInfoManager {
	
	static final Logger logger = Logger.getLogger(ReplicaServerInfoManager.class
			.getName());
	@Resource 
	ServerInfoManager serverInfoManager;
	@Resource 
	SecureCloudConnectionTemplate secureCloudConnectionTemplate;
	
	DatabaseState currentMigrationState = DatabaseState.NOT_MIGRATED ;
	Long currentMigrationStateAttempts = 1l;
	EmStatus currentEmStatus = EmStatus.CALL_HOME;
	String replicaServerIP = null ;
	Boolean isSyncPaused = false;
		
	public Boolean getIsSyncPaused() {
		return isSyncPaused;
	}
	public void setIsSyncPaused(Boolean isSyncPaused) {
		this.isSyncPaused = isSyncPaused;
	}
	public DatabaseState getCurrentMigrationState() {
		return currentMigrationState;
	}
	public void setCurrentMigrationState(DatabaseState currentMigrationState) {
		this.currentMigrationState = currentMigrationState;
	}
	public Long getCurrentMigrationStateAttempts() {
		return currentMigrationStateAttempts;
	}
	public void setCurrentMigrationStateAttempts(Long currentMigrationStateAttempts) {
		this.currentMigrationStateAttempts = currentMigrationStateAttempts;
	}
	public EmStatus getCurrentEmStatus() {
		return currentEmStatus;
	}
	public void setCurrentEmStatus(EmStatus currentEmStatus) {
		this.currentEmStatus = currentEmStatus;
	}
	public String getReplicaServerIP() {
		
			return replicaServerIP;
		
	}
	public void setReplicaServerIP(String replicaServerIP) {
		this.replicaServerIP = replicaServerIP;
	}
	
	public Long getTableLastIdSynced(String tableName) {
		Long lastSyncId = -1l; 
		try{
		CloudHttpResponse response = secureCloudConnectionTemplate.executeGet(CommunicatorConstant.getLastIdSyncedService
				+ serverInfoManager.getMacAddress()+"/"+tableName, getReplicaServerIP());
		lastSyncId = Long.parseLong(response.getResponse()) ;
		}catch (NullPointerException e)
		{   
			logger.info(e.getMessage());
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
		return lastSyncId;	
	}
	
	
	public Long getTableMaxIdSynced(String tableName) {
		Long lastSyncId = -1l; 
		try{
		CloudHttpResponse response = secureCloudConnectionTemplate.executeGet(CommunicatorConstant.getMaxIdSyncedService
				+ serverInfoManager.getMacAddress()+"/"+tableName, getReplicaServerIP());
		lastSyncId = Long.parseLong(response.getResponse()) ;
		}catch (NullPointerException e)
		{   
			logger.info(e.getMessage());
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
		return lastSyncId;	
	}
	
	public Boolean getReplicaConnectivity()
	{
		Boolean connectivity = false; 
		try{
		CloudHttpResponse response = secureCloudConnectionTemplate.executeGet(CommunicatorConstant.getReplicaConnectivity, getReplicaServerIP());
		connectivity = Boolean.parseBoolean(response.getResponse()) ;
		}catch (NullPointerException e)
		{   
			logger.info(e.getMessage());
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}finally{
		
		}
		return connectivity;
	}

}
