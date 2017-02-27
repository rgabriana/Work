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
import com.ems.model.SweepTimer;
import com.ems.model.SweepTimerDetails;
import com.ems.service.FacilityTreeManager;
import com.ems.service.SweepTimerManager;
import com.ems.types.DayType;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;

@Controller
@RequestMapping("/sweeptimer")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class SweepTimerController {
	
	@Resource
	private SweepTimerManager sweepTimerManager;
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@RequestMapping("/assignSweepTimerToFacility.ems")
	public String getSweepTimerFacilityTree(Model model) {
		TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchy();
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
		List<SweepTimer> sweepTimerList = sweepTimerManager.loadAllSweepTimer();
		model.addAttribute("sweepTimerList", sweepTimerList);
		return "sweeptimer/treeAssignment";
	}
	
	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/save_sweep_timer_assignment.ems", method = RequestMethod.POST)
	public String saveSweepTimerAssignment(Model model,
		@RequestParam("selectedSweepTimers") String selectedSweepTimers) {
		Integer status =0;
		try
		{
			String[] assignedFacilities = selectedSweepTimers.split(",");
			facilityTreeManager.setSweepTimerToFacilities(assignedFacilities);
		}catch(DataIntegrityViolationException de)
		{
			status =1;
		}
		return "redirect:/sweeptimer/list.ems?assignStatus="+status;
	}

	@RequestMapping("/list.ems")
	public String getSweepTimerList(Model model) {
		
		List<SweepTimer> sweepTimerList = sweepTimerManager.loadAllSweepTimer();
		model.addAttribute("sweepTimerList", sweepTimerList);
		return "sweeptimer/list";
	}

	@RequestMapping("/delete.ems")
	public String deleteSweepTimer(Model model, @RequestParam("sweepTimerId") long sweepTimerId) {
		Integer status =0;
		try
		{
			sweepTimerManager.deleteSweepTimerbyId(sweepTimerId);
			status = 0;
		}catch(DataIntegrityViolationException de)
		{
			status =1;
		}
		return "redirect:/sweeptimer/list.ems?deleteStatus="+status;
	}
	
	@RequestMapping("/edit.ems")
	public String editSweepTimer(Model model, @RequestParam("sweepTimerId") long sweepTimerId) {
		SweepTimer sweepTimer = sweepTimerManager.loadSweepTimerById(sweepTimerId);
		model.addAttribute("sweepTimer", sweepTimer);
	    return "sweeptimer/details";
	}
	
	@RequestMapping("/create.ems")
    public String createSweepTimer(Model model) {
        SweepTimer sweepTimer = new SweepTimer();
      
        List<SweepTimerDetails> oSTDetailsList = new ArrayList<SweepTimerDetails>();
        
        for (long count = 1; count <= 7; count++) {
        	SweepTimerDetails stDetails = new SweepTimerDetails();
        	
        	if(count == 1)
        		stDetails.setDay(DayType.Monday);
        	if(count == 2)
        		stDetails.setDay(DayType.Tuesday);
        	if(count == 3)
        		stDetails.setDay(DayType.Wednesday);
        	if(count == 4)
        		stDetails.setDay(DayType.Thursday);
        	if(count == 5)
        		stDetails.setDay(DayType.Friday);
        	if(count == 6)
        		stDetails.setDay(DayType.Saturday);
        	if(count == 7)
        		stDetails.setDay(DayType.Sunday);
        	oSTDetailsList.add(stDetails);
        }
        sweepTimer.setSweepTimerDetails(oSTDetailsList);
        model.addAttribute("sweepTimer", sweepTimer);
         return "sweeptimer/details";
    }
}
