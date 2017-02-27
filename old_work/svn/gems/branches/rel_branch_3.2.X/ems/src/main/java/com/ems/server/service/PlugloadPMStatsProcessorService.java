package com.ems.server.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.DeviceInfo;
import com.ems.cache.PlugloadCache;
import com.ems.model.Plugload;
import com.ems.model.PlugloadEnergyConsumption;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.device.plugload.PlugloadImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.PlugloadEnergyConsumptionManager;
import com.ems.service.PlugloadManager;
import com.ems.service.PlugloadProfileManager;
import com.ems.service.PricingManager;

@Service("plugloadPMStatsProcessorService")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadPMStatsProcessorService {

	private static final Logger logger = Logger.getLogger("Perf");
	private static Logger timingLogger = Logger.getLogger("TimingLogger");
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");

	public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;
	private static short ORIG_BUCKET = 0;
	
	public static final long LONG_MINUS_ONE = 4294967295L;
	public static final long SHORT_MINUS_ONE = 65535;
	private static final int MAX_CU2_LOAD = 600;
	
	@Resource
	private PlugloadManager plugloadManager;

	@Resource
	private PlugloadEnergyConsumptionManager plugloadEnergyConsumptionManager;

	@Resource
	private PricingManager pricingManager;

	@Resource
	private PlugloadProfileManager plugloadProfileManager;
	
	public void setPlugloadManager(PlugloadManager plugloadManager) {
		
		this.plugloadManager = plugloadManager;
		
	}

	public void setEnergyConsumptionManager(PlugloadEnergyConsumptionManager plugloadEnergyConsumptionManager) {
		
		this.plugloadEnergyConsumptionManager = plugloadEnergyConsumptionManager;
		
	} 

	public void setPricingManager(PricingManager pricingManager) {
		
		this.pricingManager = pricingManager;
		
	}

/*
	public void processStats(List<PMStatsWork> processingQueue) {

		long startTime = System.currentTimeMillis();

		try {
			plugloadDao.getSession().setFlushMode(FlushMode.MANUAL);
			//System.out.println("no. of stats -- " + processingQueue.size());
			for (PMStatsWork statsWork : processingQueue) {
				updateStats(statsWork.getFixture(), statsWork.getPacket(),
						statsWork.getSeqNo());
			}

			plugloadDao.getSession().flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug("Packet Processing (Stats):"
				+ (System.currentTimeMillis() - startTime) + " Processed: "
				+ processingQueue.size());
		}
		processingQueue = null;
		
	}
	*/
	private void updateStats(Plugload plugload, byte[] packet, int seqNo) {

		long startTime = System.currentTimeMillis();
		DeviceInfo device = PlugloadCache.getInstance().getDevice(plugload);
		if (device != null && device.getLastStatsSeqNo() == seqNo) {
			// duplicate packet ignore it
			logger.debug(plugload.getId() + ": Duplicate stats received");
			packet = null;
			plugload = null;
			return;
		}
		if(device.isFirstStatsAfterCommission()) {
		  logger.debug(plugload.getId() + ": ignoring the first stats after commission");
		  device.setFirstStatsAfterCommission(false);
		  return;
		}

		long currentMillis = System.currentTimeMillis();
		long min5 = 5 * 60 * 1000;
		currentMillis = currentMillis - currentMillis % min5;

		// if the last stats received time is past 5 minutes and
		// if the seq no. is +1 of last seq no. then the this stats is for
		// previous period
		if (device.getLastStatsSeqNo() + 1 == seqNo
				&& currentMillis - device.getLastStatsRcvdTime().getTime() > FIVE_MINUTE_INTERVAL) {
			currentMillis -= FIVE_MINUTE_INTERVAL;
		}
		Date statsDate = new Date(currentMillis);
		byte[] tempShortByteArr = new byte[2];
		byte[] tempIntByteArr = new byte[4];

		int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
		
		PlugloadEnergyConsumption ec = new PlugloadEnergyConsumption();
		
		// min volts (100/0)
		byte minVolts = packet[index++];
		ec.setMinVolts((short)minVolts);
		
		// max volts (100/0)
		byte maxVolts = packet[index++];
		ec.setMaxVolts((short)maxVolts);
		
		// avg volts (100/0)
		byte avgVolts = packet[index++];
		ec.setAvgVolts((float)avgVolts);
		
		// current volts (100/0)
		byte currentVolts = packet[index++];
		ec.setLastVolts((short)currentVolts);
		
		// switched on sec. time in seconds the switched control was on
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short managedOnSecs = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setManagedOnSecs(managedOnSecs);
		index += 2;

		// switched_on_to_off;   zero or sec in the period when switch went from on to off
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short managedOnToOffSec = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setManagedOnToOffSec(managedOnToOffSec);
		index += 2;

    //uint16_t switched_off_to_on;  zero or sec in the period when switch went from off to on
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short managedOffToOnSec = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setManagedOffToOnSec(managedOffToOnSec);
		index += 2;
		
    //uint16_t motion_heard_secs_ago;  // last motion heard of from peers.
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int lastMotionSecsAgo = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setLastMotionSecsAgo(lastMotionSecsAgo);
		index += 2;

		//int16_t min_temperature;  // Temperature of the CU part of the plugload sent in tenths of degrees of C.
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short minTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setMinTemperature((float)minTemp/10);
		index += 2;
		
		//int16_t  max_temp; // last measured temp in tenths of Celsius, initialized to 65535 (max value) at the beginning of the period 
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short maxTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setMaxTemperature((float)maxTemp/10);
		index += 2;
			
    //int16_t acvg_temperature;  // Temperature of the CU part of the plugload sent in tenths of degrees of C.
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short avgTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setAvgTemperature((float)avgTemp/10);
		index += 2;

    //int16_t  last_temp; // last measured temp in tenths of Celsius, initialized to 65535 (max value) at the beginning of the period 
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short lastTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setLastTemperature((float)lastTemp/10);
		index += 2;
                                                                     
		//uint8_t  savings;  // savings type (0=none, 1=occ, 2=amb, 3=task, 4=manual)
		byte savingType = packet[index++];
		ec.setSavingType(savingType);

    //uint8_t  currState; 
		// current state (0=UNKNOWN, 1=INITIAL_ON, 2=SWITCHED_ON, 3=SWITCHED_OFF, 4=MANUAL_OVERRIDE, 5=VALIDATION, 6=BASELINE
		byte currState = packet[index++];
		ec.setCurrentState(currState);

    //uint8_t curr_behavior;      
    // SCHED_BHV_ALWAYS_ON = 0, SCHED_BHV_ALWAYS_OFF = 1, SCHED_BHV_AUTO = 2, SAFETY_BHV_ON = 3, SAFETY_BHV_OFF = 4,
    // MAN_OVD_BHV_ON = 5, MAN_OVD_BHV_OFF = 6.
		byte currBehavior = packet[index++];
		ec.setCurrentBehavior(currBehavior);

    //uint32_t sysUpTimeSecs; // value of suvars.sysUpTime at end of period
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long uptime = ServerUtil.intByteArrayToLong(tempIntByteArr);
		if(logger.isDebugEnabled()) {
			logger.debug(plugload.getId() + ": uptime of node -- " + uptime);
		}
		ec.setSysUptime(uptime);
		index += 4;

    //uint8_t  num_load_changes; // Number of Load changes observed on the Switched Outlet in this period.
		byte noOfLoadChanges = packet[index++];
		ec.setNoOfLoadChanges((short)noOfLoadChanges);

    //uint8_t  num_peers_heard_from; // Number of Sensor peers heard from across all groups.
		byte noOfPeersHeardFrom = packet[index++];
		ec.setNoOfPeersHeardFrom((short)noOfPeersHeardFrom);

    //uint16_t energyTicks1;       // Switched Load's energy used in the period, only sampled at the end of the period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int managedEnergy = ServerUtil.byteArrayToShort(tempShortByteArr);		
		ec.setEnergy(new BigDecimal(managedEnergy));
		index += 2;

    //uint32_t energy_cum1;        // Switched Load's cumlative energy used, only updated at the end of the period
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long managedEnergyCum = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		ec.setManagedEnergyCum(managedEnergyCum);
		index += 4;

    //uint16_t energyTicks2;       // UnSwitched Load's energy used in the period, only sampled at the end of the period
    System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedEnergy = ServerUtil.byteArrayToShort(tempShortByteArr);		
		ec.setUnmanagedEnergy(new BigDecimal(unmanagedEnergy));
		index += 2;

    //uint32_t energy_cum2;        // Unswitched Load's cumlative energy used, only updated at the end of the period   
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long unmanagedEnergyCum = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		ec.setUnmanagedEnergyCum(unmanagedEnergyCum);
		index += 4;
 
    //int16_t power_1_sample1;                        // -1 indicates error
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int managedPower = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setManagedLastLoad(new BigDecimal(managedPower));
		index += 2;
		
		//int16_t current_1_sample1;                        // -1 indicates error
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int managedCurrent = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setManagedCurrent(new BigDecimal(managedCurrent));
		index += 2;
		
    //int16_t powerfactor_1_sample1;              // -1 indicates error
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int managedPowerFactor = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setManagedPowerFactor(new BigDecimal(managedPowerFactor));
		index += 2;
		
    //int16_t power_2_sample1;              // -1 indicates error
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedPower = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setUnmanagedLastLoad(new BigDecimal(unmanagedPower));
		index += 2;
		
		//int16_t power_1_sample1;                        // -1 indicates error
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedCurrent = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setUnmanagedCurrent(new BigDecimal(unmanagedCurrent));
		index += 2;
			
    //int16_t powerfactor_2_sample1;              // -1 indicates error
    System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedPowerFactor = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setUnmanagedPowerFactor(new BigDecimal(unmanagedPowerFactor));
		index += 2; 
 
		/*
    struct {
        uint8_t pass;
        uint8_t fail;
    } cu_cmd_stats;             // value of suvars.cu_cmd_stats at end of period
		 */
		byte cuCmdStatus = packet[index++];
		ec.setCuCmdStatus((int)cuCmdStatus);
		
    //uint16_t cuStatus;          // value of suvars.cuStatus at end of period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int cuStatus = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setCuStatus(cuStatus);
		index += 2;
		
    //uint16_t numResetsByCu;     // value of suvars.numResetsByCu at end of period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int noCuResets = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setNoOfCuResets(noCuResets);
		index += 2;
		
    //uint8_t  current_app;       // value of suvars.which_app at end of period
		byte currentApp = packet[index++];
		ec.setCurrentApp(currentApp);
		
    //uint8_t  global_profile_cksum;  // computed value of checksum for global profile when sent
		byte globalProfileChecksum = packet[index++];

    //uint8_t  schedule_profile_cksum;// computed value of checksum for schedule profile when sent
		byte schedProfileChecksum = packet[index++];

    //uint8_t  profile_group_id;      // profile ID when sent
		byte profileGrpId = packet[index++];
		
    //uint16_t config_chksum;     // Chksum of all the SU config known to EM
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int configChecksum = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

    //uint32_t utc_time_secs;     // UTC (GMT) time in seconds (zero means - not set)
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long utcTimeSecs = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		index += 4;
		if(logger.isDebugEnabled()) {
			logger.debug(plugload.getId() + ": pm stats utc date -- " + new Date(utcTimeSecs * 1000l));
		}
		if(utcTimeSecs > 0 && (utcTimeSecs % (5 * 60) == 0)) {
			statsDate = new Date(utcTimeSecs * 1000l);				
		}

    //uint16_t interval_dura;     // interval duration in seconds
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int intervalDur = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ":after packet processing: " + (System.currentTimeMillis() - startTime));
		}
		if (currState != ServerConstants.CURR_STATE_BASELINE) {
			PlugloadImpl.getInstance().handleProfileMismatch(plugload, globalProfileChecksum, schedProfileChecksum, profileGrpId,
					ServerConstants.REPT_PM_DATA_MSG_TYPE);
		} else {
			if(profileLogger.isDebugEnabled()) {
				profileLogger.debug(plugload.getId() + ": is in baseline mode..., skipping profile sync check.");
			}
			plugloadManager.enablePushProfileAndGlobalPushProfile(plugload.getId(), true, true);
		}
		if(logger.isInfoEnabled()) {
			logger.info(plugload.getId() + "(" + plugload.getGroupsSyncPending() + "): groups checksum -- " + configChecksum);
		}
		if(!plugload.getGroupsSyncPending()) {
			PlugloadImpl.getInstance().handleGroupsSynchronization(plugload, configChecksum);
		}
		
		double avgPower = -1;
		double energyConsumed = -1;
		
		if(logger.isDebugEnabled()) {
			logger.debug(plugload.getId() + ": received energy -- " + managedEnergy);
		}

		Double price = pricingManager.getPrice(statsDate);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ": after price " + (System.currentTimeMillis() - startTime));
		}
		ec.setPrice(price.floatValue());
		double basePowerUsed = 0;

		//@TODO do we need this?
		if (avgPower > MAX_CU2_LOAD) { // 600 is the max for 2.0 enlighted relays			
			// faulty CU/SU
		  logger.error(plugload.getId() + ": Energy reading too high(" + avgPower + ")");
			//avgPower = compBasePowerUsed;
		}
		
		//@TODO do we need this
		if(intervalDur > 300) {
			//short term fix so that base line is not increased  because of a longer duration pm stats
			//TODO fix it correctly so that only load corresponding to avg volts is taken into this bucket and the rest be added to the previous bucket
			logger.error(plugload.getId() + ": received a longer duration stats - " + intervalDur);
			if(avgPower > basePowerUsed) {
				avgPower = basePowerUsed;
			}		
		}
	
		if (device != null) {
			device.setBasePower(basePowerUsed);
		}
		double baseEnergy = 0;
		ec.setBaseCost((float) baseEnergy * 5 * price.floatValue() / (1000 * 60));	
		ec.setCost((float) (energyConsumed * price.floatValue()) / 1000);
		ec.setSavedCost(ec.getBaseCost() - ec.getCost());

		
		double savedEnergy = baseEnergy - managedEnergy;
		ec.setSavedEnergy(new BigDecimal(savedEnergy));
		BigDecimal zeroSaving = new BigDecimal(0.0);
		ec.setManualSaving(zeroSaving);
		ec.setOccSaving(zeroSaving);
		ec.setTuneupSaving(zeroSaving);
				
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ":after power saving handle : " + (System.currentTimeMillis() - startTime));
		}
		
		if(savedEnergy > 0) {
			adjustTaskTunedSavings(plugload, ec, savedEnergy, savingType);		  
		}	
		
		ec.setZeroBucket(ORIG_BUCKET);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ":after adjust task/amb: " + (System.currentTimeMillis() - startTime));	
		}
		if((currentMillis - device.getLastStatsRcvdTime().getTime()) >= FIVE_MINUTE_INTERVAL) {
		  try {
		  	plugloadEnergyConsumptionManager.save(ec);
		  } catch(Exception e) {
			logger.error(plugload.getId() + ": could not insert the stats -- " + e.getMessage());
		  }
		  device.setLastStatsSeqNo(seqNo);
		} else {
		  logger.error(plugload.getId() + ": Stats came too early");
		}		
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ":time for saving ec :  " + (System.currentTimeMillis() - startTime));
		}
		
		long statsDateTime = statsDate.getTime();
		Date fixtLastStatsRcvdTime = device.getLastStatsRcvdTime();
		try {
			plugload.setLastStatsRcvdTime(statsDate);
			plugload.setCurrApp((short) currentApp);
			plugload.setCurrentState(ServerUtil.getCurrentState(currState));
			plugload.setManagedLoad((float)managedPower);
			plugload.setAvgVolts(ec.getAvgVolts());
			plugload.setAvgTemperature(ec.getAvgTemperature());		
			plugload.setLastConnectivityAt(new Date());
			plugloadManager.updateStats(plugload);
			if(timingLogger.isDebugEnabled()) {
			  timingLogger.debug(plugload.getId() + ":time for saving plugload :  " + (System.currentTimeMillis() - startTime));
			}

			// update the device cache attributes
			if (device != null) {
				device.setLastStatsRcvdTime(statsDate);
				device.setUptime(uptime);
				device.setBootTime(new Date(System.currentTimeMillis() - uptime
						* 1000));
				device.setEnergyCum(managedEnergyCum);
			}	
				
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if ( fixtLastStatsRcvdTime != null
			&& (statsDateTime - fixtLastStatsRcvdTime.getTime()) > FIVE_MINUTE_INTERVAL) {
		  // Bug #749 check for zero buckets also
		  Date lastZeroBucketTime = device.getLastZeroBucketTime();
		  if (lastZeroBucketTime == null || lastZeroBucketTime.getTime() < (statsDateTime - FIVE_MINUTE_INTERVAL)) {
		  	//@TODO need to handle zero buckets based on Shrihari's implementation
		    //PerfSO.getInstance().addZeroBucket(device, plugload, statsDateTime);
		  }
		  PerfSO.getInstance().addFixtureToZeroBucketUpdQueue(plugload.getId(), fixtLastStatsRcvdTime, statsDate, false);		 
		  if(timingLogger.isDebugEnabled()) {
		    timingLogger.debug(plugload.getId() + ":after adding zero bucket updates:  " + (System.currentTimeMillis() - startTime));
		  }
		}
		if(timingLogger.isInfoEnabled()) {
		  timingLogger.info(plugload.getId() + ": Processing Single Packet :" + (System.currentTimeMillis() - startTime) );
		}
		plugload = null;
		device = null;
		ec = null;		
		packet = null;

	} // end of method updateStats

	private void adjustTaskTunedSavings(Plugload plugload, PlugloadEnergyConsumption ec, double savedEnergy, int savingType) {

		try {			
			Calendar currentDate = Calendar.getInstance();
			int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60 + currentDate.get(Calendar.MINUTE);
			int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
			if (dayOfWeek == 0) {
				dayOfWeek = 7;
			}
			int mode = plugloadProfileManager.getProfileModeForPlugload(plugload.getId(), dayOfWeek, minOfDay);
			if(mode == 2) { //always off then it is task tuned savings
				ec.setTuneupSaving(new BigDecimal(savedEnergy));
			} else {
				ec.setOccSaving(new BigDecimal(savedEnergy));
			}
		} catch (Exception ex) {
			logger.error(plugload.getId() + ": error getting current profile - " + ex.getMessage());
			ec.setOccSaving(new BigDecimal(savedEnergy));			
		}

	} // end of method adjustTaskTunedSavings

}
