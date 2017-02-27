/**
 * 
 */
package com.ems.server;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.model.EnergyConsumption;
import com.ems.model.Fixture;
import com.ems.model.SystemConfiguration;
import com.ems.server.data.PMStatsWork;
import com.ems.server.data.ZeroBucketStruct;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.processor.PMStatsThread;
import com.ems.server.processor.UpdatePMStatsThread;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.PricingManager;
import com.ems.service.SystemConfigurationManager;

/**
 * @author EMS
 * 
 */
public class PerfSO implements EmsShutdownObserver {

    public static int TEN_MINUTE_INTERVAL = 10 * 60 * 1000;
    public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;
    private static short ZERO_BUCKET = 1;

    private static int FIXTURE_OUTAGE_DETECT_WATTS = 7;

    private static PerfSO instance = null;
    private Timer perfTimer = new Timer("Perf Timer", true);
    private int perfInterval = 60 * 60 * 1000; // 60 minutes

    private Timer missBucketTimer = new Timer("Missing Buckets Timer", true);
    private int missBucketInterval = 5 * 60 * 1000; // 5 minutes

    private EnergyConsumptionManager ecMgr = null;
    private PricingManager priceMgr = null;
    private int pmStatsMode = ServerConstants.PM_STATS_GEMS_MODE;

    private static final Logger logger = Logger.getLogger("Perf");
    
    private int temperatureOffsetSU1 = 18;
    private int temperatureOffsetSU2 = 8;
    
    private boolean sweepTimerEnabled = false;

    public int getTemperatureOffsetSU1() {

        return temperatureOffsetSU1;

    } // end of method getTemperatureOffset
    
    public int getTemperatureOffsetSU2() {

      return temperatureOffsetSU2;

  } // end of method getTemperatureOffset

    // Thread pool for Stats packets
    private int noOfStatsProcessThreads = 1;

    private PMStatsThread statsProcessThPool = null;
    private UpdatePMStatsThread updatePMStatsThread = null;

    private PerfSO() {

        ecMgr = (EnergyConsumptionManager) SpringContext.getBean("energyConsumptionManager");
        priceMgr = (PricingManager) SpringContext.getBean("pricingManager");
        startHourlyTask();
        startMissingBucketsTask();

        SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext
                .getBean("systemConfigurationManager");
        int pmStatsQueueThreshold = 5000;
        int pmStatsProcessingBatchSize = 10;
        int pmBatchTimeMillis = 2000;
        
        if (sysMgr != null) {
            try {
                SystemConfiguration tempConfig = sysMgr.loadConfigByName("cmd.pmstats_processing_threads");
                if (tempConfig != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database command pmstats_processing_threads no. of threads -- "
                            + tempConfig.getValue());
                  }
                    noOfStatsProcessThreads = Integer.parseInt(tempConfig.getValue());
                }
                
                SystemConfiguration pmStatsQueueThresholdConfig = sysMgr.loadConfigByName("cmd.pmstats_queue_threshold");
                if (pmStatsQueueThresholdConfig != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database command pmstats_queue_threshold: " + pmStatsQueueThresholdConfig.getValue());
                  }
                    pmStatsQueueThreshold = Integer.parseInt(pmStatsQueueThresholdConfig.getValue());
                }
                
                SystemConfiguration pmStatsProcessBatchSize = sysMgr.loadConfigByName("cmd.pmstats_process_batch_size");
                if (pmStatsProcessBatchSize != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database command pmstats_process_batch_size: " + pmStatsProcessBatchSize.getValue());
                  }
                    pmStatsProcessingBatchSize = Integer.parseInt(pmStatsProcessBatchSize.getValue());
                }
                
                SystemConfiguration pmBatchTime = sysMgr.loadConfigByName("pmstats_process_batch_time");
                if (pmBatchTime != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database command pmstats_process_batch_time: " + 
                	pmBatchTime.getValue());
                  }
                  pmBatchTimeMillis = Integer.parseInt(pmStatsProcessBatchSize.getValue());
                }
                                
                tempConfig = sysMgr.loadConfigByName("stats.temp_offset_1");
                if (tempConfig != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database teperature offset for 1.x SUs -- " + tempConfig.getValue());
                  }
                    temperatureOffsetSU1 = Integer.parseInt(tempConfig.getValue());
                }
                
                tempConfig = sysMgr.loadConfigByName("stats.temp_offset_2");
                if (tempConfig != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database teperature offset for 2.x SUs -- " + tempConfig.getValue());
                  }
                  temperatureOffsetSU2 = Integer.parseInt(tempConfig.getValue());
                }
                
                tempConfig = sysMgr.loadConfigByName("sweeptimer.enable");
                if (tempConfig != null) {
                  if(logger.isDebugEnabled()) {
                    logger.debug("From database sweep timer enable -- " + tempConfig.getValue());
                  }
                  sweepTimerEnabled = Boolean.parseBoolean(tempConfig.getValue());
                }
                
            } catch (Exception e) {
                logger.error("Could not read the parameters of PerfSo)");
            }
        }
       // statsProcessThPool = new EmsThreadPool(noOfStatsProcessThreads, "StatsThread");
        statsProcessThPool = new PMStatsThread(noOfStatsProcessThreads, "StatsThread",
            pmStatsQueueThreshold, pmStatsProcessingBatchSize, pmBatchTimeMillis);
        statsProcessThPool.start();
        
        updatePMStatsThread = new UpdatePMStatsThread(noOfStatsProcessThreads, "UpdateZeroBucketThread",pmStatsQueueThreshold,pmStatsProcessingBatchSize);
        updatePMStatsThread.start();
    } // end of constructor PerfSO

    public static PerfSO getInstance() {

        if (instance == null) {
            synchronized (PerfSO.class) {
                if (instance == null) {
                    instance = new PerfSO();
                }
            }
        }
        return instance;

    } // end of method getInstance
    
    public boolean isSweepTimerEnabled() {
      
      return sweepTimerEnabled;
      
    } //end of method isSweepTimerEnabled

    public int getPmStatsMode() {
		return pmStatsMode;
	}

    private void startHourlyTask() {

        new Thread() {
            public void run() {

                while (true) {
                    try {
                        if (ServerUtil.getCurrentMin() == 7) { // giving 7 minutes to collect all the
                          if(logger.isDebugEnabled()) {
                            logger.debug("starting the hourly task");
                          }
                            PerfHourlyTask perfHourlyTask = new PerfHourlyTask();
                            perfTimer.scheduleAtFixedRate(perfHourlyTask, 0, perfInterval);
                            return;
                        }
                        Thread.sleep(30 * 1000); // sleep for 30 sec
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            } // end of method run
        }.start();

    } // end of method startHourlyTask

    public void setFixtureOutageDetectWatts(int watts) {
        FIXTURE_OUTAGE_DETECT_WATTS = watts;
    } // end of method setFixtureOutageDetectWatts

    
    public static int getFixtureOutageDetectWatts() {
		return FIXTURE_OUTAGE_DETECT_WATTS;
	}

	public void setStatsMode(int mode) {
        this.pmStatsMode = mode;
    } // end of method setStatsMode

    public class PerfHourlyTask extends TimerTask {

        public void run() {

            try {
                // run the stored procedure for the previous hour
                Calendar cal = Calendar.getInstance();
                Date toDate = DateUtils.truncate(cal.getTime(), Calendar.HOUR);
                int currHour = ServerUtil.getCurrentHour();
                if(logger.isDebugEnabled()) {
                  logger.debug("aggregating hourly data -- " + toDate);
                }
                // call the hourly energy consumption stored procedure
                ecMgr.aggregateHourlyData(toDate);

                if (currHour == 0) { // midnight
                    // call the daily energy consumption stored procedure
                  if(logger.isDebugEnabled()) {
                    logger.debug("aggregating daily data -- " + toDate);
                  }
                    ecMgr.aggregateDailyData(toDate);
                    ecMgr.pruneData();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } // end of method run

    } // end of class PerfHourlyTask

    /*
     * Calib Value = (V * A * T) / (3600 * (N - 1)) It is 0.1 micro WH/pulse, N is no. of pulses
     * 
     * interval in milli seconds
     */
    public static double getAvgPower(int calibVal, int pulses, long interval, Fixture fixture) {

        if (pulses < 2) {
            return 0;
        }
        
        double avgPower = ((double) calibVal * 3600 * (pulses - 1)) / (interval * (double) 10000); // 10e-7 as calib
                                                                                                      // is 0.1 micro

        /*
         * scalaing factor/error adjustment logic For 117V: %err = 0.0511*calculated_watts - 6.9192 For 277V: %err =
         * 1.4522Ln(calculated_watts) - 12.754
         * 
         * So for 117V, if calculated_watts = 180; %err = 2.2788% Adj = (1 -.022788) * 180 = 175.898 watts
         */
        if (ServerMain.getInstance().isApplyECScalingFactor() && avgPower != 0) {
            // Apply scaling factor...
            short fVolts = fixture.getVoltage();
            if(logger.isDebugEnabled()) {
              logger.debug(fixture.getId() + ": before scaling -- " + avgPower);
            }
            double errPercentage = 0.0;
            if (fVolts == 277) {
                errPercentage = ServerMain.getInstance().getScalingFactorFor277V() * Math.log10(avgPower)
                        - ServerMain.getInstance().getAdjFactorFor277V();
                avgPower = (1 - errPercentage / 100) * avgPower;
            } else if (fVolts == 110) {
                errPercentage = ServerMain.getInstance().getScalingFactorFor110V() * avgPower
                        - ServerMain.getInstance().getAdjFactorFor110V();
                avgPower = (1 - errPercentage / 100) * avgPower;
            } else if (fVolts == 240) {
                errPercentage = ServerMain.getInstance().getScalingFactorFor240V() * avgPower
                        - ServerMain.getInstance().getAdjFactorFor240V();
                avgPower = (1 - errPercentage / 100) * avgPower;
            }
        }
        return avgPower;

    } // end of method getAvgPower


    public void updateStatsFromZigbee(Fixture fixture, byte[] packet, long gwId) {

      if(logger.isDebugEnabled()) {
        logger.debug(fixture.getId() + ": stats pkt - " + ServerUtil.getLogPacket(packet));
      }

        if (!fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
            // fixture is not yet commissioned. So, ignore the stats
            logger.error(fixture.getFixtureName() + ": Fixture is not yet commissioned");
            return;
        }
        GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gwId);
        if(gwInfo != null) {
        	if (gwInfo.getOperationalMode() == GatewayInfo.GW_NORMAL_MODE) {
        		if(fixture.getSecGwId() == null || fixture.getSecGwId().compareTo(gwId) != 0) {
        			FixtureCache.getInstance().getDevice(fixture.getId()).setPmStatUpdateReq(true);
        		}
        		fixture.setSecGwId(gwId);
        		fixture.setGateway(gwInfo.getGw());
        	}
        }
        //byte[] seqNoArr = new byte[4];
        //System.arraycopy(packet, 4, seqNoArr, 0, seqNoArr.length);
        //int seqNo = ServerUtil.byteArrayToInt(seqNoArr);
        int seqNo = ServerUtil.extractIntFromByteArray(packet, 4);
        if(logger.isDebugEnabled()) {
          logger.debug(fixture.getId() + ": seq no. for stats -- " + seqNo);
        }

        DeviceServiceImpl.getInstance().setCurrentTime(fixture, seqNo);
		//sensors with smart serialization will send hla serial no as part of discovery so for
        //those sensors only we need to request other information if it is not already requested
        if(fixture.getPcbaPartNo() == null && fixture.getHlaSerialNo() != null ) {
        	//manufacturing information is not in the database, request from SU
        	DeviceServiceImpl.getInstance().sendGetManufacturingInfo(fixture);
        }

        //StatsWork statsWork = new StatsWork(fixture, packet, seqNo);
        //statsProcessThPool.addWork(statsWork);
        
        PMStatsWork statsWork = new PMStatsWork(fixture, packet, seqNo);
        statsProcessThPool.addWork(statsWork);
        fixture = null;
        packet = null;

    } // end of method updateStatsFromZigbee
    
    public void addFixtureToZeroBucketUpdQueue(Long fixtureId, Date lastStatsRcvd, 
	Date latestStatsRcvd, boolean sweepEnabled) {
    	ZeroBucketStruct struct = new ZeroBucketStruct(fixtureId, lastStatsRcvd, latestStatsRcvd,
    	    sweepEnabled);
    	updatePMStatsThread.addZeroBucketStruct(struct);

    } // end of method addFixtureToMissingQueue
    
    public class ZeroBucketUpdHandlerThread extends Thread {

		public void run() {

			while (true) {
				
				long currentMillis = System.currentTimeMillis();
		        long min5 = 5 * 60 * 1000;
		        currentMillis = currentMillis - currentMillis % min5;
		        currentMillis += 3*FIVE_MINUTE_INTERVAL;
		        ecMgr.fillGemsZerouckets(new Date(currentMillis));
		        try {
					Thread.sleep(FIVE_MINUTE_INTERVAL);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} // end of method run

	} // end of class ZeroBucketUpdHandlerThread

    private void startMissingBucketsTask() {

        new Thread() {
            public void run() {

                while (true) {
                    try {
                        if (ServerUtil.getCurrentMin() % 5 == 4) { // start at the 4th/14th/19th... minute
                          if(logger.isDebugEnabled()) {
                            logger.debug("starting the missing buckets task");
                          }
                            MissingBucketsTask missingBucketTask = new MissingBucketsTask();
                            missBucketTimer.scheduleAtFixedRate(missingBucketTask, 0, missBucketInterval);
                            return;
                        }
                        Thread.sleep(1000); // sleep for 1 sec
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            } // end of method run
        }.start();

    } // end of method startMissingBucketsTask
//
    public void addZeroBucket(DeviceInfo device, Fixture fixture, long lastStatsTime) {

        // Fixture fixture = device.getFixture();
        long fixtureId = fixture.getId();
        try {
            EnergyConsumption ec = new EnergyConsumption();
            ec.setFixture(fixture);
            ec.setPowerUsed(new BigDecimal(0));
            ec.setCost((float) 0);
            Date statsDate = new Date(lastStatsTime - FIVE_MINUTE_INTERVAL);
            device.setLastZeroBucketTime(statsDate);
        	Double price = priceMgr.getPrice(statsDate);
            ec.setPrice(price.floatValue());
            ec.setCaptureAt(statsDate);
            ec.setZeroBucket(ZERO_BUCKET);
            BigDecimal zeroSaving = new BigDecimal(0.0);
            ec.setManualSaving(zeroSaving);
            ec.setAmbientSaving(zeroSaving);
            ec.setOccSaving(zeroSaving);
            ec.setTuneupSaving(zeroSaving);
            if(logger.isDebugEnabled()) {
              logger.debug(fixtureId + ":adding zero bucket");
            }
            EnergyConsumption ec1 = ecMgr.save(ec);
            device.setLastZeroBucketId(ec1.getId());
            if(logger.isDebugEnabled()) {
              logger.debug(fixtureId + ": id of the zero bucket is - " + ec1.getId());
            }
        } catch (Exception e) {
            logger.error(fixtureId + ": " + e.getMessage());
        }

    } // end of method addZeroBucket

    public class MissingBucketsTask extends TimerTask {

      public void run() {

	try {
	  HashMap<Long, DeviceInfo> deviceMap = FixtureCache.getInstance().getDeviceMap();
	  Iterator<Long> deviceIter = deviceMap.keySet().iterator();
	  Long deviceId = null;
	  DeviceInfo device = null;
	  
	  long currentMillis = System.currentTimeMillis();
	  currentMillis = currentMillis - currentMillis % FIVE_MINUTE_INTERVAL;
	  long lastStatsTime = new Date(currentMillis).getTime();
	  
	  while (deviceIter.hasNext()) {
	  	deviceId = deviceIter.next();
	    try {
	      device = deviceMap.get(deviceId);
	      // if the device's last energy consumption is more than 10 minutes back, insert
	      // zero bucket
	      if (device.getLastStatsRcvdTime() != null
		  && (lastStatsTime - device.getLastStatsRcvdTime().getTime()) < TEN_MINUTE_INTERVAL) {
		continue;
	      }
	      Fixture fixture = device.getFixture(); 
	      if(device.getDeviceState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
		addZeroBucket(device, fixture, lastStatsTime);
	      }
	    } catch (Exception ex) {
	      logger.error(deviceId + ": Adding zero bucket -- " + ex.getMessage());
	    }
	    //Thread.sleep(1000);
	  } // end of while
	} catch (Exception ex) {
	  ex.printStackTrace();
	}

      } // end of method run

    } // end of class MissingBucketsTask

    public void cleanUp() {

        try {
            if (statsProcessThPool != null) {

                statsProcessThPool.stopThreads();
                if(logger.isDebugEnabled()) {
                  logger.debug("PerfSO thread pool stopped");
                }
            }
        } catch (Throwable th) {

        } finally {
          if(logger.isDebugEnabled()) {
            logger.debug("PerfSO Cleanup done");
          }
        }
    }
  
} // end of class PerfSO
