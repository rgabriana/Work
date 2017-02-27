package com.ems.server.service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.cache.BallastCache;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.cache.SweepTimerCache;
import com.ems.dao.FixtureDao;
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
import com.ems.server.GatewayInfo;
//import com.ems.occengine.OccupancyEngine;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.data.PMStatsWork;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureCalibrationManager;
import com.ems.service.FixtureManager;
import com.ems.service.LicenseSupportManager;
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
	
	public static short ORIG_BUCKET = 0;
	public static short ZERO_BUCKET = 1;
	
	public static final long LONG_MINUS_ONE = 4294967295L;
	public static final long SHORT_MINUS_ONE = 65535;
	private static final int MAX_CU2_LOAD = 600;
	private static final float MIN_POWER_USED_THRESHOLD = 1.5f;

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
	
	@Resource
	private FixtureCalibrationManager fixtureCalibrationManager;
	
	@Resource
	private SystemConfigurationManager systemConfigurationManager;
	private HashMap<String, Date> sweepOverrideMap = new HashMap<String, Date>();
	@Resource
	LicenseSupportManager licenseSupportManager;
//	private int temperatureOffsetSU1 = 18;
//	private int pmStatsMode = ServerConstants.PM_STATS_GEMS_MODE;
	
	public void setFixtureManager(FixtureManager fixtureManager) {
		this.fixtureManager = fixtureManager;
	}

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
		PMStatsWork statsWork = null;
		try {
			fixtureDao.getSession().setFlushMode(FlushMode.MANUAL);
			final ListIterator<PMStatsWork> itr = processingQueue.listIterator();
			while(itr.hasNext()){
				statsWork = itr.next();
				updateStats(statsWork.getFixture(), statsWork.getPacket(),
						statsWork.getSeqNo());
			}
			fixtureDao.getSession().flush();
		} catch (Exception ex) {
			String macaddress = "";
			if (statsWork != null) {
				if (statsWork.getFixture() != null) {
					macaddress = statsWork.getFixture().getMacAddress();
				}
			}
			logger.error(
					macaddress
							+ ": Error in PMstats processing! (QueueSize: "
							+ String.valueOf(processingQueue == null ? 0
									: processingQueue.size())
							+ ", AvailableVMMemory: "
							+ Runtime.getRuntime().freeMemory() + ")", ex);
		}finally{
			if(timingLogger.isDebugEnabled()) {
				  timingLogger.debug("Packet Processing (Stats):"
						+ (System.currentTimeMillis() - startTime) + " Processed: "
						+ processingQueue.size());
			}
			statsWork = null;
			processingQueue.clear();
			processingQueue = null;
		}
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
	
	public void processShortPMStats(byte[] pkt, GatewayInfo gwInfo, long timeAdj) {
		
		ServerUtil.logPacket("short pm packet", pkt, logger);
		
		//0th byte is message type
  	int index = 1;
  	//1st byte is protocol
  	index++;
  	//2nd, 3rd, 4th, 5th bytes are utc time on gateway
  	index += 4;
  	//6th byte is no. of pm stats short packets
  	int noOfStats = pkt[index++];
  	if(noOfStats == 0) {
  		//end of store and forward stream
  		//nothing to do
  		return;
  	}
  	ByteBuffer pktBuff = ByteBuffer.wrap(pkt);
  	for(int i = 0; i < noOfStats; i++) {
  		System.out.println("offset -- " + index);
  		pktBuff.position(index);
  		byte[] dst = new byte[13];
  		pktBuff.get(dst, 0, 13);
  		System.out.println("individual short pm -- " + ServerUtil.getLogPacket(dst));
  		processShortSensorPMStats(dst, timeAdj);
  		index += 13;
  	}
 
	} //end of method processShortPMStats
	
	public void processShortSensorPMStats(byte[] pkt, long timeAdj) {
  	
		ServerUtil.logPacket("individual short pm packet", pkt, logger);
		
  	String macAddr = ServerUtil.getSnapAddr(pkt[0], pkt[1], pkt[2]); 
		int index = 3;
		byte[] tempShortByteArr = new byte[2];
		byte[] tempIntByteArr = new byte[4];
		
		System.arraycopy(pkt, index, tempShortByteArr, 0, tempShortByteArr.length);
		int  energyTicks = ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;
		
		System.arraycopy(pkt, index, tempIntByteArr, 0, tempIntByteArr.length);
		long energyCum = ServerUtil.intByteArrayToLong(tempIntByteArr) * 100;
		index += 4;
		
		System.arraycopy(pkt, index, tempIntByteArr, 0, tempIntByteArr.length);
		long utcTime = ServerUtil.intByteArrayToLong(tempIntByteArr);
  	
  	Fixture fixture =  FixtureCache.getInstance().getDeviceFixture(macAddr);
    if(fixture == null) {
      logger.error(macAddr + ": There is no fixture, ignoring the short pm pkt - " + 
      		ServerUtil.getLogPacket(pkt));	
      return;
    }
    if (!fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
      // fixture is not yet commissioned. So, ignore the stats
      logger.error(macAddr + ": Fixture is not yet commissioned");
      return;
    }
    EnergyConsumption ec = new EnergyConsumption();
    ec.setFixture(fixture);
    
    DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
    
    double avgPower = -1;
		double energyConsumed = -1;
		Ballast ballast = fixtureManager.getBallastById(fixture.getBallast().getId());
		if (fixture.getCommType() == ServerConstants.COMM_TYPE_PLC ||
		    energyCum == LONG_MINUS_ONE * 100 || energyTicks == SHORT_MINUS_ONE) {
		  //for short pm stats we cannot do soft metering. so ignoring the packet
		  return;
		} else if (PerfSO.getInstance().getPmStatsMode() == ServerConstants.PM_STATS_FIRMWARE_MODE) {
		  ec.setEnergyCum(energyCum);    	
		  avgPower = (double)energyTicks * 12 / 1000;
		  if(logger.isDebugEnabled()) {
		  	logger.debug(fixture.getId() + ": received average power -- " + avgPower);
		  }
		  ec.setPowerCalc(ServerConstants.POWER_CALCULATED_SU);
		  // Support for calculating power for fixture with tomcat type ballast, where one CU drives the ganged setup, but
		  // the meter reports the consumption of one.
			if (ballast.getBallastType() != null && ballast.getBallastType() == ServerConstants.BALLAST_TYPE_TOMCAT) {
				avgPower = avgPower * fixture.getNoOfFixtures();
				  if(logger.isDebugEnabled()) {
					    logger.debug(fixture.getId() + ": adjusted average power (tomcat)-- " + avgPower);
				  }
			}
		}
		double basePowerUsed = 0;

		double compBasePowerUsed = ballast.getBallastFactor()
				* ballast.getWattage() * ballast.getLampNum()
				* fixture.getNoOfFixtures();
		
		if (avgPower > MAX_CU2_LOAD) { // 600 is the max for 2.0 enlighted relays			
			// faulty CU/SU
		  logger.error(fixture.getId() + ": Energy reading too high(" + avgPower + ")");
			avgPower = compBasePowerUsed;
		}

		if(ballast != null && ballast.getBaselineLoad() != null) {
			basePowerUsed = ballast.getBaselineLoad().doubleValue() * fixture.getNoOfFixtures();
		} else {
			if(fixture.getBaselinePower() != null) {
				basePowerUsed = fixture.getBaselinePower().doubleValue();
			}
			if (basePowerUsed == 0) { // base power was not updated in the
				// database, get it from ballast info
				basePowerUsed = compBasePowerUsed;
			}
		}

		if(avgPower > ((compBasePowerUsed * 1.2))) {
			logger.debug(fixture.getId() + ": Fixture might be configured with wrong ballast(" + avgPower + ")");
		}
		
		if(ballast == null || ballast.getBaselineLoad() == null) {
			if (avgPower > basePowerUsed) {
				basePowerUsed = avgPower;
			} 
		} else {
			if(avgPower > basePowerUsed) {				
				//raise an alarm
				logger.debug(fixture.getId() + ": Fixture might be configured with wrong ballast(" + avgPower + ")");
				eventsAndFaultManager.addAlarm(fixture, "Fixture might be configured with wrong ballast("+ avgPower + ")", EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR);
				avgPower = basePowerUsed;
			}
		}
		if (device != null) {
			device.setBasePower(basePowerUsed);
		}
		
		energyConsumed = avgPower * 5 / 60; // watts hr
		if(logger.isDebugEnabled()) {
		  logger.debug(fixture.getId() + ": energy consumed -- " + energyConsumed);
		}
		
		//if utc time is epoc time add the adjustment
		long statsTime = utcTime + timeAdj;
		if(timeAdj > 0) {
			//adjust it to nearest 5 minute
			long min5 = 5 * 60 * 1000;
			statsTime = statsTime - statsTime % min5;
		}
		Date statsDate = new Date(statsTime);
		
		Double price = pricingManager.getPrice(fixture, statsDate);
		ec.setCaptureAt(statsDate);
		ec.setEnergyTicks(energyTicks);
		ec.setPrice(price.floatValue());
		ec.setCost((float) (energyConsumed * price.floatValue()) / 1000);
		ec.setPowerUsed(new BigDecimal(avgPower));
		ec.setBasePowerUsed(new BigDecimal(basePowerUsed));
		ec.setBaseCost((float) basePowerUsed * 5 * price.floatValue() / (1000 * 60));
		ec.setSavedCost(ec.getBaseCost() - ec.getCost());
		double savedPower = basePowerUsed - avgPower;
		BigDecimal savedPowerUsed = new BigDecimal(savedPower);
		ec.setSavedPowerUsed(savedPowerUsed);
		
		BigDecimal zeroSaving = new BigDecimal(0.0);
		ec.setManualSaving(zeroSaving);
		ec.setAmbientSaving(zeroSaving);
		ec.setOccSaving(zeroSaving);
		ec.setTuneupSaving(zeroSaving);
		double diffInBaseLine = 0;
		if(ballast.getBaselineLoad()!=null) {
			if (basePowerUsed > compBasePowerUsed) {
				diffInBaseLine = basePowerUsed - compBasePowerUsed;
				logger.debug("Saving Attribution : diffInBaseLine " + diffInBaseLine + ", Saved Power : " + savedPower);
			}
			// ENL-4321 - Wrong savings attributions -  Task tune saving was more than the baseline load issue
			savedPower = savedPower - diffInBaseLine;
			logger.debug("Saving Attribution : Saved Power After deducting difference in baseline " + savedPower);
			if(savedPower < 0)
			{
				diffInBaseLine = diffInBaseLine + savedPower;
				logger.debug("Saving Attribution : Saved Power is < 0 hence adding diffInBaseLine into savedpower to attribute in tasktune" + savedPower);
			}
			// ENDS ENL-4321
		}
		ec.setSavingType((short)ServerConstants.STATS_SAVING_OCC); //TODO what should be the saving type. defaulting to occ saving
		ec.setOccSaving(savedPowerUsed);
		if(diffInBaseLine>0) {
			ec.setTuneupSaving(ec.getTuneupSaving().add(new BigDecimal(diffInBaseLine)));
		}	
		logger.debug(fixture.getId() + " trying to save as " + ec.getCaptureAt());
		
		ec.setZeroBucket((short)0);
		try {
			energyConsumptionManager.save(ec);
		}
		catch(Exception e) {
			logger.error(fixture.getMacAddress() + "- not able to save ec for " + ec.getCaptureAt(), e);
			//we need to update the existing entry
			try {
			energyConsumptionManager.updateShortEnergyStats(ec);
			} catch (Exception e1) {
				e1.printStackTrace();
				logger.error(fixture.getMacAddress() + "- error in updating zero bucket- " + ec.getCaptureAt());
			}
			
		}

  } //end of method processShortSensorPMStats

	private void updateStats(Fixture fixture, byte[] packet, int seqNo) {

		long startTime = System.currentTimeMillis();
		DeviceInfo device = FixtureCache.getInstance().getDevice(fixture);
		if (device != null && device.getLastStatsSeqNo() == seqNo) {
			// duplicate packet ignore it
			logger.debug(fixture.getId() + ": Duplicate stats received");
			packet = null;
			fixture = null;
			return;
		}
		if(device.isFirstStatsAfterCommission()) {
		  logger.debug(fixture.getId() + ": ignoring the first stats after commission");
		  device.setFirstStatsAfterCommission(false);
		  return;
		}
		//Add packet into Occupancy engine to calculate area level occupancy
		//Fixture dbFixture = fixtureManager.getFixtureById(fixture.getId());
		//if(licenseSupportManager.isZoneSensorsEnabled() && (dbFixture.getArea()!=null && dbFixture.getArea().getZoneSensorEnable() ==true))
		//OccupancyEngine.getInstance().addPacket(packet);
		
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
		int groupsChecksum = 0;
		if(packet.length > index + 1) {
			//byte 63, 64 groups checksum
			System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
			groupsChecksum = ServerUtil.byteArrayToShort(tempShortByteArr);
			logger.info(fixture.getId() + "(" + fixture.getGroupsSyncPending() + "): groups checksum -- " + groupsChecksum);
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
		byte avgTempPrecision =0;
		if (packet.length > index + 1) {
        	// 71 The tenths precision of the current 
			avgTempPrecision = packet[index++];
			// Fix to ensure that the 10th of decimal is positive.
			avgTempPrecision = (byte)Math.abs(avgTempPrecision);
        }
		int currentAmbientThreshold  = 0;
		if(logger.isDebugEnabled()) {
	          logger.debug(fixture.getId() + ": checking if  ambient calib field is there-- index:"+index);
	        }
		if (packet.length > index + 1) {
            // 72,73 The ambient calib value uint16_t ambient_calib; // The current ambient calibration level.
		    System.arraycopy(packet, index, tempShortByteArr, 0, tempShortByteArr.length);
		    currentAmbientThreshold = ServerUtil.byteArrayToShort(tempShortByteArr);
		    if(logger.isDebugEnabled()) {
                logger.debug(fixture.getId() + ": currentAmbientThreshold-- "+currentAmbientThreshold+": ManualAmbientValue-- "+fixture.getManualAmbientValue());
              }
		    index += 2;
		    fixture.setCurrentAmbientValue(currentAmbientThreshold);
		    if(fixture.getManualAmbientValue()>-1&&currentAmbientThreshold!=fixture.getManualAmbientValue()){
		        int[] fixtureArr = new int[]{fixture.getId().intValue()};
		        DeviceServiceImpl.getInstance().setFixturesAmbientThresholdValue(fixtureArr,false,fixture.getManualAmbientValue());
		    }
        }
		byte profileOverride = 0;
		if(packet.length > index + 1) {
			index +=1; //groups
			profileOverride = packet[index++];
		}
		if(!fixture.getGroupsSyncPending()) {
			DeviceServiceImpl.getInstance().handleGroupsSynchronization(fixture, groupsChecksum, profileOverride);
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
		Ballast ballast = fixtureManager.getBallastById(fixture.getBallast().getId());
		double fPowerFactor = BallastCache.getInstance().getBallastVoltPowerFactor(ballast, 
		    lastVolts);
				
//		BallastVoltPower obvp = getBallastVoltPowerFactor(ballast, 
//		    (float) lastVolts / 10);
//		if (obvp == null) {
//			logger.error("Error fetching VoltPowerMap: (" + lastVolts + " => "
//					+ fPowerFactor + ")");
//		} else {
//			fPowerFactor = obvp.getPower().floatValue();
//			if(logger.isDebugEnabled()) {
//			  logger.debug("Fetching from Ballast: " + obvp.getVoltPowerMapId()
//					+ ", VoltPowerMap for Bulb life: (" + lastVolts + " => "
//					+ fPowerFactor + ")");
//			}
//		}
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
		//setBulbLife(fixture, voltPowerFactor);

		EnergyConsumption ec = new EnergyConsumption();

//		ec.setId(energyConsumptionManager.getEnergyConsumptionId(
//				fixture.getId(), statsDate));

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
		Double fxTemp = new Double(0);
		if (ServerUtil.compareVersion(fixture.getVersion(), "2.0") < 0) {
			fxTemp = new Double (((new Float(avgTemp + "." + avgTempPrecision)) * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU1());
        } else {
        	fxTemp = new Double (((new Float(avgTemp + "." + avgTempPrecision)) * 9 / 5) + 32 - PerfSO.getInstance().getTemperatureOffsetSU2());
		}
		ec.setAvgTemperature(fxTemp);
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
		logger.debug("calculatePower :" + calculatePower + " Fixture :("+ fixture.getId()+")"+fixture.getSnapAddress());
		if (calculatePower) {
			SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
			SystemConfiguration softMeteringConfig = sysMgr.loadConfigByName("enable.softmetering");
			if (softMeteringConfig != null) {
				String softMeteringFlag = softMeteringConfig.getValue();
				if(softMeteringFlag.contentEquals("true")) {
					int volts = avgVolts;
					int rem1 = volts % 5;
					if (rem1 > 2) {
						volts += (5 - rem1);
					} else {
						volts -= rem1;
					}
					BallastVoltPower bvp = getBallastVoltPowerCurve(ballast, (float)volts/10, fixture.getVoltage());
					if(bvp!=null){
						logger.debug("Executing Soft meternig Workflow  for Fixture ("+fixture.getId()+")"+fixture.getSnapAddress() +" Refering Imported Ballast Curve for Ballast Id:"+ bvp.getBallastId()+ " Avg Power:" + bvp.getPower() + " Input Volt:" + bvp.getInputVolt() + " VoltPowerMapId:" + bvp.getVoltPowerMapId() + " Avg Volts :"+avgVolts);
						avgPower = bvp.getPower().doubleValue();
					}
					else{
						avgPower =  getAvgPowerFromVolts(avgVolts, ballast.getLampNum(),ballast) * fixture.getNoOfFixtures(); // watts
						logger.debug("Executing Soft meternig Workflow for Fixture ("+fixture.getId()+")"+fixture.getSnapAddress() +" Refering Default Ballast Curve for Ballast Id:"+ ballast.getId() +" Avg Power:" + avgPower +" Avg Volts :"+avgVolts);
					}
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

		Double price = pricingManager.getPrice(fixture, statsDate);
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
				logger.debug(fixture.getId() + ": Fixture might be configured with wrong ballast(" + avgPower + ")");
				//raise an event
//		  eventsAndFaultManager.addEvent(fixture, "Fixture might be configured with wrong ballast("
//			    + avgPower + ")", EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR,
//			    EventsAndFault.INFO_SEV_STR);
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
				//raise an alarm
				logger.debug(fixture.getId() + ": Fixture might be configured with wrong ballast(" + avgPower + ")");
				eventsAndFaultManager.addAlarm(fixture, "Fixture might be configured with wrong ballast("+ avgPower + ")", EventsAndFault.FIXTURE_ERRONEOUS_ENERGY_STR);
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
				logger.debug("Saving Attribution : diffInBaseLine " + diffInBaseLine + ", Saved Power : " + savedPower);
				
			}
			// ENL-4321 - Wrong savings attributions -  Task tune saving was more than the baseline load issue
			//if(savedPower>diffInBaseLine)
			//{
				savedPower = savedPower - diffInBaseLine;
				logger.debug("Saving Attribution : Saved Power After deducting difference in baseline " + savedPower);
			//}
			if(savedPower < 0)
			{
				diffInBaseLine = diffInBaseLine + savedPower;
				logger.debug("Saving Attribution : Saved Power is < 0 hence adding diffInBaseLine into savedpower to attribute in tasktune" + savedPower);
			}
			// ENDS ENL-4321
		}
		
		if(savedPower > 0) {
		  switch (savingType) {
		  case ServerConstants.STATS_SAVING_OCC:			
		  case ServerConstants.STATS_SAVING_AMB:
		  case ServerConstants.STATS_SAVING_TUNEUP:
				adjustTaskTunedSavings(fixture, ec, savedPower, avgVolts, savingType, lastVoltsReported, motionBitsLong, currState,
						motionOffToOn);
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
		Date fixtLastStatsRcvdTime = device.getLastStatsRcvdTime();
		try {
			fixture.setLastStatsRcvdTime(statsDate);
			fixture.setCurrApp((short) currApp);
			fixture.setCurrentState(ServerUtil.getCurrentState(currState));
			fixture.setWattage((int)Math.round(avgPower));
			fixture.setAvgTemperature(fxTemp);
			if(!sweepEnabled) {
			  fixture.setBaselinePower(new BigDecimal(basePowerUsed));
			}
			if (ServerUtil.compareVersion(fixture.getVersion(), ServerMain
					.getInstance().getGemsVersion()) == 0) {
				// This will ensure that after the upgrade of SU image the
				// version synced bit is cleared.
				fixture.setVersionSynced(0);
				//fixtureManager.updateVersionSynced(fixture.getId(), 0);
			}
			fixture.setLastConnectivityAt(new Date());
			fixtureManager.updateStats(fixture);
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
			  eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
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
		    PerfSO.getInstance().addZeroBucket(device, fixture, statsDateTime);
		  }
		  if(!calculatePower) {
		    PerfSO.getInstance().addFixtureToZeroBucketUpdQueue(fixture.getId(),
		      fixtLastStatsRcvdTime, statsDate, sweepEnabled);
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
		double fPowerFactor = BallastCache.getInstance().getBallastVoltPowerFactor(ballast, 
		    volts);
		double ballast_factor = ballast.getBallastFactor();
		double calcPower = ballast_factor * bulbWattage * noOfLamps / 100;
		if (fPowerFactor > 0) {
			calcPower = calcPower * fPowerFactor;
		}
		return calcPower;

	} // end of method getAvgPowerFromVolts
	
	private void adjustTaskTunedSavings(Fixture fixture, EnergyConsumption ec, double savedPower, 
	    short avgVolts, int savingType, int lastVolts, long motionBits, int currState, int motionOffToOn) {

	  try {	
	    Calendar currentDate = Calendar.getInstance();
	    int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60 + currentDate.get(Calendar.MINUTE);
	    int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
	    if (dayOfWeek == 0) {
	      dayOfWeek = 7;
	    }
	    int maxOnLevel = profileManager.getOnLevel(fixture.getId(), dayOfWeek, minOfDay);
	    //int minOnLevel = profileManager.getMinLevel(fixture.getId(), dayOfWeek, minOfDay);   
	    if(fixture.getLastOccupancySeen() < 300) {
    		//if the motion is observed in the last 5 mins
    		fixture.setLightingOccStatus((short)1);
    	} else {
    		//depend on the occupancy state sent in the motion off to on field.
    		if((motionOffToOn & 0x1000) == 0x1000) {
    			//it is vacant
    			fixture.setLightingOccStatus((short)0);
    		} else if((motionOffToOn & 0x2000) == 0x2000) {
    			//it is occupied
    			fixture.setLightingOccStatus((short)1);
    		}
    	}
	    /*
	    if(currState == ServerConstants.CURR_STATE_MANUAL) {
	    	//in case of manual state, lighting based occupancy is always 1 except for the following condition
	    	if(lastVolts == 100 && fixture.getLastOccupancySeen() > 5) {
	    		// if the last volts is 100, it might be because of emergency set, so check if the last occupancy bit is 0
	    		fixture.setLightingOccStatus((short)0);
	    	} else {
	    		fixture.setLightingOccStatus((short)1);
	    	}
	    } else if(currState == ServerConstants.CURR_STATE_OCC_OFF) {
	    	fixture.setLightingOccStatus((short)0);
	    }
	    */
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
		// if the current power is less than 7 watts, raise an alarm that
		// fixture is out
		// in GE ballast which is parallel, we observed 6.9 watts when all bulbs
		// are out
		if (avgPower < PerfSO.getFixtureOutageDetectWatts()) {
            eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
			logger.debug(fixture.getFixtureName()
					+ ": fixture is out. raising outage alarm");
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
    	
    	SystemConfiguration bulbConfigurationEnableConfig = systemConfigurationManager.loadConfigByName("bulbconfiguration.enable");
        if (bulbConfigurationEnableConfig != null && "false".equalsIgnoreCase(bulbConfigurationEnableConfig.getValue())) {
        	return;
        }
    	
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
            Ballast ballast = fixtureManager.getBallastById(fixture.getBallast().getId());
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
                    logger.debug(fixture.getId() + ": configured with ballast " + fixture.getBallast().getId()
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
                        eventsAndFaultManager.addUpdateAlarm(fixture, "Lamp(s) out [Light level=" + avgVolts
                                + "%, power usage=" + nf.format(avgPower) + "W, expected power usage=" + nf.format(fRefPowerUsed) + "W, count="
                                + device.getLampOutageCount(avgVolts) + "]",
                                EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
                    }
                } else {
                    logger.info(fixture.getId() + ": Clearing Lamp(s) out");
                    device.resetLampOutageCount(avgVolts);
                    eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
                }
            }
        } catch (NullPointerException npe) {
            logger.debug(npe.getMessage());
        } catch (Exception e) {
            logger.debug(e.getMessage());
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
            List<BallastVoltPower> voltPowerList = fixtureManager.getBallastVoltPowerCurve(ballast.getId().longValue(),
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
