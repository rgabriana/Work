package com.emscloud.dao;

import java.util.HashMap;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;

import com.emscloud.model.ProfileDefaultConfiguration;

@Repository("profileDeafultConfigurationDao.")
public class ProfileDefaultConfigurationDao extends BaseDaoHibernate {

	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.SystemConfigurationDao#loadAllConfig()
     */

    public List<ProfileDefaultConfiguration> loadAllConfig() {
        try {
            List<ProfileDefaultConfiguration> results = null;
            String hsql = "from ProfileDefaultConfiguration sc order by sc.id";
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
            List<ProfileDefaultConfiguration> results = null;
            String hsql = "from ProfileDefaultConfiguration sc order by sc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                for (ProfileDefaultConfiguration sc : results)
                    oSCMap.put(sc.getName(), sc.getValue());
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oSCMap;
    }
    
	public ProfileDefaultConfiguration getSystemConfigurationForKey(String key){
		Session session = this.getSession();
		
		Criteria sysConfigCriteria = session.createCriteria(ProfileDefaultConfiguration.class);
		sysConfigCriteria.add(Restrictions.like("key", key));
		ProfileDefaultConfiguration config =  (ProfileDefaultConfiguration) sysConfigCriteria.uniqueResult();		
		return config;
	}
	
	public ProfileDefaultConfiguration createSystemConfigurationWithKey(String key) {
		ProfileDefaultConfiguration config = new ProfileDefaultConfiguration();
		config.setName(key);
		config.setValue("");
		config = (ProfileDefaultConfiguration) this.saveObject(config);
		return config;
	}
	public ProfileDefaultConfiguration loadConfigById(Long id) {
        try {
            List<ProfileDefaultConfiguration> results = null;
            String hsql = "from ProfileDefaultConfiguration sc where sc.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (ProfileDefaultConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    } 
	public ProfileDefaultConfiguration loadConfigByName(String name) {
        try {
            List<ProfileDefaultConfiguration> results = null;
            String hsql = "from ProfileDefaultConfiguration sc where sc.name=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (ProfileDefaultConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
}
