package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.model.EmProfileMapping;

/**
 * @author SampathAkula
 *
 */
@Repository("emProfileMappingDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class EmProfileMappingDao extends BaseDaoHibernate{
    
    
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
    
    public EmProfileMapping getEmTemplateMappingByEmProfileNoAndEMId(Short emProfileNo, Long emInstanceId) {
        EmProfileMapping emProfileMapping =null;
        try {
            emProfileMapping = (EmProfileMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmProfileMapping.class)
                    .add(Restrictions.eq("emProfileNo", emProfileNo))
                    .add(Restrictions.eq("emId", emInstanceId)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emProfileMapping;
    }
    
    public EmProfileMapping getEmProfileMappingByUemProfileIdAndEMId(Long uemProfileId, Long emInstanceId) {
        EmProfileMapping emProfileMapping =null;
        try {
            emProfileMapping = (EmProfileMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmProfileMapping.class)
                    .add(Restrictions.eq("uemProfileId", uemProfileId))
                    .add(Restrictions.eq("emId", emInstanceId)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emProfileMapping;
    }
    
    public EmProfileMapping getEmTemplateMappingByEmGroupIdAndEMId(Long emGroupId, Long emInstanceId) {
        EmProfileMapping emProfileMapping =null;
        try {
            emProfileMapping = (EmProfileMapping) sessionFactory
                    .getCurrentSession().createCriteria(EmProfileMapping.class)
                    .add(Restrictions.eq("emGroupId", emGroupId))
                    .add(Restrictions.eq("emId", emInstanceId)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return emProfileMapping;
    }
    public List<EmProfileMapping> getListOfDirtyProfilesInUEM()
    {
        List<EmProfileMapping> emProfileMapping = null;
        emProfileMapping = sessionFactory
                .getCurrentSession().createCriteria(EmProfileMapping.class)
                .add(Restrictions.eq("syncStatus", 1)).list();
        return emProfileMapping;
    }
    
    public List<EmProfileMapping> getEMProfileMappingByUEMProfileId(Long uemProfileId)
    {
    	List<EmProfileMapping> emProfileMapping = null;
        emProfileMapping = sessionFactory
                .getCurrentSession().createCriteria(EmProfileMapping.class)
                .add(Restrictions.eq("uemProfileId", uemProfileId)).list();
        return emProfileMapping;
    }
}