package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.Customer;
import com.emscloud.model.Upgrades;

@Repository("upgradesDao")
@Transactional(propagation = Propagation.REQUIRED)
public class UpgradesDao {
	static final Logger logger = Logger.getLogger(UpgradesDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	
	public List<Upgrades> loadAllUpgrades() {
		List<Upgrades> results = new ArrayList<Upgrades>();
		try {
            
            String hsql = "from Upgrades order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            return results ;
            }
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return results;
	}
	
	public void saveOrUpdate(Upgrades upgrades)
	{
		sessionFactory.getCurrentSession().saveOrUpdate(upgrades);
	}

	public Upgrades loadUpgradesById(Long id) {
		 try {
	            List<Upgrades> results = null;
	            String hsql = "from Upgrades u where u.id=?";
	            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	            q.setParameter(0, id);
	            results = q.list();
	            if (results != null && !results.isEmpty()) {
	            	Upgrades upgrades = (Upgrades) results.get(0);
	                  // user.getRole().getModulePermissions().size();
	                   return upgrades;
	               
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return null;
	}

    
}
