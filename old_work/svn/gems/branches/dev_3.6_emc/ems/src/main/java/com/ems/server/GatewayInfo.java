/**
 * 
 */
package com.ems.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayComm;
import com.ems.server.device.GatewayImpl;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;

/**
 * @author sreedhar
 *
 */
public class GatewayInfo {

	public static int GW_DISCOVERY_MODE = 1;
	public static int GW_COMMISSIONING_MODE = 2;
	public static int GW_NORMAL_MODE = 3;
	public static int GW_IMG_UPGRADE_MODE = 4;
	public static int GW_DELETE_MODE = 5;
	
  private Gateway gw = null;
  private Date lastConnectivityAt = null;
  //private Date lastStatsRcvdAt = null;
  private boolean status = false;
  
  private long uptime;
  private long pktsFromGems;
  private long pktsToGems;
  private long pktsFromNodes;
  private long pktsToNodes;
  
  private int operationalMode = GW_NORMAL_MODE;
  
  private int FIVE_MIN = 5 * 60 * 1000; // 5 minutes
  private int ONE_HOUR = FIVE_MIN * 12; // 60 minutes
  
  private int setUtcTimeCmdOffset = 240 * 1000; // in a 5 minute period.
  private int setUtcTimeCmdFrequency = FIVE_MIN;
  
  private int utcTimeFreqCloudGW = ONE_HOUR; //60 minutes
  private int astroTimeFreq = 24 * ONE_HOUR; //1 day
  
  private boolean restartUTCThreadReqd = false;

  private HashMap<Integer, CommandPacket> cmdMap = new HashMap<Integer, CommandPacket>();
  private RetryThread retryThread = new RetryThread();
  
  private int noOfPendingDevices = 0;
  
  private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");
  private static final Logger perfLogger = Logger.getLogger("Perf");
  
  private int gwHealthPollInterval = 30 * 1000; //30 seconds
  private GwHealthPollThread healthPoller = new GwHealthPollThread();
  private TimeSyncThread timeSyncThread = new TimeSyncThread();
  private Date lastUtcTimeSent = new Date();
  
  private Date lastStatsTime = new Date(System.currentTimeMillis() - 5 * 30 * 1000);
  
  private static EventsAndFaultManager eventMgr = null;
  
  private boolean emcMode = false;
  
  private long shortPMStatsEndTime = 0L;
  
  public void setUtcTimeCmdOffset(int offset) {
  	
  	if(offset != setUtcTimeCmdOffset) {
  		restartUTCThreadReqd = true;
  	}
  	setUtcTimeCmdOffset = offset;
  	
  } //end of method setUtcTimeCmdOffset
  
  public void setShortPMStatsEndTime(long time) {
  	
  	shortPMStatsEndTime = time;
  	
  }
  
  public long getShortPMStatsEndTime() {
  	
  	return shortPMStatsEndTime;
  	
  }
  
  public void setUtcCmdFrequency(int freq) {
  	
  	if(freq > PerfSO.FIVE_MINUTE_INTERVAL) {
  		perfLogger.error(gw.getId() + ": not taking the frequency as the frequency is more than 5 minutes");
  		return;
  	}
  	if(freq != setUtcTimeCmdFrequency) {
  		restartUTCThreadReqd = true;
  	}
  	setUtcTimeCmdFrequency = freq;
  	
  } //end of method setUtcCmdFrequency
  
  public Date getLastStatsTime() {
    
    return lastStatsTime;
    
  } //end of method getLastStatsTime
  
  public void setLastStatsTime(Date lastStatsTime) {
    
    this.lastStatsTime = lastStatsTime;
    
  } //end of method setLastStatsTime
  
  class TimeSyncThread extends Thread {
    
  	private boolean running = true;
  	
  	public void stopThread() {
  		running = false;
  	}
  	
    public void run() {
      
    	if(setUtcTimeCmdOffset > PerfSO.FIVE_MINUTE_INTERVAL) {
    		perfLogger.error("utc cmd offset is more than 5 minutes. Timer is not started");
    		return;
    	}
      while(true) {
      	try {	     		
      		if(ServerUtil.getCurrentMin() % 5 == 0) { //start at the 5th/10th/15th... minute
      			Thread.sleep(setUtcTimeCmdOffset - ServerUtil.getCurrentSecond() * 1000);
      			if(perfLogger.isDebugEnabled()) {
      				perfLogger.debug(gw.getId() + ": starting the time sync task");      				
      			}
      			break;
      		}
      		Thread.sleep(1000); //sleep for 1 sec
      	}
      	catch(Exception ex) {
      		ex.printStackTrace();
      	}
      }	
      
      int waitTime = 0;
      int astroWaitTime = 0;
      boolean cloudTime = true;
      while(operationalMode != GW_DELETE_MODE) {
      	if(!running) {
      		//values have changed stop the current thread
      		break;
      	}
      	try {	  
      		lastUtcTimeSent = new Date();
      		DeviceServiceImpl.getInstance().sendUTCTime(gw, true, cloudTime);   
      		cloudTime = false;
      	}
      	catch(Exception ex) {
      		fixtureLogger.error(gw.getId() + ": " + ex.getMessage());
      	}
      	ServerUtil.sleepMilli(setUtcTimeCmdFrequency);
      	if(emcMode) {
      		waitTime += setUtcTimeCmdFrequency;
      		if(waitTime == utcTimeFreqCloudGW) {
      			cloudTime = true;
      			waitTime = 0;
      		}
      		astroWaitTime += setUtcTimeCmdFrequency;
      		if(astroWaitTime == astroTimeFreq) {
      			try {
      				//commenting out until gateway supports this
      				//DeviceServiceImpl.getInstance().sendThirtyDaysAstroClockMsg(gw, new Date());
      			}
      			catch(Exception e) {
      				fixtureLogger.error(gw.getMacAddress(), e);
      			}
      			astroWaitTime = 0;
      		}
      	}
      }      
            
    } //end of method run
    
  } //end of thread TimeSyncThread
  
  public Date getLastUtcTimeSent() {
  	
  	return lastUtcTimeSent;
  	
  } //end of method getLastUtcTimeSent
  
  public void setLastUtcTimeSent(Date date) {
  	
  	lastUtcTimeSent = date;
  	
  } //end of method setLastUtcTimeSent

  public void restartUTCThread() {
  	  	 
  	if(!restartUTCThreadReqd) { 
  		return;
  	}
  	
  	if(timeSyncThread.isAlive()) {
  		//there is another instance of thread running. it will restart if it is required
  		timeSyncThread.stopThread();
  	}
  	//values have changed. restart the thread
  	restartUTCThreadReqd = false;
  	timeSyncThread = new TimeSyncThread();
  	timeSyncThread.start();

  } //end of method restartUTCThread
  
  class GwHealthPollThread extends Thread {
    
    public void run() {
      
    	if(!emcMode) {
    		System.out.println("scheduling over ssl");
    		//check and initialize the SSL connection
    		try {
    			SSLSessionManager.getInstance().checkSSLConnection(gw.getId());
    		}
    		catch(Exception ex) {
    			ex.printStackTrace();
    		}
    	}
      while(true) {
      	try {	     
      		if(ServerUtil.getCurrentMin() % 5 == 0) { //start at the 5th/10th/15th... minute
      			if(perfLogger.isDebugEnabled()) {
      				perfLogger.debug(gw.getId() + ": starting the Gw Stats buckets task");
      			}
      			break;
      		}
      		Thread.sleep(1000); //sleep for 1 sec
      	}
      	catch(Exception ex) {
      		ex.printStackTrace();
      	}
      }	
      
      while(operationalMode != GW_DELETE_MODE) {
      	try {
      		//raise an alarm if the gateway was not communicating
      		long timeSinceLastConnectivity = System.currentTimeMillis() - lastConnectivityAt.getTime();
      		if((timeSinceLastConnectivity) > 15 * 60 * 60 * 1000) {
      			if(eventMgr == null) {
      				eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
      			}
      			eventMgr.addAlarm(gw, "gateway unreachable for (" + timeSinceLastConnectivity/1000/60 + " minutes)", EventsAndFault.GW_REACHABLILITY_FAILURE);
      		}
      		GatewayImpl.getInstance().sendGatewayInfoReq(gw);	  
      	}
      	catch(Exception ex) {
      		fixtureLogger.error(gw.getId() + ": " + ex.getMessage());
      	}
      	ServerUtil.sleepMilli(gwHealthPollInterval);
      }
      
      //will come here only when the gateway is deleted
      //remove the ssl session if it exists for this gateway
      if(!emcMode) {
      	SSLSessionManager.getInstance().removeSSLConnection(gw.getId());
      }
            
    } //end of method run
    
  } //end of thread GwHealthPollThread
    
  /**
   * 
   */
  public GatewayInfo(Gateway gw, boolean emc, int emcGwTimeFreq, int emcGwAstroFreq) {
    // TODO Auto-generated constructor stub
    this.gw = gw;
    retryThread.start();
    emcMode = emc;
    if(emcGwTimeFreq > 0) {
    	utcTimeFreqCloudGW = emcGwTimeFreq;
    }
    if(emcGwAstroFreq > 0) {
    	astroTimeFreq = emcGwAstroFreq;
    }
  }
  
  public void startTimers() {
  	
  	healthPoller.start();
    timeSyncThread.start();
    
  } //end of method startTimers

  public Gateway getGw() {
    return gw;
  }

  public void setGw(Gateway gw) {
    this.gw = gw;
  }

  public Date getLastConnectivityAt() {
    return lastConnectivityAt;
  }

  public void setLastConnectivityAt(Date lastConnectivityAt) {
    this.lastConnectivityAt = lastConnectivityAt;
  }
  
//  public Date getlastStatsRcvdAt() {
//    return lastStatsRcvdAt;
//  }
//
//  public void setlastStatsRcvdAt(Date lastStatsRcvdAt) {
//    this.lastStatsRcvdAt = lastStatsRcvdAt;
//  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }
  
  /**
   * @return the uptime
   */
  public long getUptime() {
    return uptime;
  }

  /**
   * @param uptime the uptime to set
   */
  public void setUptime(long uptime) {
    this.uptime = uptime;
  }

  /**
   * @return the pktsFromGems
   */
  public long getPktsFromGems() {
    return pktsFromGems;
  }

  /**
   * @param pktsFromGems the pktsFromGems to set
   */
  public void setPktsFromGems(long pktsFromGems) {
    this.pktsFromGems = pktsFromGems;
  }

  /**
   * @return the pktsToGems
   */
  public long getPktsToGems() {
    return pktsToGems;
  }

  /**
   * @param pktsToGems the pktsToGems to set
   */
  public void setPktsToGems(long pktsToGems) {
    this.pktsToGems = pktsToGems;
  }

  /**
   * @return the pktsFromNodes
   */
  public long getPktsFromNodes() {
    return pktsFromNodes;
  }

  /**
   * @param pktsFromNodes the pktsFromNodes to set
   */
  public void setPktsFromNodes(long pktsFromNodes) {
    this.pktsFromNodes = pktsFromNodes;
  }

  /**
   * @return the pktsToNodes
   */
  public long getPktsToNodes() {
    return pktsToNodes;
  }

  /**
   * @param pktsToNodes the pktsToNodes to set
   */
  public void setPktsToNodes(long pktsToNodes) {
    this.pktsToNodes = pktsToNodes;
  }
  
    
  /**
   * @return the operationalMode
   */
  public int getOperationalMode() {
  
  	return operationalMode;
  }

	
  /**
   * @param operationalMode the operationalMode to set
   */
  public void setOperationalMode(int operationalMode) {
  
  	this.operationalMode = operationalMode;
  }


	class RetryThread extends Thread {
    
    public void run() {
      
      fixtureLogger.debug(gw.getIpAddress() + ": starting the retry thread");
      while(true) {
	try {
	  retryCommands();	  
	}
      	catch(Exception e) {
      	  e.printStackTrace();
      	}
      	ServerUtil.sleepMilli(DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY * 3 / 2);
      }
      
    } //end of method run
    
  } //end of class RetryThread

  public synchronized void retryCommands() {
    
    Iterator<CommandPacket> iter = cmdMap.values().iterator();
    CommandPacket cmdPkt = null;
    while(iter.hasNext()) {      
      try {
	cmdPkt = iter.next();
	//command got ack or no retries left	
	if (cmdPkt.markRemoved || cmdPkt.retries == 0) {
          if(cmdPkt.markRemoved) {
            fixtureLogger.debug(gw.getIpAddress() + ": command successful retries = " + 
      	  	(DeviceInfo.getNoOfRetries() - cmdPkt.retries));
          } else {
        	  if (cmdPkt.pkt != null && cmdPkt.fixtureIdList != null) {
	        	  Fixture fixture = null;
	              byte[] seqNoArr = new byte[4];    
	              System.arraycopy(cmdPkt.pkt, ServerConstants.CMD_PKT_TX_ID_POS, seqNoArr, 0, seqNoArr.length);
	              Integer seqNo = ServerUtil.byteArrayToInt(seqNoArr);
	              int noOfFixtures = cmdPkt.fixtureIdList.size();
	    	      for(int i = 0; i < noOfFixtures; i++) {
	    	    	  fixture = cmdPkt.fixtureMap.get(cmdPkt.fixtureIdList.get(i));      
		              DeviceServiceImpl.getInstance().updateAuditRecord(seqNo, fixture.getId(), DeviceInfo.getNoOfRetries(), 1); // failed
	    	      }
        	  }
	          fixtureLogger.error(gw.getIpAddress() + ": command possibly timedout");
          }
          iter.remove();
          continue;
        }	
	byte[] packet = cmdPkt.pkt;
	//System.out.println("checking for retries");
	int retryTime = DeviceInfo.COMMAND_RETRY_MILLIS;         
        if((System.currentTimeMillis() - cmdPkt.cmdTime) > retryTime) {
          try {
            fixtureLogger.info(gw.getIpAddress() + "no. of pending devices -- " + noOfPendingDevices);
            if(cmdPkt.gwCommand) {
              fixtureLogger.info(gw.getIpAddress() + " retry command " + ServerUtil.getLogPacket(cmdPkt.pkt));	 
              GatewayComm.getInstance().sendDataToGateway(gw.getId(), cmdPkt.pkt);	    
            } else {
              byte[] origPkt = cmdPkt.pkt;
              int noOfTargets = origPkt[ServerConstants.CMD_PKT_NO_OF_TARGET_POS];	  
              int noOfFixtures = cmdPkt.fixtureIdList.size();
              if(noOfTargets > noOfFixtures) {	  
        	int newPktLen = origPkt.length - (3 * noOfTargets) + (noOfFixtures * 3);
        	packet = new byte[newPktLen];
        	byte[] snapAddr = new byte[noOfFixtures * 3];    
        	Fixture fixture = null;    
        	for(int i = 0; i < noOfFixtures; i++) {
        	  fixture = cmdPkt.fixtureMap.get(cmdPkt.fixtureIdList.get(i));      
        	  System.arraycopy(ServerUtil.getSnapAddr(fixture.getSnapAddress()), 0, snapAddr, 
        	      i * 3, 3);      
        	}
        	int pktPos = 0;	    
        	int noOfBytesToCopy = ServerConstants.CMD_PKT_NO_OF_TARGET_POS;
        	System.arraycopy(origPkt, 0, packet, pktPos, noOfBytesToCopy);
        	//replace the length in the new packet
        	byte[] newLenByteArr = ServerUtil.shortToByteArray(newPktLen);
        	//2, 3 are length bytes in the packet
        	packet[2] = newLenByteArr[0];
        	packet[3] = newLenByteArr[1];
        	pktPos = ServerConstants.CMD_PKT_NO_OF_TARGET_POS;
        	packet[pktPos++] = (byte)noOfFixtures;
        	System.arraycopy(snapAddr, 0, packet, pktPos, snapAddr.length);
        	pktPos += snapAddr.length;
        	int orgPktPos = ServerConstants.CMD_PKT_NO_OF_TARGET_POS + (noOfTargets * 3) + 1; 
        	noOfBytesToCopy = origPkt.length - orgPktPos;
        	System.arraycopy(origPkt, orgPktPos, packet, pktPos, noOfBytesToCopy);
              }
              fixtureLogger.info(gw.getIpAddress() + " multicast retry command: " + ServerUtil.getLogPacket(packet));
              GatewayComm.getInstance().sendNodeDataToGateway(gw.getId(), 
        	  gw.getIpAddress(), packet);	    
            }	  
          }
          catch(Exception ex) {
            fixtureLogger.error(gw.getId() + ": failed to send the command");
          }
          cmdPkt.cmdTime = System.currentTimeMillis();
	  cmdPkt.retries--;
	}
      }
      catch(Exception e) {
	e.printStackTrace();
      }      
      ServerUtil.sleepMilli(DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY * 3 / 2);
    }
    
  } //end of method retryCommands
  
  public int getNoOfPendingCmds() {
    //System.out.println("no. of pending jobs == " + cmdMap.size());
    return cmdMap.size();
  }

  //class to hold the actual command packet sent along with the time.
  public class CommandPacket {
    
    long cmdTime;
    byte[] pkt;    
    boolean markRemoved = false;
    int retries = DeviceInfo.NO_OF_RETRIES;
    HashMap<Long, Fixture> fixtureMap = null;
    ArrayList<Long> fixtureIdList = null;
    boolean gwCommand = false;
    
    public CommandPacket(long cmdTime, byte[] pkt) {
      
      this.cmdTime = cmdTime;
      this.pkt = pkt;
      gwCommand = true;
      
    } //end of constructor
    
//    public CommandPacket(long cmdTime, byte[] pkt, HashMap<Long, Fixture> fixMap,
//	ArrayList<Long> idList) {
//      
//      this.cmdTime = cmdTime;
//      this.pkt = pkt; 
//      fixtureMap = fixMap;
//      fixtureIdList = idList;      
//      
//    } //end of constructor CommandPacket
    
    public CommandPacket(long cmdTime, byte[] pkt, HashMap<Long, Fixture> fixMap,
	ArrayList<Long> idList, int noOfRetries) {
      
      this.cmdTime = cmdTime;
      this.pkt = pkt; 
      fixtureMap = fixMap;
      fixtureIdList = idList;
      retries = noOfRetries;
      
    } //end of constructor CommandPacket
    
    public void removeFixture(Fixture fixture) {
      fixtureIdList.remove(fixture.getId());    
      if(fixtureIdList.size() == 0) {
	markRemoved = true;
      }
    }
        
  } //end of class CommandPacket
  
  //this is the gateway command sent to the gateway
  public synchronized void addCmd(byte[] packet) {
    
    long currTime = System.currentTimeMillis();
    CommandPacket cmdPkt = new CommandPacket(currTime, packet);
    
    byte[] seqNoArr = new byte[4];    
    System.arraycopy(packet, ServerConstants.GATEWAY_CMD_SEQ_NO_POS, seqNoArr, 0, seqNoArr.length);
    Integer seqNo = ServerUtil.byteArrayToInt(seqNoArr);
    cmdMap.put(seqNo, cmdPkt);
    
  } //end of method addCmd
  
  //this is the multicast command added into gw cache for retrying with no. of retries
  public synchronized void addCmd(byte[] packet, ArrayList<Fixture> fixtureList, int noOfRetries) {
    
    long currTime = System.currentTimeMillis();
    ArrayList<Long> fixtureIdList = new ArrayList<Long>();
    HashMap<Long, Fixture> cmdFixtMap = new HashMap<Long, Fixture>();
    int noOfFixtures = fixtureList.size();
    Fixture fixture = null;
    for(int i = 0; i < noOfFixtures; i++) {
      fixture = fixtureList.get(i);	  
      cmdFixtMap.put(fixture.getId(), fixture);
      fixtureIdList.add(fixture.getId());
    }    
    CommandPacket cmdPkt = new CommandPacket(currTime, packet, cmdFixtMap, fixtureIdList, noOfRetries);
     
    byte[] seqNoArr = new byte[4];    
    System.arraycopy(packet, ServerConstants.CMD_PKT_TX_ID_POS, seqNoArr, 0, seqNoArr.length);
    Integer seqNo = ServerUtil.byteArrayToInt(seqNoArr);
    cmdMap.put(seqNo, cmdPkt);
    
  } //end of method addCmd
  
  //ack command from gateway
  public synchronized void ackCmd(byte[] ackPkt) {
    
    Integer key = -1;       
    byte[] seqNoArr = new byte[4];
    System.arraycopy(ackPkt, ServerConstants.GATEWAY_CMD_SEQ_NO_POS, seqNoArr, 0, seqNoArr.length);
    key = ServerUtil.byteArrayToInt(seqNoArr);      
    
    fixtureLogger.debug(gw.getIpAddress() + ": got ack with seq no - " + key);
    if(!cmdMap.containsKey(key)) {
      return;
    }    
    cmdMap.get(key).markRemoved = true;
    
  } //end of method ackCmd

  //ack command from fixture
  public synchronized void ackCmd(byte[] ackPkt, Fixture fixture) {
            
    Integer key = -1;       
    byte[] seqNoArr = new byte[4];
    System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
    key = ServerUtil.byteArrayToInt(seqNoArr);      
    DeviceServiceImpl.getInstance().updateAuditRecord(key.intValue(), fixture.getId(), DeviceInfo.getNoOfRetries(), 0); // success
    fixtureLogger.debug(fixture.getFixtureName() + ": got ack with seq no - " + key);
    if(!cmdMap.containsKey(key)) {
      return;
    }
    fixtureLogger.debug(fixture.getFixtureName() + ": got ack with seq no -> " + key);
    CommandPacket cmdPkt = cmdMap.get(key);
    cmdPkt.removeFixture(fixture);    
          
  } //end of method ackCmd
    
} //end of class GatewayInfo
