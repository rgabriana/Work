package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupPlugload;
import com.ems.utils.ArgumentUtils;

@Repository("gemsGroupPlugloadDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsGroupPlugloadDao extends BaseDaoHibernate {

	@SuppressWarnings("unchecked")
	public List<GemsGroupPlugload> getGemsGroupPlugloadById(Long plugloadId) {
		Session session = getSession();
		List<GemsGroupPlugload> results = session.createCriteria(GemsGroupPlugload.class)
				.add(Restrictions.eq("plugload.id", plugloadId))
				.add(Restrictions.ne("userAction", GemsGroupPlugload.USER_ACTION_PLUGLOAD_DELETE))
				.createAlias("group", "group")
				.createAlias("plugload", "plugload").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		
		if (!ArgumentUtils.isNullOrEmpty(results)) {
			return results;
		} else {
			return null;
		}

	} //end of method getGemsGroupPlugloadById
	
	@SuppressWarnings("unchecked")
	public GemsGroupPlugload getGemsGroupPlugload(Long groupId, Long plugloadId) {
		 
		Session session = getSession();
		List<GemsGroupPlugload> results = session.createCriteria(GemsGroupPlugload.class)
				.add(Restrictions.eq("group.id", groupId)).add(Restrictions.eq("plugload.id", plugloadId)).list();
		 	
		if (!ArgumentUtils.isNullOrEmpty(results)) {	
			return results.get(0);
		} else {
			return null;
		}
		 
	} //end of method getGemsGroupPlugload
	 
	public void saveGemsGroupPlugload(GemsGroupPlugload groupPlugload) {
		
		Session s = getSession();
		s.saveOrUpdate(groupPlugload);
 
	} //end of method saveGemsGroupFixtures
	
	public void deleteGemsGroup(Long groupId) {
		
		String hsql = "delete from GemsGroupPlugload where group.id=?";
		Query q = getSession().createQuery(hsql.toString());
		q.setParameter(0, groupId);
		q.executeUpdate();
		removeObject(GemsGroup.class, groupId);
		
	} //end of method deleteGemsGroup
	
	public void deleteGemsGroupPlugload(GemsGroupPlugload gemsGroupPlugload) {
        Session s = getSession();
        s.delete(gemsGroupPlugload);
    }
	
	@SuppressWarnings("unchecked")
    public List<GemsGroupPlugload> getGemsGroupPlugloadByGroup(Long groupId) {
        Session session = getSession();
        List<GemsGroupPlugload> results = session.createCriteria(GemsGroupPlugload.class)
                .add(Restrictions.eq("group.id", groupId))
                .add(Restrictions.ne("userAction", GemsGroupPlugload.USER_ACTION_PLUGLOAD_DELETE))
                .createAlias("group", "group")
                .createAlias("plugload", "plugload").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }
	@SuppressWarnings("unchecked")
	public List<GemsGroupPlugload> getAllGroupsOfPlugload(Long plugloadId) {
        Session session = getSession();
        List<GemsGroupPlugload> results = session.createCriteria(GemsGroupPlugload.class)
                .add(Restrictions.eq("plugload.id", plugloadId))
                .add(Restrictions.ne("userAction", GemsGroupPlugload.USER_ACTION_PLUGLOAD_DELETE))
                .createAlias("group", "group")
                .createAlias("plugload", "plugload").setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }
	public void updateGroupPlugloadSyncPending(Long gemsGroupId, boolean bEnable) {
        String hsql = "update plugload set groups_sync_pending = '" + bEnable
                + "' where id in (Select plugload_id from gems_group_plugload where group_id = " + gemsGroupId + ");";
        Query q = getSession().createSQLQuery(hsql.toString());
        q.executeUpdate();
    }
	public void updateGemsGroupPlugload(GemsGroupPlugload ggp) {
        Session s = getSession();
        s.saveOrUpdate(ggp);
	}
} //end of class GemsGroupPlugloadDao
