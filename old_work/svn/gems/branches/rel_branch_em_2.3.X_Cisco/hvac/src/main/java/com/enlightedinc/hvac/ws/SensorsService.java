package com.enlightedinc.hvac.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Controller;

import com.enlightedinc.hvac.communication.utils.Communicator;
import com.enlightedinc.hvac.model.Sensor;
import com.enlightedinc.hvac.service.SensorManager;
import com.enlightedinc.hvac.utils.Globals;
import com.enlightedinc.hvac.utils.JsonUtil;
import com.enlightedinc.hvac.ws.util.Response;

@Controller
@Path("/sensor")
public class SensorsService {
	
	@Resource
	SensorManager sensorManager;
	
	@Path("list")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public List<Sensor> getAllSensors() {
		return sensorManager.getAllSensors();
	}
	
	@Path("/getDimLevel/id/{sensorId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getAvgDimLevel(@PathParam("sensorId") Long sensorId) {
		Response resp = new Response();
		Integer val = sensorManager.getDimLevel(sensorId);
		if (val == -1) {
			resp.setStatus(1);
		}
		else {
			resp.setMsg(val.toString());
		}
		return resp;
	}
	
	@Path("/getLastOccupancySeen/id/{sensorId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getTimeSinceLastOccupancy(@PathParam("sensorId") Long sensorId) {
		Response resp = new Response();
		Integer val = sensorManager.getTimeSinceLastOccupancy(sensorId);
		if (val == -1) {
			resp.setStatus(1);
		}
		else {
			resp.setMsg(val.toString());
		}
		return resp;
	}
	
	@Path("/getOccupancyStatus/id/{sensorId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getOccupancyStatus(@PathParam("sensorId") Long sensorId) {
		Response resp = new Response();
		Integer val = sensorManager.getOccupancyStatus(sensorId);
		if (val == -1) {
			resp.setStatus(1);
		}
		else {
			resp.setMsg(val.toString());
		}
		return resp;
	}
	
	@Path("/setDimLevel/id/{sensorId}/{percentage}/{time}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response setDimLevel(@PathParam("sensorId") Long sensorId, @PathParam("percentage") int percentage, @PathParam("time") int time) {
		Response resp = new Response();
		Communicator communicator = new Communicator();
		Sensor s = sensorManager.getSensorById(sensorId);
		if(s != null) {
			StringBuffer postData = new StringBuffer("<fixtures>");
			postData.append("<fixture><macaddress>" + s.getMacAddress() + "</macaddress></fixture>");
			postData.append("</fixtures>");
			communicator.webServicePostRequest(postData.toString(),Globals.HTTPS + Globals.em_ip + Globals.setDimLevelURL + "/" + percentage + "/" + time, 300000, "application/xml");
			
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
		resp.setMsg("0");
		return resp;
	}
	
	

}
