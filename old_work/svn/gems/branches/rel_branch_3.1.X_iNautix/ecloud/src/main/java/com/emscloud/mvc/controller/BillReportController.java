package com.emscloud.mvc.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.emscloud.model.Customer;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SppaManager;
import com.emscloud.util.DateUtil;


@Controller
@RequestMapping("/pdfbills")
public class BillReportController extends AbstractController {
	@Resource
	CustomerManager customerManager;
	@Resource
	SppaManager sppaManager;
	
	@RequestMapping(value = "/downloadCustomerBill.ems")
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		Long customerId = ServletRequestUtils.getLongParameter(request, "dcbfCustomerId");
		Long customerBillId = ServletRequestUtils.getLongParameter(request, "dcbfCustomerBillId");
		String startDateStr = ServletRequestUtils.getStringParameter(request, "dcbfstartDateId");
		String endDateStr = ServletRequestUtils.getStringParameter(request, "dcbfendDateId");
		Customer customer = customerManager.loadCustomerById(customerId);
		CustomerDetailedBill customerBill = sppaManager.viewCustomerBill(customerBillId);
		
		Date endDate = customerBill.getBillInvoice().getBillEndDate();
		Date billDueDate = new Date();
		billDueDate = DateUtil.addDays(endDate,45);
			
		Map<String,Object> billingData = new HashMap<String,Object>();
		billingData.put("customer", customer);
		billingData.put("customerName", customer.getName());
		billingData.put("custId", customer.getId());
		billingData.put("startDate", startDateStr.replace("/", "-"));
		billingData.put("endDate", endDateStr.replace("/", "-"));
		billingData.put("billDueDate", billDueDate);
		billingData.put("noOfEmInstances", customerBill.getEmBills().size());
		billingData.put("customerBill", customerBill.getBillInvoice());
		billingData.put("sppaBill", customerBill.getEmBills());
		
		return new ModelAndView("BILLPDF","billingData",billingData);
	}
}
