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
import com.ems.model.PlugloadProfileTemplate;
import com.ems.model.ProfileTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.GroupManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadProfileTemplateManager;
import com.ems.service.ProfileTemplateManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/plugloadTemplateManagement")
public class PlugloadTemplateMgmtService {
	
	
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "plugloadGroupManager")
    private PlugloadGroupManager plugloadGroupManager;
    
    @Resource(name = "plugloadProfileTemplateManager")
    private PlugloadProfileTemplateManager plugloadProfileTemplateManager;
    
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public PlugloadTemplateMgmtService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
	 * Returns plugload profiles count associated with the templates
	 * 
	 * @param plugload profiles
	 * @param pid
	 *            property unique identifier
	 * @return Response status with msg used for sending profiles count
	 */
	@Path("count/plugloadprofiles/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getPlugloadProfilesCount(@PathParam("pid") Long pid) {
		Response oResponse = new Response();
		oResponse.setMsg(String.valueOf(plugloadGroupManager.getPlugloadCountForPlugloadProfile(pid)));
		return oResponse;
	}
	
    /**
     * Delete Plugload Template
     * 
     * @param id
     *            plugload template unique identifier
     * @return Response status
     */
    @Path("delete")
    @POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deletePlugloadTemplate(List<PlugloadProfileTemplate> plugloadProfileTemplate) {
    	
    	m_Logger.debug("Plugload Templates: " + plugloadProfileTemplate.size());
   		Iterator<PlugloadProfileTemplate> itr = plugloadProfileTemplate.iterator();
   		Response oResponse = new Response();
   		long templateId = 0;
   		PlugloadProfileTemplate template = new PlugloadProfileTemplate();
   		while (itr.hasNext()) {
   			template = (PlugloadProfileTemplate) itr.next();
   			templateId = template.getId();
   			int status = 0;
   			try {   				
   				status = plugloadProfileTemplateManager.delete(templateId);
   			} catch (Exception e) {
   				e.printStackTrace();
   				userAuditLoggerUtil.log("Exception while deleting Plugload Profile Template: " + template.getName(), UserAuditActionType.Plugload_Profile_Delete.getName());
   			}
   			userAuditLoggerUtil.log("Deleted Plugload Profile Template: " + template.getName(), UserAuditActionType.Plugload_Profile_Delete.getName());
   			oResponse.setStatus(status);
   			oResponse.setMsg(String.valueOf(templateId)); 
   		}
   		return oResponse;
    	
    }
}
