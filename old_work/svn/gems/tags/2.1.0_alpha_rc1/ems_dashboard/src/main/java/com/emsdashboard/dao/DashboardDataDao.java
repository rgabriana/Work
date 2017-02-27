package com.emsdashboard.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.BaseDaoHibernate;
import com.emsdashboard.model.EnergyConsumption;
import com.emsdashboard.model.Avgrecord;
import com.emsdashboard.model.DashboardRecord;
import com.emsdashboard.model.GemsServer;
import com.emsdashboard.model.MeterRecord;
import com.emsdashboard.service.GemsManager;
import com.emsdashboard.types.Status;
import com.emsdashboard.utils.DateUtil;
@Repository("dashboardDataDao")
@Transactional(propagation = Propagation.REQUIRED)

public class DashboardDataDao  extends BaseDaoHibernate {
    
    static final Logger logger = Logger.getLogger(DashboardDataDao.class.getName());
    
    @Resource
    SessionFactory sessionFactory;
    @Resource(name = "gemsManager")
    private GemsManager gemsManager;
    public List<MeterRecord> loadMeterDataWithDateRange(String columnName, String id, java.util.Date oFDate, java.util.Date oTDate, String gemID) {
        List<MeterRecord> oRecords = new ArrayList<MeterRecord>();
        //String tableName = "energy_consumption";
        //tableName = selectTable(from, to);
       
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
                + "from  energy_consumption ec where ";
        
                hsql += " ec.gems_id in (select id from gems "+"where  id = :gemID" + " )) as ss";
       
        // Protect against divide by zero error
        hsql += " where baseTotalConsum != 0";
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createSQLQuery(hsql.toString());
        int GEMSID = Integer.parseInt(gemID);
        q.setParameter("gemID", GEMSID);
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
        return oRecords;
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
    public List<Avgrecord> loadDayEnergyConsumption(Long id, String propertyName, Date from, Date to, String gemID) {
        List results = null;
        String hsql=null;
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            /*
            String hsql = "SELECT date_trunc('hour', capture_at) AS Hour , "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption as ec " + "where ec.fixture_id in(select id from fixture where "
                    + propertyName + " = " + id + ") " + "and capture_at < :from " + "and capture_at > :to "
                    + "GROUP BY Hour ORDER BY Hour";
                    */
            if ("company".equals(propertyName)) {
                hsql = "SELECT date_trunc('hour', capture_at) AS Hour, "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption as ec " + "where capture_at < :from and ec.gems_id in (select id from gems" + " where  id = :gemID" +")"
                        + "and capture_at > :to " + "GROUP BY Hour ORDER BY Hour";
            }
            Session session = sessionFactory.getCurrentSession();
            Query q = session.createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            int GEMSID = Integer.parseInt(gemID);
            q.setParameter("gemID", GEMSID);
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
        return loadRecentEnergyConsumption(id, propertyName, from, to,gemID);
    }

    public List<Avgrecord> loadRecentEnergyConsumption(Long id, String propertyName, Date from, Date to, String gemID) {
       
        logger.debug("From: " + from.toString() + ", To: " + to.toString());
        List results = null;
        /*
        String hsql = "SELECT date_trunc('hour', capture_at) AS Hour , "
                + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                + "FROM energy_consumption as ec " + "where ec.fixture_id in(select id from fixture where "
                + propertyName + " = " + id + ") " + "and capture_at < :from " + "and capture_at > :to "
                + "GROUP BY Hour ORDER BY Hour";
         */
        String hsql = null;
        if ("company".equals(propertyName)) {
            hsql = "SELECT date_trunc('hour', capture_at) AS Hour, "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption as ec " + "where capture_at < :from " + "and capture_at > :to " + "and ec.gems_id in (select id from gems" + " where  id = :gemID" +")"
                    + "GROUP BY Hour ORDER BY Hour";
        }
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createSQLQuery(hsql.toString());
        q.setTimestamp("from", from);
        q.setTimestamp("to", to);
        int GEMSID = Integer.parseInt(gemID);
        q.setParameter("gemID", GEMSID);
        results = q.list();
        int count = 0;
        List<Avgrecord> avgrecords = new ArrayList<Avgrecord>();
        if (results != null && !results.isEmpty()) {
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
        }
        return avgrecords;
        
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
    public List<Avgrecord> loadWeekEnergyConsumption(Long id, String propertyName, Date from, Date to,String gemID) {
        try {
            logger.debug("From: " + from.toString() + ", To: " + to.toString());
            List results = null;
            String hsql = null;
            /*
            String hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption_hourly as ec " + "where ec.fixture_id in(select id from fixture where "
                    + propertyName + " = " + id + ") " + "and capture_at <= :from " + "and capture_at > :to "
                    + "GROUP BY Day ORDER BY Day";
                    */
            if ("company".equals(propertyName)) {
                hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                        + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                        + "FROM energy_consumption as ec " + "where capture_at <= :from "
                        + "and capture_at > :to " + "GROUP BY Day ORDER BY Day";
            }
            Session session = sessionFactory.getCurrentSession();
            Query q = session.createSQLQuery(hsql.toString());
            q.setTimestamp("from", from);
            q.setTimestamp("to", to);
            int GEMSID = Integer.parseInt(gemID);
            q.setParameter("gemID", GEMSID);
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
     * load Monthly energy consumption
     * 
     * @param id
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Avgrecord> loadMonthEnergyConsumption(Long id, String propertyName, Date from, Date to,String gemID) {
        List results = null;
        String hsql=null;
        logger.debug("From: " + from.toString() + ", To: " + to.toString());
        /*
        String hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                + "FROM energy_consumption_hourly as ec " + "where ec.fixture_id in(select id from fixture where "
                + propertyName + " = " + id + ") " + "and capture_at <= :from " + "and capture_at > :to "
                + "GROUP BY Day ORDER BY Day";
                */
        if ("company".equals(propertyName)) {
            hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption as ec " + "where capture_at <= :from "
                    + "and capture_at > :to and ec.gems_id in (select id from gems" + " where  id = :gemID" +")" + "GROUP BY Day ORDER BY Day";
        }
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createSQLQuery(hsql.toString());
        q.setTimestamp("from", from);
        q.setTimestamp("to", to);
        int GEMSID = Integer.parseInt(gemID);
        q.setParameter("gemID", GEMSID);
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
        return null;
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
    public List<Avgrecord> loadYearEnergyConsumption(Long id, String columnName, Date from, Date to,String gemID) {
        logger.debug("From: " + from.toString() + ", To: " + to.toString());
        List results = null;
        String hsql =null;
        /*
        String hsql = "SELECT date_trunc('month', capture_at - interval '1 day') AS Month , "
                + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                + "FROM energy_consumption_daily as ec " + "where ec.fixture_id in(select id from fixture where "
                + columnName + " = " + id + ") " + "and capture_at <= :from " + "and capture_at > :to "
                + "GROUP BY Month ORDER BY Month";
                */
        if ("company".equals(columnName)) {
            hsql = "SELECT date_trunc('month', capture_at - interval '1 day') AS Month , "
                    + "sum(ec.power_used) as pu,sum(ec.base_power_used) as bpu, sum(ec.cost) as cost, sum(ec.base_cost) as bcost, avg(ec.price) as price "
                    + "FROM energy_consumption as ec " + "where capture_at <= :from and ec.gems_id in (select id from gems" + " where  id = :gemID" +")"
                    + "and capture_at > :to " + "GROUP BY Month ORDER BY Month";
        }
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createSQLQuery(hsql.toString());
        q.setTimestamp("from", from);
        q.setTimestamp("to", to);
        int GEMSID = Integer.parseInt(gemID);
        q.setParameter("gemID", GEMSID);
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
        return null;
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
                    + "min(ec.power_used) as minLoad " + "from " + tableName + " ec" ;
    	            if (columnName.equals("gems_id")&&id>0) {
    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
    	            }
    	            else
    	            {
    	            	 hsql +=" where " ;
    	            }
            hsql += " ec.capture_at <'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
                    + "' and ec.base_power_used != 0 " + " Group by Hour Order by Hour";
            Session session = sessionFactory.getCurrentSession();
            Query q = session.createSQLQuery(hsql.toString());
            List<Object[]> results = q.list();
            if (results != null && !results.isEmpty()) {
                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
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
                    oRecords.add(oRecord);
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
    }

	public List<DashboardRecord> loadRecentSummary(Long id, String columnName, Date from, Date to) {
		  List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
	        String tableName = "energy_consumption";

	        try {
	            String hsql = "SELECT now() , " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad " + "from " + tableName + " ec" ;
	            if (columnName.equals("gems_id")&&id>0) {
	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	            }
	            else
	            {
	            	 hsql +=" where " ;
	            }
	            hsql += " ec.capture_at <'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
	                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
	                    + "' and ec.base_power_used != 0 ";
	            Session session = sessionFactory.getCurrentSession();
	            Query q = session.createSQLQuery(hsql.toString());
	            List<Object[]> results = q.list();
	            if (results != null && !results.isEmpty()) {
	                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
	                    Object[] object = (Object[]) iterator.next();
	                    if (object[1] != null) {
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
	                    }
	                }
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return oRecords;
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
	        if (diffDays < 2) {
	            logger.debug("Fetching readings from current table");
	            return "energy_consumption";
	        } else if (diffDays < 32) { // max days in month
	            logger.debug("Fetching readings from hourly table");
	            return "energy_consumption_hourly";
	        }
	        return "energy_consumption_daily";
	    }
	public List<DashboardRecord> loadWeekSummary(Long id, String columnName,
			Date from, Date to) {
		  List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
	        String tableName = "energy_consumption";
	        //tableName = selectTable(from, to);

	        try {
	            String hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad " + "from " + tableName + " ec" ;
	    	            if (columnName.equals("gems_id")&&id>0) {
	    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	    	            }
	    	            else
	    	            {
	    	            	 hsql +=" where " ;
	    	            }
	    	            hsql += " ec.capture_at <'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
	    	                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
	    	                    + "' and ec.base_power_used != 0 " + " Group by Day Order by Day";
	            Session session = sessionFactory.getCurrentSession();
	            Query q = session.createSQLQuery(hsql.toString());
	            List<Object[]> results = q.list();
	            if (results != null && !results.isEmpty()) {
	                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
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
	                    oRecords.add(oRecord);
	                }
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return oRecords;
		
	    }
	public List<DashboardRecord> loadMonthSummary(Long id, String columnName,
			Date from, Date to) {
		 List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
	        String tableName = "energy_consumption";
	        //tableName = selectTable(from, to);

	        try {
	            String hsql = "SELECT date_trunc('day', capture_at - interval '1 hour') AS Day, " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad " + "from " + tableName + " ec" ;
	    	            if (columnName.equals("gems_id")&&id>0) {
	    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	    	            }
	    	            else
	    	            {
	    	            	 hsql +=" where " ;
	    	            }
	    	            hsql += " ec.capture_at <'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
	    	                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
	    	                    + "' and ec.base_power_used != 0 " + " Group by Day Order by Day";
	            Session session = sessionFactory.getCurrentSession();
	            Query q = session.createSQLQuery(hsql.toString());
	            List<Object[]> results = q.list();
	            if (results != null && !results.isEmpty()) {
	                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
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
	                    oRecords.add(oRecord);
	                }
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return oRecords;
		
	}
	public List<DashboardRecord> loadYearSummary(Long id, String columnName,
			Date from, Date to) {
		 List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
	        String tableName = "energy_consumption";
	        //tableName = selectTable(from, to);

	        try {
	            String hsql = "SELECT date_trunc('month', capture_at - interval '1 day') AS Month, " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad " + "from " + tableName + " ec" ;
	    	            if (columnName.equals("gems_id")&&id>0) {
	    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	    	            }
	    	            else
	    	            {
	    	            	 hsql +=" where " ;
	    	            }
	    	            hsql += " ec.capture_at <'" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
	    	                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
	    	                    + "' and ec.base_power_used != 0 " + " Group by Month Order by Month";
	            Session session = sessionFactory.getCurrentSession();
	            Query q = session.createSQLQuery(hsql.toString());
	            List<Object[]> results = q.list();
	            if (results != null && !results.isEmpty()) {
	                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
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
	                    oRecords.add(oRecord);
	                }
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return oRecords;
	}
	public Boolean saveOneHourDashBoardDetails(DashboardRecord oRecord, String ip) {
		
		 try {
			 Calendar cal = Calendar.getInstance();
	          EnergyConsumption ec = new EnergyConsumption() ;
	            Session session = sessionFactory.getCurrentSession();
	            GemsServer serverData = gemsManager.loadGEMSByGemsIp(ip);
	            if(serverData.getStatus()!='I' )
	            { ec.setGemsId(serverData.getId()) ;
	            ec.setAmbientSaving(BigDecimal.valueOf(oRecord.getAmbientsaving())) ;
	            ec.setBaseCost(0.0f) ;
	            ec.setBasePowerUsed(BigDecimal.valueOf(oRecord.getBasePowerUsed())) ;
	            ec.setCaptureAt(oRecord.getCaptureOn()) ;
	            ec.setCost(oRecord.getCost()) ;
	            ec.setManualSaving(BigDecimal.valueOf(oRecord.getManualsaving()));
	            ec.setOccSaving(BigDecimal.valueOf(oRecord.getOccsaving())) ;
	            ec.setPowerUsed(BigDecimal.valueOf(oRecord.getPowerused())) ;
	            ec.setPrice(oRecord.getPrice()) ;
	            ec.setSavedCost(oRecord.getSavedCost()) ;
	            ec.setSavedPowerUsed(BigDecimal.valueOf(oRecord.getSavedPower()));
	            ec.setTenantsId(1l) ;
	            ec.setTotalFixtureContributed(0l) ;
	            ec.setTuneupSaving(BigDecimal.valueOf(oRecord.getTasktuneupsaving())) ;
	           
	           session.saveOrUpdate(ec) ;
	            }
	           return true ;
	        } catch (HibernateException hbe) {
	        	
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	            
	        }
		 
	}

}
