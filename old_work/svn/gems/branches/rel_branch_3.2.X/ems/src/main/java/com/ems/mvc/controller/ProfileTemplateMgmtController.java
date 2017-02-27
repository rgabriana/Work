package com.ems.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.model.ProfileTemplate;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
import com.ems.service.ProfileManager;
import com.ems.service.ProfileTemplateManager;
import com.ems.types.UserAuditActionType;

@Controller
@RequestMapping("/profileTemplateManagement")
public class ProfileTemplateMgmtController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "profileTemplateManager")
	private ProfileTemplateManager profileTemplateManager;
	@Resource(name = "profileManager")
	private ProfileManager profileManager;
	@Resource(name = "metaDataManager")
    private MetaDataManager metaDataManager; 
	@Resource(name = "groupManager")
	private GroupManager groupManager;
	@Resource(name = "companyManager")
	private CompanyManager companyManager;
	
	
	@Autowired
	private MessageSource messageSource;
	
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String manageProfileTemplate(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie)
	{
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
		model.addAttribute("id", id);
		return "profileTemplateManagement/management";
	}
	
	@RequestMapping(value = "/list.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadAllTemplate(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie)
	{
				
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
    	List<ProfileTemplate> templateList = profileTemplateManager.loadAllProfileTemplate();
		model.addAttribute("templateList", templateList);
		model.addAttribute("floorId", id);		
		return "templateManagement/list";
	}
	
	@RequestMapping("/create.ems")
	public String createTemplate(Model model) {
	 	ProfileTemplate profileTemplate = new ProfileTemplate();
        model.addAttribute("profileTemplate", profileTemplate);
        return "template/details";
	}

	@RequestMapping(value = "/{templateId}/edit.ems")
    public String editTenant(Model model, @PathVariable("templateId") Long templateId) {
		ProfileTemplate profileTemplate = profileTemplateManager.getProfileTemplateById(templateId);
        model.addAttribute("profileTemplate", profileTemplate);
        return "template/details";
    }

    @RequestMapping("/save.ems")
    public String saveTemplate(ProfileTemplate profileTemplate) {
    	 String type=null;
    	 if(profileTemplate.getId()!=null)
    	 {
         	 String profileName = profileTemplate.getName();
    		 type = "edit";         	
    	 	 profileTemplate = profileTemplateManager.getProfileTemplateById(profileTemplate.getId());
    	 	 profileTemplate.setName(profileName);
    	 	 profileTemplate.setDisplayTemplate(true);
             profileTemplateManager.editName(profileTemplate);
    	 }
         else
         {
         	type = "new";
         	profileTemplate.setDisplayTemplate(true);
         	profileTemplateManager.save(profileTemplate);
         }    	 	 
    	 
    	 
    	 if(type.equalsIgnoreCase("new"))
    		 userAuditLoggerUtil.log("Create new profile template: " + profileTemplate.getName(), UserAuditActionType.Profile_Update.getName());
    	 else
    		 userAuditLoggerUtil.log("Edit profile template new name : " + profileTemplate.getName() + " Profile template id : "+profileTemplate.getId(), UserAuditActionType.Profile_Update.getName());
    	 
    	 //Create Default profile for the New Template created
    	 /*
    	 if(type=="new")
    	 {
	    	 ProfileHandler profileHandler = profileManager.createProfile("default.",ServerConstants.DEFAULT_PROFILE_GID);
	    	 Groups group = new Groups();
	         group.setName(profileTemplate.getName());
	         group.setProfileHandler(profileHandler);
	         short profileNo = (groupManager.getMaxProfileNo());
	         group.setProfileNo(profileNo);
	         group.setCompany(companyManager.getAllCompanies().get(0));
	         group.setProfileTemplate(profileTemplate);
	         group.setDisplayProfile(true);
	         group.setDefaultProfile(true);
	         Groups derivedGrp =  groupManager.getGroupById((long) 1);
	         group.setDerivedFromGroup(derivedGrp);
	         metaDataManager.saveOrUpdateGroup(group);
    	 }
    	 */
    	 
        return "redirect:/profileTemplateManagement/list.ems?status="+type;
    }
	
}


