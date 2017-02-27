package com.ems.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.EventType;
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

}
