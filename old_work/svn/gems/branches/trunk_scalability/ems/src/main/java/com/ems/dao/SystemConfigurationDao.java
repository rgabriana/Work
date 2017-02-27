/**
 * 
 */
package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SystemConfiguration;

/**
 * @author yogesh
 * 
 */
@Repository("systemConfigurationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemConfigurationDao extends BaseDaoHibernate {

    @SuppressWarnings("unchecked")
	public SystemConfiguration loadConfigByName(String name) {
        try {
            List<SystemConfiguration> results = null;
            String hsql = "from SystemConfiguration sc where sc.name=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	evict((SystemConfiguration) results.get(0));
                return (SystemConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

}
