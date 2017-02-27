package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FixtureImageVersion;

@Repository("fixtureImageVersionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureImageVersionDao extends BaseDaoHibernate {

    public List<FixtureImageVersion> getAllActiveScheduleUpgrades() {
        Criteria criteria = getSession().createCriteria(FixtureImageVersion.class);
        criteria.add(Restrictions.eq("upgradeStatus", true)).addOrder(Order.asc("upgradeDate"));
        return criteria.list();
    }

    public Long getCurrentImageVersion() {
        SQLQuery sqlQuery = getSession()
                .createSQLQuery(
                        "select fixture_image_id from   fixture_image_version where upgrade_date =(select Max(upgrade_date) from  fixture_image_version where upgrade_date in (select upgrade_date from fixture_image_version where upgrade_status=false)  )");
        List list = sqlQuery.list();
        if (list.size() > 0 && list.get(0) != null) {
            return Long.valueOf(list.get(0).toString());
        } else {
            return null;
        }
    }
}
