package com.emscloud.service;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.AppInstanceDao;
import com.emscloud.model.AppInstance;
import com.emscloud.model.AppInstanceList;
import com.emscloud.model.Customer;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.util.CommonUtils;

@Service("appInstanceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class AppInstanceManager {
	
	static final Logger logger = Logger.getLogger(AppInstanceManager.class.getName());
	
	@Resource
	private AppInstanceDao		appInstanceDao;
	
	@Resource
	UserCustomersManager userCustomersManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;

	public List<AppInstance> loadAllAppInstances() {
		
		return appInstanceDao.loadAllAppInstances() ;
	}


	public AppInstance loadAppInstanceById(long id) {
		return appInstanceDao.loadAppInstanceById(id) ;
	}
	
	public AppInstance loadAppInstanceByMac(String mac) {
		
		return appInstanceDao.loadAppInstanceByMac(mac) ;
	}
	public List<AppInstance> loadAppInstancesByCustomerId(long id) {
		
		return appInstanceDao.loadAppInstancesByCustomerId(id) ;
	}
	
	public AppInstanceList loadUnRegAppInstances(String orderBy, String orderWay, Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		
		return appInstanceDao.loadUnregAppInstances(orderBy, orderWay, bSearch, searchField, searchString, searchOper, offset, limit);
	}
	
	public AppInstance saveOrUpdate(AppInstance instance) {
		return appInstanceDao.saveOrUpdate(instance) ;	
	}
	
	public void delete(Long id)
	{
		appInstanceDao.deleteById(id);
	}

	
	public Customer getCustomer(String mac)
	{
		return appInstanceDao.getCustomer(mac) ;
	}


	public void assignSshTunnelPortToUnassignedApp() {
		List<AppInstance> appList= appInstanceDao.loadAllAppInstances();
		if(!appList.isEmpty()&&appList!=null ) {
			Iterator<AppInstance> iterate = appList.iterator();
			while(iterate.hasNext())
			{
				AppInstance app = (AppInstance) iterate.next() ;
				logger.info("Assigning to App " + app.getName());
				if(app.getActive()&&app.getSshTunnelPort().longValue()==0l)
				{
					app.setSshTunnelPort((long) CommonUtils.getRandomPort_App());
					saveOrUpdate(app);
					logger.info("Assigned ssh tunnel port " + app.getSshTunnelPort() + " to " + app.getName() + " with mac id " + app.getMacId());
				}
			}
		}
		
	}
	

	public List<AppInstance> loadAllAppInstance() {
		
		return appInstanceDao.loadAllAppInstance() ;
	}
	public AppInstance getAppInstance(long id) {
    	return appInstanceDao.getAppInstance(id);
    }	
	
	public void evict(AppInstance app) {
		appInstanceDao.evict(app);
	}
	
	public AppInstanceList loadAppInstancesListByCustomerId(Long id,
		String orderBy, String orderWay, Boolean bSearch,
		String searchField, String searchString, String searchOper, int offset,
		int limit) {
		// TODO Auto-generated method stub
		return appInstanceDao.loadAppInstancesListByCustomerId(id, orderBy, orderWay, bSearch, searchField, searchString, searchOper, offset, limit);
	}
	
}
