package com.ems.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.AvgBarChartRecord;
import com.ems.model.Avgrecord;
import com.ems.model.DashboardRecord;
import com.ems.model.EnergyConsumption;
import com.ems.model.GroupECRecord;
import com.ems.model.MeterRecord;
import com.ems.utils.DateUtil;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("energyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EnergyConsumptionDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger("WSLogger");

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
            Integer levelId, String funcName) {
        List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
        Connection con = null;
        CallableStatement toesUp = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            toesUp = con.prepareCall("{ call " + funcName + "('" + new Timestamp(toDate.getTime()) + "'" + "," + points
                    + "," + intervalValue + ",'" + level + "'," + levelId + ")}");
            logger.info(">>>>>" + "{ call " + funcName + "('" + new Timestamp(toDate.getTime()) + "'" + "," + points
                    + "," + intervalValue + ",'" + level + "'," + levelId + ")}");
            toesUp.execute();
            ResultSet resultSet = toesUp.getResultSet();
            while (resultSet.next()) {
                Avgrecord avgrecord = new Avgrecord();
                avgrecord.setEN(resultSet.getFloat("EN"));
                avgrecord.setI(resultSet.getInt("i"));
                avgrecord.setPrice(resultSet.getFloat("price"));
                avgrecord.setCost(resultSet.getFloat("cost"));
                avgrecord.setBasePowerUsed(resultSet.getFloat("basePowerUsed"));
                avgrecord.setBaseCost(resultSet.getFloat("baseCost"));
                avgrecords.add(avgrecord);
            }
            toesUp.close();
            con.close();
            return avgrecords;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

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
        try {
            List results = null;
            String hsql = "select f.id,f.name,sum(ec.power_used),max(ec.price) as price,sum(ec.cost) as cost "
                    + "from ("
                    + tableName
                    + " f left join fixture fi on fi."
                    + columnName
                    + " = f.id) "
                    + "left join energy_consumption as ec on ec.fixture_id = fi.id and ec.capture_at =(select max(capture_at) from energy_consumption)"
                    + "where f." + columnName2 + " =" + id + " group by f.id,f.name;";

            if ("company_campus".equals(tableName)) {
                hsql = "select (select id from campus where id=f.id) as name,"
                        + "(select name from campus where id=f.id) as id,sum(ec.power_used) as totalPower,max(ec.price),sum(ec.cost) "
                        + "from (company_campus f left join fixture fi "
                        + "on fi.campus_id = f.id) left join "
                        + "energy_consumption as ec on ec.fixture_id = fi.id and ec.capture_at =(select max(capture_at) from energy_consumption)"
                        + "where f.company_id =" + id + " group by f.id";
            }

            if ("fixture".equals(tableName)) {
                hsql = "select f.id,'' as name,sum(ec.power_used),max(ec.price),sum(ec.cost) "
                        + "from ("
                        + tableName
                        + " f left join fixture fi on fi."
                        + columnName
                        + " = f.id) "
                        + "left join energy_consumption as ec on ec.fixture_id = fi.id and ec.capture_at =(select max(capture_at) from energy_consumption)"
                        + "where f." + columnName2 + " =" + id + " group by f.id;";
            }

            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
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
        String ecTableName = "energy_consumption";
        ecTableName = selectTable(from, to);
        String hsql = createPieChartQuery(tableName, ecTableName, columnName, columnName2, id, from, to);
        try {
            List results = null;
            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    private String createPieChartQuery(String tableName, String ecTableName, String columnName, String columnName2,
            String id, Date from, Date to) {
        String hsql = "select f.id,f.name,sum(ec.power_used),max(ec.price) as price,sum(ec.cost) as cost " + "from ("
                + tableName + " f left join fixture fi on fi." + columnName + " = f.id) " + "left join " + ecTableName
                + " as ec on ec.fixture_id = fi.id " + "and ec.capture_at >'"
                + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "'" + "and ec.capture_at <'"
                + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "'" + "where f." + columnName2 + " =" + id
                + " group by f.id,f.name;";

        if ("company_campus".equals(tableName)) {
            hsql = "select (select id from campus where id=f.id) as name,"
                    + "(select name from campus where id=f.id) as id,sum(ec.power_used) as totalPower,max(ec.price),sum(ec.cost) "
                    + "from (company_campus f left join fixture fi " + "on fi.campus_id = f.id) left join "
                    + ecTableName + " as ec on ec.fixture_id = fi.id " + "and ec.capture_at >'"
                    + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "'" + "and ec.capture_at <'"
                    + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "'" + "where f.company_id =" + id
                    + " group by f.id";
        }
        if (ecTableName.equals("energy_consumption_daily")) {
            hsql.replaceFirst("sum(ec.power_used) as totalPower",
                    "sum(ec.power_used+ec.power_used2+ec.power_used3+ec.power_used4+ec.power_used5) as totalPower");
            hsql.replaceFirst("max(ec.price)", "max(greatest(ec.price,ec.price2,ec.price3,ec.price4,ec.price5))");
        }
        return hsql;
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
        try {
            List results = null;
            String hsql = "select totalConsum,total,round(totalConsum*100/(total * 60)) as percentage,price,cost,baseTotalConsum "
                    + "from(select sum(ec.power_used) as totalConsum,(select sum(wattage) "
                    + "from fixture where "
                    + columnName
                    + " ="
                    + id
                    + ") as total,max(ec.price) as price,sum(ec.cost) as cost,sum(ec.base_power_used) as baseTotalConsum "
                    + "from energy_consumption ec "
                    + "where ec.fixture_id in (select id from device where "
                    + columnName
                    + " ="
                    + id
                    + ")  "
                    + "and ec.capture_at = (select max(capture_at) "
                    + "from energy_consumption)) as ss";

            if ("company_id".equals(columnName)) {
                hsql = "select totalConsum,total,round(totalConsum*100/(total * 60)) as percentage,price,cost "
                        + "from(select sum(ec.power_used) as totalConsum,(select sum(wattage) "
                        + "from fixture) as total ,max(ec.price) as price,sum(ec.cost) as cost "
                        + "from energy_consumption ec " + "where ec.fixture_id in (select id from fixture)  "
                        + "and ec.capture_at = (select max(capture_at) " + "from energy_consumption)) as ss";
            }

            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
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
     *         BigDecimal[4] - Ambient saving in percentage BigDecimal[5] - Manual saving in percentage BigDecimal[6] -
     *         Total saved power Float[7] - Price Float[8] - Total Cost BigDecimal[9] - Total power consumed
     *         BigDecimal[10] - Base power used BigDecimal[11] - Avg load BigDecimal[12] - Peak load BigDecimal[13] -
     *         Min load
     * 
     */
    @SuppressWarnings("unchecked")
    public List loadMeterDataWithDateRange(String columnName, String id, Date from, Date to) {
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);
        if (tableName.equals("energy_consumption")) {
            return loadCurrentMeterData(columnName, id, from, to);
        }
        try {
            List results = null;
            String tableFilterName = "device";
          	if(columnName.equals("group_id")) {
          		tableFilterName = "fixture";
          	}
            String hsql = "select totalConsum," + "round(totalConsum*100/(baseTotalConsum)) as powerPercentage,"
                    + "round(totalOccSaved*100/(baseTotalConsum)) as occPercentage,"
                    + "round(totalTuneupSaving*100/(baseTotalConsum)) as tuneupPercentage,"
                    + "round(totalAmbientSaved*100/(baseTotalConsum)) as ambientPercentage,"
                    + "round(totalManualSaving*100/(baseTotalConsum)) as manualPercentage," + "totalPowerSaved,"
                    + "price," + "cost," + "totalConsum," + "baseTotalConsum, " + "1 as f0, " + "1 as f1, "
                    + "1 as f2, ";
            hsql += "savedCost " + "from(select " + "sum(ec.power_used) as totalConsum,"
                    + "sum(ec.saved_power_used) as totalPowerSaved," + "sum(ec.occ_saving) as totalOccSaved,"
                    + "sum(ec.tuneup_saving) as totalTuneupSaving," + "sum(ec.ambient_saving) as totalAmbientSaved,"
                    + "max(ec.price) as price," + "sum(ec.cost) as cost,"
                    + "sum(ec.manual_saving) as totalManualSaving, " + "sum(ec.base_power_used) as baseTotalConsum, "
                    + "avg(ec.power_used) as avgLoad, " + "sum(ec.saved_cost) as savedCost " + "from " + tableName
                    + " ec where " + "ec.fixture_id in (select id from " + tableFilterName + " where " + columnName + " =" + id + ")";
            if (tableName.equals("energy_consumption"))
                hsql += " and ec.zero_bucket != 1 ";
            hsql += " and ec.capture_at >'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at <'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "') as ss";

            if ("company_id".equals(columnName)) {
                hsql = "select totalConsum," + "round(totalConsum*100/(baseTotalConsum)) as powerPercentage,"
                        + "round(totalOccSaved*100/(baseTotalConsum)) as occPercentage,"
                        + "round(totalTuneupSaving*100/(baseTotalConsum)) as tuneupPercentage,"
                        + "round(totalAmbientSaved*100/(baseTotalConsum)) as ambientPercentage,"
                        + "round(totalManualSaving*100/(baseTotalConsum)) as manualPercentage," + "totalPowerSaved,"
                        + "price," + "cost," + "totalConsum," + "baseTotalConsum, " + "1 as f0, " + "1 as f1, "
                        + "1 as f2, ";
                hsql += "savedCost " + "from(select sum(ec.power_used) as totalConsum,"
                        + "sum(ec.saved_power_used) as totalPowerSaved," + "sum(ec.occ_saving) as totalOccSaved,"
                        + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                        + "sum(ec.ambient_saving) as totalAmbientSaved," + "max(ec.price) as price,"
                        + "sum(ec.cost) as cost," + "sum(ec.manual_saving) as totalManualSaving, "
                        + "sum(ec.base_power_used) as baseTotalConsum, " + "avg(ec.power_used) as avgLoad, "
                        + "sum(ec.saved_cost) as savedCost " + "from " + tableName + " ec where "
                        + "ec.fixture_id in (select id from fixture)";
                if (tableName.equals("energy_consumption"))
                    hsql += " and ec.zero_bucket != 1 ";
                hsql += "and ec.capture_at >'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                        + "and ec.capture_at <'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "') as ss";
            }

            if (tableName.equals("energy_consumption_daily")) {
                hsql.replaceFirst("sum(ec.power_used) as totalConsum",
                        "sum(ec.power_used+ec.power_used2+ec.power_used3+ec.power_used4+ec.power_used5) as totalConsum");
                hsql.replaceFirst("max(ec.price) as price",
                        "max(greatest(ec.price,ec.price2,ec.price3,ec.price4,ec.price5)) as price");
            }

            // Protect against divide by zero error
            hsql += " where baseTotalConsum != 0";

            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results == null) {
                // Initialize the default EC to 0's
                results = new ArrayList();
            }
            if (results.isEmpty()) {
                results.add(0, 0);
                results.add(1, 0);
                results.add(2, 0);
                results.add(3, 0);
                results.add(4, 0);
                results.add(5, 0);
                results.add(6, 0);
                results.add(7, 0);
                results.add(8, 0);
                results.add(9, 0);
                results.add(10, 0);
                results.add(11, 0);
                results.add(12, 0);
                results.add(13, 0);
                results.add(14, 0);
            }
            if (results != null) {
                if (tableName.equals("energy_consumption_daily")) {
                    results.add(loadPriceDetailsDaily(tableName, columnName, id, from, to));
                } else {
                    results.add(loadPriceDetails(tableName, columnName, id, from, to));
                }
                // Get Current Load
                results.add(loadCurrentLoad(tableName, columnName, id, from, to));
                // Get Peak and Min loads as per capture time
                results.add(loadPeakAndMinLoadQuery(tableName, columnName, id, from, to));
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    private List loadCurrentMeterData(String columnName, String id, Date from, Date to) {

        /*
         * select totalConsum from (select sum(lastPower) as totalConsum from (select mct.fixture_id, mct.power_used as
         * lastPower from (select fixture_id as fixtureId, max(capture_at) as currentCapture from energy_consumption
         * where fixture_id in (select id from fixture) and capture_at < now() and capture_at > now() - interval '10
         * minute' group by fixture_id) as cl join energy_consumption as mct on cl.currentCapture = mct.capture_at and
         * cl.fixtureId = mct.fixture_id) as acl) as qu;
         */

        String tableName = "energy_consumption";
        try {
            List results = null;
            String hsql = "select totalConsum," + "round(totalConsum*100/(baseTotalConsum)) as powerPercentage,"
                    + "round(totalOccSaved*100/(baseTotalConsum)) as occPercentage,"
                    + "round(totalTuneupSaving*100/(baseTotalConsum)) as tuneupPercentage,"
                    + "round(totalAmbientSaved*100/(baseTotalConsum)) as ambientPercentage,"
                    + "round(totalManualSaving*100/(baseTotalConsum)) as manualPercentage," + "totalPowerSaved,"
                    + "price," + "cost," + "totalConsum," + "baseTotalConsum, " + "1 as f0, " + "1 as f1, "
                    + "1 as f2, savedCost from(";

            hsql += "select " + "sum(lastPowerUsed) as totalConsum," + "sum(lastSavedPowerUsed) as totalPowerSaved,"
                    + "sum(lastOccSaving) as totalOccSaved," + "sum(lastTuneupSaving) as totalTuneupSaving,"
                    + "sum(lastAmbientSaving) as totalAmbientSaved," + "max(lastPrice) as price,"
                    + "sum(lastCost) as cost," + "sum(lastManualSaving) as totalManualSaving, "
                    + "sum(lastBasePowerUsed) as baseTotalConsum, " + "avg(lastPowerUsed) as avgLoad, "
                    + "sum(lastSavedCost) as savedCost "
                    + "from (select mct.fixture_id, mct.power_used as lastPowerUsed, "
                    + "mct.saved_power_used as lastSavedPowerUsed, " + "mct.occ_saving as lastOccSaving, "
                    + "mct.tuneup_saving as lastTuneupSaving, " + "mct.ambient_saving as lastAmbientSaving, "
                    + "mct.price as lastPrice, " + "mct.cost as lastCost, " + "mct.manual_saving as lastManualSaving, "
                    + "mct.base_power_used as lastBasePowerUsed, " + "mct.saved_cost as lastSavedCost "
                    + "from (select fixture_id as fixtureId, max(capture_at) as currentCapture "
                    + "from energy_consumption where fixture_id in " + "(select id from device ";

            if (!"company_id".equals(columnName)) {
                hsql += "where " + columnName + " = " + id;
            }

            hsql += ") and capture_at < now() and "
                    + "capture_at > now() - interval '10 minute' and zero_bucket != 1 group by fixture_id) "
                    + "as cl join energy_consumption as mct on cl.currentCapture = mct.capture_at "
                    + "and cl.fixtureId = mct.fixture_id) as acl) as qu where baseTotalConsum != 0";

            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results == null) {
                // Initialize the default EC to 0's
                results = new ArrayList();
            }
            if (results.isEmpty()) {
                results.add(0, 0);
                results.add(1, 0);
                results.add(2, 0);
                results.add(3, 0);
                results.add(4, 0);
                results.add(5, 0);
                results.add(6, 0);
                results.add(7, 0);
                results.add(8, 0);
                results.add(9, 0);
                results.add(10, 0);
                results.add(11, 0);
                results.add(12, 0);
                results.add(13, 0);
                results.add(14, 0);
            }
            if (results != null) {
                results.add(loadPriceDetails(tableName, columnName, id, from, to));
                // Get Current Load
                results.add(loadCurrentLoad(tableName, columnName, id, from, to));
                // Get Peak and Min loads as per capture time
                results.add(loadPeakAndMinLoadQuery(tableName, columnName, id, from, to));
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;

    } // end of method loadCurrentMeterData

    private List loadPeakAndMinLoadQuery(String tableName, String columnName, String id, Date from, Date to) {
        // we cannot take the peak load and min load from energy_consumption_daily table's power_used
        // as power used in daily table is sum of energy for the entire day.
        if (tableName.equals("energy_consumption_daily")) {
            tableName = "energy_consumption_hourly";
        }
        String filterTableName = "device";
        if(columnName.equals("group_id")) {
        	filterTableName = "fixture";
        }
        String sql = "select avg(ecl.load) as avgLoad, max(ecl.load) as peakLoad, min(ecl.load) as minLoad from "
                + "(select sum(power_used) as load from " + tableName                
                + " where fixture_id in (select id from " + filterTableName + " where " + columnName + "=" + id + ")";
        if (tableName.equals("energy_consumption"))
            sql += " and zero_bucket != 1 ";
        sql += " and capture_at <= '" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "'" + " and capture_at > '"
                + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' group by capture_at) as ecl";
        if ("company_id".equals(columnName)) {
            sql = "select avg(ecl.load) as avgLoad, max(ecl.load) as peakLoad, min(ecl.load) as minLoad from "
                    + "(select sum(power_used) as load from " + tableName
                    + " where fixture_id in (select id from fixture) ";
            if (tableName.equals("energy_consumption"))
                sql += " and zero_bucket != 1 ";
            sql += " and capture_at <= '" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "'"
                    + " and capture_at > '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss")
                    + "' group by capture_at) as ecl";
        }
        Query query = getSession().createSQLQuery(sql);
        return query.list();
    }

    private List loadCurrentLoad(String tableName, String columnName, String id, Date from, Date to) {
    	
    	String tableFilterName = "device";
    	if(columnName.equals("group_id")) {
    		tableFilterName = "fixture";
    	}
        String sql = "select sum(fixtureCurrentLoad) as currentLoad, sum(fixtureCurrentBaseLoad) as currentBaseLoad from (select "
                + "DISTINCT mct.fixture_id, mct.power_used as fixtureCurrentLoad, mct.base_power_used as fixtureCurrentBaseLoad from (select "
                + "fixture_id as fixtureId, MAX(capture_at) as currentCapture "
                + "from energy_consumption where "
                + "fixture_id in (select id from " + tableFilterName + " where "
                + columnName
                + " ="
                + id
                + ") "
                + "and capture_at < now() "
                + "and capture_at > now() - interval '10 minute' group by fixture_id) as cl JOIN energy_consumption as mct "
                + "on cl.currentCapture = mct.capture_at and cl.fixtureId = mct.fixture_id) as acl";

        if ("company_id".equals(columnName)) {
            sql = "select sum(fixtureCurrentLoad) as currentLoad, sum(fixtureCurrentBaseLoad) as currentBaseLoad from (select "
                    + "DISTINCT mct.fixture_id, mct.power_used as fixtureCurrentLoad, mct.base_power_used as fixtureCurrentBaseLoad from (select "
                    + "fixture_id as fixtureId, MAX(capture_at) as currentCapture "
                    + "from energy_consumption where "
                    + "fixture_id in (select id from fixture) "
                    + "and capture_at < now() "
                    + "and capture_at > now() - interval '10 minute' group by fixture_id) as cl JOIN energy_consumption as mct "
                    + "on cl.currentCapture = mct.capture_at and cl.fixtureId = mct.fixture_id) as acl";
        }
        Query query = getSession().createSQLQuery(sql);
        if (query.list() != null) {
            Object[] loadObjs = (Object[]) query.list().get(0);
            if (loadObjs.length == 2)
                logger.debug("Current Load: " + loadObjs[0] + ", Base Load: " + loadObjs[1]);
        }
        return query.list();
    }

    private List loadPriceDetails(String tableName, String columnName, String id, Date from, Date to) {
    	
    	String tableFilterName = "device";
    	if(columnName.equals("group_id")) {
    		tableFilterName = "fixture";
    	}
        String sql = "select " + "sum(ec.power_used) as totalConsum," + "ec.price as price " + "from " + tableName
                + " ec where " + "ec.fixture_id in (select id from " + tableFilterName + " where " + columnName + " =" + id + ") "
                + "and ec.capture_at >'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                + "and ec.capture_at <'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "' group by ec.price";
        if ("company_id".equals(columnName)) {
            sql = "select " + "sum(ec.power_used) as totalConsum," + "ec.price as price " + "from " + tableName
                    + " ec where " + "ec.fixture_id in (select id from fixture) " + "and ec.capture_at >'"
                    + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' " + "and ec.capture_at <'"
                    + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "' group by ec.price";
        }
        Query query = getSession().createSQLQuery(sql);
        List list = query.list();
        return list;
    }

    @SuppressWarnings("unchecked")
    private List loadPriceDetailsDaily(String tableName, String columnName, String id, Date from, Date to) {
    	
    	String tableFilterName = "device";
    	if(columnName.equals("group_id")) {
    		tableFilterName = "fixture";
    	}
        String sql = "select sum(ec.power_used) as pu1, sum(ec.power_used2) as pu2, sum(ec.power_used3) as pu3, sum(ec.power_used4) as pu4, sum(ec.power_used5) as pu5, ec.price,ec.price2,ec.price3,ec.price4,ec.price5 "
                + "from energy_consumption_daily ec "
                + "where ec.fixture_id in (select id from " + tableFilterName + " where "
                + columnName
                + " ="
                + id
                + ") "
                + "and ec.capture_at >'"
                + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss")
                + "' "
                + "and ec.capture_at <'"
                + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                + "' "
                + "group by ec.price,ec.price2,ec.price3,ec.price4,ec.price5";
        if ("company_id".equals(columnName)) {
            sql = "select sum(ec.power_used) as pu1, sum(ec.power_used2) as pu2, sum(ec.power_used3) as pu3, sum(ec.power_used4) as pu4, sum(ec.power_used5) as pu5, ec.price,ec.price2,ec.price3,ec.price4,ec.price5 "
                    + "from energy_consumption_daily ec "
                    + "where ec.fixture_id in (select id from fixture) "
                    + "and ec.capture_at >'"
                    + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss")
                    + "' "
                    + "and ec.capture_at <'"
                    + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' "
                    + "group by ec.price,ec.price2,ec.price3,ec.price4,ec.price5";
        }
        Query query = getSession().createSQLQuery(sql);
        List list = query.list();
        if (list != null && !list.isEmpty()) {
            Map map = new HashMap();
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Object[] objects = (Object[]) iterator.next();
                BigDecimal ec = (BigDecimal) objects[0];
                BigDecimal ec2 = (BigDecimal) objects[1];
                BigDecimal ec3 = (BigDecimal) objects[2];
                BigDecimal ec4 = (BigDecimal) objects[3];
                BigDecimal ec5 = (BigDecimal) objects[4];
                Double price = (Double) objects[5];
                Double price2 = (Double) objects[6];
                Double price3 = (Double) objects[7];
                Double price4 = (Double) objects[8];
                Double price5 = (Double) objects[9];
                if (price != null) {
                    if (!map.containsKey(price)) {
                        map.put(price, ec);
                    } else {
                        map.put(price, ((BigDecimal) map.get(price)).add(ec));
                    }
                }
                if (price2 != null) {
                    if (!map.containsKey(price2)) {
                        map.put(price2, ec2);
                    } else {
                        map.put(price2, ((BigDecimal) map.get(price2)).add(ec2));
                    }
                }
                if (price3 != null) {
                    if (!map.containsKey(price3)) {
                        map.put(price3, ec3);
                    } else {
                        map.put(price3, ((BigDecimal) map.get(price3)).add(ec3));
                    }
                }
                if (price4 != null) {
                    if (!map.containsKey(price4)) {
                        map.put(price4, ec4);
                    } else {
                        map.put(price4, ((BigDecimal) map.get(price4)).add(ec4));
                    }
                }
                if (price5 != null) {
                    if (!map.containsKey(price5)) {
                        map.put(price5, ec5);
                    } else {
                        map.put(price5, ((BigDecimal) map.get(price5)).add(ec5));
                    }
                }
                List result = new ArrayList();
                Set keys = map.keySet();
                if (keys != null && !keys.isEmpty()) {
                    Iterator iterator2 = keys.iterator();
                    while (iterator2.hasNext()) {
                        Double key = (Double) iterator2.next();
                        Object[] objects2 = new Object[2];
                        objects2[0] = map.get(key);
                        objects2[1] = key;
                        result.add(objects2);
                    }
                }
                return result;
            }
        }
        return null;
    }

    private String selectTable(Date from, Date to) {
        Calendar calstart = Calendar.getInstance();
        calstart.setTime(new Date());
        calstart.set(Calendar.HOUR, 0);
        calstart.set(Calendar.MINUTE, 0);
        calstart.set(Calendar.SECOND, 0);
        calstart.set(Calendar.DATE, 1);

        Calendar calend = Calendar.getInstance();
        calend.setTime(new Date());

        Calendar fDate = Calendar.getInstance();
        fDate.setTime(from);
        Calendar tDate = Calendar.getInstance();
        tDate.setTime(to);

        long diff = fDate.getTimeInMillis() - tDate.getTimeInMillis();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        if ((diff / (60 * 1000)) < 15) {
            logger.debug("Fetching readings from current table");
            return "energy_consumption";
        }
        if (diffDays < 2) { // max days in month
          //Sree only daily graph should fetch from hourly table
            logger.debug("Fetching readings from hourly table");
            return "energy_consumption_hourly";
        }
        return "energy_consumption_daily";
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
     * @return return pie chart data for area reports
     */
    @SuppressWarnings("unchecked")
    public List loadAreaReportPieChartData(String columnName, String id, Date fromDate, Date toDate) {
        String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
        String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
        String tableName = selectTable(fromDate, toDate);
        try {
            List results = null;
            String hsql = "select g.id,g.name,round(sum(ec.power_used)/1000,2),(select count(id) "
                    + "from fixture where group_id=g.id and " + columnName + "=" + id
                    + ") as totalCount from groups g join fixture f " + "on g.id=f.group_id and f." + columnName + "="
                    + id + " join " + tableName + " ec on ec.fixture_id = f.id " + "and capture_at>= '"
                    + fromDateString + "' and capture_at<= '" + toDateString + "' " + "and f." + columnName + "=" + id
                    + " group by g.id ,g.name order by g.id;";

            if (columnName.equals("company")) {
                hsql = "select g.id,g.name,round(sum(ec.power_used)/1000,2),(select count(id) "
                        + "from fixture where group_id=g.id) as totalCount from groups g join fixture f "
                        + "on g.id=f.group_id " + "join " + tableName + " ec on ec.fixture_id = f.id "
                        + "and capture_at>= '" + fromDateString + "' and capture_at<= '" + toDateString + "' "
                        + "group by g.id ,g.name order by g.id;";
            }

            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load latest EnergyConsumption details for given fixture id.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.EnergyConsumption
     */
    @SuppressWarnings("unchecked")
    public EnergyConsumption loadLatestEnergyConsumptionByFixtureId(Long id) {
        try {
            List<EnergyConsumption> results = null;
            String hsql = "from EnergyConsumption as ec where ec.fixture.id=? "
                    + "and ec.captureAt=(Select max(ec2.captureAt) from EnergyConsumption as ec2 "
                    + "where ec2.fixture.id=?)";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            q.setParameter(1, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (EnergyConsumption) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
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
    public Map<Integer, List<AvgBarChartRecord>> loadEnergyConsumptionStackedBarChart(Date toDate, Integer points,
            Integer intervalValue, String level, Integer levelId, Date fromDate) {
        Connection con = null;
        Map<Integer, List<AvgBarChartRecord>> map = new HashMap<Integer, List<AvgBarChartRecord>>();
        CallableStatement toesUp = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            toesUp = con.prepareCall("{ call loadenergyconsumptionbarchart('" + new Timestamp(toDate.getTime()) + "'"
                    + "," + points + "," + intervalValue + ",'" + level + "'," + levelId + ",'"
                    + new Timestamp(fromDate.getTime()) + "')}");
            logger.debug("call loadenergyconsumptionbarchart('" + new Timestamp(toDate.getTime()) + "'" + "," + points
                    + "," + intervalValue + ",'" + level + "'," + levelId + ",'" + new Timestamp(fromDate.getTime())
                    + "')");
            toesUp.execute();
            ResultSet resultSet = toesUp.getResultSet();
            while (resultSet.next()) {
                AvgBarChartRecord avgBarChartRecord = new AvgBarChartRecord();
                avgBarChartRecord.setEN(resultSet.getFloat("EN"));
                avgBarChartRecord.setId(resultSet.getInt("id"));
                avgBarChartRecord.setName(resultSet.getString("name"));
                avgBarChartRecord.setShowOn(resultSet.getDate("ondate"));
                if (map.containsKey(avgBarChartRecord.getId())) {
                    List<AvgBarChartRecord> list = map.get(avgBarChartRecord.getId());
                    list.add(avgBarChartRecord);
                    map.put(avgBarChartRecord.getId(), list);
                } else {
                    List<AvgBarChartRecord> list = new ArrayList<AvgBarChartRecord>();
                    list.add(avgBarChartRecord);
                    map.put(avgBarChartRecord.getId(), list);
                }
            }
            toesUp.close();
            con.close();
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void aggregateHourlyData(Date toDate) {

        Connection con = null;
        CallableStatement calSt = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            calSt = con.prepareCall("{ call aggregateHourlyEnergyConsumption('" + new Timestamp(toDate.getTime())
                    + "')}");
            calSt.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method aggregateHourlyData

    public void aggregateDailyData(Date toDate) {

        Connection con = null;
        CallableStatement calSt = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            calSt = con.prepareCall("{ call aggregateDailyEnergyConsumption('" + new Timestamp(toDate.getTime())
                    + "')}");
            calSt.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method aggregateDailyData

    // ////////////////////////////////////////////////////////////////////////////////////////

    public List<MeterRecord> loadMeterData(String columnName, String id, Date from, Date to) {
        List<MeterRecord> oRecords = new ArrayList<MeterRecord>();
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);
        try {
            String hsql = "select totalConsum, baseTotalConsum, totalPowerSaved, price, cost, savedcost, "
                    + "round(totalConsum*100/(baseTotalConsum)) as powerPercentage, "
                    + "round(totalOccSaved*100/(baseTotalConsum)) as occPercentage, "
                    + "round(totalTuneupSaving*100/(baseTotalConsum)) as tuneupPercentage,"
                    + "round(totalAmbientSaved*100/(baseTotalConsum)) as ambientPercentage, "
                    + "round(totalManualSaving*100/(baseTotalConsum)) as manualPercentage, " + "avgLoad, "
                    + "peakLoad, " + "minLoad " + "from(select " + "sum(ec.power_used) as totalConsum, "
                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " // Verify if this is correct
                    + "max(ec.power_used) as peakLoad, " // Verify if this is correct
                    + "min(ec.power_used) as minLoad " // Verify if this is correct
                    + "from " + tableName + " ec where ";
            if (columnName.equals("company_id")) {
                hsql += " ec.fixture_id in (select id from fixture) and ";
            } else {
            	String tableFilterName = "device";
            	if(columnName.equals("group_id")) {
            		tableFilterName = "fixture";
            	}
              hsql += " ec.fixture_id in (select id from " + tableFilterName + " where " + columnName + " =" + id + ") and";
            }
            if (tableName.equals("energy_consumption"))
                hsql += " ec.zero_bucket != 1 and ";
            hsql += " ec.capture_at >'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at <'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "') as ss";

            // Protect against divide by zero error
            hsql += " where baseTotalConsum != 0";

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    MeterRecord oRecord = new MeterRecord();
                    oRecord.setPowerused(((BigDecimal) object[0]).floatValue());
                    oRecord.setBasePowerUsed(((BigDecimal) object[1]).floatValue());
                    oRecord.setSavedPower(((BigDecimal) object[2]).floatValue());
                    oRecord.setPrice(((Double) object[3]).floatValue());
                    oRecord.setCost(((Double) object[4]).floatValue());
                    oRecord.setSavedCost(((Double) object[5]).floatValue());
                    oRecord.setPowersavingpercent(((BigDecimal) object[6]).intValue());
                    oRecord.setOccsavingpercent(((BigDecimal) object[7]).intValue());
                    oRecord.setTasktuneupsavingpercent(((BigDecimal) object[8]).intValue());
                    oRecord.setAmbientsavingpercent(((BigDecimal) object[9]).intValue());
                    oRecord.setManualsavingpercent(((BigDecimal) object[10]).intValue());
                    oRecord.setAvgLoad(((BigDecimal) object[11]).floatValue());
                    oRecord.setPeakLoad(((BigDecimal) object[12]).floatValue());
                    oRecord.setMinLoad(((BigDecimal) object[13]).floatValue());
                    oRecords.add(oRecord);
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
    }

    public List<Avgrecord> loadRecentEnergyConsumption(Long id, String propertyName, Date from, Date to) {
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            List results = null;
            String hsql = "SELECT date_trunc('hour', capture_at) AS Hour , "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption as ec " + "where ec.fixture_id in(select id from fixture where "
                    + propertyName + " = " + id + ") " + "and capture_at < :from " + "and capture_at > :to "
                    + "GROUP BY Hour ORDER BY Hour";
            if ("company".equals(propertyName)) {
                hsql = "SELECT date_trunc('hour', capture_at) AS Hour, "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption as ec " + "where capture_at < :from " + "and capture_at > :to "
                        + "GROUP BY Hour ORDER BY Hour";
            }
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            results = q.list();
            int count = 0;
            if (results != null && !results.isEmpty()) {
                List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
                for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    Avgrecord avgrecord = new Avgrecord();
                    avgrecord.setI(++count);
                    avgrecord.setCaptureOn((Date) object[0]);
                    avgrecord.setEN(((BigDecimal) object[1]).floatValue());
                    avgrecord.setBasePowerUsed(((BigDecimal) object[2]).floatValue());
                    avgrecord.setCost(((Double) object[3]).floatValue());
                    avgrecord.setBaseCost(((Double) object[4]).floatValue());
                    avgrecord.setPrice(((Double) object[5]).floatValue());
                    avgrecords.add(avgrecord);
                }
                return avgrecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load daily energy consumption
     * 
     * @param id
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Avgrecord> loadDayEnergyConsumption(Long id, String propertyName, Date from, Date to) {
        List results = null;
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            String hsql = "SELECT date_trunc('hour', capture_at) AS Hour , "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption_hourly as ec " + "where ec.fixture_id in(select id from fixture where "
                    + propertyName + " = " + id + ") " + "and capture_at < :from " + "and capture_at > :to "
                    + "GROUP BY Hour ORDER BY Hour";
            if ("company".equals(propertyName)) {
                hsql = "SELECT date_trunc('hour', capture_at) AS Hour, "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption_hourly as ec " + "where capture_at < :from "
                        + "and capture_at > :to " + "GROUP BY Hour ORDER BY Hour";
            }
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            results = q.list();
            int count = 0;
            if (results != null && !results.isEmpty()) {
                List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
                for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    Avgrecord avgrecord = new Avgrecord();
                    avgrecord.setI(++count);
                    avgrecord.setCaptureOn((Date) object[0]);
                    avgrecord.setEN(((BigDecimal) object[1]).floatValue());
                    avgrecord.setBasePowerUsed(((BigDecimal) object[2]).floatValue());
                    avgrecord.setCost(((Double) object[3]).floatValue());
                    avgrecord.setBaseCost(((Double) object[4]).floatValue());
                    avgrecord.setPrice(((Double) object[5]).floatValue());
                    avgrecords.add(avgrecord);
                }
                return avgrecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return loadRecentEnergyConsumption(id, propertyName, from, to);
    }

    /**
     * load day summary
     * 
     * @param columnName
     *            value = {company, campus_id, building_id, floor_id, area_id}
     * @param id
     *            of the column in request
     * @param from
     *            latest date
     * @param to
     *            older date
     * @return Dashboard List
     */
    public List<DashboardRecord> loadDaySummary(Long id, String columnName, Date from, Date to) {
        List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);

        try {
            String hsql = "SELECT date_trunc('hour', ec.capture_at) AS Hour, " + "sum(ec.power_used) as totalConsum, "
                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+ "from " + tableName + " ec where ";
            if (!columnName.equals("company_id")) {
            	String filterTableName = "device";
            	if(columnName.equals("group_id")) {
            		filterTableName = "fixture";
            	}
              hsql += " ec.fixture_id in (select id from " + filterTableName + " where " + columnName + " =" + id + ") and";
            }
            if (tableName.equals("energy_consumption"))
                hsql += " ec.zero_bucket != 1 and ";
            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' " + " Group by Hour Order by Hour";

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            Object[] avg_min_max = ((List<Object[]>)loadPeakAndMinLoadQuery("energy_consumption_hourly", columnName, id.toString(), to, from)).get(0);
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    try {
                    	Object[] object = (Object[]) iterator.next();
                        DashboardRecord oRecord = new DashboardRecord();
                        oRecord.setCaptureOn(((Date) object[0]));
                        oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                        oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                        oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                        oRecord.setPrice(((Double) object[4]).floatValue());
                        oRecord.setCost(((Double) object[5]).floatValue());
                        oRecord.setSavedCost(((Double) object[6]).floatValue());
                        oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                        oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                        oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                        oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                        oRecord.setAvgLoad(((BigDecimal) avg_min_max[0]).floatValue());
                        oRecord.setPeakLoad(((BigDecimal) avg_min_max[1]).floatValue());
                        oRecord.setMinLoad(((BigDecimal) avg_min_max[2]).floatValue());
                        oRecord.setBaseCost(((Double) object[14]).floatValue());
                        oRecords.add(oRecord);
                    } catch(Exception e) {
                        logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
                    }
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
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
    @SuppressWarnings("unchecked")
    public List<Avgrecord> loadWeekEnergyConsumption(Long id, String propertyName, Date from, Date to) {
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            List results = null;
            String hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption_hourly as ec " + "where ec.fixture_id in(select id from fixture where "
                    + propertyName + " = " + id + ") " + "and capture_at <= :from " + "and capture_at > :to "
                    + "GROUP BY Day ORDER BY Day";
            if ("company".equals(propertyName)) {
                hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption_hourly as ec " + "where capture_at <= :from "
                        + "and capture_at > :to " + "GROUP BY Day ORDER BY Day";
            }
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            results = q.list();
            int count = 0;
            if (results != null && !results.isEmpty()) {
                List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
                for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    Avgrecord avgrecord = new Avgrecord();
                    avgrecord.setI(++count);
                    avgrecord.setCaptureOn((Date) object[0]);
                    avgrecord.setEN(((BigDecimal) object[1]).floatValue());
                    avgrecord.setBasePowerUsed(((BigDecimal) object[2]).floatValue());
                    avgrecord.setCost(((Double) object[3]).floatValue());
                    avgrecord.setBaseCost(((Double) object[4]).floatValue());
                    avgrecord.setPrice(((Double) object[5]).floatValue());
                    avgrecords.add(avgrecord);
                }
                return avgrecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load week summary
     * 
     * @param columnName
     *            value = {company, campus_id, building_id, floor_id, area_id}
     * @param id
     *            of the column in request
     * @param from
     *            latest date
     * @param to
     *            older date
     * @return Dashboard List
     */
    public List<DashboardRecord> loadWeekSummary(Long id, String columnName, Date from, Date to) {
        List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);

        try {
            String hsql = "SELECT date_trunc('day', ec.capture_at  - interval '1 hour') AS Day, "
                    + "sum(ec.power_used) as totalConsum, " + "sum(ec.base_power_used) as baseTotalConsum, "
                    + "sum(ec.saved_power_used) as totalPowerSaved, " + "max(ec.price) as price, "
                    + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad, "+ "sum(ec.base_cost) as baseCost " + "from " + tableName + " ec where ";
            if (!columnName.equals("company_id")) {
            	String filterTableName = "device";
            	if(columnName.equals("group_id")) {
            		filterTableName = "fixture";
            	}
              hsql += " ec.fixture_id in (select id from " + filterTableName + " where " + columnName + " =" + id + ") and";
            }
            if (tableName.equals("energy_consumption"))
                hsql += " ec.zero_bucket != 1 and ";
            hsql += " ec.capture_at <= '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' " + " Group by Day Order by Day";

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            Object[] avg_min_max = ((List<Object[]>)loadPeakAndMinLoadQuery("energy_consumption_hourly", columnName, id.toString(), to, from)).get(0);
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    try {
                        Object[] object = (Object[]) iterator.next();
                        DashboardRecord oRecord = new DashboardRecord();
                        oRecord.setCaptureOn(((Date) object[0]));
                        oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                        oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                        oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                        oRecord.setPrice(((Double) object[4]).floatValue());
                        oRecord.setCost(((Double) object[5]).floatValue());
                        oRecord.setSavedCost(((Double) object[6]).floatValue());
                        oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                        oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                        oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                        oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                        oRecord.setAvgLoad(((BigDecimal) avg_min_max[0]).floatValue());
                        oRecord.setPeakLoad(((BigDecimal) avg_min_max[1]).floatValue());
                        oRecord.setMinLoad(((BigDecimal) avg_min_max[2]).floatValue());
                        oRecord.setBaseCost(((Double) object[14]).floatValue());
                        oRecords.add(oRecord);
                    } catch(Exception e) {
                        logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
                    }
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
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
    @SuppressWarnings("unchecked")
    public List<Avgrecord> loadMonthEnergyConsumption(Long id, String propertyName, Date from, Date to) {
        try {
            List results = null;
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            String hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption_hourly as ec " + "where ec.fixture_id in(select id from fixture where "
                    + propertyName + " = " + id + ") " + "and capture_at <= :from " + "and capture_at > :to "
                    + "GROUP BY Day ORDER BY Day";
            if ("company".equals(propertyName)) {
                hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption_hourly as ec " + "where capture_at <= :from "
                        + "and capture_at > :to " + "GROUP BY Day ORDER BY Day";
            }
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            results = q.list();
            int count = 0;
            if (results != null && !results.isEmpty()) {
                List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
                for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    Avgrecord avgrecord = new Avgrecord();
                    avgrecord.setI(++count);
                    avgrecord.setCaptureOn((Date) object[0]);
                    avgrecord.setEN(((BigDecimal) object[1]).floatValue());
                    avgrecord.setBasePowerUsed(((BigDecimal) object[2]).floatValue());
                    avgrecord.setCost(((Double) object[3]).floatValue());
                    avgrecord.setBaseCost(((Double) object[4]).floatValue());
                    avgrecord.setPrice(((Double) object[5]).floatValue());
                    avgrecords.add(avgrecord);
                }
                return avgrecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load month summary
     * 
     * @param columnName
     *            value = {company, campus_id, building_id, floor_id, area_id}
     * @param id
     *            of the column in request
     * @param from
     *            latest date
     * @param to
     *            older date
     * @return Dashboard List
     */
    public List<DashboardRecord> loadMonthSummary(Long id, String columnName, Date from, Date to) {
        List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);

        try {
            String hsql = "SELECT date_trunc('day', ec.capture_at  - interval '1 hour') AS Day, "
                    + "sum(ec.power_used) as totalConsum, " + "sum(ec.base_power_used) as baseTotalConsum, "
                    + "sum(ec.saved_power_used) as totalPowerSaved, " + "max(ec.price) as price, "
                    + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad, "+ "sum(ec.base_cost) as baseCost " + "from " + tableName + " ec where ";
            if (!columnName.equals("company_id")) {
            	String filterTableName = "device";
            	if(columnName.equals("group_id")) {
            		filterTableName = "fixture";
            	}
            	hsql += " ec.fixture_id in (select id from " + filterTableName + " where " + columnName + " =" + id + ") and";
            }
            if (tableName.equals("energy_consumption"))
                hsql += " ec.zero_bucket != 1 and ";
            hsql += " ec.capture_at <= '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' " + " Group by Day Order by Day";
            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            Object[] avg_min_max = ((List<Object[]>)loadPeakAndMinLoadQuery("energy_consumption_hourly", columnName, id.toString(), to, from)).get(0);
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    try {
                        Object[] object = (Object[]) iterator.next();
                        DashboardRecord oRecord = new DashboardRecord();
                        oRecord.setCaptureOn(((Date) object[0]));
                        oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                        oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                        oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                        oRecord.setPrice(((Double) object[4]).floatValue());
                        oRecord.setCost(((Double) object[5]).floatValue());
                        oRecord.setSavedCost(((Double) object[6]).floatValue());
                        oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                        oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                        oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                        oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                        oRecord.setAvgLoad(((BigDecimal) avg_min_max[0]).floatValue());
                        oRecord.setPeakLoad(((BigDecimal) avg_min_max[1]).floatValue());
                        oRecord.setMinLoad(((BigDecimal) avg_min_max[2]).floatValue());
                        oRecord.setBaseCost(((Double) object[14]).floatValue());
                        oRecords.add(oRecord);
                    } catch(Exception e) {
                        logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
                    }
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
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
    @SuppressWarnings("unchecked")
    public List<Avgrecord> loadYearEnergyConsumption(Long id, String columnName, Date from, Date to) {
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            List results = null;
            String hsql = "SELECT date_trunc('month', capture_at - interval '1 day') AS Month , "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption_daily as ec " + "where ec.fixture_id in(select id from fixture where "
                    + columnName + " = " + id + ") " + "and capture_at <= :from " + "and capture_at > :to "
                    + "GROUP BY Month ORDER BY Month";
            if ("company".equals(columnName)) {
                hsql = "SELECT date_trunc('month', capture_at - interval '1 day') AS Month , "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption_daily as ec " + "where capture_at <= :from "
                        + "and capture_at > :to " + "GROUP BY Month ORDER BY Month";
            }
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            results = q.list();
            int count = 0;
            if (results != null && !results.isEmpty()) {
                List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
                for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    Avgrecord avgrecord = new Avgrecord();
                    avgrecord.setI(++count);
                    avgrecord.setCaptureOn((Date) object[0]);
                    avgrecord.setEN(((BigDecimal) object[1]).floatValue());
                    avgrecord.setBasePowerUsed(((BigDecimal) object[2]).floatValue());
                    avgrecord.setCost(((Double) object[3]).floatValue());
                    avgrecord.setBaseCost(((Double) object[4]).floatValue());
                    avgrecord.setPrice(((Double) object[5]).floatValue());
                    avgrecords.add(avgrecord);
                }
                return avgrecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load yearly summary
     * 
     * @param columnName
     *            value = {company, campus_id, building_id, floor_id, area_id}
     * @param id
     *            of the column in request
     * @param from
     *            latest date
     * @param to
     *            older date
     * @return Dashboard List
     */
    public List<DashboardRecord> loadYearSummary(Long id, String columnName, Date from, Date to) {
        List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);

        try {
            String hsql = "SELECT date_trunc('month', ec.capture_at  - interval '1 day') AS Month, "
                    + "sum(ec.power_used) as totalConsum, " + "sum(ec.base_power_used) as baseTotalConsum, "
                    + "sum(ec.saved_power_used) as totalPowerSaved, " + "max(ec.price) as price, "
                    + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad, "+ "sum(ec.base_cost) as baseCost " + "from " + tableName + " ec where ";
            if (!columnName.equals("company_id")) {
            	String filterTableName = "device";
            	if(columnName.equals("group_id")) {
            		filterTableName = "fixture";
            	}
            	hsql += " ec.fixture_id in (select id from " + filterTableName + " where " + columnName + " =" + id + ") and";
            }
            if (tableName.equals("energy_consumption"))
                hsql += " ec.zero_bucket != 1 and ";
            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' " + " Group by Month Order by Month";

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            Object[] avg_min_max = ((List<Object[]>)loadPeakAndMinLoadQuery("energy_consumption_hourly", columnName, id.toString(), to, from)).get(0);
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    try {
                        Object[] object = (Object[]) iterator.next();
                        DashboardRecord oRecord = new DashboardRecord();
                        oRecord.setCaptureOn(((Date) object[0]));
                        oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                        oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                        oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                        oRecord.setPrice(((Double) object[4]).floatValue());
                        oRecord.setCost(((Double) object[5]).floatValue());
                        oRecord.setSavedCost(((Double) object[6]).floatValue());
                        oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                        oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                        oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                        oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                        oRecord.setAvgLoad(((BigDecimal) avg_min_max[0]).floatValue());
                        oRecord.setPeakLoad(((BigDecimal) avg_min_max[1]).floatValue());
                        oRecord.setMinLoad(((BigDecimal) avg_min_max[2]).floatValue());
                        oRecord.setBaseCost(((Double) object[14]).floatValue());
                        oRecords.add(oRecord);
                    } catch(Exception e) {
                        logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
                    }
                }
            }
            else {
            	tableName = "energy_consumption_hourly";
                hsql = "SELECT date_trunc('month', ec.capture_at  - interval '1 day') AS Month, "
                        + "sum(ec.power_used) as totalConsum, " + "sum(ec.base_power_used) as baseTotalConsum, "
                        + "sum(ec.saved_power_used) as totalPowerSaved, " + "max(ec.price) as price, "
                        + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                        + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                        + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                        + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                        + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+ "from " + tableName + " ec where ";
                if (!columnName.equals("company_id")) {
                	String tableFilterName = "device";
                	if(columnName.equals("group_id")) {
                		tableFilterName = "fixture";
                	}
                	hsql += " ec.fixture_id in (select id from " + tableFilterName + " where " + columnName + " =" + id + ") and";
                }
                if (tableName.equals("energy_consumption"))
                    hsql += " ec.zero_bucket != 1 and ";
                hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                        + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                        + "' " + " Group by Month Order by Month";

                q = getSession().createSQLQuery(hsql.toString());
                results = q.list();
                if (results != null && !results.isEmpty()) {
                    for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                        try {
                            Object[] object = (Object[]) iterator.next();
                            DashboardRecord oRecord = new DashboardRecord();
                            oRecord.setCaptureOn(((Date) object[0]));
                            oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                            oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                            oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                            oRecord.setPrice(((Double) object[4]).floatValue());
                            oRecord.setCost(((Double) object[5]).floatValue());
                            oRecord.setSavedCost(((Double) object[6]).floatValue());
                            oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                            oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                            oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                            oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                            oRecord.setAvgLoad(((BigDecimal) object[11]).floatValue());
                            oRecord.setPeakLoad(((BigDecimal) object[12]).floatValue());
                            oRecord.setMinLoad(((BigDecimal) object[13]).floatValue());
                            oRecord.setBaseCost(((Double) object[14]).floatValue());
                            oRecords.add(oRecord);
                        } catch(Exception e) {
                            logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
                        }
                    }
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * load fixture count that contributed to the energy consumption in the given period.
     * 
     * @param id
     * @param property
     * @param columnName
     * @param from
     * @param to
     * @return count
     */
    @SuppressWarnings("unchecked")
    public Integer getFixtureCount(Long id, String property, String columnName, Date from, Date to) {
        int fixtureCount = 0;
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            List results = null;
            String tableFilterName = "device";
          	if(columnName.equals("group_id")) {
          		tableFilterName = "fixture";
          	}
            String hsql = "SELECT count(distinct ec.fixture_id) as fixtureCount " + "FROM " + property + " as ec "
                    + "where ec.fixture_id in (select id from " + tableFilterName + " where " + columnName + " = " + id + ") "
                    + "and capture_at <= :from " + "and capture_at > :to ";
            if (property.equalsIgnoreCase("energy_consumption"))
                hsql += " and ec.zero_bucket != 1 ";
            if ("company".equals(columnName)) {
                hsql = "SELECT  count(distinct ec.fixture_id) as fixtureCount " + "FROM " + property + " as ec "
                        + "where capture_at <= :from " + "and capture_at > :to ";
                if (property.equalsIgnoreCase("energy_consumption"))
                    hsql += " and ec.zero_bucket != 1 ";
            }
            logger.debug(hsql);
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                BigInteger bidata = (BigInteger) results.get(0);
                fixtureCount = bidata.intValue();
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return fixtureCount;
    }

    public void updateZeroBuckets(long fixtureId, Date lastStatsDate, Date startDate, Date latestStatsDate,
	int newCU, boolean sweepEnabled) {

        Connection con = null;
        CallableStatement calSt = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            int sweep = 0;
            if(sweepEnabled) {
              sweep = 1;
            }
            String query = "{ call updatezerobuckets(" + fixtureId + ", '" + new Timestamp(lastStatsDate.getTime())
                    + "', '" + new Timestamp(startDate.getTime()) + "', '" + new Timestamp(latestStatsDate.getTime())
                    + "', " + newCU + ", " + sweep + ")}";
            if(logger.isDebugEnabled()) {
              logger.debug("zero bucket query -- " + query);
            }
            calSt = con.prepareCall(query);
            calSt.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void fillGemsMissingBuckets(Date toDate) {

        Connection con = null;
        CallableStatement calSt = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            String query = "{ call fillGemsMissingBuckets('" + new Timestamp(toDate.getTime()) + "')}";
            logger.debug("zero bucket query -- " + query);
            calSt = con.prepareCall(query);
            calSt.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method fillGemsMissingBuckets
    
    public void fillGemsZeroBuckets(Date toDate) {

        Connection con = null;
        CallableStatement calSt = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            String query = "{ call fillGemsZeroBucketsForNextTenMinutes('" + new Timestamp(toDate.getTime()) + "')}";
            logger.debug("zero bucket query -- " + query);
            calSt = con.prepareCall(query);
            calSt.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method fillGemsMissingBuckets

    public Long getEnergyConsumptionId(Long fixtureId, Date timeStamp){
    	  Session session = getSession();
          Long ecId = (Long)session.createCriteria(EnergyConsumption.class)
        		  .setProjection(Projections.id())
                  .add(Restrictions.eq("fixture.id", fixtureId))
                  .add(Restrictions.eq("captureAt", timeStamp)).uniqueResult();
          return ecId;          
    }

/*    public List<DRRecord> loadAvgEnergyConsumptionBetweenPeriodsPerDay(Date fromTimeOfDay, Date toTimeOfDay,
            int groupId, int days) {
        List<DRRecord> drRecords = new ArrayList<DRRecord>();
        Connection con = null;
        CallableStatement toesUp = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            toesUp = con.prepareCall("{ call loadAvgEnergyConsumptionBetweenPeriodsPerDay ('"
                    + new Timestamp(fromTimeOfDay.getTime()) + "', '" + new Timestamp(toTimeOfDay.getTime()) + "', "
                    + groupId + ", " + days + ")}");
            logger.info(">>>>>" + "{ call loadAvgEnergyConsumptionBetweenPeriodsPerDay ('"
                    + new Timestamp(fromTimeOfDay.getTime()) + "', '" + new Timestamp(toTimeOfDay.getTime()) + "', "
                    + groupId + ", " + days + ")}");
            toesUp.execute();
            ResultSet resultSet = toesUp.getResultSet();
            while (resultSet.next()) {
                DRRecord drRecord = new DRRecord();
                drRecord.setDay(resultSet.getDate("Day"));
                drRecord.setPowerUsed(resultSet.getFloat("powerUsed"));
                drRecord.setBasePowerUsed(resultSet.getFloat("basePowerUsed"));
                drRecord.setSavedPower(resultSet.getFloat("savedPower"));
                drRecord.setSavedCost(resultSet.getFloat("savedCost"));
                drRecord.setBaseCost(resultSet.getFloat("baseCost"));
                drRecord.setAvgPrice(resultSet.getFloat("avgPrice"));
                drRecords.add(drRecord);
            }
            toesUp.close();
            con.close();
            return drRecords;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public List<GroupECRecord> loadGroupEnergyConsumptionBetweenPeriods(Date from, Date to) {
        List<GroupECRecord> records = new ArrayList<GroupECRecord>();
        Connection con = null;
        CallableStatement toesUp = null;
        try {
            con = getSession().connection();
            con.setAutoCommit(false);
            toesUp = con.prepareCall("{ call loadGroupEnergyConsumptionBetweenPeriods ('"
                    + new Timestamp(from.getTime()) + "', '" + new Timestamp(to.getTime()) + "')}");
            logger.info(">>>>>" + "{ call loadGroupEnergyConsumptionBetweenPeriods ('" + new Timestamp(from.getTime())
                    + "', '" + new Timestamp(to.getTime()) + "')}");
            toesUp.execute();
            ResultSet resultSet = toesUp.getResultSet();
            while (resultSet.next()) {
                GroupECRecord groupRecord = new GroupECRecord();
                groupRecord.setI(resultSet.getInt("i"));
                groupRecord.setName(resultSet.getString("name"));
                groupRecord.setPowerUsed(resultSet.getFloat("powerUsed"));
                groupRecord.setBasePowerUsed(resultSet.getFloat("basePowerUsed"));
                groupRecord.setSavedPower(resultSet.getFloat("savedPower"));
                groupRecord.setSavedCost(resultSet.getFloat("savedCost"));
                groupRecord.setTotalFixtures(resultSet.getInt("totalFixtures"));
                records.add(groupRecord);
            }
            toesUp.close();
            con.close();
            return records;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public List<DashboardRecord> loadRecentSummary(Long id, String columnName, Date from, Date to) {
        List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
        String tableName = "energy_consumption";

        try {
            String hsql = "SELECT ec.capture_at , " + "sum(ec.power_used) as totalConsum, "
                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+ "from " + tableName + " ec where ";
            if (!columnName.equals("company_id")) {
            	String filterTableName = "device";
            	if(columnName.equals("group_id")) {
            		filterTableName = "fixture";
            	}
              hsql += " ec.fixture_id in (select id from " + filterTableName + " where " + columnName + " =" + id + ") and";
            }
            if (tableName.equals("energy_consumption"))
                hsql += " ec.zero_bucket != 1 and ";
            hsql += " ec.capture_at <= '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' group by ec.capture_at";

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    if (object[1] != null) {
                        try {
                            DashboardRecord oRecord = new DashboardRecord();
                            oRecord.setCaptureOn(((Date) object[0]));
                            oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                            oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                            oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                            oRecord.setPrice(((Double) object[4]).floatValue());
                            oRecord.setCost(((Double) object[5]).floatValue());
                            oRecord.setSavedCost(((Double) object[6]).floatValue());
                            oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                            oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                            oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                            oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                            oRecord.setAvgLoad(((BigDecimal) object[11]).floatValue());
                            oRecord.setPeakLoad(((BigDecimal) object[12]).floatValue());
                            oRecord.setMinLoad(((BigDecimal) object[13]).floatValue());
                            oRecord.setBaseCost(((Double) object[14]).floatValue());
                            oRecords.add(oRecord);
                        } catch(Exception e) {
                            logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
                        }
                    }
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
    }

    /**
     * load Energy consumption data for previous hour.
     * 
     * @param id
     * @param property
     */
	public List<DashboardRecord> loadHourSummary(Long pid, String property) {
		List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
        String tableName = "energy_consumption_hourly";
        Calendar oCalendarEnd = Calendar.getInstance();
        Calendar oCalendarBegin = Calendar.getInstance();
        try {
            String hsql = "SELECT  max(ec.capture_at) As Hour , " + "sum(ec.power_used) as totalConsum, "
                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad " + "from " + tableName + " ec where ";
            oCalendarEnd.add(Calendar.HOUR, -1) ;
            hsql += " ec.capture_at <'" + DateUtil.formatDate(oCalendarBegin.getTime(), "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >='" + DateUtil.formatDate(oCalendarEnd.getTime(), "yyyy-MM-dd HH:mm:ss")
                    + "' and ec.base_power_used != 0 ";

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results  = q.list();
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    if (object[1] != null) {
                        try {
                            DashboardRecord oRecord = new DashboardRecord();
                            oRecord.setCaptureOn(((Date) object[0]));
                            oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
                            oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
                            oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
                            oRecord.setPrice(((Double) object[4]).floatValue());
                            oRecord.setCost(((Double) object[5]).floatValue());
                            oRecord.setSavedCost(((Double) object[6]).floatValue());
                            oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
                            oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
                            oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
                            oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
                            oRecord.setAvgLoad(((BigDecimal) object[11]).floatValue());
                            oRecord.setPeakLoad(((BigDecimal) object[12]).floatValue());
                            oRecord.setMinLoad(((BigDecimal) object[13]).floatValue());
                            oRecords.add(oRecord);
                        } catch(Exception e) {
                            logger.warn("loadHourSummar: " + e.getMessage());
                        }
                    }
                }
            }
           
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords ;
	}

    public List<GroupECRecord> loadGroupEnergyConsumption(String property, Long pid, Date from, Date to) {
        List<GroupECRecord> records = new ArrayList<GroupECRecord>();
        String tableName = "energy_consumption";
        tableName = selectTable(from, to);
        
        String hsql_custom = "SELECT COALESCE(sum(ec.power_used), 0) AS \"powerUsed\", " +
                " (SELECT COALESCE(sum(ec.base_power_used),0)) AS \"basePowerUsed\", " +
                " (SELECT COALESCE(sum(ec.saved_power_used),0)) AS \"savedPower\", " +
                " (SELECT COALESCE(sum(ec.saved_cost),0)) AS \"savedCost\", " +
                " (SELECT COALESCE(sum(ec.base_cost),0)) AS \"baseCost\", ";
                if (property.equals("company"))
                    hsql_custom += " (select count(id) from fixture where group_id=0  and state = 'COMMISSIONED' ) AS \"totalFixtures\" ";
                else
                    hsql_custom += " (select count(id) from fixture where group_id=0 and " + property + "=" + pid + "  and state = 'COMMISSIONED' ) AS \"totalFixtures\" ";
                    
                hsql_custom += " from fixture f " +
                " join " + tableName + " ec on ec.fixture_id = f.id ";

                if (!property.equals("company"))
                    hsql_custom += " and " + property + "=" + pid;

                if (tableName.equals("energy_consumption"))
                    hsql_custom += " and ec.zero_bucket != 1 ";
                
                hsql_custom += " and ec.capture_at < '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' " +
                " and ec.capture_at > '" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "' " +
                " and f.group_id = 0";
        Query q = getSession().createSQLQuery(hsql_custom.toString());
        List<Object[]> results_custom = q.list();
        if (results_custom != null && !results_custom.isEmpty()) {
            for (Iterator<Object[]> iterator = results_custom.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                if (object[1] != null) {
                    GroupECRecord groupRecord = new GroupECRecord();
                    groupRecord.setI(0);
                    groupRecord.setName("Custom");
                    groupRecord.setPowerUsed(((BigDecimal)object[0]).floatValue());
                    groupRecord.setBasePowerUsed(((BigDecimal)object[1]).floatValue());
                    groupRecord.setSavedPower(((BigDecimal)object[2]).floatValue());
                    groupRecord.setSavedCost(((Double)object[3]).floatValue());
                    // object[4] baseCost
                    groupRecord.setTotalFixtures(((BigInteger)object[5]).intValue());
                    records.add(groupRecord);
                }
            }
        }

        // Groups other than custom
        String hsql = "select g.id as \"i\", " +
        		"g.name as \"name\", " +
        		" (SELECT COALESCE(sum(ec.power_used), 0)) AS \"powerUsed\", " +
        		" (SELECT COALESCE(sum(ec.base_power_used),0)) AS \"basePowerUsed\", " +
        		" (SELECT COALESCE(sum(ec.saved_power_used),0)) AS \"savedPower\", " +
        		" (SELECT COALESCE(sum(ec.saved_cost),0)) AS \"savedCost\", " +
        		" (SELECT COALESCE(sum(ec.base_cost),0)) AS \"baseCost\", ";
        		if (property.equals("company"))
                    hsql += " (select count(id) from fixture where group_id=g.id and state = 'COMMISSIONED' ) AS \"totalFixtures\"  ";
        		else
                    hsql += " (select count(id) from fixture where group_id=g.id and " + property + "=" + pid + " and state = 'COMMISSIONED' ) AS \"totalFixtures\" ";
        		    
        		hsql += " from groups g " +
        		" left join fixture f on g.id = f.group_id " +
        		" left join " + tableName + " ec on ec.fixture_id = f.id ";

        		if (!property.equals("company"))
                    hsql += " and " + property + "=" + pid;

        		if (tableName.equals("energy_consumption"))
       		        hsql += " and ec.zero_bucket != 1 ";
       		    
        		hsql += " and ec.capture_at < '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' " +
        		" and ec.capture_at > '" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "' " +
        		" group by g.id, g.name order by g.id";
        		
        q = getSession().createSQLQuery(hsql.toString());
        List<Object[]> results = q.list();
        if (results != null && !results.isEmpty()) {
            for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                if (object[1] != null) {
                    GroupECRecord groupRecord = new GroupECRecord();
                    groupRecord.setI(((BigInteger)object[0]).intValue());
                    groupRecord.setName((String)object[1]);
                    groupRecord.setPowerUsed(((BigDecimal)object[2]).floatValue());
                    groupRecord.setBasePowerUsed(((BigDecimal)object[3]).floatValue());
                    groupRecord.setSavedPower(((BigDecimal)object[4]).floatValue());
                    groupRecord.setSavedCost(((Double)object[5]).floatValue());
                    // object[6] baseCost
                    groupRecord.setTotalFixtures(((BigInteger)object[7]).intValue());
                    records.add(groupRecord);
                }
            }
        }
        return records;
    }
    
    @SuppressWarnings("unchecked")
    public List loadStatsInDateRange(String property, Long pid, Date from, Date to) {

      try {
	String hsql_custom = "SELECT f.id, COALESCE(ec.power_used, 0) AS \"powerUsed\", " +
	    " (SELECT COALESCE(ec.avg_volts,0)) AS \"avgVolts\", " +
	    " (SELECT COALESCE(ec.avg_temperature,0)) AS \"avgTemperature\", " +
	    " (SELECT COALESCE(ec.motion_bits,0)) AS \"occData\", " +
	    " (SELECT COALESCE(light_avg_level,0)) AS \"ambLevel\", capture_at " +                 
	    " from fixture f, energy_consumption ec where ec.fixture_id = f.id and " +
	    " ec.capture_at > '" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' " +
	    " and ec.capture_at <= '" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss") + "' " +
	    " and ec.zero_bucket != 1 ";
      
	if(!property.equals("company")) {  
		String filterTableName = "device";
		if(property.equals("group_id")) {
			filterTableName = "fixture";
		}
	  hsql_custom += " and ec.fixture_id in (select id from " + filterTableName + " where " + property + 
	      " = " + pid + ")";
	  //System.out.println("query -- " + hsql_custom.toString());
	}
   
	Query q = getSession().createSQLQuery(hsql_custom.toString());
	List<Object[]> results = q.list();
	if (results != null && !results.isEmpty()) {
	  return results;
	}
      }
      catch(Exception e) {
	e.printStackTrace();
      }
      return null;
	
    } //end of method loadStatsInDateRange
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Double> getFixtureECOverPeriod(Date startTime, Date endTime) {
		Map<Integer, Double> map = new TreeMap<Integer, Double>(); 
        try {
        	
        	Calendar cal = Calendar.getInstance();
        	cal.setTime(startTime);
        	cal.add(Calendar.DATE, -17);
        	Date queryStartTime = cal.getTime();
        	
        	Integer endMin = Integer.parseInt(DateUtil.formatDate(endTime, "mm"));
        	Integer endHour = Integer.parseInt(DateUtil.formatDate(endTime, "HH"));;
        	if(endMin.compareTo(0) > 0) {
        		endHour += 1;
        	}
        	String hsql = null;
        	if(endHour < 24) {
                hsql = "SELECT ec.fixture_id, avg(ec.power_used) as powerUsed "
                        + " from energy_consumption_hourly ec " 
                        + " where ec.capture_at > '" + DateUtil.formatDate(queryStartTime, 
                    	"yyyy-MM-dd HH:mm:ss") + "' and ec.fixture_id in " 
                        + "(select id from fixture where state = 'COMMISSIONED') "
                        + " and EXTRACT(HOUR FROM ec.capture_at) <=  '" + endHour + "'"
                        + " and EXTRACT(HOUR FROM ec.capture_at) > '" + DateUtil.formatDate(startTime, "HH") + "'" 
                        + " and rtrim(to_char(ec.capture_at, 'Day')) in " +
                        	"(select rtrim(wd.day) from fixture f, groups g, profile_handler ph, weekday wd " +
                        	"where f.id = ec.fixture_id and f.group_id = g.id and ph.id = g.profile_handler_id and " +
                        	"ph.profile_configuration_id = wd.profile_configuration_id and wd.type = 'weekday') "
                        + " group by ec.fixture_id"
                        ;
        	}
        	else {
        		hsql = "select fc.fixture_id, avg(fc.power_used) as powerUsed from "
    					+ "(SELECT ec.fixture_id, ec.power_used, ec.capture_at "
                        + " from energy_consumption_hourly ec " 
                        + " where ec.capture_at > '" + DateUtil.formatDate(queryStartTime, 
                    	"yyyy-MM-dd HH:mm:ss") + "' and ec.fixture_id in " 
                        + "(select id from fixture where state = 'COMMISSIONED') "
                        + " and EXTRACT(HOUR FROM ec.capture_at) > '" + DateUtil.formatDate(startTime, "HH") + "'" 
                        + " and rtrim(to_char(ec.capture_at, 'Day')) in " +
                        	"(select rtrim(wd.day) from fixture f, groups g, profile_handler ph, weekday wd " +
                        	"where f.id = ec.fixture_id and f.group_id = g.id and ph.id = g.profile_handler_id and " +
                        	"ph.profile_configuration_id = wd.profile_configuration_id and wd.type = 'weekday') "
                        + " UNION "
                        + " SELECT ec.fixture_id, ec.power_used, ec.capture_at "
                        + " from energy_consumption_hourly ec " 
                        + " where ec.capture_at > '" + DateUtil.formatDate(queryStartTime, 
                    	"yyyy-MM-dd HH:mm:ss") + "' and ec.fixture_id in " 
                        + "(select id from fixture where state = 'COMMISSIONED') "
                        + " and EXTRACT(HOUR FROM ec.capture_at) = 0" 
                        + " and rtrim(to_char(ec.capture_at - interval '24 hours', 'Day')) in " +
                        	"(select rtrim(wd.day) from fixture f, groups g, profile_handler ph, weekday wd " +
                        	"where f.id = ec.fixture_id and f.group_id = g.id and ph.id = g.profile_handler_id and " +
                        	"ph.profile_configuration_id = wd.profile_configuration_id and wd.type = 'weekday')) as fc "
                    	+ " group by fc.fixture_id"
                        ;
        	}


            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    if (object[0] != null) {
                        try {
                        	map.put(((BigInteger)object[0]).intValue(), ((BigDecimal)object[1]).doubleValue());
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return map;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Double> getRecentFixtureEC() {
		Map<Integer, Double> map = new TreeMap<Integer, Double>(); 
        try {
          long currentTime = System.currentTimeMillis();
            String hsql = "SELECT ec.fixture_id, ec.power_used as powerUsed "
                    + " from energy_consumption ec " 
                    + " where ec.fixture_id in (select id from fixture where state = 'COMMISSIONED') "
                    + " and ec.capture_at <=  '" + DateUtil.formatDate(new Date(currentTime - 5*60*1000), "yyyy-MM-dd HH:mm:ss") + "'"
                    + " and ec.capture_at > '" + DateUtil.formatDate(new Date(currentTime - 10*60*1000), "yyyy-MM-dd HH:mm:ss") + "'" 
                    ;

            Query q = getSession().createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                    Object[] object = (Object[]) iterator.next();
                    if (object[0] != null) {
                        try {
                        	map.put(((BigInteger)object[0]).intValue(), ((BigDecimal)object[1]).doubleValue());
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return map;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return map;
	}

    /**
	 * load Energy consumption data for previous hour.
	 * 
	 * @param id
	 * @param property
	 */
	public List<DashboardRecord> loadHourSummaryBetween(Long pid, String property , Date from,Date to) {
		List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
		String tableName = "energy_consumption_hourly";
		Calendar oCalendarEnd = Calendar.getInstance();
		oCalendarEnd.setTime(to) ;
		Calendar oCalendarBegin = Calendar.getInstance();
		 oCalendarBegin.setTime(from) ;
		try {
			String hsql = "SELECT date_trunc('hour', capture_at) As Hour," +
					" sum(ec.power_used) as totalConsum, " +
					"sum(ec.base_power_used) as baseTotalConsum," +
					" sum(ec.saved_power_used) as totalPowerSaved," +
					"max(ec.price) as price," +
					"sum(ec.cost) as cost," +
					"sum(ec.saved_cost) as savedCost," +
					" sum(ec.occ_saving) as totalOccSaved," +
					" sum(ec.tuneup_saving) as totalTuneupSaving," +
					" sum(ec.ambient_saving) as totalAmbientSaved," +
					" sum(ec.manual_saving) as totalManualSaving," +
					" avg(ec.power_used) as avgLoad," +
					" max(ec.power_used) as peakLoad," +
					" min(ec.power_used) as minLoad," +
					" sum(ec.base_cost) as baseCost" +
					" from energy_consumption_hourly ec where ";
			hsql += " ec.capture_at <'"
					+ DateUtil.formatDate(oCalendarEnd.getTime(),
							"yyyy-MM-dd HH:mm:ss")
					+ "' "
					+ "and ec.capture_at >='"
					+ DateUtil.formatDate(oCalendarBegin.getTime(),
							"yyyy-MM-dd HH:mm:ss")
					+ "' and ec.base_power_used != 0 "
			+ " Group by Hour Order by Hour";

			Query q = getSession().createSQLQuery(hsql.toString());
			List<Object[]> results = q.list();
			if (results != null && !results.isEmpty()) {
				for (Iterator<Object[]> iterator = results.iterator(); iterator
						.hasNext();) {
					Object[] object = (Object[]) iterator.next();
					if (object[1] != null) {
						DashboardRecord oRecord = new DashboardRecord();
						oRecord.setCaptureOn(((Date) object[0]));
						oRecord.setPowerused(((BigDecimal) object[1])
								.doubleValue());
						oRecord.setBasePowerUsed(((BigDecimal) object[2])
								.doubleValue());
						oRecord.setSavedPower(((BigDecimal) object[3])
								.doubleValue());
						oRecord.setPrice(((Double) object[4]).floatValue());
						oRecord.setCost(((Double) object[5]).floatValue());
						oRecord.setSavedCost(((Double) object[6]).floatValue());
						oRecord.setOccsaving(((BigDecimal) object[7])
								.doubleValue());
						oRecord.setTasktuneupsaving(((BigDecimal) object[8])
								.doubleValue());
						oRecord.setAmbientsaving(((BigDecimal) object[9])
								.doubleValue());
						oRecord.setManualsaving(((BigDecimal) object[10])
								.doubleValue());
						oRecord.setAvgLoad(((BigDecimal) object[11])
								.floatValue());
						oRecord.setPeakLoad(((BigDecimal) object[12])
								.floatValue());
						oRecord.setMinLoad(((BigDecimal) object[13])
								.floatValue());
						 oRecord.setBaseCost(((Double) object[14]).floatValue());
						oRecords.add(oRecord);
					}
				}
			}

		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return oRecords;
	}	
	
	public Date loadFirstRecordDate() {
		Date date = null;
		List<Object[]> results = null;
		String hsql = null;
		try {

			hsql = "SELECT ec.capture_at , id "
					+ "FROM energy_consumption_hourly as ec " + "where "
					+ "ec.capture_at = (select Min(capture_at) from energy_consumption_hourly )";

			Session session = sessionFactory.getCurrentSession();
			Query q = session.createSQLQuery(hsql.toString());

			results = q.list();
			if (results != null && !results.isEmpty()) {
				for (Iterator<Object[]> iterator = results.iterator(); iterator
						.hasNext();) {
					Object[] object = (Object[]) iterator.next();
					date = (Date) object[0];
				}

			}
		} catch (Exception ex) {

			ex.printStackTrace();
		}
		return date;
	}

	
	/**
	 * load aggregated summary for children nodes.
	 * 
	 * @param columnName
	 *            value = {company, campus_id, building_id, floor_id}
	 * @param childColumnName value = {campus_id, building_id, floor_id, area_id}
	 * @param id
	 *            of the column in request
	 * @param from
	 *            latest date
	 * @param to
	 *            older date
	 * @return Dashboard List
	 */
	public List<DashboardRecord> loadChildrenNodeSummary(Long id, String columnName, String childColumnName, Date from, Date to) {
	    List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
	    String tableName = "energy_consumption";
	    tableName = selectTable(from, to);
	    
	    String childNodeTable = "";
	    if("campus_id".equals(childColumnName)) {
	    	childNodeTable = "campus";
	    } else if("building_id".equals(childColumnName)) {
	    	childNodeTable = "building";
	    } else if("floor_id".equals(childColumnName)) {
	    	childNodeTable = "floor";
	    } else if("area_id".equals(childColumnName)) {
	    	childNodeTable = "area";
	    }
	
	    try {
	        String hsql = "SELECT org.name as orgname, sum(ec.power_used) as totalConsum, "
	                + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                + "min(ec.power_used) as minLoad, org.id " + "from " + tableName + " ec, fixture f, " + childNodeTable + " org "
	                + "where ec.fixture_id = f.id  and f." + childColumnName + " = org.id and " ;
	        if (!columnName.equals("company_id")) {
	            hsql += " f." + columnName + " =" + id + " and";
	        }
	        if (tableName.equals("energy_consumption"))
	            hsql += " ec.zero_bucket != 1 and ";
	        hsql += " ec.capture_at <'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
	                + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
	                + "' " + " Group by org.id, orgname Order by orgname";
	
	        Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object[]> results = q.list();
	        
	        StringBuffer orgids = new StringBuffer("");
	        boolean firstRecord = true;
	        if (results != null && !results.isEmpty()) {
	            for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
	                try {
	                    Object[] object = (Object[]) iterator.next();
	                    DashboardRecord oRecord = new DashboardRecord();
	                    oRecord.setOrgName((String)object[0]);
	                    oRecord.setPowerused(((BigDecimal) object[1]).doubleValue());
	                    oRecord.setBasePowerUsed(((BigDecimal) object[2]).doubleValue());
	                    oRecord.setSavedPower(((BigDecimal) object[3]).doubleValue());
	                    oRecord.setPrice(((Double) object[4]).floatValue());
	                    oRecord.setCost(((Double) object[5]).floatValue());
	                    oRecord.setSavedCost(((Double) object[6]).floatValue());
	                    oRecord.setOccsaving(((BigDecimal) object[7]).doubleValue());
	                    oRecord.setTasktuneupsaving(((BigDecimal) object[8]).doubleValue());
	                    oRecord.setAmbientsaving(((BigDecimal) object[9]).doubleValue());
	                    oRecord.setManualsaving(((BigDecimal) object[10]).doubleValue());
	                    oRecord.setAvgLoad(((BigDecimal) object[11]).floatValue());
	                    oRecord.setPeakLoad(((BigDecimal) object[12]).floatValue());
	                    oRecord.setMinLoad(((BigDecimal) object[13]).floatValue());
	                    oRecords.add(oRecord);
	                    if(!firstRecord) {
	                    	orgids.append(", ");
	                    }
	                    else {
	                    	firstRecord = false;
	                    }
	                    orgids.append(((BigInteger) object[14]).toString());
	                } catch(Exception e) {
	                    logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
	                }
	            }
	        }
	        else {
	        	orgids.append("-1");
	        }
	        
	        hsql = "Select name, id from " + childNodeTable + " where " + columnName + " = " + id + " and id not in ( " + orgids + " )";
            q = getSession().createSQLQuery(hsql.toString());
	        results = q.list();
	        if (results != null && !results.isEmpty()) {
	            for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
	                try {
	                    Object[] object = (Object[]) iterator.next();
	                    DashboardRecord oRecord = new DashboardRecord();
	                    oRecord.setOrgName((String)object[0]);
	                    oRecord.setPowerused(0d);
	                    oRecord.setBasePowerUsed(0d);
	                    oRecord.setSavedPower(0d);
	                    oRecord.setPrice(0f);
	                    oRecord.setCost(0f);
	                    oRecord.setSavedCost(0f);
	                    oRecord.setOccsaving(0d);
	                    oRecord.setTasktuneupsaving(0d);
	                    oRecord.setAmbientsaving(0d);
	                    oRecord.setManualsaving(0d);
	                    oRecord.setAvgLoad(0f);
	                    oRecord.setPeakLoad(0f);
	                    oRecord.setMinLoad(0f);
	                    oRecords.add(oRecord);
	                } catch(Exception e) {
	                    logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
	                }
	            }
	        }
	        
	        
	    } catch (HibernateException hbe) {
	        throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	    }
	    return oRecords;
	}

}


