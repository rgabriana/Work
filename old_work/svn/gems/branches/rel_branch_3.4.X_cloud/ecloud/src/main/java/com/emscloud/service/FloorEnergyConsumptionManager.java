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

import com.emscloud.dao.FacilityDao;
import com.emscloud.dao.FloorEnergyConsumptionDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FloorEnergyConsumption15min;
import com.emscloud.model.FloorEnergyConsumptionDaily;
import com.emscloud.model.FloorEnergyConsumptionHourly;
import com.emscloud.util.EcUtil;
import com.emscloud.vo.AggregatedSensorData;
import com.emscloud.vo.SensorData;

@Service("floorEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorEnergyConsumptionManager {
	
	@Resource
	private FloorEnergyConsumptionDao floorEcDao;	
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
		
	public FloorEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub	
	}
				
	public List<AggregatedSensorData> getFloorAggregatedEnergyData(long floorId, String timeSpan,
			String fromDate, String toDate, int tzOffset) {
		
		ArrayList<AggregatedSensorData> sensorDataList = new ArrayList<AggregatedSensorData>();
		if(timeSpan.equals("hourly")) {
			List<FloorEnergyConsumptionHourly> energyList = floorEcDao.getFloorHourlyEnergyData(floorId, fromDate, toDate); 		
			Iterator<FloorEnergyConsumptionHourly> iter = energyList.iterator();
			while(iter.hasNext()) {
				FloorEnergyConsumptionHourly data = iter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan, 
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);					
				sensorDataList.add(sensorData);			
			}
		} else if(timeSpan.equals("daily")) {
			List<FloorEnergyConsumptionDaily> energyDailyList = floorEcDao.getFloorDailyEnergyData(floorId, fromDate, toDate); 		
			Iterator<FloorEnergyConsumptionDaily> dailyIter = energyDailyList.iterator();
			while(dailyIter.hasNext()) {
				FloorEnergyConsumptionDaily data = dailyIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);					
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("15min")) {
			List<FloorEnergyConsumption15min> energy15minList = floorEcDao.getFloor15minEnergyData(floorId, fromDate, toDate); 		
			Iterator<FloorEnergyConsumption15min> min15Iter = energy15minList.iterator();
			while(min15Iter.hasNext()) {
				FloorEnergyConsumption15min data = min15Iter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName(), tzOffset);					
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("5min")) {
			HashMap<Date, Object[]> floorAggData = aggregateFloor5minEnergyReadings(floorId, fromDate, toDate);
			String target = facilityManager.loadFacilityById(floorId).getName();
			Iterator<Date> dataIter = floorAggData.keySet().iterator();
			while(dataIter.hasNext()) {
				Date captureTime = dataIter.next();
				Object[] dataArr = floorAggData.get(captureTime);
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(dataArr, "5min", target, floorId, captureTime);
				sensorDataList.add(sensorData);
			}
		}
		return sensorDataList;
		
	} //end of method getFloorAggregatedEnergyData
	
	private HashMap<Date, Object[]> aggregateFloor5minEnergyReadings(long floorId, String fromDate, String toDate) {
		
		//get the em database, replica server, em floor id 
		List<FacilityEmMapping> emMappingList = facilityDao.getEmMappingsByFacilityId(floorId);
		Iterator<FacilityEmMapping> emIdIter = emMappingList.iterator();
		HashMap<Date, Object[]> floorAggData = new HashMap<Date, Object[]>();
		while(emIdIter.hasNext()) {
			FacilityEmMapping emMapping = emIdIter.next();
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emMapping.getEmId());			
			if(emInst != null){
				if(!emInst.getSppaEnabled()) {
					continue;
				}
				String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();			
				List<Object[]> aggList = floorEcDao.getEmFloor5minEnergyReadings(emInst.getDatabaseName(), ip, 
						fromDate, toDate, emMapping.getEmFacilityId());
				System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + aggList.size());
				EcUtil.aggregateReadingsFromDB(floorAggData, aggList);
			}						
		}
		return floorAggData;
		
	} //end of method aggregateFloor5minEnergyReadings
		
	public List<SensorData> getFloorSensorData(long floorId, String fromDate, String toDate, String attributes) {
				
		List<SensorData> sensorDataList = new ArrayList<SensorData>();
		//get the em database, replica server, em floor id 
		List<FacilityEmMapping> emMappingList = facilityDao.getEmMappingsByFacilityId(floorId);
		Iterator<FacilityEmMapping> emIdIter = emMappingList.iterator();
		while(emIdIter.hasNext()) {
			FacilityEmMapping emMapping = emIdIter.next();
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emMapping.getEmId());
			if(emInst != null){
				String replicaIp = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
				sensorDataList.addAll(floorEcDao.getEmFloorSensorRawEnergyReadings(emInst.getDatabaseName(), replicaIp, 
						fromDate, toDate, emMapping.getEmFacilityId(), attributes));
			}			
		}	
		return sensorDataList;
				
	} //end of method getFloorSensorData
		
	public void aggregateFloorHourlyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getEmFloorHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			EcUtil.aggregateReadingsFromDB(floorEnergyReadings, emInst.getId(), emEnergyReadings, 4, facilityManager);		
		}		
		
		System.out.println("no. of floors -- " + floorEnergyReadings.size());
		Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
		while(floorReadingsIter.hasNext()) {
			Long floorId = floorReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			System.out.println("no. of time rows for floor " + floorId + "  ---- " +timeReadings.size());
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				FloorEnergyConsumptionHourly ec = new FloorEnergyConsumptionHourly();
				EcUtil.convertToStatsData(ec, custId, floorId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				floorEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateFloorHourlyEnergyReadings
	
	public void aggregateFloorDailyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getEmFloorDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(floorEnergyReadings, emInst.getId(), emEnergyReadings, 4, facilityManager);
		}
		
		Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
		while(floorReadingsIter.hasNext()) {
			Long floorId = floorReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				FloorEnergyConsumptionDaily ec = new FloorEnergyConsumptionDaily();
				EcUtil.convertToStatsData(ec, custId, floorId, toDate, readings);				
				System.out.println("saving -- " + ec.getCaptureAt());
				floorEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateFloorDailyEnergyReadings
	
	public void aggregateEmFloor15minEnergyReadings(long emId, Date fromDate, Date toDate) {
		
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getEmFloor15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(floorEnergyReadings, emInst.getId(), emEnergyReadings, 4, facilityManager);			
			
			Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
			while(floorReadingsIter.hasNext()) {
				Long floorId = floorReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					FloorEnergyConsumption15min ec = new FloorEnergyConsumption15min();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), floorId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					floorEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmFloor15minEnergyReadings
	
	public void aggregateEmFloorHourlyEnergyReadings(long emId, Date fromDate, Date toDate) {
		
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getEmFloorHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(floorEnergyReadings, emInst.getId(), emEnergyReadings, 4, facilityManager);			
			
			Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
			while(floorReadingsIter.hasNext()) {
				Long floorId = floorReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					FloorEnergyConsumptionHourly ec = new FloorEnergyConsumptionHourly();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), floorId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					floorEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmFloorHourlyEnergyReadings

	public void aggregateEmFloorDailyEnergyReadings(long emId, Date fromDate, Date toDate) {
	
		//get em instance
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst != null){
			HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getEmFloorDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(floorEnergyReadings, emInst.getId(), emEnergyReadings, 4, facilityManager);			
			
			Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
			while(floorReadingsIter.hasNext()) {
				Long floorId = floorReadingsIter.next();
				HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
				Iterator<Date> timeIter = timeReadings.keySet().iterator();
				while(timeIter.hasNext()) {
					Object[] readings = timeReadings.get(timeIter.next());
					FloorEnergyConsumptionDaily ec = new FloorEnergyConsumptionDaily();
					EcUtil.convertToStatsData(ec, emInst.getCustomer().getId(), floorId, toDate, readings);
					System.out.println("saving -- " + ec.getCaptureAt());
					floorEcDao.saveOrUpdate(ec);
				}
			}
		}
		
		
	} //end of method aggregateEmFloorDailyEnergyReadings
	
	public void aggregateFloor15minEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> floorEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = floorEcDao.getEmFloor15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate);
			EcUtil.aggregateReadingsFromDB(floorEnergyReadings, emInst.getId(), emEnergyReadings, 4, facilityManager);			
		}
		
		Iterator<Long> floorReadingsIter = floorEnergyReadings.keySet().iterator();
		while(floorReadingsIter.hasNext()) {
			Long floorId = floorReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = floorEnergyReadings.get(floorId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				FloorEnergyConsumption15min ec = new FloorEnergyConsumption15min();
				EcUtil.convertToStatsData(ec, custId, floorId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				floorEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateFloor15minEnergyReadings
	
} //end of class FloorEnergyConsumptionManager
