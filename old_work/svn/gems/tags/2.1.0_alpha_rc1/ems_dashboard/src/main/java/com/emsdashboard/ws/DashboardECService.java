package com.emsdashboard.ws;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.hibernate.classic.Session;
import org.springframework.stereotype.Controller;


import com.emsdashboard.model.Avgrecord;
import com.emsdashboard.model.DashboardRecord;
import com.emsdashboard.model.GemsServer;
import com.emsdashboard.model.MeterRecord;
import com.emsdashboard.service.DashboardDataManager;

@Controller
@Path("org/ecDashboard")
public class DashboardECService{
    static final Logger logger = Logger.getLogger(DashboardECService.class.getName());
    
    @Resource(name = "dashboardDataManager")
    private DashboardDataManager dashboardDataManager;
    
    @Context
    ServletContext context;
    
    /**
     * Return Meter data with range in percentage
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fdate
     *            starting date back to the specified period.
     * @param tdate
     *            ending date in specified period.
     * @param gemID
     *            GEMS Id
     */
    @Path("md/{property}/{pid}/{fdate}/{tdate}/{gemID}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public List<MeterRecord> loadMeterDataWithDateRange( @PathParam("property") String property,@PathParam("pid") String pid,@PathParam("fdate") String fdate,@PathParam("tdate") String tdate,@PathParam("gemID") String gemID) {
        try
        {
            logger.info("loadMeterDataWithDateRange webService called " +  property);
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
            //return gemList;
            return dashboardDataManager.loadMeterDataWithDateRange(property,pid,oFDate, oTDate,gemID);
        }
        catch(Exception e)
        {
            logger.info("loadMeterDataWithDateRange Service Failed");
        }
        return null;
    }
    
    /**
     * Return energy consumption for mini gems
     * 
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @param pid
     *            property unique identifier
     * @param fixedperiod
     *            (CURRENT|DAY|WEEK|MONTH|YEAR)
     * @param fdate
     *            starting date back to the specified period.
     * @param ip
     *            GEMS Id
     */
    @Path("cp/{property}/{pid}/{fixedperiod}/{fdate:.*}/{gemID}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public List<Avgrecord> loadEnergyConsumption(@PathParam("property") String property,@PathParam("pid") Long pid,@PathParam("fixedperiod") String fixedperiod,@PathParam("fdate") String fdate,@PathParam("gemID") String gemID)
    {
        try
        {
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
                return dashboardDataManager.loadRecentEnergyConsumption(pid,property, oFDate,gemID);
            } else if (fixedperiod.equalsIgnoreCase("DAY")) {
                // returns from energy_consumption_hourly table (by hour)
                return dashboardDataManager.loadDayEnergyConsumption(pid,property, oFDate,gemID);
            } else if (fixedperiod.equalsIgnoreCase("WEEK")) {
                // returns from energy_consumption_hourly table (by day)
                return dashboardDataManager.loadWeekEnergyConsumption(pid,property, oFDate,gemID);
            } else if (fixedperiod.equalsIgnoreCase("MONTH")) {
                // returns from energy_consumption_hourly table (by day)
                return dashboardDataManager.loadMonthEnergyConsumption(pid,property, oFDate,gemID);
            } else if (fixedperiod.equalsIgnoreCase("YEAR")) {
                // returns from energy_consumption_daily table (by month)
                return dashboardDataManager.loadYearEnergyConsumption(pid, property, oFDate,gemID);
            }
        }catch(Exception e)
        {
            logger.info("loadEnergyConsumption Service Failed");
        }
        return null;
    }
    
   
}
