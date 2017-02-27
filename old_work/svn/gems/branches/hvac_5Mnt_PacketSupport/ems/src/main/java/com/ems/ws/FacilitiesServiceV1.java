/**
 * RESTful webservices exposed by GEMS 
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

import com.ems.model.FacilityEnergyStats;
import com.ems.model.Fixture;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.utils.DateUtil;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/facility/v1")
// @RolesAllowed("admin")
public class FacilitiesServiceV1 {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

  @Resource(name = "fixtureManager")
  private FixtureManager fixtureManager;
    
  @Resource(name = "energyConsumptionManager")
  private EnergyConsumptionManager energyConsumptionManager ;

  private static final Logger logger = Logger.getLogger("WSLogger");

  public FacilitiesServiceV1() {
  }

  @Context
  UriInfo uriInfo;
  @Context
  Request request;
	
  /**
   * Dim all the fixtures of a particular facility
   * 
   * @param facility id
   *            Id of the facility
   * @param facility level
   *            Level of the facility
   * @param percentage
   * 						dim percentage
   * @param time
   * 						amount of time for which lights need to be dimmed
   * @return Response status
   */
  @Path("dimFacility/{facilityLevel}/{facilityId}/{percentage}/{time}")
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response dimFacility(@PathParam("facilityLevel") String facilityLevel, @PathParam("facilityId") Long facilityId,
  		@PathParam("percentage") Integer percentage, @PathParam("time") Integer time) {
  	
  	Response oResponse = new Response();
  	List<Fixture> fixtureList = null;
  	if (facilityLevel.equalsIgnoreCase("organization")) {
  		fixtureList = fixtureManager.loadAllFixtures();
  	} else if (facilityLevel.equalsIgnoreCase("campus")) {
  		fixtureList =  fixtureManager.loadFixtureByCampusId(facilityId);
  	} else if (facilityLevel.equalsIgnoreCase("building")) {
  		fixtureList =  fixtureManager.loadFixtureByBuildingId(facilityId);
  	} else if (facilityLevel.equalsIgnoreCase("floor")) {
  		fixtureList =  fixtureManager.loadFixtureByFloorId(facilityId);
  	} else if (facilityLevel.equalsIgnoreCase("area")) {
  		fixtureList =  fixtureManager.loadFixtureByAreaId(facilityId);
  	}
          
  	int[] fixtureArr = new int[fixtureList.size()];
  	Iterator<Fixture> itr = fixtureList.iterator();
  	int i = 0;
  	while (itr.hasNext()) {
  		Fixture fixture = (Fixture) itr.next();
  		fixtureArr[i++] = fixture.getId().intValue();       	
  	}
  	if(fixtureList.size() > 0) {
  		if(logger.isDebugEnabled()) {
  			logger.debug("dim " + facilityLevel + " by " + percentage + " for " + time + " mins");
  		}
  		fixtureManager.dimFixtures(fixtureArr, percentage, time);
  	}
  	return oResponse;
  	
  } //end of method dimFacility
  	
	/**
     * Returns the energy consumption data for a specified time for the fixture 
     * 
     * @param fromDate in the format yyyyMMddHHmmss
     * @param toDate in the format yyyyMMddHHmmss
     * @param floorId              
     * @return facilityEnergyStats
     */
    @Path("facilityEnergyStats/15min/floor/{floorId}/{fromDate}/{toDate}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<FacilityEnergyStats> getECDataByFloorId(@PathParam("floorId") Long floorId,@PathParam("fromDate") String fromDate,@PathParam("toDate") String toDate) {
      List<FacilityEnergyStats> list = null;      
     list = energyConsumptionManager.getECDataByFloor(DateUtil.parseString(fromDate,"yyyyMMddHHmmss"), DateUtil.parseString(toDate,"yyyyMMddHHmmss"), floorId);
      return list;
      
    }
    
}
