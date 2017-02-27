package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CommunicatorManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.vo.LdapSettings;
import com.ems.vo.MasterGemsSetting;

@Controller
@RequestMapping("/settings")
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

    @RequestMapping("/ldap.ems")
    String getListOfUsers(Model model) {

        LdapSettings ldapSettings = new LdapSettings();
        model.addAttribute("ldapSettings", ldapSettings);

        SystemConfiguration authTypeConfig = systemConfigurationManager.loadConfigByName("auth.auth_type");
        if (authTypeConfig != null) {
            ldapSettings.setAuthenticationType(authTypeConfig.getValue());
        }

        SystemConfiguration ldapUrlConfig = systemConfigurationManager.loadConfigByName("auth.ldap_url");
        if (ldapUrlConfig != null) {
            ldapSettings.setUrl(ldapUrlConfig.getValue());
        }

        SystemConfiguration ldapAuthTypeConfig = systemConfigurationManager.loadConfigByName("auth.ldap_auth_type");
        if (ldapAuthTypeConfig != null) {
            ldapSettings.setLdapAuthenticationType(ldapAuthTypeConfig.getValue());
        }

        SystemConfiguration ldapSecurityPrincipalConfig = systemConfigurationManager
                .loadConfigByName("auth.ldap_security_principal");
        if (ldapSecurityPrincipalConfig != null) {
            ldapSettings.setSecurityPrincipal(ldapSecurityPrincipalConfig.getValue());
        }

        SystemConfiguration allowNonEmsLdapUserConfig = systemConfigurationManager
                .loadConfigByName("auth.ldap_allow_non_ems_users");
        if (allowNonEmsLdapUserConfig != null && "true".equals(allowNonEmsLdapUserConfig.getValue())) {
            ldapSettings.setAllowNonVFMUsers(true);
        }

        return "settings/ldap_settings";
    }

    @RequestMapping("/ldap/save.ems")
    String saveLdapSettings(LdapSettings ldapSettings) {
        SystemConfiguration authTypeConfig = systemConfigurationManager.loadConfigByName("auth.auth_type");
        if (authTypeConfig != null) {
            authTypeConfig.setValue(ldapSettings.getAuthenticationType());
            systemConfigurationManager.update(authTypeConfig);
        }

        SystemConfiguration ldapUrlConfig = systemConfigurationManager.loadConfigByName("auth.ldap_url");
        if (ldapUrlConfig != null) {
            ldapUrlConfig.setValue(ldapSettings.getUrl());
            systemConfigurationManager.update(ldapUrlConfig);
        }

        SystemConfiguration ldapAuthTypeConfig = systemConfigurationManager.loadConfigByName("auth.ldap_auth_type");
        if (ldapAuthTypeConfig != null) {
            ldapAuthTypeConfig.setValue(ldapSettings.getLdapAuthenticationType());
            systemConfigurationManager.update(ldapAuthTypeConfig);
        }

        SystemConfiguration ldapSecurityPrincipalConfig = systemConfigurationManager
                .loadConfigByName("auth.ldap_security_principal");
        if (ldapSecurityPrincipalConfig != null) {
            ldapSecurityPrincipalConfig.setValue(ldapSettings.getSecurityPrincipal());
            systemConfigurationManager.update(ldapSecurityPrincipalConfig);
        }

        SystemConfiguration allowNonEmsLdapUserConfig = systemConfigurationManager
                .loadConfigByName("auth.ldap_allow_non_ems_users");
        if (allowNonEmsLdapUserConfig != null) {
            if (ldapSettings.isAllowNonVFMUsers()) {
                allowNonEmsLdapUserConfig.setValue("true");
            } else {
                allowNonEmsLdapUserConfig.setValue("false");
            }
            systemConfigurationManager.update(ldapSecurityPrincipalConfig);
        }
        
        userAuditLoggerUtil.log("Update ldap settings", UserAuditActionType.Ldap_Update.getName());
        
        return "redirect:/settings/ldap.ems";
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