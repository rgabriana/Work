package com.emscloud.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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

import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.types.DatabaseState;
import com.communication.utils.CloudRequest;
import com.communication.utils.CloudResponse;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmTasks;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.service.EmTasksManager;
import com.emscloud.service.ReplicaServerManager;
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
	ReplicaServerManager replicaServerManager ;
	@Resource
	CustomerManager customerManager ;
	@Resource
	EmStatsManager emStatsManager;
	@Resource
	EmTasksManager emTasksManager;
	@Resource
	EmStateManager emStateManager;

	@Path("em/cloudsyncstatus/v2")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public String getEmCloudSyncStatus(String request) {
		
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
			emInstanceManger.saveOrUpdate(emInstance);
			em = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId()) ;
		}
		
		CloudResponse cloudresponse = new CloudResponse();
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
		String migrationStatus = null ;
		
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
				//Migration status related
				if(emInstance.getLatestEmStateId()!=null)
				{
					EmState emState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId()) ;
					migrationStatus = emState.getDatabaseState().getName() ;
				}else
				{
					migrationStatus = DatabaseState.NOT_MIGRATED.getName() ;
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
		
		
		EmInstance emInstance = null ;
		com.communication.utils.v1.CloudResponse cloudresponse = new com.communication.utils.v1.CloudResponse();
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudresponse.setNameval(list);
		
		
		try {
			System.out.println("Mac Id = " + cloudrequest.getMacId() + 
								"   App Version = " + cloudrequest.getAppVersion() + 
								" Timezone = " + reqMap.get(CloudParamType.EmTimezone));
			
			emInstance = emInstanceManger.loadEmInstanceByMac(cloudrequest.getMacId());
			if(emInstance != null) {
				
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
				
				String syncStatus = "0";
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
		if(em.getSppaEnabled())
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
			emInstanceManger.saveOrUpdate(emInstance);
		}
		return emInstanceId ;

	}

}
