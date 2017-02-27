package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GemsGroup;
import com.ems.model.MotionGroup;
import com.ems.model.MotionGroupFixtureDetails;
import com.ems.utils.ArgumentUtils;

@Repository("motionGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionGroupDao extends BaseDaoHibernate {

    @SuppressWarnings("unchecked")
    public String getNextGroupNo() {
        String hsql = "select nextval('group_no_seq')";
        Query q = getSession().createSQLQuery(hsql.toString());
        List<Object> output = (List<Object>) q.list();
        return ("000000" + output.get(0).toString()).substring(0, 6 - output.get(0).toString().length())
                + output.get(0).toString();
    }

    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        Session s = getSession();
        List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.isNotNull("floor.id"))
                .add(Restrictions.sqlRestriction(" {alias}.id in (select gems_group_id from motion_group m)"))
                .addOrder(Order.desc("groupName")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    public List<GemsGroup> loadGroupsByCampus(Long campusId) {
        Session s = getSession();
        List<GemsGroup> results = s
                .createCriteria(GemsGroup.class)
                .add(Restrictions
                        .sqlRestriction(
                                " {alias}.floor_id in (select f.id from floor f where f.building_id in (select b.id from building b where b.campus_id = ? ))",
                                campusId, Hibernate.LONG))
                .add(Restrictions.sqlRestriction(" {alias}.id in (select gems_group_id from motion_group m)")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    public List<GemsGroup> loadGroupsByBuilding(Long buildingId) {
        Session s = getSession();
        List<GemsGroup> results = s
                .createCriteria(GemsGroup.class)
                .add(Restrictions.sqlRestriction(
                        " {alias}.floor_id in (select f.id from floor f where f.building_id = ? )", buildingId,
                        Hibernate.LONG))
                .add(Restrictions.sqlRestriction(" {alias}.id in (select gems_group_id from motion_group m)")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    public List<GemsGroup> loadGroupsByFloor(Long floorId) {
        Session s = getSession();
        List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("floor.id", floorId))
                .add(Restrictions.sqlRestriction(" {alias}.id in (select gems_group_id from motion_group m)")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return new ArrayList<GemsGroup>();
        }
    }

    public List<GemsGroup> loadObsoleteGroupsByCompany(Long companyId) {
        Session s = getSession();
        List<GemsGroup> results = s.createCriteria(GemsGroup.class)
                .add(Restrictions.isNull("floor.id")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }

    public MotionGroup getMotionGroupByGemsGroupId(Long gemsGroupId) {
        Session s = getSession();
        MotionGroup motionGroup = (MotionGroup)s.createCriteria(MotionGroup.class)
                .add(Restrictions.eq("gemsGroup.id", gemsGroupId)).uniqueResult();
        return motionGroup;
    }
    
    public MotionGroup getMotionGroupByGroupNo(int groupNo) {
    	
  		Session session = getSession();
  		MotionGroup motionGroup = (MotionGroup)session.createCriteria(MotionGroup.class).
    			add(Restrictions.eq("groupNo", groupNo)).uniqueResult();
    	return motionGroup;
    	
    } //end of method getMotionGroupByGroupNo

	public MotionGroupFixtureDetails getMotionGroupFixtureDetails(Long gemsgroupFixtureId) {
		Session session = getSession();
		MotionGroupFixtureDetails mgfd = (MotionGroupFixtureDetails)session.createCriteria(MotionGroupFixtureDetails.class).
    			add(Restrictions.eq("gemsGroupFixture.id", gemsgroupFixtureId)).uniqueResult();
    	return mgfd;	
    }

}
