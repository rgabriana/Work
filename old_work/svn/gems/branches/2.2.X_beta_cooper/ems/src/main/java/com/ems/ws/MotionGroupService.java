package com.ems.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionGroup;
import com.ems.mvc.util.ControllerUtils;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.service.FixtureManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.ws.util.Response;

@Controller
@Path("/org/motiongroup")
public class MotionGroupService {
	
	private static final Logger m_MGRPLogger = Logger.getLogger("WSLogger");
    @Resource
    GemsGroupManager gemsGroupManager;
    @Resource
    MotionGroupManager motionGroupManager;
    @Resource
    FixtureManager fixtureManager;
    private static Long LastActivityTime = null ;
    private static Long VALIDATION_INACTIVITY_TIME = 15 * 60 * 1000l; //15 minutes it is in sec
	
	/**
	 * Get list of fixtures for a motion group id
	 * @param groupId
	 * @return
	 */
	@Path("getGroupFixtures/{groupId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroupFixture> getSwitchFixtures(@PathParam("groupId") Long groupId) {
        MotionGroup motionGroup = motionGroupManager.getMotionGroupById(groupId);
        return gemsGroupManager.getGemsGroupFixtureByGroup(motionGroup.getGemsGroup().getId());
    }
	
	/**
	 * Get list of motion groups
	 * @return motion groups list for the property level
	 * @throws UnsupportedEncodingException 
	 */
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroup> getMotionGroups(@CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws UnsupportedEncodingException {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(URLDecoder.decode(cookie, "UTF-8"));
        List<GemsGroup> motionGroups = motionGroupManager.loadGroupsByFloor(cookieHandler.getFacilityId());
        return motionGroups;
    }

    /**
     * Update the fixtures for motion group
     * @param motiongGroupId
     * @param fixtures: List of fixture objects in the group after change
     * @return
     */
    @Path("updateGroupFixtures/{groupId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGroupFixtures(@PathParam("groupId") Long gemsGroupId, List<Fixture> fixtures) {
    	String resp = "";
    	List<GemsGroupFixture> currentGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(gemsGroupId);
    	Set<Long> currentGroupFixturesId = new HashSet<Long>();
    	if(currentGroupFixtures != null && currentGroupFixtures.size() > 0) {
    		for(GemsGroupFixture ggf: currentGroupFixtures) {
    			currentGroupFixturesId.add(ggf.getFixture().getId());
    		}
    	}

    	GemsGroup oGemsGroup = gemsGroupManager.loadGemsGroup(gemsGroupId);
    	if (oGemsGroup != null) {
            for (Fixture f : fixtures) {
                if (!currentGroupFixturesId.contains(f.getId().longValue())) {
            		List<GemsGroupFixture> listGroups = gemsGroupManager.getAllGroupsOfFixture(f);
            		Fixture fixt = fixtureManager.getFixtureById(f.getId());
            		
            		if(listGroups != null && listGroups.size() >= 10)
            		{
            			resp += fixt.getName();
            			resp += ",";
            			continue;
            		}
                	gemsGroupManager.addGroupFixture(oGemsGroup, f);
                } else {
                    currentGroupFixturesId.remove(f.getId());
                }
            }
            
            for (Long fixtureId : currentGroupFixturesId) {
                gemsGroupManager.removeGroupFixture(gemsGroupId, fixtureId);
            }
    	}
        Response response = new Response();
        response.setMsg(resp);
        return response;
    }
    
    
    /**
     * Delete Motion Group
     * @param motionGroupId
     * @return
     */
    @Path("delete/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteMotionGroup(@PathParam("id") Long motionGroupId) {
        motionGroupManager.deleteMotionGroup(motionGroupId);
        Response response = new Response();
        response.setMsg("S");
        return response;
    }
    
    /**
     * changes group sync flag for all fixture associated to this group to false
     * 
     *
     */
    @Path("updateGroupSyncFlag/{groupId}/{syncFlag}")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response toggelMotionGroupSyncFlag(@PathParam("groupId") Long groupId , @PathParam("syncFlag") Boolean syncFlag) {
    	Response response = new Response();
    	String returnValue = "failed" ;
    	MotionGroup mgrp = motionGroupManager.getMotionGroupByGemsGroupId(groupId);
    	if(mgrp!=null)
    	{
    		GemsGroup oGGroup = mgrp.getGemsGroup();
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
        m_MGRPLogger.info("updation of group Sync flag to "+ syncFlag +" for Group ID " + groupId + " resulted in " +returnValue  );
        return response;
    }
    
    /**
     * check the last connectivity time with motion group edit. If exceed the time, reset the synch flag 
     * 
     *
     */
    @Path("lastTimeValidation/")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void timeValidationforMotionSyncGroup() {
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
