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
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GemsGroupManager;
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
     * @return Response status
     */
    @Path("op/deletegroup/{gid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteGroup(@PathParam("gid") Long gid) {
        GemsGroup group = gemsGroupManager.loadGemsGroup(gid);
        //GemsGroupType gemsGroupType = group.getType();
        List<GemsGroupFixture> gemsGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(gid);
        
        List<Fixture> fixtures = new ArrayList<Fixture>();
        if (gemsGroupFixtures != null){    
            for(int i=0;i<gemsGroupFixtures.size();i++){
                fixtures.add(gemsGroupFixtures.get(i).getFixture());
            }
        }
        
        gemsGroupManager.deleteGemsGroup(gid,fixtures);
        userAuditLoggerUtil.log("Deleted group: " + group.getGroupName(), UserAuditActionType.Group_Update.getName());
        return new Response();
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
}
