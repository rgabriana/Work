package com.ems.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.ems.filter.ExternalRestApiValidationFilter;
import com.ems.model.CloudServerInfo;
import com.ems.model.LdapSettings;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.ControllerUtils;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.service.CloudServerManager;
import com.ems.service.CommunicatorManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.LdapSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.util.HardwareInfoUtils;
import com.ems.vo.AuthenticationType;
import com.ems.vo.MasterGemsSetting;
import com.ems.vo.model.FileUpload;
import com.ems.vo.model.RestApiConfigurationModel;
import com.enlightedinc.keyutil.EnlightedKeyGenerator;

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
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "ldapSettingsManager")
    private LdapSettingsManager ldapSettingsManager;
    @Resource(name = "cloudServerManager")
    private CloudServerManager cloudServerManager;

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
    String saveLdapServerSettings( Model model, LdapSettings ldapSettings) {
    	ldapSettingsManager.save(ldapSettings);
    	model.addAttribute("status", "success");
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
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/cloud_setting.ems")
    String loadCloudSettings(Model model) { 
    	model.addAttribute("cloudServerSetting", cloudServerManager.getCloudServerInformation());
        return "settings/cloud_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/cloudServerSettings/save.ems")
    String saveCloudServerSettings(Model model, CloudServerInfo cloudServerInfo) {
    	String status = cloudServerManager.saveCloudServerInformation(cloudServerInfo);
    	if ("F".equals(status)) {
    		userAuditLoggerUtil.log("Cloud server access setup was not saved successfully.", UserAuditActionType.Cloud_Server_Setup.getName());
    		return "redirect:/settings/cloud_setting.ems?save=F";
    	}
    	else {
    		userAuditLoggerUtil.log("Cloud server access setup was saved successfully.", UserAuditActionType.Cloud_Server_Setup.getName());
    		return "redirect:/settings/cloud_setting.ems?save=S" ;
    	}
    }
    
    @RequestMapping(value = "/system_management.ems")
    String cleanup() {
    	return "settings/system_management";
    }
    
    @RequestMapping(value = "/cleanUp.ems")
    String cleanSystem(Model model) {
         return "settings/cleanUpSystem";
    }
   
    
    @RequestMapping(value = "/groups.ems")
    String groupManagement() {
        return "settings/groups";
    }
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/restApi.ems")
    String restApiManagement(Model model) {
    	RestApiConfigurationModel restModel = new RestApiConfigurationModel() ;	
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
   	 if( systemconfig.getValue() !=null && !systemconfig.getValue().isEmpty() )
   	 {
   		byte[] filecontent= null ;
   		String dest = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
   		filecontent = ExternalRestApiValidationFilter.readFromFile( dest+ControllerUtils.getTheLicenseFileName(".enlighted", dest)) ;
   	 if(validated(filecontent, restModel))
   	 {
   		 		
   		  restModel.setValidated(true);
   	 }
   	 else
   	 {
   		restModel.setValidated(false);
   		model.addAttribute("restModel", restModel);
   		systemconfig.setValue(null);
   		systemConfigurationManager.save(systemconfig) ;
   		return "redirect:/settings/restApi.ems?expired=true" ;
   	 }
   	 }
   	 else{
   		 	restModel.setValidated(false);
   	 }
        model.addAttribute("restModel", restModel);
        return "settings/restApi";
    }
    @RequestMapping(value = "/validate.ems")
    String restApiValidation( Model model, RestApiConfigurationModel restModel) {
    	try{
    	byte[] filecontent= null ;
    	String uploadSatus = "false" ;
    	String fileName="";
    	String dest = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
    	FileUpload file = restModel.getLicenseFile();
    	 
		if(file.getFile().getSize()==0){
			uploadSatus="false" ;
			 model.addAttribute("restModel", restModel);
			return "redirect:/settings/restApi.ems?uploadSatus="+uploadSatus ;
			
		}
		else
		{	
			MultipartFile multipartFile = file.getFile();
			fileName = multipartFile.getOriginalFilename();
			 File licenseFile = new File(dest+fileName);
			 if(licenseFile.exists())
			 {
				 licenseFile.delete() ;
				 try {
					licenseFile.createNewFile() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			
			
			if(multipartFile!=null){
				
				 try {
					multipartFile.transferTo(licenseFile);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		
    	filecontent = ExternalRestApiValidationFilter.readFromFile( dest+fileName) ;
    	 if(systemconfig!=null&&validated(filecontent, restModel))
    	 {
    		  systemconfig.setValue(restModel.getApiKey()) ;
    		  systemConfigurationManager.save(systemconfig) ;
    		  restModel.setValidated(true);
    		  restModel.setApiKey("") ;
    		  uploadSatus="true" ;
    	 }
    	 else
    	 {
    		 restModel.setValidated(false);
 			systemconfig.setValue(null);
 			 model.addAttribute("restModel", restModel);
 			 systemConfigurationManager.save(systemconfig) ;
     	        return "redirect:/settings/restApi.ems?uploadSatus="+"false" ;
    	 }
    	 
        model.addAttribute("restModel", restModel);
        uploadSatus="true" ;
        return "redirect:/settings/restApi.ems?uploadSatus="+uploadSatus ;
		}
    	}
    	catch(Exception e)
    	{
    		restModel.setValidated(false);
    		SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
			systemconfig.setValue("");
			 model.addAttribute("restModel", restModel);
			 systemConfigurationManager.save(systemconfig) ;
    	        return "redirect:/settings/restApi.ems?uploadSatus="+"false" ;
    	}
    }
    
   private boolean validated(byte[] key, RestApiConfigurationModel restModel)
    {
  
    	boolean valid = false ;
    	
    	EnlightedKeyGenerator keyGenerator =  EnlightedKeyGenerator.getInstance() ;	
    	 keyGenerator.setEncrptedApiKey(key) ;
    	 byte[] mac = HardwareInfoUtils.getMacAddressForIp(ServerMain.getInstance().getIpAddress("eth0"));
    	 String macString = HardwareInfoUtils.macBytetoString(':',mac);	
    	 try {
    		 	macString =macString.toLowerCase();
    		 	byte[] salt = macString.getBytes("UTF-8") ;
    		 	keyGenerator.setSeceretKey(salt);
    	 	} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
    	 	}
    	 String decryptedString = keyGenerator.doDecryption() ;
    	 String keyMac[] = decryptedString.split("\\|") ;
    	
    	 if(keyMac[0].equalsIgnoreCase(macString))
    	 {
    		 restModel.setApiKey(new String(key)) ;
    		 if(ExternalRestApiValidationFilter.checkDateValidation(keyMac))
    		 {
    		 valid=true ; 
    		 }
    	 }
		
    	return valid ;
    }
 
    @RequestMapping(value = "/invalidate.ems")
    String restApiInValidation() {
    	byte[] filecontent= null ;
    	String type = "false" ;
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
    	 if(systemconfig!=null)
    	 {
    		  systemconfig.setValue("") ;
    		  systemConfigurationManager.save(systemconfig) ;
    		  String dest =ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
    		  File licenseFile = new File(dest+ ControllerUtils.getTheLicenseFileName(".enlighted", dest)) ;
    	    	 if(licenseFile.exists())
    	    	 {
    	    		 licenseFile.delete() ;
    	    	 }
    	    	 type="true" ;
    		  
    	 }
    	
    	 
        return "redirect:/settings/restApi.ems?status="+type;
    }
    @RequestMapping(value = "/cleancache.ems")
    String cleancache() {
    	fixtureManager.clearBallastVoltPowersMap();
    	facilityTreeManager.inValidateFacilitiesTreeCache();
    	groupManager.inValidateProfilesTreeCache();
    	return "redirect:/settings/cleanUp.ems";
    }
    @RequestMapping(value = "/master_gems_setting.ems")
    String masterGemsSetting(Model model) {
    	MasterGemsSetting masterGemsSetting = communicatorManager.loadConfiguration();
    	if(masterGemsSetting==null)
    	{
    		masterGemsSetting = new MasterGemsSetting() ;
    	}
    	   
          model.addAttribute("masterGemsSetting", masterGemsSetting);
    	return "settings/master_gems_settings";
    }
    @RequestMapping(value = "/master_gems_setting/save.ems")
    String saveMasterGemsSetting(MasterGemsSetting masterGemsSetting) {
    	
    	communicatorManager.setConfiguration(masterGemsSetting);
    	return "redirect:/settings/cleanUp.ems";
    }
}
