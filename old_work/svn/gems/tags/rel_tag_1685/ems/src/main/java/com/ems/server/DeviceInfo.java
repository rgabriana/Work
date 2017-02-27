/**
 * 
 */
package com.ems.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.ems.action.SpringContext;
import com.ems.model.Fixture;
import com.ems.model.OutageBasePower;
//import com.ems.server.device.GEMSGatewayImpl;
import com.ems.service.FixtureManager;
/**
 * @author EMS
 *
 */
public class DeviceInfo {

  static int COMMAND_RETRY_MILLIS = 750;
  static int NO_OF_RETRIES = 2;
  
  //private Fixture fixture = null;
  private String fixtureName = null;
  private Long dbId = null; //id in the fixture table
  private Date lastStateRcvdTime = null;
  private Date lastZeroBucketTime = null;
  private long lastStatsSeqNo = -1;
  private Date bootTime = null;
  private long uptime = -1;
  private double basePower = 0;
  //private HashMap<Integer, CommandPacket> cmdMap = new HashMap<Integer, CommandPacket>();
  private HashMap<Long, SUProfilePacket> pfhMap = new HashMap<Long, SUProfilePacket>();
  private FixtureManager fixtureMgr = null;
  private long lastGwId = 1;
  private long energyCum = 0;
  private long lastZeroBucketId = -1;
  private HashMap<Short, Double> outageBaseMap = new HashMap<Short, Double>();
      
  private int commType = ServerConstants.COMM_TYPE_ZIGBEE;
  
  private Date lastProfileSyncTime = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
  private boolean lastDateSyncPending = false;
  private int lastDateSyncSeqNo = 1;
  
  private Fixture fixture = null;
  
  private boolean valid = true;
  
  public boolean isValid() {
    
    return valid;
    
  } //end of method isValid
  
  public void setValid(boolean bool) {
    
    valid = bool;
    
  } //end of method setValid
      
  public Fixture getFixture() {
    
    return fixture;
    
  } //end of method getFixture
  
  public void setFixture(Fixture fix) {
    
    fixture = fix;
    
  } //end of method setFixture
  
  /**
   * 
   */
  public DeviceInfo() {
    // TODO Auto-generated constructor stub
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
    this.fixture = fixture;
    this.fixtureName = fixture.getFixtureName();
    this.dbId = fixture.getId();
    this.commType = fixture.getCommType();
    this.lastStateRcvdTime = fixture.getLastStatsRcvdTime();
  }

  public String getFixtureName() {
    return fixtureName;
  }

  public void setFixtureName(String name) {
    this.fixtureName = name;
  }

  public Long getDbId() {
    return dbId;
  }

  public void setDbId(Long dbId) {
    this.dbId = dbId;
  }
  
  public Date getLastStateRcvdTime() {
    return lastStateRcvdTime;
  }

  public void setLastStateRcvdTime(Date lastStateRcvdTime) {
    this.lastStateRcvdTime = lastStateRcvdTime;
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
  
//  public int getNoOfPendingCmds() {
//    //System.out.println("no. of pending jobs == " + cmdMap.size());
//    return cmdMap.size();
//  }
    
//  public void clearAllPendingJobs() {
//    
//    synchronized(cmdMap) {
//      cmdMap.clear();
//    }
//    
//  } //end of method clearAllPendingJobs
  
//  public void retryCommands() {
//    
//    final ArrayList<CommandPacket> cmdList = new ArrayList<CommandPacket>();
//    synchronized(cmdMap) {
//      Iterator<CommandPacket> iter = cmdMap.values().iterator();
//      CommandPacket cmdPkt = null;
//      while(iter.hasNext()) {      
//        try {
//          cmdPkt = iter.next();
//          // command got ack or no retries left
//          if (cmdPkt.markRemoved || cmdPkt.retries == 0) {
//            if(cmdPkt.markRemoved) {
//              fixtureLogger.debug(fixtureName + ": command successful retries = " + 
//        	  (getNoOfRetries() - cmdPkt.retries));
//            } else {
//                byte[] seqNoArr = new byte[4];    
//                System.arraycopy(cmdPkt.pkt, ServerConstants.CMD_PKT_TX_ID_POS, seqNoArr, 0, seqNoArr.length);
//                Integer seqNo = ServerUtil.byteArrayToInt(seqNoArr);
//                DeviceServiceImpl.getInstance().updateAuditRecord(seqNo, getNoOfRetries(), 1); // failed
//              fixtureLogger.error(fixtureName + ": command possibly timedout");
//            }
//            iter.remove();
//            continue;
//          }	
//          if ((System.currentTimeMillis() - cmdPkt.cmdTime) > COMMAND_RETRY_MILLIS) {            
//            cmdList.add(cmdPkt);
//            cmdPkt.retries--; 	  	    
//          }	
//        }
//        catch(Exception e) {
//  	e.printStackTrace();
//        }
//      }    
//    }
//    Iterator<CommandPacket> cmdIter = cmdList.iterator();
//    CommandPacket cmdPkt = null;
//    while (cmdIter.hasNext()) {
//      cmdPkt = cmdIter.next();
//      try {
//	fixtureLogger.info(fixtureName + ": retry command " + ServerUtil.getLogPacket(cmdPkt.pkt));
//	if (commType == ServerConstants.COMM_TYPE_ZIGBEE) {
//	  ZigbeeDeviceImpl.getInstance().sendNodeDataToGateway(dbId.longValue(),
//	      cmdPkt.pkt);
//	} else {
//	  GEMSGatewayImpl.getInstance().sendDatatoNode(dbId.longValue(),
//	      cmdPkt.pkt);
//	}
//	cmdPkt.cmdTime = System.currentTimeMillis();
//      }
//      catch(Exception ex) {
//	ex.printStackTrace();
//      }
//      ServerUtil.sleepMilli(DeviceServiceImpl.UNICAST_PKTS_DELAY * 3 / 2);
//    }
//    
//  } //end of method retryCommands
  
//  public void addCmd(byte[] packet, int retries) {
//    
//    long currTime = System.currentTimeMillis();
//    CommandPacket cmdPkt = new CommandPacket(currTime, packet, retries);
//    
//    if(fixtureMgr == null) {
//      fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
//    }
//    String version = fixtureMgr.getFixtureById(dbId).getVersion();
//    if(ServerUtil.compareVersion(version, "1.2") < 0) { //version is less than 1.2
//      //index 3 is the msg type
//      synchronized(cmdMap) {
//	cmdMap.put(new Integer(packet[3]), cmdPkt);
//      }
//      return;
//    }    
//    byte[] seqNoArr = new byte[4];    
//    System.arraycopy(packet, ServerConstants.CMD_PKT_TX_ID_POS, seqNoArr, 0, seqNoArr.length);
//    Integer seqNo = ServerUtil.byteArrayToInt(seqNoArr);
//    synchronized(cmdMap) {
//      cmdMap.put(seqNo, cmdPkt);
//    }
//
//  } //end of method addCmd
  
//  public void addCmd(byte[] packet) {
//    
//    long currTime = System.currentTimeMillis();
//    CommandPacket cmdPkt = new CommandPacket(currTime, packet);
//    
//    if(fixtureMgr == null) {
//      fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
//    }
//    String version = fixtureMgr.getFixtureById(dbId).getVersion();
//    if(ServerUtil.compareVersion(version, "1.2") < 0) { //version is less than 1.2
//      //index 3 is the msg type
//      synchronized(cmdMap) {
//	cmdMap.put(new Integer(packet[3]), cmdPkt);
//      }
//      return;
//    }
//    byte[] seqNoArr = new byte[4];    
//    System.arraycopy(packet, ServerConstants.CMD_PKT_TX_ID_POS, seqNoArr, 0, seqNoArr.length);
//    Integer seqNo = ServerUtil.byteArrayToInt(seqNoArr);
//    synchronized(cmdMap) {
//      cmdMap.put(seqNo, cmdPkt);
//    }
//
//  } //end of method addCmd
  
//  public void ackCmd(byte[] ackPkt) {
//    
//    byte startMarker = ackPkt[0];    
//    Integer key = -1;
//    int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
//    
//    if(startMarker == ServerConstants.FRAME_START_MARKER) {
//      //index 4 in packet is msg type for which this is the ack packet
//      key = new Integer(ackPkt[3]);      
//      msgStartPos = 3;
//    } else {    
//      byte[] seqNoArr = new byte[4];
//      System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
//      key = ServerUtil.byteArrayToInt(seqNoArr);      
//    }    
//    //fixtureLogger.debug(fixtureName + ": got ack with seq no - " + key);
//    
//    synchronized(cmdMap) {
//      if (cmdMap.containsKey(key)) {
//	CommandPacket  cp = cmdMap.get(key);
//    DeviceServiceImpl.getInstance().updateAuditRecord(key, cp.retries, 0); // success
//	cp.markRemoved = true;      
//      }
//    }
//
//    int msgType = (ackPkt[msgStartPos] & 0xFF);
//    switch (msgType) {
//	    case ServerConstants.SET_PROFILE_MSG_TYPE :
//	        DeviceServiceImpl.getInstance().updateAuditRecord(key, 1, 0); // success
//	    	fixtureMgr.resetPushProfileForFixture(dbId);
//		break;
//	    case ServerConstants.SET_PROFILE_ADV_MSG_TYPE:
//	        DeviceServiceImpl.getInstance().updateAuditRecord(key, 1, 0); // success
//	    	fixtureMgr.resetPushGlobalProfileForFixture(dbId);
//		break;
//    }
//    //fixtureMgr.updateLastConnectionTime(fixture.getId(), lastGwId);
//      
//  } //end of method ackCmd
  
  //class to hold the actual command packet sent along with the time.
//  public class CommandPacket {
//    
//    long cmdTime;
//    byte[] pkt;
//    int retries = NO_OF_RETRIES;
//    boolean markRemoved = false;
//    
//    public CommandPacket(long cmdTime, byte[] pkt, int retries) {
//    
//      this.cmdTime = cmdTime;
//      this.pkt = pkt;
//      this.retries = retries;
//      
//    } //end of constructor CommandPacket
//    
//    public CommandPacket(long cmdTime, byte[] pkt) {
//      
//      this.cmdTime = cmdTime;
//      this.pkt = pkt;      
//      
//    } //end of constructor CommandPacket
//        
//  } //end of class CommandPacket
  
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
      if(fixtureMgr == null) {
	fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");
      }
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
    outageBasePower.setVoltLevel(volts);
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
  
} //end of class DeviceInfo