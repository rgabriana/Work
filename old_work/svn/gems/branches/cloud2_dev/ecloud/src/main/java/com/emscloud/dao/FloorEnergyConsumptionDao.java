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
import java.util.HashMap;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.util.DateUtil;
import com.emscloud.model.FloorEnergyConsumptionDaily;
import com.emscloud.model.FloorEnergyConsumptionHourly;
import com.emscloud.util.DatabaseUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
@Repository("floorEnergyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorEnergyConsumptionDao {

	@Resource
  SessionFactory sessionFactory;

	/**
	 * 
	 */
	public FloorEnergyConsumptionDao() {
		// TODO Auto-generated constructor stub
	}
	
	private Connection getDbConnection(String dbName, String replicaServer, int port) {
		
		
		String dbUser = "postgres";
		String dbPassword = "postgres";
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
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
	
	public long getCustomerFloorId(Long emId, Long floorId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;		
		try {
			connection = getDbConnection("emscloud", "localhost", 5433);
			stmt = connection.createStatement();
			String query = "SELECT floor_id FROM floor_hierarchy_mapping WHERE em_id = " + emId +
					" AND em_floor_id = " + floorId; 
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
		
	} //end of method getCustomerFloorId

	public ArrayList<Object[]> getFloorDailyEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
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
			String query = " SELECT floor_id, capture_at, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving) from energy_consumption_daily ec, " +
					"device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + toDateString + "' AND " +
					"d.id = ec.fixture_id GROUP BY floor_id, capture_at order by floor_id;";
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[8];
				row[0] = rs.getLong(1);
				row[1] = rs.getTimestamp(2);
				row[2] = rs.getBigDecimal(3).doubleValue();
				row[3] = rs.getBigDecimal(4).doubleValue();
				row[4] = rs.getBigDecimal(5).doubleValue();
				row[5] = rs.getBigDecimal(6).doubleValue();
				row[6] = rs.getBigDecimal(7).doubleValue();
				row[7] = rs.getBigDecimal(8).doubleValue();
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
		
	} //end of method getFloorDailyEnergyReadings
	
public ArrayList<Object[]> getFloorHourlyEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
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
			String query = " SELECT floor_id, capture_at, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving) from energy_consumption_hourly ec, " +
					"device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + toDateString + "' AND " +
					"d.id = ec.fixture_id GROUP BY floor_id, capture_at order by floor_id;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[8];
				row[0] = rs.getLong(1);
				row[1] = rs.getTimestamp(2);
				row[2] = rs.getBigDecimal(3).doubleValue();
				row[3] = rs.getBigDecimal(4).doubleValue();
				row[4] = rs.getBigDecimal(5).doubleValue();
				row[5] = rs.getBigDecimal(6).doubleValue();
				row[6] = rs.getBigDecimal(7).doubleValue();
				row[7] = rs.getBigDecimal(8).doubleValue();
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
		
	} //end of method getFloorHourlyEnergyReadings
	
	public void saveOrUpdate(FloorEnergyConsumptionDaily ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(FloorEnergyConsumptionHourly ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
} //end of class FloorEnergyConsumptionDao
