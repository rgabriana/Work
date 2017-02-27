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

import org.springframework.stereotype.Controller;

import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionGroup;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.ws.util.Response;

@Controller
@Path("/org/motiongroup")
public class MotionGroupService {
	
    @Resource
    GemsGroupManager gemsGroupManager;
    @Resource
    MotionGroupManager motionGroupManager;
	
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
                } else {
                    currentGroupFixturesId.remove(f.getId());
                }
            }
            
            for (Long fixtureId : currentGroupFixturesId) {
                gemsGroupManager.removeGroupFixture(gemsGroupId, fixtureId);
            }
    	}
        Response response = new Response();
        response.setMsg("S");
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

}
