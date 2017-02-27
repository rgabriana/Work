package com.emscloud.security;

import javax.annotation.Resource;

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
import com.emscloud.types.Status;

public class EmsCloudauthenticationProvider implements AuthenticationProvider {
	@Resource
	UserManager userManager;

	@Resource
	PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Users user = userManager.loadUserByUserName(authentication.getName());
		Authentication authenticated = null;

		if (user != null) {

			if (user.getStatus() == Status.I) {
				throw new DisabledException("Account is inactive");
			}

			String encodedPassword = passwordEncoder.encodePassword(
					authentication.getCredentials().toString(), null);
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
		return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	

}
