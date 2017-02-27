package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.PlugloadSceneLevel;
import com.ems.model.Scene;
import com.ems.model.SceneLevel;
import com.ems.model.Switch;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.GemsGroupManager;
import com.ems.service.GemsPlugloadGroupManager;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.vo.model.PlugloadList;
import com.ems.ws.util.Response;


@Controller
@Path("/org/scene")
public class SceneService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "switchManager")
	private SwitchManager switchManager;
	@Resource(name = "gemsGroupManager")
	private GemsGroupManager gemsGroupManager;
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	@Resource(name = "gemsPlugloadGroupManager")
	GemsPlugloadGroupManager gemsPlugloadGroupManager;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	/**
	 * Return Scenes list based on Switch Id
	 * 
	 * @param switchId
	 *            switch unique identifier
	 * @return Scenes list for the selected switch
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("savescene")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Scene saveScene(Scene scene) {
	    String sceneName= "";
	    try {
            sceneName = URLDecoder.decode(scene.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	    scene.setName(sceneName);
	    
		userAuditLoggerUtil.log("Create scene: " + scene.getName(), UserAuditActionType.Scene_Update.getName());
		boolean isNew = false;
		if(scene.getId() == null) {
			isNew = true;
			scene.setSceneOrder(switchManager.nextSceneOrder(scene.getSwitchId()));
		}
		scene = switchManager.saveScene(scene);
		if(isNew) {
	    	 Switch sw = switchManager.getSwitchById(scene.getSwitchId());
	    	 List<GemsGroupFixture> groupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(sw.getGemsGroup().getId());
	    	 if(groupFixtures != null && groupFixtures.size() > 0) {
		    	for(GemsGroupFixture ggf: groupFixtures) {
    				SceneLevel sl = new SceneLevel(null, sw.getId(), scene.getId(), ggf.getFixture().getId(), 50);
    				switchManager.updateSceneLevel(sl);
    				if (ggf.getNeedSync() > GemsGroupFixture.SYNC_STATUS_GROUP) {
    				    ggf.setNeedSync(GemsGroupFixture.SYNC_STATUS_GROUP);
    				    gemsGroupManager.updateGemsGroup(ggf);
    				}
		    	}
	    	 }
	    	 // Adding work flow to create scene for Plug load also as per discussion PM
	    	 List<GemsGroupPlugload> groupPlugloads = gemsPlugloadGroupManager.getGemsGroupPlugloadByGroup(sw.getGemsGroup().getId());
	    	 if(groupPlugloads != null && groupPlugloads.size() > 0) {
		    	for(GemsGroupPlugload ggp: groupPlugloads) {
    				PlugloadSceneLevel p1 = new PlugloadSceneLevel(null, sw.getId(), scene.getId(), ggp.getPlugload().getId(), 100);
    				switchManager.updatePlugloadSceneLevel(p1);
    				if (ggp.getNeedSync() > GemsGroupPlugload.SYNC_STATUS_GROUP) {
    					ggp.setNeedSync(GemsGroupPlugload.SYNC_STATUS_GROUP);
    					gemsPlugloadGroupManager.updateGemsGroupPlugload(ggp);
    				}
		    	}
	    	 }
		}
		return scene;
	}
	
	/**
     * save scenes list
     * 
     * @param scenes
     *          List of Scenes
     *          <scenes><scene><id></id><name>test_scene</name><switchid>122</switchid></scene></scenes>
     * @return list of newly created scenes
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("details/sid/{switchid}/{name}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Scene loadSceneByNameandSwitchId(@PathParam("name") String sceneName, @PathParam("switchid") long switchId) {
		Response resp = new Response();
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "groupName", sceneName);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
		return switchManager.loadScenebyNameAndSwitchId(sceneName, switchId);
	}
	
	/**
	 * Delete Scene
	 * 
	 * @param id
	 * 			SceneLevel unique identifier
     * @return Response status
   	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/{id}")
	@POST
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/scenelevel/{id}")
	@GET
	public Response deleteSceneLevel(@PathParam("id") long id){
		switchManager.deleteSceneLevel(id);
		userAuditLoggerUtil.log("Delete scene level with id: " + id, UserAuditActionType.Scene_Update.getName());
		return new Response();
	}
	

	/**
	 * update plugloadscenelevel
	 * 
	 * @param sceneLevels
	 *             List of scenesLevels
	 *             <plugloadsceneLevels><plugloadsceneLevel><id></id><switchid>122</switchid><sceneid>86</sceneid><plugloadid>131</plugloadid><lightlevel>100</lightlevel></plugloadsceneLevel></plugloadsceneLevels>
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("updateplugloadscenelevel")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updatePlugloadSceneLevel(List<PlugloadSceneLevel> plugloadSceneLevels) {
		 if(plugloadSceneLevels != null && plugloadSceneLevels.size() > 0) {
			 for(PlugloadSceneLevel sceneLevel: plugloadSceneLevels) {
			     switchManager.updatePlugloadSceneLevel(sceneLevel);
			 }
		 }
         return new Response();
    }
	
	/**
	 * Return SceneLevel list based on Scene Id
	 * 
	 * @param sceneId
	 * 				Scene unique identifier
	 * @return SceneLevel for the selected Scene
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee', 'Bacnet')")
	@Path("list/plugloadscenelevel/sid/{sceneid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<PlugloadSceneLevel> loadPlugloadSceneLevelsBySceneId(@PathParam("sceneid") long sceneId) {
		return switchManager.loadPlugloadSceneLevelsBySceneId(sceneId);
	}
}
