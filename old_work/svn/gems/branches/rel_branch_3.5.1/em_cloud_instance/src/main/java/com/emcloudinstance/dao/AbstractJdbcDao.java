package com.emcloudinstance.dao;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractJdbcDao {
	
	@Resource
	JdbcConnectionTemplate jdbcConnectionTemplate;
	
	JdbcTemplate getJdbcTemplate(String mac){ 
	  return jdbcConnectionTemplate.getJdbcTemplate(mac);	
	}

}
