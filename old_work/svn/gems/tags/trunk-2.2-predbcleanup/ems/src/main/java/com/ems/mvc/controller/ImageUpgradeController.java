package com.ems.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import com.ems.model.User;
import com.ems.mvc.util.EmsModeControl;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.upgrade.ImageUpgradeJob;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.RequestUtil;

@Controller
@RequestMapping("/imageupgrade")
@PreAuthorize("hasAnyRole('Admin')")
public class ImageUpgradeController {
	static final String FIXTURE_IMAGE_KEY = "fixtureUpgradeimages";
	static final String GATEWAY_IMAGE_KEY = "gatewayUpgradeimages";
    static final String WDS_IMAGE_KEY = "wdsUpgradeimages";

	@Resource(name = "firmwareUpgradeManager")
	private FirmwareUpgradeManager firmwareUpgradeManager;

	@Resource
	FacilityTreeManager facilityTreeManager;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	UserAuditLoggerUtil userAuditLoggerUtil;

	@RequestMapping("/get_details.ems")
	public String getImageUpgradeDetails(HttpServletRequest request, Model model) {
		String tomcatBase = RequestUtil.getTomcatBasePath(request);
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
                (String[]) listsMap.get(WDS_IMAGE_KEY));
		// add Attribute companies
		// add Attribute fixture firmware upgrade name
		// add Attribute gateway firmware upgrade name

		TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
				.loadCompanyHierarchy();
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(false);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		model.addAttribute("message", request.getParameter("message"));
		model.addAttribute("viewTreeOnly", true);

		return "imageupgrade/details";
	}

	@RequestMapping("/saveNewImages.ems")
	public String uploadImageFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			@RequestParam("fileName") String fileName, Model model,
			Locale locale) {
		try {
			String tomcatBase = RequestUtil.getTomcatBasePath(request);
			String path = tomcatBase
					+ messageSource.getMessage("upgrade.image.location", null,
							locale);

			if (!file.isEmpty()) {
				firmwareUpgradeManager.uploadImageFile(path, fileName, file);
				model.addAttribute("message", messageSource.getMessage(
						"imageUpgrade.message.uploadFileSuccess", null, locale));
			} else {
				model.addAttribute("message", messageSource.getMessage(
						"imageUpgrade.message.uploadFileFailed", null, locale));
			}
		} catch (IOException ioe) {
			model.addAttribute("message", messageSource.getMessage(
					"imageUpgrade.message.uploadFileFailed", null, locale));
			ioe.printStackTrace();
		}

		// return "imageupgrade/details";
		return "redirect:/imageupgrade/get_details.ems";
	}

	@RequestMapping(value = "/startimageupgrade.ems", method = RequestMethod.POST)
	@ResponseBody
	public String startImageUpgrade(HttpServletRequest request,
			@RequestParam("gwImageName") String gwImageFileName,
			@RequestParam("gatewayIds") String gatewayIds,
			@RequestParam("fxImageName") String fxImageFileName,
			@RequestParam("fixtureIds") String fixtureIds,
            @RequestParam("wdsImageName") String wdsImageFileName,
            @RequestParam("wdsIds") String wdsIds) {
		int result = 0;
		if (ImageUpgradeSO.isInProgress()) {
			return "{\"success\":" + result + ", \"message\" : \"In Progess\"}";
		}

		if (!"S".equals(EmsModeControl.setMode("NORMAL:IMAGE_UPGRADE"))) {
			return "{\"success\":" + -1 + ", \"message\" : \"Try Later\"}";
		}

		User usr = (User) request.getSession().getAttribute("USER");
		final ArrayList<ImageUpgradeJob> jobList = new ArrayList<ImageUpgradeJob>();

		if (gatewayIds != null && gatewayIds.length() > 0) {
			int deviceIdsArr[] = convertStringToIntArray(gatewayIds.split(","));
			if (gwImageFileName != null) {
				// saves to firmware_upgrade table
				result = firmwareUpgradeManager.saveImageVersion(
						ServerConstants.DEVICE_GATEWAY, deviceIdsArr,
						gwImageFileName, usr);
				// Create a gateway upgrade job
				ImageUpgradeJob gwJob = new ImageUpgradeJob();
				gwJob.setDeviceType(ServerConstants.DEVICE_GATEWAY);
				gwJob.setDeviceIds(deviceIdsArr);
				gwJob.setFileName(gwImageFileName);
				jobList.add(gwJob);

				StringBuilder gatewayName = new StringBuilder();
				// Let's find the name of gateway from cache. LEt's put this in
				// try/catch as failure
				// to find the name should not stop decommission.
				try {
					for (int id : deviceIdsArr) {
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
			int deviceIdsArr[] = convertStringToIntArray(fixtureIds.split(","));
			if (fxImageFileName != null) {
				// Adds to the firmware_upgrade table
				result = firmwareUpgradeManager.saveImageVersion(
						ServerConstants.DEVICE_FIXTURE, deviceIdsArr,
						fxImageFileName, usr);
				// Creates a Fixture upgrade job
				ImageUpgradeJob fixtureJob = new ImageUpgradeJob();
				fixtureJob.setDeviceType(ServerConstants.DEVICE_FIXTURE);
				fixtureJob.setDeviceIds(deviceIdsArr);
				fixtureJob.setFileName(fxImageFileName);
				jobList.add(fixtureJob);

				StringBuilder fixtureName = new StringBuilder();

				// Let's find the name of fixture from cache. Let's put this in
				// try/catch as failure
				// to find the name should not stop profile change.
				try {
					for (int id : deviceIdsArr) {
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
			int deviceIdsArr[] = convertStringToIntArray(wdsIds.split(","));
			if (wdsImageFileName != null) {
				// Adds to the firmware_upgrade table
				result = firmwareUpgradeManager.saveImageVersion(ServerConstants.DEVICE_SWITCH, deviceIdsArr,
						wdsImageFileName, usr);
				// Creates a Fixture upgrade job
				ImageUpgradeJob wdsJob = new ImageUpgradeJob();
				wdsJob.setDeviceType(ServerConstants.DEVICE_SWITCH);
				wdsJob.setDeviceIds(deviceIdsArr);
				wdsJob.setFileName(wdsImageFileName);
				jobList.add(wdsJob);

				StringBuilder wdsName = new StringBuilder();

				// TODO
				// Let's find the name of wds from cache. Let's put this in
				// try/catch as failure				
				try {					
					userAuditLoggerUtil
							.log("Image Upgrade for Wds(" + wdsIds + ")", UserAuditActionType.WDS_Image_Upgrade.getName());
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

		return "{\"success\":" + result + ", \"message\" : \"success\"}";
	}

	public int[] convertStringToIntArray(String[] strArray) {
		int[] intArray = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			intArray[i] = Integer.parseInt(strArray[i]);
		}
		return intArray;
	}
}