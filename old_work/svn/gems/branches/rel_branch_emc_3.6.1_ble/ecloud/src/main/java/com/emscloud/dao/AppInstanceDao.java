package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.AppInstance;
import com.emscloud.model.AppInstanceList;
import com.emscloud.model.Customer;



@Repository("appInstanceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class AppInstanceDao extends BaseDaoHibernate {
	static final Logger logger = Logger
			.getLogger(AppInstanceDao.class.getName());

	@Resource
	SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public List<AppInstance> loadAllAppInstances() {
		try {
			List<AppInstance> appInstanceList = sessionFactory
					.getCurrentSession().createCriteria(AppInstance.class)
					.addOrder(Order.asc("name")).list();
			if (appInstanceList != null && !appInstanceList.isEmpty()) {
				return appInstanceList;
			} else {
				return null;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	
	@SuppressWarnings("unchecked")
	public List<AppInstance> getActiveAppInstance() {
		List<AppInstance> appInstanceList = null;
		Criteria oCriteria = sessionFactory.getCurrentSession().createCriteria(
				AppInstance.class, "appinst");
		oCriteria.add(Restrictions.eq("appinst.active", true));
		appInstanceList = oCriteria.list();
		return appInstanceList;
	}

	

	@SuppressWarnings("unchecked")
	public List<AppInstance> loadAppInstancesByCustomerId(long id) {
		try {
			List<AppInstance> appInstanceList = sessionFactory
					.getCurrentSession().createCriteria(AppInstance.class)
					.add(Restrictions.eq("customer.id", id))
					.addOrder(Order.asc("name")).list();
			if (appInstanceList != null && !appInstanceList.isEmpty()) {
				return appInstanceList;
			} else {
				return null;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	@SuppressWarnings("unchecked")
	public AppInstanceList loadUnregAppInstances(String orderby, String orderway, Boolean bSearch, String searchField, String searchString, String searchOper, int offset,
			int limit) {
		AppInstanceList appInstanceList = new AppInstanceList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(AppInstance.class, "appinst")
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				AppInstance.class, "appinst");

		oRowCount.add(Restrictions.eq("appinst.active", false));
		oCriteria.add(Restrictions.eq("appinst.active", false));

		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("version")) {
				orderby = "appinst.version";
			} else if (orderby.equals("macId")) {
				orderby = "appinst.macId";
			}else {
				orderby = "appinst.id";
			}
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc(orderby));
			}else{
				oCriteria.addOrder(Order.asc(orderby));
			}
			
		} else {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc("id"));
			}else{
				oCriteria.addOrder(Order.asc("id"));
			}
		}
		
		if (bSearch) {
			if (searchField.equals("version")) {
				oRowCount.add(Restrictions.like("appinst.version", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("appinst.version", "%"
						+ searchString + "%"));
			}else if (searchField.equals("macId")) {
				oRowCount.add(Restrictions.ilike("appinst.macId", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("appinst.macId", "%"
						+ searchString + "%"));
			}
		}
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("appinst.version"));
		} else {
			oCriteria.addOrder(Order.asc("appinst.version"));
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			appInstanceList.setTotal(count);
			appInstanceList.setAppInsts(oCriteria.list());
			return appInstanceList;
		}

		return appInstanceList;

	}


	public AppInstance loadAppInstanceById(long id) {
		try {

			AppInstance appInstance = (AppInstance) sessionFactory
					.getCurrentSession().createCriteria(AppInstance.class)
					.add(Restrictions.eq("id", id)).uniqueResult();

			return appInstance;
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	public AppInstance loadAppInstanceByMac(String mac) {
		try {

			AppInstance appInstance = (AppInstance) sessionFactory
					.getCurrentSession().createCriteria(AppInstance.class)
					.add(Restrictions.eq("macId", mac).ignoreCase()).uniqueResult();

			return appInstance;
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	public AppInstance saveOrUpdate(AppInstance instance) {
		sessionFactory.getCurrentSession().saveOrUpdate(instance);
		return instance;
	}

	public void deleteById(Long id) {
		String hsql = "delete from AppInstance where id=?";
		Query q = sessionFactory.getCurrentSession().createQuery(
				hsql.toString());
		q.setParameter(0, id);
		q.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public Customer getCustomer(String mac) {
		try {
			List<AppInstance> results = null;
			String hsql = "from AppInstance u where u.macId=?";
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			q.setParameter(0, mac);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				AppInstance app = (AppInstance) results.get(0);
				return app.getCustomer();

			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public AppInstance getAppInstanceForMac(String mac)
	{
            List<AppInstance> results = null;
            String hsql = "from AppInstance u where u.macId=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, mac);
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	AppInstance app  = (AppInstance) results.get(0);
                return app;               
           }
        return null;
	}

	public List<AppInstance> loadAllAppInstance() {
		try {

			ArrayList<AppInstance> appInstanceList = (ArrayList<AppInstance>) sessionFactory
					.getCurrentSession().createCriteria(AppInstance.class).list();

			return appInstanceList;
		} catch (HibernateException hbe) {
			hbe.printStackTrace();
		}
		return null;
	}
	public AppInstance getAppInstance(long id) {
		  String hsql = "from AppInstance u where u.id=?";
		  Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
          q.setParameter(0, id);          
          Object obj = q.uniqueResult();
//    	Object obj = getObject(EmInstance.class, id);
    	if(obj == null )
    		return null;
    	else
    		return (AppInstance)obj;  
    }
	
		
	    public void evict(AppInstance app) {
	    	getSession().evict(app);
	    }

	    public boolean checkPortInUseForSSHORWebappByAnyApp(long port) {
	    	boolean retVal = false;
			List<AppInstance> results = new ArrayList<AppInstance>();
			String hsql = "from AppInstance where  tunnelPort = ?";
	        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	        q.setLong(0, port);
	        results = q.list();	
	        if(results != null && results.size() > 0) {
	        	retVal = true;
	        } else {
	        	hsql = "from AppInstance where  sshTunnelPort = ?";
		        q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
		        q.setLong(0, port);
		        results = q.list();	
		        if(results != null && results.size() > 0) {
		        	retVal = true;
		        }	        	
	        }
	    	
	    	return retVal;
	    }

	@SuppressWarnings("unchecked")
	public AppInstanceList loadAppInstancesListByCustomerId(Long id,
			String orderBy, String orderWay, Boolean bSearch,
			String searchField, String searchString, String searchOper,
			int offset, int limit) {
			AppInstanceList appInstanceList = new AppInstanceList();
			Criteria oCriteria = null;
			Criteria oRowCount = null;
			oRowCount = sessionFactory.getCurrentSession()
					.createCriteria(AppInstance.class, "appinst")
					.setProjection(Projections.rowCount());
			oCriteria = sessionFactory.getCurrentSession().createCriteria(
					AppInstance.class, "appinst");
			oRowCount.add(Restrictions.eq("appinst.customer.id", id));		
			oCriteria.add(Restrictions.eq("appinst.customer.id", id));
			
		if (orderBy != null && !"".equals(orderBy)) {
			if (orderBy.equals("name")) {
				orderBy = "appinst.name";
			} else if (orderBy.equals("version")) {
				orderBy = "appinst.version";
			} else if (orderBy.equals("macId")) {
				orderBy = "appinst.macId";
			} else if (orderBy.equals("timeZone")) {
				orderBy = "appinst.timeZone";
			} else if (orderBy.equals("utcLastConnectivityAt")) {
				orderBy = "appinst.lastConnectivityAt";
			} else if (orderBy.equals("ipAddress")) {
				orderBy = "appinst.ipAddress";
			} else {
				orderBy = "appinst.id";
			}
			if ("desc".equals(orderWay)) {
				oCriteria.addOrder(Order.desc(orderBy));
			} else {
				oCriteria.addOrder(Order.asc(orderBy));
			}
		} else {
			if ("desc".equals(orderWay)) {
				oCriteria.addOrder(Order.desc("id"));
			} else {
				oCriteria.addOrder(Order.asc("id"));
			}
		}
		if (bSearch) {
			if (searchField.equals("name")) {
				oRowCount.add(Restrictions.ilike("appinst.name", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("appinst.name", "%"
						+ searchString + "%"));
			} else if (searchField.equals("version")) {
				oRowCount.add(Restrictions.like("appinst.version", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("appinst.version", "%"
						+ searchString + "%"));
			} else if (searchField.equals("macId")) {
				oRowCount.add(Restrictions.ilike("appinst.macId", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("appinst.macId", "%"
						+ searchString + "%"));
			} else if (searchField.equals("timeZone")) {
				oRowCount.add(Restrictions.ilike("appinst.timeZone", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("appinst.timeZone", "%"
						+ searchString + "%"));
			}
		}
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			appInstanceList.setTotal(count);
			appInstanceList.setAppInsts(oCriteria.list());
			return appInstanceList;
		}
		return appInstanceList;
	}

}
