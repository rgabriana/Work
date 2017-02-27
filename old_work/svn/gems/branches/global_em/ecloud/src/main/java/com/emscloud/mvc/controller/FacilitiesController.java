package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RequestMapping;

import com.emscloud.annotation.InvalidateFacilityTreeCache;
import com.emscloud.model.Facility;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FacilityTreeManager;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.FacilityType;
import com.emscloud.types.RoleType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.FacilityCookieHandler;
import com.emscloud.util.tree.TreeNode;


@Controller
@RequestMapping("/facilities")
public class FacilitiesController {
	
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@Resource
	FacilityManager facilityManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	CustomerManager customerManager;
	@Resource
	ProfileGroupManager profileGroupManager;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	private TreeNode<FacilityType> facilityTreeHierarchy;
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;	
	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/home.ems", method = { RequestMethod.GET, RequestMethod.POST })
	public String getFacilities(Model model,@RequestParam("customerId") long customerId) {
		// To get profile tree populated
		SystemConfiguration sensorProfileEnable = systemConfigurationManager.loadConfigByName("FEATURE_SENSOR_PROFILE");
        if (sensorProfileEnable != null && "true".equalsIgnoreCase(sensorProfileEnable.getValue())) {
            model.addAttribute("enableSensorProfile", sensorProfileEnable.getValue());
           // model.addAttribute("profileTreeHierarchy", getProfileTreeHierarchy());
            model.addAttribute("glemProfileTreeHierarchy", getGlemProfileTreeHierarchy(customerId));
        }
		SystemConfiguration occEnable = systemConfigurationManager.loadConfigByName("FEATURE_OCCUPANCY_AGGREGATION");
		String sValue = "false";
        if (occEnable != null) {
        	sValue = sensorProfileEnable.getValue();
        }
        model.addAttribute("occEnable", sValue);
        SystemConfiguration cloudMode = systemConfigurationManager.loadConfigByName("cloud.mode");
        model.addAttribute("cloudMode",cloudMode.getValue());
		model.addAttribute("facilityTreeHierarchy",getTreeHierarchy(model,customerId));
		model.addAttribute("customerId",customerId);
		return "facilities/home";
	}
	
	@RequestMapping(value = "/tree.ems", method = RequestMethod.GET)
	public String getTree(Model model,@RequestParam("customerId") long customerId) {

		model.addAttribute("facilityTreeHierarchy",
				getTreeHierarchy(model,customerId));
		
		return "facilities/tree";
	}
	
	private TreeNode<FacilityType> getTreeHierarchy(Model model,long custId) {

		facilityTreeHierarchy = null;
		
		facilityTreeHierarchy =facilityTreeManager.loadCompanyHierarchyByCustomerId(custId);
		
		return facilityTreeHierarchy;
	}
	
	@RequestMapping("/organization/setting.ems")
	public String orgSetting(Model model,@RequestParam("facilityId") long facilityId) {
		
		TreeNode<FacilityType> selectedFacilityNode ;
		model.addAttribute("mode", "admin");
		Facility facility = facilityManager.getFacility(facilityId);
		model.addAttribute("customerId",facility.getCustomerId());
		
		switch (FacilityType.getFacilityType(facility.getType())) {
		case ORGANIZATION: {
			model.addAttribute("facilityId", facility.getId());
			model.addAttribute("facilityType", FacilityType.getFacilityType(facility.getType()).getLowerCaseName());
			selectedFacilityNode = getFacilityByTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId());
			model.addAttribute("facilityName", selectedFacilityNode.getName());
			model.addAttribute("childFacilities", getChildFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			model.addAttribute("companyName", facilityTreeHierarchy.getName());
			model.addAttribute("siblingFacilities", getSiblingFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			return "createFacility";
		}
		case CAMPUS: {
			model.addAttribute("facilityId", facility.getId());
			model.addAttribute("facilityType", FacilityType.getFacilityType(facility.getType()).getLowerCaseName());
			selectedFacilityNode = getFacilityByTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId());
			model.addAttribute("facilityName", selectedFacilityNode.getName());
			model.addAttribute("childFacilities", getChildFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			model.addAttribute("companyName", facilityTreeHierarchy.getName());
			model.addAttribute("campusName", selectedFacilityNode.getName());
			model.addAttribute("siblingFacilities", getSiblingFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			return "createFacility";
		}
		case BUILDING: {
			model.addAttribute("facilityId", facility.getId());
			model.addAttribute("facilityType", FacilityType.getFacilityType(facility.getType()).getLowerCaseName());
			selectedFacilityNode = getFacilityByTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId());
			model.addAttribute("facilityName", selectedFacilityNode.getName());
			model.addAttribute("childFacilities", getChildFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			model.addAttribute("companyName", facilityTreeHierarchy.getName());
			model.addAttribute("campusName", getParentFacilityByChildFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()).getName());
			model.addAttribute("buildingName", selectedFacilityNode.getName());
			model.addAttribute("siblingFacilities", getSiblingFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			return "createFacility";
		}
		case FLOOR: {
			model.addAttribute("facilityId", facility.getId());
			model.addAttribute("facilityType", FacilityType.getFacilityType(facility.getType()).getLowerCaseName());
			selectedFacilityNode = getFacilityByTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId());
			model.addAttribute("facilityName", selectedFacilityNode.getName());
			model.addAttribute("childFacilities", getChildFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			model.addAttribute("companyName", facilityTreeHierarchy.getName());
			TreeNode<FacilityType> selectedBuilding = getParentFacilityByChildFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId());
			String campusName = getParentFacilityByChildFacilityTypeAndId(selectedBuilding.getNodeType().getLowerCaseName(),selectedBuilding.getNodeId()).getName();
			model.addAttribute("campusName", campusName);
			model.addAttribute("buildingName", selectedBuilding.getName());
			model.addAttribute("floorName", selectedFacilityNode.getName());
			model.addAttribute("siblingFacilities", getSiblingFacilitiessByFacilityTypeAndId(FacilityType.getFacilityType(facility.getType()).getLowerCaseName(),facility.getId()));
			return "createFacility";
		}
		default: {
			return "createFacility";
		}
		}
	}
	
	private List<TreeNode<FacilityType>> getSiblingFacilitiessByFacilityTypeAndId(String facilityType,Long facilityId){
		
		List<TreeNode<FacilityType>> facilities = new ArrayList<TreeNode<FacilityType>>();
		
		if("organization".equals(facilityType)){
			facilities.add(facilityTreeHierarchy);
			return facilities;
		}
		else if("campus".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				facilities.add(campus);
			}
			return facilities;
		}
		else if("building".equals(facilityType)){
			Long parentCampusId = getParentFacilityByChildFacilityTypeAndId(facilityType,facilityId).getNodeId();
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				if(campus.getNodeId().longValue() == parentCampusId.longValue()){
					for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
						facilities.add(building);
					}
				}
			}
			return facilities;
		}
		else if("floor".equals(facilityType)){
			Long parentBuildingId = getParentFacilityByChildFacilityTypeAndId(facilityType,facilityId).getNodeId();
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					if(building.getNodeId().longValue() == parentBuildingId.longValue()){
						for (TreeNode<FacilityType> floor : building.getTreeNodeList()){
							facilities.add(floor);
						}
					}
				}
			}
			return facilities;
		}
		else{
			return null;
		}
	}
	
	private List<TreeNode<FacilityType>> getChildFacilitiessByFacilityTypeAndId(String facilityType,Long facilityId){
		
		List<TreeNode<FacilityType>> facilities = new ArrayList<TreeNode<FacilityType>>();
		
		if("organization".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
					facilities.add(campus);
			}
			return facilities;
		}
		else if("campus".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				if(campus.getNodeId().longValue() == facilityId.longValue()){
					for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
						facilities.add(building);
					}
					break;
				}
			}
			return facilities;
		}
		else if("building".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					if(building.getNodeId().longValue() == facilityId.longValue()){
						for (TreeNode<FacilityType> floor : building.getTreeNodeList()){
							facilities.add(floor);
						}
						break;
					}
				}
			}
			return facilities;
		}
		else if("floor".equals(facilityType)){
			return facilities;
		}
		else{
			return null;
		}
	}
	
	private TreeNode<FacilityType> getParentFacilityByChildFacilityTypeAndId(String facilityType,Long facilityId) {
		
		TreeNode<FacilityType> selectedParentFacility = new TreeNode<FacilityType>();
		
		if("campus".equals(facilityType)){
			selectedParentFacility = facilityTreeHierarchy;
			return selectedParentFacility;
		}
		else if("building".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					if(building.getNodeId().longValue() == facilityId.longValue()){
						selectedParentFacility = campus;
						break;
					}
				}
			}
			return selectedParentFacility;
		}
		else if("floor".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					for (TreeNode<FacilityType> floor : building.getTreeNodeList()){
						if(floor.getNodeId().longValue() == facilityId.longValue()){
							selectedParentFacility = building;
							break;
						}
					}
				}
			}
			return selectedParentFacility;
		}
		else{
			return null;
		}
		
	}
	
	private TreeNode<FacilityType> getFacilityByTypeAndId(String facilityType,Long facilityId) {
		
		TreeNode<FacilityType> selectedFacility = new TreeNode<FacilityType>();
		
		if("organization".equals(facilityType)){
			selectedFacility = facilityTreeHierarchy;
			return selectedFacility;
		}
		else if("campus".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				if(campus.getNodeId().longValue() == facilityId.longValue()){
					selectedFacility = campus;
					break;
				}
			}
			return selectedFacility;
		}
		else if("building".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					if(building.getNodeId().longValue() == facilityId.longValue()){
						selectedFacility = building;
						break;
					}
				}
			}
			return selectedFacility;
		}
		else if("floor".equals(facilityType)){
			for (TreeNode<FacilityType> campus : facilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					for (TreeNode<FacilityType> floor : building.getTreeNodeList()){
						if(floor.getNodeId().longValue() == facilityId.longValue()){
							selectedFacility = floor;
							break;
						}
					}
				}
			}
			return selectedFacility;
		}
		else{
			return null;
		}
		
	}
	
	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addChildFacility.ems", method = RequestMethod.POST)
	public String addChildFacility(Model model,
			@RequestParam("parentFacilityId") Long parentFacilityId,
			@RequestParam("facilityName") String childFacilityName,@RequestParam("facilityType") String childFacilityType) {
		Facility parentFacility = facilityManager.getFacility(parentFacilityId);
		String parentFacilityTypeString = FacilityType.getFacilityType(parentFacility.getType()).getLowerCaseName();
		Facility childFacility = new Facility();
		childFacility.setName(childFacilityName);
		childFacility.setParentId(parentFacilityId);
		childFacility.setType(FacilityType.getFacilityType(FacilityType.valueOf(childFacilityType.toUpperCase())));
		childFacility.setCustomerId(parentFacility.getCustomerId());
		facilityManager.addFacility(childFacility);
		cloudAuditLoggerUtil.log("Created "+childFacilityType+" with name "+ childFacility.getName() +" for Customer "+customerManager.loadCustomerById(parentFacility.getCustomerId()).getName(), CloudAuditActionType.Facility_Create.getName());
		return "redirect:/facilities/organization/setting.ems?facilityId="+parentFacilityId+"&refreshTree=true";
	}
	
	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editFacilityName.ems", method = RequestMethod.POST)
	public String editFacilityName(Model model,
			@RequestParam("facilityId") Long facilityId,
			@RequestParam("facilityName") String facilityName,@RequestParam("facilityType") String facilityType) {
		Facility savedFacility = facilityManager.getFacility(facilityId);
		String oldName = savedFacility.getName();
		String savedFacilityTypeString = FacilityType.getFacilityType(savedFacility.getType()).getLowerCaseName();
		savedFacility.setName(facilityName);
		facilityManager.editFacility(savedFacility);
		cloudAuditLoggerUtil.log("Updated "+savedFacilityTypeString+" name from : " + oldName +" to " + facilityName +" for Customer "+customerManager.loadCustomerById(savedFacility.getCustomerId()).getName(), CloudAuditActionType.Facility_Update.getName());
		return "redirect:/facilities/organization/setting.ems?facilityId="+savedFacility.getId()+"&refreshTree=true";
	}
	
	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editChildFacilityName.ems", method = RequestMethod.POST)
	public String editChildFacilityName(Model model,
			@RequestParam("facilityId") Long facilityId,
			@RequestParam("facilityName") String facilityName,@RequestParam("facilityType") String facilityType) {
		Facility savedFacility = facilityManager.getFacility(facilityId);
		Long parentFacilityId = savedFacility.getParentId();
		String oldName = savedFacility.getName();
		String savedFacilityTypeString = FacilityType.getFacilityType(savedFacility.getType()).getLowerCaseName();
		savedFacility.setName(facilityName);
		facilityManager.editFacility(savedFacility);
		cloudAuditLoggerUtil.log("Updated "+savedFacilityTypeString+" name from : " + oldName +" to " + facilityName +" for Customer "+customerManager.loadCustomerById(savedFacility.getCustomerId()).getName(), CloudAuditActionType.Facility_Update.getName());
		return "redirect:/facilities/organization/setting.ems?facilityId="+parentFacilityId+"&refreshTree=true";
	}
	private TreeNode<FacilityType> getProfileTreeHierarchy() {
		TreeNode<FacilityType> ProfileTreeHierarchy = null;
		// If visibility check is applied, tree will load all profiles which are in visible state
		boolean visibilityCheck =true;
		System.out.println("enlightedAuthenticationContext.getCurrentUserRoleType() " + emsAuthContext.getCurrentUserRoleType());
		if(emsAuthContext.getCurrentUserRoleType().equals(RoleType.Admin)){
			ProfileTreeHierarchy = profileGroupManager.loadProfileHierarchy(visibilityCheck);
		}
		else{
			long currentUserId = emsAuthContext.getUserId();
			ProfileTreeHierarchy = profileGroupManager.loadProfileHierarchyForUser(currentUserId,visibilityCheck);
		}
		return ProfileTreeHierarchy;
	}
	
	private TreeNode<FacilityType> getGlemProfileTreeHierarchy(long customerId)
	{
		TreeNode<FacilityType> glemProfileTreeHierarchy = null;
		boolean visibilityCheck =true;
		if(emsAuthContext.getCurrentUserRoleType().equals(RoleType.Admin)){
			glemProfileTreeHierarchy = profileGroupManager.loadGlemProfileHierarchy(visibilityCheck,customerId);
		}
		return glemProfileTreeHierarchy;
		
	}
	
	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/delete/refresh.ems", method = RequestMethod.POST)
	public String refreshTree(Model model,@RequestParam("deletedParentFacilityId") Long deletedParentFacilityId) {
		return "redirect:/facilities/organization/setting.ems?facilityId="+deletedParentFacilityId+"&refreshTree=true";
	}
	@RequestMapping("/profiletree.ems")
    public String getProfileTree(
        Model model,
        @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
        HttpServletResponse httpResponse) {
        // To get profile tree populated
        model.addAttribute("profileTreeHierarchy", getProfileTreeHierarchy());
        // REMOVE BELOW CODE ONCE TREE REFRESH LOGIC IS INTEGRATED
        model.addAttribute("facilityTreeHierarchy",	getTreeHierarchy(model, 1));
        return "facilities/tree";
    }	
}
