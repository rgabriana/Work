package com.ems.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SceneLevel;

@Repository("sceneLevelDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SceneLevelDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(SceneLevelDao.class.getName());

    public static final String SCENELVL_CONTRACTOR = "Select new SceneLevel (sl.id," + "sl.switchId," + "sl.sceneId,"
            + "sl.fixtureId," + "sl.lightLevel)";

    public List<SceneLevel> loadLevelsBySceneId(Long id) {
        try {
            List<SceneLevel> results = null;
            String hsql = SCENELVL_CONTRACTOR + " from SceneLevel sl where sl.sceneId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public List<SceneLevel> loadLevelsBySwitchId(Long id) {
        try {
            List<SceneLevel> results = null;
            String hsql = SCENELVL_CONTRACTOR + " from SceneLevel sl where sl.switchId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public List<SceneLevel> loadLevelsBySwitchAndFixtureId(Long switchId, Long fixtureId) {
        try {
            List<SceneLevel> results = null;
            String hsql = SCENELVL_CONTRACTOR + " from SceneLevel sl where sl.switchId=? and sl.fixtureId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            q.setParameter(1, fixtureId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    /**
     * Fetch Light levels for respective fixture which are associated with a scene for a particular switch.
     * @param switchId
     * @param sceneId
     * @return SceneLevel list
     */
    public List<SceneLevel> loadLevelsBySwitchAndSceneId(Long switchId, Long sceneId) {
        try {
            List<SceneLevel> results = null;
            String hsql = SCENELVL_CONTRACTOR + " from SceneLevel sl where sl.switchId=? and sl.sceneId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            q.setParameter(1, sceneId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }


    public SceneLevel loadLevelBySceneSwitchAndFixtureId(Long sceneId, Long switchId, Long fixtureId) {
        try {
            List<SceneLevel> results = null;
            String hsql = SCENELVL_CONTRACTOR + " from SceneLevel sl where sl.sceneId=? and sl.switchId=? and sl.fixtureId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, sceneId);
            q.setParameter(1, switchId);
            q.setParameter(2, fixtureId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public SceneLevel update(SceneLevel sceneLevel) {
        Session session = getSession();
        if (sceneLevel != null) {
            session.saveOrUpdate(sceneLevel);

        }
        return sceneLevel;
    }

    public void updateSceneLevels(List<SceneLevel> sceneLevels) {
        // TODO Auto-generated method stub

    }
    
    @SuppressWarnings("unchecked")
	public Set<Long> loadSwitchSceneFixtures(Long switchId) {
    	Set<Long> out = new HashSet<Long>();
    	try {
	        String hsql = "select distinct f_id from lightlevels where switch_id =  " + switchId;
	        Query q = getSession().createSQLQuery(hsql.toString());
	        List<BigInteger> results = q.list();
	        if (results != null && !results.isEmpty()) {
	            for(BigInteger fid: results) {
	            	out.add(fid.longValue());
	            }
	        }
	    } catch (HibernateException hbe) {
	        throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	    }
    	return out;
    }
    
    public List<SceneLevel> loadSceneLevelListBySwitchAndFixtureId(Long switchId, Long fixtureId) {
        try {
            List<SceneLevel> results = null;
            String hsql = SCENELVL_CONTRACTOR + " from SceneLevel sl, Scene s where sl.sceneId=s.id and sl.switchId=? and sl.fixtureId=? order by s.sceneOrder";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            q.setParameter(1, fixtureId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    public void deleteSceneLevelsForSwitch(Long switchId, Long fixtureId) {
    	 try {
             String hsql = "delete from SceneLevel sl where sl.fixtureId=? and sl.switchId=?";
             Query q = getSession().createQuery(hsql.toString());
             q.setParameter(0, fixtureId);
             q.setParameter(1, switchId);
             int status = q.executeUpdate();
         } catch (HibernateException hbe) {
             throw SessionFactoryUtils.convertHibernateAccessException(hbe);
         }
    }
}
