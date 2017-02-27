package com.emcloudinstance.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.emcloudinstance.dao.JdbcConnectionTemplate;
import com.emcloudinstance.util.SpringContext;

public class ReplicaCleanUpFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
	    chain.doFilter(req, res);
	    
	    JdbcConnectionTemplate jdbcConnectionTemplate = (JdbcConnectionTemplate) SpringContext
                .getBean("jdbcConnectionTemplate");
	    jdbcConnectionTemplate.cleanup();
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
