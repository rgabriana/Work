/**
 * 
 */
package com.ems.dao;

import java.util.HashMap;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.SystemConfigurationDao#loadAllConfig()
     */

    public List<SystemConfiguration> loadAllConfig() {
        try {
            List<SystemConfiguration> results = null;
            String hsql = "from SystemConfiguration sc order by sc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.SystemConfigurationDao#loadAllConfigMap()
     */

    public HashMap<String, String> loadAllConfigMap() {
        HashMap<String, String> oSCMap = new HashMap<String, String>();
        try {
            List<SystemConfiguration> results = null;
            String hsql = "from SystemConfiguration sc order by sc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                for (SystemConfiguration sc : results)
                    oSCMap.put(sc.getName(), sc.getValue());
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oSCMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.SystemConfigurationDao#loadConfigById(java.lang.Long)
     */

    public SystemConfiguration loadConfigById(Long id) {
        try {
            List<SystemConfiguration> results = null;
            String hsql = "from SystemConfiguration sc where sc.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (SystemConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public SystemConfiguration loadConfigByName(String name) {
        try {
            List<SystemConfiguration> results = null;
            String hsql = "from SystemConfiguration sc where sc.name=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (SystemConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

}
