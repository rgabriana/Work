/**
 * 
 */
package com.ems.ws;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.ems.model.Avgrecord;
import com.ems.model.DashboardRecord;
import com.ems.model.GroupECRecord;
import com.ems.model.MeterRecord;
import com.ems.service.EnergyConsumptionManager;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/ec")
public class EnergyConsumptionService {
    @Resource(name = "energyConsumptionManager")
    private EnergyConsumptionManager energyConsumptionManager;

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
    @Path("cp/{property}/{pid}/{fixedperiod}/{fdate:.*}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Avgrecord> getEC(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("fixedperiod") String fixedperiod, @PathParam("fdate") String fdate) {
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
    @Path("fp/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Avgrecord> getECInPeriod(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
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
    @Path("md/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<MeterRecord> getMeterDataInPeriod(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
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
    @Path("mds/{property}/{pid}/{fixedperiod}/{fdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getDashboardMeterDataInFixedPeriod(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fixedperiod") String fixedperiod, @PathParam("fdate") String fdate) {
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
            // protection against null power_used / base_power_used
        }
        return null;
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
    @Path("fp/mds/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getDashboardMeterDataInPeriod(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
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
     * Returns the meter data savings dashboard record for one hour
     * 
     * @param pid
     *            property unique identifier
     * @param property
     *            (company|campus|building|floor|area|fixture)
     * @return Dashboard records for an hour
     */
    @Path("mds/{property}/{pid}/hour")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DashboardRecord> getHourlyDashboardDetails(@PathParam("property") String property,
            @PathParam("pid") Long pid) {
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
    @Path("gmd/{property}/{pid}/{fdate}/{tdate}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GroupECRecord> loadGroupEnergyConsumption(@PathParam("property") String property,
            @PathParam("pid") Long pid, @PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
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

}
