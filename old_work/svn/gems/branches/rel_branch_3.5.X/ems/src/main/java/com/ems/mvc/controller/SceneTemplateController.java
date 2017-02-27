package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SceneLightLevelsManager;
import com.ems.service.SceneTemplatesManager;

@Controller
@RequestMapping("/devices/scenetemplates")
public class SceneTemplateController {
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	    
    @Resource(name = "sceneTemplatesManager")
    private SceneTemplatesManager sceneTemplatesManager;
    
    @Resource(name = "sceneLightLevelsManager")
    private SceneLightLevelsManager sceneLightLevelsManager;
          
    /**
     * Manages the list of scene templates
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return scene template list to be displayed
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageSceneTemplates(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
            model.addAttribute("mode", "admin");
            break;
        }
        }

        model.addAttribute("id", id);        
        model.addAttribute("scenetemplates",sceneTemplatesManager.loadAllSceneTemlates());     
        model.addAttribute("scenelevels", sceneLightLevelsManager.loadAllSceneLightLevels());
        return "devices/scenetemplates/list";
    }
 }
	

