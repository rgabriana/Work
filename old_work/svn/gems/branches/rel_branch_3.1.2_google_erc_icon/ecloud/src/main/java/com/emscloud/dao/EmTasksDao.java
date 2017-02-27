package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.TaskStatus;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmTasks;
import com.emscloud.model.EmTasksList;

@Repository("emTasksDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmTasksDao extends BaseDaoHibernate {
	
	static final Logger logger = Logger.getLogger(EmTasksDao.class.getName());
	
	
	/*
	 * TODO: Use the following kind of logic to sort tasks by priority/task_status/start_time, etc.
	 *  
	 * private static final String SEVERITY_ORDER = "(CASE WHEN severity = 'Info' THEN '1' " +
			"WHEN severity = 'Warning' THEN '2' " +
			"WHEN severity = 'Minor' THEN '3' " +
			"WHEN severity = 'Major' THEN '4' " +
			"WHEN severity = 'Critical' THEN '5' " +
			"  ELSE '6' END) as severity";
	 * Criteria data = null;
	 * data = getSession().createCriteria(EventsAndFault.class, "ef")
        			.createAlias("device", "fixt", CriteriaSpecification.INNER_JOIN)
        			//.createAlias("gateway", "gw", CriteriaSpecification.LEFT_JOIN)
        			.setFetchMode("fixt", FetchMode.JOIN)
        			//.setFetchMode("gw", FetchMode.JOIN)
        			;
		data.add(Restrictions.sqlRestriction("{alias}.active = ? ", active, Hibernate.BOOLEAN));
		data.add(Restrictions.or(Restrictions.sqlRestriction("lower(to_char({alias}.event_time, 'YYYY:MM:DD HH12:MI:SS AM')) like ? ", "%" + searchString.toLowerCase() + "%", Hibernate.STRING), 
        					Restrictions.or(Restrictions.ilike("fixt.location", searchString, MatchMode.ANYWHERE),
        						Restrictions.or(Restrictions.ilike("fixt.name", searchString, MatchMode.ANYWHERE),
        							Restrictions.or(Restrictions.ilike("ef.severity", searchString, MatchMode.ANYWHERE),
        								Restrictions.or(Restrictions.ilike("ef.eventType", searchString, MatchMode.ANYWHERE),
        									Restrictions.ilike("ef.description", "%" + searchString + "%")))))));
    	data.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
				.add(Projections.sqlProjection("to_char({alias}.event_time, 'YYYY:MM:DD - HH12:MI:SS AM') as eventTimeDisplay", new String[] {"eventTimeDisplay"}, new Type[] {Hibernate.STRING}), "eventTimeDisplay")
				.add(Projections.property("ef.severity"), "severityDisplay")
				.add(Projections.property("ef.eventType"), "eventType")
				.add(Projections.property("ef.description"), "description")
				.add(Projections.property("ef.active"), "active")
				.add(Projections.property("ef.resolvedOn"), "resolvedOn")
				.add(Projections.property("fixt.id"), "fixtureId")
				.add(Projections.property("fixt.location"), "fixturelocation")
				.add(Projections.property("fixt.floor.id"), "floorId")
				.add(Projections.property("fixt.buildingId"), "buildingId")
				.add(Projections.property("fixt.campusId"), "campusId")
				.add(Projections.property("fixt.name"), "name")
				.add(Projections.sqlProjection(SEVERITY_ORDER, new String[] { "severity"}, new Type[] {Hibernate.STRING}), "severity")
				.add(Projections.property("ef.eventTime"), "eventTime")
				);
		data.addOrder(Order.desc("severity"))
			.addOrder(Order.desc("eventTime"))
			;
		data.list()
	 * 
	 */

	@SuppressWarnings("unchecked")
	public List<EmTasks> getEmTasksByEmInstanceId(Long emInstanceId) {
		try {
			List<EmTasks> emTasksList = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "et")
			.add(Restrictions.eq("et.emInstanceId", emInstanceId))
			.list();
			if (emTasksList != null && !emTasksList.isEmpty()) {
				return emTasksList;
			} else {
				return new ArrayList<EmTasks>();
			}
		}
		catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<EmTasks> getEmTasksList() {
		try {
			List<EmTasks> emTasksList = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "et")
			.list();
			if (emTasksList != null && !emTasksList.isEmpty()) {
				return emTasksList;
			} else {
				return new ArrayList<EmTasks>();
			}
		}
		catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<EmTasks> getActiveEmTasksByEmInstanceId(Long emInstanceId) {
		try {
			List<TaskStatus> activeTaskStatusList = new ArrayList<TaskStatus>();
			activeTaskStatusList.add(TaskStatus.SCHEDULED);
			activeTaskStatusList.add(TaskStatus.IN_PROGRESS);
			
			List<EmTasks> emTasksList = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "et")
			.add(Restrictions.eq("et.emInstanceId", emInstanceId))
			.add(Restrictions.in("et.taskStatus", activeTaskStatusList ))
			.addOrder(Order.asc("et.id"))
			.list();
			if (emTasksList != null && !emTasksList.isEmpty()) {
				return emTasksList;
			} else {
				return new ArrayList<EmTasks>();
			}
		}
		catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}
	
	@SuppressWarnings("unchecked")
	public EmTasksList loadEmTaksList(String orderway, int offset, int limit) {
		EmTasksList emTasksList = new EmTasksList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "emtask").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "emtask");
	
		//oRowCount.add(Restrictions.eq("eminst.customer.id", id));
		//oCriteria.add(Restrictions.eq("eminst.customer.id", id));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("emtask.startTime"));
		} 
		else {
			oCriteria.addOrder(Order.asc("emtask.startTime"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emTasksList.setTotal(count);
			emTasksList.setEmTasks(oCriteria.list());
			return emTasksList;
		}
		
		return emTasksList;	
		
	}
	
	
	@SuppressWarnings("unchecked")
	public EmTasksList loadEmTaksListByEmInstanceId(String orderway, int offset, int limit, Long emInstanceId) {
		EmTasksList emTasksList = new EmTasksList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "emtask").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(EmTasks.class, "emtask");
	
		oRowCount.add(Restrictions.eq("emInstanceId", emInstanceId));
		oCriteria.add(Restrictions.eq("emInstanceId", emInstanceId));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("emtask.startTime"));
		} 
		else {
			oCriteria.addOrder(Order.asc("emtask.startTime"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emTasksList.setTotal(count);
			emTasksList.setEmTasks(oCriteria.list());
			return emTasksList;
		}
		
		return emTasksList;	
		
	}
	

}
