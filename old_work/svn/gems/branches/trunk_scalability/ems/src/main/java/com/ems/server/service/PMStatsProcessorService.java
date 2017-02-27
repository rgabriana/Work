package com.ems.server.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.cache.SweepTimerCache;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.EnergyConsumption;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureCalibrationMap;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.LampCalibrationConfiguration;
import com.ems.model.SweepTimer;
import com.ems.model.SweepTimerDetails;
import com.ems.model.SystemConfiguration;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.data.PMStatsWork;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.BallastManager;
import com.ems.service.BallastVoltPowerManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureCalibrationManager;
import com.ems.service.FixtureManager;
import com.ems.service.PricingManager;
import com.ems.service.ProfileManager;
import com.ems.service.SystemConfigurationManager;

@Service("pmStatsProcessorService")
@Transactional(propagation = Propagation.REQUIRED)
public class PMStatsProcessorService {

	private static final Logger logger = Logger.getLogger("Perf");
	private static Logger timingLogger = Logger.getLogger("TimingLogger");
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");

	public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;
	private static short ORIG_BUCKET = 0;
	
	public static final long LONG_MINUS_ONE = 4294967295L;
	public static final long SHORT_MINUS_ONE = 65535;
	private static final int MAX_CU2_LOAD = 600;
	private static final float MIN_POWER_USED_THRESHOLD = 1.5f;

	@Resource
	private FixtureManager fixtureManager;

	@Resource
	private EnergyConsumptionManager energyConsumptionManager;

	@Resource
	private PricingManager pricingManager;

	@Resource
	private EventsAndFaultManager eventsAndFaultManager;

	@Resource
	private ProfileManager profileManager;
	
	@Resource
	private BallastManager ballastMgr = null;
	
	@Resource
	private FixtureCalibrationManager fixtureCalibrationManager;
	
	@Resource
	private BallastVoltPowerManager ballastVoltPowerManager;
	
	private HashMap<String, Date> sweepOverrideMap = new HashMap<String, Date>();

	public void setEnergyConsumptionManager(
			EnergyConsumptionManager energyConsumptionManager) {
		this.energyConsumptionManager = energyConsumptionManager;
	}

	public void setPricingManager(PricingManager pricingManager) {
		this.pricingManager = pricingManager;
	}

	public void setEventsAndFaultManager(
			EventsAndFaultManager eventsAndFaultManager) {
		this.eventsAndFaultManager = eventsAndFaultManager;
	}

	public void processStats(List<PMStatsWork> processingQueue) {

		long startTime = System.currentTimeMillis();

		try {
			//fixtureManager.setFlushMode(FlushMode.MANUAL);
			//System.out.println("no. of stats -- " + processingQueue.size());
			for (PMStatsWork statsWork : processingQueue) {
				updateStats(statsWork.getFixture(), statsWork.getPacket(),
						statsWork.getSeqNo());
			}

			//fixtureManager.flush();
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
	  logger.debug("floor of fixture - " + fixture.getFloor().getId());
	  SweepTimer st = stCache.getFixureSweepTimer(fixture);
	  if(st == null) {
	    //sweep timer is not defined for this fixture
	    if(logger.isDebugEnabled()) {
	      logger.debug(fixture.getId() + ": sweep timer is not defined for the fixture");
	    }
	    return false;	    
	  }
	  Date statsStartTime = new Date(captureTime.getTime() - FIVE_MINUTE_INTERVAL);
	  SweepTimerDetails std = st.getSweepTimerDetails(statsStartTime);
	  if(std == null) {
	    //sweep timer is not defined for this day for this fixture
	    if(logger.isDebugEnabled()) {
	      logger.debug(fixture.getId() + ": sweep timer is not defined for the day");
	    }
	    return false;
	  }
	  
	  String fixtSweepAssLevel = stCache.getFixtureAssocLevel(fixture);	  
	  if(!std.isSweepTimerActive(statsStartTime)) {
	    //sweep timer is not active at this time
	    //override timer is expired
	    sweepOverrideMap.remove(fixtSweepAssLevel);
	    if(logger.isDebugEnabled()) {
	      logger.debug(fixture.getId() + ": sweep timer is not on");
	    }
	    return false;
	  }
	  //sweep timer is active at this capture time
	  if(logger.isDebugEnabled()) {
	    logger.debug(fixture.getId() + ": sweep timer is on");
	  }
	  //check whether sweep override timer has started
	  Date overrideEndTime = sweepOverrideMap.get(fixtSweepAssLevel);	    
	  int overrideTime = std.getOverrideTimer();
	  if(overrideEndTime != null ) {	    
	    //sweep override timer has started
	    if(overrideTime == 0) {
	      //after occupancy is seen, as override time is 0, timer never expires
	      if(logger.isDebugEnabled()) {
		logger.debug(fixtSweepAssLevel + ": override time is 0");
	      }
	      return false;
	    }
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
	    //int overrideTime = std.getOverrideTimer();
	    if(overrideTime == 0) {
	      //override time is not set
	      overrideTime = 5;
	      //return true;
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
		DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
		if (device != null && device.getLastStatsSeqNo() == seqNo) {
			// duplicate packet ignore it
			logger.error(fixture.getId() + ": Duplicate stats received");
			packet = null;
			fixture = null;
			return;
		}
		if(device.isFirstStatsAfterCommission()) {
		  logger.error(fixture.getId() + ": ignoring the first stats after commission");
		  device.setFirstStatsAfterCommission(false);
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
				&& currentMillis - device.getLastStatsRcvdTime().getTime() > FIVE_MINUTE_INTERVAL) {
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
		int lastVoltsReported = lastVolts;
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
		  logger.debug(fixture.getId() + ": saving type -- " + savingType);
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
		System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		int cuStatus = ServerUtil.byteArrayToShort(tempShortByteArr);
		
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
				fixtureManager.enablePushProfileAndGlobalPushProfile(fixture, true, true);
			}
		}
		if(packet.length > index + 1) {
			//byte 63, 64 groups checksum
			System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
			int groupsChecksum = ServerUtil.byteArrayToShort(tempShortByteArr);
			logger.info(fixture.getId() + "(" + fixture.getGroupsSyncPending() + "): groups checksum -- " + groupsChecksum);
			if(!fixture.getGroupsSyncPending()) {
				//TODO OPTIMIZE
				DeviceServiceImpl.getInstance().handleGroupsSynchronization(fixture, groupsChecksum);
			}
			index += 2;
		}
		int pmStatsDuration = 300;
		if(packet.length > index + 1) {
			//new time field
			//byte 65, 66, 67, 68 time in sec
			System.arraycopy(packet, index, tempIntByteArr, 0, tempIntByteArr.length);
			int utcSec = ServerUtil.byteArrayToInt(tempIntByteArr);
			//System.out.println("pm stats utc sec -- " + utcSec);
			logger.debug(fixture.getId() + ": pm stats utc date -- " + new Date(utcSec * 1000l));
			if(utcSec > 0 && (utcSec % (5 * 60) == 0)) {
				statsDate = new Date(utcSec * 1000l);				
			}
			index += 4;
			
			//byte 69, 70 are duration in sec
			System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
			pmStatsDuration = ServerUtil.byteArrayToShort(tempShortByteArr);
			index += 2;
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
		
		Ballast ballast = ballastMgr.getBallastById(fixture.getBallast().getId());
		BallastVoltPowerManager ballastVoltPowerManager = (BallastVoltPowerManager) SpringContext.getBean("ballastVoltPowerManager");
		double fPowerFactor = ballastVoltPowerManager.getBallastVoltPowerFactor(ballast, 
		    lastVolts);
				
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after ballast volt power : "
				+ (System.currentTimeMillis() - startTime));
		}
		double voltPowerFactor = fPowerFactor;
		double fcFactor = 14;
		if(ServerUtil.compareVersion(fixture.getVersion(), "2.0") >= 0) {
		  fcFactor = 10.764;
		}
		if (lastVolts < 5) {
			fixture.setLightLevel((int)(minAmb / fcFactor));
		} else {
			fixture.setLightLevel((int)(maxAmb / fcFactor));
		}

		EnergyConsumption ec = new EnergyConsumption();

		ec.setBrightOffset(lightOffToOn);
		// this is the percentage of light
		ec.setBrightPercentage((short) voltPowerFactor);
		ec.setCuStatus(cuStatus);
		ec.setCaptureAt(statsDate);
		ec.setAvgVolts(avgVolts);
		ec.setDimOffset(lightOnToOff);
		ec.setFixture(fixture);
		ec.setLightAvgLevel((short) avgAmb);
		ec.setLightMaxLevel(maxAmb);
		ec.setLightMinLevel(minAmb);
		ec.setLightOff(motionOnToOff);
		ec.setLightOn(motionOffToOn);
		ec.setLightOnSeconds(lightOnSec);
		ec.setMaxTemperature((short) maxTemp);
		ec.setMinTemperature((short) minTemp);
		ec.setLastTemperature((short)lastTemp);
		if (ServerUtil.compareVersion(fixture.getVersion(), "2.0") < 0) {
			avgTemp = (short) ((avgTemp * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU1());
		} else {
			avgTemp = (short) ((avgTemp * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU2());
		}
		ec.setAvgTemperature(avgTemp);
		ec.setCurrState((short) currState);		
		ec.setEnergyCalib(energyCalib);
		ec.setMinVolts((short) minVolts);
		ec.setMaxVolts((short) maxVolts);
		ec.setEnergyTicks(pulses);
		ec.setSysUptime(uptime);
		// ec.setOccCount((short)motionCount);
		// ec.setOccIn(new Short(rand.nextInt(100)+""));
		// ec.setOccOut(new Short(rand.nextInt(100)+""));

		double avgPower = -1;
		double energyConsumed = -1;
		boolean calculatePower = true;
		if (fixture.getCommType() == ServerConstants.COMM_TYPE_PLC ||
		    energyCumTicks == LONG_MINUS_ONE * 100 || pulses == SHORT_MINUS_ONE) {
		  calculatePower = true;
		} else if (PerfSO.getInstance().getPmStatsMode() == ServerConstants.PM_STATS_FIRMWARE_MODE) {
		  ec.setEnergyCum(energyCumTicks);
		  if(ServerUtil.isNewCU(fixture)) {          	
		    avgPower = (double)pulses * 12 / 1000;		    
		  } else {
		    avgPower = PerfSO.getAvgPower(energyCalib, pulses, 5 * 60 * 1000, fixture);
		  }
		  if(logger.isDebugEnabled()) {
			    logger.debug(fixture.getId() + ": received average power -- " + avgPower);
		  }
		  	// energyConsumed = getAvgEnergyInMWH(energyCalib, pulses) * 5 / 60;
		  	calculatePower = false;
		  	ec.setPowerCalc(ServerConstants.POWER_CALCULATED_SU);
		  
		  	// Support for calculating power for fixture with tomcat type ballast, where one CU drives the ganged setup, but
		  	// the meter reports the consumption of one.
			if (ballast.getBallastType() != null && ballast.getBallastType() == ServerConstants.BALLAST_TYPE_TOMCAT) {
				avgPower = avgPower * fixture.getNoOfFixtures();
				  if(logger.isDebugEnabled()) {
					    logger.debug(fixture.getId() + ": adjusted average power (tomcat)-- " + avgPower);
				  }
			}
		  
		  	// Support for calculating power for emergency fixture, currently the reading reports consumption less
			// than 1.5W, hence need to fix this by using the softmetering...
			if (avgPower < MIN_POWER_USED_THRESHOLD
					&& fixture.getFixtureType() != null
					&& fixture.getFixtureType() == ServerConstants.FX_TYPE_EMERGENCY) {
				SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext
						.getBean("systemConfigurationManager");
				SystemConfiguration oConfig = sysMgr
						.loadConfigByName("enable.emergencyfx.calc");
				if (oConfig != null) {
					String strValue = oConfig.getValue();
					if (strValue != null && strValue.contentEquals("true")) {
						int volts = avgVolts;
						int rem1 = volts % 5;
						if (rem1 > 2) {
							volts += (5 - rem1);
						} else {
							volts -= rem1;
						}
						logger.debug(fixture.getId()
								+ ": emergency fixture, check ballast vo" + ballast.getId() + ", " + volts + ", " + fixture.getVoltage());
			            FixtureCalibrationMap fcm = getFixtureCalibration(fixture.getId(), (float)volts/10);
						BallastVoltPower bvp = getBallastVoltPowerCurve(ballast,(float)volts/10, fixture.getVoltage()); 
						if (fcm != null) {
					        avgPower = fcm.getPower().doubleValue();
							ec.setPowerCalc(ServerConstants.POWER_CALCULATED_GEMS);
							if (logger.isDebugEnabled()) {
								logger.debug(fixture.getId()
										+ ": emergency fixture, calculated average power using fxcurve-- "
										+ avgPower);
							}
						}else if (bvp != null) {
					        avgPower = bvp.getPower().doubleValue();
							ec.setPowerCalc(ServerConstants.POWER_CALCULATED_GEMS);
							if (logger.isDebugEnabled()) {
								logger.debug(fixture.getId()
										+ ": emergency fixture, calculated average power using ballastcurve -- "
										+ avgPower);
							}
						}else {
							if (logger.isDebugEnabled()) {
								logger.debug(fixture.getId()
										+ ": emergency fixture, no volt-power curve available!");
							}
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug(fixture.getId()
									+ ": emergency fixture, calculation disabled!");
						}
					}
				}
		  	}
		}
		if (calculatePower) {
			SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
			SystemConfiguration softMeteringConfig = sysMgr.loadConfigByName("enable.softmetering");
			if (softMeteringConfig != null) {
				String softMeteringFlag = softMeteringConfig.getValue();
				if(softMeteringFlag.contentEquals("true")) {
					avgPower = getAvgPowerFromVolts(avgVolts, ballast.getLampNum(),
							ballast) * fixture.getNoOfFixtures(); // watts
				} else {
					//for sppa soft metering is not allowed so ignoring the stats ideally we could use other stuff apart
					//from energy/load.
					return;	
				}
				ec.setPowerCalc(ServerConstants.POWER_CALCULATED_GEMS);
			}
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
		
		if (avgPower > MAX_CU2_LOAD) { // 600 is the max for 2.0 enlighted relays			
			// faulty CU/SU
		  logger.error(fixture.getId() + ": Energy reading too high(" + avgPower + ")");
//			eventsAndFaultManager.addAlarm(fixture, "Energy reading too high ("
//			    + avgPower + ")", EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR,
//			    EventsAndFault.MAJOR_SEV_STR);
			avgPower = compBasePowerUsed;
		}

		energyConsumed = avgPower * 5 / 60; // watts hr
		if(logger.isDebugEnabled()) {
		  logger.debug(fixture.getId() + ": energy consumed -- " + energyConsumed);
		}
		
		boolean sweepEnabled = handleSweepTimer(ec, fixture, statsDate, lastOccSeen);
		if(sweepEnabled) {
			if(logger.isDebugEnabled()) {
		    logger.debug(fixture.getId() + ": sweep timer is active");
		  }
		}
		if(ballast != null && ballast.getBaselineLoad() != null) {
			basePowerUsed = ballast.getBaselineLoad().doubleValue() * fixture.getNoOfFixtures();
		} else {
			if (currState == ServerConstants.CURR_STATE_BASELINE && pmStatsDuration <= 300) {
				if(avgPower < compBasePowerUsed) {
					basePowerUsed = compBasePowerUsed;
				} else {
					basePowerUsed = avgPower;
				}
			} else {
				if(fixture.getBaselinePower() != null) {
					basePowerUsed = fixture.getBaselinePower().doubleValue();
				}
				if (basePowerUsed == 0) { // base power was not updated in the
										// database, get it from ballast info
					basePowerUsed = compBasePowerUsed;
				}
			}
		}

		if(pmStatsDuration > 300) {
			//short term fix so that base line is not increased  because of a longer duration pm stats
			//TODO fix it correctly so that only load corresponding to avg volts is taken into this bucket and the rest be added to the previous bucket
			logger.error(fixture.getId() + ": received a longer duration stats - " + pmStatsDuration);
			if(avgPower > basePowerUsed) {
				avgPower = basePowerUsed;
			}
		} else {
			if(avgPower > ((compBasePowerUsed * 1.2))) {
				logger.error(fixture.getId() + ": Fixture might be configured with wrong ballast(" + avgPower + ")");
			}
		}
		
		if(ballast == null || ballast.getBaselineLoad() == null) {
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
			if(sweepEnabled) {
				basePowerUsed = avgPower;		  
			}
		} else {
			if(avgPower > basePowerUsed) {				
				//raise an event
				eventsAndFaultManager.addEvent(fixture, "Fixture might be configured with wrong ballast("
						+ avgPower + ")", EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR);
				avgPower = basePowerUsed;
			}
		}
		if (device != null) {
			device.setBasePower(basePowerUsed);
		}
//		timingLogger.debug("after baseline : "
//				+ (System.currentTimeMillis() - startTime));
		ec.setPowerUsed(new BigDecimal(avgPower));
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
		ec.setSavingType((short)savingType);
		ec.setLastVolts((short)lastVoltsReported);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after power saving handle : "
				+ (System.currentTimeMillis() - startTime));
		}
		
		// ENL-3626 - Savings attribution for fixture swap (sPPA) - 
	    // Need to distribute savings to different types based on computed baseline and then attribute (baseline_load of ballast table - computed baseline) 
	    // to the task tuned savings.
		double diffInBaseLine =0;
		if(ballast.getBaselineLoad()!=null)
		{
			if (basePowerUsed > compBasePowerUsed)
			{
				diffInBaseLine = basePowerUsed - compBasePowerUsed;
			}
			if(savedPower>diffInBaseLine)
			{
				savedPower = savedPower - diffInBaseLine;
			}
		}
		
		if(savedPower > 0) {
		  switch (savingType) {
		  case ServerConstants.STATS_SAVING_OCC:			
		  case ServerConstants.STATS_SAVING_AMB:
		  case ServerConstants.STATS_SAVING_TUNEUP:
			adjustTaskTunedSavings(fixture, ec, savedPower, avgVolts,savingType, lastVoltsReported, motionBitsLong);
			break;
		  case ServerConstants.STATS_SAVING_MANUAL:
			ec.setManualSaving(savedPowerUsed);
			break;
		  default:
			ec.setOccSaving(savedPowerUsed);
			break;
		  }
		}
		if(diffInBaseLine>0 && (sweepEnabled==false))
		{
			ec.setTuneupSaving(ec.getTuneupSaving().add(new BigDecimal(diffInBaseLine)));
		}		
		
		ec.setMotionBits(motionBitsLong);
		ec.setZeroBucket(ORIG_BUCKET);
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":after adjust task/amb: "
				+ (System.currentTimeMillis() - startTime));	
		}
		if((currentMillis - device.getLastStatsRcvdTime().getTime()) >= FIVE_MINUTE_INTERVAL) {
		  try {
			//TODO OPTIMIZE (Batching)
		    energyConsumptionManager.save(ec);
		  } catch(Exception e) {
			logger.error(fixture.getId() + ": could not insert the stats -- " + e.getMessage());
		  }
		  device.setLastStatsSeqNo(seqNo);
		} else {
		  logger.error(fixture.getId() + ": Stats came too early");
		}		
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixture.getId() + ":time for saving ec :  "
				+ (System.currentTimeMillis() - startTime));
		}

		long statsDateTime = statsDate.getTime();
		Date fixtLastStatsRcvdTime = device.getLastStatsRcvdTime();
		try {
			fixture.setLastStatsRcvdTime(statsDate);
			if(fixture.getCurrApp() == null || fixture.getCurrApp().compareTo((short)currApp) != 0) {
				device.setPmStatUpdateReq(true);
			}
			fixture.setCurrApp((short) currApp);
			if(fixture.getCurrentState() == null || !fixture.getCurrentState().equals(ServerUtil.getCurrentState(currState))) {
				device.setPmStatUpdateReq(true);
			}
			fixture.setCurrentState(ServerUtil.getCurrentState(currState));
			fixture.setWattage((int)Math.round(avgPower));
			fixture.setAvgTemperature(avgTemp);
			if(!sweepEnabled) {
				if(fixture.getBaselinePower() == null || fixture.getBaselinePower().doubleValue() != basePowerUsed) {
					device.setPmStatUpdateReq(true);
				}
				fixture.setBaselinePower(new BigDecimal(basePowerUsed));
			}
			if (ServerUtil.compareVersion(fixture.getVersion(), ServerMain
					.getInstance().getGemsVersion()) == 0) {
				// This will ensure that after the upgrade of SU image the
				// version synced bit is cleared.
				if(fixture.getVersionSynced() == null || fixture.getVersionSynced().compareTo(0) != 0) {
					device.setPmStatUpdateReq(true);
				}
				fixture.setVersionSynced(0);
			}
			fixture.setLastConnectivityAt(new Date());
			if(device.isPmStatUpdateReq()) {
				fixtureManager.updateStats(fixture);
				device.setPmStatUpdateReq(false);
			}
			if(timingLogger.isDebugEnabled()) {
			  timingLogger.debug(fixture.getId() + ":time for saving fixture :  "
				+ (System.currentTimeMillis() - startTime));
			}
//			timingLogger.debug("after fixture update :  "
//					+ (System.currentTimeMillis() - startTime));
			// update the device cache attributes
			if (device != null) {
				device.setLastStatsRcvdTime(statsDate);
				device.setUptime(uptime);
				device.setBootTime(new Date(System.currentTimeMillis() - uptime
						* 1000));
				device.setEnergyCum(energyCumTicks);
			}			
			if (avgPower >= PerfSO.getFixtureOutageDetectWatts()) {
				if(device.isFixtureOutageEvent()) {
					eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
					device.setFixtureOutageEvent(false);
				}
              checkForBulbOutageAlarm(minVolts, maxVolts, avgVolts, savingType, lastOccSeen, device, fixture, avgPower, compBasePowerUsed);
			} else {
			  //for new CU don't check for calib value and ticks
			  //for old CU both calib and ticks should be greater than 0 for the 
			  //stats bucket to be considered for outage
			  if(ServerUtil.isNewCU(fixture) || (energyCalib > 0 && pulses > 0)) {
			    raiseOutageAlarm(avgPower, minVolts, maxVolts, savingType,
				lastOccSeen, device, fixture, avgVolts);
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
			//TODO OPTIMIZE not required with batching in place
		    PerfSO.getInstance().addZeroBucket(device, fixture, statsDateTime);
		  }
		  if(!calculatePower) {
			//TODO OPTIMIZE
		    PerfSO.getInstance().addFixtureToZeroBucketUpdQueue(fixture.getId(),fixtLastStatsRcvdTime, statsDate, sweepEnabled);
		  }
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

	public static double getAvgPowerFromVolts(int volts, int noOfLamps, Ballast ballast) {

		if (volts == 0) {
			return 0;
		}
		int bulbWattage = ballast.getWattage();
		BallastVoltPowerManager ballastVoltPowerManager = (BallastVoltPowerManager) SpringContext.getBean("ballastVoltPowerManager");
		double fPowerFactor = ballastVoltPowerManager.getBallastVoltPowerFactor(ballast, 
		    volts);
		double ballast_factor = ballast.getBallastFactor();
		double calcPower = ballast_factor * bulbWattage * noOfLamps / 100;
		if (fPowerFactor > 0) {
			calcPower = calcPower * fPowerFactor;
		}
		return calcPower;

	} // end of method getAvgPowerFromVolts
	
	private void adjustTaskTunedSavings(Fixture fixture, EnergyConsumption ec, double savedPower, 
	    short avgVolts, int savingType, int lastVolts, long motionBits) {

	  try {	
	    Calendar currentDate = Calendar.getInstance();
	    int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60 + currentDate.get(Calendar.MINUTE);
	    int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
	    if (dayOfWeek == 0) {
	      dayOfWeek = 7;
	    }
	    //TODO OPTIMIZE
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

	private void raiseOutageAlarm(double avgPower, short minVolts, short maxVolts, int savingType, 
	    int lastOccSeen, DeviceInfo device, Fixture fixture, short avgVolts) {
		logger.info(fixture.getFixtureName() + ": outage data[minVolts="
				+ minVolts + ",maxVolts=" + maxVolts + ",saving=" + savingType
				+ ",lastOcc=" + lastOccSeen + ",power=" + avgPower + "]");

		if (avgVolts == 0 || minVolts != maxVolts
				|| !fixture.getCurrentState().equals(
						ServerConstants.CURR_STATE_AUTO_STR)
				|| lastOccSeen >= 300
				|| savingType == ServerConstants.STATS_SAVING_AMB) {
			return;
		}
		PerfSO.getInstance();
		// if the current power is less than 7 watts, raise an alarm that
		// fixture is out
		// in GE ballast which is parallel, we observed 6.9 watts when all bulbs
		// are out
		if (avgPower < PerfSO.getFixtureOutageDetectWatts()) {
			if(device.isFixtureBulbOutageEvent()) {
				eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
				device.setFixtureBulbOutageEvent(false);
			}
			logger.error(fixture.getFixtureName()
					+ ": fixture is out. raising outage alarm");
			if(device != null) {
				device.setFixtureOutageEvent(true);
			}
			eventsAndFaultManager.addAlarm(fixture, "Fixture is out",
					EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
			return;
		}
	} // end of method raiseOutageAlarm

	private int getBit(byte[] data, int pos) {

		int posByte = pos / 8;
		int posBit = pos % 8;
		byte valByte = data[posByte];
		int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
		return valInt;

	} // end of method getBit
	
	/**
	 * Detecting Lamp(s) outage for fixture
	 * @param minVolts
	 * @param maxVolts
	 * @param avgVolts
	 * @param savingType
	 * @param lastOccSeen
	 * @param device
	 * @param fixture
	 * @param avgPower
	 * @param compBasePowerUsed
	 */
    private void checkForBulbOutageAlarm(short minVolts, short maxVolts, short avgVolts, int savingType, int lastOccSeen,
            DeviceInfo device, Fixture fixture, double avgPower, double compBasePowerUsed) {
        logger.info(fixture.getId() + ": P1 [minVolts=" + minVolts + ", maxVolts=" + maxVolts + ", avgVolts= " + avgVolts
                + ", power=" + avgPower + ", ballast computed= " + compBasePowerUsed + "]");

        if (avgVolts == 0 || (minVolts != maxVolts)) {
            return;
        }
        if (avgPower < PerfSO.getFixtureOutageDetectWatts()) {
            return;
        }

        int rem = avgVolts % 5;
        if (rem > 2) {
            avgVolts += (5 - rem);
        } else {
            avgVolts -= rem;
        }
        
        if (device.getLastVoltForOutageReading() != avgVolts) {
            device.resetLampOutageCount(avgVolts);
        }
        try {
            FixtureCalibrationMap fcm = getFixtureCalibration(fixture.getId(), (float)avgVolts/10);
            Ballast ballast = ballastMgr.getBallastById(fixture.getBallast().getId());
            BallastVoltPower bvp = getBallastVoltPowerCurve(ballast, (float)avgVolts/10, fixture.getVoltage());
            double fRefPowerUsed = 0;
            double fRefVoltUsed = 0;
            boolean isLampOutageEnabled = true;
            NumberFormat nf = NumberFormat.getInstance();
           	nf.setMaximumFractionDigits(2);
            if (fcm != null) {
                fRefPowerUsed = fcm.getPower().doubleValue();
                fRefVoltUsed = fcm.getVolt().doubleValue();
                isLampOutageEnabled = fcm.getEnabled();
                logger.info(fixture.getId() + ": refering curve for fixture, [id=" + fcm.getFixtureLampCalibration().getId() + "]");
            } else if (bvp != null) {
                if (avgPower <= ((compBasePowerUsed * 1.2))) {
                    fRefPowerUsed = bvp.getPower().doubleValue();
                    fRefVoltUsed = bvp.getVolt().doubleValue();
                    isLampOutageEnabled = bvp.getEnabled();
                    logger.info(fixture.getId() + ": refering curve for ballast, [id=" + ballast.getId()
                            + ", inputvolt= " + fixture.getVoltage() + "]");
                } else {
                    logger.error(fixture.getId() + ": configured with ballast " + fixture.getBallast().getId()
                            + ", inputvolt= " + fixture.getVoltage() + " might be wrong (" + avgPower + " / "
                            + compBasePowerUsed + ")");
                    return;
                }
            } else {
                logger.info(fixture.getId() + ": configured with ballast " + fixture.getBallast().getId()
                        + ", inputvolt= " + fixture.getVoltage() + " not yet calibrated!");
                return;
            }
            
            if(!isLampOutageEnabled) {
            	return;
            }

            LampCalibrationConfiguration lcc = fixtureCalibrationManager.getCalibrationConfiguration();
            Short pd_threshold = 5;
            int LAMP_OUTAGE_FREQUENCY = 2;
            if (lcc != null) {
                pd_threshold = lcc.getPotentialDegradeThreshold();
            }

            if (fRefPowerUsed > 0) {
                Double deviation = 100 - Math.abs((avgPower * 100) / fRefPowerUsed);
                logger.info(fixture.getId() + ": P2 [control volt=" + avgVolts + ", avgPower=" + avgPower
                        + ", refVolts=" + fRefVoltUsed + ", refPower=" + fRefPowerUsed + ", counts="
                        + device.getLampOutageCount(avgVolts) + "]");
                if (deviation > pd_threshold) {
                    device.incLampOutageCount(avgVolts);
                    if (device.getLampOutageCount(avgVolts) >= LAMP_OUTAGE_FREQUENCY) {
                        logger.info(fixture.getId() + ": Lamp(s) out [count= "
                                + device.getLampOutageCount(avgVolts) + ", deviation=" + deviation + "%]");
                        device.setFixtureBulbOutageEvent(true);
                        eventsAndFaultManager.addUpdateAlarm(fixture, "Lamp(s) out [Light level=" + avgVolts
                                + "%, power usage=" + nf.format(avgPower) + "W, expected power usage=" + nf.format(fRefPowerUsed) + "W, count="
                                + device.getLampOutageCount(avgVolts) + "]",
                                EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
                    }
                } else {
                    logger.info(fixture.getId() + ": Clearing Lamp(s) out");
                    device.resetLampOutageCount(avgVolts);
            		if(device.isFixtureBulbOutageEvent()) {
            			eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
            			device.setFixtureBulbOutageEvent(false);
            		}
                    
                }
            }
        } catch (NullPointerException npe) {
            logger.error(npe.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return;
    }

    private FixtureCalibrationMap getFixtureCalibration(Long fixtureId, float avgVolts) {
        FixtureLampCalibration flc = fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixtureId);
        FixtureCalibrationMap fcm = null;
        if (flc != null) {
            Set<FixtureCalibrationMap> scp = flc.getFixtureCalibrationMap();
            Iterator<FixtureCalibrationMap> itrFCM = scp.iterator();
            while(itrFCM.hasNext()) {
                fcm = itrFCM.next();
                if (fcm != null && fcm.getVolt() == avgVolts) {
                    return fcm;
                }
            }
        }
        return fcm;
    }
    
    /**
     * Return ballast curve for the ballast id for reference volt taken at specified input volt (120, 277, 240; etc)
     */
    private BallastVoltPower getBallastVoltPowerCurve(Ballast ballast, float volt, double inputVolt) {
        if (ballast != null) {
            List<BallastVoltPower> voltPowerList = ballastVoltPowerManager.getBallastVoltPowerByBallastIdInputVoltage(ballast.getId(),
                    inputVolt);
            if (voltPowerList != null) {
                logger.info("[ballastid: " + ballast.getId() + ", inputvolts: " + inputVolt + ", curve: " + voltPowerList.size());
                Iterator<BallastVoltPower> voltPoweritr = voltPowerList.iterator();
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
}