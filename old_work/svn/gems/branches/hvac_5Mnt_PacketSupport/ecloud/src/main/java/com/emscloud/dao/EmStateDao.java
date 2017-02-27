package com.emscloud.dao;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.DatabaseState;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStateList;

@Repository("emStateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStateDao {

static final Logger logger = Logger.getLogger(EmStateDao.class.getName());
	
	@Resource
	SessionFactory sessionFactory;
	
	@SuppressWarnings("unchecked")
	public EmState loadEmStateById(long id) {
		try{
			
			List<EmState> emState =  sessionFactory.getCurrentSession().createCriteria(EmState.class)
					.add(Restrictions.eq("id",id))
					.addOrder(Order.desc("id")).list();
		
			if(emState != null && !emState.isEmpty()){
				return emState.get(0);
			}
		}	
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
		return null;			
	}
	
	@SuppressWarnings("unchecked")
	public EmState loadBeforeSyncFailedEmStateByEmInstanceId(long id) {
		try{
			
			List<EmState> emState =  sessionFactory.getCurrentSession().createCriteria(EmState.class)
					.add(Restrictions.eq("emInstanceId",id))
					.add(Restrictions.ne("databaseState", DatabaseState.SYNC_FAILED))
					.addOrder(Order.desc("id")).list();
		
			if(emState != null && !emState.isEmpty()){
				return emState.get(0);
			}
		}	
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
		return null;			
	}

	public EmState saveOrUpdate(EmState emState) {
		 sessionFactory.getCurrentSession().saveOrUpdate(emState);
		 return emState ;
	}

	@SuppressWarnings("unchecked")
	public EmState loadLastEmStatsByEmInstanceId(long id) {
		try{
			List<EmState> emStateList = sessionFactory.getCurrentSession().createCriteria(EmState.class)
					.add(Restrictions.eq("emInstanceId",id))
					.setMaxResults(1)
					.addOrder(Order.desc("id")).list();
		
		if(emStateList != null && !emStateList.isEmpty()){
			return emStateList.get(0);
		}
		}
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
		return null;	
		
	}
	
	@SuppressWarnings("unchecked")
	public EmStateList loadEmStateListByEmInstanceId(String orderway, int offset, int limit, Long emInstanceId) {
		EmStateList emStateList = new EmStateList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(EmState.class, "emstate").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(EmState.class, "emstate");
	
		oRowCount.add(Restrictions.eq("emInstanceId", emInstanceId));
		oCriteria.add(Restrictions.eq("emInstanceId", emInstanceId));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("emstate.setTime"));
		} 
		else {
			oCriteria.addOrder(Order.asc("emstate.setTime"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emStateList.setTotal(count);
			emStateList.setEmState(oCriteria.list());
			return emStateList;
		}
		
		return emStateList;	
		
	}
	
	@SuppressWarnings("unchecked")
	public EmState resetPreviousFlag(Long id) {
		try{
			List<EmState> emStateList = sessionFactory.getCurrentSession().createCriteria(EmState.class)
					.add(Restrictions.eq("emInstanceId",id))
					.addOrder(Order.desc("id")).list();
		
		if(emStateList != null && !emStateList.isEmpty()){
			 int index = 1; 
			 EmState ems = emStateList.get(index++);
			 while(ems != null && ems.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.REPLICA_UNREACHABLE.getName())) {
				 ems = emStateList.get(index++);
			 }
			 EmState newEmState = new EmState() ;
			 newEmState.setEmInstanceId(ems.getEmInstanceId());
			 newEmState.setEmStatus(ems.getEmStatus());
			 newEmState.setSetTime(Calendar.getInstance().getTime()) ;
			 DatabaseState state = ems.getDatabaseState();
			 if(ems.getDatabaseState().getName().equalsIgnoreCase(DatabaseState.MIGRATION_IN_PROGRESS.getName()))
			 {
				 state = DatabaseState.MIGRATION_READY ;
			 }
			 
			 newEmState.setDatabaseState(state) ;
			 newEmState.setFailedAttempts(ems.getFailedAttempts());
			 // we do not want duplicate logs 
			// newEmState.setLog(ems.getLog());
			
			 return saveOrUpdate(newEmState);
		}
		}
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
		return null ;	
	}

	public void deleteEmStateByEmId(Long id) {
		String hsql = "delete from EmState where emInstanceId=?";
		Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
		q.setParameter(0, id);
		q.executeUpdate();		
	}
}
