package com.ems.ws;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.model.Fixture;
import com.ems.model.MotionBitsScheduler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.MotionBitsConfigManager;
import com.ems.types.UserAuditActionType;
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
    @Resource(name = "companyManager")
    private CompanyManager companyManager;
    @Resource(name = "floorManager")
    private FloorManager floorManager;
    
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    
    public MotionBitsService() {
    }
    
    /**
     * Create new motion bits configuration
     * 
     * @param motionBitsSchedule
     * @return Response status
     * @throws IOException 
     * @throws SQLException 
     */
    @Path("op/createschedule/{gid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createMotionBitsSchedule(@PathParam("gid") Long gid, MotionBitsScheduler schedule) throws SQLException, IOException {
    	
    	schedule.setName(schedule.getDisplayName()+"_"+"schedular") ;
    	motionBitsConfigManager.saveMotionBitsSchedule(gid, schedule);
        
    	userAuditLoggerUtil.log("Create motion bits configuration: " + schedule.getName(), UserAuditActionType.Motion_Bits_update.getName());

    	motionBitsConfigManager.addMotionBitsSchedulerJob(schedule);
    	
    	Response oResponse = new Response();

        return oResponse;
    }

	@Path("op/validateschedule/{name}/{starttime}/{endtime}/{id}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public String validateSchedule(@PathParam("name") String name, @PathParam("starttime") String startTime, @PathParam("endtime") String endTime,
			@PathParam("id") Long id, List<Fixture> fixtures) {

		return motionBitsConfigManager.validateSchedule(name, startTime, endTime, fixtures, id);
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
     * Edits group for an organization
     * 
     * @param gemsGroup
     *              <gemsGroup><id></id><name>Hall</name><type><id>1</id></type></gemsGroup>
     * @return Response status
     * @throws IOException 
     * @throws SQLException 
     */
    @Path("op/editschedule")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editMotionBitsSchedule(MotionBitsScheduler schedule) throws SQLException, IOException {
        Response oResponse = new Response();
    	userAuditLoggerUtil.log("Edit motion bits configuration: " + schedule.getDisplayName(), UserAuditActionType.Motion_Bits_update.getName());
    	//save the new schedule
    	motionBitsConfigManager.saveMotionBitsSchedule(0L, schedule);
    	//delete the old schedule job
    	motionBitsConfigManager.deleteMotionBitsSchedulerJob(schedule.getId());
    	//add the new schedule job
    	motionBitsConfigManager.addMotionBitsSchedulerJob(schedule);
    	

    	oResponse.setMsg("S");
        return oResponse;
    }
}