package com.enlightedinc.hvac.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.enlightedinc.hvac.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {

	@SuppressWarnings("unused")
	public RoleType getCurrentUserRoleType() {
		EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		return null;// authUser.getRole().getRoleType();
	}

}
