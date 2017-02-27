package com.emsmgmt.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class StaticContentCachingFilter implements Filter {

    private String CACHE_CONTROL = "Cache-Control";
    private String EXPIRES = "Expires";
    private String PRAGMA = "Pragma";
    private String ETAG = "ETag";

    private long CACHE_EXPIRE_TIME = 86400000;

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setDateHeader(EXPIRES, System.currentTimeMillis() + CACHE_EXPIRE_TIME);
        httpResponse.setHeader(CACHE_CONTROL, "public");

        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

}
