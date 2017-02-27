/**
 * 
 */
package com.ems.ws;

import java.util.Iterator;
import java.util.List;

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

import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/profile")
public class ProfileService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    @Resource(name = "groupManager")
    private GroupManager groupManager;

    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public ProfileService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Assign new profile to the list of selected fixture(s)
     * 
     * @param currentProfile
     *            Name of the selected profile
     * @param originalProfile
     *            Name of the original profile
     * @param groupId
     *            Id of the selected group to be assigned
     * @param fixture
     *            fixture "<fixture><id>1</id></fixture>"
     * @return Response status
     */
    @Path("assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response assignProfile(@PathParam("currentprofile") String currentProfile,
            @PathParam("originalprofile") String originalProfile, @PathParam("groupid") Long groupId, Fixture fixture) {
        Response oStatus = new Response();
        if (fixture != null) {
            m_Logger.debug("Assinging profile: '" + currentProfile + "' to (" + fixture.getId() + ") fixtures");
            fixtureManager.changeFixtureProfile(fixture.getId(), groupId, currentProfile, originalProfile);
        }
        userAuditLoggerUtil.log("Assign Profile for Fixture Id : " + fixture.getId());
        return oStatus;
    }

    /**
     * Get the list of all the groups in system
     * 
     * @return Groups list
     */
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Groups> getAllProfiles() {
        return groupManager.loadAllGroups();
    }
}
