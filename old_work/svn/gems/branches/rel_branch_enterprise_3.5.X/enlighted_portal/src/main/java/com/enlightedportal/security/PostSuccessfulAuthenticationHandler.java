package com.enlightedportal.security;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.enlightedportal.utils.UserAuditLoggerUtil;

public class PostSuccessfulAuthenticationHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        super.onAuthenticationSuccess(request, response, authentication);
        userAuditLoggerUtil.log( com.enlightedportal.types.UserAuditActionType.Login , SecurityContextHolder.getContext().getAuthentication().getName()+" logged in" );
        
        /*// If the role is auditor let's make the session to never to expire
        EmsDashBoardAuthenticatedUser authenticatedUSer = (EmsDashBoardAuthenticatedUser) authentication.getPrincipal();
        if (authenticatedUSer.getUser().getRole().getRoleType() == RoleType.Auditor) {
            request.getSession().setMaxInactiveInterval(-1);
        }*/
    }

}