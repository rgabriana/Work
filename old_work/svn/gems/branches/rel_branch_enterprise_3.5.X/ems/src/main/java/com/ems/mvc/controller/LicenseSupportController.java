package com.ems.mvc.controller;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.enlightedinc.licenseutil.LicenseUtil;
import com.enlightedinc.vo.BacnetLicenseInstance;
import com.enlightedinc.vo.ZoneSensorsLicenseInstance;
import com.enlightedinc.vo.EmLicenseInstance;
import com.enlightedinc.vo.Licenses;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;

import com.ems.service.LicenseSupportManager;
import com.ems.service.SystemConfigurationManager;



@Controller
@RequestMapping("/licenseSupport")
public class LicenseSupportController {
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource
    private LicenseSupportManager licenseSupportManager;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String manageLicenseSupport(Model model)
	{
		
		SystemConfiguration emUUID = systemConfigurationManager.loadConfigByName("em.UUID");
		
		String secretKeyString = LicenseUtil.SECRET_LICENSE_KEY + emUUID.getValue();
		
		
		String jsonLicenseEncriptedString = systemConfigurationManager.loadConfigByName("emLicenseKeyValue").getValue();
		
		String jsonLicenseString = null;
				
		try {
			jsonLicenseString = LicenseUtil.decrypt(secretKeyString, jsonLicenseEncriptedString);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
			
		try {
			Licenses licenses = new ObjectMapper().readValue(jsonLicenseString, Licenses.class);
			model.addAttribute("UUID", emUUID.getValue());
			int totalNoOfEmDevices = 0;
			for(EmLicenseInstance emLicenseInstance : licenses.getEm().getEmLicenseInstanceList()){
				totalNoOfEmDevices = totalNoOfEmDevices + emLicenseInstance.getNoofdevices();
			}
			int totalNoOfBacnetDevices = 0;
			
			for(BacnetLicenseInstance bacnetLicenseInstance : licenses.getBacnet().getBacnetLicenseInstanceList()){
				totalNoOfBacnetDevices = totalNoOfBacnetDevices + bacnetLicenseInstance.getNoofdevices();
			}
			
			int totalNoOfZoneSensors = 0;
			
			if(licenses.getZoneSensors() != null){
				for(ZoneSensorsLicenseInstance zoneSensorsLicenseInstance : licenses.getZoneSensors().getZoneSensorsLicenseInstanceList()){
					totalNoOfZoneSensors = totalNoOfZoneSensors + zoneSensorsLicenseInstance.getNoofdevices();
				}
			}
			
			model.addAttribute("totalNoOfEmDevices", totalNoOfEmDevices);
			model.addAttribute("totalNoOfBacnetDevices", totalNoOfBacnetDevices);
			model.addAttribute("totalNoOfZoneSensors", totalNoOfZoneSensors);
			model.addAttribute("licenses", licenses);
		} catch (JsonParseException e) {
		
		} catch (JsonMappingException e) {
		
		} catch (IOException e) {
		
		}
		
		return "licenseSupportManagement/management";
	}
	
	@RequestMapping("/uploadNewLicenseFile.ems")
	public String uploadLicenseFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			Model model,
			Locale locale) {
		
		String uploadStatus = "false";
		
		if (!file.isEmpty()) {
			String jsonNewLicenseEncriptedString = null;
			try {
				byte[] fileBytes = file.getBytes();
				jsonNewLicenseEncriptedString = new String(fileBytes);
			} catch (IOException e) {
			
			}
			
			uploadStatus = licenseSupportManager.uploadLicenseFile(jsonNewLicenseEncriptedString);
		}
		return "redirect:/licenseSupport/management.ems?uploadStatus="+uploadStatus;
	}
	
}