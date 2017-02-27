package com.enlightedinc.hvac.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.dao.HvacConfigurationDao;
import com.enlightedinc.hvac.dao.ZonesDao;
import com.enlightedinc.hvac.model.HvacConfiguration;
import com.enlightedinc.hvac.model.Sensor;
import com.enlightedinc.hvac.model.Zone;
import com.enlightedinc.hvac.model.ZonesSensor;

@Service("zonesManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ZonesManager {
	
	@Resource
	private ZonesDao zonesDao;
	@Resource
	private HvacConfigurationDao hvacConfigurationDao;
	
	public List<Zone> getAllZones(){
		return zonesDao.getAllZones();
	}

	public void saveZone(Zone zone) {
		zonesDao.saveObject(zone);
    }

	public List<Sensor> getAllSensorsForZone(Long zoneId) {	    
	    return zonesDao.getAllSensorsForZone(zoneId);
    }

	public Zone getZone(Long zoneId) {	    
	    return (Zone)zonesDao.getObject(Zone.class, zoneId);
    }

	public void saveZoneSensor(ZonesSensor zoneSensor) {
		zonesDao.saveObject(zoneSensor);	    
    }

	public void dissociateSensorFromZone(Long zoneId, Long sensorId) {
		zonesDao.dissociateSensorFromZone(zoneId,sensorId);
    }
	
	public Integer getTempSetBackByZoneId(Long zoneId) {
		HvacConfiguration tempConfig = hvacConfigurationDao.loadConfigByName("delay.period.in.seconds");
		if(tempConfig != null && tempConfig.getValue() != null) {
	        if(Integer.parseInt(tempConfig.getValue()) <= zonesDao.getTimeSinceLastOccupancyByZone(zoneId)) {
	        	return 3;
			}
			else {
				return 4;
			}
		}
		else {
			return 4;
		}
	}
	
	public Integer getOutageStatus(Long zoneId) {
		return zonesDao.getOutageStatus(zoneId);
	}
	
	public Integer getAvgDimLevel(Long zoneId) {
		return zonesDao.getAvgDimLevel(zoneId);
	}
	
	public Integer getAvgTemperature(Long zoneId) {
		return zonesDao.getAvgTemperature(zoneId);
	}
	
	public Integer getTotalPower(Long zoneId) {
		return zonesDao.getTotalPower(zoneId);
	}

}
