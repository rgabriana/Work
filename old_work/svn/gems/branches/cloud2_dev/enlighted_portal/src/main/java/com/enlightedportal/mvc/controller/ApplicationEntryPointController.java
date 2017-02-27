package com.enlightedportal.mvc.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.enlightedportal.model.Customer;
import com.enlightedportal.model.LicenseDetails;
import com.enlightedportal.mvc.util.ControllerUtils;
import com.enlightedportal.service.CustomerManager;
import com.enlightedportal.service.LicenseDetailManager;
import com.enlightedportal.types.UserAuditActionType;
import com.enlightedportal.utils.UserAuditLoggerUtil;

@Controller
public class ApplicationEntryPointController {
	@Resource
	CustomerManager customerManager;
	@Resource
	LicenseDetailManager licenseDetailManager;

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

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
		userAuditLoggerUtil.log(UserAuditActionType.Customer_Create,
				"Created Customer " + customer.getName());

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
		userAuditLoggerUtil.log(UserAuditActionType.Customer_Update,
				"Updated Customer " + customer.getName());

		return "setup/edit/customer";
	}

	@RequestMapping(value = "/home.ems")
	public String entryPoint() {

		return "index";

	}

	@RequestMapping(value = "/generateKey.ems")
	public String generateKey(Model model) {
		LicenseDetails licenseDetails = new LicenseDetails();
		ArrayList<Customer> customers = (ArrayList<Customer>) customerManager
				.loadallCustomer();
		model.addAttribute("licenseDetails", licenseDetails);
		model.addAttribute("customers", customers);
		return "setup/generateKey";
	}

	@RequestMapping(value = "/saveKey.ems")
	public String saveKey(Model model, LicenseDetails licenseDetails) {
		if (licenseDetails != null) {

			licenseDetails.setStatus("active");
			licenseDetails.setCreatedOn(Calendar.getInstance().getTime());
			licenseDetails = ControllerUtils.generateKey(licenseDetails);
			licenseDetailManager.saveOrUpdate(licenseDetails);
			userAuditLoggerUtil.log(UserAuditActionType.License_Generation,
					"Generated License for Mac " + licenseDetails.getMacId());

			return "redirect:/generateKey.ems?status=true";
		}
		return "redirect:/generateKey.ems?status=false";
	}

}
