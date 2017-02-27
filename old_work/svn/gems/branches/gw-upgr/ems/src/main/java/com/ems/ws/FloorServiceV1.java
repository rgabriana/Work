/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.service.FloorManager;

/**
 * @author sreedhar.kamishetti
 *
 */
@Controller
@Path("/org/floor/v1")
public class FloorServiceV1 {

	@Resource(name = "floorManager")
  private FloorManager floorManager;
	
	private static final Logger logger = Logger.getLogger("WSLogger");
	
	/**
	 * 
	 */
	public FloorServiceV1() {
		// TODO Auto-generated constructor stub
	}
	
	/**
   * @return floor list with all the floors on the energy manager .
   */
  @SuppressWarnings("unchecked")
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
  @Path("list")
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public List<com.ems.vo.model.Floor> getFloorList() {
    
    ArrayList<com.ems.vo.model.Floor> floorList = new ArrayList<com.ems.vo.model.Floor>();
    try {
    	List<Object[]> results = floorManager.getAllFloorsOfCompanyWithNames();
    	if(results == null) {
    		return floorList;
    	}
    	Iterator<Object[]> resultsIter = results.iterator();
    	com.ems.vo.model.Floor floor = null;
    	while(resultsIter.hasNext()) {
    		Object[] data = resultsIter.next();
    		floor = new com.ems.vo.model.Floor();
    		floor.setId((BigInteger)data[0]);
    		floor.setName(data[1].toString());
    		floor.setBuildingName(data[2].toString());
    		floor.setCampusName(data[3].toString());
    		floor.setOrganizationName(data[4].toString());
    		floor.setDescription(data[5].toString());
    		if(data[6] != null) {
    			floor.setFloorPlanUrl(data[6].toString());
    		}
    		floorList.add(floor);
    	}
    } catch (SQLException e) {
    	logger.error(e.getMessage());
    } catch (IOException e) {
    	logger.error(e.getMessage());
    }
    return floorList;
    
  } //end of method getFloorList

} // end of class FloorServiceV1
