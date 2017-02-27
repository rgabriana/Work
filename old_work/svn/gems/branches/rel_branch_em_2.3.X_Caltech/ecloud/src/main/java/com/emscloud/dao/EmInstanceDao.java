package com.emscloud.dao;

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

import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;

@Repository("emInstanceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceDao {
	static final Logger logger = Logger.getLogger(EmInstanceDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	

	@SuppressWarnings("unchecked")
	public List<EmInstance> loadAllEmInstances() {
		try {
	    	 List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class).addOrder(Order.asc("name")).list();
	    	 if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}
        }
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}

	@SuppressWarnings("unchecked")
	public List<EmInstance> loadEmInstancesByCustomerId(long id) {
		try {
	    	 List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
	    			 .add(Restrictions.eq("customer.id", id))
	    			 .addOrder(Order.asc("name")).list();
	    	 if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}
        }
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}
	
	@SuppressWarnings("unchecked")
	public EmInstanceList loadEmInstanceListByCustomerId(Long id,String orderway, int offset, int limit) {
		EmInstanceList emInstanceList = new EmInstanceList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(EmInstance.class, "eminst").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(EmInstance.class, "eminst");
	
		oRowCount.add(Restrictions.eq("eminst.customer.id", id));
		oCriteria.add(Restrictions.eq("eminst.customer.id", id));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("eminst.lastConnectivityAt"));
		} 
		else {
			oCriteria.addOrder(Order.asc("eminst.lastConnectivityAt"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emInstanceList.setTotal(count);
			emInstanceList.setEmInsts(oCriteria.list());
			return emInstanceList;
		}
		
		return emInstanceList;	
		
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public EmInstanceList loadUnregEmInstances(String orderway, int offset, int limit) {
		EmInstanceList emInstanceList = new EmInstanceList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(EmInstance.class, "eminst").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(EmInstance.class, "eminst");
	
		oRowCount.add(Restrictions.eq("eminst.active", false));
		oCriteria.add(Restrictions.eq("eminst.active", false));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("eminst.version"));
		} 
		else {
			oCriteria.addOrder(Order.asc("eminst.version"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emInstanceList.setTotal(count);
			emInstanceList.setEmInsts(oCriteria.list());
			return emInstanceList;
		}
		
		return emInstanceList;	
		
	}
	
	@SuppressWarnings("unchecked")
	public List<EmInstance> loadEmInstanceByReplicaServerId(Long id) {
		try {
        	List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
					 .add(Restrictions.eq("replicaServer.id", id))
        			 .addOrder(Order.asc("name")).list();
				
			if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}
		}
        catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}

	public EmInstance loadEmInstanceById(long id) {
		try {
            
	    	 EmInstance emInstance = (EmInstance)sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
	    			 .add(Restrictions.eq("id", id)).uniqueResult();
	    			 

	    	 return emInstance;
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}
	public EmInstance loadEmInstanceByMac(String mac) {
		try {
            
	    	 EmInstance emInstance = (EmInstance)sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
	    			 .add(Restrictions.eq("macId", mac)).uniqueResult();
	    			 

	    	 return emInstance;
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}


	public EmInstance saveOrUpdate(EmInstance instance) {
		sessionFactory.getCurrentSession().saveOrUpdate(instance) ;
		return instance ;
	}
	
	public void deleteById(Long id)
	{
		String hsql = "delete from EmInstance where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public Customer getCustomer(String mac)
	{
		try {
            List<EmInstance> results = null;
            String hsql = "from EmInstance u where u.macId=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, mac);
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	EmInstance em  = (EmInstance) results.get(0);
                   return em.getCustomer();
               
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
	}

}
