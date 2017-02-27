package com.emscloud.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;

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

	@RequestMapping(value = "/list.ems", method = { RequestMethod.GET, RequestMethod.POST })
	public String listEmInstance(Model model,  @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
		
		return "eminstance/list";
	}
	
	@RequestMapping(value = "/listUnregEms.ems")
	public String loadUnregEms(Model model) {
		
		return "listUnregEms";

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
	
	@RequestMapping("/activate.ems")
    String activateEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("emInstance", emInstance);
		
		ArrayList<Customer> customers = new ArrayList<Customer>();
		customers = (ArrayList<Customer>) customerManager.loadallCustomer();
		model.addAttribute("customerList", customers);
        return "emActivate/details";
    }
	
	@RequestMapping("/activateEm.ems")
    String activateEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) {
        Customer customer = customerManager.loadCustomerById(customerId);
        
        EmInstance emInstanceObject;
		emInstanceObject = emInstanceManager.loadEmInstanceById(emInstance.getId());
        
		emInstanceObject.setCustomer(customer);
		emInstanceObject.setActive(true);
		emInstanceObject.setName(emInstance.getName());
		
		emInstanceManager.saveOrUpdate(emInstanceObject);
        
        String dbname = "em_" + customerId + "_" + emInstance.getId();
        Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(new String[]{"/usr/bin/createdb", "-U", "postgres", dbname});
			proc.waitFor();
			emInstanceObject.setDatabaseName(dbname);
	        emInstanceManager.saveOrUpdate(emInstanceObject);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "listUnregEms";
    }
	
	@RequestMapping("/edit.ems")
    String editEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("emInstance", emInstance);
		model.addAttribute("customerId", emInstance.getCustomer().getId());
        return "emInstance/details";
    }
	
	@RequestMapping("/save.ems")
    String saveEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) {
        Customer customer = customerManager.loadCustomerById(customerId);
        emInstance.setCustomer(customer);
        emInstance.setActive(true);
        emInstanceManager.saveOrUpdate(emInstance);
        
        String dbname = "em_" + customerId + "_" + emInstance.getId();
        Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(new String[]{"/usr/bin/createdb", "-U", "postgres", dbname});
			proc.waitFor();
	        emInstance.setDatabaseName(dbname);
	        emInstanceManager.saveOrUpdate(emInstance);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        return "redirect:/eminstance/list.ems?customerId=" + customerId ;
    }
	
	@RequestMapping(value = "/viewemstats.ems", method = RequestMethod.POST)
    String viewEmStats(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("emInstanceId", emInstanceId);
		model.addAttribute("emInstanceName", emInstance.getName());
		return "eminstance/viewemstats";
        
    }
	
}
