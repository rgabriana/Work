package com.ems.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Fixture;
import com.ems.model.Switch;
import com.ems.model.SwitchFixtures;

@Repository("switchFixtureDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchFixtureDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(SwitchFixtureDao.class.getName());

    public static final String SWITCHFIX_CONTRACTOR = "Select new SwitchFixtures(sf.id," + "sf.switchId,"
            + "sf.fixtureId)";

    // public static final String FIXTURE_CONTRACTOR = FixtureDaoImpl.FIXTURE_CONTRACTOR;

    public static final String SWITCH_CONTRACTOR = SwitchDao.SWITCH_CONTRACTOR;

    public List<Fixture> loadFixturesbySwitchId(Long id) {
        try {
            List<Fixture> results = null;
            String hsql = " from Fixture f, SwitchFixtures sf where f.id=sf.fixtureId and sf.switchId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public List<SwitchFixtures> loadSwitchFixturebyFixtureId(Long id) {
        try {
            List<SwitchFixtures> results = null;
            String hsql = SWITCHFIX_CONTRACTOR + " from SwitchFixtures sf where fixtureId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public List<SwitchFixtures> loadSwitchFixturebySwitchId(Long id) {
        try {
            List<SwitchFixtures> results = null;
            String hsql = SWITCHFIX_CONTRACTOR + " from SwitchFixtures sf where switchId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public Switch loadSwitchbyFixtureId(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Switch> loadSwitchesbyFixtureId(Long id) {
        try {
            List<Switch> results = null;
            String hsql = SWITCH_CONTRACTOR
                    + " from Switch s, SwitchFixtures sf where s.id=sf.switchId and sf.fixtureId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public SwitchFixtures update(SwitchFixtures switchFixtures) {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateSwitchFixtures(List<SwitchFixtures> switchFixtures) {
        // TODO Auto-generated method stub

    }

}
