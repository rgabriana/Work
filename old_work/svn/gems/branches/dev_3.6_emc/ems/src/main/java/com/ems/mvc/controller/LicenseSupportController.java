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

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;

import com.ems.security.exception.EmsValidationException;
import com.ems.service.LicenseSupportManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;



@Controller
@RequestMapping("/licenseSupport")
public class LicenseSupportController {
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource
    private LicenseSupportManager licenseSupportManager;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Autowired
	private MessageSource messageSource;
	
	private static final Logger m_Logger = Logger.getLogger("SysLog");
	
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String manageLicenseSupport(Model model, @RequestParam(value = "uploadStatus", required = false) String uploadStatus) throws EmsValidationException
	{
		
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "licenseSupportController.uploadStatus", uploadStatus);
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
			model.addAttribute("UUID", emUUID.getValue());
			int totalNoOfEmDevices = 0;
			for(EmLicenseInstance emLicenseInstance : licenses.getEm().getEmLicenseInstanceList()){
				totalNoOfEmDevices = totalNoOfEmDevices + emLicenseInstance.getNoofdevices();
			}
			
			int totalNoOfBacnetDevices = 0;
			int totalNoOfEmBaseLicenses = 0;
			int totalNoOfEmGroupPointBaseLicenses = 0;
			int totalNoOfEmSensorPointBaseLicenses = 0;
			for(BacnetLicenseInstance bacnetLicenseInstance : licenses.getBacnet().getBacnetLicenseInstanceList()){
				totalNoOfBacnetDevices = totalNoOfBacnetDevices + bacnetLicenseInstance.getNoofdevices();
				totalNoOfEmBaseLicenses = totalNoOfEmBaseLicenses + bacnetLicenseInstance.getNoOfEmBaseLicenses();
				totalNoOfEmGroupPointBaseLicenses = totalNoOfEmGroupPointBaseLicenses + bacnetLicenseInstance.getNoOfEmGroupPointBaseLicenses();
				totalNoOfEmSensorPointBaseLicenses = totalNoOfEmSensorPointBaseLicenses + bacnetLicenseInstance.getNoOfEmSensorPointBaseLicenses();
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
		return "licenseSupportManagement/management";
	}
	
	@RequestMapping("/uploadNewLicenseFile.ems")
	public String uploadLicenseFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			Model model,
			Locale locale) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,
				systemConfigurationManager, "imageUpgrade.uploadImageFile", file);
		String uploadStatus = "false";
		
		if (!file.isEmpty()) {
			String jsonNewLicenseEncriptedString = null;
			try {
				byte[] fileBytes = file.getBytes();
				jsonNewLicenseEncriptedString = new String(fileBytes);
			} catch (IOException e) {
				m_Logger.error("ERROR : IOException ", e);
			}
			
			uploadStatus = licenseSupportManager.uploadLicenseFile(jsonNewLicenseEncriptedString);
		}
		return "redirect:/licenseSupport/management.ems?uploadStatus="+uploadStatus;
	}
	
}