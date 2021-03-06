package com.ems.ws;

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
import com.ems.model.GemsGroupFixture;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.GemsGroupManager;
import com.ems.ws.util.Response;

@Controller
@Path("/org/gemsgroupfixture")
public class GemsGroupFixtureService {
    static final Logger logger = Logger.getLogger(GemsGroupService.class.getName());

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "gemsGroupManager")
    private GemsGroupManager gemsGroupManager;

    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;

    public GemsGroupFixtureService() {
    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Updates group for a set of fixtures
     * 
     * @param fixtures
     *            List of selected fixture with id "<fixtures><fixture><id>1</id></fixture></fixtures>"
     * @return Response status
     */
    @Path("op/applygroup/{gid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response applyGroupToFixtures(@PathParam("gid") Long gid, List<Fixture> fixtures) {
        gemsGroupManager.saveGemsGroupFixtures(gid, fixtures);
        userAuditLoggerUtil.log("Apply Group to Fixture. Group Id: " + gid);
        return new Response();
    }

    /**
     * Manage a group
     * 
     * @param fixtures
     *            List of selected fixture with id "<fixtures><fixture><id>1</id></fixture></fixtures>"
     * @return Response status
     */
    @Path("op/managegroup/{gid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response removeFixtureFromGroup(@PathParam("gid") Long gid, List<Fixture> fixtures) {
        gemsGroupManager.deleteGemsGroupFixtures(gid, fixtures);
        userAuditLoggerUtil.log("Remove Group from Fixture. Group Id: " + gid);
        return new Response();
    }

    
    @Path("op/resetallgroups")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response resetAllGroupsOnFixture(List<Fixture> fixtures) {
        gemsGroupManager.resetAllGroupOnFixtures(fixtures);
        userAuditLoggerUtil.log("Reseting all groups for selected fixtures...");

        return new Response();
    }

    /**
     * Returns fixtures list
     * 
     * @param groupid
     *            gems group unique identifier
     * @return fixture list for the gems group
     */
    @Path("list/{groupId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroupFixture> getGroupFixtureList(@PathParam("groupId") Long groupId) {
        return gemsGroupManager.getGemsGroupFixtureByGroup(groupId);
    }
}
