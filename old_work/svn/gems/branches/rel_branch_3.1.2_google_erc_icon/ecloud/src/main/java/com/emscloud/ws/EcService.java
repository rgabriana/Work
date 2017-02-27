package com.emscloud.ws ;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.service.BldEnergyConsumptionManager;
import com.emscloud.service.CampusEnergyConsumptionManager;
import com.emscloud.service.EMEnergyConsumptionManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FloorEnergyConsumptionManager;
import com.emscloud.service.OrganizationEnergyConsumptionManager;
import com.emscloud.vo.AggregatedEmData;
import com.emscloud.vo.AggregatedSensorData;
import com.emscloud.vo.SensorData;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@Controller
@Path("/org/ec")
public class EcService {
		
	@Resource
	FloorEnergyConsumptionManager fecManager;	
	@Resource
	BldEnergyConsumptionManager becManager;	
	@Resource
	CampusEnergyConsumptionManager cecManager;	
	@Resource
	OrganizationEnergyConsumptionManager oecManager;
	@Resource
	EMEnergyConsumptionManager eecManager;
	@Resource
	FacilityManager facilityManager;
	
	//private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public EcService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
			
	@Path("getSensorData/{levelId}/{levelType}/{timeSpan}/{sDate}/{tDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getSensorData(@PathParam("levelId") Long levelId, 
			@PathParam("levelType") Long levelType, @PathParam("timeSpan") String timeSpan,
			@PathParam("sDate") String startDateStr, @PathParam("tDate") String endDateStr,
			@QueryParam("details") Boolean details) {
		
		System.out.println("inside the get sensor data");	
		ObjectMapper mapper = new ObjectMapper();
		String energyStr = new String();
		System.out.println("agg data -- " + details);
		System.out.println("level type -- " + levelType);
		System.out.println("time span -- " + timeSpan);
		if(!details) {
			List<AggregatedSensorData> energyList = new ArrayList<AggregatedSensorData>();
			switch(levelType.intValue()) {
			case 1:
				energyList = oecManager.getOrganizationAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr);			
				break;
			case 2:
				energyList = cecManager.getCampusAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr);			
				break;
			case 3:
				energyList = becManager.getBldAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr);
				break;
			case 4:
				energyList = fecManager.getFloorAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr);
				break;			
			default:
				System.out.println("unknown hierarchy level -- " + levelType);
				break;
			}			
			try {
				energyStr = mapper.writeValueAsString(energyList);
				System.out.println("after json -- " + energyStr);				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} else { //details=yes should be requested only at floor level
			List<SensorData> sensorList = new ArrayList<SensorData>();
			switch(levelType.intValue()) {
			case 4:
				sensorList = fecManager.getFloorSensorData(levelId, startDateStr, endDateStr);
				break;
			default:
				System.out.println("not authorized to request at a non floor level");
				break;
			}
			try {
				energyStr = mapper.writeValueAsString(sensorList);
				System.out.println("after json -- " + energyStr);
				System.out.println("length -- " + energyStr.length());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return energyStr;
		
	} //end of method getSensorData
		
	@Path("getEmLevelData/{custId}/{timeSpan}/{sDate}/{tDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getEmLevelData(@PathParam("custId") Long custId, 
			@PathParam("timeSpan") String timeSpan, @PathParam("sDate") String startDateStr, 
			@PathParam("tDate") String endDateStr) {
		
		System.out.println("inside the getEmLevelData");	
		ObjectMapper mapper = new ObjectMapper();
		String energyStr = new String();		
		System.out.println("time span -- " + timeSpan);		
		List<AggregatedEmData> energyList = new ArrayList<AggregatedEmData>();
		if(timeSpan.equals("5min")) {
			energyList = eecManager.getEm5minEnergyReadings(custId, startDateStr, endDateStr);			
		} else {
			energyList = eecManager.getEmHourlyEnergyReadings(custId, startDateStr, endDateStr);
		}
			
		try {
			energyStr = mapper.writeValueAsString(energyList);
			System.out.println("after json -- " + energyStr);				
		}
		catch(Exception e) {
			e.printStackTrace();
		}				
		return energyStr;
		
	} //end of method getFloorHourlyEnergyData
		
	/**
	 * Returns none
	 * 
	 * @return none
	 * services/ec/aggregateEnergyData/150/2013-04-08/2013-04-09
	 * this should not be used. this is for testing
	 */
	@Path("aggregateEnergyData/{custId}/{sdate}/{tdate}/{timeSpan}/{level}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void aggregateEnergyData(@PathParam("custId") Long custId, @PathParam("sdate") String startDateStr,
			@PathParam("tdate") String endDateStr, @PathParam("timeSpan") String timeSpan, @PathParam("level") String level) {
		
		System.out.println("inside the function aggregateEnergyData ---" + custId + " " + startDateStr + " " + endDateStr);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
  		
		Date startDate = new Date();
		try {
			startDate = df.parse(startDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
		Date endDate = new Date();
		try {
			endDate = df.parse(endDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(level.equals("floor")) {  		
			if(timeSpan.equals("hourly")) {
				fecManager.aggregateFloorHourlyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				fecManager.aggregateFloorDailyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("15min")) {
				fecManager.aggregateFloor15minEnergyReadings(custId, startDate, endDate);
			}
		} else if(level.equals("campus")) {
			if(timeSpan.equals("hourly")) {
				cecManager.aggregateCampusHourlyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				cecManager.aggregateCampusDailyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("15min")) {
				cecManager.aggregateCampus15minEnergyReadings(custId, startDate, endDate);
			}
		} else if(level.equals("bld")) {
			if(timeSpan.equals("hourly")) {
				becManager.aggregateBldHourlyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				becManager.aggregateBldDailyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("15min")) {
				becManager.aggregateBld15minEnergyReadings(custId, startDate, endDate);
			}
		} else if(level.equals("org")) {
			if(timeSpan.equals("hourly")) {
				oecManager.aggregateOrganizationHourlyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				oecManager.aggregateOrganizationDailyEnergyReadings(custId, startDate, endDate);
			} else if(timeSpan.equals("15min")) {
				oecManager.aggregateOrganization15minEnergyReadings(custId, startDate, endDate);
			}
		}
		
	} //end of method aggregateEnergyData
	
	public static void main(String args[]) {
		
		System.out.println("inside the main");
		Date toDate = new Date();		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(toDate);		
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(calTo.getTime());
		calFrom.add(Calendar.DAY_OF_MONTH, -1);
				
		//getFloorHourlyEnergyData(150, 2, calFrom.getTime(), toDate);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println(mapper.writeValueAsString(toDate));
			System.out.println("after json");
		}
		catch(JsonGenerationException e) {
			e.printStackTrace();
		}
		catch (JsonMappingException e) {			 
			e.printStackTrace();	 
		} catch (IOException e) {	 
			e.printStackTrace();	 
		}
		
	}
	      
} //end of class EcService
