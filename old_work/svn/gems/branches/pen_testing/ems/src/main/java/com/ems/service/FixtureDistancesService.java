/**
 * 
 */
package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureDistancesDao;
import com.ems.model.FixtureDistances;

/**
 * @author EMS
 * 
 */
@Service("fixtureDistancesService")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureDistancesService {

    @Resource
    FixtureDistancesDao fdDao;

    /**
   * 
   */
    public FixtureDistancesService() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.FixtureDistancesService#addFixtureDistance(com.ems.model.FixtureDistances)
     */

    public void addFixtureDistance(FixtureDistances fd) {
        // TODO Auto-generated method stub

        fdDao.addFixtureDistance(fd);

    } // end of method addFixtureDistance

    public void removeAllFixtureDistances() {

        fdDao.removeAllFixtureDistances();

    } // end of method removeAllFixtureDistances

    public List<FixtureDistances> loadAllFixtureDistances() {

        return fdDao.loadAllFixtureDistances();

    } // end of method loadAllFixtureDistances

    public List<FixtureDistances> getFixtureDistances(String snap) {

        return fdDao.getFixtureDistances(snap);

    } // end of method getFixtureDistances

} // end of class FixtureDistancesServiceImpl
