package com.ems.server.processor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Plugload;
import com.ems.model.PlugloadEnergyConsumption;
import com.ems.server.util.ServerUtil;
import com.ems.service.PlugloadEnergyConsumptionManager;
import com.ems.service.PlugloadManager;
import com.ems.utils.DateUtil;

/*@Service("plugloadECPersistor")*/
public class PlugloadECPersistor {

	private static PlugloadECPersistor instance = null;

	static final Logger logger = Logger.getLogger("Perf");

	// hash map to hold the energy consumption object list in memory based on
	// capture at
	private Map<String, Map<String, PlugloadEnergyConsumption>> workQueue = null;

	private PlugloadManager plugloadMgr = null;
	private PlugloadEnergyConsumptionManager plugloadEnergyConsumptionManager;	

	private Timer plugloadECTimer = new Timer("Plugload EC Timer", true);
	private int ecTimeInterval = 5 * 60 * 1000; // 5 minutes
	
	private int count = 0;

	private PlugloadECPersistor() {
		workQueue = new HashMap<String, Map<String, PlugloadEnergyConsumption>>();
		startPlugloadECPersistor(); // commenting out this line so that the timer does not get invoked right now. Will uncomment it, when plugload changes are released.
	}

	public static PlugloadECPersistor getInstance() {

		if (instance == null) {
			synchronized (PlugloadECPersistor.class) {
				if (instance == null) {

					instance = new PlugloadECPersistor();
				}
			}
		}
		return instance;
	}

	public void addPlugloadECToQueue(String captureAt, String snapAddress,
			PlugloadEnergyConsumption plugloadEnergyConsumption) {
		synchronized (workQueue) {
			//System.out.println("=================== capture at is "+captureAt+" "+plugloadEnergyConsumption);
			if (workQueue.containsKey(captureAt)) {				
				workQueue.get(captureAt).put(snapAddress,
						plugloadEnergyConsumption);
			} else {				
				Map<String, PlugloadEnergyConsumption> map = new HashMap<String, PlugloadEnergyConsumption>();
				map.put(snapAddress, plugloadEnergyConsumption);
				//System.out.println("===== map is "+map.get(snapAddress));
				workQueue.put(captureAt, map);
			}
		}		
	}

	public void processPlugloadECData(String captureAt) {
		Calendar c = Calendar.getInstance();
		long startTime = c.getTimeInMillis();
		logger.info("starting energyconsumption processing for captureAT "
				+ captureAt);
		/*System.out
				.println("starting energyconsumption processing for captureAT "
						+ captureAt);*/
		BigDecimal zero = new BigDecimal(0);
		short zeroBucket = 1;		
		if (plugloadMgr == null) {
			plugloadMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");
		}
		PlugloadEnergyConsumption plugloadEnergyConsumptionZB = null;
		Plugload plugload = null;

		if (plugloadEnergyConsumptionManager == null) {
			plugloadEnergyConsumptionManager = (PlugloadEnergyConsumptionManager) SpringContext.getBean("plugloadEnergyConsumptionManager");
		}

		Map<String, PlugloadEnergyConsumption> map = this.workQueue.get(captureAt);
		//System.out.println("====== work q is " + this.workQueue);
		List<Plugload> plugloadList = plugloadMgr.getAllCommissionedPlugloads();
		if (plugloadList != null) {
			int noOfPlugloads = plugloadList.size();
			/*System.out.println("plugloads are "+plugloadList);
			System.out.println("============= no of plugloads" + noOfPlugloads);*/
			for (int i = 0; i < noOfPlugloads; i++) {
				plugload = plugloadList.get(i);
				/*System.out.println("=========== snap address"
						+ plugload);
				System.out.println("map for captureAt " + captureAt + " is "
						+ map);*/
				if (map != null && !map.containsKey(plugload.getSnapAddress())) {
					logger.info("Adding a zero bucket as no energyconsumption object present for snapaddress "
							+ plugload.getSnapAddress()
							+ " for captureAt "
							+ captureAt);
//					System.out
//							.println("Adding a zero bucket as no energyconsumption object present for snapaddress "
//									+ plugload.getSnapAddress()
//									+ " for captureAt " + captureAt);
					plugloadEnergyConsumptionZB = new PlugloadEnergyConsumption();
					plugloadEnergyConsumptionZB.setPlugload(plugload);
					plugloadEnergyConsumptionZB.setCaptureAt(DateUtil
							.parseString(captureAt, "yyyyMMddHHmmss"));
					plugloadEnergyConsumptionZB.setZeroBucket(zeroBucket);
					plugloadEnergyConsumptionZB.setBaseEnergy(zero);
					plugloadEnergyConsumptionZB.setEnergy(zero);
					plugloadEnergyConsumptionZB.setUnmanagedEnergy(zero);
					plugloadEnergyConsumptionZB.setManagedLastLoad(zero);
					plugloadEnergyConsumptionZB.setTuneupSaving(zero);
					plugloadEnergyConsumptionZB.setManualSaving(zero);
					plugloadEnergyConsumptionZB.setOccSaving(zero);
					plugloadEnergyConsumptionZB.setBaseUnmanagedEnergy(zero);
					plugloadEnergyConsumptionZB.setUnmanagedLastLoad(zero);
					this.workQueue.get(captureAt).put(plugload.getSnapAddress(),plugloadEnergyConsumptionZB);
				}else if(map == null){					
					PlugloadEnergyConsumption pec1 = plugloadEnergyConsumptionManager.getPlugloadEnergyConsumptionFromDB(
							DateUtil.parseString(captureAt, "yyyyMMddHHmmss"), plugload.getId());
					if(pec1 == null){
						logger.info("Adding a zero bucket as no energyconsumption object present  "							
								+ " for captureAt "
								+ captureAt);
						map = new HashMap<String, PlugloadEnergyConsumption>();
						plugloadEnergyConsumptionZB = new PlugloadEnergyConsumption();
						plugloadEnergyConsumptionZB.setPlugload(plugload);
						plugloadEnergyConsumptionZB.setCaptureAt(DateUtil.parseString(captureAt, "yyyyMMddHHmmss"));
						plugloadEnergyConsumptionZB.setZeroBucket(zeroBucket);
						plugloadEnergyConsumptionZB.setBaseEnergy(zero);
						plugloadEnergyConsumptionZB.setEnergy(zero);
						plugloadEnergyConsumptionZB.setUnmanagedEnergy(zero);
						plugloadEnergyConsumptionZB.setManagedLastLoad(zero);
						plugloadEnergyConsumptionZB.setTuneupSaving(zero);
						plugloadEnergyConsumptionZB.setManualSaving(zero);
						plugloadEnergyConsumptionZB.setOccSaving(zero);
						plugloadEnergyConsumptionZB.setBaseUnmanagedEnergy(zero);
						plugloadEnergyConsumptionZB.setUnmanagedLastLoad(zero);
						logger.info("inserting this zb object to map to save");
						map.put(plugload.getSnapAddress(),plugloadEnergyConsumptionZB);
					}
				}
			}
		}

		if (map != null) {
			try {
				plugloadEnergyConsumptionManager.save(map);
			} catch (Exception e) {
				logger.error("exception occurred while saving EC objects at captureAt "
						+ captureAt);
				e.printStackTrace();
			} finally {
				workQueue.remove(captureAt);
			}
		}
		
					
			processRemainingCaptureATs(null);
			
		
		long endTime = c.getTimeInMillis();
		/*System.out.println("completed energyconsumption processing for captureAT "
				+ captureAt+" in "+((endTime-startTime)/60*1000) +" minutes");*/
		logger.info("completed energyconsumption processing for captureAT "
				+ captureAt+" in "+((endTime-startTime)/60*1000) +" minutes");
	}

	
	
	public void processRemainingCaptureATs(String captureAtTime) {
		if (plugloadEnergyConsumptionManager == null) {
			plugloadEnergyConsumptionManager = (PlugloadEnergyConsumptionManager) SpringContext.getBean("plugloadEnergyConsumptionManager");
		}
		logger.info("captureattime passed while aggregation is "+captureAtTime);
		String captAt = null;
		Date captureAtTimeFromQueue,captureAtTimePassed = null;
		if(captureAtTime != null){
			captureAtTimePassed = DateUtil.parseString(captureAtTime, "yyyyMMddHHmmss");
		}
		synchronized (this) {			
				try {
					Set<String> captureAtValues = this.workQueue.keySet();
					Iterator<String> it = captureAtValues.iterator();
					Iterator<String> it1 = null;
					while(it.hasNext()){
						captAt = it.next();
						captureAtTimeFromQueue = DateUtil.parseString(captAt, "yyyyMMddHHmmss");						
						if(captureAtTimeFromQueue != null && (captureAtTimeFromQueue.before(getTruncatedDate()) || captureAtTimeFromQueue.equals(captureAtTimePassed))){
							Map<String, PlugloadEnergyConsumption> map = this.workQueue.get(captAt);
							it1 = map.keySet().iterator();
							while(it1.hasNext()){
								String snapAddress = it1.next();
								PlugloadEnergyConsumption pec = map.get(snapAddress);
								//System.out.println("========== processing pec for captureAT "+captAt+"==="+pec);
								PlugloadEnergyConsumption pec1 = plugloadEnergyConsumptionManager.getPlugloadEnergyConsumptionFromDB(
								DateUtil.parseString(captAt, "yyyyMMddHHmmss"), pec.getPlugload().getId());
								//System.out.println("========== pec1 "+pec1+" === date"+pec1.getCaptureAt());
								if(pec1 != null){
									logger.info("updating existing plugload energy consumption object for captureAT ="+captAt+" and plugload_id="+pec.getPlugload().getId());
									pec.setId(pec1.getId());
									pec.setZeroBucket((short)3);
									pec1 = pec;
									plugloadEnergyConsumptionManager.merge(pec1);		
									it1.remove();
								}else{
									logger.info("adding a new energy consumption object for captureAT = "+captAt+" and plugload_id = "+pec.getPlugload().getId());
									plugloadEnergyConsumptionManager.save(pec);
								}
							}							
							it.remove();
						}			
										
					}
					plugloadEnergyConsumptionManager.flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			
		}
			
	}
	
	public void updateZeroBuckets(){
		// find two max capture ats
		// in this get the latest energycum value
		// basd on the time difference equally distribute the value of energy cum to managed enery and unmanaged energy fields only
		if (plugloadMgr == null) {
			plugloadMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");
		}
		

		if (plugloadEnergyConsumptionManager == null) {
			plugloadEnergyConsumptionManager = (PlugloadEnergyConsumptionManager) SpringContext.getBean("plugloadEnergyConsumptionManager");
		}

		logger.info("inside update zero buckets method");
		Plugload plugload = null;		
		List<Plugload> plugloadList = plugloadMgr.getAllCommissionedPlugloads();
		try {
			if (plugloadList != null) {
				int noOfPlugloads = plugloadList.size();			
				for (int i = 0; i < noOfPlugloads; i++) {
					plugload = plugloadList.get(i);
//				List<Object[]> l = plugloadEnergyConsumptionManager.getLatestEnergyConsumptionByPlugload(plugload.getId());
					Map<String,Map<Long,List<Object[]>>> map = plugloadEnergyConsumptionManager.getAllPlugloadEnergyConsumptionZBRecords(plugload.getId());
					Iterator<String> it = map.keySet().iterator();
					while(it.hasNext()){
						String captAt = it.next();
						Map<Long, List<Object[]>> m = map.get(captAt);
						Iterator<Long> it1 = m.keySet().iterator();
						while(it1.hasNext()){
							Long plugloadId = it1.next();
							List<Object[]> o = m.get(plugloadId);
							if(o != null){
								Date nzbCaptAT_higher = (Date) o.get(0)[0];
								Date zbCaptAT_lower = (Date) o.get(1)[0];							
								Date zbCaptAT = DateUtil.parseString(captAt,"yyyyMMddHHmmss");
								//System.out.println("====== zbcaptat is "+zbCaptAT);
								Long diffInMinutes = (nzbCaptAT_higher.getTime() - zbCaptAT_lower.getTime())/(1000*60);
								Long diffFactor = 5L;
								Long spreadManagedEnergyCumValues1 = ((BigInteger) o.get(0)[2]).longValue();
								Long spreadUnManagedEnergyCumValues1 = ((BigDecimal) o.get(0)[3]).longValue();	
								
								Long spreadManagedEnergyCumValues2 = ((BigInteger) o.get(1)[2]).longValue();
								Long spreadUnManagedEnergyCumValues2 = ((BigDecimal) o.get(1)[3]).longValue();
								
								Long spreadManagedEnergyCumValues = (spreadManagedEnergyCumValues1 - spreadManagedEnergyCumValues2)/(100*diffInMinutes);
								Long spreadUnManagedEnergyCumValues = (spreadUnManagedEnergyCumValues1 - spreadUnManagedEnergyCumValues2)/(100*diffInMinutes);
								logger.info("updating zero bucket for plugload_id"+plugloadId+" at capture_At= "+captAt);
								/*System.out.println("===============spread energy cum value is "
										+ ""+spreadManagedEnergyCumValues+" ===  "+spreadUnManagedEnergyCumValues+"========== "+diffInMinutes);*/
								plugloadEnergyConsumptionManager.updateZeroBuckets(plugload.getId(),spreadManagedEnergyCumValues*diffFactor,spreadUnManagedEnergyCumValues*diffFactor,nzbCaptAT_higher,zbCaptAT);
							}
						}
					}									
				}
				plugloadEnergyConsumptionManager.flush();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

	public class PLugloadEnergyConsumptionTask extends TimerTask {

		public void run() {

			try {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				int unroundedMinutes = calendar.get(Calendar.MINUTE);
				int mod = unroundedMinutes % 5;
				calendar.add(Calendar.MINUTE, -(mod + 5));
				processPlugloadECData(DateUtil.formatDate(calendar.getTime(),
						"yyyyMMddHHmmss"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} // end of method run

	}
	
	public static Date getTruncatedDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int unroundedMinutes = calendar.get(Calendar.MINUTE);
		int mod = unroundedMinutes % 5;
		calendar.add(Calendar.MINUTE, -(mod + 5));
		return calendar.getTime();
	}

	private void startPlugloadECPersistor() {

		new Thread() {
			public void run() {
				if (logger.isDebugEnabled()) {
					logger.debug("starting the EC persistor task  task");
				}
				PLugloadEnergyConsumptionTask plugloadEnergyConsumptionTask = new PLugloadEnergyConsumptionTask();
				plugloadECTimer.scheduleAtFixedRate(
						plugloadEnergyConsumptionTask, 0, ecTimeInterval);
			}

			// end of method run
		}.start();

	}

}
