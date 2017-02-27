package com.emscloud.mvc.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.SppaBill;
import com.emscloud.model.UserCustomers;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SppaManager;
import com.emscloud.service.UserCustomersManager;
import com.emscloud.service.UserManager;
import com.emscloud.types.RoleType;
import com.emscloud.util.DateUtil;

@Controller
@PreAuthorize("hasAnyRole('Admin','SystemAdmin','SupportAdmin')")
public class CustomerController {
	@Resource
	CustomerManager customerManager;
	
	@Resource
	SppaManager sppaManager;
	
	@Resource
	UserCustomersManager userCustomersManager;
	
	@Resource
	UserManager userManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@RequestMapping(value = "/createCustomer.ems")
	public String createCustomer(Model model) {
		ArrayList<Customer> customers = new ArrayList<Customer>();
		Customer customer = (Customer) model.asMap().get("customer");
	
		//Check for super user
		if(emsAuthContext.getCurrentUserRoleType().equals(RoleType.Admin))
		customers = (ArrayList<Customer>) customerManager.loadallCustomer();
		else
		{	//Load customers by assignment
			List<UserCustomers> uCustomers = userCustomersManager.loadUserCustomersByUserId(emsAuthContext.getUserId());
			for (Iterator<UserCustomers> iterator = uCustomers.iterator(); iterator.hasNext();) {
				UserCustomers userCustomers = (UserCustomers) iterator.next();				
				customers.add(customerManager.loadCustomerById(userCustomers.getCustomer().getId()));
			}		
		}		

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
	
	@RequestMapping(value = "/addCustomer.ems")
	public String addCustomer(Model model, Customer customer) {
		customer = new Customer();
		model.addAttribute("customer", customer);
		return "setup/add/customer";
	}

	@RequestMapping(value = "/editCustomer.ems")
	@PreAuthorize("hasAnyRole('Admin')")
	public String editCustomer(@RequestParam("customerId") Long customerId,
			Model model, Customer customer) {

		customer = customerManager.loadCustomerById(customerId);

		model.addAttribute("customer", customer);
	

		return "setup/edit/customer";
	}
	
	@RequestMapping(value = "/gmbCustomerReport.ems")
	public String gmbCustomerReport(@RequestParam("customerId") Long customerId,
			Model model) {

		Customer customer = customerManager.loadCustomerById(customerId);
		
		model.addAttribute("customer", customer);
		model.addAttribute("customerName", customer.getName());
		model.addAttribute("custId", customer.getId());
	
		return "setup/gmb/customerReport";
	}
	
	@RequestMapping(value = "/gmbCustomerInvoice.ems")
	public String gmbCustomerInvoice(@RequestParam("customerId") Long customerId,@RequestParam("startDate") String startDateStr,
			@RequestParam("endDate") String endDateStr,Model model) {

		Customer customer = customerManager.loadCustomerById(customerId);
		
		DateFormat df = new SimpleDateFormat("MM/dd/yy");
		
		Date startDate = new Date();
		try {
			startDate = df.parse(startDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		Date endDate = new Date();
		try {
			endDate = df.parse(endDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		CustomerDetailedBill customerBill = sppaManager.getBillReportCustomer(customer,startDate,endDate, false, null);
		
		model.addAttribute("customer", customer);
		model.addAttribute("customerName", customer.getName());
		model.addAttribute("custId", customer.getId());
		model.addAttribute("startDate",startDateStr.replace("/", "-"));
		model.addAttribute("endDate",endDateStr.replace("/", "-"));
		
		Date billDueDate = new Date();
		billDueDate = DateUtil.addDays(endDate,45);
		model.addAttribute("billDueDate",billDueDate);
		model.addAttribute("noOfEmInstances",customerBill.getEmBills().size());
		model.addAttribute("sppaBill", customerBill.getEmBills());
		model.addAttribute("customerBill", customerBill.getBillInvoice());
		model.addAttribute("mode", "report");
		return "setup/gmb/customerInvoice";
	}
	
	@RequestMapping(value = "/gmbCustomerInvoicePrompt.ems")
	@PreAuthorize("hasAnyRole('Admin','SystemAdmin','SupportAdmin')")
	public String gmbCustomerInvoicePrompt(@RequestParam("customerId") Long customerId,
			Model model) {

		model.addAttribute("custId", customerId);

		return "setup/gmb/customerInvoicePrompt";
	}
	
	@RequestMapping(value = "/reportCustomerInvoicePrompt.ems")
	public String reportCustomerInvoicePrompt(@RequestParam("customerId") Long customerId,
			Model model) {

		model.addAttribute("custId", customerId);

		return "customer/invoice";
	}
}
