package com.emscloud.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.CloudUserAudit;


@Repository("cloudUserAuditDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CloudUserAuditDao extends BaseDaoHibernate {
	
	
	
	
	/**
     * @param order (property on which to order the result)
     * @param orderWay (asc or desc)
     * @param filter (List of objects in order username, action_type, start time, end time)
     * @param offset (offset the result)
     * @param limit (number of result rows)
     * @return
     */

    @SuppressWarnings({ "unchecked", "deprecation" })
	public List<Object> getUserAudits(String order, String orderWay, List<Object> filter, int offset, int limit) {
    	
    	Criteria data = null;
    	Criteria rowCount = null;
    	
		rowCount = getSession().createCriteria(CloudUserAudit.class, "audit")
    			//.createAlias("users", "users", CriteriaSpecification.LEFT_JOIN)
    			//.setFetchMode("users", FetchMode.JOIN)
    			.setProjection(Projections.rowCount());
		
		data = getSession().createCriteria(CloudUserAudit.class, "audit");
    			//.createAlias("users", "users", CriteriaSpecification.LEFT_JOIN)
    			//.setFetchMode("users", FetchMode.JOIN);
		
    	if(filter != null) {

    		String username = (String)filter.get(0);
    		if(username != null) {
    			rowCount.add(Restrictions.ilike("users.email", username, MatchMode.ANYWHERE));
    			data.add(Restrictions.ilike("users.email", username, MatchMode.ANYWHERE));
    		}
    		
    		List<String> actionType = (List<String>)filter.get(1);
    		if(actionType != null && actionType.size() > 0) {
    			rowCount.add(Restrictions.in("audit.actionType", actionType));
    			data.add(Restrictions.in("audit.actionType", actionType));
    		}

    		Date startDate = (Date)filter.get(2);
    		if(startDate != null) {
    			rowCount.add(Restrictions.ge("audit.logTime", startDate));
    			data.add(Restrictions.ge("audit.logTime", startDate));
    		}
    		Date endDate = (Date)filter.get(3);
    		if(endDate != null) {
    			rowCount.add(Restrictions.le("audit.logTime", endDate));
    			data.add(Restrictions.le("audit.logTime", endDate));
    		}
    		
    		String ipAddress = (String)filter.get(4);
    		if(ipAddress != null) {
    			rowCount.add(Restrictions.ilike("audit.ipAddress", ipAddress, MatchMode.ANYWHERE));
    			data.add(Restrictions.ilike("audit.ipAddress", ipAddress, MatchMode.ANYWHERE));
    		}

    	}

    	data.setProjection(Projections.projectionList()
    			.add(Projections.property("id"), "id")
    			.add(Projections.sqlProjection("to_char({alias}.log_time, 'YYYY:MM:DD - HH12:MI:SS AM') as logTimeDisplay", new String[] {"logTimeDisplay"}, new Type[] {Hibernate.STRING}), "logTimeDisplay")
    			.add(Projections.property("audit.username"), "username")
    			.add(Projections.property("audit.actionType"), "actionType")
				.add(Projections.property("audit.description"), "description")
				.add(Projections.property("audit.logTime"), "logTime")
				.add(Projections.property("audit.ipAddress"), "ipAddress")
				);
    	
    	if(limit > 0) {
			data.setMaxResults(limit)
			.setFirstResult(offset);
		}
		
		if(order != null && !"".equals(order)) {
			if("desc".equals(orderWay)) {
				data.addOrder(Order.desc(order));
			}
			else {
				data.addOrder(Order.asc(order));
			}
		}
		else {
			data.addOrder(Order.desc("logTime"));
		}
    	
    	List<Object> output = (List<Object>)rowCount.list();
    	Long count = (Long)output.get(0);
    	if(count.compareTo(new Long("0")) > 0) {
    		output.addAll(data.list());
    	}
    	return output;
    	
    }

}