package com.ems.http;

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

import com.ems.action.SpringContext;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.EmsModeControl;
import com.ems.server.SchedulerManager;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.ProfileManager;
import com.ems.service.SystemCleanUpManager;
import com.ems.service.SystemConfigurationManager;

public class EmsStartupShutdownListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(EmsStartupShutdownListener.class);
    private static final Logger profileLogger = Logger.getLogger("ProfileLogger");

    @Override
    public void contextInitialized(ServletContextEvent context) {
        // ENL-2685 : Start , If user in 2.1 Changes default profiles we must create the instance of default profile
        // again and restore it back.
        // Profile Upgrade for 2.1     
		try {
			SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
					.getBean("systemConfigurationManager");
            ProfileManager profileManager = (ProfileManager) SpringContext
                    .getBean("profileManager");

            // Upgrade from 2.3
			SystemConfiguration profileOverrideInitConfig = systemConfigurationManager
	                    .loadConfigByName("profileoverride.init.enable");
            if (profileOverrideInitConfig != null) {
                if ("true".equalsIgnoreCase(profileOverrideInitConfig.getValue())) {
                    profileManager.setupProfileOverrides();
                }
            }
            
            // Upgrade to 2.2 (if not already done)
			SystemConfiguration profileUpgradeEnableConfig = systemConfigurationManager
					.loadConfigByName("profileupgrade.enable");
			if (profileUpgradeEnableConfig != null) {

				if ("true".equalsIgnoreCase(profileUpgradeEnableConfig
						.getValue())) {
					profileManager.compareProfiles();
				} else {
					logger.error("Profile comparison has been completed in previous cycles.");
				}
			} else {
				logger.error("profileupgrade column not found");
			}
			
			// Creation of canned profiles
			SystemConfiguration cannedProfileUpgradeEnableConfig = systemConfigurationManager
					.loadConfigByName("cannedprofile.enable");			
			if (cannedProfileUpgradeEnableConfig != null) {
				if ("1".equalsIgnoreCase(cannedProfileUpgradeEnableConfig
						.getValue())) {
					profileManager.createCannedProfiles();					
				} 
			} else {
				logger.error("cannedprofileupgrade column not found");
			}
			
			//Add on More Default profiles -> Outdoor profile
			SystemConfiguration addMoreDefaultProfileConfig = systemConfigurationManager
                    .loadConfigByName("add.more.defaultprofile");      
			if (addMoreDefaultProfileConfig != null) {

                if ("true".equalsIgnoreCase(addMoreDefaultProfileConfig
                        .getValue())) {
                    profileManager.addMoreDefaultProfile();
                } else {
                    logger.error("Some more default profile has been already added.");
                }
            }
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Profiler Handler could not complete because of "
					+ e.getMessage());
			e.printStackTrace();
		}
		// ENL-2685 : End


        // Do all the clean up here. Add the clean up code in systemCleanUpManager class.
        SystemCleanUpManager systemCleanUpManager = (SystemCleanUpManager) SpringContext
                .getBean("systemCleanUpManager");
        systemCleanUpManager.resetAllFixtureGroupSyncFlag();

        String userPath = context.getServletContext().getRealPath("/");
        if (userPath != null) {
            ServerMain.getInstance().setTomcatLocation(userPath);
        } else {
            logger.fatal("Could Not get the real path of Servlet Context");
        }

        // Start the Quartz scheduler
        SchedulerManager.getInstance();
        DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
        EmsModeControl.resetToNormalIfImageUpgrade();
            
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
