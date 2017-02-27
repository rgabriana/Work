/**
 * RESTful webservices exposed by GEMS 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.Area;
import com.ems.model.FacilityEnergyStats;
import com.ems.model.Fixture;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.AreaManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.LicenseSupportManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.utils.CommonUtils;
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
  @Resource(name = "areaManager")
  private AreaManager areaManager;
  @Autowired
  private MessageSource messageSource;
  @Resource
  private SystemConfigurationManager systemConfigurationManager;
  @Resource(name= "licenseSupportManager")
	LicenseSupportManager licenseSupportManager;
  
  @Resource(name = "emsAuthContext")
  private EmsAuthenticationContext emsAuthContext;
  
  @Resource(name = "userManager")
  private UserManager userManager;
  
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
  @Path("dimFacility/{facilityLevel}/{facilityId}/{percentage}/{time}")
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response dimFacility(@PathParam("facilityLevel") String facilityLevel, @PathParam("facilityId") Long facilityId,
  		@PathParam("percentage") Integer percentage, @PathParam("time") Integer time) {
  	
	/*Response resp = new Response();
	Map<String,Object> nameValMap = new HashMap<String,Object>();
	nameValMap.put("facilityLevel", facilityLevel);
	nameValMap.put("id", facilityId);
	nameValMap.put("intPercentage", percentage);
	nameValMap.put("time", time);
	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
	if(resp!= null && resp.getStatus()!=200){
		return resp;
	}*/
	
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("facilityEnergyStats/15min/floor/{floorId}/{fromDate}/{toDate}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<FacilityEnergyStats> getECDataByFloorId(@PathParam("floorId") Long floorId,@PathParam("fromDate") String fromDate,@PathParam("toDate") String toDate) {
      List<FacilityEnergyStats> list = null;      
      
      Response resp = new Response();
      Map<String,Object> nameValMap = new HashMap<String,Object>();
      nameValMap.put("fromDate", fromDate);
      nameValMap.put("toDate", toDate);
      resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
      if(resp!= null && resp.getStatus()!=200){
    	  logger.error("Validation error"+resp.getMsg());
    	  return null;
      }
     list = energyConsumptionManager.getECDataByFloor(DateUtil.parseString(fromDate,"yyyyMMddHHmmss"), DateUtil.parseString(toDate,"yyyyMMddHHmmss"), floorId);
      return list;
      
    }
    
    /**
  	 * Returns Occupancy State of all areas of floor
  	 * 
  	 * @param floorId
  	 * @return area object with occupancy state
  	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
  	@Path("getOccupancyStateOfFloorAreas/{floorId}")
  	@GET
  	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  	public List<Area> getOccupancyStateOfFloorAreas(@PathParam("floorId") Long floorId) {		
  		
   	  Response resp = new Response();
  	  resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "id", floorId);
    	if(resp!= null && resp.getStatus()!=200){
    	logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
    	Long currentUserId = emsAuthContext.getUserId();
        User user = userManager.loadUserById(currentUserId);
  		//List<Long> occupiedAreaList = fixtureManager.getOccupiedAreasOfFloor(floorId);
  		//List<Area> areaList = areaManager.getAllAreasByFloorId(floorId);
    	List<Object[]> objList = areaManager.getAllAreasByFloorIdWithNoOfSensors(floorId);
  		HashMap<Long, BigInteger> reachList = areaManager.getAllAreasByFloorIdWithNoOfFaultySensors(floorId);
  		Iterator<Object[]> areaIter = objList.iterator();
  		Area areaObj = null;
  		ArrayList<Area> areaList = new ArrayList<Area>();
  		while(areaIter.hasNext()) {
  			Object[] area = areaIter.next();
  			areaObj = new Area();
  			//0 index is area_id
  			//1 index is area name
  			//2 index is lighting_occ_count
  			//3 index is percentage_faulty_sensors
  			//4 index is zone_sensor_enable
  			//5 index is total no. of sensor
  			
  			areaObj.setId(((BigInteger)area[0]).longValue());
  			areaObj.setName(area[1].toString());
  			areaObj.setDescription(areaObj.getName());
  			//EM-771 : Area occupancy through BACnet should work even if Zone sensor license is not enabled
  			if(!("bacnet".equals(user.getEmail())) && (!licenseSupportManager.isZoneSensorsEnabled() || !((Boolean)area[4]).booleanValue())){
    			areaObj.setOccupancyState(-2);
    			continue;
    		}
  			
  			int percentageSensorsFaulty = 75;
  			if(area[3] != null) {
  				percentageSensorsFaulty = ((Short)area[3]).intValue();
  			}
  			
  			boolean faulty = false;
  			if(reachList.containsKey(areaObj.getId()) && area[5] != null) {
  				long totalCount = ((BigInteger)area[5]).longValue();
    			long faultyCount = reachList.get(areaObj.getId()).longValue();
    			if((100 * faultyCount/totalCount) > percentageSensorsFaulty){
    				areaObj.setOccupancyState(-1);
    				faulty = true;
    			}
  			}
  			
  			if(!faulty) {
  				if(area[2] != null && ((Short)area[2]).intValue() > 0) {
  					areaObj.setOccupancyState(1);
  				} else {
  					areaObj.setOccupancyState(0);
  				}
  			}
  			areaList.add(areaObj);
  		}		
  		int unassignedOccState = fixtureManager.getFloorUnAssignedAreaOccState(floorId);
  		Area unassignedArea = new Area();
  		unassignedArea.setId(0L);
  		unassignedArea.setName("Unassigned");
  		unassignedArea.setDescription("Unassigned Area");
  		unassignedArea.setOccupancyState(unassignedOccState);
  		areaList.add(unassignedArea);		
  		return areaList;		
  				
  	} //end of method getOccupancyStateOfFloorAreas
    
}
