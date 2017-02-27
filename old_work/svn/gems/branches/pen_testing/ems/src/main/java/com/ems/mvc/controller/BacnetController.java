package com.ems.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.BACnetConfig;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.BacnetManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;

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
	@Resource
	private SystemConfigurationManager systemConfigurationManager;
	    
	@Autowired
	private MessageSource messageSource;
    @RequestMapping("/config.ems")
    public String viewConfig(Model model,@RequestParam(value = "saveconfirm", required = false) String saveconfirm) throws EmsValidationException {
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "bacnetcontroller.saveconfirm", saveconfirm);
    	BACnetConfig config = bacnetManager.getConfig();
    	if(config == null) {
    		model.addAttribute("bacnet", new BACnetConfig());
    		model.addAttribute("error", "load_error");
    	}
    	else {
    		model.addAttribute("bacnet", config);
    	}
    	
    	String bacnetInterface = networkSettingsManager.loadCurrentMappingByNetworkType("BACnet");
    	if(bacnetInterface != null){
    		model.addAttribute("isBacnetNetworkConfigured", true);
    	}else{
    		model.addAttribute("isBacnetNetworkConfigured", false);
    	}
    	
        return "bacnet/config";
    }
    
    @RequestMapping("/submit.ems")
    public String submitConfig(@ModelAttribute("config") BACnetConfig config)  throws EmsValidationException {
    	Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("bacnetcontroller.vendorId", config.getVendorId());
        nameValMap.put("bacnetcontroller.restApiKey", config.getRestApiKey());
        nameValMap.put("bacnetcontroller.restApiSecret", config.getRestApiSecret());
        nameValMap.put("bacnetcontroller.energyManagerName", config.getEnergyManagerName());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
        
    	String result = bacnetManager.saveConfig(config);
    	if("SAVE_ERROR".equals(result)) {
    		return "redirect:/bacnet/config.ems?error=save_error";
    	}
    	userAuditLoggerUtil.log("Changed Bacnet Config", UserAuditActionType.Bacnet.getName());	
    	return "redirect:/bacnet/config.ems?saveconfirm=save_success";
    }
}