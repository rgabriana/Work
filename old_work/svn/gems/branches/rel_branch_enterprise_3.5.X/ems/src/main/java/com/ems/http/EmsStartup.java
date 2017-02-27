/**
 * 
 */
package com.ems.http;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.ems.action.SpringContext;
import com.ems.model.BACnetConfig;
import com.ems.model.Company;
import com.ems.model.PlugloadProfileHandler;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.mvc.util.EmsModeControl;
import com.ems.server.SchedulerManager;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.BacnetManager;
import com.ems.service.CompanyManager;
import com.ems.service.ContactClosureManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadProfileManager;
import com.ems.service.ProfileManager;
import com.ems.service.SystemCleanUpManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.task.GenerateSecretKeyTask;
import com.ems.task.GenerateTimeZoneCache;
import com.enlightedinc.licenseutil.LicenseUtil;
//import com.ems.occengine.OccupancyEngine;
//import com.ems.service.AreaOccupancyManager;

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
	private final static int noOfThreads = Runtime.getRuntime().availableProcessors();
	private final ExecutorService es = Executors.newFixedThreadPool(noOfThreads);

	public void init(ServletConfig config) throws ServletException {
		ServletContext sc = config.getServletContext();
		System.out.println(new Date() + " startup..."
				+ ((sc != null) ? sc.getContextPath() : "-"));
		process(sc);
	}

	private void process(ServletContext context) {
		
		// Update secret key for old users in case of update if any
		es.submit(new GenerateSecretKeyTask());
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

			SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager
					.loadConfigByName("enable.plugloadprofilefeature");

			if (enablePlugloadProfileFeature != null
					&& "true".equalsIgnoreCase(enablePlugloadProfileFeature
							.getValue())) {
				logger.error("adding Default plugload profile");

				PlugloadGroupManager plugloadGroupManager = (PlugloadGroupManager) SpringContext
						.getBean("plugloadGroupManager");
				if (plugloadGroupManager.getPlugloadGroupByName("Default") == null) {
					PlugloadProfileManager plugloadProfileManager = (PlugloadProfileManager) SpringContext
							.getBean("plugloadProfileManager");

					PlugloadProfileHandler plugloadProfileHandler = plugloadProfileManager
							.createPlugloadProfile(
									"default.",
									ServerConstants.DEFAULT_PLUGLOAD_PROFILE_GID,
									true);
					plugloadProfileManager
							.saveDefaultPlugloadGroups(plugloadProfileHandler);
				}
			} else {
				logger.error("Default plugload profile is already created before");
			}

			// license

			SystemConfiguration emUUID = systemConfigurationManager
					.loadConfigByName("em.UUID");
			if (emUUID != null) {
				if ("".equalsIgnoreCase(emUUID.getValue())) {
					emUUID.setValue(UUID.randomUUID().toString());
					systemConfigurationManager.save(emUUID);
				} else {
					logger.error("emUUID column is not empty");
				}

			} else {
				logger.error("em UUID column not found");
			}

			SystemConfiguration emLicenseKeyValue = systemConfigurationManager
					.loadConfigByName("emLicenseKeyValue");

			if (emLicenseKeyValue != null) {
				if ("".equalsIgnoreCase(emLicenseKeyValue.getValue())) {
					emLicenseKeyValue.setValue(LicenseUtil
							.getDefaultEncryptedJsonLicenseString(emUUID
									.getValue()));
					systemConfigurationManager.save(emLicenseKeyValue);
				} else {
					logger.error("emLicenseKeyValue column is not empty");
				}
			} else {
				logger.error("emLicenseKeyValue column not found");
			}
			
			
			
			SystemConfiguration contactClosureConfigurationValue = systemConfigurationManager
			.loadConfigByName("contact_closure_configuration");

			if (contactClosureConfigurationValue != null) {
				if ("".equalsIgnoreCase(contactClosureConfigurationValue.getValue())) {
					ContactClosureManager contactClosureManager = (ContactClosureManager) SpringContext
					.getBean("contactClosureManager");
					contactClosureConfigurationValue.setValue(contactClosureManager.getDefaultContactClosure());
					systemConfigurationManager.save(contactClosureConfigurationValue);
				} else {
					logger.error("contact_closure_configuration column is not empty");
				}
			} else {
				logger.error("contact_closure_configuration column not found");
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
			System.out.println(new Date() + " *** Setting up path");
			ServerMain.getInstance().setTomcatLocation(userPath);
			System.out.println(new Date() + " *** Initializing logger");
			initLogger(context);
		} else {
			logger.fatal("Could Not get the real path of Servlet Context");
		}
		
		
		es.submit(new GenerateTimeZoneCache());

		// Start the Quartz scheduler
		System.out.println(new Date() + " *** Initializing scheduler");
		SchedulerManager.getInstance();
		gatewayPoll = new Timer("Gateway UTC sync", true);
		GatewayUTCSync osync = new GatewayUTCSync();
		gatewayPoll.schedule(osync, ONE_MINUTE_DELAY);
		System.out.println(new Date() + " *** set EMS mode");
		EmsModeControl.resetToNormalIfImageUpgrade();

		BacnetManager bacnetManager = (BacnetManager) SpringContext
				.getBean("bacnetManager");

		UserManager userManager = (UserManager) SpringContext
				.getBean("userManager");

		BACnetConfig bacnetConfig = bacnetManager.getConfig();
		User bacnetUser = userManager.loadBacnetUser();

		if (!bacnetUser.getEmail().equals(bacnetConfig.getRestApiKey())
				|| !bacnetUser.getSecretKey().equals(
						bacnetConfig.getRestApiSecret())) {
			// System.out.println("Setting bacnetConfig parameters in EMS startup");
			bacnetConfig.setRestApiKey(bacnetUser.getEmail());
			bacnetConfig.setRestApiSecret(bacnetUser.getSecretKey());
		}
		try {
			bacnetManager.saveConfig(bacnetConfig);
		}catch (Exception e) {
			System.out.println("Unable to start bacnet service: " + e.getMessage());
		}

		// Creating in Memory JOB instead of persisting job for occupancy engine
		Properties prop = new Properties();
		prop.setProperty("org.quartz.jobStore.class",
				"org.quartz.simpl.RAMJobStore");
		prop.setProperty("org.quartz.threadPool.class",
				"org.quartz.simpl.SimpleThreadPool");
		prop.setProperty("org.quartz.threadPool.threadCount", "1");
		SchedulerFactory schdFact;
		Scheduler schd = null;
		try {
			schdFact = new StdSchedulerFactory(prop);
			schd = schdFact.getScheduler();
			schd.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		// Start Bacnet occupancy engine
		//OccupancyEngine.getInstance().init(schd);
		//AreaOccupancyManager areaOccupancyManager = (AreaOccupancyManager) SpringContext
		//		.getBean("areaOccupancyManager");
		//areaOccupancyManager.addAreaInOccupancyEngine();
		
		// Check contact closure service
		ContactClosureManager ccMgr = (ContactClosureManager) SpringContext
				.getBean("contactClosureManager");
		if (ccMgr != null) {
			ccMgr.monitorBarionetDeviceIfEnabled(ccMgr.getCCDataFromDB());
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
