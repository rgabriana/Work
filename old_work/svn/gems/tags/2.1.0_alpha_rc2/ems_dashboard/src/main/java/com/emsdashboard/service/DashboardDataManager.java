package com.emsdashboard.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.DashboardDataDao;
import com.emsdashboard.model.Avgrecord;
import com.emsdashboard.model.DashboardRecord;
import com.emsdashboard.model.MeterRecord;
@Service("dashboardDataManager")
@Transactional(propagation = Propagation.REQUIRED)
public class DashboardDataManager {

    @Resource
    private DashboardDataDao dashboardDataDao;
    
    public List<MeterRecord> loadMeterDataWithDateRange(String property, String pid, java.util.Date oFDate,java.util.Date oTDate, String gemID) {
        List<MeterRecord> list = dashboardDataDao.loadMeterDataWithDateRange(property,pid,oFDate,oTDate,gemID);
        return list;
    }

    public List<Avgrecord> loadRecentEnergyConsumption(Long id, String propertyName, Date dateOn,String gemID) {
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
        List<Avgrecord> list = dashboardDataDao.loadRecentEnergyConsumption(id, propertyName, from, to,gemID);
        return list;
    }

    public List<Avgrecord> loadDayEnergyConsumption(Long id, String propertyName, Date oFDate, String gemID) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(oFDate);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        Date from = cal.getTime();
        // reset
        cal.setTime(oFDate);
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        Date to = cal.getTime();
        return dashboardDataDao.loadDayEnergyConsumption(id, propertyName, from, to,gemID);
    }

    public List<Avgrecord> loadWeekEnergyConsumption(Long pid, String property, Date oFDate, String gemID) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(oFDate);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        // cal.set(Calendar.DAY_OF_WEEK, 1);
        Date from = cal.getTime();
        // reset
        cal.setTime(oFDate);
        // Today min 6 for week.
        cal.add(Calendar.DATE, -6);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 00, 00, 00);
        Date to = cal.getTime();
        return dashboardDataDao.loadWeekEnergyConsumption(pid, property, from, to,gemID);
    }

    public List<Avgrecord> loadMonthEnergyConsumption(Long pid, String property, Date oFDate, String gemID) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(oFDate);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        // cal.set(Calendar.DAY_OF_MONTH, 1);
        Date from = cal.getTime();
        // reset
        cal.setTime(oFDate);
        cal.add(Calendar.MONTH, -1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        // cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date to = cal.getTime();
        return dashboardDataDao.loadMonthEnergyConsumption(pid, property, from, to,gemID);
    }

    public List<Avgrecord> loadYearEnergyConsumption(Long pid, String property, Date oFDate, String gemID) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(oFDate);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        // cal.set(Calendar.DAY_OF_YEAR, 1);
        Date from = cal.getTime();
        // reset
        cal.setTime(oFDate);
        // cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        // cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.YEAR, -1);
        Date to = cal.getTime();
        return dashboardDataDao.loadYearEnergyConsumption(pid, property, from, to,gemID);
    }

	

	public List<DashboardRecord> loadSummaryInPeriod(Long id, String propertyName, Date from, Date to) {
		   long diff = from.getTime() - to.getTime();
	        // Calculate difference in days
	        long diffDays = diff / (24 * 60 * 60 * 1000);
	        if (diffDays <= 1) {
	            return dashboardDataDao.loadDaySummary(id, propertyName, from, to);
	        } else if (diffDays <= 7) {
	            return dashboardDataDao.loadWeekSummary(id, propertyName, from, to);
	        } else if (diffDays <= 31) {
	            return dashboardDataDao.loadMonthSummary(id, propertyName, from, to);
	        }
	        return dashboardDataDao.loadYearSummary(id, propertyName, from, to);
	}

	public List<DashboardRecord> loadRecentSummary(Long id, String propertyName, Date dateOn) {
		 Calendar cal = Calendar.getInstance();
	        cal.setTime(dateOn);
	        Date from = cal.getTime();
	        // reset
	        cal.setTime(dateOn);
	        cal.add(Calendar.DATE, -1);
	        cal.set(Calendar.MINUTE, 00);
	        cal.set(Calendar.SECOND, 00);
	        Date to = cal.getTime();
	        return dashboardDataDao.loadRecentSummary(id, propertyName, from, to);
		
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

        return dashboardDataDao.loadDaySummary(id, columnName, from, to);
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
        return dashboardDataDao.loadWeekSummary(id, propertyName, from, to);
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
        return dashboardDataDao.loadMonthSummary(id, propertyName, from, to);
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
        return dashboardDataDao.loadYearSummary(id, columnName, from, to);
    }

	public Boolean saveOneHourDashBoardDetails(DashboardRecord oRecord, String ip) {
		return dashboardDataDao.saveOneHourDashBoardDetails(oRecord , ip) ;
		
	}

	

}
