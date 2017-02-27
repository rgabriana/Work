/**
 * 
 */
package com.ems.ws;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.Fixture;
import com.ems.model.Plugload;
import com.ems.service.EMManager;
import com.ems.service.FixtureManager;
import com.ems.service.PlugloadManager;
import com.ems.vo.EMPowerConsumption;
import com.ems.ws.util.Response;

/**
 * @author sreedhar.kamishetti
 *
 */
@Controller
@Path("/org/em/v1")
public class EMServiceV1 {
	@Resource(name = "emManager")
	EMManager emManager;
	@Resource(name = "fixtureManager")
	FixtureManager fixtureManager;
	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;
	public EMServiceV1() {
	}
	/** The API returns the current energy consumption of all the fixtures and plugloads managed by the Energy Manager
	 * 
	 * @return EMEnergyConsumption - The response is a JSON element with the energy-lighting and energy-plugload paramaters
	 * 		energy-lighting : current level of energy consumption by all the lighting fixtures in the area in Wh
	 * 		energy-plugload : current level of energy consumption by all the plugload fixtures in the area in Wh 
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("energy")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public EMPowerConsumption getEMEnergyConsumption() {
		String property = "company_id";
		Long pid = 0l;
		EMPowerConsumption res = emManager.getEMEnergyConsumption(pid,property);
		return res;
		
	} //end of method getEMEnergyConsumption	
	
	
	
	/** Set Energy Manager Emergency
	 * 		This API allows setting the lighting level of all the fixtures and plugloads managed by the Energy Manager 
	 * to 100% for handling emergency situations
	 * @param time
   *            Emergency duration. Default time is set to 60 minutes (Optional)
	 * @return Response
	 * 			 0- Success
	 * 			 1- Failure
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("setEmergency")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response setEMEmergency(@QueryParam("time") Integer time) {
		Response res = emManager.setEmergencyOn(time);
		return res;
		
	} //end of method setEMEmergency	
	
} // end of class EMServiceV1
