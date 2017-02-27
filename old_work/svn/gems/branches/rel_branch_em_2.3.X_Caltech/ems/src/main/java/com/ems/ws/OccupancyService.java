/**
 * 
 */
package com.ems.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.ems.server.device.DeviceServiceImpl;

/**
 * @author sreedhar.kamishetti
 *
 */
@Controller
@Path("/org/occupancy")
public class OccupancyService {

	@Context
  UriInfo uriInfo;
  @Context
  Request request;
  
	/**
	 * 
	 */  
	public OccupancyService() {
		// TODO Auto-generated constructor stub
	}
	
	@Path("configureHB/{mac}/{hPeriod}")
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void configureHeartbeat(@PathParam("mac") String mac, @PathParam("hPeriod") int hbPeriod) {
		
		System.out.println(mac + ": configure heart beat ws - " + hbPeriod);
		DeviceServiceImpl.getInstance().configureHeartbeat(mac, hbPeriod);
		
	} //end of method configureHeartbeat
	
	@Path("designateRGL/{mac}/{enableDisable}")
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void designateRGL(@PathParam("mac") String mac, @PathParam("enableDisable") byte enableDisable) {
		
		System.out.println(mac + ": designate RGL");
		DeviceServiceImpl.getInstance().designateRGL(mac, enableDisable);
		
	} //end of method designateRGL
	
	@Path("dimFixture/{mac}/{percentage}/{period}")
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void dimFixture(@PathParam("mac") String mac,  @PathParam("percentage") int percentage, @PathParam("period") int period) {
		 
		DeviceServiceImpl.getInstance().absoluteDimFixture(mac, percentage, period);
		
	} //end of method dimFixture

} //end of class OccupancyService
