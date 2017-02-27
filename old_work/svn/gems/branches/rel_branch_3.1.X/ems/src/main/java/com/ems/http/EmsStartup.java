/**
 * 
 */
package com.ems.http;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ems.action.SpringContext;
import com.ems.model.Company;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.EmsModeControl;
import com.ems.server.SchedulerManager;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.CompanyManager;
import com.ems.service.ProfileManager;
import com.ems.service.SystemCleanUpManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.uem.service.UEMSchedulerManager;

/**
 * @author yogesh
 * 
 */
public class EmsStartup extends HttpServlet {
	private static final Logger logger = Logger.getLogger("EMS");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timer gatewayPoll = null;
	private int ONE_MINUTE_DELAY = 60 * 1000;

	public void init(ServletConfig config) throws ServletException {
		ServletContext sc = config.getServletContext();
		System.out.println(new Date() + " startup..."
				+ ((sc != null) ? sc.getContextPath() : "-"));
		process(sc);
	}

	private void process(ServletContext context) {
		// ENL-2685 : Start , If user in 2.1 Changes default profiles we must
		// create the instance of default profile
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
				if ("true".equalsIgnoreCase(profileOverrideInitConfig
						.getValue())) {
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

			// Add on More Default profiles -> Outdoor profile
			CompanyManager companyManager = (CompanyManager) SpringContext
					.getBean("companyManager");
			Company company = companyManager.getCompany();
			SystemConfiguration addMoreDefaultProfileConfig = systemConfigurationManager
					.loadConfigByName("add.more.defaultprofile");
			if (company != null
					&& company.getCompletionStatus().intValue() == 3) {
				if (addMoreDefaultProfileConfig != null) {

					if ("true".equalsIgnoreCase(addMoreDefaultProfileConfig
							.getValue())) {
						profileManager.addMoreDefaultProfile();
					} else {
						logger.error("Some more default profile has been already added.");
					}
				}
			} else {
				logger.error("More Default profile cannot be added as default company has not been added yet.");
				SystemConfiguration addMoreProfileConfig = systemConfigurationManager
						.loadConfigByName("add.more.defaultprofile");
				addMoreProfileConfig.setValue("false");
				systemConfigurationManager.save(addMoreProfileConfig);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Profiler Handler could not complete because of "
					+ e.getMessage());
			e.printStackTrace();
		}
		// ENL-2685 : End

		// Do all the clean up here. Add the clean up code in
		// systemCleanUpManager class.
		SystemCleanUpManager systemCleanUpManager = (SystemCleanUpManager) SpringContext
				.getBean("systemCleanUpManager");
		systemCleanUpManager.resetAllFixtureGroupSyncFlag();

		String userPath = context.getRealPath("/");
		if (userPath != null) {
			System.out.println(new Date() + " *** Setting up path " + userPath);
			ServerMain.getInstance().setTomcatLocation(userPath);
			System.out.println(new Date() + " *** Initializing logger");
			initLogger(context);
		} else {
			logger.fatal("Could Not get the real path of Servlet Context");
		}

		// Start the Quartz scheduler
		System.out.println(new Date() + " *** Initializing scheduler");
		SchedulerManager.getInstance();
		gatewayPoll = new Timer("Gateway UTC sync", true);
		GatewayUTCSync osync = new GatewayUTCSync();
		gatewayPoll.schedule(osync, ONE_MINUTE_DELAY);
		System.out.println(new Date() + " *** set EMS mode");
		EmsModeControl.resetToNormalIfImageUpgrade();
        
        // Start UEM ping scheduler 
		try {
			System.out.println(new Date() + " *** starting GLEM check scheduler job");
			UEMSchedulerManager uemSchedulerManager = (UEMSchedulerManager) SpringContext
					.getBean("uemSchedulerManager");
			uemSchedulerManager.addUEMSchedulerJob(userPath);
		} catch (Exception e) {
			logger.fatal("Could not start uem schedule manager");
		}    				
            
		System.out.println(new Date() + " *** done");
	}

	private void initLogger(ServletContext context) {
		String userPath = context.getRealPath("/");

		// Enable to watch for changes in log4j.propeties at regular interval
		// default setting is for linux you will need to modify the path for
		// windows in
		// ems_system_config.properties file
		int intervalToWatchlogFile = 300000; // in ms
		// Get the Log4j.properties file path.

		File dir = new File(userPath);
		File parentPath = dir.getParentFile();
		String dirContainingEnlighted = parentPath.getParent();
		// changing this path affect windows and linux environment differently
		// as Enlighted folder is placed at both environment in different
		// location.
		// be cautious while changing.
		String log4jPath = dirContainingEnlighted + File.separator
				+ "Enlighted" + File.separator + "ems_log4j" + File.separator
				+ "log4j.properties";
		try {
			System.out.println("Logger: " + log4jPath);
			PropertyConfigurator.configureAndWatch(log4jPath,
					intervalToWatchlogFile);
		} catch (Exception ed) {
			System.out
					.println("Could not enable the regular watching of log4j.properties file");
		}
	}
	
	  private class GatewayUTCSync extends TimerTask {
			@Override
			public void run() {
				System.out.println(new Date() + " *** Send UTC on all gateways");
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
			}
	    }
}
