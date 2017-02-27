package com.emscloud.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.vos.DateEntityVo;
import com.emscloud.communication.vos.OccSyncVo;
import com.emscloud.dao.FacilityDao;
import com.emscloud.dao.OccReportDao;
import com.emscloud.job.Get30minOccDataFromEmJob;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmLastGenericSynctime;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.util.EmGenericSyncOperationEnums;
import com.emscloud.util.SchedulerManager;
import com.sun.jersey.api.client.GenericType;

@Service("occManager")
@Transactional(propagation = Propagation.REQUIRED)
public class OccManager {

    static final Logger logger = Logger.getLogger(OccManager.class.getName());

    @Resource
    FacilityDao facilityDao;

    @Resource
    private GlemManager glemManager;

    @Resource
    CustomerManager customerManager;

    @Resource
    SystemConfigurationManager systemConfigurationManager;

    @Resource
    FacilityEmMappingManager facilityEmMappingManager;

    @Resource
    EmLastGenericSynctimeManager emLastGenericSynctimeManager;
    @Resource
    CommunicationUtils communicationUtils;
    @Resource
    EmInstanceManager emInstanceManager;
    @Resource
    OccReportDao occReportDao;

    Scheduler sched = SchedulerManager.getInstance().getScheduler();
    JobDetail sync30minJob;

    private boolean isRunning = false;

    public void start30MinOccSyncCronJob() {

        Date startDate = new Date();
        String jobName = "job_occ_30_min";
        String triggerName = "trigger_occ_30_min";
        // String cron15MinDefault = "0 0/15 * 1/1 * ? *";
        String cron30MinDefault = "0 0/30 * * * ?";

        // Default cron statement to run the job every 15 min
        String cronstatement = cron30MinDefault;
        try {
            // check if job exist, if not create.
            // Delete the older Quartz job and create a new one
            if (sched.checkExists(new JobKey(jobName, sched.getSchedulerName()))) {
                if (sched.deleteJob(new JobKey(jobName, sched.getSchedulerName())) == false)
                    logger.debug("Failed to delete Quartz job" + jobName);
            }

            try {
                if (systemConfigurationManager != null) {
                    SystemConfiguration cronSetting = systemConfigurationManager
                            .loadConfigByName("SYNC.FLOOR.OCCUPANCY.CRON");
                    if (cronSetting.getValue() != null || !cronSetting.getValue().isEmpty()) {
                        cronstatement = cronSetting.getValue();
                    } else {
                        cronstatement = cron30MinDefault;
                    }
                }
            } catch (Exception e) {
                cronstatement = cron30MinDefault;
            }
            System.out.println("Scheduling the OCC Data CronJob");
            JobDetail job = newJob(Get30minOccDataFromEmJob.class).withIdentity(jobName,
                    SchedulerManager.getInstance().getScheduler().getSchedulerName()).build();
            CronTrigger trigger = newTrigger()
                    .withIdentity(triggerName, SchedulerManager.getInstance().getScheduler().getSchedulerName())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronstatement)).forJob(job).startAt(startDate)
                    .build();

            SchedulerManager.getInstance().getScheduler().scheduleJob(job, trigger);
            System.out.println("Occ Sync 30 Min job has been Scheduled");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void get30MinOccSyncDataFromEm() {
        try {
            setRunning(true);
            ArrayList<EmInstance> emList = (ArrayList<EmInstance>) emInstanceManager.getActiveEmInstanceWithDataSynch();
            if (emList != null && !emList.isEmpty()) {
                Iterator<EmInstance> itr = emList.iterator();
                while (itr.hasNext()) {
                    EmInstance em = itr.next();
                    try {
                        // Need to do in a new transaction so that the data isn't cached in the same session
                        saveOccData(em);
                    } catch (Exception e) {
                        logger.error(em.getMacId() + " " + e.getMessage(), e);
                    }
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("UEM has no EM attached to it . Cannot fetch the data of 15 min energy Aggregation for customer ");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            setRunning(false);
        }
    }

    /**
     * Each EM's aggregation is flushed to the DB with this function.
     * 
     * @param em
     *            instance
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveOccData(EmInstance em) {

        try {
            if (logger.isDebugEnabled()) {
                logger.debug(em.getName() + ", " + em.getMacId() + " start sync...");
            }
            String lastSyncTime = null;
            // only if EM is activate go fetch the data
            if (em.getSppaEnabled() == true) {
                final EmLastGenericSynctime emLastTime = emLastGenericSynctimeManager.getEmLastGenericSynctimeForEmId(
                        em.getId(), EmGenericSyncOperationEnums.OCCUPANCY_SYNC.name());
                final List<FacilityEmMapping> faciEmMap = facilityEmMappingManager.getFacilityEmMappingOnEmId(em
                        .getId());

                final StringBuilder builder = new StringBuilder();
                if (faciEmMap != null) {
                    for (final FacilityEmMapping emFac : faciEmMap) {
                        builder.append("," + emFac.getEmFacilityId());
                    }
                }
                String request = null;
                if (!StringUtils.isEmpty(builder.toString())) {
                    /*
                     * get the two time stamp necessary for sync. 1 :- Last time a successful sync happened for this em.
                     * 2 :- Last capture at from Floor energy consumption table for each floor assosiated with em. 2 is
                     * needed because if there is no connectivity, Uem put zero buckets in 15 min floor EC tables for
                     * that EM.
                     */
                    if (emLastTime != null && emLastTime.getLastSyncAt() != null) {
                        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
                        lastSyncTime = simpleFormatter.format(emLastTime.getLastSyncAt());
                        // System.out.println(lastSyncTime);
                    } else {
                        // This is the first time this EM is communicating send NA (last time sync Not available)
                        lastSyncTime = "NA";
                    }
                    // fire the rest service for dates.
                    ResponseWrapper<List<DateEntityVo>> responseMaxdate = glemManager.getAdapter().executePost(
                            em,
                            glemManager.getAdapter().getContextUrl()
                                    + "/services/org/occ/floor/maxmindate/energyconsumtion/",
                            MediaType.APPLICATION_XML, MediaType.APPLICATION_XML,
                            new GenericType<List<DateEntityVo>>() {
                            }, request);
                    // only if the status is OK go save the data
                    Date maxDateEM = null;
                    Date minDateEM = null;
                    if (!ArgumentUtils.isNull(responseMaxdate.getStatus())
                            && responseMaxdate.getStatus() == Response.Status.OK.getStatusCode()) {
                        if (logger.isInfoEnabled()) {
                            logger.info(em.getId() + ", " + "MaxDate:" + responseMaxdate.getItems());
                        }
                        maxDateEM = responseMaxdate.getItems().get(0).getMaxDate();
                        if ("NA".equals(lastSyncTime)) {
                            minDateEM = responseMaxdate.getItems().get(0).getMinDate();
                        }
                    }
                    final SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    if (minDateEM != null) {
                        lastSyncTime = simpleFormatter.format(minDateEM);
                    }
                    minDateEM = simpleFormatter.parse(lastSyncTime);
                    while (minDateEM.before(DateUtils.addMinutes(maxDateEM, -30))) {
                        request = lastSyncTime + builder.toString();
                        // fire the rest service.

                        ResponseWrapper<List<OccSyncVo>> response = glemManager.getAdapter().executePost(em,
                                glemManager.getAdapter().getContextUrl() + "/services/org/occ/floor/30min/sync/",
                                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML,
                                new GenericType<List<OccSyncVo>>() {
                                }, request);
                        // only if the status is OK go save the data
                        if (!ArgumentUtils.isNull(response.getStatus())
                                && response.getStatus() == Response.Status.OK.getStatusCode()) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(em.getId() + ", "
                                        + +(response.getItems() != null ? response.getItems().size() : 0));
                            }
                            occReportDao.saveOccSyncVO(response.getItems(), em.getId());

                        } else {
                            throw new Exception("Error status while fetching raw occ data. halting the current job");
                        }
                        minDateEM = DateUtils.addMinutes(minDateEM, 30);
                        lastSyncTime = simpleFormatter.format(minDateEM);
                    }
                } else {
                    logger.warn("No facility is mapped on em:" + em.getId() + ":" + em.getName());
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug(em.getName() + ", " + em.getMacId() + " end sync...");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                // Delay the next EM loop by a second to release some CPU cycles
            }
        } catch (Exception e) {
            logger.error("Exception while saving raw occ data", e);
        }
    }

    /**
     * @return the isRunning
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * @param isRunning
     *            the isRunning to set
     */
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

}
