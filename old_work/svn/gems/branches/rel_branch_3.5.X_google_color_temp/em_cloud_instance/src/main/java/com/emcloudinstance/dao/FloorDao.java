package com.emcloudinstance.dao;

import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("floorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorDao extends AbstractJdbcDao {

	static final Logger logger = Logger.getLogger(FloorDao.class.getName());

	public List getAllFloorsOfCompany(String mac) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		List<BigInteger> result = null;
		try {
			String hsql = "select f.id from floor f, building b, campus  c "
					+ " where f.building_id = b.id and b.campus_id = c.id order by f.id";
			result = jdbcTemplate.queryForList(hsql, BigInteger.class);
		} catch (Exception e) {
			logger.error("Error while getting floor list for mac :- " + mac, e);
		}
		return result;
	}

}
