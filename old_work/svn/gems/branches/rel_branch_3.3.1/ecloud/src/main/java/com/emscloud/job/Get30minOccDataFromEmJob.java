package com.emscloud.job;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.action.SpringContext;
import com.emscloud.service.OccManager;

public class Get30minOccDataFromEmJob implements Job {
    public static final Logger logger = Logger.getLogger(Get30minOccDataFromEmJob.class.getName());
    OccManager occManager;

    public Get30minOccDataFromEmJob() {
        occManager = (OccManager) SpringContext.getBean("occManager");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Occ cron job STARTED");
        long startTime = System.currentTimeMillis();
        try {
            if (occManager.isRunning() == false) {
                if (logger.isInfoEnabled()) {
                    logger.info(context.getFireTime() + ": starting new 15 min energy sync on floor levels" + " at "
                            + Calendar.getInstance().getTime().toString());
                }
                occManager.get30MinOccSyncDataFromEm();
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(context.getFireTime() + ": previous job still running  "
                            + context.getPreviousFireTime());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info(context.getFireTime() + " done... (" + (System.currentTimeMillis() - startTime) + ")");
            }
        }
    }

}
