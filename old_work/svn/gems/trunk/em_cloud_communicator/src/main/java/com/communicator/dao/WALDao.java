package com.communicator.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Repository("walDao")
@Transactional(propagation = Propagation.REQUIRED)
public class WALDao {
	
	@Resource
	JdbcTemplate jdbcTemplate;
	
	public void fillWALChangesAndWalId(Map<String, Object> map, Long lastWalLogDataId){
		Long newWalLogDataId = null;
		
		Long walThrottle = 50001L;
		
		newWalLogDataId = jdbcTemplate.queryForLong("select coalesce(max(wal.id)," + lastWalLogDataId +  ") from wal_logs wal " +
        		"where wal.id < (select coalesce(min(id),9223372036854775806) from wal_logs where id > " + lastWalLogDataId +
        		" and action = 'UPGRADE') and wal.id < (select coalesce(min(min_wal.id), " + lastWalLogDataId + ") + " +
        		walThrottle + " from wal_logs min_wal where min_wal.id > " + lastWalLogDataId + ")");

		jdbcTemplate.execute("delete from wal_logs where id < "
				+ (lastWalLogDataId - 100));
		
		String walLogsQuery =  "SELECT sql_statement FROM wal_logs WHERE id > " + lastWalLogDataId + " and id <= " 
		                       + newWalLogDataId +" and action in ('UPDATE', 'DELETE', 'INSERT') order by id";

		SqlRowSet rs = jdbcTemplate.queryForRowSet(walLogsQuery);
		List<String> statements = new ArrayList<String>();
		while (rs.next()) {
			statements.add(rs.getString("sql_statement"));
		}
		
		map.put("data", statements);
		map.put("maxWalLogDataId", newWalLogDataId);
		
		SqlRowSet nextAction = jdbcTemplate.queryForRowSet("select id, action, table_name from wal_logs where id > " + lastWalLogDataId + " order by id limit 1");
		if(nextAction.next()) {
			map.put("nextAction", nextAction.getString("action"));
			if(map.get("nextAction") != null && "UPGRADE".equals(map.get("nextAction").toString())) {
				map.put("upgradeWalId", nextAction.getLong("id"));
				map.put("upgradeFilePath", nextAction.getString("table_name"));
			}
		}
	}
	
	public Long countWalLogs() {
		return jdbcTemplate.queryForLong("select coalesce(count(id),0) from wal_logs");
	}
	
	public void deleteWalLogs(Long walId) {
		jdbcTemplate.execute("delete from wal_logs where id <= " + walId);
	}

	public void deleteInventory(long lastWalSyncId) {
		jdbcTemplate.execute("delete from wal_logs where id > " + lastWalSyncId + 
				"and table_name not in('energy_consumption','energy_consumption_hourly','energy_consumption_daily','em_motion_bits')");
	}

}
