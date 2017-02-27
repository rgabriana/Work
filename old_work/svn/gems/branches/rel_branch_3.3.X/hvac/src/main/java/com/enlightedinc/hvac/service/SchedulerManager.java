package com.enlightedinc.hvac.service;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerManager {
	
	static private SchedulerManager	instance = null;
	private Scheduler				quartzScheduler;

    
	static final Logger logger = Logger.getLogger(SchedulerManager.class.getName());

    private SchedulerManager()
	{
        try {
            quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
            quartzScheduler.start();
            System.out.println("Started the Quartz scheduler with name " + quartzScheduler.getSchedulerName());
         } catch (SchedulerException e) {
            e.printStackTrace();
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
