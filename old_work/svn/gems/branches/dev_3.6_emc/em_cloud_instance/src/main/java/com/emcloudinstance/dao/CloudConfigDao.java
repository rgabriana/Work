package com.emcloudinstance.dao;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("cloudConfigDao")
public class CloudConfigDao extends AbstractJdbcDao{
	
	Logger logger = Logger.getLogger(CloudConfigDao.class.getName());
	
	public String getCloudConfig(String mac, String configName){
		try {
			JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);		
			String value = jdbcTemplate.queryForObject("select val from cloud_config where name = ?",new Object[]{configName}, String.class);
	        return value;
		}
		catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.error("EM:" + mac + " ::" + e.getMessage(),e);
			}
			else {
				logger.error("EM:" + mac + " ::" + e.getMessage());
			}
		}
		return null;
	}
	
	public String getCloudConfig(String mac, String configName, JdbcTemplate jdbcTemplate){
		String value = jdbcTemplate.queryForObject("select val from cloud_config where name = ?",new Object[]{configName}, String.class);
        return value;
	}
	
	public int updateCloudConfig(String mac, String configName, String value){
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);	
		return jdbcTemplate.update("update cloud_config set val = ? where name = ?",value, configName);
	}
	
	public int updateCloudConfig(String mac, String configName, String value, JdbcTemplate jdbcTemplate){
		return jdbcTemplate.update("update cloud_config set val = ? where name = ?",value, configName);
	}

}
