package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BaseDaoHibernate;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.SwitchGroup;
import com.ems.utils.ArgumentUtils;

@Repository("switchGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchGroupDao extends BaseDaoHibernate {

	@SuppressWarnings("unchecked")
	public SwitchGroup getSwitchGroupByGemsGroupId(Long gemsGroupId) {
		SwitchGroup output = null;
		try {
			String hsql = "Select new SwitchGroup(sg.id, sg.groupNo, sg.fixtureVersion) from SwitchGroup sg where sg.gemsGroup.id = ?";
			getSession().flush();
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, gemsGroupId);
			List<SwitchGroup> switcheGroups = q.list();
			if (switcheGroups != null && !switcheGroups.isEmpty()) {
				output = switcheGroups.get(0);
			}

		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return output;
	}
	
	public SwitchGroup getSwitchGroupByGroupNo(int groupNo) {
  	
		Session session = getSession();
		SwitchGroup switchGroup = (SwitchGroup)session.createCriteria(SwitchGroup.class).
  			add(Restrictions.eq("groupNo", groupNo)).uniqueResult();
  	return switchGroup;
  	
  } //end of method getSwitchGroupByGroupNo
	
	public SwitchGroup getSwitchGroupBytSwitchGroupNo(Integer switchGroupNo) {
	    Session session = getSession();
        SwitchGroup oSwitchGroup = (SwitchGroup) session.createCriteria(SwitchGroup.class)
                .add(Restrictions.eq("groupNo", switchGroupNo))                              
                .createAlias("gemsGroup", "gemsGroup")
                .uniqueResult();
        return oSwitchGroup;
	    
	}
	
	@SuppressWarnings("unchecked")
	public List<GemsGroup> loadSwitchGroupsByFloor(Long floorId) {
        Session s = getSession();
        List<GemsGroup> results = s.createCriteria(GemsGroup.class).add(Restrictions.eq("floor.id", floorId))
                .add(Restrictions.sqlRestriction(" {alias}.id in (select gems_group_id from switch_group sg)")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results;
        } else {
            return null;
        }
    }
}
