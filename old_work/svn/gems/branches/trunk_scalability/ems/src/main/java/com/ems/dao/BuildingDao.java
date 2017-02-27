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

    @SuppressWarnings("unchecked")
	public List<Building> getAllBuildingsByCampusId(Long campusId) {
        Session session = getSession();
        List<Building> buildingList = session.createCriteria(Building.class)
                .add(Restrictions.eq("campus.id", campusId)).list();
        if (!ArgumentUtils.isNullOrEmpty(buildingList)) {
            return buildingList;
        } else {
            return null;
        }
    }

    public Building getBuildingById(Long id) {
        Session session = getSession();
        Building building = (Building) session.get(Building.class, id);
        return building;
    }
    
    @SuppressWarnings("unchecked")
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

    public Building editName(Building building) {
        Session session = getSession();
        session.update(building);
        return building;
    }
    
    public Long getBuildingCount() {
    	Query query2 = getSession().createQuery("Select COUNT(*) from Building building");
    	return (Long) query2.list().get(0);
    }
}
