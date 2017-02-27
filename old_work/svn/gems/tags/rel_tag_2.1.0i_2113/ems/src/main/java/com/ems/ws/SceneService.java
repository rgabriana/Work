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

import com.ems.model.Scene;
import com.ems.model.SceneLevel;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SwitchManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;


@Controller
@Path("/org/scene")
public class SceneService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "switchManager")
	private SwitchManager switchManager;

	/**
	 * Return Scenes list based on Switch Id
	 * 
	 * @param switchId
	 *            switch unique identifier
	 * @return Scenes list for the selected switch
	 */
	@Path("list/sid/{switchid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Scene> loadSceneBySwitchId(@PathParam("switchid") long switchId) {
		return switchManager.loadSceneBySwitchId(switchId);
	}
	
	/**
	 * Return SceneLevel list based on Scene Id
	 * 
	 * @param sceneId
	 * 				Scene unique identifier
	 * @return SceneLevel for the selected Scene
	 */
	@Path("list/scenelevel/sid/{sceneid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SceneLevel> loadSceneLevelsBySceneId(@PathParam("sceneid") long sceneId) {
		return switchManager.loadSceneLevelsBySceneId(sceneId);
	}
	
	 /**
     * save scene
     * 
     * @param scene
     *           <scene><id></id><name>test_scene</name><switchid>122</switchid></scene>
     * @return newly created Scene object
     */
	@Path("savescene")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Scene saveScene(Scene scene) {
		userAuditLoggerUtil.log("Create scene: " + scene.getName(), UserAuditActionType.Scene_Update.getName());
		return switchManager.saveScene(scene);
	}
	
	/**
     * save scenes list
     * 
     * @param scenes
     *          List of Scenes
     *          <scenes><scene><id></id><name>test_scene</name><switchid>122</switchid></scene></scenes>
     * @return list of newly created scenes
     */
	@Path("savescenelist")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Scene> saveSceneList(List<Scene> scenes) {
       List<Scene> newscenes = new ArrayList<Scene>();
       StringBuffer sceneNames = new StringBuffer("");
       for(Scene scene : scenes) {
           newscenes.add(switchManager.saveScene(scene));
           sceneNames.append(scene.getName() + ",");
       }
       userAuditLoggerUtil.log("Save scenes " + sceneNames, UserAuditActionType.Scene_Update.getName());
       return newscenes;
    }
	
	/**
	 * Return Scene based on Scene Name and Switch Id
	 * 
	 * @param scenename
	 * 				Scene Name
	 * @param switchId
	 * 				Switch unique identifier
	 * @return Scene Object based on scenename and switchid
	 */
	@Path("details/sid/{switchid}/{name}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Scene loadSceneByNameandSwitchId(@PathParam("name") String sceneName, @PathParam("switchid") long switchId) {
		return switchManager.loadScenebyNameAndSwitchId(sceneName, switchId);
	}
	
	/**
	 * Delete Scene
	 * 
	 * @param id
	 * 			SceneLevel unique identifier
     * @return Response status
   	 */
	@Path("delete/{id}")
	@GET
	public Response deleteScene(@PathParam("id") long id){
		switchManager.deleteScene(id);
		userAuditLoggerUtil.log("Delete scene with id : " +id, UserAuditActionType.Scene_Update.getName());
		return new Response();
	}
	
	/**
	 * save scenelevel
	 * 
	 * @param sceneLevels
	 *             List of scenesLevels
	 *             <sceneLevels><sceneLevel><id></id><switchid>122</switchid><sceneid>86</sceneid><fixtureid>131</fixtureid><lightlevel>100</lightlevel></sceneLevel></sceneLevels>
	 * @return Response status
	 */
	@Path("savescenelevel/{switchId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response saveSceneLevel(List<SceneLevel> sceneLevels,  @PathParam("switchId") long switchId) {
		 if(sceneLevels == null) {
			 sceneLevels = new ArrayList<SceneLevel>();
		 }
	     switchManager.saveSceneLevel(sceneLevels, switchId);
         return new Response();
    }
	
	/**
	 * update scenelevel
	 * 
	 * @param sceneLevels
	 *             List of scenesLevels
	 *             <sceneLevels><sceneLevel><id></id><switchid>122</switchid><sceneid>86</sceneid><fixtureid>131</fixtureid><lightlevel>100</lightlevel></sceneLevel></sceneLevels>
	 * @return Response status
	 */
	@Path("updatescenelevel")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateSceneLevel(List<SceneLevel> sceneLevels) {
		 if(sceneLevels != null && sceneLevels.size() > 0) {
			 for(SceneLevel sceneLevel: sceneLevels) {
			     switchManager.updateSceneLevel(sceneLevel);
			 }
		 }
         return new Response();
    }
	   
	/**
	 * Remove SceneLevel entry
	 * 
	 * @param id
	 * 			SceneLevel unique identifier
	 * @return Response status
	 */
	@Path("delete/scenelevel/{id}")
	@GET
	public Response deleteSceneLevel(@PathParam("id") long id){
		switchManager.deleteSceneLevel(id);
		userAuditLoggerUtil.log("Delete scene level with id: " + id, UserAuditActionType.Scene_Update.getName());
		return new Response();
	}
}
