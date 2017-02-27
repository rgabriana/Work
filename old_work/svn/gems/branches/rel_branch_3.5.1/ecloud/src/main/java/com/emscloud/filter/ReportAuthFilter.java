package com.emscloud.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.emscloud.model.Users;
import com.emscloud.security.EmsCloudAuthenticatedUser;

public class ReportAuthFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		String hostname = request.getRemoteAddr();
		
		if("locahost".equalsIgnoreCase(hostname) || "127.0.0.1".equalsIgnoreCase(hostname) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(hostname)){
			Authentication authenticated;
			Users user = new Users();
			user.setId(1L);
			user.setEmail("admin");
		
			EmsCloudAuthenticatedUser authenticatedUser = new EmsCloudAuthenticatedUser(
					user);
			authenticated = new UsernamePasswordAuthenticationToken(
					authenticatedUser, null,
					authenticatedUser.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(
					authenticated);
			chain.doFilter(request, response);
		}else{
			throw new RuntimeException("Access not authorized");
		}
				
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
