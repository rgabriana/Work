package com.emscloud.http;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.emscloud.action.SpringContext;
import com.emscloud.communication.longpollutil.RequestsBlockingPriorityQueue;
import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.ProfileHandler;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.server.ServerConstants;
import com.emscloud.service.BillTaskManager;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.ECManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.FetchEMProfileJobManager;
import com.emscloud.service.OccManager;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.service.ProfileManager;
import com.emscloud.service.SiteAnomalyValidationJobManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.util.SchedulerManager;

public class eCloudApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    Logger logger = Logger.getLogger(eCloudApplicationListener.class.getName());

    boolean init = true;

    public eCloudApplicationListener() {
        super();
        logger.info("Application context listener is created!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        logger.info("Context '" + event.getApplicationContext().getDisplayName() + "' is started/refreshed!");
        if (init) {
            try {
                if (event.getApplicationContext().getDisplayName().contains("springMVC")) {
                    init = false;
                    new Thread(new Init()).start();
                }
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
    }

    class Init implements Runnable {

        private void startEnergyAggFeature() {
            try {

                // Starting a job to get 15 min sync aggregation for all EM for allcustomer.
                logger.info("Starting a job to get 15 min sync aggregation");
                ECManager eCManager = (ECManager) SpringContext.getBean("eCManager");
                eCManager.start15MinEnergySyncCronJob();
                eCManager.startTimeZoneSyncJob();
            } catch (Exception e) {
                logger.error(e.getMessage()
                        + " Following error occured while startin 15 min energy aggregation sync job sync");
                logger.debug("Following error occured while startin 15 min energy aggregation sync job sync", e);
            }
        }

        @Override
        public void run() {
            try {
                Thread.sleep(20000);

                SystemConfigurationManager sysConfMgr = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
                SystemConfiguration sysConfVal = sysConfMgr.loadConfigByName("cloud.mode");
                
                if (sysConfVal != null && "true".equals(sysConfVal.getValue())) {
	                try {
	                    // Start the Quartz scheduler
	                    SchedulerManager.getInstance();
	                    // Start the Monthly Bill Scheduler
	                    BillTaskManager billManager = (BillTaskManager) SpringContext.getBean("billTaskManager");
	                    billManager.processBillTasks();
	                    logger.info("Quartz Scheduler to generate monthly bill has started...");
	                } catch (Exception e) {
	                    logger.error(e.getMessage() + " There is some exception while starting Quartz scheduler...");
	                }
	                
	                try {
	                    logger.info("Started Validate Site Anomaly Job");
	                    SiteAnomalyValidationJobManager validateSiteAnomalyJobManager = (SiteAnomalyValidationJobManager) SpringContext
	                            .getBean("siteAnomalyValidationJobManager");
	                    validateSiteAnomalyJobManager.startValidateSiteAnomalyJob();
	                } catch (Exception ex) {
	                    logger.error("Error while starting Validate Site Anomaly Job" + ex.getMessage());
	                }
                }
                try {
                    logger.info("Started assigning ssh port to non assigned EM");
                    EmInstanceManager emInstanceManager = (EmInstanceManager) SpringContext
                            .getBean("emInstanceManager");
                    emInstanceManager.assignSshTunnelPortToUnassignedEM();
                } catch (Exception ex) {
                    logger.error("Error while assigning ssh port to EM which did not had ports already assigned.."
                            + ex.getMessage());
                }
                try {
                    SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
                            .getBean("systemConfigurationManager");
                    SystemConfiguration systemConfiguration = systemConfigurationManager
                            .loadConfigByName("FEATURE_ENERGY_AGGREGATION");
                    if (systemConfiguration != null && "true".equals(systemConfiguration.getValue())) {
                        startEnergyAggFeature();
                    }
                } catch (Exception ex) {
                    logger.error("Error while starting Energy Aggregation job" + ex.getMessage());
                    logger.debug("Error while starting Energy Aggregation job ", ex);
                }

                try {
                    SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
                            .getBean("systemConfigurationManager");
                    SystemConfiguration systemConfiguration = systemConfigurationManager
                            .loadConfigByName("FEATURE_OCCUPANCY_AGGREGATION");
                    if (systemConfiguration != null && "true".equals(systemConfiguration.getValue())) {
                        final OccManager occManager = (OccManager) SpringContext.getBean("occManager");
                        occManager.start30MinOccSyncCronJob();
                        // occManager.testMethod();
                    }
                } catch (Exception ex) {
                    logger.error("Error while starting Occupancy Aggregation job" + ex.getMessage());
                    logger.debug("Error while starting Occupancy Aggregation job ", ex);
                }


                EmInstanceManager emInstanceManager = (EmInstanceManager) SpringContext.getBean("emInstanceManager");
                List<EmInstance> emInstances = emInstanceManager.loadAllEmInstances();
                if (emInstances != null) {
                    for (EmInstance emInstance : emInstances) {
                        if (emInstance.getActive()
                                && !RequestsBlockingPriorityQueue.getMap().containsKey(
                                        emInstance.getMacId().replaceAll(":", "").toUpperCase())) {
                            RequestsBlockingPriorityQueue queue = new RequestsBlockingPriorityQueue(emInstance
                                    .getMacId().replaceAll(":", "").toUpperCase());
                            RequestsBlockingPriorityQueue.getMap().put(queue.getMacId(), queue);
                        }
                    }
                }
                try {
                    SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
                            .getBean("systemConfigurationManager");
                    SystemConfiguration systemConfiguration = systemConfigurationManager
                            .loadConfigByName("FEATURE_SENSOR_PROFILE");
                    if (systemConfiguration != null && "true".equals(systemConfiguration.getValue())) {
                        startProfileScheduler();
                    }
                } catch (Exception ex) {
                    logger.error("Error while starting profile schedular job" + ex.getMessage());
                    logger.debug("Error while starting profile schedular job ", ex);
                }

                /*
                 * SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager)
                 * SpringContext.getBean("systemConfigurationManager"); SystemConfiguration systemConfiguration =
                 * systemConfigurationManager.getSystemConfigurationForKey("FEATURE_OCC_ENGINE"); if
                 * (systemConfiguration != null && "true".equals(systemConfiguration.getValue())) {
                 * 
                 * JMSEventListeners jmsEventListeners = (JMSEventListeners) SpringContext
                 * .getBean("jmsEventListeners"); jmsEventListeners.init();
                 * 
                 * TaskSchedularManager taskSchedularManager = (TaskSchedularManager) SpringContext
                 * .getBean("taskSchedularManager"); taskSchedularManager.init();
                 * 
                 * SensorManager sensorManager = (SensorManager) SpringContext .getBean("sensorManager");
                 * sensorManager.init();
                 * 
                 * OccEngineJobManager occEngineJobManager = (OccEngineJobManager) SpringContext
                 * .getBean("occEngineJobManager"); occEngineJobManager.init();
                 * 
                 * HVACCacheManager hvacCacheManager = (HVACCacheManager) SpringContext .getBean("hvacCacheManager");
                 * hvacCacheManager.init();
                 * 
                 * HVACEntryPoint hvacEntryPoint = (HVACEntryPoint) SpringContext .getBean("hvacEntryPoint");
                 * hvacEntryPoint.init(); }
                 */
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }

        }

        private void startProfileScheduler() {
        	try {

        		FetchEMProfileJobManager fetchEMProfileJob = (FetchEMProfileJobManager) SpringContext
                        .getBean("fetchEMProfileJobManager");
    		    
    		    // Create Default 17 profiles once the server is started
    		    fetchEMProfileJob.initiateProfileFetch();
    		    
    		    // Fetch the profiles from all registered energy manager connected to UEM and download the profiles from EM to UEM
    			// Start the Quartz scheduler
    			// Start the Hourly Scheduler to fetch derived profiles from EM to UEM
    			fetchEMProfileJob.scheduleProfileFetch();
    			
    			//Sync the Profiles Present on UEM to EM if sync_status flag present in the em_profile_mapping table is dirty
    			//Runs every 15 minutes
    			fetchEMProfileJob.syncUEMProfileToEM();
    			
            } catch (Exception e) {
                logger.error(e.getMessage() + " Error starting Profile Schedular....");
            }
           
        }
    }

}
