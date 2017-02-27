package com.emscloud.ws;

import java.util.Calendar;
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
	
	
	

	@Path("set/state/migration/{mac}/{state}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
	@Produces({ MediaType.TEXT_PLAIN})
    public String setEmMigrationState(@PathParam("mac") String mac , @PathParam("state") DatabaseState state) {
			String result = null ;
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null ;
			if(emInstance!=null)
			{
				if(emStateManager.loadEmStateById(emInstance.getLatestEmStateId()).getDatabaseState().getName().equalsIgnoreCase(state.getName())) 
				{
					emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId()) ;
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
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		
		return result;
    }
	
	@Path("append/migration/state/log/{mac}/{state}/{log}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public void setEmMigrationState(@PathParam("mac") String mac , @PathParam("state") DatabaseState state,@PathParam("log") String log) {
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
				
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
		}
		
    }
	@Path("set/sync/state/log/{mac}/{state}/{log}")
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public void setEmSyncStateLog(@PathParam("mac") String mac , @PathParam("state") DatabaseState state,@PathParam("log") String log) {
		try{
			EmInstance emInstance = emInstanceManager.loadEmInstanceByMac(mac);
			EmState emState  = null ;
			if(emInstance!=null)
			{
				if(emStateManager.loadEmStateById(emInstance.getLatestEmStateId()).getDatabaseState().getName().equalsIgnoreCase(state.getName())) 
				{
					emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId()) ;
					emState.setLog(log);
					emStateManager.saveOrUpdate(emState);
				}
				else
				{
					logger.info("States mismatch. Cannot update the log of EM with mac "+ mac +" .Its with state " + emStateManager.loadEmStateById(emInstance.getLatestEmStateId()).getDatabaseState().getName() + " .Trying to update log for state " + state) ;
				}
				
			}else 
			{
				logger.info("There is no EM with "+ mac + "mac" ) ;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
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
		catch(Exception e)
		{
			e.printStackTrace() ;
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

}
