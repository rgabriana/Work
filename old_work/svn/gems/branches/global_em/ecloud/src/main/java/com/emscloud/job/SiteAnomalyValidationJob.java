package com.emscloud.job;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.model.Site;
import com.emscloud.service.SiteManager;
import com.emscloud.service.SppaManager;

public class SiteAnomalyValidationJob implements Job {

	@Resource
	SppaManager sppaManager;
	@Resource
	SiteManager siteManager;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		System.out.println("Validate Site Anomaly Job Called");
		long startTime = System.currentTimeMillis();
  	List<Site> sitesList = siteManager.loadSitesByCustomer(150);  	
  	if(sitesList == null || sitesList.size() == 0) { 
  		System.out.println("No Sites found - " + 150);
  		return;
  	}    	
  	Iterator<Site> siteIter = sitesList.iterator();
  	while(siteIter.hasNext()) {
  		sppaManager.validateSiteBilling(siteIter.next());
  	}    	    	
  	System.out.println("time taken to all validate all sites -- " + (System.currentTimeMillis() - startTime));
  	
	} //end of method execute

} //end of class SiteAnomalyValidationJob
