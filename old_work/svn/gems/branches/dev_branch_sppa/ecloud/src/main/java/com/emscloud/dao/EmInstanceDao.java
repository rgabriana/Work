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

import com.emscloud.model.EmInstance;

@Repository("emInstanceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceDao {
	static final Logger logger = Logger.getLogger(EmInstanceDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	

	public List<EmInstance> loadAllEmInstances() {
		List<EmInstance> results = new ArrayList<EmInstance>();
		try {
            
	    	 List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class).addOrder(Order.asc("name")).list();
	    	 if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}

/*	    	String hsql = "from EmInstance order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	return results ;
            }*/
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
//        return emInstanceList;
	}

	public List<EmInstance> loadEmInstancesByCustomerId(long id) {
		List<EmInstance> results = new ArrayList<EmInstance>();
		try {
            
	    	 List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
	    			 .add(Restrictions.eq("customer.id", id))
	    			 .addOrder(Order.asc("name")).list();
	    	 if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}

/*	    	String hsql = "from EmInstance order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	return results ;
            }*/
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
//        return emInstanceList;
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


	public void saveOrUpdate(EmInstance instance) {
		sessionFactory.getCurrentSession().saveOrUpdate(instance) ;
		
	}
	
	public void deleteById(Long id)
	{
		String hsql = "delete from EmInstance where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}

	

}
