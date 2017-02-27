package com.emsdashboard.mvc.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emsdashboard.service.DashboardTreeManager;
import com.emsdashboard.types.FacilityType;
import com.emsdashboard.utils.tree.TreeNode;

@Controller
@RequestMapping("/dashboard")
public class DashboardTreeController {
    @Resource
    DashboardTreeManager dashboardTreeManager;
    
    @RequestMapping("/home.ems")
    public String getFacilities(Model model,HttpServletResponse httpResponse) {
        //To get facility tree populated
        model.addAttribute("dashboardTreeHierarchy", getTreeHierarchy(model));
        return "home";
    }
    private TreeNode<FacilityType> getTreeHierarchy(Model model) {
        TreeNode<FacilityType> gemsTreeHierarchy = null;
        gemsTreeHierarchy = dashboardTreeManager.loadGEMShierarchy();
        return gemsTreeHierarchy;
    }
}
