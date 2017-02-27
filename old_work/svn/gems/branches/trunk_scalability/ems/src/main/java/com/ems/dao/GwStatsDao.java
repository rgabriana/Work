/**
 * 
 */
package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GwStats;

/**
 * @author sreedhar
 * 
 */
@Repository("gwStatsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GwStatsDao extends BaseDaoHibernate {

    /**
   * 
   */
    public GwStatsDao() {
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    public List<GwStats> loadLastGwStatsFromDB() {

        String queryStr = "Select new GwStats(g.id, g.currUptime, g.currNoPktsFromGems, "
                + "g.currNoPktsToGems, g.currNoPktsFromNodes, g.currNoPktsToNodes) FROM Gateway g "
                + "WHERE g.lastStatsRcvdTime is not null";
        List<GwStats> gwStatsList = null;
        try {
            Query q = getSession().createQuery(queryStr);
            gwStatsList = q.list();
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return gwStatsList;

    } // end of method loadLastGwStatsFromDB

} // end of class GwStatsDaoImpl
