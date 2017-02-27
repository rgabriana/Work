package com.emscloud.mvc.controller;

import javax.annotation.Resource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;

@Controller
@RequestMapping("/eminstance")
public class EmInstanceController {

	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	CustomerManager customerManager;

	@RequestMapping(value = "/list.ems")
	public String listEmInstance(Model model,  @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		
		return "eminstance/list";
	}

	
	@RequestMapping(value = "/save_mapping.ems", method = RequestMethod.POST)
	public String saveFacilityMapping(Model model,
			@RequestParam("selectedFacilities") String selectedFacilities) {

		String[] assignedFacilities = selectedFacilities.split(",");
		return "redirect:/eminstance/list.ems";
	}

	@RequestMapping("/delete.ems")
	public String deleteEmInstance(Model model, @RequestParam("id") long id) {
		Integer status =0;
		try
		{
			emInstanceManager.delete(id);
			status = 0;
		}catch(DataIntegrityViolationException de)
		{
			status = 1;
		}
		return "redirect:/eminstance/list.ems?deleteStatus="+status;
	}
	
	@RequestMapping("/create.ems")
    String createEmInstance(Model model, @RequestParam("customerId") Long customerId) {

		EmInstance emInstance = new EmInstance();
		model.addAttribute("emInstance", emInstance);
		model.addAttribute("customerId", customerId);
        return "emInstance/details";
    }
	
	@RequestMapping("/save.ems")
    String saveEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) {
        Customer customer = customerManager.loadCustomerById(customerId);
        emInstance.setCustomer(customer);
        emInstanceManager.saveOrUpdate(emInstance);
        return "redirect:/eminstance/list.ems?customerId=" + customerId ;
    }
}
