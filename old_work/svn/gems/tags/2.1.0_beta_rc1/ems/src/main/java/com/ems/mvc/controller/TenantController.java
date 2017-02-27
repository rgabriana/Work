package com.ems.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.model.Tenant;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.TenantManager;
import com.ems.types.TenantStatus;
import com.ems.types.UserAuditActionType;

@Controller
@RequestMapping("/tenants")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class TenantController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource
    TenantManager tenantManager;

    @Resource(name = "emsAuthContext")
    private EmsAuthenticationContext emsAuthContext;
 
    @RequestMapping("/list.ems")
    public String getFacilities(Model model) {
        List<Tenant> tenantsList = tenantManager.getAllTenants();
        model.addAttribute("tenantsList", tenantsList);
        
        //To send current user tenant details to view page
        model.addAttribute("tenant", emsAuthContext.getCurrentTenant());
        
        return "tenants/list";
    }

    @RequestMapping("/create.ems")
    public String createTenant(Model model) {
        Tenant tenant = new Tenant();
        model.addAttribute("tenant", tenant);
        model.addAttribute("statusList", TenantStatus.values());	
        return "tenants/details";
    }

    @RequestMapping(value = "/{tenantId}/edit.ems")
    public String editTenant(Model model, @PathVariable("tenantId") Long tenantId) {
        Tenant tenant = tenantManager.get(tenantId);
        model.addAttribute("tenant", tenant);
        model.addAttribute("statusList", TenantStatus.values());
    	userAuditLoggerUtil.log("Update tenant: " + tenant.getName() +"(" + tenant.getId() +")", UserAuditActionType.Tenant_Update.getName());
        return "tenants/details";
    }

    @RequestMapping("/save.ems")
    public String saveTenant(Tenant tenant) {
        tenantManager.save(tenant);
        userAuditLoggerUtil.log("Create tenant: " + tenant.getName() +"(" + tenant.getId() +")", UserAuditActionType.Tenant_Update.getName());
        return "redirect:/tenants/list.ems";
    }
    
    
    @RequestMapping("/saveAssignFacility.ems")
    public String saveAssignFacilityTenant(Tenant tenant) {
        tenantManager.save(tenant);
        return "redirect:/facilities/tenant_tree_assignment.ems";
    }
}
