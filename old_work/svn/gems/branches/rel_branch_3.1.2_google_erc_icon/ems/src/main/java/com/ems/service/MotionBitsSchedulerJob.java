package com.ems.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.GemsGroupDao;
import com.ems.dao.MotionBitsSchedulerDao;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionBitsScheduler;
import com.ems.server.util.ServerUtil;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class MotionBitsSchedulerJob implements Job {

    @Autowired
    private MotionBitsSchedulerDao motionBitsSchedulerDao;
    @Resource
    private FixtureManager fixtureManager;
    @Resource
    private GemsGroupDao gemsGroupDao;
    static final Logger logger = Logger.getLogger("SchedulerLog");

    private Long id;
    private Long start;

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(Long start) {
        this.start = start;
    }

    public MotionBitsSchedulerJob(Long id, Long start) {
        this.id = id;
        this.start = start;
    }

    public MotionBitsSchedulerJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {
            MotionBitsSchedulerDao motionBitsSchedulerDao = (MotionBitsSchedulerDao) SpringContext
                    .getBean("motionBitsSchedulerDao");
            FixtureManager fixtureManager = (FixtureManager) SpringContext.getBean("fixtureManager");
            GemsGroupDao gemsGroupDao = (GemsGroupDao) SpringContext.getBean("gemsGroupDao");

            MotionBitsScheduler schedule = motionBitsSchedulerDao.loadMotionBitsScheduleById(id);

            List<GemsGroupFixture> listFixtureGroup = gemsGroupDao.getGemsGroupFixtureByGroup(schedule
                    .getMotionBitGroup().getId());
            int[] fixtureList = new int[listFixtureGroup.size()];
            if (start == 1L)
                logger.info("Starting motion bits capture for configuration: " + schedule.getName());
            else
                logger.info("Stopping motion bits capture for configuration: " + schedule.getName());

            for (GemsGroupFixture ggf : listFixtureGroup) {
                int count = 0;
                fixtureList[count++] = ggf.getFixture().getId().intValue();

                if (start == 1L) {
                    logger.info("Starting motion bits capture for fixture: " + ggf.getFixture().getId().intValue()
                            + " BitLevel: " + schedule.getBitLevel().byteValue() + " Transmit freq: "
                            + schedule.getTransmitFreq().byteValue());
                    fixtureManager.triggerMotionBits(fixtureList, schedule.getBitLevel().byteValue(), schedule
                            .getTransmitFreq().byteValue(), (byte) 1, getScheduleDate(schedule.getCaptureStart()),
                            getScheduleDate(schedule.getCaptureEnd()));
                } else {
                    logger.info("Stopping motion bits capture for fixture: " + ggf.getFixture().getId().intValue());
                    fixtureManager.triggerMotionBits(fixtureList, schedule.getBitLevel().byteValue(), schedule
                            .getTransmitFreq().byteValue(), (byte) 0, getScheduleDate(schedule.getCaptureStart()),
                            getScheduleDate(schedule.getCaptureEnd()));
                }

            }
        } catch (Exception ex) {

            logger.error("MotionBitsJob execution error" + ex.getMessage());
        }

    } // end of method run
    
    public Date getScheduleDate(String strDate) {
        try {
            String startHour = strDate.substring(0, strDate.indexOf(':'));
            String startMinute = strDate.substring(strDate.indexOf(':') + 1);
      
            Calendar startCal = Calendar.getInstance();
      
            startCal.setTimeInMillis(System.currentTimeMillis());
            // If hour to start is less than the current hour then start tomorrow
            if ((Integer.parseInt(startHour) < startCal.get(Calendar.HOUR_OF_DAY))
                    || ((Integer.parseInt(startHour) == startCal.get(Calendar.HOUR_OF_DAY)) && Integer
                            .parseInt(startMinute) < startCal.get(Calendar.MINUTE)))
                startCal.set(Calendar.DAY_OF_MONTH, startCal.get(Calendar.DAY_OF_MONTH) + 1);
      
            startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
            startCal.set(Calendar.MINUTE, Integer.parseInt(startMinute));
            startCal.set(Calendar.SECOND, 0);
            return startCal.getTime();
        }catch(Exception e) {
            System.out.println(e.getMessage());
            return new Date();
        }
    }
}
