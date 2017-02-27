/**
 * 
 */
package com.ems.mvc.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.model.Fixture;
import com.ems.model.Floor;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.Groups;
import com.ems.model.MotionBitsScheduler;
import com.ems.model.MotionGroup;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadSceneLevel;
import com.ems.model.Scene;
import com.ems.model.SceneLevel;
import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.SceneTemplate;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.SystemConfiguration;
import com.ems.model.Tenant;
import com.ems.mvc.util.ControllerUtils;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.GemsPlugloadGroupManager;
import com.ems.service.GroupManager;
import com.ems.service.MotionBitsConfigManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.service.SceneLightLevelsManager;
import com.ems.service.SceneTemplatesManager;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.FacilityType;
import com.ems.types.GGroupType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;

/**
 * @author yogesh
 * 
 */
@Controller
@RequestMapping("/devices/widget")
public class WidgetController {
    static final Logger logger = Logger.getLogger("FixtureLogger");

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource
    FacilityTreeManager facilityTreeManager;
    @Resource
    FloorManager floorManager;
    @Resource
    SwitchManager switchManager;
    @Resource
    GemsGroupManager gemsGroupManager;
    @Resource
    MotionGroupManager motionGroupManager;
    @Resource
    MotionBitsConfigManager motionBitsConfigManager;
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
    @Resource
	private UserManager userManager;
    @Resource
    FixtureManager fixtureManager;
    @Resource
    PlugloadManager plugloadManager;
    @Resource
    SceneTemplatesManager sceneTemplatesManager;
    @Resource
    SceneLightLevelsManager sceneLightLevelsManager;
    @Resource
	PlugloadGroupManager plugloadGroupManager;
    
    @Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
    
    @Resource(name="gemsPlugloadGroupManager")
    GemsPlugloadGroupManager gemsPlugloadGroupManager;
    private int MAX_ALLOWED_SCENES_PER_SWITCH = 8;

    @RequestMapping(value = "/switch/prompt.ems")
    public String showSwitchPrompt(Model model, 
    		@RequestParam(value = "bc", required = false) Boolean bulkconfigure) {
    	if (bulkconfigure == null)
    		bulkconfigure = false;
        model.addAttribute("bc", bulkconfigure);
        return "devices/switches/prompt";
    }

    @RequestMapping("/switch/show.ems")
    public String showSwitchWidgetDialog(Model model, @RequestParam("switchId") Long switchId) {
    	
    	
        TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
                .loadCompanyHierarchy();
        model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
        JsTreeOptions jsTreeOptions = new JsTreeOptions();
        jsTreeOptions.setCheckBoxes(false);
        model.addAttribute("jsTreeOptions", jsTreeOptions);
        model.addAttribute("viewTreeOnly", true);
        Switch sw = switchManager.getSwitchById(switchId);
        SwitchGroup switchGroup = switchManager.getSwitchGroupByGemsGroupId(sw.getGemsGroup().getId());
/*        if(switchGroup == null) {
        	switchManager.createSwitchGroup(new SwitchGroup(null, new Integer("14" + motionGroupManager.getNextGroupNo()) , sw.getGemsGroup()));
        }*/
        model.addAttribute("switch", sw);
        model.addAttribute("switchGroup", switchGroup);
        model.addAttribute("switchId", switchId);
        model.addAttribute("fixtureVersion", switchGroup.getFixtureVersion());
        GemsGroup oGGroup = sw.getGemsGroup();
        if (oGGroup != null) {
            gemsGroupManager.updateGroupFixtureSyncPending(oGGroup.getId(), true);          
             ControllerUtils.startTimerForFixtureGroupSyncFlag() ;
        }
        SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
            model.addAttribute("enablePlugloadProfileFeature", enablePlugloadProfileFeature.getValue());
        }else{
        	model.addAttribute("enablePlugloadProfileFeature", false);
        }
        return "switch/widget/details";
    }
    
    
    @RequestMapping("/create/switch.ems")
	public String createNewSwitch(
			@RequestParam("switchName") String switchName,
			@RequestParam("fixtureVersion") String fixtureVersion,
			@RequestParam(value = "bc", required = false) Boolean bulkconfigure,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {

    	Long switchId = null;
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
    	try {
    		GemsGroup gemsGroup = new GemsGroup();
    		gemsGroup.setGroupName(switchName);
			Floor floor = floorManager.getFloorById(cookieHandler.getFacilityId());
			gemsGroup.setFloor(floor);
			gemsGroup = gemsGroupManager.createNewGroup(gemsGroup);
			switchManager.createSwitchGroup(new SwitchGroup(null, new Integer("14" + motionGroupManager.getNextGroupNo()) , gemsGroup, fixtureVersion));
			switchId = switchManager.createNewSwitch(switchName, floor, gemsGroup).getId();
			if(bulkconfigure == null) {
				bulkconfigure = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	userAuditLoggerUtil.log("Create Switch: " + switchName , UserAuditActionType.Switch_Update.getName());
    	String redirectUrl = "redirect:/devices/widget/switch/show.ems?switchId=" + switchId;
    	if (bulkconfigure == true) {
    		redirectUrl = "redirect:/devices/widget/all/show.ems?switchId=" + switchId + "&bc=" + bulkconfigure;
    	}
    	return redirectUrl;
    }
    
    
    @RequestMapping(value = "switch/editAndApply.ems", method = RequestMethod.POST)
	@ResponseBody
	public String editAndApplySwitchChanges(@ModelAttribute("switch") Switch sw, @RequestParam("switchId") Long switchId, Locale local) {
    	
    	Switch sw1 = switchManager.getSwitchById(switchId);
    	if(sw.getName() != null && !sw.getName().isEmpty()) {
    		sw1.setName(sw.getName());
    		sw1.setModeType(sw.getModeType());
    		sw1.setInitialSceneActiveTime(sw.getInitialSceneActiveTime());
    		//Force mode should be reset because user is overriding the configuration 
    		sw1.setForceAutoMode((short)0);
    	}
    	switchManager.saveSwitch(sw1);
    	
    	userAuditLoggerUtil.log("Edit Switch id : " + sw.getId() + " Switch Name : "+sw.getName(), UserAuditActionType.Switch_Update.getName());
    	
    	if(sw.getName() != null && !sw.getName().isEmpty()) {
	    	GemsGroup gemsGroup = sw1.getGemsGroup();
			gemsGroup.setGroupName(sw.getName());
			gemsGroupManager.editGroup(gemsGroup);
    	}
    	SwitchGroup switchGroup = switchManager.getSwitchGroupByGemsGroupId(sw1.getGemsGroup().getId());

    	int status = 0;
    	
    	// Send the switch commands only for 2.x type of switchGroup
    	
    	// TODO : PLUGLOAD: Need code for manageGroupMembership for plugloads
    	if(switchGroup.getFixtureVersion().equalsIgnoreCase("2.x"))
    		switchManager.manageGroupMembership(sw1.getId());
    	switchManager.managePlugloadGroupMembership(sw1.getId());
    	
		return "{\"success\":" + status + ", \"message\" : \""
				+ "S" + "\"}";
    }
    
    @RequestMapping(value = "/scenetemplate/prompt.ems")
    public String showSceneTemplatePrompt(Model model,@RequestParam("scenetemplateId") Long scenetemplateId) {
    	String mode="new";
    	if(scenetemplateId!=null && scenetemplateId>0)
    	{
    		SceneTemplate sceneTemplates = sceneTemplatesManager.getSceneTemplateById(scenetemplateId);
    		model.addAttribute("scenetemplatename", sceneTemplates.getName());
    		mode="edit";
    	}
    	model.addAttribute("modeType", mode);
    	model.addAttribute("scenetemplateId", scenetemplateId);
        return "devices/scenetemplates/prompt";
    }
      
    @RequestMapping(value = "/group/prompt.ems")
    public String showGroupPrompt() {
        return "devices/groups/prompt";
    }
    
    @RequestMapping("/group/show.ems")
    public String showGroupWidgetDialog(Model model, @RequestParam("groupId") Long groupId) {
    	
    	
        TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
                .loadCompanyHierarchy();
        model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
        JsTreeOptions jsTreeOptions = new JsTreeOptions();
        jsTreeOptions.setCheckBoxes(false);
        model.addAttribute("jsTreeOptions", jsTreeOptions);
        model.addAttribute("viewTreeOnly", true);
        MotionGroup motionGroup = motionGroupManager.getMotionGroupByGemsGroupId(groupId);
        model.addAttribute("group", motionGroup);
        model.addAttribute("groupId", groupId);
        model.addAttribute("fixtureVersion", motionGroup.getFixtureVersion());
        if (motionGroup != null) {
            gemsGroupManager.updateGroupFixtureSyncPending(motionGroup.getGemsGroup().getId(), true);          
            ControllerUtils.startTimerForFixtureGroupSyncFlag() ;
        }
        
        String enablePlugloadProfileFeatureVal = "false";
        SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
        	enablePlugloadProfileFeatureVal = enablePlugloadProfileFeature.getValue();
        }
        
        model.addAttribute("enablePlugloadProfileFeature", enablePlugloadProfileFeatureVal);
        
        return "group/widget/details";
    }
    
    @RequestMapping("/create/group.ems")
    public String createNewGroup(@RequestParam("groupName") String groupName, @RequestParam("fixtureVersion") String fixtureVersion, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {

    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
    	GemsGroup gemsGroup = new GemsGroup();
    	try {
    		groupName = URLDecoder.decode(groupName, "UTF-8");    		
    		gemsGroup.setGroupName(groupName);
			Floor floor = floorManager.getFloorById(cookieHandler.getFacilityId());
			gemsGroup.setFloor(floor);
			gemsGroup = gemsGroupManager.createNewGroup(gemsGroup);
			motionGroupManager.saveOrUpdateMotionGroup(new MotionGroup(null, new Integer("1" + GGroupType.MotionGroup.getId() + motionGroupManager.getNextGroupNo()), gemsGroup, fixtureVersion )).getId();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	userAuditLoggerUtil.log("Create Group: " + groupName + " with Fixture Versin" + fixtureVersion, UserAuditActionType.Motion_Group_Update.getName());
    	return "redirect:/devices/widget/group/show.ems?groupId=" + gemsGroup.getId();
    }
    
    
    @RequestMapping(value = "group/editAndApply.ems", method = RequestMethod.POST)
    @ResponseBody
	public String editAndApplyGroupChanges(@ModelAttribute("gemsGroup") MotionGroup motionGroup, @RequestParam("groupId") Long groupId, Locale local) {
    	int iStatus = 0;
    	GemsGroup gemsGroup1 = gemsGroupManager.loadGemsGroup(groupId);
    	if(motionGroup.getGemsGroup()!=null && !motionGroup.getGemsGroup().getGroupName().isEmpty())
    	gemsGroup1.setGroupName(motionGroup.getGemsGroup().getGroupName());
    	//gemsGroup1.setDescription(gemsGroup.getDescription());
    	gemsGroupManager.editGroup(gemsGroup1);
       
    	MotionGroup oMotionGroup = motionGroupManager.getMotionGroupByGemsGroupId(groupId);
        logger.info(gemsGroup1.getId() + ": Apply motion group configuration...");
        int groupNo = Integer.parseInt(oMotionGroup.getGroupNo().toString(), 16);
        List<GemsGroupFixture> oGGFxList = gemsGroupManager.getAllGemsGroupFixtureByGroup(gemsGroup1.getId());
        if (oGGFxList == null) {
            iStatus = 1;
        }else {
            logger.info(oGGFxList.size() + " fixture(s) to join motion group " + groupNo);
    		gemsGroupManager.asssignFixturesToGroup(gemsGroup1.getId(), oGGFxList,
    				groupNo, GGroupType.MotionGroup.getId());
    		if (oMotionGroup.getFixtureVersion().startsWith("2.")) {
    			motionGroupManager.manageGroupMembership(gemsGroup1.getId(), groupNo);
    		}
        }
        
        List<GemsGroupPlugload> oGGPlList = gemsPlugloadGroupManager.getGemsGroupPlugloadByGroup(gemsGroup1.getId());
        if (oGGPlList == null) {
            iStatus = 1;
        }else {
            logger.info(oGGPlList.size() + " plugload(s) to join motion group " + groupNo);
            gemsPlugloadGroupManager.asssignPlugloadsToGroup(gemsGroup1.getId(), oGGPlList,
    				groupNo, GGroupType.MotionGroup.getId());
    		motionGroupManager.managePlugloadGroupMembership(gemsGroup1.getId(), groupNo);
    	}
        
        userAuditLoggerUtil.log("Edit Group id: " + groupId + "Group Name : " +gemsGroup1.getGroupName(), UserAuditActionType.Motion_Group_Update.getName());
		return "{\"success\":" + iStatus + ", \"message\" : \""
				+ "S" + "\"}";
    }
      
    @RequestMapping("/motionbitsgroup/show.ems")
    public String showMotionBitsGroupWidgetDialog(Model model, @RequestParam("groupId") Long groupId,@RequestParam("mode") String mode) {   	
    	
        TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
        model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
        JsTreeOptions jsTreeOptions = new JsTreeOptions();
        jsTreeOptions.setCheckBoxes(false);
        model.addAttribute("jsTreeOptions", jsTreeOptions);
        model.addAttribute("viewTreeOnly", true);
        
        MotionBitsScheduler motionBitsGroup = motionBitsConfigManager.loadMotionBitsScheduleById(groupId);
        model.addAttribute("motionBitsSchedule", motionBitsGroup);
             
      
        model.addAttribute("groupId", groupId);	
                
        
        return "motionbits/widget/details";
    }
    
    
    @RequestMapping("/create/motionbitsgroup.ems")
    public String createNewMotionBitsGroup(@RequestParam("groupName") String groupName, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {    	
    	Long groupId = null;
    	Long id = null;
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
    	GemsGroup gemsGroup = new GemsGroup();
    	try {    		
    		groupName = URLDecoder.decode(groupName, "UTF-8");    		
    		gemsGroup.setGroupName(groupName);
			Floor floor = floorManager.getFloorById(cookieHandler.getFacilityId());
			gemsGroup.setFloor(floor);
			gemsGroup = gemsGroupManager.createNewGroup(gemsGroup);
			MotionBitsScheduler motionBitsScheduler = new MotionBitsScheduler();
			motionBitsScheduler.setName(groupName);			
			motionBitsScheduler.setGroupNo(new Integer("1" + GGroupType.MotionBitsGroup.getId() + motionGroupManager.getNextGroupNo()));
			motionBitsScheduler.setMotionBitGroup(gemsGroup);		
			
			//Id of motion_bits_scheduler table returned.
			groupId = motionBitsConfigManager.saveMotionBitsSchedule(motionBitsScheduler).getId();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	userAuditLoggerUtil.log("Create Motion Bits Group: " + groupName , UserAuditActionType.Motion_Bits_update.getName());
    	return "redirect:/devices/widget/motionbitsgroup/show.ems?groupId=" + groupId+"&mode=create";
    }
    
    @RequestMapping(value = "motionbitsgroup/editAndApply.ems", method = RequestMethod.POST)
    @ResponseBody
	public String editAndApplyMotionBitsGroupChanges(@ModelAttribute("scheduler") MotionBitsScheduler scheduler, Locale local) {
    	
		MotionBitsScheduler scheduler1 = motionBitsConfigManager.loadMotionBitsScheduleById(scheduler.getId());
		scheduler1.setBitLevel(scheduler.getBitLevel());
		scheduler1.setName(scheduler.getName());
		scheduler1.setCaptureStart(scheduler.getCaptureStart());
		scheduler1.setCaptureEnd(scheduler.getCaptureEnd());
		scheduler1.setTransmitFreq(scheduler.getTransmitFreq());
		scheduler1.setDaysOfWeek(scheduler.getDaysOfWeek());
		
		motionBitsConfigManager.saveMotionBitsSchedule(scheduler1);
		int iStatus = 0;
/*		List<GemsGroupFixture> oGGFxList = gemsGroupManager.getAllGemsGroupFixtureByGroup(scheduler1.getMotionBitGroup().getId());
		if (oGGFxList == null) {
		    iStatus = 1;
		}else {
			int groupNo = Integer.parseInt(scheduler1.getGroupNo().toString(), 16);
		    gemsGroupManager.asssignFixturesToGroup(scheduler1.getMotionBitGroup().getId(), oGGFxList, groupNo,
		             GGroupType.MotionBitsGroup.getId());
		}*/

		// First delete the Quartz job if one already exists
		motionBitsConfigManager.deleteMotionBitsSchedulerJob(scheduler.getId());
		
		// Then add a new one
		motionBitsConfigManager.addMotionBitsSchedulerJob(scheduler1);
		userAuditLoggerUtil.log("Edit Motion Bits Group: " + scheduler1.getName() , UserAuditActionType.Motion_Bits_update.getName());
    	return "{\"success\":" + iStatus + ", \"message\" : \""
		+ "S" + "\"}";
    }

    @RequestMapping("/all/show.ems")
    public String showWidgetDialog(Model model, @RequestParam("switchId") Long switchId) {
        TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager
                .loadCompanyHierarchy();
        model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
        JsTreeOptions jsTreeOptions = new JsTreeOptions();
        jsTreeOptions.setCheckBoxes(false);
        model.addAttribute("jsTreeOptions", jsTreeOptions);
        model.addAttribute("viewTreeOnly", true);
        
        // Profiles
        //Role Based Display of profiles in the Fixture Form
    	List<Groups> profileList=null;
    	//Set the Tenant profile if present
        Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
        Long tenantId=0L;
        Long templateId = 0L;
        if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin || emsAuthContext.getCurrentUserRoleType() == RoleType.FacilitiesAdmin)
		{
			profileList = groupManager.loadAllGroups();
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
		TreeNode<FacilityType> profileTreeHierarchy = null;
		profileTreeHierarchy = groupManager.loadProfileHierarchyForUser(
				emsAuthContext.getUserId(), true);
		model.addAttribute("profileHierarchy", profileTreeHierarchy);
		
		TreeNode<FacilityType> plugloadProfileTreeHierarchy = null;
		boolean visibilityCheck =false;
		plugloadProfileTreeHierarchy = plugloadGroupManager.loadPlugloadProfileHierarchy(visibilityCheck);
		model.addAttribute("plugloadProfileTreeHierarchy", plugloadProfileTreeHierarchy);
        
        model.addAttribute("scenetemplate", switchManager.getAllSceneTemplates());
        // Switch group
        Switch sw = switchManager.getSwitchById(switchId);
        SwitchGroup switchGroup = switchManager.getSwitchGroupByGemsGroupId(sw.getGemsGroup().getId());
        model.addAttribute("switch", sw);
        model.addAttribute("switchGroup", switchGroup);
        model.addAttribute("switchId", switchId);
        model.addAttribute("fixtureVersion", switchGroup.getFixtureVersion());
        GemsGroup oGGroup = sw.getGemsGroup();
        if (oGGroup != null) {
            gemsGroupManager.updateGroupFixtureSyncPending(oGGroup.getId(), true);          
             ControllerUtils.startTimerForFixtureGroupSyncFlag() ;
        }
        SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
            model.addAttribute("enablePlugloadProfileFeature", enablePlugloadProfileFeature.getValue());
        }else{
        	model.addAttribute("enablePlugloadProfileFeature", false);
        }
        return "all/widget/details";
    }

    @RequestMapping(value = "all/editAndApply.ems", method = RequestMethod.POST)
	@ResponseBody
	public String editAndApplyBulkChanges(
			@RequestParam("bgsgid") Long switchId,
			@RequestParam("bgsgname") String switchName,
			@RequestParam("bgswitchtype") String switchMode,
			@RequestParam("bginitialSceneActiveTime") Integer switchInitialActiveTime,
			@RequestParam("bgmgroup") String isMotionGroupReq,
			@RequestParam("bgmgname") String motionGroupName,
			@RequestParam("bgapplyscenetmpl") String isSceneTemplateReq,
			@RequestParam("bgscenetemplate") Long sceneTemplateId,
			@RequestParam("bgapplyprofile") String isProfileApplyReq,
			@RequestParam("bgprofiles") Long selectedProfileId,
			@RequestParam("bgapplyplugloadprofile") String isPlugloadProfileApplyReq,
			@RequestParam("bgplugloadprofiles") Long selectedPlugloadProfileId,
			@RequestParam("isEdit") Boolean isEdit, 
			@RequestParam("oldMgrpName") String oldMgrpName, Locale local) {
    	
    	Switch sw1 = switchManager.getSwitchById(switchId);
    	if(switchName != null && !switchName.isEmpty()) {
    		sw1.setName(switchName);
    		sw1.setModeType(Short.valueOf(switchMode));
    		sw1.setInitialSceneActiveTime(switchInitialActiveTime);
    	}
    	switchManager.saveSwitch(sw1);
    	
		userAuditLoggerUtil.log("Edit Switch id : " + switchId
				+ " Switch Name : " + switchName + ", Motion: "
				+ isMotionGroupReq + ", Profile: " + isProfileApplyReq
				+ ", pno: " + selectedProfileId,
				UserAuditActionType.Switch_Update.getName());
		GemsGroup gemsGroup = null;
    	if(switchName != null && !switchName.isEmpty()) {
	    	gemsGroup = sw1.getGemsGroup();
			gemsGroup.setGroupName(sw1.getName());
			gemsGroupManager.editGroup(gemsGroup);
    	}
    	int status = 0;
    	// Create a fixture list
        String fixtureIdsList="";
		List<GemsGroupFixture> currentGroupFixtures = gemsGroupManager
				.getGemsGroupFixtureByGroup(sw1.getGemsGroup().getId());
		List<Fixture> fixtureList = new ArrayList<Fixture>();
		if(currentGroupFixtures != null) {			
			Iterator<GemsGroupFixture> oggfxItr = currentGroupFixtures.iterator();
			while(oggfxItr.hasNext()) {
				GemsGroupFixture ggf = oggfxItr.next();
				fixtureList.add(ggf.getFixture());
				fixtureIdsList+=ggf.getFixture().getId();
				if (oggfxItr.hasNext()) {   
					fixtureIdsList += ",";    
				} 
			}
		}
    	
		//Create a plugload list
        String plugloadIdsList="";
		List<GemsGroupPlugload> currentGroupPlugloads = gemsPlugloadGroupManager
				.getGemsGroupPlugloadByGroup(sw1.getGemsGroup().getId());
		List<Plugload> plugloadList = new ArrayList<Plugload>();
		if(currentGroupPlugloads != null) {			
			Iterator<GemsGroupPlugload> oggplItr = currentGroupPlugloads.iterator();
			while(oggplItr.hasNext()) {
				GemsGroupPlugload ggf = oggplItr.next();
				plugloadList.add(ggf.getPlugload());
				plugloadIdsList+=ggf.getPlugload().getId();
				if (oggplItr.hasNext()) {   
					plugloadIdsList += ",";    
				} 
			}
		}		
		
		// Apply scene if required		
		if (isSceneTemplateReq.indexOf(",") != -1 && isEdit==false) {
			System.out.println("Apply SceneTemplate " + sceneTemplateId);
			Integer sceneCount = switchManager.nextSceneOrder(switchId);
			int availableSceneCount = MAX_ALLOWED_SCENES_PER_SWITCH - sceneCount;
			
			List<SceneLightLevelTemplate> ollList = switchManager.getAllLightLevelsForSceneTemplate(sceneTemplateId);
			for(int count = 0; count < ollList.size(); count++) {
				if (count >= availableSceneCount) {
					logger.info(switchId + ": Max scene count limit reach");
					break;
				}
				SceneLightLevelTemplate sllt = ollList.get(count);
				Scene oScene = new Scene();
				oScene.setSwitchId(switchId);
				oScene.setName(sllt.getName());
				oScene.setSceneOrder(sceneCount++);
				switchManager.saveScene(oScene);
			}
			
			
			for(int count = 0; count < ollList.size(); count++) {
				if (count >= availableSceneCount) {
					logger.info(switchId + ": Max scene count limit reach");
					break;
				}
				SceneLightLevelTemplate sllt = ollList.get(count);
				Scene oScene = switchManager.loadScenebyNameAndSwitchId(sllt.getName(), switchId);
				for (int fxcount = 0; fxcount < fixtureList.size(); fxcount++) {
					SceneLevel sceneLevel = new SceneLevel();
					sceneLevel.setSceneId(oScene.getId());
					sceneLevel.setSwitchId(switchId);
					sceneLevel.setFixtureId(fixtureList.get(fxcount).getId());
					sceneLevel.setLightLevel(sllt.getLightlevel());
					switchManager.updateSceneLevel(sceneLevel);
				}
				//Plugload
				for (int plcount = 0; plcount < plugloadList.size(); plcount++) {
					PlugloadSceneLevel sceneLevel = new PlugloadSceneLevel();
					sceneLevel.setSceneId(oScene.getId());
					sceneLevel.setSwitchId(switchId);
					sceneLevel.setPlugloadId(plugloadList.get(plcount).getId());
					sceneLevel.setLightLevel(100);
					switchManager.updatePlugloadSceneLevel(sceneLevel);
				}
			}
			
		}

		// Apply profile if required
		if (isProfileApplyReq.indexOf(",") != -1 && !StringUtils.isEmpty(fixtureIdsList)) {
			System.out.println("Apply profile " + selectedProfileId);
			Groups oGroup = groupManager.getGroupById(selectedProfileId);
			fixtureManager.bulkProfileAssignToFixture(fixtureIdsList, selectedProfileId, oGroup.getName());
		}
		
		// Apply profile if required
		if (isPlugloadProfileApplyReq.indexOf(",") != -1 && !StringUtils.isEmpty(plugloadIdsList)) {
			System.out.println("Apply Plugload profile " + selectedPlugloadProfileId);
			PlugloadGroups oGroup = plugloadGroupManager.getGroupById(selectedPlugloadProfileId);
			plugloadManager.bulkProfileAssignToPlugload(plugloadIdsList, selectedPlugloadProfileId, oGroup.getName());
		}
		
		// Apply switch group
    	SwitchGroup switchGroup = switchManager.getSwitchGroupByGemsGroupId(sw1.getGemsGroup().getId());
    	// Send the switch commands only for 2.x type of switchGroup
    	if(switchGroup.getFixtureVersion().equalsIgnoreCase("2.x"))
    		switchManager.manageGroupMembership(sw1.getId());
    	switchManager.managePlugloadGroupMembership(sw1.getId());
    	// Check if motion group apply is requested.
		if (isMotionGroupReq.indexOf(",") != -1) {
			System.out.println("Create motion group " + motionGroupName);
			try {
			    Floor floor = floorManager.getFloorById(sw1.getFloorId());
			    //Creates new motion group
			    if(isEdit==false)
			    {
    				GemsGroup oMG = new GemsGroup();
    				oMG.setGroupName(motionGroupName);
    				gemsGroup.setFloor(floor);
    				oMG.setFloor(floor);
    				oMG = gemsGroupManager.createNewGroup(oMG);
    				motionGroupManager.saveOrUpdateMotionGroup(
    						new MotionGroup(null, new Integer("1"
    								+ GGroupType.MotionGroup.getId()
    								+ motionGroupManager.getNextGroupNo()), oMG,
    								"2.0")).getId();
    				createMotionGroup(oMG.getId(), fixtureList,plugloadList);
    
    				MotionGroup oMotionGroup = motionGroupManager
    						.getMotionGroupByGemsGroupId(oMG.getId());
    				logger.info(oMG.getId()
    						+ ": Apply motion group configuration...");
    				int groupNo = Integer.parseInt(oMotionGroup.getGroupNo()
    						.toString(), 16);
    				List<GemsGroupFixture> oGGFxList = gemsGroupManager
    						.getAllGemsGroupFixtureByGroup(oMG.getId());
    				if (oGGFxList != null) {
    					logger.info(oGGFxList.size()
    							+ " fixture(s) to join motion group " + groupNo);
    					gemsGroupManager.asssignFixturesToGroup(oMG.getId(),
    							oGGFxList, groupNo, GGroupType.MotionGroup.getId());
    					if (oMotionGroup.getFixtureVersion().startsWith("2.")) {
    						motionGroupManager.manageGroupMembership(oMG.getId(),
    								groupNo);
    					}
    				}
    				
    				//Plug load
    				List<GemsGroupPlugload> oGGPLList = gemsPlugloadGroupManager
    						.getGemsGroupPlugloadByGroup(oMG.getId());
    				if (oGGPLList != null) {
    					logger.info(oGGPLList.size()
    							+ " plugloads(s) to join motion group " + groupNo);
    					gemsPlugloadGroupManager.asssignPlugloadsToGroup(oMG.getId(),
    							oGGPLList, groupNo, GGroupType.MotionGroup.getId());
						motionGroupManager.managePlugloadGroupMembership(oMG.getId(),
								groupNo);
    				}
			    }else
			    {
			        //Edit only name of the motion group
			        GemsGroup oMG =null;
			        if(oMG==null)
			        {
			            oMG = gemsGroupManager.loadGroupsByGroupNameAndFloor(oldMgrpName, floor.getId());
			        }
			        if(oMG!=null)
			        {
    			        oMG.setGroupName(motionGroupName);
    			        GemsGroup savedGrp = gemsGroupManager.editGroup(oMG);
    			        logger.info("motion group name " + oMG.getGroupName()+ " updated with "+savedGrp.getGroupName());
			        }
			    }
			} catch (Exception e) {
				logger.error("Error creating motion group: " + motionGroupName + " " + e.getMessage());
			}
		}
		return "{\"success\":" + status + ", \"message\" : \""
				+ "S" + "\"}";
    }
    
    private void createMotionGroup(Long gemsGroupId, List<Fixture> fixtures,List<Plugload> plugloads) {
    	List<GemsGroupFixture> currentGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(gemsGroupId);
    	Set<Long> currentGroupFixturesId = new HashSet<Long>();
    	if(currentGroupFixtures != null && currentGroupFixtures.size() > 0) {
    		for(GemsGroupFixture ggf: currentGroupFixtures) {
    			currentGroupFixturesId.add(ggf.getFixture().getId());
    		}
    	}

    	GemsGroup oGemsGroup = gemsGroupManager.loadGemsGroup(gemsGroupId);
    	if (oGemsGroup != null) {
            for (Fixture f : fixtures) {
                if (!currentGroupFixturesId.contains(f.getId().longValue())) {
            		List<GemsGroupFixture> listGroups = gemsGroupManager.getAllGroupsOfFixture(f);
            		if(listGroups != null && listGroups.size() >= 10)
            		{
            			continue;
            		}
                	gemsGroupManager.addGroupFixture(oGemsGroup, f);
                } else {
                    currentGroupFixturesId.remove(f.getId());
                }
            }
    	}
    	
    	//PLUGLOAD
    	List<GemsGroupPlugload> currentGroupPlugloads = gemsPlugloadGroupManager.getGemsGroupPlugloadByGroup(gemsGroupId);
    	Set<Long> currentGroupPlugloadsId = new HashSet<Long>();
    	if(currentGroupPlugloads != null && currentGroupPlugloads.size() > 0) {
    		for(GemsGroupPlugload ggf: currentGroupPlugloads) {
    			currentGroupPlugloadsId.add(ggf.getPlugload().getId());
    		}
    	}
    	GemsGroup oGemsGroupPl = gemsGroupManager.loadGemsGroup(gemsGroupId);
    	if (oGemsGroupPl != null) {
            for (Plugload p : plugloads) {
                if (!currentGroupFixturesId.contains(p.getId().longValue())) {
            		List<GemsGroupPlugload> listGroups = gemsPlugloadGroupManager.getAllGroupsOfPlugload(p);
            		if(listGroups != null && listGroups.size() >= 10)
            		{
            			continue;
            		}
            		gemsPlugloadGroupManager.addGroupPlugload(oGemsGroupPl, p);
                } else {
                	currentGroupPlugloadsId.remove(p.getId());
                }
            }
    	}
        return;
    }
    @RequestMapping(value = "/scenelightlevel/prompt.ems")
    public String showSceneLightLevelPrompt(Model model,@RequestParam("sceneTemplateID") Long sceneTemplateID,@RequestParam("mode") String mode) {
    	SceneLightLevelTemplate tempSceneLightLevel = sceneLightLevelsManager.loadSceneLightLevelById(sceneTemplateID);
    	int maxSceneOrder =0;
    	Long dsceneTemplateID = sceneTemplateID;
    	SceneLightLevelTemplate sceneLightLevelTemplate = new SceneLightLevelTemplate();
    	sceneLightLevelTemplate.setSceneTemplateId(sceneTemplateID);
    	if(mode.equalsIgnoreCase("edit"))
    	{
    		sceneLightLevelTemplate.setId(tempSceneLightLevel.getId());
    		sceneLightLevelTemplate.setName(tempSceneLightLevel.getName());
    		sceneLightLevelTemplate.setLightlevel(tempSceneLightLevel.getLightlevel());
    		sceneLightLevelTemplate.setSceneOrder(tempSceneLightLevel.getSceneOrder());
    		dsceneTemplateID= tempSceneLightLevel.getSceneTemplateId();
    		sceneLightLevelTemplate.setSceneTemplateId(dsceneTemplateID);
    	}else
    	{
    		maxSceneOrder = sceneLightLevelsManager.getMaxSceneOrderForSceneTemplateID(sceneTemplateID);
    		sceneLightLevelTemplate.setSceneOrder(maxSceneOrder+1);
    	}
    	model.addAttribute("sceneLightLevelTemplate",sceneLightLevelTemplate);
    	model.addAttribute("sceneTemplateID", dsceneTemplateID);
    	model.addAttribute("mode", mode);
        return "devices/scenelightlevel/prompt";
    }
}
