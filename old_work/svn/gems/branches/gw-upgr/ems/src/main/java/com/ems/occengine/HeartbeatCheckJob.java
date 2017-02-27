package com.ems.occengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HeartbeatCheckJob implements Job {
	
	
	public static final Logger logger = Logger.getLogger(HeartbeatCheckJob.class.getName());
	
	static int delay = 12;
	
	public HeartbeatCheckJob() {
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		if(delay > 0) {
			logger.info("Delay.." + delay);
			delay--;
			return;
		} else {
			CacheManager oCacheMgr = CacheManager.getInstance();
			Map<Long, ZoneEventVO> zoneMap = oCacheMgr.getZoneMap();
			List<Long> zoneList = new ArrayList<Long>();
			Set<Long> keys = zoneMap.keySet();
			Set<String> suNames = new HashSet<String>();
			for(Long key: keys) {
				ZoneEventVO zone = zoneMap.get(key);
				zone.setInit((byte)1);
				zoneList.add(key);
				int failedCntsOutOf = zone.getoRule().getSensorHbfailureAlarmOutofCount();
				int failedCnts = zone.getoRule().getSensorHbfailureAlarmCount();
				List<String> suList = zone.getSensorList();
				for(String suName: suList) {
					if(suNames.add(suName)) {
						SensorEventVO sensor = oCacheMgr.getSensor(suName);
						synchronized (sensor) {
							SensorProcessor.fillMissingBits(sensor);
						}
						SensorProcessor.isHeartbeatFailure(sensor, failedCnts, failedCntsOutOf);
					}
				}
			}
			EventPublisher.getInstance().processGroupLevelOccStatus(zoneList);
		}
	}
}
