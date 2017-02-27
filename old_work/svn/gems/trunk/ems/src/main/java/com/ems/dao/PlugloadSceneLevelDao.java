package com.ems.dao;

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

import com.ems.model.PlugloadSceneLevel;

@Repository("plugloadSceneLevelDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadSceneLevelDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(SceneLevelDao.class.getName());


    public static final String PLUGLOAD_SCENELVL_CONTRACTOR = "Select new PlugloadSceneLevel (sl.id," + "sl.switchId," + "sl.sceneId,"
            + "sl.plugloadId," + "sl.lightLevel)";
    
    public List<PlugloadSceneLevel> loadLevelsBySceneId(Long id) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl where sl.sceneId=?";
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

    public List<PlugloadSceneLevel> loadLevelsBySwitchId(Long id) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl where sl.switchId=?";
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

    public List<PlugloadSceneLevel> loadLevelsBySwitchAndPlugloadId(Long switchId, Long plugloadId) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl where sl.switchId=? and sl.plugloadId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            q.setParameter(1, plugloadId);
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
     * Fetch Light levels for respective plugload which are associated with a scene for a particular switch.
     * @param switchId
     * @param sceneId
     * @return SceneLevel list
     */
    public List<PlugloadSceneLevel> loadLevelsBySwitchAndSceneId(Long switchId, Long sceneId) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl where sl.switchId=? and sl.sceneId=?";
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


    public PlugloadSceneLevel loadLevelBySceneSwitchAndPlugloadId(Long sceneId, Long switchId, Long plugloadId) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl where sl.sceneId=? and sl.switchId=? and sl.plugloadId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, sceneId);
            q.setParameter(1, switchId);
            q.setParameter(2, plugloadId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public PlugloadSceneLevel update(PlugloadSceneLevel sceneLevel) {
        Session session = getSession();
        if (sceneLevel != null) {
            session.saveOrUpdate(sceneLevel);

        }
        return sceneLevel;
    }

    public void updateSceneLevels(List<PlugloadSceneLevel> sceneLevels) {
        // TODO Auto-generated method stub

    }
    
    @SuppressWarnings("unchecked")
	public Set<Long> loadSwitchScenePlugloads(Long switchId) {
    	Set<Long> out = new HashSet<Long>();
    	try {
	        String hsql = "select distinct p_id from lightlevels_plugload where switch_id =  " + switchId;
	        Query q = getSession().createSQLQuery(hsql.toString());
	        List<BigInteger> results = q.list();
	        if (results != null && !results.isEmpty()) {
	            for(BigInteger pid: results) {
	            	out.add(pid.longValue());
	            }
	        }
	    } catch (HibernateException hbe) {
	        throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	    }
    	return out;
    }
    
    public List<PlugloadSceneLevel> loadSceneLevelListBySwitchAndPlugloadId(Long switchId, Long plugloadId) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl, Scene s where sl.sceneId=s.id and sl.switchId=? and sl.plugloadId=? order by s.sceneOrder";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            q.setParameter(1, plugloadId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public List<PlugloadSceneLevel> loadSceneLevelListInCreationOrderBySwitchAndPlugloadId(Long switchId, Long plugloadId) {
        try {
            List<PlugloadSceneLevel> results = null;
            String hsql = PLUGLOAD_SCENELVL_CONTRACTOR + " from PlugloadSceneLevel sl, Scene s where sl.sceneId=s.id and sl.switchId=? and sl.plugloadId=? order by sl.id";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            q.setParameter(1, plugloadId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public void deleteSceneLevelsForSwitch(Long switchId, Long plugloadId) {
    	 try {
             String hsql = "delete from PlugloadSceneLevel sl where sl.plugloadId=? and sl.switchId=?";
             Query q = getSession().createQuery(hsql.toString());
             q.setParameter(0, plugloadId);
             q.setParameter(1, switchId);
             int status = q.executeUpdate();
         } catch (HibernateException hbe) {
             throw SessionFactoryUtils.convertHibernateAccessException(hbe);
         }
    }

	public List<PlugloadSceneLevel> loadPlugloadSceneLevelsBySceneId(
			long sceneId) {
		try {
			List<PlugloadSceneLevel> results = null;
			String hsql = PLUGLOAD_SCENELVL_CONTRACTOR
					+ " from PlugloadSceneLevel sl where sl.sceneId=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, sceneId);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				return results;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}
}
