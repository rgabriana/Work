package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.model.SceneLevel;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SwitchManager;

@Controller
@Path("/org/scene/v1")
public class SceneServiceV1 {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "switchManager")
	private SwitchManager switchManager;
	
	/**
	 * Return SceneLevel list based on Scene Id
	 * 
	 * @param sceneId
	 * 				Scene unique identifier
	 * @return SceneLevel for the selected Scene
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Bacnet')")
	@Path("list/getSceneLevels/{sceneid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SceneLevel> loadSceneLevelsBySceneId(@PathParam("sceneid") long sceneId) {
		
		return switchManager.loadSceneLevelsBySceneId(sceneId);
		
	} //end of method loadSceneLevelsBySceneId
	
} //end of class SceneServiceV1
