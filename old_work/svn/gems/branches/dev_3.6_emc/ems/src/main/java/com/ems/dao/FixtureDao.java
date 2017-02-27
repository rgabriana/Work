package com.ems.dao;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.hvac.model.Sensor;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.Bulb;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureCustomGroupsProfile;
import com.ems.model.Gateway;
import com.ems.model.Groups;
import com.ems.model.OutageBasePower;
import com.ems.model.Profile;
import com.ems.model.ProfileConfiguration;
import com.ems.model.ProfileHandler;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerConstants;
import com.ems.server.util.ServerUtil;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.DeviceType;
import com.ems.types.FacilityType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.FixtureLampStatusVO;
import com.ems.vo.model.FixtureList;
import com.ems.vo.model.FixtureOutageVO;

/**
 *
 * @author pankaj kumar chauhan
 *
 */
@Repository("fixtureDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger("FixtureLogger");
	
	@Resource
	SystemConfigurationManager sysConfigMgr;
	
	private Map<Long, List<BallastVoltPower>> ballastVoltPowersMap = new HashMap<Long, List<BallastVoltPower>>();
    private ConcurrentHashMap<Long, ConcurrentHashMap<Double, List<BallastVoltPower>>> ballastVPCurveMap = new ConcurrentHashMap<Long, ConcurrentHashMap<Double, List<BallastVoltPower>>>();

	// public static final String FIXTURE_CONTRACTOR =
	// "Select new Fixture(f.id,"
	// + "f.sensorId," + "f.type," + "f.ballastType,"
	// + "f.ballastLastChanged," + "f.noOfBulbs," + "f.xaxis,"
	// + "f.yaxis," + "f.floor.id," + "f.buildingId," + "f.campusId,"
	// + "f.area.id," + "f.subArea.id," + "f.dimmerControl,"
	// + "f.currentState," + "f.lastOccupancySeen," + "f.lightLevel,"
	// + "f.savingsType," + "f.snapAddress," + "f.fixtureName,"
	// + "f.macAddress," + "f.channel," + "f.version," + "f.aesKey,"
	// + "f.bulbLife," + "f.gateway.id," + "f.gateway.gatewayName,"
	// + "f.description," + "f.notes," + "f.bulbsLastServiceDate,"
	// + "f.ballastLastServiceDate," + "f.active," + "f.state,"
	// + "f.ballast.id," + "f.ballast.itemNum," + "f.ballast.ballastName,"
	// + "f.ballast.inputVoltage," + "f.ballast.lampType,"
	// + "f.ballast.lampNum," + "f.ballast.ballastFactor,"
	// + "f.ballast.wattage," + "f.ballast.ballastManufacturer,"
	// + "f.bulb.id," + "f.bulb.manufacturer," + "f.bulb.bulbName,"
	// + "f.bulb.type," + "f.bulb.initialLumens," + "f.bulb.designLumens,"
	// + "f.bulb.energy," + "f.bulb.lifeInsStart,"
	// + "f.bulb.lifeProgStart," + "f.bulb.diameter," + "f.bulb.length,"
	// + "f.bulb.cri," + "f.bulb.colorTemp," + "f.currentProfile,"
	// + "f.originalProfileFrom," + "f.location," + "f.noOfFixtures,"
	// + "f.profileHandler.id," + "f.lastConnectivityAt," + "f.ipAddress,"
	// + "f.commType," + "f.lastStatsRcvdTime," + "f.profileChecksum,"
	// + "f.globalProfileChecksum," + "f.currApp," + "f.firmwareVersion,"
	// + "f.bootLoaderVersion," + "f.groupId," + "f.pushProfile,"
	// + "f.pushGlobalProfile," + "f.wattage,"
	// + "f.ballast.voltPowerMapId," + "f.secGwId," + "f.upgradeStatus,"
	// + "f.avgTemperature," + "f.baselinePower,"
	// + "f.voltage, f.commissionStatus, f.isHopper, f.versionSynced)";
	//
	/**
	 * Load fixture details.load all fixtures of given group
	 *
	 * @param id
	 *            group id
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis, group id details of fixture
	 *         other details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureByGroupId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("groupId", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// Session s = getSession();
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where f.groupId=?" + " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = s.createQuery(hsql.toString());
		// q.setParameter(0, id);
		// results = q.list();
		// if (!ArgumentUtils.isNullOrEmpty(results)) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// logger.error("Error in loading data>>>>>>>>>>>>",
		// hbe.fillInStackTrace());
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Load fixture locations.load all fixtures of given floor
	 *
	 * @param id
	 *            floor id
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis details of fixture other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureLocationsByFloorId(Long id) {

		try {
			Session s = getSession();
			String hsql = "SELECT f.id, f.name, f.xaxis, f.yaxis, f.groupId, f.macAddress,  f.areaId  "
					+ "from Fixture as f where f.floor.id = :floorId and state = 'COMMISSIONED' order by f.id ";
			Query q = s.createQuery(hsql);
			q.setLong("floorId", id);
			return q.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	} // end of method loadFixtureLocationsByFloorId

	/**
	 * Load fixture details.load all fixtures of given floor
	 *
	 * @param id
	 *            floor id
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis details of fixture other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureByFloorId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("floor.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// Session s = getSession();
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where f.floor.id=?" + " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = s.createQuery(hsql.toString());
		// q.setParameter(0, id);
		// results = q.list();
		// if (!ArgumentUtils.isNullOrEmpty(results)) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// logger.error("Error in loading data>>>>>>>>>>>>",
		// hbe.fillInStackTrace());
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}
	
	
	/**
	 * Load all fixture Id's of given ballast Id
	 *
	 * @param id
	 *            ballast id
	 * @return com.ems.model.Fixture Id's collection
	 */
	@SuppressWarnings("unchecked")
	public List<BigInteger> loadFixturesIdListByBallastId(Long id) {
		
		String hsql = "SELECT f.id from fixture f"
			+ " where f.state !=  '"+ServerConstants.FIXTURE_STATE_DELETED_STR+"' and f.state !=  '"+ServerConstants.FIXTURE_STATE_PLACED_STR+"' and"
			+ " f.ballast_id = "+id;

		Query q = getSession().createSQLQuery(hsql.toString());
		List<BigInteger> results = (List<BigInteger>)q.list();
		
		if (!ArgumentUtils.isNullOrEmpty(results)) {
			return results;
		} else {
			return null;
		}
		
	}
	
	/**
	 * Load all uncommissioned fixture Id's of given ballast Id
	 *
	 * @param id
	 *            ballast id
	 * @return com.ems.model.Fixture Id's collection
	 */
	@SuppressWarnings("unchecked")
	public List<BigInteger> loadUncommissionedFixturesIdListByBallastIdIfNoneCommissioned(Long id) {
		
		String hsql = "SELECT f.id from fixture f"
				+ " where f.state =  '"+ServerConstants.FIXTURE_STATE_COMMISSIONED_STR+"' and"
				+ " f.ballast_id = "+id;
		
		Query q = getSession().createSQLQuery(hsql.toString());
		List<BigInteger> results = (List<BigInteger>)q.list();
		if (ArgumentUtils.isNullOrEmpty(results)) {		
			hsql = "SELECT f.id from fixture f"
					+ " where f.state !=  '"+ServerConstants.FIXTURE_STATE_DELETED_STR+"' and f.state !=  '"+ServerConstants.FIXTURE_STATE_COMMISSIONED_STR+"' and"
					+ " f.ballast_id = "+id;
			q = getSession().createSQLQuery(hsql.toString());
			results = (List<BigInteger>)q.list();
			return results;
		}
				
		return null;	
	}
	
	/**
	 * Count of commissioned fixtures of given bulb Id
	 *
	 * @param id
	 *            bulb id
	 * @return com.ems.model.Fixture Id's collection
	 */
	@SuppressWarnings("unchecked")
	public int loadCommissionedFixturesCountByBulbId(Long id) {
		
		String hsql = "SELECT f.id from fixture f"
			+ " where f.state =  '"+ServerConstants.FIXTURE_STATE_COMMISSIONED_STR+"' and"
			+ " f.bulb_id = "+id;

		Query q = getSession().createSQLQuery(hsql.toString());
		List<BigInteger> results = (List<BigInteger>)q.list();
		
		if (!ArgumentUtils.isNullOrEmpty(results)) {
			return results.size();
		} else {
			return 0;
		}
		
	}
	
	
	/**
	 * Count of commissioned fixture Id's of given fixture class Id
	 *
	 * @param id
	 *            fixture class id
	 * @return com.ems.model.Fixture Id's collection
	 */
	@SuppressWarnings("unchecked")
	public int loadCommissionedFixturesCountByFixtureClassId(Long id) {
		
		String hsql = "SELECT f.id from fixture f"
			+ " where f.state =  '"+ServerConstants.FIXTURE_STATE_COMMISSIONED_STR+"' and"
			+ " f.fixture_class_id = "+id;

		Query q = getSession().createSQLQuery(hsql.toString());
		List<BigInteger> results = (List<BigInteger>)q.list();
		
		if (!ArgumentUtils.isNullOrEmpty(results)) {
			return results.size();
		} else {
			return 0;
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Fixture> loadPlacedAndCommissionedFixtureByFloorId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				//.createAlias("bulb", "bulb", Criteria.LEFT_JOIN)
				//.createAlias("ballast", "ballast", Criteria.LEFT_JOIN)
				.createAlias("fixtureclass", "fixtureclass",Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DISCOVER_STR))
				.add(Restrictions.eq("floor.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
	}
	
	/**
	 * Load fixture details.load all fixtures of given floor
	 *
	 * @param id
	 *            floor id
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis details of fixture other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadPlacedFixtureByFloorId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.eq("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("floor.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
	}
	
	/**
	 * load all fixtures (including deleted) for a given floor
	 *
	 * @param id
	 *            floor id
	 * @return com.ems.model.Fixture collection 
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadAllFixtureByFloorId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.eq("floor.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
	}

	/**
	 * Load fixture details.load all fixtures of given building
	 *
	 * @param id
	 *            building id
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis details of fixture other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureByBuildingId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("buildingId", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where buildingId=?" + " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, id);
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Load fixture details.load all fixtures of given campus
	 *
	 * @param id
	 *            campus id
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis details of fixture other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureByCampusId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("campusId", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR + " from Fixture f where campusId=?"
		// + " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, id);
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Loads all the fixtures belonging to an area which are not at 100%
	 *
	 * @param id
	 * @return com.ems.model.Fixture collection
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> getDimmedFixturseByAreaId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("area.id", id))
				.add(Restrictions.ne("dimmerControl", 100))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		
	}
	
	/**
	 * Loads all the fixtures belonging to an area
	 *
	 * @param id
	 * @return com.ems.model.Fixture collection
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureByAreaId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("area.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// List<Fixture> fixtures = null;
		// String hsql = "from Fixture where area.id = :areaId" +
		// " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query query = getSession().createQuery(hsql.toString());
		// query.setLong("areaId", id);
		// fixtures = query.list();
		// if (!ArgumentUtils.isNullOrEmpty(fixtures)) {
		// return fixtures;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Loads all the fixtures belonging to a sub-area
	 *
	 * @param id
	 * @return com.ems.model.Fixture collection
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureBySubareaId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("subareaId", id)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// List<Fixture> fixtures = null;
		// String hsql = "from Fixture where subArea.id = :subareaId" +
		// " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query query = getSession().createQuery(hsql.toString());
		// query.setLong("subareaId", id);
		// fixtures = query.list();
		// if (!ArgumentUtils.isNullOrEmpty(fixtures)) {
		// return fixtures;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	public void updateProfileHandler(ProfileHandler profileHandler) {
		Session session = getSession();
		if (profileHandler.getProfileConfiguration() != null) {
			logger.debug("Config id is: "
					+ profileHandler.getProfileConfiguration().getId());
		}
		session.saveOrUpdate(profileHandler);
		logger.debug("Updated Profile Handler");
	}

	public void enablePushProfileForFixture(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setPushProfile(true);
		// session.saveOrUpdate("push_profile", fixture);
	}

	public void enablePushGlobalProfileForFixture(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setPushGlobalProfile(true);
		// session.saveOrUpdate("push_global_profile", fixture);
	}

	public void resetPushProfileForFixture(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setPushProfile(false);
		// session.saveOrUpdate("push_profile", fixture);
	}

	public void resetPushGlobalProfileForFixture(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setPushGlobalProfile(false);
		// session.saveOrUpdate("push_global_profile", fixture);
	}

	public void changeFixtureProfile(Long fixtureId, Long groupId,
			Long globalPFId, String currentProfile, String originalProfileFrom) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		Groups groups = (Groups) session.get(Groups.class, groupId);
		String currProfile = fixture.getCurrentProfile();
		fixture.setGroupId(groupId);
		if(!currProfile.equals(currentProfile))
		{
			fixture.setCurrentProfile(groups.getName());
			fixture.setOriginalProfileFrom(currProfile);
		}
		/*
		ProfileHandler groupProfileHandler = getProfileHandlerByGroupId(groupId);
		// Copy the group profile data to fixture's profile.
		ProfileHandler fixtureProfileHandler = getProfileHandlerByFixtureId(fixtureId);
		if (fixtureProfileHandler.getId().longValue() == globalPFId.longValue()) {
			fixtureProfileHandler = groupProfileHandler.copy();
			if (groupId == 0)
				fixtureProfileHandler.setProfileGroupId((byte) 0);
			updateProfileHandler(fixtureProfileHandler);

			Session session = getSession();
			Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
			Groups groups = (Groups) session.get(Groups.class, groupId);
			fixture.setGroupId(groupId);
			groups.setProfileHandler(fixtureProfileHandler);
			//fixture.setProfileHandler(fixtureProfileHandler);
			fixture.setCurrentProfile(currentProfile);
			fixture.setOriginalProfileFrom(originalProfileFrom);
			fixture.setPushProfile(true);
			fixture.setPushGlobalProfile(true);
			// session.saveOrUpdate("groupId", fixture);
			// session.saveOrUpdate("profileHandler", fixture);
			// session.saveOrUpdate("currentProfile", fixture);
			// session.saveOrUpdate("originalProfileFrom", fixture);

			// enablePushProfileForFixture(fixtureId);
			// enablePushGlobalProfileForFixture(fixtureId);
		} else {
			fixtureProfileHandler.copyFrom(groupProfileHandler);
			if (groupId == 0)
				fixtureProfileHandler.setProfileGroupId((byte) 0);
			updateProfileHandler(fixtureProfileHandler);

			Session session = getSession();
			Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
			Groups groups = (Groups) session.get(Groups.class, groupId);
			fixture.setGroupId(groupId);
			fixture.setCurrentProfile(currentProfile);
			fixture.setOriginalProfileFrom(originalProfileFrom);
			fixture.setPushProfile(true);
			fixture.setPushGlobalProfile(true);

			// session.saveOrUpdate("groupId", fixture);
			// session.saveOrUpdate("currentProfile", fixture);
			// session.saveOrUpdate("originalProfileFrom", fixture);

			// enablePushProfileForFixture(fixtureId);
			// enablePushGlobalProfileForFixture(fixtureId);
		}
		*/
	}
	
	public void assignProfileToFixture(Long fixtureId, Long groupId) {
		
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		Groups groups = (Groups) session.get(Groups.class, groupId);
		String currProfile = fixture.getCurrentProfile();
		fixture.setGroupId(groupId);		
		fixture.setCurrentProfile(groups.getName());
		fixture.setOriginalProfileFrom(currProfile);
	
	} //end of method assignProfileToFixture

	public void assignGroupProfileToFixtureProfile(Long fixtureId, Long groupId) {
		/*
		ProfileHandler groupProfileHandler = getProfileHandlerByGroupId(groupId);
		// Copy the group profile data to fixture's profile.
		ProfileHandler fixtureProfileHandler = getProfileHandlerByFixtureId(fixtureId);
		fixtureProfileHandler.copyFrom(groupProfileHandler);
		updateProfileHandler(fixtureProfileHandler);
		*/
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		Groups groups = (Groups) session.get(Groups.class, groupId);
		String currProfile = fixture.getCurrentProfile();
		fixture.setGroupId(groupId);
		fixture.setCurrentProfile(groups.getName());
		fixture.setOriginalProfileFrom(currProfile);
		// session.saveOrUpdate("groupId", fixture);
		// session.saveOrUpdate("currentProfile", fixture);
		// session.saveOrUpdate("originalProfileFrom", fixture);

	}

	public void syncFixtureProfile(ProfileHandler newPFH, Long fixtureId) {
		ProfileHandler fixtureProfileHandler = getProfileHandlerByFixtureId(fixtureId);
		fixtureProfileHandler.copyFrom(newPFH);
		updateProfileHandler(fixtureProfileHandler);

		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		String currProfile = fixture.getCurrentProfile();
		fixture.setGroupId(0L);
		fixture.setCurrentProfile("Custom");
		fixture.setOriginalProfileFrom(currProfile);
		// session.saveOrUpdate("groupId", fixture);
		// session.saveOrUpdate("currentProfile", fixture);
		// session.saveOrUpdate("originalProfileFrom", fixture);

	}

	public void upgradeFixtureProfile(ProfileHandler newPFH, Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		String currProfile = fixture.getCurrentProfile();
		
		//fixture.setProfileHandler(newPFH);
		
		fixture.setGroupId(0L);
		
		fixture.setCurrentProfile("Custom");
		fixture.setOriginalProfileFrom(currProfile);
		// session.saveOrUpdate("groupId", fixture);
		// session.saveOrUpdate("profileHandler", fixture);
		// session.saveOrUpdate("currentProfile", fixture);
		// session.saveOrUpdate("originalProfileFrom", fixture);

	}

	public void updateProfileHandler(ProfileHandler profileHandler,
			Long fixtureId, Long groupId, String currentProfile,
			String originalProfileFrom) {
		Session s = getSession();
		Fixture fixture = (Fixture) s.get(Fixture.class, fixtureId);
		fixture.setGroupId(groupId);
		fixture.setCurrentProfile(currentProfile);
		fixture.setOriginalProfileFrom(originalProfileFrom);
		
		Groups group = ((Groups) s.get(Groups.class,groupId));
		group.setProfileHandler(profileHandler);
		//fixture.setProfileHandler(profileHandler);
		
		fixture.setPushProfile(true);
		// s.saveOrUpdate("groupId", fixture);
		// s.saveOrUpdate("currentProfile", fixture);
		// s.saveOrUpdate("originalProfileFrom", fixture);
		// s.saveOrUpdate("profileHandler", fixture);
		// enablePushProfileForFixture(fixtureId);
	}

	public void updateProfileHandlerIdForFixture(Long fixtureId,
			Long profileHandlerId, String currentProfile,
			String originalProfileFrom) {
		Session s = getSession();
		Fixture fixture = (Fixture) s.get(Fixture.class, fixtureId);
		
		Groups group = ((Groups) s.get(Groups.class,fixture.getGroupId()));
		group.setProfileHandler((ProfileHandler) s.load(ProfileHandler.class,	profileHandlerId));
		
		//fixture.setProfileHandler((ProfileHandler) s.load(ProfileHandler.class,	profileHandlerId));
		fixture.setCurrentProfile(currentProfile);
		fixture.setOriginalProfileFrom(originalProfileFrom);

		// SQLQuery query = getSession()
		// .createSQLQuery(
		// "update fixture set profile_handler_id = :pHId, current_profile = :currentProfile, original_profile_from = :originalProfile where id = :fixtureId");
		// query.setLong("pHId", profileHandlerId);
		// query.setString("currentProfile", currentProfile);
		// query.setString("originalProfile", originalProfileFrom);
		// query.setLong("fixtureId", fixtureId);
		// query.executeUpdate();
	}

	public ProfileHandler getProfileHandlerByFixtureId(Long fixtureId) {
		Session session = getSession();
		
		Long fixtureGroupId;
		fixtureGroupId = ((Fixture) session.get(Fixture.class,fixtureId)).getGroupId();
		
		ProfileHandler profileHandler = getProfileHandlerByGroupId(fixtureGroupId);
		
		//ProfileHandler profileHandler = ((Fixture) session.get(Fixture.class,fixtureId)).getProfileHandler();
		
		// try {
		// profileHandler = (ProfileHandler) session
		// .createQuery(
		// "from ProfileHandler where id = (Select profileHandler.id from Fixture where id = :id)")
		// .setLong("id", fixtureId).uniqueResult();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// session.clear();
		if (profileHandler != null) {
			profileHandler.setDayProfile((Profile) getObject(Profile.class,
					profileHandler.getDayProfile().getId()));
			profileHandler.setDayProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getDayProfileHoliday()
							.getId()));
			profileHandler.setDayProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getDayProfileWeekEnd()
							.getId()));

			profileHandler.setEveningProfile((Profile) getObject(Profile.class,
					profileHandler.getEveningProfile().getId()));
			profileHandler.setEveningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileHoliday()
							.getId()));
			profileHandler.setEveningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileWeekEnd()
							.getId()));

			profileHandler.setMorningProfile((Profile) getObject(Profile.class,
					profileHandler.getMorningProfile().getId()));
			profileHandler.setMorningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileHoliday()
							.getId()));
			profileHandler.setMorningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileWeekEnd()
							.getId()));

			profileHandler.setNightProfile((Profile) getObject(Profile.class,
					profileHandler.getNightProfile().getId()));
			profileHandler.setNightProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getNightProfileHoliday()
							.getId()));
			profileHandler.setNightProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getNightProfileWeekEnd()
							.getId()));
			
			profileHandler.setOverride5((Profile) getObject(
					Profile.class, profileHandler.getOverride5()
							.getId()));
			profileHandler.setOverride6((Profile) getObject(
					Profile.class, profileHandler.getOverride6()
							.getId()));
			profileHandler.setOverride7((Profile) getObject(
					Profile.class, profileHandler.getOverride7()
							.getId()));
			profileHandler.setOverride8((Profile) getObject(
					Profile.class, profileHandler.getOverride8()
							.getId()));

			profileHandler
					.setProfileConfiguration((ProfileConfiguration) getObject(
							ProfileConfiguration.class, profileHandler
									.getProfileConfiguration().getId()));
		}
		return profileHandler;
	}

	public ProfileHandler getProfileHandlerByGroupId(Long groupId) {
		Session session = getSession();
		ProfileHandler profileHandler = ((Groups) session.get(Groups.class,
				groupId)).getProfileHandler();
		// try {
		// profileHandler = (ProfileHandler) session
		// .createQuery("from ProfileHandler where id = (Select profileHandler.id from Groups where id = :id)")
		// .setLong("id", groupId).uniqueResult();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// session.clear();
		if (profileHandler != null) {
			profileHandler.setDayProfile((Profile) getObject(Profile.class,
					profileHandler.getDayProfile().getId()));
			profileHandler.setDayProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getDayProfileHoliday()
							.getId()));
			profileHandler.setDayProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getDayProfileWeekEnd()
							.getId()));

			profileHandler.setEveningProfile((Profile) getObject(Profile.class,
					profileHandler.getEveningProfile().getId()));
			profileHandler.setEveningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileHoliday()
							.getId()));
			profileHandler.setEveningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileWeekEnd()
							.getId()));

			profileHandler.setMorningProfile((Profile) getObject(Profile.class,
					profileHandler.getMorningProfile().getId()));
			profileHandler.setMorningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileHoliday()
							.getId()));
			profileHandler.setMorningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileWeekEnd()
							.getId()));

			profileHandler.setNightProfile((Profile) getObject(Profile.class,
					profileHandler.getNightProfile().getId()));
			profileHandler.setNightProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getNightProfileHoliday()
							.getId()));
			profileHandler.setNightProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getNightProfileWeekEnd()
							.getId()));
			
			profileHandler.setOverride5((Profile) getObject(
					Profile.class, profileHandler.getOverride5()
							.getId()));
			profileHandler.setOverride6((Profile) getObject(
					Profile.class, profileHandler.getOverride6()
							.getId()));
			profileHandler.setOverride7((Profile) getObject(
					Profile.class, profileHandler.getOverride7()
							.getId()));
			profileHandler.setOverride8((Profile) getObject(
					Profile.class, profileHandler.getOverride8()
							.getId()));

			profileHandler
					.setProfileConfiguration((ProfileConfiguration) getObject(
							ProfileConfiguration.class, profileHandler
									.getProfileConfiguration().getId()));
		}
		return profileHandler;
	}

	public Fixture updatePosition(Long fixtureId, Integer x, Integer y,
			String state) {

		Session session = getSession();
		Fixture fixture = (Fixture) session.load(Fixture.class, fixtureId);
		fixture.setXaxis(x);
		fixture.setYaxis(y);
		// session.saveOrUpdate("xaxis", fixture);
		// session.saveOrUpdate("yaxis", fixture);
		return fixture;

	}

	public void updateFixtures(List<Fixture> fixtures) {

		Session session = getSession();
		for (Fixture fixture : fixtures) {
			getSession().update(fixture);
		}

	}

	public Fixture update(Fixture fixture) {
		Session session = getSession();
		session.update(fixture);

		// Query query = getSession().createQuery(
		// "Update Fixture set " + "fixtureName = :fixtureName," +
		// "sensorId = :sensorId," + "type = :type,"
		// + "noOfBulbs = :noOfBulbs," + "bulbWattage = :bulbWattage," +
		// "wattage = :wattage,"
		// + "ballastManufacturer = :ballastManufacturer," +
		// "bulbManufacturer = :bulbManufacturer,"
		// + "ballast = :ballast," + "bulb = :bulb," +
		// "noOfFixtures = :noOfFixtures,"
		// + "description = :description," + "notes = :notes," +
		// "voltage = :voltage,"
		// + "isHopper = :isHopper" + " where id = :id");
		// query.setLong("id", fixture.getId());
		// query.setString("sensorId", fixture.getSensorId());
		// query.setString("fixtureName", fixture.getFixtureName());
		// query.setString("type", fixture.getType());
		// query.setInteger("noOfBulbs", fixture.getNoOfBulbs());
		// query.setInteger("bulbWattage", fixture.getBulbWattage());
		// query.setInteger("wattage", fixture.getWattage());
		// query.setString("ballastManufacturer",
		// fixture.getBallastManufacturer());
		// query.setString("bulbManufacturer", fixture.getBulbManufacturer());
		// query.setEntity("ballast", fixture.getBallast());
		// query.setEntity("bulb", fixture.getBulb());
		// query.setInteger("noOfFixtures", fixture.getNoOfFixtures());
		// query.setString("description", fixture.getDescription());
		// query.setString("notes", fixture.getNotes());
		// query.setShort("voltage", fixture.getVoltage());
		// query.setInteger("isHopper", fixture.getIsHopper());
		// query.executeUpdate();
		return fixture;
	}

	public Fixture updateFixture(Fixture fixture) {
		Session session = getSession();
		session.update(fixture);
		// Fixture fixture = (Fixture) session.load(Fixture.class,
		// fixtureObj.getId());
		//
		// fixture.setCurrentProfile(fixtureObj.getCurrentProfile());
		// fixture.setFixtureName(fixtureObj.getFixtureName());
		// fixture.setIsHopper(fixtureObj.getIsHopper());
		// fixture.setNoOfBulbs(fixtureObj.getNoOfBulbs());
		// fixture.setNoOfFixtures(fixtureObj.getNoOfFixtures());
		// fixture.setVoltage(fixtureObj.getVoltage());
		//
		// Ballast ballast = (Ballast) session.load(Ballast.class,
		// fixtureObj.getBallast().getId());
		// fixture.setBallast(ballast);
		//
		// Bulb bulb = (Bulb) session.load(Bulb.class,
		// fixtureObj.getBulb().getId());
		// fixture.setBulb(bulb);
		//
		// getSession().update(fixture);
		return fixture;
	}

	public void saveOrUpdateFixture(Fixture fixture) {
		getSession().saveOrUpdate(fixture);
	}

	/**
	 * update dimmerControl for all ids passed in ids List
	 *
	 * @param ids
	 *            collection of ids
	 * @param dimmerControl
	 *            new dimmerControl value
	 */
	@SuppressWarnings("unchecked")
	public void updateDimmerControl(List<Long> ids, Integer dimmerControl) {
		try {
			if (!ids.isEmpty() && ids != null) {
				List<Long> tmp = new ArrayList<Long>();
				for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
					Long id = new Long(iterator.next() + "");
					tmp.add(id);
				}
				String hql = "update Fixture set dimmerControl = :newValue where id in (:ids)";
				Query query = getSession().createQuery(hql);
				query.setInteger("newValue", dimmerControl);
				query.setParameterList("ids", tmp);
				query.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * update Gateway Id for all fixtures in case of GW RMA
	 *
	 * @param fromGatewayId
	 *            Old Gateway Id
	 * @param toGatewayId
	 *            New Gateway Id
	 * @return 
	 */
	public void updateGatewayId(Long fromGatewayId, Long toGatewayId){
		try {
			Query query = getSession().createQuery("Update Fixture set " + "gateway.id = :newGWId, "
					+ "secGwId = :newGWId" + " where gateway.id = :oldGWId");			
			
			query.setLong("newGWId", toGatewayId);
			query.setLong("oldGWId", fromGatewayId);
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Load fixture Id's for given gateway Id
	 *
	 * @param id
	 *            gateway id
	 * @return com.ems.model.Fixture Id's collection
	 */
	@SuppressWarnings("unchecked")
	public List<Long> loadFixturesIdListByGatewayId(Long gatewayId) {
		
		Query query = getSession().createQuery("SELECT f.id from Fixture f where f.gateway.id = :gatewayId");	
		
		query.setLong("gatewayId", gatewayId);		
		List<Long> results = (List<Long>)query.list();
		
		if (!ArgumentUtils.isNullOrEmpty(results)) {
			return results;
		} else {
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List getAllFixtures() {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// SQLQuery sqlQuery = getSession()
		// .createSQLQuery(
		// "select f.id,f.type,cmp.name as campus_name,b.name as building_name,fl.name as floor_name,ar.name as area_name from fixture f inner join (select id,name from floor) fl on f.floor_id=fl.id inner join  (select id,name from campus) cmp on f.campus_id=cmp.id inner join (select id,name from building) b on f.building_id=b.id left join (select id,name from area) ar on f.area_id=ar.id order by f.id");
		// List list = sqlQuery.list();
		// return list;
	}

	/**
	 * return list of Gateway object associated with gateway
	 *
	 * @param gatewayId
	 * @return list of Gateway object associated with gateway
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadAllFixtureByGatewayId(Long gatewayId) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("gateway.id", gatewayId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where f.gateway.id=?" + " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, gatewayId);
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * return list of commissioned fixture objects associated with secondary
	 * gateway
	 *
	 * @param secGwId
	 * @return list of fixture objects associated with secondary gateway
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> getAllCommissionedFixturesBySecGwId(Long secGwId) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.eq("state",
						ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))
				.add(Restrictions.eq("secGwId", secGwId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.addOrder(Order.asc("id")).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

	} // end of method getAllCommissionedFixtureBySecGwId

	/**
	 * return list of Gateway object associated with secondary gateway
	 *
	 * @param secGwId
	 * @return list of Gateway object associated with secondary gateway
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadAllFixtureBySecondaryGatewayId(Long secGwId) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("secGwId", secGwId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.addOrder(Order.asc("lastConnectivityAt")).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where f.secGwId=?" + " and (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, secGwId);
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Load fixtures available in fixture table
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadAllFixtures() {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR + " from Fixture f order by id ";
		// Query q = getSession().createQuery(hsql.toString());
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Load fixtures available in fixture table
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadAllOrgFixtures() {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR + " from Fixture f" +
		// " where (state != '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = getSession().createQuery(hsql.toString());
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * Load fixtures who custom profile_handler points to default (i.e global)
	 * profile_handler.
	 *
	 * @return
	 */
	public List<Fixture> loadAllFixturesWithDefaultPFID() {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("profileHandler.id", 1L))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR + " from Fixture f" +
		// " where f.profileHandler.id = 1";
		// Query q = getSession().createQuery(hsql.toString());
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;
	}

	/**
	 * get the fixture by snap address
	 *
	 * @param snapAddr
	 * @return the fixture by snap address
	 */
	public Fixture getFixtureBySnapAddr(String snapAddr) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.createCriteria(Fixture.class)
				.add(Restrictions.eq("snapAddress", snapAddr))								
				.createAlias("ballast", "ballast")
				.uniqueResult();
		return fixture;

		// Fixture fixture = null;
		// try {
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where snapAddress = :snap";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setString("snap", snapAddr);
		// fixture = (Fixture) q.uniqueResult();
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return fixture;

	} // end of method getFixtureBySnapAddr

	/**
	 * get the fixture by snap address
	 *
	 * @param snapAddr
	 * @return the fixture by snap address
	 */
	public Fixture getDeletedFixtureBySnapAddr(String snapAddr) {
		Session session = getSession();
		Fixture fixture = (Fixture) session
				.createCriteria(Fixture.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.eq("snapAddress", snapAddr)).uniqueResult();
		return fixture;

		// Fixture fixture = null;
		// try {
		// String hsql = "from Fixture f where f.snapAddress = :snap and " +
		// " (f.state = '"
		// + ServerConstants.FIXTURE_STATE_DELETED_STR + "')";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setString("snap", snapAddr);
		// fixture = (Fixture) q.uniqueResult();
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return fixture;

	} // end of method getDeletedFixtureBySnapAddr

	/**
	 * get the fixture by ip
	 *
	 * @param ip
	 * @return the fixture by ip
	 */
	public Fixture getFixtureByIp(String ip) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.createCriteria(Fixture.class)
				.add(Restrictions.eq("ip_address", ip)).uniqueResult();
		return fixture;

		// Fixture fixture = null;
		// try {
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where ip_address = ?";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, ip);
		// fixture = (Fixture) q.uniqueResult();
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return fixture;

	} // end of method getFixtureByIp
	
	/**
	 * get the fixture count by ballastId
	 *
	 * @param ballastId
	 * @return the fixture count
	 */
	@SuppressWarnings("unchecked")
	public Integer getFixtureCountByBallastId(Long ballastId) {
		
		String hsql = "Select count(fixture) from Fixture fixture where ballast.id = " + ballastId;
		Query q = getSession().createQuery(hsql.toString());
		Object output = q.uniqueResult();
    	Integer count = new Integer(output.toString());
		return count;
		

	} // end of method getFixtureCountByBallastId
	
	@SuppressWarnings("unchecked")
	public Integer getFixtureCountByBulbId(Long bulbId) {
		
		String hsql = "Select count(fixture) from Fixture fixture where bulb.id = " + bulbId;
		Query q = getSession().createQuery(hsql.toString());
		Object output = q.uniqueResult();
    	Integer count = new Integer(output.toString());
		return count;
		

	} // end of method getFixtureCountByBallastId

	/**
	 * get the fixture by macaddr
	 *
	 * @param macaddr
	 * @return the fixture by macaddr
	 */
	public Fixture getFixtureByMacAddr(String macAddr) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.createCriteria(Fixture.class)
				.add(Restrictions.eq("macAddress", macAddr)).uniqueResult();
		return fixture;

		// Fixture fixture = null;
		// try {
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where mac_address = ?";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, macAddr);
		// fixture = (Fixture) q.uniqueResult();
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return fixture;

	} // end of method getFixtureByMacAddr

	@SuppressWarnings("unchecked")
	public List<Fixture> getUnCommissionedFixtureList(long gatewayId) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))				
				.add(Restrictions.eq("gateway.id", gatewayId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		// try {
		// Session s = getSession();
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where f.gateway.id=? " + " and (state = '"
		// + ServerConstants.FIXTURE_STATE_DISCOVER_STR + "')";
		// Query q = s.createQuery(hsql.toString());
		// q.setParameter(0, gatewayId);
		// return q.list();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// return null;
		// }
	
	} // end of method getUnCommissionedFixtureList
		
	@SuppressWarnings("unchecked")
	public List<Fixture> getPlacedFixtureList(long gatewayId) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.eq("state",
						ServerConstants.FIXTURE_STATE_PLACED_STR))
				.add(Restrictions.eq("gateway.id", gatewayId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
	}
			


	@SuppressWarnings("unchecked")
	public List getUnValidatedFixtureList(int floorId) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.or(Restrictions.eq("state",
						ServerConstants.FIXTURE_STATE_COMMISSIONED_STR),
						Restrictions.eq("state",
								ServerConstants.FIXTURE_STATE_PLACED_STR)))
				.add(Restrictions.eq("floor.id", floorId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// Session s = getSession();
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where f.floor.id=? "
		// + " and (state = 'COMMISSIONED' or state = 'PLACED')";
		// Query q = s.createQuery(hsql.toString());
		// q.setParameter(0, (long) floorId);
		// return q.list();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// return null;
		// }

	} // end of method getUnValidatedFixtureList

	public String getCommissionStatus(long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		String sStatus = fixture.getState();
		if (sStatus != null) {
			return sStatus;
		}
		return "";

		// String sStatus = (String)
		// getSession().createQuery("Select state from Fixture where id = :id")
		// .setLong("id", fixtureId).uniqueResult();
		// if (sStatus != null)
		// return sStatus;
		// return "";
	} // end of method isCommissioned
	
	
	public Integer getFixtureHopperStatus(long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		Integer isHopper = fixture.getIsHopper();
		return isHopper;

	}

	@SuppressWarnings("unchecked")
	public List<Fixture> getSortedFixtures() {
		Session session = getSession();
		List<Fixture> fixtureList = session.createCriteria(Fixture.class)
				.addOrder(Order.asc("x")).addOrder(Order.asc("y")).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// List<Fixture> results = null;
		// String hsql = FIXTURE_CONTRACTOR + " from Fixture f order by x, y";
		// Query q = getSession().createQuery(hsql.toString());
		// results = q.list();
		// if (results != null && !results.isEmpty()) {
		// return results;
		// }
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return null;

	} // end of method getSortedFixtures

	/**
	 * get the fixture by name
	 *
	 * @param snapAddr
	 * @return the fixture by name
	 */
	@SuppressWarnings("unchecked")
	public Fixture getFixtureName(String name) {
		Session session = getSession();
		List<Fixture> fixtureList = session.createCriteria(Fixture.class)
				.add(Restrictions.eq("name", name)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList.get(0);
		} else {
			return null;
		}

		// Fixture fixture = null;
		// try {
		// String hsql = FIXTURE_CONTRACTOR +
		// " from Fixture f where fixtureName = ?";
		// Query q = getSession().createQuery(hsql.toString());
		// q.setParameter(0, name);
		// List<Fixture> fixtures = q.list();
		// if (fixtures != null && !fixtures.isEmpty()) {
		// fixture = fixtures.get(0);
		// }
		//
		// } catch (HibernateException hbe) {
		// throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		// }
		// return fixture;

	}

	@SuppressWarnings("unchecked")
	public List<Ballast> getAllBallasts() {
		List<Ballast> ballasts = getSession().createCriteria(Ballast.class)
				.addOrder(Order.asc("ballastName"))
				.list();
		if (!ArgumentUtils.isNullOrEmpty(ballasts)) {
			return ballasts;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Bulb> getAllBulbs() {
		List<Bulb> bulbs = getSession().createCriteria(Bulb.class).list();
		if (!ArgumentUtils.isNullOrEmpty(bulbs)) {
			return bulbs;
		}
		return null;
	}

	public Ballast getBallastById(long id) {

		// TODO Auto-generated method stub
		return (Ballast) getSession().createCriteria(Ballast.class)
				.add(Restrictions.eq("id", id)).uniqueResult();

	} // end of method getBallastById

	public Bulb getBulbById(long id) {

		// TODO Auto-generated method stub
		return (Bulb) getSession().createCriteria(Bulb.class)
				.add(Restrictions.eq("id", id)).uniqueResult();

	} // end of method getBulbById

	public void updateCurrentState(Long id, String state) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setCurrentState(state);

		// Query query = getSession().createQuery("Update Fixture set " +
		// "currentState = :currentState where id = :id");
		// query.setString("currentState", state);
		// query.setLong("id", id);
		// query.executeUpdate();

	} // end of method updateCurrentState

	public void assignGateway(Fixture fx) {
		Session session = getSession();
		session.update(fx);

		// Session session = getSession();
		// Fixture fixture = (Fixture) session.get(Fixture.class, fx.getId());
		// Gateway gateway = (Gateway) session.get(Gateway.class,
		// fx.getGateway().getId());
		// Floor floor = (Floor) session.get(Floor.class,
		// fx.getFloor().getId());
		// fixture.setFixtureName(fx.getFixtureName());
		// fixture.setFloor(floor);
		// fixture.setBuildingId(fx.getBuildingId());
		// fixture.setCampusId(fx.getCampusId());
		// fixture.setLocation(fx.getLocation());
		// fixture.setGateway(gateway);
		// fixture.setSecGwId(gateway.getId());
		// session.saveOrUpdate("fixtureName", fixture);
		// session.saveOrUpdate("floor", fixture);
		// session.saveOrUpdate("buildingId", fixture);
		// session.saveOrUpdate("campusId", fixture);
		// session.saveOrUpdate("location", fixture);
		// session.saveOrUpdate("gateway", fixture);
		// session.saveOrUpdate("secGwId", fixture);

	}
	
	public void enablePushProfileAndGlobalPushProfile(Long fixtureId,boolean pushProfileStatus, boolean globalPushProfileStatus) {
		/*
		Query query = getSession().createQuery(
				"Update Fixture set "
						+ " pushProfile= :pushProfile, "
						+ " pushGlobalProfile = :pushGlobalProfile "
						+ " where id = :id");
		query.setLong("id", fixtureId);
		query.setBoolean("pushProfile", pushProfileStatus);
		query.setBoolean("pushGlobalProfile", globalPushProfileStatus);
		query.executeUpdate();
		*/
		
		Session session = getSession();
		Fixture dbFixture = (Fixture) session.get(Fixture.class, fixtureId);
		dbFixture.setPushProfile(pushProfileStatus);
		dbFixture.setPushGlobalProfile(globalPushProfileStatus);
		
	}

	public void updateStats(Fixture fixture) {
		// Session session = getSession();
		// session.update(fixture);
		// fixture.setLastConnectivityAt(new Date());
	  
		Session session = getSession();
		Fixture dbFixture = (Fixture) session.get(Fixture.class, fixture.getId());		
		dbFixture.setLastOccupancySeen(fixture.getLastOccupancySeen());
		dbFixture.setLightLevel(fixture.getLightLevel());
		dbFixture.setLastConnectivityAt(fixture.getLastConnectivityAt());
		dbFixture.setLastStatsRcvdTime(fixture.getLastStatsRcvdTime());
		dbFixture.setCurrApp(fixture.getCurrApp());
		dbFixture.setCurrentState(fixture.getCurrentState());
		dbFixture.setWattage(fixture.getWattage());
		if(fixture.getSecGwId() != null) {
			dbFixture.setSecGwId(fixture.getSecGwId());
		}
		dbFixture.setDimmerControl(fixture.getDimmerControl());
		dbFixture.setAvgTemperature(fixture.getAvgTemperature());
		dbFixture.setBaselinePower(fixture.getBaselinePower());
		dbFixture.setVersionSynced(fixture.getVersionSynced());
		dbFixture.setBulbLife(fixture.getBulbLife());
		dbFixture.setCurrentAmbientValue(fixture.getCurrentAmbientValue());
		dbFixture.setLightingOccStatus(fixture.getLightingOccStatus());
		
		/*
		Query query = getSession().createQuery(updateStatsQueryStr);
		query.setLong("id", fixture.getId());
		query.setInteger("lastOccupancySeen", fixture.getLastOccupancySeen());
		query.setInteger("lightLevel", fixture.getLightLevel());
		query.setTimestamp("lastConnectivityAt", fixture.getLastConnectivityAt());
		query.setTimestamp("lastStatsRcvdTime", fixture.getLastStatsRcvdTime());
		query.setShort("currApp", fixture.getCurrApp());
		query.setString("currentState", fixture.getCurrentState());
		query.setInteger("wattage", fixture.getWattage());
		query.setLong("secGwId", fixture.getSecGwId());
		query.setInteger("dimmerControl", fixture.getDimmerControl());
		query.setShort("avgTemperature", fixture.getAvgTemperature());
		query.setBigDecimal("baselinePower", fixture.getBaselinePower());
		query.setInteger("versionSynced", fixture.getVersionSynced());
		query.setDouble("bulbLife", fixture.getBulbLife());
		query.executeUpdate();
		*/

	} // end of method updateStats

	public void updateRealtimeStats(Fixture fixture) {
		//Session session = getSession();
		
		Session session = getSession();
		Fixture dbFixture = (Fixture) session.get(Fixture.class, fixture.getId());
		dbFixture.setLastConnectivityAt(fixture.getLastConnectivityAt());
		dbFixture.setLastOccupancySeen(fixture.getLastOccupancySeen());
		dbFixture.setLightLevel(fixture.getLightLevel());
		dbFixture.setGlobalProfileChecksum(fixture.getGlobalProfileChecksum());
		dbFixture.setProfileChecksum(fixture.getProfileChecksum());
		dbFixture.setCurrentState(fixture.getCurrentState());
		dbFixture.setWattage(fixture.getWattage());
		dbFixture.setAvgTemperature(fixture.getAvgTemperature());
		dbFixture.setIsHopper(fixture.getIsHopper());
		dbFixture.setDimmerControl(fixture.getDimmerControl());
		dbFixture.setSecGwId(fixture.getSecGwId());
		dbFixture.setCircRatio(fixture.getCircRatio());
		dbFixture.setHeartbeatStatus(fixture.getHeartbeatStatus());
		
		/*
		Query query = getSession().createQuery(
		 "Update Fixture set " + "lastOccupancySeen = :lastOccupancySeen, " +
		 "lightLevel = :lightLevel, "
		 + "lastConnectivityAt = :lastConnectivityAt, "
		 + "globalProfileChecksum = :globalProfileChecksum, " +
		 "profileChecksum = :profileChecksum, "
		 + "currentState = :currentState, " + "wattage = :wattage, " +
		 "secGwId = :secGwId, "
		 + "avgTemperature = :avgTemperature, " + "isHopper = :isHopper, "
		 + "dimmerControl = :dimmerControl" + " where id = :id");
		 
		query.setLong("id", fixture.getId());
		 query.setInteger("lastOccupancySeen", 	fixture.getLastOccupancySeen());
		 query.setInteger("lightLevel", fixture.getLightLevel());
		 query.setTimestamp("lastConnectivityAt", fixture.getLastConnectivityAt());
		 query.setShort("globalProfileChecksum", fixture.getGlobalProfileChecksum());
		 query.setShort("profileChecksum", fixture.getProfileChecksum());
		 query.setString("currentState", fixture.getCurrentState());
		 query.setInteger("wattage", fixture.getWattage());
		 query.setShort("avgTemperature", fixture.getAvgTemperature());
		 query.setInteger("isHopper", fixture.getIsHopper());
		 query.setInteger("dimmerControl", fixture.getDimmerControl());
		 query.setLong("secGwId", fixture.getSecGwId());
		 query.executeUpdate();
		 */

	} // end of method updateRealtimeStats

	public void updateState(Fixture fixture) {
		// Session session = getSession();
		// if
		// (fixture.getState().equals(ServerConstants.FIXTURE_STATE_DELETED_STR))
		// {
		// fixture.setGateway(null);
		// }
		// logger.info("Fixture: " + fixture.getId() + ", updating to state: " +
		// fixture.getState());
		//
		// session.update(fixture);
		try {
			Session session = getSession();
			Fixture fixt = (Fixture) session
					.get(Fixture.class, fixture.getId());
			fixt.setState(fixture.getState());
			if (fixt.getState().equals(
					ServerConstants.FIXTURE_STATE_DELETED_STR)) {
				fixt.setGateway(null);
			}
			session.update(fixt);

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // end of method updateState
	
	public void updateLightingOccupancyStatus(Fixture fixture) {
	
		try {
			Session session = getSession();
			Fixture fixt = (Fixture) session.get(Fixture.class, fixture.getId());
			fixt.setLightingOccStatus(fixture.getLightingOccStatus());			
			session.update(fixt);
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // end of method updateLightingOccupancyStatus

	public void updateStateAndLastConnectivityTime(Fixture fixture) {
		try {
			Session session = getSession();
			Fixture fixt = (Fixture) session
					.get(Fixture.class, fixture.getId());
			fixt.setState(fixture.getState());
			fixt.setLastConnectivityAt(fixture.getLastConnectivityAt());
			if (fixt.getState().equals(
					ServerConstants.FIXTURE_STATE_DELETED_STR)) {
				fixt.setGateway(null);
			}
			session.update(fixt);

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // end of method updateState
	
	public void startNetworkDiscovery() {
		logger.debug("Start Network Discovery");
	}

	public void cancelNetworkDiscovery() {
		logger.debug("Cancel Network Discovery");
	}

	public Integer getTotalFixtureCountInSensor(Long floorId) {
		Integer totalFixtureCount = (Integer) getSession()
				.createCriteria(Fixture.class)
				.add(Restrictions.eq("floor.id", floorId))
				.setProjection(Projections.sum("noOfFixtures")).uniqueResult();
		return totalFixtureCount;
	}

	@SuppressWarnings("unchecked")
	public Fixture getFixtureById(Long id) {
		Session session = getSession();
		return (Fixture) session.createCriteria(Fixture.class)
				.add(Restrictions.eq("id", id))
				.createAlias("ballast", "ballast").createAlias("bulb", "bulb")
				.uniqueResult();
		/*
		 * Fixture fixture = (Fixture) session.get(Fixture.class, id);
		 * fixture.getBallast().getBallastName(); return fixture;
		 */

		// List<Fixture> fixtures = getSession().createQuery(FIXTURE_CONTRACTOR
		// + " from Fixture f where f.id = :id")
		// .setLong("id", id).list();
		// if (!ArgumentUtils.isNullOrEmpty(fixtures)) {
		// return fixtures.get(0);
		// }
		// return null;
	}

	// @SuppressWarnings("unchecked")
	// public List<Object> getFixtureDetailsById(Long id) {
	// List<Object> fixturedetails = new ArrayList<Object>();
	// List<Fixture> fixtures = getSession().createQuery(FIXTURE_CONTRACTOR +
	// " from Fixture f where f.id = :id")
	// .setLong("id", id).list();
	// if (!ArgumentUtils.isNullOrEmpty(fixtures)) {
	// Fixture oFixture = fixtures.get(0);
	// if (oFixture != null) {
	// fixturedetails.add(oFixture);
	// fixturedetails.add(oFixture.getLatestEnergyConsumption());
	// }
	// }
	// return fixturedetails;
	// }

	@SuppressWarnings("unchecked")
	public Integer getCommType(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		Integer commType = fixture.getCommType();
		if (commType != null) {
			return commType;
		}
		return null;
		// List<Object> commTypes =
		// getSession().createQuery("Select commType from Fixture where id = :id")
		// .setLong("id", fixtureId).list();
		// if (!ArgumentUtils.isNullOrEmpty(commTypes)) {
		// return Integer.valueOf(commTypes.get(0).toString());
		// }
		// return null;
	}

	@SuppressWarnings("unchecked")
	public String getIpAddress(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		String ipAddress = fixture.getIpAddress();
		if (ipAddress != null) {
			return ipAddress;
		}
		return null;
		// List<Object> ipAddresses =
		// getSession().createQuery("Select ipAddress from Fixture where id = :id")
		// .setLong("id", fixtureId).list();
		// if (!ArgumentUtils.isNullOrEmpty(ipAddresses)) {
		// return ipAddresses.get(0).toString();
		// }
		// return null;
	}

	public void updateVersion(String version, long id, long gwId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setLastConnectivityAt(new java.util.Date());
		fixture.setVersion(version);
		fixture.setCurrApp((short) 2);
		fixture.setSecGwId(gwId);

		// Query query = getSession().createQuery(
		// "Update Fixture set " + "lastConnectivityAt = :lastConnectivityAt, "
		// + "secGwId = :secGwId, currApp = :currApp, " +
		// "version = :version where id = :id");
		// query.setLong("id", id);
		// query.setTimestamp("lastConnectivityAt", new java.util.Date());
		// query.setString("version", version);
		// query.setShort("currApp", (short) 2);
		// query.setLong("secGwId", gwId);
		// query.executeUpdate();

	} // end of method updateVersion

	public void updateFirmwareVersion(String version, long id, long gwId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setLastConnectivityAt(new java.util.Date());
		fixture.setVersion(version);
		fixture.setCurrApp((short) 1);
		fixture.setSecGwId(gwId);
		// Query query = getSession().createQuery(
		// "Update Fixture set " + "lastConnectivityAt = :lastConnectivityAt, "
		// + "secGwId = :secGwId, currApp = :currApp, " +
		// "firmwareVersion = :version where id = :id");
		// query.setLong("id", id);
		// query.setTimestamp("lastConnectivityAt", new java.util.Date());
		// query.setString("version", version);
		// query.setShort("currApp", (short) 1);
		// query.setLong("secGwId", gwId);
		//
		// query.executeUpdate();

	} // end of method updateFirmwareVersion

	// this is called as part of node boot info which is called from app2
	// so set app2 as current app
	public void updateBootInfo(Fixture fx, String upgrStatus) {
						
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fx.getId());
		fixture.setVersion(fx.getVersion());		
		fixture.setBootLoaderVersion(fx.getBootLoaderVersion());
		fixture.setLastConnectivityAt(fx.getLastConnectivityAt());
		fixture.setSecGwId(fx.getSecGwId());
		fixture.setCurrApp(fx.getCurrApp());
		fixture.setFirmwareVersion(fx.getFirmwareVersion());
		fixture.setCuVersion(fx.getCuVersion());
		fixture.setResetReason(fx.getResetReason());
		if(!upgrStatus.equals(ServerConstants.IMG_UP_STATUS_NOT_PENDING)) {
	          fixture.setUpgradeStatus(fx.getUpgradeStatus());
	        }
		fixture.setIsHopper(fx.getIsHopper());
		fixture.setLastBootTime(new Date());
		if (fx.getDaliDriverCount() != null) {
			fixture.setDaliDriverCount(fx.getDaliDriverCount());
		}
		if (fx.getBleFirmwareVersion() != null && !fx.getBleFirmwareVersion().equals("")) {
			fixture.setBleFirmwareVersion(fx.getBleFirmwareVersion());
		}
		
		// session.saveOrUpdate("version", fixture);
		// session.saveOrUpdate("bootLoaderVersion", fixture);
		// session.saveOrUpdate("lastConnectivityAt", fixture);
		// session.saveOrUpdate("secGwId", fixture);
		// session.saveOrUpdate("currApp", fixture);
		// session.saveOrUpdate("firmwareVersion", fixture);
		// session.saveOrUpdate("lastBootTime", fixture);
		//
		// TODO: Check with Sreedhar if this required if we do in update way -
		// lalit
		// if (ServerUtil.compareVersion(fixture.getVersion(), "2.0") >= 0) {
		// fixture.setCuVersion(fx.getCuVersion());
		// session.saveOrUpdate("cuVersion", fixture);
		// }
		// session.flush();

	} // end of method updateBootInfo

	public void updateFixturePlc(long id, String ipAddr, String version) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setVersion(version);
		fixture.setIpAddress(ipAddr);

		// Query query = getSession().createQuery(
		// "Update Fixture set " +
		// "version = :version, ipAddress = :ipAddress where id = :id");
		// query.setLong("id", id);
		// query.setString("version", version);
		// query.setString("ipAddress", ipAddr);
		// query.executeUpdate();

	} // end of method updateFixturePlc

	public void adjustLastOccupancyTime(long floorId, int sec) {

		Query query = getSession()
				.createQuery(
						"Update Fixture set "
								+ "lastOccupancySeen = lastOccupancySeen + :lastOccupancySeen"
								+ " where floor_id = :id");
		query.setLong("id", floorId);
		query.setInteger("lastOccupancySeen", sec);
		query.executeUpdate();

	} // end of method adjustLastOccupancyTime

	@SuppressWarnings("unchecked")
	public List<BallastVoltPower> getAllBallastVoltPowers() {
		List<BallastVoltPower> ballastVoltPowers = getSession().createCriteria(
				BallastVoltPower.class).list();
		if (!ArgumentUtils.isNullOrEmpty(ballastVoltPowers)) {
			return ballastVoltPowers;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<BallastVoltPower> getAllBallastVoltPowersFromId(
			long voltPowerMapId) {
		List<BallastVoltPower> ballastVoltPowers = this.ballastVoltPowersMap
				.get(voltPowerMapId);
		if (ballastVoltPowers == null) {
			ballastVoltPowers = getSession()
					.createCriteria(BallastVoltPower.class)
					.add(Restrictions.eq("voltPowerMapId", voltPowerMapId))
					.list();
			if (ArgumentUtils.isNullOrEmpty(ballastVoltPowers)) {
				// Fetch the default.
				ballastVoltPowers = getSession()
						.createCriteria(BallastVoltPower.class)
						.add(Restrictions.eq("voltPowerMapId", 1L)).list();
			}
			ballastVoltPowersMap.put(voltPowerMapId, ballastVoltPowers);
		}
		return ballastVoltPowers;
	}

	public void updateGroupId(long id, long groupId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setGroupId(groupId);

		// Query query = getSession().createQuery("Update Fixture set " +
		// "group_id = :group_id " + "where id = :id");
		// query.setLong("id", id);
		// query.setLong("group_id", groupId);
		// query.executeUpdate();

	} // end of method updateGroupId

	public void updateGroupId(long id, long groupId, String currentProfile,
			String originalProfileFrom) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setCurrentProfile(currentProfile);
		fixture.setOriginalProfileFrom(originalProfileFrom);
		fixture.setGroupId(groupId);

		// SQLQuery query = getSession()
		// .createSQLQuery(
		// "update fixture set group_id = :group_id, current_profile = :currentProfile, original_profile_from = :originalProfile where id = :id");
		// query.setLong("group_id", groupId);
		// query.setString("currentProfile", currentProfile);
		// query.setString("originalProfile", originalProfileFrom);
		// query.setLong("id", id);
		// query.executeUpdate();
	} // end of method updateGroupId

	public void setImageUpgradeStatus(long fixtureId, String status) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setUpgradeStatus(status);

		// SQLQuery query =
		// getSession().createSQLQuery("update fixture set upgrade_status" +
		// " = :status where id = :id");
		// query.setLong("id", fixtureId);
		// query.setString("status", status);
		// query.executeUpdate();

	} // end of method setImageUpgradeStatus

	public Integer getCommissionStatus(Long fixtureId) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		if (fixture.getCommissionStatus() != null) {
			return fixture.getCommissionStatus();
		}
		return 0;

		// List<Integer> commTypes =
		// getSession().createQuery("Select commissionStatus from Fixture where id = :id")
		// .setLong("id", fixtureId).list();
		// if (!ArgumentUtils.isNullOrEmpty(commTypes)) {
		// if (commTypes.get(0) != null)
		// return Integer.valueOf(commTypes.get(0).toString());
		// }
		// return 0;
	}

	public void updateCommissionStatus(Long fixtureId, int status) {
		int commission_status = getCommissionStatus(fixtureId).intValue();
		logger.info("Fixture: " + fixtureId + ", existing status: "
				+ commission_status + " new status: " + status);
		commission_status = commission_status
				| ServerConstants.COMMISSION_STATUS_COMMUNICATION | status;
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setCommissionStatus(commission_status);
		session.update(fixture);
		// try {
		// int commission_status = getCommissionStatus(fixtureId).intValue();
		// logger.info("Fixture: " + fixtureId + ", existing status: " +
		// commission_status + " new status: " + status);
		// switch (commission_status) {
		// case ServerConstants.COMMISSION_STATUS_ALL:
		// commission_status = ServerConstants.COMMISSION_STATUS_ALL;
		// break;
		// default:
		// commission_status = commission_status |
		// ServerConstants.COMMISSION_STATUS_COMMUNICATION | status;
		// }
		// SQLQuery query = getSession().createSQLQuery(
		// "update fixture set commission_status = :commission_status where id = :id");
		// query.setInteger("commission_status", commission_status);
		// query.setLong("id", fixtureId);
		// query.executeUpdate();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void updateCommissionStatus(int[] fixtureIds, int status) {
		try {
			String queryStr = "update Fixture set commission_status = :commission_status "
					+ " where id in (";
			int noOfFixtures = fixtureIds.length;
			for (int i = 0; i < noOfFixtures; i++) {
				if (i > 0) {
					queryStr += ", ";
				}
				queryStr += "" + fixtureIds[i];
			}
			queryStr += ")";

			SQLQuery query = getSession().createSQLQuery(queryStr);
			query.setInteger("commission_status", status);
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * replace the old fixture with the new fixture attributes used for RMA
	 */
	public void replaceFixture(long fixtureId, String fixtureName,
			String macAddr, String sensorId, String snapAddr, String modelNo,
			Integer commisionStatus, String state, String firmwareVersion,
			String version) {
        try {
          /*
            String queryStr = "update fixture set fixture_name = :fixture_name, "
                    + "mac_address = :mac_address, sensor_id = :sensor_id, "
                    + "snap_address = :snap_address, commission_status = :commission_status, state = :state where id = :id";
            SQLQuery query = getSession().createSQLQuery(queryStr);
            query.setString("fixture_name", fixtureName);
            query.setString("mac_address", macAddr);
            query.setString("sensor_id", sensorId);
            query.setString("snap_address", snapAddr);
            query.setInteger("commission_status", commisionStatus);
            query.setString("state", state);
            query.setLong("id", fixtureId);
            query.executeUpdate();
            */
        	//System.out.println("replace fixture --- " + fixtureId + ", " + fixtureName + ", " + macAddr + 
        	//		", " + sensorId + ", " + snapAddr + ", " + state);
            Session session = getSession();
            Fixture dbFixture = (Fixture) session.get(Fixture.class, fixtureId);
            dbFixture.setName(fixtureName);
            dbFixture.setMacAddress(macAddr);
            dbFixture.setSensorId(sensorId);
            dbFixture.setSnapAddress(snapAddr);
            dbFixture.setCommissionStatus(commisionStatus);
            dbFixture.setState(state);
            dbFixture.setModelNo(modelNo);
            dbFixture.setFirmwareVersion(firmwareVersion);
            dbFixture.setVersion(version);
            session.flush();
                        
        } catch (Exception e) {
            logger.debug("Error in replacing the fixture: " + e.getMessage());
        }

	} // end of method replaceFixture

	public void updateFixtureVersionSyncedState(Fixture fixture) {
		Session session = getSession();
		Fixture dbFixture = (Fixture) session.get(Fixture.class, fixture.getId());
		dbFixture.setVersionSynced(fixture.getVersionSynced());
		
	} // end of method updateFixtureVersionSyncedState

	public List<OutageBasePower> getAllOutageBasePowerList() {

		List<OutageBasePower> outageBasePowerList = getSession()
				.createCriteria(OutageBasePower.class).list();
		if (!ArgumentUtils.isNullOrEmpty(outageBasePowerList)) {
			return outageBasePowerList;
		}
		return null;

	} // end of method getAllOutageBasePowerList

	public List<OutageBasePower> getFixtureOutageBasePowerList(long fixtureId) {

		List<OutageBasePower> outageBasePowerList = null;
		outageBasePowerList = getSession()
				.createCriteria(OutageBasePower.class)
				.add(Restrictions.eq("fixtureId", fixtureId)).list();
		return outageBasePowerList;

	} // end of method getFixtureOutageBasePowerList

	public OutageBasePower getOutageBasePower(long fixtureId, short voltLevel) {

		List<OutageBasePower> outageBasePowerList = null;
		outageBasePowerList = getSession()
				.createCriteria(OutageBasePower.class)
				.add(Restrictions.eq("fixtureId", fixtureId))
				.add(Restrictions.eq("voltLevel", voltLevel)).list();
		if (outageBasePowerList == null || outageBasePowerList.isEmpty()) {
			return null;
		}
		return outageBasePowerList.get(0);

	} // end of method getOutageBasePower

	@SuppressWarnings("unchecked")
	public List<Fixture> getAllCommissionedFixtureList() {

		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.eq("state",
						ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}

		// try {
		// Session s = getSession();
		// String hsql = FIXTURE_CONTRACTOR + " from Fixture f where " +
		// " state = '"
		// + ServerConstants.FIXTURE_STATE_COMMISSIONED_STR + "'";
		// Query q = s.createQuery(hsql.toString());
		// return q.list();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// return null;
		// }

	} // end of method getAllCommissionedFixtureList

	public Bulb loadBulb(Long id) {
		Session session = getSession();
		return (Bulb) session.load(Bulb.class, id);
	}

	public Ballast loadBallast(Long id) {
		Session session = getSession();
		return (Ballast) session.load(Ballast.class, id);
	}

	/**
	 * Fetch fixture count by the property association
	 *
	 * @param property
	 * @param pid
	 * @return Long count of fixtures.
	 */
	public Long getFixtureCount(String property, Long pid) {
		Long totalFixtureCount = 0L;
		if (property.equals("company")) {
			totalFixtureCount = (Long) getSession()
					.createCriteria(Fixture.class)
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_DELETED_STR))
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_PLACED_STR))
					.setProjection(Projections.sum("noOfFixtures"))
					.uniqueResult();
		} else {
			totalFixtureCount = (Long) getSession()
					.createCriteria(Fixture.class)
					.add(Restrictions.eq(property, pid))
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_DELETED_STR))
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_PLACED_STR))
					.setProjection(Projections.sum("noOfFixtures"))
					.uniqueResult();
		}
		if (totalFixtureCount == null)
			totalFixtureCount = 0L;
		return totalFixtureCount;
	}
	
	
	@SuppressWarnings("unchecked")
	public FixtureList loadFixtureList(String property, Long pid, String order,
			String orderWay, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit) {
		FixtureList fixtureList = new FixtureList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
		if (property != null && pid != null) {
			oRowCount = getSession()
					.createCriteria(Fixture.class, "fx")
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_DELETED_STR))
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_PLACED_STR))
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.createAlias("area", "ar", CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setFetchMode("ar", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());

			oCriteria = getSession()
					.createCriteria(Fixture.class, "fx")
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_DELETED_STR))
					.add(Restrictions.ne("state",
							ServerConstants.FIXTURE_STATE_PLACED_STR))
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.createAlias("area", "ar", CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setFetchMode("ar", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

			switch (orgType) {
			case CAMPUS: {
				oRowCount.add(Restrictions.eq("fx.campusId", pid));
				oCriteria.add(Restrictions.eq("fx.campusId", pid));
				break;
			}
			case BUILDING: {
				oRowCount.add(Restrictions.eq("fx.buildingId", pid));
				oCriteria.add(Restrictions.eq("fx.buildingId", pid));
				break;
			}
			case FLOOR: {
				oRowCount.add(Restrictions.eq("fx.floor.id", pid));
				oCriteria.add(Restrictions.eq("fx.floor.id", pid));
				break;
			}
			case AREA: {
				oRowCount.add(Restrictions.eq("fx.area.id", pid));
				oCriteria.add(Restrictions.eq("fx.area.id", pid));
				break;
			}
			case GROUP: {
				oRowCount.add(Restrictions.eq("fx.groupId", pid));
				oCriteria.add(Restrictions.eq("fx.groupId", pid));
				break;
			}
			default: {
				// company level all fixtures
			}

			}
			if (bSearch) {
				if (searchField.equals("id")) {
					oRowCount.add(Restrictions.eq("id",
							Long.parseLong(searchString)));
					oCriteria.add(Restrictions.eq("id",
							Long.parseLong(searchString)));
				} else if (searchField.equals("name")) {
					oRowCount.add(Restrictions.like("fx.name", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.name", "%"
							+ searchString + "%"));
				} else if (searchField.equals("snapaddress")) {
					oRowCount.add(Restrictions.like("fx.snapAddress", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.snapAddress", "%"
							+ searchString + "%"));
				} else if (searchField.equals("state")) {
					oRowCount.add(Restrictions.like("fx.state", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.state", "%"
							+ searchString + "%"));
				} else if (searchField.equals("currentprofile")) {
					oRowCount.add(Restrictions.like("fx.currentProfile", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.currentProfile", "%"
							+ searchString + "%"));
				} else if (searchField.equals("upgradestatus")) {
					oRowCount.add(Restrictions.like("fx.upgradeStatus", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.upgradeStatus", "%"
							+ searchString + "%"));
				} else if (searchField.equals("currapp")) {
					oRowCount.add(Restrictions.like("fx.currApp", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.currApp", "%"
							+ searchString + "%"));
				} else if (searchField.equals("version")) {
					oRowCount.add(Restrictions.like("fx.version", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.version", "%"
							+ searchString + "%"));
				} else if (searchField.equals("gateway")) {
					oRowCount.add(Restrictions.like("gw.name", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("gw.name", "%"
							+ searchString + "%"));
				}
			}
			if (limit > 0) {
				oCriteria.setMaxResults(limit).setFirstResult(offset);
			}
			if (order != null && !"".equals(order)) {
				if (order.equals("name")) {
					order = "fx.name";
				} else if (order.equals("snapaddress")) {
					order = "fx.snapAddress";
				} else if (order.equals("currapp")) {
					order = "fx.currApp";
				} else if (order.equals("version")) {
					order = "fx.version";
				} else if (order.equals("gateway")) {
					order = "gw.name";
				} else if (order.equals("currentprofile")) {
					order = "fx.currentProfile";
				} else if (order.equals("upgradestatus")) {
					order = "fx.upgradeStatus";
				} else if (order.equals("state")) {
					order = "fx.state";
				} else if (order.equals("status")) {
					order = "fx.lastConnectivityAt";
				} else {
					order = "fx.id";
				}
				if ("desc".equals(orderWay)) {
					oCriteria.addOrder(Order.desc(order));
				} else {
					oCriteria.addOrder(Order.asc(order));
				}
			} else {
				oCriteria.addOrder(Order.desc("id"));
			}
			List<Object> output = (List<Object>) oRowCount.list();
			Long count = (Long) output.get(0);
			if (count.compareTo(new Long("0")) > 0) {
				fixtureList.setTotal(count);
				fixtureList.setFixture(oCriteria.list());
			}
		}
		return fixtureList;
	}

	@SuppressWarnings("unchecked")
	public FixtureList loadFixtureListWithSpecificAttrs(String property, Long pid, String order,
			String orderWay, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit) {
		FixtureList fixtureList = new FixtureList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		String statusOrder1 = null;
		String statusOrder2 = null;
		String statusOrder3 = null;

		FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
		if (property != null && pid != null) {
			oRowCount = getSession()
					.createCriteria(Fixture.class, "fx")
					.add(Restrictions.ne("fx.state",
							ServerConstants.FIXTURE_STATE_DELETED_STR))
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());

			oCriteria = getSession()
					.createCriteria(Fixture.class, "fx")
					.add(Restrictions.ne("fx.state",
							ServerConstants.FIXTURE_STATE_DELETED_STR))
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setProjection(Projections.projectionList().add(Projections.property("fx.id"), "id")
					.add(Projections.property("fx.snapAddress"), "snapAddress")
					.add(Projections.property("fx.state"), "state")
					.add(Projections.property("fx.lastConnectivityAt"), "lastConnectivityAt")
					.add(Projections.property("fx.name"), "fixtureName")
					.add(Projections.property("fx.isHopper"), "isHopper")
					.add(Projections.property("fx.currApp"), "currApp")
                    .add(Projections.property("fx.version"), "version")
                    .add(Projections.property("fx.firmwareVersion"), "firmwareVersion")
					//.add(Projections.sqlProjection("(case when {alias}.curr_app = '1' then {alias}.firmware_version else {alias}.version end) as version", new String[] {"version"}, new Type[] {Hibernate.STRING}), "version")
					.add(Projections.property("fx.currentProfile"), "currentProfile")
					.add(Projections.property("fx.groupId"), "groupId")
					.add(Projections.property("fx.upgradeStatus"), "upgradeStatus")					
					.add(Projections.property("gw.name"), "gatewayNameForFilter")
					.add(Projections.property("gw.id"), "gatewayIdForFilter")
					.add(Projections.sqlProjection("hex_to_int(split_part({alias}.snap_address, ':', 1)) as mac1ForFilter", new String[] {"mac1ForFilter"}, new Type[] {Hibernate.INTEGER}), "mac1ForFilter")
					.add(Projections.sqlProjection("hex_to_int(split_part({alias}.snap_address, ':', 2)) as mac2ForFilter", new String[] {"mac2ForFilter"}, new Type[] {Hibernate.INTEGER}), "mac2ForFilter")
					.add(Projections.sqlProjection("hex_to_int(split_part({alias}.snap_address, ':', 3)) as mac3ForFilter", new String[] {"mac3ForFilter"}, new Type[] {Hibernate.INTEGER}), "mac3ForFilter")
					)
					.add(Restrictions.ne("fx.state", ServerConstants.FIXTURE_STATE_DELETED_STR))
					//.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setResultTransformer(Transformers.aliasToBean(Fixture.class));
					

			switch (orgType) {
			case CAMPUS: {
				oRowCount.add(Restrictions.eq("fx.campusId", pid));
				oCriteria.add(Restrictions.eq("fx.campusId", pid));
				break;
			}
			case BUILDING: {
				oRowCount.add(Restrictions.eq("fx.buildingId", pid));
				oCriteria.add(Restrictions.eq("fx.buildingId", pid));
				break;
			}
			case FLOOR: {
				oRowCount.add(Restrictions.eq("fx.floor.id", pid));
				oCriteria.add(Restrictions.eq("fx.floor.id", pid));
				break;
			}
			case AREA: {
				oRowCount.add(Restrictions.eq("fx.area.id", pid));
				oCriteria.add(Restrictions.eq("fx.area.id", pid));
				break;
			}
			case GROUP: {
				oRowCount.add(Restrictions.eq("fx.groupId", pid));
				oCriteria.add(Restrictions.eq("fx.groupId", pid));
				break;
			}
			default: {
				// company level all fixtures
			}

			}
			if (bSearch) {
				if (searchField.equals("id")) {
					oRowCount.add(Restrictions.eq("fx.id",
							Long.parseLong(searchString)));
					oCriteria.add(Restrictions.eq("fx.id",
							Long.parseLong(searchString)));
				} else if (searchField.equals("name")) {
					oRowCount.add(Restrictions.like("fx.name", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.name", "%"
							+ searchString + "%"));
				} else if (searchField.equals("snapaddress")) {
					oRowCount.add(Restrictions.like("fx.snapAddress", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.snapAddress", "%"
							+ searchString + "%"));
				} else if (searchField.equals("state")) {
					oRowCount.add(Restrictions.like("fx.state", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.state", "%"
							+ searchString + "%"));
				} else if (searchField.equals("currentprofile")) {
					oRowCount.add(Restrictions.like("fx.currentProfile", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.currentProfile", "%"
							+ searchString + "%"));
				} else if (searchField.equals("upgradestatus")) {
					oRowCount.add(Restrictions.like("fx.upgradeStatus", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.upgradeStatus", "%"
							+ searchString + "%"));
				} else if (searchField.equals("currapp")) {
					oRowCount.add(Restrictions.like("fx.currApp", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.currApp", "%"
							+ searchString + "%"));
				} else if (searchField.equals("version")) {
					oRowCount.add(Restrictions.like("fx.version", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("fx.version", "%"
							+ searchString + "%"));
//					oRowCount.add(Restrictions.sqlRestriction(" (case when {alias}.curr_app = '1' then {alias}.firmware_version else {alias}.version end) like '%' || ? || '%'", searchString, Hibernate.STRING));
//					oCriteria.add(Restrictions.sqlRestriction(" (case when {alias}.curr_app = '1' then {alias}.firmware_version else {alias}.version end) like '%' || ? || '%'", searchString, Hibernate.STRING));
				} else if (searchField.equals("gateway")) {
					oRowCount.add(Restrictions.like("gw.name", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("gw.name", "%"
							+ searchString + "%"));
				}
			}
			if (limit > 0) {
				oCriteria.setMaxResults(limit).setFirstResult(offset);
			}
			if (order != null && !"".equals(order)) {
				if (order.equals("name")) {
					order = "fx.name";
				} else if(order.equals("snapaddress")) {
				} else if (order.equals("currapp")) {
					order = "fx.currApp";
				} else if (order.equals("version")) {
					order = "version";
				} else if (order.equals("gateway")) {
					order = "gw.name";
				} else if (order.equals("currentprofile")) {
					order = "fx.currentProfile";
				} else if (order.equals("upgradestatus")) {
					order = "fx.upgradeStatus";
				} else if (order.equals("state")) {
					order = "fx.state";
				} else if (order.equals("status")) {
					statusOrder1 = "fx.state";
					statusOrder2 = "fx.isHopper";
					statusOrder3 = "fx.lastConnectivityAt";
				} else {
					order = "fx.id";
				}
				if ("desc".equals(orderWay)) {
					if(order.equals("snapaddress")) {
						oCriteria.addOrder(Order.desc("mac1ForFilter"))
						.addOrder(Order.desc("mac2ForFilter"))
						.addOrder(Order.desc("mac3ForFilter"));
					} else if(order.equals("status")){
						oCriteria.addOrder(Order.desc(statusOrder1));
						oCriteria.addOrder(Order.desc(statusOrder2));
						oCriteria.addOrder(Order.desc(statusOrder3));
					}
					else {
						oCriteria.addOrder(Order.desc(order));
					}
					
				} else {
					if(order.equals("snapaddress")) {
						oCriteria.addOrder(Order.asc("mac1ForFilter"))
						.addOrder(Order.asc("mac2ForFilter"))
						.addOrder(Order.asc("mac3ForFilter"));
					} else if (order.equals("status")){
						oCriteria.addOrder(Order.asc(statusOrder1));
						oCriteria.addOrder(Order.asc(statusOrder2));
						oCriteria.addOrder(Order.asc(statusOrder3));
					}
					else {
						oCriteria.addOrder(Order.asc(order));
					}
				}
			} else {
				oCriteria.addOrder(Order.desc("id"));
			}
			List<Object> output = (List<Object>) oRowCount.list();
			Long count = (Long) output.get(0);
			if (count.compareTo(new Long("0")) > 0) {
				fixtureList.setTotal(count);
				fixtureList.setFixture(oCriteria.list());
			}
		}
		return fixtureList;
	}

	public List<Fixture> getFixtureOutFixtureList(String property, Long pid) {
		Criteria oCriteria = getSession()
				.createCriteria(Fixture.class, "fx")
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.createAlias("eventsAndFaults", "ef",
						CriteriaSpecification.LEFT_JOIN)
				.setFetchMode("ef", FetchMode.JOIN)
				.add(Restrictions.eq("ef.eventType",
						EventsAndFault.FIXTURE_OUTAGE_EVENT_STR));

		FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
		if (property != null && pid != null) {
			switch (orgType) {
			case CAMPUS: {
				oCriteria.add(Restrictions.eq("fx.campusId", pid));
				break;
			}
			case BUILDING: {
				oCriteria.add(Restrictions.eq("fx.buildingId", pid));
				break;
			}
			case FLOOR: {
				oCriteria.add(Restrictions.eq("fx.floor.id", pid));
				break;
			}
			case AREA: {
				oCriteria.add(Restrictions.eq("fx.area.id", pid));
				break;
			}
			case GROUP: {
				oCriteria.add(Restrictions.eq("fx.groupId", pid));
				break;
			}
			default: {
				// company level all fixtures
			}
			}
		}
		return oCriteria.list();
	}

	/**
	 * clears ballastVoltPowersMap map. To be invoked by admin.
	 */
	public void clearBallastVoltPowersMap() {
		ballastVoltPowersMap.clear();
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Object[]> getRecentFixtureDetails() {
		Map<Integer, Object[]> map = new TreeMap<Integer, Object[]>();
		try {

			Calendar now = Calendar.getInstance();
			int currentMinute = now.get(Calendar.MINUTE);
			currentMinute += now.get(Calendar.HOUR_OF_DAY) * 60;
			int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;
			if (dayOfWeek == 0) {
				dayOfWeek = 7;
			}
			String hsql = "SELECT f.id,"
					+ " f.dimmer_control, f.current_state, "
					+ "ph.dr_reactivity, "
					+ "pr.min_level, "
					+ "pr.on_level "
					+ " from fixture f, groups g, profile_handler ph, profile pr"
					+ " where f.state =  'COMMISSIONED' "
					+ " and f.group_id = g.id"
					+ " and ph.id = g.profile_handler_id "
					+ " and pr.id in (ph.evening_profile_id,ph.morning_profile_id, ph.day_profile_id, ph.night_profile_id, ph.morning_profile_weekend, ph.day_profile_weekend, ph.evening_profile_weekend, ph.night_profile_weekend) "
					+ " and pr.id = getProfileByFixtureGroupId( f.id, "
					+ dayOfWeek + ", 0, " + currentMinute + ")";

			Query q = getSession().createSQLQuery(hsql.toString());
			List<Object[]> results = q.list();
			if (results != null && !results.isEmpty()) {
				for (Iterator<Object[]> iterator = results.iterator(); iterator
						.hasNext();) {
					Object[] object = (Object[]) iterator.next();
					if (object[0] != null) {
						try {
							map.put(((BigInteger) object[0]).intValue(), object);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				return map;
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return null;
	}

	public List getAllSensorData(long captureDate) {

	  try {
	    Session s = getSession();
	    String hsql = "SELECT f.id, f.name, ec.powerUsed, "
		+ "ec.motionBits, ec.avgTemperature, ec.lightAvgLevel from EnergyConsumption as ec, Fixture as f"
		+ " where ec.zeroBucket = 0 and f.id = ec.fixture.id and ec.captureAt = '"
		+ new Timestamp(captureDate) + "'";
	    Query q = s.createQuery(hsql);
	    return q.list();
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  return null;

	} // end of method getAllSensorData

	public List getSensorData(long fixtureId, long captureDate) {

		try {
			Session s = getSession();
			String hsql = "SELECT f.id, f.name, ec.powerUsed, "
					+ "ec.motionBits, ec.avgTemperature,ec.lightAvgLevel from EnergyConsumption as ec, Fixture as f"
					+ " where ec.zeroBucket = 0 and f.id = ec.fixture.id and f.id = :fixtureId and ec.captureAt = '"
					+ new Timestamp(captureDate) + "'";
			Query query = s.createQuery(hsql);
			query.setLong("fixtureId", fixtureId);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	} // end of method getAllSensorData
	
	@SuppressWarnings("unchecked")
	public List<Fixture> getFixtureOutageList() {
		Criteria oCriteria = getSession()
				.createCriteria(Fixture.class, "fx")
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.createAlias("eventsAndFaults", "ef",
						CriteriaSpecification.INNER_JOIN)
				.setFetchMode("ef", FetchMode.JOIN)
				.add(Restrictions.eq("ef.eventType",
						EventsAndFault.FIXTURE_OUTAGE_EVENT_STR))
				.add((Restrictions.isNull("ef.resolvedOn")));
		return oCriteria.list();
	}

	public List<FixtureOutageVO> getFixtureOutageList(String property, Long pid) {

                  boolean pidExists = true;
                  String hql = "select new com.ems.vo.model.FixtureOutageVO(f.id, f.name, f.location, f.xaxis, f.yaxis, ef.eventTime, ef.description) "
							+ " from Fixture f, EventsAndFault ef where f.id = ef.device.id and f.state = 'COMMISSIONED' and ef.eventType like 'Fixture Out'  and ef.resolvedOn is null ";
                  
                  SystemConfiguration sc = sysConfigMgr.loadConfigByName("show.cu.failue.in.outage.report");
                  if(sc != null && "true".equals(sc.getValue())) {
                	hql =   "select new com.ems.vo.model.FixtureOutageVO(f.id, f.name, f.location, f.xaxis, f.yaxis, ef.eventTime, ef.description) "
							+ " from Fixture f, EventsAndFault ef where f.id = ef.device.id and f.state = 'COMMISSIONED' and (ef.eventType like 'Fixture Out' or ef.eventType like 'Fixture CU Failure')  and ef.resolvedOn is null ";
                  }

	              FacilityType orgType = FacilityType.valueOf(property.toUpperCase());

	              if (property != null && pid != null) {
	                     switch (orgType) {
	                     case CAMPUS: {
	                           hql = hql + " and f.campusId = :pid ";
	                           break;
	                     }
	                     case BUILDING: {
	                           hql = hql + " and f.buildingId = :pid ";
	                           break;
	                     }
	                     case FLOOR: {
	                           hql = hql + " and f.floor.id = :pid ";
	                           break;
	                     }
	                     case AREA: {
	                           hql = hql + " and f.area.id = :pid ";
	                           break;
	                     }
	                     case GROUP: {
	                           hql = hql + " and f.groupId = :pid ";
	                           break;
	                     }
	                     default: {
	                           // company level all fixtures
	                           pidExists = false;
	                     }
	                     }
	              }

	              Session s = getSession();
	              Query q = s.createQuery(hql);
					if(pidExists){
	              		q.setLong("pid", pid);
			  		}

	              return q.list();
       }
	/**
	 * Load fixture details.load all fixtures of given floor
	 *
	 * @param String
	 *            fixture state
	 * @return fixture list
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureByState(String state) {
		
				Session session = getSession();
			List<Fixture> fixtureList = session
					.createCriteria(Fixture.class)
					.add(Restrictions.eq("state",
							state))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
				return fixtureList;
			} else {
				return null;
			}	
	}
	
	/**
	 * Load fixture which are hoppers of a particular gateway
	 *
	 * @param gwId gateway id
	 * @return fixture list
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadHoppersBySecGwId(long gwId) {
		
	  Session session = getSession();
	  List<Fixture> fixtureList = session.createCriteria(Fixture.class)
         .add(Restrictions.eq("state",
	                        ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))
	      .add(Restrictions.eq("secGwId", gwId))
	      .add(Restrictions.eq("isHopper", 1))
	      .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	  if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
	    return fixtureList;
	  } else {
	    return null;
	  }
	  
	} //end of method loadHoppersBySecGwId
	
	
	/**
	 * Load Fixture Custom Profile
	 *
	 * @param fixtureId fixture id
	 * @return FixtureCustomGroupsProfile
	 */
	public FixtureCustomGroupsProfile loadCustomGroupByFixureId(long fixtureId) {
	  Session session = getSession();
	  FixtureCustomGroupsProfile customFixtGrp = (FixtureCustomGroupsProfile) session.createCriteria(FixtureCustomGroupsProfile.class)
			  .add(Restrictions.eq("fixtureId", fixtureId)).uniqueResult();
	  return customFixtGrp;
	} //end of method loadCustomGroupByFixureId
	
	/**
	 * Sync Fixture Custom profile
	 *
	 * @param fixtureId fixture id, ProfileHandler fixture id
	 * @return FixtureCustomGroupsProfile
	 */
	public void syncFixtureCustomProfile(ProfileHandler newPFH, Long fixtureId, Long groupId) {
		ProfileHandler fixtureProfileHandler = getProfileHandlerByGroupId(groupId);
		fixtureProfileHandler.copyFrom(newPFH);
		updateProfileHandler(fixtureProfileHandler);
		Session session = getSession();
		Groups group = (Groups) session .get(Groups.class, groupId);
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		String currProfile = fixture.getCurrentProfile();
		fixture.setGroupId(group.getId());
		fixture.setCurrentProfile(group.getName());
		fixture.setOriginalProfileFrom(currProfile);
	}

	public Long assignSUGroupProfileToFixture(long fixtureId, Groups oGroup) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, fixtureId);
		fixture.setGroupId(oGroup.getId());
		fixture.setOriginalProfileFrom(fixture.getCurrentProfile()); // Update this first
		fixture.setCurrentProfile(oGroup.getName());
		return oGroup.getId();
	}
	
	
	/**
	 * Load Fixture Custom Profile
	 *
	 * @param fixtureId fixture id
	 * @return FixtureCustomGroupsProfile
	 */
	public void updateFixtureCustomGroupProfile(FixtureCustomGroupsProfile fixtureCustomGroup) {
	  Session session = getSession();
	  try {
		  session.saveOrUpdate(fixtureCustomGroup);
      } catch (HibernateException hbe) {
          throw SessionFactoryUtils.convertHibernateAccessException(hbe);
      }
	}
	
	public int getProfileNoForFixture(long fixtureId) {
		String hsql = "SELECT  getProfileNoForFixture(" + BigInteger.valueOf((fixtureId)) + ")";    
        Query q = getSession().createSQLQuery(hsql.toString());
        int onLevel = (Integer)q.uniqueResult();
        return onLevel;
	}

	public void addVoltPowerMap(long voltMapId, HashMap<Double, Double> curveMap) {
	  
	  Query query = getSession().createQuery("Update BallastVoltPower set power = :power" +
	      " where volt = :volt and voltPowerMapId= :voltPowerMapId");
	  
	  Session session = getSession();
	  List<BallastVoltPower> curveList = session.createCriteria(BallastVoltPower.class)
	      .add(Restrictions.eq("voltPowerMapId", voltMapId))
	      .list();	  
	  Iterator<BallastVoltPower> curveValIter = curveList.iterator();
	  BallastVoltPower bVoltPwr = null;
	  ArrayList<Double> existingList = new ArrayList<Double>();
	  while(curveValIter.hasNext()) {
	    bVoltPwr = curveValIter.next();
	    bVoltPwr.setPower(curveMap.get(bVoltPwr.getVolt()));
	    query.setLong("voltPowerMapId", voltMapId);
	    query.setDouble("volt", bVoltPwr.getVolt());
	    query.setDouble("power", bVoltPwr.getPower());
	    query.executeUpdate();
	    existingList.add(bVoltPwr.getVolt());
	  }
	  
	  Iterator<Double> iter = curveMap.keySet().iterator();
	  Double volt = null;	  
	  while(iter.hasNext()) {
	    volt = iter.next();
	    if(existingList.contains(volt)) {
	      continue;
	    }
	    bVoltPwr = new BallastVoltPower();
	    bVoltPwr.setBallastId(0L);
	    bVoltPwr.setPower(curveMap.get(volt));
	    bVoltPwr.setVolt(volt);
	    bVoltPwr.setVoltPowerMapId(voltMapId);
	    session.save(bVoltPwr);
	  }	
	  existingList = null;
	  curveList = null;
	  
	} //end of method addVoltPowerMap
	
	public void updateBallastVoltPowerMapId(long ballastId, long voltMapId) {
	  
	  Query query = getSession().createQuery("Update Ballast set voltPowerMapId= :voltPowerMapId"
	      + " where id = :id");
	  query.setLong("id", ballastId);
	  query.setLong("voltPowerMapId", voltMapId);
	  query.executeUpdate();
	  	  
	} //end of method updateBallastVoltPowerMapId
	
	public long getMaxVoltPowerMapId() {
	  
	  long maxVoltPwrId = 2;
	  try {
	    Session s = getSession();
	    String hsql = "SELECT MAX(voltPowerMapId) from BallastVoltPower";
	    Query query = s.createQuery(hsql);
	    maxVoltPwrId =  (Long)query.uniqueResult();
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  return maxVoltPwrId;
	  
	} //end of method getMaxVoltPowerMapId

	@SuppressWarnings("unused")
	public List<Fixture> loadFixtureByTemplateId(Long id) {
		List<Fixture> fixtureList= new ArrayList<Fixture>();
		List<Object> tempFixtureList=null;
		try {
		    Session s = getSession();
		    String hsql = "select f.id, f.name,f.macAddress, f.version, f.gateway.id,f.gateway.name, f.location, f.state, f.isHopper, f.lastConnectivityAt from Fixture as f, Groups as g where f.state!='DELETED' and f.state!='PLACED' and f.groupId=g.id and g.profileTemplate.id=:templateId";
		    Query query = s.createQuery(hsql);
			query.setParameter("templateId",id);
			tempFixtureList = query.list();
			
			if (tempFixtureList != null && !tempFixtureList.isEmpty())
		    {
				Iterator<Object> it = tempFixtureList.iterator();
	              while (it.hasNext()) {
	            	  Object[] rowResult = (Object[]) it.next();
	            	  Fixture fixture = new Fixture();
	            	  fixture.setId((Long)rowResult[0]);
	            	  fixture.setFixtureName((String)rowResult[1]);
	            	  Gateway gateway = new Gateway();
	            	  gateway.setGatewayName((String)rowResult[5]);
	            	  gateway.setId((Long)rowResult[4]);
	            	  fixture.setGateway(gateway);
	            	  fixture.setVersion((String)rowResult[3]);
	            	  fixture.setMacAddress((String)(rowResult[2]));
	            	  fixture.setLocation((String)rowResult[6]);
	            	  fixture.setState((String)rowResult[7]);
	            	  fixture.setIsHopper((Integer)rowResult[8]);
	            	  fixture.setLastConnectivityAt((Date)rowResult[9]);
	            	  fixtureList.add(fixture);
	              }
	        }
			return fixtureList;
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
		  return null;
	}
	
	public Long getFixtureCountByTemplateId(Long templateId) {
		Long countFixtureList = 0L;
		try {
		    Session s = getSession();
		    String hsql = "select count(f.id) as fixtureCount from Fixture f, Groups as g where f.groupId=g.id and g.profileTemplate.id=:templateId";
		    Query query = s.createQuery(hsql);
			query.setParameter("templateId",templateId);
			countFixtureList = Long.valueOf(query.list().get(0).toString());
			} catch (Exception e) {
		    e.printStackTrace();
		  }
		  return countFixtureList;
	}
	
	public void changeGroupsSyncPending(Fixture fixture) {
		
		Session session = getSession();
		Fixture dbFixture = (Fixture) session.get(Fixture.class, fixture.getId());
		dbFixture.setGroupsSyncPending(fixture.getGroupsSyncPending());
		
	} //end of method changeGroupsSyncPending

    public void changeGroupsSyncPending(long id, boolean bEnable) {
        Session session = getSession();
        Fixture fixture = (Fixture) session.get(Fixture.class, id);
        fixture.setGroupsSyncPending(bEnable);
    }

    public List<Long> loadFixturesIdWithGroupSynchFlagTrue() {
        Query query = getSession().createQuery(
                " Select new java.lang.Long(f.id) from Fixture f where f.groupsSyncPending = :synchFlag ");
        query.setBoolean("synchFlag", true);
        List<Long> fixtureIds = query.list();
        return fixtureIds;
    }
    /**
	 * This method will to bulk assignment of profiles to selected Fixtures'
	 * list.
	 * 
	 * @param fixtureId
	 *            fixture id
	 * @param groupId
	 *            profile id
	 * @return totalRecordUpdated
	 */
	public Long bulkProfileAssignToFixture(String fixtureIdsList, Long groupId,
			String currentprofileName) {
		int totalRecordUpdated = 0;
		try {
			if(fixtureIdsList!=null && fixtureIdsList.length()>0)
			{
				String hsql1 = "update Fixture set original_profile_from=current_profile, current_profile=:currentprofileName, group_id=:groupId where id in ("+fixtureIdsList+") and current_profile!=:currentprofileName and group_id!=:groupId";
				Query query1 = getSession().createSQLQuery(hsql1);
				query1.setString("currentprofileName", currentprofileName);
				query1.setLong("groupId", groupId);
				query1.executeUpdate();
				
				// Update push profile and push global profile flag 
				String hsql2 = "update Fixture set push_profile=:pushFlag, push_global_profile=:pushFlag where id in ("+fixtureIdsList+")";
				Query query2 = getSession().createSQLQuery(hsql2);
				query2.setBoolean("pushFlag", true);
				totalRecordUpdated = query2.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (long) totalRecordUpdated;
	}

	public Fixture updateAreaID(Fixture oFixture) {
		Session session = getSession();
		Fixture fixtureval = (Fixture) session.load(Fixture.class, oFixture.getId());
		fixtureval.setAreaId(oFixture.getAreaId());
		session.update(fixtureval);
		return fixtureval;
	}
	
    public void enablePushProfileForGroup(Long groupId) {
        Query query = getSession().createQuery(
                "Update Fixture set " + " pushProfile = :pushProfile, pushGlobalProfile = :pushGlobalProfile"
                        + " where groupId = :groupId");
        query.setBoolean("pushProfile", true);
        query.setBoolean("pushGlobalProfile", true);
        query.setLong("groupId", groupId);
        query.executeUpdate();

    }
    
    @SuppressWarnings("unchecked")
    public List<Fixture> get1_0CommissionedDrReactiveFixtureList() {
     Session session = getSession();
     List<Fixture> fixtureList = session
       .createCriteria(Fixture.class)
       .add(Restrictions.eq("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))
       .add(Restrictions.ilike("version", "1.", MatchMode.START))
       .add(Restrictions.sqlRestriction("(select ph.dr_reactivity from groups g, profile_handler ph where g.id = {alias}.group_id and g.profile_handler_id = ph.id) > 0"))
       .list();
     if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
      return fixtureList;
     } else {
      return null;
     }
    }
    /**
	 * This method will reset Fixture's baseline load value to null so that newly assigned ballast's baseline will get replaced by null value on next PM Stats.
	 * 
	 * @param fixtureId
	 *            fixture id
	 * @return void
	 */
    public void resetFixtureBaseline(Long id) {
		Session session = getSession();
		Fixture fixture = (Fixture) session.get(Fixture.class, id);
		fixture.setBaselinePower(null);
	}

    /**
     * Return ballast curve for the ballast id At specified input volt (120, 277, 240; etc)
     * @param ballastId
     * @param inputVolts
     * @return ballast curve map for volt 10 - 0 with their respective power used for the specified input volts
     */
    public List<BallastVoltPower> getBallastVoltPowerCurve(long ballastId, double inputVolts) {
        ConcurrentHashMap<Double, List<BallastVoltPower>> inputVoltsToVoltpowerMap = this.ballastVPCurveMap
                .get(ballastId);
        List<BallastVoltPower> voltpowerList = null;
        if (inputVoltsToVoltpowerMap == null) {
            voltpowerList = getSession()
                    .createCriteria(BallastVoltPower.class)
                    .add(Restrictions.eq("ballastId", ballastId))
                    .add(Restrictions.eq("inputVolt", inputVolts))
                    .list();
            if (!ArgumentUtils.isNullOrEmpty(voltpowerList)) {
                ConcurrentHashMap<Double, List<BallastVoltPower>> inputVoltToCurve = new ConcurrentHashMap<Double, List<BallastVoltPower>>();
                inputVoltToCurve.put(inputVolts, voltpowerList);
                ballastVPCurveMap.put(ballastId, inputVoltToCurve);
            }
        }else {
            voltpowerList = inputVoltsToVoltpowerMap.get(inputVolts);
            if (voltpowerList == null) {
                voltpowerList = getSession()
                        .createCriteria(BallastVoltPower.class)
                        .add(Restrictions.eq("ballastId", ballastId))
                        .add(Restrictions.eq("inputVolt", inputVolts))
                        .list();
                if (!ArgumentUtils.isNullOrEmpty(voltpowerList)) {
                    ConcurrentHashMap<Double, List<BallastVoltPower>> inputVoltToCurve = new ConcurrentHashMap<Double, List<BallastVoltPower>>();
                    inputVoltToCurve.put(inputVolts, voltpowerList);
                    ballastVPCurveMap.put(ballastId, inputVoltToCurve);
                }
            }
        }
        return voltpowerList;
    }
    
    public List<FixtureLampStatusVO> getLampOutStatusFixtureList(String property, Long pid) {
    	List<FixtureLampStatusVO> oRecords = new ArrayList<FixtureLampStatusVO>();
        String hql = "select distinct f.id, f.sensor_id, ef.event_time, ef.description,d.x,d.y,d.location,blst.display_label from fixture f, device d,  events_and_fault ef," +
        		" ballasts blst where f.id=d.id and f.id = ef.device_id and f.ballast_id=blst.id and f.state =:state and ef.active='t' and ef.event_type like :eventType";
        
        FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
        boolean pidExists = true;
        if (property != null && pid != null) {
               switch (orgType) {
               case CAMPUS: {
                     hql = hql + " and d.campus_id = :pid ";
                     break;
               }
               case BUILDING: {
                     hql = hql + " and d.building_id = :pid ";
                     break;
               }
               case FLOOR: {
                     hql = hql + " and d.floor_id = :pid ";
                     break;
               }
               case AREA: {
                     hql = hql + " and d.area_id = :pid ";
                     break;
               }
               default: {
                     // company level all fixtures
                     pidExists = false;
               }
           }
        }
        hql = hql + " order by ef.event_time desc";
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        q.setParameter("eventType",EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
        
        if(pidExists){
    		q.setLong("pid", pid);
  		}
        List<Object[]> results = q.list();
        if (results != null && !results.isEmpty()) {
            for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                FixtureLampStatusVO oRecord = new FixtureLampStatusVO();
                oRecord.setFixtureId(((BigInteger) object[0]).longValue());
                oRecord.setFixtureName((String) object[1]);
                oRecord.setEventTime((Date) object[2]);
                oRecord.setDescription((String) object[3]);
                oRecord.setXposition((Integer) object[4]);
                oRecord.setYposition((Integer) object[5]);
                oRecord.setLocation((String) object[6]);
                oRecord.setDisplayLabel((String) object[7]);
                oRecords.add(oRecord);
            }
        }
        return oRecords;
    }
    
    public List<FixtureLampStatusVO> getFixtureOutStatusFixtureList(String property, Long pid) {
    	List<FixtureLampStatusVO> oRecords = new ArrayList<FixtureLampStatusVO>();
        String hql = "select distinct f.id, f.sensor_id, ef.event_time,ef.description,blst.display_label from fixture f, device d, events_and_fault ef ,ballasts blst where f.id = d.id and f.id = ef.device_id and f.ballast_id=blst.id and f.state =:state " +
        		"and ef.active='t' and ef.event_type like :eventType";
        
        FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
        boolean pidExists = true;
        if (property != null && pid != null) {
               switch (orgType) {
               case CAMPUS: {
                   hql = hql + " and d.campus_id = :pid ";
                   break;
	             }
	             case BUILDING: {
	                   hql = hql + " and d.building_id = :pid ";
	                   break;
	             }
               case FLOOR: {
                     hql = hql + " and d.floor_id = :pid ";
                     break;
                }
               case AREA: {
                     hql = hql + " and d.area_id = :pid ";
                     break;
                }
               default: {
                     // company level all fixtures
                     pidExists = false;
               }
           }
        }
        hql = hql + " order by ef.event_time desc";
        
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        q.setParameter("eventType",EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
        if(pidExists){
    		q.setLong("pid", pid);
  		}
        List<Object[]> results = q.list();
        if (results != null && !results.isEmpty()) {
            for (Iterator<Object[]> iterator = results.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                FixtureLampStatusVO oRecord = new FixtureLampStatusVO();
                oRecord.setFixtureId(((BigInteger) object[0]).longValue());
                oRecord.setFixtureName((String) object[1]);
                oRecord.setEventTime((Date) object[2]);
                oRecord.setDescription((String) object[3]);
                oRecord.setDisplayLabel((String) object[4]);
                oRecords.add(oRecord);
            }
        }
        return oRecords;
    }
    
    public List<FixtureLampStatusVO> getCalibratedFixtureList(String property, Long pid) {
    	List<FixtureLampStatusVO> oRecords = new ArrayList<FixtureLampStatusVO>();
    	
    	// Get the List of FX Curve available Fixture
    	String hql = "select distinct f.id, f.sensor_id, flc.capture_at from fixture f, device d, fixture_lamp_calibration flc, fixture_calibration_map fcm where f.id = d.id " +
    			"and f.id = flc.fixture_id and flc.id= fcm.fixture_lamp_calibration_id and f.state = :state";
    	
    	 FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
         boolean pidExists = true;
         if (property != null && pid != null) {
                switch (orgType) {
                case CAMPUS: {
                    hql = hql + " and d.campus_id = :pid ";
                    break;
                }
	             case BUILDING: {
	                    hql = hql + " and d.building_id = :pid ";
	                    break;
	            }
                case FLOOR: {
                      hql = hql + " and d.floor_id = :pid ";
                      break;
                }
                case AREA: {
                      hql = hql + " and d.area_id = :pid ";
                      break;
                }
                default: {
                      // company level all fixtures
                      pidExists = false;
                }
            }
         }
         
    	Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        if(pidExists){
    		q.setLong("pid", pid);
  		}
        List<Object[]> fxCalibrated = q.list();
        if (fxCalibrated != null && !fxCalibrated.isEmpty()) {
            for (Iterator<Object[]> iterator = fxCalibrated.iterator(); iterator.hasNext();) {
            	  Object[] object = (Object[]) iterator.next();
            	  BigInteger fxId = (BigInteger) object[0];
            	  String fxName = (String) object[1];
            	  Date eventTime = (Date) object[2];
            	  FixtureLampStatusVO oRecord = new FixtureLampStatusVO();
	              oRecord.setFixtureId(fxId.longValue());
	              oRecord.setFixtureName(fxName);
	              oRecord.setEventTime(eventTime);
	              // 1- FX Curve
	              oRecord.setCurveType((long) 1);
	              oRecords.add(oRecord);
            }
        }
        
        // Now Get the List of Ballast Curve Available Fixture
    	hql = "select distinct f.id, f.sensor_id,f.last_connectivity_at from fixture f join device d on f.id = d.id join ballast_volt_power bvp on  f.ballast_id = bvp.ballast_id and " +
    			"f.voltage=bvp.inputvolt and f.state=:state";
    	
    	boolean pidExists1 = true;
        if (property != null && pid != null) {
               switch (orgType) {
               case CAMPUS: {
                   hql = hql + " and d.campus_id = :pid ";
                   break;
	             }
	             case BUILDING: {
	                   hql = hql + " and d.building_id = :pid ";
	                   break;
	             }
               case FLOOR: {
                     hql = hql + " and d.floor_id = :pid ";
                     break;
               }
               case AREA: {
                     hql = hql + " and d.area_id = :pid ";
                     break;
               }
               default: {
                     // company level all fixtures
            	   pidExists1 = false;
               }
           }
        }
    	s = getSession();
        q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        if(pidExists1){
    		q.setLong("pid", pid);
  		}
        List<Object[]> ballastCalibrated = q.list();
        
        
        //Create Consolidated List of Calibrated Fixture having either FX Curve / Ballast Curve
        
        if (ballastCalibrated != null && !ballastCalibrated.isEmpty()) {
        	Boolean bFound = false;
            for (Iterator<Object[]> iterator1 = ballastCalibrated.iterator(); iterator1.hasNext();) {
            	bFound = false;
            	FixtureLampStatusVO oRecord1 = new FixtureLampStatusVO();
            	Object[] object1 = (Object[]) iterator1.next();
            	BigInteger fxId1 = (BigInteger) object1[0];
            	String fxName1 = (String) object1[1];
            	Date eventTime1 = (Date) object1[2];
            	  if (fxCalibrated != null && !fxCalibrated.isEmpty()) {
                      for (Iterator<Object[]> iterator = fxCalibrated.iterator(); iterator.hasNext();) {
                    	  Object[] object = (Object[]) iterator.next();
                    	  BigInteger fxId = (BigInteger) object[0];
			            	if(fxId1.longValue() == fxId.longValue())
			            	{
			            		bFound = true;
			            		break;
			            	}
			            }
			     }
            	 if(!bFound)
            	 {
            		 oRecord1.setFixtureId(fxId1.longValue());
		             oRecord1.setFixtureName(fxName1);
		             oRecord1.setEventTime(eventTime1);
		             // 2- Ballast Curve
		             oRecord1.setCurveType((long) 2);
		             oRecords.add(oRecord1);
            	 }
            }
        }
        //If there are not any records from above query indicates that No Fx Curve/Ballast Curve not available for the fixture
        return oRecords;
    }
    public int[] getUnCalibratedFixtureList() {
    	int[] fixtureArr=null;
    	// Get the List of Fixture for which FX Curve not available
    	String hql = "select f.id, d.version, f.cu_version, f.sensor_id, flc.capture_at from device d join fixture f on d.id = f.id left outer join fixture_lamp_calibration flc on f.id = flc.fixture_id " +
    			"left outer join fixture_calibration_map fcm on flc.id = fcm.fixture_lamp_calibration_id where fcm.id IS NULL and f.state = :state and f.use_fx_curve= 'true' order by f.id";
    	Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        List<Object[]> fxCalibrated = q.list();
        if (fxCalibrated != null && !fxCalibrated.isEmpty()) {
        	fixtureArr = new int[fxCalibrated.size()];
        	int i=0;
            for (Iterator<Object[]> iterator = fxCalibrated.iterator(); iterator.hasNext();) {
            	  Object[] object = (Object[]) iterator.next();
            	  Fixture f = new Fixture();
            	  f.setVersion((String) object[1]);
            	  f.setCuVersion((String) object[2]);
            	  if(ServerUtil.isNewCU(f))
            	  {
            		  fixtureArr[i] = ((BigInteger) object[0]).intValue();
            	  }
	              i++;
            }
        }
		return fixtureArr;
    }
    
    
	public List<Sensor> getDimLevels() {
		List<Fixture> fixtureList = getAllCommissionedFixtureList();
		ArrayList<Sensor> records = new ArrayList<Sensor>();
		for(Fixture fixt: fixtureList) {
			Sensor record = new Sensor();
			record.setName(fixt.getName());
			record.setMacAddress(fixt.getMacAddress());
			record.setCurrentDimLevel(fixt.getDimmerControl());
			records.add(record);
		}
		return records;
	}
	
	public List<Sensor> getLastOccupancy() {
		List<Fixture> fixtureList = getAllCommissionedFixtureList();
		ArrayList<Sensor> records = new ArrayList<Sensor>();
		for(Fixture fixt: fixtureList) {
			Sensor record = new Sensor();
			record.setName(fixt.getName());
			record.setMacAddress(fixt.getMacAddress());
			record.setLastOccupancySeen(fixt.getLastOccupancySeen());
			records.add(record);
		}
		return records;
	}
	
	public List<Sensor> getFixtureOutages() {
		List<FixtureOutageVO> fixtureOutageList = getFixtureOutageList("COMPANY", 1L);
		ArrayList<Sensor> records = new ArrayList<Sensor>();
		Set<Long> fixtureIds = new HashSet<Long>();
		for(FixtureOutageVO outageFixt: fixtureOutageList) {
			Fixture fixt = getFixtureById(outageFixt.getFixtureId());
			fixtureIds.add(outageFixt.getFixtureId());
			Sensor record = new Sensor();
			record.setName(fixt.getName());
			record.setMacAddress(fixt.getMacAddress());
			record.setOutageFlag(true);
			records.add(record);
		}
		List<Fixture> fixtureList = getAllCommissionedFixtureList();
		for(Fixture fixt: fixtureList) {
			if( ! fixtureIds.contains(fixt.getId())) {
				Sensor record = new Sensor();
				record.setName(fixt.getName());
				record.setMacAddress(fixt.getMacAddress());
				record.setOutageFlag(false);
				records.add(record);
			}
		}
		return records;
	}
	
	public void invalidateBallastVPCurve(long ballastId) {
        this.ballastVPCurveMap.remove(ballastId);
	}
	
	
	public void updateHeartbeatStats(Fixture fixture) {
		Session session = getSession();
		Fixture dbFixture = (Fixture) session.get(Fixture.class, fixture.getId());
		dbFixture.setLastConnectivityAt(fixture.getLastConnectivityAt());
		dbFixture.setLastOccupancySeen(fixture.getLastOccupancySeen());
		dbFixture.setLightLevel(fixture.getLightLevel());
		dbFixture.setAvgTemperature(fixture.getAvgTemperature());
		dbFixture.setDimmerControl(fixture.getDimmerControl());
	}
	
	
	public List<Sensor> getRealTimeStats() {
		List<Fixture> fixtureList = getAllCommissionedFixtureList();
		ArrayList<Sensor> records = new ArrayList<Sensor>();
		for(Fixture fixt: fixtureList) {
			Sensor record = new Sensor();
			record.setName(fixt.getName());
			record.setMacAddress(fixt.getMacAddress());
			record.setCurrentDimLevel(fixt.getDimmerControl());
			record.setAvgAmbientLight(fixt.getLightLevel().shortValue());
			record.setAvgTemperature(fixt.getAvgTemperature().shortValue());
			record.setLastOccupancySeen(fixt.getLastOccupancySeen());
			records.add(record);
		}
		return records;
	}
	
	public FixtureLampStatusVO getOutageTypeByFixtureId(Long deviceId)
	{
		FixtureLampStatusVO oRecords = new FixtureLampStatusVO();
	    String hql = "select distinct ef.event_type from events_and_fault ef where ef.device_id=:deviceId and ef.active='t' and ef.event_type like 'Fixture Out'";
	    Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("deviceId",deviceId);
        Object fxStatusList = q.uniqueResult();
        if (fxStatusList != null) {
            oRecords.setFixtureStatus(fxStatusList.toString());
        }else
        {
        	hql = "select distinct ef.event_type from events_and_fault ef where ef.device_id=:deviceId and ef.active='t' and ef.event_type like 'Lamp Out'";
    	    s = getSession();
            q = s.createSQLQuery(hql);
            q.setParameter("deviceId",deviceId);
            Object fxStatusList1 = q.uniqueResult();
            if(fxStatusList1 != null){
            	oRecords.setFixtureStatus(fxStatusList1.toString());
            }
        }
		return oRecords;
	}
	
	public void setUseFxCurveFlag(Long fixtureId, Boolean flag) {
		Fixture fixture = getFixtureById(fixtureId);
		fixture.setUseFxCurve(flag);
		Session session = getSession();
		session.update(fixture);
    }
	
	public int[] getFixtureIdsListUsingBallastCurve(Long ballastId, Double inputVoltage)
	{
		int[] fixtureArr=null;
    	// Get the List of Fixture for which Ballast Curve available and fixture is using it
    	String hql = "select id,sensor_id from fixture where not id in (select fixture_id from fixture_lamp_calibration) and ballast_id=:ballastId and state=:state and voltage=:voltage order by id asc";
    	Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("ballastId",ballastId);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        q.setParameter("voltage",inputVoltage);
        List<Object[]> fxCalibrated = q.list();
        if (fxCalibrated != null && !fxCalibrated.isEmpty()) {
        	fixtureArr = new int[fxCalibrated.size()];
        	int i=0;
            for (Iterator<Object[]> iterator = fxCalibrated.iterator(); iterator.hasNext();) {
            	  Object[] object = (Object[]) iterator.next();
            	  fixtureArr[i] = ((BigInteger) object[0]).intValue();
	              i++;
            }
        }
		return fixtureArr;
	}
	
    public List<Object[]> getFixturesCountByModelNo()
    {
        List<Object[]> fxList = null;
        String hql = "select distinct d.model_no, count(d.id) from device d join fixture f on f.id=d.id where d.type =:type and f.state=:state group by d.model_no";
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("type",DeviceType.Fixture.getName());
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        fxList = q.list();
        return  fxList;
    }
    public List<Object[]> getCusCountByVersionNo()
    {
        List<Object[]> cusList = null;
        String hql = "select distinct f.cu_version, count(f.id) from fixture f where f.state=:state group by f.cu_version";
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        cusList = q.list();
        return  cusList;
    }
    
    /**
	 * return list of fixture objects with the id's in Array of Fixture Id's
	 * 
	 * @param fixArrayList Array of Fixture Id's
	 * @return list of fixture objects
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> getFixturesListByFixtureIds(List<Long> fixArrayList) {
		
		Session session = getSession();
		List<Fixture> fixtureList = session
		.createCriteria(Fixture.class)
		.createAlias("area", "area", Criteria.LEFT_JOIN)
		.add(Restrictions.ne("state",
				ServerConstants.FIXTURE_STATE_DELETED_STR))
		.add(Restrictions.in("id",
				fixArrayList))
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
				
	} // end of method getFixturesListByFixtureIds

	/**
	 * Load fixture locations.load all fixtures of given area
	 *
	 * @param areaid
	 *            area id
	 * @param floorId           
	 * @return com.ems.model.Fixture collection load only id,sensorId,floor
	 *         id,area id,subArea id, x axis,y axis details of fixture other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Fixture> loadFixtureLocationsByAreaId(Long areaId,Long floorId) {

		try {
			Session s = getSession();
			String hsql = null;
			Query q = null;
			if(areaId == 0){
				hsql = "SELECT f.id, f.name, f.xaxis, f.yaxis, f.groupId, f.macAddress, f.areaId  "
						+ "from Fixture as f where f.areaId is null and state = 'COMMISSIONED'  ";
						String optionalFloorClause =  "and f.floor.id = :floorId  ";
						String orderByClause =  "order by f.id";
				if(floorId!= null){
					hsql = hsql + optionalFloorClause + orderByClause;
					q = s.createQuery(hsql);
					q.setLong("floorId", floorId);
				}else{
					hsql = hsql + orderByClause;
					q = s.createQuery(hsql);
				}
			}else{
				hsql = "SELECT f.id, f.name, f.xaxis, f.yaxis, f.groupId, f.macAddress, f.areaId  "
						+ "from Fixture as f where f.area.id = :areaId and state = 'COMMISSIONED' order by f.id";
				q = s.createQuery(hsql);
				q.setLong("areaId", areaId);				
			}	
			
			return q.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	} 
	
	public int getFloorUnAssignedAreaOccState(long floorId) {
		
		String hsql = "SELECT COUNT(f) FROM Fixture f WHERE f.area.id IS NULL AND lighting_occ_status = 1 AND " +
				" state = 'COMMISSIONED' AND floor_id = " + floorId;
		try {
		Query q = getSession().createQuery(hsql.toString());
		Object output = q.uniqueResult();
    	Integer count = new Integer(output.toString());
    	if(count > 0) {
    		return 1;
    	}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
		
	} //end of method getFloorUnAssignedAreaOccState
	
	public List<Long> getOccupiedAreasOfFloor(long floorId) {
		
		String hsql = "SELECT f.area.id, COUNT(f) FROM Fixture f WHERE f.floor.id = :floorId AND state = 'COMMISSIONED' " +
				"	AND lighting_occ_status = 1 GROUP BY f.area.id";
		ArrayList<Long> areaList = new ArrayList<Long>();
		try {			
			Query q = getSession().createQuery(hsql);
			q.setLong("floorId", floorId);
			List<Object[]> output = q.list();
			Iterator<Object[]> iter = output.iterator();			
			while(iter.hasNext()) {			
				Object[] values = iter.next();			
				if(values[1] != null) {
					int occCount = ((Long)values[1]).intValue();
					if(occCount > 0) {
						areaList.add((Long)values[0]);
					}
				}
			}    	
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return areaList;
		
	} //end of method getAreaOccupancyState
	
	public Long getFixtureUpTimeByFixtureId(long fixtureId) {
		String sql = " SELECT COALESCE(TRUNC(EXTRACT(EPOCH FROM (last_stats_rcvd_time - last_boot_time))),0) FROM fixture WHERE id = "+fixtureId+" AND  last_stats_rcvd_time >= now() - interval '15 minutes'";		
		Query q = getSession().createSQLQuery(sql.toString());
		List results = q.list();	
		Long uptime = null;
		if(results != null && results.size() > 0){
			if(results.get(0)!= null){				
				uptime = new Double(results.get(0).toString()).longValue();
			}			
		}else{
			uptime = 0L;
		}
    	return uptime;
		
	} //end of method getFixtureUpTimeByFixtureId
	
	@SuppressWarnings("unchecked")
	public List<Fixture> getFixturesByArea(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session.createCriteria(Fixture.class)
				.add(Restrictions.eq("areaId", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
	}

	public Long bulkBleModeAssignToFixture(String fixtureIdsList, String bleMode) {
		int totalRecordUpdated = 0;
		try {
			if(fixtureIdsList!=null && fixtureIdsList.length()>0)
			{
				String hsql1 = "update Fixture set ble_mode=:bleMode where id in ("+fixtureIdsList+")";
				Query query1 = getSession().createSQLQuery(hsql1);
				query1.setString("bleMode", bleMode);
				totalRecordUpdated = query1.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (long) totalRecordUpdated;
	}

	public Long updateDualChannelLEDValue(String fixtString, int percentage,
			int time) {
		int totalRecordUpdated = 0;
		try {
			if(fixtString!=null && fixtString.length()>0)
			{
				String hsql1 = "update Fixture set dual_channel_led_value=:dualchannelvalue, dualchannel_led_duration=:duration where id in ("+fixtString+")";
				Query query1 = getSession().createSQLQuery(hsql1);
				query1.setInteger("dualchannelvalue", percentage);
				query1.setInteger("duration", time);
				totalRecordUpdated = query1.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (long) totalRecordUpdated;
	}
	
	public List<Fixture> loadAllHopperFixtures() {
		
		  Session session = getSession();
		  List<Fixture> fixtureList = session.createCriteria(Fixture.class)
	         .add(Restrictions.eq("state",
		                        ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))		      
		      .add(Restrictions.eq("isHopper", 1))
		      .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		  if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
		    return fixtureList;
		  } else {
		    return null;
		  }
		  
	}
	public List loadBleFixtureListByFloorId(Long pid) {
		try {
			Session s = getSession();
			String hsql = "SELECT f.id, f.name, f.xaxis, f.yaxis, f.groupId, f.macAddress,  f.areaId, f.modelNo  "
					+ "from Fixture as f where f.floor.id = :floorId and state = 'COMMISSIONED' and (f.modelNo like 'SU-4S-H%' or f.modelNo like 'SU-4S-LRW%' or f.modelNo like 'SU-4S-HRW%' or f.modelNo like 'SU-4S-LRB%' or f.modelNo like 'SU-4S-HRB%' or f.modelNo like 'SU-4E-01W%' or f.modelNo like 'FS-D22%' or f.modelNo like 'CS-D2%' or f.modelNo like 'SU-4S-L%') order by f.id ";
			Query q = s.createQuery(hsql);
			q.setLong("floorId", pid);
			return q.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public List getRecentSensorData(long fixtureId, long captureDate) {

		try {
			Session s = getSession();
			String hsql = "SELECT f.id, f.name, ec.powerUsed, "
					+ "ec.motionBits, ec.avgTemperature,ec.lastVolts from EnergyConsumption as ec, Fixture as f"
					+ " where ec.zeroBucket = 0 and f.id = ec.fixture.id and f.id = :fixtureId and ec.captureAt = '"
					+ new Timestamp(captureDate) + "'";
			Query query = s.createQuery(hsql);
			query.setLong("fixtureId", fixtureId);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	} // end of method getAllSensorData
}