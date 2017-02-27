package com.ems.task;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.service.CompanyManager;

public class GenerateTimeZoneCache implements Runnable{

	private static Logger syslog = Logger.getLogger("SysLog");
	@Override
	public void run() {
		try {
			syslog.info("Started Generating TimeZones  list in a cache...");
			final CompanyManager companyManager = (CompanyManager) SpringContext.getBean("companyManager");
			if(companyManager == null){
				syslog.error("ERROR FOUND: Not able to generate TimeZone cache at the start. Company Manager is null");
			}else{
				companyManager.getTimezoneList();
			}
		} catch (Exception e) {
			syslog.error("ERROR Occured in generating TimeZone Cache:",e);
		}
		
	}

}
