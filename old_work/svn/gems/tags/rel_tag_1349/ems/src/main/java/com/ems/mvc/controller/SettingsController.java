package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.model.LdapSettings;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CommunicatorManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureManager;
import com.ems.service.LdapSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.vo.AuthenticationType;
import com.ems.vo.MasterGemsSetting;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class SettingsController {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
	CommunicatorManager communicatorManager;
    @Resource
    SystemConfigurationManager systemConfigurationManager;
    @Resource(name="facilityTreeManager")
    FacilityTreeManager facilityTreeManager;
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    @Resource(name = "ldapSettingsManager")
    private LdapSettingsManager ldapSettingsManager;

    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap.ems")
    String getListOfUsers(Model model) {

    
        return "settings/ldap_settings";
    }

    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/save.ems")
    String saveLdapSettings( AuthenticationType authenticationType) {
        SystemConfiguration authTypeConfig = systemConfigurationManager.loadConfigByName("auth.auth_type");
        if (authTypeConfig != null) {
            authTypeConfig.setValue( authenticationType.getAuthenticationType());
            systemConfigurationManager.update(authTypeConfig);
        }

      
        
        return "settings/ldap_login_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/server/save.ems")
    String saveLdapServerSettings( LdapSettings ldapSettings) {
    	ldapSettingsManager.save(ldapSettings);
        return "settings/ldap_server_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/server_setting.ems")
    String loadLdapServerSettings(Model model) {
    	 LdapSettings ldapSettings= ldapSettingsManager.loadById(1l);
    	 if(ldapSettings == null)
    	 {
    		 ldapSettings = new LdapSettings() ;
    	 }
         model.addAttribute("ldapSettings", ldapSettings);
        userAuditLoggerUtil.log("Update Ldap Server Settings", UserAuditActionType.Ldap_Update.getName());
        
        return "settings/ldap_server_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/login_setting.ems")
    String loadLdapLoginSettings(Model model) {
        AuthenticationType authenticationType = new AuthenticationType();
        model.addAttribute("authenticationType", authenticationType);

        SystemConfiguration authTypeConfig = systemConfigurationManager.loadConfigByName("auth.auth_type");
        if (authTypeConfig != null) {
        	authenticationType.setAuthenticationType(authTypeConfig.getValue());
        }
        userAuditLoggerUtil.log("Update Ldap Server Settings", UserAuditActionType.Ldap_Update.getName());
        
        return "settings/ldap_login_settings";
    }
 
    
    @RequestMapping(value = "/system_management.ems")
    String cleanup() {
    	return "settings/system_management";
    }
    
    @RequestMapping(value = "/cleancache.ems")
    String cleancache() {
    	fixtureManager.clearBallastVoltPowersMap();
    	facilityTreeManager.inValidateFacilitiesTreeCache();
    	return "redirect:/settings/system_management.ems";
    }
    @RequestMapping(value = "/master_gems_setting.ems")
    String masterGemsSetting(Model model) {
    	MasterGemsSetting masterGemsSetting = new MasterGemsSetting();
          model.addAttribute("masterGemsSetting", masterGemsSetting);
    	return "settings/master_gems_settings";
    }
    @RequestMapping(value = "/master_gems_setting/save.ems")
    String saveMasterGemsSetting(MasterGemsSetting masterGemsSetting) {
    	communicatorManager.setConfiguration(masterGemsSetting);
    	return "redirect:/settings/system_management.ems";
    }
}
