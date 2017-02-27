package com.ems.server.processor;

/**
 * @author 
 *
 */

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.server.GatewayInfo;
import com.ems.server.service.PMStatsProcessorService;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;

public class ShortPMStatsProcessor {

	private static ShortPMStatsProcessor instance = null;
	
	private PMStatsProcessorService pmStatsProcessorService;
  private EnergyConsumptionManager ecMgr = null;
	static final Logger logger = Logger.getLogger("Perf");
	
	//this map is to hold short pm stats packets for each gateway
  private ConcurrentHashMap<Long, ConcurrentLinkedQueue<ShortPMStatsJob>> gwQueuesMap = 
    new ConcurrentHashMap<Long, ConcurrentLinkedQueue<ShortPMStatsJob>>(); 
  
  //this map is to hold thread pools for each gateway
  private ConcurrentHashMap<Long, GwShortPmThreadPoolExecutor> gwThreadPoolMap = 
    new ConcurrentHashMap<Long, GwShortPmThreadPoolExecutor>(); 

	public class ShortPMStatsJob implements Runnable {
    
		private GatewayInfo gwInfo = null;
    private byte[] shortPMStatsPkt = null;  
    private long timeAdj = 0L;
    
    public ShortPMStatsJob(GatewayInfo gwInfo, byte[] pkt) {
    	this.gwInfo = gwInfo;
    	shortPMStatsPkt = pkt;
    }
    
    public void run() {
    	try {
    		pmStatsProcessorService.processShortPMStats(shortPMStatsPkt, gwInfo, timeAdj);
    	}
    	catch(Exception e) {
    		logger.error(gwInfo.getGw().getId(), e);
    	}
    }
    
	}
	
	private ShortPMStatsProcessor() {
		
		pmStatsProcessorService = (PMStatsProcessorService) SpringContext
				.getBean("pmStatsProcessorService");
		ecMgr = (EnergyConsumptionManager) SpringContext.getBean("energyConsumptionManager");
		
	}
	
	public static ShortPMStatsProcessor getInstance() {
		
		if(instance == null) {
			synchronized(ShortPMStatsProcessor.class) {
				if(instance == null) {
					instance = new ShortPMStatsProcessor();
				}
			}
		}
		return instance;
		
	} //end of method getInstance

	public void addWork(GatewayInfo gwInfo, byte[] shortPMPkt) {
		
		Long gwId = gwInfo.getGw().getId();
		ShortPMStatsJob job = new ShortPMStatsJob(gwInfo, shortPMPkt);
		ConcurrentLinkedQueue<ShortPMStatsJob> gwQueue = gwQueuesMap.get(gwId);
		if(gwQueue == null) {
			gwQueue = new ConcurrentLinkedQueue<ShortPMStatsJob>();
			gwQueuesMap.put(gwId, gwQueue);
		}
		gwQueue.add(job);
		if(shortPMPkt[6] == 0) {
			//if the 0 count short pm stats packet is received, then process all the queued packets of that gateway	
			GwShortPmThreadPoolExecutor statsThrPool = gwThreadPoolMap.get(gwId);
			if(statsThrPool == null) {
				LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(); 
				statsThrPool = new GwShortPmThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, workQueue);
				gwThreadPoolMap.put(gwId, statsThrPool);
			}
			statsThrPool.init();
			//2nd, 3rd, 4th, 5th bytes are utc time on gateway
			int index = 2;
			byte[] tempIntByteArr = new byte[4];
			System.arraycopy(shortPMPkt, index, tempIntByteArr, 0, tempIntByteArr.length);
			long utcTime = ServerUtil.intByteArrayToLong(tempIntByteArr);
			gwInfo.setShortPMStatsEndTime(utcTime);
			statsThrPool.endTime = new Date(utcTime);
			//if end time is before 2000 just for ex (i.e time is not set on the gateway
			long currTime = System.currentTimeMillis();
			Calendar cal = Calendar.getInstance();
			cal.setTime(statsThrPool.endTime);
			long timeAdj = 0L;
			if(cal.get(Calendar.YEAR) < 2000) {
				timeAdj = currTime - utcTime;
				logger.debug("time is not set on gateway, adjustment - " + timeAdj);
			}
			Iterator<ShortPMStatsJob> iter = gwQueue.iterator();
			while(iter.hasNext()) {
				ShortPMStatsJob job1 = iter.next();
				job1.timeAdj = timeAdj;
				statsThrPool.execute(job1);
				iter.remove();
			}
		}
		
	}

	public class GwShortPmThreadPoolExecutor extends ThreadPoolExecutor {
    
		private Date startTime;
		private Date endTime;
		private boolean init = false;
		
		public void init() {
			init = true;
		}

    public GwShortPmThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
    		TimeUnit unit, LinkedBlockingQueue workQueue) { 
      
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue); 
      prestartAllCoreThreads();
      allowCoreThreadTimeOut(false);
      
    } //end of constructor

    protected void beforeExecute(Thread t, Runnable r) {
      
    	ShortPMStatsJob job = null;
      if(r instanceof ShortPMStatsJob) {
      	job = (ShortPMStatsJob)r;
      	byte[] pkt = job.shortPMStatsPkt;
      	if(pkt[6] == 0) {
      		//end of the stream 
      		if(init) {
      			//only 0 count short pm received so there are no pending short pm stats
      			return;
      		} else {
      			logger.debug(job.gwInfo.getGw().getMacAddress() + " end of short pm - " + endTime);
      			//now run the aggregation queries
      			// TODO call the hourly energy consumption stored procedure
      			Calendar cal = Calendar.getInstance();
      			cal.setTime(startTime);
      			while(cal.before(endTime)) {
      				int currMin = cal.get(Calendar.MINUTE);
      				//TODO need to check whether the 1st hour is required for aggregation because
      				//if the first time says 10:20 we need aggregation from 10 to 11 and not from 9 to 10.
      	    
      				Date toDate = DateUtils.truncate(cal.getTime(), Calendar.HOUR);
      				try {
      					ecMgr.aggregateFixtureHourlyData(toDate, job.gwInfo.getGw().getId());
      				}
      				catch(Exception e) {
      					logger.error(job.gwInfo.getGw().getMacAddress() + " error in hourly aggregation - ", e);
      				}
      				cal.add(Calendar.HOUR, 1);
      			}
      		}
      	} else if(init) {
      		//this is the first packet
      		//15th to 18th bytes represent the time in first sensor short pm stats
      		byte[] tempIntByteArr = new byte[4];
      		System.arraycopy(job.shortPMStatsPkt, 15, tempIntByteArr, 0, tempIntByteArr.length);
      		startTime = new Date(ServerUtil.intByteArrayToLong(tempIntByteArr));
      		logger.debug(job.gwInfo.getGw().getMacAddress() + " start of short pm - " + startTime);
      		init = false;
      	}
      }      
      super.beforeExecute(t, r);      
      
    } //end of method beforeExecute
    
    public void execute(ShortPMStatsJob job) {
      
      super.execute(job); 
      
    } //end of method execute
    
    protected void afterExecute(Runnable r, Throwable t) {
      
    	ShortPMStatsJob job = null;
      if(r instanceof ShortPMStatsJob) {
      	job = (ShortPMStatsJob)r;
      }
      super.afterExecute(r, t);
      
    } //end of method afterExecute
    
  } //end of class GwShortPmThreadPoolExecutor
	
}