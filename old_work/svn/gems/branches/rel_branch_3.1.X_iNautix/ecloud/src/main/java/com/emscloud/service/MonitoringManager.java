package com.emscloud.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmHealthMonitorDao;
import com.emscloud.dao.EmInstanceDao;
import com.emscloud.model.EmHealthMonitor;
import com.emscloud.model.EmInstance;


@Service("monitoringManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MonitoringManager {
	
	@Resource
	EmInstanceDao emInstanceDao;
	
	@Resource
	EmHealthMonitorDao emHealthMonitorDao;
	
	public void updateHealthMonitor(String mac, int totalGW, int uoGW, int cGW, int totalSensors, int uoSensors, int criticalSensors){
		
		EmInstance emInstance = emInstanceDao.getEmInstanceForMac(mac);		
		EmHealthMonitor healthMonitor = new EmHealthMonitor();
		healthMonitor.setCaptureAt(new Date());
		healthMonitor.setEmInstance(emInstance);
		healthMonitor.setGatewaysTotal(totalGW);
		healthMonitor.setGatewaysUnderObservation(uoGW);
		healthMonitor.setGatewaysCritical(cGW);
		healthMonitor.setSensorsTotal(totalSensors);
		healthMonitor.setSensorsUnderObservation(uoSensors);
		healthMonitor.setSensorsCritical(criticalSensors);					
		emHealthMonitorDao.saveObject(healthMonitor);
	}

}
