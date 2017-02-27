package com.ems.server.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.DeviceInfo;
import com.ems.cache.PlugloadCache;
import com.ems.model.EventsAndFault;
import com.ems.model.Plugload;
import com.ems.model.PlugloadEnergyConsumption;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.data.PlugloadPMStatsWork;
import com.ems.server.device.plugload.PlugloadImpl;
import com.ems.server.processor.PlugloadECPersistor;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.PlugloadManager;
import com.ems.service.PlugloadProfileManager;
import com.ems.service.PricingManager;
import com.ems.utils.DateUtil;

@Service("plugloadPMStatsProcessorService")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadPMStatsProcessorService {

	private static final Logger logger = Logger.getLogger("Perf");
	private static Logger timingLogger = Logger.getLogger("TimingLogger");
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");

	public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;
		
	@Resource
	private PlugloadManager plugloadManager;

	@Resource
	private PricingManager pricingManager;

	@Resource
	private PlugloadProfileManager plugloadProfileManager;
	@Resource
	private EventsAndFaultManager eventsAndFaultManager;
	
	public void setPlugloadManager(PlugloadManager plugloadManager) {
		
		this.plugloadManager = plugloadManager;
		
	}

	public void setPricingManager(PricingManager pricingManager) {
		
		this.pricingManager = pricingManager;
		
	}
	
	public void setEventsAndFaultManager(EventsAndFaultManager eventsAndFaultManager) {
		
		this.eventsAndFaultManager = eventsAndFaultManager;
		
	}

	public void processStats(List<PlugloadPMStatsWork> processingQueue) {

		long startTime = System.currentTimeMillis();
		PlugloadPMStatsWork statsWork = null;
		try {
			//plugloadDao.getSession().setFlushMode(FlushMode.MANUAL);
			//System.out.println("no. of stats -- " + processingQueue.size());
			final ListIterator<PlugloadPMStatsWork> itr = processingQueue.listIterator();
			while (itr.hasNext()) {
				statsWork = itr.next();
				updateStats(statsWork.getPlugload(), statsWork.getPacket(), statsWork.getSeqNo());
			}
			//plugloadDao.getSession().flush();
		} catch (Exception ex) {
			String macaddress = "";
			if (statsWork != null) {
				if (statsWork.getPlugload() != null) {
					macaddress = statsWork.getPlugload().getMacAddress();
				}
			}
			logger.error(
			macaddress
					+ ": Error in PL PMstats processing! (QueueSize: "
					+ String.valueOf(processingQueue == null ? 0
							: processingQueue.size())
					+ ", AvailableVMMemory: "
					+ Runtime.getRuntime().freeMemory() + ")", ex);
			logger.error(macaddress+" PL Available Memory after exception:"+Runtime.getRuntime().freeMemory());
		}finally{
			if(timingLogger.isDebugEnabled()) {
			  timingLogger.debug("PL Packet Processing (Stats):"
					+ (System.currentTimeMillis() - startTime) + " Processed: "
					+ processingQueue.size());
			}
			statsWork = null;
			processingQueue.clear();
			processingQueue = null;
		}
		
	}
	
	private void updateStats(Plugload plugload, byte[] packet, int seqNo) {
		
		long startTime = System.currentTimeMillis();
		DeviceInfo device = PlugloadCache.getInstance().getDevice(plugload);
		if (device != null && device.getLastStatsSeqNo() == seqNo) {
			// duplicate packet ignore it
			logger.error(plugload.getId() + ": Duplicate stats received");
			packet = null;
			plugload = null;
			return;
		}
		if(device.isFirstStatsAfterCommission()) {
			if(logger.isInfoEnabled()) {
				logger.info(plugload.getId() + ": ignoring the first stats after commission");
			}
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
		if(logger.isDebugEnabled()) {
			logger.debug("max volts -- " + maxVolts);
		}
		
		// avg volts (100/0)
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short avgVolts = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setAvgVolts((float)avgVolts);
		index += 2;
		
		// last volts (100/0)
		byte currentVolts = packet[index++];
		ec.setLastVolts((short)currentVolts);
		
		//int16_t min_temperature;  // Temperature of the CU part of the plugload sent in tenths of degrees of C.
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short minTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);		
		ec.setMinTemperature(((new Float(minTemp/10)) * 9 / 5) + 32);		
		index += 2;
		
		//int16_t  max_temp; // last measured temp in tenths of Celsius, initialized to 65535 (max value) at the beginning of the period 
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short maxTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setMaxTemperature(((new Float(maxTemp/10)) * 9 / 5) + 32);
		index += 2;
				
		//int32_t avg_temperature;  // Temperature of the CU part of the plugload sent in tenths of degrees of C.
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		int avgTemp = ServerUtil.byteArrayToInt(tempIntByteArr);
		ec.setAvgTemperature(((new Float(avgTemp/10)) * 9 / 5) + 32);
		index += 4;
		if(logger.isDebugEnabled()) {
			logger.debug("avg temperature =-= " + avgTemp);
		}
		
		//int16_t  last_temp; // last measured temp in tenths of Celsius, initialized to 65535 (max value) at the beginning of the period 
		//@TODO do we need any adjustments
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short lastTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setLastTemperature(((new Float(lastTemp/10)) * 9 / 5) + 32);
		index += 2;
		
		//uint16_t energyTicks1;       // Switched Load's energy used in the period, only sampled at the end of the period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int managedEnergy = ServerUtil.byteArrayToShort(tempShortByteArr);		
		index += 2;
		if(logger.isDebugEnabled()) {
			logger.debug("managed energy -- " + managedEnergy);
		}

		//uint32_t energy_cum1;        // Switched Load's cumlative energy used, only updated at the end of the period
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long managedEnergyCum = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		if(managedEnergyCum != PMStatsProcessorService.LONG_MINUS_ONE * 100) {
			//proper value received
			ec.setManagedEnergyCum(managedEnergyCum);
		}	
		index += 4;
		if(logger.isDebugEnabled()) {
			logger.debug("cumulative managed energy -- " + managedEnergyCum);
		}

		//uint16_t energyTicks2;       // UnSwitched Load's energy used in the period, only sampled at the end of the period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedEnergy = ServerUtil.byteArrayToShort(tempShortByteArr);		
		if(unmanagedEnergy != PMStatsProcessorService.SHORT_MINUS_ONE) {
			ec.setUnmanagedEnergy(new BigDecimal((float)unmanagedEnergy/1000));
			ec.setUnmanagedLastLoad(new BigDecimal((float)(12 * ec.getUnmanagedEnergy().doubleValue())));
		} else {
			ec.setUnmanagedEnergy(new BigDecimal(0));
			ec.setUnmanagedLastLoad(new BigDecimal(0));
		}
		index += 2;
		ec.setBaseUnmanagedEnergy(ec.getUnmanagedEnergy());
		ec.setSavedUnmanagedEnergy(new BigDecimal(0));

		//uint32_t energy_cum2;        // Unswitched Load's cumlative energy used, only updated at the end of the period   
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long unmanagedEnergyCum = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		if(unmanagedEnergyCum != PMStatsProcessorService.LONG_MINUS_ONE * 100) {
			//proper value received
			ec.setUnmanagedEnergyCum(unmanagedEnergyCum);
		} else {
			ec.setUnmanagedEnergyCum(-1L);
		}
		index += 4;
			
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
                               
		//uint16_t motion_off_to_on;  // zero or sec in the period the last time when went from
    //      no motion to motion
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short motionOffToOn = (short) ServerUtil.byteArrayToShort(tempShortByteArr);		
		index += 2;
		
		//uint16_t motion_on_to_off;  // zero or sec in the period the last time motion timer expired
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		short motionOnToOff = (short) ServerUtil.byteArrayToShort(tempShortByteArr);		
		index += 2;

		//uint8_t  savings;  // savings type (0=none, 1=occ, 2=amb, 3=task, 4=manual)
		byte savingType = packet[index++];
		ec.setSavingType(savingType);
		if(logger.isDebugEnabled()) {
			logger.debug("saving tye == " + savingType);
		}

    //uint8_t  currState; 
		// current state (0=UNKNOWN, 1=INITIAL_ON, 2=SWITCHED_ON, 3=SWITCHED_OFF, 4=MANUAL_OVERRIDE, 5=VALIDATION, 6=BASELINE
		byte currState = packet[index++];
		ec.setCurrentState(currState);

    //uint32_t sysUpTimeSecs; // value of suvars.sysUpTime at end of period
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		long uptime = ServerUtil.intByteArrayToLong(tempIntByteArr);
		if(logger.isDebugEnabled()) {
			logger.debug(plugload.getId() + ": uptime of node -- " + uptime);
		}
		ec.setSysUptime(uptime);
		index += 4;
		
		/*
    struct {
        uint8_t pass;
        uint8_t fail;
    } cu_cmd_stats;             // value of suvars.cu_cmd_stats at end of period
		 */
		byte cuCmdPassStats = packet[index++];
		ec.setCuCmdStatus((int)cuCmdPassStats);
		
		byte cuCmdFailStats = packet[index++];
		
    //uint16_t cuStatus;          // value of suvars.cuStatus at end of period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int cuStatus = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setCuStatus(cuStatus);
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
		long utcTimeSecs = ServerUtil.intByteArrayToLong(tempIntByteArr);
		index += 4;
		if(logger.isDebugEnabled()) {
			logger.debug(plugload.getId() + ": pm stats utc date -- " + new Date(utcTimeSecs * 1000l));
		}
		if(utcTimeSecs > 0 && (utcTimeSecs % (5 * 60) == 0)) {
			statsDate = new Date(utcTimeSecs * 1000l);				
		}
		ec.setCaptureAt(statsDate);
		
		//uint16_t interval_dura;     // interval duration in seconds
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int intervalDur = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;
		if(logger.isDebugEnabled()) {
			logger.debug("interval duration -- " + intervalDur);
		}
		
		//uint16_t config_chksum_bmap;// This field gives the config bitmap to
    // indicate the config used for computing the config checksum
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int configChsumMap = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;
		
    //uint8_t curr_mode;          // one of the following: autos(101), baseline(102),
    //   or bypass(103)		
		byte currMode = packet[index++];

		//uint8_t curr_prof_type;     // current profile type See profile_types_t, values are:
    //   WEEK_DAY_PROFILE(0), WEEK_END_PROFILE(1), OVERRIDE_PROFILE(2), and OVERRIDE_EXT1_PROFILE(3)
		byte currProfType = packet[index++];
		
		//uint8_t curr_period_type;   // current profile period of day type or override type.
    // when curr_prof_type is WEEK_DAY_PROFILE(0) or
    //      WEEK_END_PROFILE(1), the values are:
    //   MORNING_PROFILE(0), DAY_PROFILE(1), EVENING_PROFILE(2), or NIGHT_PROFILE(3),
    // when curr_prof_type is OVERRIDE_PROFILE(2) or OVERRIDE_EXT1_PROFILE(3), the values are:
    //   OVERRIDE_PROFILE_NONE(0), OVERRIDE_PROFILE_1(1), OVERRIDE_PROFILE_2(2), OVERRIDE_PROFILE_3(3), 
    //   OVERRIDE_PROFILE_4(4), OVERRIDE_PROFILE_5(5) OVERRIDE_PROFILE_6(6), OVERRIDE_PROFILE_7(7), or
    //   OVERRIDE_PROFILE_8(8)
		byte currPeriodType = packet[index++];
		
		//uint32_t loc_motion_secs_ago; // how long ago in secs that motion was locally detected ,
    //    (note: a value of 0xffffffff means no motion
    //         since boot (approx 136 years before rollover)
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		int lastLocalMotionSecsAgo = ServerUtil.byteArrayToShort(tempIntByteArr);
		ec.setLastMotionSecsAgo(lastLocalMotionSecsAgo);
		index += 4;
		
		//uint32_t rem_motion_secs_ago;// how long ago in secs that remote motion was detected
    //    (note: a value of 0xffffffff means no motion
    //         since boot (approx 136 years before rollover)
		System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
		int lastRemMotionSecsAgo = ServerUtil.byteArrayToShort(tempIntByteArr);
		//ec.setLastMotionSecsAgo(lastMotionSecsAgo);
		index += 4;
		
		//uint8_t cur_behavior;       // The behavior type if using a scheduled behavior
		// SCHED_BHV_ALWAYS_ON = 0, SCHED_BHV_ALWAYS_OFF = 1, SCHED_BHV_AUTO = 2, SAFETY_BHV_ON = 3, SAFETY_BHV_OFF = 4,
    // MAN_OVD_BHV_ON = 5, MAN_OVD_BHV_OFF = 6.
		byte currBehavior = packet[index++];
		ec.setCurrentBehavior(currBehavior);
		
		//uint8_t occ_rptor_cnt;      // Number of different sensors that got reports of 
		//  motion in the 5 minute period (max is 255)
		byte noOfPeersHeardFrom = packet[index++];
		ec.setNoOfPeersHeardFrom((short)noOfPeersHeardFrom);		
    
		//uint8_t load_chg_cnt;       // Number of load changes seen in the period (max is 255)
		byte noOfLoadChanges = packet[index++];
		ec.setNoOfLoadChanges((short)noOfLoadChanges);

		//uint16_t load_baseline;     // Last measured load (in watts)
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int baseLoad = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		if (packet.length > index + 1) { //TODO this condition will be removed once plugload firmware is upgraded
			//uint16_t rms_current_last;     // Last measured current (in milli amps)
			System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
			int lastCurrent = ServerUtil.byteArrayToShort(tempShortByteArr);
			index += 2;
			ec.setManagedCurrent(new BigDecimal(lastCurrent));
		
			//if the lastCurrent is more than 20 amps raise an alarm
			if(lastCurrent >= 20000) {
				eventsAndFaultManager.addAlarm(plugload, "Plugload " + plugload.getName() + " is with current draw of " + lastCurrent, 
						EventsAndFault.PL_HIGH_CURRENT);
			} else {
				eventsAndFaultManager.clearAlarm(plugload, EventsAndFault.PL_HIGH_CURRENT);
			}
			
			//uint16_t rms_current_last2;     // Last measured current (in milli amps) for second channel
			System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
			int lastCurrent2 = ServerUtil.byteArrayToShort(tempShortByteArr);
			index += 2;
			ec.setUnmanagedCurrent(new BigDecimal(lastCurrent2));
		}
			
		/* not done in the current release
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
		
		//int16_t currnet_2_sample1;                        // -1 indicates error
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedCurrent = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setUnmanagedCurrent(new BigDecimal(unmanagedCurrent));
		index += 2;
			
    //int16_t powerfactor_2_sample1;              // -1 indicates error
    System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int unmanagedPowerFactor = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setUnmanagedPowerFactor(new BigDecimal(unmanagedPowerFactor));
		index += 2; 
		
    //uint16_t numResetsByCu;     // value of suvars.numResetsByCu at end of period
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int noCuResets = ServerUtil.byteArrayToShort(tempShortByteArr);
		ec.setNoOfCuResets(noCuResets);
		index += 2;

		*/    
		
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
			PlugloadImpl.getInstance().handleGroupsSynchronization(plugload, configChecksum, configChsumMap);
		}

		if (device != null) {
			device.setBasePower(baseLoad);
		}		
		float baseEnergy = (float)baseLoad / 12;
		Double price = pricingManager.getPrice(statsDate);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ": after price " + (System.currentTimeMillis() - startTime));
		}		
		float energy = 0;
		short zeroBucket = PMStatsProcessorService.ORIG_BUCKET;
		if(managedEnergy == PMStatsProcessorService.SHORT_MINUS_ONE) {
			//try to compute out of average volts and base energy			
			if(avgVolts == 0) {
				//if the average volts is 0, it is off for the whole 5 minutes so set it as 0
				energy = 0;
			} else if(avgVolts == 100){
				//if the average volts is 100, it is on for the whole 5 minutes so set it as base energy
				energy = baseEnergy;
			} else {
				//plugload has transitioned from on to off or off to on
				//in this case just add it as zero bucket and let the zero bucket update handler update with the difference of
				//cumulative energy
				zeroBucket = PMStatsProcessorService.ZERO_BUCKET;
			}		
		} else {
			energy = (float)managedEnergy / 1000;
		}
		if(zeroBucket == PMStatsProcessorService.ZERO_BUCKET) {
			//record has been added as zero bucket so cost, savings cannot be calculated
		} else {			
			if(intervalDur > 300) {
				//short term fix so that base line is not increased  because of a longer duration pm stats
				//TODO fix it correctly so that only load corresponding to avg volts is taken into this bucket and the rest be added to the previous bucket
				logger.error(plugload.getId() + ": received a longer duration stats - " + intervalDur);				
				if(energy > baseEnergy) {
					energy = baseEnergy;
				}		
			} else {
				if(energy > baseEnergy) {
					baseEnergy = energy;
				}
			}
			if(logger.isDebugEnabled()) {
				logger.debug("base energy -- " + baseEnergy);
				logger.debug("energy -- " + energy);
			}
			ec.setEnergy(new BigDecimal(energy));
			ec.setManagedLastLoad(new BigDecimal((float)(12 * energy)));			
			ec.setBaseEnergy(new BigDecimal(baseEnergy));			
			ec.setPrice(price.floatValue());			
			ec.setCost((float) (energy * price.floatValue() / 1000));
			ec.setBaseCost((float) baseEnergy * price.floatValue() / 1000);
			ec.setSavedCost(ec.getBaseCost() - ec.getCost());		
			double savedEnergy = baseEnergy - energy;
			ec.setSavedEnergy(new BigDecimal(savedEnergy));
			BigDecimal zeroSaving = new BigDecimal(0.0);
			ec.setManualSaving(zeroSaving);
			ec.setOccSaving(zeroSaving);
			ec.setTuneupSaving(zeroSaving);
			if(savedEnergy > 0) {
				adjustTaskTunedSavings(plugload, ec, savedEnergy, savingType);		  
			}			
		}
		ec.setPlugload(plugload);
		ec.setZeroBucket(zeroBucket);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ":after power saving handle : " + (System.currentTimeMillis() - startTime));
		}
		
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(plugload.getId() + ":after adjust task/amb: " + (System.currentTimeMillis() - startTime));	
		}
		if((currentMillis - device.getLastStatsRcvdTime().getTime()) >= FIVE_MINUTE_INTERVAL) {
		  try {
		  	//plugloadEnergyConsumptionManager.save(ec);
		  	//add the pm stats object to the queue to be saved ito db using the new framework
		  	PlugloadECPersistor pecp = PlugloadECPersistor.getInstance();
		  	logger.info("adding packet to queue for capture_at"+ec.getCaptureAt()+" ");
		  	pecp.addPlugloadECToQueue(DateUtil.formatDate(ec.getCaptureAt(), "yyyyMMddHHmmss"), plugload.getSnapAddress(), ec);
		  } catch(Exception e) {
			logger.error(plugload.getId() + ": could not add the stats to queue-- " + e.getMessage());
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
			plugload.setManagedLoad((float)(12 * energy));	
			plugload.setUnmanagedLoad(ec.getUnmanagedLastLoad().floatValue());
			plugload.setManagedBaselineLoad(new BigDecimal((double)12 * baseEnergy));
			if(ec.getBaseUnmanagedEnergy() != null )plugload.setUnmanagedBaselineLoad(ec.getBaseUnmanagedEnergy().multiply(new BigDecimal(12)));
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
				device.setBootTime(new Date(System.currentTimeMillis() - uptime * 1000));
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
