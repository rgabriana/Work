package com.emscloud.dao;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.EmLastEcSynctime;



@Repository("EmLastEcSynctimeDao")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class EmLastEcSynctimeDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(EmLastEcSynctimeDao.class
			.getName());

	public EmLastEcSynctime saveOrUpdate(EmLastEcSynctime instance) {
		sessionFactory.getCurrentSession().saveOrUpdate(instance);
		return instance;
	}

	public EmLastEcSynctime getEmLastEcSynctimeForEmId(Long emId) {

		try {
			EmLastEcSynctime emLastEcSynctime = (EmLastEcSynctime) sessionFactory
					.getCurrentSession().createCriteria(EmLastEcSynctime.class)
					.add(Restrictions.eq("emId", emId)).uniqueResult();
			return emLastEcSynctime;
		} catch (HibernateException hbe) { // throw
			SessionFactoryUtils.convertHibernateAccessException(hbe);
			logger.error(hbe.getMessage(), hbe);
		}
		return null;
	}

}
