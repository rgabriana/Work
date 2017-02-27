package com.emscloud.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.ProfileGroups;
import com.emscloud.model.WeekDay;

@Repository("metaDataDao")
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataDao extends BaseDaoHibernate {

//    @SuppressWarnings("unchecked")
//    public List<EventType> getEventTypes() {
//        List<EventType> eventTypes = getSession().createCriteria(EventType.class).list();
//        if (!ArgumentUtils.isNullOrEmpty(eventTypes)) {
//            return eventTypes;
//        }
//        return null;
//    }

    public WeekDay saveOrUpdateWeekDay(WeekDay weekDay) {
        getSession().saveOrUpdate(weekDay);
        return weekDay;
    }

    public ProfileGroups saveOrUpdateGroup(ProfileGroups group) {
        getSession().saveOrUpdate(group);
        return group;
    }
}
