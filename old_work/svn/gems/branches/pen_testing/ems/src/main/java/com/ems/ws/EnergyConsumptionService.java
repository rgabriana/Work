/**
 * 
 */
package com.ems.ws;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.cache.BallastCache;
import com.ems.hvac.utils.JsonUtil;
import com.ems.model.Avgrecord;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.DashboardRecord;
import com.ems.model.Fixture;
import com.ems.model.GroupECRecord;
import com.ems.model.MeterRecord;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.vo.model.BallastVoltPowerCurve;
import com.ems.vo.model.FixtureStatsDetail;
import com.ems.vo.model.VoltPowerCurveValue;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/ec")
public class EnergyConsumptionService {
    @Resource(name = "energyConsumptionManager")
    private EnergyConsumptionManager energyConsumptionManager;
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    
    @Autowired
    private MessageSource messageSource;
    @Resource
    private SystemConfigurationManager systemConfigurationManager;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");
    public EnergyConsumptionService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Return energy consumption TODO: Needs testing
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fixedperiod
     *            (CURRENT|DAY|WEEK|MONTH|YEAR)
     * @param fdate
     *            starting date back to the specified period.
     * @return Avgrecord list describing the energyconsumption for given period from the start date
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("cp/{property}/{pid}/{fixedperiod}/{fdate:.*}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Avgrecord> getEC(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("fixedperiod") String fixedperiod, @PathParam("fdate") String fdate) {
        Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fixedperiod", fixedperiod);
    	nameValMap.put("fromDate", fdate);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oCalendar.setTime(oFDate);

        if (property.equalsIgnoreCase("company")) {
            property = "company";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture")) {
            property = "id";
        }

        if (fixedperiod.equalsIgnoreCase("CURRENT")) {
            // TODO: Only return latest 5-10 mins status... This is currently
            // returning it from the energy_consumption table for the day
            return energyConsumptionManager.loadRecentEnergyConsumption(pid, property, oFDate);
        } else if (fixedperiod.equalsIgnoreCase("DAY")) {
            // returns from energy_consumption_hourly table (by hour)
            return energyConsumptionManager.loadDayEnergyConsumption(pid, property, oFDate);
        } else if (fixedperiod.equalsIgnoreCase("WEEK")) {
            // returns from energy_consumption_hourly table (by day)
            return energyConsumptionManager.loadWeekEnergyConsumption(pid, property, oFDate);
        } else if (fixedperiod.equalsIgnoreCase("MONTH")) {
            // returns from energy_consumption_hourly table (by day)
            return energyConsumptionManager.loadMonthEnergyConsumption(pid, property, oFDate);
        } else if (fixedperiod.equalsIgnoreCase("YEAR")) {
            // returns from energy_consumption_daily table (by month)
            return energyConsumptionManager.loadYearEnergyConsumption(pid, property, oFDate);
        }
        return null;
    }

    /**
     * FIXME: Returns the energy consumption within defined period
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fdate
     *            latest date
     * @param tdate
     *            older date
     * @return Avgrecord list describing the energyconsumption for the given period.
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("fp/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Avgrecord> getECInPeriod(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<Avgrecord>();
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oFCalendar = Calendar.getInstance();
        Calendar oTCalendar = Calendar.getInstance();
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oFCalendar.setTime(oFDate);
        oFCalendar.set(Calendar.SECOND, 00);

        oTCalendar.setTime(oFCalendar.getTime());
        oTCalendar.set(Calendar.DAY_OF_MONTH, -7);

        try {
            oTDate = inputFormat.parse(tdate);
        } catch (ParseException pe) {
            oTDate = oTCalendar.getTime();
        }
        oTCalendar.setTime(oTDate);
        oTCalendar.set(Calendar.SECOND, 00);
        // FIXME: based on respective period.
        return energyConsumptionManager.loadDayEnergyConsumption(1L, "company", oFDate);
    }

    /**
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fdate
     *            older date
     * @param tdate
     *            latest date
     * @return
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("md/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<MeterRecord> getMeterDataInPeriod(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<MeterRecord>();
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oFCalendar = Calendar.getInstance();
        Calendar oTCalendar = Calendar.getInstance();
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oFCalendar.setTime(oFDate);
        oFCalendar.set(Calendar.SECOND, 00);

        try {
            oTDate = inputFormat.parse(tdate);
        } catch (ParseException pe) {
            oTDate = oTCalendar.getTime();
        }
        oTCalendar.setTime(oTDate);
        oTCalendar.set(Calendar.SECOND, 00);

        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        }
        return energyConsumptionManager.loadMeterData(property, pid.toString(), oFDate, oTDate);
    }

    /**
     * Returns meter data savings for a fixed period for dashboard data.
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param fixedperiod
     *            (CURRENT|DAY|WEEK|MONTH|YEAR)
     * @param fdate
     *            from date back to the fixedperiod
     * @return Dashboard records
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("mds/{property}/{pid}/{fixedperiod}/{fdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getDashboardMeterDataInFixedPeriod(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fixedperiod") String fixedperiod, @PathParam("fdate") String fdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fixedperiod", fixedperiod);
    	nameValMap.put("fromDate", fdate);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        } catch (NullPointerException npe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oCalendar.setTime(oFDate);

        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group")) {
            property = "group_id";
        }
        try {
            if (fixedperiod.equalsIgnoreCase("CURRENT")) {
                return energyConsumptionManager.loadRecentSummary(pid, property, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("DAY")) {
                return energyConsumptionManager.loadDaySummary(pid, property, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("WEEK")) {
                return energyConsumptionManager.loadWeekSummary(pid, property, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("MONTH")) {
                return energyConsumptionManager.loadMonthSummary(pid, property, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("YEAR")) {
                return energyConsumptionManager.loadYearSummary(pid, property, oFDate);
            }
        }catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    

    
    /**
     * Returns meter data savings for a fixed period for dashboard data based on device family
     * @param deviceFamily
     *           (lighting|managedpluglad|allplugload)
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture|group|plugload|plugloadgroup)
     * @param fixedperiod
     *            (CURRENT|DAY|WEEK|MONTH|YEAR)
     * @param fdate
     *            from date back to the fixedperiod
     * @return Dashboard records
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor')")
    @Path("mds/{deviceFamily}/{property}/{pid}/{fixedperiod}/{fdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getDeviceBasedDashboardMeterDataInFixedPeriod(@PathParam("property") String property, @PathParam("deviceFamily") String deviceFamily,
            @PathParam("pid") Long pid, @PathParam("fixedperiod") String fixedperiod, @PathParam("fdate") String fdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fixedperiod", fixedperiod);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("deviceFamily", deviceFamily);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        } catch (NullPointerException npe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oCalendar.setTime(oFDate);

        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture") || property.equalsIgnoreCase("plugload")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group") || property.equalsIgnoreCase("plugloadgroup")) {
            property = "group_id";
        }
        try {
            if (fixedperiod.equalsIgnoreCase("CURRENT")) {
            	if("managedplugload".equalsIgnoreCase(deviceFamily)){
            		return energyConsumptionManager.loadRecentSummaryForManagedPlugload(pid, property, oFDate);            		
            	}else if("allplugload".equalsIgnoreCase(deviceFamily)){            		
            		return energyConsumptionManager.loadRecentSummaryForAllPlugload(pid, property, oFDate);
            }else{
            	return energyConsumptionManager.loadRecentSummary(pid, property, oFDate);
            }
            }
        }catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * Returns aggregated meter data savings for a fixed period for children nodes.
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param fixedperiod
     *            (DAY|WEEK|MONTH|YEAR)
     * @param fdate
     *            from date back to the fixedperiod
     * @return Dashboard records
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("mds/child/aggregate/{property}/{pid}/{fixedperiod}/{fdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getChildrenDashboardMeterDataInFixedPeriod(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fixedperiod") String fixedperiod, @PathParam("fdate") String fdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fixedperiod", fixedperiod);
    	nameValMap.put("fromDate", fdate);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        } catch (NullPointerException npe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oCalendar.setTime(oFDate);
        String child = "";
        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
            child = "campus_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
            child = "building_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
            child = "floor_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
            child = "area_id";
        } else {
        	return null;
        }
        try {
        	if (fixedperiod.equalsIgnoreCase("DAY")) {
                return energyConsumptionManager.loadChildrenDaySummary(pid, property,child, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("WEEK")) {
                return energyConsumptionManager.loadChildrenWeekSummary(pid, property, child, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("MONTH")) {
                return energyConsumptionManager.loadChildrenMonthSummary(pid, property,child, oFDate);
            } else if (fixedperiod.equalsIgnoreCase("YEAR")) {
                return energyConsumptionManager.loadChildrenYearSummary(pid, property,child, oFDate);
            }
        }catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    /**
     * returns aggregated meter data savings for flexible period for children nodes.
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor)
     * @param fdate
     *            latest date
     * @param tdate
     *            oldest date
     * @return Dashboard records
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("fp/mds/child/aggregate/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getChildrenDashboardMeterDataInPeriod(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        try {
            oTDate = inputFormat.parse(tdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        
        String child = "";
        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
            child = "campus_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
            child = "building_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
            child = "floor_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
            child = "area_id";
        } else {
        	return null;
        }
        return energyConsumptionManager.loadChildrenSummaryInPeriod(pid, property, child, oFDate, oTDate);
    }

    /**
     * Returns the meter data savings dashboard record for flexible period
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param fdate
     *            latest date
     * @param tdate
     *            oldest date
     * @return Dashboard records
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor')")
    @Path("fp/mds/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getDashboardMeterDataInPeriod(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<DashboardRecord>();
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        try {
            oTDate = inputFormat.parse(tdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }

        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group")) {
            property = "group_id";
        }
        return energyConsumptionManager.loadSummaryInPeriod(pid, property, oFDate, oTDate);
    }


    /**
     * Returns the meter data savings dashboard record for flexible period based on the device family.
     * 
     * @param pid
     *            property unique identifier
     * @param deviceFamily
     * 			  (lighting|plugload|allplugload)           
     * @param property
     *            (company|campus|building|floor|area|fixture|group|plugload|plugloadgroup)
     * @param fdate
     *            latest date
     * @param tdate
     *            oldest date
     * @return Dashboard records
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor')")
    @Path("fp/mds/{deviceFamily}/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getDashboardMeterDataByDeviceInPeriod(@PathParam("deviceFamily") String deviceFamily ,@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("deviceFamily", deviceFamily);
        nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<DashboardRecord>();
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        try {
            oTDate = inputFormat.parse(tdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture") || property.equalsIgnoreCase("plugload") ) {
            property = "id";
        } else if (property.equalsIgnoreCase("group") || property.equalsIgnoreCase("plugloadgroup")) {
            property = "group_id";
        }
        
        if("managedplugload".equalsIgnoreCase(deviceFamily)){
        	return energyConsumptionManager.loadSummaryInPeriodForManagedPlugload(pid, property, oFDate, oTDate);
        }else if("allplugload".equalsIgnoreCase(deviceFamily)){
        	return energyConsumptionManager.loadSummaryInPeriodForAllPlugload(pid, property, oFDate, oTDate);        	
        }else{
        	return energyConsumptionManager.loadSummaryInPeriod(pid, property, oFDate, oTDate);
        }
        
        
    }
    
    
    /**
     * Returns the meter data savings dashboard record for one hour
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @return Dashboard records for an hour
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("mds/{property}/{pid}/hour")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getHourlyDashboardDetails(@PathParam("property") String property,
            @PathParam("pid") Long pid) {
    	Response resp = new Response();
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "property", property);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<DashboardRecord>();
    	}
    	if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group")) {
            property = "group_id";
        }
        return energyConsumptionManager.loadhourSummary(pid, property);
    }
    
    /**
     * Returns the meter data savings dashboard record for one hour
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @return Dashboard records for an hour in GMT time zone
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("mds/{property}/{pid}/hour/{from}/{to}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getHourlyDashboardDetailsBetween(@PathParam("property") String property,
            @PathParam("pid") Long pid,@PathParam("from") String from,@PathParam("to") String to) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("property", property);
    	nameValMap.put("fromDate", from);
    	nameValMap.put("toDate", to);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<DashboardRecord>();
    	}
    	if (property.equalsIgnoreCase("company")) {
            property = "company_id";
        } else if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group")) {
            property = "group_id";
        }
        DateFormat formatter ;
         formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date dFrom = null;
        Date dTo = null;
		try {
			dFrom = (Date)formatter.parse(from);
		    dTo = (Date)formatter.parse(to);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
     
       return energyConsumptionManager.loadHourSummaryBetween(pid, property, dFrom, dTo);

    }

    /**
     * Fetches individual groups summary in the specified time interval w.r.t org level
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fdate
     *            newer date
     * @param tdate
     *            older date
     * @return GroupECRecord list
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
    @Path("gmd/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GroupECRecord> loadGroupEnergyConsumption(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<GroupECRecord>();
    	}
    	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oFrom = Calendar.getInstance();
        Calendar oTo = Calendar.getInstance();
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oFrom.setTime(oFDate);
        try {
            oTDate = inputFormat.parse(tdate);
            oTo.setTime(oTDate);
        } catch (ParseException pe) {
            oTo.setTime(oFDate);
            oTo.add(Calendar.MINUTE, -10);
            oTo.set(Calendar.SECOND, 00);
        }

        if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("fixture")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group")) {
            property = "group_id";
        }
        return energyConsumptionManager.loadGroupEnergyConsumption(property, pid, oFrom.getTime(), oTo.getTime());
    }
    
    /**
     * Fetches individual groups summary in the specified time interval w.r.t org level
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fdate
     *            newer date
     * @param tdate
     *            older date
     * @return GroupECRecord list
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
    @Path("plugloadgmd/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GroupECRecord> loadPlugloadGroupEnergyConsumption(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    //    System.out.println("==========================inside");
        Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<GroupECRecord>();
    	}
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oFrom = Calendar.getInstance();
        Calendar oTo = Calendar.getInstance();
        Date oFDate = null;
        Date oTDate = null;
        try {
            oFDate = inputFormat.parse(fdate);
        } catch (ParseException pe) {
            oFDate = new Date(System.currentTimeMillis());
        }
        oFrom.setTime(oFDate);
        try {
            oTDate = inputFormat.parse(tdate);
            oTo.setTime(oTDate);
        } catch (ParseException pe) {
            oTo.setTime(oFDate);
            oTo.add(Calendar.MINUTE, -10);
            oTo.set(Calendar.SECOND, 00);
        }

        if (property.equalsIgnoreCase("campus")) {
            property = "campus_id";
        } else if (property.equalsIgnoreCase("building")) {
            property = "building_id";
        } else if (property.equalsIgnoreCase("floor")) {
            property = "floor_id";
        } else if (property.equalsIgnoreCase("area")) {
            property = "area_id";
        } else if (property.equalsIgnoreCase("plugload")) {
            property = "id";
        } else if (property.equalsIgnoreCase("group")) {
            property = "group_id";
        }
        return energyConsumptionManager.loadPlugloadGroupEnergyConsumption(property, pid, oFrom.getTime(), oTo.getTime());
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("stats/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FixtureStatsDetail> loadStatsInDateRange(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
    	
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("property", property);
    	nameValMap.put("fromDate", fdate);
    	nameValMap.put("toDate", tdate);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<FixtureStatsDetail>();
    	}
    	
      SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      Calendar oFrom = Calendar.getInstance();
      Calendar oTo = Calendar.getInstance();
      Date oFDate = null;
      Date oTDate = null;
      try {
	oFDate = inputFormat.parse(fdate);
      } catch (ParseException pe) {
	oFDate = new Date(System.currentTimeMillis());
      }
      oFrom.setTime(oFDate);
      try {
	oTDate = inputFormat.parse(tdate);
	oTo.setTime(oTDate);
      } catch (ParseException pe) {
	oTo.setTime(oFDate);
	oTo.add(Calendar.MINUTE, -10);
	oTo.set(Calendar.SECOND, 00);
      }
      
      if (property.equalsIgnoreCase("campus")) {
	property = "campus_id";
      } else if (property.equalsIgnoreCase("building")) {
	property = "building_id";
      } else if (property.equalsIgnoreCase("floor")) {
	property = "floor_id";
      } else if (property.equalsIgnoreCase("area")) {
	property = "area_id";
      } else if (property.equalsIgnoreCase("fixture")) {
	property = "id";
      } else if (property.equalsIgnoreCase("group")) {
	property = "group_id";
      }
      ArrayList<FixtureStatsDetail> statsList = new ArrayList<FixtureStatsDetail>();
      List<Object[]> results = energyConsumptionManager.loadStatsInDateRange(property, 
	  pid, oFrom.getTime(), oTo.getTime());
      if(results == null) {
	return statsList;
      }
      //System.out.println("no. of entries-- " + results.size());
      Iterator<Object[]> ecIter = results.iterator();
      FixtureStatsDetail details = null;      
      while(ecIter.hasNext()) {
	Object[] data = ecIter.next();
	details = new FixtureStatsDetail();
	details.setId((BigInteger)data[0]);
	details.setPower((BigDecimal)data[1]);
	details.setAvgVolts(((Integer)data[2]).shortValue());
	details.setTemperature((Double)data[3]);
	details.setOccupancy((BigInteger)data[4]);
	details.setAvgAmbient(((Integer)data[5]).shortValue());
	details.setStatsTime((Date)data[6]);
	statsList.add(details);
      }
      return statsList;
      
    } //end of method loadStatsInDateRange
    
    /**
     * Fetches Ballast volt Power curve data for the ballast specified by the fixture id
     * 
     * @param ids
     *            comma separated fixture id unique identifier
     * @return Ballast volt power curve along with ballast information
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("stats/calibrateSwMetering/{ids}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public BallastVoltPowerCurve calibrateSwMetering(@PathParam("ids") String ids) {
    	BallastVoltPowerCurve pwrCurve = new BallastVoltPowerCurve();
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("selectedfixtures", ids);    // pattern similar to comma separated fixture ids. hence reused. 	
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return pwrCurve;
    	}	
    	
      StringTokenizer st = new StringTokenizer(ids, ",");
      int noOfSus = st.countTokens();
      StringBuffer sb = new StringBuffer();
      Fixture fixture = null;
      Ballast ballast = null;
      ArrayList<Long> fixtureList = new ArrayList<Long>();
      long fixtureId = -1;
      
      
      StringBuffer sbStatus = new StringBuffer();
      for(int i = 0; i < noOfSus; i++) {
	try {
	  fixtureId = Long.parseLong(st.nextToken().toString());
	  fixture = fixtureManager.getFixtureById(fixtureId);
	}
	catch(NumberFormatException nfe) {
	  //System.out.println("invalid ids");
	  pwrCurve.setStatus("invalid fixture ids");	  
	  return pwrCurve;
	}	
	if(ballast != null) {
	  if(!ballast.getId().equals(fixture.getBallast().getId())) {
	    pwrCurve.setStatus("All the selected fixtures are not configured with the same ballast - " +
		ballast.getId() + " < > " + fixture.getBallast().getId());
	    return pwrCurve;
	  }
	}
	ballast = fixture.getBallast();
	if(!ServerUtil.isNewCU(fixture)) {
	  sbStatus.append(fixtureId + ": Calibration cannot be perfrmed with old CU;");
	  continue;
	}
	String version = fixture.getVersion();
	int bPos = version.indexOf('b');
	if(bPos <= 0) {
	  //System.out.println(fixtureId + ": wrong version - " + version);
	  sbStatus.append(fixtureId + ": version without build no. - " + version + ";");
	  continue;
	}
	String bld = version.substring(bPos + 1);
	if(bld == null) {
	  //System.out.println(fixtureId + ": no build no. - " + version);
	  sbStatus.append(fixtureId + ": version without build no. - " + version + ";");
	  continue;
	}
	try {
	  int bldNo = Integer.parseInt(bld);
	  if(bldNo <= 1005) { //1005 is the manufactured bld no. which has fx stats load issue
	    sbStatus.append(fixtureId + ": Calibration cannot be perfrmed with version - " + 
		version + ";");
	    continue;
	  }
	  fixtureList.add(fixtureId);
	  if(fixtureList.size() > 1) {
	    sb.append(",");
	  }
	  sb.append(fixture.getFixtureName());
	}
	catch(NumberFormatException nfe) {
	  //System.out.println(fixtureId + ": build no. is not correct " + bld);
	  sbStatus.append(fixtureId + ": wrong build no. - " + version + ";");
	}	
      }
      
      if(fixtureList.size() > 0) {
	Long[] fixtureIds = (Long[])fixtureList.toArray(new Long[fixtureList.size()]);
	try {
	  Collection<VoltPowerCurveValue> voltCurveList = 
	      DeviceServiceImpl.getInstance().calibrateSwMetering(fixtureIds);	  
	  pwrCurve.setFixtureName(sb.toString());
	  pwrCurve.setBallast(ballast);
	  if(voltCurveList == null) {
	    sbStatus.append("Software metering calibration failed");
	  } else {
	    int noOfEntries = voltCurveList.size();
	    if(noOfEntries > 0) {
	      pwrCurve.setVoltPowerCurveMap(voltCurveList);
	      if(noOfEntries < 21) {
		sbStatus.append("Software metering calibration is partially successful");
	      }
	    } else {
	      sbStatus.append("Software metering calibration failed");
	    }
	  }
	}
	catch(Exception ex) {
	  sbStatus.append(ex.getMessage() + ";");
	}
      }
      if(sbStatus.toString().length() == 0) {
	pwrCurve.setStatus("Software metering calibration successful");
      } else {
	pwrCurve.setStatus(sbStatus.toString());
      }
      return pwrCurve;
	      
    } //end of method calibrateSwMetering
	
    /**
     * Fetches Ballast volt Power curve data for the ballast specified by the fixture id
     * 
     * @param id
     *            fixture id unique identifier
     * @return Ballast volt power curve
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("stats/getSwMeterCalibration/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<BallastVoltPower> getSwMeterCalibration(@PathParam("id") Long id) {
		  
      Fixture fixture = fixtureManager.getFixtureById(id);
      Ballast ballast = fixture.getBallast();
      
      HashMap<Double, Double> curveMap = BallastCache.getInstance().getVoltPowerCurveMap(ballast);
      Iterator<Double> iter = curveMap.keySet().iterator();
      Double volt = null;	
      BallastVoltPower bVoltPwr = null;
      List<BallastVoltPower> list = new ArrayList<BallastVoltPower>();
      while(iter.hasNext()) {
	volt = iter.next();
	bVoltPwr = new BallastVoltPower();
	bVoltPwr.setBallastId(0L);
	bVoltPwr.setPower(curveMap.get(volt));
	bVoltPwr.setVolt(volt);
	list.add(bVoltPwr);
      }
      return list;
      
    } //end of method calibrateSwMetering
    
    
    
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getMotionBits/{lastSyncId}/{limit}")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getMotionBits(@PathParam("lastSyncId") String lastSyncId, @PathParam("limit") String limit) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("ecservice.limit", limit);
        nameValMap.put("lastSyncId", lastSyncId);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return "";
    	}
		
		Long lastECSyncId = Long.parseLong(lastSyncId);
		Long limitRows = Long.parseLong(limit);
		return JsonUtil.getJSONString(energyConsumptionManager.getMotionBits(lastECSyncId, limitRows));
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("getAvgTemperature/{lastSyncId}/{limit}")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getAvgTemperature(@PathParam("lastSyncId") String lastSyncId, @PathParam("limit") String limit) {
		
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("ecservice.limit", limit);
        nameValMap.put("lastSyncId", lastSyncId);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return "";
    	}
        
		Long lastECSyncId = Long.parseLong(lastSyncId);
		Long limitRows = Long.parseLong(limit);
		return JsonUtil.getJSONString(energyConsumptionManager.getAvgTemperature(lastECSyncId, limitRows));
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("getAmbientLight/{lastSyncId}/{limit}")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getAmbientLight(@PathParam("lastSyncId") String lastSyncId, @PathParam("limit") String limit) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("ecservice.limit", limit);
        nameValMap.put("lastSyncId", lastSyncId);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return "";
    	}
        
		Long lastECSyncId = Long.parseLong(lastSyncId);
		Long limitRows = Long.parseLong(limit);
		return JsonUtil.getJSONString(energyConsumptionManager.getAmbientLight(lastECSyncId, limitRows));
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("getSensorPower/{lastSyncId}/{limit}")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getSensorPower(@PathParam("lastSyncId") String lastSyncId, @PathParam("limit") String limit) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("ecservice.limit", limit);
        nameValMap.put("lastSyncId", lastSyncId);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return "";
    	}
        
		Long lastECSyncId = Long.parseLong(lastSyncId);
		Long limitRows = Long.parseLong(limit);
		return JsonUtil.getJSONString(energyConsumptionManager.getSensorPower(lastECSyncId, limitRows));
	}

}
