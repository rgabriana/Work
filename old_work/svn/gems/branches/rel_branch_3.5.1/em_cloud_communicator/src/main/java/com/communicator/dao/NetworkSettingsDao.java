package com.communicator.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("networkSettingsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class NetworkSettingsDao {

	static final Logger logger = Logger.getLogger(NetworkSettingsDao.class
			.getName());

	@Resource
	JdbcTemplate jdbcTemplate;

	public String getInterfaceName(String name) {
		final String query = "select interface_name from network_interface_mapping n, network_types nt, network_settings ns where n.network_type_id=nt.id and n.network_settings_id=ns.id and nt.name='" + name+ "'";
		final SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
		if (rs.next()) {
			return rs.getString(1).trim();
		}
		return null;
	}
	
	public String getCorporateInterfaceName() {
			return getInterfaceName("Corporate");
	}
}
