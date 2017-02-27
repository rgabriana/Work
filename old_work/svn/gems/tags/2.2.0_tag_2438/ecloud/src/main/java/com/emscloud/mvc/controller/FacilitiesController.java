package com.emscloud.mvc.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.CookieGenerator;

import com.emscloud.mvc.util.FacilityCookieHandler;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.FacilityTreeManager;
import com.emscloud.types.FacilityType;
import com.emscloud.utils.tree.TreeNode;

@Controller
@RequestMapping("/facilities")
public class FacilitiesController {

	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@RequestMapping("/home.ems")
	public String getFacilities(
			Model model,
			@CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie,
			HttpServletResponse httpResponse) {
		// To get facility tree populated
		model.addAttribute("facilityTreeHierarchy",
				getTreeHierarchy(model, cookie, httpResponse));
		return "facilities/home";
	}

	@RequestMapping("/tree.ems")
	public String getTree(
			Model model,
			@CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie,
			HttpServletResponse httpResponse) {

		model.addAttribute("facilityTreeHierarchy",
				getTreeHierarchy(model, cookie, httpResponse));
		return "facilities/tree";
	}

	private TreeNode<FacilityType> getTreeHierarchy(Model model, String cookie,
			HttpServletResponse httpResponse) {

		TreeNode<FacilityType> facilityTreeHierarchy = null;

		//if(emsAuthContext.getCurrentUserRoleType().equals(RoleType.Admin))
		//{
			facilityTreeHierarchy =facilityTreeManager.loadCustomerHierarchy();
		//}
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

}
