package com.emscloud.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EMEnergyConsumptionDao;
import com.emscloud.dao.FacilityDao;
import com.emscloud.model.EmInstance;
import com.emscloud.vo.AggregatedEmData;

@Service("emEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EMEnergyConsumptionManager {
	
	@Resource
	private EMEnergyConsumptionDao emEcDao;	
	@Resource
	ReplicaServerManager replicaServerManager ;	
	@Resource
	EmInstanceManager emInstanceManger;
		
	public EMEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub	
	}
		
	public List<AggregatedEmData> getEm5minEnergyReadings(long custId, String fromDate, String toDate) {
				
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();		
		EmInstance emInst = null;
		List<AggregatedEmData> emDataList = new ArrayList<AggregatedEmData>();
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<AggregatedEmData> emEnergyReadings = emEcDao.getEm5minEnergyReadings(
					emInst.getDatabaseName(), ip, fromDate, toDate, "", emInst.getName());
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			emDataList.addAll(emEnergyReadings);		
		}		
		return emDataList;
				
	} //end of method getFloorSensorData
		
	public List<AggregatedEmData> getEmHourlyEnergyReadings(long custId, String fromDate, String toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadEmInstancesByCustomerId(custId);
		Iterator<EmInstance> emIter = emInstances.iterator();		
		EmInstance emInst = null;
		List<AggregatedEmData> emDataList = new ArrayList<AggregatedEmData>();
		while(emIter.hasNext()) {
			emInst = emIter.next();		
			if(!emInst.getSppaEnabled()) {
				continue;
			}
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			System.out.println("replica server -- " + ip);
			ArrayList<AggregatedEmData> emEnergyReadings = emEcDao.getEmHourlyEnergyReadings(
					emInst.getDatabaseName(), ip, fromDate, toDate, "", emInst.getName());
			System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			emDataList.addAll(emEnergyReadings);		
		}		
		return emDataList;
				
	} //end of method getEmHourlyEnergyReadings
	
} //end of class FloorEnergyConsumptionManager
