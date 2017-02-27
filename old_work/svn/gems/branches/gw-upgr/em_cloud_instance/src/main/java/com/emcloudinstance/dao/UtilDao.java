package com.emcloudinstance.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.communication.utils.CommonStateUtils;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.DatabaseUtil;

@Repository("utilDao")
public class UtilDao extends AbstractJdbcDao{
	static final Logger logger = Logger.getLogger("UtilDao");
	@Resource
	DatabaseUtil databaseUtil ;
	@Resource
	CommonUtils commonUtils ;
	/**
	 * Please control the size of the batch from outside. this function will consider all
	 * the strings as part of one batch
	 * @param batch
	 * @return
	 */
	public int[] updateBatch(String mac, String[] batch){
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		return jdbcTemplate.batchUpdate(batch);
	}
	
	public int[] updateBatch(String mac, String[] batch, JdbcTemplate jdbcTemplate){
		return jdbcTemplate.batchUpdate(batch);
	}
	
	public boolean synchTableData(String mac, String tableName,
			InputStream otherDataStream, Boolean remigration) {	
		
		boolean status = false;
		String exceptionString = null ;
		ZipInputStream zipEnergyIn = null;
		Connection  connection = null ; 
		String dataIdSynced = null;
		try{
		this.getJdbcTemplate(mac);		
		zipEnergyIn = new ZipInputStream(otherDataStream);
		zipEnergyIn.getNextEntry();
		/*Connection con = jdbcTemplate.getDataSource().getConnection();
		CopyManager cm1 = new CopyManager((BaseConnection) jdbcTemplate.getNativeJdbcExtractor().getNativeConnection(con));*/
		
		String dbName = databaseUtil.getDbNameByMac(mac);
		if (!dbName.isEmpty()) {
			String conString = "jdbc:postgresql://localhost:"
					+ databaseUtil.port + "/" + dbName
					+ "?characterEncoding=utf-8";
			connection = DriverManager.getConnection(conString, "postgres",
					"postgres");
				CopyManager cm1 = new CopyManager(
						(BaseConnection) connection);
		cm1.copyIn("COPY " + tableName 	+ " FROM STDIN WITH DELIMITER '~'", zipEnergyIn);
		status = true;
		
		if(remigration) {
			dataIdSynced = String.valueOf(getLastMaxTableDataSynched(mac,tableName));
		}
		else {
			dataIdSynced = String.valueOf(getLastMinTableDataSynched(mac,tableName));
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("Em With Mac " + mac + " transfered its data to table " + tableName + " till " + dataIdSynced) ;
		}
		}}catch(Exception e){
			exceptionString = e.getMessage() ;
			e.printStackTrace();
		}finally{
			try {
				connection.close() ;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			if(zipEnergyIn != null){
				try {
					zipEnergyIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// for logging pupose on cloud.
		try{
			if(status)
			{
				commonUtils.setReplicaMigrationFlagLogOnCloud(mac,CommonStateUtils.getStateInProgressAccordingToTableName(tableName, remigration) , dataIdSynced, new Date() ) ;
			}else
			{
				commonUtils.setReplicaMigrationFlagLogOnCloud(mac,CommonStateUtils.getStateInProgressAccordingToTableName(tableName, remigration) , exceptionString, null);
			}
		}catch(Exception e)
		{
			e.printStackTrace() ;
		}
		return status;
	}

	@SuppressWarnings("deprecation")
	public Long getLastMinTableDataSynched(String mac, String tableName) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		String query = "select min(id) from " + tableName;
		Long minId = jdbcTemplate.queryForLong(query);
		if(minId==null)
		{
			minId= -1l;
		}
		return minId;
	}
	
	@SuppressWarnings("deprecation")
	public Long getLastMaxTableDataSynched(String mac, String tableName) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		String query = "select max(id) from " + tableName;
		Long minId = jdbcTemplate.queryForLong(query);
		if(minId==null)
		{
			minId= 0l;
		}
		return minId;
	}

}
