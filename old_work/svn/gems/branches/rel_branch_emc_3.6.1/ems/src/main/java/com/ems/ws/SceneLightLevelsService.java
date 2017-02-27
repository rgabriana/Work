package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.SceneTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SceneLightLevelsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;


@Controller
@Path("/org/scenelightlevels")
public class SceneLightLevelsService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	@Resource(name = "sceneLightLevelsManager")
	private SceneLightLevelsManager sceneLightLevelsManager;
	/**
	 * Add Scene Light Level for specified template id
	 * 
	 * @param id
	 *          unique identifier
	 * @return SceneLightLevelTemplate model for specified template id
	 */	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("add")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public SceneLightLevelTemplate saveScene(SceneLightLevelTemplate sceneLightLevelTemplate) {
		return sceneLightLevelsManager.saveScene(sceneLightLevelTemplate);
	}
	/**
	 * Return scene level list
	 * 
	 * @param id
	 *          unique identifier
	 * @return scene level list for specified template id
	 */	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("list/{sceneTemplateId}")
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SceneLightLevelTemplate> getListOfSceneLightLevel(@PathParam("sceneTemplateId") Long sceneTemplateId) {
		return sceneLightLevelsManager.loadSceneLightLevelBySceneTemplateId(sceneTemplateId);
	}
			
	/**
	 * Return scene level list
	 * 
	 * @param id
	 *          unique identifier
	 * @return scene level list for specified template id and scene order
	 */	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("list/{sceneTemplateId}/{sceneOrder}")
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getListOfSceneLightLevelBySceneTemplateIdAndSceneOrder(@PathParam("sceneTemplateId") Long sceneTemplateId, @PathParam("sceneOrder") Integer sceneOrder) {
		Response response = new Response();
		/*response = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "page", sceneOrder);
		if(response!=null && response.getStatus()!=200){
			return response;
		}*/
		List<SceneLightLevelTemplate> lightLevelList = sceneLightLevelsManager.loadSceneLightLevelBySceneTemplateIdAndSceneOrder(sceneTemplateId, sceneOrder);
		
		if(lightLevelList!=null && lightLevelList.size() > 0)
		{
			response.setStatus(1);
		}
		return response;	
			
	}	
	
	/**
	 * Delete Scene Light level associated with the scene template
	 * 
	 * @param id
	 * 			SceneLevel unique identifier
     * @return Response status
   	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/{id}")
	@POST
	public Response deleteSceneLightLevel(@PathParam("id") long id){
		sceneLightLevelsManager.deleteSceneLightLevel(id);
		return new Response();
	}
}
