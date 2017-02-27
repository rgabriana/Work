package com.ems.http;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.ems.server.SchedulerManager;
import com.ems.uem.service.UEMSchedulerManager;

public class EmsStartupShutdownListener implements ServletContextListener {

	private static final Logger logger = Logger.getLogger("EMS");
	private static final Logger profileLogger = Logger
			.getLogger("ProfileLogger");

	@Override
	public void contextInitialized(ServletContextEvent context) {
		System.out.println(new Date() + " *** Initializing context");
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
