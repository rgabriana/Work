package com.emscloud.ws ;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.model.Facility;
import com.emscloud.model.api.DetailedProfile;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.vo.Fixture;
import com.emscloud.vo.Organization;

import org.codehaus.jackson.map.ObjectMapper;

@Controller
@Path("/org/facility")
public class FacilityService {
		
	@Resource
	FacilityManager facilityManager;
	@Resource
	CustomerManager customerManager;
	
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
				
		Facility facility = facilityManager.loadFacilityById(1);			
		String facilityStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			facilityStr = mapper.writeValueAsString(facility);
			System.out.println(facilityStr);
			System.out.println("after json");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return facilityStr;
		
	} //end of method getFacilityTree
	
	@Path("getFacilityTreeXml/{custId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Facility getFacilityTreeXml(@PathParam("custId") Long custId) {
				
		Facility facility = facilityManager.loadFacilityTreeByCustomer(custId);			
		return facility;
		
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
		
		List<Fixture> fixtureList = facilityManager.getFixtures(floorId);
		String fixtureStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			fixtureStr = mapper.writeValueAsString(fixtureList);
			System.out.println(fixtureStr);
			System.out.println("after json");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return fixtureStr;
		
	}	 //end of method getFixtures
	
	@Path("getProfiles/{floorId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getProfilesByFloorId(@PathParam("floorId") Long floorId) {
		
		List<DetailedProfile> profiles = facilityManager.getProfilesByFloorId(floorId);
		String profileStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			profileStr = mapper.writeValueAsString(profiles);
			System.out.println(profileStr);
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
		return facilityManager.getFloorPlan(floorId);
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
			System.out.println(customerStr);
			System.out.println("after json");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return customerStr;
		
	} //end of method getOrganizationList
	
} //end of class EcService
