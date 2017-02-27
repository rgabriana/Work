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
import com.emscloud.model.FloorEnergyConsumption15min;
import com.emscloud.model.FloorEnergyConsumptionDaily;
import com.emscloud.model.FloorEnergyConsumptionHourly;
import com.emscloud.util.DatabaseUtil;
import com.emscloud.vo.SensorData;

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
	
	public List<Long> getEmIdsFromCustomerFloorId(Long custFloorId) {
		
		String hsql = "SELECT em_id FROM facility_em_mapping WHERE facility_id = " + custFloorId;
		List<Long> emIdList = new ArrayList<Long>();
		try {
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());		
			emIdList = q.list();
		}
		catch(HibernateException he) {
			throw SessionFactoryUtils.convertHibernateAccessException(he);
		}
		return emIdList;
		
	}
			
	public List<FloorEnergyConsumptionHourly> getFloorHourlyEnergyData(long floorId, String fromDateString, 
			String toDateString) {
		
		List<FloorEnergyConsumptionHourly> energyList = new ArrayList<FloorEnergyConsumptionHourly>();		
		
		try {
			String hsql = " Select fech from FloorEnergyConsumptionHourly fech where " +
					"fech.levelId = :levelId and fech.captureAt > '" + fromDateString + "' and fech.captureAt <= '" +
							toDateString + "' order by fech.captureAt desc";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", floorId);
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
		
	} //end of method getFloorHourlyEnergyData
	
	public List<FloorEnergyConsumption15min> getFloor15minEnergyData(long floorId, String fromDateString, 
			String toDateString) {
		
		List<FloorEnergyConsumption15min> energyList = new ArrayList<FloorEnergyConsumption15min>();		
		
		try {
			String hsql = " Select fech from FloorEnergyConsumption15min fech where " +
					"fech.levelId = :levelId and fech.captureAt > '" + fromDateString + "' and fech.captureAt <= '" +
							toDateString + "' order by fech.captureAt desc";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", floorId);
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
		
	} //end of method getFloor15minEnergyData
	
	public List<FloorEnergyConsumptionDaily> getFloorDailyEnergyData(long floorId, String fromDateString, 
			String toDateString) {
		
		List<FloorEnergyConsumptionDaily> energyList = new ArrayList<FloorEnergyConsumptionDaily>();		
		
		try {
			String hsql = " Select fecd from FloorEnergyConsumptionDaily fecd where " +
					"fecd.levelId = :levelId and fecd.captureAt > '" + fromDateString + "' and fecd.captureAt <= '" +
							toDateString + "' order by fecd.captureAt desc";
			System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", floorId);
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
		
	} //end of method getFloorDailyEnergyData
		
	public ArrayList<Object[]> getEmFloor5minEnergyReadings(String dbName, String replicaIp, String fromDateStr, 
		String toDateStr, Long floorId) {
		
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
					"ROUND(CAST(SUM(saved_cost) AS NUMERIC), 2), ROUND(CAST(SUM(cost) AS NUMERIC), 2), MIN(min_temperature), " +
					"SUM(avg_temperature), MAX(max_temperature), MIN(light_min_level), SUM(light_avg_level), MAX(light_max_level), " +
					"COUNT(*), SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', ''))) " +
					"FROM energy_consumption ec, device d WHERE capture_at> '" + fromDateStr + "' AND capture_at<= '" + 
					toDateStr + "' AND d.id = ec.fixture_id AND d.floor_id = " + floorId + " GROUP BY capture_at";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[20];
				row[0] = floorId;
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
		
	} //end of method getEmFloor5minEnergyReadings

	//this is for details of each sensor without aggregating
	public List<SensorData> getEmFloorSensorRawEnergyReadings(String dbName, String replicaServer, String fromDate, 
			String toDate, long floorId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}	
		ArrayList<SensorData> emFloorSensorList = new ArrayList<SensorData>();
		try {
			connection = getDbConnection(dbName, replicaServer, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT fixture_id, capture_at, round(base_power_used, 2), round(power_used, 2), round(occ_saving, 2), "
					+ "round(ambient_saving, 2), round(tuneup_saving + manual_saving, 2), avg_temperature, light_avg_level, motion_bits, avg_volts " +
					"from energy_consumption ec, device d WHERE capture_at> '" + fromDate + "' AND capture_at<= '" + 
					toDate + "' AND d.id = ec.fixture_id AND floor_id = " + floorId + " order by fixture_id, capture_at;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);			
			while(rs.next()) {
				SensorData data = new SensorData();
				data.setSensorId(rs.getLong(1));
				data.setTimestamp(rs.getTimestamp(2));
				data.setBaseEnergy(rs.getBigDecimal(3));
				data.setEnergy(rs.getBigDecimal(4));
				data.setSavedOccEnergy(rs.getBigDecimal(5));
				data.setSavedAmbEnergy(rs.getBigDecimal(6));
				data.setSavedTaskTunedEnergy(rs.getBigDecimal(7));
				data.setAvgTemp(rs.getFloat(8));
				data.setAvgAmb(rs.getInt(9));
				data.setMotionBits(rs.getLong(10));
				data.setAvgVolts(rs.getFloat(11));				
				data.setTimeSpan("5min");
				emFloorSensorList.add(data);
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
		return emFloorSensorList;
		
	} //end of method getEmFloorSensorRawEnergyReadings

	public ArrayList<Object[]> getEmFloorDailyEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
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
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption_daily ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id GROUP BY floor_id, capture_at order by floor_id;";
			
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
		
	} //end of method getEmFloorDailyEnergyReadings
	
	public ArrayList<Object[]> getEmFloorHourlyEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
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
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) from " +
					"energy_consumption_hourly ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id GROUP BY floor_id, capture_at order by floor_id;";
			
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
		
	} //end of method getEmFloorHourlyEnergyReadings
	
	public ArrayList<Object[]> getEmFloor15minEnergyReadings(String dbName, String replicaServer, Date fromDate, Date toDate) {
		
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
			String query = " SELECT floor_id, SUM(base_power_used), SUM(power_used), SUM(occ_saving), " +
					"SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), " +
					"SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), " +
					"min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) " +
					"from energy_consumption ec, device d WHERE capture_at> '" + fromDateString + "' AND capture_at<= '" + 
					toDateString + "' AND d.id = ec.fixture_id GROUP BY floor_id order by floor_id;";
			
			System.out.println("query -- " + query);
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while(rs.next()) {
				row = new Object[19];
				row[0] = rs.getLong(1);	
				row[1] = toDate;
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
	
	public void saveOrUpdate(FloorEnergyConsumptionDaily ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(FloorEnergyConsumptionHourly ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
	public void saveOrUpdate(FloorEnergyConsumption15min ec) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(ec) ;
		
	}
	
} //end of class FloorEnergyConsumptionDao
