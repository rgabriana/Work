package com.enlightedinc.hvac.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.dao.SensorDao;
import com.enlightedinc.hvac.model.Sensor;

@Service("sensorManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SensorManager {
	
	@Resource
	SensorDao sensorDao;

	public Sensor getSensorFromMac(String macAddress) {	    
	    return sensorDao.getSensorFromMac(macAddress);
    }
	
	public Sensor saveSensor(Sensor sensor) {
		return (Sensor)sensorDao.saveObject(sensor);
	}

}
