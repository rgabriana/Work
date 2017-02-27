package com.emscloud.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.enlightedUrls.EmEnergyConsumptionUrls;
import com.emscloud.communication.vos.EcSyncVo;
import com.emscloud.dao.BldEnergyConsumptionDao;
import com.emscloud.dao.CampusEnergyConsumptionDao;
import com.emscloud.dao.FacilityDao;
import com.emscloud.dao.FloorEnergyConsumptionDao;
import com.emscloud.dao.OrganizationEnergyConsumptionDao;
import com.emscloud.job.Get15minEnergyAggregationFromEmJob;
import com.emscloud.model.BldEnergyConsumption15min;
import com.emscloud.model.BldEnergyConsumptionDaily;
import com.emscloud.model.BldEnergyConsumptionHourly;
import com.emscloud.model.CampusEnergyConsumption15min;
import com.emscloud.model.CampusEnergyConsumptionDaily;
import com.emscloud.model.CampusEnergyConsumptionHourly;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmLastEcSynctime;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FloorEnergyConsumption15min;
import com.emscloud.model.FloorEnergyConsumptionDaily;
import com.emscloud.model.FloorEnergyConsumptionHourly;
import com.emscloud.model.OrganizationEnergyConsumption15min;
import com.emscloud.model.OrganizationEnergyConsumptionDaily;
import com.emscloud.model.OrganizationEnergyConsumptionHourly;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.types.FacilityType;
import com.emscloud.types.GlemModeType;
import com.emscloud.util.DateUtil;
import com.emscloud.util.EcUtil;
import com.emscloud.util.SchedulerManager;
import com.emscloud.vo.DashboardRecord;
import com.sun.jersey.api.client.GenericType;

/**
 * 
 * @author Shilpa
 * 
 */
@Service("eCManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ECManager {

	static final Logger logger = Logger.getLogger(ECManager.class.getName());

	@Resource
	OrganizationEnergyConsumptionDao organizationEnergyConsumptionDao;

	@Resource
	CampusEnergyConsumptionDao campusEnergyConsumptionDao;

	@Resource
	BldEnergyConsumptionDao bldEnergyConsumptionDao;

	@Resource
	FloorEnergyConsumptionDao floorEnergyConsumptionDao;

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
	EmLastEcSynctimeManager emLastEcSynctimeManager;
	@Resource
	CommunicationUtils communicationUtils;
	@Resource
	EmInstanceManager emInstanceManager;

	Scheduler sched = SchedulerManager.getInstance().getScheduler();
	JobDetail sync15minJob;

	private boolean isRunning = false;

	/**
	 * @param customer
	 * 
	 *            Method goes to every EM for a given customer and fetch the 15
	 *            min energy aggregation data.
	 */
	public void get15MinEnergySyncDataFromEm() {
		try {
			setRunning(true);
			SimpleDateFormat inputFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss");
			ArrayList<EmInstance> emList = null; 
			
			if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
				emList = (ArrayList<EmInstance>) emInstanceManager.getActiveEmInstanceWithDataSynch();
			}else {
				emList = (ArrayList<EmInstance>) emInstanceManager.getActiveEmInstance();
			}
			if (emList != null  && !emList.isEmpty()) {
				Iterator<EmInstance> itr = emList.iterator();
				while (itr.hasNext()) {
					EmInstance em = itr.next();
					try {
						// Need to do in a new transaction so that the data isn't cached in the same session
						saveECData(em);
					}catch(Exception e) {
						logger.error(em.getMacId() + " " + e.getMessage(), e);
					}
				}
			} else {
				if(logger.isInfoEnabled()) {
					logger.info("UEM has no EM attached to it . Cannot fetch the data of 15 min energy Aggregation for customer ");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally {
			setRunning(false);
		}
	}
	
	/**
	 * Each EM's aggregation is flushed to the DB with this function.
	 * @param em instance
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void saveECData(EmInstance em) {
		
		if(logger.isDebugEnabled()) {
			logger.debug(em.getName() + ", " + em.getMacId() + " start sync...");
		}
		String lastSyncTime = null;
		// only if EM is activate go fetch the data
		boolean bProcess = false;
		if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			bProcess = em.getSppaEnabled();
		}else {
			bProcess = em.getActive();
		}
		
		if (bProcess == true) {
			EmLastEcSynctime emLastTime = emLastEcSynctimeManager
					.getEmLastEcSynctimeForEmId(em.getId());
			String request = null;
			if (emLastTime != null) {
				/*
				 * get the two time stamp necessary for sync. 1 :-
				 * Last time a successful sync happened for this em.
				 * 2 :- Last capture at from Floor energy
				 * consumption table for each floor assosiated with
				 * em. 2 is needed because if there is no
				 * connectivity, Uem put zero buckets in 15 min floor
				 * EC tables for that EM.
				 */
				if (emLastTime.getLastSyncAt() != null) {	
					SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
					lastSyncTime= simpleFormatter.format(emLastTime.getLastSyncAt());
					//System.out.println(lastSyncTime);
				} else {
					//This is the first time  this EM is communicating send NA (last time sync Not available)
					lastSyncTime = "NA";
				}
				request =lastSyncTime;
				HashMap<Long, String> facLastTimeStamp = getLatestCaptureAtFor15MinFloor(em
						.getId());
				
				// fire the rest service.
				ResponseWrapper<List<EcSyncVo>> response = glemManager.getAdapter()
						.executePost(
								em,
								glemManager.getAdapter().getContextUrl() + EmEnergyConsumptionUrls.sync15MinEnergyAggregationSyncUrl,
								MediaType.APPLICATION_XML,
								MediaType.APPLICATION_XML,
								new GenericType<List<EcSyncVo>>() {
								},
								request);
				// only if the status is OK go save the data
				if (!ArgumentUtils.isNull(response.getStatus()) &&response.getStatus() == Response.Status.OK
						.getStatusCode()) {
					if (facLastTimeStamp != null) {
						if(logger.isInfoEnabled()) {
							logger.info(em.getId() + ", " + facLastTimeStamp + ", " + (response.getItems() != null ? response.getItems().size() : 0));
						}
						floorEnergyConsumptionDao.saveEcSyncVO(
								response.getItems(), em.getId(),
								facLastTimeStamp);
					} else {
						logger.error("Mapping for em "
								+ em.getMacId()
								+ " cannot be null."
								+ " Aggregation data for this em will be inserted only if the mapping is done."
								+ "Contact Admin");
					}
					
				} else {
					// initiate zero bucket logic.
					if(logger.isInfoEnabled()) {
						logger.info("Em "
							+ em.getMacId()
							+ " with name "
							+ em.getName()
							+ " failed to fetch the data with error code "
							+ response.getStatus() + "initiating zero bucket logic");
					}					
					
					// add zero bucket logic if there is connectivity prolem
					floorEnergyConsumptionDao.putEcZeroBucket(em
							.getId());
				}
			} else {
				logger.error("Last Sync information for em with mac :- "+ em.getMacId() +"missing. Contact admin.");
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug(em.getName() + ", " + em.getMacId() + " end sync...");
		}
		try {
			Thread.sleep(1000);
		}catch(InterruptedException ie){
			// Delay the next EM loop by a second to release some CPU cycles
		}	
	}

	// job schedular for Energy Sync from uem to EM.
	/**
	 * @param customerId
	 */
	public void start15MinEnergySyncCronJob() {

		String syncJobName = "sync15minJob_ecloud";
		String syncTriggerName = "sync15minJobTrigger_ecloud";
		//String cron15MinDefault = "0 0/15 * 1/1 * ? *";
		String cron15MinDefault = "0 8/15 * 1/1 * ? *";
		// Default cron statement to run the job every 15 min	
		String cronstatement = cron15MinDefault;
		try {
			// check if job exist, if not create.
			// Delete the older Quartz job and create a new one
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(syncJobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(syncJobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					logger.debug("Failed to delete Quartz job" + syncJobName);
			}
				
				try {
					if (systemConfigurationManager != null) {
						SystemConfiguration cronSetting = systemConfigurationManager
								.loadConfigByName("SYNC.FLOOR.ENERGY.CRON");
						if (cronSetting.getValue() != null
								|| !cronSetting.getValue().isEmpty()) {
							cronstatement = cronSetting.getValue();
						} else {
							cronstatement = cron15MinDefault;
						}
					}
				} catch (Exception e) {
					cronstatement = cron15MinDefault;
				}
				// create job
				sync15minJob = newJob(Get15minEnergyAggregationFromEmJob.class)
						.withIdentity(
								syncJobName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName()).build();
				// create trigger
				CronTrigger sync15minJobTrigger = (CronTrigger) newTrigger()
						.withIdentity(
								syncTriggerName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName())
						.withSchedule(
								CronScheduleBuilder.cronSchedule(cronstatement))
						.startNow().build();

				// schedule job
				SchedulerManager.getInstance().getScheduler()
						.scheduleJob(sync15minJob, sync15minJobTrigger);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param emId
	 * @return hashmap containing emFacilityId as key and latest capture_at from
	 *         15 min floor energyconsumption for this facility as value.
	 */
	public HashMap<Long, String> getLatestCaptureAtFor15MinFloor(Long emId) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		ArrayList<FacilityEmMapping> floorEm = (ArrayList<FacilityEmMapping>) facilityEmMappingManager
				.getFacilityEmMappingOnEmId(emId);
		if (!ArgumentUtils.isNullOrEmpty(floorEm)) {
			HashMap<Long, String> tempFd = new HashMap<Long, String>();
			for (FacilityEmMapping ef : floorEm) {
				Date latestEcTable = floorEnergyConsumptionDao
						.getLatestCaptureAtFor15MinFloor(ef.getFacilityId());
				if (latestEcTable != null) {
					tempFd.put(ef.getEmFacilityId(),
							inputFormat.format(latestEcTable));
				} else {
					// at start there will be nothing so give
					// Epoch.
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(0);
					tempFd.put(ef.getEmFacilityId(),
							inputFormat.format(c.getTime()));

				}
			}
			return tempFd;
		} else {
			return null;
		}
	}

	public List<DashboardRecord> loadEnergyDataForPeriod(Long id, String propertyName, Date from, Date to) {
        long diff = to.getTime() - from.getTime();
        // Calculate difference in days
        long diffDays = diff / (24 * 60 * 60 * 1000);
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(to);
    	//cal.add(Calendar.DATE, 1);
    	cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 00, 00, 00);

    	ArrayList<DashboardRecord> dashboardRecordList = new ArrayList<DashboardRecord>();
        
    	// For daily 
    	if (diffDays <= 1) {
        	if(propertyName.compareToIgnoreCase(FacilityType.ORGANIZATION.getName()) == 0)
        	{
        		List<OrganizationEnergyConsumptionHourly> consumptionHourlies = organizationEnergyConsumptionDao.getOrganizationHourlyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)organizationEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<OrganizationEnergyConsumptionHourly> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				OrganizationEnergyConsumptionHourly data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.CAMPUS.getName()) == 0)
        	{
        		List<CampusEnergyConsumptionHourly> consumptionHourlies = campusEnergyConsumptionDao.getCampusHourlyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)campusEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<CampusEnergyConsumptionHourly> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				CampusEnergyConsumptionHourly data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.BUILDING.getName()) == 0)
        	{
        		List<BldEnergyConsumptionHourly> consumptionHourlies = bldEnergyConsumptionDao.getBldHourlyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)bldEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<BldEnergyConsumptionHourly> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				BldEnergyConsumptionHourly data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.FLOOR.getName()) == 0)
        	{
        		List<FloorEnergyConsumptionHourly> consumptionHourlies = floorEnergyConsumptionDao.getFloorHourlyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)floorEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<FloorEnergyConsumptionHourly> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				FloorEnergyConsumptionHourly data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        } else if (diffDays <= 6 || diffDays <= 31) {
        	if(propertyName.compareToIgnoreCase(FacilityType.ORGANIZATION.getName()) == 0)
        	{
        		List<OrganizationEnergyConsumptionDaily> consumptionHourlies = organizationEnergyConsumptionDao.getOrganizationDailyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)organizationEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<OrganizationEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				OrganizationEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.CAMPUS.getName()) == 0)
        	{
        		List<CampusEnergyConsumptionDaily> consumptionHourlies = campusEnergyConsumptionDao.getCampusDailyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)campusEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<CampusEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				CampusEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.BUILDING.getName()) == 0)
        	{
        		List<BldEnergyConsumptionDaily> consumptionHourlies = bldEnergyConsumptionDao.getBldDailyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)bldEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<BldEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				BldEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.FLOOR.getName()) == 0)
        	{
        		List<FloorEnergyConsumptionDaily> consumptionHourlies = floorEnergyConsumptionDao.getFloorDailyEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
                Object[] avg_min_max = ((List<Object[]>)floorEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<FloorEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				FloorEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        } else {
        	if(propertyName.compareToIgnoreCase(FacilityType.ORGANIZATION.getName()) == 0)
        	{
        		List<OrganizationEnergyConsumptionDaily> consumptionHourlies = organizationEnergyConsumptionDao.getOrganizationMonthlyEnergyData(id, from, to);
                Object[] avg_min_max = ((List<Object[]>)organizationEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<OrganizationEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				OrganizationEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.CAMPUS.getName()) == 0)
        	{
        		List<CampusEnergyConsumptionDaily> consumptionHourlies = campusEnergyConsumptionDao.getCampusMonthlyEnergyData(id, from, to);
                Object[] avg_min_max = ((List<Object[]>)campusEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<CampusEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				CampusEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.BUILDING.getName()) == 0)
        	{
        		List<BldEnergyConsumptionDaily> consumptionHourlies = bldEnergyConsumptionDao.getBldMonthlyEnergyData(id, from, to);
                Object[] avg_min_max = ((List<Object[]>)bldEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<BldEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				BldEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        	if(propertyName.compareToIgnoreCase(FacilityType.FLOOR.getName()) == 0)
        	{
        		List<FloorEnergyConsumptionDaily> consumptionHourlies = floorEnergyConsumptionDao.getFloorMonthlyEnergyData(id, from, to);
                Object[] avg_min_max = ((List<Object[]>)floorEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
                
    			Iterator<FloorEnergyConsumptionDaily> orgIter = consumptionHourlies.iterator();
    			while(orgIter.hasNext()) {
    				FloorEnergyConsumptionDaily data = orgIter.next();
    				
    				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
    				dashboardRecordList.add(dashboardRecord);
    			}
        	}
        }
    	
    	return dashboardRecordList;
    }
    
    
    public List<DashboardRecord> loadCurrentEnergyData(Long id, String propertyName, Date dateOn) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOn);
        cal.add(Calendar.MINUTE, -5);
        Date to = cal.getTime();
        
        // reset
        cal.setTime(dateOn);
        cal.add(Calendar.MINUTE, -20);
        Date from = cal.getTime();
        
    	ArrayList<DashboardRecord> dashboardRecordList = new ArrayList<DashboardRecord>();

		if (propertyName.compareToIgnoreCase(FacilityType.ORGANIZATION.getName()) == 0) {
			List<OrganizationEnergyConsumption15min> consumptionHourlies = organizationEnergyConsumptionDao
					.getOrganization15minEnergyData(id,
							DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"),
							DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
			Object[] avg_min_max = ((List<Object[]>) organizationEnergyConsumptionDao
					.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);

			Iterator<OrganizationEnergyConsumption15min> orgIter = consumptionHourlies
					.iterator();
			while (orgIter.hasNext()) {
				OrganizationEnergyConsumption15min data = orgIter.next();
				
				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
				dashboardRecordList.add(dashboardRecord);
			}
		}
		if (propertyName.compareToIgnoreCase(FacilityType.CAMPUS.getName()) == 0) {
			List<CampusEnergyConsumption15min> consumptionHourlies = campusEnergyConsumptionDao
					.getCampus15minEnergyData(id,
							DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"),
							DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
			Object[] avg_min_max = ((List<Object[]>) campusEnergyConsumptionDao
					.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);

			Iterator<CampusEnergyConsumption15min> orgIter = consumptionHourlies
					.iterator();
			while (orgIter.hasNext()) {
				CampusEnergyConsumption15min data = orgIter.next();
				
				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
				dashboardRecordList.add(dashboardRecord);
			}
    	}
    	if(propertyName.compareToIgnoreCase(FacilityType.BUILDING.getName()) == 0)
    	{
    		List<BldEnergyConsumption15min> consumptionHourlies = bldEnergyConsumptionDao.getBld15minEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
            Object[] avg_min_max = ((List<Object[]>)bldEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
            
			Iterator<BldEnergyConsumption15min> orgIter = consumptionHourlies.iterator();
			while(orgIter.hasNext()) {
				BldEnergyConsumption15min data = orgIter.next();
				
				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
				dashboardRecordList.add(dashboardRecord);
			}
    	}
    	if(propertyName.compareToIgnoreCase(FacilityType.FLOOR.getName()) == 0)
    	{
    		List<FloorEnergyConsumption15min> consumptionHourlies = floorEnergyConsumptionDao.getFloor15minEnergyData(id, DateUtil.formatDate(from, "yyyy-MM-dd HH:mm:ss"), DateUtil.formatDate(to, "yyyy-MM-dd HH:mm:ss"));
            Object[] avg_min_max = ((List<Object[]>)floorEnergyConsumptionDao.loadPeakAndMinLoadQuery(id.toString(), from, to)).get(0);
            
			Iterator<FloorEnergyConsumption15min> orgIter = consumptionHourlies.iterator();
			while(orgIter.hasNext()) {
				FloorEnergyConsumption15min data = orgIter.next();
				
				DashboardRecord dashboardRecord = EcUtil.convertToDashboardRecord(data, avg_min_max[0] != null ? ((BigDecimal) avg_min_max[0]).floatValue() : null, avg_min_max[1] != null ? ((BigDecimal) avg_min_max[1]).floatValue() : null, avg_min_max[2] != null ? ((BigDecimal) avg_min_max[2]).floatValue() : null);
				dashboardRecordList.add(dashboardRecord);
			}
    	}
    	
    	return dashboardRecordList;
    }

	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * @param isRunning the isRunning to set
	 */
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

}