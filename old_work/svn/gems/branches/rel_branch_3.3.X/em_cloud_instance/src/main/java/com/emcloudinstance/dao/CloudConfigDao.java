package com.emcloudinstance.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("cloudConfigDao")
public class CloudConfigDao extends AbstractJdbcDao{
	
	public String getCloudConfig(String mac, String configName){
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);		
		String value = jdbcTemplate.queryForObject("select val from cloud_config where name = ?",new Object[]{configName}, String.class);
        return value;
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
