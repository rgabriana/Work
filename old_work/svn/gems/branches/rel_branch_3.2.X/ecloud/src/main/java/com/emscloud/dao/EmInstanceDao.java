package com.emscloud.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;


@Repository("emInstanceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceDao extends BaseDaoHibernate {
	static final Logger logger = Logger
			.getLogger(EmInstanceDao.class.getName());

	@Resource
	SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public List<EmInstance> loadAllEmInstances() {
		try {
			List<EmInstance> emInstanceList = sessionFactory
					.getCurrentSession().createCriteria(EmInstance.class)
					.addOrder(Order.asc("name")).list();
			if (emInstanceList != null && !emInstanceList.isEmpty()) {
				return emInstanceList;
			} else {
				return null;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	@SuppressWarnings("unchecked")
	public List<EmInstance> getActiveEmInstanceWithDataSynch() {
		List<EmInstance> emInstanceList = null;
		Criteria oCriteria = sessionFactory.getCurrentSession().createCriteria(
				EmInstance.class, "eminst");
		oCriteria.add(Restrictions.eq("eminst.active", true));
		oCriteria.add(Restrictions.eq("sppaEnabled", true));
		emInstanceList = oCriteria.list();
		return emInstanceList;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmInstance> getActiveEmInstance() {
		List<EmInstance> emInstanceList = null;
		Criteria oCriteria = sessionFactory.getCurrentSession().createCriteria(
				EmInstance.class, "eminst");
		oCriteria.add(Restrictions.eq("eminst.active", true));
		emInstanceList = oCriteria.list();
		return emInstanceList;
	}

	public EmInstanceList getHealthOfEmInstancesWithDataSynch(String orderby,
			String orderway, String searchField, String searchString, String searchOper, int offset, int limit) {
		
		return getEMHealthData(null, orderby, orderway, searchField, searchString, searchOper, offset, limit);

	}
	
	public EmInstanceList getHealthOfEmInstancesWithDataSynchByCustomerList(List<Long> custList,String orderby,
			String orderway, String searchField, String searchString, String searchOper, int offset, int limit) {
		
		return getEMHealthData(custList, orderby, orderway, searchField, searchString, searchOper, offset, limit);
	}
	
	private EmInstanceList getEMHealthData(List<Long> custList,String orderby,
			String orderway, String searchField, String searchString, String searchOper, int offset, int limit)
	{
		EmInstanceList oEmInstList = new EmInstanceList();
		Criteria oCriteria = sessionFactory.getCurrentSession().createCriteria(EmInstance.class, "eminst");
		oCriteria.createAlias("customer", "cust");
		oCriteria.add(Restrictions.eq("eminst.active", true));
		oCriteria.add(Restrictions.eq("eminst.sppaEnabled", true));
		if(!CollectionUtils.isEmpty(custList))
		{
			oCriteria.add(Restrictions.in("cust.id", custList));
		}
		if (StringUtils.isNotBlank(searchString)&&StringUtils.isNotBlank(searchField)) 
		{
			if("customerName".equals(searchField))
			{
				searchField = "cust.name";
				oCriteria.add(Restrictions.ilike(searchField, "%"+ searchString + "%"));
			}
			else if ("emInstanceName".equals(searchField)) 
			{
				searchField = "eminst.name";
				oCriteria.add(Restrictions.ilike(searchField, "%"+ searchString + "%"));
			}
			else if ("lastEmConnectivity".equals(searchField)) 
			{
				searchField = "to_char(eminst.lastConnectivityAt)";
				oCriteria.add(Restrictions.sqlRestriction("to_char(last_connectivity_at,'yyyy-MM-dd HH:mm') like ?", "%"+ searchString + "%", new StringType()));
			} 
			
		}
		if("customerName".equals(orderby))
		{
			orderby = "cust.name";
		}
		else if ("emInstanceName".equals(orderby)) 
		{
			orderby = "eminst.name";
		}
		else if ("lastEmConnectivity".equals(orderby)) 
		{
			orderby = "eminst.lastConnectivityAt";
		} 
		else 
		{
			orderby = "cust.name";
		}
		
		Long count  = (Long)oCriteria.setProjection(Projections.rowCount()).uniqueResult();
		
		if(count.longValue()>0)
		{
			if ("desc".equals(orderway))
			{
				oCriteria.addOrder(Order.desc(orderby));
			}
			else
			{
				oCriteria.addOrder(Order.asc(orderby));
			}
			oCriteria.setProjection(null);
			oCriteria.setResultTransformer(Criteria.ROOT_ENTITY);
			if (limit > 0) 
			{
				oCriteria.setMaxResults(limit).setFirstResult(offset);
				oEmInstList.setTotal(count);
				oEmInstList.setEmInsts(oCriteria.list());
			}
		}
		return oEmInstList;
	}
	
	public List<Long> getActiveEmInstanceIds() {
		List<Long> activeEmInstanceIds = null;
		// Criteria criteria =
		// sessionFactory.getCurrentSession().createCriteria(arg0)
		return activeEmInstanceIds;
	}

	@SuppressWarnings("unchecked")
	public List<EmInstance> loadEmInstancesByCustomerId(long id) {
		try {
			List<EmInstance> emInstanceList = sessionFactory
					.getCurrentSession().createCriteria(EmInstance.class)
					.add(Restrictions.eq("customer.id", id))
					.addOrder(Order.asc("name")).list();
			if (emInstanceList != null && !emInstanceList.isEmpty()) {
				return emInstanceList;
			} else {
				return null;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	@SuppressWarnings("unchecked")
	public EmInstanceList loadEmInstanceListByCustomerId(Long id,String orderby,
			String orderway, Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		EmInstanceList emInstanceList = new EmInstanceList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(EmInstance.class, "eminst")
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				EmInstance.class, "eminst");

		oRowCount.add(Restrictions.eq("eminst.customer.id", id));
		oCriteria.add(Restrictions.eq("eminst.customer.id", id));
		
		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("name")) {
				orderby = "eminst.name";
			} else if (orderby.equals("version")) {
				orderby = "eminst.version";
			} else if (orderby.equals("macId")) {
				orderby = "eminst.macId";
			} else if (orderby.equals("timeZone")) {
				orderby = "eminst.timeZone";
			} else if (orderby.equals("utcLastConnectivityAt")) {
				orderby = "eminst.lastConnectivityAt";
			} else if (orderby.equals("ipAddress")) {
				orderby = "eminst.ipAddress";
			} else {
				orderby = "eminst.id";
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
		
		if (bSearch) {
			if (searchField.equals("name")) {
				oRowCount.add(Restrictions.ilike("eminst.name", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("eminst.name", "%"
						+ searchString + "%"));
			}else if (searchField.equals("version")) {
				oRowCount.add(Restrictions.like("eminst.version", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("eminst.version", "%"
						+ searchString + "%"));
			}else if (searchField.equals("macId")) {
				oRowCount.add(Restrictions.ilike("eminst.macId", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("eminst.macId", "%"
						+ searchString + "%"));
			}else if (searchField.equals("timeZone")) {
				oRowCount.add(Restrictions.ilike("eminst.timeZone", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("eminst.timeZone", "%"
						+ searchString + "%"));
			}
		}

		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emInstanceList.setTotal(count);
			emInstanceList.setEmInsts(oCriteria.list());
			return emInstanceList;
		}

		return emInstanceList;

	}

	@SuppressWarnings("unchecked")
	public EmInstanceList loadUnregEmInstances(String orderby, String orderway, Boolean bSearch, String searchField, String searchString, String searchOper, int offset,
			int limit) {
		EmInstanceList emInstanceList = new EmInstanceList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(EmInstance.class, "eminst")
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				EmInstance.class, "eminst");

		oRowCount.add(Restrictions.eq("eminst.active", false));
		oCriteria.add(Restrictions.eq("eminst.active", false));

		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("version")) {
				orderby = "eminst.version";
			} else if (orderby.equals("macId")) {
				orderby = "eminst.macId";
			}else {
				orderby = "eminst.id";
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
		
		if (bSearch) {
			if (searchField.equals("version")) {
				oRowCount.add(Restrictions.like("eminst.version", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("eminst.version", "%"
						+ searchString + "%"));
			}else if (searchField.equals("macId")) {
				oRowCount.add(Restrictions.ilike("eminst.macId", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("eminst.macId", "%"
						+ searchString + "%"));
			}
		}
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("eminst.version"));
		} else {
			oCriteria.addOrder(Order.asc("eminst.version"));
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			emInstanceList.setTotal(count);
			emInstanceList.setEmInsts(oCriteria.list());
			return emInstanceList;
		}

		return emInstanceList;

	}

	@SuppressWarnings("unchecked")
	public List<EmInstance> loadEmInstanceByReplicaServerId(Long id) {
		try {
			List<EmInstance> emInstanceList = sessionFactory
					.getCurrentSession().createCriteria(EmInstance.class)
					.add(Restrictions.eq("replicaServer.id", id))
					.addOrder(Order.asc("name")).list();

			if (emInstanceList != null && !emInstanceList.isEmpty()) {
				return emInstanceList;
			} else {
				return null;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	public EmInstance loadEmInstanceById(long id) {
		try {

			EmInstance emInstance = (EmInstance) sessionFactory
					.getCurrentSession().createCriteria(EmInstance.class)
					.add(Restrictions.eq("id", id)).uniqueResult();

			return emInstance;
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	public EmInstance loadEmInstanceByMac(String mac) {
		try {

			EmInstance emInstance = (EmInstance) sessionFactory
					.getCurrentSession().createCriteria(EmInstance.class)
					.add(Restrictions.eq("macId", mac).ignoreCase()).uniqueResult();

			return emInstance;
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}

	public EmInstance saveOrUpdate(EmInstance instance) {
		sessionFactory.getCurrentSession().saveOrUpdate(instance);
		return instance;
	}

	public void deleteById(Long id) {
		String hsql = "delete from EmInstance where id=?";
		Query q = sessionFactory.getCurrentSession().createQuery(
				hsql.toString());
		q.setParameter(0, id);
		q.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public Customer getCustomer(String mac) {
		try {
			List<EmInstance> results = null;
			String hsql = "from EmInstance u where u.macId=?";
			Query q = sessionFactory.getCurrentSession().createQuery(
					hsql.toString());
			q.setParameter(0, mac);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				EmInstance em = (EmInstance) results.get(0);
				return em.getCustomer();

			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public EmInstance getEmInstanceForMac(String mac)
	{
            List<EmInstance> results = null;
            String hsql = "from EmInstance u where u.macId=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, mac);
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	EmInstance em  = (EmInstance) results.get(0);
                return em;               
           }
        return null;
	}

	public List<EmInstance> loadAllEmInstance() {
		try {

			ArrayList<EmInstance> emInstanceList = (ArrayList<EmInstance>) sessionFactory
					.getCurrentSession().createCriteria(EmInstance.class).list();

			return emInstanceList;
		} catch (HibernateException hbe) {
			hbe.printStackTrace();
		}
		return null;
	}
	public EmInstance getEmInstance(long id) {
    	Object obj = getObject(EmInstance.class, id);
    	if(obj == null )
    		return null;
    	else
    		return (EmInstance)obj;  
    }
	
	 public List<Object[]> getEmCountByReplicaServer()
	 {
		 String query = "select DISTINCT (e.replicaServer.id)  as replicaId ,count(e.id) as emCount  from EmInstance e, ReplicaServer r, Customer c where e.customer.id = c.id and e.replicaServer.id = r.id and e.sppaEnabled='true' and e.databaseName is NOT NULL group by "+
				 		"e.replicaServer.id order by e.replicaServer.id";
		 Query q = sessionFactory.getCurrentSession().createQuery(query.toString());
         List<Object[]> emCountList = new ArrayList<Object[]>();
         List<Object[]> resObj = q.list();
         Iterator<Object[]> itr = resObj.iterator();
         while(itr.hasNext()) {
             Object[] emCountObj = new Object[2];
             Object[] Obj = itr.next();
             emCountObj[0] = (Long) Obj[0];
             emCountObj[1] =(Long) Obj[1];
             emCountList.add(emCountObj);
         }
         if(emCountList==null && emCountList.size()==0)
         {
             logger.debug("getEmCountByReplicaServer() returned null while fetching Count of data of EMInstance");
         }
         return emCountList;  
	 }
	 
		@SuppressWarnings("unchecked")
		public String loadKeyByMac(String mac) {
			List<EmInstance> results = new ArrayList<EmInstance>();
			String hsql = "from EmInstance where replace(macId, ':', '') = ?";
	        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	        q.setString(0, mac);
	        results = q.list();	
	        if(results != null && results.size() > 0) {
	        	return results.get(0).getSecretKey();
	        }
	        return null;
		}
		
	    public void evict(EmInstance em) {
	    	getSession().evict(em);
	    }

}
