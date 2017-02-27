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
import com.ems.cache.FixtureCache;
import com.ems.model.EnergyConsumption;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.SystemConfiguration;
import com.ems.server.data.PMStatsWork;
import com.ems.server.data.ZeroBucketStruct;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.processor.PMStatsThread;
import com.ems.server.processor.UpdatePMStatsThread;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.PricingManager;
import com.ems.service.ProfileManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.cache.DeviceInfo;

/**
 * @author EMS
 * 
 */
public class PerfSO implements EmsShutdownObserver {

    public static int TEN_MINUTE_INTERVAL = 10 * 60 * 1000;
    public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;

    private static short ORIG_BUCKET = 0;
    private static short ZERO_BUCKET = 1;
    private static short UPD_BUCKET = 2;

    private static int BASE_POWER_CORRECTION_PERCENTAGE = 5;
    private static int OUTAGE_DETECT_PERCENTAGE = 10;
    private static int FIXTURE_OUTAGE_DETECT_WATTS = 7;

    private static PerfSO instance = null;
    private Timer perfTimer = new Timer("Perf Timer", true);
    private int perfInterval = 60 * 60 * 1000; // 60 minutes

    private Timer missBucketTimer = new Timer("Missing Buckets Timer", true);
    private int missBucketInterval = 5 * 60 * 1000; // 5 minutes

    private EnergyConsumptionManager ecMgr = null;
    private EventsAndFaultManager eventMgr = null;
    private FixtureManager fixtureMgr = null;
    private PricingManager priceMgr = null;
    private int pmStatsMode = ServerConstants.PM_STATS_GEMS_MODE;

    private int outageVolts = 70;
    private int outageAmbLight = 100;
    private static final Logger logger = Logger.getLogger("Perf");
    
    private int temperatureOffsetSU1 = 18;
    private int temperatureOffsetSU2 = 8;
    
    private boolean sweepTimerEnabled = false;

//    class ZeroBucketStruct {
//
//        long fixtureId;
//        Date lastStatsRcvdTime;
//        Date latestStateRcvdTime;
//
//        public ZeroBucketStruct(long fixtureId, Date lastStatsRcvdTime, Date latestStatsRcvdTime) {
//
//            this.fixtureId = fixtureId;
//            this.lastStatsRcvdTime = lastStatsRcvdTime;
//            this.latestStateRcvdTime = latestStatsRcvdTime;
//
//        } // end of constructor
//
//    } // end of class MissingBucketStruct

    public int getTemperatureOffsetSU1() {

        return temperatureOffsetSU1;

    } // end of method getTemperatureOffset
    
    public int getTemperatureOffsetSU2() {

      return temperatureOffsetSU2;

  } // end of method getTemperatureOffset
    
//
//    private HashMap<Long, ZeroBucketStruct> zeroBucketUpdFixtureMap = new HashMap<Long, ZeroBucketStruct>();
//
//    private LinkedList<Long> zeroBucketUpdQueue = new LinkedList<Long>();

    // Thread pool for Stats packets
    private int noOfStatsProcessThreads = 1;
//    private EmsThreadPool statsProcessThPool = null;
    
    private PMStatsThread statsProcessThPool = null;
    private UpdatePMStatsThread updatePMStatsThread = null;

    private PerfSO() {

        ecMgr = (EnergyConsumptionManager) SpringContext.getBean("energyConsumptionManager");
        eventMgr = (EventsAndFaultManager) SpringContext.getBean("eventsAndFaultManager");
        fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
        priceMgr = (PricingManager) SpringContext.getBean("pricingManager");
        
        initVoltPowerMap();
        //fillGemsMissingBuckets();
        startHourlyTask();
        startMissingBucketsTask();
//         ZeroBucketUpdHandlerThread zeroBucketUpdThread = new ZeroBucketUpdHandlerThread();
        //zeroBucketUpdThread.start();
        
//        UpdateZeroBucketUpdHandlerThread updateZeroBucketUpdThread = new UpdateZeroBucketUpdHandlerThread();
//        updateZeroBucketUpdThread.start();

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

	private void initVoltPowerMap() {

        // voltPowerList = fixtureMgr.getAllBallastVoltPowers();

    } // end of method initVoltPowerMap

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

    public void setOutageVolts(int volts) {

        this.outageVolts = volts;

    } // end of method setOutageVolts

    public void setOutageAmbLight(int amb) {

        this.outageAmbLight = amb;

    } // end of method setOutageAmbLight

    public void setBasePowerCorrectionPercentage(int percentage) {

        this.BASE_POWER_CORRECTION_PERCENTAGE = percentage;

    } // end of method setOutageAmbLight

    public void setOutageDetectPercentage(int percentage) {

        this.OUTAGE_DETECT_PERCENTAGE = percentage;

    } // end of method setOutageDetectPercentage

    public void setFixtureOutageDetectWatts(int watts) {

        this.FIXTURE_OUTAGE_DETECT_WATTS = watts;

    } // end of method setFixtureOutageDetectWatts

    
    public static int getFixtureOutageDetectWatts() {
		return FIXTURE_OUTAGE_DETECT_WATTS;
	}

	public void setStatsMode(int mode) {

        this.pmStatsMode = mode;

    } // end of method setStatsMode

    // this function will add the zero buckets for the duration gems shutdown
    private void fillGemsMissingBuckets() {

        new Thread() {
            public void run() {
                long min5 = 5 * 60 * 1000;
                long currentMillis = System.currentTimeMillis() - min5;
                currentMillis = currentMillis - currentMillis % min5;
                Date toDate = new Date(currentMillis);
                ecMgr.fillGemsMissingBuckets(toDate);
            }
        }.start();

    } // end of method fillGemsMissingBuckets

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

    // get average energy in milli watt hour
    private static double getAvgEnergyInMWH(short calibVal, short pulses) {

        return ((double) calibVal * 3600 * (pulses - 1)) / (1000);

    } // end of method getAvgEnergyInMWH

//    private BallastVoltPower getBallastVoltPowerFactor(Ballast ballast, float volt) {
//        if (ballast != null) {
//            List<BallastVoltPower> voltPowerList = fixtureMgr.getAllBallastVoltPowersFromId(ballast.getVoltPowerMapId()
//                    .longValue());
//            if (voltPowerList != null) {
//                logger.debug("VoltPowerList size: " + voltPowerList.size());
//                Iterator<BallastVoltPower> voltPoweritr = voltPowerList.iterator();
//                BallastVoltPower obvp = null;
//                while (voltPoweritr.hasNext()) {
//                    obvp = (BallastVoltPower) voltPoweritr.next();
//                    if (obvp.getVolt().floatValue() == volt)
//                        return obvp;
//                }
//            }
//        }
//        return null;
//    }

//    public static double getAvgPowerFromVolts(int volts, int noOfLamps, Ballast ballast) {
//
//        if (volts == 0) {
//            return 0;
//        }
//        int bulbWattage = ballast.getWattage();
//        int rem = volts % 5;
//        if (rem > 2) {
//            volts += (5 - rem);
//        } else {
//            volts -= rem;
//        }
//        float fPowerFactor = 0;
//        BallastVoltPower obvp = getInstance().getBallastVoltPowerFactor(ballast, (float) volts / 10);
//        if (obvp == null) {
//            logger.error("Error fetching VoltPowerMap: (" + volts + " => " + fPowerFactor + ")");
//        } else {
//            fPowerFactor = obvp.getPower().floatValue();
//            logger.debug("Fetching from Ballast: " + obvp.getVoltPowerMapId() + ", VoltPowerMap: (" + volts + " => "
//                    + fPowerFactor + ")");
//        }
//        double ballast_factor = ballast.getBallastFactor();
//        double calcPower = ballast_factor * bulbWattage * noOfLamps / 100;
//        if (fPowerFactor > 0) {
//            calcPower = calcPower * fPowerFactor;
//        }
//        return calcPower;
//
//    } // end of method getAvgPowerFromVolts

//    private void setBulbLife(Fixture fixture, float voltPowerFactor) {
//
//        try {
//            double bulbLife = fixture.getBulbLife();
//            Bulb bulb = fixture.getBulb();
//            if (bulb == null) {
//                logger.error(fixture.getId() + ": error in retrieving the bulb");
//                return;
//            }
//            long totalLife = bulb.getLifeInsStart();
//
//            double usedLife = voltPowerFactor * 5 / 60 / 100; // in hours
//            double usedPercent = usedLife * 100 / totalLife;
//            fixture.setBulbLife(bulbLife - usedPercent);
//        } catch (Exception e) {
//            logger.error(fixture.getId() + ": error in retrieving the bulb life");
//        }
//
//    } // end of method setBulbLife

//    private static int getBit(byte[] data, int pos) {
//
//        int posByte = pos / 8;
//        int posBit = pos % 8;
//        byte valByte = data[posByte];
//        int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
//        return valInt;
//
//    } // end of method getBit

    public void updateStatsFromZigbee(Fixture fixture, byte[] packet, long gwId) {

      if(logger.isDebugEnabled()) {
        logger.debug(fixture.getId() + ": stats pkt - " + ServerUtil.getLogPacket(packet));
      }

        fixture.setSecGwId(gwId);
        GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gwId);
        if(gwInfo != null) {
        	fixture.setGateway(gwInfo.getGw());
        }
        if (!fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
            // fixture is not yet commissioned. So, ignore the stats
            logger.error(fixture.getFixtureName() + ": Fixture is not yet commissioned");
            return;
        }
        //byte[] seqNoArr = new byte[4];
        //System.arraycopy(packet, 4, seqNoArr, 0, seqNoArr.length);
        //int seqNo = ServerUtil.byteArrayToInt(seqNoArr);
        int seqNo = ServerUtil.extractIntFromByteArray(packet, 4);
        if(logger.isDebugEnabled()) {
          logger.debug(fixture.getId() + ": seq no. for stats -- " + seqNo);
        }

        DeviceServiceImpl.getInstance().setCurrentTime(fixture, seqNo);

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
//        synchronized (zeroBucketUpdQueue) {
//            if (!zeroBucketUpdQueue.contains(fixtureId)) {
//                zeroBucketUpdQueue.add(fixtureId);
//                ZeroBucketStruct struct = new ZeroBucketStruct(fixtureId, lastStatsRcvd, latestStatsRcvd);
//                zeroBucketUpdFixtureMap.put(fixtureId, struct);  
//            }
//        }

    } // end of method addFixtureToMissingQueue

//    public class StatsWork implements Runnable {
//
//        private Fixture fixture;
//        private byte[] packet;
//        private int seqNo;
//
//        public StatsWork(Fixture fixture, byte[] packet, int seqNo) {
//
//            this.fixture = fixture;
//            this.packet = packet;
//            this.seqNo = seqNo;
//
//        } // end of constructor
//
//        public void run() {
//
//            try {
//                long startTime = System.currentTimeMillis();
//                updateStats(fixture, packet, seqNo);
//                timingLogger.debug("Processing PM Stats Packet:" + (System.currentTimeMillis() - startTime));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        } // end of method run
//
//    } // end of class StatsWork

//    public void updateStats(final Fixture fixture, byte[] packet, int seqNo) {
//
//        long startTime = System.currentTimeMillis();
//        DeviceInfo device = ServerMain.getInstance().getDevice(fixture);
//        if (device != null && device.getLastStatsSeqNo() == seqNo) {
//            // duplicate packet ignore it
//            logger.error(fixture.getId() + ": Duplicate stats received");
//            return;
//        }
//        timingLogger.debug("time taken after set time: " + (System.currentTimeMillis() - startTime));
//        long currentMillis = System.currentTimeMillis();
//        long min5 = 5 * 60 * 1000;
//        currentMillis = currentMillis - currentMillis % min5;
//
//        // if the last stats received time is past 5 minutes and
//        // if the seq no. is +1 of last seq no. then the this stats is for previous period
//        if (device.getLastStatsSeqNo() + 1 == seqNo
//                && currentMillis - device.getLastStateRcvdTime().getTime() > FIVE_MINUTE_INTERVAL) {
//            currentMillis -= FIVE_MINUTE_INTERVAL;
//        }
//        Date statsDate = new Date(currentMillis);
//        device.setLastStatsSeqNo(seqNo);
//        byte[] tempShortByteArr = new byte[2];
//        byte[] tempIntByteArr = new byte[4];
//
//        int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
//        if (packet[0] == ServerConstants.FRAME_START_MARKER) { // old packet
//            index = 3;
//        }
//        // byte 3 is min voltage
//        byte minVolts = packet[index++];
//
//        // byte 4 is max voltage
//        byte maxVolts = packet[index++];
//
//        // byte 5, 6 is avg voltage
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short avgVolts = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        if (avgVolts > 100) {
//            avgVolts = 100;
//        }
//        index += 2;
//
//        // byte 7 is last voltage
//        byte lastVolts = packet[index++];
//        if (lastVolts > 100) {
//            lastVolts = 100;
//        }
//        fixture.setDimmerControl((int) lastVolts);
//
//        // bytes 8, 9 is min amb
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short minAmb = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 10, 11 is max amb
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short maxAmb = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 12, 13, 14, 15 is avg amb
//        System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
//        int avgAmb = ServerUtil.byteArrayToInt(tempIntByteArr);
//        index += 4;
//
//        // byte 16 is min temp
//        byte minTemp = packet[index++];
//
//        // byte 17 is max temp
//        byte maxTemp = packet[index++];
//
//        // byte 18, 19 is avg temp
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short avgTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 20 is last temperature
//        byte lastTemp = packet[index++];
//
//        // bytes 21, 22 is energy_calib value
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        int energyCalib = ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 23, 24 is energy ticks
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        int pulses = ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 25, 26, 27, 28 is energy_cum
//        // this need to be used to compute energy of missing buckets
//        System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
//        long energyCumTicks = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
//        index += 4;
//
//        // bytes 29,30 is light on sec
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short lightOnSec = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 31, 32 is light on to off
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short lightOnToOff = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 33, 34 is light off to on
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short lightOffToOn = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 35, 36 is motion off to on
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short motionOffToOn = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        // bytes 37, 38 is motion on to off
//        System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
//        short motionOnToOff = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
//        index += 2;
//
//        byte[] motionBits = new byte[8];
//        // bytes 39, 40, 41, 42 is motion mask1
//        System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
//        int motionMask1 = ServerUtil.byteArrayToInt(tempIntByteArr);
//        System.arraycopy(packet, index, motionBits, 4, 4);
//        index += 4;
//
//        // bytes 43, 44, 45, 46 is motion mask2
//        System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
//        int motionMask2 = ServerUtil.byteArrayToInt(tempIntByteArr);
//        System.arraycopy(packet, index, motionBits, 0, 4);
//        index += 4;
//
//        long motionBitsLong = ServerUtil.byteArrayToLong(motionBits);
//
//        int lastOccSeen = getLastOccupancySec(motionMask1, motionMask2);
//        if (lastOccSeen == 300) { // no occupancy seen
//            fixture.setLastOccupancySeen(fixture.getLastOccupancySeen() + 300);
//        } else {
//            fixture.setLastOccupancySeen(lastOccSeen);
//        }
//
//        // byte 47
//        // 0 - none
//        // 1 - occupancy
//        // 2 - ambient
//        // 3 - individual(tune_up)
//        // 4 - manual
//        int savingType = packet[index++];
//
//        logger.debug("saving type -- " + savingType);
//
//        // byte 48 is current state
//        byte currState = packet[index++];
//
//        // byte 49, 50, 51, 52 is sys up time
//        System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
//        long uptime = ServerUtil.intByteArrayToLong(tempIntByteArr);
//        logger.debug(fixture.getId() + ": uptime of node -- " + uptime);
//        index += 4;
//        // byte 53, 54 is last reset reason
//        index += 2;
//        // byte 55, 56 is cuStatus
//        index += 2;
//        // byte 57, 58 is num reset by cu
//        index += 2;
//
//        // from version 1.2
//        // byte 59 is app
//        int currApp = 2;
//        if (ServerUtil.compareVersion(fixture.getVersion(), "1.2") >= 0) {
//            currApp = packet[index++];
//        }
//
//        timingLogger.debug("after packet processing: " + (System.currentTimeMillis() - startTime));
//        if (packet.length > index + 1) {
//            // byte 60 is global profile checksum
//            final byte gChecksum = packet[index++];
//            // byte 61 scheduled profile checksum
//            final byte sChecksum = packet[index++];
//            // byte 62 profile group id
//            final byte profileGroupId = packet[index++];
//            if (currState != ServerConstants.CURR_STATE_BASELINE) {
//                DeviceServiceImpl.getInstance().handleProfileMismatch(fixture, gChecksum, sChecksum, profileGroupId,
//                        ServerConstants.REPT_PM_DATA_MSG_TYPE);
//            } else {
//                profileLogger.debug(fixture.getId() + ": is in baseline mode..., skipping profile sync check.");
//                // Enable the push flag for pushing the profile to the fixture, when the fixture moves out of
//                // baseline...
//                fixtureMgr.enablePushGlobalProfileForFixture(fixture.getId());
//                fixtureMgr.enablePushProfileForFixture(fixture.getId());
//            }
//        }
//
//        timingLogger.debug("after profile processing : " + (System.currentTimeMillis() - startTime));
//        int rem = lastVolts % 5;
//        if (rem > 2) {
//            lastVolts += (5 - rem);
//        } else {
//            lastVolts -= rem;
//        }
//        float fPowerFactor = 0;
//        BallastVoltPower obvp = getBallastVoltPowerFactor(fixture.getBallast(), (float) lastVolts / 10);
//        if (obvp == null) {
//            logger.error("Error fetching VoltPowerMap: (" + lastVolts + " => " + fPowerFactor + ")");
//        } else {
//            fPowerFactor = obvp.getPower().floatValue();
//            logger.debug("Fetching from Ballast: " + obvp.getVoltPowerMapId() + ", VoltPowerMap for Bulb life: ("
//                    + lastVolts + " => " + fPowerFactor + ")");
//        }
//        timingLogger.debug("after ballast volt power : " + (System.currentTimeMillis() - startTime));
//        float voltPowerFactor = fPowerFactor;
//        if (lastVolts < 5) {
//            fixture.setLightLevel(minAmb / 14);
//        } else {
//            fixture.setLightLevel(maxAmb / 14);
//        }
//        setBulbLife(fixture, voltPowerFactor);
//
//        EnergyConsumption ec = new EnergyConsumption();
//        		
//        ec.setId(ecMgr.getEnergyConsumptionId(fixture.getId(), statsDate));
//
//        ec.setBrightOffset(lightOffToOn);
//        // this is the percentage of light
//        ec.setBrightPercentage((short) voltPowerFactor);
//
//        ec.setCaptureAt(statsDate);
//        ec.setAvgVolts(avgVolts);
//        ec.setDimOffset(lightOnToOff);
//        ec.setFixture(fixture);
//        ec.setLightAvgLevel((short) avgAmb);
//        ec.setLightMaxLevel(maxAmb);
//        ec.setLightMinLevel(minAmb);
//        ec.setLightOff(motionOnToOff);
//        ec.setLightOn(motionOffToOn);
//        ec.setMaxTemperature((short) maxTemp);
//        ec.setMinTemperature((short) minTemp);
//        if (ServerUtil.compareVersion(fixture.getVersion(), "2.0") < 0) {
//            avgTemp = (short) ((avgTemp * 9 / 5) + 32 - temperatureOffsetSU1);
//        } else {
//            avgTemp = (short) (((avgTemp) * 9 / 5) + 32);
//        }
//        ec.setAvgTemperature(avgTemp);
//        ec.setCurrState((short) currState);
//        ec.setEnergyCum(energyCumTicks);
//        ec.setEnergyCalib(energyCalib);
//        ec.setMinVolts((short) minVolts);
//        ec.setMaxVolts((short) maxVolts);
//        ec.setEnergyTicks(pulses);
//        // ec.setOccCount((short)motionCount);
//        // ec.setOccIn(new Short(rand.nextInt(100)+""));
//        // ec.setOccOut(new Short(rand.nextInt(100)+""));
//
//        double avgPower = -1;
//        double energyConsumed = -1;
//        Ballast ballast = fixture.getBallast();
//        boolean calculatePower = true;
//        if (fixture.getCommType() == ServerConstants.COMM_TYPE_PLC) {
//            calculatePower = true;
//        } else if (pmStatsMode == ServerConstants.PM_STATS_FIRMWARE_MODE) {
//            // if(energyCalib > 0 && pulses > 0) {
//            avgPower = getAvgPower(energyCalib, pulses, 5 * 60, fixture); // this is micro watts hour
//            // energyConsumed = getAvgEnergyInMWH(energyCalib, pulses) * 5 / 60;
//            calculatePower = false;
//            ec.setPowerCalc(ServerConstants.POWER_CALCULATED_SU);
//            // }
//        }
//        if (calculatePower) {
//            avgPower = getAvgPowerFromVolts(avgVolts, ballast.getLampNum(), ballast) * fixture.getNoOfFixtures(); // watts
//            ec.setPowerCalc(ServerConstants.POWER_CALCULATED_GEMS);
//        }
//        timingLogger.debug("after power calc : " + (System.currentTimeMillis() - startTime));
//        // ec.setDimPercentage((short)avgPower1);
//        logger.debug(fixture.getId() + ": received average power -- " + avgPower);
//
//        Double price = priceMgr.getPrice(statsDate);
//        ec.setPrice(price.floatValue());
//        double basePowerUsed = 0;
//
//        double compBasePowerUsed = ballast.getBallastFactor() * ballast.getWattage() * ballast.getLampNum()
//                * fixture.getNoOfFixtures();
//        if (avgPower > 400) { // 400 is the max for 2.0 enlighted relays
//            avgPower = compBasePowerUsed;
//            // faulty CU/SU
//            eventMgr.addAlarm(fixture, "Energy reading too high (" + avgPower + ")",
//                    EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR, EventsAndFault.MAJOR_SEV_STR);
//        }
//
//        energyConsumed = avgPower * 5 / 60; // watts hr
//        logger.debug(fixture.getId() + ": energy consumed -- " + energyConsumed);
//
//        ec.setPowerUsed(new BigDecimal(avgPower));
//        /*
//         * //get it from database basePowerUsed = fixture.getBaselinePower().doubleValue(); if (basePowerUsed == 0) { //
//         * base power was not updated in the database, get it from ballast info basePowerUsed =
//         * ballast.getBallastFactor() * ballast.getWattage() * ballast.getLampNum() fixture.getNoOfFixtures(); }
//         * 
//         * if(avgPower > basePowerUsed) { //if the avgPower is abnormal, don't take it if((avgPower - basePowerUsed) <=
//         * (avgPower * 10 / 100)) { //if the new average power is not more than base power by 10%, update the base power
//         * basePowerUsed = avgPower; } else { //assume that new average power is abnormal and take it as base power
//         * avgPower = basePowerUsed; } } else { if(currState == ServerConstants.CURR_STATE_BASELINE) { basePowerUsed =
//         * avgPower; } else if (avgVolts == 100) { // if the new value is less than previous base power by not more than
//         * 8% and voltage is 100 // set the new value as base power if ((basePowerUsed - avgPower) <= (basePowerUsed * 8
//         * / 100)) { basePowerUsed = avgPower; } } }
//         */
//        if (currState == ServerConstants.CURR_STATE_BASELINE) {
//            basePowerUsed = avgPower;
//        } else {
//            basePowerUsed = fixture.getBaselinePower().doubleValue();
//            if (basePowerUsed == 0) { // base power was not updated in the database, get it from ballast info
//                basePowerUsed = compBasePowerUsed;
//            }
//        }
//        if (avgPower > basePowerUsed) {
//            basePowerUsed = avgPower;
//        } else {
//            // if the new value is less than previous base power by not more than 8% and voltage is 100
//            // set the new value as base power
//            if (avgVolts == 100) {
//                if ((basePowerUsed - avgPower) <= (basePowerUsed * 8 / 100)) {
//                    basePowerUsed = avgPower;
//                }
//            }
//        }
//        if (device != null) {
//            device.setBasePower(basePowerUsed);
//        }
//        timingLogger.debug("after baseline : " + (System.currentTimeMillis() - startTime));
//        ec.setBasePowerUsed(new BigDecimal(basePowerUsed));
//        ec.setBaseCost((float) basePowerUsed * 5 * price.floatValue() / (1000 * 60));
//        // price is per kWH and energy consumed is milli WH
//        // ec.setCost((float)(energyConsumed * company.getPrice() / (1000000)));
//        ec.setCost((float) (energyConsumed * price.floatValue()) / 1000);
//        ec.setSavedCost(ec.getBaseCost() - ec.getCost());
//
//        double savedPower = basePowerUsed - avgPower;
//        BigDecimal savedPowerUsed = new BigDecimal(savedPower);
//        ec.setSavedPowerUsed(savedPowerUsed);
//        BigDecimal zeroSaving = new BigDecimal(0.0);
//        ec.setManualSaving(zeroSaving);
//        ec.setAmbientSaving(zeroSaving);
//        ec.setOccSaving(zeroSaving);
//        ec.setTuneupSaving(zeroSaving);
//        switch (savingType) {
//        case ServerConstants.STATS_SAVING_OCC:
//            ec.setOccSaving(savedPowerUsed);
//            break;
//        case ServerConstants.STATS_SAVING_AMB:
//        case ServerConstants.STATS_SAVING_TUNEUP:
//            adjustAmbTaskTunedSavings(fixture, ec, savedPower, avgVolts, savingType);
//            break;
//        case ServerConstants.STATS_SAVING_MANUAL:
//            ec.setManualSaving(savedPowerUsed);
//            break;
//        default:
//            ec.setOccSaving(savedPowerUsed);
//            break;
//        }
//        ec.setMotionBits(motionBitsLong);
//        ec.setZeroBucket(ORIG_BUCKET);
//        ecMgr.save(ec);
//        // want to catch the exception if the zero bucket is added for the same time period
////        try {
////            ecMgr.save(ec);
////            timingLogger.debug("time taken after saving the ec : " + (System.currentTimeMillis() - startTime));
////        } catch (Exception ex) {
////            // ex.printStackTrace();
////            long lastZeroBucketId = device.getLastZeroBucketId();
////            logger.error(fixture.getId() + ": error- zero bucket(" + lastZeroBucketId
////                    + ") added before original bucket for " + ec.getCaptureAt());
////            try {
////                if (lastZeroBucketId != -1) {
////                    ec.setId(lastZeroBucketId);
////                    device.setLastZeroBucketId(-1);
////                    ecMgr.update(ec);
////                }
////            } catch (Exception e1) {
////                // e1.printStackTrace();
////                logger.error(fixture.getId() + ": error in updating zero bucket- " + lastZeroBucketId);
////            }
////        }
////        long statsDateTime = statsDate.getTime();
////        if (fixture.getLastStatsRcvdTime() != null
////                && (statsDateTime - fixture.getLastStatsRcvdTime().getTime()) > FIVE_MINUTE_INTERVAL) {
////            // Bug #749 check for zero buckets also
////            Date lastZeroBucketTime = device.getLastZeroBucketTime();
////            if (lastZeroBucketTime == null || lastZeroBucketTime.getTime() < (statsDateTime - FIVE_MINUTE_INTERVAL)) {
////                addZeroBucket(device, fixture, statsDateTime);
////            }
////            addFixtureToZeroBucketUpdQueue(fixture.getId(), fixture.getLastStatsRcvdTime(), statsDate);
////        }
//        timingLogger.debug("after zero bucket :  " + (System.currentTimeMillis() - startTime));
//        try {
//            fixture.setLastStatsRcvdTime(statsDate);
//            fixture.setCurrApp((short) currApp);
//            fixture.setCurrentState(ServerUtil.getCurrentState(currState));
//            fixture.setWattage((int) avgPower);
//            fixture.setAvgTemperature(avgTemp);
//            fixture.setBaselinePower(new BigDecimal(basePowerUsed));
//            if (ServerUtil.compareVersion(fixture.getVersion(), ServerMain.getInstance().getGemsVersion()) == 0) {
//                // This will ensure that after the upgrade of SU image the version synced bit is cleared.
//                fixture.setVersionSynced(0);
//            }
//            fixtureMgr.updateStats(fixture);
//            timingLogger.debug("after fixture update :  " + (System.currentTimeMillis() - startTime));
//            // update the device cache attributes
//            if (device != null) {
//                device.setLastStateRcvdTime(statsDate);
//                device.setUptime(uptime);
//                device.setBootTime(new Date(System.currentTimeMillis() - uptime * 1000));
//                device.setEnergyCum(energyCumTicks);
//            }
//            raiseOutageAlarm(avgPower, minVolts, maxVolts, savingType, lastOccSeen, device, fixture);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//    } // end of method updateStats

//    private void raiseOutageAlarm(double avgPower, short minVolts, short maxVolts, int savingType, int lastOccSeen,
//            DeviceInfo device, Fixture fixture) {
//
//        logger.debug(fixture.getFixtureName() + ": outage data[minVolts=" + minVolts + ",maxVolts=" + maxVolts
//                + ",saving=" + savingType + ",lastOcc=" + lastOccSeen + ",power=" + avgPower + "]");
//        if (minVolts != maxVolts || !fixture.getCurrentState().equals(ServerConstants.CURR_STATE_AUTO_STR)
//                || lastOccSeen >= 300 || savingType == ServerConstants.STATS_SAVING_AMB) {
//            return;
//        }
//        // if the current power is less than 7 watts, raise an alarm that fixture is out
//        // in GE ballast which is parallel, we observed 6.9 watts when all bulbs are out
//        if (avgPower < FIXTURE_OUTAGE_DETECT_WATTS) {
//            logger.error(fixture.getFixtureName() + ": fixture is out. raising outage alarm");
//            eventMgr.addAlarm(fixture, "Fixture is out", EventsAndFault.FIXTURE_OUTAGE_EVENT_STR,
//                    EventsAndFault.CRITICAL_SEV_STR);
//            return;
//        }
//        // raiseBulbOutageAlarm(avgPower, minVolts, maxVolts, savingType, lastOccSeen,
//        // device, fixture);
//
//    } // end of method raiseOutageAlarm

    private void raiseBulbOutageAlarm(double avgPower, short minVolts, short maxVolts, int savingType, int lastOccSeen,
            DeviceInfo device, Fixture fixture) {

        // retrieve the database entry for this fixture for this voltage level
        Double dbPower = device.getOutageBasePower(minVolts);
        if (dbPower == null) { // it is not there in the database
            if (avgPower != 0) {
                device.addOutageBasePower(minVolts, avgPower);
            }
            return;
        }
        // logger.info(fixture.getFixtureName() + ": outage base power from db - " + dbPower);
        if ((dbPower - avgPower) / dbPower * 100 >= OUTAGE_DETECT_PERCENTAGE) {
            // it is off by more than 10%, so probably a light is out
            // raise bulb outage alarm
            logger.error(fixture.getFixtureName() + ": bulbs are out. raising outage alarm");
            eventMgr.addAlarm(fixture, "Fixture degraded (bulbs may be out)", EventsAndFault.FIXTURE_OUTAGE_EVENT_STR,
                    EventsAndFault.MAJOR_SEV_STR);
        } else {
            if ((avgPower - dbPower) / avgPower * 100 >= BASE_POWER_CORRECTION_PERCENTAGE) {
                // update the current average power in the db
                // logger.info(fixture.getFixtureName() + ": increase in the consumption, update db");
                device.updateOutageBasePower(minVolts, avgPower);
            }
        }

    } // end of method raiseBulbOutageAlarm

//    private void adjustAmbTaskTunedSavings(Fixture fixture, EnergyConsumption ec, double savedPower, short avgVolts,
//            int savingType) {
//
//        try {
//            long prHandId = fixture.getProfileHandler().getId();
//            ProfileHandler prHand = profileMgr.getProfileHandlerById(prHandId);
//            int maxOnLevel = prHand.getCurrentProfile().getOnLevel().intValue();
//            // If the SU has been in profile for which maxOnLevel = 100 for 2 mins and then moved to another profile for
//            // which maxOnLevel=60
//            // then the avgVolts could be 80, In this situation the % savings will be shown as more than 100% which is
//            // not correct.
//            if (avgVolts <= maxOnLevel) {
//                logger.debug(fixture.getId() + ": (" + avgVolts + " <= " + maxOnLevel + ")");
//                double ttSaving = (100 - maxOnLevel) * savedPower / (100 - avgVolts);
//                double ambSaving = savedPower - ttSaving;
//                ec.setAmbientSaving(new BigDecimal(ambSaving));
//                ec.setTuneupSaving(new BigDecimal(ttSaving));
//            } else {
//                logger.debug(fixture.getId() + ": (" + avgVolts + " > " + maxOnLevel + ")");
//                if (savingType == ServerConstants.STATS_SAVING_AMB)
//                    ec.setAmbientSaving(new BigDecimal(savedPower));
//                else
//                    ec.setTuneupSaving(new BigDecimal(savedPower));
//            }
//        } catch (Exception ex) {
//            logger.error(fixture.getId() + ": error getting current profile - " + ex.getMessage());
//            // if max on level could not be retrieved, then it will add the consumption entry with savings based
//            // on the savings type
//            if (savingType == ServerConstants.STATS_SAVING_AMB) {
//                ec.setAmbientSaving(new BigDecimal(savedPower));
//            } else {
//                ec.setTuneupSaving(new BigDecimal(savedPower));
//            }
//        }
//
//    } // end of method adjustAmbTaskTunedSavings

    private void parseEvents(String snapId, String eventMask) {

        byte[] eventBytes = eventMask.getBytes();
        EventsAndFault event = null;
        if ((eventBytes[1] & 128) == 1) { // 1000 0000
            // temp high
            event = new EventsAndFault();
            event.setEventType("Temperature High");
            event.setEventTime(new Date());
            event.setDevice(fixtureMgr.getFixtureBySnapAddr(snapId));
            eventMgr.save(event);
        }
        if ((eventBytes[1] & 64) == 1) { // 0100 0000
            // temp low
            event = new EventsAndFault();
            event.setEventType("Temperature Low");
            event.setEventTime(new Date());
            event.setDevice(fixtureMgr.getFixtureBySnapAddr(snapId));
            eventMgr.save(event);
        }
        if ((eventBytes[1] & 32) == 1) { // 0010 0000
            // light fixture out
            event = new EventsAndFault();
            event.setEventType("Light Fixture Out");
            event.setEventTime(new Date());
            event.setDevice(fixtureMgr.getFixtureBySnapAddr(snapId));
            eventMgr.save(event);
        }
        if ((eventBytes[1] & 16) == 1) { // 0001 0000
            //
        }

    } // end of method parseEvents

//    /*
//     * mask1 is 1st word, mask2 is 2nd word mask2 represents most recent word in the 5 minute interval 300 seconds are
//     * represented by 60 bits of which 32 bits are in mask1, 28 bits are in mask2 remaining 4 most significant bits of
//     * word 2 are ignored in a word, more recent 5 second bits are msb. for ease of calculation, bits are numbered 0
//     * through 31 from msb instead of lsb first mask2 is looked into to find the most recent occupancy from 4th bit and
//     * then mask1 is looked into if occupancy is not observed in mask2 from msb
//     */
//    public static int getLastOccupancySec(int mask1, int mask2) {
//
//        byte[] mask2ByteArray = ServerUtil.intToByteArray(mask2);
//        for (int i = 4; i < 32; i++) {
//            if (getBit(mask2ByteArray, i) == 1) {
//                return 5 * (i - 3);
//            }
//        }
//
//        byte[] maskByteArray = ServerUtil.intToByteArray(mask1);
//        for (int i = 0; i < 32; i++) {
//            if (getBit(maskByteArray, i) == 1) {
//                return 5 * (i + 29);
//            }
//        }
//        return 300;
//
//    } // end of method getLastOccupancySec
    
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

//    public void addFixtureToZeroBucketUpdQueue(Long fixtureId, Date lastStatsRcvd, Date latestStatsRcvd) {
//
//        synchronized (zeroBucketUpdQueue) {
//            if (!zeroBucketUpdQueue.contains(fixtureId)) {
//                zeroBucketUpdQueue.add(fixtureId);
//                ZeroBucketStruct struct = new ZeroBucketStruct(fixtureId, lastStatsRcvd, latestStatsRcvd);
//                zeroBucketUpdFixtureMap.put(fixtureId, struct);  
//            }
//        }
//
//    } // end of method addFixtureToMissingQueue

//    public class UpdateZeroBucketUpdHandlerThread extends Thread {
//
//        public void run() {
//            
//        	int intervalToSleep = FIVE_MINUTE_INTERVAL;
//        	
//            while (true) {
//                if (zeroBucketUpdQueue.size() == 0) {
//                    ServerUtil.sleep(intervalToSleep);
//                }
//                while (zeroBucketUpdQueue.size() > 0) {
//                    updateZeroBuckets(zeroBucketUpdQueue.peek());
//                    zeroBucketUpdQueue.poll();
//                }
//            }
//
//        } // end of method run
//
//    } // end of class ZeroBucketUpdHandlerThread

//    private void updateZeroBuckets(Long fixtureId) {
//
//        logger.debug(fixtureId + ":update zero buckets");
//        ZeroBucketStruct struct = zeroBucketUpdFixtureMap.get(fixtureId);
//        DeviceInfo device = ServerMain.getInstance().getDevice(fixtureId);
//        // this is the start time from when zero buckets have to be filled. this is 5 minutes
//        // after last stats received
//        Date startTime = null;
//        Date lastStatsRcvdTime = struct.lastStatsRcvdTime;
//        if (device != null) {
//            startTime = device.getBootTime();
//        }
//        if (startTime == null || startTime.before(struct.lastStatsRcvdTime)) {
//            startTime = new Date(struct.lastStatsRcvdTime.getTime() + FIVE_MINUTE_INTERVAL);
//        } else {
//            long startMillis = startTime.getTime();
//            startMillis = startMillis + (FIVE_MINUTE_INTERVAL - startMillis % FIVE_MINUTE_INTERVAL);
//            startTime.setTime(startMillis);
//            lastStatsRcvdTime = new Date(startMillis - FIVE_MINUTE_INTERVAL);
//        }
//        if (startTime.getTime() >= struct.latestStateRcvdTime.getTime()) {
//            // we got the first stats after reboot so no missing
//            return;
//        }
//        logger.debug(fixtureId + ":update zero buck:startTime - " + startTime + " last received time - "
//                + struct.lastStatsRcvdTime + " latest received time - " + struct.latestStateRcvdTime);
//        ecMgr.updateZeroBuckets(fixtureId, lastStatsRcvdTime, startTime, struct.latestStateRcvdTime);
//
//    } // end of method updateZeroBuckets
//
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
