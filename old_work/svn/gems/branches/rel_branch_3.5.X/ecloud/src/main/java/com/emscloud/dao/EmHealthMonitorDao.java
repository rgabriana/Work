package com.emscloud.dao;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Repository("emHealthMonitorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmHealthMonitorDao extends BaseDaoHibernate {
	
	@Resource
	private EmInstanceDao emInstanceDao;

	public void deleteEmHealthMonitorByEmId(Long id) {				
		String hsql = "delete from EmHealthMonitor where emInstance.id=?";
		Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
		q.setParameter(0, id);	
		q.executeUpdate();
	}

}
