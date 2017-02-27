package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Floor;
import com.ems.model.User;
import com.ems.model.UserLocations;
import com.ems.types.FacilityType;

@Repository("userLocationsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class UserLocationsDao extends BaseDaoHibernate {
	
	
	@SuppressWarnings("unchecked")
	public boolean isFloorAssignedToUser(Long id) {
    	Criteria rowCount = getSession().createCriteria(UserLocations.class)
		.setProjection(Projections.rowCount())
		.add(Restrictions.eq("approvedLocationType", FacilityType.FLOOR))
		.add(Restrictions.eq("locationId", id));
    	List<Object> output = (List<Object>)rowCount.list();
    	Long count = (Long)output.get(0);
    	if(count.compareTo(new Long("0")) > 0) {
    		return true;
    	}
    	return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean isAreaAssignedToUser(Long id) {
    	Criteria rowCount = getSession().createCriteria(UserLocations.class)
		.setProjection(Projections.rowCount())
		.add(Restrictions.eq("approvedLocationType", FacilityType.AREA))
		.add(Restrictions.eq("locationId", id));
    	List<Object> output = (List<Object>)rowCount.list();
    	Long count = (Long)output.get(0);
    	if(count.compareTo(new Long("0")) > 0) {
    		return true;
    	}
    	return false;
	}

	public UserLocations loadUserLocation(long userId, FacilityType type, long locationId) {
		Criteria rowCount = getSession().createCriteria(UserLocations.class)
				.add(Restrictions.eq("user.id", userId))
				.add(Restrictions.eq("approvedLocationType", type))
				.add(Restrictions.eq("locationId", locationId));
		return (UserLocations)rowCount.uniqueResult();
	}

}
