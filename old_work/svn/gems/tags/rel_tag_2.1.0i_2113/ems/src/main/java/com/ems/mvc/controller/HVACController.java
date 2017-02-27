package com.ems.mvc.controller;

import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.model.HVACDevice;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.HVACDevicesManager;
import com.ems.types.HvacDeviceType;
import com.ems.types.UserAuditActionType;

@Controller
@RequestMapping("/devices/hvac")
public class HVACController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "hvacDevicesManager")
    private HVACDevicesManager hvacDevicesManager;
	
	@Autowired
	private MessageSource messageSource;
	
	/**
     * 
     * @param model
     * @return
     */
    @RequestMapping("/create/dialog.ems")
    public String loadHvacObject(Model model,@RequestParam("xaxis") String xaxis,@RequestParam("yaxis") String yaxis,@RequestParam("floorid") String floorid) {
    	HVACDevice hvacDevice = new HVACDevice() ;
    	model.addAttribute("xaxis", Long.parseLong(xaxis));
    	model.addAttribute("yaxis", Long.parseLong(yaxis));
    	model.addAttribute("floorid", Long.parseLong(floorid));
        model.addAttribute("deviceType", HvacDeviceType.class.getEnumConstants());
        model.addAttribute("hvacDevice", hvacDevice);
        return "devices/hvac/create/dialog";
    }
    
    /**
     * 
     * @param model
     * @return
     */
    @RequestMapping("/hvacdetails.ems")
    public String loadHvacDetails(Model model,@RequestParam("hvacId") String hvacId) {
    	model.addAttribute("hvacId",  hvacId);
        return "devices/hvac/details";
    }
    
    /**
     * 
     * @param model
     * @return
     */
    @RequestMapping("/hvaclist.ems")
    public String loadallHvac(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
    	
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
	    Long id = cookieHandler.getFacilityId();
    	String facilityType = cookieHandler.getFaciltiyType().toString();
    	model.addAttribute("hvaclist", hvacDevicesManager.loadHVACDevicesByFacilityId(facilityType, id));
        return "devices/hvac/details";
    }
    
    /**
	 * Manages the list of HVAC devicese
	 * 
	 * @param model
	 *            used in communicating back
	 * @param cookie
	 *            distinguishes the appropriate level of the organization
	 * @return titles template definition to display manageGateways page
	 */
	@RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
	public String manageHVACDevices(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie, @RequestParam(value = "updateStatus", required = false) String updateStatus) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
		model.addAttribute("updateStatus", updateStatus);	
		switch (cookieHandler.getFaciltiyType()) {
		case COMPANY: {
			model.addAttribute("page", "company");
			model.addAttribute("hvacDevices", hvacDevicesManager.loadHVACDevicesByFacilityId("company", id));
			model.addAttribute("mode", "admin");
			break;
		}
		case CAMPUS: {
			model.addAttribute("page", "campus");
			model.addAttribute("hvacDevices", hvacDevicesManager.loadHVACDevicesByFacilityId("campus", id));
			model.addAttribute("mode", "admin");
			break;
		}
		case BUILDING: {
			model.addAttribute("page", "building");
			model.addAttribute("hvacDevices", hvacDevicesManager.loadHVACDevicesByFacilityId("building", id));
			model.addAttribute("mode", "admin");
			break;
		}
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("floorId", id);
            model.addAttribute("hvacDevices", hvacDevicesManager.loadHVACDevicesByFacilityId("floor", id));
            model.addAttribute("mode", "admin");
            break;
        }
		default: {
			model.addAttribute("page", "area");
    		model.addAttribute("hvacDevices", hvacDevicesManager.loadHVACDevicesByFacilityId("area", id));
			model.addAttribute("mode", "admin");
			break;
		}
		}

        return "devices/hvac/list";
	}
    
	@RequestMapping("/load.ems")
	public String loadHVACDevice(Model model,
			@RequestParam("hvacId") long hvacId) {
		HVACDevice hvacDevice = hvacDevicesManager.loadHvacById(hvacId);
		model.addAttribute("hvacDevice", hvacDevice);
		return "devices/hvac/loadDetails";
	}
	
	@RequestMapping(value = "/update.ems")
	public String updateHvac(HVACDevice device) {
		HVACDevice savedDevice = hvacDevicesManager.loadHvacById(device.getId());
		savedDevice.setDeviceId(device.getDeviceId());
		savedDevice.setName(device.getName());
		savedDevice.setDeviceType(device.getDeviceType());
		hvacDevicesManager.save(savedDevice);
		userAuditLoggerUtil.log("Update HVAC Device: " + device.getName(), UserAuditActionType.HVAC_Device_Update.getName());	
		return "redirect:/devices/hvac/manage.ems?updateStatus=success";
	}
}
