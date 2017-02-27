package com.communicator.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


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
	public void addWalLogTriggers()
	{	
		SqlRowSet rs = jdbcTemplate.queryForRowSet("select addTriggers()");
		
	}
	public void removeWalLogTriggers()
	{	
		SqlRowSet rs = jdbcTemplate.queryForRowSet("select removeAllTriggers()");
		
	}

	public boolean doBeforeMigrationcleanUp() {
	Boolean result = false ;
	try{
	logger.info("Clean up of EM started to make it Migration Ready");
	removeWalLogTriggers() ;
	this.jdbcTemplate.execute("delete from wal_logs");
	this.jdbcTemplate.execute("alter SEQUENCE wal_logs_seq restart with 1");
	result=true ;
	logger.info("Clean Done!!");
	}catch(Exception ex)
	{
		logger.error(ex.getMessage());
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
	public Long getLastIdBeforeTriggers(String tableName)
	{
		Long id =0l ;
		String walId =null ;
		String normalId = null ;
		if(!energySyncUpDao.isTabelEmpty(tableName)){
				//normal id
				SqlRowSet rs = this.jdbcTemplate.queryForRowSet("select max(id) from " +tableName);
				if (rs.next()) {
						normalId = rs.getString(1);
						}
				//wal_log id
				 rs = this.jdbcTemplate.queryForRowSet("select min(record_id) from wal_logs where action='INSERT' and table_name='"+tableName+"'");
				if (rs.next()) {
					walId = rs.getString(1);
					}
				// get the largest id from the two	
				if(walId==null)
				{
					id = Long.valueOf(normalId) ;
					//This is needed because bulk copy query start from min and end on one less than the last. (min >= id and id < max).
					id= id + 1l ;
					
				}else
				{
					id=Long.valueOf(walId) ;
					
				}
				
			}
		return id ;
	}
	
}
