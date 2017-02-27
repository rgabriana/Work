/**
 * 
 */
package com.ems.ws;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Calendar;
import java.util.Date;

import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.utils.DateUtil;
import com.ems.model.Sensor;
import com.ems.vo.model.SensorEnergyStats;
import com.ems.ws.util.Response;

/**
 * @author sreedhar.kamishetti
 *
 */
@Controller
@Path("/org/sensor/v1")
public class SensorServiceV1 {

	@Resource(name = "energyConsumptionManager")
  private EnergyConsumptionManager energyConsumptionManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	
	/**
	 * 
	 */
	public SensorServiceV1() {
		// TODO Auto-generated constructor stub
	}
	
	/**
   * Returns the energy consumption data for the last 15 minutes for all the fixtures of a floor
   * 
   * @param floorId              
   * @return sensorEnergyStats
   */
  @Path("lastSensorEnergyStats/15min/floor/{floorId}")
  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public List<SensorEnergyStats> getLastECDataByFixtureId(@PathParam("floorId") Long floorId) {
    
  	Date toDate = new Date();
  	Calendar calendar = Calendar.getInstance();
  	calendar.setTime(toDate);
  	int mod = calendar.get(Calendar.MINUTE) % 15;
  	calendar.add(Calendar.MINUTE, -mod);
  	toDate = calendar.getTime();
  	calendar.add(Calendar.MINUTE, -15);
  	Date fromDate = calendar.getTime();
  	
  	String fDate = DateUtil.formatDate(fromDate, "yyyyMMddHHmmss");
  	String tDate = DateUtil.formatDate(toDate, "yyyyMMddHHmmss");    	
  	return getECDataByFixtureId(floorId, fDate, tDate);
    
  } //end of method getLastECDataByFixtureId
  
  /**
   * Returns the energy consumption data for a specified time for the fixture 
   * 
   * @param fromDate in the format yyyyMMddHHmmss
   * @param toDate in the format yyyyMMddHHmmss
   * @param floorId              
   * @return sensorEnergyStats
   */
  @Path("sensorEnergyStats/15min/floor/{floorId}/{fromDate}/{toDate}")
  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public List<SensorEnergyStats> getECDataByFixtureId(@PathParam("floorId") Long floorId,@PathParam("fromDate") String fromDate,@PathParam("toDate") String toDate) {
    List<SensorEnergyStats> list = null;     
    list = energyConsumptionManager.getECDataByFixture(DateUtil.parseString(fromDate,"yyyyMMddHHmmss"), DateUtil.parseString(toDate,"yyyyMMddHHmmss"), floorId);
    return list;
    
  }
  
  /**
	 * Returns Fixture Dim Value
	 * 
	 * @param fid
	 *            fixture unique identifier
	 * @return fixture Dim Value
	 */
	@Path("getSensorDimValue/{fid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getSensoreDimValue(@PathParam("fid") Long fid) {
		Fixture fixture = fixtureManager.getFixtureById(fid);
		Response oStatus = new Response();
		if(fixture != null){		
			oStatus.setStatus(fixture.getDimmerControl());
		}
		return oStatus;
		
	}
	
	/**
	 * Returns Fixture Dim Value
	 * 
	 * @param sensors  List of fixtures
   *            list of fixtures "<sensors><sensor><id>1</id></sensor></sensors>"
	 * @return fixture Dim Value
	 */
	@Path("getSensorDimValues")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Sensor> getSensorDimValues(List<com.ems.vo.model.Sensor> sensors) {
		
		Iterator<com.ems.vo.model.Sensor> sensorIter = sensors.iterator();
		ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
		while(sensorIter.hasNext()) {			
			Fixture fixture = FixtureCache.getInstance().getCachedFixture(sensorIter.next().getId().longValue());
			if(fixture == null) {
				continue;
			}
			Sensor sensor = new Sensor();
			sensor.setFixtureId(BigInteger.valueOf(fixture.getId()));
			sensor.setDimLevel(fixture.getDimmerControl().floatValue());		
			sensorList.add(sensor);
		}
		return sensorList;
		
	} //end of method getSensorDimValues

} //end of class getSensorDimValues
