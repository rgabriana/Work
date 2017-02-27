package com.emscloud.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FacilityEmMappingList;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FacilityTreeManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.util.CloudAuditLoggerUtil;

@Controller
@Path("/org/facilityemmap/")
public class FacilityEmMappingService {
	
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	
	@Resource
	FacilityTreeManager facilityTreeManager;

	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	EmInstanceManager emInstanceManager;
	
	@Resource
	FacilityManager facilityManager;

	/**
	 * Returns Response Object
	 * 
	 * @param emInstId
	 *           em Instance id 
	 * @param emFacilityId
	 *           Facility id of the Em Instance
	 * @param emFacilityPath
	 * 			 Facility path of the Em Instance Facility
	 * @param cloudFacilityId
	 * 			 Facility id of the Customer in Cloud
	 * @return Response Object
	 */
	@Path("emInst/{emInstId}/emFacility/{emFacilityId}/emFacilityPath/{emFacilityPath}/cloud/{cloudFacilityId}/customerId/{customerId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveEmMapping(@PathParam("emInstId") Long emInstId,@PathParam("emFacilityId") Long emFacilityId,
			@PathParam("emFacilityPath") String emFacilityPath,@PathParam("cloudFacilityId") Long cloudFacilityId,@PathParam("customerId") Long customerId) {
		Response response = facilityEmMappingManager.saveEmMapping(emInstId, emFacilityId, emFacilityPath,cloudFacilityId,customerId);
		cloudAuditLoggerUtil.log("Em Instance Mapping added for Em Instance (Mac Id:"+emInstanceManager.loadEmInstanceById(emInstId).getMacId()+" Facility Path:"
				+emFacilityPath+") to Customer (Name:"+customerManager.loadCustomerById(customerId).getName()+" Facility Path:"
				+facilityTreeManager.getNodePath(cloudFacilityId)+")", CloudAuditActionType.Cloud_Em_Instance_Mapping.getName());
		return response;
	}
	
	
	/**
	 * Fetch FacilityEmMapping list with pagination support
	 * 
	 * @param orderby
	 *            sort by column name
	 * @param orderway
	 *            (asc or desc)
	 * @return filtered FacilityEmMapping list
	 */
	@Path("listCustomerCloudEmInstanceMapping/{customerId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FacilityEmMappingList loadFacilityEmMappingListByCustomerId(@PathParam("customerId") Long customerId,
    		@RequestParam("data") String userdata) throws UnsupportedEncodingException,ParseException {
		
		String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		//long total, records = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;
		String searchField = null;
		String searchString = null;
		String searchOper = "cn";
		Boolean bSearch = false;
		
		
		String[] params = null;

		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("userData")) {
					query = URLDecoder.decode(keyval[1], "UTF-8");
					output.append("\"" + keyval[0] + "\": \"" + query + "\"");
					params = query.split("#");
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
			}
		}
		
		
		if (params != null && params.length > 0) {
			if (params[1] != null && !"".equals(params[1])) {
				searchField = params[1];
			} else {
				searchField = null;
			}

			if (params[2] != null && !"".equals(params[2])) {
				searchString = URLDecoder.decode(params[2], "UTF-8");
			} else {
				searchString = null;
			}
			
			if (params[3] != null && !"".equals(params[3])) {
				bSearch = true;
			} else {
				bSearch = false;
			}

		}
    	
    	FacilityEmMappingList oFacilityEmMappingList = facilityEmMappingManager.loadFacilityEmMappingListByCustomerId(orderBy,orderWay, 
    			bSearch, searchField, searchString,searchOper,(page - 1) * FacilityEmMappingList.DEFAULT_ROWS, FacilityEmMappingList.DEFAULT_ROWS ,customerId);
    	
    	oFacilityEmMappingList.setPage(page);
		List<FacilityEmMapping> facilityEmMappingList = oFacilityEmMappingList.getFacilityEminsts();
		if(facilityEmMappingList ==null || facilityEmMappingList.isEmpty()){
			facilityEmMappingList = new ArrayList<FacilityEmMapping>();
		}
		for(FacilityEmMapping facilityEmMapping : facilityEmMappingList){
			String cloudFacilityNodePath = facilityTreeManager.getNodePath(facilityEmMapping.getFacilityId());
			facilityEmMapping.setCloudFacilityNodePath(cloudFacilityNodePath);
		}
		oFacilityEmMappingList.setFacilityEminsts(facilityEmMappingList);
		return oFacilityEmMappingList;
    }
	
	/**
     * Delete FacilityEmMapping
     * 
     * @param id
     *            FacilityEmMapping unique identifier
     * @return Response status
     */
    @Path("delete/{id}")
    @GET
    public Response deleteFacilityEmMapping(@PathParam("id") long id) {
	    Response response = new Response();
	    FacilityEmMapping facilityEmMapping = facilityEmMappingManager.getFacilityEmMapping(id);
	    facilityEmMappingManager.deleteFacilityEmMapping(id);
	    cloudAuditLoggerUtil.log("Em Instance Mapping deleted for Em Instance (Mac Id:"+emInstanceManager.loadEmInstanceById(facilityEmMapping.getEmId()).getMacId()+" Facility Path:"
				+facilityEmMapping.getEmFacilityPath()+") to Customer (Name:"+customerManager.loadCustomerById(facilityEmMapping.getCustId()).getName()+" Facility Path:"
				+facilityTreeManager.getNodePath(facilityEmMapping.getFacilityId())+")", CloudAuditActionType.Cloud_Em_Instance_Mapping.getName());	    
	    return response;
    }
	
}
