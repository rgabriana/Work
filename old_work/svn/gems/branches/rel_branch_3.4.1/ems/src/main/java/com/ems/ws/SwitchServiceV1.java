/**
 * 
 */
package com.ems.ws;

import java.io.IOException;
import java.sql.SQLException;
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

import com.ems.model.Floor;
import com.ems.model.GemsGroup;
import com.ems.model.Scene;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.FloorManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.SwitchManager;
import com.ems.types.UserAuditActionType;
import com.ems.vo.SwitchScenes;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/switch/v1")
public class SwitchServiceV1 extends SwitchService{
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
  FloorManager floorManager;
	@Resource
  GemsGroupManager gemsGroupManager;
	@Resource
  MotionGroupManager motionGroupManager;
	
	@Resource(name = "switchManager")
	private SwitchManager switchManager;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	public SwitchServiceV1() {
		
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Return Switch Scenes
	 * 
	 * @param property
	 *            (company|campus|building|floor|area)
	 * @param pid
	 *            property unique identifier
	 * @param name
	 *            switch name
	 * @return Switch for the selected name and org level
	 */
	@Path("getSwitchScenes/{floorId}/{name}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public SwitchScenes getSwitchScenes(@PathParam("floorId") Long floorId,
			@PathParam("name") String name) {
      
		Switch sw = switchManager.loadSwitchByNameandFloorId(name, floorId);
		List<Scene> sceneList = switchManager.loadSceneBySwitchId(sw.getId());
		SwitchScenes swScenes = new SwitchScenes();
		swScenes.setId(sw.getId());
		swScenes.setName(name);
		swScenes.setSceneList(sceneList);
		return swScenes;
    
	} //end of method getSwitchScenes    
	
	/**
	 * Apply scene of the switch, mostly using the mode as 102 and using scene light level for individual
	 * fixtures
	 * 
	 * @param switchId
	 *            switch identifier
	 * @param sceneId
	 *            scene identifier
	 * @param time
	 *            duration in "minutes"
	 * @return Response status
	 */
	@Path("op/applyScene/{switchid}/{sceneid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyScene(@PathParam("switchid") Long switchId,
			@PathParam("sceneid") Long sceneId, @QueryParam("time") Integer time) {
    
		Switch fetchedSwitch = null;
    if(switchId!=null) {
    	fetchedSwitch= switchManager.getSwitchById(switchId);    	
    }        
    
    //Mapping to the correct scene.        	
    List<Scene> defaultList = switchManager.loadSceneInCreationOrderBySwitchId(switchId);
    int arg = 0;
    for(int i = 0 ; i < defaultList.size(); i ++) {
    	Scene s = defaultList.get(i);
    	//System.out.println(" s.id == " + s.getId() + " scene - " + sceneId + " i == " + i);
    	if(s.getId() == sceneId.longValue()) {
    		arg = i;
    		break;
    	}
    } 
    
		return sendSwitchGroupMsgToFixtures(fetchedSwitch, arg, "scene");		
				
	} //end of method applyScene
	
	/**
	 * dim switch group. sends a dim as switch action to all the sensors
	 * @param floorid
	 *            floor unique identifier
	 * @param name
	 *            switch identifier
	 * @param percentage
	 *            dim percentage
	 * @return Response status
	 */
	@Path("op/dimGroup/{floorid}/{name}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimSwitchGroup(@PathParam("floorid") Long floorId,
			@PathParam("name") String name, @PathParam("percentage") Integer percentage) {
    
		Long switchId = switchManager.loadSwitchByNameandFloorId(name, floorId).getId();
		Switch sw = switchManager.getSwitchById(switchId);
		return sendSwitchGroupMsgToFixtures(sw, percentage, "dim");		
				
	} //end of method dimSwitchGroup
    
	/**
	 * Move fixtures assoicated with switch to auto
	 * 
	 * @param switchId
	 *            switch identifier
	 * 
	 * @return Response status
	 */
	@Path("op/auto/{switchid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyAuto(@PathParam("switchid") Long switchId) {           
		
		return dimFixturesBySwitchId(switchId, 101, 0); //0 is not used for auto
    	
	} //end of method applyAuto

	/**
	 * Dim fixtures based on switch and sceneid, mostly using the mode as 102 and using scene light level for individual
	 * fixtures
	 * 
	 * @param switchId
	 *            switch identifier
	 * @param sceneId
	 *            scene identifier
	 * @param percentage
	 *            percentage (0 (full off) | 100 (full on) | 101 (auto) | 102 (pick scene light levels for individual
	 *            fixtures))
	 * @param time
	 *            duration in "minutes"
	 * @return Response status
	 */
	@Path("op/dim/{switchid}/{sceneid}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixturesBySwitchAndSceneId(@PathParam("switchid") Long switchId,
			@PathParam("sceneid") Long sceneId, @PathParam("percentage") Integer percentage,
			@QueryParam("time") Integer time) {
    	
		// Get called for the scenes
		m_Logger.debug("Switch: " + switchId + ", Scene: " + sceneId + ", Percentage: " + percentage + ", Time: " + time);
		if(time == null) {
			time = 60;
		}
		DeviceServiceImpl.getInstance().dimFixturesBySceneOfSwitch(switchId, sceneId, percentage, time);
		if (percentage == 102) {
			userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
					+ " by applying scene " + sceneId + " for " + time + " minutes",
					UserAuditActionType.Switch_Fixture_Dimming.getName());
		} else {
			userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
					+ " by " + percentage + "% for " + time + " minutes",
					UserAuditActionType.Switch_Fixture_Dimming.getName());
		}
		return new Response();
 
	} //end of method dimFixturesBySwitchAndSceneId

	/**
	 * Dims fixtures based on switch id, used mostly for the slider functionality and auto mode
	 * 
	 * @param switchId
	 *            switch identifier
	 * @param percentage
	 *            percentage (0 (full off) | 100 (full on) | 101 (auto))
	 * @param time
	 *            duration in "minutes"
	 * @return Response status
	 */
	@Path("op/dim/{switchid}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixturesBySwitchId(@PathParam("switchid") Long switchId,
			@PathParam("percentage") Integer percentage, @QueryParam("time") Integer time) {
    	
		m_Logger.debug("Switch: " + switchId + ", Percentage: " + percentage + ", Time: " + time);
		if(time == null) {
			time = 60;
		}
		DeviceServiceImpl.getInstance().dimFixturesBySwitch(switchId, percentage, time);
		if (percentage == 101) {
			userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
					+ " to auto mode for " + time + " minutes", UserAuditActionType.Switch_Fixture_Dimming.getName());
		} else {
			userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
					+ " by " + percentage + "% for " + time + " minutes",
					UserAuditActionType.Switch_Fixture_Dimming.getName());
		}
		return new Response();
  
	} //end of method dimFixturesBySwitchId
	
	@Path("createAndConfigureSwitch/{name}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createAndConfigureSwitch(@PathParam("name") String name) {
		
		System.out.println("name of the switch -- " + name);
		String fixtureVersion = "2.x";
		long floorId = 1;
		Long switchId = null;  	
  	try {
  		GemsGroup gemsGroup = new GemsGroup();
  		gemsGroup.setGroupName(name);
  		Floor floor = floorManager.getFloorById(floorId);
  		gemsGroup.setFloor(floor);
  		gemsGroup = gemsGroupManager.createNewGroup(gemsGroup);
  		switchManager.createSwitchGroup(new SwitchGroup(null, new Integer("14" + motionGroupManager.getNextGroupNo()) , 
  				gemsGroup, fixtureVersion));
  		switchId = switchManager.createNewSwitch(name, floor, gemsGroup).getId();		
  	} catch (SQLException e) {
  		e.printStackTrace();
  	} catch (IOException e) {
  		e.printStackTrace();
  	}
  	userAuditLoggerUtil.log("Create Switch: " + name , UserAuditActionType.Switch_Update.getName());
  	
  	return new Response();
  	  			
	} //end of method createAndConfigureSwitch
    
} //end of class SwitchServiceV1