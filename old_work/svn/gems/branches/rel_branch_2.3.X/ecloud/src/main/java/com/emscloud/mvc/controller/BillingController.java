package com.emscloud.mvc.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.service.BillPaymentManager;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SppaManager;
import com.emscloud.util.DateUtil;

@Controller
@RequestMapping("/bill")
public class BillingController {
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	SppaManager sppaManager;
	
	@Resource
	BillPaymentManager billPaymentManager;
	
	@RequestMapping(value = "/customerBilling.ems")
	public String customerBilling(@RequestParam("customerId") Long customerId,@RequestParam("lastBillDate") String lastBillDate,
			@RequestParam("totalBillCount") Long totalBillCount,
			Model model) {

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date startDate = new Date();
		Calendar cal = Calendar.getInstance();
		try {
			startDate = df.parse(lastBillDate);
			startDate = DateUtil.addDays(startDate,1);
		} catch (ParseException e) {
			//e.printStackTrace();
			cal.setTime(startDate);
	        cal.set(Calendar.DAY_OF_MONTH, 1);
	        startDate = cal.getTime();
		}
		String nextBillingDateStr = df.format(startDate);
		Date currentServerDate = new Date();
	    cal.setTime(currentServerDate);
	    cal.add(Calendar.DAY_OF_MONTH, -1);
	    currentServerDate = cal.getTime();

		String currentServerDateStr = df.format(currentServerDate);
		model.addAttribute("totalBillCount", totalBillCount);
		model.addAttribute("nextBillingDate",startDate.getTime());
		model.addAttribute("nextBillingDateStr",nextBillingDateStr);
		model.addAttribute("currentServerDateStr",currentServerDateStr);
		model.addAttribute("customerId", customerId);
		return "customer/billingPrompt";
	}
	
	@RequestMapping(value = "/generateCustomerBill.ems")
	public String generateCustomerBill(@RequestParam("gbfCustomerId") Long billCustomerId,@RequestParam("gbfbillstartDateId") String startDateStr,
			@RequestParam("gbfbillendDateId") String endDateStr,Model model) {

		Customer customer = customerManager.loadCustomerById(billCustomerId);
		
		DateFormat df = new SimpleDateFormat("MM/dd/yy");
		
		Date startDate = new Date();
		try {
			startDate = df.parse(startDateStr);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		Date endDate = new Date();
		try {
			endDate = df.parse(endDateStr);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		Date billDueDate = new Date();
		billDueDate = DateUtil.addDays(endDate,45);
		
		CustomerDetailedBill customerBill = sppaManager.generateBillPerCustomer(customer,startDate,endDate);
		
		model.addAttribute("customer", customer);
		model.addAttribute("customerName", customer.getName());
		model.addAttribute("custId", customer.getId());
		model.addAttribute("startDate",startDateStr.replace("/", "-"));
		model.addAttribute("endDate",endDateStr.replace("/", "-"));
		model.addAttribute("billDueDate",billDueDate);
		model.addAttribute("noOfEmInstances",customerBill.getEmBills().size());
		model.addAttribute("sppaBill", customerBill.getEmBills());
		model.addAttribute("customerBill", customerBill.getBillInvoice());
		model.addAttribute("mode", "bill");
	
		return "setup/gmb/customerInvoice";
	}
	
	
	@RequestMapping(value = "/reGenerateCustomerBill.ems")
	public String reGenerateCustomerBill(@RequestParam("rgbCustomerBillId") Long rgbCustomerBillId,@RequestParam("rgbCustomerId") Long rgbCustomerId,Model model) {

		Customer customer = customerManager.loadCustomerById(rgbCustomerId);
		CustomerDetailedBill customerDetailedBill = sppaManager.regenerateCustomerBill(rgbCustomerBillId);
		Date startDate = customerDetailedBill.getBillInvoice().getBillStartDate();
		Date endDate = customerDetailedBill.getBillInvoice().getBillEndDate();
		model.addAttribute("customer", customer);
		model.addAttribute("customerName", customer.getName());
		model.addAttribute("custId", customer.getId());
		
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(startDate);
		DateFormat df = new SimpleDateFormat("MM/dd/yy");
		String startDateStr = df.format(cal1.getTime());
		
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(endDate);
		DateFormat df2 = new SimpleDateFormat("MM/dd/yy");
		String endDateStr = df2.format(cal2.getTime());
		
		Date billDueDate = new Date();
		billDueDate = DateUtil.addDays(endDate,45);
		
		model.addAttribute("startDate",startDateStr.replace("/", "-"));
		model.addAttribute("endDate",endDateStr.replace("/", "-"));
		model.addAttribute("billDueDate",billDueDate);
		model.addAttribute("noOfEmInstances",customerDetailedBill.getEmBills().size());
		model.addAttribute("customerBill", customerDetailedBill.getBillInvoice());
		model.addAttribute("sppaBill", customerDetailedBill.getEmBills());
		model.addAttribute("mode", "bill");
		return "setup/gmb/customerInvoice";
	}
	
	@RequestMapping(value = "/generateCustomerReport.ems")
	public String generateCustomerReport(@RequestParam("crfCustomerId") Long customerId,@RequestParam("crfCustomerBillId") Long customerBillId,@RequestParam("crfstartDateId") String startDateStr,
			@RequestParam("crfendDateId") String endDateStr,Model model) {

		Customer customer = customerManager.loadCustomerById(customerId);
		CustomerDetailedBill customerBill = sppaManager.viewCustomerBill(customerBillId);
		
		Date endDate = customerBill.getBillInvoice().getBillEndDate();
		Date billDueDate = new Date();
		billDueDate = DateUtil.addDays(endDate,45);
		model.addAttribute("customer", customer);
		model.addAttribute("customerName", customer.getName());
		model.addAttribute("custId", customer.getId());
		model.addAttribute("startDate",startDateStr.replace("/", "-"));
		model.addAttribute("endDate",endDateStr.replace("/", "-"));
		model.addAttribute("billDueDate",billDueDate);
		model.addAttribute("noOfEmInstances",customerBill.getEmBills().size());
		
		model.addAttribute("customerBill", customerBill.getBillInvoice());
		model.addAttribute("sppaBill", customerBill.getEmBills());
		model.addAttribute("mode", "bill");
		return "setup/gmb/customerInvoice";
	}
	
	@RequestMapping(value = "/emBillingMain.ems")
	public String emBillingMain(Model model, @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
        return "customer/emBillingMain";
	}
	@RequestMapping(value = "/billingListDetails.ems")
	public String billingListDetails(Model model, @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
        return "customer/billingListView";
	}
	@RequestMapping(value = "/billingPaymentDetails.ems")
	public String billingPaymentDetails(Model model, @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
        return "customer/billingPaymentListView";
	}
	@RequestMapping(value = "/billingPaymentPromt.ems")
	public String billingPaymentPromt(Model model, @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
		Date paymentDate = new Date();
		model.addAttribute("paymentDate",paymentDate);
        return "customer/billingPaymentPrompt";
	}
	
	@RequestMapping(value = "/updateBillPayment.ems")
	public String updateBillPayment(Model model, @RequestParam("bppfCustomerId") long customerId,@RequestParam("bppfPaymentAmt") BigDecimal paymentAmount)
	{
		model.addAttribute("customerId", customerId);
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
		
		billPaymentManager.updateCustomerBillPayment(customerId,paymentAmount);
		
        return "customer/billingPaymentListView";
	}
	
}
