package com.ems.server.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.SweepTimerCache;
import com.ems.dao.FixtureDao;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.Bulb;
import com.ems.model.EnergyConsumption;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.SweepTimer;
import com.ems.model.SweepTimerDetails;
import com.ems.server.DeviceInfo;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.data.PMStatsWork;
import com.ems.server.device.DeviceListener;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.PricingManager;
import com.ems.service.ProfileManager;

@Service("pmStatsProcessorService")
@Transactional(propagation = Propagation.REQUIRED)
public class PMStatsProcessorService {

	private static final Logger logger = Logger.getLogger("Perf");
	private static Logger timingLogger = Logger.getLogger("TimingLogger");
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");

	public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;
	private static short ORIG_BUCKET = 0;
//	private static int FIXTURE_OUTAGE_DETECT_WATTS = 7;

	@Resource
	private FixtureManager fixtureManager;

	@Resource
	private FixtureDao fixtureDao;

	@Resource
	private EnergyConsumptionManager energyConsumptionManager;

	@Resource
	private PricingManager pricingManager;

	@Resource
	private EventsAndFaultManager eventsAndFaultManager;

	@Resource
	private ProfileManager profileManager;
	
	private HashMap<String, Date> sweepOverrideMap = new HashMap<String, Date>();
	
//	private int temperatureOffsetSU1 = 18;
//	private int pmStatsMode = ServerConstants.PM_STATS_GEMS_MODE;

	public void processStats(List<PMStatsWork> processingQueue) {

		long startTime = System.currentTimeMillis();

		try {
			fixtureDao.getSession().setFlushMode(FlushMode.MANUAL);
			//System.out.println("no. of stats -- " + processingQueue.size());
			for (PMStatsWork statsWork : processingQueue) {
				updateStats(statsWork.getFixture(), statsWork.getPacket(),
						statsWork.getSeqNo());
			}

			fixtureDao.getSession().flush();
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
	
	private boolean handleSweepTimer(EnergyConsumption ec, Fixture fixture, Date captureTime, 
	    int occupancySeen) {
	  
	  if(!PerfSO.getInstance().isSweepTimerEnabled()) {
	    //sweep timer feature is disabled
	    if(logger.isDebugEnabled()) {
	      logger.debug("Sweep timer is not enabled");
	    }
	    return false;
	  }
	  SweepTimerCache stCache = SweepTimerCache.getInstance();
	  logger.debug("floor of fixture =-= " + fixture.getFloor().getId());
	  SweepTimer st = stCache.getFixureSweepTimer(fixture);
	  if(st == null) {
	    //sweep timer is not defined for this fixture
	    if(logger.isDebugEnabled()) {
	      logger.debug(fixture.getId() + ": sweep timer is not defined for the fixture");
	    }
	    return false;	    
	  }
	  SweepTimerDetails std = st.getSweepTimerDetails(captureTime);
	  if(std == null) {
	    //sweep timer is not defined for this day for this fixture	    
	    return false;
	  }
	  
	  if(!std.isSweepTimerActive(captureTime)) {
	    //sweep timer is not active at this time
	    return false;
	  }
	  //sweep timer is active at this capture time
	  if(logger.isDebugEnabled()) {
	    logger.debug(fixture.getId() + ": sweep timer is on");
	  }
	  //check whether sweep override timer has started
	  String fixtSweepAssLevel = stCache.getFixtureAssocLevel(fixture);
	  Date overrideEndTime = sweepOverrideMap.get(fixtSweepAssLevel);	    
	  if(overrideEndTime != null ) {
	    //sweep override timer has started	      
	    if(captureTime.before(overrideEndTime)) {
	      //override timer is not expired
	      if(logger.isDebugEnabled()) {
		logger.debug(fixtSweepAssLevel + ": override timer is not expired");
	      }
	      return false;		
	    } else {
	      //override timer is expired
	      sweepOverrideMap.remove(fixtSweepAssLevel);
	    }	
	  }
	  //sweep override timer has not started or it is expired	    
	  if(occupancySeen < 300) { //occupancy observed in this 5 minute bucket
	    //start the sweep override timer
	    int overrideTime = std.getOverrideTimer();
	    if(overrideTime == 0) {
	      //override time is not set
	      return true;
	    }
	    long overrideExpiryTime =  overrideTime * 60 * 1000 + captureTime.getTime();
	    sweepOverrideMap.put(fixtSweepAssLevel, new Date(overrideExpiryTime));
	    if(logger.isDebugEnabled()) {
	      logger.debug(fixtSweepAssLevel + ": override timer is started");
	    }
	    return false;
	  }
	  return true;
	  
	} //end of method handleSweepTimer

	private void updateStats(Fixture fixture, byte[] packet, int seqNo) {

		long startTime = System.currentTimeMillis();
		DeviceInfo device = ServerMain.getInstance().getDevice(fixture);
		if (device != null && device.getLastStatsSeqNo() == seqNo) {
			// duplicate packet ignore it
			logger.error(fixture.getId() + ": Duplicate stats received");
			packet = null;
			fixture = null;
			return;
		}
//		timingLogger.debug("time taken after set time: "
//				+ (System.currentTimeMillis() - startTime));
		long currentMillis = System.currentTimeMillis();
		long min5 = 5 * 60 * 1000;
		currentMillis = currentMillis - currentMillis % min5;

		// if the last stats received time is past 5 minutes and
		// if the seq no. is +1 of last seq no. then the this stats is for
		// previous period
		if (device.getLastStatsSeqNo() + 1 == seqNo
				&& currentMillis - device.getLastStateRcvdTime().getTime() > FIVE_MINUTE_INTERVAL) {
			currentMillis -= FIVE_MINUTE_INTERVAL;
		}
		Date statsDate = new Date(currentMillis);
		byte[] tempShortByteArr = new byte[2];
		byte[] tempIntByteArr = new byte[4];

		int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
		if (packet[0] == ServerConstants.FRAME_START_MARKER) { // old packet
			index = 3;
		}
		// byte 3 is min voltage
		byte minVolts = packet[index++];

		// byte 4 is max voltage
		byte maxVolts = packet[index++];

		// byte 5, 6 is avg voltage
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short avgVolts = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		if (avgVolts > 100) {
			avgVolts = 100;
		}
		index += 2;

		// byte 7 is last voltage
		byte lastVolts = packet[index++];
		if (lastVolts > 100) {
			lastVolts = 100;
		}
		fixture.setDimmerControl((int) lastVolts);

		// bytes 8, 9 is min amb
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short minAmb = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 10, 11 is max amb
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short maxAmb = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 12, 13, 14, 15 is avg amb
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		int avgAmb = ServerUtil.byteArrayToInt(tempIntByteArr);
		index += 4;

		// byte 16 is min temp
		byte minTemp = packet[index++];

		// byte 17 is max temp
		byte maxTemp = packet[index++];

		// byte 18, 19 is avg temp
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short avgTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 20 is last temperature
		byte lastTemp = packet[index++];

		// bytes 21, 22 is energy_calib value
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		int energyCalib = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 23, 24 is energy ticks
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		int pulses = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 25, 26, 27, 28 is energy_cum
		// this need to be used to compute energy of missing buckets
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		long energyCumTicks = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		index += 4;

		// bytes 29,30 is light on sec
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short lightOnSec = (short) ServerUtil
				.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 31, 32 is light on to off
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short lightOnToOff = (short) ServerUtil
				.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 33, 34 is light off to on
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short lightOffToOn = (short) ServerUtil
				.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 35, 36 is motion off to on
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short motionOffToOn = (short) ServerUtil
				.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 37, 38 is motion on to off
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short motionOnToOff = (short) ServerUtil
				.byteArrayToShort(tempShortByteArr);
		index += 2;

		byte[] motionBits = new byte[8];
		// bytes 39, 40, 41, 42 is motion mask1
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		int motionMask1 = ServerUtil.byteArrayToInt(tempIntByteArr);
		System.arraycopy(packet, index, motionBits, 4, 4);
		index += 4;

		// bytes 43, 44, 45, 46 is motion mask2
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		int motionMask2 = ServerUtil.byteArrayToInt(tempIntByteArr);
		System.arraycopy(packet, index, motionBits, 0, 4);
		index += 4;

		long motionBitsLong = ServerUtil.byteArrayToLong(motionBits);

		int lastOccSeen = getLastOccupancySec(motionMask1, motionMask2);
		if (lastOccSeen == 300) { // no occupancy seen
			fixture.setLastOccupancySeen(fixture.getLastOccupancySeen() + 300);
		} else {
			fixture.setLastOccupancySeen(lastOccSeen);
		}

		// byte 47
		// 0 - none
		// 1 - occupancy
		// 2 - ambient
		// 3 - individual(tune_up)
		// 4 - manual
		int savingType = packet[index++];

		if(logger.isDebugEnabled()) {
		  logger.debug("saving type -- " + savingType);
		}

		// byte 48 is current state
		byte currState = packet[index++];

		// byte 49, 50, 51, 52 is sys up time
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		long uptime = ServerUtil.intByteArrayToLong(tempIntByteArr);
		if(logger.isDebugEnabled()) {
		  logger.debug(fixture.getId() + ": uptime of node -- " + uptime);
		}
		index += 4;
		// byte 53, 54 is last reset reason
		index += 2;
		// byte 55, 56 is cuStatus
		index += 2;
		// byte 57, 58 is num reset by cu
		index += 2;

		// from version 1.2
		// byte 59 is app
		int currApp = 2;
		if (ServerUtil.compareVersion(fixture.getVersion(), "1.2") >= 0) {
			currApp = packet[index++];
		}
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after packet processing: "
				+ (System.currentTimeMillis() - startTime));
		}
		if (packet.length > index + 1) {
			// byte 60 is global profile checksum
			final byte gChecksum = packet[index++];
			// byte 61 scheduled profile checksum
			final byte sChecksum = packet[index++];
			// byte 62 profile group id
			final byte profileGroupId = packet[index++];
			if (currState != ServerConstants.CURR_STATE_BASELINE) {
				DeviceServiceImpl.getInstance().handleProfileMismatch(fixture,
						gChecksum, sChecksum, profileGroupId,
						ServerConstants.REPT_PM_DATA_MSG_TYPE);
			} else {
			  if(profileLogger.isDebugEnabled()) {
				profileLogger.debug(fixture.getId()
					+ ": is in baseline mode..., skipping profile sync check.");
			  }
				// Enable the push flag for pushing the profile to the fixture,
				// when the fixture moves out of
				// baseline...
//				fixtureManager.enablePushGlobalProfileForFixture(fixture
//						.getId());
//				fixtureManager.enablePushProfileForFixture(fixture.getId());
//				fixture.setPushGlobalProfile(true);
//				fixture.setPushProfile(true);	
				fixtureManager.enablePushProfileAndGlobalPushProfile(fixture.getId(), true, true);
			}
		}
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after profile processing : "
				+ (System.currentTimeMillis() - startTime));
		}
		int rem = lastVolts % 5;
		if (rem > 2) {
			lastVolts += (5 - rem);
		} else {
			lastVolts -= rem;
		}
		float fPowerFactor = 0;
		Ballast ballast = fixtureManager.getBallastById(fixture.getBallast().getId());
		BallastVoltPower obvp = getBallastVoltPowerFactor(ballast, 
		    (float) lastVolts / 10);
		if (obvp == null) {
			logger.error("Error fetching VoltPowerMap: (" + lastVolts + " => "
					+ fPowerFactor + ")");
		} else {
			fPowerFactor = obvp.getPower().floatValue();
			if(logger.isDebugEnabled()) {
			  logger.debug("Fetching from Ballast: " + obvp.getVoltPowerMapId()
					+ ", VoltPowerMap for Bulb life: (" + lastVolts + " => "
					+ fPowerFactor + ")");
			}
		}
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after ballast volt power : "
				+ (System.currentTimeMillis() - startTime));
		}
		float voltPowerFactor = fPowerFactor;
		double fcFactor = 14;
		if(ServerUtil.compareVersion(fixture.getVersion(), "2.0") >= 0) {
		  fcFactor = 10.764;
		}
		if (lastVolts < 5) {
			fixture.setLightLevel((int)(minAmb / fcFactor));
		} else {
			fixture.setLightLevel((int)(maxAmb / fcFactor));
		}
		//setBulbLife(fixture, voltPowerFactor);

		EnergyConsumption ec = new EnergyConsumption();

//		ec.setId(energyConsumptionManager.getEnergyConsumptionId(
//				fixture.getId(), statsDate));

		ec.setBrightOffset(lightOffToOn);
		// this is the percentage of light
		ec.setBrightPercentage((short) voltPowerFactor);

		ec.setCaptureAt(statsDate);
		ec.setAvgVolts(avgVolts);
		ec.setDimOffset(lightOnToOff);
		ec.setFixture(fixture);
		ec.setLightAvgLevel((short) avgAmb);
		ec.setLightMaxLevel(maxAmb);
		ec.setLightMinLevel(minAmb);
		ec.setLightOff(motionOnToOff);
		ec.setLightOn(motionOffToOn);
		ec.setMaxTemperature((short) maxTemp);
		ec.setMinTemperature((short) minTemp);
		if (ServerUtil.compareVersion(fixture.getVersion(), "2.0") < 0) {
			avgTemp = (short) ((avgTemp * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU1());
		} else {
			avgTemp = (short) ((avgTemp * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU2());
		}
		ec.setAvgTemperature(avgTemp);
		ec.setCurrState((short) currState);		
		ec.setEnergyCum(energyCumTicks);
		ec.setEnergyCalib(energyCalib);
		ec.setMinVolts((short) minVolts);
		ec.setMaxVolts((short) maxVolts);
		ec.setEnergyTicks(pulses);
		// ec.setOccCount((short)motionCount);
		// ec.setOccIn(new Short(rand.nextInt(100)+""));
		// ec.setOccOut(new Short(rand.nextInt(100)+""));

		double avgPower = -1;
		double energyConsumed = -1;
		boolean calculatePower = true;
		if (fixture.getCommType() == ServerConstants.COMM_TYPE_PLC) {
			calculatePower = true;
		} else if (PerfSO.getInstance().getPmStatsMode() == ServerConstants.PM_STATS_FIRMWARE_MODE) {
		  if(ServerUtil.isNewCU(fixture)) {          	
		    avgPower = (double)pulses * 12 / 1000;		    
		  } else {
		    avgPower = PerfSO.getInstance().getAvgPower(energyCalib, pulses, 5 * 60 * 1000, fixture);
		  }
		  if(logger.isDebugEnabled()) {
		    logger.debug(fixture.getId() + ": received average power -- " + avgPower);														// hour
		  }
			// energyConsumed = getAvgEnergyInMWH(energyCalib, pulses) * 5 / 60;
			calculatePower = false;
			ec.setPowerCalc(ServerConstants.POWER_CALCULATED_SU);
			// }
		}
		if (calculatePower) {
			avgPower = getAvgPowerFromVolts(avgVolts, ballast.getLampNum(),
					ballast) * fixture.getNoOfFixtures(); // watts
			ec.setPowerCalc(ServerConstants.POWER_CALCULATED_GEMS);
		}
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId()  + ":after power calc : "
				+ (System.currentTimeMillis() - startTime));
		}
		// ec.setDimPercentage((short)avgPower1);		

		Double price = pricingManager.getPrice(statsDate);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ": after price " + 
		      (System.currentTimeMillis() - startTime));
		}
		ec.setPrice(price.floatValue());
		double basePowerUsed = 0;

		double compBasePowerUsed = ballast.getBallastFactor()
				* ballast.getWattage() * ballast.getLampNum()
				* fixture.getNoOfFixtures();
		if (avgPower > 400) { // 400 is the max for 2.0 enlighted relays
			avgPower = compBasePowerUsed;
			// faulty CU/SU
			eventsAndFaultManager.addAlarm(fixture, "Energy reading too high ("
					+ avgPower + ")",
					EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR,
					EventsAndFault.MAJOR_SEV_STR);
		}

		energyConsumed = avgPower * 5 / 60; // watts hr
		if(logger.isDebugEnabled()) {
		  logger.debug(fixture.getId() + ": energy consumed -- " + energyConsumed);
		}

		ec.setPowerUsed(new BigDecimal(avgPower));
		/*
		 * //get it from database basePowerUsed =
		 * fixture.getBaselinePower().doubleValue(); if (basePowerUsed == 0) {
		 * // base power was not updated in the database, get it from ballast
		 * info basePowerUsed = ballast.getBallastFactor() *
		 * ballast.getWattage() * ballast.getLampNum()
		 * fixture.getNoOfFixtures(); }
		 * 
		 * if(avgPower > basePowerUsed) { //if the avgPower is abnormal, don't
		 * take it if((avgPower - basePowerUsed) <= (avgPower * 10 / 100)) {
		 * //if the new average power is not more than base power by 10%, update
		 * the base power basePowerUsed = avgPower; } else { //assume that new
		 * average power is abnormal and take it as base power avgPower =
		 * basePowerUsed; } } else { if(currState ==
		 * ServerConstants.CURR_STATE_BASELINE) { basePowerUsed = avgPower; }
		 * else if (avgVolts == 100) { // if the new value is less than previous
		 * base power by not more than 8% and voltage is 100 // set the new
		 * value as base power if ((basePowerUsed - avgPower) <= (basePowerUsed
		 * * 8 / 100)) { basePowerUsed = avgPower; } } }
		 */
		if (currState == ServerConstants.CURR_STATE_BASELINE) {
			basePowerUsed = avgPower;
		} else {
			basePowerUsed = fixture.getBaselinePower().doubleValue();
			if (basePowerUsed == 0) { // base power was not updated in the
										// database, get it from ballast info
				basePowerUsed = compBasePowerUsed;
			}
		}
		if (avgPower > basePowerUsed) {
			basePowerUsed = avgPower;
		} else {
			// if the new value is less than previous base power by not more
			// than 8% and voltage is 100
			// set the new value as base power
			if (avgVolts == 100) {
				if ((basePowerUsed - avgPower) <= (basePowerUsed * 8 / 100)) {
					basePowerUsed = avgPower;
				}
			}
		}
		if (device != null) {
			device.setBasePower(basePowerUsed);
		}
		boolean sweepEnabled = false;
		if(handleSweepTimer(ec, fixture, statsDate, lastOccSeen)) {
		  if(logger.isDebugEnabled()) {
		    logger.debug(fixture.getId() + ": sweep timer is active");
		  }
		  basePowerUsed = avgPower;
		  sweepEnabled = true;
		}
//		timingLogger.debug("after baseline : "
//				+ (System.currentTimeMillis() - startTime));
		ec.setBasePowerUsed(new BigDecimal(basePowerUsed));
		ec.setBaseCost((float) basePowerUsed * 5 * price.floatValue()
				/ (1000 * 60));
		// price is per kWH and energy consumed is milli WH
		// ec.setCost((float)(energyConsumed * company.getPrice() / (1000000)));
		ec.setCost((float) (energyConsumed * price.floatValue()) / 1000);
		ec.setSavedCost(ec.getBaseCost() - ec.getCost());

//		timingLogger.debug("after setting cost : "
//				+ (System.currentTimeMillis() - startTime));

		double savedPower = basePowerUsed - avgPower;
		BigDecimal savedPowerUsed = new BigDecimal(savedPower);
		ec.setSavedPowerUsed(savedPowerUsed);
		BigDecimal zeroSaving = new BigDecimal(0.0);
		ec.setManualSaving(zeroSaving);
		ec.setAmbientSaving(zeroSaving);
		ec.setOccSaving(zeroSaving);
		ec.setTuneupSaving(zeroSaving);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after power saving handle : "
				+ (System.currentTimeMillis() - startTime));
		}
		if(savedPower > 0) {
		  switch (savingType) {
		  case ServerConstants.STATS_SAVING_OCC:			
		  case ServerConstants.STATS_SAVING_AMB:
		  case ServerConstants.STATS_SAVING_TUNEUP:
			adjustTaskTunedSavings(fixture, ec, savedPower, avgVolts, savingType);
			break;
		  case ServerConstants.STATS_SAVING_MANUAL:
			ec.setManualSaving(savedPowerUsed);
			break;
		  default:
			ec.setOccSaving(savedPowerUsed);
			break;
		  }
		}
		ec.setMotionBits(motionBitsLong);
		ec.setZeroBucket(ORIG_BUCKET);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after adjust task/amb: "
				+ (System.currentTimeMillis() - startTime));	
		}
		if((currentMillis - device.getLastStateRcvdTime().getTime()) >= FIVE_MINUTE_INTERVAL) {
		  energyConsumptionManager.save(ec);
		  device.setLastStatsSeqNo(seqNo);
		} else {
		  logger.error(fixture.getId() + ": Stats came too early");
		}		
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":time for saving ec :  "
				+ (System.currentTimeMillis() - startTime));
		}
		// want to catch the exception if the zero bucket is added for the same
		// time period
//		try {
//			ecMgr.save(ec);
//			timingLogger.debug("time taken after saving the ec : "
//					+ (System.currentTimeMillis() - startTime));
//		} catch (Exception ex) {
//			// ex.printStackTrace();
//			long lastZeroBucketId = device.getLastZeroBucketId();
//			logger.error(fixture.getId() + ": error- zero bucket("
//					+ lastZeroBucketId + ") added before original bucket for "
//					+ ec.getCaptureAt());
//			try {
//				if (lastZeroBucketId != -1) {
//					ec.setId(lastZeroBucketId);
//					device.setLastZeroBucketId(-1);
//					ecMgr.update(ec);
//				}
//			} catch (Exception e1) {
//				// e1.printStackTrace();
//				logger.error(fixture.getId()
//						+ ": error in updating zero bucket- "
//						+ lastZeroBucketId);
//			}
//		}
		long statsDateTime = statsDate.getTime();
		Date fixtLastStatsRcvdTime = device.getLastStateRcvdTime();
		try {
			fixture.setLastStatsRcvdTime(statsDate);
			fixture.setCurrApp((short) currApp);
			fixture.setCurrentState(ServerUtil.getCurrentState(currState));
			fixture.setWattage((int)Math.round(avgPower));
			fixture.setAvgTemperature(avgTemp);
			if(sweepEnabled) {
			  fixture.setBaselinePower(fixture.getBaselinePower());
			} else {
			  fixture.setBaselinePower(new BigDecimal(basePowerUsed));
			}
			if (ServerUtil.compareVersion(fixture.getVersion(), ServerMain
					.getInstance().getGemsVersion()) == 0) {
				// This will ensure that after the upgrade of SU image the
				// version synced bit is cleared.
				fixture.setVersionSynced(0);
			}
			fixtureManager.updateStats(fixture);
			if(timingLogger.isDebugEnabled()) {
			  timingLogger.debug(fixture.getId() + ":time for saving fixture :  "
				+ (System.currentTimeMillis() - startTime));
			}
//			timingLogger.debug("after fixture update :  "
//					+ (System.currentTimeMillis() - startTime));
			// update the device cache attributes
			if (device != null) {
				device.setLastStateRcvdTime(statsDate);
				device.setUptime(uptime);
				device.setBootTime(new Date(System.currentTimeMillis() - uptime
						* 1000));
				device.setEnergyCum(energyCumTicks);
			}			
			if (avgPower >= PerfSO.getInstance().getFixtureOutageDetectWatts()) {
			  eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
			} else {
			  //for new CU don't check for calib value and ticks
			  //for old CU both calib and ticks should be greater than 0 for the 
			  //stats bucket to be considered for outage
			  if(ServerUtil.isNewCU(fixture) || (energyCalib > 0 && pulses > 0)) {
			    raiseOutageAlarm(avgPower, minVolts, maxVolts, savingType,
				lastOccSeen, device, fixture);
			  }
			  if(timingLogger.isDebugEnabled()) {
			    timingLogger.debug(fixture.getId() + ":after outage alarm :  "
				+ (System.currentTimeMillis() - startTime));
			  }
			}			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if ( fixtLastStatsRcvdTime != null
			&& (statsDateTime - fixtLastStatsRcvdTime.getTime()) > FIVE_MINUTE_INTERVAL) {
		  // Bug #749 check for zero buckets also
		  Date lastZeroBucketTime = device.getLastZeroBucketTime();
		  if (lastZeroBucketTime == null || lastZeroBucketTime.getTime() < (statsDateTime - FIVE_MINUTE_INTERVAL)) {
		    PerfSO.getInstance().addZeroBucket(device, fixture, statsDateTime);
		  }
		  PerfSO.getInstance().addFixtureToZeroBucketUpdQueue(fixture.getId(),
		      fixtLastStatsRcvdTime, statsDate, sweepEnabled);
		  if(timingLogger.isDebugEnabled()) {
		    timingLogger.debug(fixture.getId() + ":after adding zero bucket updates:  "
				+ (System.currentTimeMillis() - startTime));
		  }
		}
		if(timingLogger.isInfoEnabled()) {
		  timingLogger.info(fixture.getId() + ": Processing Single Packet :"
				+ (System.currentTimeMillis() - startTime) );
		}
		fixture = null;
		device = null;
		ec = null;		
		packet = null;

	} // end of method updateStats

	/*
	 * mask1 is 1st word, mask2 is 2nd word mask2 represents most recent word in
	 * the 5 minute interval 300 seconds are represented by 60 bits of which 32
	 * bits are in mask1, 28 bits are in mask2 remaining 4 most significant bits
	 * of word 2 are ignored in a word, more recent 5 second bits are msb. for
	 * ease of calculation, bits are numbered 0 through 31 from msb instead of
	 * lsb first mask2 is looked into to find the most recent occupancy from 4th
	 * bit and then mask1 is looked into if occupancy is not observed in mask2
	 * from msb
	 */
	public int getLastOccupancySec(int mask1, int mask2) {

		byte[] mask2ByteArray = ServerUtil.intToByteArray(mask2);
		for (int i = 4; i < 32; i++) {
			if (getBit(mask2ByteArray, i) == 1) {
				return 5 * (i - 3);
			}
		}

		byte[] maskByteArray = ServerUtil.intToByteArray(mask1);
		for (int i = 0; i < 32; i++) {
			if (getBit(maskByteArray, i) == 1) {
				return 5 * (i + 29);
			}
		}
		return 300;

	} // end of method getLastOccupancySec

	private BallastVoltPower getBallastVoltPowerFactor(Ballast ballast,
			float volt) {
		if (ballast != null) {
			List<BallastVoltPower> voltPowerList = fixtureManager
					.getAllBallastVoltPowersFromId(ballast.getVoltPowerMapId()
							.longValue());
			if (voltPowerList != null) {
			  if(logger.isDebugEnabled()) {
				logger.debug("VoltPowerList size: " + voltPowerList.size());
			  }
				Iterator<BallastVoltPower> voltPoweritr = voltPowerList
						.iterator();
				BallastVoltPower obvp = null;
				while (voltPoweritr.hasNext()) {
					obvp = (BallastVoltPower) voltPoweritr.next();
					if (obvp.getVolt().floatValue() == volt)
						return obvp;
				}
			}
		}
		return null;
	}

	private void setBulbLife(Fixture fixture, float voltPowerFactor) {

		try {
			double bulbLife = fixture.getBulbLife();
			Bulb bulb = fixtureManager.getBulbById(fixture.getBulb().getId());
			if (bulb == null) {
				logger.error(fixture.getId() + ": error in retrieving the bulb");
				return;
			}
			long totalLife = bulb.getLifeInsStart();
			if(totalLife == 0) {
			  //cannot figure out the bulb life left as total life is not known.
			  return;
			}

			double usedLife = voltPowerFactor * 5 / 60 / 100; // in hours
			double usedPercent = usedLife * 100 / totalLife;
			fixture.setBulbLife(bulbLife - usedPercent);
		} catch (Exception e) {
			logger.error(fixture.getId()
					+ ": error in setting the bulb life");
		}

	} // end of method setBulbLife

//	public double getAvgPower(int calibVal, int pulses, int interval,
//			Fixture fixture) {
//
//		if (pulses < 2) {
//			return 0;
//		}
//		double avgPower = ((double) calibVal * 3600 * (pulses - 1))
//				/ (interval * (double) 10000000); // 10e-7 as calib
//													// is 0.1 micro
//
//		/*
//		 * scalaing factor/error adjustment logic For 117V: %err =
//		 * 0.0511*calculated_watts - 6.9192 For 277V: %err =
//		 * 1.4522Ln(calculated_watts) - 12.754
//		 * 
//		 * So for 117V, if calculated_watts = 180; %err = 2.2788% Adj = (1
//		 * -.022788) * 180 = 175.898 watts
//		 */
//		if (ServerMain.getInstance().isApplyECScalingFactor() && avgPower != 0) {
//			// Apply scaling factor...
//			short fVolts = fixture.getVoltage();
//			logger.debug(fixture.getId() + ": before scaling -- " + avgPower);
//			double errPercentage = 0.0;
//			if (fVolts == 277) {
//				errPercentage = ServerMain.getInstance()
//						.getScalingFactorFor277V()
//						* Math.log(avgPower)
//						- ServerMain.getInstance().getAdjFactorFor277V();
//				avgPower = (1 - errPercentage / 100) * avgPower;
//			} else if (fVolts == 110) {
//				errPercentage = ServerMain.getInstance()
//						.getScalingFactorFor110V()
//						* avgPower
//						- ServerMain.getInstance().getAdjFactorFor110V();
//				avgPower = (1 - errPercentage / 100) * avgPower;
//			} else if (fVolts == 240) {
//				errPercentage = ServerMain.getInstance()
//						.getScalingFactorFor240V()
//						* avgPower
//						- ServerMain.getInstance().getAdjFactorFor240V();
//				avgPower = (1 - errPercentage / 100) * avgPower;
//			}
//		}
////		return avgPower;
//
//	} // end of method getAvgPower

	public double getAvgPowerFromVolts(int volts, int noOfLamps, Ballast ballast) {

		if (volts == 0) {
			return 0;
		}
		int bulbWattage = ballast.getWattage();
		int rem = volts % 5;
		if (rem > 2) {
			volts += (5 - rem);
		} else {
			volts -= rem;
		}
		float fPowerFactor = 0;
		BallastVoltPower obvp = getBallastVoltPowerFactor(ballast,
				(float) volts / 10);
		if (obvp == null) {
			logger.error("Error fetching VoltPowerMap: (" + volts + " => "
					+ fPowerFactor + ")");
		} else {
			fPowerFactor = obvp.getPower().floatValue();
			if(logger.isDebugEnabled()) {
			  logger.debug("Fetching from Ballast: " + obvp.getVoltPowerMapId()
					+ ", VoltPowerMap: (" + volts + " => " + fPowerFactor + ")");
			}
		}
		double ballast_factor = ballast.getBallastFactor();
		double calcPower = ballast_factor * bulbWattage * noOfLamps / 100;
		if (fPowerFactor > 0) {
			calcPower = calcPower * fPowerFactor;
		}
		return calcPower;

	} // end of method getAvgPowerFromVolts
	
	private void adjustTaskTunedSavings(Fixture fixture, EnergyConsumption ec, double savedPower, 
	    short avgVolts, int savingType) {

	  try {	
	    Calendar currentDate = Calendar.getInstance();
	    int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60 + currentDate.get(Calendar.MINUTE);
	    int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
	    if (dayOfWeek == 0) {
	      dayOfWeek = 7;
	    }
	    int maxOnLevel = profileManager.getOnLevel(fixture.getId(), dayOfWeek, minOfDay);
	    // If the SU has been in profile for which maxOnLevel = 100 for 2
	    // mins and then moved to another profile for
	    // which maxOnLevel=60
	    // then the avgVolts could be 80, In this situation the % savings
	    // will be shown as more than 100% which is
	    // not correct.
	    if (avgVolts <= maxOnLevel) {
	      if(logger.isDebugEnabled()) {
		logger.debug(fixture.getId() + ": (" + avgVolts + " <= " + maxOnLevel + ")");
	      }
	      double ttSaving = (100 - maxOnLevel) * savedPower / (100 - avgVolts);
	      double otherSaving = savedPower - ttSaving;
	      ec.setTuneupSaving(new BigDecimal(ttSaving));
	      if(savingType == ServerConstants.STATS_SAVING_OCC) {
		ec.setOccSaving(new BigDecimal(otherSaving));
	      } else {
		ec.setAmbientSaving(new BigDecimal(otherSaving));
	      }
	      return;
	    } else {
	      if(logger.isDebugEnabled()) { 
		logger.debug(fixture.getId() + ": (" + avgVolts + " > " + maxOnLevel + ")");
	      }	      
	    }
	  } catch (Exception ex) {
	    logger.error(fixture.getId() + ": error getting current profile - " + ex.getMessage());
	    //if max on level could not be retrieved, then it will add the
	    // consumption entry with savings based on the savings type	   
	  }
	  switch(savingType) {	  
	  case ServerConstants.STATS_SAVING_AMB:
	  	ec.setAmbientSaving(new BigDecimal(savedPower));
	  	break;
	  case ServerConstants.STATS_SAVING_TUNEUP:
	  	ec.setTuneupSaving(new BigDecimal(savedPower));
	  	break;
	  default:
	  	ec.setOccSaving(new BigDecimal(savedPower));
	  	break;
	  }
	  
	} // end of method adjustTaskTunedSavings	

	private void adjustAmbTaskTunedSavings(Fixture fixture,
			EnergyConsumption ec, double savedPower, short avgVolts,
			int savingType) {

		try {
			//long startTime = System.currentTimeMillis();
			// long prHandId = fixture.getProfileHandler().getId();
			// timingLogger.debug("Getting Profile Handler Id : "
			// + (System.currentTimeMillis() - startTime));
			// ProfileHandler prHand = profileManager
			// .loadProfileHandler(prHandId);
			// timingLogger.debug("Getting Profile Handler : "
			// + (System.currentTimeMillis() - startTime));
			// int maxOnLevel =
			// prHand.getCurrentProfile().getOnLevel().intValue();
			// timingLogger.debug("Getting Max Level : "
			// + (System.currentTimeMillis() - startTime));

			Calendar currentDate = Calendar.getInstance();
			int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60
					+ currentDate.get(Calendar.MINUTE);
			int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
			if (dayOfWeek == 0) {
				dayOfWeek = 7;
			}
			int maxOnLevel = profileManager.getOnLevel(fixture.getId(),
					dayOfWeek, minOfDay);
//			timingLogger.debug("Query Done : "
//					+ (System.currentTimeMillis() - startTime));
			// If the SU has been in profile for which maxOnLevel = 100 for 2
			// mins and then moved to another profile for
			// which maxOnLevel=60
			// then the avgVolts could be 80, In this situation the % savings
			// will be shown as more than 100% which is
			// not correct.
			if (avgVolts <= maxOnLevel) {
			  if(logger.isDebugEnabled()) {
			    logger.debug(fixture.getId() + ": (" + avgVolts + " <= "
						+ maxOnLevel + ")");
			  }
				double ttSaving = (100 - maxOnLevel) * savedPower
						/ (100 - avgVolts);
				double ambSaving = savedPower - ttSaving;
				ec.setAmbientSaving(new BigDecimal(ambSaving));
				ec.setTuneupSaving(new BigDecimal(ttSaving));
			} else {
			  if(logger.isDebugEnabled()) {
				logger.debug(fixture.getId() + ": (" + avgVolts + " > "
						+ maxOnLevel + ")");
			  }
				if (savingType == ServerConstants.STATS_SAVING_AMB)
					ec.setAmbientSaving(new BigDecimal(savedPower));
				else
					ec.setTuneupSaving(new BigDecimal(savedPower));
			}
//			timingLogger.debug("Complet Amobint Light Calc : "
//					+ (System.currentTimeMillis() - startTime));
		} catch (Exception ex) {
			logger.error(fixture.getId() + ": error getting current profile - "
					+ ex.getMessage());
			// if max on level could not be retrieved, then it will add the
			// consumption entry with savings based
			// on the savings type
			if (savingType == ServerConstants.STATS_SAVING_AMB) {
				ec.setAmbientSaving(new BigDecimal(savedPower));
			} else {
				ec.setTuneupSaving(new BigDecimal(savedPower));
			}

		}

	} // end of method adjustAmbTaskTunedSavings

	private void raiseOutageAlarm(double avgPower, short minVolts, short maxVolts, int savingType, 
	    int lastOccSeen, DeviceInfo device, Fixture fixture) {

	  if(logger.isDebugEnabled()) {
		logger.debug(fixture.getFixtureName() + ": outage data[minVolts="
				+ minVolts + ",maxVolts=" + maxVolts + ",saving=" + savingType
				+ ",lastOcc=" + lastOccSeen + ",power=" + avgPower + "]");
	  }
		if (minVolts != maxVolts
				|| !fixture.getCurrentState().equals(
						ServerConstants.CURR_STATE_AUTO_STR)
				|| lastOccSeen >= 300
				|| savingType == ServerConstants.STATS_SAVING_AMB) {
			return;
		}
		// if the current power is less than 7 watts, raise an alarm that
		// fixture is out
		// in GE ballast which is parallel, we observed 6.9 watts when all bulbs
		// are out
		if (avgPower < PerfSO.getInstance().getFixtureOutageDetectWatts()) {
			logger.error(fixture.getFixtureName()
					+ ": fixture is out. raising outage alarm");
			eventsAndFaultManager.addAlarm(fixture, "Fixture is out",
					EventsAndFault.FIXTURE_OUTAGE_EVENT_STR,
					EventsAndFault.CRITICAL_SEV_STR);
			return;
		}
		// raiseBulbOutageAlarm(avgPower, minVolts, maxVolts, savingType,
		// lastOccSeen,
		// device, fixture);

	} // end of method raiseOutageAlarm

	private int getBit(byte[] data, int pos) {

		int posByte = pos / 8;
		int posBit = pos % 8;
		byte valByte = data[posByte];
		int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
		return valInt;

	} // end of method getBit

}
