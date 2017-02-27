package com.ems.ws;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import org.quartz.JobDetail;
import org.springframework.stereotype.Controller;


import com.ems.service.ContactClosureManager;

import com.ems.service.SystemConfigurationManager;
import com.ems.vo.ContactClosure;

import com.ems.ws.util.Response;

@Controller
@Path("/org/contactclosure")
public class ContactClosureService {
	
	private static final Logger m_Logger = Logger.getLogger("SysLog");
	
	@Resource(name = "systemConfigurationManager")
   	SystemConfigurationManager systemConfigurationManager ;
	
	@Resource(name = "contactClosureManager")
	ContactClosureManager contactClosureManager;
	   
	JobDetail contactClosureSchedulerJob;
	
	@Path("saveContactClosure")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveContactClosureScheduler(ContactClosure contactClosure){
		Response response = new Response();
		contactClosureManager.saveContactClosure(contactClosure);
		
		//contactClosureManager.createNewContactClosureSchedulerJob(contactClosure.getEnabled());
		
		return response;
	}

	@Path("discover")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response startDiscovery(){
		Response response = new Response();
		contactClosureManager.sendDiscoveryCmd();
		return response;
	}
	
	@Path("getcontactclosure")
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public ContactClosure getContactClosureList() {
		return contactClosureManager.getCCDataFromDB();
	}
	
	@Path("enabledisablecontactclosure")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response enableDisableContactClosure(ContactClosure contactClosure){
		Response response = new Response();
		contactClosureManager.enableDisableContactClosure(contactClosure.getEnabled());
		
		contactClosureManager.createNewContactClosureSchedulerJob(contactClosure.getEnabled());
		
		return response;
	}
		
	@Path("removecontactclosure/{macAddress}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String removecontactClosure(@PathParam("macAddress") String macAddress) {
		Boolean retVal = contactClosureManager.removeContactClosure(macAddress); 
		return retVal.toString();
	}
	
}
