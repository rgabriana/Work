package com.motion.utils;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringAppContext {
	
	static AbstractApplicationContext  springContext = null;
	
	public static void init(){
		springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/spring-context.xml");
	}
	
	public static AbstractApplicationContext  getContext(){
		return springContext;
	}
	
}
