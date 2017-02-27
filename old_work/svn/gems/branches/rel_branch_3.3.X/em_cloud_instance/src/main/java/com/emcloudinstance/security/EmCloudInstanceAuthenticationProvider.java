package com.emcloudinstance.security;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

public class EmCloudInstanceAuthenticationProvider implements AuthenticationProvider {
	
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		
		Authentication authenticated = new Authentication() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 123432123421344132L;

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isAuthenticated() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public Object getPrincipal() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getDetails() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getCredentials() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<GrantedAuthority> getAuthorities() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		authenticated.setAuthenticated(true);

		return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		return true;
	}

	

}
