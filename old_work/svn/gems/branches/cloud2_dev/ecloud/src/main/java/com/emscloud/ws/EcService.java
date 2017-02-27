package com.emscloud.ws ;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.service.FloorEnergyConsumptionManager;


@Controller
@Path("/ec")
public class EcService {
		
	@Resource
	FloorEnergyConsumptionManager floorEcMgr;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public EcService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
  	
	/**
	 * Returns none
	 * 
	 * @return none
	 * services/sppa/aggregateDailyEnergyPerFloor/50/20130408150000
	 */
	@Path("aggregateDailyEnergyPerFloor/{custId}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void aggregateDailyEnergyPerFloor(@PathParam("custId") Long custId,
			@PathParam("tdate") String tDate) {
		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");    
    Calendar oTCalendar = Calendar.getInstance();    
    Date toDate = null;
    
    try {
        toDate = inputFormat.parse(tDate);
    } catch (ParseException pe) {
        toDate = oTCalendar.getTime();
    }
    oTCalendar.setTime(toDate);
    oTCalendar.set(Calendar.SECOND, 00);
		floorEcMgr.aggregateFloorDailyEnergyReadings(toDate);		
		
	} //end of method aggregateDailyEnergyPerFloor
	
	/**
	 * Returns none
	 * 
	 * @return none
	 * services/sppa/aggregateHourlyEnergyPerFloor/50/20130408150000
	 */
	@Path("aggregateHourlyEnergyPerFloor/{custId}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void aggregateHourlyEnergyPerFloor(@PathParam("custId") Long custId,
			@PathParam("tdate") String tDate) {
		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");    
    Calendar oTCalendar = Calendar.getInstance();    
    Date toDate = null;
    
    try {
        toDate = inputFormat.parse(tDate);
    } catch (ParseException pe) {
        toDate = oTCalendar.getTime();
    }
    oTCalendar.setTime(toDate);
    oTCalendar.set(Calendar.SECOND, 00);
		floorEcMgr.aggregateFloorHourlyEnergyReadings(toDate);		
		
	} //end of method aggregateHourlyEnergyPerFloor
      
} //end of class EcService
