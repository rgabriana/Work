package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Floor;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("floorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorDao extends BaseDaoHibernate {

    @SuppressWarnings("rawtypes")
	public List getAllFloorsOfCompany() {
      
      try {       
        String hsql = "select f.id, f.name, building_id, campus_id, company_id, " +
            "f.description, floorplan_url from floor f, building b, campus  c " +
            " where f.building_id = b.id and b.campus_id = c.id order by f.id";
        Query q = getSession().createSQLQuery(hsql.toString());       
        List results = q.list();
        if (results != null && !results.isEmpty()) {
            return results;
        }
      } catch (HibernateException hbe) {
        throw SessionFactoryUtils.convertHibernateAccessException(hbe);
      }
      return null;
      
    } //end of method getAllFloorsOfCompany   
    
    @SuppressWarnings("unchecked")
	public List<Floor> getAllFloorsByBuildingId(Long buildingId) {
        Session session = getSession();
        List<Floor> floorList = session.createCriteria(Floor.class).add(Restrictions.eq("building.id", buildingId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(floorList)) {
            return floorList;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	public Floor getFloorById(Long id) {
        Criteria criteria = getSession().createCriteria(Floor.class);
        criteria.add(Restrictions.eq("id", id));
        List<Floor> list = criteria.list();
        if (list != null) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	public Floor getFloorByNameAndBuildingId(String floorName, Long bldgId) {
        Criteria criteria = getSession().createCriteria(Floor.class);
        criteria.add(Restrictions.eq("name", floorName));
        criteria.add(Restrictions.eq("building.id", bldgId));
        List<Floor> list = criteria.list();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public Floor editName(Floor floor) {
        getSession().createQuery("Update Floor set name = :name where id = :id").setString("name", floor.getName())
                .setLong("id", floor.getId()).executeUpdate();
        return getFloorById(floor.getId());
    }
    
    public Long getFloorCount() {
    	Query query2 = getSession().createQuery("Select COUNT(*) from Floor floor");
    	return (Long) query2.list().get(0);
    }
}
