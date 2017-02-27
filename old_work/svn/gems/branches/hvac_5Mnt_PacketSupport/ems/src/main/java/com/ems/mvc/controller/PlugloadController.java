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

import com.ems.model.BallastVoltPower;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.Gateway;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Groups;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.SystemConfiguration;
import com.ems.model.Tenant;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.security.EmsAuthenticationContext;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.types.TemperatureType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.vo.model.FixtureLampStatusVO;


@Controller
@RequestMapping("/devices/plugloads")
public class PlugloadController {
	
	@Resource
	PlugloadManager plugloadManager ;
	
	@Resource(name = "groupManager")
    private GroupManager groupManager;
	
	@Resource(name = "plugloadGroupManager")
	private PlugloadGroupManager plugloadGroupManager;
	
	@Resource(name="gatewayManager")
	private GatewayManager gatewayManager;
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	@Autowired
    private MessageSource messageSource;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
	public String managePlugloads(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
		Long emInstanceId = null;
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
		model.addAttribute("pid", id);
		return "devices/plugloads/list";
	}

	
	@RequestMapping("/plugload_details.ems")
	public String loadPlugloadDetails(Model model,
			@RequestParam("plugloadId") long plugloadId) {
		model.addAttribute("plugloadId", plugloadId);
		System.out.println("inside load plugload details");
		return "devices/plugloads/tabpaneldetails";
	}

	
	@RequestMapping("/plugload_form.ems")
	public String loadPlugloadObject(Model model,
			@RequestParam("plugloadId") long plugloadId) {

		//Long templateId = null;
		System.out.println("plugload id is" + plugloadId);
		Plugload plugload = plugloadManager.getPlugloadById(plugloadId);
		//System.out.println("plugload is " + plugload);
		model.addAttribute("plugload", plugload);
		model.addAttribute("originalPlugloadProfileFrom",
				plugload.getOriginalProfileFrom());
		model.addAttribute("currentPlugloadProfile", plugload.getCurrentProfile());
		model.addAttribute("state", plugload.getState());
		List<PlugloadGroups> profileList = null;
		//PlugloadGroups group = plugloadGroupManager.getPlugloadGroupById(plugload.getGroupId());
		//System.out.println("group id is" + plugload.getGroupId());
		//if (group.getPlugloadProfileTemplate() != null) {
		//	templateId = group.getPlugloadProfileTemplate().getId();
		//	profileList = plugloadGroupManager.loadAllProfileTemplateById(templateId, 0L);

		//}
		profileList = plugloadGroupManager.loadAllPlugloadGroupsExceptDeafult();
		SystemConfiguration temperatureConfig = systemConfigurationManager
				.loadConfigByName("temperature_unit");
		String dbTemp;
		if (temperatureConfig != null) {
			dbTemp = temperatureConfig.getValue();
			String tempUnit = "";
			if (dbTemp.equalsIgnoreCase(TemperatureType.F.getName())) {
				tempUnit = TemperatureType.F.getName();
			} else if (dbTemp.equalsIgnoreCase(TemperatureType.C.getName())) {
				tempUnit = TemperatureType.C.getName();
			}
			model.addAttribute("temperatureunit", tempUnit);
		}
		model.addAttribute("groups", profileList);

		return "devices/plugloads/details";
	}

	@RequestMapping(value = "/updatePlugload.ems")
	@ResponseBody
	public String updatePlugload(Plugload plugload, Locale local) {
		//System.out.println("inside updateplugload");
		Plugload savedPlugload = plugloadManager.getPlugloadById(plugload
				.getId());
		savedPlugload.setName(plugload.getName());
		savedPlugload.setCurrentState(plugload.getCurrentState());
		savedPlugload.setIsHopper(plugload.getIsHopper());
		//System.out.println("new plugload name is " + plugload.getName());
		//System.out.println("new current profile is "
		//		+ plugload.getCurrentProfile() + " " + plugload.getGroupId());
		
		//System.out.println("new orginal profile is "+plugload.getOriginalProfileFrom());
		plugloadManager.updatePlugload(savedPlugload);
		
		if (savedPlugload.getGroupId().longValue() != plugload.getGroupId().longValue()) {
			plugloadManager.changePlugloadProfile(plugload.getId(), plugload.getGroupId(), plugload.getCurrentProfile(), plugload.getOriginalProfileFrom());
        }
		
		return "{\"success\":1, \"message\" : \""
				+ messageSource.getMessage("plugloadForm.message.success",
						null, local) + "\"}";
	}

	@RequestMapping(value = "/plugloadsetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String plugloadProfileSetting(Model model,
			@RequestParam(value = "plugloadId", required = false) Long id,
			@RequestParam(value = "groupId", required = false) Long groupId) {
		System.out.println("inside plugload profile controller");
		return "profile/plugload/setting";
	}

	@RequestMapping("/plugload_delete_dialog.ems")
	public String plugloadDeleteDialog(Model model) {
		return "plugloads/delete/dialog";
	}

	@RequestMapping("/plugload_discovery_form.ems")
	public String plugloadDiscovery(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();

		switch (cookieHandler.getFaciltiyType()) {
		case FLOOR: {
			List<Gateway> floorGatewaysList = gatewayManager
					.loadFloorGateways(id);
			Map<Long, Integer> plugloadCountMap = new HashMap<Long, Integer>();
			Map<Long, Integer> sensorsCountMap = new HashMap<Long, Integer>();
			List<Gateway> gatewayData = new ArrayList<Gateway>();

			if ((floorGatewaysList != null) && (floorGatewaysList.size() > 0)) {
				for (Gateway gateway : floorGatewaysList) {
					if (gateway.isCommissioned()) {
						gatewayData.add(gateway);
						sensorsCountMap.put(gateway.getId(),
								gateway.getNoOfPlugloads());
						List<Plugload> plugloadList = plugloadManager
								.loadAllPlugloadsByGatewayId(gateway.getId());
						plugloadCountMap
								.put(gateway.getId(),
										(plugloadList != null ? plugloadList
												.size() : 0));
					}
				}
			}

			model.addAttribute("floorId", id);
			model.addAttribute("gateways", gatewayData);
			model.addAttribute("sensorsCountMap", sensorsCountMap);
			model.addAttribute("plugloadCountMap", plugloadCountMap);
			model.addAttribute("message", "");
			break;
		}
		default: {
			model.addAttribute("floorId", id);
			model.addAttribute("gateways", null);
			model.addAttribute("sensorsCountMap", null);
			model.addAttribute("fixturesCountMap", null);
			model.addAttribute("message", "Not supported.");
			break;
		}
		}
		return "devices/plugload/discovery";
	}

	@RequestMapping("/plugload_commission_start_identify.ems")
	public String identifyPlugloadCommission(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
			@RequestParam("isBulkCommission") Boolean isBulkCommission,
			@RequestParam("gatewayId") Long gatewayId,
			@RequestParam("plugloadId") Long plugloadId) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();

		switch (cookieHandler.getFaciltiyType()) {
		case FLOOR: {
			List<Gateway> floorGatewaysList = null;
			if (isBulkCommission) {
				floorGatewaysList = gatewayManager.loadFloorGateways(id);
			} else {
				floorGatewaysList = new ArrayList<Gateway>();
				floorGatewaysList.add(gatewayManager.loadGateway(gatewayId));
			}

			floorGatewaysList = gatewayManager.loadFloorGateways(id);

			Map<Long, Integer> plugloadMap = new HashMap<Long, Integer>();
			List<Gateway> gatewayData = new ArrayList<Gateway>();

			if ((floorGatewaysList != null) && (floorGatewaysList.size() > 0)) {
				for (Gateway gateway : floorGatewaysList) {
					if (gateway.isCommissioned()) {
						gatewayData.add(gateway);
						List<Plugload> plugloadList = plugloadManager
								.getUnCommissionedPlugloadList(gateway.getId());
						plugloadMap.put(gateway.getId(),
								(plugloadList != null ? plugloadList.size() : 0));
					}
				}
			}

			model.addAttribute("floorId", id);
			model.addAttribute("gateways", gatewayData);
			model.addAttribute("message", "");
			// options
			model.addAttribute("isBulkCommission", isBulkCommission);
			model.addAttribute("plugloadId", plugloadId);
			break;
		}
		default: {
			model.addAttribute("floorId", id);
			model.addAttribute("gateways", null);
			model.addAttribute("message", "Not supported.");
			// options
			model.addAttribute("isBulkCommission", isBulkCommission);
			model.addAttribute("plugloadId", plugloadId);
			break;
		}
		}
		return "devices/plugload/startcommission";
	}

	 @RequestMapping("/plugload_commission_form")
	    public String commissionPlugload(Model model, @RequestParam("gatewayId") long gatewayId,
	            @RequestParam("type") long type, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
	            @RequestParam("isBulkCommission") Boolean isBulkCommission,
	            @RequestParam(value = "plugloadId", required = false) Long plugloadId) {
	        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
	        Long id = cookieHandler.getFacilityId();

	        Gateway gw = gatewayManager.loadGateway(gatewayId);
	        String enablePlugloadProfileFeatureVal = "false";
	        SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
	        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
	        	enablePlugloadProfileFeatureVal = enablePlugloadProfileFeature.getValue();
	        }
	        switch (cookieHandler.getFaciltiyType()) {
	        case FLOOR: {
	            model.addAttribute("groups", plugloadGroupManager.loadAllPlugloadGroupsExceptDeafult());
	            model.addAttribute("floorId", id);
	            model.addAttribute("plugloadId", plugloadId);
	            model.addAttribute("type", type);
	            model.addAttribute("gateway", gw);
	            model.addAttribute("isBulkCommission", isBulkCommission);
	            model.addAttribute("message", "");
	            model.addAttribute("enablePlugloadProfileFeature", enablePlugloadProfileFeatureVal);
	            break;
	        }
	        default: {
	            model.addAttribute("groups", null);
	            model.addAttribute("floorId", id);
	            model.addAttribute("plugloadId", plugloadId);
	            model.addAttribute("type", type);
	            model.addAttribute("gateway", gw);
	            model.addAttribute("isBulkCommission", isBulkCommission);
	            model.addAttribute("message", "Not supported.");
	            model.addAttribute("enablePlugloadProfileFeature", enablePlugloadProfileFeatureVal);
	            break;
	        }
	        }
	        return "devices/plugload/commission";
	    }
	@RequestMapping("/assignplugloadprofiletoplugloads.ems")
    public String assignPlugloadProfileToPlugloadDialog(Model model){
		
		TreeNode<FacilityType> plugloadProfileHierachy = null;
		//facilityTreeHierarchy = plugloadGroupManager.loadPlugloadProfileHierarchy(emsAuthContext.getUserId(),true);
		plugloadProfileHierachy = plugloadGroupManager.loadPlugloadProfileHierarchy(true);
    	model.addAttribute("plugloadProfileHierarchy", plugloadProfileHierachy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jTreeOptions", jsTreeOptions);		
        return "devices/plugloads/assignplugloadprofile/dialog";
    }
	
	@RequestMapping("/assignplugloadprofiletoplugloadsemployeerole.ems")
	 public String assignprofiletoplugloadsemployeerole(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie){
      FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
      Long id = cookieHandler.getFacilityId();
      model.addAttribute("pid", id);
      return "devices/plugloads/assignprofileEmployee/dialog";
	}
	
}
