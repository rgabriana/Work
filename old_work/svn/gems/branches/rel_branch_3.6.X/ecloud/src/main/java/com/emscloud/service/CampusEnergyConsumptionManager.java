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

import com.emscloud.dao.CampusEnergyConsumptionDao;
import com.emscloud.dao.FacilityDao;
import com.emscloud.model.CampusEnergyConsumption15min;
import com.emscloud.model.EmInstance;
import com.emscloud.model.CampusEnergyConsumptionDaily;
import com.emscloud.model.CampusEnergyConsumptionHourly;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.util.EcUtil;
import com.emscloud.vo.AggregatedEnergyData;
import com.emscloud.vo.AggregatedSensorData;

@Service("campusEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CampusEnergyConsumptionManager {
	
	@Resource
	private CampusEnergyConsumptionDao campusEcDao;	
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
	  	
	public CampusEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub		
	}
	
	public List<AggregatedEnergyData> getCampusAggregatedEnergyData(long campusId, String timeSpan,
			String fromDate, String toDate, int tzOffset) {
		
		ArrayList<AggregatedEnergyData> sensorDataList = new ArrayList<AggregatedEnergyData>();
		if(timeSpan.equals("hourly")) {
			List<CampusEnergyConsumptionHourly> campusEnergyList = campusEcDao.getCampusHourlyEnergyData(campusId, 
					fromDate, toDate);								
			Iterator<CampusEnergyConsumptionHourly> campusIter = campusEnergyList.iterator();
			while(campusIter.hasNext()) {
				CampusEnergyConsumptionHourly data = campusIter.next();
				AggregatedEnergyData sensorData = EcUtil.convertToAggregatedEnergyData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);				
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("daily")) {
			List<CampusEnergyConsumptionDaily> energyDailyList = campusEcDao.getCampusDailyEnergyData(campusId, 
					fromDate, toDate); 		
			Iterator<CampusEnergyConsumptionDaily> dailyIter = energyDailyList.iterator();
			while(dailyIter.hasNext()) {
				CampusEnergyConsumptionDaily data = dailyIter.next();
				AggregatedEnergyData sensorData = EcUtil.convertToAggregatedEnergyData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);					
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("15min")) {
			List<CampusEnergyConsumption15min> energy15minList = campusEcDao.getCampus15minEnergyData(campusId, 
					fromDate, toDate); 		
			Iterator<CampusEnergyConsumption15min> min15Iter = energy15minList.iterator();
			while(min15Iter.hasNext()) {
				CampusEnergyConsumption15min data = min15Iter.next();
				AggregatedEnergyData sensorData = EcUtil.convertToAggregatedEnergyData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);
				sensorDataList.add(sensorData);
			} 
		}
		return sensorDataList;
		
	} //end of method getCampusAggregatedEnergyData
	
	public List<AggregatedSensorData> getCampusAggregatedSensorData(long campusId, String timeSpan,
			String fromDate, String toDate, int tzOffset) {
		
		ArrayList<AggregatedSensorData> sensorDataList = new ArrayList<AggregatedSensorData>();
		if(timeSpan.equals("hourly")) {
			List<CampusEnergyConsumptionHourly> campusEnergyList = campusEcDao.getCampusHourlyEnergyData(campusId, 
					fromDate, toDate);								
			Iterator<CampusEnergyConsumptionHourly> campusIter = campusEnergyList.iterator();
			while(campusIter.hasNext()) {
				CampusEnergyConsumptionHourly data = campusIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);				
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("daily")) {
			List<CampusEnergyConsumptionDaily> energyDailyList = campusEcDao.getCampusDailyEnergyData(campusId, 
					fromDate, toDate); 		
			Iterator<CampusEnergyConsumptionDaily> dailyIter = energyDailyList.iterator();
			while(dailyIter.hasNext()) {
				CampusEnergyConsumptionDaily data = dailyIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);					
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("15min")) {
			List<CampusEnergyConsumption15min> energy15minList = campusEcDao.getCampus15minEnergyData(campusId, 
					fromDate, toDate); 		
			Iterator<CampusEnergyConsumption15min> min15Iter = energy15minList.iterator();
			while(min15Iter.hasNext()) {
				CampusEnergyConsumption15min data = min15Iter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);
				sensorDataList.add(sensorData);
			} 
		}	else if(timeSpan.equals("5min")) {
			HashMap<Date, Object[]> campusAggData = aggregateCampus5minEnergyReadings(campusId, fromDate, toDate);
			String target = facilityManager.loadFacilityById(campusId).getName();
			Iterator<Date> dataIter = campusAggData.keySet().iterator();
			while(dataIter.hasNext()) {
				Date captureTime = dataIter.next();
				Object[] dataArr = campusAggData.get(captureTime);
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(dataArr, "5min", target, campusId, captureTime);
				sensorDataList.add(sensorData);
			}
		}
		return sensorDataList;
		
	} //end of method getCampusAggregatedSensorData
	
	private HashMap<Date, Object[]> aggregateCampus5minEnergyReadings(long campusId, String fromDate, String toDate) {
		
		//get the em database, replica server, em floor id 
		List<FacilityEmMapping> emMappingList = facilityDao.getEmMappingsByFacilityId(campusId);
		Iterator<FacilityEmMapping> emIdIter = emMappingList.iterator();
		HashMap<Date, Object[]> campusAggData = new HashMap<Date, Object[]>();
		while(emIdIter.hasNext()) {
			FacilityEmMapping emMapping = emIdIter.next();
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emMapping.getEmId());			
			if(emInst != null){
				if(!emInst.getSppaEnabled()) {
					continue;
				}
				String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();			
				List<Object[]> aggList = campusEcDao.getEmCampus5minEnergyReadings(emInst.getDatabaseName(), ip, 
						fromDate, toDate, emMapping.getEmFacilityId());
				System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + aggList.size());
				EcUtil.aggregateReadingsFromDB(campusAggData, aggList);	
			}					
		}
		return campusAggData;
		
	} //end of method aggregateBld5minEnergyReadings
						
	public void aggregateCampusHourlyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> campusEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<Object[]> emEnergyReadings = campusEcDao.getEmCampusHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			EcUtil.aggregateReadingsFromDB(campusEnergyReadings, emInst.getId(), emEnergyReadings, 2, facilityManager);
		}
		
		System.out.println("no. of campuss -- " + campusEnergyReadings.size());
		Iterator<Long> campusReadingsIter = campusEnergyReadings.keySet().iterator();
		while(campusReadingsIter.hasNext()) {
			Long campusId = campusReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = campusEnergyReadings.get(campusId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			System.out.println("no. of time rows for campus " + campusId + "  ---- " +timeReadings.size());
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				CampusEnergyConsumptionHourly ec = new CampusEnergyConsumptionHourly();
				EcUtil.convertToStatsData(ec, custId, campusId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				campusEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateCampusHourlyEnergyReadings

	public void aggregateCampusDailyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> campusEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = campusEcDao.getEmCampusDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(campusEnergyReadings, emInst.getId(), emEnergyReadings, 2, facilityManager);	
		}
		
		Iterator<Long> campusReadingsIter = campusEnergyReadings.keySet().iterator();
		while(campusReadingsIter.hasNext()) {
			Long campusId = campusReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = campusEnergyReadings.get(campusId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				CampusEnergyConsumptionDaily ec = new CampusEnergyConsumptionDaily();
				EcUtil.convertToStatsData(ec, custId, campusId, toDate, readings);
				campusEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateCampusDailyEnergyReadings
	
	public void aggregateEmCampus15minEnergyReadings(long emId, Date fromDate, Date toDate) {
		
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> campusEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = campusEcDao.getEmCampus15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(campusEnergyReadings, emInst.getId(), emEnergyReadings, 2, facilityManager);			
			
			Iterator<Long> campusReadingsIter = campusEnergyReadings.keySet().iterator();
			while(campusReadingsIter.hasNext()) {
				Long campusId = campusReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = campusEnergyReadings.get(campusId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					CampusEnergyConsumption15min ec = new CampusEnergyConsumption15min();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), campusId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					campusEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmCampus15minEnergyReadings

	public void aggregateEmCampusDailyEnergyReadings(long emId, Date fromDate, Date toDate) {
	
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> campusEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = campusEcDao.getEmCampusDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(campusEnergyReadings, emInst.getId(), emEnergyReadings, 2, facilityManager);			
			
			Iterator<Long> campusReadingsIter = campusEnergyReadings.keySet().iterator();
			while(campusReadingsIter.hasNext()) {
				Long campusId = campusReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = campusEnergyReadings.get(campusId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					CampusEnergyConsumptionDaily ec = new CampusEnergyConsumptionDaily();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), campusId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					campusEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmCampusDailyEnergyReadings
	
	public void aggregateEmCampusHourlyEnergyReadings(long emId, Date fromDate, Date toDate) {
		
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> campusEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = campusEcDao.getEmCampusHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(campusEnergyReadings, emInst.getId(), emEnergyReadings, 2, facilityManager);			
			
			Iterator<Long> campusReadingsIter = campusEnergyReadings.keySet().iterator();
			while(campusReadingsIter.hasNext()) {
				Long campusId = campusReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = campusEnergyReadings.get(campusId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					CampusEnergyConsumptionHourly ec = new CampusEnergyConsumptionHourly();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), campusId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					campusEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmCampusHourlyEnergyReadings
	
	public void aggregateCampus15minEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> campusEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = campusEcDao.getEmCampus15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(campusEnergyReadings, emInst.getId(), emEnergyReadings, 2, facilityManager);	
		}
		
		Iterator<Long> campusReadingsIter = campusEnergyReadings.keySet().iterator();
		while(campusReadingsIter.hasNext()) {
			Long campusId = campusReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = campusEnergyReadings.get(campusId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				CampusEnergyConsumption15min ec = new CampusEnergyConsumption15min();
				EcUtil.convertToStatsData(ec, custId, campusId, toDate, readings);
				campusEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateCampus15minEnergyReadings
	
} //end of class CampusEnergyConsumptionManager
