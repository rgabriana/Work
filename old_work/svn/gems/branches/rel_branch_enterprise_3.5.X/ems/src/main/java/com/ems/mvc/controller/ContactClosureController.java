package com.ems.mvc.controller;

import java.io.IOException;
import javax.annotation.Resource;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.model.SystemConfiguration;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;

import com.ems.vo.ContactClosure;

@Controller
@RequestMapping("/contactClosure")
public class ContactClosureController {
	
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	
	@Resource
    private SwitchManager switchManager;
	
	@PreAuthorize("hasAnyRole('Admin')")
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String manageContactClosure(Model model)
	{
		
		
		SystemConfiguration contactClosureConfiguration = systemConfigurationManager.loadConfigByName("contact_closure_configuration");
		
		if(contactClosureConfiguration!=null)
		{
			ContactClosure contactClosure = null;
			try {
				if(!"".equals(contactClosureConfiguration.getValue())){
					contactClosure = new ObjectMapper().readValue(contactClosureConfiguration.getValue(),ContactClosure.class);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(contactClosure != null){
			
				if(contactClosure.getEnabled()) {
					model.addAttribute( "contactClosureEnable", "true");
				}else{
					model.addAttribute( "contactClosureEnable", "false");
				}
				
				model.addAttribute("contactClosureList", contactClosure.getContactClosureVo().get(0).getContactClosureControlsList());
				model.addAttribute("macAddress1", contactClosure.getContactClosureVo().get(0).getMacAddress());
				model.addAttribute("ipAddress1", contactClosure.getContactClosureVo().get(0).getIpAddress());
				model.addAttribute("productId1", contactClosure.getContactClosureVo().get(0).getProductId());
				model.addAttribute("hwType1", contactClosure.getContactClosureVo().get(0).getHwType());
				model.addAttribute("fwVersion1", contactClosure.getContactClosureVo().get(0).getFwVersion());
				
				model.addAttribute("switchList", switchManager.loadAllSwitches());
			}
		}
		
		return "contactClosure/management";
	}

}
