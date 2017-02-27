package com.emsdashboard.dao;

import java.math.BigDecimal;
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
import org.hibernate.classic.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.model.DashboardRecord;
import com.emsdashboard.model.EnergyConsumption;
import com.emsdashboard.model.GemsServer;
import com.emsdashboard.service.GemsManager;
import com.emsdashboard.utils.DateUtil;
import com.emsdashboard.ws.util.WebServiceUtils;
@Repository("dashboardDataDao")
@Transactional(propagation = Propagation.REQUIRED)

public class DashboardDataDao  extends BaseDaoHibernate {
    
    static final Logger logger = Logger.getLogger(DashboardDataDao.class.getName());
    
    @Resource
    SessionFactory sessionFactory;
    @Resource(name = "gemsManager")
    private GemsManager gemsManager;
    
  
 
   
 
    
   
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
        String offset = WebServiceUtils.getServerTimeZoneOffsetFromGMT() ;
        String tableName = "energy_consumption";
        //tableName = selectTable(from, to);

        try {
            String hsql = "SELECT date_trunc('hour', ec.capture_at + interval '"+ offset +"' )  AS Hour, " + "sum(ec.power_used) as totalConsum, "
                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
                    + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+"from " + tableName + " ec" ;
    	            if (columnName.equals("gems_id")&&id>0) {
    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
    	            }
    	            else
    	            {
    	            	 hsql +=" where " ;
    	            }
            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
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
                    oRecord.setBaseCost(((Double) object[14]).floatValue());
                    oRecords.add(oRecord);
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oRecords;
    }

	@SuppressWarnings("unchecked")
	public List<DashboardRecord> loadRecentSummary(Long id, String columnName, Date from, Date to) {
		  List<DashboardRecord> oRecords = new ArrayList<DashboardRecord>();
		  String offset = WebServiceUtils.getServerTimeZoneOffsetFromGMT() ;
	        String tableName = "energy_consumption";

	        try {
	            String hsql = "SELECT now() , " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+"from " + tableName + " ec" ;
	            String hsql1 = "SELECT sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum " + "from " + tableName + " ec " 
	            		+ "where ec.capture_at = (select max(ec1.capture_at) from energy_consumption ec1) " ;
	            if (columnName.equals("gems_id")&&id>0) {
	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	                hsql1 += " and ec."+columnName + "=" + id +" and " +
	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) " ;
	            }
	            else
	            {
	            	 hsql +=" where " ;
	            }
	            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
	                    + "and ec.capture_at >'" + DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss")
	                    + "' and ec.base_power_used != 0 ";
	            Session session = sessionFactory.getCurrentSession();
	            Query q = session.createSQLQuery(hsql.toString());
	            Query q1 = session.createSQLQuery(hsql1.toString());
	            List<Object[]> results = q.list();
	            List<Object[]> results1 = q1.list();
	            if (results != null && !results.isEmpty()) {
	                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
	                    Object[] object = (Object[]) iterator.next();
	                    if (object[1] != null) {
	                        DashboardRecord oRecord = new DashboardRecord();
	                        oRecord.setCaptureOn(((Date) object[0]));
	                        if(results1 != null && !results1.isEmpty()) {
	                        	Object[] object1 = (Object[])results1.get(0);
	                        	oRecord.setPowerused(((BigDecimal) object1[0]).doubleValue());
	                        	oRecord.setBasePowerUsed(((BigDecimal) object1[1]).doubleValue());
	                        }
	                        else {
		                        oRecord.setPowerused(0D);
		                        oRecord.setBasePowerUsed(0D);
	                        }
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
		  String offset = WebServiceUtils.getServerTimeZoneOffsetFromGMT() ;
	        String tableName = "energy_consumption";
	        //tableName = selectTable(from, to);

	        try {
	            String hsql = "SELECT date_trunc('day', capture_at  )  AS Day, " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+"from " + tableName + " ec" ;
	    	            if (columnName.equals("gems_id")&&id>0) {
	    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	    	            }
	    	            else
	    	            {
	    	            	 hsql +=" where " ;
	    	            }
	    	            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
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
	                    oRecord.setBaseCost(((Double) object[14]).floatValue());
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
		 String offset = WebServiceUtils.getServerTimeZoneOffsetFromGMT() ;
	        String tableName = "energy_consumption";
	        //tableName = selectTable(from, to);

	        try {
	            String hsql = "SELECT date_trunc('day', capture_at  ) AS Day, " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad, " + "sum(ec.base_cost) as baseCost "+"from " + tableName + " ec" ;
	    	            if (columnName.equals("gems_id")&&id>0) {
	    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	    	            }
	    	            else
	    	            {
	    	            	 hsql +=" where " ;
	    	            }
	    	            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
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
	                    oRecord.setBaseCost(((Double) object[14]).floatValue());
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
		 String offset = WebServiceUtils.getServerTimeZoneOffsetFromGMT() ;
	        String tableName = "energy_consumption";
	        //tableName = selectTable(from, to);

	        try {
	            String hsql = "SELECT date_trunc('month', capture_at ) AS Month, " + "sum(ec.power_used) as totalConsum, "
	                    + "sum(ec.base_power_used) as baseTotalConsum, " + "sum(ec.saved_power_used) as totalPowerSaved, "
	                    + "max(ec.price) as price, " + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
	                    + "sum(ec.occ_saving) as totalOccSaved, " + "sum(ec.tuneup_saving) as totalTuneupSaving,"
	                    + "sum(ec.ambient_saving) as totalAmbientSaved," + "sum(ec.manual_saving) as totalManualSaving, "
	                    + "avg(ec.power_used) as avgLoad, " + "max(ec.power_used) as peakLoad, "
	                    + "min(ec.power_used) as minLoad, " +"sum(ec.base_cost) as baseCost "+ "from " + tableName + " ec" ;
	    	            if (columnName.equals("gems_id")&&id>0) {
	    	                hsql +=   "  where ec."+columnName + "=" + id +" and " +
	    	                		"ec.gems_id in (select id from gems" + " where status = 'A' ) and";
	    	            }
	    	            else
	    	            {
	    	            	 hsql +=" where " ;
	    	            }
	    	            hsql += " ec.capture_at <='" + DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss") + "' "
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
	                    oRecord.setBaseCost(((Double) object[14]).floatValue());
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
	            ec.setBaseCost(oRecord.getBaseCost()) ;
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
	public Date loadLastRecordDate(String gemIp)
	{
		GemsServer serverData = gemsManager.loadGEMSByGemsIp(gemIp);
		Long Id =0l;
		Date date = null ;
		List results = null;
        String hsql=null;
        try{
		if(serverData!=null)
		{
			 Id = serverData.getId() ;
		   hsql = "SELECT ec.capture_at , id "
                   + "FROM energy_consumption as ec " + "where  ec.gems_id = :gemID " 
                   + "and ec.capture_at = (select Max(capture_at) from energy_consumption where gems_id=:gemID)";
       
       Session session = sessionFactory.getCurrentSession();
       Query q = session.createSQLQuery(hsql.toString());
       long GEMSID = Id;
       q.setParameter("gemID", GEMSID);
       results = q.list();
       if (results != null && !results.isEmpty()) {
           for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
               Object[] object = (Object[]) iterator.next();
               		date = (Date) object[0] ;
           }
	}
		}
        }catch(Exception ex)
        {
        	
        	ex.printStackTrace();
        }
		return date ;
	}

}
