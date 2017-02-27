package com.communicator.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communicator.type.SyncTaskStatus;
import com.communicator.util.SyncTasks;

@Repository("syncTasksDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SyncTasksDao {
	
	@Resource
	JdbcTemplate jdbcTemplate;
	
	public boolean isDataPresentInQueue() {
		Long id = jdbcTemplate.queryForLong("select coalesce(min(last_wal_id), -1000) from sync_tasks " +
					"where status != '" + SyncTaskStatus.Success + "' and status != '" + SyncTaskStatus.InProgress + "'");
		if(id.compareTo(-1000L) == 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public SyncTasks getSyncTask(Long lastSyncWalId) {
		SqlRowSet nextAction = jdbcTemplate.queryForRowSet("select last_wal_id, file_name, creation_time, action_type, checksum, status, failed_attemps from sync_tasks where last_wal_id > " + lastSyncWalId + " order by last_wal_id limit 1");
		SyncTasks syncTasks = null;
		if(nextAction.next()) {
			syncTasks = new SyncTasks();
			syncTasks.setLastWalId(nextAction.getLong("last_wal_id"));
			syncTasks.setFilename(nextAction.getString("file_name"));
			syncTasks.setCreationTime(nextAction.getTimestamp("creation_time"));
			syncTasks.setAction(nextAction.getString("action_type"));
			syncTasks.setChecksum(nextAction.getString("checksum"));
			syncTasks.setFailedAttemps(nextAction.getInt("failed_attemps"));
			syncTasks.setStatus(SyncTaskStatus.valueOf(nextAction.getString("status")));
		}
		return syncTasks;
	}
	
	public void updateSyncTaskStatus(Long lastWalId, SyncTaskStatus status) {
		jdbcTemplate.execute("update sync_tasks set status = '" + status + "' where last_wal_id = " + lastWalId);
	}
	
	public void increaseFailedAttempts(Long lastWalId) {
		jdbcTemplate.execute("update sync_tasks set failed_attemps = failed_attemps + 1 where last_wal_id = " + lastWalId);
	}
	
	public void createSyncTask(String actionType, String filename, SyncTaskStatus status, String checksum, Long lastWalId) {
		jdbcTemplate.execute("insert into sync_tasks (last_wal_id, file_name, creation_time, action_type, checksum, status) values ("
								+ lastWalId + ", "
								+ "'" + filename + "', "
								+ "current_timestamp, "
								+ "'" + actionType + "',"
								+ "'" + checksum + "',"
								+ "'" + status + "'"
								+ ")"
								);
	}
	
	public Long getMaxWalLogId() {
		return jdbcTemplate.queryForLong("select coalesce(max(last_wal_id), -99) from sync_tasks");
	}
	
	
	public SyncTasks getOldestSyncTaskBeforeDate(int durationInDays) {
		SqlRowSet nextAction = jdbcTemplate.queryForRowSet("select last_wal_id, file_name, creation_time, action_type, checksum, status, failed_attemps " +
															"from sync_tasks " +
															"where status = '" + SyncTaskStatus.Success.getName() + "' and " +
																"creation_time < current_timestamp - interval '" + durationInDays + " day' " +
																" order by last_wal_id limit 1");
		SyncTasks syncTasks = null;
		if(nextAction.next()) {
			syncTasks = new SyncTasks();
			syncTasks.setLastWalId(nextAction.getLong("last_wal_id"));
			syncTasks.setFilename(nextAction.getString("file_name"));
			syncTasks.setCreationTime(nextAction.getTimestamp("creation_time"));
			syncTasks.setAction(nextAction.getString("action_type"));
			syncTasks.setChecksum(nextAction.getString("checksum"));
			syncTasks.setFailedAttemps(nextAction.getInt("failed_attemps"));
			syncTasks.setStatus(SyncTaskStatus.valueOf(nextAction.getString("status")));
		}
		return syncTasks;
	}
	
	public void deleteSyncTasksByLastWalId(Long lastWalId) {
		jdbcTemplate.execute("delete from sync_tasks where last_wal_id = " + lastWalId);
	}
	
	public List<String> getQueuedEntries(){
		
		List<String> queuedList = new ArrayList<String>();
		SqlRowSet nextAction = jdbcTemplate.queryForRowSet("select file_name from sync_tasks where status in ('" + SyncTaskStatus.Failed.getName() + "','" + SyncTaskStatus.Queued.getName() + "') order by last_wal_id");
		
		while(nextAction.next()){
			queuedList.add(nextAction.getString("file_name"));
		}
		return queuedList;
		
	}

}
