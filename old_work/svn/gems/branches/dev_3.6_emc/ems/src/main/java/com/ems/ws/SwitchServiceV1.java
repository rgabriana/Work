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

import com.ems.model.Floor;
import com.ems.model.GemsGroup;
import com.ems.model.Scene;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.FloorManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.vo.SwitchScenes;
import com.ems.vo.model.SwitchDetail;
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
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("op/applyScene/{switchid}/{sceneid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyScene(@PathParam("switchid") Long switchId,
			@PathParam("sceneid") Long sceneId, @QueryParam("time") Integer time) {
		Response res = new Response();
		Switch fetchedSwitch = null;
		
		if (switchId != null) {
			try {
				fetchedSwitch = switchManager.getSwitchById(switchId);
			} catch (Exception e) {
				res.setStatus(1);
				return res;
			}
		}
		// Mapping to the correct scene.
		List<Scene> defaultList = switchManager
				.loadSceneInCreationOrderBySwitchId(switchId);
		int arg = 0;
		boolean found=false;
		for (int i = 0; i < defaultList.size(); i++) {
			Scene s = defaultList.get(i);
			// System.out.println(" s.id == " + s.getId() + " scene - " +
			// sceneId + " i == " + i);
			if (s.getId() == sceneId.longValue()) {
				arg = i;
				found = true;
				break;
			}
		}
		if(arg==0 && found==false)
		{
			//EM-710 :Ideally if the scene id is more than the supported value then we should reject the command and should not change the scene to the first scene
			res.setStatus(-1);
			return res;
		}
		return sendSwitchGroupMsgToFixtures(fetchedSwitch, arg, "scene");

	} //end of method applyScene
    
	/**
	 * Move fixtures assoicated with switch to auto
	 * 
	 * @param switchId
	 *            switch identifier
	 * 
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
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
	 * 			0 - Success
	 * 			1- Failure
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("op/dim/{switchid}/{sceneid}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixturesBySwitchAndSceneId(@PathParam("switchid") Long switchId,
			@PathParam("sceneid") Long sceneId, @PathParam("percentage") Integer percentage,
			@QueryParam("time") Integer time) {
    	Response res = new Response();
		// Get called for the scenes
		m_Logger.debug("Switch: " + switchId + ", Scene: " + sceneId + ", Percentage: " + percentage + ", Time: " + time);
		if(time == null) {
			time = 60;
		}
		int sceneOrder = switchManager.getSceneOrderForGivenSwitchAndScene(switchId,sceneId);
		if(sceneOrder>=0)
		{
			if (percentage == 101) 
	        	switchManager.sendSwitchGroupMsgToFixture(switchId, "auto", sceneId.intValue());
	        else if(percentage == 0 || percentage == 100 || percentage == 102)
	        	switchManager.sendSwitchGroupMsgToFixture(switchId, "scene", sceneOrder);
	        else
	        {
	        	m_Logger.debug("Invalid percentage is passed. Please pass valid argument");
	    		res.setStatus(1);
	    		res.setMsg("Invalid percentage is passed. Please pass valid argument");
	    		return res;
	        }
			if (percentage == 102) {
				userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
						+ " by applying scene " + sceneId + " for " + time + " minutes",
						UserAuditActionType.Switch_Fixture_Dimming.getName());
			} else {
				userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
						+ " by " + percentage + "% for " + time + " minutes",
						UserAuditActionType.Switch_Fixture_Dimming.getName());
			}
		}else
    	{
    		m_Logger.debug("Either Invalid switchId or sceneId is passed. Please pass valid arguments");
    		res.setStatus(1);
    		res.setMsg("Either Invalid switchId or sceneId is passed. Please pass valid arguments");
    		return res;
    	}
		//DeviceServiceImpl.getInstance().dimFixturesBySceneOfSwitch(switchId, sceneId, percentage, time);
		return res;
 
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
		Response oResponse = new Response();
       /* Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("intPercentage", percentage);
        nameValMap.put("time", time);
		oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oResponse!= null && oResponse.getStatus()!=200){
    		return oResponse;
    	}*/
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
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("createAndConfigureSwitch/{name}/{percentage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createAndConfigureSwitch(@PathParam("name") String name) {
		
	//	System.out.println("name of the switch -- " + name);
		Response oResponse = new Response();
        oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "groupName", name);
        if(oResponse!= null && oResponse.getStatus()!=200){
        	m_Logger.error("Validation error "+oResponse.getMsg());
    		return oResponse;
    	}
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
    
	/**
     * Return switch list
     * 
     * @param property
     *            (company|campus|building|floor|area)
     * @param pid
     *            property unique identifier
     * @param name
     *            switch name
     * @return Switch for the selected name and org level
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("details/{property}/{pid}/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Switch loadSwitchByNameandProperty(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("name") String name) {
        if (property.equalsIgnoreCase("floor")) {
            return switchManager.loadSwitchByNameandFloorId(name, pid);
        } else if (property.equalsIgnoreCase("area")) {
            return switchManager.loadSwitchByNameandAreaId(name, pid);
        }
        return null;
    }
	
	/**
     * Return switch list by User ID
     * 
     * @param property
     *            (UserID)
     * @param UId
     *            user ID
     * @param name
     *            switch name
     * @return Switch for the Given users
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
    @Path("list/user/{uId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<SwitchDetail> getSwitchesbyUser(@PathParam("uId") String uId) {
        return switchManager.loadSwitchDetailsByUserId(uId);
    }
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("list/userfacilities/{uId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<SwitchDetail> getSwitchesbyUserByFacilities(@PathParam("uId") String uId) {
        return switchManager.loadSwitchDetailsByUserIdAndFacilities(uId);
    }
	
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
    @Path("op/dim/switch/{switchid}/{percentage}/{time}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dimFixtureBySwitch(@PathParam("switchid") Long switchId,
            @PathParam("percentage") Integer percentage, @PathParam("time") Integer time) {
    	m_Logger.debug("Switch: " + switchId + ", Percentage: " + percentage + ", Time: " + time);
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
    }
	
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
     *  		0 - Success
	 * 			1- Failure
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("op/dim/switch/{switchid}/scene/{sceneid}/{percentage}/{time}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dimFixtureBySwitchAndSceneId(@PathParam("switchid") Long switchId,
            @PathParam("sceneid") Long sceneId, @PathParam("percentage") Integer percentage,
            @PathParam("time") Integer time) {
    	// Get called for the scenes
    	Response res = new Response();
        m_Logger.debug("Switch: " + switchId + ", Scene: " + sceneId + ", Percentage: " + percentage + ", Time: "
                + time);
        
        int sceneOrder = switchManager.getSceneOrderForGivenSwitchAndScene(switchId,sceneId);
		if(sceneOrder>=0)
		{
			if (percentage == 101) 
	        	switchManager.sendSwitchGroupMsgToFixture(switchId, "auto", sceneId.intValue());
	        else if(percentage == 0 || percentage == 100 || percentage == 102)
	        	switchManager.sendSwitchGroupMsgToFixture(switchId, "scene", sceneOrder);
	        else
	        {
	        	m_Logger.debug("Invalid percentage is passed. Please pass valid argument");
	    		res.setStatus(1);
	    		res.setMsg("Invalid percentage is passed. Please pass valid argument");
	    		return res;
	        }
			if (percentage == 102) {
				userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
						+ " by applying scene " + sceneId + " for " + time + " minutes",
						UserAuditActionType.Switch_Fixture_Dimming.getName());
			} else {
				userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchId).getName()
						+ " by " + percentage + "% for " + time + " minutes",
						UserAuditActionType.Switch_Fixture_Dimming.getName());
			}
		}else
    	{
    		m_Logger.debug("Either Invalid switchId or sceneId is passed. Please pass valid arguments");
    		res.setStatus(1);
    		res.setMsg("Either Invalid switchId or sceneId is passed. Please pass valid arguments");
    		return res;
    	}
        return res;
    }
	
	/**
	* Dims fixtures based on switch name and floor_id
	* 
	* @param floor_id
	* 			unique identifier of the floor where the switch is located
	* @param switch_name
	*            name of the switch group
	* @param dimLevel
	*            Lighting level to which the switch group should be set up(0-100)
	* @param time
	*            In minutes for which this command is in effect. (optional)
	* @return Response status
	*/
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("op/dimGroup/{floor_id}/{switch_name}/{dimLevel}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixtureByFloorSwitch(@PathParam("floor_id") Long floorId,@PathParam("switch_name") String switchName,
	       @PathParam("dimLevel") Integer percentage, @QueryParam("time") Integer time) {
		Response res = new Response();
		m_Logger.debug("switch name: " + switchName + ", Percentage: " + percentage + ", Time: " + time);
		if(time == null) {
			time = 60;
		}
		Switch switchObj = switchManager.loadSwitchByNameandFloorId(switchName, floorId);
	   DeviceServiceImpl.getInstance().dimFixturesBySwitch(switchObj.getId(), percentage, time);
	   if (percentage == 101) {
	       userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchObj.getId()).getName()
	               + " to auto mode for " + time + " minutes", UserAuditActionType.Switch_Fixture_Dimming.getName());
	   } else {
	       userAuditLoggerUtil.log("Dim fixture with switch " + switchManager.getSwitchById(switchObj.getId()).getName()
	               + " by " + percentage + "% for " + time + " minutes",
	               UserAuditActionType.Switch_Fixture_Dimming.getName());
	   }
	   return res;
	}
} //end of class SwitchServiceV1