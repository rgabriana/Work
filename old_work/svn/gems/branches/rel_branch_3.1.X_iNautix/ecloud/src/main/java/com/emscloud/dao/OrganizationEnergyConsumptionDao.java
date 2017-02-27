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
	
	public List<OrganizationEnergyConsumptionHourly> getOrganizationHourlyEnergyData(long organizationId, 
			String fromDateString, String toDateString) {
		
		List<OrganizationEnergyConsumptionHourly> energyList = new ArrayList<OrganizationEnergyConsumptionHourly>();		
		
		try {
			String hsql = " Select eech from OrganizationEnergyConsumptionHourly eech where " +
					"eech.levelId = :levelId and eech.captureAt > '" + fromDateString + "' and eech.captureAt <= '" +
							toDateString + "' order by eech.captureAt desc";
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
							toDateString + "' order by eecd.captureAt desc";
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

	public List<OrganizationEnergyConsumption15min> getOrganization15minEnergyData(long organizationId, 
			String fromDateString, String toDateString) {
		
		List<OrganizationEnergyConsumption15min> energyList = new ArrayList<OrganizationEnergyConsumption15min>();		
		
		try {
			String hsql = " Select eecd from OrganizationEnergyConsumption15min eecd where " +
					"eecd.levelId = :levelId and eecd.captureAt > '" + fromDateString + "' and eecd.captureAt <= '" +
							toDateString + "' order by eecd.captureAt desc";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", organizationId);
			energyList = q.list();
			System.out.println("no. of 15min rows -- " + energyList.size());
			if (energyList != null && !energyList.isEmpty()) {
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
			String query = " SELECT capture_at, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
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
			String query = " SELECT capture_at, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
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
				String query = " SELECT capture_at, ROUND(SUM(base_power_used)/12, 2), ROUND(SUM(power_used)/12, 2), " +
						"ROUND(SUM(occ_saving)/12, 2), ROUND(SUM(ambient_saving)/12, 2), ROUND(SUM(tuneup_saving)/12, 2), " +
						"ROUND(SUM(manual_saving)/12, 2), ROUND(SUM(saved_power_used)/12, 2), ROUND(CAST(SUM(base_cost) AS NUMERIC), 2), " +
						"ROUND(CAST(SUM(saved_cost) AS NUMERIC), 2), ROUND(CAST(SUM(cost) AS NUMERIC), 2), min(min_temperature), " +
						"SUM(avg_temperature), MAX(max_temperature), MIN(light_min_level), SUM(light_avg_level), MAX(light_max_level), " +
						"COUNT(*), SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', ''))) " +
						"FROM energy_consumption ec, device d WHERE capture_at> '" + fromDateStr + "' AND capture_at<= '" + 
						toDateStr + "' AND d.id = ec.fixture_id GROUP BY capture_at";
				
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
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[19];
				row[0] = orgId;	
				row[1] = toDate;
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
