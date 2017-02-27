package com.emcloudinstance.http;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;

public class Log4jEnablingListener implements ServletContextListener{

    @Override
    public void contextDestroyed(ServletContextEvent context) {
     
    }

    @Override
    public void contextInitialized(ServletContextEvent context) {
        String userPath = context.getServletContext().getRealPath("/");
        
        // Enable to watch for changes in log4j.propeties at regular interval
           //default setting is for linux you will need to modify the path for windows in
           //ems_system_config.properties file
           int intervalToWatchlogFile = 300000; // in ms
           // Get the Log4j.properties file path.
           
           File dir = new File(userPath);
           File parentPath = dir.getParentFile() ;
           String dirContainingEnlighted = parentPath.getParent() ; 
           // changing this path affect windows and linux environment differently as Enlighted folder is placed at both environment in different location.
           //be cautious while changing.
          String log4jPath =  dirContainingEnlighted + File.separator +"Enlighted"+ File.separator+ "ems_log4j"+File.separator+"log4j.properties" ;
          try {
              PropertyConfigurator.configureAndWatch(log4jPath,
                  intervalToWatchlogFile);
          } catch (Exception ed) {
              System.out.println("Could not enable the regular watching of log4j.properties file");
          }
        
    }

}
