/**
 * 
 */
package com.ems.mvc.controller;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.LocatorDevice;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.LocatorDeviceManager;
import com.ems.types.LocatorDeviceType;


/**
 * @author Sampath Akula
 * 
 */
@Controller
@RequestMapping("/devices/locatordevices")
public class LocatorDevicesController {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;
    
    @Resource(name = "locatorDeviceManager")
    private LocatorDeviceManager locatorDeviceManager;
        

    /**
     * Manages the list of locator devices and create more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * 
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageSwitches(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            List<LocatorDevice> locatordevicesList = null;
            locatordevicesList = locatorDeviceManager.loadAllLocatorDevices();
            model.addAttribute("locatordevices", locatordevicesList);
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            List<LocatorDevice> locatordevicesList = null;
            locatordevicesList = locatorDeviceManager.loadLocatorDevicesByCampusId(id);
            model.addAttribute("locatordevices", locatordevicesList);
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            List<LocatorDevice> locatordevicesList = null;
            locatordevicesList = locatorDeviceManager.loadLocatorDevicesByBuldingId(id);
            model.addAttribute("locatordevices", locatordevicesList);
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            List<LocatorDevice> locatordevicesList = null;
            locatordevicesList = locatorDeviceManager.loadLocatorDevicesByFloorId(id);
            model.addAttribute("locatordevices", locatordevicesList);
            model.addAttribute("floorId", id);
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
            model.addAttribute("mode", "admin");
            break;
        }
        }

        return "devices/locatordevices/list";
    }

    @RequestMapping("/locatorDevice_create.ems")
    String createLocatorDevice(Model model, @RequestParam("xaxis") String xaxis, @RequestParam("yaxis") String yaxis, @RequestParam("page") String page, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        LocatorDevice locatorDeviceObj = new LocatorDevice();
        locatorDeviceObj.setFloorId(cookieHandler.getFacilityId());
        model.addAttribute("locatordevice", locatorDeviceObj);
        model.addAttribute("action", "create");
        model.addAttribute("xaxis", Double.parseDouble(xaxis));
        model.addAttribute("yaxis", Double.parseDouble(yaxis));
        model.addAttribute("page", page);
        model.addAttribute("locatorDeviceTypeList", LocatorDeviceType.values());
        
        return "devices/locatordevices/details";
    }
    
    @RequestMapping("/locatorDevice_edit.ems")
    String editLocatorDevice(Model model, @RequestParam("locatorDeviceId") String locatorDeviceId, @RequestParam("page") String page) {
        LocatorDevice locatorDeviceObj = locatorDeviceManager.getLocatorDeviceById(Long.parseLong(locatorDeviceId));
        model.addAttribute("locatordevice", locatorDeviceObj);
        model.addAttribute("action", "edit");
        model.addAttribute("page", page);
        model.addAttribute("locatorDeviceTypeList", LocatorDeviceType.values());
        
        return "devices/locatordevices/details";
    }
        

}
