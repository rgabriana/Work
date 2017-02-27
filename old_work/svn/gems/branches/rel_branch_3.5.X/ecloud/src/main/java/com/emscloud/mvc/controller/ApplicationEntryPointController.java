package com.emscloud.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emscloud.model.Customer;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SystemConfigurationManager;


@Controller
public class ApplicationEntryPointController {
	@Resource
	SystemConfigurationManager systemConfigurationManager;
	@Resource
	CustomerManager customerManager;
	@RequestMapping(value = "/home.ems")
	public String entryPoint() {
		String url="redirect:/createCustomer.ems";
		SystemConfiguration sysConf = systemConfigurationManager.loadConfigByName("cloud.mode");
		if (sysConf != null && "false".equals(sysConf.getValue()))
		{
			List<Customer> custList = customerManager.loadallCustomer();
			if(custList!=null && custList.size()>0)
			{
				url="redirect:/facilities/home.ems?customerId=" + custList.get(0).getId() ;
			}
		}
		return url;
	}
}
