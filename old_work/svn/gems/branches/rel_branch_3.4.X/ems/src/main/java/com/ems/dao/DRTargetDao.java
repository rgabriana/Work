package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.DRTarget;
import com.ems.types.DRStatusType;
import com.ems.types.DRType;
import com.ems.types.DrLevel;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.DRTargetList;

/**
 * DRTargetDaoImpl, class implementing DRTargetDao interface
 * 
 * @author Shiv Mohan
 */
@Repository("drTargetDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DRTargetDao extends BaseDaoHibernate {

	private static final String SEVERITY_ORDER = "(CASE WHEN severity = 'High' THEN '1' " +
			"WHEN severity = 'Moderate' THEN '2' " +
			"WHEN severity = 'Low' THEN '3' " +
			"WHEN severity = 'Special' THEN '4' ) as severity";
	
	 /**
     * Get all DRTarget objects.
     * 
     * @return list of all DRTarget objects in the database
     */
    @SuppressWarnings("unchecked")
    public List<DRTarget> getAllDRTargets() {
        try {
        	Criteria criteria = getSession().createCriteria(DRTarget.class); 
        	Criterion rest1= Restrictions.and(Restrictions.ne("drStatus",DRStatusType.Completed.getName()), 
        			Restrictions.ne("drStatus",DRStatusType.Cancelled.getName()));
        	Criterion rest2= Restrictions.isNull("drStatus");
        	Criterion rest3= Restrictions.eq("optIn", true);
        	List<DRTarget> drTargets = criteria.add(Restrictions.or(rest1, rest2))
        	.add(rest3)
        	.addOrder(Order.desc("drType"))
        	.addOrder(Order.asc("priority"))
        	.addOrder(Order.desc("startTime"))
        	.addOrder(Order.desc("priceLevel"))
        	.addOrder(Order.desc("uid")).list();
            if (!ArgumentUtils.isNullOrEmpty(drTargets)) {
                return drTargets;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    @SuppressWarnings({ "rawtypes" })
	public List getAllDRTargets(Integer offset,Integer limit,DRTargetList mList) {
    	List drList = new ArrayList();		
        try {
        	String queryStr = "Select a.id as id,a.priceLevel as pricelevel,a.pricing as pricing,a.duration as duration,a.startTime as starttime,a.priority as priority,a.drIdentifier as drIdentifier,a.drStatus as drStatus,a.optIn as optIn,a.jitter as jitter From DRTarget a where a.drType = 'OADR' and (upper(a.drStatus)=? or upper(a.drStatus)=? or upper(a.drStatus)=?) order by a.startTime";            
            Query query = getSession().createQuery(queryStr);
            query.setParameter(0, "far".toUpperCase());
            query.setParameter(1, "near".toUpperCase());
            query.setParameter(2, "active".toUpperCase());
            if(limit > 0)
            {
            mList.setRecords(query.list().size());                       	
            int totalpages = (int) (Math.ceil(query.list().size()/new Double(DRTargetList.DEFAULT_ROWS)));
            mList.setTotal(totalpages);
            query.setMaxResults(limit).setFirstResult(offset);
            }
            drList = query.list();
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return drList;
    }

@SuppressWarnings({ "rawtypes" })
public List getAllDRTargetsCanceledOrCompleted(Integer offset,Integer limit,DRTargetList mList) {
    	List drList = new ArrayList();		
        try {
        	String queryStr = "Select a.id as id,a.priceLevel as pricelevel,a.pricing as pricing,a.duration as duration,a.startTime as starttime,a.priority as priority,a.drIdentifier as drIdentifier,a.drStatus as drStatus,a.optIn as optIn,a.jitter as jitter From DRTarget a where a.drType = 'OADR' and (upper(a.drStatus)=? or upper(a.drStatus)=?) order by a.startTime desc";            
            Query query = getSession().createQuery(queryStr);
            query.setParameter(0, "cancelled".toUpperCase());
            query.setParameter(1, "completed".toUpperCase());            
            if(limit > 0)
            {
            mList.setRecords(query.list().size());                       	
            int totalpages = (int) (Math.ceil(query.list().size()/new Double(DRTargetList.DEFAULT_ROWS)));
            mList.setTotal(totalpages);
            query.setMaxResults(limit).setFirstResult(offset);
            }
            drList = query.list();
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return drList;
    }
    
    /**
     * Get all DRTarget objects.
     * 
     * @return list of all DRTarget objects in the database
     */
    @SuppressWarnings("unchecked")
    public List<DRTarget> getAllManualDRTargets() {
        try {
        	List<String> types = new ArrayList<String>();
        	types.add(DRType.MANUAL.getName().toUpperCase());
        	types.add(DRType.HOLIDAY.getName().toUpperCase());
        	
            List<DRTarget> drTargets = getSession().createCriteria(DRTarget.class)
            		.add(Restrictions.in("drType", types))
            		.addOrder(Order.asc("id")).list();
            if (!ArgumentUtils.isNullOrEmpty(drTargets)) {
                return drTargets;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    /**
     * Get all DRTarget objects (pagination)
     * 
     * @return list of all DRTarget objects in the database
     */
    @SuppressWarnings({ "rawtypes" })
    public List getAllManualDRTargets(Integer offset, Integer limit, DRTargetList mList, Boolean showAllChecked, String orderby, String orderway) {       
    	List drList = new ArrayList();
    	String queryStr = "";
    	Query query = null;
    	
    	if(orderby.equals("starttime"))
    		orderby = "startTime";
    	else if(orderby.equals("pricelevel"))
    		orderby = "priceLevel";
    	else if(orderby.equals("drtype"))
    		orderby = "drType";    	
    	else if(orderby.equals("drstatus"))
    		orderby = "drStatus";
    		
    	try {    		
        	if(showAllChecked == true){
        		queryStr = "Select a.id as id,a.priceLevel as pricelevel,a.pricing as pricing,a.duration as duration,a.startTime as starttime,a.drStatus as drStatus,a.drType as drtype, a.description as description From DRTarget a where a.drType=? or a.drType=? order by a.";
        		queryStr = queryStr + orderby + " " +orderway;        		
                query = getSession().createQuery(queryStr);
                query.setParameter(0, "manual".toUpperCase());
                query.setParameter(1, "holiday".toUpperCase());                       		        		
        	}
        	else{
        		queryStr = "Select a.id as id,a.priceLevel as pricelevel,a.pricing as pricing,a.duration as duration,a.startTime as starttime,a.drStatus as drstatus,a.drType as drtype, a.description as description From DRTarget a where (a.drType in (?,?)) and (a.drStatus not in (?,?)) order by a.";
        		queryStr = queryStr + orderby + " " +orderway;
                query = getSession().createQuery(queryStr); 
                query.setParameter(0, "manual".toUpperCase());
                query.setParameter(1, "holiday".toUpperCase());
                query.setParameter(2, DRStatusType.Cancelled.getName());
                query.setParameter(3, DRStatusType.Completed.getName()); 
        	}
        	
        	if(limit > 0)
            {
        		mList.setRecords(query.list().size());                       	
                int totalpages = (int) (Math.ceil(query.list().size()/new Double(DRTargetList.DEFAULT_ROWS)));
                mList.setTotal(totalpages);
                query.setMaxResults(limit).setFirstResult(offset);
            }
        	drList = query.list();
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return drList;        	
    }

    /**
     * Get all ADR DRTarget objects.
     * 
     * @return list of all ADR DRTarget objects in the database
     */
    @SuppressWarnings("unchecked")
    public List<DRTarget> getAllScheduledADRTargets() {
        try {
            List<DRTarget> drTargets = getSession().createCriteria(DRTarget.class)
            		.add(Restrictions.eq("drType",DRType.OADR.getName().toUpperCase()))
            		.add(Restrictions.ne("drStatus", DRStatusType.Cancelled.getName()))
            		.add(Restrictions.ne("drStatus", DRStatusType.Completed.getName()))
            		.addOrder(Order.asc("id")).list();
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
     * Opt out of dr event
     * 
     * @param dr identifier
     *       
     */
    public void optOutOfDrByIdentifier(String drIdentifier) {
    	 try {
    		 String hql = "update DRTarget a set a.optIn = false, a.drStatus = '" + DRStatusType.Cancelled.getName() + "' where a.drIdentifier = :id";
             Query query = getSession().createQuery(hql);
             query.setString("id", drIdentifier);             
             query.executeUpdate();
         } catch (HibernateException hbe) {             
             throw SessionFactoryUtils.convertHibernateAccessException(hbe);
         }
    }
    
    public DRTarget saveOrUpdateDRTarget(DRTarget drTarget) {
        try {
            return (DRTarget) saveObject(drTarget);
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    public DRTarget getFirstDRTargetByIdentifier(String drIdentifier) {
    	 try {
             DRTarget drTarget = (DRTarget) getSession().createCriteria(DRTarget.class)
     					.add(Restrictions.eq("drIdentifier",drIdentifier))
     					.add(Restrictions.eq("uid", 0)).uniqueResult();
             return drTarget;
         } catch (HibernateException hbe) {
             throw SessionFactoryUtils.convertHibernateAccessException(hbe);
         }
    }

    /**
     * Get DRTarget object by price_level and dr_type.
     * 
     * @param id
     *            the id
     * @return the DRTarget object
     */
    public DRTarget getDRTargetByPriceLevelAndDrType(DrLevel priceLevel, String drType) {
    	String level = priceLevel.getName();
    	String drLevel = level.substring(0, 1).toUpperCase() +  level.substring(1).toLowerCase();
        try {
            DRTarget drTarget = (DRTarget) getSession().createCriteria(DRTarget.class)
            					.add(Restrictions.eq("drType",drType.toUpperCase()))
            					.add(Restrictions.eq("priceLevel",drLevel))
            					.addOrder(Order.asc("id")).uniqueResult();
            return drTarget;
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    /**
     * Get DRTarget object by dr_identifier and uid
     * 
     * @param id
     *            the id
     * @return the DRTarget object
     */

	public DRTarget getDRTargetByDRIdentifierAndUid(String drIdentifier, Integer uid) {

        try {
            DRTarget drTarget = (DRTarget) getSession().createCriteria(DRTarget.class)
            					.add(Restrictions.eq("drIdentifier",drIdentifier))
            					.add(Restrictions.eq("uid",uid))
            					.addOrder(Order.asc("id")).uniqueResult();
            return drTarget;
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}

	public int deleteDRTarget(Long id) {
		int status;
		try {
			String hql = "delete from DRTarget a where a.id=?";
			Query q = getSession().createQuery(hql.toString());
			q.setParameter(0, id);
			status = q.executeUpdate();
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return status;		
	}
	
}
