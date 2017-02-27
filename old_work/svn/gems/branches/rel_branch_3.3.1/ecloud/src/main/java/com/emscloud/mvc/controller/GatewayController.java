package com.emscloud.mvc.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.communication.vos.Gateway;
import com.emscloud.service.GatewayManager;
import com.emscloud.util.FacilityCookieHandler;


@Controller
@RequestMapping("/devices/gateways")
public class GatewayController {
	
	@Resource
	GatewayManager gatewayManager;
	
	@RequestMapping(value = "/gateway_form.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadGatwayObject(Model model,
			@RequestParam("gatewayId") long gatewayId,@RequestParam("pid") long pid) {
		
		Gateway gw = gatewayManager.getGatewayDetails(pid,gatewayId);
		if(gw != null){
			model.addAttribute("gateway", gw);
		}else{
			model.addAttribute("gateway", new Gateway());
		}
		return "devices/gateways/details";
	}
	
	/**
     * Manages the list of gateways and discover more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return titles template definition to display manageGateways page
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageGateways(
            Model model,
            @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();
        switch (cookieHandler.getFaciltiyType()) {
        case ORGANIZATION: {
            model.addAttribute("page", "company");
            //model.addAttribute("gateways", gatewayManager.loadAllGatewaysWithActiveSensors());
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            //model.addAttribute("gateways", gatewayManager.loadCampusGatewaysWithActiveSensors(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            //model.addAttribute("gateways", gatewayManager.loadBuildingGatewaysWithActiveSensors(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("floorId", id);
           // model.addAttribute("gateways", gatewayManager.loadFloorGatewaysWithActiveSensors(id));
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
            model.addAttribute("gateways", null); //Show blank grid for area selection
            model.addAttribute("mode", "admin");
            break;
        }
        }
        model.addAttribute("pid", id);
        return "devices/gateways/list";
    }
}
