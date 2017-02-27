package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.model.Pricing;
import com.ems.model.SystemConfiguration;
import com.ems.service.CompanyManager;
import com.ems.service.PricingManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.util.Constants;

@Controller
@RequestMapping("/pricing")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class PricingController {
	
	@Resource (name= "pricingManager")
	private PricingManager pricingManager;
	
	@Resource (name= "companyManager")
	private CompanyManager companyManager;
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
   
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
		model.addAttribute("mode", "admin");
		
		SystemConfiguration pricingTypeConfig = systemConfigurationManager
        .loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing    		
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {    			
    			model.addAttribute("pricingType","1");
			}       
    		else if("2".equalsIgnoreCase(pricingTypeConfig
					.getValue()))
    		{    			
    			model.addAttribute("pricingType","2");
    		}
    	}
    	
    	String[] currencyArray = {"AED","AFN","ALL","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BOV","BRL","BSD","BTN","BWP","BYR","BZD","CAD","CDF","CHE","CHF","CHW","CLF","CLP","CNY","COP","COU","CRC","CUP","CVE","CYP","CZK","DJF","DKK","DOP","DZD","EEK","EGP","ERN","ETB","EUR","FJD","FKP","GBP","GEL","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL","HRK","HTG","HUF","IDR","ILS","INR","IQD","IRR","ISK","JMD","JOD","JPY","KES","KGS","KHR","KMF","KPW","KRW","KWD","KYD","KZT","LAK","LBP","LKR","LRD","LSL","LTL","LVL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRO","MTL","MUR","MVR","MWK","MXN","MXV","MYR","MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF","SAR","SBD","SCR","SDG","SEK","SGD","SHP","SKK","SLL","SOS","SRD","STD","SYP","SZL","THB","TJS","TMM","TND","TOP","TRY","TTD","TWD","TZS","UAH","UGX","USD","USN","USS","UYU","UZS","VEB","VND","VUV","WST","XAF","XAG","XAU","XBA","XBB","XBC","XBD","XCD","XDR","XFO","XFU","XOF","XPD","XPF","XPT","XTS","XXX","YER","ZAR","ZMK","ZWD"};
    	model.addAttribute("currencyArray", currencyArray);
    	
    	SystemConfiguration pricingCurrencyConfig = systemConfigurationManager.loadConfigByName("pricing.currency");
		if(pricingCurrencyConfig!=null)
		{
		   model.addAttribute("currencyType",pricingCurrencyConfig.getValue());			
		}
    	
    	Float defaultPrice = companyManager.getCompany().getPrice();
    	
    	model.addAttribute("defaultPrice", defaultPrice);
    	
    	return "pricing/listPricing";
    }
}
