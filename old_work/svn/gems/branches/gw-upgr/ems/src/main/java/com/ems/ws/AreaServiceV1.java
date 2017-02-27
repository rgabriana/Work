/**
 * 
 */
package com.ems.ws;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.Area;
import com.ems.model.Fixture;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.occengine.OccupancyEngine;
import com.ems.service.AreaManager;
import com.ems.service.EMManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.LicenseSupportManager;
import com.ems.service.PlugloadManager;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.vo.AreaOutage;
import com.ems.vo.EMPowerConsumption;
import com.ems.vo.OccupancyStatus;
import com.ems.vo.model.FixtureOutageVO;
import com.ems.ws.util.Response;

/**
 * @author sreedhar.kamishetti
 *
 */
@Controller
@Path("/org/area/v1")
public class AreaServiceV1 {
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	@Resource(name = "floorManager")
	private FloorManager floorManager;
	@Resource(name = "areaManager")
    private AreaManager areaManager;
	@Resource(name = "emManager")
	EMManager emManager;
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name="fixtureManager")
	FixtureManager fixtureManager;

	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;
	
	@Resource(name= "licenseSupportManager")
	LicenseSupportManager licenseSupportManager;
	public AreaServiceV1() {
	}
	
	/**
	 * Allows plugload to be turned off/on
	 * 
	 * @param status
	 *            {(0 | 1) for abs}
	 * @param plugload_id
	 *            unique plugload identifier
	 * @return response
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("setPlugloadStatus/{plugload_id}/{Status}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response turnOnOffPlugloads(@PathParam("plugload_id") Long plugload_id, @PathParam("Status") Long Status) {
		Response res = new Response();
		int status = Status.intValue();
		if(status == 0 || status==1)
		{
			int time = 60; //Minutes
			m_Logger.debug("Status: " + Status + ", Plugload: " + plugload_id);
			int[] plugloadList = new int[1];
			int count = 0;
			plugloadList[count++] = plugload_id.intValue();		
			plugloadManager.turnOnOffPlugloads(plugloadList,status , time);
			userAuditLoggerUtil.log("Dimming plugload " + plugload_id + " to " + Status + " for " + time + " minutes", 
					UserAuditActionType.Plugload_Dimming.getName());
		}else
		{
			res.setStatus(1);
		}
		return res;
		
	} //end of method turnOnOffPlugloads	
	
	/**
     * @param floorid
     * @return area list for the floor id
     */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
    @Path("list/{floorid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Area> getAreaList(@PathParam("floorid") Long floorid) {
    	List<Area> areaList = areaManager.getAllAreasByFloorId(floorid);    	
      return areaList;
    }
    
    /** The API returns the current energy consumption of the area.
	 * 
	 * @param	area_id (id of the area which is returned in the Get All Areas API)
	 * @return EMEnergyConsumption - The response is a JSON element with the energy-lighting and energy-plugload paramaters
	 * 		energy-lighting : current level of energy consumption by lighting fixtures in the area in Wh. 
	 * 		energy-plugload : current level of energy consumption by plugload fixtures in the area in Wh.  
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("energy/{area_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public EMPowerConsumption getEMEnergyConsumptionByArea(@PathParam("area_id") Long area_id) {
		String property = "area_id";
		EMPowerConsumption res = emManager.getEMEnergyConsumption(area_id,property);
		return res;
		
	} //end of method getEMEnergyConsumptionByArea	
	
	 /**
  	 *	The API returns the current occupancy status of the area.
  	 * 
  	 * @param area_id 
  	 * 			(id of the area which is returned in the Get All Areas API)
  	 * @return occupancyState - current occupancy status. Possible values are:
	 *			0: not occupied
	 *			1: occupied
	 *		   -1: initial state 
  	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
  	@Path("occ/{area_id}")
  	@GET
  	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  	public OccupancyStatus getOccupancyStateOfArea(@PathParam("area_id") Long area_id) {	
  		OccupancyStatus occStatus = new OccupancyStatus();
  		byte occState =-1;
  		if(licenseSupportManager.isZoneSensorsEnabled()){
  			occState =  OccupancyEngine.getInstance().getOccupancyStateByZoneId(area_id);
  		}
  		occStatus.setOccupancyState(Byte.toString(occState));
  		return occStatus;		
  		
  	} //end of method getOccupancyStateOfArea
  	
  	
  	/** This API allows setting the lighting level of the sensors in the area to 100% for handling emergency situations
	 * 		
	 * @param time
     *            Emergency duration. Default time is set to 60 minutes (Optional)
     * @param  area_id
     * 			  unique identifier of the area
	 * @return Response
	 * 			 0- Success
	 * 			 1- Failure
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("setEmergency/{area_id}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response setEMEmergency(@PathParam("area_id") Long area_id,@QueryParam("time") Integer time) {
		Response res = new Response();
		if(time == null) {
			time = 60;
		}
		List<Fixture> fixtures = fixtureManager.loadFixtureByAreaId(area_id);
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
		}
		fixtureManager.absoluteDimFixtures(fixtureList, 100, time);
		return res;
		
	} //end of method setEMEmergency	
	
	/** The API returns the total number of fixtures and the number of fixtures which are out in an area
	 * 		
     * @param  area_id
     * 			  id of the area which is returned in the Get All Areas API
	 * @return Response
	 * 			 totalSensors - total number of sensors in the area
	 * 			 outSensors - number of sensors reporting outage in the area
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("out/{area_id}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public AreaOutage getAreaOutage(@PathParam("area_id") Long area_id) {
		AreaOutage res = new AreaOutage();
		List<Fixture> fixtureListInArea = fixtureManager.loadFixtureByAreaId(area_id);
		List<FixtureOutageVO> fixtureOutageList = fixtureManager.getFixtureOutageList(FacilityType.AREA.getName(),area_id);
		if(fixtureListInArea!=null && fixtureListInArea.size()>0)
			res.setTotalSensors(fixtureListInArea.size());
		if(fixtureOutageList!=null && fixtureOutageList.size()>0)
			res.setOutSensors(fixtureOutageList.size());
		return res;
	} //end of method setEMEmergency	
} // end of class AreaServiceV1
