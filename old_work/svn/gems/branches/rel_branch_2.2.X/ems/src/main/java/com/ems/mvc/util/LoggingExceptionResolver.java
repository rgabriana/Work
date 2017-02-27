package com.ems.mvc.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class LoggingExceptionResolver extends SimpleMappingExceptionResolver {

    public LoggingExceptionResolver() {

    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        ex.printStackTrace();
        return super.resolveException(request, response, handler, ex);
    }

}
