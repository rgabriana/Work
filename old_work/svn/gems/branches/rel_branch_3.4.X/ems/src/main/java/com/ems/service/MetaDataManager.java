package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.ems.annotaion.InvalidateProfileTreeCache;
import com.ems.dao.CompanyDao;
import com.ems.dao.MetaDataDao;
import com.ems.model.Company;
import com.ems.model.EventType;
import com.ems.model.Groups;
import com.ems.model.PlugloadGroups;
import com.ems.model.WeekDay;
import com.ems.model.WeekdayPlugload;

@Service("metaDataManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataManager {

    static final Logger logger = Logger.getLogger(MetaDataManager.class.getName());

    @Resource
    private CompanyDao companyDao;

    @Resource
    private MetaDataDao metaDataDao;

    public List<Company> getCompanyList() {
        return companyDao.getAllCompanies();
    }

    public List<EventType> getEventTypes() {
        return metaDataDao.getEventTypes();
    }

    public WeekDay saveOrUpdateWeekDay(WeekDay weekDay) {
        return metaDataDao.saveOrUpdateWeekDay(weekDay);
    }
    
    public WeekdayPlugload saveOrUpdateWeekdayPlugload(WeekdayPlugload weekdayPlugload) {
        return metaDataDao.saveOrUpdateWeekdayPlugload(weekdayPlugload);
    }
    @InvalidateProfileTreeCache
    public Groups saveOrUpdateGroup(Groups group) {
        return metaDataDao.saveOrUpdateGroup(group);
    }
    
    public PlugloadGroups saveOrUpdatePlugloadGroup(PlugloadGroups group) {
        return metaDataDao.saveOrUpdatePlugloadGroup(group);
    }

    public Company loadCompanyTree(Long companyId) {
        Company company = companyDao.getCompanyById(companyId);
        company.getCampuses().size();
        return company;
    }
    
    public void flush() {
        metaDataDao.flush();
    }
}
