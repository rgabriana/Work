package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.TimezoneDao;
import com.ems.model.Timezone;

@Service("timezoneManager")
@Transactional(propagation = Propagation.REQUIRED)
public class TimezoneManager {

    @Resource
    private TimezoneDao timezoneDao;

    public Timezone getTimezoneById(Long id) {
        return timezoneDao.getTimezoneById(id);
    }

    public Timezone getTimezoneByName(String timezone) {
        return timezoneDao.getTimezoneByName(timezone);
    }

    public List<Timezone> getTimezoneList() {
        return timezoneDao.getTimeZoneList();
    }
}
