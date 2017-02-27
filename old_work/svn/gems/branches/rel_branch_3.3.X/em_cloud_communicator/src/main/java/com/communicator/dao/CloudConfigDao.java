package com.communicator.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("cloudConfigDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CloudConfigDao {
	static final Logger logger = Logger.getLogger(CloudConfigDao.class
			.getName());
	
	@Resource
	JdbcTemplate jdbcTemplate;
	
	
	public String getCloudConfigValue(String name) {
		String status = "0";
		
		String query = "select val from cloud_config where name = '" + name + "'";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getString(1);
			}
		return status;
	}
	
	public int updateCloudConfig(String name, String val) {
		int out = 1;
		try{
		String query = "update cloud_config set val= '" + val + "' where name='" + name + "'";
		jdbcTemplate.update(query) ;
		}catch(Exception e)
		{
			logger.error(e.getMessage() ,e) ;
			out = 0;
		}
		return out;
	}

}
