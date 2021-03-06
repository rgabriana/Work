package com.ems.ws;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import com.ems.model.Company;
import com.ems.model.Fixture;
import com.ems.model.Floor;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionGroup;
import com.ems.model.SwitchGroup;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.SwitchManager;
import com.ems.types.GGroupType;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

/**
 * @author Shilpa Chalasani
 * 
 */
@Controller
@Path("/org/gemsgroups")
public class GemsGroupService {
    static final Logger logger = Logger.getLogger(GemsGroupService.class.getName());
    
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "gemsGroupManager")
    private GemsGroupManager gemsGroupManager;
    @Resource(name = "motionGroupManager")
    private MotionGroupManager motionGroupManager;
    @Resource(name = "switchManager")
    private SwitchManager switchManager;

    @Resource(name = "companyManager")
    private CompanyManager companyManager;
    @Resource(name = "floorManager")
    private FloorManager floorManager;
    
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    
    public GemsGroupService() {
    }

    /**
     * Returns groups list
     * 
     * @param pid
     *            property unique identifier
     * @return fixture list for the property level
     */
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroup> getGroupsList(@CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws UnsupportedEncodingException {
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(URLDecoder.decode(cookie, "UTF-8"));
        return gemsGroupManager.loadGroupsByFloor(cookieHandler.getFacilityId());
    }

    /**
     * returns GemsGroup object for the given group name
     * @param groupName
     * 
     * @return GemsGroup object
     * @throws UnsupportedEncodingException 
     */
    @Path("loadbyname/{groupName}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public GemsGroup getGroupsByName(@PathParam("groupName")String groupName, @CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws UnsupportedEncodingException {
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(URLDecoder.decode(cookie, "UTF-8"));
        return gemsGroupManager.loadGroupsByGroupNameAndFloor(groupName, cookieHandler.getFacilityId());
    }
    
    /**
     * Create new group for an organization
     * 
     * @param gemsGroup
     *              <gemsGroup><id></id><name>Hall</name><type><id>1</id></type></gemsGroup>
     * @return Response status
     * @throws IOException 
     * @throws SQLException 
     */
    @Path("op/creategroup")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createNewGroup(GemsGroup gemsGroup, @CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws SQLException, IOException {
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(URLDecoder.decode(cookie, "UTF-8"));
        Company oCompany = companyManager.loadCompany();
        Floor floor = floorManager.getFloorById(cookieHandler.getFacilityId());
        //gemsGroup.setCompany(oCompany);
        gemsGroup.setFloor(floor);
        GemsGroup oGemsGroup;
        
//        if(gemsGroup.getType().getId() == 2)
//        	oGemsGroup = gemsGroupManager.createNewGroup(gemsGroup, GGroupType.MotionGroup);
//        else
//        	oGemsGroup = gemsGroupManager.createNewGroup(gemsGroup, GGroupType.MotionBitsGroup);

        Response oResponse = new Response();
//        oResponse.setMsg(oGemsGroup.getId().toString());
        userAuditLoggerUtil.log("Create group: " + gemsGroup.getGroupName(), UserAuditActionType.Group_Update.getName());
        return oResponse;
    }

    
    /**
     * Delete a group
     * 
     * @param gid
     *            Group ID that needs to be deleted
     * @param forceDelete
     *            forceDelete = 1 if the group is to be deleted even when no ack is received from SU
     * @return Response status
     */
    @Path("op/deletegroup/gtype/{type}/groupid/{gid}/{forceDelete}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteGroup(@PathParam("type") int gType, @PathParam("gid") Long gid, @PathParam("forceDelete") Long forceDelete) {
        GemsGroup group = gemsGroupManager.loadGemsGroup(gid);
        List<GemsGroupFixture> gemsGroupFixtures = null;
        int groupNo = 0;
        Long iGroupId = 0L;
        if (gType == GGroupType.MotionGroup.getId()) {
            MotionGroup oGroup = motionGroupManager.getMotionGroupByGemsGroupId(gid);
            iGroupId = oGroup.getId();
            gemsGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(gid);
            groupNo = Integer.parseInt(oGroup.getGroupNo().toString(), 16);
        }else if (gType == GGroupType.SwitchGroup.getId()) {
            SwitchGroup oGroup = switchManager.getSwitchGroupByGemsGroupId(gid);
            iGroupId = oGroup.getId();
            gemsGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(gid);
            groupNo = Integer.parseInt(oGroup.getGroupNo().toString(), 16);
        }
        int iStatus = 0;
        if (gemsGroupFixtures != null) {
            iStatus = gemsGroupManager.removeFixturesFromGroup(gid, gemsGroupFixtures, groupNo, gType, forceDelete);
        }
        if (iStatus == 0) {
            if (gType == GGroupType.MotionGroup.getId()) {
                motionGroupManager.deleteMotionGroup(iGroupId);
                userAuditLoggerUtil.log("Deleted group: " + group.getGroupName(), UserAuditActionType.Group_Update.getName());
           }
        }
        
        Response resp = new Response();
        resp.setStatus(iStatus);
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
    @Path("op/editgroup")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editGroup(GemsGroup gemsGroup) throws SQLException, IOException {
    	GemsGroup gemsGroup1 = gemsGroupManager.loadGemsGroup(gemsGroup.getId());
    	if(gemsGroup1 != null) {
    		gemsGroup1.setGroupName(gemsGroup.getGroupName());
    	}

        gemsGroupManager.editGroup(gemsGroup1);
        
        Response oResponse = new Response();
        oResponse.setMsg("S");
        userAuditLoggerUtil.log("Edit group: " + gemsGroup.getGroupName(), UserAuditActionType.Group_Update.getName());
        return oResponse;
    }
    
	@Path("/status/{gid}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String getGroupProcessingStatus(@PathParam("gid") Long gid) {
		return gemsGroupManager.grpProcessingStatus(gid);
	}
	
	@Path("/op/deletegroup/{gid}")
	@POST
	public void deleteObsoleteGemsGroup(@PathParam("gid") Long gid) {
		gemsGroupManager.deleteGemsGroups(gid);
	}
}
