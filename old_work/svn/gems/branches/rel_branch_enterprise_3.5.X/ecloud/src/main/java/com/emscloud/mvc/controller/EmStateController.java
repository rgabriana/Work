package com.emscloud.mvc.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.EmInstance;
import com.emscloud.service.EmInstanceManager;

@Controller
@RequestMapping("/emstate")
public class EmStateController {
	
	@Resource
	EmInstanceManager		emInstanceManager;
	
	@RequestMapping(value = "/loademstate.ems", method = RequestMethod.GET)
    String loadEmStats(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
			model.addAttribute("emInstanceId", emInstanceId);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("customerId", emInstance.getCustomer().getId());
		}		
		return "eminstance/loademstate";
     }
}
