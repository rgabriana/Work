package com.emscloud.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.SppaDao;
import com.emscloud.model.Customer;
import com.emscloud.model.CustomerBills;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.EmInstance;
import com.emscloud.model.SppaBill;
import com.emscloud.types.BillStatus;

@Service("sppaManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SppaManager {
	
	@Resource
	private SppaDao sppaDao;
	
	@Resource
	EmInstanceManager emInstanceManger;
	
	public SppaManager() {
		// TODO Auto-generated constructor stub
	}
		
	public CustomerDetailedBill getBillReportCustomer(long custId, Date startDate, Date endDate, boolean save, CustomerSppaBill aggregateBill) {
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(endDate);		
		//calTo.add(Calendar.DATE, 1);
		calTo.set(Calendar.HOUR, 0);
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(startDate);		
		calFrom.set(Calendar.HOUR, 0);
		calFrom.set(Calendar.MINUTE, 0);
		calFrom.set(Calendar.SECOND, 0);
			
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		
		SppaBill emBill = null;
		double totalBaselineEnergy = 0.0;
		double totalBaseCost = 0.0;
		double totalConsumedEnergy = 0.0;
		double totalSavedCost = 0.0;
		double totalSppaCost = 0.0;
		double totalTax = 0.0;
		
		ArrayList<SppaBill> emBills = new ArrayList<SppaBill>();
		while(emIter.hasNext()) {
			EmInstance emInst = emIter.next();			
			if(!emInst.getSppaBillEnabled() || !emInst.getSppaEnabled()) {
				continue;
			}
			//generate the monthly bill for the em
			emBill = generateEmSppaBill(emInst, calFrom, calTo, save, aggregateBill);
			emBill.setGeoLocation(emInst.getGeoLocation());
			emBills.add(emBill);
			if(emBill.getBaseCost() != null) {
				totalBaseCost += emBill.getBaseCost();
			}
			if(emBill.getBaselineEnergy() != null) {
				totalBaselineEnergy += emBill.getBaselineEnergy().doubleValue();
			}
			if(emBill.getConsumedEnergy() != null) {
				totalConsumedEnergy += emBill.getConsumedEnergy().doubleValue();
			}
			if(emBill.getSavedCost() != null) {
				totalSavedCost += emBill.getSavedCost();
			}
			if(emBill.getSppaCost() != null) {
				totalSppaCost += emBill.getSppaCost();
			}
			if(emBill.getTax() != null) {
				totalTax += emBill.getTax();
			}
			
		}
		
		//SppaBill aggregateBill = new SppaBill();		
		if(aggregateBill == null) {
			aggregateBill = new CustomerSppaBill();
		}
		aggregateBill.setBillCreationTime(new Date());
		aggregateBill.setBillEndDate(calTo.getTime());
		aggregateBill.setBillStartDate(calFrom.getTime());
		aggregateBill.setNoOfDays(noOfDaysBetween(calFrom.getTime(), calTo.getTime()));
		aggregateBill.setBaseCost(totalBaseCost);
		aggregateBill.setBaselineEnergy(new BigDecimal(totalBaselineEnergy));
		aggregateBill.setConsumedEnergy(new BigDecimal(totalConsumedEnergy));		
		aggregateBill.setSavedCost(totalSavedCost);
		aggregateBill.setSppaCost(totalSppaCost);
		aggregateBill.setTax(totalTax);
		
		CustomerDetailedBill billObj = new CustomerDetailedBill();		
		billObj.setBillInvoice(aggregateBill);
		billObj.setEmBills(emBills);
		return billObj;
		
	} //end of method getBillReportCustomer
	
	//both startDate and endDate inclusive
	public CustomerDetailedBill generateBillPerCustomer(Customer customer, Date startDate, Date endDate) {
				
		CustomerSppaBill custSppaBill = new CustomerSppaBill();
		custSppaBill.setCustomer(customer);
		custSppaBill.setBillStatus(BillStatus.INACTIVE.ordinal());
		//System.out.println("customer id-- " + customer.getId());
		custSppaBill = sppaDao.saveOrUpdate(custSppaBill);
		CustomerDetailedBill billObj = getBillReportCustomer(customer.getId(), startDate, endDate, true, custSppaBill);
		billObj.getBillInvoice().setBillStatus(BillStatus.ACTIVE.ordinal());
		custSppaBill = sppaDao.saveOrUpdate(billObj.getBillInvoice());
		billObj.setBillInvoice(custSppaBill);
		return billObj;
				
	} //end of method generateBillPerCustomer
	
	public CustomerDetailedBill viewCustomerBill(long custBillId) {
		
		CustomerDetailedBill billObj = new CustomerDetailedBill();
		CustomerSppaBill custSppaBill = sppaDao.loadCustomerSppaBillById(custBillId);
		billObj.setBillInvoice(custSppaBill);
		
		List<SppaBill> sppaBillList = sppaDao.getEmBillsOfCustomerBill(custBillId);
		Iterator<SppaBill> sppaBillIter = sppaBillList.iterator();	
		double totalTax = 0.0;
		while(sppaBillIter.hasNext()) {
			SppaBill sppaBill = sppaBillIter.next();
			EmInstance emInstance = emInstanceManger.loadEmInstanceById(sppaBill.getEmInstance().getId());
			sppaBill.setGeoLocation(emInstance.getGeoLocation());
			double savedEnergy = 0;
			double sppaCost = 0;
			double tax = 0;
			if(sppaBill.getBaselineEnergy() != null && sppaBill.getConsumedEnergy() != null) {
				savedEnergy = sppaBill.getBaselineEnergy().doubleValue() - sppaBill.getConsumedEnergy().doubleValue();
				sppaCost = savedEnergy * sppaBill.getEmInstance().getSppaPrice() / 1000;
				tax = sppaCost * sppaBill.getEmInstance().getTaxRate() / 100;
			}
			sppaBill.setEnergySaved(new BigDecimal(savedEnergy));
			sppaBill.setTax(tax);
			totalTax += tax;
			sppaBill.setSppaCost(sppaCost);
			sppaBill.setSppaPayableDue(sppaCost+tax);
		}
		custSppaBill.setTax(totalTax);
		billObj.setEmBills(sppaBillList);
		return billObj;
		
	} //end of method viewCustomerBill
	
	public List<SppaBill> getEmBiillsOfCustomerBill(long custBillId) {
		
		return sppaDao.getEmBillsOfCustomerBill(custBillId);
				
	}
	
	public List<SppaBill> getLastMonthBillPerCustomer(long custId) {
					
		return sppaDao.getLastMonthBillPerCustomer(custId);
				
	} //end of method getLastMonthBillPerCustomer
	
	
	public SppaBill generateLastMonthBillPerSite(EmInstance emInstance,Date startDate, Date endDate) {
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(endDate);		
		//calTo.add(Calendar.DATE, 1);
		calTo.set(Calendar.HOUR, 0);
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(startDate);		
		calFrom.set(Calendar.HOUR, 0);
		calFrom.set(Calendar.MINUTE, 0);
		calFrom.set(Calendar.SECOND, 0);

		if(!emInstance.getSppaBillEnabled() || !emInstance.getSppaEnabled()) {
			return null;
		}
		
		//generate the monthly bill for the em
		return generateEmSppaBill(emInstance, calFrom, calTo, false, null);
		
	} //end of method generateLastMonthBillPerSite
	
	private SppaBill generateEmSppaBill(EmInstance emInst, Calendar fromCal, Calendar toCal, boolean save, CustomerSppaBill aggregateBill) {
		
		SppaBill bill = sppaDao.getMonthlyBillData(emInst.getDatabaseName(), emInst.getReplicaServer().getInternalIp(), 
				fromCal.getTime(), toCal.getTime());
		if(bill == null) {
			bill = new SppaBill();			
		} else {
			if(bill.getBaselineEnergy() != null && bill.getConsumedEnergy() != null) {
				double savedEnergy = bill.getBaselineEnergy().doubleValue() - bill.getConsumedEnergy().doubleValue();
				double sppaCost = savedEnergy * emInst.getSppaPrice() / 1000;
				double tax = sppaCost * emInst.getTaxRate() / 100;
				bill.setEnergySaved(new BigDecimal(savedEnergy));
				bill.setTax(tax);
				bill.setSppaCost(sppaCost);
				bill.setSppaPayableDue(sppaCost+tax);
			}
		}
		bill.setBillCreationTime(new Date());
		bill.setBillStartDate(fromCal.getTime());
		bill.setBillEndDate(toCal.getTime());
		bill.setNoOfDays(noOfDaysBetween(fromCal.getTime(), toCal.getTime()));
		bill.setEmInstance(emInst);
		if(save) {
			//save the monthly bill in the monthly table
			if(aggregateBill != null) {
				bill.setCustomerBill(aggregateBill);
			}
			double blockEnergyConsumed = 0;
			double energySaved = 0;
			long noOfDays = 0;
			if(emInst.getBlockEnergyConsumed() != null) {
				blockEnergyConsumed = emInst.getBlockEnergyConsumed().doubleValue();
			}
			if(bill.getEnergySaved()!=null)
			{
				energySaved = bill.getEnergySaved().doubleValue();
			}
			if(emInst.getTotalBilledNoOfDays() != null) {
				noOfDays = emInst.getTotalBilledNoOfDays().longValue();
			}
			double totalConsumedEnergy =  blockEnergyConsumed +	energySaved / 1000;
			long totalNoOfDays = noOfDays + bill.getNoOfDays();
			double blockEnergyRemaining = emInst.getBlockPurchaseEnergy().doubleValue() - totalConsumedEnergy;
			bill.setBlockEnergyRemaining(new BigDecimal(blockEnergyRemaining));
			double timeRemaining = blockEnergyRemaining * totalNoOfDays / totalConsumedEnergy;
			bill.setBlockTermRemaining((long)timeRemaining);
			bill = sppaDao.saveOrUpdate(bill);			
			emInst.setBlockEnergyConsumed(new BigDecimal(totalConsumedEnergy));
			emInst.setTotalBilledNoOfDays(totalNoOfDays);
			emInstanceManger.saveOrUpdate(emInst);
		}
		return bill;
		
	} //end of method generateEmSppaBill	
	
	private static int noOfDaysBetween(Date fromDate, Date toDate) {
		
		System.out.println("no of days from date -- " + fromDate.getTime());
		System.out.println("no of days to date -- " + toDate.getTime());
		//1 hour is added to compensate for day light savings in march
		return (int)( (toDate.getTime() - fromDate.getTime() + 1000 * 60 * 60) / (1000 * 60 * 60 * 24)) + 1;
		
	} //end of method noOfDaysBetween
	
	public static void main(String args[]) {
		
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(Calendar.MONTH, Calendar.NOVEMBER);
		fromCal.set(Calendar.DATE, 1);
		fromCal.set(Calendar.HOUR, 0);
		fromCal.set(Calendar.MINUTE, 0);
		fromCal.set(Calendar.SECOND, 0);
		System.out.println("from date -- " + fromCal.getTime());
		
		Calendar toCal = Calendar.getInstance();
		toCal.set(Calendar.MONTH, Calendar.NOVEMBER);
		toCal.set(Calendar.DATE, 30);
		toCal.set(Calendar.HOUR, 0);
		toCal.set(Calendar.MINUTE, 0);
		toCal.set(Calendar.SECOND, 0);
		System.out.println("from date -- " + toCal.getTime());
		
		System.out.println("no. of days -- " + noOfDaysBetween(fromCal.getTime(), toCal.getTime()));
		
	} //end of method main

	public CustomerBills getAllBillsPerCustomer(Long customerId, String orderway, int offset, int defaultRows) {
		return sppaDao.getAllBillsPerCustomer(customerId,orderway,offset,defaultRows);
	}
	
	public CustomerDetailedBill regenerateCustomerBill(Long customerBillId) {
		
		CustomerSppaBill bill = sppaDao.loadCustomerSppaBillById(customerBillId);
		bill.setBillStatus(BillStatus.OBSOLETE.ordinal());

		//update the em instance object to discount this obsolete consumed energy	
		List<SppaBill> emBillList = sppaDao.getEmBillsOfCustomerBill(customerBillId);
		Iterator<SppaBill> iter = emBillList.iterator();
		while(iter.hasNext()) {
			SppaBill sppaBill = iter.next();
			EmInstance emInstance = emInstanceManger.loadEmInstanceById(sppaBill.getEmInstance().getId());
			double totalConsumed = emInstance.getBlockEnergyConsumed().doubleValue();
			long noOfDays = emInstance.getTotalBilledNoOfDays();
			double savedEnergy = 0;
			if(sppaBill.getConsumedEnergy() != null) {
				savedEnergy = (sppaBill.getBaselineEnergy().doubleValue() - 
						sppaBill.getConsumedEnergy().doubleValue()) / 1000;				
			}
			emInstance.setBlockEnergyConsumed(new BigDecimal(totalConsumed - savedEnergy));
			emInstance.setTotalBilledNoOfDays(noOfDays - sppaBill.getNoOfDays());
			emInstanceManger.saveOrUpdate(emInstance);
		}
		return generateBillPerCustomer(bill.getCustomer(), bill.getBillStartDate(), bill.getBillEndDate());
		
	} //end of method regenerateCustomerBill

	public List<CustomerSppaBill> getEMBillByCustomerId(Long customerId)
	{
		return sppaDao.getEMBillsByCustomerId(customerId);
	}
} //end of class SppaManager
