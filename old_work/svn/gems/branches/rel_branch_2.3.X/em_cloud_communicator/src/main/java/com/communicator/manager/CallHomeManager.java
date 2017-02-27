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
import com.communicator.dao.EmStatsDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.util.CommunicatorConstant;

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
	CloudConnectionTemplate cloudConnectionTemplate;

	public void sendDataCallHome() {

		logger.info("Starting new sync event at " + new Date());

		CloudRequest cloudrequest = new CloudRequest(serverInfoManager.getMacAddress(), serverInfoManager.getAppVersion());
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudrequest.setNameval(list);
		
		try {
			TimeZone tz = Calendar.getInstance().getTimeZone();
			list.add(new NameValue(CloudParamType.EmTimezone, tz.getID()));
			list.add(new NameValue(CloudParamType.StatsEmAccessible, serverInfoManager.checkEMAcess()));
			emStatsDao.fillCallHomeData(list);
		
			CloudHttpResponse response = cloudConnectionTemplate.executePost(CommunicatorConstant.callHomeService, JsonUtil.getJSONString(cloudrequest) ,serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
			JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
			CloudResponse cloudresponse = jsonUtil.getCloudResponseObject(response.getResponse(), CloudResponse.class);
			HashMap<CloudParamType, String> respMap = cloudresponse.getNameValueMap();
			
			serverInfoManager.setCloudSyncType(respMap.get(CloudParamType.EmCloudSyncStatus));
			
			if (respMap.containsKey(CloudParamType.ReplicaServerIp)) {
				serverInfoManager.setReplicaServerIP(respMap.get(CloudParamType.ReplicaServerIp));
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
						}else if(state.containsKey(CloudParamType.MigrationAttempts))
						{
							replicaServerInfoManager.setCurrentMigrationStateAttempts(Long.valueOf(state.get(CloudParamType.MigrationAttempts)));
						}
						
					}
				}catch(Exception e)
				{
					logger.error("Error while getting migration Details from replica server : " + e.getMessage()) ;
				}
			}
			
			
			taskManager.processTasks(respMap.get(CloudParamType.EmTasks));
			
			if(cloudresponse.getStatus() != 0) {
				logger.info("CALL HOME SYNC MIGHT HAVE FAILED!!");
			}

		} catch (Exception e) {
			logger.error( e.toString(), e);
		}
	}
}
