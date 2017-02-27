package com.emscloud.dao;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.ProfileSyncStatus;


/**
 * @author SharadM
 *
 */
@Repository("profileSyncStatusDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class ProfileSyncStatusDao extends BaseDaoHibernate{
    
    public ProfileSyncStatus getProfileSyncStatusByEMId(long id) {
        ProfileSyncStatus profileSyncStatus =null;
        try {
            profileSyncStatus = (ProfileSyncStatus) sessionFactory
                    .getCurrentSession().createCriteria(ProfileSyncStatus.class)
                    .add(Restrictions.eq("emId", id)).uniqueResult();

        } catch (HibernateException hbe) {
            //throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return profileSyncStatus;
    }
    
    public ProfileSyncStatus saveOrUpdate(ProfileSyncStatus profileSyncStatus) {
        sessionFactory.getCurrentSession().saveOrUpdate(profileSyncStatus);
        return profileSyncStatus;
    }
}