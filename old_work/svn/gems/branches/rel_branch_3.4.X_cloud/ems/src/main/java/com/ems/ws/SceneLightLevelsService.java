package com.ems.ws;

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

import org.springframework.stereotype.Controller;

import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.SceneTemplate;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SceneLightLevelsManager;
import com.ems.ws.util.Response;


@Controller
@Path("/org/scenelightlevels")
public class SceneLightLevelsService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "sceneLightLevelsManager")
	private SceneLightLevelsManager sceneLightLevelsManager;
	/**
	 * Add Scene Light Level for specified template id
	 * 
	 * @param id
	 *          unique identifier
	 * @return SceneLightLevelTemplate model for specified template id
	 */	
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
	@Path("list/{sceneTemplateId}/{sceneOrder}")
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getListOfSceneLightLevelBySceneTemplateIdAndSceneOrder(@PathParam("sceneTemplateId") Long sceneTemplateId, @PathParam("sceneOrder") Integer sceneOrder) {
		List<SceneLightLevelTemplate> lightLevelList = sceneLightLevelsManager.loadSceneLightLevelBySceneTemplateIdAndSceneOrder(sceneTemplateId, sceneOrder);
		Response response = new Response();
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
	@Path("delete/{id}")
	@POST
	public Response deleteSceneLightLevel(@PathParam("id") long id){
		sceneLightLevelsManager.deleteSceneLightLevel(id);
		return new Response();
	}
}
