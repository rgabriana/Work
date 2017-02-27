/**
 * 
 */
package com.emscloud.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.util.DateUtil;
import com.emscloud.model.OrganizationEnergyConsumption15min;
import com.emscloud.model.OrganizationEnergyConsumptionDaily;
import com.emscloud.model.OrganizationEnergyConsumptionHourly;
import com.emscloud.util.DatabaseUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
@Repository("organizationEnergyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class OrganizationEnergyConsumptionDao {

	@Resource
  SessionFactory sessionFactory;
	
	private static final Logger logger = Logger.getLogger("StatsAgg");

	/**
	 * 
	 */
	public OrganizationEnergyConsumptionDao() {
		// TODO Auto-generated constructor stub
	}
	
	private Connection getDbConnection(String dbName, String replicaServer, int port) {		
		
		String dbUser = "postgres";
		String dbPassword = "postgres";
		
		Connection connection = null;		
		if(dbName == null) {
			return null;
		}
		try {
			String conString = "jdbc:postgresql://" + replicaServer + ":" + port + "/" + dbName +  "?characterEncoding=utf-8";
			connection = DriverManager.getConnection(conString, dbUser, dbPassword);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return connection;
		
	} //end of method getDbConnection
	
	public long getCustomerOrganizationId(Long emId, Long organizationId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;		
		try {
			connection = getDbConnection("emscloud", "localhost", 5433);
			stmt = connection.createStatement();
			String query = "SELECT facility_id FROM facility_em_mapping WHERE em_id = " + emId +
					" AND em_facility_id = " + organizationId + " AND em_facility_type = 1"; 
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);			
			while(rs.next()) {
				return rs.getLong(1);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return -1;
		
	} //end of method getCustomerOrganizationId
	
	  public List loadPeakAndMinLoadQuery(String id, String from, String to) {
	        // we cannot take the peak load and min load from energy_consumption_daily table's power_used
	        // as power used in daily table is sum of energy for the entire day.

	        String sql = "select avg(ecl.load) as avgLoad, max(ecl.load) as peakLoad, min(ecl.load) as minLoad from "
	                + "(select sum(energy) as load from organization_energy_consumption_hourly "                
	                + " where level_id = " + id;
	        sql += " and capture_at <= '" + to + "'" + " and capture_at > '"
	                + from + "' group by capture_at) as ecl";
	        Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
	        return query.list();
	    }
	
	public List<OrganizationEnergyConsumptionHourly> getOrganizationHourlyEnergyData(long organizationId, 
			String fromDateString, String toDateString) {
		
		List<OrganizationEnergyConsumptionHourly> energyList = new ArrayList<OrganizationEnergyConsumptionHourly>();		
		
		try {
			String hsql = " Select eech from OrganizationEnergyConsumptionHourly eech where " +
					"eech.levelId = :levelId and eech.captureAt >= '" + fromDateString + "' and eech.captureAt < '" +
							toDateString + "' order by eech.captureAt";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", organizationId);
			energyList = q.list();
			System.out.println("no. of hourly rows -- " + energyList.size());
			if (energyList != null && !energyList.isEmpty()) {
				return energyList ;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;
		
	} //end of method getOrganizationHourlyEnergyData
	
	public List<OrganizationEnergyConsumptionDaily> getOrganizationDailyEnergyData(long organizationId, 
			String fromDateString, String toDateString) {
		
		List<OrganizationEnergyConsumptionDaily> energyList = new ArrayList<OrganizationEnergyConsumptionDaily>();		
		
		try {
			String hsql = " Select eecd from OrganizationEnergyConsumptionDaily eecd where " +
					"eecd.levelId = :levelId and eecd.captureAt > '" + fromDateString + "' and eecd.captureAt <= '" +
							toDateString + "' order by eecd.captureAt";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", organizationId);
			energyList = q.list();
			System.out.println("no. of daily rows -- " + energyList.size());
			if (energyList != null && !energyList.isEmpty()) {
				return energyList ;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;
		
	} //end of method getOrganizationDailyEnergyData
	
	   public List<OrganizationEnergyConsumptionDaily> getOrganizationMonthlyEnergyData(long organizationId, 
				String from, String to) {
			
			List<OrganizationEnergyConsumptionDaily> energyList = new ArrayList<OrganizationEnergyConsumptionDaily>();		
			
			try {
		           String hsql = "SELECT date_trunc('month', ec.capture_at  - interval '1 day') AS Month, "
		                    + "sum(ec.energy) as totalConsum, " + "sum(ec.base_energy) as baseTotalConsum, "
		                    + "sum(ec.saved_energy) as totalPowerSaved, " + "max(ec.price) as price, "
		                    + "sum(ec.cost) as cost, " + "sum(ec.saved_cost) as savedCost, "
		                    + "sum(ec.occ_savings) as totalOccSaved, " + "sum(ec.tuneup_savings) as totalTuneupSaving,"
		                    + "sum(ec.ambient_savings) as totalAmbientSaved," + "sum(ec.manual_savings) as totalManualSaving, "
		                    + "avg(ec.energy) as avgLoad, " + "max(ec.energy) as peakLoad, "
		                    + "min(ec.energy) as minLoad, "+ "sum(ec.base_cost) as baseCost " + "from organization_energy_consumption_daily"+ " ec where ";
		            hsql += "ec.level_id = " + organizationId + " and ";
		            hsql += " ec.capture_at >'" + from + "' "
		                    + "and ec.capture_at <='" + to 
		                    + "' " + " Group by Month Order by Month";

		            Query q = sessionFactory.getCurrentSession().createSQLQuery(hsql.toString());
		            List<Object[]> results = q.list();
//		            Object[] avg_min_max = loadPeakAndMinLoadQuery(String.valueOf(organizationId), from, to).toArray();
		            if (results != null && !results.isEmpty()) {
		                for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
		                    try {
		                        Object[] object = (Object[]) iterator.next();
		                        OrganizationEnergyConsumptionDaily oRecord = new OrganizationEnergyConsumptionDaily();
		                        oRecord.setCaptureAt(((Date) object[0]));
		                        if(object[1] != null) oRecord.setEnergy((BigDecimal)object[1]);
		                        if(object[2] != null) oRecord.setBaseEnergy(((BigDecimal) object[2]).doubleValue());
		                        if(object[3] != null) oRecord.setSavedEnergy((BigDecimal) object[3]);
		                        if(object[4] != null) oRecord.setPrice(((Double) object[4]).floatValue());
		                        if(object[5] != null) oRecord.setCost((Double) object[5]);
		                        if(object[6] != null) oRecord.setSavedCost((Double) object[6]);
		                        if(object[7] != null) oRecord.setOccSavings((BigDecimal) object[7]);
		                        if(object[8] != null) oRecord.setTuneupSavings((BigDecimal) object[8]);
		                        if(object[9] != null) oRecord.setAmbientSavings((BigDecimal) object[9]);
		                        if(object[10] != null) oRecord.setManualSavings((BigDecimal) object[10]);
		                        if(object[14] != null) oRecord.setBaseCost((Double) object[14]);
		                        energyList.add(oRecord);
		                    } catch(Exception e) {
//		                        logger.warn("Error processing request: {" + id + ", " + columnName + ", " + from + ", " + to + "} =>"+ e.getMessage());
		                    }
		                }
		            }
			}
			catch (HibernateException hbe) {
				throw SessionFactoryUtils.convertHibernateAccessException(hbe);
			}
			return energyList;
			
		} //end of method getOrganizationMonthlyEnergyData
	   
	   
	   
	   
	   
	   


	public List<OrganizationEnergyConsumption15min> getOrganization15minEnergyData(long organizationId, 
			String fromDateString, String toDateString) {
		
		List<OrganizationEnergyConsumption15min> energyList = new ArrayList<OrganizationEnergyConsumption15min>();		
		List<OrganizationEnergyConsumption15min> latestCurrentEnergyDataList = new ArrayList<OrganizationEnergyConsumption15min>();
		
		try {
			String hsql = " Select eecd from OrganizationEnergyConsumption15min eecd where " +
					"eecd.levelId = :levelId and eecd.captureAt >= '" + fromDateString + "' and eecd.captureAt < '" +
							toDateString + "' order by eecd.captureAt desc";
		//	System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", organizationId);
			energyList = q.list();
			//System.out.println("no. of 15min rows -- " + energyList.size());
			if (energyList != null && !energyList.isEmpty()) {
				if(energyList.size() > 1){
					latestCurrentEnergyDataList.add(energyList.get(0)); //EDAC-356 if there are two consecutive 15 min records in last half hour, send the latest 15 min data
					return latestCurrentEnergyDataList;
				}
				return energyList ;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;
		
	} //end of method getOrganization15minEnergyData
	
	public ArrayList<Object[]> getEmOrganizationDailyEnergyReadings(String dbName, String replicaServer, 
			Date fromDate, Date toDate, long orgId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
    String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> organizationEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT capture_at + '-1 day', SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption_daily ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id GROUP BY capture_at;";
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[19];				
				row[0] = orgId; 
				row[1] = rs.getTimestamp(1);
				row[2] = rs.getBigDecimal(2).doubleValue();
				row[3] = rs.getBigDecimal(3).doubleValue();
				row[4] = rs.getBigDecimal(4).doubleValue();
				row[5] = rs.getBigDecimal(5).doubleValue();
				row[6] = rs.getBigDecimal(6).doubleValue();
				row[7] = rs.getBigDecimal(7).doubleValue();
				row[8] = rs.getBigDecimal(8).doubleValue();
				row[9] = rs.getDouble(9);				
				row[10] = rs.getDouble(10);
				row[11] = rs.getDouble(11);
				row[12] = rs.getFloat(12);
				row[13] = rs.getDouble(13);
				row[14] = rs.getFloat(14);
				row[15] = rs.getFloat(15);
				row[16] = rs.getDouble(16);
				row[17] = rs.getFloat(17);				
				row[18] = rs.getLong(18);
				organizationEnergyData.add(row);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return organizationEnergyData;
		
	} //end of method getEmOrganizationDailyEnergyReadings
	
	public ArrayList<Object[]> getEmOrganizationHourlyEnergyReadings(String dbName, String replicaServer, 
			Date fromDate, Date toDate, long orgId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
    String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> organizationEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT capture_at + '-1 hour', SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption_hourly ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id GROUP BY capture_at;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[19];				
				row[0] = orgId; 
				row[1] = rs.getTimestamp(1);
				row[2] = rs.getBigDecimal(2).doubleValue();
				row[3] = rs.getBigDecimal(3).doubleValue();
				row[4] = rs.getBigDecimal(4).doubleValue();
				row[5] = rs.getBigDecimal(5).doubleValue();
				row[6] = rs.getBigDecimal(6).doubleValue();
				row[7] = rs.getBigDecimal(7).doubleValue();
				row[8] = rs.getBigDecimal(8).doubleValue();
				row[9] = rs.getDouble(9);				
				row[10] = rs.getDouble(10);
				row[11] = rs.getDouble(11);
				row[12] = rs.getFloat(12);
				row[13] = rs.getDouble(13);
				row[14] = rs.getFloat(14);
				row[15] = rs.getFloat(15);
				row[16] = rs.getDouble(16);
				row[17] = rs.getFloat(17);				
				row[18] = rs.getLong(18);
				organizationEnergyData.add(row);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return organizationEnergyData;
		
	} //end of method getEmFloorHourlyEnergyReadings
	
	public ArrayList<Object[]> getEmOrg5minEnergyReadings(String dbName, String replicaIp, String fromDateStr, 
			String toDateStr, Long orgId) {
			
		long startTime = System.currentTimeMillis();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}		
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaIp, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT capture_at + '-5 min', ROUND(SUM(base_power_used)/12, 2), ROUND(SUM(power_used)/12, 2), " +
					"ROUND(SUM(occ_saving)/12, 2), ROUND(SUM(ambient_saving)/12, 2), ROUND(SUM(tuneup_saving)/12, 2), " +
					"ROUND(SUM(manual_saving)/12, 2), ROUND(SUM(saved_power_used)/12, 2), ROUND(CAST(SUM(base_cost) AS NUMERIC), 2), " +
					"ROUND(CAST(SUM(saved_cost) AS NUMERIC), 2), ROUND(CAST(SUM(cost) AS NUMERIC), 2), min(min_temperature), " +
					"SUM(avg_temperature), MAX(max_temperature), MIN(light_min_level), SUM(light_avg_level), MAX(light_max_level), " +
					"COUNT(*), SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', ''))) " +
					"FROM energy_consumption ec, device d WHERE capture_at> '" + fromDateStr + "' AND capture_at<= '" + 
					toDateStr + "' AND d.id = ec.fixture_id GROUP BY capture_at  ORDER BY capture_at desc";
				
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[20];
				row[0] = orgId;
				row[1] = rs.getTimestamp(1);
				row[2] = rs.getBigDecimal(2).doubleValue();
				row[3] = rs.getBigDecimal(3).doubleValue();
				row[4] = rs.getBigDecimal(4).doubleValue();
				row[5] = rs.getBigDecimal(5).doubleValue();
				row[6] = rs.getBigDecimal(6).doubleValue();
				row[7] = rs.getBigDecimal(7).doubleValue();
				row[8] = rs.getBigDecimal(8).doubleValue();
				row[9] = rs.getDouble(9);
				row[10] = rs.getDouble(10);
				row[11] = rs.getDouble(11);
				row[12] = rs.getFloat(12);
				row[13] = rs.getDouble(13);
				row[14] = rs.getFloat(14);
				row[15] = rs.getFloat(15);
				row[16] = rs.getDouble(16);
				row[17] = rs.getFloat(17);
				row[18] = rs.getLong(18);
				row[19] = rs.getLong(19);
				flEnergyData.add(row);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("getEmOrg5minEnergyReadings: " + (System.currentTimeMillis() - startTime));
		}
		return flEnergyData;
			
	} //end of method getEmOrg5minEnergyReadings
	
	public ArrayList<Object[]> getEmOrganization15minEnergyReadings(String dbName, String replicaServer, 
			Date fromDate, Date toDate, long orgId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
    String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*),SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', ''))) " +
					"from energy_consumption ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id AND zero_bucket != 1";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[20];
				row[0] = orgId;	
				row[1] = fromDate;
				row[2] = rs.getBigDecimal(1).doubleValue();
				row[3] = rs.getBigDecimal(2).doubleValue();
				row[4] = rs.getBigDecimal(3).doubleValue();
				row[5] = rs.getBigDecimal(4).doubleValue();
				row[6] = rs.getBigDecimal(5).doubleValue();
				row[7] = rs.getBigDecimal(6).doubleValue();
				row[8] = rs.getBigDecimal(7).doubleValue();
				row[9] = rs.getDouble(8);
				row[10] = rs.getDouble(9);
				row[11] = rs.getDouble(10);
				row[12] = rs.getFloat(11);
				row[13] = rs.getDouble(12);
				row[14] = rs.getFloat(13);
				row[15] = rs.getFloat(14);
				row[16] = rs.getDouble(15);
				row[17] = rs.getFloat(16);
				row[18] = rs.getLong(17);
				row[19] = rs.getLong(18);
				flEnergyData.add(row);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return flEnergyData;
		
	} //end of method getEmFloor15minEnergyReadings
	
	public void saveOrUpdate(OrganizationEnergyConsumptionDaily ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(OrganizationEnergyConsumptionHourly ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(OrganizationEnergyConsumption15min ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
} //end of class OrganizationEnergyConsumptionDao
