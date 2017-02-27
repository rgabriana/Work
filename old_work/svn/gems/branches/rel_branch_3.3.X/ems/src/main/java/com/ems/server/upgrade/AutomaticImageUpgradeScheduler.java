package com.ems.server.upgrade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.server.util.ServerUtil;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.types.DeviceType;

public class AutomaticImageUpgradeScheduler {
	
	private static AutomaticImageUpgradeScheduler instance = null;
	
	private FirmwareUpgradeScheduleManager firmwareUpgradeScheduleMgr;
	
  private static final Logger logger = Logger.getLogger("ImageUpgrade");  
	private static Thread firmwareMaintenanceDaemon = null;
			
	private AutomaticImageUpgradeScheduler() {
	
		firmwareUpgradeScheduleMgr = (FirmwareUpgradeScheduleManager) SpringContext.getBean("firmwareUpgradeScheduleManager");
		firmwareMaintenanceDaemon = new FirmwareMaintenanceDaemon();
		firmwareMaintenanceDaemon.start();
		
	} //end of constructor
		
  public static AutomaticImageUpgradeScheduler getInstance() {
		 
    if(instance == null) {
    	synchronized(AutomaticImageUpgradeScheduler.class) {
    		if(instance == null) {
    			instance = new AutomaticImageUpgradeScheduler();
    		}
    	}
    }
    return instance;
		 
	} //end of method getInstance
	 
  public class FirmwareMaintenanceDaemon extends Thread {
  
  	public FirmwareMaintenanceDaemon() {
  		
  		setDaemon(true);
  		
  	} //end of constructor
  	
  	public void run() {
  		
  		while(true) {
  			try {
  				//get all active upgrade schedules from the database
  				List<FirmwareUpgradeSchedule> activeScheduleList = firmwareUpgradeScheduleMgr.getAllActiveFirwareSchedules();
  				Iterator<FirmwareUpgradeSchedule> iter = activeScheduleList.iterator();
  				while(iter.hasNext()) {
  					FirmwareUpgradeSchedule schedule = iter.next();
  					if(schedule.getStartTime() == null) {
  						//continue; this is active but not scheduled properly
  					}
  					if(!schedule.getStartTime().before(new Date())) {
  						//schedule's maintenance window time is not elapsed
  						continue;
  					}
  					logger.info(schedule.getFileName() + " scheduled");
  					//schedule the job
  					ImageUpgradeDBJob fixtureJob = new ImageUpgradeDBJob();
  			    fixtureJob.setDeviceType(DeviceType.Fixture.getName());
  			    fixtureJob.setExcludeList(schedule.getExcludeList());
  			    fixtureJob.setIncludeList(schedule.getIncludeList());  			    		    
  			    fixtureJob.setImageName(schedule.getFileName());    
  			    fixtureJob.setRetryInterval(schedule.getRetryInterval());
  			    fixtureJob.setNoOfRetries(schedule.getRetries());
  			    fixtureJob.setVersion(schedule.getVersion());
  			    if(schedule.getDuration() > 0) {
  			    	Calendar toCal = Calendar.getInstance();
  			    	toCal.setTime(schedule.getStartTime());
  			    	toCal.add(Calendar.MINUTE, schedule.getDuration());
  			    	fixtureJob.setStopTime(toCal.getTime());
  			    } else {
  			    	fixtureJob.setStopTime(null); //no stop time run to completion
  			    }
  			    fixtureJob.setJobName(schedule.getJobPrefix() + "_" + System.currentTimeMillis());
  			    final ArrayList<ImageUpgradeDBJob> jobList = new ArrayList<ImageUpgradeDBJob>();
  			    jobList.add(fixtureJob);
  			    new Thread("Sensor ImageUpgrade") {
  						public void run() {
  							ImageUpgradeSO.getInstance().startDeviceImageUpgrade(jobList);
  						}
  					}.start();
  			   
  					//update the maintenance window to next day
  					Calendar startCal = Calendar.getInstance();
  					startCal.add(Calendar.DATE, 1);
  					schedule.setStartTime(startCal.getTime());
  					firmwareUpgradeScheduleMgr.modifyFirmwareUpgradeSchedule(schedule);  					
  				}
  				ServerUtil.sleep(60);
  			}
  			catch(Exception e) {
  				e.printStackTrace();
  			}
  		}
  	} //end of method run
  	
  } //end of class FirmwareMainenanceDaemon
  
} //end of class AutomaticImageUpgradeScheduler
