package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Fixture;
import com.ems.model.FixtureGroup;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("fixtureGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureGroupDao extends BaseDaoHibernate {

    /**
     * load all fixture of group
     * 
     * @param id
     *            Group id
     * @return com.ems.dao.Fixture collection
     */
    @SuppressWarnings("unchecked")
    public List<Fixture> loadFixtureByGroupId(Long id) {
        List<Fixture> results = null;
        try {
            String hsql = "Select new Fixture(fg.fixture.id," + "fg.fixture.sensorId," + "fg.fixture.type,"
                    + "fg.fixture.ballastType," + "fg.fixture.ballastLastChanged," + "fg.fixture.noOfBulbs,"
                    + "fg.fixture.xaxis," + "fg.fixture.yaxis," + "fg.fixture.floor.id," + "fg.fixture.buildingId,"
                    + "fg.fixture.campusId," + "fg.fixture.area.id," + "fg.fixture.subArea.id,"
                    + "fg.fixture.dimmerControl," + "fg.fixture.currentState," + "fg.fixture.lastOccupancySeen,"
                    + "fg.fixture.lightLevel," + "fg.fixture.savingsType," + "fg.fixture.snapAddress,"
                    + "fg.fixture.fixtureName," + "fg.fixture.macAddress," + "fg.fixture.channel,"
                    + "fg.fixture.version," + "fg.fixture.aesKey," + "fg.fixture.bulbLife," + "fg.fixture.gateway.id,"
                    + "fg.fixture.description," + "fg.fixture.notes," + "fg.fixture.bulbsLastServiceDate,"
                    + "fg.fixture.ballastLastServiceDate," + "fg.fixture.active," + "fg.fixture.state,"
                    + "fg.fixture.ballast.id," + "fg.fixture.ballast.itemNum," + "fg.fixture.ballast.ballastName,"
                    + "fg.fixture.ballast.inputVoltage," + "fg.fixture.ballast.lampType,"
                    + "fg.fixture.ballast.lampNum," + "fg.fixture.ballast.ballastFactor,"
                    + "fg.fixture.ballast.wattage," + "fg.fixture.ballast.ballastManufacturer," + "fg.fixture.bulb.id,"
                    + "fg.fixture.bulb.manufacturer," + "fg.fixture.bulb.bulbName," + "fg.fixture.bulb.type,"
                    + "fg.fixture.bulb.initialLumens," + "fg.fixture.bulb.designLumens," + "fg.fixture.bulb.energy,"
                    + "fg.fixture.bulb.lifeInsStart," + "fg.fixture.bulb.lifeProgStart," + "fg.fixture.bulb.diameter,"
                    + "fg.fixture.bulb.length," + "fg.fixture.bulb.cri," + "fg.fixture.bulb.colorTemp,"
                    + "fg.fixture.currentProfile," + "fg.fixture.originalProfileFrom," + "fg.fixture.location,"
                    + "fg.fixture.noOfFixtures," + "fg.fixture.profileHandler.id," + "fg.fixture.lastConnectivityAt,"
                    + "fg.fixture.ipAddress," + "fg.fixture.commType)" + " from FixtureGroup fg where fg.group.id = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return results;
    }

    /**
     * Loads all fixtureGroups within a group
     * 
     * @param id
     * @return com.ems.model.FixtureGroup collection
     */
    @SuppressWarnings("unchecked")
    public List<FixtureGroup> loadFixtureGroupByGroupId(Long id) {
        try {
            String sql = "from FixtureGroup where group.id = :groupId";
            Query query = getSession().createQuery(sql.toString());
            query.setLong("groupId", id);
            List<FixtureGroup> fixtureGroups = query.list();
            if (!ArgumentUtils.isNullOrEmpty(fixtureGroups)) {
                return fixtureGroups;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Loads all fixtureGroups within a floor
     * 
     * @param id
     * @return com.ems.model.FixtureGroup collection
     */
    @SuppressWarnings("unchecked")
    public List<FixtureGroup> loadFixtureGroupByFloorId(Long id) {
        try {
            String sql = "from FixtureGroup where fixture.floor.id = :floorId";
            Query query = getSession().createQuery(sql.toString());
            query.setLong("floorId", id);
            List<FixtureGroup> fixtureGroups = query.list();
            if (!ArgumentUtils.isNullOrEmpty(fixtureGroups)) {
                return fixtureGroups;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Loads all fixtureGroups within an area
     * 
     * @param id
     * @return com.ems.model.FixtureGroup collection
     */
    @SuppressWarnings("unchecked")
    public List<FixtureGroup> loadFixtureGroupByAreaId(Long id) {
        try {
            String sql = "from FixtureGroup where fixture.area.id = :areaId";
            Query query = getSession().createQuery(sql.toString());
            query.setLong("areaId", id);
            List<FixtureGroup> fixtureGroups = query.list();
            if (!ArgumentUtils.isNullOrEmpty(fixtureGroups)) {
                return fixtureGroups;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Loads all fixtureGroups within a sub-area
     * 
     * @param id
     * @return com.ems.model.FixtureGroup collection
     */
    @SuppressWarnings("unchecked")
    public List<FixtureGroup> loadFixtureGroupBySubareaId(Long id) {
        try {
            String sql = "from FixtureGroup where fixture.subArea.id = :subareaId";
            Query query = getSession().createQuery(sql.toString());
            query.setLong("subareaId", id);
            List<FixtureGroup> fixtureGroups = query.list();
            if (!ArgumentUtils.isNullOrEmpty(fixtureGroups)) {
                return fixtureGroups;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
}
