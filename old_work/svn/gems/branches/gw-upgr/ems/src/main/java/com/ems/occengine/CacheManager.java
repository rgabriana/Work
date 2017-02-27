/**
 * 
 */
package com.ems.occengine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CacheManager {
	private final static CacheManager m_instance = new CacheManager();
	private Map<String, SensorEventVO> oSUMap = new ConcurrentHashMap<String, SensorEventVO>();
	private Map<Long, ZoneEventVO> oZoneMap = new ConcurrentHashMap<Long, ZoneEventVO>();

	private CacheManager() {
	}

	public static CacheManager getInstance() {
		return m_instance;
	}
	
	public Map<String, SensorEventVO> getSUMap() {
		return oSUMap;
	}

	public SensorEventVO getSensor(String sName) {
		return oSUMap.get(sName);
	}

	public void addSensor(String sName, SensorEventVO oSensor) {
		oSUMap.put(sName, oSensor);
	}

	public Map<Long, ZoneEventVO> getZoneMap() {
		return oZoneMap;
	}

	public ZoneEventVO getZone(Long zoneId) {
		return oZoneMap.get(zoneId);
	}

	public void addZone(Long zoneId, ZoneEventVO oZone) {
		oZoneMap.put(zoneId, oZone);
	}

	public boolean deleteZone(Long zoneId) {
		ZoneEventVO zoneEventVO =  oZoneMap.get(zoneId);
		if(zoneEventVO != null) {
			// first remove the zone sensor association
			for (String sensor : zoneEventVO.getSensorList()) {
				SensorEventVO sensorEventVO = getSensor(sensor);
				if(sensorEventVO.getZoneList().contains(zoneId)) {
					int index = -1;
					for(int i = 0; i < sensorEventVO.getZoneList().size(); i++) {
						index = i;
						if(sensorEventVO.getZoneList().get(i).longValue() == zoneId.longValue()) {
							break;
						}
					}
					if(index > -1) {
						sensorEventVO.getZoneList().remove(index);
					}
				}
			}
			oZoneMap.remove(zoneId);
		}
		return false;
	}
}
