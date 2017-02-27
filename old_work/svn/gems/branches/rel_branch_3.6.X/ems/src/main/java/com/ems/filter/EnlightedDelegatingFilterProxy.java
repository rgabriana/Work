package com.ems.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.ems.util.Constants;

public class EnlightedDelegatingFilterProxy extends DelegatingFilterProxy{
	private static final Logger logger = Logger.getLogger(EnlightedDelegatingFilterProxy.class);

	 @Override
	 public void doFilter(final ServletRequest request1, final ServletResponse response1,
	                         final FilterChain chain) throws IOException, ServletException {
		 	final HttpServletRequest request = (HttpServletRequest)request1;
		 	HttpSession session = request.getSession(true);
		SecurityContextImpl securityContextImpl = (SecurityContextImpl) session
				.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		 	final HttpServletResponse response = (HttpServletResponse)response1;
	        String path = request.getRequestURI();
	        final String context = request.getContextPath();
	        path = path.replace(context, "");
		if (securityContextImpl != null
				&& (path.startsWith(Constants.LOGIN) || path.equals("/")
						|| path.equals(Constants.LOGIN_SEC) || path
							.equals(Constants.FORGOT_PASS))) {
			Authentication authenticated = null;
			authenticated = securityContextImpl.getAuthentication();
			SecurityContextHolder.getContext().setAuthentication(authenticated);
			request.getRequestDispatcher("/home.ems")
					.forward(request, response);
		} else {
			boolean flagToBypass = false;
			if (path.toLowerCase().startsWith(Constants.EXCLUDE_PATH)) {
				// Check token if it is there then forward to the resource else
				// call super filter
				flagToBypass = true;
			}
			if (!flagToBypass) {
				super.doFilter(request, response, chain);
			} else {
				request.getRequestDispatcher(path).forward(request, response);
			}
		}
	        
	    }

}
