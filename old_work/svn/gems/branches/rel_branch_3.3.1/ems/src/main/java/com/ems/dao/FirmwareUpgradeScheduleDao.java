package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.FirmwareUpgradeScheduleList;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("firmwareUpgradeScheduleDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FirmwareUpgradeScheduleDao extends BaseDaoHibernate {
    
	@SuppressWarnings("unchecked")
	public List<FirmwareUpgradeSchedule> loadAllFirmwareUpgradeSchedules() {
		
		Session session = getSession();
		return session.createCriteria(FirmwareUpgradeSchedule.class).list();		
		
	} //end of method loadAllFirmwareUpgradeSchedules
	
	@SuppressWarnings("unchecked")
	public FirmwareUpgradeScheduleList loadFirmwareUpgradeScheduleList(String orderby, String orderway, int offset, int limit) {
		FirmwareUpgradeScheduleList firmwareUpgradeScheduleList = new FirmwareUpgradeScheduleList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession()
					.createCriteria(FirmwareUpgradeSchedule.class, "firmwareupgradeschedule")
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());
		
		oCriteria = sessionFactory.getCurrentSession()
					.createCriteria(FirmwareUpgradeSchedule.class, "firmwareupgradeschedule")
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		if (orderby != null && !"".equals(orderby)) {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc(orderby));
			}else{
				oCriteria.addOrder(Order.asc(orderby));
			}
			
		} else {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc("addedTime"));
			}else{
				oCriteria.addOrder(Order.asc("addedTime"));
			}
		}
	
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			firmwareUpgradeScheduleList.setTotal(count);
			firmwareUpgradeScheduleList.setFirmwareUpgradeSchedules(oCriteria.list());
			return firmwareUpgradeScheduleList;
		}
		
		return firmwareUpgradeScheduleList;	
		
	}
	
	@SuppressWarnings("unchecked")
	public List<FirmwareUpgradeSchedule> getFirmwareUpgradeSchedule(String fileName) {
		
		Session session = getSession();
		List<FirmwareUpgradeSchedule> scheduleList = session.createCriteria(FirmwareUpgradeSchedule.class).				
				add(Restrictions.eq("fileName", fileName)).list();
		return scheduleList;
		
	} //end of method getFirmwareUpgradeSchedule

	public FirmwareUpgradeSchedule getFirmwareUpgradeSchedule(String fileName, String model) {
		
		Session session = getSession();
		FirmwareUpgradeSchedule schedule = (FirmwareUpgradeSchedule)session.createCriteria(FirmwareUpgradeSchedule.class).
				add(Restrictions.eq("modelNo", model)).
				add(Restrictions.eq("fileName", fileName)).uniqueResult();
		return schedule;
		
	} //end of method getFirmwareUpgradeSchedule
	
	public void addFirmwareUpgradeSchedule(FirmwareUpgradeSchedule fus) {
		
		Session session = getSession();
    session.saveOrUpdate(fus);
		
	} //end of method addFirmwareUpgradeSchedule
	
	public void modifyFirmwareUpgradeSchedule(FirmwareUpgradeSchedule fus) {
		
		Session session = getSession();
    session.saveOrUpdate(fus);
		
	} //end of method modifyFirmwareUpgradeSchedule

	@SuppressWarnings("unchecked")
	public List<FirmwareUpgradeSchedule> getAllActiveFirwareSchedules() {
  	
		Session session = getSession();
		List<FirmwareUpgradeSchedule> scheduleList = session.createCriteria(FirmwareUpgradeSchedule.class).
				add(Restrictions.eq("active", true)).list();
		return scheduleList;
		
  } //end of method getAllActiveFirwareSchedules
	
	public void deleteFirmwareSchedule(Long id) {
		
		removeObject(FirmwareUpgradeSchedule.class, id);
  	
  } //end of method deleteFirmwareSchedule
  
  public void deActivateFirmwareSchedule(String fileName, String deviceType, String modelNo) {
  	
  	Session session = getSession();
		FirmwareUpgradeSchedule schedule = (FirmwareUpgradeSchedule) session.createCriteria(FirmwareUpgradeSchedule.class).
				add(Restrictions.eq("fileName", fileName)).add(Restrictions.eq("deviceType", deviceType)).
				add(Restrictions.eq("modelNo", modelNo)).uniqueResult();
		schedule.setActive(false);
  	
  } //end of method deActivateFirmwareSchedule
  
  public void activateFirmwareSchedule(String fileName, String deviceType, String modelNo) {
	  	
	  	Session session = getSession();
			FirmwareUpgradeSchedule schedule = (FirmwareUpgradeSchedule) session.createCriteria(FirmwareUpgradeSchedule.class).
					add(Restrictions.eq("fileName", fileName)).add(Restrictions.eq("deviceType", deviceType)).
					add(Restrictions.eq("modelNo", modelNo)).uniqueResult();
			schedule.setActive(true);
	  	
   } //end of method activateFirmwareSchedule
  
  	@SuppressWarnings("unchecked")
	public FirmwareUpgradeSchedule getActiveFirmwareScheduleByModelNo(String deviceType,String modelNo) {
	
		Session session = getSession();
		List<FirmwareUpgradeSchedule> scheduleList = session.createCriteria(FirmwareUpgradeSchedule.class).
		add(Restrictions.eq("modelNo", modelNo)).add(Restrictions.eq("deviceType", deviceType)).add(Restrictions.eq("active", true)).list();
		if (!ArgumentUtils.isNullOrEmpty(scheduleList)) {
            return scheduleList.get(0);
        } else {
            return null;
        }
 	}

} //end of class FirmwareUpgradeScheduleDao