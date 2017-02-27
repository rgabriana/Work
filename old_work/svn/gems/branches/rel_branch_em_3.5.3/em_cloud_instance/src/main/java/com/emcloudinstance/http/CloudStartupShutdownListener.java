package com.emcloudinstance.http;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.emcloudinstance.service.UidManager;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.SchedulerManager;

public class CloudStartupShutdownListener implements ServletContextListener {

	static final Logger logger = Logger.getLogger("EmCloudInstance");
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
        SchedulerManager.getInstance().shutdownScheduler();
        logger.info("Cloud Instance Stopped");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.info("Cloud Instance Started");
		try {
			Class.forName("org.postgresql.Driver");
			SchedulerManager.getInstance();
			UidManager.startUidJob();
			CommonUtils.createSyncDataDir();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
