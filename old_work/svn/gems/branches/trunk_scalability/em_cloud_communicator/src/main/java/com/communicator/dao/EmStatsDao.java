package com.communicator.dao;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.CloudParamType;
import com.communication.utils.NameValue;


@Repository("emStatsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStatsDao {
	
	@Resource
	JdbcTemplate jdbcTemplate;
	
	public void fillCallHomeData(ArrayList<NameValue> list){
		
        String query = "select id, capture_at, "
				+ "active_thread_count, gc_count, gc_time, "
				+ "heap_used, non_heap_used, sys_load, cpu_percentage "
				+ "from em_stats order by capture_at desc limit 1";
	
	   SqlRowSet rs = 	jdbcTemplate.queryForRowSet(query);				
		if (rs.next()) {
			
			list.add(new NameValue(CloudParamType.StatsId, new Long(rs.getLong("id")).toString()));
			list.add(new NameValue(CloudParamType.StatsCaptureAt, new Long(rs.getTimestamp("capture_at").getTime()).toString()));
			list.add(new NameValue(CloudParamType.StatsActiveThreadCount, new Integer(rs.getInt("active_thread_count")).toString()));
			list.add(new NameValue(CloudParamType.StatsGcCount, new Long(rs.getLong("gc_count")).toString()));
			list.add(new NameValue(CloudParamType.StatsGcTime, new Long(rs.getLong("gc_time")).toString()));
			list.add(new NameValue(CloudParamType.StatsHeadUsed, rs.getBigDecimal("heap_used").toString()));
			list.add(new NameValue(CloudParamType.StatsNonHeapUsed, rs.getBigDecimal("non_heap_used").toString()));
			list.add(new NameValue(CloudParamType.StatsSysLoad, rs.getBigDecimal("sys_load").toString()));
			list.add(new NameValue(CloudParamType.StatsCpuPercentage, rs.getBigDecimal("cpu_percentage").toString()));
			
		}
			
	}



}
