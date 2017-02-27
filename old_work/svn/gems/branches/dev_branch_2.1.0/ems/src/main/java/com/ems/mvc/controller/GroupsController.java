/**
 * 
 */
package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.GemsGroupManager;
import com.ems.types.GGroupType;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/groups")
public class GroupsController {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;
    
      
    @Resource(name = "gemsGroupManager")
    private GemsGroupManager gemsGroupManager;
    
    /**
     * Manages the list of groups and create more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return titles template definition to display manageGroups page
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageGroups(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            model.addAttribute("groups", gemsGroupManager.loadGroupsByCompany(1L));
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            model.addAttribute("groups", gemsGroupManager.loadGroupsByCampus(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            model.addAttribute("groups", gemsGroupManager.loadGroupsByBuilding(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("groups", gemsGroupManager.loadGroupsByFloor(id));
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
            model.addAttribute("groups", null);
            model.addAttribute("mode", "admin");
            break;
        }
        }

        return "devices/groups/list";
    }
    
    /**
     * Manages the list of Obsolete groups 
     * 
     * @param model
     *            used in communicating back
     * @return titles template definition to display manage Obsolete Groups page
     */
    @RequestMapping(value = "/obsoleteManage.ems", method = RequestMethod.GET)
    public String manageObseleteGroups(Model model) {
        
        model.addAttribute("page", "company");
        model.addAttribute("groups", gemsGroupManager.loadObsoleteGroupsByCompany(1L));
        //model.addAttribute("mode", "admin");
        
        return "devices/obsoleteGroups/list";
    }
    
	/**
	 * Create/Join group
	 * @param model
	 * @param cookie - Facility Cookie
	 * @return
	 */
	@RequestMapping("/groups_create.ems")
    public String assignGemsGroupToFixtureDialog(Model model,  @CookieValue(value = FacilityCookieHandler.selectedFacilityCookie) String cookie){
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long floorId = cookieHandler.getFacilityId();
            
        model.addAttribute("groups", gemsGroupManager.loadGroupsByFloor(floorId));
        model.addAttribute("grouptypes", GGroupType.MotionGroup.getName());
        model.addAttribute("mode", "create");
        return "devices/groups/group_form";
    }
	
	/**
	 * Edit group
	 * @param model
	 * @param cookie - Facility Cookie
	 * @return
	 */
	@RequestMapping("/groups_edit.ems")
    public String editGroups(Model model){

        model.addAttribute("grouptypes", GGroupType.MotionGroup.getName());
        model.addAttribute("mode", "edit");
        return "devices/groups/group_form";
    }
   
}