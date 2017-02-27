package com.emscloud.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.SiteDao;
import com.emscloud.model.Site;

@Service("siteManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SiteManager {
	
	@Resource
	private SiteDao	siteDao;
	
	public List<Site> loadAllSites() {
		
		return siteDao.loadAllSites() ;
		
	} //end of method loadAllSites

	public Site loadSiteById(long id) {
		
		return siteDao.loadSiteById(id) ;
		
	} //end of method loadSiteById
		
	public List<Site> loadSitesByCustomer(long id) {
		
		return siteDao.loadSitesByCustomer(id) ;
		
	} //end of method loadSitesByCustomer
		
	public void saveOrUpdate(Site site) {		
		
		siteDao.saveOrUpdate(site) ;	
	
	} //end of method saveOrUpdate
		
} //end of class SiteManager
