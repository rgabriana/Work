package com.ems.dao;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Pricing;
import com.ems.utils.ArgumentUtils;

/**
 * PricingDaoImpl, Class implementing the PricingDao interface
 * 
 * @author Shiv Mohan
 */
@Repository("pricingDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PricingDao extends BaseDaoHibernate {

    /**
     * Gets pricing by unique id
     * 
     * @param id
     * @return Pricing object
     */
    public Pricing getPricingById(Long id) {
        try {
            Pricing pricing = (Pricing) getSession().get(Pricing.class, id);
            return pricing;
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * Gets the pricing list
     * 
     * @return the pricing list
     */
    @SuppressWarnings("unchecked")
    public List<Pricing> getPricingList() {
        try {
            List<Pricing> pricingList = getSession().createCriteria(Pricing.class).addOrder(Order.asc("fromTime"))
                    .list();
            if (!ArgumentUtils.isNullOrEmpty(pricingList)) {
                return pricingList;
            }
        } catch (HibernateException hbe) {
            SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Saves or updates pricing object
     * 
     * @param the
     *            Pricing object to be saved/updated
     */
    public void saveOrUpdatePricing(Pricing pricing) {
        try {
            getSession().saveOrUpdate(pricing);
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * Removes pricing object by id
     * 
     * @param id
     *            , the unique identifier
     */
    public void removePricing(Long id) {
        getSession().createSQLQuery("delete from pricing where id = :id").setLong("id", id).executeUpdate();
    }

    public boolean validateTime(Date fromTime, Date toTime, String dayType) {
        StringBuffer hsql = new StringBuffer("select count(*) from pricing where day_type = :dType and");
        hsql.append(" ((:startTime between from_time + interval '1 minute' and to_time - interval '1 minute')");
        hsql.append(" or (:endTime between from_time + interval '1 minute' and to_time - interval '1 minute')");
        hsql.append(" or (from_time + interval '1 minute' between :startTime and :endTime)");
        hsql.append(" or (to_time - interval '1 minute' between :startTime and :endTime))");
        SQLQuery query = getSession().createSQLQuery(hsql.toString());
        query.setString("dType", dayType);
        query.setTimestamp("startTime", fromTime);
        query.setTimestamp("endTime", toTime);
        BigInteger noOfHits = (BigInteger) query.uniqueResult();
        if (noOfHits.intValue() > 0) {
            return false;
        }
        return true;
    }

    public boolean validateTime(Date fromTime, Date toTime, String dayType, long id) {
        StringBuffer hsql = new StringBuffer("select count(*) from pricing where day_type = :dType and id != :id and");
        hsql.append(" ((:startTime between from_time + interval '1 minute' and to_time - interval '1 minute')");
        hsql.append(" or (:endTime between from_time + interval '1 minute' and to_time - interval '1 minute')");
        hsql.append(" or (from_time + interval '1 minute' between :startTime and :endTime)");
        hsql.append(" or (to_time - interval '1 minute' between :startTime and :endTime))");
        SQLQuery query = getSession().createSQLQuery(hsql.toString());
        query.setLong("id", id);
        query.setString("dType", dayType);
        query.setTimestamp("startTime", fromTime);
        query.setTimestamp("endTime", toTime);
        BigInteger noOfHits = (BigInteger) query.uniqueResult();
        if (noOfHits.intValue() > 0) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
	public List<Pricing> getPricingByDayType(String dayType) {
    	try {
            List<Pricing> pricingList = getSession().createCriteria(Pricing.class).add(Restrictions.eq("dayType", dayType)).addOrder(Order.asc("fromTime"))
                    .list();
            if (!ArgumentUtils.isNullOrEmpty(pricingList)) {
                return pricingList;
            }
        } catch (HibernateException hbe) {
            SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;

    } // end of method getPrice
}
