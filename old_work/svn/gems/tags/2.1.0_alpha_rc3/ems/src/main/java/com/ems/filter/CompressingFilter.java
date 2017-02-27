package com.ems.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CompressingFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest))
            return;
        if (!(response instanceof HttpServletResponse))
            return;

        HttpServletRequest hReq = (HttpServletRequest) request;
        if (!isGzipSupported(hReq)) {
            chain.doFilter(request, response);
        } else {

            HttpServletResponse hResp = (HttpServletResponse) response;
            GzipResponse gResp = new GzipResponse(hResp);
            chain.doFilter(hReq, gResp);
            gResp.close();
        }
    }

    private static boolean isGzipSupported(final HttpServletRequest request) {
        Enumeration<String> accept = request.getHeaders("accept-encoding");
        if (accept == null) {
            return false;
        }
        while (accept.hasMoreElements()) {
            String encod = accept.nextElement();
            if (encod != null && encod.contains("gzip")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
