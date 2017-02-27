/**
 * 
 */
package com.emscloud.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.vos.EcSyncVo;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmLastEcSynctime;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FloorEnergyConsumption15min;
import com.emscloud.model.FloorEnergyConsumptionDaily;
import com.emscloud.model.FloorEnergyConsumptionHourly;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmLastEcSynctimeManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.types.FacilityType;
import com.emscloud.util.DatabaseUtil;
import com.emscloud.util.DateUtil;
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
	@Resource
	EmInstanceManager emInstanceManager;
	@Resource
	EmLastEcSynctimeManager emLastEcSynctimeManager;
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;

	private static final Logger logger = Logger.getLogger("StatsAgg");

	/**
	 * 
	 */
	public FloorEnergyConsumptionDao() {
		// TODO Auto-generated constructor stub
	}

	private Connection getDbConnection(String dbName, String replicaServer,
			int port) {

		String dbUser = "postgres";
		String dbPassword = "postgres";

		Connection connection = null;
		if (dbName == null) {
			return null;
		}
		try {
			String conString = "jdbc:postgresql://" + replicaServer + ":"
					+ port + "/" + dbName + "?characterEncoding=utf-8";
			connection = DriverManager.getConnection(conString, dbUser,
					dbPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;

	} // end of method getDbConnection

	public List<Long> getEmIdsFromCustomerFloorId(Long custFloorId) {

		String hsql = "SELECT em_id FROM facility_em_mapping WHERE facility_id = "
				+ custFloorId;
		List<Long> emIdList = new ArrayList<Long>();
		try {
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			emIdList = q.list();
		} catch (HibernateException he) {
			throw SessionFactoryUtils.convertHibernateAccessException(he);
		}
		return emIdList;

	}
	
	public List loadPeakAndMinLoadQuery(String id, String from, String to) {
		// we cannot take the peak load and min load from
		// energy_consumption_daily table's power_used
		// as power used in daily table is sum of energy for the entire day.

		String sql = "select avg(ecl.load) as avgLoad, max(ecl.load) as peakLoad, min(ecl.load) as minLoad from "
				+ "(select sum(energy) as load from floor_energy_consumption_hourly "
				+ " where level_id = " + id;
		sql += " and capture_at <= '"
				+ to + "'"
				+ " and capture_at > '"
				+ from 
				+ "' group by capture_at) as ecl";
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		return query.list();
	}

	public List<FloorEnergyConsumptionHourly> getFloorHourlyEnergyData(
			long floorId, String fromDateString, String toDateString) {

		List<FloorEnergyConsumptionHourly> energyList = new ArrayList<FloorEnergyConsumptionHourly>();

		try {
			String hsql = " Select fech from FloorEnergyConsumptionHourly fech where "
					+ "fech.levelId = :levelId and fech.captureAt >= '"
					+ fromDateString
					+ "' and fech.captureAt < '"
					+ toDateString + "' order by fech.captureAt";
			if(logger.isDebugEnabled()) {
				logger.debug("getFloorHourlyEnergyData query -- " + hsql);
			}
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			// q.setLong("custId", custId);
			q.setLong("levelId", floorId);
			energyList = q.list();
			if(logger.isDebugEnabled()) {
				logger.debug("no. of hourly rows -- " + energyList.size());
			}
			if (energyList != null && !energyList.isEmpty()) {
				return energyList;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;

	} // end of method getFloorHourlyEnergyData

	public List<FloorEnergyConsumption15min> getFloor15minEnergyData(
			long floorId, String fromDateString, String toDateString) {

		List<FloorEnergyConsumption15min> energyList = new ArrayList<FloorEnergyConsumption15min>();
		List<FloorEnergyConsumption15min> latestCurrentEnergyDataList = new ArrayList<FloorEnergyConsumption15min>();

		try {
			String hsql = " Select fech from FloorEnergyConsumption15min fech where "
					+ "fech.levelId = :levelId and fech.captureAt >= '"
					+ fromDateString
					+ "' and fech.captureAt < '"
					+ toDateString + "' order by fech.captureAt desc";
			if(logger.isDebugEnabled()) {
				logger.debug("getFloor15minEnergyData query -- " + hsql);
			}
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			// q.setLong("custId", custId);
			q.setLong("levelId", floorId);
			energyList = q.list();
			if(logger.isDebugEnabled()) {
				logger.debug("no. of 15min rows -- " + energyList.size());
			}
			if (energyList != null && !energyList.isEmpty()) {
				if(energyList.size() > 1){
					latestCurrentEnergyDataList.add(energyList.get(0));  //EDAC-356 if there are two consecutive 15 min records in last half hour, send the latest 15 min data
					return latestCurrentEnergyDataList;
				}
				return energyList;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;

	} // end of method getFloor15minEnergyData

	public List<FloorEnergyConsumptionDaily> getFloorDailyEnergyData(
			long floorId, String fromDateString, String toDateString) {

		List<FloorEnergyConsumptionDaily> energyList = new ArrayList<FloorEnergyConsumptionDaily>();

		try {
			String hsql = " Select fecd from FloorEnergyConsumptionDaily fecd where "
					+ "fecd.levelId = :levelId and fecd.captureAt > '"
					+ fromDateString
					+ "' and fecd.captureAt <= '"
					+ toDateString + "' order by fecd.captureAt";
			if(logger.isDebugEnabled()) {
				logger.debug("getFloorDailyEnergyData query -- " + hsql);
			}
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			// q.setLong("custId", custId);
			q.setLong("levelId", floorId);
			energyList = q.list();
			if(logger.isDebugEnabled()) {
				logger.debug("no. of daily rows -- " + energyList.size());
			}
			if (energyList != null && !energyList.isEmpty()) {
				return energyList;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return energyList;

	} // end of method getFloorDailyEnergyData
	
	public List<FloorEnergyConsumptionDaily> getFloorMonthlyEnergyData(
			long floorId, String from, String to) {

		List<FloorEnergyConsumptionDaily> energyList = new ArrayList<FloorEnergyConsumptionDaily>();

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
					+ "from floor_energy_consumption_daily" + " ec where ";
			hsql += "ec.level_id = " + floorId + " and ";
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
						FloorEnergyConsumptionDaily oRecord = new FloorEnergyConsumptionDaily();
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
	
	
	
	public List<FloorEnergyConsumptionDaily> getFloorWeeklyEnergyData(
			long floorId, String from, String to) {

		List<FloorEnergyConsumptionDaily> energyList = new ArrayList<FloorEnergyConsumptionDaily>();

		try {
			String hsql = "SELECT date_trunc('week', ec.capture_at  - interval '1 day') AS Week, "
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
					+ "from floor_energy_consumption_daily" + " ec where ";
			hsql += "ec.level_id = " + floorId + " and ";
			hsql += " ec.capture_at >'"
					+ from + "' "
					+ "and ec.capture_at <='"
					+ to + "' "
					+ " Group by Week Order by Week";

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
						FloorEnergyConsumptionDaily oRecord = new FloorEnergyConsumptionDaily();
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

	public ArrayList<Object[]> getEmFloor5minEnergyReadings(String dbName,
			String replicaIp, String fromDateStr, String toDateStr, Long floorId) {

		long startTime = System.currentTimeMillis();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaIp, DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT capture_at + '-5 min', ROUND(SUM(base_power_used)/12, 2), ROUND(SUM(power_used)/12, 2), "
					+ "ROUND(SUM(occ_saving)/12, 2), ROUND(SUM(ambient_saving)/12, 2), ROUND(SUM(tuneup_saving)/12, 2), "
					+ "ROUND(SUM(manual_saving)/12, 2), ROUND(SUM(saved_power_used)/12, 2), ROUND(CAST(SUM(base_cost) AS NUMERIC), 2), "
					+ "ROUND(CAST(SUM(saved_cost) AS NUMERIC), 2), ROUND(CAST(SUM(cost) AS NUMERIC), 2), MIN(min_temperature), "
					+ "SUM(avg_temperature), MAX(max_temperature), MIN(light_min_level), SUM(light_avg_level), MAX(light_max_level), "
					+ "COUNT(*), SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', ''))) "
					+ "FROM energy_consumption ec, device d WHERE capture_at> '"
					+ fromDateStr
					+ "' AND capture_at<= '"
					+ toDateStr
					+ "' AND zero_bucket != 1 AND d.id = ec.fixture_id AND d.floor_id = "
					+ floorId + " GROUP BY capture_at";

			if(logger.isDebugEnabled()) {
				logger.debug("getEmFloor5minEnergyReadings query -- " + query);
			}
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while (rs.next()) {
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("getEmFloor5minEnergyReadings: "
					+ (System.currentTimeMillis() - startTime));
		}
		return flEnergyData;

	} // end of method getEmFloor5minEnergyReadings

	// this is for details of each sensor without aggregating
	public List<SensorData> getEmFloorSensorRawEnergyReadings(String dbName,
			String replicaServer, String fromDate, String toDate, long floorId,
			String attributes) {

		long startTime = System.currentTimeMillis();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		ArrayList<SensorData> emFloorSensorList = new ArrayList<SensorData>();
		try {
			connection = getDbConnection(dbName, replicaServer,
					DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = null;

			if (attributes != null) {
				StringBuffer rawQueryBuffer = new StringBuffer(
						"SELECT fixture_id, capture_at + '-5 min'");
				if (attributes.contains("baseEnergy")) {
					rawQueryBuffer
							.append(", round(base_power_used, 2) AS baseEnergy");
				}
				if (attributes.contains("energy")) {
					rawQueryBuffer.append(", round(power_used, 2) AS energy");
				}
				if (attributes.contains("savedOccEnergy")) {
					rawQueryBuffer
							.append(", round(occ_saving, 2) AS occSavings");
				}
				if (attributes.contains("savedAmbEnergy")) {
					rawQueryBuffer
							.append(", round(ambient_saving, 2) AS ambSavings");
				}
				if (attributes.contains("savedTaskTunedEnergy")) {
					rawQueryBuffer
							.append(", round(tuneup_saving + manual_saving, 2) AS ttSavings");
				}
				if (attributes.contains("avgTemp")) {
					rawQueryBuffer.append(", avg_temperature");
				}
				if (attributes.contains("avgAmb")) {
					rawQueryBuffer.append(", light_avg_level");
				}
				if (attributes.contains("motionBits")) {
					rawQueryBuffer.append(", motion_bits");
				}
				if (attributes.contains("avgVolts")) {
					rawQueryBuffer.append(", avg_volts");
				}
				rawQueryBuffer
						.append(" from energy_consumption ec, device d WHERE capture_at> '");
				rawQueryBuffer.append(fromDate);
				rawQueryBuffer.append("' AND capture_at<= '");
				rawQueryBuffer.append(toDate);
				rawQueryBuffer
						.append("' AND d.id = ec.fixture_id AND zero_bucket != 1 AND floor_id = ");
				rawQueryBuffer.append(floorId);
				query = rawQueryBuffer.toString();
			} else {
				query = " SELECT fixture_id, capture_at + '-5 min', round(base_power_used, 2) AS baseEnergy, "
						+ "round(power_used, 2) AS energy, round(occ_saving, 2) AS occSavings, round(ambient_saving, 2) AS "
						+ "ambSavings, round(tuneup_saving + manual_saving, 2) AS ttSavings, avg_temperature, light_avg_level,"
						+ " motion_bits, avg_volts from energy_consumption ec, device d WHERE capture_at> '"
						+ fromDate
						+ "' AND capture_at<= '"
						+ toDate
						+ "' AND d.id = ec.fixture_id AND zero_bucket != 1 AND floor_id = "
						+ floorId;
			}
			if(logger.isDebugEnabled()) {
				logger.debug("getEmFloorSensorRawEnergyReadings query -- " + query);
			}
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				SensorData data = new SensorData();
				data.setSensorId(rs.getLong(1));
				data.setTimestamp(rs.getTimestamp(2));
				if (attributes == null) {
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
				} else {
					if (attributes.contains("baseEnergy")) {
						data.setBaseEnergy(rs.getBigDecimal("baseEnergy"));
					}
					if (attributes.contains("energy")) {
						data.setEnergy(rs.getBigDecimal("energy"));
					}
					if (attributes.contains("savedOccEnergy")) {
						data.setSavedOccEnergy(rs.getBigDecimal("occSavings"));
					}
					if (attributes.contains("savedAmbEnergy")) {
						data.setSavedAmbEnergy(rs.getBigDecimal("ambSavings"));
					}
					if (attributes.contains("savedTaskTunedEnergy")) {
						data.setSavedTaskTunedEnergy(rs
								.getBigDecimal("ttSavings"));
					}
					if (attributes.contains("avgTemp")) {
						data.setAvgTemp(rs.getFloat("avg_temperature"));
					}
					if (attributes.contains("avgAmb")) {
						data.setAvgAmb(rs.getInt("light_avg_level"));
					}
					if (attributes.contains("motionBits")) {
						data.setMotionBits(rs.getLong("motion_bits"));
					}
					if (attributes.contains("avgVolts")) {
						data.setAvgVolts(rs.getFloat("avg_volts"));
					}
				}
				emFloorSensorList.add(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("getEmFloorSensorRawEnergyReadings: "
					+ (System.currentTimeMillis() - startTime));
		}
		return emFloorSensorList;

	} // end of method getEmFloorSensorRawEnergyReadings

	public ArrayList<Object[]> getEmFloorDailyEnergyReadings(String dbName,
			String replicaServer, Date fromDate, Date toDate) {

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate,
				"yyyy-MM-dd HH:mm");
		String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer,
					DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT floor_id, capture_at + '-1 day', SUM(base_power_used), SUM(power_used), SUM(occ_saving), "
					+ "SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), "
					+ "SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), "
					+ "min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) "
					+ "from energy_consumption_daily ec, device d WHERE capture_at> '"
					+ fromDateString
					+ "' AND capture_at<= '"
					+ toDateString
					+ "' AND d.id = ec.fixture_id GROUP BY floor_id, capture_at order by floor_id;";

			if(logger.isDebugEnabled()) {
				logger.debug("getEmFloorDailyEnergyReadings query -- " + query);
			}
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while (rs.next()) {
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return flEnergyData;

	} // end of method getEmFloorDailyEnergyReadings

	public ArrayList<Object[]> getEmFloorHourlyEnergyReadings(String dbName,
			String replicaServer, Date fromDate, Date toDate) {

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate,
				"yyyy-MM-dd HH:mm");
		String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer,
					DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT floor_id, capture_at + '-1 hour', SUM(base_power_used), SUM(power_used), SUM(occ_saving), "
					+ "SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), "
					+ "SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), "
					+ "min(light_min_level), sum(light_avg_level), max(light_max_level), count(*) from "
					+ "energy_consumption_hourly ec, device d WHERE capture_at> '"
					+ fromDateString
					+ "' AND capture_at<= '"
					+ toDateString
					+ "' AND d.id = ec.fixture_id GROUP BY floor_id, capture_at order by floor_id;";

			if(logger.isDebugEnabled()) {
				logger.debug("getEmFloorHourlyEnergyReadings query -- " + query);
			}
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while (rs.next()) {
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return flEnergyData;

	} // end of method getEmFloorHourlyEnergyReadings

	public ArrayList<Object[]> getEmFloor15minEnergyReadings(String dbName,
			String replicaServer, Date fromDate, Date toDate) {

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		String fromDateString = DateUtil.formatDate(fromDate,
				"yyyy-MM-dd HH:mm");
		String toDateString = DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer,
					DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = "SELECT floorId, SUM(base_energy), SUM(energy), SUM(ec_occ_saving), "
					+ "SUM(ec_ambient_saving), SUM(ec_tuneup_saving), SUM(ec_manual_saving), SUM(ec_saved_power_used), SUM(ec_base_cost),"
					+ " SUM(ec_saved_cost), SUM(ec_cost), MIN(min_temp), SUM(avg_temp), MAX(max_temp), MIN(min_light_level), "
					+ "SUM(avg_light_level), MAX(max_light_level), SUM(countFx), SUM(motion_bits) FROM (SELECT d.floor_id AS floorId,"
					+ "ec.fixture_id AS fixture_id, avg(base_power_used) * 0.25 AS base_energy, AVG(power_used) * 0.25 AS energy, "
					+ "AVG(occ_saving) * 0.25 AS ec_occ_saving, AVG(ambient_saving) * 0.25 AS ec_ambient_saving, "
					+ "AVG(tuneup_saving) * 0.25 AS ec_tuneup_saving, AVG(manual_saving) * 0.25 AS ec_manual_saving, "
					+ "AVG(saved_power_used) * 0.25 AS ec_saved_power_used, AVG(base_cost) * 0.25 AS ec_base_cost, "
					+ "AVG(saved_cost) * 0.25 AS ec_saved_cost, avg(cost) * 0.25 AS ec_cost, min(min_temperature) AS min_temp, "
					+ "AVG(avg_temperature) AS avg_temp, MAX(max_temperature) AS max_temp, MIN(light_min_level) AS min_light_level, "
					+ "AVG(light_avg_level) AS avg_light_level, MAX(light_max_level) AS max_light_level, count(*) AS countFx, "
					+ "(SELECT COALESCE(SUM(LENGTH(REPLACE(CAST(CAST(motion_bits AS bit(64)) AS TEXT), '0', ''))),0)) AS motion_bits "
					+ "FROM energy_consumption ec, device d WHERE capture_at > '"
					+ fromDateString
					+ "' AND capture_at <= '"
					+ toDateString
					+ "' AND d.id = ec.fixture_id and zero_bucket != 1 GROUP BY floor_id, fixture_id ORDER BY fixture_id) "
					+ "as min15Avg GROUP BY floorId ORDER BY floorId;";

			if(logger.isDebugEnabled()) {
				logger.debug("getEmFloor15minEnergyReadings query -- " + query);
			}
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while (rs.next()) {
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
				flEnergyData.add(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return flEnergyData;

	} // end of method getEmFloor15minEnergyReadings

	public ArrayList<Object[]> getLast15minEmFloorEnergyReadings(String dbName,
			String replicaServer) {

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		// String fromDateString = DateUtil.formatDate(fromDate,
		// "yyyy-MM-dd HH:mm");
		// String toDateString = DateUtil.formatDate(toDate,
		// "yyyy-MM-dd HH:mm");
		ArrayList<Object[]> flEnergyData = new ArrayList<Object[]>();
		try {
			connection = getDbConnection(dbName, replicaServer,
					DatabaseUtil.port);
			stmt = connection.createStatement();
			String query = " SELECT floor_id, SUM(base_power_used), SUM(power_used), SUM(occ_saving), "
					+ "SUM(ambient_saving), SUM(tuneup_saving), SUM(manual_saving), SUM(saved_power_used), SUM(base_cost), "
					+ "SUM(saved_cost), SUM(cost), min(min_temperature), sum(avg_temperature), max(max_temperature), "
					+ "min(light_min_level), sum(light_avg_level), max(light_max_level), count(*), SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) "
					+ "AS TEXT), '0', ''))), date_trunc('min', floor_minutes(now()::timestamp, 15)) + interval '-15 min' "
					+ "from energy_consumption ec, device d WHERE capture_at > date_trunc('min', floor_minutes(now()::timestamp, 15)) + "
					+ "interval '-15 min' AND capture_at <= date_trunc('min', floor_minutes(now()::timestamp, 15)) AND d.id = "
					+ "ec.fixture_id GROUP BY floor_id order by floor_id;";

			if(logger.isDebugEnabled()) {
				logger.debug("getLast15minEmFloorEnergyReadings query -- " + query);
			}
			rs = stmt.executeQuery(query);
			Object[] row = null;
			while (rs.next()) {
				row = new Object[20];
				row[0] = rs.getLong(1);
				row[1] = rs.getDate(20);
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return flEnergyData;

	} // end of method getEmFloor15minEnergyReadings

	public void saveOrUpdate(FloorEnergyConsumptionDaily ec) {

		sessionFactory.getCurrentSession().saveOrUpdate(ec);

	}

	public void saveOrUpdate(FloorEnergyConsumptionHourly ec) {

		sessionFactory.getCurrentSession().saveOrUpdate(ec);

	}

	public void saveOrUpdate(FloorEnergyConsumption15min ec) {

		sessionFactory.getCurrentSession().saveOrUpdate(ec);

	}

	public void getAggregateHourlyMotionEvents(Date toDate) {
		String hsql = "SELECT  aggregatehourlymotionevents(" + toDate + ")";
		Query q = sessionFactory.getCurrentSession().createSQLQuery(
				hsql.toString());
		Integer result = (Integer) q.uniqueResult();

	}

	public void getAggregateDailyMotionEvents() {
		String hsql = "SELECT  aggregatehourlymotionevents()";
		Query q = sessionFactory.getCurrentSession().createSQLQuery(
				hsql.toString());
		q.uniqueResult();
	}

	public void saveEcSyncVO(List<EcSyncVo> items, Long emId,
			HashMap<Long, String> facLastTimeStamp) {
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss");
			if (!ArgumentUtils.isNullOrEmpty(items)) {
				EmLastEcSynctime emLastTimeSync = emLastEcSynctimeManager
						.getEmLastEcSynctimeForEmId(emId);
				// this is needed beacause uem put zero bucket for the floor if
				// uem was not able too communicate with em
				// this fuction analyzes the record which are present in
				// ecsyncvo and mark them as update if already present as zero
				// bucket. which then are handle
				// here in this function appropriately.
				items = analyzeEcSyncVoforUEMZb(items, facLastTimeStamp,
						emLastTimeSync);
				Collections.sort(items);
				for (EcSyncVo item : items) {
					EmInstance em = emInstanceManager.getEmInstance(emId);
					if(em!=null){
						FacilityEmMapping facEmMap = facilityEmMappingManager
								.getFacilityEmMappingOnEmFloorId(em.getId(),
										item.getLevelId());
						if (!ArgumentUtils.isNull(facEmMap)) {
							Long levelId = facEmMap.getFacilityId();
							if (item.getZbUpdate()) {
								// this record is a zero bucket record which means
								// there
								// exist a row already in the database
								// fetch and update that row.
								if(logger.isDebugEnabled()) {
									logger.debug(item.getCaptureAt() +": Got Zero bucket update packet for em " + em.getMacId());
								}
								FloorEnergyConsumption15min newRow = getFloor15minEnergyData(
										levelId, item.getCaptureAt());
								if (newRow != null) {
									newRow.setCustId(em.getCustomer().getId());
									newRow.setLevelId(levelId);
									newRow.setCaptureAt(inputFormat
											.parse(inputFormat.format(item
													.getCaptureAt())));
									newRow.setBaseEnergy(item.getBaseEnergy());
									newRow.setEnergy(item.getEnergy());
									newRow.setSavedEnergy(item.getSavedEnergy());
									newRow.setOccSavings(item.getOccSavings());
									newRow.setAmbientSavings(item
											.getAmbientSavings());
									newRow.setTuneupSavings(item.getTuneupSavings());
									newRow.setManualSavings(item.getManualSavings());
									newRow.setBaseCost(item.getBaseCost());
									newRow.setCost(item.getCost());
									newRow.setSavedCost(item.getSavedCost());
									newRow.setPrice(item.getPrice());
									newRow.setMinTemp(item.getMinTemp());
									newRow.setAvgTemp(item.getAvgTemp());
									newRow.setMaxTemp(item.getMaxTemp());
									newRow.setMinAmb(item.getMinAmb());
									newRow.setAvgAmb(item.getAvgAmb());
									newRow.setMaxAmb(item.getMaxAmb());
									newRow.setMotionEvents(item.getMotionEvents());
									saveOrUpdate(newRow);
									EmLastEcSynctime lastTime = emLastEcSynctimeManager
											.getEmLastEcSynctimeForEmId(emId);
									if (lastTime.getLastSyncAt().before(
											newRow.getCaptureAt())) {
										lastTime.setLastSyncAt(newRow
												.getCaptureAt());
										emLastEcSynctimeManager
												.saveOrUpdate(lastTime);
									}

								}

							} else {
								if(logger.isDebugEnabled()) {
									logger.debug(item.getCaptureAt() + ": Got Normal packet for em " + em.getMacId());
								}
								FloorEnergyConsumption15min newRow = new FloorEnergyConsumption15min();
								newRow.setCustId(em.getCustomer().getId());
								newRow.setLevelId(levelId);
								newRow.setCaptureAt(item.getCaptureAt());
								newRow.setBaseEnergy(item.getBaseEnergy());
								newRow.setEnergy(item.getEnergy());
								newRow.setSavedEnergy(item.getSavedEnergy());
								newRow.setOccSavings(item.getOccSavings());
								newRow.setAmbientSavings(item.getAmbientSavings());
								newRow.setTuneupSavings(item.getTuneupSavings());
								newRow.setManualSavings(item.getManualSavings());
								newRow.setBaseCost(item.getBaseCost());
								newRow.setCost(item.getCost());
								newRow.setSavedCost(item.getSavedCost());
								newRow.setPrice(item.getPrice());
								newRow.setMinTemp(item.getMinTemp());
								newRow.setAvgTemp(item.getAvgTemp());
								newRow.setMaxTemp(item.getMaxTemp());
								newRow.setMinAmb(item.getMinAmb());
								newRow.setAvgAmb(item.getAvgAmb());
								newRow.setMaxAmb(item.getMaxAmb());
								newRow.setMotionEvents(item.getMotionEvents());
								saveOrUpdate(newRow);
								EmLastEcSynctime lastTime = emLastEcSynctimeManager
										.getEmLastEcSynctimeForEmId(emId);
								lastTime.setLastSyncAt(newRow.getCaptureAt());
								emLastEcSynctimeManager.saveOrUpdate(lastTime);
							}
						} else {
							logger.error(em.getMacId() + ": Facility is not mapped on Cloud. Aggregation for this facility will not be done. "
									+ " Facility id on EM that is not mapped = " + item.getLevelId());
						}
					}
					
				}
			} else {
				if(logger.isInfoEnabled()) {
					logger.info(emId + " :written empty EcSyncVo List for this em");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * @param items
	 * @param facLastTimeStamp
	 *            this fuction analyzes the record which are present in ecsyncvo
	 *            and mark them as update if already present as zero bucket
	 * @param emLastTimeSync
	 */
	private List<EcSyncVo> analyzeEcSyncVoforUEMZb(List<EcSyncVo> items,
			HashMap<Long, String> facLastTimeStamp,
			EmLastEcSynctime emLastTimeSync) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		ArrayList<EcSyncVo> modifieditems = new ArrayList<EcSyncVo>();
		String dateFormat = "yyyyMMddHHmmss";
		Date lastSync = null;
		if (emLastTimeSync.getLastSyncAt() != null) {
			lastSync = DateUtil.parseString(DateUtil.formatDate(
					emLastTimeSync.getLastSyncAt(), dateFormat), dateFormat);
		} else {
			// at start there will be nothing so give
			// Epoch.
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(0);
			lastSync = DateUtil.parseString(
					DateUtil.formatDate(c.getTime(), dateFormat), dateFormat);
		}
		for (Long key : facLastTimeStamp.keySet()) {
			Date lastFloorLevelecTableCaptureAt = DateUtil.parseString(
					facLastTimeStamp.get(key), dateFormat);
			// this means zero buckets are inserted. lets set zbUpdate to true
			// for thoes records which are inserted as zero buckets
			if (lastSync.before(lastFloorLevelecTableCaptureAt)) {
				for (EcSyncVo item : items) {
					EcSyncVo temp = item;
					Date toSearch = DateUtil.parseString(DateUtil.formatDate(
							temp.getCaptureAt(), dateFormat), dateFormat);
					// see if the packet is within given range including the
					// bounderies.
					if ((toSearch.before(lastFloorLevelecTableCaptureAt) || toSearch
							.equals(lastFloorLevelecTableCaptureAt))
							&& (toSearch.after(lastSync) || toSearch
									.equals(lastSync))) {
						temp.setZbUpdate(true);
						modifieditems.add(temp);
					} else {
						modifieditems.add(temp);
					}
				}
				return modifieditems;
			} else if (lastSync.after(lastFloorLevelecTableCaptureAt)) {
				logger.error("lastSync timestamp :- "
						+ lastSync.toString()
						+ " for EM with em id :- "
						+ emLastTimeSync.getEmId()
						+ " is after the last capture_at time timestamp :- "
						+ lastFloorLevelecTableCaptureAt.toString()
						+ " of 15 min floor energy consumption table for level id :- "
						+ key
						+ " This means there was either data corruption or some rows got deleted for this level. Contact Admin.");
				modifieditems = (ArrayList<EcSyncVo>) items;
				return modifieditems;
			} else if (lastSync.equals(lastFloorLevelecTableCaptureAt)) {
				// if both dates are equal this function will not do anything on
				// the
				// EcSyncVo List just return it as it is.
				modifieditems = (ArrayList<EcSyncVo>) items;
				return modifieditems;
			}
		}

		return items;
	}

	/**
	 * @param emId
	 *            Insert zero bucket record into 15 min floor consumption table
	 *            in all the facility that belong to this EM.
	 */
	public void putEcZeroBucket(Long emId) {
		try {
			ArrayList<FacilityEmMapping> facEmMappingList = (ArrayList<FacilityEmMapping>) facilityEmMappingManager
					.getFacilityEmMappingOnEmId(emId);
			EmInstance em = emInstanceManager.getEmInstance(emId);
			if (!ArgumentUtils.isNullOrEmpty(facEmMappingList) && em != null) {
				for (FacilityEmMapping fe : facEmMappingList) {
					if(fe.getFacilityType().ordinal() != FacilityType.FLOOR.ordinal()) {
						continue;
					}
					// There are no previous record for this facility dont put
					// zero bucket.
					if (!is15MinFloorTableEmptyForFacility(fe.getFacilityId())) {
						/*
						 * FloorEnergyConsumption15min zeroBucket = new
						 * FloorEnergyConsumption15min();
						 * zeroBucket.setCustId(em.getCustomer().getId());
						 * zeroBucket.setLevelId(fe.getFacilityId()); // add 15
						 * min to previous record capture_at and insert. Date
						 * newDate = getLatestCaptureAtFor15MinFloor(fe
						 * .getFacilityId()); DateUtils.addMinutes(newDate, 15);
						 * logger.info("Zero Bucket for em "+
						 * em.getMacAddress()+" for time "+ newDate);
						 * zeroBucket.setCaptureAt(newDate);
						 * zeroBucket.setBaseEnergy(0.0);
						 * zeroBucket.setEnergy(new BigDecimal(0));
						 * zeroBucket.setSavedEnergy(new BigDecimal(0));
						 * zeroBucket.setOccSavings(new BigDecimal(0));
						 * zeroBucket.setAmbientSavings(new BigDecimal(0));
						 * zeroBucket.setTuneupSavings(new BigDecimal(0));
						 * zeroBucket.setManualSavings(new BigDecimal(0));
						 * zeroBucket.setBaseCost(0.0); zeroBucket.setCost(0.0);
						 * zeroBucket.setSavedCost(0.0);
						 * zeroBucket.setPrice(0.0f);
						 * zeroBucket.setMinTemp(0.0f);
						 * zeroBucket.setAvgTemp(0.0f);
						 * zeroBucket.setMaxTemp(0.0f); zeroBucket.setMinAmb(new
						 * Float(0.0)); zeroBucket.setAvgAmb(0f);
						 * zeroBucket.setMaxAmb(0.0f);
						 * zeroBucket.setMotionEvents(0l);
						 * saveOrUpdate(zeroBucket);
						 */
						Date newDate = getLatestCaptureAtFor15MinFloor(fe
								.getFacilityId());
						newDate = DateUtils.addMinutes(newDate, 15);
						if(logger.isInfoEnabled()) {
							logger.info(newDate + " :Zero Bucket for em " + em.getMacId());
						}
						String sql = "insert into floor_energy_consumption_15min (cust_id,level_id,energy,cost,occ_savings,tuneup_savings,ambient_savings,manual_savings,capture_at) "
								+ "values("
								+ em.getCustomer().getId()
								+ ","
								+ +fe.getFacilityId()
								+ ",0,0,0,0,0,0,'"
								+ DateUtil.formatDate(newDate,
										"yyyy-MM-dd HH:mm") + "')";
						Query q = sessionFactory.getCurrentSession()
								.createSQLQuery(sql.toString());
						q.executeUpdate();

					} else {
						if(logger.isInfoEnabled()) {
							logger.info(fe.getFacilityId() + " :There are no record for this level id facility in the 15 min floor ec table.");
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Date getLatestCaptureAtFor15MinFloor(Long levelId) {
		Date latest = null;
		try {
			DetachedCriteria maxDateQuery = DetachedCriteria
					.forClass(FloorEnergyConsumption15min.class);
			maxDateQuery.add(Restrictions.eq("levelId", levelId));
			ProjectionList proj = Projections.projectionList();
			proj.add(Projections.max("captureAt"));
			maxDateQuery.setProjection(proj);
			Criteria cr = sessionFactory.getCurrentSession().createCriteria(
					FloorEnergyConsumption15min.class);
			FloorEnergyConsumption15min fe = (FloorEnergyConsumption15min) cr
					.add(Restrictions.eq("levelId", levelId))
					.add((Subqueries.propertyEq("captureAt", maxDateQuery)))
					.uniqueResult();
			// String sql =
			// HibernateUtil.toSql(sessionFactory.getCurrentSession(), cr);
			if (fe != null) {
				return latest = fe.getCaptureAt();
			}
		} catch (HibernateException hbe) {
			logger.error(hbe.getMessage(), hbe);
		}
		return latest;
	}

	/**
	 * @param id
	 * @return this function return false if there are any records related to
	 *         the given facility in the 15 min floor Energy consumption table.
	 */
	public Boolean is15MinFloorTableEmptyForFacility(Long levelId) {
		Boolean flag = true;
		try {
			String hsql = "SELECT 1 FROM FloorEnergyConsumption15min where levelId= :levelId";
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			q.setLong("levelId", levelId);
			if (q.list() != null && q.list().size() != 0) {
				flag = false;
			}

		} catch (HibernateException hbe) {
			logger.error(hbe.getMessage(), hbe);
		}
		return flag;
	}

	public FloorEnergyConsumption15min getFloor15minEnergyData(long floorId,
			Date capture_at) {
		FloorEnergyConsumption15min energy = null;

		try {
			FloorEnergyConsumption15min fe = (FloorEnergyConsumption15min) sessionFactory
					.getCurrentSession()
					.createCriteria(FloorEnergyConsumption15min.class)
					.add(Restrictions.eq("levelId", floorId))
					.add(Restrictions.eq("captureAt", capture_at))
					.uniqueResult();
			if (fe != null) {
				energy = fe;
			}

		} catch (HibernateException hbe) {
			logger.error(hbe.getMessage(), hbe);
		}
		return energy;

	} // end of method getFloorDailyEnergyData

} // end of class FloorEnergyConsumptionDao
