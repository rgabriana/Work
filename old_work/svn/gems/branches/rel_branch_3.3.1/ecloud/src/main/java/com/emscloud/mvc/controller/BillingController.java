package com.emscloud.mvc.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.mvc.controller.util.ControllerUtils;
import com.emscloud.service.BillPaymentManager;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SppaManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.DateUtil;

@Controller
@RequestMapping("/bill")
public class BillingController {
    static final Logger logger = Logger.getLogger("CloudBilling");
	@Resource
	CustomerManager customerManager;
	
	@Resource
	SppaManager sppaManager;
	
	@Resource
	BillPaymentManager billPaymentManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
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

        ControllerUtils controllerUtilsObj = ControllerUtils.getInstance();
        if (controllerUtilsObj.getBillProcessRunningStatus() == 0) {
            controllerUtilsObj.setBillProcessRunningStatus(1);
            logger.info("Current Running Thread Name :"+ Thread.currentThread().getName() +". New Bill process started. Setting billProcessRunningStatus flag to 1");
            //System.out.println("Current Running Thread Name :"+ Thread.currentThread().getName() +"Bill process started. Setting billProcessRunningStatus flag to 1");
            Customer customer = null;
            Date billDueDate = new Date();
            CustomerDetailedBill customerBill = new CustomerDetailedBill();
            try {
                customer = customerManager.loadCustomerById(billCustomerId);
                DateFormat df = new SimpleDateFormat("MM/dd/yy");
                Date startDate = new Date();
                try {
                    startDate = df.parse(startDateStr);
                } catch (ParseException e) {
                    logger.error("Start date is invalid");
                }
                Date endDate = new Date();
                try {
                    endDate = df.parse(endDateStr);
                } catch (ParseException e) {
                    logger.error("end date is invalid");
                }
                billDueDate = DateUtil.addDays(endDate, 45);
                customerBill = sppaManager.generateBillPerCustomer(customer, startDate, endDate);
                
                cloudAuditLoggerUtil.log("Customer Bill generated for : "+customer.getName()+" ( "+customer.getId()+" ) from "+ startDateStr.replace("/", "-") +"to "+endDateStr.replace("/", "-"), CloudAuditActionType.Customer_Bill_Generate.getName());
                
            } catch (Exception e) {
                logger.error("Error is generating the bills for customer " + customer.getName() + ". Reason : "+ e.getMessage());
                cloudAuditLoggerUtil.log("Customer Bill Generation Failed for : "+customer.getName()+" ( "+customer.getId()+" ) from "+ startDateStr.replace("/", "-") +"to "+endDateStr.replace("/", "-"), CloudAuditActionType.Customer_Bill_Generate.getName());
            } finally {
                controllerUtilsObj.setBillProcessRunningStatus(0);
                logger.info("Current Running Thread Name :"+ Thread.currentThread().getName() +". Resetting billProcessRunningStatus flag to 0");
                //System.out.println("Current Running Thread Name :"+ Thread.currentThread().getName() +"Resetting billProcessRunningStatus flag to 0");
            }
            model.addAttribute("customer", customer);
            model.addAttribute("customerName", customer.getName());
            model.addAttribute("custId", customer.getId());
            model.addAttribute("startDate", startDateStr.replace("/", "-"));
            model.addAttribute("endDate", endDateStr.replace("/", "-"));
            model.addAttribute("billDueDate", billDueDate);
            model.addAttribute("noOfEmInstances", customerBill.getEmBills().size());
            model.addAttribute("sppaBill", customerBill.getEmBills());
            model.addAttribute("customerBill", customerBill.getBillInvoice());
            model.addAttribute("mode", "bill");
        }else
        {
            logger.info("Current Running Thread Name :"+ Thread.currentThread().getName() +". New Bill process already running");
            //System.out.println("Current Running Thread Name :"+ Thread.currentThread().getName() +"Bill process already running");
        }
		return "setup/gmb/customerInvoice";
	}
	
	
	@RequestMapping(value = "/reGenerateCustomerBill.ems")
	public String reGenerateCustomerBill(@RequestParam("rgbCustomerBillId") Long rgbCustomerBillId,@RequestParam("rgbCustomerId") Long rgbCustomerId,Model model) {

	    ControllerUtils controllerUtilsObj = ControllerUtils.getInstance();
        if (controllerUtilsObj.getBillProcessRunningStatus() == 0) {
            controllerUtilsObj.setBillProcessRunningStatus(1);
            logger.info("Current Running Thread Name :"+ Thread.currentThread().getName() +". Regenerate Bill process started. Setting billProcessRunningStatus flag to 1");
            Customer customer=null;
            String startDateStr=null;
            String endDateStr=null;
            Date billDueDate = new Date();
            CustomerDetailedBill customerDetailedBill=new CustomerDetailedBill();
            try {
        		customer = customerManager.loadCustomerById(rgbCustomerId);
        		customerDetailedBill = sppaManager.regenerateCustomerBill(rgbCustomerBillId);
        		Date startDate = customerDetailedBill.getBillInvoice().getBillStartDate();
        		Date endDate = customerDetailedBill.getBillInvoice().getBillEndDate();
        		model.addAttribute("customer", customer);
        		model.addAttribute("customerName", customer.getName());
        		model.addAttribute("custId", customer.getId());
        		
        		Calendar cal1 = Calendar.getInstance();
        		cal1.setTime(startDate);
        		DateFormat df = new SimpleDateFormat("MM/dd/yy");
        		startDateStr = df.format(cal1.getTime());
        		
        		Calendar cal2 = Calendar.getInstance();
        		cal2.setTime(endDate);
        		DateFormat df2 = new SimpleDateFormat("MM/dd/yy");
        		endDateStr = df2.format(cal2.getTime());
        		
        		billDueDate = DateUtil.addDays(endDate,45);
        		
        		cloudAuditLoggerUtil.log("Customer Bill regenerated for : "+customer.getName()+" ( "+customer.getId()+" ) from "+ startDateStr.replace("/", "-") +"to "+endDateStr.replace("/", "-"), CloudAuditActionType.Customer_Bill_Generate.getName());
        		
            } catch (Exception e) {
                logger.error("Error is regenerating the bills for customer " + customer.getName() + ". Reason : "+ e.getMessage());
                cloudAuditLoggerUtil.log("Customer Bill regeneration failed for : "+customer.getName()+" ( "+customer.getId()+" ) from "+ startDateStr.replace("/", "-") +"to "+endDateStr.replace("/", "-"), CloudAuditActionType.Customer_Bill_Generate.getName());
            } finally {
                controllerUtilsObj.setBillProcessRunningStatus(0);
                logger.info("Current Running Thread Name :"+ Thread.currentThread().getName() +". Resetting billProcessRunningStatus flag to 0");
            }
    		model.addAttribute("startDate",startDateStr.replace("/", "-"));
    		model.addAttribute("endDate",endDateStr.replace("/", "-"));
    		model.addAttribute("billDueDate",billDueDate);
    		model.addAttribute("noOfEmInstances",customerDetailedBill.getEmBills().size());
    		model.addAttribute("customerBill", customerDetailedBill.getBillInvoice());
    		model.addAttribute("sppaBill", customerDetailedBill.getEmBills());
    		model.addAttribute("mode", "bill");
        }else
        {
            logger.info("Current Running Thread Name :"+ Thread.currentThread().getName() +". Regenerate Bill process already running");
        }
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
		cloudAuditLoggerUtil.log("Bill viewed for Customer: " + customer.getName() + " (id - "
                + customerId + ").", CloudAuditActionType.Customer_Bill_View.getName());
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
		cloudAuditLoggerUtil.log("Bill payed for Customer: " + customer.getName() + " (id - "
                + customerId + "). Payment Amount : "+paymentAmount , CloudAuditActionType.Customer_Bill_Payment.getName());
        return "customer/billingPaymentListView";
	}
	
}
