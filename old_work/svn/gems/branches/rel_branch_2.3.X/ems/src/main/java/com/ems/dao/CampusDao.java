package com.ems.dao;

import org.hibernate.Session;
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
    @SuppressWarnings("unchecked")
    public Campus loadCampusById(Long id) {
        Session session = getSession();
        Campus campus = (Campus) session.get(Campus.class, id);
        return campus;

        // try{
        // List<Campus> results = null;
        // String hsql =
        // "Select new Campus(c.id,c.name,c.location,c.zipcode,c.profileHandler.id) from Campus as c where c.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // results = q.list();
        // if(results != null && !results.isEmpty()){
        // return (Campus)results.get(0);
        // }
        // }catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public Campus editName(Campus campus) {
        Session session = getSession();
        session.update(campus);
        return campus;
        // getSession().createQuery("Update Campus set name = :name where id = :id").setString("name", campus.getName())
        // .setLong("id", campus.getId()).executeUpdate();
        // return loadCampusById(campus.getId());
    }
}
