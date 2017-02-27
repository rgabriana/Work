package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.CloudCampus;

@Repository("cloudCampusDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CloudCampusDao extends BaseDaoHibernate {

	@Resource
    SessionFactory sessionFactory;

	public List<CloudCampus> loadCloudCampusesByCustomerId(long id) {
		try {
            
	    	 List<CloudCampus> cloudCampusList = sessionFactory.getCurrentSession().createCriteria(CloudCampus.class)
	    			 .add(Restrictions.eq("customer.id", id))
	    			 .addOrder(Order.asc("name")).list();
	    	 if (cloudCampusList != null && !cloudCampusList.isEmpty()) {
	 			return cloudCampusList;
	 		} else {
	 			return null;
	 		}

            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}
}
