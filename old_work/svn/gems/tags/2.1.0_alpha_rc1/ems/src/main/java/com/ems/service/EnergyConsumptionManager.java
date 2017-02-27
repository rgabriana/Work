package com.ems.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.EnergyConsumptionDao;
import com.ems.model.AvgBarChartRecord;
import com.ems.model.Avgrecord;
import com.ems.model.DRRecord;
import com.ems.model.DashboardRecord;
import com.ems.model.EnergyConsumption;
import com.ems.model.GroupECRecord;
import com.ems.model.MeterRecord;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("energyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EnergyConsumptionManager {
    static final Logger logger = Logger.getLogger(EnergyConsumptionManager.class.getName());

    @Resource
    private EnergyConsumptionDao energyConsumptionDao;

    /**
     * save EnergyConsumption details.
     * 
     * @param energyConsumption
     *            com.ems.model.EnergyConsumption
     */
    public EnergyConsumption save(EnergyConsumption energyConsumption) {
        return (EnergyConsumption) energyConsumptionDao.saveObject(energyConsumption);
    }

    /**
     * update EnergyConsumption details.
     * 
     * @param energyConsumption
     *            com.ems.model.EnergyConsumption
     */
    public EnergyConsumption update(EnergyConsumption energyConsumption) {
        return (EnergyConsumption) energyConsumptionDao.saveObject(energyConsumption);
    }

    /**
     * Delete EnergyConsumption details
     * 
     * @param id
     *            database id(primary key)
     */
    public void delete(Long id) {
        energyConsumptionDao.removeObject(EnergyConsumption.class, id);
    }

    /**
     * load avg energy used for given time interval.
     * 
     * @param toDate
     *            to date for what we are calculating the avg.(from current date)
     * @param points
     *            number of points or avg we get
     * @param intervalValue
     *            time duration for what we are calculating the avg.
     * @param level
     *            like campus,building,floor,area,sub area.
     * @param levelId
     *            database id of level
     * @return
     */
    public List<Avgrecord> loadEnergyConsumptionAvg(Date toDate, Integer points, Integer intervalValue, String level,
            Integer levelId) {
        Long startTime = System.currentTimeMillis();
        logger.info("Entering loadEnergyConsumptionAvg");
        List<Avgrecord> avgrecords = energyConsumptionDao.loadEnergyConsumptionAvg(toDate, points, intervalValue,
                level, levelId, "loadEnergyConsumption");
        logger.info("Exiting loadEnergyConsumptionAvg");
        logPerfStats("loadEnergyConsumptionAvg", startTime);
        return avgrecords;
    }

    /**
     * load avg energy used for given time interval.
     * 
     * @param toDate
     *            to date for what we are calculating the avg.(from current date)
     * @param points
     *            number of points or avg we get
     * @param intervalValue
     *            time duration for what we are calculating the avg.
     * @param level
     *            like campus,building,floor,area,sub area.
     * @param levelId
     *            database id of level
     * @return
     */
    public List<Avgrecord> loadDailyEnergyConsumptionAvg(Date toDate, Integer points, Integer intervalValue,
            String level, Integer levelId) {
        Long startTime = System.currentTimeMillis();
        logger.info("Entering loadEnergyConsumptionAvg");
        List<Avgrecord> avgrecords = energyConsumptionDao.loadEnergyConsumptionAvg(toDate, points, intervalValue,
                level, levelId, "loaddailyenergyconsumption");
        logger.info("Exiting loadEnergyConsumptionAvg");
        logPerfStats("loadEnergyConsumptionAvg", startTime);
        return avgrecords;
    }

    /**
     * load avg energy used for given time interval.
     * 
     * @param toDate
     *            to date for what we are calculating the avg.(from current date)
     * @param points
     *            number of points or avg we get
     * @param intervalValue
     *            time duration for what we are calculating the avg.
     * @param level
     *            like campus,building,floor,area,sub area.
     * @param levelId
     *            database id of level
     * @return
     */
    public List<Avgrecord> loadHourlyEnergyConsumptionAvg(Date toDate, Integer points, Integer intervalValue,
            String level, Integer levelId) {
        Long startTime = System.currentTimeMillis();
        logger.info("Entering loadEnergyConsumptionAvg");
        List<Avgrecord> avgrecords = energyConsumptionDao.loadEnergyConsumptionAvg(toDate, points, intervalValue,
                level, levelId, "loadhourlyenergyconsumption");
        logger.info("Exiting loadEnergyConsumptionAvg");
        logPerfStats("loadEnergyConsumptionAvg", startTime);
        return avgrecords;
    }

    /**
     * load pie chart data.
     * 
     * @param columnName
     *            name of column.
     * @param id
     *            value of given column
     * @param tableName
     *            name of table in which need to search
     * @return PieChartData collection
     */
    @SuppressWarnings("unchecked")
    public List loadPieChart(String columnName, String columnName2, String id, String tableName) {
        Long startTime = System.currentTimeMillis();
        logger.info("Entering loadPieChar");
        List list = energyConsumptionDao.loadPieChart(columnName, columnName2, id, tableName);
        logger.info("Exiting loadPieChart");
        logPerfStats("loadPieChart", startTime);
        return list;
    }

    /**
     * load pie chart date to generate area reports
     * 
     * @param columnName
     *            area id associated with fixture
     * @param id
     *            area id value
     * @param fromDate
     *            from date
     * @param toDate
     *            to date
     * @return area report data
     */
    @SuppressWarnings("unchecked")
    public List loadAreaReportPieChartData(String columnName, String id, Date fromDate, Date toDate) {
        Long startTime = System.currentTimeMillis();
        List list = energyConsumptionDao.loadAreaReportPieChartData(columnName, id, fromDate, toDate);
        logPerfStats("loadAreaReportPieChartData", startTime);
        return list;
    }

    /**
     * load meter data
     * 
     * @param columnName
     *            name of the column on search need to perform
     * @param id
     *            database id
     * @return retur meter data as collection
     */
    @SuppressWarnings("unchecked")
    public List loadMeterData(String columnName, String id) {
        Long startTime = System.currentTimeMillis();
        logger.info("Entering loadMeterData");
        List list = energyConsumptionDao.loadMeterData(columnName, id);
        logger.info("Exiting loadMeterData");
        logPerfStats("loadMeterData", startTime);
        return list;
    }

    /**
     * load latest EnergyConsumption details for given fixture id.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.EnergyConsumption
     */
    public EnergyConsumption loadLatestEnergyConsumptionByFixtureId(Long id) {
        return energyConsumptionDao.loadLatestEnergyConsumptionByFixtureId(id);
    }

    /**
     * load avg energy used for given time interval.
     * 
     * @param toDate
     *            to date for what we are calculating the avg.(from current date)
     * @param points
     *            number of points or avg we get
     * @param intervalValue
     *            time duration for what we are calculating the avg.
     * @param level
     *            like campus,building,floor,area,sub area.
     * @param levelId
     *            database id of level
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, List<AvgBarChartRecord>> loadEnergyConsumptionStackedBarChart(Date toDate, Integer points,
            Integer intervalValue, String level, Integer levelId, Date fromDate) {
        Long startTime = System.currentTimeMillis();
        Map map = energyConsumptionDao.loadEnergyConsumptionStackedBarChart(toDate, points, intervalValue, level,
                levelId, fromDate);
        logPerfStats("loadEnergyConsumptionStackedBarChart", startTime);
        return map;
    }

    public void logPerfStats(String methodName, Long startTime) {
        logger.info("Time taken in " + methodName + " execution is >>>>>>>>>>>>"
                + (System.currentTimeMillis() - startTime));
    }

    public void aggregateHourlyData(Date toDate) {
        energyConsumptionDao.aggregateHourlyData(toDate);
    }

    public void aggregateDailyData(Date toDate) {
        energyConsumptionDao.aggregateDailyData(toDate);
    }

    public void updateZeroBuckets(long fixtureId, Date lastStatsDate, Date startDate, Date latestStatsDate) {
        energyConsumptionDao.updateZeroBuckets(fixtureId, lastStatsDate, startDate, latestStatsDate);
    }

    public List<MeterRecord> loadMeterData(String columnName, String id, Date from, Date to) {
        return energyConsumptionDao.loadMeterData(columnName, id, from, to);
    }

    public List<Avgrecord> loadRecentEnergyConsumption(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.MINUTE, -10);
        cal.set(Calendar.SECOND, 00);
        Date to = cal.getTime();
        return energyConsumptionDao.loadRecentEnergyConsumption(id, propertyName, from, to);
    }

    /**
     * load current summary
     * 
     * @param id
     * @param propertyName
     * @param dateOn
     * @return Dashboard record
     */
    public List<DashboardRecord> loadRecentSummary(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.MINUTE, -10);
        cal.set(Calendar.SECOND, 00);
        Date to = cal.getTime();
        return energyConsumptionDao.loadRecentSummary(id, propertyName, from, to);
    }

    /**
     * load daily energy consumption
     * 
     * @param id
     * @param columnName
     * @param dateOn
     * @return
     */
    public List<Avgrecord> loadDayEnergyConsumption(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        Date to = cal.getTime();
        return energyConsumptionDao.loadDayEnergyConsumption(id, propertyName, from, to);
    }

    /**
     * load daily summary
     * 
     * @param columnName
     * @param id
     * @param dateOn
     * @return
     */
    public List<DashboardRecord> loadDaySummary(Long id, String columnName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        Date from = cal.getTime();
        cal.setTime(dateOn);
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        Date to = cal.getTime();

        return energyConsumptionDao.loadDaySummary(id, columnName, from, to);
    }

    /**
     * load weekly energy consumption
     * 
     * @param id
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    public List<Avgrecord> loadWeekEnergyConsumption(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        // cal.set(Calendar.DAY_OF_WEEK, 1);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        // Today min 6 for week.
        cal.add(Calendar.DATE, -6);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 00, 00, 00);
        Date to = cal.getTime();
        return energyConsumptionDao.loadWeekEnergyConsumption(id, propertyName, from, to);
    }

    /**
     * load weekly dashboard summary
     * 
     * @param id
     * @param propertyName
     * @param dateOn
     * @return Dashboard records
     */
    public List<DashboardRecord> loadWeekSummary(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        // Today min 6 for week.
        cal.add(Calendar.DATE, -6);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 00, 00, 00);
        Date to = cal.getTime();
        return energyConsumptionDao.loadWeekSummary(id, propertyName, from, to);
    }

    /**
     * load monthly energy consumption
     * 
     * @param id
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    public List<Avgrecord> loadMonthEnergyConsumption(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        // cal.set(Calendar.DAY_OF_MONTH, 1);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.MONTH, -1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        // cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date to = cal.getTime();
        return energyConsumptionDao.loadMonthEnergyConsumption(id, propertyName, from, to);
    }

    /**
     * load Monthly summary
     * 
     * @param id
     * @param propertyName
     * @param dateOn
     * @return Dashboard records
     */
    public List<DashboardRecord> loadMonthSummary(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.MONTH, -1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        Date to = cal.getTime();
        return energyConsumptionDao.loadMonthSummary(id, propertyName, from, to);
    }

    /**
     * load yearly energy consumption
     * 
     * @param id
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    public List<Avgrecord> loadYearEnergyConsumption(Long id, String columnName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        // cal.set(Calendar.DAY_OF_YEAR, 1);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        // cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.YEAR, -1);
        Date to = cal.getTime();
        return energyConsumptionDao.loadYearEnergyConsumption(id, columnName, from, to);
    }

    /**
     * load Yearly summary
     * 
     * @param id
     * @param columnName
     * @param dateOn
     * @return Dashboard records
     */
    public List<DashboardRecord> loadYearSummary(Long id, String columnName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        Date from = cal.getTime();
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.YEAR, -1);
        Date to = cal.getTime();
        return energyConsumptionDao.loadYearSummary(id, columnName, from, to);
    }

    /**
     * load pie chart data for given date range.
     * 
     * @param columnName
     *            name of column.
     * @param id
     *            value of given column
     * @param tableName
     *            name of table in which need to search
     * @return PieChartData collection
     */
    @SuppressWarnings("unchecked")
    public List loadPieChartWithDateRange(String columnName, String columnName2, String id, String tableName,
            Date from, Date to) {
        return energyConsumptionDao.loadPieChartWithDateRange(columnName, columnName2, id, tableName, from, to);
    }

    /**
     * load meter data for given date range
     * 
     * @param columnName
     *            name of the column on search need to perform
     * @param id
     *            database id
     * @return retur meter data as collection BigDecimal[0] - Total power consumed BigDecimal[1] - Power saving in
     *         percentage BigDecimal[2] - Occ saving in percentage BigDecimal[3] - Tuneup saving in percentage
     *         BigDecimal[4] - Ambient saving in percentage BigDecimal[5] - Total saved power Float[6] - Price Float[7]
     *         - Total Cost BigDecimal[8] - Base power used
     * 
     */
    @SuppressWarnings("unchecked")
    public List loadMeterDataWithDateRange(String columnName, String id, Date from, Date to) {
        return energyConsumptionDao.loadMeterDataWithDateRange(columnName, id, from, to);
    }

    public void fillGemsMissingBuckets(Date toDate) {

        energyConsumptionDao.fillGemsMissingBuckets(toDate);

    } // end of method fillGemsMissingBuckets
    
    public void fillGemsZerouckets(Date toDate) {

        energyConsumptionDao.fillGemsZeroBuckets(toDate);

    } // end of method fillGemsMissingBuckets
    
    public Long getEnergyConsumptionId(Long fixtureId, Date timeStamp){
    	return energyConsumptionDao.getEnergyConsumptionId(fixtureId, timeStamp);
    }

    public static void main(String[] args) {
        /*
         * Date dateOn =new Date(); dateOn.setDate(13); Calendar cal = Calendar.getInstance(); cal.setTime(dateOn);
         * cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
         * cal.set(Calendar.DAY_OF_YEAR, 1);
         * 
         * //cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0); Date from =
         * cal.getTime(); cal.setTime(dateOn); cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
         * cal.get(Calendar.DATE), 23, 59, 59); System.out.println(cal.getTime()); cal.set(Calendar.DAY_OF_MONTH, 1);
         * cal.add(Calendar.DATE, -1); Date to = cal.getTime(); System.out.println(from); System.out.println(to);
         */
    }

    public List<DRRecord> loadAvgEnergyConsumptionBetweenPeriodsPerDay(Date fromTimeOfDay, Date toTimeOfDay,
            int groupId, int days) {
        return energyConsumptionDao.loadAvgEnergyConsumptionBetweenPeriodsPerDay(fromTimeOfDay, toTimeOfDay, groupId,
                days);
    }

    public List<GroupECRecord> loadGroupEnergyConsumptionBetweenPeriods(Date from, Date to) {
        return energyConsumptionDao.loadGroupEnergyConsumptionBetweenPeriods(from, to);
    }

    public Integer getFixtureCount(Long id, String property, String columnName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        Date from, to;
        if (property.equalsIgnoreCase("current")) {
            from = cal.getTime();
            cal.setTime(dateOn);
            cal.add(Calendar.MINUTE, -10);
            cal.set(Calendar.SECOND, 00);
            to = cal.getTime();
            property = "energy_consumption";
        } else if (property.equalsIgnoreCase("day")) {
            from = cal.getTime();
            cal.setTime(dateOn);
            cal.add(Calendar.DATE, -1);
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 00);
            to = cal.getTime();
            property = "energy_consumption_hourly";
        } else if (property.equalsIgnoreCase("week")) {
            from = cal.getTime();
            cal.setTime(dateOn);
            // Today min 6 for week.
            cal.add(Calendar.DATE, -6);
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 00, 00, 00);
            to = cal.getTime();
            property = "energy_consumption_hourly";
        } else if (property.equalsIgnoreCase("month")) {
            from = cal.getTime();
            // reset
            cal.setTime(dateOn);
            cal.add(Calendar.MONTH, -1);
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
            to = cal.getTime();
            property = "energy_consumption_hourly";
        } else {
            from = cal.getTime();
            cal.setTime(dateOn);
            cal.add(Calendar.YEAR, -1);
            to = cal.getTime();
            property = "energy_consumption_daily";
        }
        return energyConsumptionDao.getFixtureCount(id, property, columnName, from, to);
    }

    public List<DashboardRecord> loadSummaryInPeriod(Long id, String propertyName, Date from, Date to) {
        long diff = from.getTime() - to.getTime();
        // Calculate difference in days
        long diffDays = diff / (24 * 60 * 60 * 1000);
        if (diffDays <= 1) {
            return energyConsumptionDao.loadDaySummary(id, propertyName, from, to);
        } else if (diffDays <= 7) {
            return energyConsumptionDao.loadWeekSummary(id, propertyName, from, to);
        } else if (diffDays <= 31) {
            return energyConsumptionDao.loadMonthSummary(id, propertyName, from, to);
        }
        return energyConsumptionDao.loadYearSummary(id, propertyName, from, to);
    }

    /**
     * load previous hour data for energy consumption
     * 
     * @param Property
     *            name of Company,campus etc.
     * @param id
     *            value of given column
     */
	public List<DashboardRecord> loadhourSummary(Long pid, String property) {
		
		  return  energyConsumptionDao.loadHourSummary(pid, property);	
		     
	}

    /**
     * Returns individual groups summary in the specified time interval w.r.t org level
     * 
     * @param property
     *            (campus_id, building_id, floor_id, area_id)
     * @param pid
     *            unique property id
     * @param from
     *            latest date
     * @param to
     *            older date
     * @return GroupECRecord list
     */
    public List<GroupECRecord> loadGroupEnergyConsumption(String property, Long pid, Date from, Date to) {
        return energyConsumptionDao.loadGroupEnergyConsumption(property, pid, from, to);
    }
	
}
