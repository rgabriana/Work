/**
 * 
 */
package com.ems.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.ProfileTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.types.UserAuditActionType;
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

	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name = "groupManager")
  private GroupManager groupManager;

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
	@Path("op/assignProfile/{fixtureId}/{groupId}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response assignProfile(@PathParam("fixtureId") long fixtureId, @PathParam("groupId") long groupId) {
			
		fixtureManager.assignProfileToFixture(fixtureManager.getFixtureById(fixtureId), groupId);
		userAuditLoggerUtil.log("assign profile to fixture " + fixtureManager.getFixtureById(fixtureId).getFixtureName(),
				UserAuditActionType.Fixture_Profile_Update.getName());
		fixtureManager.pushProfileToFixtureNow(fixtureId);
		return new Response();
		
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
	@Path("op/dim/{mode}/{percentage}/{fixtureId}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixtures(@PathParam("mode") String mode, @PathParam("percentage") Integer percentage,
			@QueryParam("time") Integer time, @PathParam("fixtureId") Long fixtureId) {
		
		m_Logger.debug("Percentage: " + percentage + ", Time: " + time
				+ ", Fixtures: " + 1);
		
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
	
	@Path("op/dim/{mode}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixtures(@PathParam("mode") String mode, @PathParam("percentage") Integer percentage,
			@QueryParam("time") Integer time, List<Fixture> fixtures) {
		
		m_Logger.debug("Percentage: " + percentage + ", Time: " + time
				+ ", Fixtures: " + fixtures.size());
		StringBuffer fixtString = new StringBuffer("");
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
	@Path("op/mode/{modetype}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyModeToFixtures(@PathParam("modetype") String modetype, List<Fixture> fixtures) {
		
		m_Logger.debug("Fixtures: " + fixtures.size());
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
	
	@Path("printCache")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response printCache() {
		
		HashMap<Long, DeviceInfo> cacheMap = FixtureCache.getInstance().getDeviceMap();
		Iterator<Long> cacheIter = cacheMap.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while(cacheIter.hasNext()) {
			sb.append(cacheMap.get(cacheIter.next()).dumpDeviceInfo());
		}
		m_Logger.debug("cache -- " + sb.toString());
		return new Response();
	} //end of method printCache
	
	/**
	 * Returns all applicable profiles to a fixture
	 * 
	 * @param fid
	 *            fixture unique identifier
	 * @return fixture details
	 */
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
	
} //end of class FixtureServiceV1