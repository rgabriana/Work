package com.emscloud.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmStatsDao;
import com.emscloud.model.EmStats;

@Service("emStatsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStatsManager {
	
	@Resource
	private EmStatsDao emStatsDao;
	
    public Object saveObject(EmStats emStats) {
        return emStatsDao.saveObject(emStats);
    }
    
    public EmStats getLatestEmStatsByEmInstanceId(Long id){
    	return emStatsDao.getLatestEmStatsByEmInstanceId(id);	
	}

}
