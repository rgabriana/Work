package com.emscloud.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.emscloud.action.SpringContext;
import com.emscloud.model.Customer;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SppaManager;
import com.emscloud.util.DateUtil;

public class GenerateBillJob implements Job {
	static final Logger logger = Logger.getLogger("CloudBilling");

	CustomerManager customerManager;
	SppaManager sppaManager;
		
	public GenerateBillJob()
	{
		customerManager = (CustomerManager)SpringContext.getBean("customerManager");
		sppaManager = (SppaManager)SpringContext.getBean("sppaManager");
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		try{
			//This Job will fire the Generate Bill Service on Every 4th Of Calendar Months. 
			// 1. Get List of all Customers.
			// 2. Get All bills per Customer
			// 2. Check whether any previous bill present for the said customer
			// 3. If any previous bill present then get next billing date which would be last bill's end date + that month's last day
			// 4. If no previous bill present then calculate the bill from 1st to last of month
		
			List<Customer> customers = customerManager.loadallCustomer();
			Iterator<Customer> custIter = customers.iterator();
			while(custIter.hasNext()) {
				Customer customer = custIter.next();
				if(customer!=null)
				{
					//System.out.println("Billing Job Initiated: Customer : " +customer.getName()  + "Jobkey "+ jobKey + " executing at " + new Date());
					logger.info("Billing Job Initiated: Customer : " +customer.getName()  + " : Jobkey :"+ jobKey + " executing at " + new Date());
					List<CustomerSppaBill> customerSppaBillList = sppaManager.getEMBillByCustomerId(customer.getId());
					CustomerSppaBill customerSppaBill=null;
					if(customerSppaBillList.size()>=1)
					{
						customerSppaBill = customerSppaBillList.get(0);
					}
					Date startDate = null;
					Date endDate = null;
					Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
					Calendar aCalendar = Calendar.getInstance();
					// add -1 month to current month to get previous month
					aCalendar.add(Calendar.MONTH, -1);
					//That is there is some previous bill generated...Hence calculate last bill's bill end date
					if(customerSppaBill!=null)
					{
						startDate = customerSppaBill.getBillEndDate();
						startDate = DateUtil.addDays(startDate,1);
						
					}else
					{
						// set DATE to 1, so first date of previous month
						aCalendar.set(Calendar.DATE, 1);
						startDate = aCalendar.getTime();
					}
					// set actual maximum date of previous month
					aCalendar.set(Calendar.DAY_OF_MONTH, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
					//read the last day of the billing month
					endDate = aCalendar.getTime();
					
				        	
					//Check if any monthly complete bill already generated...!
					if(customerSppaBill!=null && DateUtil.isEqualDate(endDate, customerSppaBill.getBillEndDate()))
					{
						//System.out.println("Bill is already generated for customer : " + customer.getName() + "  in previous month!");
						logger.info("Bill is already generated for customer :" + customer.getName() + "  in previous month!");
						continue;
					}

					//Check DATE Validation : start date < end date
					boolean bFlag = true;
					if(startDate.compareTo(endDate)>0){
						bFlag = false;
		        		//System.out.println("startDate is after endDate");
		        	}else if(startDate.compareTo(endDate)<0){
		        		bFlag = true;
		        		//System.out.println("startDate is before endDate");
		        	}else if(startDate.compareTo(endDate)==0){
		        		bFlag = true;
		        		//System.out.println("endDate is equal to endDate");
		        	}
					
					//Generate the Bill
					if(bFlag)
					{
						CustomerDetailedBill customerBill = sppaManager.generateBillPerCustomer(customer,startDate,endDate);
						logger.info("Bill Generated for the Customer :"+ customer.getName() +  " for Period " + startDate + " to " + endDate );
						//System.out.println("Bill Generated for the Customer :"+ customer.getName() +  " for Period " + startDate + " to " + endDate );
					}else
					{
						logger.info("Bill Could not be generated for customer :"+ customer.getName()  + ", Please check  start date should be always less than end date");
						//System.out.println("Bill Could not be generated for customer :"+ customer.getName()  + ", Please check  start date should be always less than end date");
					}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			logger.info(e.getMessage() +  " There is some problem starting the billing Job " + jobKey);
			//System.out.println(e.getMessage() +  " There is some problem starting the billing Job " + jobKey);
		}
	}
}
