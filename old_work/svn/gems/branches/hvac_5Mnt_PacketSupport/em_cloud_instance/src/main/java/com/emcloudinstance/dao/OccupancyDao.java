package com.emcloudinstance.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.util.DateUtil;
import com.emcloudinstance.vo.OccSyncVo;

@Repository("occupancyReportDao")
@Transactional(propagation = Propagation.REQUIRED)
public class OccupancyDao extends AbstractJdbcDao {

    private static final Logger logger = Logger.getLogger(OccupancyDao.class.getName());

    /**
     * Loads the data for the last 30 minutes interval.
     * 
     * @param oFDate
     * @param mac
     * @param emTimeZone
     * @return
     */
    public List<OccSyncVo> load30MinOccupancyData(Date oFDate, String mac, String emTimeZone, List<Long> emFacIds) {

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < emFacIds.size(); i++) {
            if (i == (emFacIds.size() - 1)) {
                builder.append(String.valueOf(emFacIds.get(i)));
            } else {
                builder.append(String.valueOf(emFacIds.get(i)));
                builder.append(",");
            }
        }
        List<OccSyncVo> list = new ArrayList<OccSyncVo>();
        final Map<String, OccSyncVo> listMap = new HashMap<String, OccSyncVo>();
        final JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
        try {
            final Date maxDataAvailableDate = loadLatestDataDate(mac, emTimeZone);
            if (maxDataAvailableDate != null && oFDate != null) {
                final Date minDate = DateUtils.addMinutes(DateUtil.truncateToPrevSlotsXmin(oFDate, 30), -30);
                // final Date maxDate = DateUtil.truncateToPrevSlotsXmin(maxDataAvailableDate, 30);
                final Date maxDate = DateUtils.addMinutes(minDate, 30);
                // Get motion bits against the fixtures
                final String queryForMotionBits = "select date_trunc('hour', ec.capture_at + interval '29 min') + (date_part('minute', ec.capture_at + interval '29 min'))::int / 30 * interval '30 min' as Time,"
                        + " d.floor_id, f.group_id, sum(length(replace(cast(ec.motion_bits::bit(64) as text), '0', ''))) as occupied_bits, "
                        + " count(ec.fixture_id) * 60 as total_bits, count(distinct(ec.fixture_id)) as reporting_fixtures, "
                        + "(SELECT count(f1.id) from fixture f1, groups g1, device d1 "
                        + " where f1.id=d1.id and f1.group_id=g1.id and f1.group_id=f.group_id and d1.floor_id=d.floor_id ) as installed_sensors "
                        + "from energy_consumption ec join fixture f on f.id=ec.fixture_id join device d on d.id=f.id  where "
                        + " ec.capture_at > '"
                        + DateUtil.formatDate(minDate, "yyyy-MM-dd HH:mm")
                        + "' AT TIME ZONE '"
                        + emTimeZone
                        + "' "
                        + " AND "
                        + "ec.capture_at <= '"
                        + DateUtil.formatDate(maxDate, "yyyy-MM-dd HH:mm")
                        + " ' AT TIME ZONE '"
                        + emTimeZone
                        + "'   and d.floor_id in ("
                        + builder.toString()
                        + ") "
                        + "and ec.zero_bucket = 0   group by Time, d.floor_id, f.group_id order by Time;";

                logger.debug("Query for motionbits:" + queryForMotionBits);
                list = jdbcTemplate.query(queryForMotionBits, new RowMapper<OccSyncVo>() {
                    @Override
                    public OccSyncVo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        final int floorId = rs.getInt("floor_id");
                        final int groupId = rs.getInt("group_id");
                        final int occupied_bits = rs.getInt("occupied_bits");
                        final int total_bits = rs.getInt("total_bits");
                        final int reporting_fixtures = rs.getInt("reporting_fixtures");
                        final int installed_sensors = rs.getInt("installed_sensors");
                        // final Timestamp ts = rs.getTimestamp("time");
                        final OccSyncVo vo = new OccSyncVo();
                        vo.setCaptureAt(maxDate);
                        vo.setLevelId(floorId);
                        vo.setNoOf1bits(occupied_bits);
                        vo.setTotalBits(total_bits);
                        vo.setNoOfSensors(reporting_fixtures);
                        vo.setGroupId(groupId);
                        vo.setTotalSensors(installed_sensors);
                        return vo;
                    }

                });
            }
            return list;

        } catch (Exception e) {
            logger.error("Exception while loading 30 min data", e);
        }
        return list;
    }

    private abstract class CustomRowMapper<T> implements RowMapper<T> {
        public CustomRowMapper() {
        }
    }

    @SuppressWarnings("unchecked")
    public Date loadLatestDataDate(String mac, String emTimeZone) {

        JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
        Date latestTimeStamp = null;
        try {
            // treats capture_at as em Time Zone and then convert it to utc . Rather than directly treating it as UTC.
            String queryForMaxTimeStamp = "select max(capture_at)  AT TIME ZONE '" + emTimeZone
                    + "' from energy_consumption";
            latestTimeStamp = jdbcTemplate.queryForObject(queryForMaxTimeStamp, Date.class);
        } catch (Exception ex) {

            logger.error("error while getting Last occupancy date for mac :- " + mac, ex);
        }
        return latestTimeStamp;
    }

    @SuppressWarnings("unchecked")
    public Date loadFirstDataDate(String mac, String emTimeZone) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
        Date firstTimeStamp = null;
        try {
            // treats capture_at as em Time Zone and then convert it to utc . Rather than directly treating it as UTC.
            String queryForMaxTimeStamp = "select min(capture_at)  AT TIME ZONE '" + emTimeZone
                    + "' from energy_consumption";
            firstTimeStamp = jdbcTemplate.queryForObject(queryForMaxTimeStamp, Date.class);
        } catch (Exception ex) {

            logger.error("error while getting first occupancy date for mac :- " + mac, ex);
        }
        return firstTimeStamp;
    }
}