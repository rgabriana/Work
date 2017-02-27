package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.Site;

@Repository("siteDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SiteDao {
	
	static final Logger logger = Logger.getLogger(EmInstanceDao.class.getName());
	
	@Resource 
	SessionFactory sessionFactory;	

	@SuppressWarnings("unchecked")
	public List<Site> loadAllSites() {
		
		try {
			List<Site> siteList = sessionFactory.getCurrentSession().createCriteria(Site.class).addOrder(Order.asc("name")).list();
			if (siteList != null && !siteList.isEmpty()) {
				return siteList;
	 		} else {
	 			return null;
	 		}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadAllSites

	@SuppressWarnings("unchecked")
	public List<Site> loadSitesByCustomer(long id) {
		
		try {
			List<Site> siteList = sessionFactory.getCurrentSession().createCriteria(Site.class)
	    			 .add(Restrictions.eq("customer.id", id))
	    			 .addOrder(Order.asc("name")).list();
			if (siteList != null && !siteList.isEmpty()) {
	 			return siteList;
	 		} else {
	 			return null;
	 		}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadSitesByCustomer
	
	public Site loadSiteById(long id) {
		
		try {
			Site site = (Site)sessionFactory.getCurrentSession().createCriteria(Site.class).add(Restrictions.eq("id", id)).uniqueResult();
			return site;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadSiteById
	
	public void saveOrUpdate(Site site) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(site) ;
				
	} //end of method saveOrUpdate
	
	public List<Long> getSiteEms(long siteId) {
		
		List<Long> emList = new ArrayList<Long>();
		try {
			String hsql = "Select emId from EmSite site where site.siteId = :siteId";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("siteId", siteId);
			emList = q.list();			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return emList;
		
	} //end of method getSiteEms
		
} //end of class SiteDao
