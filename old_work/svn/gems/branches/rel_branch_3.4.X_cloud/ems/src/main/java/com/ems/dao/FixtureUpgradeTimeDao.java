package com.ems.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FixtureUpgradeTime;

@Repository("fixtureUpgradeTimeDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureUpgradeTimeDao extends BaseDaoHibernate {

    public FixtureUpgradeTime getFixtureUpgradeTimeById(Long id) {
        Criteria criteria = getSession().createCriteria(FixtureUpgradeTime.class);
        criteria.add(Restrictions.eq("id", id));
        return (FixtureUpgradeTime) criteria.list().get(0);
    }
}
