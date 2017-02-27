package com.ems.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.CookieGenerator;

import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.FacilityTreeManager;
import com.ems.service.GroupManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
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
    
    @RequestMapping("/auditlog.ems")
    public String getAuditLog(Model model) {
        return "reports/auditlog";
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
    
    // Following added by Nitin to get content of a log file
    
    public String readfile(String filepath){
        File file = new File(filepath);
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            // repeat until all lines is read
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException e) {
            //System.out.print(">>>> msg1: " + e.toString()); 
            e.printStackTrace();
        } catch (IOException e) {
            //System.out.print(">>>> msg2: " + e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                //System.out.print(">>>> msg3: " + e.toString());
                e.printStackTrace();
            }
        }
        return contents.toString().trim().replace("\r", "").replace("\n", "");
    }
    
    
	@RequestMapping(value = "/auditfilter.ems", method = RequestMethod.GET)
	public String filterAudits(Model model) {
		List<String> actions = new ArrayList<String>();
		for (UserAuditActionType type: UserAuditActionType.values()) {
			actions.add(type.getName());
		}
		model.addAttribute("actions", actions);
		return "reports/auditfilter";
	}
}