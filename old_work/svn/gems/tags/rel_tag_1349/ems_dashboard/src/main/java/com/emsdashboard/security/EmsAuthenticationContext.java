package com.emsdashboard.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.emsdashboard.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {

    public RoleType getCurrentUserRoleType() {
        EmsDashBoardAuthenticatedUser authUser = (EmsDashBoardAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return null;//authUser.getRole().getRoleType();
    }

//    public Tenant getCurrentTenant() {
//        EmsAuthenticatedUser authUser = (EmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
//                .getPrincipal();
//        return authUser.getTenant();
//    }
}
