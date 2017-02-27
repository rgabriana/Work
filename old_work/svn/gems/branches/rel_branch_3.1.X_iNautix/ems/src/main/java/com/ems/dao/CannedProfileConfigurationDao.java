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

import com.ems.model.CannedProfileConfiguration;
import com.ems.model.SystemConfiguration;

/**
 * @author yogesh
 * 
 */
@Repository("customProfileConfigurationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CannedProfileConfigurationDao extends BaseDaoHibernate {

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.SystemConfigurationDao#loadAllConfig()
     */

    public List<CannedProfileConfiguration> loadAllConfig() {
        try {
            List<CannedProfileConfiguration> results = null;
            String hsql = "from CannedProfileConfiguration sc order by sc.id";
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

    public HashMap<String, Boolean> loadAllConfigMap() {
        HashMap<String, Boolean> oSCMap = new HashMap<String, Boolean>();
        try {
            List<CannedProfileConfiguration> results = null;
            String hsql = "from CannedProfileConfiguration sc order by sc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                for (CannedProfileConfiguration sc : results)
                    oSCMap.put(sc.getName(), sc.getStatus());
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

    public List<CannedProfileConfiguration> loadConfigById(Long id) {
        try {
            List<CannedProfileConfiguration> results = null;
            String hsql = "from CannedProfileConfiguration sc where sc.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    public List<CannedProfileConfiguration> loadConfigByProfileId(Integer id) {
        try {        	
            List<CannedProfileConfiguration> results = null;
            String hsql = "from CannedProfileConfiguration sc where sc.parentProfileid=? and status='FALSE'";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public CannedProfileConfiguration loadConfigByName(String name) {
        try {
            List<CannedProfileConfiguration> results = null;
            name = name.toUpperCase();
            String hsql = "from CannedProfileConfiguration sc where upper(sc.name)=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (CannedProfileConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
}
