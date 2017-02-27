package com.ems.ws;

import java.io.IOException;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import org.quartz.JobDetail;
import org.springframework.stereotype.Controller;


import com.ems.model.SystemConfiguration;
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
	public Response saveEmailNotificationScheduler(ContactClosure contactClosure){
		Response response = new Response();
		contactClosureManager.saveContactClosure(contactClosure);
		
		contactClosureManager.createNewContactClosureSchedulerJob(contactClosure.getEnabled());
		
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
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public ContactClosure getContactClosure(){
		ContactClosure contactClosure = null;
		SystemConfiguration contactClosureConfiguration = systemConfigurationManager.loadConfigByName("contact_closure_configuration");
		if(contactClosureConfiguration!=null)
		{
			try {
				if(!"".equals(contactClosureConfiguration.getValue())){
					contactClosure = new ObjectMapper().readValue(contactClosureConfiguration.getValue(),ContactClosure.class);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return contactClosure;
	}
	
}
