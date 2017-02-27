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
import com.emscloud.model.Site;
import com.emscloud.vo.AggregatedEmData;
import com.emscloud.vo.RawEnergyData;

@Service("emEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EMEnergyConsumptionManager {
	
	@Resource
	private EMEnergyConsumptionDao emEcDao;	
	@Resource
	ReplicaServerManager replicaServerManager ;	
	@Resource
	EmInstanceManager emInstanceManger;
	@Resource
	SiteManager siteManager;
		
	public EMEnergyConsumptionManager() {
		// TODO Auto-generated constructor stub	
	}
	
	public Date getMaxCaptureAtForEM(String dbName, String replicaIp) {
		return emEcDao.getMaxCaptureAtForEM(dbName, replicaIp);
	}
		
	public List<RawEnergyData> getEm5minEnergyReadings(long emId, String fromDate, String toDate) {
		List<RawEnergyData> emEnergyReadings = null;	
		EmInstance emInst = emInstanceManger.loadEmInstanceById(emId);
		if(emInst!= null){
			String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
			//System.out.println("replica server -- " + ip);
			emEnergyReadings = emEcDao.getEmEnergyRawEnergyReadings(emInst.getDatabaseName(), ip, fromDate, toDate);
			//System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
			
		}
		return emEnergyReadings;
			
	} //end of method getFloorSensorData
		
	public List<AggregatedEmData> getEmHourlyEnergyReadings(long custId, String fromDate, String toDate) {
		
		//get all the sites
		List<Site> sites = siteManager.loadSitesByCustomer(custId);
		Iterator<Site> siteIter = sites.iterator();
		List<AggregatedEmData> emDataList = new ArrayList<AggregatedEmData>();
		while(siteIter.hasNext()) {
			Site site = siteIter.next();
			List<Long> emIdList = siteManager.getSiteEms(site.getId());
			Iterator<Long> emIdIter = emIdList.iterator();
			while(emIdIter.hasNext()) {
				EmInstance emInst = emInstanceManger.loadEmInstanceById(emIdIter.next());
				if(emInst!= null){
					if(!emInst.getSppaEnabled()) {
						continue;
					}
					String ip = replicaServerManager.getReplicaServersbyId(emInst.getReplicaServer().getId()).getInternalIp();
					System.out.println("replica server -- " + ip);
					ArrayList<AggregatedEmData> emEnergyReadings = emEcDao.getEmHourlyEnergyReadings(
							emInst.getDatabaseName(), ip, fromDate, toDate, site.getName(), emInst.getName());
					System.out.println("no. of rows from db( " + emInst.getDatabaseName() + ") -- " + emEnergyReadings.size());
					emDataList.addAll(emEnergyReadings);
				}				
			}
		}		
		return emDataList;
				
	} //end of method getEmHourlyEnergyReadings
	
} //end of class FloorEnergyConsumptionManager
