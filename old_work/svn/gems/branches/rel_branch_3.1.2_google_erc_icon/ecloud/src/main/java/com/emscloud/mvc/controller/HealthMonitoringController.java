package com.emscloud.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.EmInstance;
import com.emscloud.service.EmInstanceManager;

@Controller
public class HealthMonitoringController {
	
	@Resource
	EmInstanceManager emInstanceManager;
	
	@RequestMapping("/health/monitoring")
	public String getHealthStats(Model model){
		List<EmInstance> emInstanceList = emInstanceManager.getActiveEmInstanceWithDataSynch();
		model.addAttribute("emInstanceList", emInstanceList);		
		return "healthDashboard";		
	}
	
	@RequestMapping(value = "/health/details", method = { RequestMethod.POST })
	public String getEMHealthDetails(Model model,  @RequestParam("emInstanceId") long emInstanceId){
	    model.addAttribute("emInstanceId", emInstanceId);
		return "health/details";		
	}
	
	@RequestMapping(value = "/health/devices/details", method = { RequestMethod.GET })
	public String getDevicessHealthDetails(Model model, @RequestParam("emInstanceId") long emInstanceId,
			@RequestParam("deviceType") String deviceType){
		
		String page = "";
		if(deviceType.equalsIgnoreCase("gateways")){
			page = getGatewaysHealthDetails(model,emInstanceId);
		}else if(deviceType.equalsIgnoreCase("fixtures")){
			page = getFixturesHealthDetails(model,emInstanceId);
		}
		return page;
	}
	
	private String getFixturesHealthDetails(Model model,long emInstanceId) {
		
		return "health/fixtures/details";	
	}

	private String getGatewaysHealthDetails(Model model,long emInstanceId) {
		
		return "health/gateways/details";	
	}
}
