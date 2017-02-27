package com.ems.ws;

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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.Plugload;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.EMManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.PlugloadManager;
import com.ems.vo.PlugloadPower;

@Controller
@Path("/org/plugload/v1")
public class PlugloadServiceV1 {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;
	@Resource
	private CampusManager campusManager;
	@Resource
	private BuildingManager buildingManager;
	@Resource
	private FloorManager floorManager;

	@Resource(name="emManager")
	EMManager emManager;
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@Resource(name="gatewayManager")
	GatewayManager gatewayManager;

	/**
	 * Returns Plugload Details
	 * 
	 * @param fid
	 *            Plugload unique identifier
	 * @return Plugload details
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("details/{plugload_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Plugload getPlugloadDetails(@PathParam("plugload_id") Long plugload_id) {
		Plugload plugload = plugloadManager.getPlugloadById(plugload_id);
		return plugload;
	}
	
	/**
	 * Returns plugload list
	 * 
	 * @param property
	 *            (floor|area)
	 * @param pid
	 *            property unique identifier
	 * @param limit
	 * @return plugload list for the property level
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("location/list/{property}/{floor_id}/1")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Plugload> loadAllPlugLoad(@PathParam("property") String property, @PathParam("floor_id") Long floor_id) {
		if (property.equalsIgnoreCase("floor")) {
			return plugloadManager.loadPlugloadByFloorId(floor_id);
		} else if (property.equalsIgnoreCase("area")) {
			return plugloadManager.loadPlugloadByAreaId(floor_id);
		}
		return null;
	}
	
	
	/** The API returns the current energy consumed by the plugload
	 * @param  plugload_id
	 * 		   unique identifier of the plugload  
	 * @return PlugloadEnergy - The response is a JSON element with the energy parameter
	 * 			managed-energy - current level of managed energy consumption (in Wh) 
	 *			unmanaged-energy - current level of unmanaged energy consumption (in Wh)
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("energy/{plugload_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public PlugloadPower getPlugloadEnergyConsumption( @PathParam("plugload_id") Long plugload_id) {
		String property = "id";
		PlugloadPower res = emManager.getPlugloadEnergyConsumption(plugload_id,property);
		return res;
		
	} //end of method getFixtureEnergyConsumption	
	
	
	/** The API returns the current energy consumed by the fixture.
	 * 
	 * @param	area_id 
	 * 			unique identifier of the area
	 * @return PlugloadEnergy - The response is a JSON element with the energy parameter
	 * 			managed-energy - current level of managed energy consumption (in Wh) 
	 *			unmanaged-energy - current level of unmanaged energy consumption (in Wh)
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("energy/area/{area_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public PlugloadPower getPlugloadEnergyConsumptionByArea(@PathParam("area_id") Long area_id) {
		String property = "area_id";
		PlugloadPower res = emManager.getPlugloadEnergyConsumption(area_id,property);
		return res;
		
	} //end of method getFixtureEnergyConsumptionByArea
}
