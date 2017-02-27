package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.SceneTemplate;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Repository("switchDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(SwitchDao.class.getName());

	public static final String SWITCH_CONTRACTOR = "Select new Switch(s.id,"
			+ "s.name," + "s.floorId," + "s.buildingId," + "s.campusId,"
			+ "s.xaxis," + "s.yaxis," + "s.areaId," + "s.modeType,"
			+ "s.initialSceneActiveTime," + "s.extendSceneActiveTime,"
			+ "s.operationMode)";

	public Switch getswitchName(String name) {
		Switch switchval = null;
		try {
			String hsql = SWITCH_CONTRACTOR + " from Switch s where name=?";
			getSession().flush();
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, name);
			List<Switch> switches = q.list();
			if (switches != null && !switches.isEmpty()) {
				switchval = switches.get(0);
			}

		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return switchval;
	}

	public List<Switch> loadAllSwitches() {
		try {
			List<Switch> results = null;
			String hsql = SWITCH_CONTRACTOR + " from Switch s";
			Query q = getSession().createQuery(hsql.toString());
			results = q.list();
			if (results != null && !results.isEmpty()) {
				return results;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}

	public List<Switch> loadSwitchByAreaId(Long id) {
		List<Switch> results = new ArrayList<Switch>();
		try {
			String hsql = SWITCH_CONTRACTOR
					+ " from Switch s where s.areaId=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, id);
			results = q.list();
			if (!ArgumentUtils.isNullOrEmpty(results)) {
				return results;
			}
		} catch (HibernateException hbe) {
			logger.error("Error in loading data>>>>>>>>>>>>",
					hbe.fillInStackTrace());
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return results;
	}
	public List<Switch> loadSwitchByFloorId(Long id) {
		List<Switch> results = new ArrayList<Switch>();
		try {
			String hsql = SWITCH_CONTRACTOR
					+ " from Switch s where s.floorId=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, id);
			results = q.list();
			if (!ArgumentUtils.isNullOrEmpty(results)) {
				return results;
			}
		} catch (HibernateException hbe) {
			logger.error("Error in loading data>>>>>>>>>>>>",
					hbe.fillInStackTrace());
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return results;
	}

	public List<Switch> loadSwitchByBuildingId(Long id) {
		try {
			List<Switch> results = null;
			String hsql = SWITCH_CONTRACTOR
					+ " from Switch s where buildingId=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, id);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				return results;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}

	public List<Switch> loadSwitchByCampusId(Long id) {
		try {
			List<Switch> results = null;
			String hsql = SWITCH_CONTRACTOR + " from Switch s where campusId=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, id);
			results = q.list();
			if (results != null && !results.isEmpty()) {
				return results;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}

	public Switch updateAreaID(Switch oSwitch) {
		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, oSwitch.getId());
		switchval.setAreaId(oSwitch.getAreaId());
		session.update(switchval);
		return switchval;
	}

	public void updateSwitches(List<Switch> switches) {
		Session session = getSession();
		for (Switch switchval : switches) {
			getSession().merge(switchval);
		}

	}

	public Switch updatePosition(Long Id, Integer x, Integer y) {
		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, Id);
		switchval.setXaxis(x);
		switchval.setYaxis(y);
		session.saveOrUpdate("xaxis", switchval);
		session.saveOrUpdate("yaxis", switchval);
		return switchval;
	}

	public Switch updateLocation(Long Id, Long buildingId, Long campusId) {
		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, Id);
		switchval.setBuildingId(buildingId);
		switchval.setCampusId(campusId);
		session.update(switchval);
		return switchval;
	}

	public Long getLastSwitchId() {
		Long result = null;
		try {
			String hsql = "select max(id) from Switch";
			Query q = getSession().createQuery(hsql.toString());
			List<Long> results = q.list();
			if (results != null && !results.isEmpty()) {
				result = results.get(0);
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return result;
	}

	public Switch getSwitchToMove(Integer x) {
		Switch switchval = null;
		try {
			String hsql = SWITCH_CONTRACTOR + " from Switch s where x=?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, x);
			List<Switch> switches = q.list();
			if (switches != null && !switches.isEmpty()) {
				switchval = switches.get(0);
			}

		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return switchval;
	}

	public Switch updatePosition(String name, Integer xaxis, Integer yaxis) {
		Switch switchval = this.getswitchName(name);
		return this.updatePosition(switchval.getId(), xaxis, yaxis);
	}

	public Switch loadSwitchByNameandFloorId(String name, Long id) {
		Switch switchval = null;
		getSession().flush();
		try {
			String hsql = SWITCH_CONTRACTOR
					+ " from Switch s where name = ? and floorId = ?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, name);
			q.setParameter(1, id);
			List<Switch> switches = q.list();
			if (switches != null && !switches.isEmpty()) {
				switchval = switches.get(0);
			}

		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return switchval;
	}

	public Switch updateLocation(Switch switchval) {
		Switch currSwitch = this.loadSwitchByNameandFloorId(
				switchval.getName(), switchval.getFloorId());
		return this.updateLocation(currSwitch.getId(),
				switchval.getBuildingId(), switchval.getCampusId());
	}

	public Switch updateName(String name, Long Id) {
		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, Id);
		switchval.setName(name);
		session.update(switchval);
		return switchval;
	}

/*	public Switch updateDimmerControl(Long id, Integer dimmerControl) {

		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, id);
		switchval.setDimmerControl(dimmerControl);
		session.saveOrUpdate("dimmerControl", switchval);

		return switchval;
	}*/

/*	public Switch updateActiveControl(Long id, Integer activeControl) {

		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, id);
		switchval.setActiveControl(activeControl);
		session.saveOrUpdate("activeControl", switchval);

		return switchval;

	}*/

/*	public Switch updateSceneId(Long id, Long scene_id) {

		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, id);
		switchval.setSceneId(scene_id);
		session.saveOrUpdate("sceneId", switchval);

		return switchval;
	}*/

	public Switch updatePositionById(Switch switchval) {
		Session session = getSession();
		Switch oSwitch = (Switch) session.load(Switch.class, switchval.getId());
		oSwitch.setXaxis(switchval.getXaxis());
		oSwitch.setYaxis(switchval.getYaxis());
		session.saveOrUpdate("xaxis", oSwitch);
		session.saveOrUpdate("yaxis", oSwitch);
		return oSwitch;
	}

     
	public List loadSwitchDetailsByUserId(String uId) {
		List results = null;
		String hsql = " Select switch.id, switch.name, count(scene) , scene.id , scene.name, scene.sceneOrder  from Switch switch , UserSwitches us, "
				+ " Scene scene where switch.id = us.switchId and switch.id = scene.switchId and  us.userId = :uid group by switch.id, switch.name,scene.id,scene.name,  scene.sceneOrder order by scene.sceneOrder ";
		Query q = getSession().createQuery(hsql.toString());
		q.setParameter("uid", Long.parseLong(uId));
		results = q.list();
		return results;
	}
	

	/**
	 * @return  Switch list for user Admin , Added to give support for mobile admin login
	 */
	public List loadSwitchDetailsForAdmin() {
		List results = null;
		String hsql = " Select switch.id, switch.name, count(scene) ,scene.id , scene.name ,scene.sceneOrder  from Switch switch ,"
				+ " Scene scene where switch.id = scene.switchId group by switch.id, switch.name,scene.id,scene.name,scene.sceneOrder order by scene.sceneOrder";
		Query q = getSession().createQuery(hsql.toString());
		results = q.list();
		return results;
	}
	
	/**
	 * For New Mobile Design above 2.2 only
	 * @param uId
	 * @return
	 */
	public List loadSwitchByFacilityAssignedForUser(String uId) {
		/**
		 * sql query select distinct switch.id ,switch.name from
		 * switch,user_locations where ((user_locations.approved_location_type =
		 * 
		 * 'FLOOR' and user_locations.location_id = switch.floor_id) or
		 * (user_locations.approved_location_type = 'AREA' and
		 * user_locations.location_id
		 * 
		 * = switch.area_id)) and user_locations.user_id=4;
		 */
		List switchList = null;

		Session session = getSession();
		String hsql = "select distinct switch.id ,switch.name from Switch switch,UserLocations userLocations where ((userLocations.approvedLocationType = 'FLOOR' and userLocations.locationId = switch.floorId) or (userLocations.approvedLocationType ='AREA' and userLocations.locationId = switch.areaId)) and userLocations.user.id=:uid";
		Query q = session.createQuery(hsql);
		q.setParameter("uid", Long.parseLong(uId));
		switchList = q.list();

		return switchList;
	}
	
	public List loadSwitchByFacilityAssignedForAdmin() {
		/**
		 * sql query select distinct switch.id ,switch.name from
		 * switch,user_locations where ((user_locations.approved_location_type =
		 * 
		 * 'FLOOR' and user_locations.location_id = switch.floor_id) or
		 * (user_locations.approved_location_type = 'AREA' and
		 * user_locations.location_id
		 * 
		 * = switch.area_id)) and user_locations.user_id=4;
		 */
		List switchList = null;		
		Session session = getSession();
		String hsql = "select distinct switch.id ,switch.name from Switch switch";
		Query q = session.createQuery(hsql);
		switchList = q.list();
		
		return switchList;
	}

	public Switch loadSwitchByNameandAreaId(String name, Long pid) {
		Switch switchval = null;
		getSession().flush();
		try {
			String hsql = SWITCH_CONTRACTOR
					+ " from Switch s where name = ? and areaId = ?";
			Query q = getSession().createQuery(hsql.toString());
			q.setParameter(0, name);
			q.setParameter(1, pid);
			List<Switch> switches = q.list();
			if (switches != null && !switches.isEmpty()) {
				switchval = switches.get(0);
			}

		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return switchval;
	}

	/**
	 * return list of Gateway object associated with secondary gateway
	 *
	 * @param secGwId
	 * @return list of Gateway object associated with secondary gateway
	 */
	@SuppressWarnings("unchecked")
	public List<Switch> loadAllWdsByGatewayId(Long secGwId) {
		Session session = getSession();
		List<Switch> wdsList = session
				.createCriteria(Switch.class)
				.add(Restrictions.eq("state",ServerConstants.WDS_STATE_DISCOVER_STR))
				.add(Restrictions.eq("gatewayId",secGwId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.addOrder(Order.asc("id")).list();
		if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
			return wdsList;
		} else {
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	public List<Switch> loadWdsSwitchById(Long wdsId) {
		Session session = getSession();
		List<Switch> wdsList = session
				.createCriteria(Switch.class)
				.add(Restrictions.eq("state",ServerConstants.WDS_STATE_DISCOVER_STR))
				.add(Restrictions.eq("id",wdsId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.addOrder(Order.asc("id")).list();
		if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
			return wdsList;
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Switch getSwitchById(Long wdsId) {
		Session session = getSession();
		Switch wds = (Switch) session
				.createCriteria(Switch.class).add(Restrictions.eq("id",wdsId)).uniqueResult();
		return wds;
	}

	public Switch updateWds(Switch savedWds) {
		Session session = getSession();
		session.update(savedWds);
		return savedWds;
	}
	@SuppressWarnings("unchecked")
	public List<Switch> getUnCommissionedWDSList(long gatewayId) {
		Session session = getSession();
		List<Switch> wdsList = session
				.createCriteria(Switch.class)
				.add(Restrictions.ne("state",
						ServerConstants.WDS_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.WDS_STATE_COMMISSIONED_STR))
				.add(Restrictions.eq("gatewayId", gatewayId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
			return wdsList;
		} else {
			return null;
		}
	}
	
	/*public String getCommissionStatus(long wdsId) {
		Session session = getSession();
		Switch switchObj = (Switch) session.get(Switch.class, wdsId);
		String sStatus = switchObj.getState();
		if (sStatus != null) {
			return sStatus;
		}
		return "";
	}*/ 
	
    public Switch getWdsSwitchBySnapAddress(String snapAddress) {
        Session session = getSession();
        Switch wds = (Switch) session.createCriteria(Switch.class).
                add(Restrictions.eq("snapAddress", snapAddress))
                .uniqueResult();
        if (wds != null) {
            return wds;
        } else {
            return null;
        }
    }

    public Switch AddWdsSwitch(Switch oSwitch) {
        return (Switch) saveObject(oSwitch);
    }
    
    public Switch createNewSwitch(Switch sw) {
    	return (Switch) saveObject(sw);
    }

    
    public SwitchGroup loadSwitchGroupByGemsGroupId(Long gemsGroupId) {
        Session session = getSession();
        SwitchGroup oSGroup = (SwitchGroup)session
                .createCriteria(SwitchGroup.class)
                .add(Restrictions.eq("gemsGroup.id",gemsGroupId)).uniqueResult();
        return oSGroup;
    }
    
    public Switch loadSwitchByGemsGroupId(Long gemsGroupId) {
    	
    	Session session = getSession();
    	Switch oSwitch = (Switch)session.createCriteria(Switch.class).
    			add(Restrictions.eq("gemsGroup.id", gemsGroupId)).uniqueResult();
    	return oSwitch;
    	
    } //end of method loadSwitchByGemsGroupId

	public List<SceneTemplate> getAllSceneTemplates() {
		Session session = getSession();
		List<SceneTemplate> oList = session.createCriteria(SceneTemplate.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return oList;
	}

	public List<SceneLightLevelTemplate> getAllLightLevelsForSceneTemplate(
			Long sceneTemplateId) {
		Session session = getSession();
		List<SceneLightLevelTemplate> oList = session
				.createCriteria(SceneLightLevelTemplate.class)
				.add(Restrictions.eq("sceneTemplateId", sceneTemplateId))
				.addOrder(Order.asc("sceneOrder"))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return oList;
	}

}
