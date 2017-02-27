package com.ems.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.utils.ArgumentUtils;

/**
 * @author Shilpa Chalasani
 * 
 */
@Repository("gemsGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsGroupDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger("GemsGroupLogger");

    @SuppressWarnings("unchecked")
    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("company.id", companyId))
            		.add(Restrictions.isNotNull("floor.id"))
                    .list();

            if (!ArgumentUtils.isNullOrEmpty(results)) {
                return results;
            } else {
                return null;
            }
        } catch (HibernateException hbe) {
            logger.error("Error in loading data>>>>>>>>>>>>", hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<GemsGroup> loadObsoleteGroupsByCompany(Long companyId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("company.id", companyId))
                    .add(Restrictions.isNull("floor.id"))
                    .list();
            
            if (!ArgumentUtils.isNullOrEmpty(results)) {
                return results;
            } else {
                return null;
            }
        } catch (HibernateException hbe) {
            logger.error("Error in loading data>>>>>>>>>>>>", hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public List<GemsGroup> loadGroupsByBuilding(Long buildingId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class)
            		.add(Restrictions.sqlRestriction(" {alias}.floor_id in (select f.id from floor f where f.building_id = ? )", buildingId, Hibernate.LONG))
                    .list();

            if (!ArgumentUtils.isNullOrEmpty(results)) {
                return results;
            } else {
                return null;
            }
        } catch (HibernateException hbe) {
            logger.error("Error in loading data>>>>>>>>>>>>", hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<GemsGroup> loadGroupsByCampus(Long campusId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class)
            		.add(Restrictions.sqlRestriction(" {alias}.floor_id in (select f.id from floor f where f.building_id in (select b.id from building b where b.campus_id = ? ))", campusId, Hibernate.LONG))
                    .list();

            if (!ArgumentUtils.isNullOrEmpty(results)) {
                return results;
            } else {
                return null;
            }
        } catch (HibernateException hbe) {
            logger.error("Error in loading data>>>>>>>>>>>>", hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<GemsGroup> loadGroupsByFloor(Long floorId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("floor.id", floorId))
                    .addOrder(Order.asc("groupName"))
                    .list();

            if (!ArgumentUtils.isNullOrEmpty(results)) {
                return results;
            } else {
                return null;
            }
        } catch (HibernateException hbe) {
            logger.error("Error in loading data>>>>>>>>>>>>", hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    @SuppressWarnings("unchecked")
    public GemsGroup loadGroupsByGroupNameAndFloor(String groupName, Long floorId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("groupName", groupName))
            		.add(Restrictions.eq("floor.id", floorId))
                    .list();

            if (!ArgumentUtils.isNullOrEmpty(results)) {
                return results.get(0);
            } else {
                return null;
            }
        } catch (HibernateException hbe) {
            logger.error("Error in loading data>>>>>>>>>>>>", hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    public GemsGroup loadGemsGroup(Long id) {
        Session session = getSession();
        
        GemsGroup gemsGroup = (GemsGroup) session.load(GemsGroup.class, id);
        return gemsGroup;
    }

    public GemsGroup createNewGroup(GemsGroup gemsGroup) {
        Session s = getSession();
        s.saveOrUpdate(gemsGroup);
        return gemsGroup;
    }
    
    public GemsGroup saveGroup(GemsGroup gemsGroup) {
        Session s = getSession();
        s.saveOrUpdate(gemsGroup);
        return gemsGroup;
    }

    public void deleteGroup(GemsGroup gemsGroup) {
        Session s = getSession();
        s.delete(gemsGroup);
    }

    @SuppressWarnings("unchecked")
    public GemsGroupFixture getGemsGroupFixture(Long groupId, Long fixtureid) {
        Session session = getSession();
        List<GemsGroupFixture> results = session.createCriteria(GemsGroupFixture.class)
                .add(Restrictions.eq("group.id", groupId)).add(Restrictions.eq("fixture.id", fixtureid)).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<GemsGroupFixture> getGemsGroupFixtureByGroup(Long groupId) {
        Session session = getSession();
        List<GemsGroupFixture> results = session.createCriteria(GemsGroupFixture.class)
                .add(Restrictions.eq("group.id", groupId))
                .add(Restrictions.ne("userAction", GemsGroupFixture.USER_ACTION_FIXTURE_DELETE))
                .createAlias("group", "group")
                .createAlias("fixture", "fixture").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    public List<GemsGroupFixture> getAllGemsGroupFixtureByGroup(Long groupId) {
        Session session = getSession();
        List<GemsGroupFixture> results = session.createCriteria(GemsGroupFixture.class)
                .add(Restrictions.eq("group.id", groupId))
                .createAlias("group", "group")
                .createAlias("fixture", "fixture").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	public List<GemsGroupFixture> getGemsGroupFixtureByFixture(Long fixtureId) {
        Session session = getSession();
        List<GemsGroupFixture> results = session.createCriteria(GemsGroupFixture.class)
                .add(Restrictions.eq("fixture.id", fixtureId))
                .add(Restrictions.ne("userAction", GemsGroupFixture.USER_ACTION_FIXTURE_DELETE))
                .createAlias("group", "group")
                .createAlias("fixture", "fixture").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    
    public void saveGemsGroupFixtures(GemsGroupFixture groupFixture) {
        Session s = getSession();
        s.saveOrUpdate(groupFixture);
    }

    public void deleteGemsGroupFixtures(GemsGroupFixture groupFixture) {
        Session s = getSession();
        s.delete(groupFixture);
    }
    
    public void deleteGemsGroupsFromFixture(Long fixtureId) {
		String hsql = "delete from GemsGroupFixture where fixture.id=?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, fixtureId);
		q.executeUpdate();
    }
    
    public void deleteGemsGroup(Long groupId) {
		String hsql = "delete from GemsGroupFixture where group.id=?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, groupId);
		q.executeUpdate();
		removeObject(GemsGroup.class, groupId);
    }
}
