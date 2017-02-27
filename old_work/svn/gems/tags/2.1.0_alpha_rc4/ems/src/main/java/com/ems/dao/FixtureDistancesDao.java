/**
 * 
 */
package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FixtureDistances;

/**
 * @author EMS
 * 
 */
@Repository("fixtureDistancesDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureDistancesDao extends BaseDaoHibernate {

    public void addFixtureDistance(FixtureDistances fd) {
        // TODO Auto-generated method stub

        getSession().saveOrUpdate(fd);

    } // end of method addFixtureDistance

    public List<FixtureDistances> loadAllFixtureDistances() {

        try {
            List<FixtureDistances> results = null;
            String hsql = "from FixtureDistances fd)";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;

    } // end of method loadAllFixtureDistances

    public List<FixtureDistances> getFixtureDistances(String snap) {

        try {
            List<FixtureDistances> results = null;
            String hsql = "from FixtureDistances fd where srcFixture = ? order by lightLevel";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, snap);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;

    } // end of method getFixtureDistances

    public void removeAllFixtureDistances() {

        try {
            Session s = getSession();
            String hql = "delete from FixtureDistances fd";
            Query query = s.createQuery(hql);
            query.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method removeAllFixtureDistances

} // end of class FixtureDistancesDaoImpl
