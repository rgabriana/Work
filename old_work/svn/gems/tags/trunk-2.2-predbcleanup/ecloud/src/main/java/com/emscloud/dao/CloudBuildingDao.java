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

import com.emscloud.model.CloudBuilding;

@Repository("cloudBuildingDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CloudBuildingDao extends BaseDaoHibernate  {
	@Resource
    SessionFactory sessionFactory;

	public List<CloudBuilding> loadCloudBuildingsByCampusId(long id) {
		try {
            
	    	 List<CloudBuilding> cloudBldgList = sessionFactory.getCurrentSession().createCriteria(CloudBuilding.class)
	    			 .add(Restrictions.eq("cloudCampus.id", id))
	    			 .addOrder(Order.asc("name")).list();
	    	 if (cloudBldgList != null && !cloudBldgList.isEmpty()) {
	 			return cloudBldgList;
	 		} else {
	 			return null;
	 		}

            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}

}
