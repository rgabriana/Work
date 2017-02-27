package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.model.Pricing;
import com.ems.service.PricingManager;
import com.ems.util.Constants;

@Controller
@RequestMapping("/pricing")
public class PricingController {
	
	@Resource (name= "pricingManager")
	private PricingManager pricingManager;
   
    @RequestMapping("/listPricing.ems")
    public String listPricing(Model model) {
		List<Pricing> pricingList = pricingManager.getPricingList();
		List<Pricing> weekDayPricingList = new ArrayList<Pricing>();
		List<Pricing> weekEndPricingList = new ArrayList<Pricing>();
		for (Pricing pricing : pricingList) {
			if (pricing.getDayType().equals(Constants.WEEK_DAY)) {
				weekDayPricingList.add(pricing);
			}
			if (pricing.getDayType().equals(Constants.WEEK_END)) {
				weekEndPricingList.add(pricing);		
			}
		}
		model.addAttribute("weekdays", weekDayPricingList);
		model.addAttribute("weekends",weekEndPricingList);      
    	return "pricing/listPricing";
    }
}
