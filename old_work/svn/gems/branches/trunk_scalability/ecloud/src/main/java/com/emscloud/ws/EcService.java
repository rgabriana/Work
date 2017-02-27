package com.emscloud.ws ;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

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

import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.Site;
import com.emscloud.service.BldEnergyConsumptionManager;
import com.emscloud.service.CampusEnergyConsumptionManager;
import com.emscloud.service.EMEnergyConsumptionManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FloorEnergyConsumptionManager;
import com.emscloud.service.OrganizationEnergyConsumptionManager;
import com.emscloud.service.SiteManager;
import com.emscloud.vo.AggregatedEmData;
import com.emscloud.vo.AggregatedEnergyData;
import com.emscloud.vo.AggregatedSensorData;
import com.emscloud.vo.RawEnergyData;
import com.emscloud.vo.SensorData;

import org.codehaus.jackson.map.ObjectMapper;

@Controller
@Path("/org/ec/v1")
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
	@Resource
	FacilityEmMappingManager facEmMappingManager;
	@Resource
	EmInstanceManager emInstManager;
	@Resource
	SiteManager siteManager;
	
	private static final Logger logger = Logger.getLogger("WSAPI");

	public EcService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	private FacilityEmMapping getFacilityEmMapping(long facilityId) {
		
		//get EM mapping 
		FacilityEmMapping facEmMapping = facEmMappingManager.getFacilityEmMappingOnFacilityId(facilityId);
		if(facEmMapping != null) {
			return facEmMapping;
		}
		//get the child facility
		List<Facility> childFacList = facilityManager.getChildFacilitiesByFacilityId(facilityId);
		Iterator<Facility> childFacIter = childFacList.iterator();
		while(childFacIter.hasNext()) {
			facEmMapping = getFacilityEmMapping(childFacIter.next().getId());
			if(facEmMapping != null) {
				break;
			}
		}
		return facEmMapping;
		
	} //end of method getFacilityEmMapping
	
	private int getMinuteTimeOffset(long levelId) {
		
		FacilityEmMapping facEmMapping = getFacilityEmMapping(levelId);		
		while(facEmMapping == null) {
			return 0;
		}
		Long emId = facEmMapping.getEmId();
		String timeZone = emInstManager.loadEmInstanceById(emId).getTimeZone();
		TimeZone tz = TimeZone.getTimeZone(timeZone);
		return tz.getRawOffset() / 1000 / 60;
		
	} //end of method getMinuteTimeOffset
	
	@Path("getAggregatedSiteData/{geoLoc}/{timeSpan}/{sDate}/{tDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<AggregatedEnergyData> getAggregatedSiteData(@PathParam("geoLoc") String geoLoc, @PathParam("timeSpan") String timeSpan,
			@PathParam("sDate") String startDateStr, @PathParam("tDate") String endDateStr) {
		
		Site site = siteManager.getSiteByGeoLocation(geoLoc);		
		//get facility of the site
		long levelId = facilityManager.getFacility(site.getName()).getId();
		//get time offset for facility		
		int tzOffset  = getMinuteTimeOffset(levelId);
		System.out.println("offset -- " + tzOffset);
		//adjust the start and end times if the level type is floor/building/campus
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar fromCal = Calendar.getInstance();
		Calendar toCal = Calendar.getInstance();
		try {
			Date startDate = df.parse(startDateStr);
			Date endDate = df.parse(endDateStr);
			toCal.setTime(endDate);				
			fromCal.setTime(startDate);
			fromCal.add(Calendar.MINUTE, -1 * tzOffset);
			toCal.add(Calendar.MINUTE, -1 * tzOffset);
			startDateStr = df.format(fromCal.getTime());				
			endDateStr = df.format(toCal.getTime());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		List<AggregatedEnergyData> energyList = new ArrayList<AggregatedEnergyData>();
		energyList = cecManager.getCampusAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr, tzOffset);			
		return energyList;
		
	} //end of method getAggregagedSiteData
	
	@Path("getRawSiteData/{geoLoc}/{sDate}/{tDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<RawEnergyData> getRawSiteData(@PathParam("geoLoc") String geoLoc, @PathParam("sDate") String startDateStr, 
			@PathParam("tDate") String endDateStr, @QueryParam("attributes") String attributes) {
				
		Site site = siteManager.getSiteByGeoLocation(geoLoc);
		List<Long> emIdList = siteManager.getSiteEms(site.getId());
		Iterator<Long> emIdIter = emIdList.iterator();
		List<RawEnergyData> rawDataList = new ArrayList<RawEnergyData>();
		while(emIdIter.hasNext()) {
			rawDataList.addAll(eecManager.getEm5minEnergyReadings(emIdIter.next(), startDateStr, endDateStr));
		}
		return rawDataList;
		
	} //end of method getRawSiteData
	
	private List<AggregatedSensorData> getAggSensorData(Long levelType, Long levelId, String timeSpan, String startDateStr, 
			String endDateStr) {
		
		//get time offset for facility		
		int tzOffset  = getMinuteTimeOffset(levelId);
		System.out.println("offset -- " + tzOffset);
		//adjust the start and end times if the level type is floor/building/campus
		if(levelType.intValue() == 2 || levelType.intValue() == 3 || levelType.intValue() == 4) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Calendar fromCal = Calendar.getInstance();
			Calendar toCal = Calendar.getInstance();
			try {
				Date startDate = df.parse(startDateStr);
				Date endDate = df.parse(endDateStr);
				toCal.setTime(endDate);				
				fromCal.setTime(startDate);
				fromCal.add(Calendar.MINUTE, -1 * tzOffset);
				toCal.add(Calendar.MINUTE, -1 * tzOffset);
				startDateStr = df.format(fromCal.getTime());				
				endDateStr = df.format(toCal.getTime());
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		List<AggregatedSensorData> energyList = new ArrayList<AggregatedSensorData>();
		switch(levelType.intValue()) {
		case 1:
			energyList = oecManager.getOrganizationAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr);			
			break;
		case 2:
			energyList = cecManager.getCampusAggregatedSensorData(levelId, timeSpan, startDateStr, endDateStr, tzOffset);			
			break;
		case 3:
			energyList = becManager.getBldAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr, tzOffset);
			break;
		case 4:
			energyList = fecManager.getFloorAggregatedEnergyData(levelId, timeSpan, startDateStr, endDateStr, tzOffset);
			break;			
		default:
			logger.error(" unknown hierarchy level -- " + levelType);
			break;
		}			
		return energyList;
		
	} //end of method getAggSensorData
	
	private List<SensorData> getRawSensorData(Long levelId, Long levelType, String startDateStr, String endDateStr, String attrib) {
		
		List<SensorData> sensorList = new ArrayList<SensorData>();
		if(levelType.intValue() == 4) {
			sensorList = fecManager.getFloorSensorData(levelId, startDateStr, endDateStr, attrib);
		} else {
			logger.error("not authorized to request at a non floor level");				
		}
		return sensorList;
		
	} //end of method getRawSensorData
				
	@Path("getSensorData/{levelId}/{levelType}/{timeSpan}/{sDate}/{tDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getSensorData(@PathParam("levelId") Long levelId, 
			@PathParam("levelType") Long levelType, @PathParam("timeSpan") String timeSpan,
			@PathParam("sDate") String startDateStr, @PathParam("tDate") String endDateStr,
			@QueryParam("details") Boolean details, @QueryParam("attributes") String attributes) {
		
		long apiTime = System.currentTimeMillis();
		ObjectMapper mapper = new ObjectMapper();
		String energyStr = new String();
		if(logger.isDebugEnabled()) {
			logger.debug("agg data -- " + details);
			logger.debug("level type -- " + levelType);
			logger.debug("time span -- " + timeSpan);
		}
		if(details == null) {
			details = false;
		}
		if(!details) {
			List<AggregatedSensorData> energyList = getAggSensorData(levelType, levelId, timeSpan, startDateStr, endDateStr);
			try {
				energyStr = mapper.writeValueAsString(energyList);
				if(logger.isDebugEnabled()) {
					logger.debug(" after json -- " + energyStr);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} else { 
			//details=yes should be requested only at floor level			
			if(!timeSpan.equals("5min")) {
				logger.error("authorized only request at 5 min time span");
			} else {
				try {
					List<SensorData> sensorList = getRawSensorData(levelId, levelType, startDateStr, endDateStr, attributes);
					energyStr = mapper.writeValueAsString(sensorList);
					if(logger.isDebugEnabled()) {
						logger.debug(" after json -- " + energyStr);
						logger.debug(" length -- " + energyStr.length());
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("get sensor data api time - " + (System.currentTimeMillis() - apiTime));
		}
		return energyStr;
		
	} //end of method getSensorData
		
	@Path("getAggregatedEmLevelData/{custId}/{timeSpan}/{sDate}/{tDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getAggregatedEmLevelData(@PathParam("custId") Long custId, 
			@PathParam("timeSpan") String timeSpan, @PathParam("sDate") String startDateStr, 
			@PathParam("tDate") String endDateStr) {
		
		long apiTime = System.currentTimeMillis();			
		ObjectMapper mapper = new ObjectMapper();
		String energyStr = new String();		
		if(logger.isDebugEnabled()) {
			logger.debug("inside the getEmLevelData, time span - " + timeSpan);
		}
		List<AggregatedEmData> energyList = new ArrayList<AggregatedEmData>();
		energyList = eecManager.getEmHourlyEnergyReadings(custId, startDateStr, endDateStr);
				
		try {
			energyStr = mapper.writeValueAsString(energyList);
			if(logger.isDebugEnabled()) {
				logger.debug("after json -- " + energyStr);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}				
		if(logger.isDebugEnabled()) {
			logger.debug("get em level time - " + (System.currentTimeMillis() - apiTime));
		}
		return energyStr;
		
	} //end of method getAggregatedEmLevelData
		
	/**
	 * Returns none
	 * 
	 * @return none
	 * services/ec/aggregateEm15minEnergyData/150/2013-04-08/2013-04-09
	 * this should not be used. this is for testing
	 */
	@Path("aggregateEm15minEnergyData/{emId}/{sdate}/{tdate}/{level}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void aggregateEm15minEnergyData(@PathParam("emId") Long emId, @PathParam("sdate") String startDateStr,
			@PathParam("tdate") String endDateStr, @PathParam("level") String level) {
		
		long apiTime = System.currentTimeMillis();
		if(logger.isDebugEnabled()) {
			logger.debug("inside the function aggregateEmEnergyData ---" + emId + " " + startDateStr + " " + endDateStr);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		Calendar fromCal = Calendar.getInstance();
		Calendar toCal = Calendar.getInstance();
		try {
			Date startDate = df.parse(startDateStr);			
			toCal.setTime(df.parse(endDateStr));
			Date toDate = toCal.getTime();
			while(toDate.after(startDate)) {
				fromCal.setTime(toDate);
				fromCal.add(Calendar.MINUTE, -15);				
				Date fromDate = fromCal.getTime();
				System.out.println("running 15min between " + fromDate + " and " + toDate);
				if(level.equals("floor")) {
					fecManager.aggregateEmFloor15minEnergyReadings(emId, fromDate, toDate);
				} else if(level.equals("bld")) {
					becManager.aggregateEmBld15minEnergyReadings(emId, fromDate, toDate);
				} else if(level.equals("campus")) {
					cecManager.aggregateEmCampus15minEnergyReadings(emId, fromDate, toDate);
				}
				toDate = fromDate;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}		
		if(logger.isDebugEnabled()) {
			logger.debug("agg api time - " + (System.currentTimeMillis() - apiTime));
		}
		
	} //end of method aggregateEm15minEnergyData
	
	/**
	 * Returns none
	 * 
	 * @return none
	 * services/ec/aggregateEmEnergyData/150/2013-04-08/2013-04-09
	 * this should not be used. this is for testing
	 */
	@Path("aggregateEmEnergyData/{emId}/{sdate}/{tdate}/{timeSpan}/{level}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void aggregateEmEnergyData(@PathParam("emId") Long emId, @PathParam("sdate") String startDateStr,
			@PathParam("tdate") String endDateStr, @PathParam("timeSpan") String timeSpan, 
			@PathParam("level") String level) {
		
		long apiTime = System.currentTimeMillis();
		if(logger.isDebugEnabled()) {
			logger.debug("inside the function aggregateEmEnergyData ---" + emId + " " + startDateStr + " " + endDateStr);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  		
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
				fecManager.aggregateEmFloorHourlyEnergyReadings(emId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				fecManager.aggregateEmFloorDailyEnergyReadings(emId, startDate, endDate);
			}
		} else if(level.equals("campus")) {
			if(timeSpan.equals("hourly")) {
				cecManager.aggregateEmCampusHourlyEnergyReadings(emId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				cecManager.aggregateEmCampusDailyEnergyReadings(emId, startDate, endDate);
			} 
		} else if(level.equals("bld")) {
			if(timeSpan.equals("hourly")) {
				becManager.aggregateEmBldHourlyEnergyReadings(emId, startDate, endDate);
			} else if(timeSpan.equals("daily")) {
				becManager.aggregateEmBldDailyEnergyReadings(emId, startDate, endDate);
			} 	
		}
		if(logger.isDebugEnabled()) {
			logger.debug("agg api time - " + (System.currentTimeMillis() - apiTime));
		}
		
	} //end of method aggregateEmEnergyData
	
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
			@PathParam("tdate") String endDateStr, @PathParam("timeSpan") String timeSpan, 
			@PathParam("level") String level) {
		
		long apiTime = System.currentTimeMillis();
		if(logger.isDebugEnabled()) {
			logger.debug("inside the function aggregateEnergyData ---" + custId + " " + startDateStr + " " + endDateStr);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  		
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
		if(logger.isDebugEnabled()) {
			logger.debug("agg api time - " + (System.currentTimeMillis() - apiTime));
		}
		
	} //end of method aggregateEnergyData
	
	@Path("aggregateTotalEnergyData/{custId}/{sdate}/{tdate}/{timeSpan}/{level}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void aggregateTotalEnergyData(@PathParam("custId") Long custId, @PathParam("sdate") String startDateStr,
			@PathParam("tdate") String endDateStr, @PathParam("timeSpan") String timeSpan, 
			@PathParam("level") String level) {
		
		long apiTime = System.currentTimeMillis();
		//String endDateStr = "2013-12-01 05:00";
		//String startDateStr = "2013-05-01 04:00";
				
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar fromCal = Calendar.getInstance();
		Calendar toCal = Calendar.getInstance();
		try {
			Date startDate = df.parse(startDateStr);
			//hourly
			toCal.setTime(df.parse(endDateStr));
			Date toDate = toCal.getTime();
			while(toDate.after(startDate)) {
				fromCal.setTime(toDate);
				if(timeSpan.equals("hourly")) {
					fromCal.add(Calendar.HOUR, -1);
				} else if(timeSpan.equals("daily")) {
					fromCal.add(Calendar.DAY_OF_MONTH, -1);
				} else if(timeSpan.equals("15min")) {
					fromCal.add(Calendar.MINUTE, -15);
				}
				Date fromDate = fromCal.getTime();
				System.out.println("running " + timeSpan + " between " + fromDate + " and " + toDate);
				if(level.equals("org")) {
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "org");
				} else if(level.equals("bld")) {
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "bld");
				} else if(level.equals("campus")) {
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "campus");
				} else if(level.equals("floor")) {
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "floor");
				} else if(level.equals("all")) {
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "floor");
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "bld");
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "campus");
					aggregateEnergyData(custId, df.format(fromDate), df.format(toDate), timeSpan, "org");
				}
				toDate = fromDate;
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(logger.isDebugEnabled()) {
			logger.debug("total agg time - " + (System.currentTimeMillis() - apiTime));
		}
		
	}
	
	public static void main(String args[]) {
		
		try {
			String endDateStr = "2013-12-01 00:00";
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
			System.out.println("inside the main");
			Calendar calTo = Calendar.getInstance();
		
			calTo.setTime(df.parse(endDateStr));		
			calTo.set(Calendar.MINUTE, 0);
			calTo.set(Calendar.SECOND, 0);
			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(calTo.getTime());
			calFrom.add(Calendar.HOUR, -1);
				
			//getFloorHourlyEnergyData(150, 2, calFrom.getTime(), toDate);
		
			//ObjectMapper mapper = new ObjectMapper();
					
			System.out.println("form date -- " + calFrom.getTime());
			System.out.println("to date -- " + calTo.getTime());
				
			System.out.println("from str -- " + df.format(calFrom.getTime()));
			
		}
		catch (Exception e) {	 
			e.printStackTrace();	 
		}
		
	} //end of method main
	      
} //end of class EcService
