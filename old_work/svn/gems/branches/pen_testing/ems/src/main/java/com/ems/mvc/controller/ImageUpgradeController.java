package com.ems.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ems.cache.FixtureCache;
import com.ems.cache.PlugloadCache;
import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.mvc.util.EmsModeControl;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.DeviceType;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.CommonUtils;
import com.ems.utils.RequestUtil;

@Controller
@RequestMapping("/imageupgrade")
@PreAuthorize("hasAnyRole('Admin')")
public class ImageUpgradeController {
	static final String FIXTURE_IMAGE_KEY = "fixtureUpgradeimages";
	static final String GATEWAY_IMAGE_KEY = "gatewayUpgradeimages";
    static final String WDS_IMAGE_KEY = "wdsUpgradeimages";
    static final String PLUGLOAD_IMAGE_KEY = "plugloadUpgradeimages";
    static final String  _IMAGE_KEY = "wdsUpgradeimages";
    
    static final long ImageFileSizeLimit  = 10*1024*1024; // file size limit of 10MB

	@Resource(name = "firmwareUpgradeManager")
	private FirmwareUpgradeManager firmwareUpgradeManager;
	@Resource(name = "firmwareUpgradeScheduleManager")
	private FirmwareUpgradeScheduleManager firmwareUpgradeScheduleManager;

	@Resource
	FacilityTreeManager facilityTreeManager;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;

	@RequestMapping("/get_details.ems")
	public String getImageUpgradeDetails(HttpServletRequest request, Model model)
			throws EmsValidationException {
		/*String tomcatBase = RequestUtil.getTomcatBasePath(request);
		String imagespath = tomcatBase
				+ messageSource
						.getMessage("upgrade.image.location", null, null);

		Map<String, String[]> listsMap = firmwareUpgradeManager
				.getFirmwareImagesList(imagespath);
		model.addAttribute(FIXTURE_IMAGE_KEY,
				(String[]) listsMap.get(FIXTURE_IMAGE_KEY));
		model.addAttribute(GATEWAY_IMAGE_KEY,
				(String[]) listsMap.get(GATEWAY_IMAGE_KEY));
        model.addAttribute(WDS_IMAGE_KEY,
                (String[]) listsMap.get(WDS_IMAGE_KEY));*/
		// add Attribute companies
		// add Attribute fixture firmware upgrade name
		// add Attribute gateway firmware upgrade name

		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "imageUpgrade.message", request.getParameter("message"));

		TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
				.loadCompanyHierarchy();
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(false);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		model.addAttribute("message", request.getParameter("message"));
		model.addAttribute("viewTreeOnly", true);
		SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
            model.addAttribute("enablePlugloadFeature", enablePlugloadProfileFeature.getValue());
        }else{
          	 model.addAttribute("enablePlugloadFeature", false);
        }
		return "imageupgrade/details";
	}
	
	@RequestMapping("/upload.ems")
	public String ImageUpload(HttpServletRequest request, Model model)
			throws EmsValidationException {
		String tomcatBase = RequestUtil.getTomcatBasePath(request);
		String imagespath = tomcatBase
				+ messageSource
						.getMessage("upgrade.image.location", null, null);

		Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("imageUpgrade.uploadStatus", request.getParameter("uploadStatus"));
		nameValMap.put("imageUpgrade.message", request.getParameter("message"));
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,
				systemConfigurationManager, nameValMap);

		Map<String, String[]> listsMap = firmwareUpgradeManager
				.getFirmwareImagesList(imagespath);
		model.addAttribute(FIXTURE_IMAGE_KEY,
				(String[]) listsMap.get(FIXTURE_IMAGE_KEY));
		model.addAttribute(GATEWAY_IMAGE_KEY,
				(String[]) listsMap.get(GATEWAY_IMAGE_KEY));
        model.addAttribute(WDS_IMAGE_KEY,
                (String[]) listsMap.get(WDS_IMAGE_KEY));
        model.addAttribute(PLUGLOAD_IMAGE_KEY,
                (String[]) listsMap.get(PLUGLOAD_IMAGE_KEY));
        model.addAttribute("uploadStatus", request.getParameter("uploadStatus"));
        model.addAttribute("message", request.getParameter("message"));
        model.addAttribute("ImageFileSizeLimit", ImageFileSizeLimit);
        SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
            model.addAttribute("enablePlugloadFeature", enablePlugloadProfileFeature.getValue());
        }else{
         	 model.addAttribute("enablePlugloadFeature", false);
       }
		return "imageupgrade/upload";
	}
	
	@RequestMapping("/saveNewImages.ems")
	public String uploadImageFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			@RequestParam("fileName") String fileName, Model model,
			Locale locale) throws EmsValidationException {
		
		Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("imageUpgrade.uploadImageFile", file);
		nameValMap.put("imageUpgrade.uploadImageFileName", fileName);
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,
				systemConfigurationManager, nameValMap);
		if (!file.isEmpty()) {
   			
   			if(file.getSize() > ImageFileSizeLimit){
   				long ImageFileSizeLimitInMB = (ImageFileSizeLimit/(1024*1024));
   				model.addAttribute("uploadStatus", "false");
   				model.addAttribute("message", "Upload File size should be less than "+ImageFileSizeLimitInMB+" MB.");
   				return "redirect:/imageupgrade/upload.ems";
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

		// return "imageupgrade/details";
		//return "redirect:/imageupgrade/get_details.ems";
		return "redirect:/imageupgrade/upload.ems";
	}

	@RequestMapping(value = "/startimageupgrade.ems", method = RequestMethod.POST)
	@ResponseBody
	public String startImageUpgrade(HttpServletRequest request,
			@RequestParam("gwImageName") String gwImageFileName,
			@RequestParam("gatewayIds") String gatewayIds,
			@RequestParam("fxImageName") String fxImageFileName,
			@RequestParam("fixtureIds") String fixtureIds,
            @RequestParam("wdsImageName") String wdsImageFileName,
            @RequestParam("wdsIds") String wdsIds,
            @RequestParam("plugloadImageFileName") String plugloadImageFileName,
			@RequestParam("plugloadIds") String plugloadIds)
			throws EmsValidationException {
		int result = 0;
		if (ImageUpgradeSO.isInProgress()) {
			return "{\"success\":" + result + ", \"message\" : \"In Progess\"}";
		}

		if (!"S".equals(EmsModeControl.setMode("NORMAL:IMAGE_UPGRADE"))) {
			return "{\"success\":" + -1 + ", \"message\" : \"Try Later\"}";
		}

		Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("imageUpgrade.gwImageFileName", gwImageFileName);
		nameValMap.put("imageUpgrade.fxImageFileName", fxImageFileName);
		nameValMap.put("imageUpgrade.wdsImageFileName", wdsImageFileName);
		nameValMap.put("imageUpgrade.plugloadImageFileName",
				plugloadImageFileName);
		nameValMap.put("imageUpgrade.gatewayIds", gatewayIds);
		nameValMap.put("imageUpgrade.fixtureIds", fixtureIds);
		nameValMap.put("imageUpgrade.wdsIds", wdsIds);
		nameValMap.put("imageUpgrade.plugloadIds", plugloadIds);
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,
				systemConfigurationManager, nameValMap);

		try {
			User usr = (User) request.getSession().getAttribute("USER");
			final ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
	
			if (gatewayIds != null && gatewayIds.length() > 0) {
				Long deviceIdsArr[] = convertStringToLongArray(gatewayIds.split(","));
				if (gwImageFileName != null) {
					// saves to firmware_upgrade table
					result = firmwareUpgradeManager.saveImageVersion(
							ServerConstants.DEVICE_GATEWAY, deviceIdsArr,
							gwImageFileName, usr);
					// Create a gateway upgrade job
					ImageUpgradeDBJob gwJob = new ImageUpgradeDBJob();
					gwJob.setDeviceType(DeviceType.Gateway.getName());
					gwJob.setDeviceIds(deviceIdsArr);
					gwJob.setIncludeList(gatewayIds);
					gwJob.setImageName(gwImageFileName);
					gwJob.setStopTime(null);
					gwJob.setNoOfRetries(ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES + 1);
					gwJob.setRetryInterval(0);
					jobList.add(gwJob);
	
					StringBuilder gatewayName = new StringBuilder();
					// Let's find the name of gateway from cache. LEt's put this in
					// try/catch as failure
					// to find the name should not stop decommission.
					try {
						for (Long id : deviceIdsArr) {
	                        if(ServerMain.getInstance()
									.getGatewayInfo(id).getGw() != null){
							gatewayName.append(ServerMain.getInstance()
									.getGatewayInfo(id).getGw().getGatewayName()
									+ ",");
	                        }else{
	                        	gatewayName.append("No Name(id)" + ",");	
							}				
						}
						userAuditLoggerUtil.log("Image Upgrade for " + gatewayName,
								UserAuditActionType.Gateway_Image_Upgrade.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
	
			if (fixtureIds != null && fixtureIds.length() > 0) {
				Long deviceIdsArr[] = convertStringToLongArray(fixtureIds.split(","));
				if (fxImageFileName != null) {
					// Adds to the firmware_upgrade table
					result = firmwareUpgradeManager.saveImageVersion(
							ServerConstants.DEVICE_FIXTURE, deviceIdsArr,
							fxImageFileName, usr);
					// Creates a Fixture upgrade job
					ImageUpgradeDBJob fixtureJob = new ImageUpgradeDBJob();
					fixtureJob.setDeviceType(DeviceType.Fixture.getName());
					fixtureJob.setDeviceIds(deviceIdsArr);
					fixtureJob.setIncludeList(fixtureIds);
					fixtureJob.setImageName(fxImageFileName);
					fixtureJob.setStopTime(null);
					fixtureJob.setNoOfRetries(ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES + 1);
					fixtureJob.setRetryInterval(0);
					jobList.add(fixtureJob);
	
					StringBuilder fixtureName = new StringBuilder();
	
					// Let's find the name of fixture from cache. Let's put this in
					// try/catch as failure
					// to find the name should not stop profile change.
					try {
						for (Long id : deviceIdsArr) {
							if (FixtureCache.getInstance().getDevice(new Long(id)) != null) {
								fixtureName.append(FixtureCache.getInstance()
										.getDevice(new Long(id)).getFixtureName()
										+ ",");
							}else{
								fixtureName.append("No Name(id)" + "," + id);	
							}
						}
						userAuditLoggerUtil
								.log("Image Upgrade for " + fixtureName,
										UserAuditActionType.Fixture_Image_Upgrade
												.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
	
				}
			}
			
			if (wdsIds != null && wdsIds.length() > 0) {
				Long deviceIdsArr[] = convertStringToLongArray(wdsIds.split(","));
				if (wdsImageFileName != null) {
					// Adds to the firmware_upgrade table
					result = firmwareUpgradeManager.saveImageVersion(ServerConstants.DEVICE_SWITCH, deviceIdsArr,
							wdsImageFileName, usr);
					// Creates a Fixture upgrade job
					ImageUpgradeDBJob wdsJob = new ImageUpgradeDBJob();
					wdsJob.setDeviceType(DeviceType.WDS.getName());
					wdsJob.setDeviceIds(deviceIdsArr);
					wdsJob.setIncludeList(wdsIds);
					wdsJob.setImageName(wdsImageFileName);
					wdsJob.setStopTime(null);
					wdsJob.setNoOfRetries(ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES + 1);
					wdsJob.setRetryInterval(0);
					jobList.add(wdsJob);
	
					StringBuilder wdsName = new StringBuilder();
	
					// TODO
					// Let's find the name of wds from cache. Let's put this in
					// try/catch as failure				
					try {					
						userAuditLoggerUtil
								.log("Image Upgrade for ERC(" + wdsIds + ")", UserAuditActionType.ERC_Image_Upgrade.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}
			}
			
			
			if (plugloadIds != null && plugloadIds.length() > 0) {
				Long deviceIdsArr[] = convertStringToLongArray(plugloadIds.split(","));
				if (plugloadImageFileName != null) {
					// Adds to the firmware_upgrade table
					result = firmwareUpgradeManager.saveImageVersion(
							ServerConstants.DEVICE_PLUGLOAD, deviceIdsArr,
							plugloadImageFileName, usr);
					// Creates a Plugload upgrade job
					ImageUpgradeDBJob plugloadJob = new ImageUpgradeDBJob();
					plugloadJob.setDeviceType(DeviceType.Plugload.getName());
					plugloadJob.setDeviceIds(deviceIdsArr);
					plugloadJob.setIncludeList(plugloadIds);
					plugloadJob.setImageName(plugloadImageFileName);
					plugloadJob.setStopTime(null);
					plugloadJob.setNoOfRetries(ImageUpgradeSO.IMG_DEFAULT_FAIL_RETRIES + 1);
					plugloadJob.setRetryInterval(0);
					jobList.add(plugloadJob);
	
					StringBuilder plugloadName = new StringBuilder();
	
					// Let's find the name of plugload from cache. Let's put this in
					// try/catch as failure
					// to find the name should not stop profile change.
					try {
						for (Long id : deviceIdsArr) {
							if (PlugloadCache.getInstance().getDevice(new Long(id)) != null) {
								plugloadName.append(PlugloadCache.getInstance()
										.getDevice(new Long(id)).getPlugload().getName()
										+ ",");
							}else{
								plugloadName.append("No Name(id)" + "," + id);	
							}
						}
						userAuditLoggerUtil
								.log("Image Upgrade for " + plugloadName,
										UserAuditActionType.Plugload_Image_Upgrade
												.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
	
				}
			}
			
			new Thread("Device ImageUpgrade") {
				public void run() {
					ImageUpgradeSO.getInstance().startDeviceImageUpgrade(jobList);
				}
			}.start();
		}
		catch (Exception e) {
			e.printStackTrace();
			EmsModeControl.resetMode();
		}

		return "{\"success\":" + result + ", \"message\" : \"success\"}";
	}

	private Long[] convertStringToLongArray(String[] strArray) {
  	
		Long[] longArray = new Long[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			longArray[i] = Long.parseLong(strArray[i]);
		}
		return longArray;
		
	} //end of method convertStringToLongArray
	
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
