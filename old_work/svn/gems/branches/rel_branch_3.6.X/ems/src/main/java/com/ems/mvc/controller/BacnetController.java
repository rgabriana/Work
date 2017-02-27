package com.ems.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.BACnetConfig;
import com.ems.model.BACnetConfiguration;
import com.ems.model.BacnetObjectsCfg;
import com.ems.model.BacnetReportConfiguration;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.BACnetConfigurationManager;
import com.ems.service.BacnetManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.NetworkType;
import com.ems.types.UserAuditActionType;
import com.ems.util.Constants;
import com.ems.utils.CommonUtils;
import com.enlightedinc.licenseutil.LicenseUtil;
import com.enlightedinc.vo.BacnetLicenseInstance;
import com.enlightedinc.vo.EmLicenseInstance;
import com.enlightedinc.vo.Licenses;
import com.enlightedinc.vo.ZoneSensorsLicenseInstance;

@Controller
@RequestMapping("/bacnet")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class BacnetController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "bacnetManager")
	private BacnetManager bacnetManager;
	@Resource(name = "bacnetConfigurationManager")
	private BACnetConfigurationManager bacnetConfigurationManager;
	@Resource
	private NetworkSettingsManager networkSettingsManager;
	@Resource
	private SystemConfigurationManager systemConfigurationManager;
	    
	@Autowired
	private MessageSource messageSource;
	
	private static final Logger m_Logger = Logger.getLogger("SysLog");
	
    @RequestMapping("/config.ems")
    public String viewConfig(Model model,@RequestParam(value = "saveconfirm", required = false) String saveconfirm, @RequestParam(value = "error", required = false) String error) throws EmsValidationException {
    	Map<String,Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("bacnetcontroller.saveconfirm", saveconfirm);
		nameValMap.put("bacnetcontroller.error", error);
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager,nameValMap);
    	BACnetConfig config = bacnetManager.getConfig();
    	//BACnetConfig config = bacnetManager.getConfigByBacnetConfiguration();
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
    
    @RequestMapping("/allConfDetails.ems")
    String getBACnetConfigAllDetails(Model model) throws EmsValidationException {

    	try {
			List<BACnetConfiguration> bacnetConfigurationDeatils = null;
			
			bacnetConfigurationDeatils = bacnetConfigurationManager.loadBACnetConfigForUI();
			
			model.addAttribute("bacnetConfigurationDeatils", bacnetConfigurationDeatils);
			
			String bacnetInterface = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.BACnet.getName());
			
			if(bacnetInterface != null){
				model.addAttribute("isBacnetNetworkConfigured", true);
			}else{
				model.addAttribute("isBacnetNetworkConfigured", false);
			}
			
			SystemConfiguration bacnetConfigEnableConfiguration = systemConfigurationManager.loadConfigByName("bacnet_config_enable");
			
			if(bacnetConfigEnableConfiguration !=null){
				model.addAttribute("isBacnetEnabled", Boolean.valueOf(bacnetConfigEnableConfiguration.getValue()));
			}
		} catch (Exception e) {
			m_Logger.debug("ERROR : whille setting bacnetConfigurationDeatils Model attribute", e);
		}
    	
        m_Logger.debug("bacnetConfigurationDeatils Model attribute is set");
        
        return "bacnet/alldetails";
    }
    
    @RequestMapping(value = "/bacnetConfiguration.ems")
    String bacnetConfiguration(Model model) {
         return "bacnet/bacnet_config";
    }
    
    @RequestMapping(value = "/serverConfiguration.ems")
    String serverConfiguration(Model model) {
         return "bacnet/server_config";
    }
    
    @RequestMapping(value = "/pointConfiguration.ems")
    String pointConfiguration(Model model, @RequestParam(value = "saveconfirm", required = false) String saveconfirm, @RequestParam(value = "error", required = false) String error) throws EmsValidationException {
    	Map<String,Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("bacnetcontroller.saveconfirm", saveconfirm);
		nameValMap.put("bacnetcontroller.error", error);
    	CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager,nameValMap);
    	try {
    		List<BacnetObjectsCfg> bocEmList = new ArrayList<BacnetObjectsCfg>();
    		List<BacnetObjectsCfg> bocAreaList = new ArrayList<BacnetObjectsCfg>();
    		List<BacnetObjectsCfg> bocFixtureList = new ArrayList<BacnetObjectsCfg>();
    		List<BacnetObjectsCfg> bocPlugloadList = new ArrayList<BacnetObjectsCfg>();
    		List<BacnetObjectsCfg> bocSwitchList = new ArrayList<BacnetObjectsCfg>();
    		
    		SystemConfiguration emUUID = systemConfigurationManager.loadConfigByName("em.UUID");
    		
    		String secretKeyString = LicenseUtil.SECRET_LICENSE_KEY + emUUID.getValue();
    		
    		String jsonLicenseEncriptedString = systemConfigurationManager.loadConfigByName("emLicenseKeyValue").getValue();
    		
    		String jsonLicenseString = null;
    				
    		try {
    			jsonLicenseString = LicenseUtil.decrypt(secretKeyString, jsonLicenseEncriptedString);
    		} catch (Exception e1) {
    			m_Logger.error("ERROR : manageLicenseSupport", e1);
    		}
    		
    		try {
    			Licenses licenses = new ObjectMapper().readValue(jsonLicenseString, Licenses.class);
    			
    			int totalNoOfEmBaseLicenses = 0;
    			int totalNoOfEmGroupPointBaseLicenses = 0;
    			int totalNoOfEmSensorPointBaseLicenses = 0;
    			for(BacnetLicenseInstance bacnetLicenseInstance : licenses.getBacnet().getBacnetLicenseInstanceList()){
    				totalNoOfEmBaseLicenses = totalNoOfEmBaseLicenses + bacnetLicenseInstance.getNoOfEmBaseLicenses();
    				totalNoOfEmGroupPointBaseLicenses = totalNoOfEmGroupPointBaseLicenses + bacnetLicenseInstance.getNoOfEmGroupPointBaseLicenses();
    				totalNoOfEmSensorPointBaseLicenses = totalNoOfEmSensorPointBaseLicenses + bacnetLicenseInstance.getNoOfEmSensorPointBaseLicenses();
    			}
    			
    			model.addAttribute("totalNoOfEmBaseLicenses", totalNoOfEmBaseLicenses);
    			model.addAttribute("totalNoOfEmGroupPointBaseLicenses", totalNoOfEmGroupPointBaseLicenses);
    			model.addAttribute("totalNoOfEmSensorPointBaseLicenses", totalNoOfEmSensorPointBaseLicenses);
    		} catch (JsonParseException e) {
    			m_Logger.error("ERROR : JsonParseException", e);
    		} catch (JsonMappingException e) {
    			m_Logger.error("ERROR : JsonMappingException", e);
    		} catch (IOException e) {
    			m_Logger.error("ERROR : IOException ", e);
    		} catch (Exception e) {
    			m_Logger.error("ERROR : Exception ", e);
    		}
    		
    		List<BacnetObjectsCfg> bacnetObjectsCfgList = bacnetConfigurationManager.getAllBacnetObjectCfgsForUI();
    		if(bacnetObjectsCfgList!=null && !bacnetObjectsCfgList.isEmpty()){
    			model.addAttribute("bacnetObjectsCfgList", bacnetObjectsCfgList);
    			for(BacnetObjectsCfg obj :bacnetObjectsCfgList){
    				if(obj.getBacnetpointtype()!=null){
    					if(Constants.BACNET_EMHEADER.equals((obj.getBacnetpointtype()))){
    						bocEmList.add(obj);
    					} else if(Constants.BACNET_AREASUBHEADER.equals((obj.getBacnetpointtype()))){
    						bocAreaList.add(obj);
    					} else if(Constants.BACNET_FIXSUBHEADER.equals((obj.getBacnetpointtype()))){
    						bocFixtureList.add(obj);
    					} else if(Constants.BACNET_PLSUBHEADER.equals((obj.getBacnetpointtype()))){
    						bocPlugloadList.add(obj);
    					} else if(Constants.BACNET_SWITCHSUBHEADER.equals((obj.getBacnetpointtype()))){
    						bocSwitchList.add(obj);
    					}
    				}
    			}
    			model.addAttribute("bocEmList", bocEmList);
    			model.addAttribute("bocAreaList", bocAreaList);
    			model.addAttribute("bocFixtureList", bocFixtureList);
    			model.addAttribute("bocPlugloadList", bocPlugloadList);
    			model.addAttribute("bocSwitchList", bocSwitchList);
    		}
		} catch (Exception e) {
			m_Logger.error("ERROR: While fetching all the bacnet point configurations.", e);
		}
    	
    	try {
			String bacnetInterface = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.BACnet.getName());
	        
	    	if(bacnetInterface != null){
	    		model.addAttribute("isBacnetNetworkConfigured", true);
	    	}else{
	    		model.addAttribute("isBacnetNetworkConfigured", false);
	    	}
	    	
	    	SystemConfiguration bacnetConfigEnableConfiguration = systemConfigurationManager.loadConfigByName("bacnet_config_enable");
	        
	        if(bacnetConfigEnableConfiguration !=null){
	        	model.addAttribute("isBacnetEnabled", Boolean.valueOf(bacnetConfigEnableConfiguration.getValue()));
	        }
			
		} catch (Exception e) {
			m_Logger.error("ERROR: While fetching all the system configuration's Bacnet License Points.", e);
		}
    	
        return "bacnet/points_config";
    }
    
    @RequestMapping("/reportConfiguration.ems")
    public String reportConfiguration(Model model){
    	List<BacnetReportConfiguration> reportConfigurationList = null;
    	reportConfigurationList = bacnetConfigurationManager.loadAllBacnetReportCfgs();
    	if(reportConfigurationList!=null && !reportConfigurationList.isEmpty()){
    		model.addAttribute("reportConfigurationList", reportConfigurationList);
    	}
    	String bacnetReportConfigurations ="";
    	model.addAttribute("bacnetReportConfigurations", bacnetReportConfigurations);
    	return "bacnet/reports_config";
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