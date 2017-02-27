/**
 * 
 */
package com.ems.ws;
import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BacnetHealthManager;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/bacnet")
public class BacnetHealthService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource
	BacnetHealthManager bacnetHealthManager;
	public BacnetHealthService() {

	}

	/**
	 * Get health of Bacnet service. Bacnet service polls this method which in turn returns current time stamp to denote Bacnet 
	 * is running.
	 * 
	 * @return Response object
	 * 		  
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("health")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getBacnetHealthStatus() {
		bacnetHealthManager.setLastBacnetConnectivityAt(System.currentTimeMillis());
		return new Response();
	}
	
	/**
	 * Get last connectivity of bacnet
	 * 
	 * @return timestamp in miliseconds 
	 * 		  
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("lastconnectivity")
	@GET
	@Produces({ MediaType.TEXT_PLAIN})
	public String getBacnetLastConnectivity() {
		Long lastConnectivityAt = bacnetHealthManager.getLastBacnetConnectivityAt();
		return lastConnectivityAt.toString(); 
	}
	
	/**
	 * Get bacnet current running status
	 * 
	 * @return boolean
	 *      true : if bacnet is running
	 *      false: if bacnet is not running
	 * 		  
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("getrunningstatus")
	@GET
	@Produces({ MediaType.TEXT_PLAIN})
	public String getBacnetRunningStatus () {
		return String.valueOf(bacnetHealthManager.isBacnetRunning()); 
	}
}
