package com.enlightedinc.hvac.job;

import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.type.TypeReference;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.enlightedinc.hvac.communication.utils.Communicator;
import com.enlightedinc.hvac.model.Sensor;
import com.enlightedinc.hvac.service.SensorManager;
import com.enlightedinc.hvac.utils.Globals;
import com.enlightedinc.hvac.utils.JsonUtil;
import com.enlightedinc.hvac.utils.SpringContext;

public class StatsPollJob implements Job {
	
	SensorManager sensorManager;
    
	
	public StatsPollJob() {
		sensorManager = (SensorManager)SpringContext.getBean("sensorManager");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		if(Globals.enableStatsPolling) {
			//System.out.println("Polling EM for sensor fxstats");
			Communicator communicator = new Communicator();
			StringBuilder data = communicator.webServiceGetRequest(Globals.HTTPS + Globals.em_ip + Globals.getRealTimeStatsURL, 300000, "application/json");
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
							savedSensor.setAvgAmbientLight(sensor.getAvgAmbientLight());
							savedSensor.setAvgTemperature(sensor.getAvgTemperature());
							savedSensor.setLastOccupancySeen(sensor.getLastOccupancySeen());
							sensorManager.saveSensor(savedSensor);
						}
					}
				}
			}
		}
		
	}

}
