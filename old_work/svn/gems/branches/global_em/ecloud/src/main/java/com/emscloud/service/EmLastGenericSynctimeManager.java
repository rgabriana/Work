package com.emscloud.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmLastGenericSynctimeDao;
import com.emscloud.model.EmLastGenericSynctime;

@Service("EmLastGenericSynctimeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmLastGenericSynctimeManager {
    static final Logger logger = Logger.getLogger(EmLastEcSynctimeManager.class.getName());

    @Resource
    EmLastGenericSynctimeDao emLastGenericSynctimeDao;

    public EmLastGenericSynctime saveOrUpdate(EmLastGenericSynctime instance) {
        return emLastGenericSynctimeDao.saveOrUpdate(instance);
    }

    public EmLastGenericSynctime getEmLastGenericSynctimeForEmId(Long emId, String operationName) {
        return emLastGenericSynctimeDao.getEmLastGenericSynctimeForEmId(emId, operationName);
    }

}
