package com.communicator.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author lalit
 */
public class SpringApplicationContext {
    
    private static ApplicationContext appContext;
    
    public static ApplicationContext getAppContext(){
        if(appContext == null){
            appContext = new ClassPathXmlApplicationContext("spring-context.xml");
        }
        return appContext;
    }
    
    public static Object getBean(String name){
        return getAppContext().getBean(name);
    }
}
