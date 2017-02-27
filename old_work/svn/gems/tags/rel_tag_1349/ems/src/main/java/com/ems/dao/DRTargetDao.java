package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.DRTarget;
import com.ems.utils.ArgumentUtils;

/**
 * DRTargetDaoImpl, class implementing DRTargetDao interface
 * 
 * @author Shiv Mohan
 */
@Repository("drTargetDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DRTargetDao extends BaseDaoHibernate {

    /**
     * Get all DRTarget objects.
     * 
     * @return list of all DRTarget objects in the database
     */
    @SuppressWarnings("unchecked")
    public List<DRTarget> getAllDRTargets() {
        try {
            List<DRTarget> drTargets = getSession().createCriteria(DRTarget.class).addOrder(Order.asc("id")).list();
            if (!ArgumentUtils.isNullOrEmpty(drTargets)) {
                return drTargets;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Get DRTarget object by id.
     * 
     * @param id
     *            the id
     * @return the DRTarget object
     */
    public DRTarget getDRTargetById(Long id) {
        try {
            DRTarget drTarget = (DRTarget) getSession().get(DRTarget.class, id);
            return drTarget;
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * Save or update DRTarget object.
     * 
     * @param drTarget
     *            the dr target
     */
    public void saveOrUpdateDRTarget(DRTarget drTarget) {
        try {
            getSession().saveOrUpdate(drTarget);
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * Creates a custom update query using the parameters supplied in updateSpecificDRTargetAttribs function
     * 
     * @see DRTargetDaoImpl#updateSpecificDRTargetAttribs(Long, String, Double, Integer, Integer, String)
     * 
     * @param drTarget
     * @return the query string
     */
    // private String createCustomUpdateQuery(DRTarget drTarget) {
    // StringBuffer query = new StringBuffer("Update DRTarget set id = id");
    // if (!ArgumentUtils.isNullOrEmpty(drTarget.getPriceLevel())) {
    // query.append(",priceLevel = :priceLevel");
    // }
    // if (drTarget.getPricing() != null) {
    // query.append(",pricing = :pricing");
    // }
    // if (drTarget.getDuration() != null) {
    // query.append(",duration = :duration");
    // }
    // if (drTarget.getTargetReduction() != null) {
    // query.append(",targetReduction = :targetReduction");
    // }
    // if (!ArgumentUtils.isNullOrEmpty(drTarget.getEnabled())) {
    // query.append(",enabled = :enabled");
    // }
    // if (drTarget.getStartTime() != null) {
    // query.append(",startTime = :startTime");
    // }
    // query = query.append(" where id = :id");
    // if(query.toString().contains(",")){
    // return query.toString();
    // }
    // return null;
    // }

    /**
     * Updates the non null DRTarget attributes
     * 
     * @param the
     *            drTarget to update
     */
    public void updateAttributes(DRTarget drTarget) {
        Session session = getSession();
        session.update(drTarget);

        // String queryString = createCustomUpdateQuery(drTarget);
        // if (!ArgumentUtils.isNullOrEmpty(queryString)) {
        // try {
        // Query query = getSession().createQuery(queryString);
        // if (!ArgumentUtils.isNullOrEmpty(drTarget.getPriceLevel())) {
        // query.setString("priceLevel", drTarget.getPriceLevel());
        // }
        // if (drTarget.getPricing() != null) {
        // query.setDouble("pricing", drTarget.getPricing());
        // }
        // if (drTarget.getDuration() != null) {
        // query.setInteger("duration", drTarget.getDuration());
        // }
        // if (drTarget.getTargetReduction() != null) {
        // query.setInteger("targetReduction", drTarget.getTargetReduction());
        // }
        // if (!ArgumentUtils.isNullOrEmpty(drTarget.getEnabled())) {
        // query.setString("enabled", drTarget.getEnabled());
        // }
        // if (drTarget.getStartTime() != null) {
        // query.setTimestamp("startTime", drTarget.getStartTime());
        // }
        // query.setLong("id", drTarget.getId());
        // query.executeUpdate();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // }
    }

}
