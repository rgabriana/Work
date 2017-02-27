package com.ems.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ems.model.User;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.server.ServerConstants;
import com.ems.server.upgrade.ImageUpgradeJob;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.RequestUtil;

@Controller
@RequestMapping("/imageupgrade")
public class ImageUpgradeController {
    static final String FIXTURE_IMAGE_KEY = "fixtureUpgradeimages";
    static final String GATEWAY_IMAGE_KEY = "gatewayUpgradeimages";

    @Resource(name = "firmwareUpgradeManager")
    private FirmwareUpgradeManager firmwareUpgradeManager;

    @Resource
    FacilityTreeManager facilityTreeManager;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping("/get_details.ems")
    public String getImageUpgradeDetails(HttpServletRequest request, Model model) {
        String tomcatBase = RequestUtil.getTomcatBasePath(request);
        String imagespath = tomcatBase + messageSource.getMessage("upgrade.image.location", null, null);

        Map<String, String[]> listsMap = firmwareUpgradeManager.getFirmwareImagesList(imagespath);
        model.addAttribute(FIXTURE_IMAGE_KEY, (String[]) listsMap.get(FIXTURE_IMAGE_KEY));
        model.addAttribute(GATEWAY_IMAGE_KEY, (String[]) listsMap.get(GATEWAY_IMAGE_KEY));
        // add Attribute companies
        // add Attribute fixture firmware upgrade name
        // add Attribute gateway firmware upgrade name

        TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
        model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
        JsTreeOptions jsTreeOptions = new JsTreeOptions();
        jsTreeOptions.setCheckBoxes(false);
        model.addAttribute("jsTreeOptions", jsTreeOptions);
        model.addAttribute("message", request.getParameter("message"));
        model.addAttribute("viewTreeOnly", true);
        
        return "imageupgrade/details";
    }

    @RequestMapping("/saveNewImages.ems")
    public String uploadImageFile(HttpServletRequest request, @RequestParam("upload") MultipartFile file,
            @RequestParam("fileName") String fileName, Model model, Locale locale) {
        try {
            String tomcatBase = RequestUtil.getTomcatBasePath(request);
            String path = tomcatBase + messageSource.getMessage("upgrade.image.location", null, locale);

            if (!file.isEmpty()) {
                firmwareUpgradeManager.uploadImageFile(path, fileName, file);
                model.addAttribute("message",
                        messageSource.getMessage("imageUpgrade.message.uploadFileSuccess", null, locale));
            } else {
                model.addAttribute("message",
                        messageSource.getMessage("imageUpgrade.message.uploadFileFailed", null, locale));
            }
        } catch (IOException ioe) {
            model.addAttribute("message",
                    messageSource.getMessage("imageUpgrade.message.uploadFileFailed", null, locale));
            ioe.printStackTrace();
        }
        
        // return "imageupgrade/details";
        return "redirect:/imageupgrade/get_details.ems";
    }

    @RequestMapping(value = "/saveImageVersion.ems")
    @ResponseBody
    public String saveImageVersion(HttpServletRequest request, @RequestParam("deviceType") String deviceType,
            @RequestParam("deviceIds") String deviceIds, @RequestParam("imageName") String imageFileName) {
        int result = 0;
        if (ImageUpgradeSO.isInProgress()) {
            return "{\"success\":" + result + ", \"message\" : \"In Progess\"}";
        }

        User usr = (User) request.getSession().getAttribute("USER");

        if (deviceIds != null) {
            int deviceIdsArr[] = convertStringToIntArray(deviceIds.split(","));
            int device = Integer.parseInt(deviceType);
            result = firmwareUpgradeManager.saveImageVersion(device, deviceIdsArr, imageFileName, usr);
        }

        return "{\"success\":" + result + ", \"message\" : \"success\"}";
    }

    @RequestMapping(value = "/startimageupgrade.ems", method = RequestMethod.POST)
    @ResponseBody
    public String startImageUpgrade(HttpServletRequest request, @RequestParam("gwImageName") String gwImageFileName,
            @RequestParam("gatewayIds") String gatewayIds, @RequestParam("fxImageName") String fxImageFileName,
            @RequestParam("fixtureIds") String fixtureIds) {
        int result = 0;
        if (ImageUpgradeSO.isInProgress()) {
            return "{\"success\":" + result + ", \"message\" : \"In Progess\"}";
        }

        User usr = (User) request.getSession().getAttribute("USER");
        final ArrayList<ImageUpgradeJob> jobList = new ArrayList<ImageUpgradeJob>();

        if (gatewayIds != null && gatewayIds.length() > 0) {
            int deviceIdsArr[] = convertStringToIntArray(gatewayIds.split(","));
            if (gwImageFileName != null) {
                result = firmwareUpgradeManager.saveImageVersion(ServerConstants.DEVICE_GATEWAY, deviceIdsArr, gwImageFileName, usr);
                ImageUpgradeJob gwJob = new ImageUpgradeJob();
                gwJob.setDeviceType(ServerConstants.DEVICE_GATEWAY);
                gwJob.setDeviceIds(deviceIdsArr);
                gwJob.setFileName(gwImageFileName);
                jobList.add(gwJob);
            }
        }
        
        if (fixtureIds != null && fixtureIds.length() > 0) {
            int deviceIdsArr[] = convertStringToIntArray(fixtureIds.split(","));
            if (fxImageFileName != null) {
                result = firmwareUpgradeManager.saveImageVersion(ServerConstants.DEVICE_FIXTURE, deviceIdsArr, fxImageFileName, usr);
                ImageUpgradeJob fixtureJob = new ImageUpgradeJob();
                fixtureJob.setDeviceType(ServerConstants.DEVICE_FIXTURE);
                fixtureJob.setDeviceIds(deviceIdsArr);
                fixtureJob.setFileName(fxImageFileName);
                jobList.add(fixtureJob);
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
