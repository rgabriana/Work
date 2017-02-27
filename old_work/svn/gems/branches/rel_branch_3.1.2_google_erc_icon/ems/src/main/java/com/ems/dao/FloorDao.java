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
import com.ems.model.PlanMap;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("floorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorDao extends BaseDaoHibernate {

	//TODO no use case found. getAllFloorsByBuildingId exists.
    /**
     * Load Floor
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Floor collection load only id,name,description details of floor. other details loads as
     *         null
     */
    @SuppressWarnings("unchecked")
    public List<Floor> loadFloorByBuildingId(Long id) {
        Session session = getSession();
        List<Floor> floorList = session.createCriteria(Floor.class).add(Restrictions.eq("building.id", id)).list();
        if (!ArgumentUtils.isNullOrEmpty(floorList)) {
            return floorList;
        } else {
            return null;
        }

        // try{
        // List<Floor> results = null;
        // String hsql =
        // "Select new Floor(f.id,f.name,f.description,f.floorPlanUrl,f.profileHandler.id,f.planMap.id,f.noInstalledSensors,f.noInstalledFixtures) from Floor f where f.building.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // results = q.list();
        // if(results != null && !results.isEmpty()){
        // return results;
        // }
        // }catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

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
    
    public List<Floor> getAllFloorsByBuildingId(Long buildingId) {
        Session session = getSession();
        List<Floor> floorList = session.createCriteria(Floor.class).add(Restrictions.eq("building.id", buildingId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(floorList)) {
            return floorList;
        } else {
            return null;
        }

        // Query query = getSession().createQuery("from Floor where building.id = :buildingId");
        // query.setLong("buildingId", buildingId);
        // List<Floor> floors = query.list();
        // return floors;
    }

    //TODO not required.
    public Floor updateFloorPlan(Long floorId, PlanMap planMap) {
        Floor floor = (Floor) getSession().get(Floor.class, floorId);
        floor.setPlanMap(planMap);
        getSession().saveOrUpdate("planMap", floor);
        return floor;
    }

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

    //TODO no use case found. getFloorById exists.
    /**
     * load floor details by id
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Floor
     */
    public Floor loadFloor(Long id) {
        try {
            List<Floor> results = null;
            String hsql = "Select new Floor(f.id,f.name,f.description,f.floorPlanUrl,f.profileHandler.id,f.planMap.id,f.noInstalledSensors,f.noInstalledFixtures) from Floor f where f.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (Floor) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public Floor editName(Floor floor) {
        getSession().createQuery("Update Floor set name = :name where id = :id").setString("name", floor.getName())
                .setLong("id", floor.getId()).executeUpdate();
        return loadFloor(floor.getId());
    }

    //TODO no use case found.
    public Floor updateFields(Floor floor) {
        getSession()
                .createQuery(
                        "Update Floor set noInstalledSensors = :noInstalledSensors, noInstalledFixtures = :noInstalledFixtures where id = :floorId")
                .setInteger("noInstalledSensors", floor.getNoInstalledSensors())
                .setInteger("noInstalledFixtures", floor.getNoInstalledFixtures()).setLong("floorId", floor.getId())
                .executeUpdate();
        return floor;
    }
    
    public Long getFloorCount() {
    	Query query2 = getSession().createQuery("Select COUNT(*) from Floor floor");
    	return (Long) query2.list().get(0);
    }
}
