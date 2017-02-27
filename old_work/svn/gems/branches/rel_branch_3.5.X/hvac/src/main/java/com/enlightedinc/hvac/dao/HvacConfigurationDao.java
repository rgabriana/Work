/**
 * 
 */
package com.enlightedinc.hvac.dao;

import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.model.HvacConfiguration;

@Repository("hvacConfigurationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class HvacConfigurationDao extends BaseDaoHibernate {


    @SuppressWarnings("unchecked")
	public List<HvacConfiguration> loadAllConfig() {
        try {
            List<HvacConfiguration> results = null;
            String hsql = "from HvacConfiguration sc order by sc.id";
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


    @SuppressWarnings("unchecked")
	public HashMap<String, String> loadAllConfigMap() {
        HashMap<String, String> oSCMap = new HashMap<String, String>();
        try {
            List<HvacConfiguration> results = null;
            String hsql = "from HvacConfiguration sc order by sc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                for (HvacConfiguration sc : results)
                    oSCMap.put(sc.getName(), sc.getValue());
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oSCMap;
    }

    @SuppressWarnings("unchecked")
	public HvacConfiguration loadConfigById(Long id) {
        try {
            List<HvacConfiguration> results = null;
            String hsql = "from HvacConfiguration sc where sc.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (HvacConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public HvacConfiguration loadConfigByName(String name) {
        try {
            List<HvacConfiguration> results = null;
            String hsql = "from HvacConfiguration sc where sc.name=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (HvacConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

}
