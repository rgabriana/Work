package com.ems.occengine;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;

import com.ems.server.SchedulerManager;

public class OccupancyEngine {
	
	private ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]> (2000);
	
	private static OccupancyEngine instance = new OccupancyEngine();
	
	private OccupancyEngine() {
	}
	
	public static OccupancyEngine getInstance() {
		return instance;
	}
	
	public Boolean addPacket(byte[] packet) {
		return queue.add(packet);
	}

	public ArrayBlockingQueue<byte[]> getQueue() {
		return queue;
	}
	
	public Boolean addZone(Long id, String name, List<String> sensors) {
		ZoneEventVO oZone = new ZoneEventVO();
		oZone.setId(id);
		oZone.setName(name);
		oZone.setSensorList(sensors);
		oZone.setoRule(new Rules());
		CacheManager.getInstance().addZone(id, oZone);
		List<String> oSensorList = oZone.getSensorList();
		Iterator<String> itrSensor = oSensorList.iterator();
		while (itrSensor.hasNext()) {
			String sensorName = itrSensor.next();
			SensorEventVO oSensor = CacheManager.getInstance().getSensor(sensorName);
			if (oSensor == null) {
				oSensor = new SensorEventVO(
						sensorName, oZone.getoRule().getSensorOccToUnoccChangeTime(), 
						oZone.getoRule().getZoneHbTimeInterval());
				CacheManager.getInstance().addSensor(sensorName, oSensor);
			}
			oSensor.addZone(oZone.getId());
		}
		return true;
	}
	
	public Boolean deleteZone(Long id) {
		CacheManager.getInstance().deleteZone(id);
		return true;
	}
	
	public Boolean resetZoneSensors(Long zoneId, Set<String> sensors) {
		ZoneEventVO z = CacheManager.getInstance().getZone(zoneId);
		
		for (String sensor : z.getSensorList()) {
			SensorEventVO sensorEventVO = CacheManager.getInstance().getSensor(sensor);
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
		
		z.setSensorList(new ArrayList<String>());
		
		for(String sensor: sensors) {
			SensorEventVO oSensor = CacheManager.getInstance().getSensor(sensor);
			if (oSensor == null) {
				oSensor = new SensorEventVO(sensor, z.getoRule().getSensorOccToUnoccChangeTime(), 
						z.getoRule().getZoneHbTimeInterval());
				CacheManager.getInstance().addSensor(sensor, oSensor);
			}
			oSensor.addZone(z.getId());
		}
		return true;
	}
	
	public Boolean init(Scheduler scheduler) {
		new Thread(new EMCmdProcessor()).start();
		JobDetail hbCheckJob;
		try {
			hbCheckJob = newJob(HeartbeatCheckJob.class)
					.withIdentity("HeartbeatCheckJob", 
							scheduler.getSchedulerName())
					.build();
			
			SimpleTrigger hbCheckTrigger = (SimpleTrigger) newTrigger()
					.withIdentity( "HeartbeatCheckTrigger",
							scheduler.getSchedulerName())
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							        .withIntervalInSeconds(30)
							        .repeatForever())
					        .build();
			
			scheduler.scheduleJob(hbCheckJob, hbCheckTrigger);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public List<String> getSensorsListByZoneId(Long id) {
		ZoneEventVO z = CacheManager.getInstance().getZoneMap().get(id);
		if(z != null) {
			return z.getSensorList();
		}
		return null;
	}
	
	public List<Long> getZoneIdBySensor(String name) {
		 SensorEventVO s = CacheManager.getInstance().getSensor(name);
		 if(s != null) {
			 return s.getZoneList();
		 }
		 return null;
	}
	
	public byte getOccupancyStateByZoneId(Long id) {
		ZoneEventVO z = CacheManager.getInstance().getZoneMap().get(id);
		if(z != null) {
			return z.getOccOutput();
		}
		return (byte) -1;
	}
	

}
