/**
 * 
 */
package com.ems.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Gateway;
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

    public void updateCurrentGwStats(GwStats currGwStats) {

      Session session = getSession();
      Gateway gw = (Gateway) session.get(Gateway.class, currGwStats.getGwId());
      gw.setCurrUptime(currGwStats.getUptime());
      gw.setCurrNoPktsFromGems(currGwStats.getNoPktsFromGems());
      gw.setCurrNoPktsFromNodes(currGwStats.getNoPktsFromNodes());
      gw.setCurrNoPktsToGems(currGwStats.getNoPktsToGems());
      gw.setCurrNoPktsToNodes(currGwStats.getNoPktsToNodes());
      gw.setLastConnectivityAt(new Date());
      gw.setLastStatsRcvdTime(new Date());
      
      /*
        Query query = getSession().createQuery(
                "Update Gateway set " + "currUptime = :uptime, currNoPktsFromGems = :pktsFromGems, "
                        + "currNoPktsToGems = :pktsToGems, currNoPktsFromNodes = :pktsFromNodes, "
                        + "currNoPktsToNodes = :pktsToNodes, lastConnectivityAt = :lastConnectivityAt, "
                        + "lastStatsRcvdTime = :lastStatsRcvdTime where id = :id");

        query.setLong("uptime", currGwStats.getUptime());
        query.setLong("pktsFromGems", currGwStats.getNoPktsFromGems());
        query.setLong("pktsToGems", currGwStats.getNoPktsToGems());
        query.setLong("pktsFromNodes", currGwStats.getNoPktsFromNodes());
        query.setLong("pktsToNodes", currGwStats.getNoPktsToNodes());
        query.setTimestamp("lastConnectivityAt", new Date());
        query.setTimestamp("lastStatsRcvdTime", new Date());
        query.setLong("id", currGwStats.getGwId());

        query.executeUpdate();
	*/
      
    } // end of method updateCurrentGwStats

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
