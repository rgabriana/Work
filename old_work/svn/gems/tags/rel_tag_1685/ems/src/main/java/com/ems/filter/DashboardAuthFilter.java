package com.ems.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javassist.bytecode.Descriptor.Iterator;

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ems.action.SpringContext;
import com.ems.model.User;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserStatus;
import com.ems.util.HardwareInfoUtils;

/**
 * @author SAMEER SURJIKAR
 * 
 */
public class DashboardAuthFilter implements Filter {

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
				UserManager userManager = (UserManager) SpringContext
						.getBean("userManager");
				Authentication authenticated;
				User user = new User();
				user.setEmail("APPlication From dashboard");
				user.setRole(userManager.getRoleFromRoleList(RoleType.Admin));
				user.setStatus(UserStatus.ACTIVE);
				EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
						user);
				authenticated = new UsernamePasswordAuthenticationToken(
						authenticatedUser, null,
						authenticatedUser.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(
						authenticated);
				response.sendRedirect("/ems/facilities/home.ems");
			} else
				response.sendRedirect("/ems");
		} catch (IllegalArgumentException lae) {
			response.sendRedirect("/ems");
		} catch (AuthenticationException ae) {
			response.sendRedirect("/ems");
		} catch (Exception ae) {
			response.sendRedirect("/ems");
		}

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
	public void destroy() {
	}
}
