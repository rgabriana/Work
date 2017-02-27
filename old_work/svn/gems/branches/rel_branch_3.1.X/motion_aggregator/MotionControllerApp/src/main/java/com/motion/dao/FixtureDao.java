package com.motion.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("fixtureDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureDao {
	public static final Logger logger = Logger.getLogger(FixtureDao.class
			.getName());
	@Resource 
	JdbcTemplate jdbcTemplate ;
	

	@Resource
	PostgreSQLSequenceMaxValueIncrementer fixtureIncrementer;
	
	public String getFixtureIdFromMac(String mac) {
		Integer status = null;
		
		String query = "select id from fixture where mac = '" +mac+"'" ;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getInt(1);
			}
		if(status!=null)
		{
		return Integer.toString(status);
		}
		return null ;
		
	}
	public String getFixtureYbyId(String fixture_id) {
		Integer status = null;
		
		String query = "select y from fixture where id = " +Integer.parseInt(fixture_id) ;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getInt(1);
			}
		return Integer.toString(status);
	}
	public String getFixtureXbyId(String fixture_id) {
		Integer status = null;
		
		String query = "select x from fixture where id = " +Integer.parseInt(fixture_id) ;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

		if (rs.next()) {
				status = rs.getInt(1);
			}
		return Integer.toString(status);
	}
	public void loadFixturesFromConfig(String fixtureDetails) {
		String fixtures[] = fixtureDetails.split("\\|");
		for( int i = 0 ; i < fixtures.length ; i++)
		{
			String currentfix = fixtures[i] ;
			String fixtureData[] = currentfix.split(",") ;
			String mac =fixtureData[0] ;
			String isExist = getFixtureIdFromMac(mac);
			String query = null ;
			if(isExist!=null) 
			{
				 query = "update fixture set mac=? , x=? , y = ? ,floor_id=? where id = ?";
				 jdbcTemplate.update(
							query,
							new Object[] { fixtureData[0],
									Integer.parseInt(fixtureData[1]),
									Integer.parseInt(fixtureData[2]),
									Integer.parseInt(fixtureData[3]),
									Integer.parseInt(isExist) });
			}else
			{
				query = "insert into fixture (id ,mac , x , y , floor_id ) values(?,?,?,?,?)";
				jdbcTemplate.update(
						query,
						new Object[] { fixtureIncrementer.nextLongValue(),
								fixtureData[0],
								Integer.parseInt(fixtureData[1]),
								Integer.parseInt(fixtureData[2]),
								Integer.parseInt(fixtureData[3])});
			}
			
			
		}
		
	}
}
