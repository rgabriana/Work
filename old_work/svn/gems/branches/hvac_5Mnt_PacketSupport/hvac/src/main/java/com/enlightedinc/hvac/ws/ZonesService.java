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
import com.enlightedinc.hvac.model.Zone;
import com.enlightedinc.hvac.service.SensorManager;
import com.enlightedinc.hvac.service.ZonesManager;
import com.enlightedinc.hvac.utils.Globals;
import com.enlightedinc.hvac.utils.JsonUtil;
import com.enlightedinc.hvac.ws.util.Response;

@Controller
@Path("/zones")
public class ZonesService {

	@Resource
	ZonesManager zonesManager;
	@Resource
	SensorManager sensorManager;

	@Path("list")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public List<Zone> getAllZones() {
		return zonesManager.getAllZones();
	}
	
	
	@Path("/getTempSetBack/zone/{zoneId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getTempSetBackByZoneId(@PathParam("zoneId") Long zoneId) {
		Response resp = new Response();
		resp.setMsg(zonesManager.getTempSetBackByZoneId(zoneId).toString());
		return resp;
	}
	
	@Path("/getOutageStatus/zone/{zoneId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getOutageStatus(@PathParam("zoneId") Long zoneId) {
		Response resp = new Response();
		resp.setMsg(zonesManager.getOutageStatus(zoneId).toString());
		return resp;
	}
	
	
	@Path("/getAvgDimLevel/zone/{zoneId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getAvgDimLevel(@PathParam("zoneId") Long zoneId) {
		Response resp = new Response();
		resp.setMsg(zonesManager.getAvgDimLevel(zoneId).toString());
		return resp;
	}
	
	@Path("/getAvgTemperature/zone/{zoneId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getAvgTemperature(@PathParam("zoneId") Long zoneId) {
		Response resp = new Response();
		resp.setMsg(zonesManager.getAvgTemperature(zoneId).toString());
		return resp;
	}
	
	
	@Path("/getTotalPower/zone/{zoneId}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getTotalPower(@PathParam("zoneId") Long zoneId) {
		Response resp = new Response();
		resp.setMsg(zonesManager.getTotalPower(zoneId).toString());
		return resp;
	}
	
	@Path("/setDimLevel/zone/{zoneId}/{percentage}/{time}")
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response setDimLevel(@PathParam("zoneId") Long zoneId, @PathParam("percentage") int percentage, @PathParam("time") int time) {
		Response resp = new Response();
		Communicator communicator = new Communicator();
		List<Sensor> sensors = zonesManager.getAllSensorsForZone(zoneId);
		if(sensors != null && sensors.size() > 0) {
			StringBuffer postData = new StringBuffer("<fixtures>");
			for(Sensor s: sensors) {
				postData.append("<fixture><macaddress>" + s.getMacAddress() + "</macaddress></fixture>");
			}
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
