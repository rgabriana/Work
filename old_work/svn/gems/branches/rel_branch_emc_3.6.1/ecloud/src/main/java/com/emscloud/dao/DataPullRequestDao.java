package com.emscloud.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.DataPullRequestStateType;
import com.emscloud.model.DataPullRequest;
import com.emscloud.vo.DataPullRequestList;

@Repository("dataPullRequestDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class DataPullRequestDao extends BaseDaoHibernate {
	
	@SuppressWarnings("unchecked")
	public DataPullRequest getTopDataPullRequestByReplicaId(Long replicaId) {
		List<DataPullRequest> list = 
				(List<DataPullRequest>)sessionFactory.getCurrentSession().createQuery(
						" from DataPullRequest dpr " +
						"where dpr.state in ('" + DataPullRequestStateType.Queued + "', '" + DataPullRequestStateType.Processing + "') " +
							"and dpr.em.replicaServer.id in ( " + replicaId + ") " +
						"order by dpr.requestedAt, dpr.id").list();
		if(list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<DataPullRequest>  getOlderRequests(Long replicaId, Integer days) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, days*-1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			List<DataPullRequest> list = 
					(List<DataPullRequest>)sessionFactory.getCurrentSession().createQuery(
							" from DataPullRequest dpr " +
							" where dpr.state not in ('" + DataPullRequestStateType.Deleted + "', '" 
														+ DataPullRequestStateType.Queued + "', '" 
														+ DataPullRequestStateType.Processing + "') " +
							" and dpr.em.replicaServer.id  in ( " + replicaId + ") " +														
							" and dpr.requestedAt <= '" + sdf.format(c.getTime()) + "'" +  
							" order by dpr.requestedAt, dpr.id").list();
			return list;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public DataPullRequestList loadDataByCustomerId(Long id, String orderby, String orderway, int offset, int limit) {
		DataPullRequestList dataPullList = new DataPullRequestList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(DataPullRequest.class, "dpr")
				.createAlias("em", "em", CriteriaSpecification.INNER_JOIN)
        			.setFetchMode("em", FetchMode.JOIN)
        		.createAlias("requestedBy", "requestedBy", CriteriaSpecification.INNER_JOIN)
        			.setFetchMode("requestedBy", FetchMode.JOIN)
        			
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				DataPullRequest.class, "dpr").createAlias("em", "em", CriteriaSpecification.INNER_JOIN)
    			.setFetchMode("em", FetchMode.JOIN).createAlias("requestedBy", "requestedBy", CriteriaSpecification.INNER_JOIN)
    			.setFetchMode("requestedBy", FetchMode.JOIN)
    			.createAlias("em.replicaServer", "replicaServer", CriteriaSpecification.INNER_JOIN)
    			.setFetchMode("em.replicaServer", FetchMode.JOIN);
		

		oRowCount.add(Restrictions.eq("em.customer.id", id));
		oCriteria.add(Restrictions.eq("em.customer.id", id));
		
		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("em")) {
				orderby = "em.name";
			} else if (orderby.equals("requestDate")) {
				orderby = "requestedAt";
			} else if (orderby.equals("requestedBy")) {
				orderby = "requestedBy.email";
			} else if (orderby.equals("replicaServer")) {
				orderby = "replicaServer.name";
			} else if (orderby.equals("from")) {
				orderby = "fromDate";
			} else if (orderby.equals("to")) {
				orderby = "toDate";
			} else if (orderby.equals("state")) {
				orderby = "state";
			} else if (orderby.equals("tableName")) {
				orderby = "tableName";
			} else if (orderby.equals("lastUpdateDate")) {
				orderby = "lastUpdatedAt";
			} else  {
				orderby = "id";
			}
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc(orderby));
			}else{
				oCriteria.addOrder(Order.asc(orderby));
			}
			
		} else {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc("id"));
			}else{
				oCriteria.addOrder(Order.asc("id"));
			}
		}

		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (count.compareTo(new Long("0")) > 0) {
			dataPullList.setTotal(count);
			dataPullList.setList(oCriteria.list());
			for(DataPullRequest d: dataPullList.getList()) {
				d.setEmName(d.getEm().getName());
				d.setUserName(d.getRequestedBy().getEmail());
				d.setReplicaServer(d.getEm().getReplicaServer().getName());
				d.setRequestDate(sdf.format(d.getRequestedAt()));
				d.setFrom(sdf.format(d.getFromDate()));
				d.setTo(sdf.format(d.getToDate()));
				d.setLastUpdateDate(sdf.format(d.getLastUpdatedAt()));
			}
		}

		return dataPullList;

	}

}
