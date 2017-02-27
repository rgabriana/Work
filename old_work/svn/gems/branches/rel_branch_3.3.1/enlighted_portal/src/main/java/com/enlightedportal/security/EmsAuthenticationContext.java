package com.enlightedportal.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.enlightedportal.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {

	public RoleType getCurrentUserRoleType() {
		EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		return null;// authUser.getRole().getRoleType();
	}

}
