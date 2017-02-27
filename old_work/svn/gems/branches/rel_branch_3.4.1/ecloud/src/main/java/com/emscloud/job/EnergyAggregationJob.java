package com.emscloud.job;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.emscloud.action.SpringContext;
import com.emscloud.model.Customer;
import com.emscloud.service.BldEnergyConsumptionManager;
import com.emscloud.service.CampusEnergyConsumptionManager;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.OrganizationEnergyConsumptionManager;
import com.emscloud.service.FloorEnergyConsumptionManager;

public class EnergyAggregationJob implements Job {
	
	private static final Logger logger = Logger.getLogger("StatsAgg");

	private FloorEnergyConsumptionManager floorEnergyManager;
	private BldEnergyConsumptionManager bldEnergyManager;
	private CampusEnergyConsumptionManager campusEnergyManager;
	private OrganizationEnergyConsumptionManager organizationEnergyManager;
	private CustomerManager custManager;
		
	private AggTask aggTask = null;
	
	public EnergyAggregationJob() {
		
		floorEnergyManager = (FloorEnergyConsumptionManager)SpringContext.getBean("floorEnergyConsumptionManager");
		bldEnergyManager = (BldEnergyConsumptionManager)SpringContext.getBean("bldEnergyConsumptionManager");
		campusEnergyManager = (CampusEnergyConsumptionManager)SpringContext.getBean("campusEnergyConsumptionManager");
		organizationEnergyManager = (OrganizationEnergyConsumptionManager)SpringContext.getBean("organizationEnergyConsumptionManager");
		custManager = (CustomerManager)SpringContext.getBean("customerManager");
		
		if(aggTask == null) {
			startAggregateTask();
		}
				
	} //end of constructor
	
	private void scheduleAggJob() {
		
		long aggJobTime = System.currentTimeMillis();		
		try {    		
  		Calendar calTo = Calendar.getInstance();
  		calTo.set(Calendar.MINUTE, 0);
  		calTo.set(Calendar.SECOND, 0);
  		calTo.set(Calendar.MILLISECOND, 0);
  		int currHour = calTo.get(Calendar.HOUR);
  		
  		Calendar calFromHourly = Calendar.getInstance();
  		calFromHourly.setTime(calTo.getTime());
  		calFromHourly.add(Calendar.HOUR, -1);
  		calFromHourly.set(Calendar.MILLISECOND, 0);
  		
  		Calendar calFromDaily = Calendar.getInstance();
  		calFromDaily.setTime(calTo.getTime());
  		calFromDaily.add(Calendar.DAY_OF_MONTH, -1);
  		
  		//get all customer ids
  		List<Customer> custList = custManager.loadallCustomer();
  		Iterator<Customer> custIter = custList.iterator();
  		while(custIter.hasNext()) {
  			Customer cust = custIter.next();
  			// call the hourly energy consumption stored procedure
  			floorEnergyManager.aggregateFloorHourlyEnergyReadings(cust.getId(), calFromHourly.getTime(), calTo.getTime());
  			bldEnergyManager.aggregateBldHourlyEnergyReadings(cust.getId(), calFromHourly.getTime(), calTo.getTime());
  			campusEnergyManager.aggregateCampusHourlyEnergyReadings(cust.getId(), calFromHourly.getTime(), calTo.getTime());
  			organizationEnergyManager.aggregateOrganizationHourlyEnergyReadings(cust.getId(), calFromHourly.getTime(), calTo.getTime());
  			if(logger.isDebugEnabled()) {
  				logger.debug("agg job time after hourly: " + (System.currentTimeMillis() - aggJobTime));
  			}
  			if (currHour == 0) { // midnight 				
    			// call the daily energy consumption stored procedure             
    			floorEnergyManager.aggregateFloorDailyEnergyReadings(cust.getId(), calFromDaily.getTime(), calTo.getTime());
    			bldEnergyManager.aggregateBldDailyEnergyReadings(cust.getId(), calFromDaily.getTime(), calTo.getTime());
    			campusEnergyManager.aggregateCampusDailyEnergyReadings(cust.getId(), calFromDaily.getTime(), calTo.getTime());
    			organizationEnergyManager.aggregateOrganizationDailyEnergyReadings(cust.getId(), calFromDaily.getTime(), calTo.getTime());
    		}
  			//aggregate for 15 min data  			
  			aggregate15minEnergyReadings(cust.getId(), calFromHourly.getTime(), calTo.getTime());
  		}  		
  	} catch (Exception ex) {
  		ex.printStackTrace();
  	}
		if(logger.isDebugEnabled()) {
			logger.debug("agg job time: " + (System.currentTimeMillis() - aggJobTime));
		}
		
	} //end of method scheduleAggJob
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobKey jobKey = context.getJobDetail().getKey();
		scheduleAggJob();	
		
	} //end of method execute
	
	public void aggregate15minEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		long aggTime = System.currentTimeMillis();
		Date startDate = fromDate;
		Calendar calEnd = Calendar.getInstance();		
		while((toDate.getTime() - startDate.getTime()) >= 15 * 60 * 1000) {
			calEnd.setTime(startDate);
			calEnd.add(Calendar.MINUTE, 15);
			floorEnergyManager.aggregateFloor15minEnergyReadings(custId, startDate, calEnd.getTime());
			bldEnergyManager.aggregateBld15minEnergyReadings(custId, startDate, calEnd.getTime());
			campusEnergyManager.aggregateCampus15minEnergyReadings(custId, startDate, calEnd.getTime());
			organizationEnergyManager.aggregateOrganization15minEnergyReadings(custId, startDate, calEnd.getTime());
			startDate = calEnd.getTime();
		}
		logger.debug("15 min agg time - " + (System.currentTimeMillis() - aggTime));
		
	} //end of method aggregate15minEnergyReadingsTimeRange
	
	public class AggTask extends TimerTask {

    public void run() {

    	try {
    		scheduleAggJob();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    } // end of method run

  } // end of class AggTask
	
	private Timer aggTimer = new Timer("Aggregation Timer", true);
	
	private void startAggregateTask() {

    new Thread() {
    	public void run() {

    		synchronized(AggTask.class) {
    			if(aggTask != null) {
    				return;
    			}
    			while (true) {
    				try {
    					Calendar cal = Calendar.getInstance();
    					int currMin = cal.get(Calendar.MINUTE);
    					if (currMin == 10) { // start at the 10th minute
    						if(logger.isDebugEnabled()) {
    							logger.debug("starting the aggregation task");
    						}
    						AggTask aggTask = new AggTask();
    						aggTimer.scheduleAtFixedRate(aggTask, 0, 60 * 60 * 1000);
    						return;
    					}    		   
    					Thread.sleep(1000); // sleep for 1 sec
    				} catch (Exception ex) {
    					ex.printStackTrace();
    				}
    			}
    		}
    		
    	} // end of method run
    }.start();

	} // end of method startAggregateTask
	
	public static void main(String args[]) {
		
		Calendar calTo = Calendar.getInstance();
		System.out.println("current date -- " + calTo.getTime());
		
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		System.out.println("current date -- " + calTo.getTime());
		
	}
	
} //end of class EnergyAggregationJob
