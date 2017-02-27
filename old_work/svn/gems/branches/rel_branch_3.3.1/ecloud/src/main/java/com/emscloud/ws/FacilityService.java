package com.emscloud.ws ;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.model.Facility;
import com.emscloud.model.api.DetailedProfile;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FacilityTreeManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.FacilityType;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.CommonUtils;
import com.emscloud.vo.Device;
import com.emscloud.vo.FacilityEmInfo;
import com.emscloud.vo.Fixture;
import com.emscloud.vo.Gateway;
import com.emscloud.vo.Organization;

@Controller
@Path("/org/facility/v1")
public class FacilityService {
		
	@Resource
	FacilityManager facilityManager;
	@Resource
	CustomerManager customerManager;
	
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
		
	@Resource
	CloudAdapter cloudAdapter;
	
	@Resource
	SystemConfigurationManager systemConfigurationManager;
	
	//private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public FacilityService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
			
	@Path("getFacilityTree/{custId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String getFacilityTree(@PathParam("custId") Long custId) {
				
		String facilityStr = "";
		String metaDataServerIp = CommonUtils.getMetaDataServer(systemConfigurationManager);
  	if(metaDataServerIp.equalsIgnoreCase("localhost")) {
  		Facility facility = facilityManager.loadFacilityTreeByCustomer(custId);			
  		ObjectMapper mapper = new ObjectMapper();
  		try {
  			facilityStr = mapper.writeValueAsString(facility);	
  		}
  		catch(Exception e) {
  			e.printStackTrace();
  		}
  		return facilityStr;
  	}
  	
  	String result = null;
  	try{  			
			ResponseWrapper<String> response = cloudAdapter.executeGet(metaDataServerIp, 
					"/ecloud/services/internal/v1/getFacilityTree/" + custId, MediaType.APPLICATION_JSON, String.class);  	        
			if (response.getStatus()== javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
				result = response.getItems();
			} 
		}catch(Exception e) {
			e.printStackTrace();
		}
  	return result;
		
	} //end of method getFacilityTree
	
	/**
	 * Returns fixture location list TODO: limit is currently not used, need to
	 * fix this.
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|gateway|secondarygateway|
	 *            group)
	 * @param pid
	 *            property unique identifier
	 * @param limit
	 * @return fixture list for the property level
	 */
	@Path("getFixtureList/{floorId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String getFixtures(@PathParam("floorId") Long floorId) {
		
		String metaDataServerIp = CommonUtils.getMetaDataServer(systemConfigurationManager);
		List<Fixture> fixtureList = new ArrayList<Fixture>();
  	if(metaDataServerIp.equalsIgnoreCase("localhost")) {
  		fixtureList = facilityManager.getFixtures(floorId);  		
  	} else {
  		FacilityEmInfo femInfo = null;
  		try{
  			ResponseWrapper<FacilityEmInfo> response = cloudAdapter.executeGet(metaDataServerIp, 
  					"/ecloud/services/internal/v1/getFacilityEmInfo/" + floorId, MediaType.APPLICATION_JSON, FacilityEmInfo.class);			
  			if (response.getStatus()== javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
  				femInfo = response.getItems();
  				fixtureList = facilityManager.getFixtures(femInfo.getDbName(), femInfo.getReplicaIp(), femInfo.getEmFacilityId());  				
  			} 
  		}catch(Exception e) {
  			e.printStackTrace();
  		}
  	}
  	String fixtureStr = "";
  	ObjectMapper mapper = new ObjectMapper();
  	try {
  		fixtureStr = mapper.writeValueAsString(fixtureList);
  	}
  	catch(Exception e) {
  		e.printStackTrace();
  	}
		return fixtureStr;
		
	}	 //end of method getFixtures
	
	@Path("getDeviceList/{floorId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Device> getDevices(@PathParam("floorId") Long floorId) {
						
		List<Device> deviceList = new ArrayList<Device>();
		String metaDataServerIp = CommonUtils.getMetaDataServer(systemConfigurationManager);
  	if(metaDataServerIp.equalsIgnoreCase("localhost")) {
  		deviceList = facilityManager.getDevices(floorId);
  		return deviceList;
  	}
  	
  	FacilityEmInfo femInfo = null;
  	try{  			
			ResponseWrapper<FacilityEmInfo> response = cloudAdapter.executeGet(metaDataServerIp, 
					"/ecloud/services/internal/v1/getFacilityEmInfo/" + floorId, MediaType.APPLICATION_JSON, FacilityEmInfo.class);  	        
			if (response.getStatus()== javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
				femInfo = response.getItems();
				deviceList = facilityManager.getDevices(femInfo.getDbName(), femInfo.getReplicaIp(), femInfo.getEmFacilityId());
	  		return deviceList;
			} 
		}catch(Exception e) {
			e.printStackTrace();
		}
  	return deviceList;
		
	}	 //end of method getDevices
	
	@Path("getGatewayList/{floorId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Gateway> getGateways(@PathParam("floorId") Long floorId) {
		
		List<Gateway> gwList = new ArrayList<Gateway>();
		String metaDataServerIp = CommonUtils.getMetaDataServer(systemConfigurationManager);
  	if(metaDataServerIp.equalsIgnoreCase("localhost")) {
  		gwList = facilityManager.getGateways(floorId);
  		return gwList;
  	}
  	
  	FacilityEmInfo femInfo = null;
  	try{  			
			ResponseWrapper<FacilityEmInfo> response = cloudAdapter.executeGet(metaDataServerIp, 
					"/ecloud/services/internal/v1/getFacilityEmInfo/" + floorId, MediaType.APPLICATION_JSON, FacilityEmInfo.class);  	        
			if (response.getStatus()== javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
				femInfo = response.getItems();
				gwList = facilityManager.getGateways(femInfo.getDbName(), femInfo.getReplicaIp(), femInfo.getEmFacilityId());
	  		return gwList;
			} 
		}catch(Exception e) {
			e.printStackTrace();
		}
  	return gwList;
		
	}	 //end of method getGateways
	
	@Path("getProfiles/{floorId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getProfilesByFloorId(@PathParam("floorId") Long floorId) {
		
		List<DetailedProfile> profiles = facilityManager.getProfilesByFloorId(floorId);
		String profileStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			profileStr = mapper.writeValueAsString(profiles);
			//System.out.println(profileStr);
			System.out.println("after json");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return profileStr;
		
	} //end of method getProfilesByFloorId
	
	@Path("getFloorPlan/{floorId}")
	@GET
  @Produces("image/jpeg")
	public byte[] getFloorPlanByFloorId(@PathParam("floorId") Long floorId) {
		String metaDataServerIp = CommonUtils.getMetaDataServer(systemConfigurationManager);
  	if(metaDataServerIp.equalsIgnoreCase("localhost")) {
  		return facilityManager.getFloorPlan(floorId);
  	}
  	FacilityEmInfo femInfo = null;
  	try{  			
			ResponseWrapper<FacilityEmInfo> response = cloudAdapter.executeGet(metaDataServerIp, 
					"/ecloud/services/internal/v1/getFacilityEmInfo/" + floorId, MediaType.APPLICATION_JSON, FacilityEmInfo.class);  	        
			if (response.getStatus()== javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
				femInfo = response.getItems();
				byte[] planMap = facilityManager.getFloorPlan(femInfo.getDbName(), femInfo.getReplicaIp(), femInfo.getEmFacilityId());
	  		return planMap;
			} 
		}catch(Exception e) {
			e.printStackTrace();
		}
  	return null;
  	
	}
	
	@Path("getOrganizationList")
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String getOrganizationList() {
		
		List<Organization> customerList = customerManager.getAllOrganizations();
		String customerStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			customerStr = mapper.writeValueAsString(customerList);
			//System.out.println(customerStr);
			System.out.println("after json");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return customerStr;
		
	} //end of method getOrganizationList
	
	/**
   * Sets the floor plan image for the selected floor
   *  
   * @param floorId
   * @param imageurl
   * @throws UnsupportedEncodingException 
   */
 
  @Path("updateFloorPlan/{floorId}")
  @POST
  @Consumes({"image/jpeg", "image/png"})
  public Response setFloorPlan(@PathParam("floorId") Long floorId, @RequestParam byte[] imageData) throws UnsupportedEncodingException {

  	System.out.println("length -- " + imageData.length);
  	Response resp = new Response();  	
  	facilityManager.setFloorPlan(floorId, imageData);   		
  	return resp;
  
  } //end of method setFloorPlan
  
  
  /** Returns path of the facility in the facility Tree
   * 
   * @param nodeID
   * 			facility Id
   * @return path of the facility in the facility Tree
   * 		e.g. Company name -> Campus name -> Building name -> Floor name
   */
  @Path("nodepath/{nodeId}")
  @GET
  @Produces({ MediaType.APPLICATION_OCTET_STREAM })
  public String getNodePath(@PathParam("nodeId") Long nodeID) {
      String path = facilityTreeManager.getNodePath(nodeID);        
      return path;
  }
  
  
  /**
   * Delete a facility with the specified id
   * 
   * @param facilityId
   * 			Facility Id to be deleted
   * @return 'S' if Success or 'F' if Failed to delete the facility
   */
  @Path("delete/{facilityId}")
  @POST
  @Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String deleteFacility(@PathParam("facilityId") Long facilityId) {
  	List<Facility> list = null;
		list = (List<Facility>)facilityManager.getChildFacilitiesByFacilityId(facilityId);
		
  	if(list == null || list.size() == 0) {
  		Facility facility = facilityManager.getFacility(facilityId);
  		if(FacilityType.getFacilityType(facility.getType()) == FacilityType.FLOOR){
  			if (facilityEmMappingManager.getFacilityEmMappingOnFacilityId(facilityId) != null){
  				return "FEMMAP";
  			}
  		}
  		facilityManager.deleteFacility(facilityId);
  		cloudAuditLoggerUtil.log("Deleted "+FacilityType.getFacilityType(facility.getType()).getLowerCaseName()+" with name "+ facility.getName() +" for Customer "+customerManager.loadCustomerById(facility.getCustomerId()).getName(), CloudAuditActionType.Facility_Delete.getName());
  		return "S";
  	}
  	else {
  		return "F";
  	}
  	
  }
} //end of class FacilityService
