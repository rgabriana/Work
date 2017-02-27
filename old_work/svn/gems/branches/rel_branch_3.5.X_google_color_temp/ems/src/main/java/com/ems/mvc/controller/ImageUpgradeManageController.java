package com.ems.mvc.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.RequestUtil;

@Controller
@RequestMapping("/imageupgrademanage")
@PreAuthorize("hasAnyRole('Admin')")
public class ImageUpgradeManageController {
	
    static final long ImageFileSizeLimit  = 10*1024*1024; // file size limit of 10MB

	@Resource(name = "firmwareUpgradeManager")
	private FirmwareUpgradeManager firmwareUpgradeManager;

	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@Resource(name = "firmwareUpgradeScheduleManager")
    private FirmwareUpgradeScheduleManager firmwareUpgradeScheduleManager;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	UserAuditLoggerUtil userAuditLoggerUtil;

	
	@RequestMapping("/image_upgrade_management.ems")
	public String imageUpgradeManagement() {
		return "imageupgrade/management";
	}
	
		
	@RequestMapping("/imageFileUpload.ems")
	public String imageFileUpload(HttpServletRequest request, Model model) {
		model.addAttribute("uploadStatus", request.getParameter("uploadStatus"));
        model.addAttribute("message", request.getParameter("message"));
        model.addAttribute("ImageFileSizeLimit", ImageFileSizeLimit);
		return "imageupgrade/imageupload";
	}
	
	@RequestMapping("/imageSchedule.ems")
	public String imageSchedule(HttpServletRequest request, Model model) {
		TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
		.loadCompanyHierarchy();
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(false);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		model.addAttribute("message", request.getParameter("message"));
		model.addAttribute("viewTreeOnly", true);
		List<FirmwareUpgradeSchedule>  firmwareUpgradeSchedules = firmwareUpgradeScheduleManager.loadAllFirmwareUpgradeSchedules();
		model.addAttribute("firmwareUpgradeSchedules", firmwareUpgradeSchedules);
		Boolean isFixtureUpgradeActive = false;
		for(FirmwareUpgradeSchedule firmwareUpgradeSchedule : firmwareUpgradeSchedules){
			if(firmwareUpgradeSchedule.getActive() && "Fixture".equals(firmwareUpgradeSchedule.getDeviceType())){
				isFixtureUpgradeActive = true;
				if(firmwareUpgradeSchedule.getStartTime() != null){
					model.addAttribute("startDatePicker", firmwareUpgradeSchedule.getStartTime().toString());
				}else{
					model.addAttribute("startDatePicker", "");
				}
				model.addAttribute("retries", firmwareUpgradeSchedule.getRetries());
				model.addAttribute("retryInterval", firmwareUpgradeSchedule.getRetryInterval());
				model.addAttribute("jobName", firmwareUpgradeSchedule.getJobPrefix());
				if(firmwareUpgradeSchedule.getDuration().intValue() == 0){
					model.addAttribute("runtoComplete",true);
					model.addAttribute("duration","");
				}else{
					model.addAttribute("runtoComplete",false);
					model.addAttribute("duration",firmwareUpgradeSchedule.getDuration().intValue());
				}
				if(firmwareUpgradeSchedule.getOnReboot()){
					model.addAttribute("onreboot",true);
				}else{
					model.addAttribute("onreboot",false);
				}
				
				if(firmwareUpgradeSchedule.getIncludeList() == null && firmwareUpgradeSchedule.getExcludeList() == null){
					model.addAttribute("deviceSelection","All");
				}else if(firmwareUpgradeSchedule.getIncludeList() != null && firmwareUpgradeSchedule.getExcludeList() == null){
					model.addAttribute("deviceSelection","OnlySelected");
				}else if(firmwareUpgradeSchedule.getIncludeList() == null && firmwareUpgradeSchedule.getExcludeList() != null){
					model.addAttribute("deviceSelection","ExceptSelected");
				}else{
					model.addAttribute("deviceSelection","OnlySelected");
				}
				
				break;
			}
		}
		model.addAttribute("isFixtureUpgradeActive", isFixtureUpgradeActive);
		return "imageupgrade/imageschedule";
	}
	
	@RequestMapping("/imageJob.ems")
	public String imageJob() {
		return "imageupgrade/imagejob";
	}
	
	@RequestMapping("/imageStatus.ems")
	public String imageStatus() {
		return "imageupgrade/imagestatus";
	}
		
		
	@RequestMapping("/saveNewDeviceImage.ems")
	public String uploadDeviceImageFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			@RequestParam("fileName") String fileName, Model model,
			Locale locale) {
		
		if (!file.isEmpty()) {
   			
   			if(file.getSize() > ImageFileSizeLimit){
   				long ImageFileSizeLimitInMB = (ImageFileSizeLimit/(1024*1024));
   				model.addAttribute("uploadStatus", "false");
   				model.addAttribute("message", "Upload File size should be less than "+ImageFileSizeLimitInMB+" MB.");
   				return "redirect:/imageupgrade/imageFileUpload.ems";
   			}
		}
		try {
			String tomcatBase = RequestUtil.getTomcatBasePath(request);
			String path = tomcatBase
					+ messageSource.getMessage("upgrade.image.location", null,
							locale);

			if (!file.isEmpty()) {
				firmwareUpgradeManager.uploadImageFile(path, fileName, file);
				model.addAttribute("uploadStatus", "true");
				model.addAttribute("message", messageSource.getMessage(
						"imageUpgrade.message.uploadFileSuccess", null, locale));
				firmwareUpgradeScheduleManager.parseAndAddImagesToDB(fileName);
				
				//firmwareUpgradeManager.addFirmwareImage(fileName, "", "", "", "");
			} else {
				model.addAttribute("uploadStatus", "false");
				model.addAttribute("message", messageSource.getMessage(
						"imageUpgrade.message.uploadFileFailed", null, locale));
			}
		} catch (IOException ioe) {
			model.addAttribute("uploadStatus", "false");
			model.addAttribute("message", messageSource.getMessage(
					"imageUpgrade.message.uploadFileFailed", null, locale));
			ioe.printStackTrace();
		}

		return "redirect:/imageupgrademanage/imageFileUpload.ems";
	}

	public void scheduleFirmwareUpgrade(String fileName, String deviceType, Date scheduledTime, Date startTime, int duration, 
			boolean reboot, boolean active, String excludeList, String includeList, int retries, int retryInterval, String desc ) {
		
		//on demand run once job create a image upgrade job
		//run during maintenance window daily starting from scheduled time. create a firmware upgrade schedule
		FirmwareUpgradeSchedule fus = new FirmwareUpgradeSchedule();
		fus.setActive(active);
		fus.setDescription(desc);
		fus.setDeviceType(deviceType);
		fus.setDuration(duration);
		fus.setExcludeList(excludeList);
		fus.setFileName(fileName);
		fus.setIncludeList(includeList);
		fus.setRetries(retries);
		fus.setOnReboot(reboot);
		fus.setRetryInterval(retryInterval);
		fus.setScheduledTime(scheduledTime);
		fus.setStartTime(startTime);		
		firmwareUpgradeScheduleManager.addFirmwareImage(fus);
		
	} //end of method scheduleFirmwareUpgrade
	
}
