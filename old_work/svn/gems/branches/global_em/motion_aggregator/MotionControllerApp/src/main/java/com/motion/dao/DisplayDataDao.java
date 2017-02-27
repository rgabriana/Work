package com.motion.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("displayDataDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DisplayDataDao {
	public static final Logger logger = Logger.getLogger(DisplayDataDao.class
			.getName());
	@Resource
	JdbcTemplate jdbcTemplate;

	@Resource(name="displayDataIncrementer")
	PostgreSQLSequenceMaxValueIncrementer displayDataIncrementer;

	public void saveDisplayData(String message) {
		
		try {
			String query = "insert into display_data (id , message) values(?,?)";
			jdbcTemplate.update(query,
					new Object[] { displayDataIncrementer.nextLongValue(),message });
		} catch (Exception e) {
			e.printStackTrace() ;
			logger.error(e.getMessage());
		}
	}
}
