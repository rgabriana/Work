/**
 * 
 */
package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sreedhar
 * 
 */
@Repository("emStatsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStatsDao extends BaseDaoHibernate {

    /**
   * 
   */
    public EmStatsDao() {
        // TODO Auto-generated constructor stub
    }

} // end of class EmStatsDao
