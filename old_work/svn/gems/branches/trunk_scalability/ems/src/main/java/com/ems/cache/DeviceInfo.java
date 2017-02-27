/**
 * 
 */
package com.ems.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.ems.action.SpringContext;
import com.ems.model.Fixture;
import com.ems.model.OutageBasePower;
import com.ems.server.ServerConstants;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
/**
 * @author EMS
 *
 */
public class DeviceInfo {

  public static int COMMAND_RETRY_MILLIS = 750;
  public static int NO_OF_RETRIES = 2;
  
  //private Fixture fixture = null;
  private String fixtureName = null;
  private Long dbId = null; //id in the fixture table
  private Date lastStatsRcvdTime = null;
  private Date lastZeroBucketTime = null;
  private long lastStatsSeqNo = -1;
  private long lastMBitSeqNo = -1;
  private Date bootTime = null;
  private long uptime = -1;
  private double basePower = 0;
  //private HashMap<Integer, CommandPacket> cmdMap = new HashMap<Integer, CommandPacket>();
  private HashMap<Long, SUProfilePacket> pfhMap = new HashMap<Long, SUProfilePacket>();
  private FixtureManager fixtureMgr = null;
  private EnergyConsumptionManager ecManager = null;
  private long lastGwId = 1;
  private long energyCum = 0;
  private long lastZeroBucketId = -1;
  private HashMap<Short, Double> outageBaseMap = new HashMap<Short, Double>();
  
  private Date lastProfileSyncTime = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
  private boolean lastDateSyncPending = false;
  private int lastDateSyncSeqNo = 1;
  
  private Long fixtureId = null;
  
  private boolean valid = true;
  private boolean firstStatsAfterCommission = false;
  private String deviceState = ServerConstants.FIXTURE_STATE_COMMISSIONED_STR;
  private int lampOutageCount = 0;

  private short lastVoltsForOutageReading = 0;
  
  //profile push flag has to be reset only after receiving acks for all the 3 profile packets
  private ArrayList<Long> profileSeqList = new ArrayList<Long>();
  
  // 0 - FX Curve not present, 1 - FX Curve Present
  private int lampCalibrationStatus=0;
  
  private boolean fixtureOutageEvent = true;
  private boolean fixtureBulbOutageEvent = true;

  private boolean pmStatUpdateReq = true; 
    
  public void addProfileSeqNo(long txId) {
        
    profileSeqList.add(txId);
    
  } //end of method setWeekDaySeqNo
  
  public void clearPushProfileFlag() {
        
    profileSeqList.clear();
    
  } //end of method clearPushProfileFlag
   
  public void resetPushProfileForFixture(long seqNo) {
    
    profileSeqList.remove(seqNo);    
    if(profileSeqList.size() == 0) {  
      fixtureMgr.resetPushProfileForFixture(fixtureMgr.getFixtureById(fixtureId));
    }
    
  } //end of method resetPushProfileForFixture
  
  public String getDeviceState() {
  	
  	return deviceState;
  	
  } //end of method getDeviceState
  
  public void setDeviceState(String state) {
  	
  	deviceState = state;
  	
  } //end of method setDeviceState
  
  public boolean isFirstStatsAfterCommission() {
    
    return firstStatsAfterCommission;
    
  } //end of method isFirstStatsAfterCommission
  
  public void setFirstStatsAfterCommission(boolean bool) {
    
    firstStatsAfterCommission = bool;
    
  } //end of method setFirstStatsAfterCommission
  
  public boolean isValid() {
    
    return valid;
    
  } //end of method isValid
  
  public void setValid(boolean bool) {
    
    valid = bool;
    
  } //end of method setValid
      
  public Fixture getFixture() {
    return fixtureMgr.getFixtureById(fixtureId);
  } //end of method getFixture
  
  public void setFixtureId(Long id) {
    fixtureId = id;
  }
  
  /**
   * 
   */
  public DeviceInfo() {
	  fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
	  ecManager = (EnergyConsumptionManager) SpringContext.getBean("energyConsumptionManager");
  }
  
  public double getBasePower() {
    return basePower;
  }
  
  public void setBasePower(double power) {
    basePower = power;
  }
  public static int getCommandRetryDelay() {
    return COMMAND_RETRY_MILLIS;
  }
  public static void setCommandRetryDelay(int millis) {
    COMMAND_RETRY_MILLIS = millis;
  }
  public static int getNoOfRetries() {
    return NO_OF_RETRIES;
  }
  public static void setNoOfRetries(int retries) {
    NO_OF_RETRIES = retries;
  }
  public DeviceInfo(Fixture fixture) {
	fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
	ecManager = (EnergyConsumptionManager) SpringContext.getBean("energyConsumptionManager");
    this.fixtureId = fixture.getId();
    this.fixtureName = fixture.getFixtureName();
    this.dbId = fixture.getId();
    this.deviceState = fixture.getState();
    this.lastStatsRcvdTime = ecManager.getLastECCaptureAtByFixtureId(fixtureId);
    if(this.lastStatsRcvdTime == null) {
      this.lastStatsRcvdTime = new Date();
    }
    
  }

  public String getFixtureName() {
    return fixtureName;
  }

  public Long getDbId() {
    return dbId;
  }

  public void setDbId(Long dbId) {
    this.dbId = dbId;
  }
  
  public Date getLastStatsRcvdTime() {
    return lastStatsRcvdTime;
  }

  public void setLastStatsRcvdTime(Date lastStatsRcvdTime) {
    this.lastStatsRcvdTime = lastStatsRcvdTime;
  }
  
  public Date getLastProfileSyncTime() {
    return lastProfileSyncTime;
  }

  public void setLastProfileSyncTime(Date lastProfileSyncTime) {
    this.lastProfileSyncTime = lastProfileSyncTime;
  }
  
  public boolean isLastDateSyncPending() {
    return lastDateSyncPending;
  }

  public void setLastDateSyncPending(boolean lastDateSyncPending) {
    this.lastDateSyncPending = lastDateSyncPending;
  }
  
  public Date getLastZeroBucketTime() {
    return lastZeroBucketTime;
  }

  public void setLastZeroBucketTime(Date lastZeroBucketTime) {
    this.lastZeroBucketTime = lastZeroBucketTime;
  }
  
  public long getLastStatsSeqNo() {
    return lastStatsSeqNo;
  }

  public void setLastStatsSeqNo(long lastStatsSeqNo) {
    this.lastStatsSeqNo = lastStatsSeqNo;
  }
  
  /**
 * @return the lastMBitSeqNo
 */
public long getLastMBitSeqNo() {
    return lastMBitSeqNo;
}

/**
 * @param lastMBitSeqNo the lastMBitSeqNo to set
 */
public void setLastMBitSeqNo(long lastMBitSeqNo) {
    this.lastMBitSeqNo = lastMBitSeqNo;
}

public long getLastZeroBucketId() {
    return lastZeroBucketId;
  }

  public void setLastZeroBucketId(long lastZeroBucketId) {
    this.lastZeroBucketId = lastZeroBucketId;
  }
  
  public Date getBootTime() {
    return bootTime;
  }

  public void setBootTime(Date bootTime) {
    this.bootTime = bootTime;
  }
  
  public int getLastDateSyncSeqNo() {
    return lastDateSyncSeqNo;
  }

  public void setLastDateSyncSeqNo(int lastDateSyncSeqNo) {
    this.lastDateSyncSeqNo = lastDateSyncSeqNo;
  }
  
  public void setLastGwId(long gwId) {
    
    this.lastGwId = gwId;
    
  } //end of method setLastGwId
  
  public long getLastGwId() {
    
    return lastGwId;
    
  } //end of method getLastGwId
  
  public void setUptime(long uptime) {
    this.uptime = uptime;
  }
  
  public long getUptime() {
    return uptime;
  }
  
  public void setEnergyCum(long energyCum) {
    this.energyCum = energyCum;
  }
  
  public long getEnergyCum() {
    return energyCum;
  }
  
  /**
   * Holds the profile packets received from SU
   * @author yogesh
   *
   */
  public class SUProfilePacket {
	    
	    long cmdTime;
	    byte[] pkt;
	    boolean markRemoved = false;
	    
	    public SUProfilePacket() {
	    	markRemoved = false;
	    }
	    
	    public void addPfPacket(long cmdTime, byte[] pkt) {
	      this.cmdTime = cmdTime;
	      this.pkt = pkt;
	      markRemoved = true;
	    } //end of constructor SUProfilePacket
  } //end of class CommandPacket
  
  public synchronized boolean isPFPktMarked(long fixtureId) {
	    SUProfilePacket oSUPfPkt = pfhMap.get(fixtureId);
	    if (oSUPfPkt != null) {
	    	return oSUPfPkt.markRemoved;
	    }
	    return false;
  }
  
  public synchronized byte[] getProfilePkt(long fixtureId) {
	    SUProfilePacket oSUPfPkt = pfhMap.get(fixtureId);
	    if (oSUPfPkt != null) {
	    	return oSUPfPkt.pkt;
	    }
	    return null;
  }

  public synchronized void removeProfilePkt(long fixtureId) {
	    pfhMap.remove(fixtureId);
  }

  public synchronized void ackPfH(long fixtureId, byte[] ackPkt) {
	    SUProfilePacket oSUPfPkt = pfhMap.get(fixtureId);
	    if (oSUPfPkt == null) {
	    	oSUPfPkt = new SUProfilePacket();
	    } 
    	oSUPfPkt.addPfPacket(System.currentTimeMillis(), ackPkt);
    	pfhMap.put(fixtureId, oSUPfPkt);
  } //end of method ackPfH
  
  public Double getOutageBasePower(short volts) {
      
    if(outageBaseMap.isEmpty()) {
      //outage base map is not loaded from database
      List<OutageBasePower> basePowerList = fixtureMgr.getFixtureOutageBasePowerList(dbId);
      if(basePowerList != null) {
	Iterator<OutageBasePower> iter = basePowerList.iterator();
	OutageBasePower outageBasePower = null;
	while(iter.hasNext()) {
	  outageBasePower = iter.next();
	  outageBaseMap.put(outageBasePower.getVoltLevel(), outageBasePower.getBasePower());
	}
      }
    }
    if(outageBaseMap.containsKey(volts)) {
      return outageBaseMap.get(volts);
    }
    return null;
    
  } //end of method getOutageBasePower
  
  public void addOutageBasePower(short volts, double power) {
    
    outageBaseMap.put(volts, power);
    OutageBasePower outageBasePower = new OutageBasePower();
    outageBasePower.setBasePower(power);
    outageBasePower.setFixtureId(dbId);
    fixtureMgr.saveOutageBasePower(outageBasePower);
    
  } //end of method addOutageBasePower
  
  public void updateOutageBasePower(short volts, double power) {
	      
    OutageBasePower outageBasePower = fixtureMgr.getOutageBasePower(dbId, volts);
    if(outageBasePower == null) {
      addOutageBasePower(volts, power);
      return;
    }
    outageBaseMap.put(volts, power);    
    outageBasePower.setBasePower(power);    
    fixtureMgr.saveOutageBasePower(outageBasePower);
    
  } //end of method addOutageBasePower

/**
 * @return the lampOutageCount
 */
public int getLampOutageCount(short avgVolts) {
    if (this.lastVoltsForOutageReading == avgVolts)
        return lampOutageCount;
    return -1;
}

/**
 * @param lampOutageCount the lampOutageCount to set
 */
public void incLampOutageCount(short avgVolts) {
    if (this.lastVoltsForOutageReading == 0) {
        this.lastVoltsForOutageReading = avgVolts;
    }
    if (this.lastVoltsForOutageReading == avgVolts) {
        this.lampOutageCount++;
    }else {
        resetLampOutageCount(avgVolts);
    }
}
public void resetLampOutageCount(short avgVolts) {
    this.lastVoltsForOutageReading = avgVolts;
    this.lampOutageCount = 0;
}

public short getLastVoltForOutageReading() {
    return this.lastVoltsForOutageReading;
}

/**
 * @return the lampCalibrationStatus
 */
public int getLampCalibrationStatus() {
	return lampCalibrationStatus;
}

/**
 * @param lampCalibrationStatus the lampCalibrationStatus to set
 */
public void setLampCalibrationStatus(int lampCalibrationStatus) {
	this.lampCalibrationStatus = lampCalibrationStatus;
}

  public String dumpDeviceInfo() {
  	Fixture fixture = getFixture();
  	StringBuffer sb = new StringBuffer();  	
  	sb.append("name=");
  	sb.append(fixtureName);
  	sb.append(";id=");
  	sb.append(fixtureId);
  	sb.append(";valid=");
  	sb.append(valid);
  	if(fixture != null) {
	  	sb.append(";secGwId=");
	  	sb.append(fixture.getSecGwId());
	  	sb.append(";state=");
	  	sb.append(fixture.getState());
  	}
  	return sb.toString();
  	
  } //end of method dumpDeviceInfo

/**
 * @return the fixtureOutageEvent
 */
public boolean isFixtureOutageEvent() {
	return fixtureOutageEvent;
}

/**
 * @param fixtureOutageEvent the fixtureOutageEvent to set
 */
public void setFixtureOutageEvent(boolean fixtureOutageEvent) {
	this.fixtureOutageEvent = fixtureOutageEvent;
}

/**
 * @return the fixtureBulbOutageEvent
 */
public boolean isFixtureBulbOutageEvent() {
	return fixtureBulbOutageEvent;
}

/**
 * @param fixtureBulbOutageEvent the fixtureBulbOutageEvent to set
 */
public void setFixtureBulbOutageEvent(boolean fixtureBulbOutageEvent) {
	this.fixtureBulbOutageEvent = fixtureBulbOutageEvent;
}

/**
 * @return the pmStatUpdateReq
 */
public boolean isPmStatUpdateReq() {
	return pmStatUpdateReq;
}

/**
 * @param pmStatUpdateReq the pmStatUpdateReq to set
 */
public void setPmStatUpdateReq(boolean pmStatUpdateReq) {
	this.pmStatUpdateReq = pmStatUpdateReq;
}

  
} //end of class DeviceInfo
