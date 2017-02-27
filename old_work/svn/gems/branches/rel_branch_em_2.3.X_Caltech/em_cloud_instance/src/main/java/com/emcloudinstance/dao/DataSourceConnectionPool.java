package com.emcloudinstance.dao;

import java.util.HashMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;


import com.emcloudinstance.util.DatabaseUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;

@Component("dataSourceConnectionPool")
public class DataSourceConnectionPool {

	@Resource
	DatabaseUtil databaseUtil;

	private HashMap<String, ComboPooledDataSource> dataSourceMap = new HashMap<String, ComboPooledDataSource>();

	public DataSource getDataSource(String mac) {
		ComboPooledDataSource ds = dataSourceMap.get(mac);

		try {
			if (ds == null) {
				ds = new ComboPooledDataSource();
				ds.setDriverClass("org.postgresql.Driver");
				// loads the jdbc driver
				ds.setJdbcUrl("jdbc:postgresql://localhost:" + databaseUtil.port+"/"
						+ databaseUtil.getDbNameByMac(mac));
				ds.setUser("postgres");
				ds.setPassword("postgres");
				ds.setMaxStatements(10);
				ds.setMinPoolSize(2);
				ds.setAcquireIncrement(1);
				ds.setMaxPoolSize(10);
				dataSourceMap.put(mac, ds);
			}
		} catch (Exception e) {

		}

		return ds;
	}
}
