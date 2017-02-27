package com.emcloudinstance.dao;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

/**
 * The class helps in making JdbcTemplate. It also ensure that only one
 * jdbctemplate is returned in a thread local scope
 * @author lalit
 *
 */
@Component("jdbcConnectionTemplate")
public class JdbcConnectionTemplate {
	
	private static final ThreadLocal<JdbcTemplate> jdbcTemplateContext = new ThreadLocal<JdbcTemplate>();
	private static final ThreadLocal<DataSourceTransactionManager> transactionManagerContext = new ThreadLocal<DataSourceTransactionManager>();

	@Resource
	DataSourceConnectionPool dataSourceConnectionPool;

	public JdbcTemplate getJdbcTemplate(String mac) {
		
		if(jdbcTemplateContext.get() == null){
			createLocalTransactionContext(mac);
		}
		
		return jdbcTemplateContext.get();			
		
	}
	
	public void cleanup(){
		jdbcTemplateContext.remove();
		transactionManagerContext.remove();
	}
	
	public DataSourceTransactionManager getTransactionManager(String mac){
		if(this.transactionManagerContext.get() == null){
			this.createLocalTransactionContext(mac);
		}
		return this.transactionManagerContext.get();
	}
	
	private void createLocalTransactionContext(String mac){
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSourceConnectionPool.getDataSource(mac));
		JdbcTemplate jdbcTemplate = new JdbcTemplate(transactionManager.getDataSource());
		this.jdbcTemplateContext.set(jdbcTemplate);
		this.transactionManagerContext.set(transactionManager);
	}

}
