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
import com.ems.cache.PlugloadCache;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.MotionGroup;
import com.ems.model.MotionGroupFixtureDetails;
import com.ems.model.Plugload;
import com.ems.mvc.util.ControllerUtils;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.service.FixtureManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.GemsPlugloadGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.types.GGroupType;
import com.ems.ws.util.Response;

@Controller
@Path("/org/motiongroup")
public class MotionGroupService {
	
	private static final Logger m_MGRPLogger = Logger.getLogger("WSLogger");
    @Resource
    GemsGroupManager gemsGroupManager;
    @Resource
    GemsPlugloadGroupManager gemsPlugloadGroupManager;
    @Resource
    MotionGroupManager motionGroupManager;
    @Resource
    FixtureManager fixtureManager;
    @Resource
    PlugloadManager plugloadManager;
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
	@Deprecated
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
            
            /*
            for (Long fixtureId : currentGroupFixturesId) {
                gemsGroupManager.removeGroupFixture(gemsGroupId, fixtureId);
            }
            */
            MotionGroup motionGroup = motionGroupManager.getMotionGroupByGemsGroupId(oGemsGroup.getId());
        	int groupNo = Integer.parseInt(motionGroup.getGroupNo().toString(), 16);
            List<GemsGroupFixture> oGGFxList = new ArrayList<GemsGroupFixture>();
            for (Long fixtureId : currentGroupFixturesId) {
                GemsGroupFixture gemsGroupFixture = gemsGroupManager.getGemsGroupFixture(gemsGroupId, fixtureId);
                oGGFxList.add(gemsGroupFixture);
                int iStatus = gemsGroupManager.removeFixturesFromGroup(gemsGroupId, oGGFxList, groupNo, GGroupType.MotionGroup.getId(), (long) 0);
                oGGFxList.clear();
                if (iStatus == 0) {
                    m_MGRPLogger.info(fixtureId + ": successfully left group " + groupNo);
                }else {
                	m_MGRPLogger.warn(fixtureId + ": failed to leave group " + groupNo);
                	//Fixture seems to be unreachable. Mark user_action to 2 - so that in the next sync, current group will get deleted from SU
             		 gemsGroupFixture.setUserAction(GemsGroupFixture.USER_ACTION_FIXTURE_DELETE);
                }
                // set group sync flag to false as fixture left the group
                Fixture fixture = fixtureManager.getFixtureById(fixtureId) ;
        		fixture.setGroupsSyncPending(false);
            	fixtureManager.changeGroupsSyncPending(fixture) ;
                FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
            }
            GemsGroup oGGroup = motionGroup.getGemsGroup();
            if (oGGroup != null) {
                gemsGroupManager.updateGroupFixtureSyncPending(oGGroup.getId(), true);
            }
    	}
        Response response = new Response();
        response.setMsg(resp);
        return response;
    }
    
    @Path("updatemotiongroupfixtures/{groupId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateMotionGroupFixtures(@PathParam("groupId") Long gemsGroupId, List<GemsGroupFixture> ggfxList) {
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
            for (GemsGroupFixture ggf : ggfxList) {
            	Fixture f = ggf.getFixture();
            	if (f == null)
            		continue;
                if (!currentGroupFixturesId.contains(f.getId().longValue())) {
            		List<GemsGroupFixture> listGroups = gemsGroupManager.getAllGroupsOfFixture(f);
            		Fixture fixt = fixtureManager.getFixtureById(f.getId());
            		
            		if(listGroups != null && listGroups.size() >= 10)
            		{
            			resp += fixt.getName();
            			resp += ",";
            			continue;
            		}
                	gemsGroupManager.addGroupFixture(oGemsGroup, fixt, ggf);
                } else {
                    currentGroupFixturesId.remove(f.getId());
                }
            }
            
            MotionGroup motionGroup = motionGroupManager.getMotionGroupByGemsGroupId(oGemsGroup.getId());
        	int groupNo = Integer.parseInt(motionGroup.getGroupNo().toString(), 16);
            List<GemsGroupFixture> oGGFxList = new ArrayList<GemsGroupFixture>();
            for (Long fixtureId : currentGroupFixturesId) {
                GemsGroupFixture gemsGroupFixture = gemsGroupManager.getGemsGroupFixture(gemsGroupId, fixtureId);
                oGGFxList.add(gemsGroupFixture);
                int iStatus = gemsGroupManager.removeFixturesFromGroup(gemsGroupId, oGGFxList, groupNo, GGroupType.MotionGroup.getId(), (long) 0);
                oGGFxList.clear();
                if (iStatus == 0) {
                    m_MGRPLogger.info(fixtureId + ": successfully left group " + groupNo);
                }else {
                	m_MGRPLogger.warn(fixtureId + ": failed to leave group " + groupNo);
                	//Fixture seems to be unreachable. Mark user_action to 2 - so that in the next sync, current group will get deleted from SU
             		 gemsGroupFixture.setUserAction(GemsGroupFixture.USER_ACTION_FIXTURE_DELETE);
                }
                // set group sync flag to false as fixture left the group
                Fixture fixture = fixtureManager.getFixtureById(fixtureId) ;
        		fixture.setGroupsSyncPending(false);
            	fixtureManager.changeGroupsSyncPending(fixture) ;
                FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
            }
            GemsGroup oGGroup = motionGroup.getGemsGroup();
            if (oGGroup != null) {
                gemsGroupManager.updateGroupFixtureSyncPending(oGGroup.getId(), true);
            }
    	}
        Response response = new Response();
        response.setMsg(resp);
        return response;
    }
    
    @Path("updatemotiongroupplugloads/{groupId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateMotionGroupPlugloads(@PathParam("groupId") Long gemsGroupId, List<GemsGroupPlugload> ggpgList) {
    	String resp = "";
    	List<GemsGroupPlugload> currentGroupPlugloads = gemsPlugloadGroupManager.getGemsGroupPlugloadByGroup(gemsGroupId);
    	Set<Long> currentGroupPlugloadsId = new HashSet<Long>();
    	if(currentGroupPlugloads != null && currentGroupPlugloads.size() > 0) {
    		for(GemsGroupPlugload ggpg: currentGroupPlugloads) {
    			currentGroupPlugloadsId.add(ggpg.getPlugload().getId());
    		}
    	}

    	GemsGroup oGemsGroup = gemsGroupManager.loadGemsGroup(gemsGroupId);
    	if (oGemsGroup != null) {
            for (GemsGroupPlugload ggpg : ggpgList) {
            	Plugload pg = ggpg.getPlugload();
            	if (pg == null)
            		continue;
                if (!currentGroupPlugloadsId.contains(pg.getId().longValue())) {
            		List<GemsGroupPlugload> listGroups = gemsPlugloadGroupManager.getAllGroupsOfPlugload(pg);
            		Plugload plg = plugloadManager.getPlugloadById(pg.getId());
            		
            		if(listGroups != null && listGroups.size() >= 10)
            		{
            			resp += plg.getName();
            			resp += ",";
            			continue;
            		}
            		gemsPlugloadGroupManager.addGroupPlugload(oGemsGroup, plg, ggpg);
                } else {
                	currentGroupPlugloadsId.remove(pg.getId());
                }
            }
            
            MotionGroup motionGroup = motionGroupManager.getMotionGroupByGemsGroupId(oGemsGroup.getId());
        	int groupNo = Integer.parseInt(motionGroup.getGroupNo().toString(), 16);
            List<GemsGroupPlugload> oGGPgList = new ArrayList<GemsGroupPlugload>();
            for (Long plugloadId : currentGroupPlugloadsId) {
            	GemsGroupPlugload gemsGroupPlugload = gemsPlugloadGroupManager.getGemsGroupPlugload(gemsGroupId, plugloadId);
                oGGPgList.add(gemsGroupPlugload);
                int iStatus = gemsPlugloadGroupManager.removePlugloadsFromGroup(gemsGroupId, oGGPgList, groupNo, GGroupType.MotionGroup.getId(), (long) 0);
                oGGPgList.clear();
                if (iStatus == 0) {
                    m_MGRPLogger.info(plugloadId + ": successfully left group " + groupNo);
                }else {
                	m_MGRPLogger.warn(plugloadId + ": failed to leave group " + groupNo);
                	//Plugload seems to be unreachable. Mark user_action to 2 - so that in the next sync, current group will get deleted from Plugload
             		 gemsGroupPlugload.setUserAction(GemsGroupPlugload.USER_ACTION_PLUGLOAD_DELETE);
                }
                // set group sync flag to false as plugload left the group
                Plugload plugload = plugloadManager.getPlugloadById(plugloadId);
                plugload.setGroupsSyncPending(false);
                plugloadManager.changeGroupsSyncPending(plugload) ;
                PlugloadCache.getInstance().invalidateDeviceCache(plugload.getId());
            }
            GemsGroup oGGroup = motionGroup.getGemsGroup();
            if (oGGroup != null) {
            	gemsPlugloadGroupManager.updateGroupPlugloadSyncPending(oGGroup.getId(), true);
            }
    	}
        Response response = new Response();
        response.setMsg(resp);
        return response;
    }
    
    
    @Path("updatemotiongroupfixturesdetails/{groupId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateMotionGroupFixturesDetails(@PathParam("groupId") Long gemsGroupId, GemsGroupFixture ggfx) {
        Response response = new Response();
    	String resp = "";
    	GemsGroupFixture ggf = gemsGroupManager.getGemsGroupFixture(gemsGroupId, ggfx.getFixture().getId());
    	if (ggf != null) {
	    	if (motionGroupManager.updateMotionGroupFixtureDetails(gemsGroupId, ggf, ggfx.getMotionGrpFxDetails()) == false) {
	    		response.setStatus(1);	    	
	    	}
    	}else {
    		response.setStatus(2);
    	}
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
    
    @Path("applymotiondetailstofixtures/{groupId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response sendMotionGroupDetailsToFixtures(@PathParam("groupId") Long groupId) {
    	Response oRes = new Response();
    	List<GemsGroupFixture> currentGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(groupId);
        for (GemsGroupFixture ggf : currentGroupFixtures) {
			motionGroupManager.sendMotionGroupDetailsToFixture(groupId, ggf);
        }
    	return oRes;
    }

}
