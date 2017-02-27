package com.emscloud.http;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.emscloud.action.SpringContext;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.service.BillTaskManager;
import com.emscloud.service.ECManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.service.SiteAnomalyValidationJobManager;
import com.emscloud.util.SchedulerManager;


public class eCloudStartupShutdownListener implements ServletContextListener {
	private static final Logger logger = Logger.getLogger("CloudBilling");
	
    @Override
    public void contextInitialized(ServletContextEvent context) {
    	
    	
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent context) {

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource datasource = (DataSource) envContext.lookup("jdbc/emscloud");
            datasource.getConnection().close();
          
            datasource = null;

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Shutdown the Quartz scheduler
        SchedulerManager.getInstance().shutdownScheduler();

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
