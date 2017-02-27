package com.emscloud.ws.em;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.service.FixtureManager;
import com.emscloud.communication.vos.Fixture;
import com.emscloud.vo.FixtureList;
import com.emscloud.util.Response;



@Controller
@Path("/org/fixture/")
public class FixtureService {
	

	@Resource
	FixtureManager fixtureManager;
	
	
	/**
	 * Returns fixture list
	 * 
	 * @param property
	 *            (company|campus|building|floor|gateway|secondarygateway|group)
	 * @param pid
	 *            property unique identifier
	 * @return fixture list for the property level
	 */
	@Path("list/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureList(@PathParam("property") String property,
			@PathParam("pid") Long pid) {
		return fixtureManager.getFixtureList(property,pid);
	}
	
	/**
     * Fetch fixture list (only selected attributes) with pagination support at organization level
     * 
     * @param property
     *            (company|campus|building|floor|area|group)
     * @param pid
     *            property unique identifier
     * @param page
     *            page no.
     * @param orderby
     *            sort by columnname
     * @param orderway
     *            (asc or desc)
     * @param bSearch
     *            (true or false)
     * @param searchField
     *            search by column field
     * @param searchString
     *            search pattern
     * @param searchOper
     *            (eq: equals, cn: like)
     * @return filtered Fixture list
     */
    @Path("list/alternate/filter/{property}/{pid}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FixtureList loadFixtureListWithSpecificAttrs(@RequestParam("data") String userdata,@PathParam("property") String property,
            @PathParam("pid") Long pid) {
        int page=0;
        String[] input = userdata.split("&");
        if (input != null && input.length > 0) {
            for (String each : input) {
                String[] keyval = each.split("=", 2);
                if (keyval[0].equals("page")) {
                    page = Integer.parseInt(keyval[1]);
                }
            }
        }
        FixtureList oFixtureList = fixtureManager.loadFixtureListWithSpecificAttrs(userdata,property,pid);
        if(oFixtureList!=null)
        {
            oFixtureList.setPage(page);
        }
        return oFixtureList;
    }
	/**
	 * Allows selected set of fixture to be dimmed or brightened from the
	 * floorplan
	 * 
	 * @param mode
	 *            (rel | abs)
	 * @param percentage
	 *            {(-100 | 0 | 100) for rel} AND {(0 | 100) for abs}
	 * @param time
	 *            minutes
	 * @param pid
	 * 			  facility Id
	 * @param fixtures
	 *            list of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return response
	 */
	@Path("op/dim/{mode}/{percentage}/{time}/{pid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixture(@PathParam("mode") String mode,
			@PathParam("percentage") String percentage,
			@PathParam("time") String time, @PathParam("pid") Long pid, List<Fixture> fixtures) {
		fixtureManager.dimFixtures(mode, percentage, time, pid, fixtures);
		return new Response();
	}
	
	/**
	 * Place the selected fixtures in specified mode.
	 * 
	 * @param modetype
	 *            String {AUTO|BASELINE|BYPASS}
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("op/mode/{modetype}/{pid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyModeToFixtures(@PathParam("modetype") String modetype,
			@PathParam("pid") Long pid, List<Fixture> fixtures) {
		
		Response oStatus = new Response();
		fixtureManager.applyModeToFixtures(modetype,pid,fixtures);
		return oStatus;
	}
	
	/**
	 * Returns Fixture Details
	 * 
	 * @param fid
	 *            fixture unique identifier
	 *  @param pid
	 *  		facility Id          
	 * @return fixture details
	 */
	@Path("details/{fid}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Fixture getFixtureDetails(@PathParam("fid") Long fid,@PathParam("pid") Long pid) {
		return fixtureManager.getFixtureDetails(fid,pid);
	}
	
	/**
	 * Sends a realtime command to selected fixtures.
	 * @param pid
	 *            facility Id
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("op/realtime/{pid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureRealTimeStats(@PathParam("pid") Long pid,List<Fixture> fixtures) {
		return fixtureManager.getFixtureRealTimeStats(pid, fixtures);
	}

}
