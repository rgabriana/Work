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

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.util.DateUtil;
import com.emscloud.util.DatabaseUtil;
import com.emscloud.vo.AggregatedEmData;

/**
 * @author sreedhar.kamishetti
 *
 */
@Repository("emEnergyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EMEnergyConsumptionDao {

	/**
	 * 
	 */
	public EMEnergyConsumptionDao() {
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
		
	public ArrayList<AggregatedEmData> getEmHourlyEnergyReadings(String dbName, String replicaServer, 
			String fromDateString, String toDateString, String siteName, String emName) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		ArrayList<AggregatedEmData> dataList = new ArrayList<AggregatedEmData>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = "SELECT capture_at, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving + manual_saving), SUM(avg_temperature), " +
					"SUM(saved_power_used) FROM energy_consumption_hourly ec WHERE capture_at> '" + fromDateString + 
					"' AND capture_at<= '" + toDateString + "' GROUP BY capture_at;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);			
			while(rs.next()) {
				AggregatedEmData data = new AggregatedEmData();		
				data.setEmName(emName);
				data.setSiteName(siteName);
				data.setDate((Date)(rs.getTimestamp(1)));
				data.setBaseEnergy(rs.getBigDecimal(2));
				data.setEnergy(rs.getBigDecimal(3));
				data.setSavedOccEnergy(rs.getBigDecimal(4));
				data.setSavedAmbEnergy(rs.getBigDecimal(5));
				data.setSavedTaskTunedEnergy(rs.getBigDecimal(6));				
				data.setAvgTemp((float)rs.getDouble(7));
				data.setSavedEnergy(rs.getBigDecimal(8));
				dataList.add(data);
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
		return dataList;
		
	} //end of method getEmHourlyEnergyReadings
	
	public ArrayList<AggregatedEmData> getEm5minEnergyReadings(String dbName, String replicaServer, 
			String fromDateString, String toDateString, String siteName, String emName) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}		
		ArrayList<AggregatedEmData> dataList = new ArrayList<AggregatedEmData>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = "SELECT capture_at, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving + manual_saving), SUM(avg_temperature), " +
					"SUM(saved_power_used) FROM energy_consumption ec WHERE capture_at> '" + fromDateString + 
					"' AND capture_at<= '" + toDateString + "' GROUP BY capture_at;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);			
			while(rs.next()) {
				AggregatedEmData data = new AggregatedEmData();		
				data.setEmName(emName);
				data.setSiteName(siteName);
				data.setDate((Date)(rs.getTimestamp(1)));
				data.setBaseEnergy(rs.getBigDecimal(2));
				data.setEnergy(rs.getBigDecimal(3));
				data.setSavedOccEnergy(rs.getBigDecimal(4));
				data.setSavedAmbEnergy(rs.getBigDecimal(5));
				data.setSavedTaskTunedEnergy(rs.getBigDecimal(6));				
				data.setAvgTemp((float)rs.getDouble(7));
				data.setSavedEnergy(rs.getBigDecimal(8));
				dataList.add(data);
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
		return dataList;
		
	} //end of method getEm5minEnergyReadings
		
} //end of class OrganizationEnergyConsumptionDao
