package com.communicator.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.CloudResponse;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.CommunicatorEntryPoint;
import com.communicator.dao.CloudConfigDao;
import com.communicator.dao.EmStatsDao;
import com.communicator.dao.NetworkSettingsDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.job.CommunicatorJob;
import com.communicator.util.CommonUtils;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.HardwareInfoUtils;

@Service("callHomeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CallHomeManager {

	

	static final Logger logger = Logger.getLogger(CallHomeManager.class
			.getName());

	@Resource
	EmStatsDao emStatsDao;

	@Resource
	ServerInfoManager serverInfoManager;
	@Resource
	TaskManager taskManager;
	@Resource
	ReplicaServerInfoManager replicaServerInfoManager ;
	@Resource
	SystemConfigDao systemConfigDao ;
	@Resource
	NetworkSettingsDao networkSettingsDao ;
	@Resource
	CloudConfigDao cloudConfigDao;
	@Resource
	EmManager emManager;
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate;
	@Resource 
	TunnelingManager tunnelingManager;
	
	private ArrayList<NameValue> responseNameValue = new ArrayList<NameValue>();
	

	public void sendDataCallHome() throws Exception{

		logger.info("Starting new sync event at " + new Date());

		CloudRequest cloudrequest = new CloudRequest(serverInfoManager.getMacAddress(), serverInfoManager.getAppVersion());
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudrequest.setNameval(list);
		
		try {
			TimeZone tz = Calendar.getInstance().getTimeZone();
			list.add(new NameValue(CloudParamType.EmTimezone, tz.getID()));
			list.add(new NameValue(CloudParamType.StatsEmAccessible, serverInfoManager.checkEMAcess()));
			list.add(new NameValue(CloudParamType.IpAddress, HardwareInfoUtils.getIpAddress(networkSettingsDao.getCorporateInterfaceName())));
			list.add(new NameValue(CloudParamType.RemigrationRequired, CommunicatorEntryPoint.remigrationRequired ? "1" : "0"));
			list.addAll(responseNameValue);
			String temp = emManager.getAllFloors();
			if(temp != null && !CommunicatorConstant.CONNECTION_FAILURE.equals(temp)) {
				list.add(new NameValue(CloudParamType.NoOfFloors, temp));
			}
			emStatsDao.fillCallHomeData(list);
			
			logger.info("INFO SEND TO CALL HOME SERVER :- " + JsonUtil.getJSONString(cloudrequest));
			CloudHttpResponse response = cloudConnectionTemplate.executePost(CommunicatorConstant.callHomeService, JsonUtil.getJSONString(cloudrequest) ,serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
			
			if(response.getStatus() == 200) {
				try {
					Runtime rt = Runtime.getRuntime();
					Process proc;
					proc = rt.exec("/opt/enLighted/communicator/last_communication_time.sh");
					proc.waitFor();
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e) ;
				}
			}
			JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
			CloudResponse cloudresponse = jsonUtil.getCloudResponseObject(response.getResponse(), CloudResponse.class);
			HashMap<CloudParamType, String> respMap = cloudresponse.getNameValueMap();
			logger.info("RESPONSE FROM CALL HOME SERVER :- "+ JsonUtil.getJSONString(cloudresponse));
			CommunicatorJob.callHomeSuccess = true;
			serverInfoManager.setCloudSyncType(respMap.get(CloudParamType.EmCloudSyncStatus));
			
			if (respMap.containsKey(CloudParamType.ReplicaServerIp)) {
				serverInfoManager.setReplicaServerIP(respMap.get(CloudParamType.ReplicaServerIp));
			}
			
			if (respMap.containsKey(CloudParamType.EmCloudPauseSyncStatus)) {
				replicaServerInfoManager.setIsSyncPaused("true".equals(respMap.get(CloudParamType.EmCloudPauseSyncStatus)));
			}

			// Setting replica server info based on call home response 
			
			if (respMap.containsKey(CloudParamType.ReplicaServerIp)) {
				replicaServerInfoManager.setReplicaServerIP(respMap.get(CloudParamType.ReplicaServerIp));
			}
			if (respMap.containsKey(CloudParamType.EmCloudSyncStatus)) {
				replicaServerInfoManager.setCurrentEmStatus(respMap.get(CloudParamType.EmCloudSyncStatus).equalsIgnoreCase("1")?EmStatus.CALL_HOME:EmStatus.SPPA);
				//This needs to be update for triggers.
				String status = systemConfigDao.checkCloudConnectivity() ;
				if(!status.equalsIgnoreCase(respMap.get(CloudParamType.EmCloudSyncStatus)))
				{
					systemConfigDao.updateCloudCommunicateType(respMap.get(CloudParamType.EmCloudSyncStatus)) ;
				}
			}
			if (respMap.containsKey(CloudParamType.MigrationStatusDetails)) {
				try {	
				ArrayList<HashMap<CloudParamType, String>> MigrationDetails = null;
				JsonUtil<ArrayList<HashMap<CloudParamType, String>>> jsonUtilListMap = new JsonUtil<ArrayList<HashMap<CloudParamType, String>>>();
				MigrationDetails = jsonUtilListMap.getObject(respMap.get(CloudParamType.MigrationStatusDetails), new TypeReference<ArrayList<HashMap<CloudParamType, String>>>() { });
				
				for (HashMap<CloudParamType, String> state: MigrationDetails) {
						if(state.containsKey(CloudParamType.CurrentMigrationStatus))
						{
							replicaServerInfoManager.setCurrentMigrationState(DatabaseState.valueOf(state.get(CloudParamType.CurrentMigrationStatus)));
							if(replicaServerInfoManager.getCurrentMigrationState().equals(DatabaseState.SYNC_READY)
									|| replicaServerInfoManager.getCurrentMigrationState().equals(DatabaseState.SYNC_FAILED)) {
								CommunicatorJob.syncReady = true;
							}
							else {
								CommunicatorJob.syncReady = false;
							}
							if(replicaServerInfoManager.getCurrentMigrationState().equals(DatabaseState.MIGRATION_READY)) {
								if(CommunicatorEntryPoint.remigrationRequired) {
									cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigRemigrationRequired, "0");
									cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime, ((Long)(new Date().getTime())).toString());
									CommunicatorEntryPoint.remigrationRequired = false;
								}
							}
						}else if(state.containsKey(CloudParamType.MigrationAttempts))
						{
							replicaServerInfoManager.setCurrentMigrationStateAttempts(Long.valueOf(state.get(CloudParamType.MigrationAttempts)));
						}
						
					}
				}catch(Exception e)
				{
					logger.error("Error while getting migration Details from replica server : " + e.getMessage()) ;
					throw e;
				}
			}
			if (respMap.containsKey(CloudParamType.TunnelDetails)) {
				ArrayList<HashMap<CloudParamType, String>> TunnelDetails = null;
				JsonUtil<ArrayList<HashMap<CloudParamType, String>>> jsonUtilListMap = new JsonUtil<ArrayList<HashMap<CloudParamType, String>>>();
				TunnelDetails = jsonUtilListMap.getObject(respMap.get(CloudParamType.TunnelDetails), new TypeReference<ArrayList<HashMap<CloudParamType, String>>>() { });
				if(TunnelDetails.get(0).get(CloudParamType.OpenTunnel).equalsIgnoreCase("true"))
				{
					tunnelingManager.StartTunnel(TunnelDetails.get(0).get(CloudParamType.TunnelPort));
				}else
				{
					tunnelingManager.StopTunnel(TunnelDetails.get(0).get(CloudParamType.TunnelPort));
				}
			}
			if (respMap.containsKey(CloudParamType.SshTunnelDetails)) {
				ArrayList<HashMap<CloudParamType, String>> sshTunnelDetails = null;
				JsonUtil<ArrayList<HashMap<CloudParamType, String>>> jsonUtilListMap = new JsonUtil<ArrayList<HashMap<CloudParamType, String>>>();
				sshTunnelDetails = jsonUtilListMap.getObject(respMap.get(CloudParamType.SshTunnelDetails), new TypeReference<ArrayList<HashMap<CloudParamType, String>>>() { });
				CommonUtils.updatePropertyWithValue("sshTunnelOn", sshTunnelDetails.get(0).get(CloudParamType.OpenSshTunnel) , CommunicatorConstant.configFilePath);
				CommonUtils.updatePropertyWithValue("remoteSshTunnelingPort", sshTunnelDetails.get(0).get(CloudParamType.remoteSshTunnelPort) , CommunicatorConstant.configFilePath);				
			}
			taskManager.processTasks(respMap.get(CloudParamType.EmTasks));
			
			process(respMap.get(CloudParamType.RequestType), respMap.get(CloudParamType.PayLoad), respMap.get(CloudParamType.Uid));
			
			if(cloudresponse.getStatus() != 0) {
				throw new Exception("CALL HOME SYNC MIGHT HAVE FAILED!!");
			}

		} catch (Exception e) {
			CommunicatorJob.callHomeSuccess = false;			
			throw e;
		}
	}
	
	private void process(String requestType, String payload, String uid) {
		
		responseNameValue = new ArrayList<NameValue>();
		
		

		if(requestType != null) {
			responseNameValue.add(new NameValue(CloudParamType.RequestType, requestType));
			if(uid != null) {
				responseNameValue.add(new NameValue(CloudParamType.Uid, uid));
			}
			try{
				if(CloudParamType.valueOf(requestType).getName().equals(requestType)) {
					switch (CloudParamType.valueOf(requestType)) {
						case RequestFacilityTree: {
							String data = emManager.getEmFacilityTree(); //Get TreeNode
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
								responseNameValue.add(new NameValue(CloudParamType.PayLoad, data));
							}
							break;
							
						}
						case AddUEMGateway: {
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.addUpdateUEMGateway(payloads[0],payloads[1], payloads[2]); //Get status. Accept host :::: port :::: key
							}
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
								responseNameValue.add(new NameValue(CloudParamType.PayLoad, data));
							}
							break;								
						}
						case RequestAllSensors: {
							String data = emManager.getAllSensors(); //Get SensorList
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
								responseNameValue.add(new NameValue(CloudParamType.PayLoad, data));
							}
							break;
						}
						case RequestSensor : {
							String data = emManager.getSensorData(payload);	//Get Sensor. Accept macAddress
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
								responseNameValue.add(new NameValue(CloudParamType.PayLoad, data));
							}
							break;
						}
						case RequestFloorPlan : {
							String data = emManager.getFloorPlan(Long.parseLong(payload)); //Get planMap encoded. Accept em floor 1.
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
								responseNameValue.add(new NameValue(CloudParamType.PayLoad, data));
							}
							break;
						}
						case RequestDimLevelAndLastConnectivity : {
							String data = emManager.getDimLevelAndLastConnectivityAt(payload); //Get SensorList. Accept xml fixtures list with mac address.
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
								responseNameValue.add(new NameValue(CloudParamType.PayLoad, data));
							}
							break;
						}
						/*case SetHB: {
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.setOccChangeTrigger(payloads[0], Short.parseShort(payloads[1]), Short.parseShort(payloads[3]), new Short("0")); //Get SensorList. Accept xml fixtures list with mac address :::: enableHb :::: enableRealTime :::: triggerDelayTime
							}
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
							}
							break;
						}*/
						case SetDimLevel: {
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.setDimLevel(payloads[0],Integer.parseInt(payloads[1]), Integer.parseInt(payloads[2])); //Get SensorList. Accept xml fixtures list with mac address :::: percentage :::: time
							}
							if(CommunicatorConstant.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Failure.getName()));
							}
							else {
								responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
							}
							break;
						}
						default : {
							logger.warn("============No Task=======");
							responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.Success.getName()));
						}
					}
				}
			}
			catch (IllegalArgumentException e) {
				responseNameValue.add(new NameValue(CloudParamType.SuccessAck, CloudParamType.NotAllowed.getName()));
				responseNameValue.add(new NameValue(CloudParamType.PayLoad, e.getMessage()));
				logger.error(e.getMessage(), e) ;
			}
		}
		
	}
}
