/**
 * 
 */
package com.emscloud.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
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
import com.emscloud.model.CampusEnergyConsumption15min;
import com.emscloud.model.CampusEnergyConsumptionDaily;
import com.emscloud.model.CampusEnergyConsumptionHourly;
import com.emscloud.util.DatabaseUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
@Repository("campusEnergyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CampusEnergyConsumptionDao {

	@Resource
  SessionFactory sessionFactory;
	
	private static final Logger logger = Logger.getLogger("StatsAgg");

	/**
	 * 
	 */
	public CampusEnergyConsumptionDao() {
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
	
	public long getCustomerCampusId(Long emId, Long campusId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;		
		try {
			connection = getDbConnection("emscloud", "localhost", 5433);
			stmt = connection.createStatement();
			String query = "SELECT facility_id FROM facility_em_mapping WHERE em_id = " + emId +
					" AND em_facility_id = " + campusId + " AND em_facility_type = 2"; 
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
		
	} //end of method getCustomerCampusId
	
	public List<CampusEnergyConsumption15min> getCampus15minEnergyData(long campusId, String fromDateString, 
			String toDateString) {
		
		List<CampusEnergyConsumption15min> energyList = new ArrayList<CampusEnergyConsumption15min>();		
		
		try {
			String hsql = " Select cech from CampusEnergyConsumption15min cech where " +
					"cech.levelId = :levelId and cech.captureAt >= '" + fromDateString + "' and cech.captureAt < '" +
							toDateString + "'";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", campusId);
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
		
	} //end of method getCampus15minEnergyData
	
	public List<CampusEnergyConsumptionHourly> getCampusHourlyEnergyData(long campusId, String fromDateString, 
			String toDateString) {
		
		List<CampusEnergyConsumptionHourly> energyList = new ArrayList<CampusEnergyConsumptionHourly>();		
		
		try {
			String hsql = " Select cech from CampusEnergyConsumptionHourly cech where " +
					"cech.levelId = :levelId and cech.captureAt >= '" + fromDateString + "' and cech.captureAt < '" +
							toDateString + "'";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", campusId);
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
		
	} //end of method getCampusHourlyEnergyData
	
	public List<CampusEnergyConsumptionDaily> getCampusDailyEnergyData(long campusId, String fromDateString, 
			String toDateString) {
		
		List<CampusEnergyConsumptionDaily> energyList = new ArrayList<CampusEnergyConsumptionDaily>();		
		
		try {
			String hsql = " Select cecd from CampusEnergyConsumptionDaily cecd where " +
					"cecd.levelId = :levelId and cecd.captureAt >= '" + fromDateString + "' and cecd.captureAt < '" +
							toDateString + "'";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", campusId);
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
		
	} //end of method getCampusDailyEnergyData
	
	public ArrayList<Object[]> getEmCampus5minEnergyReadings(String dbName, String replicaIp, String fromDateStr, 
			String toDateStr, Long campusId) {
			
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
					"ROUND(CAST(SUM(saved_cost) AS NUMERIC), 2), ROUND(CAST(SUM(cost) AS NUMERIC), 2), MIN(min_temperature), " +
					"SUM(avg_temperature), MAX(max_temperature), MIN(light_min_level), SUM(light_avg_level), MAX(light_max_level), " +
					"COUNT(*), SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', ''))) " +
					"FROM energy_consumption ec, device d WHERE capture_at> '" + fromDateStr + "' AND capture_at<= '" + 
					toDateStr + "' AND d.id = ec.fixture_id AND d.campus_id = " + campusId + " GROUP BY capture_at";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[20];
				row[0] = campusId;
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
			logger.debug("getEmCampus5minEnergyReadings: " + (System.currentTimeMillis() - startTime));
		}
		return flEnergyData;
		
	} //end of method getEmCampus5minEnergyReadings

	public ArrayList<Object[]> getEmCampusDailyEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
    String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> campusEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT campus_id, capture_at + '-1 day', SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption_daily ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id GROUP BY campus_id, capture_at order by campus_id;";
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[19];
				row[0] = rs.getLong(1);
				row[1] = rs.getTimestamp(2);
				row[2] = rs.getBigDecimal(3).doubleValue();
				row[3] = rs.getBigDecimal(4).doubleValue();
				row[4] = rs.getBigDecimal(5).doubleValue();
				row[5] = rs.getBigDecimal(6).doubleValue();
				row[6] = rs.getBigDecimal(7).doubleValue();
				row[7] = rs.getBigDecimal(8).doubleValue();
				row[8] = rs.getBigDecimal(9).doubleValue();
				row[9] = rs.getDouble(10);
				row[10] = rs.getDouble(11);
				row[11] = rs.getDouble(12);
				row[12] = rs.getFloat(13);
				row[13] = rs.getDouble(14);
				row[14] = rs.getFloat(15);
				row[15] = rs.getFloat(16);
				row[16] = rs.getDouble(17);
				row[17] = rs.getFloat(18);
				row[18] = rs.getLong(19);
				campusEnergyData.add(row);
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
		return campusEnergyData;
		
	} //end of method getEmCampusDailyEnergyReadings
	
	public ArrayList<Object[]> getEmCampus15minEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
    String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> campusEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT campus_id, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) ,SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', '')))" +
					"from energy_consumption ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id AND zero_bucket != 1 GROUP BY campus_id order by campus_id;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[20];
				row[0] = rs.getLong(1);	
				row[1] = fromDate;
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
				campusEnergyData.add(row);
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
		return campusEnergyData;
		
	} //end of method getEmCampus15minEnergyReadings
	
	public ArrayList<Object[]> getEmCampusHourlyEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm");
    String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> campusEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT campus_id, capture_at + '-1 hour', SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption_hourly ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" +
					toDateString + "' AND d.id = ec.fixture_id GROUP BY campus_id, capture_at order by campus_id;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[19];
				row[0] = rs.getLong(1);
				row[1] = rs.getTimestamp(2);
				row[2] = rs.getBigDecimal(3).doubleValue();
				row[3] = rs.getBigDecimal(4).doubleValue();
				row[4] = rs.getBigDecimal(5).doubleValue();
				row[5] = rs.getBigDecimal(6).doubleValue();
				row[6] = rs.getBigDecimal(7).doubleValue();
				row[7] = rs.getBigDecimal(8).doubleValue();
				row[8] = rs.getBigDecimal(9).doubleValue();
				row[9] = rs.getDouble(10);
				row[10] = rs.getDouble(11);
				row[11] = rs.getDouble(12);
				row[12] = rs.getFloat(13);
				row[13] = rs.getDouble(14);
				row[14] = rs.getFloat(15);
				row[15] = rs.getFloat(16);
				row[16] = rs.getDouble(17);
				row[17] = rs.getFloat(18);
				row[18] = rs.getLong(19);
				campusEnergyData.add(row);
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
		return campusEnergyData;
		
	} //end of method getEmFloorHourlyEnergyReadings
	
	public void saveOrUpdate(CampusEnergyConsumptionDaily ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(CampusEnergyConsumptionHourly ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(CampusEnergyConsumption15min ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
} //end of class CampusEnergyConsumptionDao