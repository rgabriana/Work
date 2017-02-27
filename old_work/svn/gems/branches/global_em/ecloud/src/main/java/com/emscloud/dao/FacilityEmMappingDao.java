package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FacilityEmMappingList;
import com.emscloud.types.FacilityType;


@Repository("facilityEmMappingDao")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class FacilityEmMappingDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(FacilityEmMappingDao.class
			.getName());

	@Resource
	SessionFactory sessionFactory;

	public FacilityEmMapping getFacilityEmMapping(long id) {
		Object obj = getObject(FacilityEmMapping.class, id);
		if (obj == null)
			return null;
		else
			return (FacilityEmMapping) obj;
	}
	
	public void deleteFacilityEmMapping(long id) {
		Object obj = getObject(FacilityEmMapping.class, id);
		sessionFactory.getCurrentSession().delete(obj);
	}
    
    public FacilityEmMapping getFacilityEmMappingOnEmFloorId(Long emInstanceId, Long floorId){
    	FacilityEmMapping facEmMap =null ;
    	Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(FacilityEmMapping.class);
    	criteria.add(Restrictions.eq("emFacilityType", (long)FacilityType.FLOOR.ordinal()));
    	criteria.add(Restrictions.eq("emFacilityId", floorId));
    	criteria.add(Restrictions.eq("emId", emInstanceId));
    	facEmMap = (FacilityEmMapping)criteria.uniqueResult();
    	return facEmMap;
    }

	public FacilityEmMapping getFacilityEmMappingOnFacilityId(long id) {
		FacilityEmMapping facEmMap = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FacilityEmMapping.class);
		criteria.add(Restrictions.eq("facilityId", id));
		facEmMap = (FacilityEmMapping) criteria.uniqueResult();
		return facEmMap;
	}

	//this returns only floor facility mappings on the em
	public List<FacilityEmMapping> getFacilityEmMappingOnEmId(long emId) {
		ArrayList<FacilityEmMapping> facEmMap = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FacilityEmMapping.class);
		criteria.add(Restrictions.eq("emId", emId));
		criteria.add(Restrictions.eq("emFacilityType", new Long(FacilityType.FLOOR.ordinal())));
		facEmMap = (ArrayList<FacilityEmMapping>) criteria.list();
		if (!ArgumentUtils.isNullOrEmpty(facEmMap)) {
			return facEmMap;
		} else {
			return null;
		}
	}
	
	public void saveEmMapping(Long emInstId,Long emFacilityId,String emFacilityPath,Long cloudFacilityId, Long customerId) {
		FacilityEmMapping facEmMap = new FacilityEmMapping();
		facEmMap.setEmId(emInstId);
		facEmMap.setEmFacilityType((long)FacilityType.FLOOR.ordinal());
		facEmMap.setEmFacilityId(emFacilityId);
		facEmMap.setCustId(customerId);
		facEmMap.setFacilityId(cloudFacilityId);
		facEmMap.setEmFacilityPath(emFacilityPath);
		sessionFactory.getCurrentSession().saveOrUpdate(facEmMap);
	}
	
	@SuppressWarnings("unchecked")
	public List<FacilityEmMapping> getAllMappedFaciltyList() {
		List<FacilityEmMapping> results = new ArrayList<FacilityEmMapping>();
		String hsql = "from FacilityEmMapping order by id";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        results = q.list();		
        return results;
	}
	
	@SuppressWarnings("unchecked")
	public FacilityEmMappingList loadFacilityEmMappingListByCustomerId(String orderby,
			String orderway, Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit,Long customerId) {
		FacilityEmMappingList facilityEmMappingList = new FacilityEmMappingList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(FacilityEmMapping.class, "facilityEmMapping")
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				FacilityEmMapping.class, "facilityEmMapping");
		
		oRowCount.add(Restrictions.eq("facilityEmMapping.custId", customerId));
		oCriteria.add(Restrictions.eq("facilityEmMapping.custId", customerId));
		
		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("emFacilityId")) {
				orderby = "facilityEmMapping.emFacilityId";
			} else if (orderby.equals("emFacilityPath")) {
				orderby = "facilityEmMapping.emFacilityPath";
			} else if (orderby.equals("emId")) {
				orderby = "facilityEmMapping.emId";
			} else {
				orderby = "facilityEmMapping.id";
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
			if (searchField.equals("emFacilityId")) {
				oRowCount.add(Restrictions.eq("facilityEmMapping.emFacilityId", Long.parseLong(searchString) ));
				oCriteria.add(Restrictions.eq("facilityEmMapping.emFacilityId", Long.parseLong(searchString) ));
			}else if (searchField.equals("emFacilityPath")) {
				oRowCount.add(Restrictions.ilike("facilityEmMapping.emFacilityPath", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("facilityEmMapping.emFacilityPath", "%"
						+ searchString + "%"));
			}else if (searchField.equals("emId")) {
				oRowCount.add(Restrictions.eq("facilityEmMapping.emId", Long.parseLong(searchString) ));
				oCriteria.add(Restrictions.eq("facilityEmMapping.emId", Long.parseLong(searchString) ));
			}
		}

		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			facilityEmMappingList.setTotal(count);
			facilityEmMappingList.setFacilityEminsts((oCriteria.list()));
			return facilityEmMappingList;
		}

		return facilityEmMappingList;

	}
	
	public List<Long> getDistictMappedEMIdList()
	{
	    List<Long> emInstanceIds = new ArrayList<Long>();
	    Session session = sessionFactory.getCurrentSession();
	    Criteria criteria = session.createCriteria(FacilityEmMapping.class)
	                   .setProjection(Projections.distinct(Projections.property("emId")));
	    emInstanceIds = (List<Long>) criteria.list();
        return emInstanceIds;
	}
}
