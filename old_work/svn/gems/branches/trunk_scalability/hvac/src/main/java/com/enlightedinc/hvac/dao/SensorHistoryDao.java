package com.enlightedinc.hvac.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.model.SensorHistory;

@Repository("sensorHistoryDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class SensorHistoryDao extends BaseDaoHibernate{
	
	public Object saveSensorHistory(SensorHistory sensorHistory) {
		return sessionFactory.getCurrentSession().save(sensorHistory);
	}
	
	@SuppressWarnings("unchecked")
	public Long getMaxHistoryId() {
		
		Long minId = 0L;
		try {
			String hsql = "select max(id) from sensor_history";
			Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object> results = q.list();
			if (results != null && !results.isEmpty() && results.get(0) != null) {
	            minId = Long.parseLong(results.get(0).toString());
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return minId;
	}


}
