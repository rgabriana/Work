package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SubArea;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("subAreaDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SubAreaDao extends BaseDaoHibernate {

    /**
     * Load SubArea
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.SubArea collection
     */
    @SuppressWarnings("unchecked")
    public List<SubArea> loadSubAreaByAreaId(Long id) {
        try {
            List<SubArea> results = null;
            String hsql = "Select new SubArea(sa.id,sa.name,sa.description) from SubArea sa where sa.area.id=?";
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

    /**
     * load subarea details by id
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.SubArea
     */
    public SubArea loadSubArea(Long id) {
        try {
            List<SubArea> results = null;
            String hsql = "Select new SubArea(sa.id,sa.name,sa.description) from SubArea sa where sa.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (SubArea) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
}
