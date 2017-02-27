package com.emscloud.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmInstanceDao;
import com.emscloud.dao.EmStatsDao;
import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;

@Service("emInstanceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceManager {
	
	@Resource
	private EmInstanceDao		emInstanceDao;
	
	@Resource
	private EmStatsDao emStatsDao;

	public List<EmInstance> loadallEmInstances() {
		
		return emInstanceDao.loadAllEmInstances() ;
	}

	public EmInstance loadEmInstanceById(long id) {
		
		return emInstanceDao.loadEmInstanceById(id) ;
	}
	
	public List<EmInstance> loadEmInstanceByReplicaServerId(Long id) {
		
		return emInstanceDao.loadEmInstanceByReplicaServerId(id) ;
	}
	
	public EmInstance loadEmInstanceByMac(String mac) {
		
		return emInstanceDao.loadEmInstanceByMac(mac) ;
	}
	public List<EmInstance> loadEmInstancesByCustomerId(long id) {
		
		return emInstanceDao.loadEmInstancesByCustomerId(id) ;
	}
	
	public EmInstanceList loadUnRegEmInstances(String orderway, int offset, int limit) {
		
		return emInstanceDao.loadUnregEmInstances(orderway, offset, limit);
	}
	
	public List<EmStats> loadEmStatsByEmInstanceId(Long id){
		return emStatsDao.loadEmStatsByEmInstanceId(id);
	}
	
	public List<EmStats> loadEmStatsByEmInstanceId(Long id,int offset, int limit){
		return emStatsDao.loadEmStatsByEmInstanceId(id,offset,limit);
	}
	
	public EmStatsList loadEmStatsListByEmInstanceId(Long id, String orderway, int offset, int limit){
		return emStatsDao.loadEmStatsListByEmInstanceId(id, orderway,offset, limit);
	}
	
	public EmInstanceList loadEmInstancesListByCustomerId(Long id, String orderway, int offset, int limit) {
		
		return emInstanceDao.loadEmInstanceListByCustomerId(id, orderway, offset, limit);
	}
	
	public EmInstance saveOrUpdate(EmInstance instance) {		
		return emInstanceDao.saveOrUpdate(instance) ;	
	//	replicaServerConnectionTemplate
	//			.executeGet(ReplicaServerWebserviceUrl.CREATE_DATABASE, instance.getReplicaServer().getIp());

		//TODO: Disable the database creation for now
	//	DatabaseUtil.createDatabase(instance.getDatabaseName());
	}
	
	public void delete(Long id)
	{
		emInstanceDao.deleteById(id);
	}

	
	public String getHelathOfEmInstance(EmStats latestEmStats){
		
		long serverCurrentTime =  new Date().getTime();
		long connectivityDifference = ((serverCurrentTime - latestEmStats.getCaptureAt().getTime())/1000)/60;
		String status="Ok";
		if(connectivityDifference > 15) // greater than 15 minutes
			status = "Unreachable for "+ connectivityDifference+" mins";
		else if(connectivityDifference <= 15 && latestEmStats.getCpuPercentage() > 70) // less than 15 mins
			status = "Connected";
		else if(connectivityDifference > 5 && connectivityDifference <= 15 ) // between 5 minutes and 15 minutes
			status = "Ok";
		return status;
	}
	public Customer getCustomer(String mac)
	{
		return emInstanceDao.getCustomer(mac) ;
	}
}
