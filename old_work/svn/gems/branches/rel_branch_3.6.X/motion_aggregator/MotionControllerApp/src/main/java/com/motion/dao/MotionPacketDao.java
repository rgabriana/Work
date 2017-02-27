package com.motion.dao;

import java.util.HashMap;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("motionPacketDao")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionPacketDao {
	public static final Logger logger = Logger.getLogger(MotionPacketDao.class
			.getName());
	@Resource
	JdbcTemplate jdbcTemplate;

	@Resource
	PostgreSQLSequenceMaxValueIncrementer incrementer;

	public void saveMotionPacket(HashMap<String, String> data) {
		try {
			String query = "insert into motion_packets (id ,fixture_id , blob_id , local_x , local_y , global_x , global_y , capture_at) values(?,?,?,?,?,?,?,?)";
			jdbcTemplate.update(
					query,
					new Object[] { incrementer.nextLongValue(),
							Integer.parseInt(data.get("fixture_id")),
							Integer.parseInt(data.get("blob_id")),
							Integer.parseInt(data.get("local_x")),
							Integer.parseInt(data.get("local_y")),
							Integer.parseInt(data.get("global_x")),
							Integer.parseInt(data.get("global_y")),
							new java.util.Date() });
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
