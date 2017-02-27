package com.motion.dao;

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
	
	public Integer getUdpPort() {
		String status = "0";
		
		String query = "select value from system_configuration where name = 'motion.udp.port'";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getString(1);
			}
		return Integer.parseInt(status);
	}
}
