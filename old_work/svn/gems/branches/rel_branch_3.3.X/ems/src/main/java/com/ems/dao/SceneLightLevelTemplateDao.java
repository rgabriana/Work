package com.ems.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.User;

@Repository("SceneLightLevelTemplateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SceneLightLevelTemplateDao extends BaseDaoHibernate{
	static final Logger logger = Logger.getLogger(SceneLightLevelTemplateDao.class.getName());

	public static final String LIGHTLEVEL_CONTRACTOR = "Select new SceneLightLevelTemplate(s.sceneTemplateId,"+ "s.lightlevel)";
	
	public List<SceneLightLevelTemplate> loadAllSceneLightLevels() {
		try {
			List<SceneLightLevelTemplate> results = null;
			String hsql = "from SceneLightLevelTemplate s order by s.sceneOrder";
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
	public SceneLightLevelTemplate loadSceneLightLevelById(
			Long sceneLightLevelId) {
		List<SceneLightLevelTemplate> results = null;
         String hsql = "from SceneLightLevelTemplate s where s.id=?";
         Query q = getSession().createQuery(hsql.toString());
         q.setParameter(0, sceneLightLevelId);
         results = q.list();
         if (results != null && !results.isEmpty()) {
        	 SceneLightLevelTemplate sceneLightLevelTemplate = (SceneLightLevelTemplate) results.get(0);
             return sceneLightLevelTemplate;
         }
		return null;
	}
	public List<SceneLightLevelTemplate> loadSceneLightLevelBySceneTemplateId(
			Long sceneTemplateId) {
		List<SceneLightLevelTemplate> results = null;
         String hsql = "from SceneLightLevelTemplate s where s.sceneTemplateId=? order by s.sceneOrder";
         Query q = getSession().createQuery(hsql.toString());
         q.setParameter(0, sceneTemplateId);
         results = q.list();
         if (results != null && !results.isEmpty()) {
             return (List<SceneLightLevelTemplate>) results;
         }
		return null;
	}

	public List<SceneLightLevelTemplate> loadSceneLightLevelBySceneTemplateIdAndSceneOrder(
			Long sceneTemplateId, Integer sceneOrder) {
		List<SceneLightLevelTemplate> results = null;
        String hsql = "from SceneLightLevelTemplate s where s.sceneTemplateId=? AND s.sceneOrder=?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, sceneTemplateId);
        q.setParameter(1, sceneOrder);
        results = q.list();
        if (results != null && !results.isEmpty()) {
            return (List<SceneLightLevelTemplate>) results;
        }
		return null;
	}

	public int getMaxSceneOrderForSceneTemplateID(Long sceneTemplateID) {
		Integer maxSceneOrder=-1;
		String hsql = "select max(sceneOrder) from SceneLightLevelTemplate s where s.sceneTemplateId=?";
		Query q = getSession().createQuery(hsql.toString());
		q.setParameter(0, sceneTemplateID);
		List<Object> oResult = q.list();
		maxSceneOrder = (Integer) oResult.get(0);
		if(maxSceneOrder == null)
		{
			maxSceneOrder=-1;
		}
		return maxSceneOrder;
	}
	public int deleteSceneLightLevel(Long sceneLightLevelId) {
		int status;
		try {
			String hsql = "delete from SceneLightLevelTemplate sl where sl.id=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, sceneLightLevelId);
			status = q.executeUpdate();
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return status;
	}
}
