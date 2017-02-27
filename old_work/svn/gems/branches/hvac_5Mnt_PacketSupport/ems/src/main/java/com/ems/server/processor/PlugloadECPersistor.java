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

	static final Logger logger = Logger.getLogger("WSLogger");

	// hash map to hold the energy consumption object list in memory based on
	// capture at
	private Map<String, Map<String, PlugloadEnergyConsumption>> workQueue = null;

	private PlugloadManager plugloadMgr = null;
	private PlugloadEnergyConsumptionManager plugloadEnergyConsumptionManager;	

	private Timer plugloadECTimer = new Timer("Plugload EC Timer", true);
	private int ecTimeInterval = 5 * 60 * 1000; // 5 minutes

	private PlugloadECPersistor() {
		workQueue = new HashMap<String, Map<String, PlugloadEnergyConsumption>>();
		//startPlugloadECPersistor(); // commenting out this line so that the timer does not get invoked right now. Will uncomment it, when plugload changes are released.
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
			if (workQueue.containsKey(captureAt)) {				
				workQueue.get(captureAt).put(snapAddress,
						plugloadEnergyConsumption);
			} else {				
				Map<String, PlugloadEnergyConsumption> map = new HashMap<String, PlugloadEnergyConsumption>();
				map.put(snapAddress, plugloadEnergyConsumption);
				workQueue.put(captureAt, map);
			}
		}
		
	}

	public void processPlugloadECData(String captureAt) {
		Calendar c = Calendar.getInstance();
		long startTime = c.getTimeInMillis();
		logger.info("starting energyconsumption processing for captureAT "
				+ captureAt);
		System.out
				.println("starting energyconsumption processing for captureAT "
						+ captureAt);
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
		System.out.println("====== work q is " + this.workQueue);
		List<Plugload> plugloadList = plugloadMgr.getAllPlugloads();
		if (plugloadList != null) {
			int noOfPlugloads = plugloadList.size();
			System.out.println("============= no of plugloads" + noOfPlugloads);
			for (int i = 0; i < noOfPlugloads; i++) {
				plugload = plugloadList.get(i);
				System.out.println("=========== snap address"
						+ plugload.getSnapAddress());
				System.out.println("map for captureAt " + captureAt + " is "
						+ map);
				if (map != null && !map.containsKey(plugload.getSnapAddress())) {
					logger.info("Adding a zero bucket as no energyconsumption object present for snapaddress "
							+ plugload.getSnapAddress()
							+ " for captureAt "
							+ captureAt);
					System.out
							.println("Adding a zero bucket as no energyconsumption object present for snapaddress "
									+ plugload.getSnapAddress()
									+ " for captureAt " + captureAt);
					plugloadEnergyConsumptionZB = new PlugloadEnergyConsumption();
					plugloadEnergyConsumptionZB.setPlugload(plugload);
					plugloadEnergyConsumptionZB.setCaptureAt(DateUtil
							.parseString(captureAt, "yyyyMMddHHmmss"));
					plugloadEnergyConsumptionZB.setZeroBucket(zeroBucket);
					plugloadEnergyConsumptionZB.setBaseEnergy(zero);
					plugloadEnergyConsumptionZB.setEnergy(zero);
					plugloadEnergyConsumptionZB.setManagedLastLoad(zero);
					plugloadEnergyConsumptionZB.setTuneupSaving(zero);
					plugloadEnergyConsumptionZB.setManualSaving(zero);
					plugloadEnergyConsumptionZB.setOccSaving(zero);
					plugloadEnergyConsumptionZB.setBaseUnmanagedEnergy(zero);
					plugloadEnergyConsumptionZB.setUnmanagedLastLoad(zero);
					workQueue.get(captureAt).put(plugload.getSnapAddress(),plugloadEnergyConsumptionZB);
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
		
		if(ServerUtil.getCurrentMin() % 15 == 0){
			updateZeroBuckets();
			processRemainingCaptureATs(captureAt);			
		}
		
		long endTime = c.getTimeInMillis();
		System.out.println("completed energyconsumption processing for captureAT "
				+ captureAt+" in "+((endTime-startTime)/60*1000) +" minutes");
		logger.info("completed energyconsumption processing for captureAT "
				+ captureAt+" in "+((endTime-startTime)/60*1000) +" minutes");
	}

	private void processRemainingCaptureATs(String captureAt) {
		Set<String> captureAtValues = this.workQueue.keySet();
		Iterator<String> it = captureAtValues.iterator();
		while(it.hasNext()){
			String captAt = it.next();
			Map<String, PlugloadEnergyConsumption> map = workQueue.get(captAt);
			PlugloadEnergyConsumption pec = map.get(captAt);
			pec.setZeroBucket((short) 3);
			plugloadEnergyConsumptionManager.save(map.get(captAt));
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

		
		Plugload plugload = null;
		PlugloadEnergyConsumption plugloadEnergyConsumptionZB = null;
		List<Plugload> plugloadList = plugloadMgr.getAllPlugloads();
		if (plugloadList != null) {
			int noOfPlugloads = plugloadList.size();			
			for (int i = 0; i < noOfPlugloads; i++) {
				plugload = plugloadList.get(i);
				List<Object[]> l = plugloadEnergyConsumptionManager.getLatestEnergyConsumptionByPlugload(plugload.getId());
				if(l != null && l.size() == 2){
					Date d1 = (Date) l.get(0)[0];
					Date d2 = (Date) l.get(1)[0];
				/*	Long managedEnergyCumValue = (Long) l.get(0)[2];
					Long unManagedEnergyCumValue = (Long) l.get(0)[3];
				*/	
					Long diffInMinutes = (d1.getTime() - d2.getTime())/(1000*60);
					Long diffFactor = diffInMinutes/5;
					Long spreadManagedEnertyCumValues = ((BigInteger) l.get(0)[2]).longValue()/diffInMinutes;
					Long spreadUnManagedEnertyCumValues = ((BigDecimal) l.get(0)[3]).longValue()/diffInMinutes;
					/*System.out.println("=== managed "+l.get(0)[2]+" ===== unmanaged "+l.get(0)[3]);
					System.out.println("===============spread energy cum value is "+spreadManagedEnertyCumValues+" ===  "+spreadUnManagedEnertyCumValues+"========== "+diffInMinutes);*/
					plugloadEnergyConsumptionManager.updateZeroBuckets(plugload.getId(),spreadManagedEnertyCumValues*diffFactor,spreadUnManagedEnertyCumValues*diffFactor,d1,d2);
					
				}				
			}
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
