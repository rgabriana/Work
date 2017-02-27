package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.annotaion.InvalidateFacilityTreeCache;
import com.ems.model.MotionBitsScheduler;
import com.ems.service.FacilityTreeManager;
import com.ems.service.MotionBitsConfigManager;
import com.ems.types.DayType;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;

@Controller
@RequestMapping("/motionbits")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class MotionBitsConfigController {
	
	@Resource
	private MotionBitsConfigManager  motionBitsConfigConfigManager;
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@RequestMapping("/list.ems")
	public String getMotionBitsScheduleList(Model model) {
		
		List<MotionBitsScheduler> motionBitsScheduleList = motionBitsConfigConfigManager.loadAllMotionBitsSchedules();
		model.addAttribute("motionBitsScheduleList", motionBitsScheduleList);
		return "motionbits/list";
	}

	@RequestMapping("/delete.ems")
	public String deleteMotionBitsSchedule(Model model, @RequestParam("mbScheduleId") long mbScheduleId) {
		Integer status =0;
		try
		{
			motionBitsConfigConfigManager.deleteMotionBitsScheduleById(mbScheduleId);
			status = 0;
		}catch(DataIntegrityViolationException de)
		{
			status =1;
		}
		return "redirect:/sweeptimer/list.ems?deleteStatus="+status;
	}
	
	@RequestMapping("/edit.ems")
	public String editMotionBitsSchedule(Model model, @RequestParam("mbScheduleId") long mbScheduleId) {
		MotionBitsScheduler motionBitsSchedule = motionBitsConfigConfigManager.loadMotionBitsScheduleById(mbScheduleId);
		model.addAttribute("motionBitsSchedule", motionBitsSchedule);
		
        model.addAttribute("mode", "edit");
        model.addAttribute("SELECTED_FIXTURES", "");
        return "motionbits/details";
	}
	
	@RequestMapping("/create.ems")
    public String createSweepTimer(Model model) {

         return "motionbits/details";
    }
}
