package com.communicator.dao;

import java.io.File;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.CommonStateUtils;
import com.communicator.util.CommunicatorConstant;


@Repository("systemConfigDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemConfigDao{
	static final Logger logger = Logger.getLogger(SystemConfigDao.class
			.getName());
	@Resource
	JdbcTemplate jdbcTemplate;
	
	@Resource
	EnergySyncUpDao energySyncUpDao;
	
	public String checkCloudConnectivity() {
		String status = "0";
		
		String query = "select value from system_configuration where name = 'cloud.communicate.type'";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getString(1);
			}
		return status;
	}
	public String isCloudEnabled() {
		String status = "0";
		
		String query = "select value from system_configuration where name = 'enable.cloud.communication'";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getString(1);
			}
		return status;
	}
	public void updateCloudCommunicateType(String str)
	{
		try{
		String query = "update system_configuration set value= '"+ str +"' where name='cloud.communicate.type'";
		jdbcTemplate.update(query) ;
		}catch(Exception e)
		{
			logger.error(e.getMessage() ,e) ;
		}
	}
	
	public void emptyMaxIdValues() {
		updateSysConfigValue("energy_consumption.sync.max.id", "");
		updateSysConfigValue("energy_consumption_hourly.sync.max.id", "");
		updateSysConfigValue("energy_consumption_daily.sync.max.id", "");
		updateSysConfigValue("em_motion_bits.sync.max.id", "");
	}
	
	public void setMaxIdValues() {
		updateSysConfigValue("energy_consumption.sync.max.id", getMaxIdToBeSynced(CommonStateUtils.energyTableName).toString());
		updateSysConfigValue("energy_consumption_hourly.sync.max.id", getMaxIdToBeSynced(CommonStateUtils.energyHourlyTableName).toString());
		updateSysConfigValue("energy_consumption_daily.sync.max.id", getMaxIdToBeSynced(CommonStateUtils.energyDailyTableName).toString());
		updateSysConfigValue("em_motion_bits.sync.max.id", getMaxIdToBeSynced(CommonStateUtils.motionBitTableName).toString());
	}
	
	public void addWalLogTriggers() {	
		jdbcTemplate.queryForRowSet("select addTriggers()");
	}
	
	public void removeWalLogTriggers() {	
		jdbcTemplate.queryForRowSet("select removeAllTriggers()");
	}

	public boolean doBeforeMigrationcleanUp() {
	Boolean result = false ;
	try{
	logger.info("Clean up of EM started to make it Migration Ready");
	removeWalLogTriggers() ;
	emptyMaxIdValues();
	this.jdbcTemplate.execute("truncate wal_logs");
	this.jdbcTemplate.execute("truncate sync_tasks");
	this.jdbcTemplate.execute("alter SEQUENCE wal_logs_seq restart with 1");
	FileUtils.cleanDirectory(new File(CommunicatorConstant.syncDataDir)); 
	result=true ;
	logger.info("Clean Done!!");
	}catch(Exception ex)
	{
		logger.error(ex.getMessage(), ex);
		result=false;
	}
	return result ;
}
	
	public Long getTableLocalMinId(String tableName) {
		Long status = 0l ;
		if(!energySyncUpDao.isTabelEmpty(tableName)){
		SqlRowSet rs = this.jdbcTemplate.queryForRowSet("select min(id) from " +tableName);

		if (rs.next()) {
				status = Long.valueOf(rs.getString(1));
			}
		}
		return status;
	}
	
	public Long getMaxIdToBeSynced(String tableName) {
		Long id =0l ;
		String maxId =null ;
		if(!energySyncUpDao.isTabelEmpty(tableName)){
				SqlRowSet rs = this.jdbcTemplate.queryForRowSet("select max(id) from "+ tableName + " where id < (select coalesce(min(record_id), 99999999999999999999999999999999999999999999999999999999) from wal_logs where table_name = '" + tableName + "' and action = 'INSERT')");
				if (rs.next()) {
					maxId = rs.getString(1);
					if(maxId != null && !"".equals(maxId)) {
						id = Long.parseLong(maxId);
					}
				}
			}
		return id ;
	}
	
	
	public Long getLastIdBeforeTriggers(String tableName)
	{
		Long id =0l ;
		String maxId =null ;
		
		maxId = getSysConfigValue(tableName.toLowerCase()+ ".sync.max.id");
		if(maxId != null && !"".equals(maxId)) {
			id = Long.parseLong(maxId);
		}
		return id ;
	}
	
	
	public Long getWalMinRecordIdForTable(String tableName)
	{
		Long id =0l ;
		String walId =null ;
		if(!energySyncUpDao.isTabelEmpty(tableName)){
				SqlRowSet rs = this.jdbcTemplate.queryForRowSet("select min(record_id) from wal_logs where action='INSERT' and table_name='"+tableName+"'");
				if (rs.next()) {
						walId = rs.getString(1);
						if(walId!=null)
						{
							id=Long.valueOf(walId) ;
						}
					}		
			}
		return id ;
	}
	
	public String getSysConfigValue(String name) {
		String status = "0";
		
		String query = "select value from system_configuration where name = '" + name + "'";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getString(1);
			}
		return status;
	}
	
	public void updateSysConfigValue(String name, String value) {
		String query = null;
		if (value == null) {
			query = "update system_configuration set value = null where name = '" + name + "'";
		} else {
			query = "update system_configuration set value = '" + value + "' where name = '" + name + "'";
		}
		jdbcTemplate.execute(query);
	}
}
