package com.ems.mvc.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.CookieGenerator;

import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.FacilityTreeManager;
import com.ems.service.GroupManager;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Resource
    FacilityTreeManager facilityTreeManager;

    @Resource(name = "emsAuthContext")
    private EmsAuthenticationContext emsAuthContext;

    @Resource
    GroupManager groupManager;
    
/*    @RequestMapping("/home.ems")
    public String getFacilities(Model model) {
        TreeNode companyHierarchy = facilityTreeManager.loadCompanyHierarchy();
        model.addAttribute("companyHierarchy", companyHierarchy);
        return "reports/home";
    }
    */
    
    @RequestMapping("/usagebyprofile.ems")
    public String getFacilitiesForUsageByProfile(Model model,
            @CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie,
            HttpServletResponse httpResponse) {
        // To get facility tree populated
        model.addAttribute("facilityTreeHierarchy", getTreeHierarchy(model, cookie, httpResponse));

        // To get facility tree populated
        //model.addAttribute("profileTreeHierarchy", getProfileTreeHierarchy());

        // To send current user tenant details to view page
        model.addAttribute("tenant", emsAuthContext.getCurrentTenant());

        model.addAttribute("viewTreeOnly", true);

        return "reports/usagebyprofile";
    }
    
    @RequestMapping("/outage.ems")
    public String getFacilitiesForOutage(Model model,
            @CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie,
            HttpServletResponse httpResponse) {
        // To get facility tree populated
        model.addAttribute("facilityTreeHierarchy", getTreeHierarchy(model, cookie, httpResponse));

        // To send current user tenant details to view page
        model.addAttribute("tenant", emsAuthContext.getCurrentTenant());

        model.addAttribute("viewTreeOnly", true);
        
        return "reports/outage";
    }
    
    @RequestMapping("/bulb.ems")
    public String getFacilitiesForBulb(Model model,
            @CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie,
            HttpServletResponse httpResponse) {
        // To get facility tree populated
        model.addAttribute("facilityTreeHierarchy", getTreeHierarchy(model, cookie, httpResponse));

        // To send current user tenant details to view page
        model.addAttribute("tenant", emsAuthContext.getCurrentTenant());

        model.addAttribute("viewTreeOnly", true);
        
        return "reports/bulb";
    }
    
    private TreeNode<FacilityType> getTreeHierarchy(Model model, String cookie, HttpServletResponse httpResponse) {

        TreeNode<FacilityType> facilityTreeHierarchy = null;

        switch (emsAuthContext.getCurrentUserRoleType()) {
            case Admin:
            case FacilitiesAdmin:
            case Auditor: {
                facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
                break;
            }
            default: {
                if (emsAuthContext.getCurrentTenant() != null) {
                    facilityTreeHierarchy = facilityTreeManager.loadTenantFacilitiesHierarchy(emsAuthContext
                            .getCurrentTenant().getId());
                } else {
                    facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
                }
                break;
            }
        }

        // check if cookie is not already set then find the default node in tree and store in cookie for js-tree.
        if (cookie == null || "".equals(cookie)) {
            String nodeId = FacilityCookieHandler.getDefaultNodeIdToSelect(facilityTreeHierarchy);

            CookieGenerator generator = new CookieGenerator();
            generator.setCookieName(FacilityCookieHandler.selectedFacilityCookie);
            generator.addCookie(httpResponse, nodeId);
        }
        return facilityTreeHierarchy;
    }
    
    // Following added by Nitin to get profile tree view details

    /*private TreeNode<FacilityType> getProfileTreeHierarchy() {
        TreeNode<FacilityType> ProfileTreeHierarchy = null;
        ProfileTreeHierarchy = groupManager.loadProfileHierarchy();
        return ProfileTreeHierarchy;
    }*/
}