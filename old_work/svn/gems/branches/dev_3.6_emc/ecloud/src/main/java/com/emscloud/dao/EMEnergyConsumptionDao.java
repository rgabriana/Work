/**
 * 
 */
package com.emscloud.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.util.DatabaseUtil;
import com.emscloud.vo.AggregatedEmData;
import com.emscloud.vo.RawEnergyData;

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
	}
	
	public Date getMaxCaptureAtForEM(String dbName, String replicaIp) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if(dbName == null || dbName.isEmpty()) {
				return null;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			String query = "select max(capture_at) from energy_consumption";			
			rs = stmt.executeQuery(query);
			if(rs != null && rs.next()) {
				Timestamp ts = rs.getTimestamp(1);
				if(ts != null) {
					return new Date(ts.getTime());
				}
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return null;
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
			String query = "SELECT (hour_time + '1 hour'), SUM(base_power_used)/12, SUM(power_used)/12, " +
					"SUM(occ_saving)/12, SUM(ambient_saving)/12, SUM(tuneup_saving + manual_saving)/12, " + 
					"AVG(avg_temperature), SUM(saved_power_used)/12, SUM(motion) * 100/COUNT(*) from (SELECT " +
					"date_trunc('hour', capture_at + '-5 min') AS hour_time, base_power_used, power_used, " +
					"occ_saving, ambient_saving, tuneup_saving, manual_saving, saved_power_used, avg_temperature, " +
					"(CASE WHEN (motion_bits > 0) THEN 1 ELSE 0 END) AS motion FROM energy_consumption WHERE " +
					"capture_At > '" + fromDateString + "' AND capture_at <= '" + toDateString + "' AND " + 
					"zero_bucket != 1) AS sq GROUP BY hour_time;";	
			
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
				data.setOccPercentage(rs.getInt(9));
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
			String query = "SELECT capture_at, SUM(base_power_used), SUM(power_used), " +
					"SUM(occ_saving), SUM(ambient_saving), SUM(tuneup_saving + manual_saving), " + 
					"AVG(avg_temperature), SUM(saved_power_used), SUM(motion) * 100/COUNT(*) FROM (SELECT " +
					"capture_at, base_power_used, power_used, occ_saving, ambient_saving, tuneup_saving, " +
					"manual_saving, saved_power_used, avg_temperature, (CASE WHEN (motion_bits > 0) THEN 1 ELSE " +
					"0 END) AS motion from energy_consumption WHERE capture_At > '" + fromDateString + 
					"' AND capture_at < '" + toDateString + "' AND zero_bucket != 1) as sq GROUP by capture_at";
			
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
				data.setOccPercentage(rs.getInt(9));
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
	
	//this is for details of each sensor without aggregating
	public List<RawEnergyData> getEmEnergyRawEnergyReadings(String dbName, String replicaServer, String fromDate, 
			String toDate) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		ArrayList<RawEnergyData> emSensorList = new ArrayList<RawEnergyData>();
		try {
			connection = getDbConnection(dbName, replicaServer,
					DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = null;
			query = " SELECT name, capture_at + '-5 min' AS ecTime, round(base_power_used, 2) AS baseEnergy, " + 
					"round(power_used, 2) AS energy, cost, round(saved_power_used, 2) AS savedEnergy, " +
					"saved_cost, round(occ_saving, 2) AS occSavings, round(ambient_saving, 2) AS " + 
					"ambSavings, round(tuneup_saving + manual_saving, 2) AS ttSavings, motion_bits from " +
					"energy_consumption ec, device d WHERE capture_at> '" + fromDate + "' AND capture_at<= '" + toDate + 
					"' AND d.id = ec.fixture_id AND zero_bucket != 1";
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				RawEnergyData data = new RawEnergyData();
				data.setName(rs.getString("name"));
				data.setTimestamp(rs.getTimestamp("ecTime"));
				data.setBaseEnergy(rs.getBigDecimal("baseEnergy").doubleValue());
				data.setEnergy(rs.getBigDecimal("energy"));
				data.setCost(rs.getDouble("cost"));
				data.setSavedEnergy(rs.getBigDecimal("savedEnergy"));
				data.setSavedCost(rs.getDouble("saved_cost"));
				data.setSavedOccEnergy(rs.getBigDecimal("occSavings"));
				data.setSavedAmbEnergy(rs.getBigDecimal("ambSavings"));
				data.setSavedTaskTunedEnergy(rs.getBigDecimal("ttSavings"));				
				data.setMotionBits(rs.getLong("motion_bits"));				
				data.setTimeSpan("5min");
				emSensorList.add(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}		
		return emSensorList;

	} // end of method getEmSensorRawEnergyReadings
		
} //end of class OrganizationEnergyConsumptionDao
