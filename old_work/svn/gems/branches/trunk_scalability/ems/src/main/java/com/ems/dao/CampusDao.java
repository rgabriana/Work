package com.ems.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Campus;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("campusDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CampusDao extends BaseDaoHibernate {

    /**
     * load Campus
     * 
     * @param id
     *            Database id(primary key)
     * @return com.ems.model.Campus object
     */
    public Campus loadCampusById(Long id) {
        Session session = getSession();
        Campus campus = (Campus) session.get(Campus.class, id);
        return campus;
    }

    public Campus editName(Campus campus) {
        Session session = getSession();
        session.update(campus);
        return campus;
    }
    
    @SuppressWarnings("unchecked")
	public Campus getCampusByName(String name) {
		List<Campus> campusList = getSession().createCriteria(Campus.class)
        .add(Restrictions.eq("name", name)).list();
		if(campusList.size() > 0) {
			return campusList.get(0);
		}
		return null;    
	}
}
