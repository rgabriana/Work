package com.ems.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ems.action.SpringContext;
import com.ems.server.ServerMain;
import com.ems.service.DRTargetManager;

public class EmsStartupShutdownListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(EmsStartupShutdownListener.class);

    @Override
    public void contextInitialized(ServletContextEvent context) {
        String userPath = context.getServletContext().getRealPath("/");
        if (userPath != null) {
            ServerMain.getInstance().setTomcatLocation(userPath);
        } else {
            logger.fatal("Could Not get the real path of Servlet Context");
        }
        
     // Enable to watch for changes in log4j.propeties at regular interval
        //default setting is for linux you will need to modify the path for windows in
        //ems_system_config.properties file
    	int intervalToWatchlogFile = 300000; // in ms
    	// Get the Log4j.properties file path.
    	String path =  ServerMain.getInstance().getTomcatLocation() ;
    	 File dir = new File(path);
    	 File parentPath = dir.getParentFile() ;
    	 String dirContainingEnlighted = parentPath.getParent() ; 
    	 // changing this path affect windows and linux environment differently as Enlighted folder is placed at both environment in different location.
    	 //be cautious while changing.
    	String log4jPath =  dirContainingEnlighted + File.separator +"Enlighted"+ File.separator+ "ems_log4j"+File.separator+"log4j.properties" ;
    	try {
    	    PropertyConfigurator.configureAndWatch(log4jPath,
    		    intervalToWatchlogFile);
    	} catch (Exception ed) {
    	    logger.fatal("Could not enable the regular watching of log4j.properties file");
    	}
    	
        
        //TODO might have to move this code to some other location.
        DRTargetManager drTargetManager = (DRTargetManager) SpringContext
                .getBean("drTargetManager");
        drTargetManager.instantiateDRThread();
    }

    @Override
    public void contextDestroyed(ServletContextEvent context) {

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource datasource = (DataSource) envContext.lookup("jdbc/ems");
            datasource.getConnection().close();

            datasource = (DataSource) envContext.lookup("jdbc/debugems");
            datasource.getConnection().close();

            datasource = null;

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

}
