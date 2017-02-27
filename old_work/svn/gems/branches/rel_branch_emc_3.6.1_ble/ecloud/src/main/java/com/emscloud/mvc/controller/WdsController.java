/**
 * 
 */
package com.emscloud.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.vos.Wds;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.WdsManager;
import com.emscloud.types.FacilityType;
import com.emscloud.util.DateUtil;
import com.emscloud.util.FacilityCookieHandler;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/wds")
public class WdsController {
      
    @Resource(name = "wdsManager")
    private WdsManager wdsManager;
    
    @Resource(name = "communicationUtils")
	CommunicationUtils communicationUtils;
    
    @Resource(name = "facilityManager")
    private FacilityManager facilityManager;
   
    @RequestMapping(value = "/wds_form.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadWdsObject(Model model,
			@RequestParam("wdsId") long wdsId,@RequestParam("pid") long pid) {
		
		Wds wds = wdsManager.getWdsDetails(wdsId,pid);
		if(wds != null){
			model.addAttribute("wds", wds);
		}else{
			model.addAttribute("wds", new Wds());
		}
		return "devices/wds/details";
	}
    
    /**
     * Manages the list of Wds and create more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return titles template definition to display manageWds page
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageWds(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();   
        //List<Wds> wdsList = new ArrayList<Wds>();
        switch (cookieHandler.getFaciltiyType()) {
        case ORGANIZATION: {
            model.addAttribute("page", "company");
            //wdsList = wdsManager.loadAllWds();
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            //wdsList = wdsManager.loadWdsByCampusId(id);
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            //wdsList = wdsManager.loadWdsByBuildingId(id);
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            //wdsList = wdsManager.loadWdsByFloorId(id);
            model.addAttribute("floorId", id);
            model.addAttribute("mode", "admin");
            Facility facility = facilityManager.getFacility(id);
            if (FacilityType.getFacilityType(FacilityType.FLOOR) == facility.getType()) {
				List<EmInstance> emList = communicationUtils.getEmMap(facility);
				if(!ArgumentUtils.isNullOrEmpty(emList)){
					model.addAttribute("emtimezone", DateUtil.getAbbreviatedTimeZone(emList.get(0).getTimeZone()));
				}
			}
            break;
        }
        default: {
            model.addAttribute("page", "area");
            //wdsList = wdsManager.loadWdsByAreaId(id);
            model.addAttribute("mode", "admin");
            break;
        }
        }
        
        //wdsList = wdsManager.getWdsList(cookieHandler.getFaciltiyType().toString(), id);
        model.addAttribute("pid", id);
        //model.addAttribute("wdss", wdsList);
        return "devices/wds/list";
    }  
    
  }
