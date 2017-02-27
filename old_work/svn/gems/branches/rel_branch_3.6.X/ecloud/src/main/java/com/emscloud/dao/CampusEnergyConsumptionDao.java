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
	
	  public List loadPeakAndMinLoadQuery(String id, String from, String to) {
	        // we cannot take the peak load and min load from energy_consumption_daily table's power_used
	        // as power used in daily table is sum of energy for the entire day.

	        String sql = "select avg(ecl.load) as avgLoad, max(ecl.load) as peakLoad, min(ecl.load) as minLoad from "
	                + "(select sum(energy) as load from campus_energy_consumption_hourly "                
	                + " where level_id = " + id;
	        sql += " and capture_at <= '" + to + "'" + " and capture_at > '"
	                + from  + "' group by capture_at) as ecl";
	        Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
	        return query.list();
	    }
	  
	public List<CampusEnergyConsumption15min> getCampus15minEnergyData(long campusId, String fromDateString, 
			String toDateString) {
		
		List<CampusEnergyConsumption15min> energyList = new ArrayList<CampusEnergyConsumption15min>();	
		List<CampusEnergyConsumption15min> latestCurrentEnergyDataList = new ArrayList<CampusEnergyConsumption15min>();
		
		try {
			String hsql = " Select cech from CampusEnergyConsumption15min cech where " +
					"cech.levelId = :levelId and cech.captureAt >= '" + fromDateString + "' and cech.captureAt < '" +
							toDateString + "' order by cech.captureAt desc";
			//System.out.println("query -- " + hsql);
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			//q.setLong("custId", custId);
			q.setLong("levelId", campusId);
			energyList = q.list();
			//System.out.println("no. of hourly rows -- " + energyList.size());
			if (energyList != null && !energyList.isEmpty()) {
				if(energyList.size() > 1){
					latestCurrentEnergyDataList.add(energyList.get(0));  //EDAC-356 if there are two consecutive 15 min records in last half hour, send the latest 15 min data
					return latestCurrentEnergyDataList;
				}
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
							toDateString + "' order by cech.captureAt";
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
					"cecd.levelId = :levelId and cecd.captureAt > '" + fromDateString + "' and cecd.captureAt <= '" +
							toDateString + "' order by cecd.captureAt";
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
	
	public List<CampusEnergyConsumptionDaily> getCampusMonthlyEnergyData(
			long campusId, String from, String to) {

		List<CampusEnergyConsumptionDaily> energyList = new ArrayList<CampusEnergyConsumptionDaily>();

		try {
			String hsql = "SELECT date_trunc('month', ec.capture_at  - interval '1 day') AS Month, "
					+ "sum(ec.energy) as totalConsum, "
					+ "sum(ec.base_energy) as baseTotalConsum, "
					+ "sum(ec.saved_energy) as totalPowerSaved, "
					+ "max(ec.price) as price, "
					+ "sum(ec.cost) as cost, "
					+ "sum(ec.saved_cost) as savedCost, "
					+ "sum(ec.occ_savings) as totalOccSaved, "
					+ "sum(ec.tuneup_savings) as totalTuneupSaving,"
					+ "sum(ec.ambient_savings) as totalAmbientSaved,"
					+ "sum(ec.manual_savings) as totalManualSaving, "
					+ "avg(ec.energy) as avgLoad, "
					+ "max(ec.energy) as peakLoad, "
					+ "min(ec.energy) as minLoad, "
					+ "sum(ec.base_cost) as baseCost "
					+ "from campus_energy_consumption_daily" + " ec where ";

			hsql += "ec.level_id = " + campusId + " and ";
			hsql += " ec.capture_at >'"
					+ from + "' "
					+ "and ec.capture_at <='"
					+ to + "' "
					+ " Group by Month Order by Month";

			Query q = sessionFactory.getCurrentSession().createSQLQuery(
					hsql.toString());
			List<Object[]> results = q.list();
			// Object[] avg_min_max =
			// loadPeakAndMinLoadQuery(String.valueOf(organizationId), from,
			// to).toArray();
			if (results != null && !results.isEmpty()) {
				for (Iterator<Object[]> iterator = results.iterator(); iterator
						.hasNext();) {
					try {
						Object[] object = (Object[]) iterator.next();
						CampusEnergyConsumptionDaily oRecord = new CampusEnergyConsumptionDaily();
						oRecord.setCaptureAt(((Date) object[0]));
						if (object[1] != null)
							oRecord.setEnergy((BigDecimal) object[1]);
						if (object[2] != null)
							oRecord.setBaseEnergy(((BigDecimal) object[2])
									.doubleValue());
						if (object[3] != null)
							oRecord.setSavedEnergy((BigDecimal) object[3]);
						if (object[4] != null)
							oRecord.setPrice(((Double) object[4]).floatValue());
						if (object[5] != null)
							oRecord.setCost((Double) object[5]);
						if (object[6] != null)
							oRecord.setSavedCost((Double) object[6]);
						if (object[7] != null)
							oRecord.setOccSavings((BigDecimal) object[7]);
						if (object[8] != null)
							oRecord.setTuneupSavings((BigDecimal) object[8]);
						if (object[9] != null)
							oRecord.setAmbientSavings((BigDecimal) object[9]);
						if (object[10] != null)
							oRecord.setManualSavings((BigDecimal) object[10]);
						if (object[14] != null)
							oRecord.setBaseCost((Double) object[14]);
						energyList.add(oRecord);
					} catch (Exception e) {
						// logger.warn("Error processing request: {" + id + ", "
						// + columnName + ", " + from + ", " + to + "} =>"+
						// e.getMessage());
					}
				}
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;
	}
	  
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
