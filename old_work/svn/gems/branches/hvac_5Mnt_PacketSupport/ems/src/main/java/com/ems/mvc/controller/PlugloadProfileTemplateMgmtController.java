package com.ems.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.model.PlugloadProfileTemplate;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.PlugloadProfileTemplateManager;
import com.ems.types.UserAuditActionType;

@Controller
@RequestMapping("/plugloadProfileTemplateManagement")
public class PlugloadProfileTemplateMgmtController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "plugloadProfileTemplateManager")
	private PlugloadProfileTemplateManager plugloadProfileTemplateManager;
		
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String managePlugloadProfileTemplate(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie)
	{
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
		model.addAttribute("id", id);
		return "plugloadProfileTemplateManagement/management";
	}
	
	@RequestMapping(value = "/list.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadAllPlugloadTemplate(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie)
	{
				
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
    	List<PlugloadProfileTemplate> plugloadProfileTemplateList = plugloadProfileTemplateManager.loadAllPlugloadProfileTemplate();
		model.addAttribute("plugloadProfileTemplateList", plugloadProfileTemplateList);
		model.addAttribute("floorId", id);		
		return "plugloadTemplateManagement/list";
	}
	
	@RequestMapping("/create.ems")
	public String createPlugloadTemplate(Model model) {
	 	PlugloadProfileTemplate plugloadProfileTemplate = new PlugloadProfileTemplate();
        model.addAttribute("plugloadProfileTemplate", plugloadProfileTemplate);
        return "plugloadTemplate/details";
	}
	
	@RequestMapping(value = "/{plugloadProfileTemplateId}/edit.ems")
    public String editPlugloadProfileTemplate(Model model, @PathVariable("plugloadProfileTemplateId") Long plugloadProfileTemplateId) {
		PlugloadProfileTemplate plugloadProfileTemplate = plugloadProfileTemplateManager.getPlugloadProfileTemplateById(plugloadProfileTemplateId);
        model.addAttribute("plugloadProfileTemplate", plugloadProfileTemplate);
        return "plugloadTemplate/details";
    }

	
    @RequestMapping("/save.ems")
    public String savePlugloadTemplate(PlugloadProfileTemplate plugloadProfileTemplate) {
    	 String type=null;
    	 if(plugloadProfileTemplate.getId()!=null)
    	 {
         	 String plugloadProfileTemplateName = plugloadProfileTemplate.getName();
    		 type = "edit";         	
    		 plugloadProfileTemplate = plugloadProfileTemplateManager.getPlugloadProfileTemplateById(plugloadProfileTemplate.getId());
    		 plugloadProfileTemplate.setName(plugloadProfileTemplateName);
    		 plugloadProfileTemplate.setDisplayTemplate(true);
             plugloadProfileTemplateManager.editName(plugloadProfileTemplate);
    	 }
         else
         {
         	type = "new";
         	plugloadProfileTemplate.setDisplayTemplate(true);
         	plugloadProfileTemplateManager.save(plugloadProfileTemplate);
         }    	 	 
    	 
    	 
    	 if(type.equalsIgnoreCase("new"))
    		 userAuditLoggerUtil.log("Create new plugload profile template: " + plugloadProfileTemplate.getName(), UserAuditActionType.Plugload_Profile_Update.getName());
    	 else
    		 userAuditLoggerUtil.log("Edit plugload profile template new name : " + plugloadProfileTemplate.getName() + " Plugload Profile template id : "+plugloadProfileTemplate.getId(), UserAuditActionType.Plugload_Profile_Update.getName());
    	 return "redirect:/plugloadProfileTemplateManagement/list.ems?status="+type;
    }
	
}