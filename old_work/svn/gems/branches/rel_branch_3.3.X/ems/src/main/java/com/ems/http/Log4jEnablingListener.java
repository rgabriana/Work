package com.ems.http;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Log4jEnablingListener implements ServletContextListener{

    @Override
    public void contextDestroyed(ServletContextEvent context) {
     
    }

    @Override
    public void contextInitialized(ServletContextEvent context) {
    }

}
