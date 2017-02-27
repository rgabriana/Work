package com.emscloud.dao;

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

import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;


@Repository("emStatsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStatsDao extends BaseDaoHibernate {
	static final Logger logger = Logger.getLogger(EmStatsDao.class.getName());
	
	@Resource
	SessionFactory sessionFactory;
	
	public List<EmStats> loadEmStatsByEmInstanceId(long id) {
		try{
			List<EmStats> results= null;
			String hsql = "from EmStats where em_instance_id=?";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	        q.setParameter(0, id);
	        results = q.list();
		
	        if (results != null && !results.isEmpty()) {
            	return results;
               
            }
		}
		
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
		return null;	
		
	}
	
	public List<EmStats> loadEmStatsByEmInstanceId(long id, int offset, int limit) {
		try{
			List<EmStats> results= null;
			String hsql = "from EmStats where em_instance_id=?";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString()).setMaxResults(limit).setFirstResult(offset);
	        q.setParameter(0, id);
	        results = q.list();
		
	        if (results != null && !results.isEmpty()) {
            	return results;
               
            }
		}
		
		catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
		return null;	
		
	}
	
	@SuppressWarnings("unchecked")
	public EmStatsList loadEmStatsListByEmInstanceId(Long id,String orderway, int offset, int limit) {
		EmStatsList emStatsList = new EmStatsList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = getSession()
				.createCriteria(EmStats.class, "emst")
				.setProjection(Projections.rowCount());
		
		oCriteria = getSession()
				.createCriteria(EmStats.class, "emst");
		
		oRowCount.add(Restrictions.eq("emst.emInstanceId", id));
		oCriteria.add(Restrictions.eq("emst.emInstanceId", id));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("emst.captureAt"));
		} 
		else {
			oCriteria.addOrder(Order.asc("emst.captureAt"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emStatsList.setTotal(count);
			emStatsList.setEmStats(oCriteria.list());
			return emStatsList;
		}
		
		return emStatsList;	
		
	}
	
	@SuppressWarnings("unchecked")
	public EmStats getLatestEmStatsByEmInstanceId(Long id){
		
		List<EmStats> emStatsList = sessionFactory.getCurrentSession().createCriteria(EmStats.class)
					.add(Restrictions.eq("emInstanceId",id))
					.setMaxResults(1)
					.addOrder(Order.desc("captureAt")).list();
		
		if(emStatsList != null && !emStatsList.isEmpty()){
			return emStatsList.get(0);
		}
		
		return null;
		
	}

}
