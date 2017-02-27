package com.emscloud.dao;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.EmLastGenericSynctime;

@Repository("EmLastGenericSynctimeDao")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class EmLastGenericSynctimeDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(EmLastGenericSynctimeDao.class.getName());

    public EmLastGenericSynctime saveOrUpdate(EmLastGenericSynctime instance) {
        sessionFactory.getCurrentSession().saveOrUpdate(instance);
        return instance;
    }

    public EmLastGenericSynctime getEmLastGenericSynctimeForEmId(Long emId, String operationName) {

        try {
            EmLastGenericSynctime emLastGenericSynctime = (EmLastGenericSynctime) sessionFactory.getCurrentSession()
                    .createCriteria(EmLastGenericSynctime.class).add(Restrictions.eq("emId", emId))
                    .add(Restrictions.eq("syncOperation", operationName)).uniqueResult();
            return emLastGenericSynctime;
        } catch (HibernateException hbe) { // throw
            SessionFactoryUtils.convertHibernateAccessException(hbe);
            logger.error(hbe.getMessage(), hbe);
        }
        return null;
    }
    
    public void deleteEmLastGenericSynctimeByEmId(Long emId) {
		String hsql = "delete from EmLastGenericSynctime where emId=?";
		Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
		q.setParameter(0, emId);
		q.executeUpdate();
	}

}
