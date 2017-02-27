package com.ems.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.EventType;
import com.ems.model.Groups;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.WeekDay;
import com.ems.model.WeekdayPlugload;
import com.ems.utils.ArgumentUtils;

@Repository("metaDataDao")
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataDao extends BaseDaoHibernate {

    @SuppressWarnings("unchecked")
    public List<EventType> getEventTypes() {
        List<EventType> eventTypes = getSession().createCriteria(EventType.class).list();
        if (!ArgumentUtils.isNullOrEmpty(eventTypes)) {
            return eventTypes;
        }
        return null;
    }

    public WeekDay saveOrUpdateWeekDay(WeekDay weekDay) {
        getSession().saveOrUpdate(weekDay);
        return weekDay;
    }
    
    public WeekdayPlugload saveOrUpdateWeekDay(WeekdayPlugload weekDay) {
        getSession().saveOrUpdate(weekDay);
        return weekDay;
    }

    public Groups saveOrUpdateGroup(Groups group) {
        getSession().saveOrUpdate(group);
        return group;
    }
    
    public PlugloadGroups saveOrUpdatePlugloadGroup(PlugloadGroups group) {
        getSession().saveOrUpdate(group);
        return group;
    }

	public WeekdayPlugload saveOrUpdateWeekdayPlugload(WeekdayPlugload weekdayPlugload) {
		getSession().saveOrUpdate(weekdayPlugload);
		return weekdayPlugload;
	}
	
	
}
