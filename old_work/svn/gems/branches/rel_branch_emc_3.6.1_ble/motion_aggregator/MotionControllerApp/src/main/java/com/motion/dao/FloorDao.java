package com.motion.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("floorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorDao {

	@Resource 
	JdbcTemplate jdbcTemplate ;
	

	@Resource
	PostgreSQLSequenceMaxValueIncrementer floorIncrementer;
	
	public static final Logger logger = Logger.getLogger(FloorDao.class
			.getName());

	public void loadFloorFromConfig(String floorDetails) {
			String floors[] = floorDetails.split("\\|");
			for( int i = 0 ; i < floors.length ; i++)
			{
				String floorData[] = floors[i].split(",") ;
				String isExist = getFloorIdOnName(floorData[0]);
				String query = null ;
				if(isExist!=null) 
				{
					 query = "update floor set name=? , width=? , height= ? where id = ?";
					 jdbcTemplate.update(
								query,
								new Object[] { floorData[0],
										Integer.parseInt(floorData[1]),
										Integer.parseInt(floorData[2]),
										Integer.parseInt(isExist) });
				}else
				{
					query = "insert into floor (id ,name , width , height ) values(?,?,?,?)";
					jdbcTemplate.update(
							query,
							new Object[] { floorIncrementer.nextLongValue(),
									floorData[0],
									Integer.parseInt(floorData[1]),
									Integer.parseInt(floorData[2])});
				}
				
				
			}
		
	}

	private String getFloorIdOnName(String name) {
			Integer status = null;
			
			String query = "select id from floor where name = '" +name+"'" ;
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
}
