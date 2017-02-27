/**
 * 
 */
package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.CompanyManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.GGroupType;
import com.ems.utils.CommonUtils;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/groups")
public class MotionGroupsController {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;
    
    @Resource
    CompanyManager companyManager;
      
    @Resource(name = "motionGroupManager")
    private MotionGroupManager motionGroupManager;
    
    @Autowired
	private MessageSource messageSource;
    
    @Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
    /**
     * Manages the list of groups and create more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return titles template definition to display manageGroups page
     * @throws EmsValidationException 
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageGroups(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws EmsValidationException {
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "faclities.em_facilites_jstree_select", cookie);
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            model.addAttribute("groups", motionGroupManager.loadGroupsByCompany(companyManager.loadCompany().getId()));
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            model.addAttribute("groups", motionGroupManager.loadGroupsByCampus(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            model.addAttribute("groups", motionGroupManager.loadGroupsByBuilding(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("groups", motionGroupManager.loadGroupsByFloor(id));
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
        model.addAttribute("groups", motionGroupManager.loadObsoleteGroupsByCompany(companyManager.loadCompany().getId()));
        //model.addAttribute("mode", "admin");
        
        return "devices/obsoleteGroups/list";
    }
    
	/**
	 * Create/Join group
	 * @param model
	 * @param cookie - Facility Cookie
	 * @return
	 * @throws EmsValidationException 
	 */
	@RequestMapping("/groups_create.ems")
    public String assignGemsGroupToFixtureDialog(Model model,  @CookieValue(value = FacilityCookieHandler.selectedFacilityCookie) String cookie) throws EmsValidationException{
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "faclities.em_facilites_jstree_select", cookie);
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long floorId = cookieHandler.getFacilityId();
            
        model.addAttribute("groups", motionGroupManager.loadGroupsByFloor(floorId));
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
	
	@RequestMapping("/fixture_groups_reset.ems")
    public String resetFixtureGroups(Model model){
        return "devices/groups/resetFixtureGroups";
    }
   
}
