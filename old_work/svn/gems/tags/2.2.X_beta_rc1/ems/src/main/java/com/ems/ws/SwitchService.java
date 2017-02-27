/**
 * 
 */
package com.ems.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.dao.FixtureDao;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Scene;
import com.ems.model.SceneLevel;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.Wds;
import com.ems.mvc.util.ControllerUtils;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.FixtureManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.SwitchManager;
import com.ems.service.WdsManager;
import com.ems.types.GGroupType;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.SwitchDetail;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/switch")
public class SwitchService {
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "switchManager")
    private SwitchManager switchManager;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");
    private static final Logger m_SWLogger = Logger.getLogger("SwitchLogger");
   private static Long LastActivityTime = null ;
   private static Long VALIDATION_INACTIVITY_TIME = 15 * 60 * 1000l; //15 minutes it is in sec

    @Resource(name = "gemsGroupManager")
    private GemsGroupManager gemsGroupManager;
    
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    
    @Resource(name = "wdsManager")
    private WdsManager wdsManager;

    public SwitchService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Return switch list
     * 
     * @param property
     *            (company|campus|building|floor|area|secondarygateway)
     * @param pid
     *            property unique identifier
     * @return Switch list for the selected org level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Switch> getSwitchList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        if (property.equalsIgnoreCase("company")) {
            return switchManager.loadAllSwitches();
        } else if (property.equalsIgnoreCase("campus")) {
            return switchManager.loadSwitchByCampusId(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return switchManager.loadSwitchByBuildingId(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return switchManager.loadSwitchByFloorId(pid);
        } else if (property.equalsIgnoreCase("area")) {
            // TODO: Need to update switch model to support area, for now send
            // floor data
            return switchManager.loadSwitchByAreaId(pid);
        }
        return null;
    }

    /**
     * Updates the position of the selected switches on the floorplan
     * 
     * @param switches
     *            List of selected switches with their respective x & y co-ordinates
     *            "<switches><switch><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></switch></switches>"
     * @return Response status
     */
    @Path("du/updateposition")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateSwitchPosition(List<Switch> switches) {
        m_Logger.debug("Switches: " + switches.size());
        Iterator<Switch> itr = switches.iterator();
        StringBuffer switchNames = new StringBuffer("");
        boolean log = false;
        while (itr.hasNext()) {
            Switch oSwitch = (Switch) itr.next();
            if (oSwitch.getXaxis() != null && oSwitch.getYaxis() != null) {
                switchManager.updatePositionById(oSwitch);
                switchNames.append(switchManager.getSwitchById(oSwitch.getId()).getName() + "(X:" + oSwitch.getXaxis()
                        + " Y:" + oSwitch.getYaxis() + ") ");
                log = true;
            }
        }
        if (log) {
            userAuditLoggerUtil.log("Update switch positions for " + switchNames,
                    UserAuditActionType.Switch_Update.getName());
        }
        return new Response();
    }

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
    @Path("list/user/{uId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<SwitchDetail> getSwitchesbyUser(@PathParam("uId") String uId) {
        return switchManager.loadSwitchDetailsByUserId(uId);

    }

    /**
     * Delete Switch
     * 
     * @param id
     *            Switch unique identifier
     * @return Response status
     */
    @Path("delete/{id}")
    @GET
    public Response deleteSwitch(@PathParam("id") long id) {
        
        Response response = new Response();
        
        Switch sw = switchManager.getSwitchById(id);
        if (sw.getGemsGroup() != null) {
            List<GemsGroupFixture> gemsGroupFixtureList = gemsGroupManager.getGemsGroupFixtureByGroup(sw.getGemsGroup().getId());
            List<Wds> wdsList = wdsManager.loadCommissionedWdsBySwitchId(id);
            if ((gemsGroupFixtureList != null && !gemsGroupFixtureList.isEmpty())
                    || (wdsList != null && !wdsList.isEmpty())) {
                response.setStatus(1);
            }
        }
        if (response.getStatus() == 0) {
            String switchName = switchManager.getSwitchById(id).getName();
            switchManager.deleteSwitch(id);
            userAuditLoggerUtil.log("Delete switch: " + switchName, UserAuditActionType.Switch_Update.getName());
        }
        return response;
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
     */
    @Path("op/dim/switch/{switchid}/scene/{sceneid}/{percentage}/{time}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dimFixtureBySwitchAndSceneId(@PathParam("switchid") Long switchId,
            @PathParam("sceneid") Long sceneId, @PathParam("percentage") Integer percentage,
            @PathParam("time") Integer time) {
        m_Logger.debug("Switch: " + switchId + ", Scene: " + sceneId + ", Percentage: " + percentage + ", Time: "
                + time);
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
     * 
     * @param switchId
     * @param fixtureId
     * @return
     */
    @Path("op/push/cfg/switch/{switchid}/fixture/{fixtureid}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response pushSwitchConfiguration(@PathParam("switchid") Long switchId, @PathParam("fixtureid") Long fixtureId) {
        Response oResponse = new Response();
        m_Logger.debug("Switch: " + switchId + ", pushing switch configuration to fixture: " + fixtureId);
        oResponse.setStatus(switchManager.sendSwitchGroupParams(switchId, fixtureId));
        return oResponse;
    }

    /**
     * 
     * @param switchId
     * @return
     */
    @Path("op/push/wdscfg/switch/{switchid}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response pushWdsConfiguration(@PathParam("switchid") Long switchId) {
        Response oResponse = new Response();
        m_Logger.debug("Switch: " + switchId + ", pushing wds configuration to all fixture in the switch group");
        oResponse.setStatus(switchManager.sendSwitchGroupWdsParams(switchId));
        return oResponse;
    }

    @Path("op/{switchid}/action/{action}/argument/{argument}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response sendSwitchGroupMsgToFixtures(@PathParam("switchid") Long switchId,
            @PathParam("action") String action, @PathParam("argument") int argument) {
        Response oResponse = new Response();
        m_Logger.debug("Switch: " + switchId + ", sending command " + action + ", argument " + argument);
        oResponse.setStatus(switchManager.sendSwitchGroupMsgToFixture(switchId, action, argument));
        return oResponse;
    }
    
    /**
     * returns number of associated fixture to the selected gateway
     * 
     * @param gatewayId
     *            Gateway unique identifier
     * @return No. of fixtures as a part of response
     */
    @Path("getcountbygateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCountByGateway(@PathParam("gatewayId") Long gatewayId) {
        Response oStatus = new Response();
        List<Fixture> fxList = switchManager.loadAllFixtureByGatewayId(gatewayId);
        oStatus.setStatus((fxList != null ? fxList.size() : 0));
        return oStatus;
    }

    @Path("getSwitchFixtures/{switchId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroupFixture> getSwitchFixtures(@PathParam("switchId") Long switchId) {
        Switch sw = switchManager.getSwitchById(switchId);
        return gemsGroupManager.getGemsGroupFixtureByGroup(sw.getGemsGroup().getId());
    }

    @Path("updateSwitchFixtures/{switchId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateSwitchFixtures(@PathParam("switchId") Long switchId, List<Fixture> fixtures) {
    	String resp = "";
        Switch sw = switchManager.getSwitchById(switchId);
        Long groupId = sw.getGemsGroup().getId();
        SwitchGroup sg = switchManager.getSwitchGroupByGemsGroupId(groupId);
        int groupNo = Integer.parseInt(sg.getGroupNo().toString(), 16);
    	List<Scene> scenesList = switchManager.loadSceneBySwitchId(switchId);
        Set<Long> currentSceneFixtures = switchManager.loadSwitchSceneFixtures(switchId);
        List<Fixture> addFixtures = new ArrayList<Fixture>();
        for (Fixture f : fixtures) {
            if (!currentSceneFixtures.contains(f.getId().longValue())) {
        		List<GemsGroupFixture> listGroups = gemsGroupManager.getAllGroupsOfFixture(f);
        		Fixture fixt = fixtureManager.getFixtureById(f.getId());
        		
        		if(sg.getFixtureVersion().equalsIgnoreCase("2.x") && listGroups != null && listGroups.size() >= 10)
        		{
        			resp += fixt.getName();
        			resp += ",";
        			continue;
        		}
            	addFixtures.add(f);
                for (Scene s : scenesList) {
                    SceneLevel sl = null;
                    if (s.getSceneOrder() == 0) {
                        sl = new SceneLevel(null, switchId, s.getId(), f.getId(), 100);
                    } else if (s.getSceneOrder() == 1) {
                        sl = new SceneLevel(null, switchId, s.getId(), f.getId(), 0);
                    } else {
                        sl = new SceneLevel(null, switchId, s.getId(), f.getId(), 50);
                    }
                    switchManager.updateSceneLevel(sl);
                }
            } else {	         	
                currentSceneFixtures.remove(f.getId());
            }
        }
      
        gemsGroupManager.addSwitchFixtures(switchManager.getSwitchById(switchId), addFixtures);

        List<GemsGroupFixture> oGGFxList = new ArrayList<GemsGroupFixture>();
        for (Long fixtureId : currentSceneFixtures) {
            GemsGroupFixture gemsGroupFixture = gemsGroupManager.getGemsGroupFixture(groupId, fixtureId);
            oGGFxList.add(gemsGroupFixture);
            int iStatus = gemsGroupManager.removeFixturesFromGroup(groupId, oGGFxList, groupNo, GGroupType.SwitchGroup.getId(), (long) 0);
            oGGFxList.clear();
            if (iStatus == 0) {
                switchManager.deleteSceneLevelsForSwitch(switchId, fixtureId);
                m_SWLogger.info(fixtureId + ": successfully left group " + groupNo);
                // set group sync flag to false as fixture left the group
                Fixture fixture = fixtureManager.getFixtureById(fixtureId) ;
        		fixture.setGroupsSyncPending(false);
            	fixtureManager.changeGroupsSyncPending(fixture) ;
                FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
                
            }else {
                m_SWLogger.warn(fixtureId + ": failed to leave group " + groupNo);
            }
        }
        GemsGroup oGGroup = sw.getGemsGroup();
        if (oGGroup != null) {
            gemsGroupManager.updateGroupFixtureSyncPending(oGGroup.getId(), true);
        }
        Response response = new Response();
        response.setMsg(resp);
        return response;
    }
    
    /**
     * changes group sync flag for all fixture assosiated to this switch to false
     * 
     *
     */
    @Path("updateGroupSyncFlag/{switchId}/{syncFlag}")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response toggelGroupSyncFlag(@PathParam("switchId") Long switchId , @PathParam("syncFlag") Boolean syncFlag) {
    	Response response = new Response();
    	String returnValue = "failed" ;
    	Switch sw = switchManager.getSwitchById(switchId);
    	if(sw!=null)
    	{
    		GemsGroup oGGroup = sw.getGemsGroup();
            if (oGGroup != null) {
            
            	ArrayList<GemsGroupFixture> ggf = (ArrayList<GemsGroupFixture>) gemsGroupManager.getAllGemsGroupFixtureByGroup(oGGroup.getId()) ;
            	if(ggf!=null && (!ggf.isEmpty()))
            	{
            		for(GemsGroupFixture gf : ggf)
            		{	
            			Fixture f = gf.getFixture() ;
            			f.setGroupsSyncPending(false);
            			fixtureManager.changeGroupsSyncPending(f) ;
            	        FixtureCache.getInstance().invalidateDeviceCache(f.getId());
            	}
            		returnValue = "success" ;
            	}
            }
    	}
        response.setMsg(returnValue);  
        m_SWLogger.info("updation of group Sync flag to "+ syncFlag +" for switch ID " + switchId + " resulted in " +returnValue  );
        return response;
    }
    
  
    /**
     * check the last conectivity time with switch edit ox if excede the 
     * 
     *
     */
    @Path("lastTimeValidation/")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void timeValidationforSwitchSyncGroup() {
    	if(LastActivityTime==null)
    	{
    		LastActivityTime = System.currentTimeMillis();
    	}
    	else
    	{
    		if(((System.currentTimeMillis()) - LastActivityTime.longValue()) < VALIDATION_INACTIVITY_TIME.longValue())
    		{
    			LastActivityTime = System.currentTimeMillis() ;
    			ControllerUtils.timerFlagUpdate(true) ;
    		}
    		else
    		{
    			ControllerUtils.timerFlagUpdate(false) ;
    		}
    		
    	}
    	
}     
    
 
    

}