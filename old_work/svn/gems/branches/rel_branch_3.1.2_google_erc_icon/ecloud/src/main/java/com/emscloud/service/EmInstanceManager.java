package com.emscloud.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.DatabaseState;
import com.emscloud.dao.EmInstanceDao;
import com.emscloud.dao.EmStateDao;
import com.emscloud.dao.EmStatsDao;
import com.emscloud.model.Customer;
import com.emscloud.model.EmHealthMonitor;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.DateUtil;
import com.emscloud.vo.EmHealthDataVO;

@Service("emInstanceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceManager {
	
	static final Logger logger = Logger.getLogger(EmInstanceManager.class.getName());
	
	@Resource
	private EmInstanceDao		emInstanceDao;
	
	@Resource
	private EmStatsDao emStatsDao;
	
	@Resource 
	private EmStateDao emStateDao;

	public List<EmInstance> loadallEmInstances() {
		
		return emInstanceDao.loadAllEmInstances() ;
	}

	public List<EmInstance> getActiveEmInstanceWithDataSynch() {
		return emInstanceDao.getActiveEmInstanceWithDataSynch();
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

	public List<EmHealthDataVO> getEMHealthDataVOList() {
		List<EmInstance> emInstanceList = emInstanceDao.GetHealthOfEmInstancesWithDataSynch();
		
		List<EmHealthDataVO> emHealthDataVOList = new ArrayList(emInstanceList.size());
		
		Date now = new Date();
		String synchDataDateFormat = "y-M-d H:m:s";
		String dateFormat = "yyyy-MM-dd HH:mm";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		
		for(EmInstance emInstance : emInstanceList){
			EmHealthDataVO emHealthDataVO = new EmHealthDataVO();
			emHealthDataVO.setEmInstanceId(emInstance.getId());
			emHealthDataVO.setEmInstanceName(emInstance.getName());
			emHealthDataVO.setCustomerName(emInstance.getCustomer().getName());
			
			Date lastCallHomeConnectivity = emInstance.getLastConnectivityAt();
			if(lastCallHomeConnectivity != null){			
				Long lastCallHomeInMinutes = DateUtil.getDateDiffInMinutes(lastCallHomeConnectivity,now);
			    emHealthDataVO.setLastEmConnectivity(simpleDateFormat.format(lastCallHomeConnectivity));
			    emHealthDataVO.setEmConnectivityInMinutes(lastCallHomeInMinutes.toString());
			}
			
			
			emHealthDataVO.setLastDataSynchConnectivity(simpleDateFormat.format(emInstance.getLastConnectivityAt()));
			
			Long emStateId = emInstance.getLatestEmStateId();
			EmState emState = emStateDao.loadEmStateById(emStateId);
			
			if(emState != null){
				Date lastDataSynchConnectivity = emState.getSetTime();
				
				if(emState.getDatabaseState() == DatabaseState.SYNC_READY){
					try{
						String log = emState.getLog();
						String logTokens[] = log.split("@");
					
						lastDataSynchConnectivity = DateUtil.parseStringWithFormat(logTokens[1],synchDataDateFormat);					
						Long dataSynchDifferenceInMinutes = DateUtil.getDateDiffInMinutes(lastCallHomeConnectivity, now);
						emHealthDataVO.setLastDataSynchConnectivity(simpleDateFormat.format(lastDataSynchConnectivity));
						emHealthDataVO.setLastDataSynchConnectivityInMinutes(dataSynchDifferenceInMinutes.toString());
					}catch(Exception e){
						logger.error(e.getMessage());
					}
					
				}else{
					emHealthDataVO.setLastDataSynchConnectivityInMinutes(emState.getDatabaseState().toString());
				}
			}
			
			EmHealthMonitor latestEmsHealthMonitor= emInstance.getLatestEmsHealthMonitor();
			if (latestEmsHealthMonitor != null) {
				emHealthDataVO.setGatewaysTotal(latestEmsHealthMonitor.getGatewaysTotal());
				emHealthDataVO.setGatewaysCriticalNo(latestEmsHealthMonitor.getGatewaysCritical());
				emHealthDataVO.setGatewaysUnderObservationNo(latestEmsHealthMonitor.getGatewaysUnderObservation());
				emHealthDataVO.setSensorsTotal(latestEmsHealthMonitor.getSensorsTotal());
				emHealthDataVO.setSensorsCriticalNo(latestEmsHealthMonitor.getSensorsCritical());
				emHealthDataVO.setSensorsUnderObservationNo(latestEmsHealthMonitor.getSensorsUnderObservation());
			}
			
			emHealthDataVOList.add(emHealthDataVO);
		}
		return emHealthDataVOList;
	}

	public void assignSshTunnelPortToUnassignedEM() {
		List<EmInstance> emList= emInstanceDao.loadAllEmInstances();
		if(!emList.isEmpty()&&emList!=null ) {
			Iterator<EmInstance> iterate = emList.iterator();
			while(iterate.hasNext())
			{
				EmInstance em = (EmInstance) iterate.next() ;
				logger.info("Assigning to Em " + em.getName());
				if(em.getActive()&&em.getSshTunnelPort().longValue()==0l)
				{
					em.setSshTunnelPort((long) CommonUtils.getRandomPort());
					saveOrUpdate(em);
					logger.info("Assigned ssh tunnel port " + em.getSshTunnelPort() + " to " + em.getName() + " with mac id " + em.getMacId());
				}
			}
		}
		
	}
}
