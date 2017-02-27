/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.PlugloadProfileTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.PlugloadProfileTemplateManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/plugloadProfileTemplate")
public class PlugloadProfileTemplateService {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Autowired
	private MessageSource messageSource;
    @Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource(name = "plugloadProfileTemplateManager")
	private PlugloadProfileTemplateManager plugloadProfileTemplateManager;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	

	public PlugloadProfileTemplateService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	/**
     * Check for duplicate plugload profile template name.
     * 
     * @param plugload templatename
     * @return Response object - If Duplicate, response object will return template name otherwise returns 0
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("duplicatecheck/{plugloadTemplateName}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicatePlugloadProfileTemplate(@PathParam("plugloadTemplateName") String plugloadTemplateName) {
		Response oStatus = new Response();
		oStatus= CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "templateName", plugloadTemplateName);
		if(oStatus!= null && oStatus.getStatus()!=200){
			m_Logger.error("Validation error"+oStatus.getMsg());
    		return oStatus;
    	}
		if (plugloadProfileTemplateManager
				.getPlugloadProfileTemplateCountByName(plugloadTemplateName) > 0) {
			oStatus.setMsg(plugloadTemplateName);
			oStatus.setStatus(0);
			return oStatus;
					}
		
		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}
	
	/**
     * Fetch all derived profile templates (other than the one default plugload template) and return list to UEM
     * 
     * @return List of Plugload Profile Templates except default
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getallderivedemplugloadtemplates")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<PlugloadProfileTemplate> getAllDerivedEMPlugloadTemplates() {
        return plugloadProfileTemplateManager.loadAllDerivedPlugloadProfileTemplate();
    }
}
