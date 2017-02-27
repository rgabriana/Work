package com.communicator.dao;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communicator.manager.CloudManager;
import com.communicator.manager.ServerInfoManager;



@Repository("energySyncUpDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EnergySyncUpDao  {


	@Resource
	ServerInfoManager serverInfoManager;
	
	@Resource
	CloudManager cloudManager ;
	
	@Resource
	JdbcTemplate jdbcTemplate;
	
	static final Logger logger = Logger.getLogger(EnergySyncUpDao.class.getName());
	
	public ByteArrayOutputStream getBulkData(String tableName , Long lastDataId , Long limit   ) 
	{
		Connection connection = null;
		ByteArrayOutputStream baos_energy = new ByteArrayOutputStream();
		ZipOutputStream zipDataOut = new ZipOutputStream(baos_energy);
		
		try {
			
			 
			connection = jdbcTemplate.getDataSource().getConnection() ;
			zipDataOut.putNextEntry(new ZipEntry("energy_data"));
			
			CopyManager cm = new CopyManager((BaseConnection) connection);
			cm.copyOut("COPY (SELECT *"
				       + " from "+tableName+" where id < " + lastDataId + " order by id desc limit " +  limit + " ) "
				       + "TO STDOUT with delimiter '~'" , zipDataOut);
			
			zipDataOut.closeEntry();
			logger.info("Compressed energy data size = " + baos_energy.toByteArray().length);
			
		}catch (SQLException e) {
		logger.error(e.toString(), e);
		
	} catch (Exception e) {
		logger.error( e.toString(), e);
		
	}
	finally {
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error( e.toString(), e);
				
			}
		}
	}
		return baos_energy ;
	}
	
	

	public Boolean isTabelEmpty(String tableName)
{
	Boolean isEmpty = true ;
	String result = null ;
	String sql = "SELECT count(*) FROM (SELECT 1 FROM "+tableName+" LIMIT 1) AS t" ;
	SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);

	if (rs.next()) {
		result = rs.getString(1);
		}
	int count = Integer.valueOf(result) ;
	if(count==1)
	{
		isEmpty = false ;
	}
	return isEmpty ;
}
}
