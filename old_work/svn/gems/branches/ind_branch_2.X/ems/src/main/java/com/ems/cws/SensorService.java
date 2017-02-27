/**
 * 
 */
package com.ems.cws;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.vo.model.Sensor;
import com.ems.vo.model.SensorStats;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/sensor")
public class SensorService {

    static final Logger logger = Logger.getLogger(SensorService.class.getName());

    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    @Resource(name = "energyConsumptionManager")
    private EnergyConsumptionManager energyConsumptionManager;

    /**
     * no argument constructor
     */
    public SensorService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Returns fixture list 
     * 
     * 
     * @param property
     *            (company|campus|building|floor|area|gateway|secondarygateway| group)
     * @param pid
     *            property unique identifier
     * @param limit Limit
     * @return fixture list for the property level
     */
    @Path("list/{property}/{pid}/{limit:.*}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Sensor> getSensorList(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("limit") String limit) {

        if (property.equalsIgnoreCase("company")) {
            long captureTime = System.currentTimeMillis();
            long min5 = 5 * 60 * 1000;
            captureTime = captureTime - captureTime % min5 - min5;
            List results = fixtureManager.getAllSensorData(captureTime);
            ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
            if (results != null && !results.isEmpty()) {
                Iterator sensorIt = results.iterator();
                while (sensorIt.hasNext()) {
                    Object[] dataArr = (Object[]) sensorIt.next();
                    Sensor sensor = new Sensor((Long) dataArr[0], dataArr[1].toString(), (BigDecimal) dataArr[2],
                            (Long) dataArr[3], (Short) dataArr[4]);
                    sensorList.add(sensor);
                }
            }
            return sensorList;
        }
        return null;

    } // end of method getSensorList

    /**
     * Returns Fixture Details
     * 
     * @param fid
     *            fixture unique identifier
     * @return fixture details
     */
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
                        (Long) dataArr[3], (Short) dataArr[4]);
            }
        }
        return sensor;

    } // end of method getSensorDetails

    /**
     * Allows selected set of fixture to be dimmed or brighted from the floorplan
     * 
     * @param mode
     *            (rel | abs)
     * @param percentage
     *            {(-100 | 0 | 100) for rel} AND {(0 | 100) for abs}
     * @param time
     *            minutes
     * @param sensors  List of fixtures
     *            list of fixtures "<fixtures><fixture><id>1</id></fixture></fixtures>"
     * @return Response
     */
    @Path("op/dim/{mode}/{percentage}/{time}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dimFixture(@PathParam("mode") String mode, @PathParam("percentage") String percentage,
            @PathParam("time") String time, List<Sensor> sensors) {

        logger.debug("Percentage: " + percentage + ", Time: " + time + ", Fixtures: " + sensors.size());
        int[] sensorList = new int[sensors.size()];
        int count = 0;
        Iterator<Sensor> itr = sensors.iterator();
        while (itr.hasNext()) {
            Sensor sensor = (Sensor) itr.next();
            sensorList[count++] = sensor.getId().intValue();
        }
        if (mode.equalsIgnoreCase("REL"))
            fixtureManager.dimFixtures(sensorList, Integer.parseInt(percentage), Integer.parseInt(time));
        else if (mode.equalsIgnoreCase("ABS"))
            fixtureManager.absoluteDimFixtures(sensorList, Integer.parseInt(percentage), Integer.parseInt(time));
        return new Response();

    } // end of method dimFixture
    
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
    @Path("stats/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Sensor> loadStatsInDateRange(@PathParam("property") String property,
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
	details.setTemperature(((Integer)data[3]).shortValue());
	details.setOccupancy(((BigInteger)data[4]).longValue());	
	details.setCaptureTime(outputFormat.format((Date)data[6]));
	statsList.add(details);
      }
      return statsList;
      
    } //end of method loadStatsInDateRange
    
    /**
     * Returns fixture list
     * 
     * @param property
     *            (company|campus|building|floor|area|gateway|secondarygateway| group)
     * @param pid
     *            property unique identifier
     * @param limit Limit
     * @return SensorStats fixture list for property level
     */
    @Path("v1/list/{property}/{pid}/{limit:.*}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SensorStats getV1SensorList(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("limit") String limit) {

    	if (property.equalsIgnoreCase("company")) {
            long captureTime = System.currentTimeMillis();
            long min5 = 5 * 60 * 1000;
            captureTime = captureTime - captureTime % min5 - min5;
            List results = fixtureManager.getAllSensorData(captureTime);
            ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
            if (results != null && !results.isEmpty()) {
                Iterator sensorIt = results.iterator();
                while (sensorIt.hasNext()) {
                    Object[] dataArr = (Object[]) sensorIt.next();
                    Sensor sensor = new Sensor((Long) dataArr[0], dataArr[1].toString(), (BigDecimal) dataArr[2],
                            (Long) dataArr[3], (Short) dataArr[4]);
                    sensor.setLightLevel((Short) dataArr[5]);
                    sensorList.add(sensor);
                }
            }
            SensorStats sensorStats = new SensorStats();
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sensorStats.setCaptureTime(outputFormat.format(new Date(captureTime)));
            sensorStats.setSensorList(sensorList);
            return sensorStats;
        }
        return null;

    } // end of method getSensorList

    /**
     * Returns Fixture Details
     * 
     * @param fid
     *            fixture unique identifier
     * @return SensorStats fixture details
     */
    @Path("v1/details/{fid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SensorStats getV1SensorDetails(@PathParam("fid") Long fid) {

    	long captureTime = System.currentTimeMillis();
        long min5 = 5 * 60 * 1000;
        captureTime = captureTime - captureTime % min5 - min5;
        List results = fixtureManager.getSensorData(fid, captureTime);
        Sensor sensor = null;
        ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
        if (results != null && !results.isEmpty()) {
            Iterator sensorIt = results.iterator();
            if (sensorIt.hasNext()) {
                Object[] dataArr = (Object[]) sensorIt.next();
                sensor = new Sensor((Long) dataArr[0], dataArr[1].toString(), (BigDecimal) dataArr[2],
                        (Long) dataArr[3], (Short) dataArr[4]);
                sensor.setLightLevel((Short) dataArr[5]);
                sensorList.add(sensor);
            }
        }
        SensorStats sensorStats = new SensorStats();
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");        
        sensorStats.setCaptureTime(outputFormat.format(new Date(captureTime)));
        sensorStats.setSensorList(sensorList);
        return sensorStats;

    } // end of method getSensorDetails
    
} // end of class SensorService
