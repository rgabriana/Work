package com.ems.mvc.controller;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.CookieGenerator;

import com.ems.annotaion.InvalidateFacilityTreeCache;
import com.ems.model.SystemConfiguration;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.model.UserLocations;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.FacilityTreeManager;
import com.ems.service.GroupManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;

@Controller
@RequestMapping("/facilities")
public class FacilitiesController {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource
	FacilityTreeManager facilityTreeManager;

	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;

	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;

	@Resource
	TenantManager tenantManager;

	@Resource
	GroupManager groupManager;

	@Resource
	UserManager userManager;

	@RequestMapping("/home.ems")
	public String getFacilities(
			Model model,
			@CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie,
			HttpServletResponse httpResponse) {
		// To get facility tree populated
		model.addAttribute("facilityTreeHierarchy",
				getTreeHierarchy(model, cookie, httpResponse));

		// To get profile tree populated
		model.addAttribute("profileTreeHierarchy", getProfileTreeHierarchy());

		// To send current user tenant details to view page
		model.addAttribute("tenant", emsAuthContext.getCurrentTenant());

		SystemConfiguration config = systemConfigurationManager
				.loadConfigByName("menu.bacnet.show");
		model.addAttribute("showBacnet", config.getValue());
		model.addAttribute("showOpenADR", systemConfigurationManager
				.loadConfigByName("menu.openADR.show").getValue());

		RoleType role = emsAuthContext.getCurrentUserRoleType();
		model.addAttribute("role", role.getName().toLowerCase());

		return "facilities/home";
	}

	@RequestMapping("/tree.ems")
	public String getTree(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
			HttpServletResponse httpResponse) {

		model.addAttribute("facilityTreeHierarchy",
				getTreeHierarchy(model, cookie, httpResponse));
		return "facilities/tree";
	}

	private TreeNode<FacilityType> getTreeHierarchy(Model model, String cookie,
			HttpServletResponse httpResponse) {

		TreeNode<FacilityType> facilityTreeHierarchy = null;

		// switch (emsAuthContext.getCurrentUserRoleType()) {
		// case Admin:
		// case FacilitiesAdmin:
		// case Auditor: {
		// facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
		// break;
		// }
		// default: {
		// if (emsAuthContext.getCurrentTenant() != null) {
		// facilityTreeHierarchy =
		// facilityTreeManager.loadTenantFacilitiesHierarchy(emsAuthContext
		// .getCurrentTenant().getId());
		// } else {
		// facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
		// }
		// break;
		// }
		// }
		if(emsAuthContext.getCurrentUserRoleType().equals(RoleType.Admin))
		{
			facilityTreeHierarchy =facilityTreeManager.loadCompanyHierarchy();
		}
		else
		{
		long currentUserId = emsAuthContext.getUserId();
		facilityTreeHierarchy = facilityTreeManager
				.loadFacilityHierarchyForUser(currentUserId);
		}
		// check if cookie is not already set then find the default node in tree
		// and store in cookie for js-tree.
		if (cookie == null || "".equals(cookie)) {
			String nodeId = FacilityCookieHandler
					.getDefaultNodeIdToSelect(facilityTreeHierarchy);

			CookieGenerator generator = new CookieGenerator();
			generator
					.setCookieName(FacilityCookieHandler.selectedFacilityCookie);
			generator.addCookie(httpResponse, nodeId);
		}

		return facilityTreeHierarchy;
	}

	@RequestMapping("/tenant_tree_assignment.ems")
	public String getTenantAssignmentTree(Model model) {
		
		long currentUserId = emsAuthContext.getUserId();
		TreeNode<FacilityType>  facilityTreeHierarchy = facilityTreeManager
				.loadFacilityHierarchyForUser(currentUserId);
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);

		List<Tenant> tenantsList = tenantManager.getAllTenants();
		model.addAttribute("tenantsList", tenantsList);
		return "facilities/tenant/treeAssignment";
	}

	@RequestMapping("/{tenantId}/get_tenant_tree.ems")
	public String getTenantTree(Model model,
			@PathVariable("tenantId") Long tenantId) {
		TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
				.loadTenantFacilitiesHierarchy(tenantId);
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);

		model.addAttribute("tenantId", tenantId);

		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(false);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		return "facilities/tenant/treeAssignment";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/save_tenant_locations.ems", method = RequestMethod.POST)
	public String saveTenantLocationsAssignment(Model model,
			@RequestParam("selectedFacilities") String selectedFacilities) {

		String[] assignedFacilities = selectedFacilities.split(",");
		facilityTreeManager.setTenantFacilities(assignedFacilities);
		userAuditLoggerUtil.log("Changed tenant assignments",
				UserAuditActionType.Tenant_Facility.getName());
		return "redirect:/tenants/list.ems?facilityAssignmentStatus=success";
	}

	// ---------------------------------------------------------------
	// Added by Nitin
	// Following is to get profile tree view details
	// ---------------------------------------------------------------

	private TreeNode<FacilityType> getProfileTreeHierarchy() {
		TreeNode<FacilityType> ProfileTreeHierarchy = null;
		ProfileTreeHierarchy = groupManager.loadProfileHierarchy();
		return ProfileTreeHierarchy;
	}

	// ---------------------------------------------------------------
	// Added by Nitin
	// Following is to get facilities assigned to user
	// ---------------------------------------------------------------

	@RequestMapping("/{userId}/assignFacilityUser.ems")
	public String getUserTree(Model model, @PathVariable("userId") Long userId) {
		Long tenantId = 0L;
		TreeNode<FacilityType> facilityTreeHierarchy = null;
		;


		long currentUserId = emsAuthContext.getUserId();

		if (emsAuthContext.getCurrentUserRoleType() != RoleType.Admin) {
			facilityTreeHierarchy = facilityTreeManager
					.loadFacilityHierarchyForUser(currentUserId);
		} else {
			 //If user belongs to any tenant than load tenant's assigned tree else
			 // whole company tree.
			 Tenant tenant = userManager.loadUserById(userId).getTenant();
			 if(tenant != null) {
			 tenantId = tenant.getId();
			 facilityTreeHierarchy =
			 facilityTreeManager.loadTenantFacilitiesHierarchy(tenantId);
			 } else {
			 facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
			 }
		}

		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
		model.addAttribute("userId", userId);

		// Let's get the selected nodes
		User user = userManager.loadUserById(userId);
		Set<UserLocations> userLocations = user.getUserLocations();
		model.addAttribute("userLocations", userLocations);

		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		return "facilities/user/treeAssignment";
	}

	// ---------------------------------------------------------------
	// Added by Nitin
	// Following is to save facilities assigned to user
	// ---------------------------------------------------------------

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/save_user_locations.ems", method = RequestMethod.POST)
	public String saveUserLocationsAssignment(Model model,
			@RequestParam("selectedFacilities") String selectedFacilities,
			@RequestParam("userId") Long userId) {

		String[] assignedFacilities = selectedFacilities.split(",");
		facilityTreeManager.setUserFacilities(assignedFacilities, userId);
		userAuditLoggerUtil.log("Changed user assignments",
				UserAuditActionType.Tenant_Facility.getName());

		Tenant tenant = userManager.loadUserById(userId).getTenant();
		if (tenant != null) {
			return "redirect:/users/list.ems?tenantId=" + tenant.getId()+"&facilityAssignmentStatus=success";
		} else {
			return "redirect:/users/list.ems?facilityAssignmentStatus=success";
		}
	}
}
