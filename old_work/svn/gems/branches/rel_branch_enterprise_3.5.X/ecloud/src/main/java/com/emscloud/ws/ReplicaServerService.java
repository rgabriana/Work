package com.emscloud.ws;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.ArgumentUtils;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.emscloud.communication.longpollutil.NewRequest;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmState;
import com.emscloud.model.ReplicaServer;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.MonitoringManager;
import com.emscloud.service.ReplicaServerManager;

@Controller
@Path("/org/replicaserver")
public class ReplicaServerService {
	Logger logger = Logger.getLogger(ReplicaServerService.class) ;
	@Resource
	ReplicaServerManager	replicaServerManager;
	@Resource
	EmInstanceManager emInstanceManager;
	@Resource
	EmStateManager emStateManager;
	
	@Resource 
	MonitoringManager monitoringManager;
	
	@Path("mac/db/cache")
	@POST
	@Consumes({ MediaType.TEXT_PLAIN})
	@Produces({ MediaType.TEXT_PLAIN})
	public String getMacDbRelationByReplicaUID(String request) {
	
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> map = cloudrequest.getNameValueMap();
		
		ReplicaServer replicaServer = replicaServerManager.getReplicaServersbyUid(map.get(CloudParamType.ReplicaServerUID));
		List<EmInstance> emInstances = emInstanceManager.loadEmInstanceByReplicaServerId(replicaServer.getId());
		
		HashMap<String, String> macDb = new HashMap<String, String>();
		if(emInstances != null) {
			for(EmInstance emInstance: emInstances) {
				macDb.put(emInstance.getMacId(), emInstance.getDatabaseName());
			}
		}
		
		String output = JsonUtil.getJSONString(macDb);
		System.out.println(output);
		return output;
	
	}
	
	@Path("macuid")
	@POST
	@Consumes({MediaType.TEXT_PLAIN})
	@Produces({ MediaType.TEXT_PLAIN})
	public String getReplicaMacIdUIDRelationByReplicaMacId(String macId) {
	
		ReplicaServer replicaServer = replicaServerManager.getReplicaServersbyMacId(macId);
		
		String uid = null;
		if(replicaServer != null){
			uid = replicaServer.getUid();
		}
		return uid;
	}
	
	
	@Path("set/sync/state/log/{mac}/{state}/{log}/{lastSyncSuccessTime}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public void setEmSyncStateLog(@PathParam("mac") String mac , @PathParam("state") DatabaseState state, @PathParam("log") String log, @PathParam("lastSyncSuccessTime") Long lastSyncSuccessTime) {
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null ;
			if(emInstance!=null)
			{
				emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
				if(!checkIfRestrictedMigrationTriggered(mac, state, emState, emInstance)){
					return;
				}
				if(emState.getDatabaseState().getName().equalsIgnoreCase(state.getName()))  {
					if(emState.getDatabaseState().getName().equals(DatabaseState.SYNC_FAILED.getName())) {
						emState.setFailedAttempts(emState.getFailedAttempts() + 1);
						if(emState.getFailedAttempts() >= 3) {
							emInstance.setPauseSyncStatus(true);
							emInstanceManager.saveOrUpdate(emInstance);
						}
						emState.setLog(emState.getLog() + "," +  log);
					}
					else {
						emState.setLog(log);
					}
					
				} 
				else {
					emState = new EmState() ;
					emState.setFailedAttempts(0);
					emState.setEmInstanceId(emInstance.getId());
					emState.setDatabaseState(state);
					
					if(state.getName().contains("_IN_")) {
						emState.setLog("{lastMinId/Exception}{,} :- ");
					}
					else if(state.getName().equalsIgnoreCase(DatabaseState.SYNC_FAILED.getName()) || state.getName().equalsIgnoreCase(DatabaseState.SYNC_READY.getName())){
						emState.setLog(log);
					}
					
					if (emInstance.getSppaEnabled()) {
						emState.setEmStatus(EmStatus.SPPA);
					} 
					else {
						emState.setEmStatus(EmStatus.CALL_HOME);
					}
				}
				
				emState.setSetTime(Calendar.getInstance().getTime()) ;
				emStateManager.saveOrUpdate(emState);
				
				if(lastSyncSuccessTime != null && lastSyncSuccessTime.compareTo(-1L) != 0) {
					emInstance.setLastSuccessfulSyncTime(new Date(lastSyncSuccessTime));
					emInstanceManager.saveOrUpdate(emInstance);
				}
				
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
    }

	public boolean checkIfRestrictedMigrationTriggered(String mac,
			DatabaseState state, EmState emState, EmInstance emInstance) {
		boolean flagToUpdateState = true;
		//We have 2 threads in communicator CommunicatorJob and CallHome. We need to handle the scnerio here if we are directly updating the em_state in Master from UI or anywhere 
		//and EM/Replica is not aware of this.
		//For Restricted Migration if the last state in em_state table is RESTRICTED_REMIGRATION_READY and the state to be updates is either SYNC_READY or SYNC_FAIL then do not update that state
		final boolean isSkipForRestrictedMigration = 
					(emState.getDatabaseState() == DatabaseState.RESTRICTED_REMIGRATION_READY)
				&& 	(state == DatabaseState.SYNC_READY || state == DatabaseState.SYNC_FAILED)
				&&  emInstance.getPauseSyncStatus();
		if(isSkipForRestrictedMigration){
			logger.error(state.getName()+" is not updated for mac "+mac+" as restricted migration is triggered in the master.");
			flagToUpdateState = false;
		}
		return flagToUpdateState;
	}
	
	@Path("v2/set/state/migration/{mac}/{state}/{lastSyncSuccessTime}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
	@Produces({ MediaType.TEXT_PLAIN})
    public String setEmMigrationState_V2(@PathParam("mac") String mac , @PathParam("state") DatabaseState state, @PathParam("lastSyncSuccessTime") Long lastSyncSuccessTime) {
			String result = null ;
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null;
			if(emInstance!=null)
			{
				emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
				if(!checkIfRestrictedMigrationTriggered(mac, state, emState,emInstance)){
					return emState.getDatabaseState().getName();
				}
				//Check if the state is SYNC_READY and to identify if the last immediate state is RESTRICTED_REMIGRATION_SUCCESS
				//then we need to set the pause_sync flag to false as well because even if this has to be done after completion of all restricted migration process but doing
				//intermittent is good so that in case of any further states (say for plugload) get added it will work. and even if the sync is ready state but last state is not SYNC_READY
				//so it will anyways will do nothing sync activity and we are already removing the task details from the cache (SyncData.run).
				final boolean isPartialMigrationDone = emState.getDatabaseState() == DatabaseState.RESTRICTED_REMIGRATION_SUCCESS;
				final boolean isPartialMigrationFailed = 
						emState.getDatabaseState() == DatabaseState.RESTRICTED_ENERGY_CONSUMPTION_DAILY_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_ENERGY_CONSUMPTION_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_ENERGY_CONSUMPTION_HOURLY_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_PLUGLOAD_ENERGY_CONSUMPTION_DAILY_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_PLUGLOAD_ENERGY_CONSUMPTION_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_PLUGLOAD_ENERGY_CONSUMPTION_HOURLY_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_MOTION_BIT_FAIL ||
						emState.getDatabaseState() == DatabaseState.RESTRICTED_REMIGRATION_FAIL;
				
				if(emState.getDatabaseState().getName().equalsIgnoreCase(state.getName())) 
				{
					emState.setFailedAttempts(emState.getFailedAttempts() +1);
					emState.setLog(emState.getLog());
				}else
				{
					emState = new EmState() ;
					emState.setFailedAttempts(0);
					emState.setEmInstanceId(emInstance.getId());
					emState.setDatabaseState(state);
					if(state.getName().contains("_IN_"))
					{
						emState.setLog("{lastMinId/Exception}{,} :- ");
					}
					if (emInstance.getSppaEnabled()) {
						emState.setEmStatus(EmStatus.SPPA);
					} else {
						emState.setEmStatus(EmStatus.CALL_HOME);
					}
				}
				
				if(lastSyncSuccessTime != null && lastSyncSuccessTime.compareTo(-1L) != 0) {
					emInstance.setLastSuccessfulSyncTime(new Date(lastSyncSuccessTime));
					emInstanceManager.saveOrUpdate(emInstance);
				}
				
				emState.setSetTime(Calendar.getInstance().getTime()) ;
				EmState newstat = emStateManager.saveOrUpdate(emState);
				
				if(isPartialMigrationFailed){
					//Partial Migration failed hence add one more state as SYNC_FAILED
					emState = new EmState() ;
					emState.setFailedAttempts(0);
					emState.setEmInstanceId(emInstance.getId());
					emState.setDatabaseState(DatabaseState.SYNC_FAILED);
					
					if (emInstance.getSppaEnabled()) {
						emState.setEmStatus(EmStatus.SPPA);
					} 
					else {
						emState.setEmStatus(EmStatus.CALL_HOME);
					}
					newstat = emStateManager.saveOrUpdate(emState);
				}
				
				Thread taskThread = new Thread(new NewRequest.AddEmTaskToQueue(emInstance.getMacId().toUpperCase(), (short) 0, 30000L));
				taskThread.start();
				result = newstat.getDatabaseState().getName() ;
				if(isPartialMigrationDone){
					emInstanceManager.updatePauseResumeStatus(emInstance.getId(), false);
				}
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
    }
	

	@Path("set/state/migration/{mac}/{state}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
	@Produces({ MediaType.TEXT_PLAIN})
    public String setEmMigrationState(@PathParam("mac") String mac , @PathParam("state") DatabaseState state) {
			String result = null ;
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null;
			if(emInstance!=null)
			{
				emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
				if(emState.getDatabaseState().getName().equalsIgnoreCase(state.getName())) 
				{
					emState.setFailedAttempts(emState.getFailedAttempts() +1);
					emState.setLog(emState.getLog());
				}else
				{
					emState = new EmState() ;
					emState.setFailedAttempts(0);
					emState.setEmInstanceId(emInstance.getId());
					emState.setDatabaseState(state);
					if(state.getName().contains("_IN_"))
					{
						emState.setLog("{lastMinId/Exception}{,} :- ");
					}
					if (emInstance.getSppaEnabled()) {
						emState.setEmStatus(EmStatus.SPPA);
					} else {
						emState.setEmStatus(EmStatus.CALL_HOME);
					}
				}
				
				emState.setSetTime(Calendar.getInstance().getTime()) ;
				EmState newstat = emStateManager.saveOrUpdate(emState);
				Thread taskThread = new Thread(new NewRequest.AddEmTaskToQueue(emInstance.getMacId().toUpperCase(), (short) 0, 30000L));
				taskThread.start();
				result = newstat.getDatabaseState().getName() ;
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
    }
	
	@Path("append/migration/state/log/{mac}/{state}/{log}/{lastSyncSuccessTime}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public void setEmMigrationState(@PathParam("mac") String mac , @PathParam("state") DatabaseState state,@PathParam("log") String log, @PathParam("lastSyncSuccessTime") Long lastSyncSuccessTime) {
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null ;
			if(emInstance!=null)
			{
				if(emStateManager.loadEmStateById(emInstance.getLatestEmStateId()).getDatabaseState().getName().equalsIgnoreCase(state.getName())) 
				{
					emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId()) ;
					if(!ArgumentUtils.isNullOrEmpty(emState.getLog()))
					{ 
						emState.setLog(emState.getLog()+","+log);
					}else
					{
						emState.setLog(log);
					}
					emStateManager.saveOrUpdate(emState);
				}
				else
				{
					logger.info("States mismatch. Cannot update the log of EM with mac "+ mac +" .Its with state " + emStateManager.loadEmStateById(emInstance.getLatestEmStateId()).getDatabaseState().getName() + " .Trying to update log for state " + state) ;
				}
				if(lastSyncSuccessTime != null && lastSyncSuccessTime.compareTo(-1L) != 0) {
					emInstance.setLastSuccessfulSyncTime(new Date(lastSyncSuccessTime));
					emInstanceManager.saveOrUpdate(emInstance);
				}
				
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}	
	
	
	@Path("set/previous/state/{mac}")
    @GET
    @Consumes({MediaType.TEXT_PLAIN})
	@Produces({ MediaType.TEXT_PLAIN})
    public String setPreviousEmMigrationState(@PathParam("mac") String mac ) {
			String result = null ;
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null ;
			if(emInstance!=null)
			{
				emState =emStateManager.resetPreviousFlagByEmInstanceId(emInstance.getId()) ;
				result = emState.getDatabaseState().getName() ;
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
    }
	
	@Path("device/health/{mac}")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
	public void updateDeviceHealthMonitor(@PathParam("mac")String mac, @FormParam("totalGW")int totalGW,
			@FormParam("uoGW") int uoGW, @FormParam("cGW")int cGW, @FormParam("totalSensors")int totalSensors,
			@FormParam("uoSensors")int uoSensors, @FormParam("criticalSensors")int criticalSensors){
	     monitoringManager.updateHealthMonitor(mac, totalGW, uoGW, cGW, totalSensors, uoSensors, criticalSensors);	
	}
	
    /**
     * get pause_sync status of EmInstance
     * 
     * @param id
     *            EmInstance unique identifier
     * @return boolean is sync paused
     */
    @Path("isSyncPaused/{mac}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String isSyncPaused(@PathParam("mac") String mac) {
    	return emInstanceManager.isSyncPaused(mac) ? "Y" : "N";
    }

}
