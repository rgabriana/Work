package com.ems.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Building;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("buildingDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BuildingDao extends BaseDaoHibernate {

	//TODO no use case found. getAllBuildingsByCampusId already exists.
    /**
     * Load building by campus id
     * 
     * @param id
     *            Campus's database id.
     * @return com.ems.model.Building collection. load only id,name details of building. other details loads as null.
     */
    @SuppressWarnings("unchecked")
    public List<Building> loadBuildingByCampusId(Long id) {
        Session session = getSession();
        List<Building> buildingList = session.createCriteria(Building.class).add(Restrictions.eq("campus.id", id))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(buildingList)) {
            return buildingList;
        } else {
            return null;
        }
        // try{
        // List<Building> results = null;
        // String hsql = "Select new Building(b.id,b.name,b.profileHandler.id) from Building b where b.campus.id=?";
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

    public List<Building> getAllBuildingsByCampusId(Long campusId) {
        Session session = getSession();
        List<Building> buildingList = session.createCriteria(Building.class)
                .add(Restrictions.eq("campus.id", campusId)).list();
        if (!ArgumentUtils.isNullOrEmpty(buildingList)) {
            return buildingList;
        } else {
            return null;
        }

        // Query query = getSession().createQuery("from Building where campus.id=:campusId");
        // query.setLong("campusId", campusId);
        // List<Building> buildings = query.list();
        // return buildings;
    }

    public Building getBuildingById(Long id) {
        Session session = getSession();
        Building building = (Building) session.get(Building.class, id);
        return building;

        // Criteria criteria = getSession().createCriteria(Building.class);
        // criteria.add(Restrictions.eq("id", id));
        // List<Building> list = criteria.list();
        // if (list != null) {
        // return list.get(0);
        // } else {
        // return null;
        // }
    }
    
    public Building getBuildingByNameAndCampusId(String bldgName, Long campusId) {
        Session session = getSession();
        List<Building> buildingList = session.createCriteria(Building.class)
        		.add(Restrictions.eq("campus.id", campusId))
        		.add(Restrictions.eq("name", bldgName))
                .list();
        		
        		if(buildingList != null && buildingList.size() > 0)
        			return buildingList.get(0);
        		else
        			return null;
    }

    //TODO no use case found. getBuildingById already exists.
    /**
     * Load Building if id given
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Building
     */
    public Building loadBuilding(Long id) {

        Session session = getSession();
        Building building = (Building) session.get(Building.class, id);
        return building;

        // try {
        // List<Building> results = null;
        // String hsql = "Select new Building(b.id,b.name,b.profileHandler.id) from Building b where b.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return (Building) results.get(0);
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public Building editName(Building building) {
        Session session = getSession();
        session.update(building);
        return building;

        // getSession().createQuery("Update Building set name = :name where id = :id")
        // .setString("name", building.getName()).setLong("id", building.getId()).executeUpdate();
        // return loadBuilding(building.getId());
    }
    
    public Long getBuildingCount() {
    	Query query2 = getSession().createQuery("Select COUNT(*) from Building building");
    	return (Long) query2.list().get(0);
    }
}
