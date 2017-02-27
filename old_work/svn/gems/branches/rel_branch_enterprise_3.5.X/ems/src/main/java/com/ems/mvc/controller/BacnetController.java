package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.model.BACnetConfig;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BacnetManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.types.NetworkType;
import com.ems.types.UserAuditActionType;

@Controller
@RequestMapping("/bacnet")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class BacnetController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "bacnetManager")
	private BacnetManager bacnetManager;
	
	@Resource
	private NetworkSettingsManager networkSettingsManager;
	
    @RequestMapping("/config.ems")
    public String viewConfig(Model model) {
    	BACnetConfig config = bacnetManager.getConfig();
    	if(config == null) {
    		model.addAttribute("bacnet", new BACnetConfig());
    		model.addAttribute("error", "load_error");
    	}
    	else {
    		model.addAttribute("bacnet", config);
    	}
    	
    	String bacnetInterface = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.BACnet.getName());
    	if(bacnetInterface != null){
    		model.addAttribute("isBacnetNetworkConfigured", true);
    	}else{
    		model.addAttribute("isBacnetNetworkConfigured", false);
    	}
    	
        return "bacnet/config";
    }
    
    @RequestMapping("/submit.ems")
    public String submitConfig(@ModelAttribute("config") BACnetConfig config) {
    	String result = bacnetManager.saveConfig(config);
    	if("SAVE_ERROR".equals(result)) {
    		return "redirect:/bacnet/config.ems?error=save_error";
    	}
    	userAuditLoggerUtil.log("Changed Bacnet Config", UserAuditActionType.Bacnet.getName());	
    	return "redirect:/bacnet/config.ems?saveconfirm=save_success";
    }
}