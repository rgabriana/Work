package com.ems.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupType;
import com.ems.server.ServerConstants;
import com.ems.types.GGroupType;
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
    public List<GemsGroupType> getGroupTypeList() {
        try {
            Session s = getSession();
            List<GemsGroupType> results = s.createCriteria(GemsGroupType.class).list();

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

    public void saveGemsGroupType(GemsGroupType gemsGroupType) {
        Session s = getSession();
        s.saveOrUpdate(gemsGroupType);
    }

    @SuppressWarnings("unchecked")
    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        try {
            Session s = getSession();
            List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("company.id", companyId))
            		.add(Restrictions.isNotNull("floor.id"))
                    .createAlias("type", "type").list();

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
                    .createAlias("type", "type").list();
            
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
                    .createAlias("type", "type").list();

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
                    .createAlias("type", "type").list();

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
                    .createAlias("type", "type").list();

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
                    .createAlias("type", "type").list();

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
        return (GemsGroup) session.load(GemsGroup.class, id);
    }

    public GemsGroup createNewGroup(GemsGroup gemsGroup) {
        GemsGroupType oGemsGroupType = new GemsGroupType();
        oGemsGroupType.setGroupNo(getNextGroupId());
        oGemsGroupType.setGroupTypeName(GGroupType.MotionGroup.getName());
        saveGemsGroupType(oGemsGroupType);

        Session s = getSession();
        gemsGroup.setType(oGemsGroupType);
        s.saveOrUpdate(gemsGroup);
        return gemsGroup;
    }
    
    public GemsGroup saveGroup(GemsGroup gemsGroup) {
        Session s = getSession();
        s.saveOrUpdate(gemsGroup);
        return gemsGroup;
    }
    
    public Integer getNextGroupId() {
        Integer iNextGroupId = ServerConstants.MOTION_GRP_START_NO;
        iNextGroupId = (Integer) getSession().createCriteria(GemsGroupType.class)
                    .setProjection(Projections.max("groupNo")).uniqueResult();
        if (iNextGroupId == null) {
            return ServerConstants.MOTION_GRP_START_NO;
        }
        return iNextGroupId + 1;
    }

    public void deleteGroup(GemsGroup gemsGroup) {
        Session s = getSession();
        s.delete(gemsGroup);
    }
    
    public void deleteGroupType(GemsGroupType gemsGroupType) {
        Session s = getSession();
        s.delete(gemsGroupType);
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
                .add(Restrictions.eq("group.id", groupId)).createAlias("group", "group")
                .createAlias("fixture", "fixture").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

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
                .add(Restrictions.eq("fixture.id", fixtureId)).createAlias("group", "group")
                .createAlias("fixture", "fixture").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    
    public void saveGemsGroupFixtures(GemsGroupFixture groupFixture) {
        Session s = getSession();
        s.save(groupFixture);
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
}
