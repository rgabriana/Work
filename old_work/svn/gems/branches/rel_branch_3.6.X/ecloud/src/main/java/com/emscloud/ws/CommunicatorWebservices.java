package com.emscloud.ws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.communication.types.CloudParamType;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.CloudRequest;
import com.communication.utils.CloudResponse;
import com.communication.utils.EmTasksUUIDUtil;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communication.vo.OSDetails;
import com.emscloud.communication.longpollutil.RequestsBlockingPriorityQueue;
import com.emscloud.model.AppInstance;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmSync;
import com.emscloud.model.EmTasks;
import com.emscloud.model.EmTasksUUID;
import com.emscloud.service.AppInstanceManager;
import com.emscloud.service.CertificateManager;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.service.EmTasksManager;
import com.emscloud.service.EmTasksUUIDManager;
import com.emscloud.service.GlemManager;
import com.emscloud.service.ReplicaServerManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.types.GlemModeType;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import com.sun.jersey.multipart.FormDataParam;

@Controller
@Path("/org/communicate")
public class CommunicatorWebservices {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Resource
	EmInstanceManager emInstanceManger ;
	@Resource
	AppInstanceManager appInstanceManger ;
	
	@Resource
	ReplicaServerManager replicaServerManager ;
	@Resource
	CustomerManager customerManager ;
	@Resource
	EmStatsManager emStatsManager;
	@Resource
	EmTasksManager emTasksManager;
	@Resource
	EmStateManager emStateManager;
	@Resource
	SystemConfigurationManager sysConfigManager;
	@Resource
	CertificateManager certificateManager;	
	@Resource
	EmTasksUUIDManager emTasksUUIDManager;
	
	@Resource
	private GlemManager glemManager;
	
	static final Logger logger = Logger
			.getLogger(CommunicatorWebservices.class.getName());
	
	
	@Path("em/uemsyncstatus")
	@POST
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public com.emscloud.model.oldem.Response getEmUEMSyncStatus(EmSync request) {
		
		com.emscloud.model.oldem.Response response = new com.emscloud.model.oldem.Response();

		// return from the procedure if mac address not found
		if (request == null)
			return null;

		if (request.getMacAddress() == null
				|| request.getMacAddress().equalsIgnoreCase("")
				|| request.getIpAddress() == null
				|| request.getIpAddress().equalsIgnoreCase("")
				|| request.getVersion() == null
				|| request.getVersion().equalsIgnoreCase("")) {
			return null;
		}
		
		if (glemManager.getGLEMMode() == GlemModeType.UEM.getMode()) {
			
		EmInstance emInstance = emInstanceManger.loadEmInstanceByMac(request
				.getMacAddress().trim());
		// Update the IP address , version if they are changed

		if (emInstance != null) {
			// For older entries
			// Entry is there in the table.			

			String requestEmAck = request.getACK();
			Integer emAck = Integer.parseInt(requestEmAck);
			
			if(request.getTimeZone() !=null) {
				emInstance.setTimeZone(request.getTimeZone().trim());
			}

			if (request.getIpAddress() != null) {
				emInstance.setIpAddress(request.getIpAddress());
			}

			if (request.getVersion() != null) {
				emInstance.setVersion(request.getVersion());
			}

			if (request.getEmName() != null && emInstance.getName() == null) {
				emInstance.setName(request.getEmName());
			}

			emInstance.setNoOfFloors(Integer.parseInt(request.getNoOfFloors().trim()));
			
			emInstance.setLastConnectivityAt(new Date());
			emInstanceManger.saveOrUpdate(emInstance);			
			Integer stateOnUem = emInstance.getActive() ? 1 : 0;
			
			System.out.println("Mac::" + emInstance.getMacId() + ", " +
						"apiKey:::" + emInstance.getApiKey() + ", " +
						"secretKey:::" + emInstance.getSecretKey() + ", " +
						"emAck:::" + emAck + ", " +
						"stateOnUem:::" + stateOnUem
						);
			
			if(stateOnUem == 0)
			{
				response.setStatus(0);
				response.setMsg("");				
			}
			else if(emAck==0 && emInstance.getApiKey()!=null && !emInstance.getApiKey().equalsIgnoreCase(""))
			{
				response.setStatus(1);
				response.setMsg(emInstance.getApiKey());	
				response.setCommMessage(emInstance.getSecretKey());		// set secret key			
			}
			else if(emAck == 1 && emInstance.getApiKey()!=null && !emInstance.getApiKey().equalsIgnoreCase(""))
			{
				//Update the status to 2				
				response.setStatus(2);
				response.setMsg("");			
			}
			else if(emAck == 2)  {
				response.setStatus(2);
				response.setMsg("");				
			}
		} else {
			// For new entries
			EmInstance emInstanceNew = new EmInstance();
			emInstanceNew.setName(request.getEmName().trim());
			emInstanceNew.setApiKey(null);
			emInstanceNew.setIpAddress(request.getIpAddress().trim());
			emInstanceNew.setLastConnectivityAt(new Date());
			emInstanceNew.setMacId(request.getMacAddress().toUpperCase().trim());
			emInstanceNew.setVersion(request.getVersion().trim());
			emInstanceNew.setTimeZone(request.getTimeZone().trim());
			emInstanceNew.setNoOfFloors(Integer.parseInt(request.getNoOfFloors().trim()));
			emInstanceNew.setActive(false);	
			emInstanceNew.setLastSuccessfulSyncTime(new Date());
			emInstanceNew.setPauseSyncStatus(false);
			emInstanceManger.saveOrUpdate(emInstanceNew);
			response.setStatus(1);
			response.setMsg("");		//Return empty api Key as no api Key is generated for new entries.
		}
			
		}
				
		return response;
	}
	
	@Path("em/callhome/v4")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String call_home_communicate_v4(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		EmInstance emInstance = null ;
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		
		try {
			System.out.println("Mac Id = " + cloudrequest.getMacId() + 
								"   App Version = " + cloudrequest.getAppVersion() + 
								" Timezone = " + reqMap.get(CloudParamType.EmTimezone));
			
			emInstance = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId());
			if(emInstance != null) {
				updateOldEMInstanceArchAndVersion(emInstance);
				if(reqMap.get(CloudParamType.Uid) != null && RequestsBlockingPriorityQueue.getCache().getIfPresent(reqMap.get(CloudParamType.Uid)) != null && reqMap.get(CloudParamType.SuccessAck) != null) {
					if(CloudParamType.Success.getName().equals(reqMap.get(CloudParamType.SuccessAck)) && reqMap.get(CloudParamType.PayLoad) != null) {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), reqMap.get(CloudParamType.PayLoad));
					}
					else if (CloudParamType.NotAllowed.getName().equals(reqMap.get(CloudParamType.SuccessAck)) ) {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), CloudParamType.NotAllowed.getName());
					}
					else {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), CloudParamType.Failure.getName());
					}
				}
				
				TimeZone tz = TimeZone.getTimeZone(reqMap.get(CloudParamType.EmTimezone));
				if (tz != null) {
					emInstance.setTimeZone(tz.getID());
				}
				emInstance.setVersion(cloudrequest.getAppVersion());
				if(reqMap.get(CloudParamType.NoOfFloors) != null && !"null".equals(reqMap.get(CloudParamType.NoOfFloors))) {
					emInstance.setNoOfFloors(Integer.parseInt(reqMap.get(CloudParamType.NoOfFloors)));
				}
				if(reqMap.get(CloudParamType.IpAddress) != null) {
					emInstance.setIpAddress(reqMap.get(CloudParamType.IpAddress));
				}
				emInstance.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(emInstance);
				
				EmStats emStats = new EmStats();
				emStats.setEmInstanceId(emInstance.getId());
				emStats.setCaptureAt(new Date(Long.parseLong(reqMap.get(CloudParamType.StatsCaptureAt))));
				emStats.setActiveThreadCount(Integer.parseInt(reqMap.get(CloudParamType.StatsActiveThreadCount)));
				emStats.setGcCount(Long.parseLong(reqMap.get(CloudParamType.StatsGcCount)));
				emStats.setGcTime(Long.parseLong(reqMap.get(CloudParamType.StatsGcTime)));
				emStats.setHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsHeadUsed)));
				emStats.setNonHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsNonHeapUsed)));
				emStats.setSysLoad(Double.parseDouble(reqMap.get(CloudParamType.StatsSysLoad)));
				emStats.setCpuPercentage(Float.parseFloat(reqMap.get(CloudParamType.StatsCpuPercentage)));
				emStats.setIsEmAccessible("TRUE".equals(reqMap.get(CloudParamType.StatsEmAccessible)));
				emStatsManager.saveObject(emStats);
				
				
				if(!RequestsBlockingPriorityQueue.getMap().containsKey(emInstance.getMacId().toUpperCase().replaceAll(":", ""))) {
					RequestsBlockingPriorityQueue queue = new RequestsBlockingPriorityQueue(emInstance.getMacId().toUpperCase().replaceAll(":", ""));
					RequestsBlockingPriorityQueue.getMap().put(queue.getMacId(), queue);
				}
				
				if(reqMap.get(CloudParamType.RemigrationRequired) != null && reqMap.get(CloudParamType.RemigrationRequired).equals("1")) {
					EmState state = null;
					if(emInstance.getLatestEmStateId()!=null) {
						state = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
						if(!(state.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.REMIGRATION_REQUIRED.getName())
								|| state.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.NOT_MIGRATED.getName())
								)) {
							//update the state
							EmState emState = new EmState() ;
							emState.setEmInstanceId(emInstance.getId());
							emState.setDatabaseState(DatabaseState.REMIGRATION_REQUIRED);
							emState.setEmStatus(EmStatus.SPPA);
							emState.setSetTime(Calendar.getInstance().getTime());
							emState.setFailedAttempts(0);
							emStateManager.saveOrUpdate(emState);
							
							logger.warn("Setting remigration required flag for em " + cloudrequest.getMacId() );
						}
					}
				}
				list.addAll(RequestsBlockingPriorityQueue.getMap().get(emInstance.getMacId().toUpperCase().replaceAll(":", "")).getNameValues());
				emInstanceManger.evict(emInstance);
				emInstance = emInstanceManger.getEmInstance(emInstance.getId());
				if(emInstance != null){
					if(emInstance.getSppaEnabled()) {
						list.add(new NameValue(CloudParamType.ReplicaServerIp, replicaServerManager.getReplicaServersbyId(emInstance.getReplicaServer().getId()).getIp()));
					}
					
					String syncStatus = "0";
					if(emInstance.getActive()) {
						syncStatus = "1";
						if(emInstance.getSppaEnabled()) {
							syncStatus = "2";
						}
					}
					
					list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
					
					ArrayList<HashMap<CloudParamType, String>> migrationMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> migrationMap = new HashMap<CloudParamType, String>();
					EmState emState = null;
					if(emInstance.getLatestEmStateId()!=null)
					{
						emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
						migrationMap.put(CloudParamType.CurrentMigrationStatus, emState.getDatabaseState().getName());
						migrationMap.put(CloudParamType.MigrationAttempts, String.valueOf(emState.getFailedAttempts()));
						migrationMapList.add(migrationMap);
						list.add(new NameValue(CloudParamType.MigrationStatusDetails ,JsonUtil.getJSONString(migrationMapList)));
					}
					
					String pauseSyncStatus = "false";
					if(emInstance.getPauseSyncStatus()) {
						pauseSyncStatus = "true";	
					}
					else {
						if(emState != null) {
							if(emState.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.SYNC_FAILED.getName()) && emState.getFailedAttempts() >= 3) {
								pauseSyncStatus = "true";
							}
						}
					}
					list.add(new NameValue(CloudParamType.EmCloudPauseSyncStatus, pauseSyncStatus));
					
					ArrayList<HashMap<CloudParamType, String>> mapList = new ArrayList<HashMap<CloudParamType,String>>();
					List<EmTasks> emTasks = emTasksManager.getActiveEmTasksByEmInstanceId(emInstance.getId());
					for(EmTasks emTask: emTasks) {
						HashMap<CloudParamType, String> map = new HashMap<CloudParamType, String>();
						map.put(CloudParamType.TaskCode, emTask.getTaskCode().getName());
						map.put(CloudParamType.TaskStatus, emTask.getTaskStatus().getName());
						map.put(CloudParamType.TaskProgressStatus, emTask.getProgressStatus() == null ? null : emTask.getProgressStatus().getName());
						map.put(CloudParamType.TaskAttempts, new Integer(emTask.getNumberOfAttempts()).toString());
						map.put(CloudParamType.TaskId, new Long(emTask.getId()).toString());
						map.put(CloudParamType.TaskParameters, emTask.getParameters() == null ? null : emTask.getParameters());
						mapList.add(map);
					}
					
					list.add(new NameValue(CloudParamType.EmTasks, JsonUtil.getJSONString(mapList)));
					
					//For tunneling 
					ArrayList<HashMap<CloudParamType, String>> tunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> tunnelMap = new HashMap<CloudParamType, String>();
					tunnelMap.put(CloudParamType.OpenTunnel, String.valueOf(emInstance.getOpenTunnelToCloud()));
					tunnelMap.put(CloudParamType.TunnelPort, String.valueOf(emInstance.getTunnelPort()));
					tunnelMapList.add(tunnelMap);
					list.add(new NameValue(CloudParamType.TunnelDetails ,JsonUtil.getJSONString(tunnelMapList)));
					

					//For SSH tunneling 
					ArrayList<HashMap<CloudParamType, String>> sshTunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> sshTunnelMap = new HashMap<CloudParamType, String>();
					sshTunnelMap.put(CloudParamType.OpenSshTunnel, String.valueOf(emInstance.getOpenSshTunnelToCloud()));
					sshTunnelMap.put(CloudParamType.remoteSshTunnelPort, String.valueOf(emInstance.getSshTunnelPort()));
					sshTunnelMapList.add(sshTunnelMap);
					list.add(new NameValue(CloudParamType.SshTunnelDetails ,JsonUtil.getJSONString(sshTunnelMapList)));
				}
				else {
					cloudresponse.setStatus(1);
					cloudresponse.setMsg("Em Instance Not Registered");
				}
				}
				
		} catch (Exception e) {
			cloudresponse.setStatus(1);
			cloudresponse.setMsg("Unexpected error on server");
			e.printStackTrace();
		}
		

		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}
	
	private final void updateOldEMInstanceArchAndVersion(final EmInstance em){
		if (em != null){
			em.setOsArch("32");
			em.setOsVersion("10.04");
		}
	}
	@Path("em/callhome/v5")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String call_home_communicate_v5(String request) {

		System.out.println("em/callhome/v5:request::" + request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request,
				CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();

		EmInstance emInstance = null;
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);

		try {
			System.out.println("Mac Id = " + cloudrequest.getMacId()
					+ "   App Version = " + cloudrequest.getAppVersion()
					+ " Timezone = " + reqMap.get(CloudParamType.EmTimezone));

			emInstance = emInstanceManger.loadEmInstanceByMac(cloudrequest
					.getMacId());
			if (emInstance != null) {
				if (cloudrequest != null && cloudrequest.getOs() != null){
					final OSDetails os = cloudrequest.getOs();
					if (!StringUtils.isEmpty(os.getArch())){
						emInstance.setOsArch(os.getArch());
					}
					if (!StringUtils.isEmpty(os.getVersion())){
						emInstance.setOsVersion(os.getVersion());
					}
				}
				if (reqMap.get(CloudParamType.Uid) != null
						&& RequestsBlockingPriorityQueue.getCache()
								.getIfPresent(reqMap.get(CloudParamType.Uid)) != null
						&& reqMap.get(CloudParamType.SuccessAck) != null) {
					if (CloudParamType.Success.getName().equals(
							reqMap.get(CloudParamType.SuccessAck))
							&& reqMap.get(CloudParamType.PayLoad) != null) {
						RequestsBlockingPriorityQueue.getCache().put(
								reqMap.get(CloudParamType.Uid),
								reqMap.get(CloudParamType.PayLoad));
					} else if (CloudParamType.NotAllowed.getName().equals(
							reqMap.get(CloudParamType.SuccessAck))) {
						RequestsBlockingPriorityQueue.getCache().put(
								reqMap.get(CloudParamType.Uid),
								CloudParamType.NotAllowed.getName());
					} else {
						RequestsBlockingPriorityQueue.getCache().put(
								reqMap.get(CloudParamType.Uid),
								CloudParamType.Failure.getName());
					}
				}

				TimeZone tz = TimeZone.getTimeZone(reqMap
						.get(CloudParamType.EmTimezone));
				if (tz != null) {
					emInstance.setTimeZone(tz.getID());
				}
				emInstance.setVersion(cloudrequest.getAppVersion());
				if (reqMap.get(CloudParamType.NoOfFloors) != null
						&& !"null"
								.equals(reqMap.get(CloudParamType.NoOfFloors))) {
					emInstance.setNoOfFloors(Integer.parseInt(reqMap
							.get(CloudParamType.NoOfFloors)));
				}
				if (reqMap.get(CloudParamType.IpAddress) != null) {
					emInstance.setIpAddress(reqMap
							.get(CloudParamType.IpAddress));
				}
				emInstance.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(emInstance);

				EmStats emStats = new EmStats();
				emStats.setEmInstanceId(emInstance.getId());
				emStats.setCaptureAt(new Date(Long.parseLong(reqMap
						.get(CloudParamType.StatsCaptureAt))));
				emStats.setActiveThreadCount(Integer.parseInt(reqMap
						.get(CloudParamType.StatsActiveThreadCount)));
				emStats.setGcCount(Long.parseLong(reqMap
						.get(CloudParamType.StatsGcCount)));
				emStats.setGcTime(Long.parseLong(reqMap
						.get(CloudParamType.StatsGcTime)));
				emStats.setHeapUsed(Double.parseDouble(reqMap
						.get(CloudParamType.StatsHeadUsed)));
				emStats.setNonHeapUsed(Double.parseDouble(reqMap
						.get(CloudParamType.StatsNonHeapUsed)));
				emStats.setSysLoad(Double.parseDouble(reqMap
						.get(CloudParamType.StatsSysLoad)));
				emStats.setCpuPercentage(Float.parseFloat(reqMap
						.get(CloudParamType.StatsCpuPercentage)));
				emStats.setIsEmAccessible("TRUE".equals(reqMap
						.get(CloudParamType.StatsEmAccessible)));
				emStatsManager.saveObject(emStats);

				if (!RequestsBlockingPriorityQueue.getMap()
						.containsKey(
								emInstance.getMacId().toUpperCase()
										.replaceAll(":", ""))) {
					RequestsBlockingPriorityQueue queue = new RequestsBlockingPriorityQueue(
							emInstance.getMacId().toUpperCase()
									.replaceAll(":", ""));
					RequestsBlockingPriorityQueue.getMap().put(
							queue.getMacId(), queue);
				}

				if (reqMap.get(CloudParamType.RemigrationRequired) != null
						&& reqMap.get(CloudParamType.RemigrationRequired)
								.equals("1")) {
					EmState state = null;
					if (emInstance.getLatestEmStateId() != null) {
						state = emStateManager.loadEmStateById(emInstance
								.getLatestEmStateId());
						if (!(state
								.getDatabaseState()
								.getName()
								.equalsIgnoreCase(
										DatabaseState.REMIGRATION_REQUIRED
												.getName()) || state
								.getDatabaseState()
								.getName()
								.equalsIgnoreCase(
										DatabaseState.NOT_MIGRATED.getName()))) {
							// update the state
							EmState emState = new EmState();
							emState.setEmInstanceId(emInstance.getId());
							emState.setDatabaseState(DatabaseState.REMIGRATION_REQUIRED);
							emState.setEmStatus(EmStatus.SPPA);
							emState.setSetTime(Calendar.getInstance().getTime());
							emState.setFailedAttempts(0);
							emStateManager.saveOrUpdate(emState);

							logger.warn("Setting remigration required flag for em "
									+ cloudrequest.getMacId());
						}
					}
				}
				list.addAll(RequestsBlockingPriorityQueue
						.getMap()
						.get(emInstance.getMacId().toUpperCase()
								.replaceAll(":", "")).getNameValues());
				emInstanceManger.evict(emInstance);
				emInstance = emInstanceManger.getEmInstance(emInstance.getId());
				if (emInstance != null) {
					if (emInstance.getSppaEnabled()) {
						list.add(new NameValue(CloudParamType.ReplicaServerIp,
								replicaServerManager.getReplicaServersbyId(
										emInstance.getReplicaServer().getId())
										.getIp()));
					}

					String syncStatus = "0";
					if (emInstance.getActive()) {
						syncStatus = "1";
						if (emInstance.getSppaEnabled()) {
							syncStatus = "2";
						}
					}

					list.add(new NameValue(CloudParamType.EmCloudSyncStatus,
							syncStatus));

					ArrayList<HashMap<CloudParamType, String>> migrationMapList = new ArrayList<HashMap<CloudParamType, String>>();
					HashMap<CloudParamType, String> migrationMap = new HashMap<CloudParamType, String>();
					EmState emState = null;
					if (emInstance.getLatestEmStateId() != null) {
						emState = emStateManager.loadEmStateById(emInstance
								.getLatestEmStateId());
						migrationMap.put(CloudParamType.CurrentMigrationStatus,
								emState.getDatabaseState().getName());
						migrationMap.put(CloudParamType.MigrationAttempts,
								String.valueOf(emState.getFailedAttempts()));
						migrationMapList.add(migrationMap);
						list.add(new NameValue(
								CloudParamType.MigrationStatusDetails, JsonUtil
										.getJSONString(migrationMapList)));
					}

					String pauseSyncStatus = "false";
					if (emInstance.getPauseSyncStatus()) {
						pauseSyncStatus = "true";
					} else {
						if (emState != null) {
							if (emState
									.getDatabaseState()
									.getName()
									.equalsIgnoreCase(
											DatabaseState.SYNC_FAILED.getName())
									&& emState.getFailedAttempts() >= 3) {
								pauseSyncStatus = "true";
							}
						}
					}
					list.add(new NameValue(
							CloudParamType.EmCloudPauseSyncStatus,
							pauseSyncStatus));

					ArrayList<HashMap<CloudParamType, String>> mapList = new ArrayList<HashMap<CloudParamType, String>>();
					List<EmTasks> emTasks = emTasksManager
							.getActiveEmTasksByEmInstanceId(emInstance.getId());
					for (EmTasks emTask : emTasks) {
						HashMap<CloudParamType, String> map = new HashMap<CloudParamType, String>();
						map.put(CloudParamType.TaskCode, emTask.getTaskCode()
								.getName());
						map.put(CloudParamType.TaskStatus, emTask
								.getTaskStatus().getName());
						map.put(CloudParamType.TaskProgressStatus, emTask
								.getProgressStatus() == null ? null : emTask
								.getProgressStatus().getName());
						map.put(CloudParamType.TaskAttempts,
								new Integer(emTask.getNumberOfAttempts())
										.toString());
						map.put(CloudParamType.TaskId,
								new Long(emTask.getId()).toString());
						map.put(CloudParamType.TaskParameters,
								emTask.getParameters() == null ? null : emTask
										.getParameters());
						mapList.add(map);
					}

					list.add(new NameValue(CloudParamType.EmTasks, JsonUtil
							.getJSONString(mapList)));

					// For tunneling
					ArrayList<HashMap<CloudParamType, String>> tunnelMapList = new ArrayList<HashMap<CloudParamType, String>>();
					HashMap<CloudParamType, String> tunnelMap = new HashMap<CloudParamType, String>();
					tunnelMap.put(CloudParamType.OpenTunnel,
							String.valueOf(emInstance.getOpenTunnelToCloud()));
					tunnelMap.put(CloudParamType.TunnelPort,
							String.valueOf(emInstance.getTunnelPort()));
					tunnelMapList.add(tunnelMap);
					list.add(new NameValue(CloudParamType.TunnelDetails,
							JsonUtil.getJSONString(tunnelMapList)));

					// For SSH tunneling
					ArrayList<HashMap<CloudParamType, String>> sshTunnelMapList = new ArrayList<HashMap<CloudParamType, String>>();
					HashMap<CloudParamType, String> sshTunnelMap = new HashMap<CloudParamType, String>();
					sshTunnelMap.put(CloudParamType.OpenSshTunnel, String
							.valueOf(emInstance.getOpenSshTunnelToCloud()));
					sshTunnelMap.put(CloudParamType.remoteSshTunnelPort,
							String.valueOf(emInstance.getSshTunnelPort()));
					sshTunnelMapList.add(sshTunnelMap);
					list.add(new NameValue(CloudParamType.SshTunnelDetails,
							JsonUtil.getJSONString(sshTunnelMapList)));
				} else {
					cloudresponse.setStatus(1);
					cloudresponse.setMsg("Em Instance Not Registered");
				}
			}

		} catch (Exception e) {
			cloudresponse.setStatus(1);
			cloudresponse.setMsg("Unexpected error on server");
			e.printStackTrace();
		}

		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}
	
	@Path("em/cloudsyncstatus/v3")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String getEmCloudSyncStatus_V3(String request) {
		
		System.out.println("Request:"+request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		EmInstance em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
		
		if(em != null) {
				em.setLastConnectivityAt(new Date());
				if(reqMap.get(CloudParamType.NoOfFloors) != null && !"null".equals(reqMap.get(CloudParamType.NoOfFloors))) {
					em.setNoOfFloors(Integer.parseInt(reqMap.get(CloudParamType.NoOfFloors)));
				}
				if(reqMap.get(CloudParamType.IpAddress) != null) {
					em.setIpAddress(reqMap.get(CloudParamType.IpAddress));
				}
				emInstanceManger.saveOrUpdate(em);
		}
		else {
			EmInstance emInstance = new EmInstance();
			emInstance.setActive(false);
			emInstance.setVersion(cloudrequest.getAppVersion());
			emInstance.setMacId(cloudrequest.getMacId());
			emInstance.setLastConnectivityAt(new Date());
			emInstance.setPauseSyncStatus(false);
			if(reqMap.get(CloudParamType.NoOfFloors) != null && !"null".equals(reqMap.get(CloudParamType.NoOfFloors))) {
				emInstance.setNoOfFloors(Integer.parseInt(reqMap.get(CloudParamType.NoOfFloors)));
			}
			if(reqMap.get(CloudParamType.IpAddress) != null) {
				emInstance.setIpAddress(reqMap.get(CloudParamType.IpAddress));
			}
			emInstanceManger.saveOrUpdate(emInstance);
			em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
		}	
		
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		if("true".equals(reqMap.get(CloudParamType.ShareKey))) {
			list.add(new NameValue(CloudParamType.ApiKey, em.getApiKey()));
			list.add(new NameValue(CloudParamType.SecretKey, em.getSecretKey()));
		}
		
		String syncStatus = "0";
		if(em.getActive()) {
			syncStatus = "1";
			if(em.getSppaEnabled()) {
				syncStatus = "2";
			}
		}
		
		list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
		list.add(new NameValue(CloudParamType.CloudMode, sysConfigManager.loadConfigByName("cloud.mode").getValue()));
		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}

	
	@Path("em/callhome/v3")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String call_home_communicate_v3(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		EmInstance emInstance = null ;
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		
		try {
			System.out.println("Mac Id = " + cloudrequest.getMacId() + 
								"   App Version = " + cloudrequest.getAppVersion() + 
								" Timezone = " + reqMap.get(CloudParamType.EmTimezone));
			
			emInstance = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId());
			if(emInstance != null) {
				updateOldEMInstanceArchAndVersion(emInstance); 
				if(reqMap.get(CloudParamType.Uid) != null && RequestsBlockingPriorityQueue.getCache().getIfPresent(reqMap.get(CloudParamType.Uid)) != null && reqMap.get(CloudParamType.SuccessAck) != null) {
					if(CloudParamType.Success.getName().equals(reqMap.get(CloudParamType.SuccessAck)) && reqMap.get(CloudParamType.PayLoad) != null) {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), reqMap.get(CloudParamType.PayLoad));
					}
					else if (CloudParamType.NotAllowed.getName().equals(reqMap.get(CloudParamType.SuccessAck)) ) {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), CloudParamType.NotAllowed.getName());
					}
					else {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), CloudParamType.Failure.getName());
					}
				}
				
				TimeZone tz = TimeZone.getTimeZone(reqMap.get(CloudParamType.EmTimezone));
				if (tz != null) {
					emInstance.setTimeZone(tz.getID());
				}
				emInstance.setVersion(cloudrequest.getAppVersion());
				if(reqMap.get(CloudParamType.NoOfFloors) != null && !"null".equals(reqMap.get(CloudParamType.NoOfFloors))) {
					emInstance.setNoOfFloors(Integer.parseInt(reqMap.get(CloudParamType.NoOfFloors)));
				}
				if(reqMap.get(CloudParamType.IpAddress) != null) {
					emInstance.setIpAddress(reqMap.get(CloudParamType.IpAddress));
				}
				emInstance.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(emInstance);
				
				EmStats emStats = new EmStats();
				emStats.setEmInstanceId(emInstance.getId());
				emStats.setCaptureAt(new Date(Long.parseLong(reqMap.get(CloudParamType.StatsCaptureAt))));
				emStats.setActiveThreadCount(Integer.parseInt(reqMap.get(CloudParamType.StatsActiveThreadCount)));
				emStats.setGcCount(Long.parseLong(reqMap.get(CloudParamType.StatsGcCount)));
				emStats.setGcTime(Long.parseLong(reqMap.get(CloudParamType.StatsGcTime)));
				emStats.setHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsHeadUsed)));
				emStats.setNonHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsNonHeapUsed)));
				emStats.setSysLoad(Double.parseDouble(reqMap.get(CloudParamType.StatsSysLoad)));
				emStats.setCpuPercentage(Float.parseFloat(reqMap.get(CloudParamType.StatsCpuPercentage)));
				emStats.setIsEmAccessible("TRUE".equals(reqMap.get(CloudParamType.StatsEmAccessible)));
				emStatsManager.saveObject(emStats);
				
				
				if(!RequestsBlockingPriorityQueue.getMap().containsKey(emInstance.getMacId().toUpperCase().replaceAll(":", ""))) {
					RequestsBlockingPriorityQueue queue = new RequestsBlockingPriorityQueue(emInstance.getMacId().toUpperCase().replaceAll(":", ""));
					RequestsBlockingPriorityQueue.getMap().put(queue.getMacId(), queue);
				}
				
				list.addAll(RequestsBlockingPriorityQueue.getMap().get(emInstance.getMacId().toUpperCase().replaceAll(":", "")).getNameValues());
				emInstanceManger.evict(emInstance);
				emInstance = emInstanceManger.getEmInstance(emInstance.getId());
				if(emInstance != null){
					if(emInstance.getSppaEnabled()) {
						list.add(new NameValue(CloudParamType.ReplicaServerIp, replicaServerManager.getReplicaServersbyId(emInstance.getReplicaServer().getId()).getIp()));
					}
					
					String syncStatus = "0";
					if(emInstance.getActive()) {
						syncStatus = "1";
						if(emInstance.getSppaEnabled()) {
							syncStatus = "2";
						}
					}
					
					list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));


					ArrayList<HashMap<CloudParamType, String>> migrationMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> migrationMap = new HashMap<CloudParamType, String>();
					if(emInstance.getLatestEmStateId()!=null)
					{
						EmState emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
						if(emState.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.SYNC_FAILED.getName())) {
							emState = emStateManager.loadBeforeSyncFailedEmStateByEmInstanceId(emInstance.getId());
						}
						migrationMap.put(CloudParamType.CurrentMigrationStatus, emState.getDatabaseState().getName());
						migrationMap.put(CloudParamType.MigrationAttempts, String.valueOf(emState.getFailedAttempts()));
						migrationMapList.add(migrationMap);
						list.add(new NameValue(CloudParamType.MigrationStatusDetails ,JsonUtil.getJSONString(migrationMapList)));
					}
					
					ArrayList<HashMap<CloudParamType, String>> mapList = new ArrayList<HashMap<CloudParamType,String>>();
					List<EmTasks> emTasks = emTasksManager.getActiveEmTasksByEmInstanceId(emInstance.getId());
					for(EmTasks emTask: emTasks) {
						HashMap<CloudParamType, String> map = new HashMap<CloudParamType, String>();
						map.put(CloudParamType.TaskCode, emTask.getTaskCode().getName());
						map.put(CloudParamType.TaskStatus, emTask.getTaskStatus().getName());
						map.put(CloudParamType.TaskProgressStatus, emTask.getProgressStatus() == null ? null : emTask.getProgressStatus().getName());
						map.put(CloudParamType.TaskAttempts, new Integer(emTask.getNumberOfAttempts()).toString());
						map.put(CloudParamType.TaskId, new Long(emTask.getId()).toString());
						map.put(CloudParamType.TaskParameters, emTask.getParameters() == null ? null : emTask.getParameters());
						mapList.add(map);
					}
					
					list.add(new NameValue(CloudParamType.EmTasks, JsonUtil.getJSONString(mapList)));
					
					//For tunneling 
					ArrayList<HashMap<CloudParamType, String>> tunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> tunnelMap = new HashMap<CloudParamType, String>();
					tunnelMap.put(CloudParamType.OpenTunnel, String.valueOf(emInstance.getOpenTunnelToCloud()));
					tunnelMap.put(CloudParamType.TunnelPort, String.valueOf(emInstance.getTunnelPort()));
					tunnelMapList.add(tunnelMap);
					list.add(new NameValue(CloudParamType.TunnelDetails ,JsonUtil.getJSONString(tunnelMapList)));
					

					//For SSH tunneling 
					ArrayList<HashMap<CloudParamType, String>> sshTunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> sshTunnelMap = new HashMap<CloudParamType, String>();
					sshTunnelMap.put(CloudParamType.OpenSshTunnel, String.valueOf(emInstance.getOpenSshTunnelToCloud()));
					sshTunnelMap.put(CloudParamType.remoteSshTunnelPort, String.valueOf(emInstance.getSshTunnelPort()));
					sshTunnelMapList.add(sshTunnelMap);
					list.add(new NameValue(CloudParamType.SshTunnelDetails ,JsonUtil.getJSONString(sshTunnelMapList)));
				}
				else {
					cloudresponse.setStatus(1);
					cloudresponse.setMsg("Em Instance Not Registered");
				}
				}
				
		} catch (Exception e) {
			cloudresponse.setStatus(1);
			cloudresponse.setMsg("Unexpected error on server");
			e.printStackTrace();
		}
		

		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}
	
	@Path("em/cloudsyncstatus/v2")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String getEmCloudSyncStatus(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		String syncStatus = "0";

		if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			EmInstance em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
			
			if(em != null) {
					em.setLastConnectivityAt(new Date());
					emInstanceManger.saveOrUpdate(em);
			}
			else {
				EmInstance emInstance = new EmInstance();
				emInstance.setActive(false);
				emInstance.setVersion(cloudrequest.getAppVersion());
				emInstance.setMacId(cloudrequest.getMacId());
				emInstance.setLastConnectivityAt(new Date());
				emInstance.setPauseSyncStatus(false);
				emInstanceManger.saveOrUpdate(emInstance);
				em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
			}
			
			
			
			if(em.getActive()) {
				syncStatus = "1";
				if(em.getSppaEnabled()) {
					syncStatus = "2";
				}
			}
		}
		list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}
	
	
	
	
	@Path("em/callhome/v2")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String call_home_communicate(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();		
		String syncStatus = "0";
		
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			EmInstance emInstance = null;
			try {
				System.out.println("Mac Id = " + cloudrequest.getMacId() + 
									"   App Version = " + cloudrequest.getAppVersion() + 
									" Timezone = " + reqMap.get(CloudParamType.EmTimezone));
				
				emInstance = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId());
				if(emInstance != null) {
					updateOldEMInstanceArchAndVersion(emInstance);
					TimeZone tz = TimeZone.getTimeZone(reqMap.get(CloudParamType.EmTimezone));
					if (tz != null) {
						emInstance.setTimeZone(tz.getID());
					}
					emInstance.setVersion(cloudrequest.getAppVersion());
					emInstance.setLastConnectivityAt(new Date());
					emInstanceManger.saveOrUpdate(emInstance);
					
					EmStats emStats = new EmStats();
					emStats.setEmInstanceId(emInstance.getId());
					emStats.setCaptureAt(new Date(Long.parseLong(reqMap.get(CloudParamType.StatsCaptureAt))));
					emStats.setActiveThreadCount(Integer.parseInt(reqMap.get(CloudParamType.StatsActiveThreadCount)));
					emStats.setGcCount(Long.parseLong(reqMap.get(CloudParamType.StatsGcCount)));
					emStats.setGcTime(Long.parseLong(reqMap.get(CloudParamType.StatsGcTime)));
					emStats.setHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsHeadUsed)));
					emStats.setNonHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsNonHeapUsed)));
					emStats.setSysLoad(Double.parseDouble(reqMap.get(CloudParamType.StatsSysLoad)));
					emStats.setCpuPercentage(Float.parseFloat(reqMap.get(CloudParamType.StatsCpuPercentage)));
					emStats.setIsEmAccessible("TRUE".equals(reqMap.get(CloudParamType.StatsEmAccessible)));
					emStatsManager.saveObject(emStats);
					if(emInstance.getSppaEnabled()) {
						list.add(new NameValue(CloudParamType.ReplicaServerIp, replicaServerManager.getReplicaServersbyId(emInstance.getReplicaServer().getId()).getIp()));
					}
										
					if(emInstance.getActive()) {
						syncStatus = "1";
						if(emInstance.getSppaEnabled()) {
							syncStatus = "2";
						}
					}
					
					list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));


					ArrayList<HashMap<CloudParamType, String>> migrationMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> migrationMap = new HashMap<CloudParamType, String>();
					if(emInstance.getLatestEmStateId()!=null)
					{
						EmState emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
						if(emState.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.SYNC_FAILED.getName())) {
							emState = emStateManager.loadBeforeSyncFailedEmStateByEmInstanceId(emInstance.getId());
						}
						migrationMap.put(CloudParamType.CurrentMigrationStatus, emState.getDatabaseState().getName());
						migrationMap.put(CloudParamType.MigrationAttempts, String.valueOf(emState.getFailedAttempts()));
						migrationMapList.add(migrationMap);
						list.add(new NameValue(CloudParamType.MigrationStatusDetails ,JsonUtil.getJSONString(migrationMapList)));
					}
					
					ArrayList<HashMap<CloudParamType, String>> mapList = new ArrayList<HashMap<CloudParamType,String>>();
					List<EmTasks> emTasks = emTasksManager.getActiveEmTasksByEmInstanceId(emInstance.getId());
					for(EmTasks emTask: emTasks) {
						HashMap<CloudParamType, String> map = new HashMap<CloudParamType, String>();
						map.put(CloudParamType.TaskCode, emTask.getTaskCode().getName());
						map.put(CloudParamType.TaskStatus, emTask.getTaskStatus().getName());
						map.put(CloudParamType.TaskProgressStatus, emTask.getProgressStatus() == null ? null : emTask.getProgressStatus().getName());
						map.put(CloudParamType.TaskAttempts, new Integer(emTask.getNumberOfAttempts()).toString());
						map.put(CloudParamType.TaskId, new Long(emTask.getId()).toString());
						map.put(CloudParamType.TaskParameters, emTask.getParameters() == null ? null : emTask.getParameters());
						mapList.add(map);
					}
					
					list.add(new NameValue(CloudParamType.EmTasks, JsonUtil.getJSONString(mapList)));
					
					//For tunneling 
					ArrayList<HashMap<CloudParamType, String>> tunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> tunnelMap = new HashMap<CloudParamType, String>();
					tunnelMap.put(CloudParamType.OpenTunnel, String.valueOf(emInstance.getOpenTunnelToCloud()));
					tunnelMap.put(CloudParamType.TunnelPort, String.valueOf(emInstance.getTunnelPort()));
					tunnelMapList.add(tunnelMap);
					list.add(new NameValue(CloudParamType.TunnelDetails ,JsonUtil.getJSONString(tunnelMapList)));
					

					//For SSH tunneling 
					ArrayList<HashMap<CloudParamType, String>> sshTunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> sshTunnelMap = new HashMap<CloudParamType, String>();
					sshTunnelMap.put(CloudParamType.OpenSshTunnel, String.valueOf(emInstance.getOpenSshTunnelToCloud()));
					sshTunnelMap.put(CloudParamType.remoteSshTunnelPort, String.valueOf(emInstance.getSshTunnelPort()));
					sshTunnelMapList.add(sshTunnelMap);
					list.add(new NameValue(CloudParamType.SshTunnelDetails ,JsonUtil.getJSONString(sshTunnelMapList)));
				}
				else {
					cloudresponse.setStatus(1);
					cloudresponse.setMsg("Em Instance Not Registered");
				}
			} catch (Exception e) {
				cloudresponse.setStatus(1);
				cloudresponse.setMsg("Unexpected error on server");
				e.printStackTrace();
			}
		}
		else {
			list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
			cloudresponse.setStatus(1);
			cloudresponse.setMsg("Em Instance Not Registered");
		}
		
		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}
	
	
	
	
	@Path("em/callhome")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String call_home_communicate_2_2(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		
		String syncStatus = "0";
		com.communication.utils.v1.CloudResponse cloudresponse = new com.communication.utils.v1.CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			EmInstance emInstance = null ;
			try {
				System.out.println("Mac Id = " + cloudrequest.getMacId() + 
									"   App Version = " + cloudrequest.getAppVersion() + 
									" Timezone = " + reqMap.get(CloudParamType.EmTimezone));
				
				emInstance = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId());
				if(emInstance != null) {
					updateOldEMInstanceArchAndVersion(emInstance);
					TimeZone tz = TimeZone.getTimeZone(reqMap.get(CloudParamType.EmTimezone));
					if (tz != null) {
						emInstance.setTimeZone(tz.getID());
					}
					emInstance.setVersion(cloudrequest.getAppVersion());
					emInstance.setLastConnectivityAt(new Date());
					emInstanceManger.saveOrUpdate(emInstance);
					
					EmStats emStats = new EmStats();
					emStats.setEmInstanceId(emInstance.getId());
					emStats.setCaptureAt(new Date(Long.parseLong(reqMap.get(CloudParamType.StatsCaptureAt))));
					emStats.setActiveThreadCount(Integer.parseInt(reqMap.get(CloudParamType.StatsActiveThreadCount)));
					emStats.setGcCount(Long.parseLong(reqMap.get(CloudParamType.StatsGcCount)));
					emStats.setGcTime(Long.parseLong(reqMap.get(CloudParamType.StatsGcTime)));
					emStats.setHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsHeadUsed)));
					emStats.setNonHeapUsed(Double.parseDouble(reqMap.get(CloudParamType.StatsNonHeapUsed)));
					emStats.setSysLoad(Double.parseDouble(reqMap.get(CloudParamType.StatsSysLoad)));
					emStats.setCpuPercentage(Float.parseFloat(reqMap.get(CloudParamType.StatsCpuPercentage)));
					emStats.setIsEmAccessible("TRUE".equals(reqMap.get(CloudParamType.StatsEmAccessible)));
					emStatsManager.saveObject(emStats);
					if(emInstance.getSppaEnabled()) {
						list.add(new NameValue(CloudParamType.ReplicaServerIp, replicaServerManager.getReplicaServersbyId(emInstance.getReplicaServer().getId()).getIp()));
					}
					
					if(emInstance.getActive()) {
						syncStatus = "1";
						if(emInstance.getSppaEnabled()) {
							syncStatus = "2";
						}
					}
					
					list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
					
				}
				else {
					cloudresponse.setStatus(1);
					cloudresponse.setMsg("Em Instance Not Registered");
				}
			} catch (Exception e) {
				cloudresponse.setStatus(1);
				cloudresponse.setMsg("Unexpected error on server");
				e.printStackTrace();
			}
		}else{
			list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
			cloudresponse.setStatus(1);
			cloudresponse.setMsg("Em Instance Not Registered");
		}
				
		return JsonUtil.getJSONString(cloudresponse);
	}
	
	@Path("em/cloudsyncstatus")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String getEmCloudSyncStatus_2_2(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		
		EmInstance em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
		
		if(em != null) {
				em.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(em);
		}
		else {
			EmInstance emInstance = new EmInstance();
			emInstance.setActive(false);
			emInstance.setVersion(cloudrequest.getAppVersion());
			emInstance.setMacId(cloudrequest.getMacId());
			emInstance.setLastConnectivityAt(new Date());
			emInstance.setLastSuccessfulSyncTime(new Date());
			emInstance.setPauseSyncStatus(false);
			emInstanceManger.saveOrUpdate(emInstance);
			em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
		}
		
		com.communication.utils.v1.CloudResponse cloudresponse = new com.communication.utils.v1.CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		String syncStatus = "0";
		if(em.getActive()) {
			syncStatus = "1";
			if(em.getSppaEnabled()) {
				syncStatus = "2";
			}
		}
		
		list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
		return JsonUtil.getJSONString(cloudresponse);
	}
	
	
	@Path("em/replicaServerIp/{emMacId}")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	public String replicaServerIp(@PathParam("emMacId") String macId) {

		String replicaServerIp = null;
		EmInstance em = emInstanceManger.loadEmInstanceByMac(macId) ;
		if(em != null && em.getSppaEnabled())
		{
			replicaServerIp = em.getReplicaServer().getIp() ;
		}
		return replicaServerIp ;

	}

	/**
	 * DO NOT CHANGE THIS SERVICE
	 * FOR BACKWARD COMPATIBILITY WITH 2.2.x
	 * @param otherDataStream
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Path("em/stats")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response call_home_communicate( @FormDataParam("data") InputStream otherDataStream) {
		
		boolean fail = false;

		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		ObjectInputStream objectIn = null;
		
		try {
			zipOtherIn.getNextEntry();
			objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn.readObject();
			System.out.println("Mac Id = " + map.get("macId") + "   App Version = " + map.get("version"));
			EmInstance emInstance = emInstanceManger.loadEmInstanceByMac(map.get("macId").toString());
			
			if(emInstance != null && map.containsKey("id") && map.get("id") != null && !"".equals(map.get("id").toString())) {
				emInstance.setVersion(map.get("version").toString());
				emInstance.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(emInstance);
				
				EmStats emStats = new EmStats();
				emStats.setEmInstanceId(emInstance.getId());
				emStats.setCaptureAt((Date)map.get("capture_at"));
				emStats.setActiveThreadCount((Integer) map.get("active_thread_count"));
				emStats.setGcCount((Long)map.get("gc_count"));
				emStats.setGcTime((Long)map.get("gc_time"));
				emStats.setHeapUsed(((BigDecimal)map.get("heap_used")).doubleValue());
				emStats.setNonHeapUsed(((BigDecimal)map.get("non_heap_used")).doubleValue());
				emStats.setSysLoad(((BigDecimal)map.get("sys_load")).doubleValue());
				emStats.setCpuPercentage(((BigDecimal)map.get("cpu_percentage")).floatValue());
				emStats.setIsEmAccessible(((Boolean) (map.get("em_accessible"))).booleanValue());
				emStatsManager.saveObject(emStats);
			}
			else {
				fail = true;
			}
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} catch (Exception e) {
			fail = true;
			e.printStackTrace();
		} finally {
			if(zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if(objectIn != null) {
				try {
					objectIn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if(fail) {
			return Response.status(500).entity("").build();
		}
		else {
			return Response.status(200).entity("").build();
		}
	}
	
	

	/**
	 * DO NOT CHANGE THIS SERVICE
	 * FOR BACKWARD COMPATIBILITY WITH 2.2.x
	 * @param data
	 * @return
	 */
	@Path("em/info")
	@POST
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String sendEmInfo(String data) {

		System.out.println(data);
		
		String[] arr = data.split("#");
		String emInstanceId = "-1";
		EmInstance em = emInstanceManger.loadEmInstanceByMac(arr[0]) ;
		if(em != null) {
			if(em.getActive()) {
				emInstanceId = String.valueOf(em.getId()) ;
			}
			else {
				em.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(em);
			}
		}
		else {
			EmInstance emInstance = new EmInstance();
			emInstance.setActive(false);
			emInstance.setVersion(arr[1]);
			emInstance.setMacId(arr[0]);
			emInstance.setLastConnectivityAt(new Date());
			emInstance.setLastSuccessfulSyncTime(new Date());
			emInstance.setPauseSyncStatus(false);
			emInstanceManger.saveOrUpdate(emInstance);
		}
		return emInstanceId ;

	}

	@Path("toggle/em/browsability/{emMacId}")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response toggleEmBrowsability(@PathParam("emMacId") String macId,String request) {
		try{
			EmInstance em = emInstanceManger.loadEmInstanceByMac(macId);
			if(em!= null){
				em.setBrowseEnabledFromCloud(Boolean.parseBoolean(request));
				emInstanceManger.saveOrUpdate(em);				
			}
			
			return Response.status(200).entity("").build();
		}catch (Exception e){
			e.printStackTrace();
			return Response.status(500).entity(e.getMessage()).build();	
		}
		
	}

	@Path("app/cloudsyncstatus/v1")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String getAppCloudSyncStatus_V1(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		AppInstance app = appInstanceManger.loadAppInstanceByMac(cloudrequest.getMacId()) ;
		
		if(app != null) {
				app.setLastConnectivityAt(new Date());
				if(reqMap.get(CloudParamType.IpAddress) != null) {
					app.setIpAddress(reqMap.get(CloudParamType.IpAddress));
				}
				appInstanceManger.saveOrUpdate(app);
		}
		else {
			AppInstance appInstance = new AppInstance();
			appInstance.setActive(false);
			appInstance.setVersion(cloudrequest.getAppVersion());
			appInstance.setMacId(cloudrequest.getMacId());
			appInstance.setLastConnectivityAt(new Date());
			if(reqMap.get(CloudParamType.IpAddress) != null) {
				appInstance.setIpAddress(reqMap.get(CloudParamType.IpAddress));
			}
			// TODO : hardcoded to aire for now, needs to be replaced with correct application type
			appInstance.setAppType("AIRE");
			appInstanceManger.saveOrUpdate(appInstance);
			app = appInstanceManger.loadAppInstanceByMac(cloudrequest.getMacId()) ;
		}	
		
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		
		String syncStatus = "0";
		if(app.getActive()) {
			syncStatus = "1";
		}
		
		list.add(new NameValue(CloudParamType.EmCloudSyncStatus, syncStatus));
		list.add(new NameValue(CloudParamType.CloudMode, sysConfigManager.loadConfigByName("cloud.mode").getValue()));
		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}

	@Path("app/callhome/v1")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String generic_call_home_communicate_v1(String request) {
		
		System.out.println(request);
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		AppInstance appInstance = null ;
		CloudResponse cloudresponse = new CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		
		try {
			System.out.println("Mac Id = " + cloudrequest.getMacId() + 
								"   App Version = " + cloudrequest.getAppVersion() + 
								" Timezone = " + reqMap.get(CloudParamType.EmTimezone));
			
			appInstance = appInstanceManger.loadAppInstanceByMac(cloudrequest.getMacId());
			if(appInstance != null) {
				
				if(reqMap.get(CloudParamType.Uid) != null && RequestsBlockingPriorityQueue.getCache().getIfPresent(reqMap.get(CloudParamType.Uid)) != null && reqMap.get(CloudParamType.SuccessAck) != null) {
					if(CloudParamType.Success.getName().equals(reqMap.get(CloudParamType.SuccessAck)) && reqMap.get(CloudParamType.PayLoad) != null) {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), reqMap.get(CloudParamType.PayLoad));
					}
					else if (CloudParamType.NotAllowed.getName().equals(reqMap.get(CloudParamType.SuccessAck)) ) {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), CloudParamType.NotAllowed.getName());
					}
					else {
						RequestsBlockingPriorityQueue.getCache().put(reqMap.get(CloudParamType.Uid), CloudParamType.Failure.getName());
					}
				}
				
				TimeZone tz = TimeZone.getTimeZone(reqMap.get(CloudParamType.EmTimezone));
				if (tz != null) {
					appInstance.setTimeZone(tz.getID());
				}
				appInstance.setVersion(cloudrequest.getAppVersion());
				if(reqMap.get(CloudParamType.IpAddress) != null) {
					appInstance.setIpAddress(reqMap.get(CloudParamType.IpAddress));
				}
				appInstance.setLastConnectivityAt(new Date());
				appInstanceManger.saveOrUpdate(appInstance);
				
				
				
				if(!RequestsBlockingPriorityQueue.getMap().containsKey(appInstance.getMacId().toUpperCase().replaceAll(":", ""))) {
					RequestsBlockingPriorityQueue queue = new RequestsBlockingPriorityQueue(appInstance.getMacId().toUpperCase().replaceAll(":", ""));
					RequestsBlockingPriorityQueue.getMap().put(queue.getMacId(), queue);
				}
				
				list.addAll(RequestsBlockingPriorityQueue.getMap().get(appInstance.getMacId().toUpperCase().replaceAll(":", "")).getNameValues());
				appInstanceManger.evict(appInstance);
				appInstance = appInstanceManger.getAppInstance(appInstance.getId());
				if(appInstance != null){
					
					String syncStatus = "0";
					if(appInstance.getActive()) {
						syncStatus = "1";
					}
					
					list.add(new NameValue(CloudParamType.AppCloudSyncStatus, syncStatus));
					
					
					//For tunneling 
					ArrayList<HashMap<CloudParamType, String>> tunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> tunnelMap = new HashMap<CloudParamType, String>();
					tunnelMap.put(CloudParamType.OpenTunnel, String.valueOf(appInstance.getOpenTunnelToCloud()));
					tunnelMap.put(CloudParamType.TunnelPort, String.valueOf(appInstance.getTunnelPort()));
					tunnelMapList.add(tunnelMap);
					list.add(new NameValue(CloudParamType.TunnelDetails ,JsonUtil.getJSONString(tunnelMapList)));
					

					//For SSH tunneling 
					ArrayList<HashMap<CloudParamType, String>> sshTunnelMapList = new ArrayList<HashMap<CloudParamType,String>>();
					HashMap<CloudParamType, String> sshTunnelMap = new HashMap<CloudParamType, String>();
					sshTunnelMap.put(CloudParamType.OpenSshTunnel, String.valueOf(appInstance.getOpenSshTunnelToCloud()));
					sshTunnelMap.put(CloudParamType.remoteSshTunnelPort, String.valueOf(appInstance.getSshTunnelPort()));
					sshTunnelMapList.add(sshTunnelMap);
					list.add(new NameValue(CloudParamType.SshTunnelDetails ,JsonUtil.getJSONString(sshTunnelMapList)));
				}
				else {
					cloudresponse.setStatus(1);
					cloudresponse.setMsg("App Instance Not Registered");
				}
				}
				
		} catch (Exception e) {
			cloudresponse.setStatus(1);
			cloudresponse.setMsg("Unexpected error on server");
			e.printStackTrace();
		}
		

		return JsonUtil.getCloudResponseJSONString(cloudresponse);
	}

	@Path("toggle/app/browsability/{appMacId}")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response toggleAppBrowsability(@PathParam("appMacId") String macId,String request) {
		try{
			AppInstance app = appInstanceManger.loadAppInstanceByMac(macId);
			if(app!= null){
				app.setOpenTunnelToCloud(Boolean.parseBoolean(request));
				appInstanceManger.saveOrUpdate(app);				
			}
			
			return Response.status(200).entity("").build();
		}catch (Exception e){
			e.printStackTrace();
			return Response.status(500).entity(e.getMessage()).build();	
		}
		
	}
	
	/**
     * Service returns the trust store file  
     * @param aid
     * @return
     */
    @Path("getTSFile")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getTSFile(String request) {
    	
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		EmTasks emTask = emTasksManager.getEmTasksById(Long.parseLong(reqMap.get(CloudParamType.TaskId)));
		emTask.setProgressStatus(TaskProgressStatus.TSCertDownloadRequested);
		emTask.setTaskStatus(TaskStatus.IN_PROGRESS);
		emTask.setNumberOfAttempts(emTask.getNumberOfAttempts() + 1);
		
		emTasksManager.saveObject(emTask);
		
		String mFileLocation = certificateManager.getTSLocation();
		File mFile = new File(mFileLocation);
		ResponseBuilder mResponseBuilder = new ResponseBuilderImpl();
		mResponseBuilder.type("application/octet-stream");
		mResponseBuilder.header("Content-Disposition", "attachment; filename="
				+ "enlighted.ts");
		byte[] rb = null;
		try {
			RandomAccessFile rFile = new RandomAccessFile(mFile, "r");
			rb = new byte[(int) rFile.length()];
			rFile.read(rb, 0, (int) rFile.length());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mResponseBuilder.entity(rb);
		return mResponseBuilder.build();    	    	
	}
    
    /**
     * Service returns the keystore file corresponding to specified EM. 
     * @param aid
     * @return
     */
    @Path("getKSFile")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getKSFile(String request) {
    	
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		//get encrypted uuid from cloud request
		String encryptedUUID = reqMap.get(CloudParamType.EmTaskUUID);
		//decrypt the uuid and chk if its same as that of uuid in em_tasks table
		String decryptedUUID = "";
		String originalUUID = "";
		try{
			decryptedUUID = EmTasksUUIDUtil.decrypt(EmTasksUUIDUtil.SECRET_LICENSE_KEY, encryptedUUID);
		}catch(Exception e){
			e.printStackTrace();
		}		
		
		EmTasks emTask = emTasksManager.getEmTasksById(Long.parseLong(reqMap.get(CloudParamType.TaskId)));
		EmTasksUUID uuidObj = emTasksUUIDManager.getEmTasksUUIDById(emTask.getEmTasksUuid());
		if(uuidObj!=null){
			originalUUID = uuidObj.getUuid();
		}
		
		//case : uuid match
		if(originalUUID.equals(decryptedUUID)){
			//set otp/uuid inactive
			uuidObj.setActive(false);
			emTasksUUIDManager.saveOrUpdate(uuidObj);
			
			emTask.setProgressStatus(TaskProgressStatus.KSCertDownloadRequested);
			emTask.setTaskStatus(TaskStatus.IN_PROGRESS);
			emTask.setNumberOfAttempts(emTask.getNumberOfAttempts() + 1);
			
			emTasksManager.saveObject(emTask);
			
			String mFileLocation = certificateManager.getKSLocation(emTask.getEmInstanceId());
			
	    	if(mFileLocation != null) {
	    		
	    		String mFileName = mFileLocation.substring(mFileLocation.lastIndexOf('/')+1);
	    		File mFile = new File(mFileLocation);    	
	    		ResponseBuilder mResponseBuilder = new ResponseBuilderImpl();
	    		mResponseBuilder.type("application/octet-stream");
	    		mResponseBuilder.header("Content-Disposition",  "attachment; filename="+mFileName);    	
	    		byte[] rb = null;
				try {
					RandomAccessFile rFile = new RandomAccessFile(mFile, "r");
					rb = new byte[(int)rFile.length()];
					rFile.read(rb, 0, (int)rFile.length());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mResponseBuilder.entity(rb);
				return mResponseBuilder.build();
	    	}
	    	else {
	    		return null;
	    	} 			
		} else{ //case : uuid not matched
			//set otp/uuid inactive
			uuidObj.setActive(false);
			emTasksUUIDManager.saveOrUpdate(uuidObj);
			
			emTask.setProgressStatus(TaskProgressStatus.KSCertAuthFailure);
			emTask.setTaskStatus(TaskStatus.FAILED);
			emTask.setNumberOfAttempts(emTask.getNumberOfAttempts() + 1);
			
			emTasksManager.saveObject(emTask);
		}
		return null;		  
	}
	

}