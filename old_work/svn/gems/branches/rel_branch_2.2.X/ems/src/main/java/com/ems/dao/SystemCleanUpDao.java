package com.ems.dao;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("systemCleanUpDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemCleanUpDao extends BaseDaoHibernate { 

	public void resetAllFixtureGroupSyncFlag() {
		
	
			  try {
					String queryStr = "update Fixture set groups_sync_pending = :newValue " ;
							

					SQLQuery query = getSession().createSQLQuery(queryStr);
					query.setBoolean("newValue", false);
					query.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
		
	}

}
