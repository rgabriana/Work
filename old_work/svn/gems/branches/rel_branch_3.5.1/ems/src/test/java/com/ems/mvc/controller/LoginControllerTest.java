package com.ems.mvc.controller;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

import com.ems.AbstractEnlightedTest;

public class LoginControllerTest extends AbstractEnlightedTest {

	@Test
	public void testLogin(){
		try {
			request.setRequestURI("/home.ems");
			
			//request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathvars);

			request.setMethod("GET");
		    request.setServerName("localhost");
		    request.setRemoteAddr("192.168.137.222");
		    request.setScheme("https");
			final ModelAndView modelAndView = (new AnnotationMethodHandlerAdapter()).handle(request, response, applicationEntryPointController);

			//ModelAndViewAssert.assertAndReturnModelAttributeOfType(modelAndView, "SomeModelAttribute", String.class);
			//ModelAndViewAssert.assertModelAttributeValue(modelAndView, "SomeModelAttribute", "Path_Var_Value");
			ModelAndViewAssert.assertViewName(modelAndView, "redirect:facilities/home.ems");
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}
			
}
