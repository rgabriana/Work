package com.adrcom.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.oasis_open.docs.ns.energyinterop._201110.EventStatusEnumeratedType;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.adrcom.model.DrEvent;
import com.adrcom.model.DrEventSignal;
import com.adrcom.model.DrEventSignalInterval;

/**
 * 
 * @author Kushal
 */
@Repository("drEventDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DrEventDao{
	
	@Resource
    SessionFactory sessionFactory;
	
	public Session getSession() {
        return sessionFactory.getCurrentSession();
    }
	
    public Object saveObject(Object o) {
        sessionFactory.getCurrentSession().saveOrUpdate(o);
        return o;
    }
	
    @SuppressWarnings("unchecked")
    public DrEvent loadDrEventByEventId(String id) {
    	
    	 Criteria criteria = getSession().createCriteria(DrEvent.class);
         criteria.add(Restrictions.eq("eventId", id));
         List<DrEvent> list = criteria.list();
         if (list != null && list.size() > 0) {
             return list.get(0);
         } else {
             return null;
         }
    }
    
    @SuppressWarnings("unchecked")
    public DrEventSignal loadDrEventSignalByEventIdAndSignalId(Long eventId, String signalId) {
    	
    	 Criteria criteria = getSession().createCriteria(DrEventSignal.class);
         criteria.add(Restrictions.eq("drEvent.id", eventId));
         criteria.add(Restrictions.eq("signalId", signalId));
         List<DrEventSignal> list = criteria.list();
         if (list != null && list.size() > 0) {
             return list.get(0);
         } else {
             return null;
         }
    }
    
    @SuppressWarnings("unchecked")
    public List<DrEventSignal> loadDrEventSignalsByEventId(Long eventId) {
    	
    	 Criteria criteria = getSession().createCriteria(DrEventSignal.class);
         criteria.add(Restrictions.eq("drEvent.id", eventId));
         List<DrEventSignal> list = criteria.list();
         if (list != null && list.size() > 0) {
             return list;
         } else {
             return new ArrayList<DrEventSignal>();
         }
    }
    
    @SuppressWarnings("unchecked")
    public List<DrEventSignalInterval> loadDrEventSignalIntervalsByEventSignalId(Long signalId) {
    	
    	 Criteria criteria = getSession().createCriteria(DrEventSignalInterval.class);
         criteria.add(Restrictions.eq("drEventSignal.id", signalId)).addOrder(Order.asc("uid"));
         List<DrEventSignalInterval> list = criteria.list();
         if (list != null && list.size() > 0) {
             return list;
         } else {
             return new ArrayList<DrEventSignalInterval>();
         }
    }
    
    public void deleteSignalsAndIntervals(Long eventId) {
   	 	try {  		 
		 	List<DrEventSignal> signals = loadDrEventSignalsByEventId(eventId);
		 	for(DrEventSignal signal: signals) {
		 		getSession().delete(signal);
		 	}
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
   }
    
    @SuppressWarnings("unchecked")
    public List<DrEvent> getAllQueuedDREvents() {
    	ArrayList<String> status = new ArrayList<String>();
    	status.add(EventStatusEnumeratedType.ACTIVE.value());
    	status.add(EventStatusEnumeratedType.FAR.value());
    	status.add(EventStatusEnumeratedType.NEAR.value());
    	
    	List<DrEvent> drEvents = new ArrayList<DrEvent>();
        try {
            drEvents = getSession().createCriteria(DrEvent.class, "drEvent").
            										add(Restrictions.in("drEvent.eventStatus", status)).
            										addOrder(Order.asc("id")).list();
            if (drEvents == null || drEvents.size() == 0) {
            	drEvents = new ArrayList<DrEvent>();
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return drEvents;
    }

}
