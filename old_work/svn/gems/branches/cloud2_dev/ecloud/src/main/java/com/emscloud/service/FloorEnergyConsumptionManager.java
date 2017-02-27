package com.emscloud.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.FloorEnergyConsumptionDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.FloorEnergyConsumptionDaily;
import com.emscloud.model.FloorEnergyConsumptionHourly;
import com.emscloud.model.ReplicaServer;

@Service("floorEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorEnergyConsumptionManager {
	
	@Resource
	private FloorEnergyConsumptionDao floorEcDao;
	
	@Resource
	ReplicaServerManager replicaServerManager ;
	
	@Resource
	EmInstanceManager emInstanceManger;
	
	private static Timer aggregateTimer = null;
	private static int aggregateInterval = 60 * 60 * 1000; // 60 minutes
  
	public class HourlyTask extends TimerTask {

    public void run() {

        try {
            // run the stored procedure for the previous hour
            Calendar cal = Calendar.getInstance();
            Date toDate = DateUtils.truncate(cal.getTime(), Calendar.HOUR);            
      	    int currHour = cal.get(Calendar.HOUR);
      	
            // call the hourly energy consumption stored procedure
      	    aggregateFloorHourlyEnergyReadings(toDate);
            if (currHour == 0) { // midnight
            	// call the daily energy consumption stored procedure             
            	aggregateFloorDailyEnergyReadings(toDate);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method run

	} // end of class HourlyTask
	
	private void startHourlyTask() {
		
    new Thread() {
      public void run() {

      	while (true) {
      		try {
      			Calendar cal = Calendar.getInstance();
      	    int currMin = cal.get(Calendar.MINUTE);
      			if (currMin == 7) { // giving 7 minutes to collect all the
      				HourlyTask hourlyTask = new HourlyTask();
      				aggregateTimer = new Timer("Aggregate Timer", true);
      				aggregateTimer.scheduleAtFixedRate(hourlyTask, aggregateInterval, aggregateInterval);
      				return;
      			}
      			Thread.sleep(30 * 1000); // sleep for 30 sec
      		} catch (Exception ex) {
      			ex.printStackTrace();
      		}
      	}

      } // end of method run
    }.start();

	} //end of method startHourlyTask
	
	public FloorEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub
		if(aggregateTimer == null) {
			startHourlyTask();
		}
		
	}
		
	public static void main(String args[]) {
						
	} //end of method main
	
	public void aggregateFloorHourlyEnergyReadings(Date toDate) {
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(toDate);		
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(calTo.getTime());
		calFrom.add(Calendar.HOUR, -1);
		System.out.println("from date -- " + calFrom.getTime());
		aggregateFloorHourlyEnergyReadings(50, calFrom.getTime(), toDate);
		
	} //end of method aggregateFloorHourlyEnergyReadings
	
	public void aggregateFloorDailyEnergyReadings(Date toDate) {
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(toDate);		
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(calTo.getTime());
		calFrom.add(Calendar.DAY_OF_MONTH, -1);
		aggregateFloorDailyEnergyReadings(50, calFrom.getTime(), toDate);
		
	} //end of method aggregateFloorDailyEnergyReadings
	
	public void aggregateFloorHourlyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getFloorHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			System.out.println("no. of rows -- " + emEnergyReadings.size());
			Iterator<Object[]> emFloorIter = emEnergyReadings.iterator();
			while(emFloorIter.hasNext()) {				
				Object[] emReadings = emFloorIter.next();
				long floorId = ((Long)emReadings[0]).longValue();
				//get customer floor id corresponding to the em instance floor id
				long custFloorId = floorEcDao.getCustomerFloorId(emInst.getId(), floorId);
				System.out.println("customer floor id = " + custFloorId + " for em floorid -- " + emInst.getId() + "  " + floorId);
				if(custFloorId == -1) {
					continue;
				}
				Date captureTime = (Date)emReadings[1];
				if(!floorEnergyReadings.containsKey(custFloorId)) {
					//floor does not exist in the map					
					HashMap<Date, Object[]> energyTimeReadings = new HashMap<Date, Object[]>();
					energyTimeReadings.put(captureTime, emReadings);
					floorEnergyReadings.put(custFloorId, energyTimeReadings);
				} else {					
					//floor already exists in the map,
					HashMap<Date, Object[]> energyTimeReadings = floorEnergyReadings.get(custFloorId);					
					if(energyTimeReadings.containsKey(captureTime)) {					
						Object[] floorReadings = energyTimeReadings.get(captureTime);
						floorReadings[2] = ((Double)floorReadings[2]).doubleValue() + ((Double)emReadings[2]).doubleValue();
						floorReadings[3] = ((Double)floorReadings[3]).doubleValue() + ((Double)emReadings[3]).doubleValue();
						floorReadings[4] = ((Double)floorReadings[4]).doubleValue() + ((Double)emReadings[4]).doubleValue();
						floorReadings[5] = ((Double)floorReadings[5]).doubleValue() + ((Double)emReadings[5]).doubleValue();
						floorReadings[6] = ((Double)floorReadings[6]).doubleValue() + ((Double)emReadings[6]).doubleValue();
						floorReadings[7] = ((Double)floorReadings[7]).doubleValue() + ((Double)emReadings[7]).doubleValue();
					} else {						
						energyTimeReadings.put(captureTime, emReadings);
					}
				}
			}
		}
		
		System.out.println("no. of floors -- " + floorEnergyReadings.size());
		Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
		while(floorReadingsIter.hasNext()) {
			Long floorId = floorReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			System.out.println("no. of time rows for floor " + floorId + "  ---- " +timeReadings.size());
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				FloorEnergyConsumptionHourly ec = new FloorEnergyConsumptionHourly();
				ec.setCustId(custId);
				ec.setFloorId(floorId);
				ec.setCaptureAt((Date)readings[1]);
				ec.setBasePowerUsed(new BigDecimal((Double)readings[2]));
				ec.setPowerUsed(new BigDecimal((Double)readings[3]));
				ec.setOccSavings(new BigDecimal((Double)readings[4]));
				ec.setAmbientSavings(new BigDecimal((Double)readings[5]));
				ec.setTuneupSavings(new BigDecimal((Double)readings[6]));
				ec.setManualSavings(new BigDecimal((Double)readings[7]));			
				System.out.println("saving -- " + ec.getCaptureAt());
				floorEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateFloorHourlyEnergyReadings

	public void aggregateFloorDailyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getFloorDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			Iterator<Object[]> emFloorIter = emEnergyReadings.iterator();
			while(emFloorIter.hasNext()) {				
				Object[] emReadings = emFloorIter.next();
				long floorId = ((Long)emReadings[0]).longValue();
				//get customer floor id corresponding to the em instance floor id
				long custFloorId = floorEcDao.getCustomerFloorId(emInst.getId(), floorId);
				if(custFloorId == -1) {
					continue;
				}
				Date captureTime = (Date)emReadings[1];
				if(!floorEnergyReadings.containsKey(custFloorId)) {
					//floor does not exist in the map					
					HashMap<Date, Object[]> energyTimeReadings = new HashMap<Date, Object[]>();
					energyTimeReadings.put(captureTime, emReadings);
					floorEnergyReadings.put(custFloorId, energyTimeReadings);
				} else {					
					//floor already exists in the map,
					HashMap<Date, Object[]> energyTimeReadings = floorEnergyReadings.get(custFloorId);					
					if(energyTimeReadings.containsKey(captureTime)) {					
						Object[] floorReadings = energyTimeReadings.get(captureTime);
						floorReadings[2] = ((Double)floorReadings[2]).doubleValue() + ((Double)emReadings[2]).doubleValue();
						floorReadings[3] = ((Double)floorReadings[3]).doubleValue() + ((Double)emReadings[3]).doubleValue();
						floorReadings[4] = ((Double)floorReadings[4]).doubleValue() + ((Double)emReadings[4]).doubleValue();
						floorReadings[5] = ((Double)floorReadings[5]).doubleValue() + ((Double)emReadings[5]).doubleValue();
						floorReadings[6] = ((Double)floorReadings[6]).doubleValue() + ((Double)emReadings[6]).doubleValue();
						floorReadings[7] = ((Double)floorReadings[7]).doubleValue() + ((Double)emReadings[7]).doubleValue();
					} else {						
						energyTimeReadings.put(captureTime, emReadings);
					}
				}
			}
		}
		
		Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
		while(floorReadingsIter.hasNext()) {
			Long floorId = floorReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				FloorEnergyConsumptionDaily ec = new FloorEnergyConsumptionDaily();
				ec.setCustId(custId);
				ec.setFloorId(floorId);
				ec.setCaptureAt((Date)readings[1]);
				ec.setBasePowerUsed(new BigDecimal((Double)readings[2]));
				ec.setPowerUsed(new BigDecimal((Double)readings[3]));
				ec.setOccSavings(new BigDecimal((Double)readings[4]));
				ec.setAmbientSavings(new BigDecimal((Double)readings[5]));
				ec.setTuneupSavings(new BigDecimal((Double)readings[6]));
				ec.setManualSavings(new BigDecimal((Double)readings[7]));			
				floorEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateFloorDailyEnergyReadings
	
} //end of class SppaManager
