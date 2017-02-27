package com.ems.dao;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SweepTimer;
import com.ems.utils.ArgumentUtils;

@Repository("sweepTimerDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SweepTimerDao  extends BaseDaoHibernate {
	
    @SuppressWarnings("unchecked")
	public SweepTimer loadSweepTimerById(Long id) {
        List<SweepTimer> sweepTimer = getSession().createCriteria(SweepTimer.class)
                .add(Restrictions.eq("id", id)).list();
        if(sweepTimer.size() > 0) {
        	return sweepTimer.get(0);
        }
        return null;
    }
    
    
    @SuppressWarnings("unchecked")
	public List<SweepTimer> loadAllSweepTimer() {
    	
    	 List<SweepTimer> sweepTimerList = getSession().createCriteria(SweepTimer.class).addOrder(Order.asc("name")).list();
    	 if (!ArgumentUtils.isNullOrEmpty(sweepTimerList)) {
 			return sweepTimerList;
 		} else {
 			return null;
 		}
    }

    @SuppressWarnings("unchecked")
	public SweepTimer loadSweepTimerByName(String sweeptimername) {
		 List<SweepTimer> sweepTimer = getSession().createCriteria(SweepTimer.class)
	                .add(Restrictions.eq("name", sweeptimername)).list();
	        if(sweepTimer.size() > 0) {
	        	return sweepTimer.get(0);
	        }
	        return null;
	}
}
