package com.ems.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.CookieGenerator;

import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.security.EmsAuthenticationContext;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.LocatorDeviceManager;
import com.ems.service.PlugloadManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.WdsManager;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.CommonUtils;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Resource
    FacilityTreeManager facilityTreeManager;
    
    @Autowired
    private MessageSource messageSource;

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
    @Resource
	PlugloadManager plugloadManager ;
    
    @Resource
	WdsManager wdsManager ;
    
    @Resource
   	SystemConfigurationManager systemConfigurationManager ;
    
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
            HttpServletResponse httpResponse) throws EmsValidationException {
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "faclities.em_facilites_jstree_select", cookie);
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
            HttpServletResponse httpResponse) throws EmsValidationException {
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "faclities.em_facilites_jstree_select", cookie);
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
            HttpServletResponse httpResponse) throws EmsValidationException {
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "faclities.em_facilites_jstree_select", cookie);
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
	    Long mPlugloadListCount = plugloadManager.loadAllCommissionedPlugloadsCount();
	    List<Object[]> totalBallastAssociated = ballastManager.getBallastCountByBallastName();
        List<Object[]> totalBulbsAssociated = bulbManager.getBulbsCountByBulbName();
        List<Object[]> totalCommissionedCus = fixtureManager.getCusCountByVersionNo();
        List<Object[]> totalFxTypeAssociated = fixtureClassManager.getCommissionedFxTypeCount();
        List<Object[]> totalErcAssociated = wdsManager.getErcCountByVersionNo();
        Long totalCommissionedSensorsCount = totalCommissionedSensors.get("TotalCount");
        Long totalOtherDeviceCount = totalOtherDevices.get("TotalCount");
        Long totalBallastAssociatedCount = (long) 0;
        Long totalLampsAssociatedCount = (long) 0;
        Long totalFxTypeAssociatedCount = (long) 0;
        Long totalCommissionedCuCount = (long) 0;
        Long totalBallastsCount = (long) 0;
        Long totalBulbsCount= (long) 0;
        Long totalErcCount= (long) 0;
        totalOtherDevices.put("Plugload", mPlugloadListCount);
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
        
        //Calculate Total ERC Associated
        if (totalErcAssociated != null && !totalErcAssociated.isEmpty()) {
            Iterator<Object[]> erciterator = totalErcAssociated.iterator();
            while (erciterator.hasNext()) {
                Object[] itrObject = (Object[]) erciterator.next();
                Long count = ((BigInteger) itrObject[1]).longValue();
                totalErcCount+= count;
            }
        }
        
        totalCommissionedSensors.remove("TotalCount");
        totalOtherDevices.remove("TotalCount");
        model.addAttribute("totalCommissionedSensorsCount", totalCommissionedSensorsCount);
        model.addAttribute("totalOtherDeviceCount", totalOtherDeviceCount+mPlugloadListCount);
        model.addAttribute("totalBallastAssociatedCount", totalBallastAssociatedCount);
        model.addAttribute("totalLampsAssociatedCount", totalLampsAssociatedCount);
        model.addAttribute("totalFxTypeAssociatedCount", totalFxTypeAssociatedCount);
        model.addAttribute("totalCommissionedCuCount", totalCommissionedCuCount);
        model.addAttribute("totalBallastsCount", totalBallastsCount);
        model.addAttribute("totalBulbsCount", totalBulbsCount);
        model.addAttribute("totalPlugloadCount",mPlugloadListCount);
        model.addAttribute("totalErcCount",totalErcCount);
        
        model.addAttribute("totalCommissionedSensors", totalCommissionedSensors);
        model.addAttribute("totalOtherDevices", totalOtherDevices);
        model.addAttribute("totalCommissionedCus", totalCommissionedCus);
        model.addAttribute("totalErcAssociated", totalErcAssociated);
        model.addAttribute("totalBallastAssociated", totalBallastAssociated);
        model.addAttribute("totalBulbsAssociated", totalBulbsAssociated);
        model.addAttribute("totalFxTypeAssociated", totalFxTypeAssociated);
      return "reports/inventoryreport";
   }
	
	@PreAuthorize("hasAnyRole('Admin','Auditor','FacilitiesAdmin', 'TenantAdmin')")
    @RequestMapping("/ercbatteryreport.ems")
    public String getErcbatteryReport(Model model) {
		
		SystemConfiguration ercBatteryReportSchedulerEnable = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.enable");
		
		if(ercBatteryReportSchedulerEnable != null && ercBatteryReportSchedulerEnable.getValue() != null && !"".equals(ercBatteryReportSchedulerEnable.getValue())) {
			model.addAttribute( "ercBatteryReportSchedulerEnable", ercBatteryReportSchedulerEnable.getValue());
		}else{
			model.addAttribute( "ercBatteryReportSchedulerEnable", "false");
		}
		
		SystemConfiguration ercBatteryReportSchedulerEmail = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.email");
		
		if(ercBatteryReportSchedulerEmail != null && ercBatteryReportSchedulerEmail.getValue() != null && !"".equals(ercBatteryReportSchedulerEmail.getValue())) {
			model.addAttribute( "ercBatteryReportSchedulerEmail", ercBatteryReportSchedulerEmail.getValue());
		}else{
			model.addAttribute( "ercBatteryReportSchedulerEmail", "");
		}
		
		SystemConfiguration ercBatteryReportSchedulerCronexpression = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.cronexpression");
		
		if(ercBatteryReportSchedulerCronexpression != null && ercBatteryReportSchedulerCronexpression.getValue() != null && !"".equals(ercBatteryReportSchedulerCronexpression.getValue())) {
			String[] ercBatteryReportSchedulerCronexpressionParts = ercBatteryReportSchedulerCronexpression.getValue().split(" ");
			
			SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");
		    SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
		    Date date = null;
			try {
				date = parseFormat.parse(ercBatteryReportSchedulerCronexpressionParts[2]+":"+ercBatteryReportSchedulerCronexpressionParts[1]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			model.addAttribute( "ercBatteryReportTime", displayFormat.format(date));
			model.addAttribute( "ercBatteryReportRecurrence", ercBatteryReportSchedulerCronexpressionParts[5]);
		}else{
			model.addAttribute( "ercBatteryReportTime", "");
			model.addAttribute( "ercBatteryReportRecurrence", "");
		}
		
        return "reports/ercbatteryreport";
    }
}