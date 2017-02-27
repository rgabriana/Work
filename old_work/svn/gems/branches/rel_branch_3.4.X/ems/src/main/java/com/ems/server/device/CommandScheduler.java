/**
 * 
 */
package com.ems.server.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.cache.PlugloadCache;
import com.ems.cache.PlugloadInfo;
import com.ems.model.Device;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.Plugload;
import com.ems.model.Wds;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.ServerMain.DBUpdateOnAckWork;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.util.ServerUtil;
import com.ems.service.EmsAuditService;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.PlugloadManager;
import com.ems.service.WdsManager;
import com.ems.types.DeviceType;

/**
 * @author Sreedhar
 *
 */
public class CommandScheduler {

  public static int JOB_STATUS_READY = 1;
  public static int JOB_STATUS_SUSPEND = 2;
  public static int JOB_STATUS_RUNNING = 3;
  public static int JOB_STATUS_FINISHED = 4;
  
  public static int PRIORITY_HIGH = 1;
  public static int PRIORITY_LOW = 2;
  
  private static CommandScheduler instance = null;

  private List<Long> pendingSUList = Collections.synchronizedList(new ArrayList<Long>());  
  
  //this map is to hold thread pools for each gateway
  private ConcurrentHashMap<Long, PausableThreadPoolExecutor> gwThreadPoolMap = 
    new ConcurrentHashMap<Long, PausableThreadPoolExecutor>(); 
   
  //this map is used for acks
  private ConcurrentHashMap<Long, CommandJob> cmdAckMap = 
    new ConcurrentHashMap<Long, CommandJob>();
  
  private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");
  private static Logger logger = Logger.getLogger(CommandScheduler.class);
  private static Logger switchLogger = Logger.getLogger("SwitchLogger");
  private static Logger plugloadLogger = Logger.getLogger("PlugloadLogger");
  private FixtureManager fixtureMgr = null;
  private EmsAuditService emsAuditMgr = null;  
  private GatewayManager gwMgr = null;
  private WdsManager wdsMgr = null;
  private PlugloadManager plugloadMgr = null;
  
  private List<StatusObserver> statusObserverList = Collections.synchronizedList(
      new ArrayList<StatusObserver>());
    
  private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();        
  private ThreadPoolExecutor cmdThreadPool = new ThreadPoolExecutor(1, 1, 0, 
      TimeUnit.MILLISECONDS, workQueue);
      
  public interface StatusObserver {
    
    public void statusUpdate(Device fixture, int msgType, boolean ackStatus, int retries);
    
  } //end of interface StatusObserver
  
  public void addStatusObserver(StatusObserver observer) {
    
    statusObserverList.add(observer);
    
  } //end of method addStatusObserver
  
  public void removeStatusObserver(StatusObserver observer) {
    
    statusObserverList.remove(observer);
    
  } //end of method removeStatusObserver
    
  private void notifyObservers(Device fixture, int msgType, boolean ackStatus, int retries) {
    
    Iterator<StatusObserver> observerIter = statusObserverList.iterator();
    while(observerIter.hasNext()) {
      observerIter.next().statusUpdate(fixture, msgType, ackStatus, retries);
    }
    
  } //end of method notifyObservers
  
  private CommandScheduler() {  
    
    // TODO Auto-generated constructor stub      
    fixtureMgr = (FixtureManager)SpringContext.getBean("fixtureManager");    
    gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");
    emsAuditMgr = (EmsAuditService)SpringContext.getBean("emsAuditService");
    wdsMgr = (WdsManager)SpringContext.getBean("wdsManager");    
    plugloadMgr = (PlugloadManager)SpringContext.getBean("plugloadManager");
  } //end of constructor
  
  public static CommandScheduler getInstance() {
    
    if(instance == null) {
      synchronized(CommandScheduler.class) {
	if(instance == null) {
	  instance = new CommandScheduler();	  
	}
      }
    }      
    return instance;
    
  } //end of method getInstance
    
  public long addCommand(Device device, byte[] dataPacket, int msgType, boolean retryReq,
      int pktDelay) {
    
    int[] fixtureArr = new int[1];
    fixtureArr[0] = device.getId().intValue();
    long seqNo = addCommand(fixtureArr, dataPacket, msgType, device.getType(), retryReq, pktDelay,
    		DeviceInfo.getNoOfRetries());
    device = null;
    dataPacket = null;
    return seqNo;
    
  } //end of method addCommand

  public long addCommand(int[] fixtArr, byte[] dataPacket, int msgType, boolean retryReq,
          int pktDelay, int noOfCmdRetries) {
      long seqNo = addCommand(fixtArr, dataPacket, msgType, DeviceType.Fixture.getName(), retryReq, pktDelay,
      		noOfCmdRetries);
      return seqNo;
  }
  
  public long addCommand(int[] fixtArr, byte[] dataPacket, int msgType, boolean retryReq,
      int pktDelay) {
  	
  	return addCommand(fixtArr, dataPacket, msgType, retryReq, pktDelay, DeviceInfo.getNoOfRetries());
  	
  }
  
  public long addCommand(int[] fixtArr, byte[] dataPacket, int msgType, String deviceType, boolean retryReq, int pktDelay) {
  	
  	return addCommand(fixtArr, dataPacket, msgType, deviceType, retryReq, pktDelay, DeviceInfo.getNoOfRetries());
  	
  }
  
  public long addCommand(int[] fixtArr, byte[] dataPacket, int msgType, String deviceType, boolean retryReq,
      int pktDelay, int noOfCmdRetries) {
        
    CommandJob job = new CommandJob(pktDelay, deviceType);
    long seqNo = job.cmdSeq;
    switch(msgType) {
    case ServerConstants.SET_PROFILE_MSG_TYPE:
    case ServerConstants.SET_PROFILE_ADV_MSG_TYPE:
    case ServerConstants.SET_ACK_SU:
    case ServerConstants.SET_CURRENT_TIME:
    case ServerConstants.GET_MANUF_INFO_REQ:
    case ServerConstants.SU_CMD_REQ_DETAIL_CONFIG_CRC_REQ:
      job.priority = PRIORITY_LOW;
      break;
    default:
      job.priority = PRIORITY_HIGH;
      break;
    }
    if(fixtureLogger.isInfoEnabled()) {
      fixtureLogger.info("cmd(txId=" + job.cmdSeq + ",msg=" + msgType + ",prio=" + job.priority
	+ ") on " + fixtArr.length + " fixtures " + ServerUtil.getLogPacket(dataPacket));
    }
    job.fixtureArr = fixtArr;
    job.dataPacket = dataPacket;
    job.msgType = msgType;
    if(retryReq) {
      if(msgType == ServerConstants.SET_VALIDATION_MSG_TYPE) {
	job.noOfRetries = DeviceServiceImpl.VALIDATION_NO_OF_RETRIES;
      } else {
	job.noOfRetries = noOfCmdRetries; //DeviceInfo.getNoOfRetries();
      }
    }
    job.retryInterval = DeviceInfo.getCommandRetryDelay();    
    //addCommand(job);
    cmdThreadPool.execute(job);
    /* commenting out the this audit as we have user level audit log
    //add the audit log
    if (msgType != ServerConstants.IMAGE_UPGRADE_MSG_TYPE && 
	msgType != ServerConstants.SET_CURRENT_TIME && 
	msgType != ServerConstants.GET_STATUS_MSG_TYPE &&
	msgType != ServerConstants.GET_VERSION_MSG_TYPE &&
	msgType != ServerConstants.SET_ACK_SU &&
	msgType != ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE) {
      int noOfDevices = fixtArr.length;
      for(int i = 0; i < noOfDevices; i++) {
	Fixture fixture = fixtureMgr.getFixtureById(fixtArr[i]);
	emsAuditMgr.insertAuditRecord(job.cmdSeq.intValue(), (long)fixtArr[i], 
	    fixture.getFixtureName(), ServerConstants.DEVICE_FIXTURE, msgType);
      }
    }
    */
    job = null;
    fixtArr = null;
    dataPacket = null;
    return seqNo;
    
  } //end of method addCommand
    
  public void cancelCommand(long seq) {
    
    
  } //end of method cancelCommand
    
  //private Object cmdMonitor = new Object();
  
  public class SuJobDetails {
    
    private Device fixture = null;
    private int currentRetryAtt = 0;
    private long cmdSentTime;
    private long retryInterval;
    private boolean markedForDelete = false;
    
    public SuJobDetails(Device fixture, long cmdTime, long retryInterval) {
      
      this.fixture = fixture;
      this.cmdSentTime = cmdTime;
      this.retryInterval = retryInterval;
      
    } //end of constructor
   
    public boolean isRetryEligible() {
      
      if((System.currentTimeMillis() - cmdSentTime) > retryInterval) {
	return true;
      }
      return false;
      
    } //end of method isRetryEligible
    
  } //end of class SuJobDetails
  
  public class GwCmdJob implements Runnable {
      
    private long gwId;
    private ArrayList<Device> suList = new ArrayList<Device>();
    private HashMap<Long, SuJobDetails> suRetryMap = new HashMap<Long, SuJobDetails>();    
    private long lastCmdSentTime = 0;
    private int interPktDelay = DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY;
    private Gateway gw = null;
    
    private CommandJob cmdJob;
    private int runStatus = JOB_STATUS_READY;    
        
    public GwCmdJob(long gwId, int pktDelay, CommandJob cmdJob) {
      
      this.gwId = gwId;
      interPktDelay = pktDelay;
      this.cmdJob = cmdJob;
      gw = gwMgr.loadGateway(gwId);
      
    } //end of constructor
    
    public void addSU(Fixture fixture) {
      suList.add(fixture);
    }

    public void addWDS(Wds oWds) {
        suList.add(oWds);
    }

    public void addPlugload(Plugload oPL) {
        suList.add(oPL);
    }

    public int getRunStatus() {
      
      return runStatus;
      
    } //end of method getRunStatus
    
    public void addSuToRetryList(SuJobDetails suDetails) {	
      suRetryMap.put(suDetails.fixture.getId(), suDetails);
    }
    
    public void setLastCmdSentTime(long date) {
      this.lastCmdSentTime = date;
    }
    
    public boolean isGwReady() {
      
    	if(ServerUtil.compareVersion(gw.getApp2Version(), "2.0") >= 0) {
    		//new hardware
    		interPktDelay = DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY2;
    	}
      if(System.currentTimeMillis() > lastCmdSentTime + interPktDelay) {
      	return true;
      }
      return false;
      
    } //end of method isGwReady
    
    private void sendCommands() {
            
    	SSLSessionManager.getInstance().checkSSLConnection(gwId);
      McastPacket mPkt = new McastPacket();
      //if the interval between two successive commands on the gateway is not 300 millis      	
      Iterator<Device> suIter = suList.iterator();	
      Device oDevice = null;	
      long cmdSentTime = System.currentTimeMillis();     
      Long cmdSeq = cmdJob.cmdSeq;
      int msgType = cmdJob.msgType;
      mPkt.setTransactionId(cmdSeq.intValue());            
      while(suIter.hasNext()) {
      	oDevice = suIter.next();	  
      	if(pendingSUList.contains(oDevice.getId())) {
      		//there is an outstanding command for SU
      		if(fixtureLogger.isInfoEnabled()) {
      			fixtureLogger.info(cmdSeq + ": pending " + oDevice.getName());
      		}
      		continue;
      	}
      	byte[] snapByteArr = ServerUtil.getSnapAddr(oDevice.getMacAddress());
      	if(snapByteArr == null) {
      		fixtureLogger.error(cmdSeq + ":could not find " + oDevice.getMacAddress());
      		suIter.remove();
      		continue;
      	}
      	int noOfTargets = mPkt.addMcastTarget(snapByteArr); 
      	snapByteArr = null;
      	if(noOfTargets > 0) {
      		//su added to multicast packet	    
      		//add su to pending list
      		//for status command, SU should not put into pending list
      		if( msgType != ServerConstants.GET_STATUS_MSG_TYPE &&
      				msgType != ServerConstants.GET_VERSION_MSG_TYPE &&
      				msgType != ServerConstants.SET_ACK_SU &&
      				msgType != ServerConstants.SU_CMD_REQ_DETAIL_CONFIG_CRC_REQ &&
      				msgType != ServerConstants.GET_MANUF_INFO_REQ &&
      				msgType != ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE) {      			
      			pendingSUList.add(oDevice.getId());
      		}
      		//add the su to retry list if retry is required
      		if(cmdJob.noOfRetries > 0) {
      			if (oDevice.getType().equals(DeviceType.Fixture.getName()) ||
      					oDevice.getType().equals(DeviceType.WDS.getName()) ||
      					oDevice.getType().equals(DeviceType.Plugload.getName())) {
      				SuJobDetails suDetails = new SuJobDetails(oDevice, cmdSentTime, cmdJob.retryInterval);
      				addSuToRetryList(suDetails);
      			}
      		}	    
      		//remove the su from su list
      		suIter.remove();
      		/* commenting out the audit log as we have user audit log
	  			//update the state in the ems audit that it is in progress
	  			if (msgType != ServerConstants.IMAGE_UPGRADE_MSG_TYPE && 
	      		msgType != ServerConstants.SET_CURRENT_TIME && 
	      		msgType != ServerConstants.GET_STATUS_MSG_TYPE &&
	      		msgType != ServerConstants.GET_VERSION_MSG_TYPE &&
	      		msgType != ServerConstants.SET_ACK_SU &&
	      		msgType != ServerConstants.PROFILE_DOWNLOAD_MSG_TYPE) {
	    				emsAuditMgr.updateAuditRecord(cmdSeq.intValue(), fixture.getId(), 
								1, ServerConstants.AUDIT_INPROGRESS_STATUS);
	  			}
      		 */
      	} else {
      		//su is not added to multicast packet. reached the max. no of targets	    
      		break;
      	}
      }
      //traverse the retry su list if no. of targets is not max and add eligible su from
      //the retry list
      if(mPkt.getNoOfTargets() < DeviceServiceImpl.NO_OF_MULTICAST_TARGETS) {
      	//there is still room in the multicast packet
      	Iterator<Long> retryIter = suRetryMap.keySet().iterator();	  
      	SuJobDetails suDetails = null;
      	//fixtureLogger.debug(cmdSeq + ": before retry su list iter");
      	while(retryIter.hasNext()) {
      		suDetails = suRetryMap.get(retryIter.next());
      		if(suDetails.markedForDelete) {
      			//SU got ack so removing it
      			if(fixtureLogger.isDebugEnabled()) {
      				fixtureLogger.debug(suDetails.fixture.getName() + 
      						" removing from the retry list as marked for delete");
      			}
      			retryIter.remove();
      			continue;
      		}
      		if(!suDetails.isRetryEligible()) {
      			//time elapsed from last command sent time is less than retry interval
      			continue;
      		}  
      		oDevice = suDetails.fixture;
      		if(suDetails.currentRetryAtt == cmdJob.noOfRetries) {
      			//command has been retried for the max. no. of times for this SU
      			//command timed out on the fixture
      			retryIter.remove();
      			fixtureLogger.debug("removing " + oDevice.getId() + " from pending list");
      			pendingSUList.remove(oDevice.getId());
      			fixtureLogger.error(suDetails.fixture.getName() + 
      					": command(" + msgType + ") timed out after " + cmdJob.noOfRetries + " retries");
      			/*
	     			//YGC: The retry attempts is timed out, but the deletion thread has its own wait for 30 seconds to allow the delayed response from the sensors
	     			// So sending this notification is not necessary.
	    			if(msgType == ServerConstants.SU_APPLY_WIRELESS_CMD ||
							msgType == ServerConstants.SU_SET_WIRELESS_CMD) {
	      			DeviceServiceImpl.getInstance().suWirelessChangeAckStatus(fixture, false);
	    			}
      			 */
      			notifyObservers(oDevice, msgType, false, cmdJob.noOfRetries);
      			/* commenting out the audit log as we have user level audit log
	    				emsAuditMgr.updateAuditRecord(cmdSeq.intValue(), fixture.getId(), 
							suDetails.currentRetryAtt, 1);
      			 */
      			continue;
      		}		   	 
      		byte[] snapByteArr = ServerUtil.getSnapAddr(oDevice.getMacAddress());
      		if(snapByteArr == null) {
      			fixtureLogger.error(cmdSeq + ":could not find " + oDevice.getMacAddress());
      			retryIter.remove();
      			continue;
      		}
      		if(mPkt.addMcastTarget(snapByteArr) > 0) {
      			suDetails.currentRetryAtt++;
      			suDetails.cmdSentTime = cmdSentTime;
      			if(fixtureLogger.isInfoEnabled()) {
      				fixtureLogger.info(cmdSeq + "(" + suDetails.fixture.getName() + 
      						") retry -- " + suDetails.currentRetryAtt);	    
      			}
      		} else {
      			//su is not added to multicast packet. reached the max. no of targets	    
      			break;
      		}
      		snapByteArr = null;
      	}
      }
      if(mPkt.getNoOfTargets() > 0) {
      	setLastCmdSentTime(cmdSentTime);
      	//send the packet
      	if(msgType == ServerConstants.SET_CURRENT_TIME) {
      		DeviceInfo device = null;
      		if(oDevice instanceof Fixture) {
      			device = FixtureCache.getInstance().getDevice((Fixture)oDevice);
      		} else if(oDevice instanceof Plugload) {
      			device = PlugloadCache.getInstance().getDevice((Plugload)oDevice);
      		}
      		int seqNo = 1;
          if(device != null) {         
          	seqNo = device.getLastDateSyncSeqNo();
          	device.setLastDateSyncPending(false);
          }
      		byte[] timeArray = DeviceServiceImpl.getInstance().getCurrentTimePacket(seqNo);
      		if(timeArray == null) {
      			fixtureLogger.error(oDevice.getId() + ": could not form the time packet");
      			return;
      		}
      		mPkt.setCommand((byte)msgType, timeArray);
      		timeArray = null;
      		device = null;
      	} else {
      		mPkt.setCommand((byte)msgType, cmdJob.dataPacket);
      	}
      	if (oDevice.getType().equals(DeviceType.Fixture.getName())) {
      		// For now outgoing for SU and plugload is the same
            if(fixtureLogger.isInfoEnabled()) {
                fixtureLogger.info(cmdSeq + "(" + gwId + "): sending -- " + ServerUtil.getLogPacket(mPkt.getPacket()));
              }
      	    GatewayComm.getInstance().sendNodeDataToGateway(gwId, gw.getIpAddress(), mPkt.getPacket());
      	}else if(oDevice.getType().equals(DeviceType.WDS.getName())){
            if(switchLogger.isInfoEnabled()) {
                switchLogger.info(cmdSeq + "(" + gwId + "): sending --- " + ServerUtil.getLogPacket(mPkt.getPacket()));
              }
            GatewayComm.getInstance().sendWDSDataToGateway(gwId, gw.getIpAddress(), mPkt.getPacket());
      	} else { //plugload
      		if(plugloadLogger.isInfoEnabled()) {
            plugloadLogger.info(cmdSeq + "(" + gwId + "): sending -- " + ServerUtil.getLogPacket(mPkt.getPacket()));
          }
      		GatewayComm.getInstance().sendNodeDataToGateway(gwId, gw.getIpAddress(), mPkt.getPacket());
      	}
      }
      mPkt = null;
      
    } //end of method sendCommands
    
    public void run() {
      
      try {
	runStatus = JOB_STATUS_RUNNING;	
	if(fixtureLogger.isDebugEnabled()) {
	  fixtureLogger.debug(cmdJob.cmdSeq + "(" + gwId + "): job started");
	}
	while(true) {	
	  if(isGwReady()) {
	    sendCommands();
	  }	  
	  //job is suspended by a higher priority job
	  if(runStatus == JOB_STATUS_SUSPEND) {
	    suspendJob();
	    break;
	  }
	  //if all the SUs are done with
	  if(suList.size() == 0 && suRetryMap.size() == 0) {
	    //fixtureLogger.info(cmdJob.cmdSeq + "(" + gwId + "): done with all the fixtures");
	    runStatus = JOB_STATUS_FINISHED;	
	    cmdJob.gatewayFinished(gwId);
	    break;
	  }
	  ServerUtil.sleepMilli(10);
	}
      }
      catch(Exception e) {
	e.printStackTrace();
      } 

    } //end of method run
    
    private void suspendJob() {
      
      try {
	runStatus = JOB_STATUS_READY;
	//assuming only low priority jobs can be paused
	if(fixtureLogger.isInfoEnabled()) {
	  fixtureLogger.info(cmdJob.cmdSeq + " job is suspended");
	}
	//workerQueueMap.get(PRIORITY_LOW).add(0, this);
	gwThreadPoolMap.get(gwId).execute(this);	
      }
      catch(Exception e) {
	//e.printStackTrace();
	fixtureLogger.error(cmdJob.cmdSeq + ": could not be suspended - " + e.getMessage());
      }
      finally {
	//when the job is paused, some SUs might be in pending list. If the same SU is in
	//the next scheduled job, that will be waiting for the pending job which will never
	//come out. so it will dead lock
	//as only lower priority jobs are paused, only SUs of lower priority jobs
	//could be in pending list. so, clearing the pending list so that they
	//can be scheduled by higher priority job
	pendingSUList.clear();
      }
      
    }  //end of method suspendJob
         
    public void pauseJob() {
      
      runStatus = JOB_STATUS_SUSPEND;
            
    } //end of method pauseJob
        
  } //end of class GwCmdJob
  
  public class CommandJob implements Runnable {
    
    Long cmdSeq;
    int msgType;
    String deviceType;
    private int[] fixtureArr = null;
    private byte[] dataPacket = null;    
    private int priority;
    private int runStatus = JOB_STATUS_READY;    
    private HashMap<Long, GwCmdJob> gwMap = new HashMap<Long, GwCmdJob>();    
    private boolean pauseAllowed = false;
    private int interPktDelay = DeviceServiceImpl.MULTICAST_INTER_PKT_DELAY;
    private int noOfRetries = 0;
    private int retryInterval = 0;
    
    private String getCommonJobDesc(){
    	return "cmdSeq:"+cmdSeq+":msgType:"+msgType+":deviceType:"+deviceType+":fixtureArr:"+fixtureArr+":noOfRetries:"+noOfRetries;
    }
    public CommandJob(int pktDelay, String deviceType) {
      this.deviceType = deviceType;
      if(priority == PRIORITY_LOW) {
	pauseAllowed = true;
      }      
      cmdSeq = new Long(DeviceServiceImpl.getNextSeqNo());
      cmdAckMap.put(cmdSeq, this);
      interPktDelay = pktDelay;
      
    } //end of constructor
        
    public int markRemoved(Fixture fixture) {
      
      long gwId = fixture.getSecGwId();      
      if(!gwMap.containsKey(gwId)) {
	//command is finished on that gateway
	return 0;
      }
      pendingSUList.remove(fixture.getId());
      GwCmdJob gwJob = gwMap.get(gwId);
      SuJobDetails jobDetails = gwJob.suRetryMap.get(fixture.getId());
      //fixtureLogger.info(fixture.getFixtureName() + ": marking for delete for command " + cmdSeq);
      if(jobDetails != null) {
	jobDetails.markedForDelete = true;
	return jobDetails.currentRetryAtt;
      }      
      return 0;
      
    } //end of method markRemoved
    
    public int markRemoved(Plugload plugload) {
      
      long gwId = plugload.getSecGwId();      
      if(!gwMap.containsKey(gwId)) {
      	//command is finished on that gateway
      	return 0;
      }
      pendingSUList.remove(plugload.getId());
      GwCmdJob gwJob = gwMap.get(gwId);
      SuJobDetails jobDetails = gwJob.suRetryMap.get(plugload.getId());      
      if(jobDetails != null) {
      	jobDetails.markedForDelete = true;
      	return jobDetails.currentRetryAtt;
      }      
      return 0;
      
    } //end of method markRemoved
    
    public int markRemoved(Wds wds) {
      
      long gwId = wds.getGatewayId();      
      if(!gwMap.containsKey(gwId)) {
      	//command is finished on that gateway
      	return 0;
      }
      pendingSUList.remove(wds.getId());
      GwCmdJob gwJob = gwMap.get(gwId);
      SuJobDetails jobDetails = gwJob.suRetryMap.get(wds.getId());      
      if(jobDetails != null) {
      	jobDetails.markedForDelete = true;
      	return jobDetails.currentRetryAtt;
      }      
      return 0;
      
    } //end of method markRemoved
       
    public void run() {
    
      try {
	runStatus = JOB_STATUS_RUNNING;
	  if(fixtureLogger.isDebugEnabled()) {
	fixtureLogger.debug(cmdSeq + ": job is started");
      }
      if(gwMap.size() == 0) {
	//separate all the SUs to the proper GW map
	int noOfFixtures = fixtureArr.length;
	for(int i = 0; i < noOfFixtures; i++) {
	    if (this.deviceType.equals(DeviceType.Fixture.getName())) {
    	  Fixture fixture = fixtureMgr.getFixtureById(fixtureArr[i]);   
    	  if(fixture == null) {
    	    fixtureLogger.error(fixtureArr[i] + ": There is no Fixture");
    	    continue;
    	  }	
    	  Long gwId = fixture.getSecGwId();	  
    	  if(!gwMap.containsKey(gwId)) {
    	    gwMap.put(gwId, new GwCmdJob(gwId, interPktDelay, this));
    	  }	  
    	  if(!gwThreadPoolMap.containsKey(gwId)) {
    	    PriorityBlockingQueue<GwCmdJob> gwWorkQueue = 
    	      new PriorityBlockingQueue<GwCmdJob>(100, cmdComparator);
    	    PausableThreadPoolExecutor gwCmdThreadPool = new PausableThreadPoolExecutor(
    		1, 1, 0, TimeUnit.MILLISECONDS, gwWorkQueue);
    	    gwThreadPoolMap.put(gwId, gwCmdThreadPool);
    	  }
    	  gwMap.get(gwId).addSU(fixture);
	    }else if (this.deviceType.equals(DeviceType.WDS.getName())) {
	        // Currently re-using the SU list to add the WDS device as well. 
	          Wds oWds = wdsMgr.getWdsSwitchById(new Long(fixtureArr[i]));   
	          if(oWds == null) {
	            switchLogger.error(fixtureArr[i] + ": There is no ERC");
	            continue;
	          } 
	          Long gwId = oWds.getGatewayId();   
	          if(!gwMap.containsKey(gwId)) {
	            gwMap.put(gwId, new GwCmdJob(gwId, interPktDelay, this));
	          }   
	          if(!gwThreadPoolMap.containsKey(gwId)) {
	            PriorityBlockingQueue<GwCmdJob> gwWorkQueue = 
	              new PriorityBlockingQueue<GwCmdJob>(100, cmdComparator);
	            PausableThreadPoolExecutor gwCmdThreadPool = new PausableThreadPoolExecutor(
	            1, 1, 0, TimeUnit.MILLISECONDS, gwWorkQueue);
	            gwThreadPoolMap.put(gwId, gwCmdThreadPool);
	          }
	          gwMap.get(gwId).addWDS(oWds);
	    }else if (this.deviceType.equals(DeviceType.Plugload.getName())) {
	    	 Plugload oObj = plugloadMgr.getPlugloadById(fixtureArr[i]);   
	    	  if(oObj == null) {
	    	    fixtureLogger.error(fixtureArr[i] + ": There is no Plugload");
	    	    continue;
	    	  }	
	    	  Long gwId = oObj.getSecGwId();	  
	    	  if(!gwMap.containsKey(gwId)) {
	    	    gwMap.put(gwId, new GwCmdJob(gwId, interPktDelay, this));
	    	  }	  
	    	  if(!gwThreadPoolMap.containsKey(gwId)) {
	    	    PriorityBlockingQueue<GwCmdJob> gwWorkQueue = 
	    	      new PriorityBlockingQueue<GwCmdJob>(100, cmdComparator);
	    	    PausableThreadPoolExecutor gwCmdThreadPool = new PausableThreadPoolExecutor(
	    		1, 1, 0, TimeUnit.MILLISECONDS, gwWorkQueue);
	    	    gwThreadPoolMap.put(gwId, gwCmdThreadPool);
	    	  }
	    	  gwMap.get(gwId).addPlugload(oObj);
	    }
	}
      }
      Iterator<Long> gwIter = gwMap.keySet().iterator();
      GwCmdJob gwJob = null;      
      while(gwIter.hasNext()) {
	long gwId = gwIter.next();
	gwJob = gwMap.get(gwId);
	gwThreadPoolMap.get(gwId).execute(gwJob);
	  }
	} catch (Exception e) {
		logger.error("ERROR: OCCURED for CommandJob "+this.getCommonJobDesc(),e);
	}
      //TBD do we need to wait for all the gateways to finish?      
	    
    } //end of method run
    
    public void gatewayFinished(long gwId) {
      
      gwMap.remove(gwId);
      if(gwMap.size() == 0) {
	//all gateways are done
	runStatus = JOB_STATUS_FINISHED;
	cmdAckMap.remove(cmdSeq);
      }
      
    } //end of method gatewayFinished
        
  } //end of class CommandJob
  
  /*
   * this function is called when ack packet is received
   */
  public void gotAck(byte[] ackPkt, Fixture fixture, long gwId) {
    
    byte startMarker = ackPkt[0];    
    Long key = -1L;
    int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        
    if(startMarker == ServerConstants.FRAME_START_MARKER) {
      //index 4 in packet is msg type for which this is the ack packet
      key = new Long(ackPkt[3]);      
      msgStartPos = 3;
    } else {    
      byte[] seqNoArr = new byte[4];
      System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
      key = ServerUtil.intByteArrayToLong(seqNoArr);      
    }    
   
    if(fixtureLogger.isInfoEnabled()) {
      fixtureLogger.info(fixture.getFixtureName() + ": got the ack for command -- " + key);
    }
    int succAttempts = 1;
    if (cmdAckMap.containsKey(key)) {
      CommandJob  cj = cmdAckMap.get(key);	
      succAttempts = cj.markRemoved(fixture) + 1;      
    }
    
    DeviceInfo device = FixtureCache.getInstance().getDevice(fixture.getId());
    if(device != null) {      
      device.setLastGwId(gwId);      
    }
    
    int msg = (ackPkt[ServerConstants.RES_CMD_PKT_MSG_START_POS] & 0xFF);
    notifyObservers(fixture, msg, true, succAttempts);
    DBUpdateOnAckWork dbWork = ServerMain.getInstance().new DBUpdateOnAckWork(fixture, 
	msg, gwId, key, succAttempts);
    ServerMain.getInstance().addDbUpdateWork(dbWork);

    int msgType = (ackPkt[msgStartPos] & 0xFF);
    switch (msgType) {
    case ServerConstants.SET_PROFILE_MSG_TYPE :      
      FixtureCache.getInstance().getDevice(fixture).resetPushProfileForFixture(key);
      //fixtureMgr.resetPushProfileForFixture(fixture.getId());
      break;
    case ServerConstants.SET_PROFILE_ADV_MSG_TYPE:      
      fixtureMgr.resetPushGlobalProfileForFixture(fixture.getId());
      break;
    }
    fixture = null;
    ackPkt = null;

  } //end of method gotAck

  /**
   * Returned from Plugload when the Plugload rejects command 
   * @param ackPkt
   * @param plugload
   * @param gwId
   */
  public void gotNack(byte[] ackPkt, Plugload plugload, long gwId) {
  	
  	byte startMarker = ackPkt[0];
  	Long key = -1L;
  	int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;

  	if (startMarker == ServerConstants.FRAME_START_MARKER) {
  		// index 4 in packet is msg type for which this is the ack packet
  		key = new Long(ackPkt[3]);
  		msgStartPos = 3;
  	} else {
  		byte[] seqNoArr = new byte[4];
  		System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
  		key = ServerUtil.intByteArrayToLong(seqNoArr);
  	}

  	if (fixtureLogger.isInfoEnabled()) {
  		fixtureLogger.info(plugload.getName() + ": got the nack for command -- " + key);
  	}
  	int succAttempts = 1;
  	if (cmdAckMap.containsKey(key)) {
  		CommandJob cj = cmdAckMap.get(key);
  		succAttempts = cj.markRemoved(plugload) + 1;
  	}

  	DeviceInfo device = PlugloadCache.getInstance().getDevice(plugload.getId());
  	if (device != null) {
  		device.setLastGwId(gwId);
  	}
  	
  	int msgType = (ackPkt[msgStartPos] & 0xFF);
  	plugload = null;
  	ackPkt = null;
  	
  } //end of method gotNack
    
  /**
   * Returned from SU when the SU rejects command 
   * @param ackPkt
   * @param fixture
   * @param gwId
   */
    public void gotNack(byte[] ackPkt, Fixture fixture, long gwId) {

        byte startMarker = ackPkt[0];
        Long key = -1L;
        int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;

        if (startMarker == ServerConstants.FRAME_START_MARKER) {
            // index 4 in packet is msg type for which this is the ack packet
            key = new Long(ackPkt[3]);
            msgStartPos = 3;
        } else {
            byte[] seqNoArr = new byte[4];
            System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
            key = ServerUtil.intByteArrayToLong(seqNoArr);
        }

        if (fixtureLogger.isInfoEnabled()) {
            fixtureLogger.info(fixture.getFixtureName() + ": got the nack for command -- " + key);
        }
        int succAttempts = 1;
        if (cmdAckMap.containsKey(key)) {
            CommandJob cj = cmdAckMap.get(key);
            succAttempts = cj.markRemoved(fixture) + 1;
        }

        DeviceInfo device = FixtureCache.getInstance().getDevice(fixture.getId());
        if (device != null) {
            device.setLastGwId(gwId);
        }

        int msgType = (ackPkt[msgStartPos] & 0xFF);
        fixture = null;
        ackPkt = null;
    }

  /*
   * this function is called when ack packet is received
   */
  public void gotAck(byte[] ackPkt, Wds device, long gwId) {
    
    byte startMarker = ackPkt[0];    
    Long key = -1L;       
    if(startMarker == ServerConstants.FRAME_START_MARKER) {
      //index 4 in packet is msg type for which this is the ack packet
      key = new Long(ackPkt[3]);      
    } else {    
      byte[] seqNoArr = new byte[4];
      System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
      key = ServerUtil.intByteArrayToLong(seqNoArr);      
    }    
   
    if(switchLogger.isDebugEnabled()) {
        switchLogger.debug(device.getName() + ": got the ack for command -- " + key);
    }    
    if (cmdAckMap.containsKey(key)) {
      CommandJob  cj = cmdAckMap.get(key);	
      cj.markRemoved(device);      
    }   
    device = null;
    ackPkt = null;

  } //end of method gotAck
  
  /*
   * this function is called when ack packet is received from plugload
   */
  public void gotAck(byte[] ackPkt, Plugload device, long gwId) {
    
    byte startMarker = ackPkt[0];    
    Long key = -1L;       
    int msgStartPos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
    if(startMarker == ServerConstants.FRAME_START_MARKER) {
      //index 4 in packet is msg type for which this is the ack packet
      key = new Long(ackPkt[3]);      
      msgStartPos = 3;
    } else {    
      byte[] seqNoArr = new byte[4];
      System.arraycopy(ackPkt, ServerConstants.RES_ACK_CMD_PKT_SEQNO_POS, seqNoArr, 0, seqNoArr.length);
      key = ServerUtil.intByteArrayToLong(seqNoArr);      
    }    
   
    if(plugloadLogger.isDebugEnabled()) {
    	plugloadLogger.debug(device.getName() + ": got the ack for command -- " + key);
    }    
    int succAttempts = 1;
    if (cmdAckMap.containsKey(key)) {
      CommandJob  cj = cmdAckMap.get(key);	
      succAttempts = cj.markRemoved(device) + 1;      
    }
    
    PlugloadInfo plInfo = PlugloadCache.getInstance().getDevice(device.getId());
    if(plInfo != null) {      
      plInfo.setLastGwId(gwId);      
    }
    
    int msg = (ackPkt[ServerConstants.RES_CMD_PKT_MSG_START_POS] & 0xFF);
    notifyObservers(device, msg, true, succAttempts);
    
    /* not doing it now as light level message and validation message are not applicable to Plugload
    DBUpdateOnAckWork dbWork = ServerMain.getInstance().new DBUpdateOnAckWork(device, msg, gwId, key, succAttempts);
    ServerMain.getInstance().addDbUpdateWork(dbWork);
     */
    
    int msgType = (ackPkt[msgStartPos] & 0xFF);
    switch (msgType) {
    case ServerConstants.SET_PROFILE_MSG_TYPE :      
      PlugloadCache.getInstance().getDevice(device).resetPushProfileForPlugload(key);      
      break;
    case ServerConstants.SET_PROFILE_ADV_MSG_TYPE:      
      plugloadMgr.resetPushGlobalProfileForPlugload(device.getId());
      break;
    }
    device = null;
    ackPkt = null;

  } //end of method gotAck
  
  private CommandComparator cmdComparator = new CommandComparator();
  
  //comparator class to compare two command jobs that are added to the thread pool work queue
  public class CommandComparator implements Comparator<GwCmdJob> {
    
    @Override
    public int compare(GwCmdJob gwJob1, GwCmdJob gwJob2) {

      CommandJob job1 = gwJob1.cmdJob;
      CommandJob job2 = gwJob2.cmdJob;
//      fixtureLogger.info("job1(" + job1.cmdSeq + ") priority - " + job1.priority + " job2( " 
//	  + job2.cmdSeq + ") priority - " + job2.priority);
      if(job1.priority < job2.priority) {
	return -1;
      } else if(job1.priority > job2.priority) {
	return 1;
      }
      //priority is same, compare based on the sequence no.
      //if it does not matter to sort with in the equal priority jobs, 
      //then this can be commented
      if (job1.cmdSeq < job2.cmdSeq) {
	return -1;
      } else if(job1.cmdSeq > job2.cmdSeq) {
	return 1;
      }     
      return 0;
      
    } //end of method compare
    
  } //end of class CommandComparator
      
  public class PausableThreadPoolExecutor extends ThreadPoolExecutor {
    
    private ArrayList<GwCmdJob> activeLowPriorityJobList = new ArrayList<GwCmdJob>();

    public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, 
	long keepAliveTime, TimeUnit unit, PriorityBlockingQueue workQueue) { 
      
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue); 
      prestartAllCoreThreads();
      allowCoreThreadTimeOut(false);
      
    } //end of constructor

    protected void beforeExecute(Thread t, Runnable r) {
      
      GwCmdJob job = null;
      if(r instanceof GwCmdJob) {
	job = (GwCmdJob)r;
	//fixtureLogger.info(job.cmdJob.cmdSeq + ": before execute of cmd");
	if(job.cmdJob.priority == PRIORITY_LOW) {
	  activeLowPriorityJobList.add(job);
	}
      }      
      super.beforeExecute(t, r);      
      
    } //end of method beforeExecute
    
    public void execute(GwCmdJob cmd) {
      
      super.execute(cmd);
      if(cmd.runStatus == JOB_STATUS_RUNNING) {
      	//job started running
      	return;
      }
      //job is in the queue
      //if the new task is of high priority
      if(cmd.cmdJob.priority == PRIORITY_HIGH) { 
      	//all the threads are busy
      	if(cmd.getRunStatus() != JOB_STATUS_RUNNING) {	  
      		if(!activeLowPriorityJobList.isEmpty()) {
      		  if(fixtureLogger.isDebugEnabled()) {
      			fixtureLogger.debug(cmd.cmdJob.cmdSeq + ": some other low priority task is running");
      		  }
      			activeLowPriorityJobList.get(0).pauseJob();
      		}
      	}	
      }      
      
    } //end of method execute
    
    protected void afterExecute(Runnable r, Throwable t) {
      
      GwCmdJob job = null;
      if(r instanceof GwCmdJob) {
	job = (GwCmdJob)r;      	
      	if(job.getRunStatus() == JOB_STATUS_FINISHED) {
      	  if(fixtureLogger.isDebugEnabled()) {
      	    fixtureLogger.debug(job.cmdJob.cmdSeq + "(" + job.gwId + "): job is finished");
      	  }
      	  activeLowPriorityJobList.remove(job);
      	//this can be used for logging
      	} else {
      	  if(fixtureLogger.isInfoEnabled()) {
      	    fixtureLogger.info(job.cmdJob.cmdSeq + "(" + job.gwId + "): job is paused");
      	  }
      	}
      }
      super.afterExecute(r, t);
      
    } //end of method afterExecute
    
  } //end of class PausableThreadPoolExecutor  
  
} //end of class CommandScheduler
