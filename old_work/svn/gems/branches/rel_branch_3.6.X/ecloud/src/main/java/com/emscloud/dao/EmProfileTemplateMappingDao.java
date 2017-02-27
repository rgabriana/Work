package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.model.EmProfileMapping;
import com.emscloud.model.EmTemplateMapping;

/**
 * @author SharadM
 *
 */
@Repository("emProfileTemplateMappingDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class EmProfileTemplateMappingDao extends BaseDaoHibernate{
    
    
	public EmProfileMapping getEmProfileMapping(long id) {
		Object obj = getObject(EmProfileMapping.class, id);
		if (obj == null)
			return null;
		else
			return (EmProfileMapping) obj;
	}
	
	public List<EmProfileMapping> getEmProfileMappingListOnUemProfileId(long uemProfileId) {
		ArrayList<EmProfileMapping> emProfileMappingList = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(EmProfileMapping.class);
		criteria.add(Restrictions.eq("uemProfileId", uemProfileId));
		emProfileMappingList = (ArrayList<EmProfileMapping>) criteria.list();
		if (!ArgumentUtils.isNullOrEmpty(emProfileMappingList)) {
			return emProfileMappingList;
		} else {
			return null;
		}
	}
	
	public List<EmTemplateMapping>  getProfileTemplateMappingListOnUemProfileTemplateId(long uemTemplateId) {
		ArrayList<EmTemplateMapping> emTemplateMappingList = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(EmTemplateMapping.class);
		criteria.add(Restrictions.eq("uemTemplateId", uemTemplateId));
		emTemplateMappingList = (ArrayList<EmTemplateMapping>) criteria.list();
		if (!ArgumentUtils.isNullOrEmpty(emTemplateMappingList)) {
			return emTemplateMappingList;
		} else {
			return null;
		}
	}
	
	
	/*public EmProfileMapping getEmProfileMappingByEmId(long id) {
        EmProfileMapping emProfileMapping =null;
        try {
            emProfileMapping = (EmProfileMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmProfileMapping.class)
                    .add(Restrictions.eq("emId", id)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emProfileMapping;
    }*/
	
	public List<EmProfileMapping> getEmProfileMappingByEmId(long emId) {
		ArrayList<EmProfileMapping> emProfileMappingList = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(EmProfileMapping.class);
		criteria.add(Restrictions.eq("emId", emId));
		emProfileMappingList = (ArrayList<EmProfileMapping>) criteria.list();
		if (!ArgumentUtils.isNullOrEmpty(emProfileMappingList)) {
			return emProfileMappingList;
		} else {
			return null;
		}
    }
    
    public EmProfileMapping saveOrUpdate(EmProfileMapping emProfileMapping) {
        sessionFactory.getCurrentSession().saveOrUpdate(emProfileMapping);
        return emProfileMapping;
    }
    
    public EmTemplateMapping getEmTemplateMappingByEmId(long id) {
        EmTemplateMapping emTemplateMapping =null;
        try {
            emTemplateMapping = (EmTemplateMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmTemplateMapping.class)
                    .add(Restrictions.eq("emId", id)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emTemplateMapping;
    }

    public EmTemplateMapping saveOrUpdate(EmTemplateMapping emTemplateMapping) {
        sessionFactory.getCurrentSession().saveOrUpdate(emTemplateMapping);
        return emTemplateMapping;
    }

    public EmTemplateMapping getEmTemplateMappingByEmTemplateIdAndEMId(Long emTemplateId,Long emInstanceId) {
        EmTemplateMapping emTemplateMapping =null;
        try {
            emTemplateMapping = (EmTemplateMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmTemplateMapping.class)
                    .add(Restrictions.eq("emTemplateId", emTemplateId))
                    .add(Restrictions.eq("emId", emInstanceId)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emTemplateMapping;
    }
    
    public EmTemplateMapping getEmTemplateMappingByUEMTemplateIdAndEMId(Long uemTemplateId,Long emInstanceId) {
        EmTemplateMapping emTemplateMapping =null;
        try {
            emTemplateMapping = (EmTemplateMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmTemplateMapping.class)
                    .add(Restrictions.eq("uemTemplateId", uemTemplateId))
                    .add(Restrictions.eq("emId", emInstanceId)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emTemplateMapping;
    }

}