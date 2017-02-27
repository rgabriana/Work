/**
 * 
 */
package com.ems.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.MotionBitsScheduler;
import com.ems.utils.ArgumentUtils;

/**
 * @author Shilpa Nene
 * 
 */
@Repository("motionBitsSchedulerDao")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionBitsSchedulerDao extends BaseDaoHibernate {

    @SuppressWarnings("unchecked")
	public MotionBitsScheduler loadMotionBitsScheduleById(Long id) {
        List<MotionBitsScheduler> motionBitSchedule = getSession().createCriteria(MotionBitsScheduler.class)
                .add(Restrictions.eq("id", id)).list();
        if(motionBitSchedule.size() > 0) {
        	return motionBitSchedule.get(0);
        }
        return null;
    }
    
    
    @SuppressWarnings("unchecked")
	public List<MotionBitsScheduler> loadAllMotionBitsSchedules() {
    	
    	 List<MotionBitsScheduler> motionBitScheduleList = getSession().createCriteria(MotionBitsScheduler.class).addOrder(Order.asc("name")).list();
    	 if (!ArgumentUtils.isNullOrEmpty(motionBitScheduleList)) {
 			return motionBitScheduleList;
 		} else {
 			return null;
 		}
    }
}
