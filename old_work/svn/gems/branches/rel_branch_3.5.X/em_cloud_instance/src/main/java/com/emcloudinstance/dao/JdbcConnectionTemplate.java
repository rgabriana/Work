package com.emcloudinstance.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import com.emcloudinstance.util.DatabaseUtil;

/**
 * The class helps in making JdbcTemplate. It also ensure that only one
 * jdbctemplate is returned in a thread local scope
 * @author lalit
 *
 */
@Component("jdbcConnectionTemplate")
public class JdbcConnectionTemplate {
	static final Logger logger = Logger.getLogger("EmCloudInstance");
	private static final ThreadLocal<JdbcTemplate> jdbcTemplateContext = new ThreadLocal<JdbcTemplate>();
	private static final ThreadLocal<DataSourceTransactionManager> transactionManagerContext = new ThreadLocal<DataSourceTransactionManager>();

	@Resource
	DatabaseUtil databaseUtil;

	public JdbcTemplate getJdbcTemplate(String mac) {
		
		if(jdbcTemplateContext.get() == null){
			long startTime = System.currentTimeMillis() ;
			createLocalTransactionContext(mac);
			logger.debug("Time Taken for obtaining JDBC Connection for mac "+ mac + " is :- " + (System.currentTimeMillis() - startTime) );
		}
		JdbcTemplate template = jdbcTemplateContext.get();
		logger.debug("update batch for mac = " + mac + " and db name " +  databaseUtil.getDbNameByMac(mac) + " and temp = " + template + " and ds = " + template.getDataSource().toString());
		return template;			
		
	}
	
	public void cleanup(){
		long startTime = System.currentTimeMillis() ;
		jdbcTemplateContext.remove();
		transactionManagerContext.remove();
		logger.debug("Time Taken for Tear Down is :- " + (System.currentTimeMillis() - startTime) );

	}
	
	public DataSourceTransactionManager getTransactionManager(String mac){
		if(JdbcConnectionTemplate.transactionManagerContext.get() == null){
			this.createLocalTransactionContext(mac);
		}
		return JdbcConnectionTemplate.transactionManagerContext.get();
	}
	
	private void createLocalTransactionContext(String mac){
		// This code has been changed from pooling logic to datasource creation everytime due to memory contraint.
		// Previous code can be found at revesion 3838 on trunk.  
		 DriverManagerDataSource dataSource = new DriverManagerDataSource();
		    dataSource.setDriverClassName("org.postgresql.Driver");
		    dataSource.setUrl("jdbc:postgresql://localhost:" + databaseUtil.port+"/"
					+ databaseUtil.getDbNameByMac(mac));
		    dataSource.setUsername("postgres");
		    dataSource.setPassword("postgres");
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(transactionManager.getDataSource());
		JdbcConnectionTemplate.jdbcTemplateContext.set(jdbcTemplate);
		JdbcConnectionTemplate.transactionManagerContext.set(transactionManager);
	}
	
	public DataSourceTransactionManager  createAndGetTransactionManager(String mac){
		 DriverManagerDataSource dataSource = new DriverManagerDataSource();
		    dataSource.setDriverClassName("org.postgresql.Driver");
		    dataSource.setUrl("jdbc:postgresql://localhost:" + databaseUtil.port+"/"
					+ databaseUtil.getDbNameByMac(mac));
		    dataSource.setUsername("postgres");
		    dataSource.setPassword("postgres");
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		return transactionManager;
	}

}