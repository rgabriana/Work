package com.ems.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.CookieGenerator;

import com.ems.model.Ballast;
import com.ems.model.Bulb;
import com.ems.model.Fixture;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.InventoryDeviceService;
import com.ems.service.LocatorDeviceManager;
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
    @Resource
    FixtureManager fixtureManager;
    @Resource
    BallastManager ballastManager;
    @Resource
    BulbManager bulbManager;
    @Resource
    LocatorDeviceManager locatorDeviceManager;
    @Resource
    FixtureClassManager fixtureClassManager;
/*    @RequestMapping("/home.ems")
    public String getFacilities(Model model) {
        TreeNode companyHierarchy = facilityTreeManager.loadCompanyHierarchy();
        model.addAttribute("companyHierarchy", companyHierarchy);
        return "reports/home";
    }
    */
    
    @PreAuthorize("hasAnyRole('Admin','Auditor','TenantAdmin','FacilitiesAdmin')")
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
    
    @PreAuthorize("hasAnyRole('Admin','Auditor','TenantAdmin','FacilitiesAdmin')")
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
    
    @PreAuthorize("hasAnyRole('Admin','Auditor','TenantAdmin','FacilitiesAdmin')")
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
    
    @PreAuthorize("hasAnyRole('Admin','Auditor','FacilitiesAdmin', 'TenantAdmin')")
    @RequestMapping("/auditlog.ems")
    public String getAuditLog(Model model) {
        return "reports/auditlog";
    }
    
    private TreeNode<FacilityType> getTreeHierarchy(Model model, String cookie, HttpServletResponse httpResponse) {

        TreeNode<FacilityType> facilityTreeHierarchy = null;

//        switch (emsAuthContext.getCurrentUserRoleType()) {
//            case Admin:
//            case FacilitiesAdmin:
//            case Auditor: {
//                facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
//                break;
//            }
//            default: {
//                if (emsAuthContext.getCurrentTenant() != null) {
//                    facilityTreeHierarchy = facilityTreeManager.loadTenantFacilitiesHierarchy(emsAuthContext
//                            .getCurrentTenant().getId());
//                } else {
//                    facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
//                }
//                break;
//            }
//        }

        long currentUserId = emsAuthContext.getUserId();
        facilityTreeHierarchy = facilityTreeManager.loadFacilityHierarchyForUser(currentUserId);
        
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
	@SuppressWarnings("unchecked")
    @RequestMapping("/inventoryreport.ems")
    public String assignprofiletofixturesemployeerole(Model model){
	    HashMap<String, Long> totalCommissionedSensors = fixtureManager.getFixturesCountByModelNo();
	    HashMap<String, Long> totalOtherDevices = locatorDeviceManager.getOtherDevicesCount();
	    List<Object[]> totalBallastAssociated = ballastManager.getBallastCountByBallastName();
        List<Object[]> totalBulbsAssociated = bulbManager.getBulbsCountByBulbName();
        List<Object[]> totalCommissionedCus = fixtureManager.getCusCountByVersionNo();
        List<Object[]> totalFxTypeAssociated = fixtureClassManager.getCommissionedFxTypeCount();
        Long totalCommissionedSensorsCount = totalCommissionedSensors.get("TotalCount");
        Long totalOtherDeviceCount = totalOtherDevices.get("TotalCount");
        Long totalBallastAssociatedCount = (long) 0;
        Long totalLampsAssociatedCount = (long) 0;
        Long totalFxTypeAssociatedCount = (long) 0;
        Long totalCommissionedCuCount = (long) 0;
        Long totalBallastsCount = (long) 0;
        Long totalBulbsCount= (long) 0;
        //Calculate Total Cus Associated
        if (totalCommissionedCus != null && !totalCommissionedCus.isEmpty()) {
            Iterator<Object[]> iterator = totalCommissionedCus.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Long count = ((BigInteger) itrObject[1]).longValue();
                totalCommissionedCuCount+= count;
            }
        }
        
        //Calculate Total Ballast Associated
        if (totalBallastAssociated != null && !totalBallastAssociated.isEmpty()) {
            Iterator<Object[]> iterator = totalBallastAssociated.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Long count = (Long) itrObject[2];
                Long fxcount = (Long) itrObject[3];
                totalBallastsCount+= count;
                totalBallastAssociatedCount+= fxcount;
            }
        }
        
      //Calculate Total bulbs Associated
        if (totalBulbsAssociated != null && !totalBulbsAssociated.isEmpty()) {
            Iterator<Object[]> iterator1 = totalBulbsAssociated.iterator();
            while (iterator1.hasNext()) {
                Object[] itrObject1 = (Object[]) iterator1.next();
                Long count = ((BigInteger) itrObject1[2]).longValue();
                Long fxcount = ((BigInteger) itrObject1[3]).longValue();
                totalBulbsCount+= count;
                totalLampsAssociatedCount+= fxcount;
            }
        }
        
      //Calculate Total FxType Associated
        if (totalFxTypeAssociated != null && !totalFxTypeAssociated.isEmpty()) {
            Iterator<Object[]> iterator1 = totalFxTypeAssociated.iterator();
            while (iterator1.hasNext()) {
                Object[] itrObject1 = (Object[]) iterator1.next();
                Long count = ((BigInteger) itrObject1[0]).longValue();
                totalFxTypeAssociatedCount+= count;
            }
        }
        
        totalCommissionedSensors.remove("TotalCount");
        totalOtherDevices.remove("TotalCount");
        model.addAttribute("totalCommissionedSensorsCount", totalCommissionedSensorsCount);
        model.addAttribute("totalOtherDeviceCount", totalOtherDeviceCount);
        model.addAttribute("totalBallastAssociatedCount", totalBallastAssociatedCount);
        model.addAttribute("totalLampsAssociatedCount", totalLampsAssociatedCount);
        model.addAttribute("totalFxTypeAssociatedCount", totalFxTypeAssociatedCount);
        model.addAttribute("totalCommissionedCuCount", totalCommissionedCuCount);
        model.addAttribute("totalBallastsCount", totalBallastsCount);
        model.addAttribute("totalBulbsCount", totalBulbsCount);
        
        model.addAttribute("totalCommissionedSensors", totalCommissionedSensors);
        model.addAttribute("totalOtherDevices", totalOtherDevices);
        model.addAttribute("totalCommissionedCus", totalCommissionedCus);
        model.addAttribute("totalBallastAssociated", totalBallastAssociated);
        model.addAttribute("totalBulbsAssociated", totalBulbsAssociated);
        model.addAttribute("totalFxTypeAssociated", totalFxTypeAssociated);
      return "reports/inventoryreport";
   }
}