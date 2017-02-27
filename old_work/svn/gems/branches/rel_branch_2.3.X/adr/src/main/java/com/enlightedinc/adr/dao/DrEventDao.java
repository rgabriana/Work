package com.enlightedinc.adr.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.adr.model.DrEvent;
import com.enlightedinc.adr.model.DrEventSignal;

/**
 * 
 * @author Kushal
 */
@Repository("drEventDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DrEventDao extends BaseDaoHibernate {
	
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

}
