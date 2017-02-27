package com.emscloud.job;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.emscloud.action.SpringContext;
import com.emscloud.model.Site;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SiteManager;
import com.emscloud.service.SppaManager;
import com.emscloud.vo.Organization;

public class SiteAnomalyValidationJob implements Job {

	@Resource
	SppaManager sppaManager;
	@Resource
	SiteManager siteManager;
	@Resource
	CustomerManager custManager;
	
	static final Logger logger = Logger.getLogger("CloudBilling");
	
	public SiteAnomalyValidationJob()
	{
		siteManager = (SiteManager)SpringContext.getBean("siteManager");
		sppaManager = (SppaManager)SpringContext.getBean("sppaManager");
		custManager =  (CustomerManager)SpringContext.getBean("customerManager");
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		JobKey jobKey = arg0.getJobDetail().getKey();
		logger.info("Validate Site Anomaly Job Called" +" : Jobkey :"+ jobKey + " executing at " + new Date());
		List<Organization> orgList = custManager.getAllOrganizations();
		Iterator<Organization> iter = orgList.iterator();
		while(iter.hasNext()) {
			try {
				validateSitesOfCustomer(iter.next().getId());
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
  	
	} //end of method execute
	
	private void validateSitesOfCustomer(long custId) {
		
		long startTime = System.currentTimeMillis();
  	List<Site> sitesList = siteManager.loadSitesByCustomer(custId);  	
  	if(sitesList == null || sitesList.size() == 0) { 
  		logger.debug("No Sites found - " + custId);
  		return;
  	}    	
  	Iterator<Site> siteIter = sitesList.iterator();
  	while(siteIter.hasNext()) {
  		sppaManager.validateSiteBilling(siteIter.next());
  	}    	    	
  	logger.info("time taken to all validate all sites of customer (" + custId + " -- " + (System.currentTimeMillis() - startTime));
  	
	} //end of method validateSitesOfCustoemr

} //end of class SiteAnomalyValidationJob
