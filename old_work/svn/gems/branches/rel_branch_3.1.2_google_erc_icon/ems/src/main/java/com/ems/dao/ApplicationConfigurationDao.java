package com.ems.dao;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.ApplicationConfiguration;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("applicationConfigurationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ApplicationConfigurationDao extends BaseDaoHibernate {

    /**
     * load all applicationConfiguration objects. 1. Self login config 2. Valid domain list
     * 
     * @return com.ems.model.ApplicationConfiguration collection
     */
    @SuppressWarnings("unchecked")
    public List<ApplicationConfiguration> loadAllConfig() {
        // try{
        List<ApplicationConfiguration> results = null;
        String hsql = "from ApplicationConfiguration ac order by ac.id";
        Query q = getSession().createQuery(hsql.toString());
        results = q.list();
        if (results != null && !results.isEmpty()) {
            return results;
        }
        // }catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        return null;
    }

    /**
     * load applicationConfiguration object.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.ApplicationConfiguration
     */
    @SuppressWarnings("unchecked")
    public ApplicationConfiguration loadConfigById(Long id) {
        // try {
        List<ApplicationConfiguration> results = null;
        String hsql = "from ApplicationConfiguration ac where ac.id=?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, id);
        results = q.list();
        if (results != null && !results.isEmpty()) {
            return (ApplicationConfiguration) results.get(0);
        }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        return null;
    }
}
