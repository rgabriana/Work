/**
 * 
 */
package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.EmStatsDao;
import com.ems.dao.GwStatsDao;
import com.ems.model.EmStats;
import com.ems.model.GwStats;

/**
 * @author sreedhar
 * 
 */
@Service("emStatsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStatsManager {

    @Resource
    private EmStatsDao emStatsDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.GwStatsManager#save(com.ems.model.GwStats)
     */

    public EmStats save(EmStats emStats) {
        // TODO Auto-generated method stub
        return (EmStats) emStatsDao.saveObject(emStats);
    } // end of method save

} // end of class EmStatsManagerImpl
