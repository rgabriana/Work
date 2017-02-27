package com.emscloud.service;

import java.io.IOException;
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
import org.springframework.util.CollectionUtils;

import com.communication.types.DatabaseState;
import com.emscloud.dao.EmInstanceDao;
import com.emscloud.dao.EmStateDao;
import com.emscloud.dao.EmStatsDao;
import com.emscloud.dao.InventoryReportDao;
import com.emscloud.model.Customer;
import com.emscloud.model.EmHealthList;
import com.emscloud.model.EmHealthMonitor;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;
import com.emscloud.model.ReplicaServer;
import com.emscloud.model.UserCustomers;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.types.RoleType;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.DateUtil;
import com.emscloud.vo.EmHealthDataVO;
import com.emscloud.vo.SiteReportVo;

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
	
	@Resource
	private InventoryReportDao inventoryReportDao;
	
	@Resource
	UserCustomersManager userCustomersManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	@Resource
	ReplicaServerManager replicaServerManager;
	public List<EmInstance> loadAllEmInstances() {
		
		return emInstanceDao.loadAllEmInstances() ;
	}

	public List<EmInstance> getActiveEmInstanceWithDataSynch() {
		return emInstanceDao.getActiveEmInstanceWithDataSynch();
	}

	public List<EmInstance> getActiveEmInstance() {
		return emInstanceDao.getActiveEmInstance();
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
	
	public EmInstanceList loadUnRegEmInstances(String orderBy, String orderWay, Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		
		return emInstanceDao.loadUnregEmInstances(orderBy, orderWay, bSearch, searchField, searchString, searchOper, offset, limit);
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
	
	public EmInstanceList loadEmInstancesListByCustomerId(Long id, String orderby,String orderway, 
			Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		
		return emInstanceDao.loadEmInstanceListByCustomerId(id, orderby, orderway, bSearch, searchField, searchString, searchOper, offset, limit);
	}
	
	public EmInstance saveOrUpdate(EmInstance instance) {
		if(instance.getLastSuccessfulSyncTime() == null) {
			instance.setLastSuccessfulSyncTime(new Date());
		}
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

	public EmHealthList getEMHealthDataVOList(int page,String orderby,String orderway, String searchField, String searchString, 
			String searchOper, int offset, int limit) {
		EmHealthList emHealthList = new EmHealthList();
		emHealthList.setPage(page);
		
		EmInstanceList oEmInstList = null;
		if (emsAuthContext.getCurrentUserRoleType().getName().equals(RoleType.Admin.getName())) {
			oEmInstList = emInstanceDao.getHealthOfEmInstancesWithDataSynch(orderby,
					orderway, searchField, searchString, searchOper, offset, limit);
		}else{
			List<Long> cList = new ArrayList<Long>();
			List<UserCustomers> uCustomers = userCustomersManager.loadUserCustomersByUserId(emsAuthContext.getUserId());
			for (Iterator<UserCustomers> iterator = uCustomers.iterator(); iterator.hasNext();) {
				UserCustomers userCustomers = (UserCustomers) iterator.next();
				cList.add(userCustomers.getCustomer().getId());
			}
			oEmInstList = emInstanceDao.getHealthOfEmInstancesWithDataSynchByCustomerList(cList,orderby,
					orderway, searchField, searchString, searchOper, offset, limit);
		}
		
		
		if(oEmInstList != null && !CollectionUtils.isEmpty(oEmInstList.getEmInsts()))
		{
			emHealthList.setTotal(oEmInstList.getRecords());
			List<EmHealthDataVO> emHealthDataVOList = new ArrayList<EmHealthDataVO>();
			
			Date now = new Date();
			String dateFormat = "yyyy-MM-dd HH:mm";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
			
			for(EmInstance emInstance : oEmInstList.getEmInsts()){
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
				
				Date lastDataSynchConnectivity = emInstance.getLastSuccessfulSyncTime();	
				if(lastDataSynchConnectivity != null) {
					Long dataSynchDifferenceInMinutes = DateUtil.getDateDiffInMinutes(lastCallHomeConnectivity, now);
					emHealthDataVO.setLastDataSynchConnectivity(simpleDateFormat.format(lastDataSynchConnectivity));
					emHealthDataVO.setLastDataSynchConnectivityInMinutes(dataSynchDifferenceInMinutes.toString());
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
				else {
					SiteReportVo siteReportVo = inventoryReportDao.getAggregatedInventoryReport(emInstance.getDatabaseName(), emInstance.getReplicaServer().getInternalIp());
                    if (siteReportVo != null) {
                    	emHealthDataVO.setGatewaysTotal(siteReportVo.getGatewayCount().intValue());
                    	emHealthDataVO.setSensorsTotal(siteReportVo.getSensorCount().intValue());
                    	emHealthDataVO.setGatewaysCriticalNo(0);
    					emHealthDataVO.setGatewaysUnderObservationNo(0);
    					emHealthDataVO.setSensorsCriticalNo(0);
    					emHealthDataVO.setSensorsUnderObservationNo(0);
                    }
				}
				
				emHealthDataVOList.add(emHealthDataVO);
			}
			emHealthList.setHealthDataVOList(emHealthDataVOList);
		}
		return emHealthList;
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
	
	public void generateSppaCert(long emId) throws IOException {
        Runtime rt = Runtime.getRuntime(); 
        String[] cmd = {"php","/home/enlighted/utils/generateSPPACerts.php"};
        try {
        	final Process pr = rt.exec("sudo php /home/enlighted/utils/generateSPPACerts_SingleEM.php "+emId);
        	CommonUtils.readStreamOfProcess(pr);
			pr.waitFor(); 
            Thread.sleep(1500);
            logger.error("Command Successfuly fired to generate SPPACerts");
        } catch (InterruptedException e) {
            logger.error("Command Failed to generate SPPACerts", e);
        }
    }

	public List<EmInstance> loadAllEmInstance() {
		
		return emInstanceDao.loadAllEmInstance() ;
	}
	public EmInstance getEmInstance(long id) {
    	return emInstanceDao.getEmInstance(id);
    }	
	public List<Object[]> getEmCountByReplicaServer()
	{
		return emInstanceDao.getEmCountByReplicaServer();
	}
	
	public List<ReplicaServer> getAllReplicaServerWithEMCount()
	{
		List<Object[]> emCountobjList = getEmCountByReplicaServer();
		List<ReplicaServer> replicaServerList = replicaServerManager.getAllReplicaServers();
		Iterator<ReplicaServer> replicaSeverListItr = replicaServerList.iterator();
		if(replicaServerList!=null && replicaServerList.size()>0)
		{
			while(replicaSeverListItr.hasNext())
			{
				ReplicaServer replicaServerObj = replicaSeverListItr.next();
				Iterator<Object[]> emCountListItr = emCountobjList.iterator();
				if(emCountobjList!=null && emCountobjList.size()>0)
				{
					while(emCountListItr.hasNext())
					{
						Object[] emCountObj = emCountListItr.next();
						Long emReplicaId = (Long) emCountObj[0];
						Long emCount = (Long) emCountObj[1];
						Long replicaId = replicaServerObj.getId();
						if(replicaId.compareTo(emReplicaId)==0)
						{
							replicaServerObj.setEmCount(emCount);
							break;
						}
					}
				}
			}
		}
		return replicaServerList;
	}
	
	public String loadKeyByMac(String mac) {
		return emInstanceDao.loadKeyByMac(mac.replaceAll(":", ""));
	}
	
	public void evict(EmInstance em) {
		emInstanceDao.evict(em);
	}
	
	public String updatePauseResumeStatus(long id, Boolean status) {
		EmInstance emInstance = emInstanceDao.getEmInstance(id);
		if(emInstance!=null){
			emInstance.setPauseSyncStatus(status);
			if(!status && emInstance.getLatestEmStateId() != null) {
				EmState emState = emStateDao.loadEmStateById(emInstance.getLatestEmStateId());
				if(emState.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.SYNC_FAILED.getName())) {
					emStateDao.resetPreviousFlag(id);
				}
			}
		}
		emInstanceDao.saveOrUpdate(emInstance);
						
		return "success";		
	}
	
	public boolean isSyncPaused(String mac) {
		EmInstance emInstance = emInstanceDao.loadEmInstanceByMac(mac);
		if(emInstance != null) {
			return emInstance.getPauseSyncStatus();
		}
		else {
			return false;
		}
	}
}
