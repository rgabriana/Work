package com.ems.mvc.controller;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.service.ContactClosureManager;
import com.ems.service.SwitchManager;

import com.ems.utils.ArgumentUtils;
import com.ems.vo.ContactClosureVo;

@Controller
@RequestMapping("/contactClosure")
public class ContactClosureController {
	
	
	@Resource
    private SwitchManager switchManager;
		
	@Resource
    private ContactClosureManager contactClosureManager;
	
	@PreAuthorize("hasAnyRole('Admin')")
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String manageContactClosure(Model model)
	{
		return "contactClosure/management";
	}
	
	@PreAuthorize("hasAnyRole('Admin')")
	@RequestMapping(value = "/contactclosure_form.ems")
	public String editContactClosure(Model model, @RequestParam("macAddress") String macAddress)
	{
		
		if(contactClosureManager.getCCDataFromDB().getEnabled()) {
			model.addAttribute( "contactClosureEnable", "true");
		}else{
			model.addAttribute( "contactClosureEnable", "false");
		}
		
		ContactClosureVo contactClosureVo = contactClosureManager.getContactClosureVoByMacAddress(macAddress);
		
		if(contactClosureVo != null){
			if(!ArgumentUtils.isNullOrEmpty(contactClosureVo.getContactClosureControlsList())){
				model.addAttribute("contactClosureList", contactClosureVo.getContactClosureControlsList());
				model.addAttribute("contactClosureListSize", contactClosureVo.getContactClosureControlsList().size());
				model.addAttribute("macAddress", contactClosureVo.getMacAddress());
				model.addAttribute("ipAddress", contactClosureVo.getIpAddress());
				model.addAttribute("productId", contactClosureVo.getProductId());
				model.addAttribute("hwType", contactClosureVo.getHwType());
				model.addAttribute("fwVersion", contactClosureVo.getFwVersion());
				model.addAttribute("switchList", switchManager.loadAllSwitches());
				model.addAttribute("switchScenesList", switchManager.loadAllSwitchScenes());
			}
		}
				
		return "contactClosure/details";
	}

}
