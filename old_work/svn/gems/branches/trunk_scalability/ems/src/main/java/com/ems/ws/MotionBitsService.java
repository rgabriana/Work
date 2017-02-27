package com.ems.ws;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionBitsScheduler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionBitsConfigManager;
import com.ems.ws.util.Response;

/**
 * @author Shilpa Chalasani
 * 
 */
@Controller
@Path("/org/motionbits")
public class MotionBitsService {
    static final Logger logger = Logger.getLogger(MotionBitsService.class.getName());
    
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "motionBitsConfigManager")
    private  MotionBitsConfigManager motionBitsConfigManager;
    @Resource
    GemsGroupManager gemsGroupManager;
    @Resource
    FixtureManager	fixtureManager;
    
    public MotionBitsService() {
    }

	@Path("op/validateschedule/{name}/{starttime}/{endtime}/{id}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public String validateSchedule(@PathParam("name") String name, @PathParam("starttime") String startTime, @PathParam("endtime") String endTime,
			@PathParam("id") Long id) {

		return motionBitsConfigManager.validateSchedule(name, startTime, endTime, id);
	}
    
    /**
     * Delete a group
     * 
     * @param id
     *            Schedule ID that needs to be deleted
     * @return Response status
     */
    @Path("op/deleteschedule/{id}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteMotionBitsSchedule(@PathParam("id") Long id) {
    	Response resp = new Response();
    	
    	if(motionBitsConfigManager.deleteMotionBitsScheduleById(id) == false)
    		resp.setStatus(1);
    	else
    		resp.setStatus(0);
  
    	return resp;
    }
    
    /**
     * Stop motion bit activity
     * 
     * @param id
     *            Schedule ID that needs to be stopped
     * @return Response status
     */
    @Path("op/stopschedule/{id}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response stopMotionBitsSchedule(@PathParam("id") Long id) {

    	Response resp = new Response();
    	
    	if(motionBitsConfigManager.stopMotionBitsScheduleById(id) == false)
    		resp.setStatus(1);
    	else
    		resp.setStatus(0);
  
    	return resp;
    }
    
	/**
	 * Get list of fixtures for a motion bits group id
	 * @param groupId
	 * @return
	 */
	@Path("getGroupFixtures/{groupId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroupFixture> getGroupFixtures(@PathParam("groupId") Long groupId) {
		MotionBitsScheduler scheduler = motionBitsConfigManager.loadMotionBitsScheduleById(groupId);
        return gemsGroupManager.getGemsGroupFixtureByGroup(scheduler.getMotionBitGroup().getId());
    }
	
    /**
     * Update the fixtures for motion bits group
     * @param motiongBitsGroupId
     * @param fixtures: List of fixture objects in the group after change
     * @return
     */
    @Path("updateGroupFixtures/{groupId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGroupFixtures(@PathParam("groupId") Long gemsGroupId, List<Fixture> fixtures) {       
        
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
                	gemsGroupManager.addGroupFixture(oGemsGroup, f);
                	fixtureManager.changeGroupsSyncPending(fixtureManager.getFixtureById(f.getId()), false);
                } else {
                    currentGroupFixturesId.remove(f.getId());
                }
            }
            
            for (Long fixtureId : currentGroupFixturesId) {
                gemsGroupManager.removeGroupFixture(gemsGroupId, fixtureId);
                fixtureManager.changeGroupsSyncPending(fixtureManager.getFixtureById(fixtureId), false);
            }
    	}
        Response response = new Response();
        response.setMsg("S");
        return response;
    }
    
    @Path("duplicatecheck/{name}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateProfile(@PathParam("name") String name) {
		Response oStatus = new Response();
		if (motionBitsConfigManager.loadMotionBitsScheduleByName(name) > 0) {
			oStatus.setMsg(name);
			oStatus.setStatus(0);
			return oStatus;
		}

		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}
	
	
}