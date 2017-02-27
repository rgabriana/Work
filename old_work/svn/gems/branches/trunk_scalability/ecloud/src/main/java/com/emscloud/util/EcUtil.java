package com.emscloud.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.emscloud.service.FacilityManager;
import com.emscloud.model.AggregatedStatsData;
import com.emscloud.vo.AggregatedEnergyData;
import com.emscloud.vo.AggregatedSensorData;

public class EcUtil {

	public static AggregatedSensorData convertToAggregatedData(AggregatedStatsData data, String timeSpan, String target, int tzOffset) {
		
		AggregatedSensorData sensorData = new AggregatedSensorData();
		sensorData.setLevelId(data.getLevelId());
		sensorData.setTarget(target);
		if(tzOffset == 0) {
			sensorData.setTimestamp(data.getCaptureAt());
		} else {
			//covert the time to the given time zone
			Calendar dateCal = Calendar.getInstance();
			dateCal.setTime(data.getCaptureAt());
			dateCal.add(Calendar.MINUTE, tzOffset);
			sensorData.setTimestamp(dateCal.getTime());
		}
		sensorData.setAvgTemp(data.getAvgTemp());
		sensorData.setBaseEnergy(data.getBaseEnergy());
		sensorData.setEnergy(data.getEnergy());
		sensorData.setTimeSpan(timeSpan);
		sensorData.setSavedAmbEnergy(data.getAmbientSavings());
		sensorData.setSavedOccEnergy(data.getOccSavings());
		sensorData.setSavedTaskTunedEnergy(new BigDecimal(data.getTuneupSavings().doubleValue() + 
				data.getManualSavings().doubleValue()).setScale(4, RoundingMode.CEILING));
		sensorData.setMotionEvents(data.getMotionEvents());
		return sensorData;
		
	} //end of method convertToAggregatedData
	
	public static AggregatedEnergyData convertToAggregatedEnergyData(AggregatedStatsData data, String timeSpan, 
			String target, int tzOffset) {
		
		AggregatedEnergyData sensorData = new AggregatedEnergyData();
		sensorData.setLevelId(data.getLevelId());
		sensorData.setName(target);
		if(tzOffset == 0) {
			sensorData.setTimestamp(data.getCaptureAt());
		} else {
			//covert the time to the given time zone
			Calendar dateCal = Calendar.getInstance();
			dateCal.setTime(data.getCaptureAt());
			dateCal.add(Calendar.MINUTE, tzOffset);
			sensorData.setTimestamp(dateCal.getTime());
		}		
		sensorData.setBaseEnergy(data.getBaseEnergy());
		sensorData.setEnergy(data.getEnergy());
		sensorData.setTimeSpan(timeSpan);
		sensorData.setSavedAmbEnergy(data.getAmbientSavings());
		sensorData.setSavedOccEnergy(data.getOccSavings());
		sensorData.setCost(new BigDecimal(data.getCost()).setScale(6, RoundingMode.CEILING).doubleValue());
		sensorData.setSavedCost(new BigDecimal(data.getSavedCost()).setScale(6, RoundingMode.CEILING).doubleValue());
		sensorData.setSavedTaskTunedEnergy(new BigDecimal(data.getTuneupSavings().doubleValue() + 
				data.getManualSavings().doubleValue()).setScale(4, RoundingMode.CEILING));
		sensorData.setMotionEventCount(data.getMotionEvents());
		return sensorData;
		
	} //end of method convertToAggregatedData
	
	public static AggregatedSensorData convertToAggregatedData(Object[] data, String timeSpan, String target, long levelId,
			Date captureTime) {
		
		AggregatedSensorData sensorData = new AggregatedSensorData();
		sensorData.setLevelId(levelId);
		sensorData.setTarget(target);
		sensorData.setTimestamp(captureTime);
		sensorData.setAvgTemp(new Float((Double)data[13] / (Long)data[18]));				
		sensorData.setBaseEnergy((Double)data[2]);
		sensorData.setEnergy(new BigDecimal((Double)data[3]));
		sensorData.setTimeSpan(timeSpan);
		sensorData.setSavedAmbEnergy(new BigDecimal((Double)data[5]));
		sensorData.setSavedOccEnergy(new BigDecimal((Double)data[4]));
		sensorData.setSavedTaskTunedEnergy(new BigDecimal((Double)data[6] + (Double)data[7]).setScale(4, RoundingMode.CEILING));
		if(data.length == 20) {
			sensorData.setMotionEvents(((Long)data[19]));
		}
		return sensorData;
	}

	public static void convertToStatsData(AggregatedStatsData ec, long custId, long levelId, Date toDate, Object[] readings) {
  	
  	ec.setCustId(custId);
		ec.setLevelId(levelId);
		ec.setCaptureAt((Date)readings[1]);
		ec.setBaseEnergy((Double)readings[2]);
		ec.setEnergy(new BigDecimal((Double)readings[3]));
		ec.setOccSavings(new BigDecimal((Double)readings[4]));
		ec.setAmbientSavings(new BigDecimal((Double)readings[5]));
		ec.setTuneupSavings(new BigDecimal((Double)readings[6]));
		ec.setManualSavings(new BigDecimal((Double)readings[7]));		
		ec.setSavedEnergy(new BigDecimal((Double)readings[8]));
		ec.setBaseCost((Double)readings[9]);
		ec.setSavedCost((Double)readings[10]);
		ec.setCost((Double)readings[11]);
		ec.setMinTemp((Float)readings[12]);
		ec.setAvgTemp(new Float((((Double)readings[13]).doubleValue()/((Long)readings[18]).longValue())));
		ec.setMaxTemp((Float)readings[14]);
		ec.setMinAmb((Float)readings[15]);
		ec.setAvgAmb(new Float((((Double)readings[16]).doubleValue()/((Long)readings[18]).longValue())));
		ec.setMaxAmb((Float)readings[17]);
		if(readings.length == 20) {
			ec.setMotionEvents(((Long)readings[19]).longValue());
		}
		
  } //end of method convertToStatsData
	
	public static void aggregateReadingsFromDB(HashMap<Long, HashMap<Date, Object[]>> levelEnergyReadings, long emId,
			ArrayList<Object[]> emEnergyReadings, int levelType, FacilityManager facilityMgr) {
		
		Iterator<Object[]> emLevelIter = emEnergyReadings.iterator();
		while(emLevelIter.hasNext()) {				
			Object[] emReadings = emLevelIter.next();
			long levelId = ((Long)emReadings[0]).longValue();
			//get global level id corresponding to the em instance level id
			long custLevelId = facilityMgr.getCustomerLevelId(emId, levelId, levelType);
			if(custLevelId == -1) {
				continue;
			}
			Date captureTime = (Date)emReadings[1];
			if(!levelEnergyReadings.containsKey(custLevelId)) {
				//level does not exist in the map					
				HashMap<Date, Object[]> energyTimeReadings = new HashMap<Date, Object[]>();				
				levelEnergyReadings.put(custLevelId, energyTimeReadings);
			}					
			//level already exists in the map,
			HashMap<Date, Object[]> energyTimeReadings = levelEnergyReadings.get(custLevelId);				
			if(!energyTimeReadings.containsKey(captureTime)) {					
				energyTimeReadings.put(captureTime, emReadings);
			} else {
				Object[] aggData = energyTimeReadings.get(captureTime);
				aggregateEnergyObjectArrays(aggData, emReadings);
			}		
		}
		
	} //end of method aggregateReadingsFromDB
	
	private static void aggregateEnergyObjectArrays(Object[] aggData, Object[] data) {
		
		aggData[2] = ((Double)aggData[2]).doubleValue() + ((Double)data[2]).doubleValue();
		aggData[3] = ((Double)aggData[3]).doubleValue() + ((Double)data[3]).doubleValue();
		aggData[4] = ((Double)aggData[4]).doubleValue() + ((Double)data[4]).doubleValue();
		aggData[5] = ((Double)aggData[5]).doubleValue() + ((Double)data[5]).doubleValue();
		aggData[6] = ((Double)aggData[6]).doubleValue() + ((Double)data[6]).doubleValue();
		aggData[7] = ((Double)aggData[7]).doubleValue() + ((Double)data[7]).doubleValue();
		aggData[8] = ((Double)aggData[8]).doubleValue() + ((Double)data[8]).doubleValue();
		aggData[9] = ((Double)aggData[9]).doubleValue() + ((Double)data[9]).doubleValue();
		aggData[10] = ((Double)aggData[10]).doubleValue() + ((Double)data[10]).doubleValue();
		aggData[11] = ((Double)aggData[11]).doubleValue() + ((Double)data[11]).doubleValue();
		if(((Float)data[12]).floatValue() < ((Float)aggData[12]).floatValue()) { //min temperature
			aggData[12] = ((Float)data[12]).floatValue(); 							
		}
		aggData[13] = ((Double)aggData[13]).doubleValue() + ((Double)data[13]).doubleValue(); //avg temperature
		if(((Float)data[14]).floatValue() > ((Float)aggData[14]).floatValue()) { //max temperature
			aggData[14] = ((Float)data[14]).floatValue(); 							
		}
		if(((Float)data[15]).floatValue() < ((Float)aggData[15]).floatValue()) { //min ambient
			aggData[15] = ((Float)data[15]).floatValue(); 							
		}
		aggData[16] = ((Double)aggData[16]).doubleValue() + ((Double)data[16]).doubleValue(); // avg ambient
		if(((Float)data[17]).floatValue() > ((Float)aggData[17]).floatValue()) { //max ambient
			aggData[17] = ((Float)data[17]).floatValue(); 							
		}						
		aggData[18] = ((Long)aggData[18]).longValue() + ((Long)data[18]).longValue();	//no. of records
		
		if(aggData.length == 20) { //motion events
			aggData[19] = ((Long)aggData[19]).longValue() + ((Long)data[19]).longValue();
		}
		
	} //end of method aggregateEnergyObjectArrays
	
	public static void aggregateReadingsFromDB(HashMap<Date, Object[]> levelEnergyReadings, List<Object[]> emEnergyReadings) {
						
		for(Object[] emReadings:emEnergyReadings) {			
			Date captureTime = (Date)emReadings[1];
			if(!levelEnergyReadings.containsKey(captureTime)) {
				//time does not exist in the map				
				levelEnergyReadings.put(captureTime, emReadings);				
			} else {					
				//time already exists in the map,
				Object[] aggData = levelEnergyReadings.get(captureTime);					
				aggregateEnergyObjectArrays(aggData, emReadings);	
			}
		}
		
	} //end of method aggregateReadingsFromDB
	
	public static void aggregateOrgReadingsFromDB(HashMap<Long, HashMap<Date, Object[]>> levelEnergyReadings, long emId,
			ArrayList<Object[]> emEnergyReadings, long custLevelId, FacilityManager facilityMgr) {
		
		Iterator<Object[]> emLevelIter = emEnergyReadings.iterator();
		while(emLevelIter.hasNext()) {				
			Object[] emReadings = emLevelIter.next();			
			//get customer org id corresponding to the customer
			if(facilityMgr == null) {
				System.out.println("manager is null");
			}
			//Long custLevelId = facilityMgr.getOrganizationIdOfCustomer(custId);
			if(custLevelId == -1) {
				continue;
			}
			Date captureTime = (Date)emReadings[1];
			if(!levelEnergyReadings.containsKey(custLevelId)) {
				//level does not exist in the map					
				HashMap<Date, Object[]> energyTimeReadings = new HashMap<Date, Object[]>();
				energyTimeReadings.put(captureTime, emReadings);
				levelEnergyReadings.put(custLevelId, energyTimeReadings);
			} else {					
				//level already exists in the map,
				HashMap<Date, Object[]> energyTimeReadings = levelEnergyReadings.get(custLevelId);					
				if(energyTimeReadings.containsKey(captureTime)) {					
					Object[] aggData = energyTimeReadings.get(captureTime);
					aggregateEnergyObjectArrays(aggData, emReadings);					
				} else {						
					energyTimeReadings.put(captureTime, emReadings);
				}
			}
		}
		
	} //end of method aggregateOrgReadingsFromDB
	
} //end of class EcUtil
