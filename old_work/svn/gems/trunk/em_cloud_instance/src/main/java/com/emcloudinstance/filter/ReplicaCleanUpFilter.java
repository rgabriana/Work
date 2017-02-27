package com.emcloudinstance.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import com.emcloudinstance.dao.JdbcConnectionTemplate;
import com.emcloudinstance.util.SpringContext;

public class ReplicaCleanUpFilter implements Filter {
	static final Logger logger = Logger.getLogger(ReplicaCleanUpFilter.class.getName());
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		try{
			chain.doFilter(req, res);
		}catch(Exception e) {
			logger.error(e.getMessage(),e);
		}
	    JdbcConnectionTemplate jdbcConnectionTemplate = (JdbcConnectionTemplate) SpringContext
                .getBean("jdbcConnectionTemplate");
	    jdbcConnectionTemplate.cleanup();
	
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
