package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FloorZbUpdate;

@Repository("floorZbUpdateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorZbUpdateDao extends BaseDaoHibernate {

	private static final long PROCESSED_SUCESSFULLY = 1l;
	private static final long NOT_PROCESSED = 0l;

	public void saveOrUpdate(FloorZbUpdate fZb) {
		getSession().saveOrUpdate(fZb);
	}

	public FloorZbUpdate loadUnProcessedFloorZbUpdateByFoorId(Long floorId) {
		FloorZbUpdate fZb = null;
		try {
			Criteria cr = getSession().createCriteria(FloorZbUpdate.class);
			cr.add(Restrictions.eq("floorId", floorId));
			cr.add(Restrictions.ne("processedState", PROCESSED_SUCESSFULLY));
			fZb = (FloorZbUpdate) cr.uniqueResult();
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return fZb;
	}

	public List<FloorZbUpdate> loadAllUnProcessedFloorZbUpdate() {
		ArrayList<FloorZbUpdate> fZb = null;
		try {
			Criteria cr = getSession().createCriteria(FloorZbUpdate.class);
			cr.add(Restrictions.ne("processedState", PROCESSED_SUCESSFULLY));
			fZb = (ArrayList<FloorZbUpdate>) cr.list();
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return fZb;
	}

}
