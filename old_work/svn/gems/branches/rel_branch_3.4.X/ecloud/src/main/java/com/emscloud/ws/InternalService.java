package com.emscloud.ws;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;

import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.vo.Site;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.SiteManager;
import com.emscloud.vo.Device;
import com.emscloud.vo.FacilityEmInfo;
import com.emscloud.vo.Fixture;
import com.emscloud.vo.Gateway;

@Controller
@Path("/internal/v1")
public class InternalService {
	
	private static final Logger logger = Logger.getLogger("WSAPI");

	@Resource
	SiteManager siteManager;

	@Resource
	FacilityManager facilityManager;
	@Resource
	FacilityEmMappingManager femMgr;
	@Resource
	EmInstanceManager emInstMgr;
	
	@Path("getSitesOfCustomer/{custId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Site> getSitesOfCustomer(@PathParam("custId") Long custId) throws UnsupportedEncodingException {
				
		System.out.println("inside the internal service of getSitesOfCustomer " + custId);
		List<Site> siteList = siteManager.loadSiteFacilitiesByCustomer(custId);		
		return siteList;
      
	} //end of method getSitesOfCustomer
    
	@Path("getFacilityTree/{custId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String getFacilityTree(@PathParam("custId") Long custId) {
				
		Facility facility = facilityManager.loadFacilityTreeByCustomer(custId);			
		String facilityStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			facilityStr = mapper.writeValueAsString(facility);
			//System.out.println(facilityStr);
			System.out.println("after json");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return facilityStr;
		
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
			//System.out.println(fixtureStr);
			System.out.println("after json");
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
		
		List<Device> deviceList = facilityManager.getDevices(floorId);
		return deviceList;
		
	}	 //end of method getDevices
	
	@Path("getGatewayList/{floorId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Gateway> getGateways(@PathParam("floorId") Long floorId) {
		
		List<Gateway> gwList = facilityManager.getGateways(floorId);
		return gwList;
		
	}	 //end of method getGateways
		
	@Path("getFacilityEmInfo/{facilityId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FacilityEmInfo getFacilityEmInfo(@PathParam("facilityId") Long facilityId) {
		
		FacilityEmMapping fem = femMgr.getFacilityEmMappingOnFacilityId(facilityId);
		if(fem == null) {
			return null;
		}
		EmInstance emInst = emInstMgr.loadEmInstanceById(fem.getEmId());
		FacilityEmInfo femInfo = new FacilityEmInfo();
		if(emInst != null){
			
			femInfo.setDbName(emInst.getDatabaseName());
			femInfo.setEmFacilityId(fem.getEmFacilityId());
			femInfo.setEmId(fem.getEmId());
			femInfo.setFacilityId(facilityId);
			femInfo.setReplicaIp(emInst.getReplicaServer().getInternalIp());
			return femInfo;
		}
		return femInfo;
		
	} //end of method getFacilityEmInfo
	
} //end of class InternalService
