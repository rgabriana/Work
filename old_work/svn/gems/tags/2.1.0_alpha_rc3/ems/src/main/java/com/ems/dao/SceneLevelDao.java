package com.ems.dao;

import java.util.List;

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

}
