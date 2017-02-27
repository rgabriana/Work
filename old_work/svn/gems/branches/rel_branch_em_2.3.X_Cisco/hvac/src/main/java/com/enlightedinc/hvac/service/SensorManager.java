package com.enlightedinc.hvac.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.dao.HvacConfigurationDao;
import com.enlightedinc.hvac.dao.SensorDao;
import com.enlightedinc.hvac.model.HvacConfiguration;
import com.enlightedinc.hvac.model.Sensor;

@Service("sensorManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SensorManager {
	
	@Resource
	SensorDao sensorDao;
	@Resource
	private HvacConfigurationDao hvacConfigurationDao;

	public Sensor getSensorFromMac(String macAddress) {	    
	    return sensorDao.getSensorFromMac(macAddress);
    }
	
	public Sensor saveSensor(Sensor sensor) {
		return (Sensor)sensorDao.saveObject(sensor);
	}
	
	public List<Sensor> getAllSensors(){
		return sensorDao.getAllSensors();
	}
	
	public Integer getDimLevel(Long sensorId) {
		return sensorDao.getDimLevel(sensorId);
	}
	
	public Integer getTimeSinceLastOccupancy(Long sensorId) {
		return sensorDao.getTimeSinceLastOccupancy(sensorId);
	}
	
	public Integer getOccupancyStatus(Long sensorId) {
		HvacConfiguration tempConfig = hvacConfigurationDao.loadConfigByName("delay.period.in.seconds");
		Integer val = sensorDao.getTimeSinceLastOccupancy(sensorId);
		if(tempConfig != null && tempConfig.getValue() != null && val != -1) {
	        if(Integer.parseInt(tempConfig.getValue()) <= val) {
	        	return 0;
			}
			else {
				return 1;
			}
		}
		else {
			return -1;
		}
	}
	
	public Sensor getSensorById(Long id) {
		return sensorDao.getSensorById(id);
	}
	
	

}
