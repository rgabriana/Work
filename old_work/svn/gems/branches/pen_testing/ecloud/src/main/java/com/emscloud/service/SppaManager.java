package com.emscloud.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.BillPaymentDao;
import com.emscloud.dao.SiteDao;
import com.emscloud.dao.SppaDao;
import com.emscloud.model.Customer;
import com.emscloud.model.CustomerBills;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Site;
import com.emscloud.model.SiteAnomaly;
import com.emscloud.model.SppaBill;
import com.emscloud.types.BillStatus;
import com.emscloud.types.SiteAnomalyType;

@Service("sppaManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SppaManager {
	
	@Resource
	private SppaDao sppaDao;
	
	@Resource
	private SiteDao siteDao;
	@Resource
	SiteManager siteManger;
	
	@Resource
	private BillPaymentDao billPmtDao;
	
	@Resource
	EmInstanceManager emInstanceManger;
	
	@Resource
	CustomerManager customerManager;
	static final Logger logger = Logger.getLogger("CloudBilling");
	public SppaManager() {
		// TODO Auto-generated constructor stub
	}
		
	public CustomerDetailedBill getBillReportCustomer(Customer customer, Date startDate, Date endDate, boolean save, CustomerSppaBill aggregateBill) {
		
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
			
		//get all the sites
		List<Site> sites = siteManger.loadSitesByCustomer(customer.getId());
		Iterator<Site> siteIter = sites.iterator();	
		
		SppaBill siteBill = null;
		double totalBaselineEnergy = 0.0;
		double totalBaseCost = 0.0;
		double totalConsumedEnergy = 0.0;
		double totalSavedCost = 0.0;
		double totalSppaCost = 0.0;
		double totalTax = 0.0;
		
		ArrayList<SppaBill> siteBills = new ArrayList<SppaBill>();
		while(siteIter.hasNext()) {
			Site site = siteIter.next();			
			Date billStartDate = calFrom.getTime();
			if(site.getBillStartDate() != null && (site.getBillStartDate().before(calTo.getTime()) ||
					site.getBillStartDate().getTime() == calTo.getTime().getTime())) {				
				if(site.getBillStartDate().after(calFrom.getTime())) {
					//partial month
					billStartDate = site.getBillStartDate();
				}
			} else {
				continue;
			}
			//generate the monthly bill for the em
			siteBill = generateSiteSppaBill(site, billStartDate, calTo, save, aggregateBill);
			if(siteBill == null) {
				//there are no Energy Managers which sppa bill enabled in this site
				continue;
			}
			siteBill.setGeoLocation(site.getGeoLocation());
			siteBill.setPoNumber(site.getPoNumber());
			siteBill.setName(site.getName());
			siteBill.setSppaPrice(site.getSppaPrice());
			siteBills.add(siteBill);
			if(siteBill.getBaseCost() != null) {
				totalBaseCost += siteBill.getBaseCost();
			}
			if(siteBill.getBaselineEnergy() != null) {
				totalBaselineEnergy += siteBill.getBaselineEnergy().doubleValue();
			}
			if(siteBill.getConsumedEnergy() != null) {
				totalConsumedEnergy += siteBill.getConsumedEnergy().doubleValue();
			}
			if(siteBill.getSavedCost() != null) {
				totalSavedCost += siteBill.getSavedCost();
			}
			if(siteBill.getSppaCost() != null) {
				totalSppaCost += siteBill.getSppaCost();
			}
			if(siteBill.getTax() != null) {
				totalTax += siteBill.getTax();
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
		
		aggregateBill.setCurrentCharges(totalSppaCost + totalTax);		
		double paymentsRcvd = billPmtDao.getPaymentRcvdFromDate(customer.getId(), customer.getLastBillGenDate());
		aggregateBill.setPaymentReceived(paymentsRcvd);
		aggregateBill.setTotalAmtDue(totalSppaCost + totalTax + customer.getPrevAmtDue() - paymentsRcvd);
		aggregateBill.setPrevAmtDue(customer.getPrevAmtDue());
		
		CustomerDetailedBill billObj = new CustomerDetailedBill();		
		billObj.setBillInvoice(aggregateBill);
		billObj.setEmBills(siteBills);
		return billObj;
		
	} //end of method getBillReportCustomer
	
	//both startDate and endDate inclusive
	public CustomerDetailedBill generateBillPerCustomer(Customer customer, Date startDate, Date endDate) {
				
		CustomerSppaBill custSppaBill = new CustomerSppaBill();
		custSppaBill.setCustomer(customer);
		custSppaBill.setBillStatus(BillStatus.INACTIVE.ordinal());
		//System.out.println("customer id-- " + customer.getId());
		custSppaBill = sppaDao.saveOrUpdate(custSppaBill);
		CustomerDetailedBill billObj = getBillReportCustomer(customer, startDate, endDate, true, custSppaBill);
		billObj.getBillInvoice().setBillStatus(BillStatus.ACTIVE.ordinal());
		custSppaBill = sppaDao.saveOrUpdate(billObj.getBillInvoice());
		billObj.setBillInvoice(custSppaBill);
		
		//save the customer previous bill amount with the new total
		Customer cust = customerManager.loadCustomerById(customer.getId());
		cust.setPrevAmtDue(custSppaBill.getTotalAmtDue());
		cust.setLastBillGenDate(custSppaBill.getBillCreationTime());
		customerManager.saveOrUpdate(cust);
		
		//run the billing validation
		validateCustomerBill(billObj.getBillInvoice().getId());		
		return billObj;
				
	} //end of method generateBillPerCustomer
	
	public CustomerDetailedBill viewCustomerBill(long custBillId) {
		
		CustomerDetailedBill billObj = new CustomerDetailedBill();
		CustomerSppaBill custSppaBill = sppaDao.loadCustomerSppaBillById(custBillId);
		billObj.setBillInvoice(custSppaBill);
		
		List<SppaBill> dbSppaBillList = sppaDao.getEmBillsOfCustomerBill(custBillId);
		Iterator<SppaBill> sppaBillIter = dbSppaBillList.iterator();	
		double totalTax = 0.0;
		ArrayList<SppaBill> sppaBillList = new ArrayList<SppaBill>();
		while(sppaBillIter.hasNext()) {
			SppaBill dbSppaBill = sppaBillIter.next();
			SppaBill sppaBill = new SppaBill();
			sppaBill.copyFrom(dbSppaBill);			
			
			Site site = siteManger.loadSiteById(dbSppaBill.getSiteId());
			sppaBill.setGeoLocation(site.getGeoLocation());
			sppaBill.setPoNumber(site.getPoNumber());
			//sppaBill.setSppaPrice(site.getSppaPrice());
			sppaBill.setName(site.getName());
			double savedEnergy = 0;
			double sppaCost = 0;
			double tax = 0;
			if(dbSppaBill.getBaselineEnergy() != null && dbSppaBill.getConsumedEnergy() != null) {
				savedEnergy = dbSppaBill.getBaselineEnergy().doubleValue() - dbSppaBill.getConsumedEnergy().doubleValue();
				sppaCost = dbSppaBill.getSppaCost();
				float sppaPrice = (float)(dbSppaBill.getSppaCost() * 1000 / savedEnergy);
				sppaBill.setSppaPrice(sppaPrice);
				tax = dbSppaBill.getTax();
			}
			sppaBill.setEnergySaved(new BigDecimal(savedEnergy));
			//sppaBill.setTax(tax);
			totalTax += tax;
			//sppaBill.setSppaCost(sppaCost);
			sppaBill.setSppaPayableDue(sppaCost + tax);
			sppaBillList.add(sppaBill);
		}
		custSppaBill.setTax(totalTax);
		custSppaBill.setPrevAmtDue(custSppaBill.getTotalAmtDue() + custSppaBill.getPaymentReceived() - custSppaBill.getCurrentCharges());
		billObj.setEmBills(sppaBillList);
		return billObj;
		
	} //end of method viewCustomerBill
	
	public List<SppaBill> getEmBiillsOfCustomerBill(long custBillId) {
		
		return sppaDao.getEmBillsOfCustomerBill(custBillId);
				
	}
	
	public List<SppaBill> getLastMonthBillPerCustomer(long custId) {
					
		return sppaDao.getLastMonthBillPerCustomer(custId);
				
	} //end of method getLastMonthBillPerCustomer
	
	
	public SppaBill generateLastMonthBillPerSite(Site site,Date startDate, Date endDate) {
		
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

//		if(!emInstance.getSppaBillEnabled() || !emInstance.getSppaEnabled()) {
//			return null;
//		}
		
		//generate the monthly bill for the site
		return generateSiteSppaBill(site, calFrom.getTime(), calTo, false, null);
		
	} //end of method generateLastMonthBillPerSite
	
	private SppaBill generateSiteSppaBill(Site site, Date fromDate, Calendar toCal, boolean save, CustomerSppaBill aggregateBill) {
		
		//get all the em instances of the site
		List<Long> emIdList = siteDao.getSiteEms(site.getId());
		Iterator<Long> emIdIter = emIdList.iterator();
		SppaBill siteBill = null;
		int noOfEms = 0;
		int billNoOfDays = noOfDaysBetween(fromDate, toCal.getTime());
		while(emIdIter.hasNext()) {
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emIdIter.next());
			if(emInst != null){
				if(!emInst.getSppaBillEnabled() || !emInst.getSppaEnabled()) {
					continue;
				}			
				noOfEms++;
				SppaBill emBill = sppaDao.getMonthlyBillData(emInst.getDatabaseName(), emInst.getReplicaServer().getInternalIp(), 
						fromDate, toCal.getTime());
				if(emBill == null || emBill.getBaselineEnergy() == null) {
					//could not get data for this energy manager
					continue;
				}
				Integer noOfEmerFixtures = emInst.getNoOfEmergencyFixtures();
				if(noOfEmerFixtures != null && noOfEmerFixtures > 0) {
					BigDecimal emergGuideLoad = emInst.getEmergencyFixturesGuidelineLoad();
					BigDecimal emergLoad = emInst.getEmergencyFixturesLoad();
					double baseEnergy = emBill.getBaselineEnergy().doubleValue();
					double baseCost = emBill.getBaseCost();
					double utilityRate = baseCost * 1000 / baseEnergy;
					double emergBaseEnergy = emergGuideLoad.doubleValue() * 24 * 1000 * billNoOfDays;	
					emBill.setEmergencyBaselineEnergy(new BigDecimal(emergBaseEnergy));
					emBill.setBaselineEnergy(new BigDecimal(baseEnergy + emergBaseEnergy));
					emBill.setBaseCost(baseCost + emergBaseEnergy * utilityRate / 1000);
					double emergEnergy = emergLoad.doubleValue() * 24 * 1000 * billNoOfDays;
					emBill.setEmergencyConsumedEnergy(new BigDecimal(emergEnergy));
					double consumEnergy = emBill.getConsumedEnergy().doubleValue();
					emBill.setConsumedEnergy(new BigDecimal(emergEnergy + consumEnergy));
					double emergCost = emergEnergy * utilityRate / 1000;
					double cost = consumEnergy * utilityRate / 1000;
					emBill.setSavedCost(emBill.getBaseCost() - cost - emergCost);				
				} else {
					emBill.setEmergencyBaselineEnergy(new BigDecimal(0));
					emBill.setEmergencyConsumedEnergy(new BigDecimal(0));
				}
				if(siteBill == null) {
					siteBill = emBill;
				} else {
					siteBill.setBaselineEnergy(new BigDecimal(siteBill.getBaselineEnergy().doubleValue() + emBill.getBaselineEnergy().doubleValue()));
					siteBill.setBaseCost(siteBill.getBaseCost() + emBill.getBaseCost());
					siteBill.setConsumedEnergy(new BigDecimal(siteBill.getConsumedEnergy().doubleValue() + emBill.getConsumedEnergy().doubleValue()));
					siteBill.setSavedCost(siteBill.getSavedCost() + emBill.getSavedCost());				
					siteBill.setEmergencyBaselineEnergy(new BigDecimal(siteBill.getEmergencyBaselineEnergy().doubleValue() + 
							emBill.getEmergencyBaselineEnergy().doubleValue()));
					siteBill.setEmergencyConsumedEnergy(new BigDecimal(siteBill.getEmergencyConsumedEnergy().doubleValue() +
							emBill.getEmergencyConsumedEnergy().doubleValue()));
				}
			}
			
		}
		if(noOfEms == 0) {
			return null;
		}
		if(siteBill == null) {
			siteBill = new SppaBill();			
		} else {
			if(siteBill.getBaselineEnergy() != null && siteBill.getConsumedEnergy() != null) {
				double savedEnergy = siteBill.getBaselineEnergy().doubleValue() - siteBill.getConsumedEnergy().doubleValue();
				double sppaCost = savedEnergy * site.getSppaPrice() / 1000;
				double tax = sppaCost * site.getTaxRate() / 100;
				siteBill.setEnergySaved(new BigDecimal(savedEnergy));
				siteBill.setTax(tax);
				siteBill.setSppaPrice(site.getSppaPrice());
				siteBill.setName(site.getName());
				siteBill.setSppaCost(sppaCost);
				siteBill.setSppaPayableDue(sppaCost+tax);
			}
		}
		siteBill.setBillCreationTime(new Date());
		siteBill.setBillStartDate(fromDate);
		siteBill.setBillEndDate(toCal.getTime());
		siteBill.setNoOfDays(billNoOfDays);	
		siteBill.setSiteId(site.getId());
		if(save) {
			//save the monthly bill in the monthly table
			if(aggregateBill != null) {
				siteBill.setCustomerBill(aggregateBill);
			}
			double blockEnergyConsumed = 0;
			double energySaved = 0;
			long noOfDays = 0;
			if(site.getBlockEnergyConsumed() != null) {
				blockEnergyConsumed = site.getBlockEnergyConsumed().doubleValue();
			}
			if(siteBill.getEnergySaved()!=null) {
				energySaved = siteBill.getEnergySaved().doubleValue();
			}
			if(site.getTotalBilledNoOfDays() != null) {
				noOfDays = site.getTotalBilledNoOfDays().longValue();
			}
			double totalConsumedEnergy =  blockEnergyConsumed +	energySaved / 1000;
			long totalNoOfDays = noOfDays + siteBill.getNoOfDays();
			double blockEnergyRemaining = site.getBlockPurchaseEnergy().doubleValue() - totalConsumedEnergy;
			siteBill.setBlockEnergyRemaining(new BigDecimal(blockEnergyRemaining));
			double timeRemaining = blockEnergyRemaining * totalNoOfDays / totalConsumedEnergy;
			siteBill.setBlockTermRemaining((long)timeRemaining);
			siteBill = sppaDao.saveOrUpdate(siteBill);			
			site.setBlockEnergyConsumed(new BigDecimal(totalConsumedEnergy));
			site.setTotalBilledNoOfDays(totalNoOfDays);
			siteManger.saveOrUpdate(site);
		}
		return siteBill;
		
	} //end of method generateSiteSppaBill
	
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
			Site site = siteManger.loadSiteById(sppaBill.getSiteId());
			double totalConsumed = site.getBlockEnergyConsumed().doubleValue();
			long noOfDays = site.getTotalBilledNoOfDays();
			double savedEnergy = 0;
			if(sppaBill.getConsumedEnergy() != null) {
				savedEnergy = (sppaBill.getBaselineEnergy().doubleValue() - 
						sppaBill.getConsumedEnergy().doubleValue()) / 1000;				
			}
			site.setBlockEnergyConsumed(new BigDecimal(totalConsumed - savedEnergy));
			site.setTotalBilledNoOfDays(noOfDays - sppaBill.getNoOfDays());
			siteManger.saveOrUpdate(site);
		}
		
		//update the customer for previous amount due
		Customer customer = customerManager.loadCustomerById(bill.getCustomer().getId());
		customer.setPrevAmtDue(customer.getPrevAmtDue() - bill.getCurrentCharges());
		customerManager.saveOrUpdate(customer);
		return generateBillPerCustomer(customer, bill.getBillStartDate(), bill.getBillEndDate());
		
	} //end of method regenerateCustomerBill

	public List<CustomerSppaBill> getLastCustomerBill(Long customerId)
	{
		return sppaDao.getLastCustomerBill(customerId);
	}
	
	public void validateCustomerBill(Long custBillId) {
		
		//get all the sites whose block term remaining more than 10 years
		CustomerSppaBill custBill = sppaDao.loadCustomerSppaBillById(custBillId);
		List<SppaBill> siteBills = sppaDao.getLongTermRemainingSiteBills(custBill.getId(), 3650);		
		Iterator<SppaBill> siteBillsIter = siteBills.iterator();
		while(siteBillsIter.hasNext()) {
			SppaBill siteBill = siteBillsIter.next();
			SiteAnomaly anomaly = new SiteAnomaly();			
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			anomaly.setDetails("" + Double.valueOf(twoDForm.format(siteBill.getBlockTermRemaining().doubleValue() / 365)) + " years");
			Site site = siteManger.loadSiteById(siteBill.getSiteId());			
			anomaly.setGeoLocation(site.getGeoLocation());
			anomaly.setIssue(SiteAnomalyType.BlockTermRemaining.getName());
			anomaly.setStartDate(custBill.getBillStartDate());
			anomaly.setEndDate(custBill.getBillEndDate());
			anomaly.setReportDate(new Date());
			siteDao.addSiteAnomaly(anomaly);
		}
		
	} //end of method validateCustomerBill
	
	public void validateSiteBilling(Site site) {
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(new Date());		
		//calTo.add(Calendar.DATE, 1);
		calTo.set(Calendar.HOUR, 0);
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		calTo.add(Calendar.DAY_OF_MONTH, -1);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(calTo.getTime());		
		calFrom.add(Calendar.DAY_OF_MONTH, -1);
		logger.info("validating site (" + site.getGeoLocation() + ") from " + calFrom.getTime().toString() + " to " + calTo.getTime().toString());
		validateSiteBilling(site, calFrom.getTime(), calTo.getTime());
		
	} //end of method validateSiteBilling
	
	public void validateSiteBilling(Site site, Date fromDate, Date toDate) {
		
		//get all the em instances of the site
		List<Long> emIdList = siteDao.getSiteEms(site.getId());
		Iterator<Long> emIdIter = emIdList.iterator();	
		HashMap<String, String> siteAnomalies = new HashMap<String, String>();
		long noOfEcRows = 0;
		long noOfSensors = 0;		
		while(emIdIter.hasNext()) {
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emIdIter.next());
			if(emInst != null){
				if(!emInst.getSppaBillEnabled() || !emInst.getSppaEnabled()) {
					continue;
				}		
				Map<String, Object> emAnomalies = sppaDao.validateSiteEm(emInst.getDatabaseName(), emInst.getReplicaServer().getInternalIp(), 
						fromDate, toDate);
				Iterator<String> anomalyIter = emAnomalies.keySet().iterator();
				while(anomalyIter.hasNext()) {
					String anomaly = anomalyIter.next();
					if(anomaly.equals(SiteAnomalyType.BaselineLoad.getName()) || anomaly.equals(SiteAnomalyType.Profile.getName()) || 
							anomaly.equals(SiteAnomalyType.Consumption.getName()) || anomaly.equals(SiteAnomalyType.Connectivity.getName())) {
						String anomalyStr = emInst.getName() + ": " + emAnomalies.get(anomaly).toString();				
						if(siteAnomalies.containsKey(anomaly)) {
							anomalyStr += ", ";
							anomalyStr += siteAnomalies.get(anomaly);
						}
						siteAnomalies.put(anomaly, anomalyStr);
					} else if(anomaly.equals(SiteAnomalyType.BurnHour.getName())) {
						Map<String, Long> burnHourMap = (Map<String, Long>)emAnomalies.get(anomaly);
						noOfEcRows += burnHourMap.get("noOfEcRows");
						noOfSensors += burnHourMap.get("noOfSensors");				
					}				
				}
			}
						
		}			
		//check for burn hours anomaly
		if(noOfSensors > 0) {
			double burnHours = noOfEcRows / noOfSensors;
			double burnHrDiff = site.getEstimatedBurnHours() - burnHours;
			if(burnHrDiff < 0) {
				burnHrDiff = -burnHrDiff;
			}
			if(burnHrDiff * 100 / site.getEstimatedBurnHours() >= 20) {
				//there is a at least 20% difference in the actual burn hours and estimated burn hours
				siteAnomalies.put(SiteAnomalyType.BurnHour.getName(), "Estimated: " + site.getEstimatedBurnHours() + ", Actual: " + burnHours);
			}
		}
		Iterator<String> siteAnomalyIter = siteAnomalies.keySet().iterator();
		while(siteAnomalyIter.hasNext()) {
			SiteAnomaly anomaly = new SiteAnomaly();
			String issue = siteAnomalyIter.next();
			anomaly.setDetails(siteAnomalies.get(issue));
			anomaly.setGeoLocation(site.getGeoLocation());
			anomaly.setIssue(issue);
			anomaly.setStartDate(fromDate);
			anomaly.setEndDate(toDate);
			anomaly.setReportDate(new Date());
			siteDao.addSiteAnomaly(anomaly);
		}
		
	} //end of method validateSiteBilling

} //end of class SppaManager
