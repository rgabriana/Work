/**
 * 
 */
package com.emscloud.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

/**
 * @author yogesh
 * 
 */
public class ExpiredSessionFilter extends GenericFilterBean {
    static final String FILTER_APPLIED = "__spring_security_expired_session_filter_applied";

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (request.getAttribute(FILTER_APPLIED) != null) {
            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
        if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SESSION_TIMED_OUT");
            return;
        }

        chain.doFilter(request, response);

    }

}
