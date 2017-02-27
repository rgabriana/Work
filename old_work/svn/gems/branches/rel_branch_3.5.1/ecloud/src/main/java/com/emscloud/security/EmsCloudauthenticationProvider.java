package com.emscloud.security;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.emscloud.model.Users;
import com.emscloud.service.UserManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.Status;
import com.emscloud.util.CloudAuditLoggerUtil;

public class EmsCloudauthenticationProvider implements AuthenticationProvider {
	@Resource
	UserManager userManager;

	@Resource
	PasswordEncoder shaPasswordEncoder ;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Autowired
	private HttpServletRequest request;
	
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		
		String ipAddress = null;
		if(request != null) {
			ipAddress = request.getHeader("x-forwarded-for");
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
			}
		}
		if (ipAddress == null) {
			ipAddress = "";
		}
		
		Users user = userManager.loadUserByUserName(authentication.getName());
		Authentication authenticated = null;

		if (user != null) {

			if (user.getStatus() == Status.I) {
				throw new DisabledException("Account is inactive");
			}
			
			String salt = user.getSalt();
			String encodedPassword = shaPasswordEncoder.encodePassword(
					authentication.getCredentials().toString(), salt);
			if (!encodedPassword.equals(user.getPassword())) {
				throw new BadCredentialsException("Password is not correct");
			}

			EmsCloudAuthenticatedUser authenticatedUser = new EmsCloudAuthenticatedUser(
					user);
			authenticated = new UsernamePasswordAuthenticationToken(
					authenticatedUser, null, authenticatedUser.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authenticated);
		} else {
			throw new AuthenticationCredentialsNotFoundException(
					"User not Found");
		}
		cloudAuditLoggerUtil.log("User " + user.getEmail() + " logged in",
				CloudAuditActionType.Login.getName(), ipAddress);
		return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	

}
