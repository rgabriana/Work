package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Area;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("areaDao")
@Transactional(propagation = Propagation.REQUIRED)
public class AreaDao extends BaseDaoHibernate {

    /**
     * Load Area details.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Area collection. load only id,name,description details of area object. other details loads
     *         as null.
     */
    @SuppressWarnings("unchecked")
    public List<Area> loadAreaByFloorId(Long id) {
        Session session = getSession();
        List<Area> areaList = session.createCriteria(Area.class).add(Restrictions.eq("floor.id", id)).list();
        if (!ArgumentUtils.isNullOrEmpty(areaList)) {
            return areaList;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	public List<Area> getAllAreasByFloorId(Long floorId) {
        Session session = getSession();
        List<Area> areaList = session.createCriteria(Area.class).add(Restrictions.eq("floor.id", floorId)).list();
        if (!ArgumentUtils.isNullOrEmpty(areaList)) {
            return areaList;
        } else {
            return new ArrayList<Area>();
        }
    }

    public Area editName(Area area) {
        Session session = getSession();
        session.update(area);
        return area;
    }

}
