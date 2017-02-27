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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.model.Groups;
import com.ems.model.ProfileTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.GroupManager;
import com.ems.service.ProfileTemplateManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/templateManagement")
public class TemplateMgmtService {
	
	
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "profileTemplateManager")
    private ProfileTemplateManager profileTemplateManager;
    
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public TemplateMgmtService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
	 * Returns profiles count associated with the templates
	 * 
	 * @param profiles
	 * @param pid
	 *            property unique identifier
	 * @return Response status with msg used for sending profiles count
	 */
	@Path("count/profiles/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getProfilesCount(@PathParam("pid") Long pid) {
		Response oResponse = new Response();
		oResponse.setMsg(String.valueOf(groupManager.getProfilesCount(pid)));
		return oResponse;
	}
	
    /**
     * Delete Template
     * 
     * @param id
     *            template unique identifier
     * @return Response status
     */
    @Path("delete")
    @POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteTemplate(List<ProfileTemplate> profileTemplate) {
    	
    	m_Logger.debug("Templates: " + profileTemplate.size());
   		Iterator<ProfileTemplate> itr = profileTemplate.iterator();
   		Response oResponse = new Response();
   		long templateId = 0;
   		ProfileTemplate template = new ProfileTemplate();
   		while (itr.hasNext()) {
   			template = (ProfileTemplate) itr.next();
   			templateId = template.getId();
   			int status = 0;
   			try {   				
   				status = profileTemplateManager.delete(templateId);
   			} catch (Exception e) {
   				e.printStackTrace();
   				userAuditLoggerUtil.log("Exception while deleting Profile Template: " + template.getName(), UserAuditActionType.Profile_Update.getName());
   			}
   			userAuditLoggerUtil.log("Deleted Profile Template: " + template.getName(), UserAuditActionType.Profile_Update.getName());
   			oResponse.setStatus(status);
   			oResponse.setMsg(String.valueOf(templateId)); 
   		}
   		return oResponse;
    	
    }
}
