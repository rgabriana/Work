package com.ems.ws;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.utils.DateUtil;
import com.ems.vo.model.Sensor;
import com.ems.vo.model.SensorEnergyStats;
import com.ems.ws.util.Response;

/**
 * @author sreedhar.kamishetti
 * 
 */
@Controller
@Path("/org/sensor/v2")
public class SensorServiceV2 extends SensorServiceV1 {

	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Autowired
	private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	/**
	 * 
	 */
	public SensorServiceV2() {
	}

	/**
	 * Returns the energy consumption data for a specified time for the fixture
	 * 
	 * @param fromDate
	 *            in the format yyyyMMddHHmmss
	 * @param toDate
	 *            in the format yyyyMMddHHmmss
	 * @param floorId
	 * @return sensorEnergyStats
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("sensorEnergyStats/15min/floor/{floorId}/{fromDate}/{toDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<SensorEnergyStats> getECDataByFixtureId(
			@PathParam("floorId") Long floorId,
			@PathParam("fromDate") String fromDate,
			@PathParam("toDate") String toDate) {
		List<SensorEnergyStats> list = null;
		Response resp = new Response();
		Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("fromDate", fromDate);
		nameValMap.put("toDate", toDate);
		resp = CommonUtils.isParamValueAllowed(messageSource,
				systemConfigurationManager, nameValMap);
		if (resp != null && resp.getStatus() != 200) {
			m_Logger.error("Validation error " + resp.getMsg());
			return null;
		}
		list = energyConsumptionManager.getECDataByFixture(
				DateUtil.parseString(fromDate, "yyyyMMddHHmmss"),
				DateUtil.parseString(toDate, "yyyyMMddHHmmss"), floorId);
		
		if (list != null && !list.isEmpty()) {
			for (final SensorEnergyStats ses : list){
				if (ses != null){
					final List<com.ems.model.Sensor> sl = ses.getList();
					if(sl != null && !sl.isEmpty()){
						for(final com.ems.model.Sensor s : sl){
							s.setOccCount(null);
						}
					}
				}
			}
		}
		return list;
	}
	 /**
     * Fetches stats data for the fixtures in the specified time interval w.r.t org level
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fdate
     *            newer date
     * @param tdate
     *            older date
     * @return FixtureStatsDetail list
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
    @Path("stats/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Sensor> loadV1StatsInDateRange(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
       
      SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmm");
      SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      
      Calendar oFrom = Calendar.getInstance();      
      Date oFDate = null;
      Date oTDate = null;
      try {
	oTDate = inputFormat.parse(tdate);	
      } catch (ParseException pe) {
	oTDate = new Date(System.currentTimeMillis());	
      }
            
      try {
	oFDate = inputFormat.parse(fdate);
	oFrom.setTime(oFDate);
      } catch (ParseException pe) {
	oFDate = new Date(System.currentTimeMillis());
	oFrom.setTime(oFDate);
	oFrom.add(Calendar.MINUTE, -10);
	oFrom.set(Calendar.SECOND, 00);
      } 
      if((oTDate.getTime() - oFrom.getTimeInMillis()) > 60 * 60 * 1000) {
	//valid duration is only 1 hour
	return null;
      }
          
      if (property.equalsIgnoreCase("floor")) {
	property = "floor_id";
      } else if (property.equalsIgnoreCase("area")) {
	property = "area_id";
      } else if (property.equalsIgnoreCase("fixture")) {
	property = "id";
      } 
      ArrayList<Sensor> statsList = new ArrayList<Sensor>();
      List<Object[]> results = energyConsumptionManager.loadStatsInDateRange(property, 
	  pid, oFrom.getTime(), oTDate);
      if(results == null) {
	return statsList;
      }
      //System.out.println("no. of entries-- " + results.size());
      Iterator ecIter = results.iterator();
      Sensor details = null;      
      while(ecIter.hasNext()) {
	Object[] data = (Object[])ecIter.next();
	details = new Sensor();
	details.setId(((BigInteger)data[0]).longValue());
	details.setPower((BigDecimal)data[1]);	
	details.setTemperature(((BigDecimal)data[3]).doubleValue());
	details.setLightLevel(((Integer) data[5]).shortValue());
	details.setOccupancy(null);	
	details.setCaptureTime(outputFormat.format((Date)data[6]));
	statsList.add(details);
      }
      return statsList;
      
    } //end of method loadStatsInDateRange
	
	/**
     * Returns Fixture Details
     * 
     * @param fid
     *            fixture unique identifier
     * @return fixture details
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor','Bacnet')")
    @Path("details/{fid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Sensor getSensorDetails(@PathParam("fid") Long fid) {

        long captureTime = System.currentTimeMillis();
        long min5 = 5 * 60 * 1000;
        captureTime = captureTime - captureTime % min5 - min5;
        List results = fixtureManager.getSensorData(fid, captureTime);
        Sensor sensor = null;
        if (results != null && !results.isEmpty()) {
            Iterator sensorIt = results.iterator();
            if (sensorIt.hasNext()) {
                Object[] dataArr = (Object[]) sensorIt.next();
                sensor = new Sensor((Long) dataArr[0], dataArr[1].toString(), (BigDecimal) dataArr[2],
                        (Long) dataArr[3], (Double) dataArr[4]);
                
                sensor.setLightLevel((Short) dataArr[5]);
                sensor.setOccupancy(null);
            }
        }
        return sensor;

    } // end of method getSensorDetails
	
} // end of class SensorServiceV1
