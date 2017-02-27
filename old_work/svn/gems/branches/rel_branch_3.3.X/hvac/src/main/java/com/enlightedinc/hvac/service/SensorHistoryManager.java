package com.enlightedinc.hvac.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.dao.SensorHistoryDao;
import com.enlightedinc.hvac.model.SensorHistory;

@Service("sensorHistoryManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SensorHistoryManager {
	
	@Resource
	SensorHistoryDao sensorHistoryDao;

	public Object saveSensorHistory(SensorHistory sensorHistory) {	    
	    return sensorHistoryDao.saveSensorHistory(sensorHistory);
    }
	
	public Long getMaxHistoryId() {
		return sensorHistoryDao.getMaxHistoryId();
	}

}
