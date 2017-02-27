package com.ems.server;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.service.EventsAndFaultManager;

public class SchedulerManager {

	static private SchedulerManager	instance = null;
	private Scheduler				quartzScheduler;
	private EventsAndFaultManager 	eventMgr = null;

	static final Logger logger = Logger.getLogger("SchedulerLog");

    private SchedulerManager()
	{
        try
        {
            eventMgr = (EventsAndFaultManager) SpringContext.getBean("eventsAndFaultManager");

            // Grab the Scheduler instance from the Factory
            quartzScheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            quartzScheduler.start();

            eventMgr.addEvent("Started the Quartz scheduler with name " + quartzScheduler.getSchedulerName(), EventsAndFault.SCHEDULER_EVENT);
            logger.info("Started the Quartz scheduler with name " + quartzScheduler.getSchedulerName());

         } catch (SchedulerException se) {
            logger.error("Scheduler could not be started :" + se.getMessage(), se);
        }
	}

    public static SchedulerManager getInstance() {

        if (instance == null) {
            synchronized (SchedulerManager.class) {
                if (instance == null) {
                    instance = new SchedulerManager();
                }
            }
        }
        return instance;

    } // end of method getInstance

    public void shutdownScheduler() {
    	try {
	    	quartzScheduler.shutdown();
            eventMgr.addEvent("Quartz scheduler stopped", EventsAndFault.SCHEDULER_EVENT);
            logger.info("Quartz scheduler stopped");
	    } catch (SchedulerException se) {
            logger.error("Scheduler could not be stopped :" + se.getMessage());
	    }
    }

    public Scheduler getScheduler()
    {
    	return quartzScheduler;
    }

}
