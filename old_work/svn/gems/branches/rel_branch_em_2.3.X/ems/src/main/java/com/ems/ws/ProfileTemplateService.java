/**
 * 
 */
package com.ems.ws;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.ProfileTemplateManager;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/profiletemplate")
public class ProfileTemplateService {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	
	@Resource(name = "profileTemplateManager")
	private ProfileTemplateManager profileTemplateManager;
	

	

	public ProfileTemplateService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	@Path("duplicatecheck/{templatename}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateProfile(@PathParam("templatename") String templatename) {
		Response oStatus = new Response();
		if (profileTemplateManager
				.getProfileTemplateCountByName(templatename) > 0) {
			oStatus.setMsg(templatename);
			oStatus.setStatus(0);
			return oStatus;
					}
		
		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}

	}
