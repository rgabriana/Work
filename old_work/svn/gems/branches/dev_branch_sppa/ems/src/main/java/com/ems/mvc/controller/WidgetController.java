/**
 * 
 */
package com.ems.mvc.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.model.Floor;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionBitsScheduler;
import com.ems.model.MotionGroup;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FloorManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.MotionBitsConfigManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.SwitchManager;
import com.ems.types.FacilityType;
import com.ems.types.GGroupType;
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
    
    @RequestMapping(value = "/switch/prompt.ems")
    public String showSwitchPrompt() {
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
        if(switchGroup == null) {
        	switchManager.createSwitchGroup(new SwitchGroup(null, new Integer("14" + motionGroupManager.getNextGroupNo()) , sw.getGemsGroup()));
        }
        model.addAttribute("switch", sw);
        model.addAttribute("switchId", switchId);
        GemsGroup oGGroup = sw.getGemsGroup();
        if (oGGroup != null) {
            gemsGroupManager.updateGroupFixtureSyncPending(oGGroup.getId(), true);
        }
        return "switch/widget/details";
    }
    
    
    @RequestMapping("/create/switch.ems")
    public String createNewSwitch(@RequestParam("switchName") String switchName, @CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {

    	Long switchId = null;
    	FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
    	try {
    		GemsGroup gemsGroup = new GemsGroup();
    		gemsGroup.setGroupName(switchName);
			Floor floor = floorManager.getFloorById(cookieHandler.getFacilityId());
			gemsGroup.setFloor(floor);
			gemsGroup = gemsGroupManager.createNewGroup(gemsGroup);
			switchManager.createSwitchGroup(new SwitchGroup(null, new Integer("14" + motionGroupManager.getNextGroupNo()) , gemsGroup));
			switchId = switchManager.createNewSwitch(switchName, floor, gemsGroup).getId();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return "redirect:/devices/widget/switch/show.ems?switchId=" + switchId;
    }
    
    
    @RequestMapping(value = "switch/editAndApply.ems", method = RequestMethod.POST)
	@ResponseBody
	public String editAndApplySwitchChanges(@ModelAttribute("switch") Switch sw, Locale local) {
    	
    	Switch sw1 = switchManager.getSwitchById(sw.getId());
    	sw1.setName(sw.getName());
    	sw1.setModeType(sw.getModeType());
    	switchManager.saveSwitch(sw1);
    	int status = switchManager.manageGroupMembership(sw1.getId());
		return "{\"success\":" + status + ", \"message\" : \""
				+ "S" + "\"}";
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
    	return "redirect:/devices/widget/group/show.ems?groupId=" + gemsGroup.getId();
    }
    
    
    @RequestMapping(value = "group/editAndApply.ems", method = RequestMethod.POST)
    @ResponseBody
	public String editAndApplyGroupChanges(@ModelAttribute("gemsGroup") MotionGroup motionGroup, @RequestParam("groupId") Long groupId, Locale local) {
    	int iStatus = 0;
    	GemsGroup gemsGroup1 = gemsGroupManager.loadGemsGroup(groupId);
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
            gemsGroupManager.asssignFixturesToGroup(gemsGroup1.getId(), oGGFxList, groupNo,
                     GGroupType.MotionGroup.getId());
        }
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
		List<GemsGroupFixture> oGGFxList = gemsGroupManager.getAllGemsGroupFixtureByGroup(scheduler1.getMotionBitGroup().getId());
		if (oGGFxList == null) {
		    iStatus = 1;
		}else {
			int groupNo = Integer.parseInt(scheduler1.getGroupNo().toString(), 16);
		    gemsGroupManager.asssignFixturesToGroup(scheduler1.getMotionBitGroup().getId(), oGGFxList, groupNo,
		             GGroupType.MotionBitsGroup.getId());
		}

		// First delete the Quartz job if one already exists
		motionBitsConfigManager.deleteMotionBitsSchedulerJob(scheduler.getId());
		
		// Then add a new one
		motionBitsConfigManager.addMotionBitsSchedulerJob(scheduler1);
		
    	return "{\"success\":" + iStatus + ", \"message\" : \""
		+ "S" + "\"}";
    }

}
