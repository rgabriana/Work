package com.emscloud.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.SppaDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.SppaBill;

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
		
	//both startDate and endDate inclusive
	public SppaBill generateLastMonthBillPerCustomer(long custId, Date startDate, Date endDate) {
		
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
		
		while(emIter.hasNext()) {
			EmInstance emInst = emIter.next();			
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			//generate the monthly bill for the em
			emBill = generateEmSppaBill(emInst, calFrom, calTo);			
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
		}
		
		SppaBill aggregateBill = new SppaBill();		
		aggregateBill.setBillCreationTime(new Date());
		aggregateBill.setBillEndDate(calTo.getTime());
		aggregateBill.setBillStartDate(calFrom.getTime());
		aggregateBill.setNoOfDays(noOfDaysBetween(calFrom.getTime(), calTo.getTime()));
		aggregateBill.setBaseCost(totalBaseCost);
		aggregateBill.setBaselineEnergy(new BigDecimal(totalBaselineEnergy));
		aggregateBill.setConsumedEnergy(new BigDecimal(totalConsumedEnergy));		
		aggregateBill.setSavedCost(totalSavedCost);
		aggregateBill.setSppaCost(totalSppaCost);
		return aggregateBill;
		
	} //end of method generateLastMonthBillPerCustomer
	
	public List<SppaBill> getLastMonthBillPerCustomer(long custId) {
		
			
		return sppaDao.getLastMonthBillPerCustomer(custId);
		
		
	} //end of method getLastMonthBillPerCustomer
	
	
	public SppaBill generateLastMonthBillPerSite(long emId,Date startDate, Date endDate) {
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(endDate);		
		calTo.add(Calendar.DATE, 1);
		calTo.set(Calendar.HOUR, 0);
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(startDate);		
		calFrom.set(Calendar.HOUR, 0);
		calFrom.set(Calendar.MINUTE, 0);
		calFrom.set(Calendar.SECOND, 0);
		
		//get the em instance
		EmInstance emInstance = emInstanceManger.loadEmInstanceById(emId);
		if(!emInstance.getSppaEnabled()) {
			return null;
		}
		
		//generate the monthly bill for the em
		return generateEmSppaBill(emInstance, calFrom, calTo);
		
	} //end of method generateLastMonthBillPerSite
	
	private SppaBill generateEmSppaBill(EmInstance emInst, Calendar fromCal, Calendar toCal) {
		
		SppaBill bill = sppaDao.getMonthlyBillData(emInst.getDatabaseName(), emInst.getReplicaServer().getInternalIp(), 
				fromCal.getTime(), toCal.getTime());
		if(bill == null) {
			bill = new SppaBill();			
		} else {
			if(bill.getBaselineEnergy() != null && bill.getConsumedEnergy() != null) {
				double savedEnergy = bill.getBaselineEnergy().doubleValue() - bill.getConsumedEnergy().doubleValue();
				double sppaCost = savedEnergy * emInst.getSppaPrice() / 1000;
				double tax = sppaCost * emInst.getTaxRate();
				bill.setTax(tax);
				bill.setSppaCost(sppaCost);
				/* TODO this may be moved to em_instance itself no need to do in sppa bill
				if(emInst.getBlockEnergyRemaining() != null) {					
					bill.setBlockEnergyRemaining(new BigDecimal(emInst.getBlockEnergyRemaining().doubleValue() - 
							bill.getConsumedEnergy().doubleValue()));
					//to do block term remaining
				} */
			}
		}
		bill.setBillCreationTime(new Date());
		bill.setBillStartDate(fromCal.getTime());
		bill.setBillEndDate(toCal.getTime());
		bill.setNoOfDays(noOfDaysBetween(fromCal.getTime(), toCal.getTime()));
		bill.setEmInstance(emInst);
		//save the monthly bill in the monthly table
		bill = sppaDao.saveOrUpdate(bill);
		return bill;
		
	} //end of method generateEmSppaBill	
	
	private int noOfDaysBetween(Date fromDate, Date toDate) {
		
		return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
		
	} //end of method noOfDaysBetween
	
	public static void main(String args[]) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		System.out.println("current month -- " + cal.get(Calendar.MONTH));
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 3);
		System.out.println("current month -- " + cal.get(Calendar.MONTH));
		
	} //end of method main

} //end of class SppaManager
