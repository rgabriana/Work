package com.emcloudinstance.dao;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.vo.FloorZbUpdate;

@Repository("floorZbUpdateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorZbUpdateDao extends AbstractJdbcDao {

	static final Logger logger = Logger.getLogger(FloorZbUpdateDao.class
			.getName());

	public void update(FloorZbUpdate fZb, String mac) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		try{
		jdbcTemplate
				.update("update floor_zbupdate set id = ? , floor_id = ? , start_time=? , end_time= ? , processed_state=?  where id = ?",
						fZb.getId(), fZb.getFloorId(), fZb.getStartTime(),
						fZb.getEndTime(), fZb.getProcessedState(), fZb.getId());
		}catch(Exception ex){
			logger.error("error while updating zb floor level entries for mac :- " + mac , ex);
		}
	}

	public List<FloorZbUpdate> loadAllUnProcessedFloorZbUpdate(String mac) {
		List<FloorZbUpdate> floorZbUpdates = null;
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		try {
			String queryForFloorZbUpdate = "select id as id , floor_id as floor_id , start_time as start_time , end_time as end_time , processed_state as processed_state from floor_zbupdate where processed_state =0 ";
			floorZbUpdates = jdbcTemplate.query(queryForFloorZbUpdate,
					new RowMapper<FloorZbUpdate>() {

						@Override
						public FloorZbUpdate mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							FloorZbUpdate floorZbUpdate = new FloorZbUpdate();
							floorZbUpdate.setId(rs.getLong("id"));
							floorZbUpdate.setFloorId(rs.getLong("floor_id"));
							floorZbUpdate.setStartTime(rs.getDate("start_time"));
							floorZbUpdate.setEndTime(rs.getDate("end_time"));
							floorZbUpdate.setProcessedState(rs
									.getLong("processed_state"));
							return floorZbUpdate;
						}

					});
		} catch (Exception ex) {
			logger.error(
					"error while getting all un processed floor level zb update for mac :- " + mac,
					ex);
		}

		return floorZbUpdates;
	}

	public Boolean isTableAvailable(String mac) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		DatabaseMetaData md;
		ResultSet rs;
		try {
			md = jdbcTemplate.getDataSource().getConnection().getMetaData();
			rs = md.getTables(null, null, "floor_zbupdate", null);
			if (rs.next()) {
				return true;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("checking if table exist failed for max :- " + mac, e);
		}

		return false;
	}

}
