package com.ems.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.server.device.DeviceServiceImpl;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ADRSchedulerJob implements Job {

	static final Logger logger = Logger.getLogger("SchedulerLog");

    public ADRSchedulerJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

		DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
		deviceImpl.sendUTCTimeOnAllGateways();

		Date now = new Date();
				
		logger.info("ADRScheduler job got fired " + now.toString());

    } // end of method run
    

}
