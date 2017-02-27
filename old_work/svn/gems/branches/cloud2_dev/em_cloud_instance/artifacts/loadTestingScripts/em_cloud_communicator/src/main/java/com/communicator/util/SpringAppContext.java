package com.communicator.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringAppContext {
	
	static ApplicationContext springContext = null;
	
	public static void init(){
		springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/spring-context.xml");
	}
	
	public static ApplicationContext getContext(){
		return springContext;
	}

}
