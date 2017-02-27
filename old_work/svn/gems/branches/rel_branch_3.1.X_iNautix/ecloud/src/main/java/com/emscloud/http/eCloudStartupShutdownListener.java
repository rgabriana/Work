package com.emscloud.http;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.emscloud.action.SpringContext;
import com.emscloud.service.BillTaskManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.util.SchedulerManager;


public class eCloudStartupShutdownListener implements ServletContextListener {
	private static final Logger logger = Logger.getLogger("CloudBilling");
    @Override
    public void contextInitialized(ServletContextEvent context) {
    	try
    	{
    		//Start the Quartz scheduler
    		SchedulerManager.getInstance();
    		//Start the Monthly Bill Scheduler
    		BillTaskManager billManager = (BillTaskManager) SpringContext.getBean("billTaskManager");
    		billManager.processBillTasks();
    		logger.info("Quartz Scheduler to generate monthly bill has started...");
    	}catch (Exception e) {
    		logger.error(e.getMessage() + " There is some exception while starting Quartz scheduler...");
		}
    	try
    	{
    		logger.info("Started assigning ssh port to non assigned EM");
    		EmInstanceManager emInstanceManager = (EmInstanceManager) SpringContext.getBean("emInstanceManager");
    		emInstanceManager.assignSshTunnelPortToUnassignedEM();
    	}catch(Exception ex)
    	{
    		logger.error("Error while assigning ssh port to EM which did not had ports already assigned.." + ex.getMessage());
    	}
    	
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
        //SchedulerManager.getInstance().shutdownScheduler();

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
