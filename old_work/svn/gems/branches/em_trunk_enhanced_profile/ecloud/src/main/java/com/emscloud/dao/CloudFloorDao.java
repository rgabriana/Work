package com.emscloud.dao;

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

import com.emscloud.model.CloudFloor;

@Repository("cloudFloorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CloudFloorDao extends BaseDaoHibernate {
	@Resource
    SessionFactory sessionFactory;

	public List<CloudFloor> loadCloudFloorsByBldgId(long id) {
		try {
            
	    	 List<CloudFloor> cloudFloorList = sessionFactory.getCurrentSession().createCriteria(CloudFloor.class)
	    			 .add(Restrictions.eq("cloudBuilding.id", id))
	    			 .addOrder(Order.asc("name")).list();
	    	 if (cloudFloorList != null && !cloudFloorList.isEmpty()) {
	 			return cloudFloorList;
	 		} else {
	 			return null;
	 		}

            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}
}
