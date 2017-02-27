/**
 * 
 */
package com.occengine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.occengine.dao.OccSystemConfigurationDao;
import com.occengine.dao.SensorDao;
import com.occengine.dao.ZoneDao;
import com.occengine.model.OccSystemConfiguration;
import com.occengine.model.Sensor;
import com.occengine.model.Zone;
import com.occengine.service.EmService;
import com.occengine.types.OccupancyStatus;
import com.occengine.utils.OccUtil;
import com.occengine.model.SensorRGL;

/**
 * @author sreedhar.kamishetti
 *
 */
public class OccupancyEngine {

	private static OccupancyEngine instance = null;
	
	//private ConcurrentHashMap<Long, ZoneStatus> zoneStatusMap = new ConcurrentHashMap<Long, ZoneStatus>();
	//private ConcurrentHashMap<Long, Integer> zoneOccStatusMap = new ConcurrentHashMap<Long, Integer>();
	
	private ConcurrentHashMap<Long, List<Long>> zoneSensorMap = new ConcurrentHashMap<Long, List<Long>>();
	private ConcurrentHashMap<Long, List<String>> zoneRGLSensorMap = new ConcurrentHashMap<Long, List<String>>();
	
	private ConcurrentHashMap<Long, Sensor> sensorMap = new ConcurrentHashMap<Long, Sensor>();
	private ConcurrentHashMap<String, Long> sensorIdMap = new ConcurrentHashMap<String, Long>();
	
	private ConcurrentHashMap<Long, BmsClientInfo> zoneRGLValueMap = new ConcurrentHashMap<Long, BmsClientInfo>();
		
	private int hbEnlFailureTolerance = 10; //10 sec
	private int hbBmsFailureTolerance = 10; //10 sec
	private int unOccPeriod = 900; //15 minutes
	private int occEngineLoopTime = 1; //1 second
	private int endTimeTolerance = 900 / 60; //15 minutes
	private int rglRefreshTime = 60; // 1 minute
	private int hbFailedSensorsPercentage = 0;
	
	private SensorDao sensorDao;
	
	private ZoneDao zoneDao;
	
	static final Logger logger = Logger.getLogger("OccLog");
			
	/**
	 * 
	 */
	private OccupancyEngine() {
		// TODO Auto-generated constructor stub
		
		try {
			initialize();		
			ZoneStatusThread zoneStatusThread = new ZoneStatusThread();
			zoneStatusThread.start();
			
			ZoneBmsThread zoneBmsThread = new ZoneBmsThread();
			zoneBmsThread.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static OccupancyEngine getInstance() {
		
		if(instance == null) {
			synchronized(OccupancyEngine.class) {
				if(instance == null) {
					instance = new OccupancyEngine();
				}
			}
		}
		return instance;
		
	} //end of method getInstance

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		System.out.println("occ -- " + OccupancyStatus.VACANT.ordinal());

	}
	
	private void initialize() {
				
		// reading from the database
    OccSystemConfigurationDao occSysDao = (OccSystemConfigurationDao) SpringContext.getBean("occSystemConfigurationDao");

    OccSystemConfiguration tempConfig = occSysDao.loadConfigByName("occ.hbEnlFailureTolerance");
    if(tempConfig != null) {
    	System.out.println("from database occ heart beat enl failure tolerance -- " + tempConfig.getValue());
    	hbEnlFailureTolerance = Integer.valueOf(tempConfig.getValue());
    }
    	
    tempConfig = occSysDao.loadConfigByName("occ.hbBmsFailureTolerance");
    if(tempConfig != null) {
    	System.out.println("from database occ heart beat bms failure tolerance -- " + tempConfig.getValue());
    	hbBmsFailureTolerance = Integer.valueOf(tempConfig.getValue());
    }
    
    tempConfig = occSysDao.loadConfigByName("occ.unOccPeriod");
    if(tempConfig != null) {
    	System.out.println("from database occ un occupied period -- " + tempConfig.getValue());
    	unOccPeriod = Integer.valueOf(tempConfig.getValue());
    }
    
    tempConfig = occSysDao.loadConfigByName("occ.occEngineLoopTime");
    if(tempConfig != null) {
    	System.out.println("from database occ engine loop time -- " + tempConfig.getValue());
    	occEngineLoopTime = Integer.valueOf(tempConfig.getValue());
    }
    
    tempConfig = occSysDao.loadConfigByName("occ.endTimeTolerance");
    if(tempConfig != null) {
    	System.out.println("from database occ end time tolerance -- " + tempConfig.getValue());
    	endTimeTolerance = Integer.valueOf(tempConfig.getValue()) / 60;
    }
    
    tempConfig = occSysDao.loadConfigByName("occ.rglRefreshTime");
    if(tempConfig != null) {
    	System.out.println("from database occ rgl refresh time -- " + tempConfig.getValue());
    	rglRefreshTime = Integer.valueOf(tempConfig.getValue());
    }
    
    tempConfig = occSysDao.loadConfigByName("occ.hbFailedSensors");
    if(tempConfig != null) {
    	System.out.println("from database occ heart beat failed sensors -- " + tempConfig.getValue());
    	hbFailedSensorsPercentage = Integer.valueOf(tempConfig.getValue());
    }
    
		if (zoneDao == null) {
      zoneDao = (ZoneDao) SpringContext.getBean("zoneDao");
    }
		if (sensorDao == null) {
      sensorDao = (SensorDao) SpringContext.getBean("sensorDao");
    }
		List<Zone> zoneList = zoneDao.getAllZones();
		Iterator<Zone> zoneIter = zoneList.iterator();
		while(zoneIter.hasNext()) {
			Zone zone = zoneIter.next();
			//initialize sensor zone map
			List<Sensor> sensors = sensorDao.loadSensorsByZoneId(zone.getId());	
			if(sensors != null) {
				Iterator<Sensor> sensorIter = sensors.iterator();
				ArrayList<Long> sensorIdsList = new ArrayList<Long>();
				Long sensorId = null;
				Date lastOccTime = new Date(System.currentTimeMillis() - 5 * 60 * 1000);
				//int noOfSensors = sensors.size();
				while(sensorIter.hasNext()) {
					Sensor sensor = sensorIter.next();
					System.out.println("sensor mac -- " + sensor.getMac());
					sensor.setOccStatus(OccupancyStatus.OCCUPIED.ordinal());
					sensor.setLastStatusTime(lastOccTime);
					sensorId = sensor.getId();
					sensorIdsList.add(sensorId);
					sensorMap.put(sensorId, sensor);		
					sensorIdMap.put(sensor.getMac(), sensorId);
				}
				zoneSensorMap.put(zone.getId(), sensorIdsList);			
				//zoneOccStatusMap.put(zone.getId(), noOfSensors);
			}
			
			//initialize zone sensor rgl map
			List<SensorRGL> rglSensors = sensorDao.loadRGLSensorsByZoneId(zone.getId());
			if(rglSensors != null) {
				Iterator<SensorRGL> rglSensorIter = rglSensors.iterator();
				ArrayList<String> rglSensorMacList = new ArrayList<String>();
				while(rglSensorIter.hasNext()) {
					SensorRGL rglSensor = rglSensorIter.next();
					System.out.println("rgl mac -- " + rglSensor.getId());
					rglSensorMacList.add(rglSensor.getMac());
				}
				zoneRGLSensorMap.put(zone.getId(), rglSensorMacList);
			}
			
			//create a BmsClientInfo for each zone
			BmsClientInfo bmsClientInfo = new BmsClientInfo();
			if(zone.getLastBmsClientSetback() == null) {
				bmsClientInfo.setSetbackValue((byte)4);
				bmsClientInfo.setLastHeartbeatTime(new Date(System.currentTimeMillis() - hbBmsFailureTolerance * 1000));
			} else {
				bmsClientInfo.setSetbackValue(zone.getLastBmsClientSetback().byteValue());
				bmsClientInfo.setLastHeartbeatTime(zone.getBmsClientLastHbTime());				
			}
			zoneRGLValueMap.put(zone.getId(), bmsClientInfo);
		}	
		
	} //end of method initialize
	
	/*
	public ZoneStatus getZoneStatus(Long zoneId) {
		
		return zoneStatusMap.get(zoneId);
		
	} //end of method getZoneStatus
		
	public boolean getSetBack(Long zoneId) {
	
		Long oldCommSensorId = zoneSensorMap.get(zoneId).get(0);
		long oldCommSensorTime = sensorMap.get(oldCommSensorId).getLastStatusTime().getTime();
		if(System.currentTimeMillis() - oldCommSensorTime > setBackInterval) {
			//sensor is not communicating for the setback interval
			return false;
		}
		int zoneOccStatus = zoneOccStatusMap.get(zoneId);
		if(zoneOccStatus > 0) {
			return false;
		}
		return true;
		
	} //end of method getSetBack
	*/
	
	public void updateOccStatus(String sensorMac, int motionSecAgo, OccupancyStatus status, int ambLight, float temp) {
	
		Sensor sensor = sensorDao.getSensorByMac(sensorMac);
		//get the sensor		
		//long sensorId = sensorIdMap.get(sensorMac);
		long sensorId = sensor.getId();
		//Sensor sensor = sensorMap.get(sensorId);
		
		/*
		boolean occStateChanged = false;
		
		if(sensor.getOccStatus() != status.ordinal()) {
			occStateChanged = true;
			sensor.setOccStatus(status.ordinal());
		}	*/
		
		sensor.setOccStatus(motionSecAgo);
		sensor.setAvgAmbientLight(ambLight);
		sensor.setAvgTemperature(temp);
		sensor.setLastStatusTime(new Date());
		sensorDao.update(sensor);
		
		//get the zone id
		long zoneId = sensor.getZone().getId();
		
		zoneSensorMap.get(zoneId).remove(sensorId);
		zoneSensorMap.get(zoneId).add(sensorId);
		//update the zone status		
		
		/*
		if(occStateChanged) {
			int zoneStatus = zoneOccStatusMap.get(zoneId);
			if(status.equals(OccupancyStatus.OCCUPIED)) {
				zoneStatus += 1;				
			} else {
				zoneStatus -= 1;
			}
			zoneOccStatusMap.put(zoneId, zoneStatus);
		}
		*/
		
	} //end of method updateOccStatus
	
	class ZoneStatusThread extends Thread {
				
		public void run() {
			
			while(true) {
				try {
					//System.out.println("calling zone occ status " + new Date());
					Date startTime = new Date();
					Calendar calTo = Calendar.getInstance();		
					calTo.setTime(startTime);
					calTo.add(Calendar.SECOND, endTimeTolerance);					
					
					Calendar calHb = Calendar.getInstance();
					calHb.setTime(startTime);
					calHb.add(Calendar.SECOND, -hbEnlFailureTolerance);
					
					zoneDao.zoneOccStatus(hbEnlFailureTolerance, unOccPeriod, startTime, calTo.getTime(), calHb.getTime(), hbFailedSensorsPercentage);				
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				OccUtil.sleep(occEngineLoopTime);
			}
						
		} //end of method run
		
	} //end of class ZoneStatusThread
	
	class ZoneBmsThread extends Thread {
		
		public void run() {
			
			while(true) {
				//System.out.println("running zone bms thread");
				try {
					Iterator<Long> zoneIter = zoneRGLValueMap.keySet().iterator();
					while(zoneIter.hasNext()) {
						long zoneId = zoneIter.next();
						BmsClientInfo clientInfo = zoneRGLValueMap.get(zoneId);
						if(clientInfo.getLastHeartbeatTime().before(new Date(System.currentTimeMillis() - hbBmsFailureTolerance * 1000))) {
							//not received the bms heart beat for bms heart beat tolerance time
							//send red/green signal light signal to all the rgl sensors of that zone
							Iterator<String> rglSensorIter = zoneRGLSensorMap.get(zoneId).iterator();
							//System.out.println("no. of rgl -- " + zoneRGLSensorMap.get(zoneId).size());
							while(rglSensorIter.hasNext()) {
								String rglSensorMac = rglSensorIter.next();
								EmService.getInstance().turnOnGreenRed(rglSensorMac, endTimeTolerance);
							}							
						} else {
							//heart beat passed
							Iterator<String> rglSensorIter = zoneRGLSensorMap.get(zoneId).iterator();
							while(rglSensorIter.hasNext()) {
								String rglSensorMac = rglSensorIter.next();
								if(clientInfo.getSetbackValue() == 1) {
									EmService.getInstance().turnOnGreen(rglSensorMac, endTimeTolerance);
								} else if(clientInfo.getSetbackValue() == 4) {
									EmService.getInstance().turnOnRed(rglSensorMac, endTimeTolerance);
								} else {
									System.out.println("invalid value for setback -- " + clientInfo.getSetbackValue());
								}
							}	
						}
					}					
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				OccUtil.sleep(rglRefreshTime);
			}
						
		} //end of method run
		
	} //end of class ZoneBmsThread
	
	class BmsClientInfo {
		
		Date lastHeartbeatTime;
		byte setbackValue;
		/**
		 * @return the lastHeartbeatTime
		 */
		public Date getLastHeartbeatTime() {
			return lastHeartbeatTime;
		}
		/**
		 * @param lastHeartbeatTime the lastHeartbeatTime to set
		 */
		public void setLastHeartbeatTime(Date lastHeartbeatTime) {
			this.lastHeartbeatTime = lastHeartbeatTime;
		}
		/**
		 * @return the setbackValue
		 */
		public byte getSetbackValue() {
			return setbackValue;
		}
		/**
		 * @param setbackValue the setbackValue to set
		 */
		public void setSetbackValue(byte setbackValue) {
			this.setbackValue = setbackValue;
		}
			
	} //end of class BmsClientInfo
	
	public void updateBmsClientInfo(long zoneId, byte setbackValue) {
		
		BmsClientInfo clientInfo = zoneRGLValueMap.get(zoneId);
		if(clientInfo == null) {
			clientInfo = new BmsClientInfo();
		}
		Date hbTime = new Date();
		clientInfo.setLastHeartbeatTime(hbTime);
		clientInfo.setSetbackValue(setbackValue);
		zoneRGLValueMap.put(zoneId, clientInfo);
			
		zoneDao.updateBmsClientInfo(zoneId, hbTime, setbackValue);
		
		//turn on/off rgl lights
		Iterator<String> rglSensorIter = zoneRGLSensorMap.get(zoneId).iterator();
		while(rglSensorIter.hasNext()) {
			String rglSensorMac = rglSensorIter.next();
			if(clientInfo.getSetbackValue() == 1) {
				EmService.getInstance().turnOnGreen(rglSensorMac, endTimeTolerance);
			} else if(clientInfo.getSetbackValue() == 4) {
				EmService.getInstance().turnOnRed(rglSensorMac, endTimeTolerance);
			} else {
				System.out.println("invalid value for setback -- " + clientInfo.getSetbackValue());
			}
		}
		
	} //end of method updateBmsClientInfo
	
} //end of class OccupancyEngine
