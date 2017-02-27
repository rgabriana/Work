package com.enlightedinc.adr.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.adr.model.ADRTarget;

/**
 * 
 * @author Kushal
 */
@Repository("adrTargetDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ADRTargetDao extends BaseDaoHibernate {

    /**
     * Get all ADRTarget objects.
     * 
     * @return list of all ADRTarget objects in the database
     */
    @SuppressWarnings("unchecked")
    public List<ADRTarget> getAllADRTargets() {
        try {
            List<ADRTarget> adrTargets = getSession().createCriteria(ADRTarget.class).addOrder(Order.asc("id")).list();
            if (adrTargets != null && adrTargets.size() > 0) {
                return adrTargets;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    /**
     * Get all ADRTarget objects with event status = NEAR, FAR, ACTIVE.
     * 
     * @return list of all ADRTarget objects in the database
     */
    @SuppressWarnings("unchecked")
    public List<ADRTarget> getAllQueuedADRTargets() {
    	ArrayList<String> status = new ArrayList<String>();
    	status.add("NEAR");
    	status.add("FAR");
    	status.add("ACTIVE");
        try {
            List<ADRTarget> adrTargets = getSession().createCriteria(ADRTarget.class, "adrTarget").
            										add(Restrictions.in("adrTarget.drStatus", status)).
            										addOrder(Order.asc("id")).list();
            if (adrTargets != null && adrTargets.size() > 0) {
                return adrTargets;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Get ADRTarget object by id.
     * 
     * @param id
     *            the id
     * @return the ADRTarget object
     */
    public ADRTarget getADRTargetById(Long id) {
        try {
            ADRTarget adrTarget = (ADRTarget) getSession().get(ADRTarget.class, id);
            return adrTarget;
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * Save or update ADRTarget object.
     * 
     * @param drTarget
     *            the adr target
     */
    public void saveOrUpdateADRTarget(ADRTarget adrTarget) {
        try {
            getSession().saveOrUpdate(adrTarget);
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    /**
     * @param adr event identifier
     * @return adr target object.
     */
    @SuppressWarnings("unchecked")
	public ADRTarget getADRTargetByIdentifier(String identifier) {
        try {
            List<ADRTarget> adrTargets = getSession().createCriteria(ADRTarget.class, "adrTarget")
            							.add(Restrictions.eq("adrTarget.drIdentifier", identifier))
            							.addOrder(Order.desc("id")).list();
            if (adrTargets != null && adrTargets.size() > 0) {
                return adrTargets.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

}
