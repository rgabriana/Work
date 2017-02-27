package com.emscloud.mvc.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.communication.vos.Fixture;
import com.emscloud.service.EmProfileMappingManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FixtureManager;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.types.FacilityType;
import com.emscloud.util.JsTreeOptions;
import com.emscloud.util.tree.TreeNode;
import com.emscloud.vo.FixtureDetails;


@Controller
@RequestMapping("/devices/fixtures")
public class FixtureController {
	
	@Resource
	FixtureManager fixtureManager;
	
	@Resource
	ProfileGroupManager profileGroupManager;
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	
	
	@Resource
	EmProfileMappingManager emProfileMappingManager;
	
	@RequestMapping(value = "/fixture_form.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadFixtureObject(Model model,
			@RequestParam("fixtureId") long fixtureId,@RequestParam("pid") long pid) {
		
		FixtureDetails fixtureDetails = fixtureManager.getFixtureObjectDetails(fixtureId, pid);
		
		Long emInstanceId = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(pid).getEmId();
		
		Fixture fixture = new Fixture();
		
		if(fixtureDetails != null){
			
			fixture = fixtureDetails.getFixture();
			
			Long currentUemProfileId = null;
			
			Long originalUemProfileId = null;
			
			if(fixtureDetails.getCurrentProfileId() <= 16){
				currentUemProfileId = fixtureDetails.getCurrentProfileId();
			}else{
				currentUemProfileId = emProfileMappingManager.getEmTemplateMappingByEmGroupIdAndEMId(fixtureDetails.getCurrentProfileId(), emInstanceId).getUemProfileId();
			}
			
			if(fixtureDetails.getOriginalProfileFromId() <= 16){
				originalUemProfileId = fixtureDetails.getOriginalProfileFromId();
			}else{
				originalUemProfileId = emProfileMappingManager.getEmTemplateMappingByEmGroupIdAndEMId(fixtureDetails.getOriginalProfileFromId(), emInstanceId).getUemProfileId();
			}
			
			
			String originalProfileFrom = profileGroupManager.getDisplayProfileName(originalUemProfileId);
			
			fixture.setGroupId(currentUemProfileId);
					
			model.addAttribute("originalProfileFrom",originalProfileFrom);
	        model.addAttribute("currentProfile",profileGroupManager.getGroupById(currentUemProfileId));
	        model.addAttribute("temperatureunit", fixtureDetails.getTemperatureunit());
	        model.addAttribute("fixtureclasses", fixtureDetails.getFixtureclasses());
	        model.addAttribute("groupList", fixtureDetails.getGroupList());
	        model.addAttribute("characterizationStatus", fixtureDetails.getCharacterizationStatus());
	        model.addAttribute("fixtureStatus", fixtureDetails.getFixtureStatus());
	        model.addAttribute("groups",profileGroupManager.loadAllGroupsIncludingDefault()); // the Default Groups will have Default Groups attached '_Default' at end.
	        model.addAttribute("pid", pid);
			
		}
		
		model.addAttribute("fixture", fixture);
		
		return "devices/fixtures/details";
	}
	
	/**
     * 
     * @param model
     * @param fixtureId
     * @param pid
     * @return
     */
    @RequestMapping("/fixture_details.ems")
    public String loadFixtureDetails(Model model, @RequestParam("fixtureId") long fixtureId,@RequestParam("pid") long pid) {
        model.addAttribute("fixtureId", fixtureId);
        model.addAttribute("pid", pid);
        return "devices/fixtures/tabpaneldetails";
    }
    
    @RequestMapping("/assignprofiletofixtures.ems")
    public String assignProfileToFixtureDialog(Model model,@RequestParam("facilityId") long facilityId){
		
		TreeNode<FacilityType> ProfileTreeHierarchy = null;
		ProfileTreeHierarchy = profileGroupManager.loadProfileHierarchy(true);
    	model.addAttribute("profileHierarchy", ProfileTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jTreeOptions", jsTreeOptions);
		model.addAttribute("facilityId", facilityId);
        return "devices/fixtures/assignprofile/dialog";
    }
    
}
