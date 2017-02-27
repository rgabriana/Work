package com.emscloud.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.emscloud.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {

    public RoleType getCurrentUserRoleType() {
        EmsCloudAuthenticatedUser authUser = (EmsCloudAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return null;//authUser.getRole().getRoleType();
    }

//    public Tenant getCurrentTenant() {
//        EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
//                .getPrincipal();
//        return authUser.getTenant();
//    }
}
