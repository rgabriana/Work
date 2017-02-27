package com.ems.dao;

import java.util.List;
import java.util.TimeZone;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Timezone;

/**
 * 
 * @author Abhishek sinha
 * 
 */
@Repository("timezoneDao")
@Transactional(propagation = Propagation.REQUIRED)
public class TimezoneDao extends BaseDaoHibernate {

    public Timezone getTimezoneById(Long id) {
        Criteria criteria = getSession().createCriteria(Timezone.class);
        criteria.add(Restrictions.eq("id", id));
        Timezone timezone = (Timezone) criteria.list().get(0);
        return timezone;
    }

    public Timezone getTimezoneByName(String timezoneName) {
        Criteria criteria = getSession().createCriteria(Timezone.class);
        criteria.add(Restrictions.eq("name", timezoneName));
        Timezone timezone = (Timezone) criteria.list().get(0);
        return timezone;
    }

    public List getTimeZoneList() {
        return loadAll(TimeZone.class);

    }
}
