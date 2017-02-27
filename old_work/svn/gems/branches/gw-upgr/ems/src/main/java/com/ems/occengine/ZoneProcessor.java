package com.ems.occengine;

import java.util.Date;

import org.apache.log4j.Logger;

public class ZoneProcessor {
	
	static final Logger logger = Logger.getLogger(ZoneProcessor.class.getName());

	public static int getOnBitsCnt(ZoneEventVO zone, int totalBits) {
		if(logger.isDebugEnabled()) {
			logger.debug("Zone Name::" + zone.getName() + ", totalBits = " + totalBits);
		}
		setZoneData(zone);
		CacheManager oCacheMgr = CacheManager.getInstance();
		int arrSize = 0;		
		while(totalBits > 0) {
			arrSize++;
			totalBits -= 64;
		}
		Long [] zoneBits = new Long[arrSize];
		
		for (int a = 0; a < arrSize ; a++) {
			zoneBits[a] = 0L; 
		}
		
		int totalSensors = zone.getSensorList().size();
		int failedSensors = 0;
		for (String key: zone.getSensorList()) {
			SensorEventVO s = oCacheMgr.getSensor(key);
			SensorEventVO oSensor = null;
			if(s != null) {
				oSensor = s.clone();
			}
			if (oSensor != null) {
				SensorProcessor.tempBitAdjust(oSensor);
				for(int a = 0 ; a < arrSize; a++) {
					if(logger.isDebugEnabled()) {
						logger.debug("Zone Name::" + zone.getName() + ", Start zoneOnBits::" + a + "::" + Long.toBinaryString(zoneBits[a]));
					}
					zoneBits[a] = zoneBits[a] | SensorProcessor.getLong(oSensor, a);
					if(logger.isDebugEnabled()) {
						logger.debug("Zone Name::" + zone.getName() + ", End zoneOnBits::" + a + "::" + Long.toBinaryString(zoneBits[a]));
					}
				}
				if(oSensor.isHeartbeatFailure()) {
					failedSensors++;
				}
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Zone Name::" + zone.getName() + 
				", failedSensors = " + failedSensors + 
				", totalSensors = " + totalSensors + ", percentsensorhbfailure = " + zone.getoRule().getPercentSensorHbfailureZoneAlarm());
		}
		zone.setZoneFailure((failedSensors * 100/ totalSensors) >= zone.getoRule().getPercentSensorHbfailureZoneAlarm());
		
		
		int onBits = 0;
		int y = 64;
		for (int x = 0; x < zoneBits.length ; x++) {
			Long l =  zoneBits[x];
			if(x+1 == zoneBits.length) {
				y = totalBits + 64;
			}
			long temp = 1;
			long tempAndZone = 0;
			if(logger.isDebugEnabled()) {
				logger.debug("Zone Name::" + zone.getName() + ", y = " + y + ", l = " + Long.toBinaryString(l));
			}
			for(int i = 0; i < y; i++) {
				tempAndZone = 0;
				tempAndZone = l & temp;
				if (tempAndZone > 0) {
					onBits++;
				}
				temp = temp << 1;
				
			}
		}
		
		return onBits;
	}
	
	
	public static int getOffBitsCnt(ZoneEventVO zone, int totalBits) {
		if(logger.isDebugEnabled()) {
			logger.debug("Zone Name::" + zone.getName() + ", totalBits = " + totalBits);
		}
		setZoneData(zone);
		CacheManager oCacheMgr = CacheManager.getInstance();
		int arrSize = 0;
		while(totalBits > 0) {
			arrSize++;
			totalBits -= 64;
		}
		Long [] zoneBits = new Long[arrSize];
		
		for (int a = 0; a < arrSize ; a++) {
			zoneBits[a] = 0L; 
		}
		
		int totalSensors = zone.getSensorList().size();
		int failedSensors = 0;
		for (String key: zone.getSensorList()) {
			SensorEventVO s = oCacheMgr.getSensor(key);
			SensorEventVO oSensor = null;
			if(s != null) {
				oSensor = s.clone();
			}
			if (oSensor != null) {
				SensorProcessor.tempBitAdjust(oSensor);
				synchronized (oSensor) {
					for(int a = 0 ; a < arrSize; a++) {
						if(logger.isDebugEnabled()) {
							logger.debug("Zone Name::" + zone.getName() + ", Start zoneOffBits::" + a + "::" + Long.toBinaryString(zoneBits[a]));
						}
						zoneBits[a] = zoneBits[a] | SensorProcessor.getLong(oSensor, a);
						if(logger.isDebugEnabled()) {
							logger.debug("Zone Name::" + zone.getName() + ", End zoneOffBits::" + a + "::" + Long.toBinaryString(zoneBits[a]));
						}
					}
					if(oSensor.isHeartbeatFailure()) {
						failedSensors++;
					}
				}
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Zone Name::" + zone.getName() + 
				", failedSensors = " + failedSensors + 
				", totalSensors = " + totalSensors + ", percentsensorhbfailure = " + zone.getoRule().getPercentSensorHbfailureZoneAlarm());
		}
		zone.setZoneFailure((failedSensors * 100/ totalSensors) >= zone.getoRule().getPercentSensorHbfailureZoneAlarm());
		
		int offBits = 0;
		int y = 64;
		for (int x = 0; x < zoneBits.length ; x++) {
			Long l =  zoneBits[x];
			if(x+1 == zoneBits.length) {
				y = totalBits + 64;
			}
			long temp = 1;
			long tempAndZone = 0;
			if(logger.isDebugEnabled()) {
				logger.debug("Zone Name::" + zone.getName() + ", y = " + y + ", l = " + Long.toBinaryString(l));
			}
			for(int i = 0; i < y; i++) {
				tempAndZone = 0;
				tempAndZone = l & temp;
				if (tempAndZone == 0) {
					offBits++;
				}
				temp = temp << 1;
			}
		}
		
		return offBits;
	}
	
	public static void realtimemotionevent(ZoneEventVO zone) {
		setZoneData(zone);
		if(zone.getOccStatus() != CommandsConstants.OCCUPIED) {
			zone.setOverride(true);
			zone.setOccStatus(CommandsConstants.OCCUPIED);
			zone.setStateChangeDate(new Date());
		}
	}
	
	public static void setZoneData(ZoneEventVO zone) {
		CacheManager oCacheMgr = CacheManager.getInstance();
		int avgTemp = -100;
		int minTemp = -100;
		int maxTemp = -100;
		int avgPower = -100;
		int avgDimLevel = -100;
		int occupiedCount = 0;
		boolean first = true;
		short count = 1;
		for (String key: zone.getSensorList()) {
			SensorEventVO oSensor = oCacheMgr.getSensor(key);
			synchronized (oSensor) {
				if (oSensor != null && !oSensor.isHeartbeatFailure()) {
					if(first) {
						first = false;
						avgTemp = oSensor.getFxTemp();
						minTemp = oSensor.getFxTemp();
						maxTemp = oSensor.getFxTemp();
						avgPower = oSensor.getPower();
						avgDimLevel = oSensor.getCurrVolt();
					}
					else {
						avgTemp += oSensor.getFxTemp();
						minTemp = Math.min(oSensor.getFxTemp(), minTemp);
						maxTemp = Math.max(oSensor.getFxTemp(), maxTemp);
						avgPower += oSensor.getPower();
						avgDimLevel += oSensor.getCurrVolt();
						count++;
					}
					
					if(oSensor.getOccStatus() == 1) 
						occupiedCount += 1;
				}
			}
		}
		zone.setAvgTemp(avgTemp/count);
		zone.setAvgDimLevel(avgDimLevel/count);
		zone.setAvgPower(avgPower);
		zone.setMinTemp(minTemp);
		zone.setMaxTemp(maxTemp);
		zone.setPercentOccupancy(occupiedCount*100/count);
	}
	
	
}
