/**
 * 
 */
package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.AreaManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.GroupManager;
import com.ems.types.FacilityType;
import com.ems.types.GGroupType;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/fixtures")
public class FixtureController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;

    @Resource(name = "groupManager")
    private GroupManager groupManager;

    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;

    @Resource(name = "areaManager")
    private AreaManager areaManager;

    @Resource(name = "gemsGroupManager")
    private GemsGroupManager gemsGroupManager;
    
    @Autowired
    private MessageSource messageSource;

    /**
     * 
     * @param model
     * @param fixtureId
     * @return
     */
    @RequestMapping("/fixture_form.ems")
    public String loadFixtureObject(Model model, @RequestParam("fixtureId") long fixtureId) {
        model.addAttribute("fixture", fixtureManager.getFixtureById(fixtureId));
        model.addAttribute("ballasts", fixtureManager.getAllBallasts());
        model.addAttribute("lamps", fixtureManager.getAllBulbs());
        model.addAttribute("groups", groupManager.loadAllGroups());

        return "devices/fixtures/details";
    }

    /**
     * 
     * @param model
     * @param fixtureId
     * @return
     */
    @RequestMapping("/fixture_details.ems")
    public String loadFixtureDetails(Model model, @RequestParam("fixtureId") long fixtureId) {
        model.addAttribute("fixtureId", fixtureId);
        return "devices/fixtures/tabpaneldetails";
    }

    @RequestMapping(value = "/updateFixture.ems")
    @ResponseBody
    public String updateFixture(Fixture fixture, Locale local) {
        // Get the fixture from the database
        Fixture savedFixture = fixtureManager.getFixtureById(fixture.getId());

        // Let's populate the values on the fixture
        savedFixture.setFixtureName(fixture.getFixtureName());
        savedFixture.setIsHopper(fixture.getIsHopper());

        savedFixture.setBallast(fixtureManager.loadBallast(fixture.getBallast().getId()));
        savedFixture.setBulb(fixtureManager.loadBulb(fixture.getBulb().getId()));
        savedFixture.setNoOfBulbs(fixture.getNoOfBulbs());
        savedFixture.setNoOfFixtures(fixture.getNoOfFixtures());
        savedFixture.setVoltage(fixture.getVoltage());
        fixtureManager.updateFixture(savedFixture);

        if (savedFixture.getGroupId().longValue() != fixture.getGroupId().longValue()) {
            fixtureManager.changeFixtureProfile(fixture.getId(), fixture.getGroupId(), fixture.getCurrentProfile(), fixture.getOriginalProfileFrom());
        }

        userAuditLoggerUtil.log("Update Fixture: " + savedFixture.getId());	
        
        return "{\"success\":1, \"message\" : \""
                + messageSource.getMessage("fixtureForm.message.success", null, local) + "\"}";
    }

    /**
     * Manages the list of fixtures and discover more
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate level of the organization
     * @return titles template definition to display manageFixtures page
     */
    @RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
    public String manageFixtures(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case COMPANY: {
            model.addAttribute("page", "company");
            //model.addAttribute("fixtures", fixtureManager.loadAllFixtures());
            model.addAttribute("mode", "admin");
            break;
        }
        case CAMPUS: {
            model.addAttribute("page", "campus");
            //model.addAttribute("fixtures", fixtureManager.loadFixtureByCampusId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case BUILDING: {
            model.addAttribute("page", "building");
            //model.addAttribute("fixtures", fixtureManager.loadFixtureByBuildingId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        case FLOOR: {
            model.addAttribute("page", "floor");
            //model.addAttribute("fixtures", fixtureManager.loadFixtureByFloorId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        default: {
            model.addAttribute("page", "area");
            //model.addAttribute("fixtures", fixtureManager.loadFixtureByAreaId(id));
            model.addAttribute("mode", "admin");
            break;
        }
        }

        model.addAttribute("pid", id);
        model.addAttribute("currenttime", new Date().getTime());
        return "devices/fixtures/list";
    }

    @RequestMapping("/fixture_commission_form")
    public String commissionFixtures(Model model, @RequestParam("gatewayId") long gatewayId,
            @RequestParam("type") long type, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
            @RequestParam("isBulkCommission") Boolean isBulkCommission,
            @RequestParam(value = "fixtureId", required = false) Long fixtureId) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        Gateway gw = gatewayManager.loadGateway(gatewayId);
        switch (cookieHandler.getFaciltiyType()) {
        case FLOOR: {
            model.addAttribute("ballasts", fixtureManager.getAllBallasts());
            model.addAttribute("lamps", fixtureManager.getAllBulbs());
            model.addAttribute("groups", groupManager.loadAllGroups());

            model.addAttribute("floorId", id);
            model.addAttribute("fixtureId", fixtureId);
            model.addAttribute("type", type);
            model.addAttribute("gateway", gw);
            model.addAttribute("isBulkCommission", isBulkCommission);
            model.addAttribute("message", "");
            break;
        }
        default: {
            model.addAttribute("ballasts", null);
            model.addAttribute("lamps", null);
            model.addAttribute("groups", null);

            model.addAttribute("floorId", id);
            model.addAttribute("fixtureId", fixtureId);
            model.addAttribute("type", type);
            model.addAttribute("gateway", gw);
            model.addAttribute("isBulkCommission", isBulkCommission);
            model.addAttribute("message", "Not supported.");
            break;
        }
        }
        return "devices/fixture/commission";
    }

    @RequestMapping("/fixture_commission_start_identify.ems")
    public String identifyFixtureCommission(Model model,
            @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
            @RequestParam("isBulkCommission") Boolean isBulkCommission, @RequestParam("gatewayId") Long gatewayId,
            @RequestParam("fixtureId") Long fixtureId) {
        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
        Long id = cookieHandler.getFacilityId();

        switch (cookieHandler.getFaciltiyType()) {
        case FLOOR: {
            List<Gateway> floorGatewaysList = null;

            if (isBulkCommission) {
                floorGatewaysList = gatewayManager.loadFloorGateways(id);
            } else {
                floorGatewaysList = new ArrayList<Gateway>();
                floorGatewaysList.add(gatewayManager.loadGateway(gatewayId));
            }

            Map<Long, Integer> FixtureMap = new HashMap<Long, Integer>();
            List<Gateway> gatewayData = new ArrayList<Gateway>();

            if ((floorGatewaysList != null) && (floorGatewaysList.size() > 0)) {
                for (Gateway gateway : floorGatewaysList) {
                    if (gateway.isCommissioned()) {
                        gatewayData.add(gateway);
                        List<Fixture> fxList = fixtureManager.getUnCommissionedFixtureList(gateway.getId());
                        FixtureMap.put(gateway.getId(), (fxList != null ? fxList.size() : 0));
                    }
                }
            }

            model.addAttribute("floorId", id);
            model.addAttribute("gateways", gatewayData);
            model.addAttribute("fixturesMap", FixtureMap);
            model.addAttribute("message", "");

            // options
            model.addAttribute("isBulkCommission", isBulkCommission);
            model.addAttribute("fixtureId", fixtureId);
            break;
        }
        default: {
            model.addAttribute("floorId", id);
            model.addAttribute("gateways", null);
            model.addAttribute("fixturesMap", null);
            model.addAttribute("message", "Not supported.");

            // options
            model.addAttribute("isBulkCommission", isBulkCommission);
            model.addAttribute("fixtureId", fixtureId);
            break;
        }
        }
        return "devices/fixture/startcommission";
    }

    @RequestMapping("/fixture_discovery_form.ems")
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
        return "devices/fixture/discovery";
    }
    
	@RequestMapping("/fixture_delete_dialog.ems")
    public String fixtureDeleteDialog(Model model){
        return "fixtures/delete/dialog";
    }
	
	@RequestMapping("/assignprofiletofixtures.ems")
    public String assignProfileToFixtureDialog(Model model){
        model.addAttribute("profiles", groupManager.loadAllGroups());
        return "devices/fixtures/assignprofile/dialog";
    }
	
	@RequestMapping("/assignareatofixtures.ems")
    public String assignAreaToFixtureDialog(Model model, @CookieValue(value = FacilityCookieHandler.selectedFacilityCookie, required = false) String cookie){
	    FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);

        if(cookieHandler.getFaciltiyType() == FacilityType.FLOOR){
            Long floorId = cookieHandler.getFacilityId();
            try {
                model.addAttribute("areas", areaManager.loadAreaByFloorId(floorId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            model.addAttribute("areas", null);
        }
        return "devices/fixtures/assignarea/dialog";
    }
	
	@RequestMapping("/assigngroupstoswitches.ems")
    public String assignGemsGroupToFixtureDialog(Model model){
        model.addAttribute("groups", gemsGroupManager.loadGroupsByCompany(1L));
        model.addAttribute("grouptypes", GGroupType.MotionGroup.getName());
        return "devices/fixtures/assigngemsgroups/dialog";
    }
}
