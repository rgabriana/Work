package com.enlightedinc.hvac.job;

import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.type.TypeReference;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.enlightedinc.hvac.communication.utils.Communicator;
import com.enlightedinc.hvac.model.Sensor;
import com.enlightedinc.hvac.model.SensorHistory;
import com.enlightedinc.hvac.service.SensorHistoryManager;
import com.enlightedinc.hvac.service.SensorManager;
import com.enlightedinc.hvac.utils.Globals;
import com.enlightedinc.hvac.utils.JsonUtil;
import com.enlightedinc.hvac.utils.SpringContext;

public class EMPollJob implements Job {
	SensorHistoryManager sensorHistoryManager;
	SensorManager sensorManager;
    
	
	public EMPollJob() {
		sensorHistoryManager = (SensorHistoryManager)SpringContext.getBean("sensorHistoryManager");
		sensorManager = (SensorManager)SpringContext.getBean("sensorManager");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Polling EM for sensor records");
		Communicator communicator = new Communicator();
		
		
		if(Globals.enabledDimLevels) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getDimLevelsURL, 300000, "application/json");
			JsonUtil<ArrayList<Sensor>> jsonUtil = new JsonUtil<ArrayList<Sensor>>();
			ArrayList<Sensor> sensorsList = null;
			if(data != null ) {
				sensorsList = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<Sensor>>() { });
				if (sensorsList != null && sensorsList.size() > 0) {
					for(Sensor sensor: sensorsList) {
						Sensor savedSensor = sensorManager.getSensorFromMac(sensor.getMacAddress());
						if(savedSensor == null) {
							sensor.setLastStatusTime(new Date());
							sensorManager.saveSensor(sensor);
						}
						else {
							savedSensor.setLastStatusTime(new Date());
							savedSensor.setCurrentDimLevel(sensor.getCurrentDimLevel());
							sensorManager.saveSensor(savedSensor);
						}
					}
				}
			}
		}
		
		if(Globals.enabledFixtureOutages) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getFixtureOutagesURL, 300000, "application/json");
			JsonUtil<ArrayList<Sensor>> jsonUtil = new JsonUtil<ArrayList<Sensor>>();
			ArrayList<Sensor> sensorsList = null;
			if(data != null ) {
				sensorsList = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<Sensor>>() { });
				if (sensorsList != null && sensorsList.size() > 0) {
					for(Sensor sensor: sensorsList) {
						Sensor savedSensor = sensorManager.getSensorFromMac(sensor.getMacAddress());
						if(savedSensor == null) {
							sensor.setLastStatusTime(new Date());
							sensorManager.saveSensor(sensor);
						}
						else {
							savedSensor.setLastStatusTime(new Date());
							savedSensor.setOutageFlag(sensor.getOutageFlag());
							sensorManager.saveSensor(savedSensor);
						}
					}
				}
			}
		}
		
		if(Globals.enabledLastOccupancy) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getLastOccupancyURL, 300000, "application/json");
			JsonUtil<ArrayList<Sensor>> jsonUtil = new JsonUtil<ArrayList<Sensor>>();
			ArrayList<Sensor> sensorsList = null;
			if(data != null ) {
				sensorsList = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<Sensor>>() { });
				if (sensorsList != null && sensorsList.size() > 0) {
					for(Sensor sensor: sensorsList) {
						Sensor savedSensor = sensorManager.getSensorFromMac(sensor.getMacAddress());
						if(savedSensor == null) {
							sensor.setLastStatusTime(new Date());
							sensorManager.saveSensor(sensor);
						}
						else {
							savedSensor.setLastStatusTime(new Date());
							savedSensor.setLastOccupancySeen(sensor.getLastOccupancySeen());
							sensorManager.saveSensor(savedSensor);
						}
					}
				}
			}
		}
		
		
		//Long toSyncId = -1L;
		int recordsSize = -1;
		boolean cont = true;
		Long lastSyncId = sensorHistoryManager.getMaxHistoryId();
		ArrayList<SensorHistory> motionBitsHistory = null;
		ArrayList<SensorHistory> temperatureHistory = null;
		ArrayList<SensorHistory> ambientHistory = null;
		ArrayList<SensorHistory> powerHistory = null;
		if(Globals.enabledMotionBits && cont) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getMotionBitsURL + lastSyncId + "/" + recordsSize, 300000, "application/json");
			JsonUtil<ArrayList<SensorHistory>> jsonUtil = new JsonUtil<ArrayList<SensorHistory>>();
			if(data != null ) {
				motionBitsHistory = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<SensorHistory>>() { });
				if (motionBitsHistory.size() > 0 && recordsSize == -1) {
					recordsSize = motionBitsHistory.size();
					lastSyncId = motionBitsHistory.get(0).getId() - 1;
				}
				else if (motionBitsHistory.size() <= 0) {
					cont = false;
				}
			}
		}
		if(Globals.enabledAmbientLight && cont) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getAmbientLightURL + lastSyncId + "/" + recordsSize, 300000, "application/json");
			JsonUtil<ArrayList<SensorHistory>> jsonUtil = new JsonUtil<ArrayList<SensorHistory>>();
			if(data != null ) {
				ambientHistory = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<SensorHistory>>() { });
				if (ambientHistory.size() > 0 && recordsSize == -1) {
					recordsSize = ambientHistory.size();
					//toSyncId = (ambientHistory.get(ambientHistory.size() - 1)).getId();
					lastSyncId = ambientHistory.get(0).getId() - 1;
				}
				else if (ambientHistory.size() <= 0) {
					cont = false;
				}
			}
		}
		if(Globals.enabledAvgTemperature && cont) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getAvgTemperatureURL + lastSyncId + "/" + recordsSize, 300000, "application/json");
			JsonUtil<ArrayList<SensorHistory>> jsonUtil = new JsonUtil<ArrayList<SensorHistory>>();
			if(data != null ) {
				temperatureHistory = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<SensorHistory>>() { });
				if (temperatureHistory.size() > 0 && recordsSize == -1) {
					recordsSize = temperatureHistory.size();
					//toSyncId = (temperatureHistory.get(temperatureHistory.size() - 1)).getId();
					lastSyncId = temperatureHistory.get(0).getId() - 1;
				}
				else if (temperatureHistory.size() <= 0){
					cont = false;
				}
			}
		}
		if(Globals.enabledSensorPower && cont) {
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getSensorPowerURL + lastSyncId  + "/" + recordsSize, 300000, "application/json");
			JsonUtil<ArrayList<SensorHistory>> jsonUtil = new JsonUtil<ArrayList<SensorHistory>>();
			if(data != null ) {
				powerHistory = jsonUtil.getObject(data.toString(), new TypeReference<ArrayList<SensorHistory>>() { });
				if (powerHistory.size() > 0 && recordsSize == -1) {
					recordsSize = powerHistory.size();
					//toSyncId = (powerHistory.get(powerHistory.size() - 1)).getId();
					lastSyncId = powerHistory.get(0).getId() - 1;
				}
				else if (powerHistory.size() <= 0) {
					cont = false;
				}
			}
		}
		if(recordsSize > 0  && cont) {
			for(int i = 0; i < recordsSize; i++) {
				SensorHistory sensorHistory = null;
				if(Globals.enabledMotionBits) {
					if(sensorHistory == null) {
						sensorHistory = motionBitsHistory.get(i);
					}
				}
				if(Globals.enabledAmbientLight) {
					if(sensorHistory == null) {
						sensorHistory = ambientHistory.get(i);
					}
					else {
						sensorHistory.setAvgAmbientLight(ambientHistory.get(i).getAvgAmbientLight());
					}
				}
				if(Globals.enabledAvgTemperature) {
					if(sensorHistory == null) {
						sensorHistory = temperatureHistory.get(i);
					}
					else {
						sensorHistory.setAvgTemperature(temperatureHistory.get(i).getAvgTemperature());
					}
				}
				if(Globals.enabledSensorPower) {
					if(sensorHistory == null) {
						sensorHistory = powerHistory.get(i);
					}
					else {
						sensorHistory.setPowerUsed(powerHistory.get(i).getPowerUsed());
						sensorHistory.setBasePowerUsed(powerHistory.get(i).getBasePowerUsed());
					}
				}
				sensorHistoryManager.saveSensorHistory(sensorHistory);
			}
		}
		
	}

}
