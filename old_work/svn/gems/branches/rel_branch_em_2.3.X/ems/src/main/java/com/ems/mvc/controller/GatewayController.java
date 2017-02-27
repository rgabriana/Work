package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.AreaManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;

@Controller
@RequestMapping("/devices/gateways")
public class GatewayController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "gatewayManager")
	private GatewayManager gatewayManager;
	
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;

    @Resource(name = "areaManager")
    private AreaManager areaManager;

    @Resource
    SystemConfigurationManager systemConfigurationManager;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping("/gateway_form.ems")
	public String loadGatwayObject(Model model,
			@RequestParam("gatewayId") long gatewayId) {
		Gateway gw =gatewayManager.loadGatewayWithActiveSensors(gatewayId) ;
	    List<Fixture> fxList = fixtureManager.loadAllFixtureByGatewayId(gw.getId());

		model.addAttribute("gateway", gw);
		model.addAttribute("message", "");
		if (fxList != null)
		    model.addAttribute("fixturecount", fxList.size());
		else
            model.addAttribute("fixturecount", 0);
		
		return "devices/gateways/details";
	}

	@RequestMapping(value = "/updateGateway.ems")
	@ResponseBody
	public String updateGateway(Gateway gateway, Locale local) {
		gatewayManager.updateGatewayParameters(gateway);
		userAuditLoggerUtil.log("Update gateway: " + gateway.getGatewayName(), UserAuditActionType.Gateway_Update.getName());	
		return "{\"success\":1, \"message\" : \""
				+ messageSource.getMessage("gatewayForm.label.successMessage",
						null, local) + "\"}";
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
		case COMPANY: {
			model.addAttribute("page", "company");
			model.addAttribute("gateways", gatewayManager.loadAllGatewaysWithActiveSensors());
			model.addAttribute("mode", "admin");
			break;
		}
		case CAMPUS: {
			model.addAttribute("page", "campus");
			model.addAttribute("gateways",
					gatewayManager.loadCampusGatewaysWithActiveSensors(id));
			model.addAttribute("mode", "admin");
			break;
		}
		case BUILDING: {
			model.addAttribute("page", "building");
			model.addAttribute("gateways",
					gatewayManager.loadBuildingGatewaysWithActiveSensors(id));
			model.addAttribute("mode", "admin");
			break;
		}
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("floorId", id);
            model.addAttribute("gateways", gatewayManager.loadFloorGatewaysWithActiveSensors(id));
            model.addAttribute("mode", "admin");
            break;
        }
		default: {
			model.addAttribute("page", "area");
//			Area oArea = areaManager.getAreaUsingId(id);
//			model.addAttribute("gateways", (oArea.getFloor() != null ? (gatewayManager.loadFloorGateways(oArea.getFloor().getId())) : null));
            model.addAttribute("gateways", null); //Show blank grid for area selection
			model.addAttribute("mode", "admin");
			break;
		}
		}

        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
        if (dhcpConfig != null) {
    		model.addAttribute("dhcpEnable", dhcpConfig.getValue());
        }

        return "devices/gateways/list";
	}

	@RequestMapping(value = "/discoverGateway.ems")
	public String discoverGateway(Model model,
			@RequestParam("floorId") Long floorId) {
		List<Gateway> oDiscoveredGatewayList = new ArrayList<Gateway>(); 
		try {        
    		oDiscoveredGatewayList = gatewayManager
    				.discoverGateways(floorId);
		}catch(Exception e) {
		    //System.out.println("Discovery Gateway, inventory table already has the gateway " + e.getMessage());
		}
		if (oDiscoveredGatewayList.size() > 0) {
			model.addAttribute("message", "New gateway discovered.");
		}
		model.addAttribute("page", "floor");
		model.addAttribute("floorId", floorId);
		model.addAttribute("gateways",
				gatewayManager.loadFloorGatewaysWithActiveSensors(floorId));
        model.addAttribute("uncommissionedgateways",
                gatewayManager.loadUnCommissionedGateways());
		model.addAttribute("mode", "admin");
        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
        if (dhcpConfig != null) {
    		model.addAttribute("dhcpEnable", dhcpConfig.getValue());
        }
		return "devices/gateways/list";
	}

	@RequestMapping(value = "/addGateway.ems")
	public String addGateway(Model model,
			@RequestParam("floorId") Long floorId) {

		model.addAttribute("floorId", floorId);

		return "devices/gateways/add";
	}
	
	@RequestMapping("/gateway_commission_form.ems")
	public String commissionGateway(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
			@RequestParam(value = "gatewayId", required = false) Long gatewayId) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();

		switch (cookieHandler.getFaciltiyType()) {
		case FLOOR: {
			List<Gateway> gatewayList = null;
			if (gatewayId == null || gatewayId == 0) {
				gatewayList = gatewayManager.loadUnCommissionedGateways();
			} else {
				gatewayList = new ArrayList<Gateway>();
				gatewayList.add(gatewayManager.loadGatewayWithActiveSensors(gatewayId));
			}
			model.addAttribute("floorId", id);
			model.addAttribute("gateways", gatewayList);
			model.addAttribute("message", "");
			break;
		}
		default: {
			model.addAttribute("floorId", id);
			model.addAttribute("gateways", null);
			model.addAttribute("message", "Not supported.");
			break;
		}
		}
		return "devices/gateways/commission";

	}

}
