package com.emscloud.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.emscloud.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {
	
    public RoleType getCurrentUserRoleType() {
        EmsCloudAuthenticatedUser authUser = (EmsCloudAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();        
        if(authUser.getUser().getRoleType().toString().equals(RoleType.Admin.toString()))
        	return RoleType.Admin;
        else if(authUser.getUser().getRoleType().toString().equals(RoleType.SystemAdmin.toString()))
            return RoleType.SystemAdmin;
        else if(authUser.getUser().getRoleType().toString().equals(RoleType.SupportAdmin.toString()))
            return RoleType.SupportAdmin;
        return null;
    }
    
    public Long getUserId() {
        EmsCloudAuthenticatedUser authUser = (EmsCloudAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return authUser.getUser().getId();//authUser.getRole().getRoleType();
    }
    
    public String getUserName() {
    	EmsCloudAuthenticatedUser authUser = (EmsCloudAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
        return authUser.getUsername();
    }
}
