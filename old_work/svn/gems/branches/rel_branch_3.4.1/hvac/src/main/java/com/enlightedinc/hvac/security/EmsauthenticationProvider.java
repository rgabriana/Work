package com.enlightedinc.hvac.security;

import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.enlightedinc.hvac.model.User;
import com.enlightedinc.hvac.service.UserManager;

public class EmsauthenticationProvider implements AuthenticationProvider {
	@Resource
	UserManager userManager;

	@Resource
	PasswordEncoder passwordEncoder;


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		User user = userManager.loadUserByUserName(authentication.getName());
		Authentication authenticated = null;
		EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(user);
		authenticated = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
		/*if (user != null) {

			if (user.getStatus() == Status.INACTIVE) {
				throw new DisabledException("Account is inactive");
			}

			String encodedPassword = passwordEncoder.encodePassword(authentication.getCredentials().toString(), null);
			if (!encodedPassword.equals(user.getPassword())) {
				throw new BadCredentialsException("Password is not correct");
			}
			EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(user);
			authenticated = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authenticated);
		} else {
			throw new AuthenticationCredentialsNotFoundException("User not Found");
		}*/
		return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	

}
