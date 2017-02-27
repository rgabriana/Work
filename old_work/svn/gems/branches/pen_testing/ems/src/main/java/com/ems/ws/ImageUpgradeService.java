package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.GatewayImpl;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.DeviceType;
import com.ems.utils.CommonUtils;
import com.ems.vo.DeviceFixture;
import com.ems.vo.DeviceGateway;
import com.ems.vo.DevicePlugload;
import com.ems.vo.DeviceStatusList;
import com.ems.vo.DeviceWds;
import com.ems.ws.util.Response;


@Controller
@Path("/org/imageupgrade")
public class ImageUpgradeService {
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name = "gatewayManager")
	private GatewayManager gatewayManager;
	
	@Resource(name = "firmwareUpgradeManager")
	private FirmwareUpgradeManager firmwareUpgradeManager;
	@Resource(name = "firmwareUpgradeScheduleManager")
	private FirmwareUpgradeScheduleManager firmwareUpgradeScheduleManager;

	@Resource
	SwitchManager switchManager;
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	private static final Logger logger = Logger.getLogger("WSLogger");
	
	/**
	 * Return status if image upgrade is running
	 * @return boolean (true/false)
	 */
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("jobstatus")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getImageUpgradeJobCurrentStatus() {
		Response oStatus = new Response();
		if(ImageUpgradeSO.isInProgress())
		{
			oStatus.setStatus(0);
			oStatus.setMsg("Image Running");
		}else
		{
			oStatus.setStatus(-1);
		}
		return oStatus;
	}
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("status/{date}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public DeviceStatusList getAllDeviceStatus(@PathParam("date") String date) {
		DeviceStatusList mDeviceStatusList = new DeviceStatusList();
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("dateFormat", date);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	logger.error("Validation error"+resp.getMsg());
    		return mDeviceStatusList;
    	}
		
		

		List<DeviceFixture> mDeviceFixtureList = new ArrayList<DeviceFixture>();
		List<DeviceGateway> mDeviceGatewayList = new ArrayList<DeviceGateway>();
		List<DeviceWds> mDeviceWdsList = new ArrayList<DeviceWds>();
		List<DevicePlugload> mDevicePlugloadList = new ArrayList<DevicePlugload>();

		Date convertedFromDate = null;
		Date convertedToDate = null;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	    try {
			convertedToDate = dateFormat.parse(date);
			convertedFromDate = new Date(convertedToDate.getTime() - 24 * 3600 * 1000 );	// Get date 24 hours before current date/time
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	   
	    
		// Status of the fixtures
		List<ImageUpgradeDeviceStatus> oList = firmwareUpgradeManager
				.loadDeviceStatus(convertedFromDate,convertedToDate);		
		Iterator<ImageUpgradeDeviceStatus> oItr = oList.iterator();
		while (oItr.hasNext()) {
			ImageUpgradeDeviceStatus oDeviceStatus = oItr.next();
			// System.out.println(oDeviceStatus.getDeviceId() + ": " +
			// oDeviceStatus.getDevice_type());			
				if (DeviceType.Fixture.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DeviceFixture mDeviceFixture = new DeviceFixture();
					mDeviceFixture.setFixtureId(oDeviceStatus.getDeviceId());
					mDeviceFixture.setFixtureName("");
					mDeviceFixture.setStatus(oDeviceStatus.getStatus());
					mDeviceFixture.setVersion(oDeviceStatus.getNew_version());
					mDeviceFixtureList.add(mDeviceFixture);		
				}
				
				if (DeviceType.Gateway.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DeviceGateway mDeviceGateway = new DeviceGateway();					
					mDeviceGateway.setGatewayId(oDeviceStatus.getDeviceId());					
					mDeviceGateway.setGatewayName("");
					mDeviceGateway.setStatus(oDeviceStatus.getStatus());
					mDeviceGateway.setVersion(oDeviceStatus.getNew_version());
					mDeviceGatewayList.add(mDeviceGateway);			
				}
				if (DeviceType.WDS.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DeviceWds mDeviceWds = new DeviceWds();									
					mDeviceWds.setWdsId(oDeviceStatus.getDeviceId());					
					mDeviceWds.setWdsName("");
					mDeviceWds.setStatus(oDeviceStatus.getStatus());					
					mDeviceWds.setVersion(oDeviceStatus.getNew_version());
					mDeviceWdsList.add(mDeviceWds);			
				}
				if (DeviceType.Plugload.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DevicePlugload mDevicePlugload = new DevicePlugload();
					mDevicePlugload.setPlugloadId(oDeviceStatus.getDeviceId());
					mDevicePlugload.setPlugloadName("");
					mDevicePlugload.setStatus(oDeviceStatus.getStatus());
					mDevicePlugload.setVersion(oDeviceStatus.getNew_version());
					mDevicePlugloadList.add(mDevicePlugload);		
				}
			 
			 //Take the scheduled list from the thread map
			 //Take the in progress states from the fixture table
			 //Take the success/fails from the database 
			 
		}
		mDeviceStatusList.setDeviceFixtureList(mDeviceFixtureList);
		mDeviceStatusList.setDeviceGatewayList(mDeviceGatewayList);
		mDeviceStatusList.setDeviceWdsList(mDeviceWdsList);
		mDeviceStatusList.setDevicePlugloadList(mDevicePlugloadList);
		
		return mDeviceStatusList;

	}
	
	/**
	 * Allows selected set of fixture to be scheduled for image upgrade
	 * 
	 * @param fileName	 
	 * @param fixtures
	 *            list of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return response
	 */
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("upgradeuncommissionedfixtures/{fileName}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response upgradeUnCommissionedFixtures(@PathParam("fileName") String fileName, List<Fixture> fixtures) {
		
		System.out.println("file nmae -- " + fileName);
		Long[] fixtureList = new Long[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		long gwId = -1;
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			System.out.println("id -- " + fixture.getId());			
			if(gwId == -1) {
				fixture = fixtureManager.getFixtureById(fixture.getId());
				gwId = fixture.getSecGwId();
			}
			fixtureList[count++] = fixture.getId();		 
		}
		
		//move the gateway to wireless defaults	
		Gateway gw = gatewayManager.loadGateway(gwId);
		GatewayImpl.getInstance().setWirelessFactoryDefaults(gw);		
		//wait for some time
		ServerUtil.sleep(2);
		ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
		ImageUpgradeDBJob job = new ImageUpgradeDBJob();
		job.setDeviceIds(fixtureList);
		job.setDeviceType("fixture");
		job.setImageName(fileName);
		job.setNoOfRetries(ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES);
		jobList.add(job);
		ImageUpgradeSO.getInstance().startDeviceImageUpgrade(jobList);
		
		return new Response();
		
	} //end of method upgradeUnCommissionedFixtures
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("addFirmwareImage/{fileName}/{deviceType}/{modelNo}/{version}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response addFirmwareImage(@PathParam("fileName") String fileName, @PathParam("deviceType") String deviceType, 
			@PathParam("modelNo") String modelNo, @PathParam("version") String version) {
		
		Response resp = new Response();
        /*Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("deviceType", deviceType);
        nameValMap.put("modelNo", modelNo);
        nameValMap.put("version", version);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/
		
	//	System.out.println("inside web service of addFirmwareUpgrade");
	
		
		if(fileName == null || fileName.isEmpty()) {
			resp.setStatus(1);
			resp.setMsg("Firmware file cannot be empty");
			return resp;
		}	
		
		firmwareUpgradeScheduleManager.addFirmwareImage(fileName, deviceType, modelNo, version, "");
		resp.setStatus(0);
		return resp;
		
	} //end of method addFirmwareImage
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("deactivateFirmwareImage/{fileName}/{deviceType}/{modelNo}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deactivateFirmwareImage(@PathParam("fileName") String fileName, @PathParam("deviceType") String deviceType, 
			@PathParam("modelNo") String modelNo) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
       /* nameValMap.put("deviceType", deviceType);
        nameValMap.put("modelNo", modelNo);
      
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/
	//	System.out.println("inside web service of deactivateFirmwareImage");
		
		
		if(fileName == null || fileName.isEmpty()) {
			resp.setStatus(1);
			resp.setMsg("Firmware file name is missing");
			return resp;
		}	
		
		firmwareUpgradeScheduleManager.deactivateFirmwareImage(fileName, deviceType, modelNo);
		resp.setStatus(0);
		return resp;
		
	} //end of method deactivateFirmwareImage
	
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("activateFirmwareImage/{fileName}/{deviceType}/{modelNo}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response activateFirmwareImage(@PathParam("fileName") String fileName, @PathParam("deviceType") String deviceType, 
			@PathParam("modelNo") String modelNo) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("deviceType", deviceType);
        nameValMap.put("modelNo", modelNo);
       
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	logger.error("Validation error "+resp.getMsg());
    		return resp;
    	}
		
		
		if(fileName == null || fileName.isEmpty()) {
			resp.setStatus(1);
			resp.setMsg("Firmware file name is missing");
			return resp;
		}	
		
		firmwareUpgradeScheduleManager.activateFirmwareImage(fileName, deviceType, modelNo);
		resp.setStatus(0);
		return resp;
		
	} //end of method activateFirmwareImage
	
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("checkDuplicateActiveFirmwareScheduleByModelNo/{deviceType}/{modelNo}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateActiveFirmwareScheduleByModelNo(@PathParam("deviceType") String deviceType, 
			@PathParam("modelNo") String modelNo) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("deviceType", deviceType);
        nameValMap.put("modelNo", modelNo);
       
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	logger.error("Validation error "+resp.getMsg());
    		return resp;
    	}
		FirmwareUpgradeSchedule  firmwareUpgradeSchedule = firmwareUpgradeScheduleManager.getActiveFirmwareScheduleByModelNo(deviceType, modelNo);
		if(firmwareUpgradeSchedule == null){
			resp.setStatus(0);
		}else{
			resp.setStatus(1);
		}
		return resp;
	}

	@PreAuthorize("hasAnyRole('Admin')")
	@Path("deleteFirmwareImage/{id}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteFirmwareImage(@PathParam("id") Long id) {
		Response resp = new Response();
       /* Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("id", id);       
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/	
		//System.out.println("inside web service of deleteFirmwareImage");
		
		firmwareUpgradeScheduleManager.deleteFirmwareSchedule(id);
		resp.setStatus(1);
		return resp;
		
	} //end of method deleteFirmwareImage
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("cancelFirmwareUpgrade/{jobId}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelFirmwareUpgrade(@PathParam("jobId") Long id) {
		Response resp = new Response();
     /*   Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("id", id);       
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}
		System.out.println("inside web service of cancelFirmwareUpgrade");
		*/
		
		firmwareUpgradeManager.abortJob(id);
		resp.setStatus(1);
		return resp;
		
	} //end of method cancelFirmwareUpgrade
	
	@PreAuthorize("hasAnyRole('Admin')")
	@Path("modifyFirmwareUpgradeSchedule/{startTime}/{runtoComplete}/{duration}/{runDaily}/{reboot}/{retries}/{retryInterval}/{deviceType}")
	@POST  
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response modifyFirmwareUpgradeSchedule( @PathParam("startTime") String startTimeStr, @PathParam("duration") int duration, 
			@PathParam("runDaily") boolean runDaily, @PathParam("reboot") boolean reboot, @PathParam("retries") int retries, 
			@PathParam("retryInterval") int retryInt,@PathParam("deviceType") String deviceType, @RequestParam("modelImageMapString") String modelImageMapString, 
			@QueryParam("includeList") String includeList, @QueryParam("excludeList") String excludeList, 
			@QueryParam("jobName") String jobName) {

		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("duration", duration);
        nameValMap.put("runDaily", runDaily);
        nameValMap.put("reboot", reboot);
        nameValMap.put("retries", retries);
        nameValMap.put("deviceType", deviceType);        
        nameValMap.put("retryInterval", retryInt);               
        nameValMap.put("jobName", jobName);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	logger.error("Validation error "+resp.getMsg());
    		return resp;
    	}
           
		HashMap<String, String> modelImageMap = new HashMap<String, String>();

		JSONObject modelImageMapStringObj = null;
		
		try {
			modelImageMapStringObj = new JSONObject(modelImageMapString);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSONArray modelImageMapData = null;
		
		try {
			modelImageMapData = modelImageMapStringObj.getJSONArray("modelimagemapkeyvalues");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
           
		int n = modelImageMapData.length();
		for (int i = 0; i < n; ++i) {
			JSONObject modelImageMapKeyVal = null;
			try {
				modelImageMapKeyVal = modelImageMapData.getJSONObject(i);
				modelImageMap.put(modelImageMapKeyVal.getString("modelname"),modelImageMapKeyVal.getString("imagefile"));
				System.out.println(modelImageMapKeyVal.getString("modelname")+":"+modelImageMapKeyVal.getString("imagefile"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		if(modelImageMap == null || modelImageMap.size() == 0) {
			resp.setStatus(1);
			resp.setMsg("Firmware file cannot be empty");
			return resp;
		}
		
		HashMap<String, String> selDeviceMap = new HashMap<String, String>();
		
		if(!(includeList == null && excludeList == null)){
			
			JSONObject selDeviceMapStringObj = null;
			
			try {
				if(includeList != null && excludeList == null){
					selDeviceMapStringObj = new JSONObject(includeList);
				}else if(includeList == null && excludeList != null){
					selDeviceMapStringObj = new JSONObject(excludeList);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JSONArray selDeviceMapData = null;
			
			try {
				selDeviceMapData = selDeviceMapStringObj.getJSONArray("seldevicemapkeyvalues");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	           
			for (int i = 0; i < selDeviceMapData.length(); ++i) {
				JSONObject selDeviceMapKeyVal = null;
				try {
					selDeviceMapKeyVal = selDeviceMapData.getJSONObject(i);
					selDeviceMap.put(selDeviceMapKeyVal.getString("modelNo"),selDeviceMapKeyVal.getString("deviceIds"));
					System.out.println(selDeviceMapKeyVal.getString("modelNo")+":"+selDeviceMapKeyVal.getString("deviceIds"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
				                       
		System.out.println("start time -- " + startTimeStr);
		System.out.println("duration -- " + duration);
		Date startTime = null;
		boolean scheduleNow = false;
		ArrayList<ImageUpgradeDBJob> jobList = null;
		try {                                    
			if("Now".equals(startTimeStr)){
				startTime = new Date();                  
				scheduleNow = true;				
			} else {
				DateFormat df = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss");       
				startTime = df.parse(startTimeStr);                           
			}                    
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		if(!runDaily) {
			scheduleNow = true;
		}
		
		if(scheduleNow) {
			//submit the jobs to run immediately also
			jobList = new ArrayList<ImageUpgradeDBJob>();
		}
		
		String includeListString = null;
		
		String excludeListString = null;
    
		Iterator<String> iter = modelImageMap.keySet().iterator();                 
		while(iter.hasNext()) {
			
			includeListString = null;
			
			excludeListString = null;
			
			String model = iter.next();
			String imgFile = modelImageMap.get(model);
			System.out.println("model == " + model);
			System.out.println("image -- " + imgFile);
			
			if(includeList == null && excludeList == null){
				includeListString = null;
				excludeListString = null;
			}else if(includeList != null && excludeList == null){
				if("".equals(selDeviceMap.get(model))){
					includeListString = "0";
				}else{
					includeListString = selDeviceMap.get(model);
				}
			}else if(includeList == null && excludeList != null){
				if("".equals(selDeviceMap.get(model))){
					excludeListString = "0";
				}else{
					excludeListString = selDeviceMap.get(model);
				}
			}
			
			if(runDaily) {
				//maintenance window is configured. so, update the schedule in the database
				FirmwareUpgradeSchedule schedule = firmwareUpgradeScheduleManager.getFirmwareUpgradeSchedule(imgFile, model);                             
				schedule.setActive(true);                       
				schedule.setStartTime(startTime);
				schedule.setScheduledTime(startTime);                         
				schedule.setDuration(duration); //should we allow to run to completion during daily schedule
				schedule.setExcludeList(excludeListString);
				schedule.setIncludeList(includeListString);
				schedule.setRetries(retries);
				schedule.setOnReboot(reboot);
				schedule.setRetryInterval(retryInt);                   
				schedule.setJobPrefix(jobName);
				firmwareUpgradeScheduleManager.modifyFirmwareUpgradeSchedule(schedule);
			}
			if(scheduleNow) {                        
				// Creates a Fixture upgrade job
				ImageUpgradeDBJob firmJob = new ImageUpgradeDBJob();
				if(model.contains("GW")) {
					firmJob.setDeviceType(DeviceType.Gateway.getName());
				} else if(model.contains("WS")) {
					firmJob.setDeviceType(DeviceType.WDS.getName());
				} else {
					firmJob.setDeviceType(DeviceType.Fixture.getName());
				}
				firmJob.setExcludeList(excludeListString);
				firmJob.setIncludeList(includeListString);
				firmJob.setImageName(imgFile);
				firmJob.setNoOfRetries(retries);
				firmJob.setRetryInterval(retryInt);
				firmJob.setJobName(jobName);
				firmJob.setScheduledTime(startTime);
				if(duration > 0) {
					Calendar stopTimeCal = Calendar.getInstance();
					stopTimeCal.setTime(startTime);
					stopTimeCal.add(Calendar.MINUTE, duration);
					firmJob.setStopTime(stopTimeCal.getTime());
				} else {
					firmJob.setStopTime(null);
				}
				jobList.add(firmJob);
			}                    
		}
		if(scheduleNow) {
			firmwareUpgradeManager.scheduleFirmwareUpgradeJob(jobList, startTime);			
		}
		resp.setStatus(0);
		return resp;
		
	} //end of method modifyFirmwareUpgradeSchedule

	@PreAuthorize("hasAnyRole('Admin')")
	@Path("scheduleFirmwareUpgradeJob/{fileName}/{retries}/{retryInterval}/{schedTime}")
	@POST	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response scheduleFirmwareUpgradeJob(@PathParam("fileName") String fileName, 
			@PathParam("retries") int retries, @PathParam("retryInterval") int retryInt,
			@PathParam("schedTime") String schedTimeStr, @QueryParam("excludeList") String excludeList, 
			@QueryParam("includeList") String includeList, @QueryParam("jobName") String jobName) {
		
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();        
        nameValMap.put("retries", retries);    
        nameValMap.put("retryInterval", retryInt);               
        nameValMap.put("jobName", jobName);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	logger.error("Validation error "+resp.getMsg());
    		return resp;
    	}
		
		if(fileName == null || fileName.isEmpty()) {
			resp.setStatus(1);
			resp.setMsg("Firmware file cannot be empty");
			return resp;
		}
				
		// Creates a Fixture upgrade job
		ImageUpgradeDBJob fixtureJob = new ImageUpgradeDBJob();
		fixtureJob.setDeviceType(DeviceType.Fixture.getName());		
		fixtureJob.setExcludeList(excludeList);
		fixtureJob.setIncludeList(includeList);
		fixtureJob.setImageName(fileName);
		fixtureJob.setNoOfRetries(retries);
		fixtureJob.setRetryInterval(retryInt);
		fixtureJob.setJobName(jobName);
		Date schedTime = null;
		if(schedTimeStr != null && !schedTimeStr.isEmpty()) {
			try {					
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				if("Now".equals(schedTimeStr)){
					schedTime = new Date();
					fixtureJob.setScheduledTime(schedTime);
				}else{
					schedTime = df.parse(schedTimeStr);
					fixtureJob.setScheduledTime(schedTime);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
		jobList.add(fixtureJob);
		firmwareUpgradeManager.scheduleFirmwareUpgradeJob(jobList, schedTime);
		resp.setStatus(0);
		return resp;
		
	} //end of method scheduleFirmwareUpgradeJob
	 
} //end of class ImageUpgradeService
