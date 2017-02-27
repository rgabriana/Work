package com.emscloud.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.BldEnergyConsumptionDao;
import com.emscloud.dao.FacilityDao;
import com.emscloud.model.BldEnergyConsumption15min;
import com.emscloud.model.BldEnergyConsumptionDaily;
import com.emscloud.model.BldEnergyConsumptionHourly;
import com.emscloud.model.EmInstance;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.util.EcUtil;
import com.emscloud.vo.AggregatedSensorData;

@Service("bldEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BldEnergyConsumptionManager {
	
	@Resource
	private BldEnergyConsumptionDao bldEcDao;	
	@Resource
	ReplicaServerManager replicaServerManager ;	
	@Resource
	EmInstanceManager emInstanceManger;	
	@Resource
	CustomerManager custManager;	
	@Resource
	FacilityManager facilityManager;
	@Resource
	FacilityDao facilityDao;
	
	public BldEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub		
	}
		
	public List<AggregatedSensorData> getBldAggregatedEnergyData(long bldId, String timeSpan,
			String fromDate, String toDate, int tzOffset) {
		
		ArrayList<AggregatedSensorData> sensorDataList = new ArrayList<AggregatedSensorData>();
		if(timeSpan.equals("hourly")) {
			List<BldEnergyConsumptionHourly> bldEnergyList = bldEcDao.getBldHourlyEnergyData(bldId, fromDate, toDate);								
			Iterator<BldEnergyConsumptionHourly> bldIter = bldEnergyList.iterator();
			while(bldIter.hasNext()) {
				BldEnergyConsumptionHourly data = bldIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);		
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("daily")) {
			List<BldEnergyConsumptionDaily> energyDailyList = bldEcDao.getBldDailyEnergyData(bldId, fromDate, toDate); 		
			Iterator<BldEnergyConsumptionDaily> dailyIter = energyDailyList.iterator();
			while(dailyIter.hasNext()) {
				BldEnergyConsumptionDaily data = dailyIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);					
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("15min")) {
			List<BldEnergyConsumption15min> energy15minList = bldEcDao.getBld15minEnergyData(bldId, fromDate, toDate); 		
			Iterator<BldEnergyConsumption15min> min15Iter = energy15minList.iterator();
			while(min15Iter.hasNext()) {
				BldEnergyConsumption15min data = min15Iter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);				
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("5min")) {
			HashMap<Date, Object[]> bldAggData = aggregateBld5minEnergyReadings(bldId, fromDate, toDate);
			String target = facilityManager.loadFacilityById(bldId).getName();
			Iterator<Date> dataIter = bldAggData.keySet().iterator();
			while(dataIter.hasNext()) {
				Date captureTime = dataIter.next();
				Object[] dataArr = bldAggData.get(captureTime);
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(dataArr, "5min", target, bldId, captureTime);
				sensorDataList.add(sensorData);
			}
		}
		return sensorDataList;
		
	} //end of method getBldAggregatedEnergyData
	
	private HashMap<Date, Object[]> aggregateBld5minEnergyReadings(long bldId, String fromDate, String toDate) {
		
		//get the em database, replica server, em floor id 
		List<FacilityEmMapping> emMappingList = facilityDao.getEmMappingsByFacilityId(bldId);
		Iterator<FacilityEmMapping> emIdIter = emMappingList.iterator();
		HashMap<Date, Object[]> bldAggData = new HashMap<Date, Object[]>();
		while(emIdIter.hasNext()) {
			FacilityEmMapping emMapping = emIdIter.next();
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emMapping.getEmId());	
			if(emInst != null){
				if(!emInst.getSppaEnabled()) {
					continue;
				}
				String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();			
				List<Object[]> aggList = bldEcDao.getEmBld5minEnergyReadings(emInst.getDatabaseName(), ip, 
						fromDate, toDate, emMapping.getEmFacilityId());
				System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + aggList.size());
				EcUtil.aggregateReadingsFromDB(bldAggData, aggList);			
			
			}
			}
		return bldAggData;
		
	} //end of method aggregateBld5minEnergyReadings
		
	public void aggregateBldHourlyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> bldEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<Object[]> emEnergyReadings = bldEcDao.getEmBldHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			EcUtil.aggregateReadingsFromDB(bldEnergyReadings, emInst.getId(), emEnergyReadings, 3, facilityManager);	
		}
		
		System.out.println("no. of buildings -- " + bldEnergyReadings.size());
		Iterator<Long> bldReadingsIter = bldEnergyReadings.keySet().iterator();
		while(bldReadingsIter.hasNext()) {
			Long bldId = bldReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = bldEnergyReadings.get(bldId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			System.out.println("no. of time rows for building " + bldId + "  ---- " + timeReadings.size());
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				BldEnergyConsumptionHourly ec = new BldEnergyConsumptionHourly();
				EcUtil.convertToStatsData(ec, custId, bldId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				bldEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateBldHourlyEnergyReadings

	public void aggregateBldDailyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> bldEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = bldEcDao.getEmBldDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(bldEnergyReadings, emInst.getId(), emEnergyReadings, 3, facilityManager);	
		}
		
		Iterator<Long> bldReadingsIter = bldEnergyReadings.keySet().iterator();
		while(bldReadingsIter.hasNext()) {
			Long bldId = bldReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = bldEnergyReadings.get(bldId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				BldEnergyConsumptionDaily ec = new BldEnergyConsumptionDaily();
				EcUtil.convertToStatsData(ec, custId, bldId, toDate, readings);
				bldEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateBldDailyEnergyReadings
	
	public void aggregateEmBld15minEnergyReadings(long emId, Date fromDate, Date toDate) {
		
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		HashMap<Long, HashMap<Date, Object[]>> bldEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		if(emInst != null){
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = bldEcDao.getEmBld15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(bldEnergyReadings, emInst.getId(), emEnergyReadings, 3, facilityManager);			
			
			Iterator<Long> bldReadingsIter = bldEnergyReadings.keySet().iterator();
			while(bldReadingsIter.hasNext()) {
				Long bldId = bldReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = bldEnergyReadings.get(bldId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					BldEnergyConsumption15min ec = new BldEnergyConsumption15min();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), bldId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					bldEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmBld15minEnergyReadings
	
	public void aggregateEmBldHourlyEnergyReadings(long emId, Date fromDate, Date toDate) {
		
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> bldEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = bldEcDao.getEmBldHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(bldEnergyReadings, emInst.getId(), emEnergyReadings, 3, facilityManager);			
			
			Iterator<Long> bldReadingsIter = bldEnergyReadings.keySet().iterator();
			while(bldReadingsIter.hasNext()) {
				Long bldId = bldReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = bldEnergyReadings.get(bldId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					BldEnergyConsumptionHourly ec = new BldEnergyConsumptionHourly();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), bldId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					bldEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmBldHourlyEnergyReadings

	public void aggregateEmBldDailyEnergyReadings(long emId, Date fromDate, Date toDate) {
	
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> bldEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = bldEcDao.getEmBldDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(bldEnergyReadings, emInst.getId(), emEnergyReadings, 3, facilityManager);			
			
			Iterator<Long> bldReadingsIter = bldEnergyReadings.keySet().iterator();
			while(bldReadingsIter.hasNext()) {
				Long bldId = bldReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = bldEnergyReadings.get(bldId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					BldEnergyConsumptionDaily ec = new BldEnergyConsumptionDaily();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), bldId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					bldEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmBldDailyEnergyReadings
	
	public void aggregateBld15minEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> bldEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = bldEcDao.getEmBld15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(bldEnergyReadings, emInst.getId(), emEnergyReadings, 3, facilityManager);	
		}
		
		Iterator<Long> bldReadingsIter = bldEnergyReadings.keySet().iterator();
		while(bldReadingsIter.hasNext()) {
			Long bldId = bldReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = bldEnergyReadings.get(bldId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				BldEnergyConsumption15min ec = new BldEnergyConsumption15min();
				EcUtil.convertToStatsData(ec, custId, bldId, toDate, readings);
				bldEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateBld15minEnergyReadings
	
} //end of class BldEnergyConsumptionManager