package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.Company;
import com.ems.model.DRUsers;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.DRTargetManager;
import com.ems.service.DRUserManager;

@Controller
@RequestMapping("/dr")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class DRController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "drTargetManager")
    private DRTargetManager drTargetManager;
    @Resource(name = "drUserManager")
    private DRUserManager drUserManager;
	
	@RequestMapping("/listDR.ems")
    public String listDR(Model model) {
        model.addAttribute("drlist", drTargetManager.getAllDRTargets());
        return "dr/listDR";
    }
	
	@RequestMapping("/addUser.ems")
    public String addDRUser(Model model) {
		model.addAttribute("druser", new DRUsers());
        return "dr/addDRUser";
    }
	
	@RequestMapping(value = "/registerUser.ems",  method = RequestMethod.POST)
	public String registerUser(@RequestParam("newPassword") String password, @RequestParam("server") String server, @ModelAttribute("druser") DRUsers druser) {
		druser.setPassword(password);
		if(drUserManager.save(druser, server) != null) {
			return "redirect:/dr/addUser.ems?status=S";
		}
		else {
			return "redirect:/dr/addUser.ems?status=E";
		}
		
	}

}
