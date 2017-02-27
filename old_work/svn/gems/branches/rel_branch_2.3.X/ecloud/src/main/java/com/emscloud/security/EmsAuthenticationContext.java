package com.emscloud.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.emscloud.types.RoleType;

@Component("emsAuthContext")
public class EmsAuthenticationContext {

    public RoleType getCurrentUserRoleType() {
        EmsCloudAuthenticatedUser authUser = (EmsCloudAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return RoleType.Admin;//authUser.getRole().getRoleType();
    }


}
