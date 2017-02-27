package com.ems.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Switch;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.SwitchDetail;

@Repository("switchDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(SwitchDao.class.getName());

	public static final String SWITCH_CONTRACTOR = "Select new Switch(s.id,"
			+ "s.name," + "s.floorId," + "s.buildingId," + "s.campusId,"
			+ "s.xaxis," + "s.yaxis," + "s.dimmerControl," + "s.sceneId,"
			+ "s.activeControl)";

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

	public Switch update(Switch switchval) {
		Query query = getSession().createQuery(
				"Update Switch set " + "name = :name," + "floorId = :floorId,"
						+ "buildingId = :buildingId," + "campusId = :campusId,"
						+ " where id = :id");
		query.setLong("id", switchval.getId());
		query.setLong("floorId", switchval.getFloorId());
		query.setString("name", switchval.getName());
		query.setLong("buildingId", switchval.getBuildingId());
		query.setLong("campusId", switchval.getCampusId());
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

	public Switch updateDimmerControl(Long id, Integer dimmerControl) {

		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, id);
		switchval.setDimmerControl(dimmerControl);
		session.saveOrUpdate("dimmerControl", switchval);

		return switchval;
	}

	public Switch updateActiveControl(Long id, Integer activeControl) {

		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, id);
		switchval.setActiveControl(activeControl);
		session.saveOrUpdate("activeControl", switchval);

		return switchval;

	}

	public Switch updateSceneId(Long id, Long scene_id) {

		Session session = getSession();
		Switch switchval = (Switch) session.load(Switch.class, id);
		switchval.setSceneId(scene_id);
		session.saveOrUpdate("sceneId", switchval);

		return switchval;
	}

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
		String hsql = " Select switch.id, switch.name, count(scene) ,switch.dimmerControl , scene.id , scene.name  from Switch switch , UserSwitches us, "
				+ " Scene scene where switch.id = us.switchId and switch.id = scene.switchId and  us.userId = :uid group by switch.id, switch.name,switch.dimmerControl,scene.id,scene.name  ";
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
		String hsql = " Select switch.id, switch.name, count(scene) ,switch.dimmerControl , scene.id , scene.name  from Switch switch ,"
				+ " Scene scene where switch.id = scene.switchId group by switch.id, switch.name,switch.dimmerControl,scene.id,scene.name  ";
		Query q = getSession().createQuery(hsql.toString());
		results = q.list();
		return results;
	}

}
