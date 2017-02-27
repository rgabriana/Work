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

import com.ems.model.BallastVoltPower;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.Gateway;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Groups;
import com.ems.model.Tenant;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.server.ServerConstants;
import com.ems.service.AreaManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureCalibrationManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.GroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.ProfileManager;
import com.ems.service.SwitchManager;
import com.ems.service.UserManager;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.vo.model.FixtureLampStatusVO;

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
        
    @Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
    
    @Resource
	private UserManager userManager;
    @Autowired
    private MessageSource messageSource;

    @Resource(name = "gemsGroupManager")
	private GemsGroupManager gemsGroupManager;

    @Resource(name = "motionGroupManager")
	private MotionGroupManager motionGroupManager;

    @Resource(name = "switchManager")
	private SwitchManager switchManager;
    
    @Resource
    private FixtureCalibrationManager fixtureCalibrationManager;
    
    @Resource
	private EventsAndFaultManager eventsAndFaultManager;
    /**
     * 
     * @param model
     * @param fixtureId
     * @return
     */
    @RequestMapping("/fixture_form.ems")
    public String loadFixtureObject(Model model, @RequestParam("fixtureId") long fixtureId) {
    	Fixture fixture = fixtureManager.getFixtureById(fixtureId);
    	model.addAttribute("fixture", fixture);
        model.addAttribute("ballasts", fixtureManager.getAllBallasts());
        model.addAttribute("lamps", fixtureManager.getAllBulbs());
        
        //Role Based Display of profiles in the Fixture Form
    	List<Groups> profileList=null;
    	//Set the Tenant profile if present
        Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
        Long tenantId=0L;
        Long templateId = 0L;
        Groups group = groupManager.getGroupById(fixture.getGroupId());
		if(group.getProfileTemplate()!=null)
			templateId= group.getProfileTemplate().getId();
        if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin || emsAuthContext.getCurrentUserRoleType() == RoleType.FacilitiesAdmin)
		{
			profileList = groupManager.loadAllGroupsExceptDeafult();
		}else if(emsAuthContext.getCurrentUserRoleType()==RoleType.TenantAdmin || emsAuthContext.getCurrentUserRoleType()==RoleType.Employee)
		{
			if(tenant != null)
			{
				tenantId = tenant.getId();
				profileList = groupManager.loadAllProfileForTenantByTemplateId(templateId,tenantId);
			}
			else
			{
				profileList = groupManager.loadAllProfileTemplateById(templateId, 0L);
			}
		}
        model.addAttribute("groups", profileList);
        //Set current profile and original profile as per type
        String originalProfileFrom=getDisplayProfileName(fixture.getOriginalProfileFrom());
        String currentProfile=getDisplayProfileName(fixture.getCurrentProfile());
        model.addAttribute("originalProfileFrom",originalProfileFrom);
        model.addAttribute("currentProfile",currentProfile);
        List<String> groupNames = new ArrayList<String>();
        List<GemsGroupFixture> listGrFix = gemsGroupManager.getAllGroupsOfFixture(fixture);
        if(listGrFix != null)
        {
	        for (GemsGroupFixture grpFix : listGrFix) {
	        	if(motionGroupManager.getMotionGroupByGemsGroupId(grpFix.getGroup().getId()) != null)
	        	{
	        		String name = grpFix.getGroup().getGroupName() + "[M]";
	        		groupNames.add(name);
	        	}
	        	else if(switchManager.getSwitchByGemsGroupId(grpFix.getGroup().getId()) != null)
	        	{
	        		String name = grpFix.getGroup().getGroupName() + "[S]";
	        		groupNames.add(name);
	        	}
	        }
	        model.addAttribute("groupList", groupNames);
        }
        
        FixtureLampCalibration flc = fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixtureId);
        List<BallastVoltPower> voltPowerList = fixtureManager.getBallastVoltPowerCurve(fixture.getBallast().getId().longValue(),fixture.getVoltage());
        String characterizationStatus = "";
        String fixtureStatus = "";
        if(flc!=null){
        	characterizationStatus = FixtureLampStatusVO.INDIVIDUAL;
        }else if(voltPowerList!=null && voltPowerList.size() >0){
        	characterizationStatus = FixtureLampStatusVO.GENERIC_FROM_BALLAST;
        }else{
        	characterizationStatus  = FixtureLampStatusVO.UNCHARACTERISED;
        }
        
        FixtureLampStatusVO fixtLampStatusVo= fixtureManager.getOutageTypeByFixtureId(fixtureId);
        if(fixtLampStatusVo.getFixtureStatus()!=null && fixtLampStatusVo.getFixtureStatus().equalsIgnoreCase(FixtureLampStatusVO.FIXTURE_OUT)){
        	fixtureStatus = FixtureLampStatusVO.FIXTURE_OUT;
        }else if(fixtLampStatusVo.getFixtureStatus()!=null && fixtLampStatusVo.getFixtureStatus().equalsIgnoreCase(FixtureLampStatusVO.LAMP_OUT)){
        	fixtureStatus = FixtureLampStatusVO.LAMP_OUT;
        }else{
        	fixtureStatus = FixtureLampStatusVO.OPERATIONAL;
        }
        model.addAttribute("characterizationStatus", characterizationStatus);
        model.addAttribute("fixtureStatus", fixtureStatus);
        return "devices/fixtures/details";
    }
    private String getDisplayProfileName(String profileName)
    {
    	 if(profileName.indexOf("_"+ServerConstants.DEFAULT_PROFILE)==-1 && !profileName.equals(ServerConstants.DEFAULT_PROFILE))
         {
         	Groups oriProfileGrpObj = groupManager.getGroupByName(profileName);
         	if(oriProfileGrpObj!=null && oriProfileGrpObj.isDefaultProfile()==true && oriProfileGrpObj.getProfileNo()>0)
         	{
         		profileName=profileName+"_"+ServerConstants.DEFAULT_PROFILE;
         	}
         }
    	return profileName;
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
        fixtureManager.updateFixture(savedFixture, false);

        if (savedFixture.getGroupId().longValue() != fixture.getGroupId().longValue()) {
            fixtureManager.changeFixtureProfile(fixture.getId(), fixture.getGroupId(), fixture.getCurrentProfile(), fixture.getOriginalProfileFrom());
        }

        if(savedFixture.getBallast().getId()!=fixture.getBallast().getId())
        {
        	eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
        	userAuditLoggerUtil.log("Update Ballast: " + savedFixture.getBallast().getBallastName()+ " for fixture "+savedFixture.getFixtureName(), UserAuditActionType.Ballast_Change.getName());
        }
        userAuditLoggerUtil.log("Update fixture: " + savedFixture.getFixtureName(), UserAuditActionType.Fixture_Update.getName());	
        
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
        model.addAttribute("groups", groupManager.loadAllGroups());
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
            model.addAttribute("groups", groupManager.loadAllGroupsExceptDeafult());

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
		
		TreeNode<FacilityType> facilityTreeHierarchy = null;
		facilityTreeHierarchy = groupManager.loadProfileHierarchyForUser(emsAuthContext.getUserId(),true);
    	model.addAttribute("profileHierarchy", facilityTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jTreeOptions", jsTreeOptions);		
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
	
	@RequestMapping("/assignprofiletofixturesemployeerole.ems")
	 public String assignprofiletofixturesemployeerole(Model model, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie){
       FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
       Long id = cookieHandler.getFacilityId();
       model.addAttribute("pid", id);
       return "devices/fixtures/assignprofileEmployee/dialog";
	}
	
	@RequestMapping("/fixturePowerMapForm.ems")
	 public String showFixturePowerMapForm(Model model, @RequestParam("fixtureId") long fixtureId,@RequestParam("fixtCnt") long fixtCnt){
		FixtureLampCalibration fixtureVoltPower = fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixtureId);
		model.addAttribute("fixtureId", fixtureId);
		if(fixtureVoltPower!=null)
		{
			model.addAttribute("fixtureLampCalibrationId", fixtureVoltPower.getId());
		}
		if(fixtCnt>1)
		{
			model.addAttribute("type", "mulitiple");
		}else
		{
			model.addAttribute("type", "single");
		}
		Fixture fixture = fixtureManager.getFixtureById(fixtureId);
		String source = "Fixture " + fixture.getFixtureName();
		model.addAttribute("fixtureSourceName",source);
		model.addAttribute("ballastType",fixture.getBallast().getBallastName());
        String displayLampStr = fixture.getBallast().getLampNum() + "," + fixture.getBallast().getWattage() +"W" + "," + fixture.getBallast().getLampType();
        model.addAttribute("lamps",displayLampStr);
        model.addAttribute("lampManufacturer",fixture.getBallast().getBallastManufacturer());
        model.addAttribute("lineVoltage", fixture.getVoltage());
		return "devices/fixtures/showFixturepowermapsDialog";
	}
}
