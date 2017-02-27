package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.service.CustomerManager;

@Controller
public class CustomerController {
	@Resource
	CustomerManager customerManager;
	
	@RequestMapping(value = "/createCustomer.ems")
	public String createCustomer(Model model) {
		ArrayList<Customer> customers = new ArrayList<Customer>();
		Customer customer = (Customer) model.asMap().get("customer");
		customers = (ArrayList<Customer>) customerManager.loadallCustomer();

		if (customers == null || customers.isEmpty()) {
			customers.add(new Customer());

		}
		if (customer == null) {
			customer = new Customer();
		}
		model.addAttribute("customers", customers);
		model.addAttribute("customer", customer);
		

		return "createCustomer";

	}

	@RequestMapping(value = "/saveCustomer.ems")
	public String saveCustomer(Model model, Customer customer) {
		customerManager.saveOrUpdate(customer);
		
		List<Customer> customers = null;
		customers = customerManager.loadallCustomer();
		if (customers == null) {
			customers.add(new Customer());
		}

		customer = new Customer();

		model.addAttribute("customers", customers);
		model.addAttribute("customer", customer);

		return "redirect:/createCustomer.ems";
	}

	@RequestMapping(value = "/editCustomer.ems")
	public String editCustomer(@RequestParam("customerId") Long customerId,
			Model model, Customer customer) {

		customer = customerManager.loadCustomerById(customerId);

		model.addAttribute("customer", customer);
	

		return "setup/edit/customer";
	}

}
