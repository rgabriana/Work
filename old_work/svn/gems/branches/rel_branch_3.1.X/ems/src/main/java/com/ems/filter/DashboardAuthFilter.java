package com.ems.filter;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ems.action.SpringContext;
import com.ems.model.User;
import com.ems.mvc.debug.DebugController;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserStatus;
import com.ems.util.HardwareInfoUtils;

/**
 * @author SAMEER SURJIKAR
 * 
 */
public class DashboardAuthFilter implements Filter {
	
	 private static final Logger log = Logger.getLogger(DashboardAuthFilter.class);
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String url = request.getServletPath();
		boolean allowedRequest = false;

		// generate session if one doesn't exist
		request.getSession();
		//Get api key from the query string
		String name = "api_key";
		String apiKey = request.getParameter(name);

		try {
			if (evaluateKey(apiKey)) {
				letItIn(response ) ;
			} else if(evaluateOldDashboardRequest(request))
			{
				letItIn(response ) ;
			}
			else
				response.sendRedirect("/ems");
		} catch (IllegalArgumentException lae) {
			response.sendRedirect("/ems");
		} catch (AuthenticationException ae) {
			response.sendRedirect("/ems");
		} catch (Exception ae) {
			response.sendRedirect("/ems");
		}

	}
	private void letItIn(HttpServletResponse response ) throws IOException
	{
		UserManager userManager = (UserManager) SpringContext
				.getBean("userManager");
		Authentication authenticated;
		User user = new User();
		user.setId(1L);
		user.setEmail("Application From Dashboard");
		user.setRole(userManager.getRoleFromRoleList(RoleType.Admin));
		user.setStatus(UserStatus.ACTIVE);
		EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
				user);
		authenticated = new UsernamePasswordAuthenticationToken(
				authenticatedUser, null,
				authenticatedUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(
				authenticated);
		response.sendRedirect("/ems/home.ems");
	}
	private Boolean evaluateKey(String apiKey) {
		Boolean result = false ;
		ArrayList<String> macIpList = HardwareInfoUtils.getMacAddress() ;
		java.util.Iterator<String> itr =  macIpList.iterator() ;
		while(itr.hasNext())
		{
			String macip = itr.next() ;
			if(macip.equals(apiKey))
			{
				result = true ;
			}
		}
		return result ;
	}
	private Boolean evaluateOldDashboardRequest(HttpServletRequest request) {
		SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
				.getBean("systemConfigurationManager");
		Boolean result = false ;
		if(systemConfigurationManager.loadConfigByName("dashboard_sso")!=null)
		{
		
		Boolean isDashboardString = request.getRequestURI().equalsIgnoreCase("/ems/ems_dashboard") ;
		Boolean isDatabaseSet = systemConfigurationManager.loadConfigByName("dashboard_sso").getValue().equalsIgnoreCase("true");
		if(isDashboardString&&isDatabaseSet)
		{
			result=true ;
		}
		else{
			log.warn("Single Sign on field not updated to true.") ;
		}
		}
		else
		{
			log.warn("Single Sign on field not present in system configuration table. ") ;
		}
		return result ;
	}
	public void destroy() {
	}
}
