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

import com.ems.model.Scene;

@Repository("sceneDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SceneDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(SceneDao.class.getName());

    public static final String SCENE_CONTRACTOR = "Select new Scene(s.id," + "s.switchId," + "s.name," + "s.sceneOrder)";

    public List<Scene> LoadAllScenes() {
        try {
            List<Scene> results = null;
            String hsql = SCENE_CONTRACTOR + " from Scene s";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public Scene getSceneByNameandId(String name, Long id) {
        Scene scene = null;
        try {
            String hsql = SCENE_CONTRACTOR + " from Scene s where name = ? and switchId = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            q.setParameter(1, id);
            List<Scene> scenes = q.list();
            if (scenes != null && !scenes.isEmpty()) {
                scene = scenes.get(0);
            }

        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return scene;

    }

    public Scene getSceneById(Long sceneId) {
        Scene scene = null;
        try {
            String hsql = SCENE_CONTRACTOR + " from Scene s where id = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, sceneId);
            List<Scene> scenes = q.list();
            if (scenes != null && !scenes.isEmpty()) {
                scene = scenes.get(0);
            }

        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return scene;

    }

    public Scene getSceneByName(String name) {
        Scene scene = null;
        try {
            String hsql = SCENE_CONTRACTOR + " from Scene s where name = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            List<Scene> scenes = q.list();
            if (scenes != null && !scenes.isEmpty()) {
                scene = scenes.get(0);
            }

        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return scene;

    }

    public List<Scene> loadSceneBySwitchId(Long id) {
        try {
            List<Scene> results = null;
            String hsql = SCENE_CONTRACTOR + " from Scene s where switchId=?" + "order by sceneOrder asc";
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

    
    public List<Scene> loadSceneInCreationOrderBySwitchId(Long id) {
        try {
            List<Scene> results = null;
            String hsql = SCENE_CONTRACTOR + " from Scene s where switchId=?" + "order by id asc";
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

    public Scene update(Scene scene) {
        Query query = getSession().createQuery(
                "Update Scene set " + "name = :name," + "switchId = :switchId," + " where id = :id");
        query.setLong("id", scene.getId());
        query.setLong("switchId", scene.getSwitchId());
        query.setString("name", scene.getName());
        return scene;
    }

    public void updateScenes(List<Scene> scenes) {
        Session session = getSession();
        for (Scene scene : scenes) {
            getSession().merge(scene);
        }

    }
    
    @SuppressWarnings({ "unchecked" })
	public Integer nextSceneOrder(Long switchId) {
		String hsql = "Select count(scene) from Scene scene where switchId = " + switchId;
		Query q = getSession().createQuery(hsql.toString());
		List<Object> output = (List<Object>)q.list();
    	Integer count = new Integer(output.get(0).toString());
		return count;
    }
    
    public void updateSceneOrder(Long switchId, int sceneOrderId) {
        Query query = getSession().createQuery(
                "Update Scene set " + "sceneOrder = sceneOrder-1 " + " where switchId = :switchId and sceneOrder > :deletedSceneOrder");
        query.setLong("switchId", switchId);
        query.setLong("deletedSceneOrder", sceneOrderId);
        query.executeUpdate();
    }


}