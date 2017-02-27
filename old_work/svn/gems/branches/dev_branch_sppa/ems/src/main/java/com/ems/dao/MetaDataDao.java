package com.ems.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.EventType;
import com.ems.model.Groups;
import com.ems.model.WeekDay;
import com.ems.utils.ArgumentUtils;

@Repository("metaDataDao")
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataDao extends BaseDaoHibernate {

    @SuppressWarnings("unchecked")
    public List<EventType> getEventTypes() {
        List<EventType> eventTypes = getSession().createCriteria(EventType.class)
        		.add(Restrictions.eq("active", (short)1)).list();
        if (!ArgumentUtils.isNullOrEmpty(eventTypes)) {
            return eventTypes;
        }
        return null;
    }

    public WeekDay saveOrUpdateWeekDay(WeekDay weekDay) {
        getSession().saveOrUpdate(weekDay);
        return weekDay;
    }

    public Groups saveOrUpdateGroup(Groups group) {
        getSession().saveOrUpdate(group);
        return group;
    }
}
