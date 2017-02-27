package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.SceneTemplate;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Repository("sceneTemplateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SceneTemplateDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(SceneTemplateDao.class.getName());

	public static final String SWITCH_CONTRACTOR = "Select new SceneTemplate(s.id,"+ "s.name)";
	
	public List<SceneTemplate> loadAllSceneTemlates() {
		try {
			List<SceneTemplate> results = null;
			String hsql = SWITCH_CONTRACTOR + " from SceneTemplate s";
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

	public List<SceneTemplate> getSceneTemplateByName(String name) {
		try {
			List<SceneTemplate> results = null;
			String hsql = "Select new SceneTemplate(s.name) from SceneTemplate s where s.name=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, name);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				return results;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;		
	}
	
	public SceneTemplate createNewSceneTemplate(SceneTemplate newSceneTemplate) {
    	return (SceneTemplate) saveObject(newSceneTemplate);
    }

	public SceneTemplate getSceneTemplateById(Long scenetemplateId) {
		try {
			SceneTemplate rs = null;
			List<SceneTemplate> results = null;
			String hsql = "from SceneTemplate s where s.id=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, scenetemplateId);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				rs = results.get(0);
				return rs;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;		
	}
	
	public int deleteSceneTemplate(Long sceneTemplateId) {
		int status;
		try {
			String hsql = "delete from SceneTemplate sl where sl.id=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, sceneTemplateId);
			status = q.executeUpdate();
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return status;
	}
}
