/**
 * 
 */
package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.Wds;
import com.ems.model.WdsModelType;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.WdsManager;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/wds")
public class WdsController {
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;

    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;

    @Resource(name = "wdsManager")
    private WdsManager wdsManager;

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

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            model.addAttribute("wdss", wdsManager.loadAllWds());
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            model.addAttribute("wdss", wdsManager.loadWdsByCampusId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            model.addAttribute("wdss", wdsManager.loadWdsByBuildingId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            model.addAttribute("wdss", wdsManager.loadWdsByFloorId(id));
            model.addAttribute("floorId", id);
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
            model.addAttribute("wdss", wdsManager.loadWdsByAreaId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        }

        return "devices/wds/list";
    }
    
    
    @RequestMapping(value = "/wdsedit.ems", method = RequestMethod.GET)
    public String editWds(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,@RequestParam("wdsId") String wdsId) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);                
        
        model.addAttribute("wds",wdsManager.loadWdsById(Long.parseLong(wdsId)));
        model.addAttribute("wdsmodel",wdsManager.loadWdsById(Long.parseLong(wdsId)).getWdsModelType().getName());
        
        return "/devices/wds/wdsedit";
    }

    
    

    @RequestMapping(value = "/discoverWds.ems")
    public String discoverGateway(Model model, @RequestParam("wdsId") Long gwId) {
        List<Wds> oDiscoveredWdsList = new ArrayList<Wds>();
        try {
            oDiscoveredWdsList = wdsManager.getUnCommissionedWDSList(gwId);
        } catch (Exception e) {
            System.out.println("Discovery WDS, inventory table already has the Wireless Decorative Wds "
                    + e.getMessage());
        }
        if (oDiscoveredWdsList.size() > 0) {
            model.addAttribute("message", "New WDS discovered.");
        }
        model.addAttribute("page", "floor");
        model.addAttribute("switches", oDiscoveredWdsList);
        model.addAttribute("mode", "admin");

        return "devices/switches/list";
    }

    @RequestMapping("/wds_commission_start_identify.ems")
    public String identifyFixtureCommission(Model model,
            @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case FLOOR: {
            List<Gateway> floorGatewaysList = null;

            floorGatewaysList = gatewayManager.loadFloorGateways(id);

            //Map<Long, Integer> FixtureMap = new HashMap<Long, Integer>();
            List<Gateway> gatewayData = new ArrayList<Gateway>();

            if ((floorGatewaysList != null) && (floorGatewaysList.size() > 0)) {
                for (Gateway gateway : floorGatewaysList) {
                    if (gateway.isCommissioned()) {
                        gatewayData.add(gateway);
                    }
                }
            }

            model.addAttribute("floorId", id);
            model.addAttribute("gateways", gatewayData);
            model.addAttribute("message", "");
            break;
        }
        default: {
            model.addAttribute("floorId", id);
            model.addAttribute("gateways", null);
            model.addAttribute("message", "Not supported.");
            break;
        }
        }
        return "devices/wds/startcommission";
    }
    
    @RequestMapping("/wds_commission_form")
    public String commissionFixtures(Model model, @RequestParam("gatewayId") long gatewayId,
            @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        Gateway gw = gatewayManager.loadGateway(gatewayId);

        switch (cookieHandler.getFaciltiyType()) {
        case FLOOR: {
            model.addAttribute("floorId", id);
            model.addAttribute("gateway", gw);
            model.addAttribute("message", "");
            break;
        }
        default: {
            model.addAttribute("floorId", id);
            model.addAttribute("gateway", gw);
            model.addAttribute("message", "Not supported.");
            break;
        }
        }
        return "devices/wds/commission";
    }

    @RequestMapping("/wds_discovery_form.ems")
    public String fixtureDiscovery(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case FLOOR: {
            List<Gateway> floorGatewaysList = gatewayManager.loadFloorGateways(id);
            Map<Long, Integer> fixturesCountMap = new HashMap<Long, Integer>();
            Map<Long, Integer> sensorsCountMap = new HashMap<Long, Integer>();
            List<Gateway> gatewayData = new ArrayList<Gateway>();

            if ((floorGatewaysList != null) && (floorGatewaysList.size() > 0)) {
                for (Gateway gateway : floorGatewaysList) {
                    if (gateway.isCommissioned()) {
                        gatewayData.add(gateway);
                        sensorsCountMap.put(gateway.getId(), gateway.getNoOfSensors());
                        List<Fixture> fxList = fixtureManager.loadAllFixtureByGatewayId(gateway.getId());
                        fixturesCountMap.put(gateway.getId(), (fxList != null ? fxList.size() : 0));
                    }
                }
            }

            model.addAttribute("floorId", id);
            model.addAttribute("gateways", gatewayData);
            model.addAttribute("sensorsCountMap", sensorsCountMap);
            model.addAttribute("fixturesCountMap", fixturesCountMap);
            model.addAttribute("message", "");
            break;
        }
        default: {
            model.addAttribute("floorId", id);
            model.addAttribute("gateways", null);
            model.addAttribute("sensorsCountMap", null);
            model.addAttribute("fixturesCountMap", null);
            model.addAttribute("message", "Not supported.");
            break;
        }
        }
        return "devices/wds/discovery";
    }
}
