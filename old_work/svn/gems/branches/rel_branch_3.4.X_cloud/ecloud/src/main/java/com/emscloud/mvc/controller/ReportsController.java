package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.service.FacilityTreeManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

@Controller
@RequestMapping("/reports")
public class ReportsController {
    @Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
    private TreeNode<FacilityType> facilityTreeHierarchy;
    @Resource
    FacilityTreeManager facilityTreeManager;

    @RequestMapping("/auditlog.ems")
    public String getAuditLog(Model model) {
        return "reports/auditlog";
    }

    @RequestMapping(value = "/auditfilter.ems", method = RequestMethod.GET)
    public String filterAudits(Model model) {
        List<String> actions = new ArrayList<String>();
        for (CloudAuditActionType type : CloudAuditActionType.values()) {
            actions.add(type.getName());
        }
        model.addAttribute("actions", actions);
        return "reports/auditfilter";
    }

    @RequestMapping(value = "/presense.ems", method = RequestMethod.GET)
    public String presenseReport(Model model, @RequestParam("customerId") long customerId) {
        model.addAttribute("facilityTreeHierarchy", getTreeHierarchy(model, customerId));
        model.addAttribute("enableSensorProfile", false);
        model.addAttribute("customerId", customerId);
        return "reports/presense";
    }

    private TreeNode<FacilityType> getTreeHierarchy(Model model, long custId) {
        facilityTreeHierarchy = null;
        facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchyByCustomerId(custId);
        return facilityTreeHierarchy;
    }

    @RequestMapping(value = "/occupancyreport.ems", method = RequestMethod.GET)
    public String occupancyReportFloorLevel(Model model, @RequestParam("customerId") long customerId) {
        model.addAttribute("customerId", customerId);
        return "report/occupancyreport";
    }

    @RequestMapping(value = "/occupancyreportnonfloorlevel.ems", method = RequestMethod.GET)
    public String occupancyReportNonFloorLevel(Model model, @RequestParam("customerId") long customerId) {
        model.addAttribute("customerId", customerId);
        return "report/occupancyreportnonfloorlevel";
    }

    @RequestMapping(value = "/occupancymaps.ems", method = RequestMethod.GET)
    public String occupancyMaps(Model model, @RequestParam("customerId") long customerId) {
        model.addAttribute("customerId", customerId);
        return "report/occupancymaps";
    }
}