package com.enlightedinc.hvac.listener;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

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

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;

import com.enlightedinc.hvac.dao.HvacConfigurationDao;
import com.enlightedinc.hvac.job.EMPollJob;
import com.enlightedinc.hvac.job.StatsPollJob;
import com.enlightedinc.hvac.model.HvacConfiguration;
import com.enlightedinc.hvac.service.SchedulerManager;
import com.enlightedinc.hvac.utils.Globals;
import com.enlightedinc.hvac.utils.SpringContext;

public class StartupShutdownListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent context) {
        // Start the Quartz scheduler
        Scheduler sched = SchedulerManager.getInstance().getScheduler();
        JobDetail pollJob;
        JobDetail statsPollJob;
        HvacConfigurationDao hvacConfigurationDao = (HvacConfigurationDao) SpringContext.getBean("hvacConfigurationDao");
        HvacConfiguration tempConfig = hvacConfigurationDao.loadConfigByName("em.host");
        if(tempConfig != null) {
        	Globals.em_ip = tempConfig.getValue();
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("em.username");
        if(tempConfig != null) {
        	Globals.em_username = tempConfig.getValue();
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("em.password");
        if(tempConfig != null) {
        	Globals.em_password = tempConfig.getValue();
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.temperature");
        if(tempConfig != null) {
        	Globals.enabledAvgTemperature = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.motionbits");
        if(tempConfig != null) {
        	Globals.enabledMotionBits = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.ambient");
        if(tempConfig != null) {
        	Globals.enabledAmbientLight = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.power");
        if(tempConfig != null) {
        	Globals.enabledSensorPower = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.dimlevel");
        if(tempConfig != null) {
        	Globals.enabledDimLevels = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.outage");
        if(tempConfig != null) {
        	Globals.enabledFixtureOutages = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.timesincelastoccupancy");
        if(tempConfig != null) {
        	Globals.enabledLastOccupancy = "true".equals(tempConfig.getValue());
        }
        tempConfig = hvacConfigurationDao.loadConfigByName("em.polling.interval.in.seconds");
        if(tempConfig != null) {
        	Globals.emPollingInterval = Long.parseLong(tempConfig.getValue()) * 1000;
        }
        
        tempConfig = hvacConfigurationDao.loadConfigByName("enable.stats.polling");
        if(tempConfig != null) {
        	Globals.enableStatsPolling = "true".equals(tempConfig.getValue());
        }
        
        tempConfig = hvacConfigurationDao.loadConfigByName("em.stats.polling.interval.in.seconds");
        if(tempConfig != null) {
        	Globals.emStatsPollingInterval = Long.parseLong(tempConfig.getValue()) * 1000;
        }
        
        Globals.loginUrl = Globals.HTTPS + Globals.em_ip + Globals.loginUrl;
        
        
		try {
			pollJob = newJob(EMPollJob.class)
					.withIdentity("EMPollJob", 
							sched.getSchedulerName())
					.build();
			
			SimpleTrigger pollTrigger = (SimpleTrigger) newTrigger()
					.withIdentity( "EMPollTrigger",
							sched.getSchedulerName())
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							        .withIntervalInMilliseconds(Globals.emPollingInterval)
							        .repeatForever())
					        .build();
			SchedulerManager.getInstance().getScheduler().scheduleJob(pollJob, pollTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
		try {
			statsPollJob = newJob(StatsPollJob.class)
					.withIdentity("StatsPollJob", 
							sched.getSchedulerName())
					.build();
			
			SimpleTrigger statsPollTrigger = (SimpleTrigger) newTrigger()
					.withIdentity( "StatsPollTrigger",
							sched.getSchedulerName())
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							        .withIntervalInMilliseconds(Globals.emStatsPollingInterval)
							        .repeatForever())
					        .build();
			SchedulerManager.getInstance().getScheduler().scheduleJob(statsPollJob, statsPollTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}


    }

    @Override
    public void contextDestroyed(ServletContextEvent context) {

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource datasource = (DataSource) envContext.lookup("jdbc/hvac");
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
