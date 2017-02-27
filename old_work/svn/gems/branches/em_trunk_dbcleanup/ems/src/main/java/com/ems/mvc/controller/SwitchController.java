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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.model.Switch;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.AreaManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.SwitchManager;
import com.ems.types.UserAuditActionType;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/switches")
public class SwitchController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "switchManager")
    private SwitchManager switchManager;

    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;

    @Resource(name = "areaManager")
    private AreaManager areaManager;
    
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    
    @RequestMapping("/switch_create.ems")
    String createSwitch(Model model, @RequestParam("xaxis") String xaxis, @RequestParam("yaxis") String yaxis, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        Switch switchObj = new Switch();
        model.addAttribute("switch", switchObj);
        model.addAttribute("action", "create");
        model.addAttribute("xaxis", Double.parseDouble(xaxis));
        model.addAttribute("yaxis", Double.parseDouble(yaxis));
        
        return "devices/switches/details";
    }

	@RequestMapping("/switch_edit.ems")
	public String loadFixtureObject(Model model, @RequestParam("switchId") long switchId, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        Switch switchObj = switchManager.getSwitchById(switchId);
        model.addAttribute("switch", switchObj);
        model.addAttribute("action", "edit");
		
		return "devices/switches/details";
	} 
	
	@RequestMapping(value = "/saveSwitch.ems")
	@ResponseBody
	public Switch saveSwitch(Switch switchObj) {
		userAuditLoggerUtil.log("Create switch: " + switchObj.getName(), UserAuditActionType.Switch_Update.getName());
		return switchManager.saveSwitch(switchObj);
	}
	
    /**
     * Manages the list of switches and create more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return titles template definition to display manageSwitches page
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageSwitches(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            model.addAttribute("switches", switchManager.loadAllSwitches());
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            model.addAttribute("switches", switchManager.loadSwitchByCampusId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            model.addAttribute("switches", switchManager.loadSwitchByBuildingId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("switches", switchManager.loadSwitchByFloorId(id));
            model.addAttribute("floorId", id);
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
//            Area oArea = areaManager.getAreaUsingId(id);
//            model.addAttribute("switches", (oArea.getFloor() != null ? (switchManager.loadSwitchByFloorId(oArea.getFloor().getId())) : null));
            //model.addAttribute("switches", null);
            model.addAttribute("switches", switchManager.loadSwitchByAreaId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        }

        return "devices/switches/list";
    }
    
    @RequestMapping("/settings/dialog.ems")
    public String switchSettingDialog(Model model, @RequestParam("switchId") long switchId) {
        Switch switchObj = switchManager.getSwitchById(switchId);
        model.addAttribute("switch", switchObj);
        model.addAttribute("scenes", switchManager.loadSceneBySwitchId(switchId));
        return "devices/switches/settings/dialog";
    } 
}
