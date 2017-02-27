package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GemsGroup;
import com.ems.model.SwitchGroup;
import com.ems.types.FacilityType;
import com.ems.utils.ArgumentUtils;

@Repository("switchGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchGroupDao extends BaseDaoHibernate {
	private static final Logger logger = Logger.getLogger("SwitchLogger");

	@SuppressWarnings("unchecked")
	public SwitchGroup getSwitchGroupByGemsGroupId(Long gemsGroupId) {
		SwitchGroup output = null;
		try {
			String hsql = "Select new SwitchGroup(sg.id, sg.groupNo, sg.fixtureVersion) from SwitchGroup sg where sg.gemsGroup.id = ?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, gemsGroupId);
			List<SwitchGroup> switcheGroups = q.list();
			if (switcheGroups != null && !switcheGroups.isEmpty()) {
				output = switcheGroups.get(0);
			}

		} catch (HibernateException hbe) {
			logger.error("Error getting switchgroup by gemsgroupId: "
					+ gemsGroupId + ", " + hbe.getMessage());
			return null;
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
	
	@SuppressWarnings("unchecked")
	public List<GemsGroup> getSwitchGroupByFacility(final String property, final long id) {
        Session s = getSession();
        List<Long> floorIds = new ArrayList<Long>(); //Logic to get all floorIds from property and id
        String hsql = "select f.id, f.name, building_id, campus_id, company_id, " +
                "f.description, floorplan_url from floor f, building b, campus  c " +
                " where f.building_id = b.id and b.campus_id = c.id";
        FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
        boolean pidExists1 = true;
        if (property != null) {
            switch (orgType) {
            case CAMPUS: {
            	hsql = hsql + " and f.building_id = b.id and b.campus_id= :pid ";
                  break;
            }
            case BUILDING: {
            	hsql = hsql + " and f.building_id = :pid ";
                  break;
            }
            case FLOOR: {
            	hsql = hsql + " and f.id = :pid ";
                  break;
            }
            default: {
                  // company level all fixtures
            		pidExists1 = false;
            	}
            }
        }
        Query q = getSession().createSQLQuery(hsql.toString());       
        if(pidExists1){
    		q.setLong("pid", id);
  		}     
        List<Object[]> results = q.list();
        if (results != null && !results.isEmpty()) {
            for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
            	  Object[] object = (Object[]) iterator.next();
            	  BigInteger floorId = (BigInteger) object[0];
            	  floorIds.add(floorId.longValue());
            }
        }
        List<GemsGroup> results1 = s.createCriteria(GemsGroup.class).add(Restrictions.in("floor.id", floorIds))
                .add(Restrictions.sqlRestriction(" {alias}.id in (select gems_group_id from switch_group sg)")).list();

        if (!ArgumentUtils.isNullOrEmpty(results)) {
            return results1;
        } else {
            return null;
        }
    }
	
}
