package com.ems.ws;
import java.io.UnsupportedEncodingException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.FacilityEnergyStats;
import com.ems.model.Floor;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FacilitiesManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.utils.DateUtil;
import com.ems.vo.model.BuildingInfo;
import com.ems.vo.model.CampusInfo;
import com.ems.vo.model.FloorInfo;
import com.ems.vo.model.OrganizationInfo;
import com.ems.ws.util.Response;

import org.apache.log4j.Logger;
/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/facility/v2")
// @RolesAllowed("admin")
public class FacilitiesServiceV2 {
	
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
  private static final Logger logger = Logger.getLogger("WSLogger");
  
  @Resource
 	private CompanyManager companyManager;
     
  @Resource
 	private CampusManager campusManager;
 	
  @Resource
 	private BuildingManager buildingManager;
 	
  @Resource
	private FacilitiesManager facilitiesManager;

  public FacilitiesServiceV2() {
  }

  @Context
  UriInfo uriInfo;
  @Context
  Request request;
  
  /**
   * Returns the energy consumption data for a specified time for the fixture 
   * 
   * @param fromDate in the format yyyyMMddHHmmss
   * @param toDate in the format yyyyMMddHHmmss
   * @param floorId              
   * @return facilityEnergyStats
   */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
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
   
   if (list != null && !list.isEmpty()) {
		for (final FacilityEnergyStats ses : list){
			if (ses != null){
				ses.setOccCount(null);
			}
		}
	}
    return list;
  }
	
	/* *
 	 * Sends Fixture placement information
 	 * 
 	 * @param fixturePlacementInfo
 	 *            List of fixturePlacementInfo
 	 * @return Response status
 	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
 	@Path("updateOrgFacility")
 	@POST
 	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_XML })
 	public Response updateOrgFacility(OrganizationInfo orgInfo) {
	
		Response response = null;
		try {
			response = facilitiesManager.updateOrgFacility(orgInfo);
		} catch (Exception e) {
			String message = "Problem uploading EmConfig file";
			logger.error(message, e) ;
			response = new Response();
			response.setMsg(message);
			response.setStatus(1);
		}
		return response;
		
 	} //end of method updateOrgFacility

}
