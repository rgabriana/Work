package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.AreaManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.CommonUtils;
import com.ems.vo.GatewayWirelessNetworkIdVO;

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
    
    @Resource
    NetworkSettingsManager networkSettingsManager;
    

	@Autowired
	private MessageSource messageSource;
	
	@Resource
	FloorManager floorManager;
	

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
		
		SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
		
		if (dhcpConfig != null) {
			if(!networkSettingsManager.isDhcpServerRunning() && "false".equals(dhcpConfig.getValue())){
				model.addAttribute("dhcpenable", false);
			}else{
				model.addAttribute("dhcpenable", true);
			}
		}else{
			model.addAttribute("dhcpenable", true);
		}
		
		return "devices/gateways/details";
	}

	@RequestMapping(value = "/updateGateway.ems")
	@ResponseBody
	public String updateGateway(Gateway gateway, Locale local) throws EmsValidationException {
    	Map<String,Object> nameValMap = new HashMap<String, Object>();
    	nameValMap.put("gwcontroller.campusName", gateway.getCampusName());
    	nameValMap.put("gwcontroller.buildingName",gateway.getBuildingName());
    	nameValMap.put("gwcontroller.floorName",gateway.getFloorName());
    	nameValMap.put("gwcontroller.ipAddress",gateway.getIpAddress());
    	nameValMap.put("gwcontroller.snapAddress",gateway.getSnapAddress());
    	nameValMap.put("gwcontroller.wirelessEncryptKey",gateway.getWirelessEncryptKey());
    	nameValMap.put("gwcontroller.ethSecKey",gateway.getEthSecKey());
    	nameValMap.put("gwcontroller.app1Version",gateway.getApp1Version());
    	nameValMap.put("gwcontroller.app2Version",gateway.getApp2Version());
    	nameValMap.put("gwcontroller.subnetMask",gateway.getSubnetMask());
    	nameValMap.put("gwcontroller.upgradeStatus",gateway.getUpgradeStatus());
    	nameValMap.put("gwcontroller.bootLoaderVersion",gateway.getBootLoaderVersion());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		Gateway dbGateway = gatewayManager.getGatewayByIp(gateway.getIpAddress());
		if(dbGateway!= null && !dbGateway.getId().equals(gateway.getId())){
			return "{\"failure\":1, \"message\" : \""
					+ messageSource.getMessage("gatewayForm.label.failedUpdateMessage",
							null, local) + "\"}";
		}
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
	 * @throws EmsValidationException 
	 */
	@RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
	public String manageGateways(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,	systemConfigurationManager,	"faclities.em_facilites_jstree_select",	cookie);
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
		SystemConfiguration sc = systemConfigurationManager.loadConfigByName("isMaintenanceMode");
		boolean isMaintenanceMode = false;
		if(sc != null && sc.getValue()!= null){
			isMaintenanceMode = sc.getValue().equalsIgnoreCase("true");
		}
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
            model.addAttribute("isMaintenanceMode",isMaintenanceMode);
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
		List<Gateway> uncommissionedGateways = gatewayManager.loadUnCommissionedGateways();
		model.addAttribute("uncommissionedgateways",uncommissionedGateways);
		if(uncommissionedGateways != null && uncommissionedGateways.size() > 0) gatewayManager.updateGatewayFloor(uncommissionedGateways, floorId);
        
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
		
		SystemConfiguration emcMode = systemConfigurationManager.loadConfigByName("emc.mode");
		
		if (emcMode != null) {
			if("1".equals(emcMode.getValue())){
				model.addAttribute("emcMode", "true");
			}else{
				model.addAttribute("emcMode", "false");
			}
		}

		return "devices/gateways/add";
	}
	
	@RequestMapping("/gateway_commission_form.ems")
	public String commissionGateway(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
			@RequestParam(value = "gatewayId", required = false) Long gatewayId) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,	systemConfigurationManager,	"faclities.em_facilites_jstree_select",	cookie);
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();

		switch (cookieHandler.getFaciltiyType()) {
		case FLOOR: {
			List<Gateway> gatewayList = null;
			List<GatewayWirelessNetworkIdVO> gatewayWirelessNetworkIdVOList = null;
			
			List<Gateway> commissionedGatewayList = null;
			
			Floor floor = floorManager.getFloorusingId(id);
			
			commissionedGatewayList = gatewayManager.loadCommissionedBuildingGateways(floor.getBuilding().getId());
			
			ArrayList<GatewayWirelessNetworkIdVO> commissionedGatewayWirelessNetworkIdList = new ArrayList<GatewayWirelessNetworkIdVO>();
			if(!ArgumentUtils.isNullOrEmpty(commissionedGatewayList)){
				GatewayWirelessNetworkIdVO commisionedgatewayWirelessNetworkIdVO = null;
				for(Gateway gateway :commissionedGatewayList){
					commisionedgatewayWirelessNetworkIdVO = new GatewayWirelessNetworkIdVO();
					commisionedgatewayWirelessNetworkIdVO.setGatewayName(gateway.getGatewayName());
					if(gateway.getGatewayType().intValue() != 5)
						commisionedgatewayWirelessNetworkIdVO.setWirelessNetworkId(gateway.getWirelessNetworkId().toString());
					commissionedGatewayWirelessNetworkIdList.add(commisionedgatewayWirelessNetworkIdVO);
				}
			}
			
			if (gatewayId == null || gatewayId == 0) {
				gatewayList = gatewayManager.loadUnCommissionedGateways();
				gatewayWirelessNetworkIdVOList = new ArrayList<GatewayWirelessNetworkIdVO>();
				GatewayWirelessNetworkIdVO gatewayWirelessNetworkIdVO = null;
				for(Gateway gateway:gatewayList){
					//int i = getRandomGatewayWirelessNetworkId(commissionedGatewayWirelessNetworkIdList);
					String wirelessNetworkId = getGatewayWirelessNetworkId(gateway.getMacAddress());
					gatewayWirelessNetworkIdVO = new GatewayWirelessNetworkIdVO();
					gatewayWirelessNetworkIdVO.setGatewayName(gateway.getGatewayName());
					gatewayWirelessNetworkIdVO.setWirelessNetworkId(wirelessNetworkId);
					gatewayWirelessNetworkIdVOList.add(gatewayWirelessNetworkIdVO);
				}
			} else {
				gatewayList = new ArrayList<Gateway>();
				gatewayList.add(gatewayManager.loadGatewayWithActiveSensors(gatewayId));
				gatewayWirelessNetworkIdVOList = new ArrayList<GatewayWirelessNetworkIdVO>();
				GatewayWirelessNetworkIdVO gatewayWirelessNetworkIdVO = null;
				for(Gateway gateway:gatewayList){
					//int i = getRandomGatewayWirelessNetworkId(commissionedGatewayWirelessNetworkIdList);
					String wirelessNetworkId = getGatewayWirelessNetworkId(gateway.getMacAddress());
					gatewayWirelessNetworkIdVO = new GatewayWirelessNetworkIdVO();
					gatewayWirelessNetworkIdVO.setGatewayName(gateway.getGatewayName());
					gatewayWirelessNetworkIdVO.setWirelessNetworkId(wirelessNetworkId);
					gatewayWirelessNetworkIdVOList.add(gatewayWirelessNetworkIdVO);
				}
			}
			model.addAttribute("floorId", id);
			model.addAttribute("gateways", gatewayList);
			model.addAttribute("message", "");
			model.addAttribute("gatewayWirelessNetworkIdVOList", gatewayWirelessNetworkIdVOList);
			model.addAttribute("commissionedGatewayWirelessNetworkIdList", commissionedGatewayWirelessNetworkIdList);
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
	
	public int getRandomGatewayWirelessNetworkId(ArrayList<Integer> commissionedGatewayWirelessNetworkIdList){
		
		int minWirelessNetworkId = 1;
		int maxWirelessNetworkId = 65534;
		int i = (int) (Math.random() * (maxWirelessNetworkId - minWirelessNetworkId)) + minWirelessNetworkId;
		
		if(!ArgumentUtils.isNullOrEmpty(commissionedGatewayWirelessNetworkIdList)){
			if(!commissionedGatewayWirelessNetworkIdList.contains(i)){
				return i;
			}
		}else{
			return i;
		}
		
		return getRandomGatewayWirelessNetworkId(commissionedGatewayWirelessNetworkIdList);
	}
	
	public String getGatewayWirelessNetworkId(String macAddress){
		String wirelessNetworkId = "";
		String[] bits = macAddress.split(":");
		String bits1 = bits[bits.length-1];
		String bits2 = bits[bits.length-2];
		if(bits1.length() == 1){
			bits1 = "0" + bits[bits.length-1];
		}
		
		if(bits2.length() == 1){
			bits2 = "0" + bits[bits.length-2];
		}
		
		wirelessNetworkId = bits2 + bits1 ;
		
		return wirelessNetworkId;
	}
}
