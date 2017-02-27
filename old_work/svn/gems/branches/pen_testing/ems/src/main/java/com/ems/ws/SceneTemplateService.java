package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.SceneTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SceneTemplatesManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/org/scenetemplate")
public class SceneTemplateService {
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;
    @Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
    @Resource(name = "sceneTemplatesManager")
    private SceneTemplatesManager sceneTemplatesManager;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");
    public SceneTemplateService() {}
    
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
	/**
	 * Return scene template list
	 * 
	 * @param name
	 *          scene template name
	 * @return Scene template list for the specified name
	 */	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/{name}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SceneTemplate> getSceneTemplateByName(@PathParam("name") String name) {
		List<SceneTemplate> templateList = null;
		Response resp = new Response();
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "templateName", name);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error "+resp.getMsg());
    		return templateList;
    	}
		templateList = sceneTemplatesManager.getSceneTemplateByName(name);	
		//if templateList is null => there is no entry in db with specified name
		if(templateList == null)
		{
			//create new scene template and return the same	
			templateList = new ArrayList<SceneTemplate>();
			templateList.add(sceneTemplatesManager.createNewSceneTemplate(name)); 
			return templateList;
		}
		//scene template with specified name already exists in db
		else
		{
			templateList = null;
			return templateList;
		}			
	}	
	
	/**
	 * Edit Scene Template name
	 * 
	 * @param id
	 * 			SceneLevel unique identifier
     * @return Response status
   	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("edit/{id}/{newscenename}")
	@POST
	public SceneTemplate editSceneTemplateName(@PathParam("id") long id,@PathParam("newscenename") String newscenename){
		Response resp = new Response();
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "templateName", newscenename);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error "+resp.getMsg());
    		return new SceneTemplate();
    	}
		SceneTemplate scenetemplate = sceneTemplatesManager.editSceneTemplateName(id, newscenename);
		return scenetemplate;
	}
	
	/**
	 * Delete Scene Template
	 * 
	 * @param id
	 * 			SceneLevel unique identifier
     * @return Response status
   	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/{id}")
	@POST
	public Response deleteSceneTemplate(@PathParam("id") long id){
		sceneTemplatesManager.deleteSceneTemplate(id);
		return new Response();
	}
}
