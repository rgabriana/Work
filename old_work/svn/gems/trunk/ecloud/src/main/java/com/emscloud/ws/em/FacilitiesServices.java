package com.emscloud.ws.em;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.emscloud.model.Facility;
import com.emscloud.types.FacilityType;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FacilityTreeManager;



@Controller
@Path("/org/facility/")
public class FacilitiesServices {
	

	@Resource
	FacilityManager facilityManager;
	
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	
	/**
	 * Returns the floor plan image for the specified floor id
	 * 
	 * @param floorId
	 * 
	 * @return embedded image in the byte array
	 */
	@Path("getFloorPlan/{floorId}")
	@GET
	@Produces("image/jpeg")
	public byte[] getFloorPlanByFloorId(@PathParam("floorId") Long floorId) {
		return facilityManager.getFloorPlan(floorId);
	}

	/**
	 * Returns The Server Time Offset From GMT
	 * 
	 * @return the server time offset from GMT in minutes
	 */
	@Path("getServerTimeOffsetFromGMT")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getServerTimeOffsetFromGMT() {
    	DateFormat dateFormat = DateFormat.getDateTimeInstance( 
    			 DateFormat.LONG, DateFormat.LONG );
    	
    	TimeZone zone = dateFormat.getTimeZone();
    	Date d = new Date();
    	Integer offset = zone.getOffset(d.getTime())/60000;
		return offset.toString(); 
    }

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
    		if(facility.getType() == FacilityType.getFacilityType(FacilityType.FLOOR)){
    			if (facilityEmMappingManager.getFacilityEmMappingOnFacilityId(facilityId) != null){
    				return "FEMMAP";
    			}
    		}
    		facilityManager.deleteFacility(facilityId);
    		return "S";
    	}
    	else {
    		return "F";
    	}
    	
    }
    
    /**
     * Returns Em Instance Server's time OffSet from GMT
     * @param pid
     * 			facility Id
     * 	
     * @return Em Instance Server's time OffSet from GMT in String format.
     */
    @Path("getEMInstanceServerTimeOffsetFromGMT/{pid}")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getEMInstanceServerTimeOffsetFromGMT(@PathParam("pid") Long pid) {
    	return facilityManager.getEMInstanceServerTimeOffsetFromGMT(pid);
    }
}
