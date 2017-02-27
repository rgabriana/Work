package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.MetaDataDao;
import com.ems.model.Company;
import com.ems.model.EventType;
import com.ems.model.WeekDay;

@Service("metaDataManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataManager {

    static final Logger logger = Logger.getLogger(MetaDataManager.class.getName());

    @Resource
    private CompanyManager companyManager;

    @Resource
    private MetaDataDao metaDataDao;

    public List<Company> getCompanyList() {
        return companyManager.getAllCompanies();
    }

    public List<EventType> getEventTypes() {
        return metaDataDao.getEventTypes();
    }

    public Company loadCompanyTree(Long companyId) {
        Company company = companyManager.loadCompanyById(companyId);
        company.getCampuses().size();
        return company;
    }
    
    public void flush() {
        metaDataDao.flush();
    }
}
