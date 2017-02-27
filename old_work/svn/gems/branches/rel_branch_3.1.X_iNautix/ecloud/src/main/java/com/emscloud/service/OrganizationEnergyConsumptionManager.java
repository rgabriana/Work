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
import com.emscloud.dao.OrganizationEnergyConsumptionDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.OrganizationEnergyConsumption15min;
import com.emscloud.model.OrganizationEnergyConsumptionDaily;
import com.emscloud.model.OrganizationEnergyConsumptionHourly;
import com.emscloud.util.EcUtil;
import com.emscloud.vo.AggregatedSensorData;

@Service("organizationEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class OrganizationEnergyConsumptionManager {
	
	@Resource
	private OrganizationEnergyConsumptionDao organizationEcDao;	
	@Resource
	ReplicaServerManager replicaServerManager ;
	@Resource
	FacilityDao facilityDao;	
	@Resource
	EmInstanceManager emInstanceManger;	
	@Resource
	CustomerManager custManager;
	@Resource
	FacilityManager facilityManager;
	  	
	public OrganizationEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub		
	}
	
	public List<AggregatedSensorData> getOrganizationAggregatedEnergyData(long orgId, String timeSpan,
			String fromDate, String toDate) {
		
		ArrayList<AggregatedSensorData> sensorDataList = new ArrayList<AggregatedSensorData>();
		if(timeSpan.equals("hourly")) {
			List<OrganizationEnergyConsumptionHourly> orgEnergyList = organizationEcDao.getOrganizationHourlyEnergyData( 
					orgId, fromDate, toDate);								
			Iterator<OrganizationEnergyConsumptionHourly> orgIter = orgEnergyList.iterator();
			while(orgIter.hasNext()) {
				OrganizationEnergyConsumptionHourly data = orgIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName());
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("daily")) {
			List<OrganizationEnergyConsumptionDaily> energyDailyList = organizationEcDao.getOrganizationDailyEnergyData( 
					orgId, fromDate, toDate); 		
			Iterator<OrganizationEnergyConsumptionDaily> dailyIter = energyDailyList.iterator();
			while(dailyIter.hasNext()) {
				OrganizationEnergyConsumptionDaily data = dailyIter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName());					
				sensorDataList.add(sensorData);
			}
		} else if(timeSpan.equals("15min")) {
			List<OrganizationEnergyConsumption15min> energy15minList = organizationEcDao.getOrganization15minEnergyData(orgId, 
					fromDate, toDate); 		
			Iterator<OrganizationEnergyConsumption15min> min15Iter = energy15minList.iterator();
			while(min15Iter.hasNext()) {
				OrganizationEnergyConsumption15min data = min15Iter.next();
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(data, timeSpan,
						facilityManager.loadFacilityById(data.getLevelId()).getName());
				sensorDataList.add(sensorData);
			}
		}	else if(timeSpan.equals("5min")) {
			HashMap<Date, Object[]> orgAggData = aggregateOrg5minEnergyReadings(orgId, fromDate, toDate);
			String target = facilityManager.loadFacilityById(orgId).getName();
			Iterator<Date> dataIter = orgAggData.keySet().iterator();
			while(dataIter.hasNext()) {
				Date captureTime = dataIter.next();
				Object[] dataArr = orgAggData.get(captureTime);
				AggregatedSensorData sensorData = EcUtil.convertToAggregatedData(dataArr, "5min", target, orgId, captureTime);
				sensorDataList.add(sensorData);
			}
		}
		return sensorDataList;
		
	} //end of method getOrganizationAggregatedEnergyData
	
	private HashMap<Date, Object[]> aggregateOrg5minEnergyReadings(long orgId, String fromDate, String toDate) {
		
		//get the em database, replica server, em floor id 
		List<FacilityEmMapping> emMappingList = facilityDao.getEmMappingsByFacilityId(orgId);
		Iterator<FacilityEmMapping> emIdIter = emMappingList.iterator();
		HashMap<Date, Object[]> orgAggData = new HashMap<Date, Object[]>();
		while(emIdIter.hasNext()) {
			FacilityEmMapping emMapping = emIdIter.next();
			EmInstance emInst = emInstanceManger.loadEmInstanceById(emMapping.getEmId());			
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();			
			List<Object[]> aggList = organizationEcDao.getEmOrg5minEnergyReadings(emInst.getDatabaseName(), ip, 
					fromDate, toDate, emMapping.getEmFacilityId());
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + aggList.size());
			EcUtil.aggregateReadingsFromDB(orgAggData, aggList);			
		}
		return orgAggData;
		
	} //end of method aggregateOrg5minEnergyReadings
	
	public void aggregateOrganizationHourlyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> organizationEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			long custOrgId = facilityDao.getOrganizationIdOfCustomer(custId);
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<Object[]> emEnergyReadings = organizationEcDao.getEmOrganizationHourlyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate, custOrgId);
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			EcUtil.aggregateOrgReadingsFromDB(organizationEnergyReadings, emInst.getId(), emEnergyReadings, custOrgId, 
					facilityManager);
		}
		
		System.out.println("no. of organizations -- " + organizationEnergyReadings.size());
		Iterator<Long> organizationReadingsIter = organizationEnergyReadings.keySet().iterator();
		while(organizationReadingsIter.hasNext()) {
			Long organizationId = organizationReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = organizationEnergyReadings.get(organizationId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			System.out.println("no. of time rows for organization " + organizationId + "  ---- " +timeReadings.size());
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				OrganizationEnergyConsumptionHourly ec = new OrganizationEnergyConsumptionHourly();
				EcUtil.convertToStatsData(ec, custId, organizationId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				organizationEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateOrganizationHourlyEnergyReadings

	public void aggregateOrganizationDailyEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> organizationEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			long custOrgId = facilityDao.getOrganizationIdOfCustomer(custId);
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = organizationEcDao.getEmOrganizationDailyEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate, custOrgId);
			EcUtil.aggregateOrgReadingsFromDB(organizationEnergyReadings, emInst.getId(), emEnergyReadings, custOrgId, facilityManager);			
		}
		
		Iterator<Long> organizationReadingsIter = organizationEnergyReadings.keySet().iterator();
		while(organizationReadingsIter.hasNext()) {
			Long organizationId = organizationReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = organizationEnergyReadings.get(organizationId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				OrganizationEnergyConsumptionDaily ec = new OrganizationEnergyConsumptionDaily();
				EcUtil.convertToStatsData(ec, custId, organizationId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				organizationEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateOrganizationDailyEnergyReadings
	
	public void aggregateOrganization15minEnergyReadings(long custId, Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();	
		HashMap<Long, HashMap<Date, Object[]>> organizationEnergyReadings = new HashMap<Long, HashMap<Date, Object[]>>();
		EmInstance emInst = null;
		while(emIter.hasNext()) {
			emInst = emIter.next();
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			long custOrgId = facilityDao.getOrganizationIdOfCustomer(custId);
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			ArrayList<Object[]> emEnergyReadings = organizationEcDao.getEmOrganization15minEnergyReadings(emInst.getDatabaseName(), 
					ip, fromDate, toDate, custOrgId);
			EcUtil.aggregateOrgReadingsFromDB(organizationEnergyReadings, emInst.getId(), emEnergyReadings, custOrgId, facilityManager);			
		}
		
		Iterator<Long> organizationReadingsIter = organizationEnergyReadings.keySet().iterator();
		while(organizationReadingsIter.hasNext()) {
			Long organizationId = organizationReadingsIter.next();
			HashMap<Date, Object[]> timeReadings = organizationEnergyReadings.get(organizationId);
			Iterator<Date> timeIter = timeReadings.keySet().iterator();
			while(timeIter.hasNext()) {
				Object[] readings = timeReadings.get(timeIter.next());
				OrganizationEnergyConsumption15min ec = new OrganizationEnergyConsumption15min();
				EcUtil.convertToStatsData(ec, custId, organizationId, toDate, readings);
				System.out.println("saving -- " + ec.getCaptureAt());
				organizationEcDao.saveOrUpdate(ec);
			}
		}
		
	} //end of method aggregateOrganization15minEnergyReadings
	
} //end of class OrganizationEnergyConsumptionManager
