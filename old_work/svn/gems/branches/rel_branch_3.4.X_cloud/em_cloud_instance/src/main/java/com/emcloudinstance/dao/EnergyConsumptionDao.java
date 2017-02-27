package com.emcloudinstance.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emcloudinstance.jdbcutils.rowmapper.EcSyncVoMapper;
import com.emcloudinstance.service.FloorManager;
import com.emcloudinstance.service.FloorZbUpdateManager;
import com.emcloudinstance.util.DateUtil;
import com.emcloudinstance.vo.EcSyncVo;
import com.emcloudinstance.vo.FloorZbUpdate;

@Repository("energyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EnergyConsumptionDao extends AbstractJdbcDao {

	static final Logger logger = Logger.getLogger(EnergyConsumptionDao.class
			.getName());

	@Resource
	FloorZbUpdateManager floorZbUpdateManager;

	@Resource
	FloorManager floorManager;

	/**
	 * @param oFDate
	 * @param mac
	 * @return a List of Floor level energy consumption for 15 min time duration
	 *         from this date till the latest that is present. Also look up zb
	 *         update for indiviual floor and add them to the list.
	 */
	public List<EcSyncVo> load15minFloorEnergyConsumptionForAllFloorWithZb(
			Date oFDate, String mac, String emTimeZone) {
		ArrayList<EcSyncVo> records = new ArrayList<EcSyncVo>();
		Date lastEcDate = loadLatestEnergyConsumptionDate(mac,emTimeZone);
		Date minDate = null;
		try {
			if (lastEcDate != null && oFDate != null) {
				// truncate dates to nearest previous 15 min boudary.
				minDate = DateUtil.truncateToPrevious15min(oFDate);
				Date maxDate = DateUtil.truncateToPrevious15min(lastEcDate);
				// set third parameter false as these are not update but inserts
				// in uem
				ArrayList<EcSyncVo> nonZbrecords = (ArrayList<EcSyncVo>) get15minFloorEnergyConsumptionForFloorForUEM(
						minDate, maxDate, false, mac ,emTimeZone);
				if (!ArgumentUtils.isNullOrEmpty(nonZbrecords)) {
					records.addAll(nonZbrecords);
				}
			}
			// some EMs may not be upgraded to latest version and will not have
			// floor ZBUpdate tables for such EM skip this step.
			if (floorZbUpdateManager.isTableAvailable(mac)) {
				// get all zero bucket update aggregation for all floor and add
				// them
				// to record before sending as zb updates.
				ArrayList<FloorZbUpdate> floorZbUpdateList = (ArrayList<FloorZbUpdate>) floorZbUpdateManager
						.loadAllUnProcessedFloorZbUpdate(mac);
				if (!ArgumentUtils.isNullOrEmpty(floorZbUpdateList)) {
					for (FloorZbUpdate f : floorZbUpdateList) {
						// if minDate is after zb update end date them it is ok
						// to send zero buckets
						// other wise dont send zero buckets in this iteration.
						if (minDate.after(f.getEndTime())) {
							ArrayList<EcSyncVo> e = (ArrayList<EcSyncVo>) get15minFloorEnergyConsumptionForFloorZbUpdate(
									f, mac,emTimeZone);
							if (!ArgumentUtils.isNullOrEmpty(e)) {
								records.addAll(e);
								// as processed save the processed flag to 1 and
								// save
								// the floorZbupdate back.
								f.setProcessedState(1l);
								floorZbUpdateManager.update(f, mac);
							}
						}
					}
				}
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return records;
	}

	/**
	 * @param mac
	 * @param oFDate
	 * @return aggregation for zero bucket that happend.
	 */
	public List<EcSyncVo> get15minFloorEnergyConsumptionForFloorZbUpdate(
			FloorZbUpdate fZb, String mac,String emTimeZone) {
		ArrayList<EcSyncVo> records = new ArrayList<EcSyncVo>();
		try {
			// nearest previous 15 min boudary because we want to include it as
			// it is zero
			// bucket update. Also sub 15 because we want the record for this
			// previous boudary. (As the
			// get15minFloorEnergyConsumptionForFloorForUEM exclude the min
			// boudary but in zero update case we want it to be a part of the
			// bucket.)
			Date minDate = DateUtils.addMinutes(
					DateUtil.truncateToPrevious15min(fZb.getStartTime()), -15);
			// Nearest futuer 15 min boudary as we want it in zb update.
			Date maxDate = DateUtil.truncateToFuture15min(fZb.getEndTime());
			// third parameter true because it is a zero bucket update record
			// and we want to treat it as update
			records = (ArrayList<EcSyncVo>) get15minFloorEnergyConsumptionForFloorForUEM(
					minDate, maxDate, true, mac,emTimeZone);
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return records;
	}

	/**
	 * @param minTime
	 * @param maxTime
	 * @param zbUpdate
	 * @param mac
	 * @return
	 * 
	 *         IMPORTANT :- minTime and maxTime should be on 15 min boundary.
	 *         This is callers Responsibility to truncate dates to 15 min
	 *         boudaries. returns 15 min EC records betwen the given date on
	 *         floor level for all floor.
	 * 
	 */
	public List<EcSyncVo> get15minFloorEnergyConsumptionForFloorForUEM(
			Date minTime, Date maxTime, Boolean zbUpdate, String mac,
			String emTimeZone) {
		final int TEN_DAY = 96;
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		if (minTime == null || maxTime == null || zbUpdate == null
				|| emTimeZone == null) {
			throw new NullPointerException(
					"Argument to get15minFloorEnergyConsumptionForFloorForUEM cannot be null");
		}
		if (maxTime.before(minTime)) {
			throw new IllegalArgumentException(
					"Till date cannot be less than from date...");
		}
		ArrayList<EcSyncVo> records = new ArrayList<EcSyncVo>();
		try {
			int count = 0;
			Calendar startDate = Calendar.getInstance();
			Long startTime = startDate.getTimeInMillis();
			while (!minTime.equals(maxTime) && count < TEN_DAY) {
				String query = "SELECT floorId , captureAt, "
						+ "(SELECT COALESCE(round(sum(ec_base_power_used),2),0)) as basePowerUsed, "
						+ "(SELECT COALESCE(round(sum(ec_power_used),2),0)) as powerUsed, "
						+ "(SELECT COALESCE(round(sum(ec_occ_saving),2),0)) as occSavings,"
						+ "(SELECT COALESCE(round(sum(ec_ambient_saving),2),0)) as ambientSavings,"
						+ "(SELECT COALESCE(round(sum(ec_tuneup_saving),2),0)) as tuneUpSavings, "
						+ "(SELECT COALESCE(round(sum(ec_manual_saving),2),0)) as manualSavings, "
						+ "(SELECT COALESCE(round(sum(ec_saved_power_used),2),0)) as savedPowerUsed, "
						+ "(SELECT COALESCE(sum(ec_base_cost),0)) as baseCost, "
						+ "(SELECT COALESCE(sum(ec_saved_cost),0)) as savedCost, "
						+ "(SELECT COALESCE(sum(ec_cost),0)) as cost, "
						+ "(SELECT COALESCE(round(min(min_temperature),2),0)) as minTemp, "
						+ "(SELECT COALESCE(round(avg(avg_temperature),2),0)) as avgTemp, "
						+ "(SELECT COALESCE(round(max(max_temperature),2),0)) as maxTemp, "
						+ "(SELECT COALESCE(round(min(light_min_level),2),0)) as minLightLevel, "
						+ "(SELECT COALESCE(round(avg(light_avg_level),2),0)) as avgLightLevel, "
						+ "(SELECT COALESCE(round(max(light_max_level),2),0)) as maxLightLevel, "
						+ "(SELECT COALESCE(round(sum(countFx),2),0)) as countFx, "
						+ "(SELECT COALESCE(round(sum(motion_bits),2),0)) as totalMbits, "
						+ "(SELECT COALESCE(max(price),0)) as maxPrice "
						+ "from (select  d.floor_id as floorId, ec.fixture_id as fixture_id,cast('"
						+ DateUtil.formatDate(
								DateUtils.addMinutes(minTime, 15),
								"yyyy-MM-dd HH:mm")
						+ "'as text) as captureAt, "
						+ "(SELECT COALESCE(round(avg(base_power_used) * 0.25,2),0)) as ec_base_power_used, "
						+ "(SELECT COALESCE(round(avg(power_used) * 0.25,2),0)) as ec_power_used, "
						+ "(SELECT COALESCE(round((avg(occ_saving) * 0.25),2),0)) as ec_occ_saving, "
						+ "(SELECT COALESCE(round(avg(ambient_saving) * 0.25,2),0)) as ec_ambient_saving, "
						+ "(SELECT COALESCE(round(avg(tuneup_saving) * 0.25,2),0)) as ec_tuneup_saving, "
						+ "(SELECT COALESCE(round(avg(manual_saving) * 0.25,2),0)) as ec_manual_saving, "
						+ "(SELECT COALESCE(round(avg(saved_power_used) * 0.25,2),0)) as ec_saved_power_used, "
						+ "(SELECT COALESCE(avg(base_cost) * 0.25,0)) as ec_base_cost, "
						+ "(SELECT COALESCE(avg(saved_cost) * 0.25,0)) as ec_saved_cost, "
						+ "(SELECT COALESCE((avg(cost) * 0.25),0)) as ec_cost, "
						+ "(SELECT COALESCE(round(min(min_temperature),2),0)) as min_temperature, "
						+ "(SELECT COALESCE(round(avg(avg_temperature),2),0)) as avg_temperature, "
						+ "(SELECT COALESCE(round(max(max_temperature),2),0)) as max_temperature, "
						+ "(SELECT COALESCE(round(min(light_min_level),2),0)) as light_min_level, "
						+ "(SELECT COALESCE(round(avg(light_avg_level),2),0)) as light_avg_level, "
						+ "(SELECT COALESCE(round(max(light_max_level),2),0)) as light_max_level, "
						+ "count(*) as countFx, "
						+ "(SELECT COALESCE(SUM(LENGTH(REPLACE(CAST(CAST(motion_bits as bit(64)) AS TEXT), '0', ''))),0)) as motion_bits, "
						+ "(SELECT COALESCE(max(price),0)) as price "
						+ "from energy_consumption ec, device d WHERE "
						+ " capture_at > '"
						+ DateUtil.formatDate(minTime, "yyyy-MM-dd HH:mm")
						+ "' AT TIME ZONE '"
						+ emTimeZone
						+ "' "
						+ "AND "
						+ "capture_at <= '"
						+ DateUtil.formatDate(
								DateUtils.addMinutes(minTime, 15),
								"yyyy-MM-dd HH:mm")
						+ "' AT TIME ZONE '"
						+ emTimeZone
						+ "' "
						+ "AND d.id = ec.fixture_id "
						+ "GROUP BY fixture_id, floor_id order by fixture_id) as min15Avg group by floorId, captureAt";
				EcSyncVoMapper ex = new EcSyncVoMapper();
				ex.setZbUpdate(zbUpdate);
				List<EcSyncVo> results = jdbcTemplate.query(query.toString(),
						ex);
				if (results != null && !results.isEmpty()) {
					records.addAll(results);
				} else {
					// if result is empty we send a zero bucket for that slot.
					// This scenario occurs if EM was completely
					// down for that 15 min bucket.
					List<BigInteger> resultsfloor = floorManager
							.getAllFloorsOfCompany(mac);
					Iterator<BigInteger> resultsIter = resultsfloor.iterator();
					while (resultsIter.hasNext()) {
						BigInteger data = resultsIter.next();
						EcSyncVo record = new EcSyncVo();
						record.setLevelId((data).longValue());
						record.setCaptureAt(DateUtils.addMinutes(minTime, 15));
						record.setBaseEnergy(0.0);
						record.setEnergy(new BigDecimal(0));
						record.setOccSavings(new BigDecimal(0));
						record.setAmbientSavings(new BigDecimal(0));
						record.setTuneupSavings(new BigDecimal(0));
						record.setManualSavings(new BigDecimal(0));
						record.setSavedEnergy(new BigDecimal(0));
						record.setBaseCost(0.0);
						record.setCost(0.0);
						record.setSavedCost(0.0);
						record.setMinTemp(0.0f);
						record.setAvgTemp(0.0f);
						record.setMaxTemp(0.0f);
						record.setMinAmb(0.0f);
						record.setAvgAmb(0.0f);
						record.setMaxAmb(0.0f);
						record.setMotionEvents(0l);
						record.setPrice(0.0f);
						// as they are zb update set it to true
						record.setZbUpdate(zbUpdate);
						records.add(record);
					}
				}
				minTime = DateUtils.addMinutes(minTime, 15);
				count++;
			}
			Calendar endDate = Calendar.getInstance();
			Long endTime = endDate.getTimeInMillis();
			logger.debug("Cloud 15 min aggregation data collection took "
					+ (endTime - startTime) + "MiliSec");
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return records;
	}

	/**
	 * load latest EnergyConsumption details .
	 * 
	 * @return com.ems.model.EnergyConsumption
	 */
	@SuppressWarnings("unchecked")
	public Date loadLatestEnergyConsumptionDate(String mac , String emTimeZone) {

		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		Date latestTimeStamp = null;
		try {
			// treats capture_at as em Time Zone and then convert it to utc . Rather than directly treating it as UTC. 
			String queryForMaxTimeStamp = "select max(capture_at)  AT TIME ZONE '"+ emTimeZone +"' from energy_consumption";
			latestTimeStamp = jdbcTemplate.queryForObject(queryForMaxTimeStamp,
					Date.class);
		} catch (Exception ex) {

			logger.error(
					"error while getting Last energy consumption date for mac :- "
							+ mac, ex);
		}
		return latestTimeStamp;
	}

	/**
	 * load first EnergyConsumption details .
	 * 
	 * @param mac
	 * 
	 * @return com.ems.model.EnergyConsumption
	 */
	@SuppressWarnings("unchecked")
	public Date loadFirstEnergyConsumptionDate(String mac , String emTimeZone) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		Date firstTimeStamp = null;
		try {
			// treats capture_at as em Time Zone and then convert it to utc . Rather than directly treating it as UTC. 
			String queryForMaxTimeStamp = "select min(capture_at)  AT TIME ZONE '"+ emTimeZone +"' from energy_consumption";
			firstTimeStamp = jdbcTemplate.queryForObject(queryForMaxTimeStamp,
					Date.class);
		} catch (Exception ex) {

			logger.error(
					"error while getting first energy consumption date for mac :- "
							+ mac, ex);
		}
		return firstTimeStamp;
	}

}
