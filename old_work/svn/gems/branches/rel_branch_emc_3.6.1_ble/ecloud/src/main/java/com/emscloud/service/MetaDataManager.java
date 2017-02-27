package com.emscloud.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.MetaDataDao;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.WeekDay;

@Service("metaDataManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataManager {

    static final Logger logger = Logger.getLogger(MetaDataManager.class.getName());

    @Resource
    private MetaDataDao metaDataDao;

//    public List<Company> getCompanyList() {
//        return companyDao.getAllCompanies();
//    }

//    public List<EventType> getEventTypes() {
//        return metaDataDao.getEventTypes();
//    }

    public WeekDay saveOrUpdateWeekDay(WeekDay weekDay) {
        return metaDataDao.saveOrUpdateWeekDay(weekDay);
    }
    //@InvalidateProfileTreeCache
    public ProfileGroups saveOrUpdateGroup(ProfileGroups group) {
        return metaDataDao.saveOrUpdateGroup(group);
    }

//    public Company loadCompanyTree(Long companyId) {
//        Company company = companyDao.getCompanyById(companyId);
//        company.getCampuses().size();
//        return company;
//    }
    
    public void flush() {
        metaDataDao.flush();
    }
}
