/**
 * 
 */
package com.ems.ws;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.ProfileTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.EMManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.vo.FixturePower;
import com.ems.vo.FixtureProfiles;
import com.ems.vo.Profile;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/fixture/v1")
public class FixtureServiceV1 {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name = "groupManager")
	private GroupManager groupManager;
	@Resource(name = "emManager")
	EMManager emManager;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public FixtureServiceV1() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Assign profile to the selected fixture
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @param groupId
	 *            Profile (Group) unique identifier
	 * 
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("op/assignProfile/{fixtureId}/{groupId}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response assignProfile(@PathParam("fixtureId") long fixtureId, @PathParam("groupId") long groupId) {
		Response res = new Response();
		Fixture fx = fixtureManager.getFixtureById(fixtureId);
		Groups grp = groupManager.getGroupById(groupId);
		if(fx!=null && grp!=null)
		{
			fixtureManager.assignProfileToFixture(fixtureId, groupId);
			userAuditLoggerUtil.log("assign profile to fixture " + fixtureManager.getFixtureById(fixtureId).getFixtureName(),
					UserAuditActionType.Fixture_Profile_Update.getName());
			fixtureManager.pushProfileToFixtureNow(fixtureId);
		}else
		{
			res.setStatus(-1);
			res.setMsg("Either Fixture or Group does not exists. Please provide valid inputs");
		}
		return res;
	} //end of method assignProfile

	/**
	 * Allows selected set of fixture to be dimmed or brighted from the
	 * floorplan
	 * 
	 * @param mode
	 *            (rel | abs)
	 * @param percentage
	 *            {(-100 | 0 | 100) for rel} AND {(0 | 100) for abs}
	 * @param time
	 *            minutes
	 * @param fixtures
	 *            list of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return response
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
	@Path("op/dim/{mode}/{percentage}/{fixtureId}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixtures(@PathParam("mode") String mode, @PathParam("percentage") Integer percentage,
			@QueryParam("time") Integer time, @PathParam("fixtureId") Long fixtureId) {
		
		m_Logger.debug("Percentage: " + percentage + ", Time: " + time
				+ ", Fixtures: " + 1);
		Response oResponse = new Response();
        /*Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("mode", mode);
        nameValMap.put("time", time);
        nameValMap.put("intPercentage", percentage);
		oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oResponse!= null && oResponse.getStatus()!=200){
        	m_Logger.error("Validation error"+oResponse.getMsg());
    		return oResponse;
    	}*/
		if(time == null) {
			time = 60;
		}
		int[] fixtureList = new int[1];
		fixtureList[0] = fixtureId.intValue();
		if (mode.equalsIgnoreCase("REL")) {
			fixtureManager.dimFixtures(fixtureList, percentage, time);
		} else if (mode.equalsIgnoreCase("ABS")) {
			fixtureManager.absoluteDimFixtures(fixtureList, percentage, time);
		}
		userAuditLoggerUtil.log("Dimming fixtures " + fixtureId + " to "
			+ percentage + "% in " + mode + " mode for " + time
			+ " minutes", UserAuditActionType.Fixture_Dimming.getName());
		return new Response();
		
	} //end of method dimFixtures
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Bacnet')")
	@Path("op/dim/{mode}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixtures(@PathParam("mode") String mode, @PathParam("percentage") Integer percentage,
			@QueryParam("time") Integer time, List<Fixture> fixtures) {
		
		m_Logger.debug("Percentage: " + percentage + ", Time: " + time
				+ ", Fixtures: " + fixtures.size());
		StringBuffer fixtString = new StringBuffer("");
		Response oResponse = new Response();
       /* Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("mode", mode);
        nameValMap.put("time", time);
        nameValMap.put("intPercentage", percentage);
		oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oResponse!= null && oResponse.getStatus()!=200){
    		return oResponse;
    	}*/
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
			
			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
	        //to find the name should not stop decommission.
			try {
			    if(FixtureCache.getInstance().getDevice(fixture.getId()) != null){
				fixtString.append(FixtureCache.getInstance()
						.getDevice(fixture.getId()).getFixtureName() + ",");
			    }else{
			        if(fixture.getId() != null){
			            fixtString.append(fixture.getId() + ","); 
			        }
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		if(time == null) {
			time = 60;
		}
		if (mode.equalsIgnoreCase("REL")) {
			fixtureManager.dimFixtures(fixtureList, percentage, time);
		} else if (mode.equalsIgnoreCase("ABS")) {
			fixtureManager.absoluteDimFixtures(fixtureList, percentage, time);
		}
		userAuditLoggerUtil.log("Dimming fixtures " + fixtString + " to "
			+ percentage + "% in " + mode + " mode for " + time
			+ " minutes", UserAuditActionType.Fixture_Dimming.getName());
		return new Response();
		
	} //end of method dimFixtures
	
	/**
	 * Place the selected fixtures in auto mode
	 *
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Bacnet')")
	@Path("op/auto")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyAuto(List<Fixture> fixtures) {
		
		return applyModeToFixtures("AUTO", fixtures);
				
	} //end of method applyAuto
	
	/**
	 * Place the selected fixtures in specified mode.
	 * 
	 * @param modetype
	 *            String {AUTO|BASELINE|BYPASS}
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
	@Path("op/mode/{modetype}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyModeToFixtures(@PathParam("modetype") String modetype, List<Fixture> fixtures) {
		
		m_Logger.debug("Fixtures: " + fixtures.size());
		Response oResponse = new Response();
       /* oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "modetype", modetype);
        if(oResponse!= null && oResponse.getStatus()!=200){
    		return oResponse;
    	}*/
		StringBuilder fixtString = new StringBuilder("");
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Response oStatus = new Response();
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
			
			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
	        //to find the name should not stop decommission.
			try {
				if(FixtureCache.getInstance().getDevice(fixture.getId()) != null){
					fixtString.append(FixtureCache.getInstance().getDevice(fixture.getId()).getFixtureName() + ",");
				} else{
					if(fixture.getId() != null){
						fixtString.append(fixture.getId() + ","); 
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		if (modetype.equalsIgnoreCase("AUTO")) {
			fixtureManager.auto(fixtureList);
		} else if (modetype.equalsIgnoreCase("BASELINE")) {
			fixtureManager.baseline(fixtureList);
		} else {
			oStatus.setStatus(1);
			oStatus.setMsg("Undefined mode type");
		}
		userAuditLoggerUtil.log("Setting Mode of  fixtures " + fixtString + " at  " + modetype,
				UserAuditActionType.Fixture_Mode_Change.getName());
		return oStatus;
		
	} //end of method applyModeToFixtures
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("printCache")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response printCache() {
		Response response = new Response();
		HashMap<Long, DeviceInfo> cacheMap = FixtureCache.getInstance().getDeviceMap();
		Iterator<Long> cacheIter = cacheMap.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while(cacheIter.hasNext()) {
			sb.append(cacheMap.get(cacheIter.next()).dumpDeviceInfo());
			sb.append(" -- ");
		}
		m_Logger.debug("cache -- " + sb.toString());
		response.setStatus(1);
		response.setMsg(sb.toString());
		return response;
	} //end of method printCache
	
	/**
	 * Returns all applicable profiles to a fixture
	 * 
	 * @param fid
	 *            fixture unique identifier
	 * @return fixture details
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("getFixtureApplicableProfiles/{fid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FixtureProfiles getFixtureApplicableProfiles(@PathParam("fid") Long fId) {
		
		Fixture fixture = null;
//		if(FixtureCache.getInstance().getDevice(fId) != null){
//			fixture = FixtureCache.getInstance().getDevice(fId).getFixture();
//		}
		if(fixture == null) {
			fixture = fixtureManager.getFixtureById(fId);
		}
		Long groupId = fixture.getGroupId();
		//get the profile template of the fixture group
		ProfileTemplate prTemp = groupManager.getGroupById(groupId).getProfileTemplate();		
		//get all the groups associated with the profile template
  	List<Groups> derivedGroups = groupManager.loadAllProfileTemplateById(prTemp.getId(), 0L); //.getAllDerivedGroups(parentGrpId);
  	ArrayList<Profile> profilesList = new ArrayList<Profile>();
  	Iterator<Groups> iter = derivedGroups.iterator();
  	while(iter.hasNext()) {
  		Groups group = iter.next();
  		Profile profile = new Profile();
  		profile.setName(group.getName());
  		profile.setGroupId(group.getId());
  		profilesList.add(profile);
  	}
  	
  	FixtureProfiles fProfiles = new FixtureProfiles();
  	fProfiles.setId(fId);
  	fProfiles.setName(fixture.getName());
  	fProfiles.setCurrentProfile(fixture.getCurrentProfile());
  	fProfiles.setGroupId(groupId);
  	fProfiles.setApplicableProfiles(profilesList);
  	return fProfiles;
		
	} //end of method getFixtureApplicableProfiles
	
	/**
	 * Returns fixture location list TODO: limit is currently not used, need to
	 * fix this.
	 * 
	 * @param property
	 *            this api is customized to get fixture location details by area	 *            
	 * @param pid
	 *            property unique identifier i.e areaId in this case. For unassigned areas, value 0 must be passed.
	 * @Queryparam floorId is an optional query parameter
	 * @return fixture list for the property level
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
	@Path("location/list/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureLocationListByArea(
			@PathParam("property") String property, @PathParam("pid") Long pid,@QueryParam("floorId") Long fid,
			@QueryParam("limit") String limit) {

		Response resp = new Response();
		 Map<String,Object> nameValMap = new HashMap<String, Object>();        
	        nameValMap.put("id", pid);
	        nameValMap.put("property", property);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
		if(resp!= null && resp.getStatus()!=200){			
			m_Logger.error("Validation error"+resp.getMsg());
			return new ArrayList<Fixture>();
		}
		List results = null;
		if("area".equalsIgnoreCase(property)){
			results = fixtureManager.loadFixtureLocationsByAreaId(pid,fid);
		}
		ArrayList<Fixture> fixtLocationList = new ArrayList<Fixture>();
		if (results != null && !results.isEmpty()) {
			Iterator fixtLocationIt = results.iterator();
			while (fixtLocationIt.hasNext()) {			 

				Object[] dataArr = (Object[]) fixtLocationIt.next();
				Fixture fxLocation = new Fixture();
				fxLocation.setId((Long) dataArr[0]);
				fxLocation.setFixtureName(dataArr[1].toString());
				fxLocation.setXaxis((Integer) dataArr[2]);
				fxLocation.setYaxis((Integer) dataArr[3]);
				fxLocation.setGroupId((Long) dataArr[4]);
				fxLocation.setMacAddress(dataArr[5].toString());
				if("area".equalsIgnoreCase(property) && dataArr[6] == null){
					fxLocation.setAreaId(0L);					
				}else{
					fxLocation.setAreaId((Long) dataArr[6]);
				}
								
				fixtLocationList.add(fxLocation);
			}
		}
		return fixtLocationList;

	}
	/** The API returns the current energy consumed by the fixture
	 * @param fixture_id
	 * 		   unique identifier of the fixture 
	 * @return FixtureEnergy - The response is a JSON element with the energy parameter
	 * 		energy : current level of energy consumed by the lighting fixture (in Wh). 
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("energy/{fixture_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FixturePower getFixtureEnergyConsumption( @PathParam("fixture_id") Long fixture_id) {
		String property = "id";
		FixturePower res = emManager.getFixtureEnergyConsumption(fixture_id,property);
		return res;
		
	} //end of method getFixtureEnergyConsumption	
	
	
	/** The API returns the current energy consumed by the fixture.
	 * 
	 * @param	area_id 
	 * 			unique identifier of the area
	 * @return FixtureEnergy - The response is a JSON element with the energy parameter
	 * 		energy : current level of energy consumed by the lighting fixture (in Wh). 
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("energy/area/{area_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FixturePower getFixtureEnergyConsumptionByArea(@PathParam("area_id") Long area_id) {
		String property = "area_id";
		FixturePower res = emManager.getFixtureEnergyConsumption(area_id,property);
		return res;
		
	} //end of method getFixtureEnergyConsumptionByArea	
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("setOccChangeTrigger/{triggerDelayTime}/{ack}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response setPeriodicHB(List<Fixture> fixtures, @PathParam("triggerDelayTime") Integer triggerDelayTime, 
			@PathParam("ack") Short ack) {
		Response resp = new Response();
		
		if(fixtures != null && fixtures.size() > 0) {
			for(Fixture f: fixtures) {
				Fixture fix =  fixtureManager.getFixtureByMacAddr(f.getMacAddress());
				fix.setOccLevelTriggerTime(triggerDelayTime);
				fix.enableZoneOccupancyTriggerType();
				fixtureManager.save(fix);
				DeviceServiceImpl.getInstance().setTriggerType(fix, ServerConstants.SU_CMD_HB_CONFIG_MSG_TYPE, ack);
			}
		}
		return resp;
	}
} //end of class FixtureServiceV1
