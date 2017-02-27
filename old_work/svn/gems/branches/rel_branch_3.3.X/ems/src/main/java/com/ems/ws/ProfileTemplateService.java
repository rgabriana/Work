/**
 * 
 */
package com.ems.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.ems.model.ProfileTemplate;
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
	/**
     * Check for duplicate template name.
     * 
     * @param templatename
     * @return Response object - If Duplicate, response object will return template name otherwise returns 0
     */
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
	
	/**
     * Fetch all derived profile templates (other than default 16 default templates) and return list to UEM
     * 
     * @return List of ProfileTemplate
     */
    @Path("getallderivedemtemplates")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ProfileTemplate> getAllDerivedEMTemplates() {
        return profileTemplateManager.loadAllDerivedProfileTemplate();
    }
}
