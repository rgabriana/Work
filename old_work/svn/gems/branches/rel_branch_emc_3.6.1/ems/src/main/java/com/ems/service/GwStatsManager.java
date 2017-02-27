/**
 * 
 */
package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GwStatsDao;
import com.ems.model.Gateway;
import com.ems.model.GwStats;

/**
 * @author sreedhar
 * 
 */
@Service("gwStatsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GwStatsManager {

    @Resource
    private GwStatsDao gwStatsDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.GwStatsManager#save(com.ems.model.GwStats)
     */

    public GwStats save(GwStats gwStats) {
        // TODO Auto-generated method stub
        return (GwStats) gwStatsDao.saveObject(gwStats);
    } // end of method save

    public void updateCurrentGwStats(GwStats currGwStats, Gateway gw) {
        gwStatsDao.updateCurrentGwStats(currGwStats, gw);
    }

    public List<GwStats> loadLastGwStatsFromDB() {
        return gwStatsDao.loadLastGwStatsFromDB();
    }

} // end of class GwStatsManagerImpl
