package com.ems.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ems.model.Tenant;
import com.ems.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {

    public RoleType getCurrentUserRoleType() {
        EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return authUser.getRole().getRoleType();
    }

    public Tenant getCurrentTenant() {
        EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return authUser.getTenant();
    }
    
    public Long getUserId() {
        EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return authUser.getUser().getId();
    }
    
    public String getUserName() {
        EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return authUser.getUsername();
    }
}
