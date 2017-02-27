package com.emscloud.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class CommunicationFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest httprequest = (HttpServletRequest)request;
		HttpServletResponse httpresponse = (HttpServletResponse)response;
		String apiKey = httprequest.getHeader("apiKey");
		
		Properties properties = new Properties();
		
		try {
			InputStream is = getClass().getResourceAsStream("/key.properties");
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String serverKey = properties.getProperty("ApiKey");
		System.out.println("server Api Key: "+ serverKey);
		
		if(!apiKey.equals(serverKey)){
			
			
			System.out.println("authentication failed");
			httpresponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
			return;
		}
		
		System.out.println("authentication succeeded");
		chain.doFilter(httprequest, httpresponse);
		
	}

}
