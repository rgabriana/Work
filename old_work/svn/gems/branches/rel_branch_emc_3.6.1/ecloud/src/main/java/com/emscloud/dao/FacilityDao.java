package com.emscloud.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FacilityType;
import com.emscloud.model.api.DetailedProfile;
import com.emscloud.model.api.Profile;
import com.emscloud.util.DatabaseUtil;
import com.emscloud.vo.Device;
import com.emscloud.vo.Fixture;
import com.emscloud.vo.Gateway;

@Repository("facilityDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityDao {
	
	static final Logger logger = Logger.getLogger(FacilityDao.class.getName());
	
	@Resource 
	SessionFactory sessionFactory;	

   public Facility getFacility(long id) {
    	Object obj = getObject(Facility.class, id);
    	if(obj == null )
    		return null;
    	else
    		return (Facility)obj;  
    }
   
	@SuppressWarnings("unchecked")
	public Facility loadFacilityTreeByCustomer(long custId) {
		
		try {
			Facility facility = (Facility)sessionFactory.getCurrentSession().createCriteria(Facility.class).
					add(Restrictions.eq("customerId", custId)).
					add(Restrictions.eq("type", 1)).uniqueResult();
			return facility;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadFacilityTreeByCustomer

	public Facility loadFacilityById(long id) {
		
		try {
			Facility facility = (Facility)sessionFactory.getCurrentSession().createCriteria(Facility.class).
					add(Restrictions.eq("id", id)).uniqueResult();
			return facility;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	}
	
	public Facility loadFacilityByName(String name) {
		
		try {
			Facility facility = (Facility)sessionFactory.getCurrentSession().createCriteria(Facility.class).
					add(Restrictions.eq("name", name)).uniqueResult();
			return facility;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	}
	
	public long getOrganizationIdOfCustomer(long custId) {
		
		try {
			String query = "SELECT id FROM Facility WHERE customer_id = " + custId + " AND type = 1";
			System.out.println("query -- " + query);			
			Query q = sessionFactory.getCurrentSession().createQuery(query);  
			Long orgId = (Long)q.uniqueResult();
      if(orgId == null) {
      	return -1;
      }
      return orgId;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method getOrganizationIdOfCustomer
	
	public long getCustomerLevelId(Long emId, Long levelId, int levelType) {

		try {
			String query = "SELECT facilityId FROM FacilityEmMapping fem WHERE fem.emId = " + emId +
					" AND fem.emFacilityId = " + levelId + " AND fem.emFacilityType = " + levelType; 
			System.out.println("query -- " + query);
			Query q = sessionFactory.getCurrentSession().createQuery(query); 
			Long facId =  (Long)q.uniqueResult();
			if(facId == null) {
				return -1;
			}
			return facId;			
		}
		catch(HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
				
	} //end of method getCustomerLevelId
	
	public List<FacilityType> loadAllFacilityTypes() {
		
		try {
			List<FacilityType> list = sessionFactory.getCurrentSession().createQuery(
					" from FacilityType f").list();			
			if (list != null && !list.isEmpty()) {
				return list;
	 		} else {
	 			return null;
	 		}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadAllFacilityTypes 
	
//	public List<Facility> loadBuildingFacilityByCustomer(long custId)
//	{
//		int buildingFacilityTypeId = com.emscloud.types.FacilityType.getFacilityType(com.emscloud.types.FacilityType.BUILDING);
//		try {
//			//Query query = sessionFactory.getCurrentSession().createQuery("from Facility where ")
//			List<Facility> buildingList = sessionFactory.getCurrentSession().createCriteria(Facility.class).
//					add(Restrictions.eq("customerId", custId)).
//					add(Restrictions.eq("type", buildingFacilityTypeId)).list();
//			return buildingList;
//		}
//		catch (HibernateException hbe) {
//			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
//		}
//	}
	
	public List<Facility> loadFacilityByCustomerAndFacilityType(long custId,com.emscloud.types.FacilityType facilityType)
	{
		int facilityTypeId = com.emscloud.types.FacilityType.getFacilityType(facilityType);
		try {
			//Query query = sessionFactory.getCurrentSession().createQuery("from Facility where ")
			List<Facility> buildingList = sessionFactory.getCurrentSession().createCriteria(Facility.class).
					add(Restrictions.eq("customerId", custId)).
					add(Restrictions.eq("type", facilityTypeId)).list();
			return buildingList;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
	}
	
	public void saveOrUpdate(Facility facility) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(facility) ;
				
	} //end of method saveOrUpdate
	
	@SuppressWarnings("unchecked")
	public List<FacilityEmMapping> getEmMappingsByFacilityId(long facilityId) {
		
		try {
			List<FacilityEmMapping> list = sessionFactory.getCurrentSession().createQuery(
					" from FacilityEmMapping f where f.facilityId = " + facilityId).list();
			if(list != null && !list.isEmpty()) {
				return list;
			} else {
				return null;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method getEmMappingsByFacilityId	
	
	public List<DetailedProfile> getProfilesByEmFloorId(String dbName, String replicaIp, Long id) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<DetailedProfile> list = new ArrayList<DetailedProfile>();
		try {
			if(dbName == null || dbName.isEmpty()) {
				return list;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			String query = "select groups.id, groups.name, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Monday') as weekdayMonday, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Tuesday') as weekdayTuesday, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Wednesday') as weekdayWednesday, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Thursday') as weekdayThursday, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Friday') as weekdayFriday, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Saturday') as weekdaySaturday, " +
					"(select case when type = 'weekday' then 'true' else 'false' end from weekday w where w.profile_configuration_id = pc.id and day = 'Sunday') as weekdaySunday, " +
					"pc.morning_time, pc.day_time, pc.evening_time, pc.night_time, ph.min_level_before_off, ph.to_off_linger, " +
					"pm1.min_level, pm1.on_level, pm1.ramp_up_time, pm1.motion_detect_duration, pm1.motion_sensitivity, pm1.ambient_sensitivity, " +
					"pd1.min_level, pd1.on_level, pd1.ramp_up_time, pd1.motion_detect_duration, pd1.motion_sensitivity, pd1.ambient_sensitivity, " +
					"pe1.min_level, pe1.on_level, pe1.ramp_up_time, pe1.motion_detect_duration, pe1.motion_sensitivity, pe1.ambient_sensitivity, " +
					"pn1.min_level, pn1.on_level, pn1.ramp_up_time, pn1.motion_detect_duration, pn1.motion_sensitivity, pn1.ambient_sensitivity, " +
					"pm2.min_level, pm2.on_level, pm2.ramp_up_time, pm2.motion_detect_duration, pm2.motion_sensitivity, pm2.ambient_sensitivity,  " +
					"pd2.min_level, pd2.on_level, pd2.ramp_up_time, pd2.motion_detect_duration, pd2.motion_sensitivity, pd2.ambient_sensitivity, " +
					"pe2.min_level, pe2.on_level, pe2.ramp_up_time, pe2.motion_detect_duration, pe2.motion_sensitivity, pe2.ambient_sensitivity, " +
					"pn2.min_level, pn2.on_level, pn2.ramp_up_time, pn2.motion_detect_duration, pn2.motion_sensitivity, pn2.ambient_sensitivity " +
					"from profile_handler ph, groups groups, profile_configuration pc, profile pm1, profile pd1, " +
					"profile pe1, profile pn1, profile pm2, profile pd2, profile pe2, profile pn2 " +
					"where ph.id in (select grps.profile_handler_id from groups grps where grps.id in " +
						"(select distinct f.group_id from fixture f, device d where d.id = f.id and d.floor_id = " + id + ")) " +
					"and groups.profile_handler_id = ph.id and pc.id = ph.profile_configuration_id and " +
					"ph.morning_profile_id = pm1.id and ph.day_profile_id = pd1.id and ph.evening_profile_id = pe1.id " +
					"and ph.night_profile_id = pn1.id and ph.morning_profile_weekend = pm2.id and " +
					"ph.day_profile_weekend = pd2.id and ph.evening_profile_weekend = pe2.id and " +
					"ph.night_profile_weekend = pn2.id";
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				DetailedProfile dp = new DetailedProfile();
				dp.setProfileId(rs.getLong(1));
				dp.setProfileName(rs.getString(2));
				dp.setWeekdayMonday("true".equals(rs.getString(3)));
				dp.setWeekdayTuesday("true".equals(rs.getString(4)));
				dp.setWeekdayWednesday("true".equals(rs.getString(5)));
				dp.setWeekdayThursday("true".equals(rs.getString(6)));
				dp.setWeekdayFriday("true".equals(rs.getString(7)));
				dp.setWeekdaySaturday("true".equals(rs.getString(8)));
				dp.setWeekdaySunday("true".equals(rs.getString(9)));
				dp.setStartMorning(rs.getString(10));
				dp.setStartDay(rs.getString(11));
				dp.setStartEvening(rs.getString(12));
				dp.setStartNight(rs.getString(13));
				dp.setAdvancedDimLingerLightLevel(rs.getInt(14));
				dp.setAdvancedLingerTime(rs.getInt(15));
				
				dp.setWeekdayMorning(new Profile());
				dp.getWeekdayMorning().setMinLight(rs.getShort(16));
				dp.getWeekdayMorning().setMaxLight(rs.getShort(17));
				dp.getWeekdayMorning().setRampupTime(rs.getShort(18));
				dp.getWeekdayMorning().setActiveMotionWindow(rs.getShort(19));
				dp.getWeekdayMorning().setMotionSensitivity(rs.getShort(20));
				dp.getWeekdayMorning().setAmbientSensitivity(rs.getShort(21));
				
				dp.setWeekdayDay(new Profile());
				dp.getWeekdayDay().setMinLight(rs.getShort(22));
				dp.getWeekdayDay().setMaxLight(rs.getShort(23));
				dp.getWeekdayDay().setRampupTime(rs.getShort(24));
				dp.getWeekdayDay().setActiveMotionWindow(rs.getShort(25));
				dp.getWeekdayDay().setMotionSensitivity(rs.getShort(26));
				dp.getWeekdayDay().setAmbientSensitivity(rs.getShort(27));
				
				dp.setWeekdayEvening(new Profile());
				dp.getWeekdayEvening().setMinLight(rs.getShort(28));
				dp.getWeekdayEvening().setMaxLight(rs.getShort(29));
				dp.getWeekdayEvening().setRampupTime(rs.getShort(30));
				dp.getWeekdayEvening().setActiveMotionWindow(rs.getShort(31));
				dp.getWeekdayEvening().setMotionSensitivity(rs.getShort(32));
				dp.getWeekdayEvening().setAmbientSensitivity(rs.getShort(33));
				
				dp.setWeekdayNight(new Profile());
				dp.getWeekdayNight().setMinLight(rs.getShort(34));
				dp.getWeekdayNight().setMaxLight(rs.getShort(35));
				dp.getWeekdayNight().setRampupTime(rs.getShort(36));
				dp.getWeekdayNight().setActiveMotionWindow(rs.getShort(37));
				dp.getWeekdayNight().setMotionSensitivity(rs.getShort(38));
				dp.getWeekdayNight().setAmbientSensitivity(rs.getShort(39));
				
				
				dp.setWeekendMorning(new Profile());
				dp.getWeekendMorning().setMinLight(rs.getShort(40));
				dp.getWeekendMorning().setMaxLight(rs.getShort(41));
				dp.getWeekendMorning().setRampupTime(rs.getShort(42));
				dp.getWeekendMorning().setActiveMotionWindow(rs.getShort(43));
				dp.getWeekendMorning().setMotionSensitivity(rs.getShort(44));
				dp.getWeekendMorning().setAmbientSensitivity(rs.getShort(45));
				
				dp.setWeekendDay(new Profile());
				dp.getWeekendDay().setMinLight(rs.getShort(46));
				dp.getWeekendDay().setMaxLight(rs.getShort(47));
				dp.getWeekendDay().setRampupTime(rs.getShort(48));
				dp.getWeekendDay().setActiveMotionWindow(rs.getShort(49));
				dp.getWeekendDay().setMotionSensitivity(rs.getShort(50));
				dp.getWeekendDay().setAmbientSensitivity(rs.getShort(51));
				
				dp.setWeekendEvening(new Profile());
				dp.getWeekendEvening().setMinLight(rs.getShort(52));
				dp.getWeekendEvening().setMaxLight(rs.getShort(53));
				dp.getWeekendEvening().setRampupTime(rs.getShort(54));
				dp.getWeekendEvening().setActiveMotionWindow(rs.getShort(55));
				dp.getWeekendEvening().setMotionSensitivity(rs.getShort(56));
				dp.getWeekendEvening().setAmbientSensitivity(rs.getShort(57));
				
				dp.setWeekendNight(new Profile());
				dp.getWeekendNight().setMinLight(rs.getShort(58));
				dp.getWeekendNight().setMaxLight(rs.getShort(59));
				dp.getWeekendNight().setRampupTime(rs.getShort(60));
				dp.getWeekendNight().setActiveMotionWindow(rs.getShort(61));
				dp.getWeekendNight().setMotionSensitivity(rs.getShort(62));
				dp.getWeekendNight().setAmbientSensitivity(rs.getShort(63));
				
				list.add(dp);
				
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return list;
		
	} //end of method getProfilesByEmFloorId
	
	public void savePlanMap(String dbName, String replicaIp, byte[] imageData, long floorId) {
		
		Connection connection = null;
		PreparedStatement stmt = null;		
		try {
			if(dbName == null || dbName.isEmpty()) {
				return;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.prepareStatement("UPDATE plan_map set plan = ? WHERE id = (select plan_map_id from floor WHERE id = ?)");
			stmt.setBytes(1, imageData);
			stmt.setLong(2, floorId);
			stmt.executeUpdate();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {			
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
			
	}
	
	public byte[] getFloorPlan(String dbName, String replicaIp, Long id) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		byte[] planMap = null;
		try {
			if(dbName == null || dbName.isEmpty()) {
				return planMap;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			String query = "select plan from plan_map pm, floor f " +
					"where f.plan_map_id = pm.id and f.id = " + id;
			rs = stmt.executeQuery(query);
			if(rs.next()) {
				planMap = rs.getBytes(1);
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return planMap;
		
	} //end of method getFloorPlan
	
	public List<Gateway> getGateways(String dbName, String replicaIp, Long floorId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Gateway> gwList = new ArrayList<Gateway>();
		try {
			if(dbName == null || dbName.isEmpty()) {
				return gwList;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			String query = "select d.id, name, x, y, mac_address from device d, gateway g " +
					"where d.id = g.id AND commissioned = 'true' AND floor_id = " + floorId;
			System.out.println("query - " + query);
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				Gateway gw = new Gateway();
				gw.setId(rs.getLong(1));
				gw.setName(rs.getString(2));
				gw.setLocX(rs.getFloat(3));
				gw.setLocY(rs.getFloat(4));
				gw.setMacAddress(rs.getString(5));
				gw.setFloorId(floorId);
				gwList.add(gw);
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return gwList;
		
	} //end of method getGateways
	
	public List<Fixture> getFixtures(String dbName, String replicaIp, Long floorId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Fixture> fixtureList = new ArrayList<Fixture>();
		try {
			if(dbName == null || dbName.isEmpty()) {
				return fixtureList;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			String query = "select d.id, name, x, y, mac_address, current_profile, baseline_power, d.version, f.gateway_id, " +
							"model_no, is_hopper, f.last_connectivity_at, g.channel, g.wireless_networkid, f.state, " +
							"(select array_to_string(array(select m.gems_group_id from motion_group m join gems_group_fixture ggf on ggf.group_id = m.gems_group_id where ggf.fixture_id = f.id), ',' )) as motion_groups," +
							"(select array_to_string(array(select s.gems_group_id from switch_group s join gems_group_fixture ggf on ggf.group_id = s.gems_group_id where ggf.fixture_id = f.id), ',' )) as switch_groups" +
							" from device d, fixture f, ballasts b, gateway g WHERE d.id = f.id AND f.ballast_id = b.id AND " +
							"g.id = f.gateway_id and state = 'COMMISSIONED' AND floor_id =" + floorId;
			System.out.println("query - " + query);
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				Fixture fix = new Fixture();
				fix.setId(rs.getLong(1));
				fix.setName(rs.getString(2));
				fix.setLocX(rs.getFloat(3));
				fix.setLocY(rs.getFloat(4));
				fix.setMacAddress(rs.getString(5));
				fix.setCurrProfile(rs.getString(6));
				fix.setBaselineEnergy(rs.getFloat(7));
				fix.setVersion(rs.getString(8));
				fix.setGatewayId(rs.getLong(9));
				fix.setFloorId(floorId);
				fix.setHardwareVersion(rs.getString(10));
				fix.setIsHopper(rs.getInt(11));
				fix.setLastConnectivity(rs.getTimestamp(12));
				fix.setChannel(rs.getShort(13));
				fix.setNetworkId(rs.getInt(14));
				fix.setState(rs.getString(15));
				fix.setMotionGroups(rs.getString(16));
				fix.setSwitchGroups(rs.getString(17));
				fixtureList.add(fix);
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return fixtureList;
		
	} //end of method getFixtures
	
	public List<Device> getDevices(String dbName, String replicaIp, Long floorId) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ArrayList<Device> deviceList = new ArrayList<Device>();
		try {
			if(dbName == null || dbName.isEmpty()) {
				return deviceList;
			}
			connection = DatabaseUtil.getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			String query = "select id, name, x, y, mac_address, type, version, model_no from device d WHERE type != 'Fixture' and floor_id = " + floorId;
			System.out.println("query - " + query);
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				Device device = new Device();
				device.setId(rs.getLong(1));
				device.setName(rs.getString(2));
				device.setLocX(rs.getFloat(3));
				device.setLocY(rs.getFloat(4));
				device.setMacAddress(rs.getString(5));
				device.setType(rs.getString(6));
				device.setVersion(rs.getString(7));
				device.setHardwareVersion(rs.getString(8));
				device.setFloorId(floorId);
				deviceList.add(device);
			}
			
			query = "select d.id, name, x, y, mac_address, d.type, version, model_no, is_hopper, " +
					"f.last_connectivity_at, g.channel, g.wireless_networkid, f.state," +
					"(select array_to_string(array(select m.gems_group_id from motion_group m join gems_group_fixture ggf on ggf.group_id = m.gems_group_id where ggf.fixture_id = f.id), ',' )) as motion_groups," +
					"(select array_to_string(array(select s.gems_group_id from switch_group s join gems_group_fixture ggf on ggf.group_id = s.gems_group_id where ggf.fixture_id = f.id), ',' )) as switch_groups" +
					" from device d, fixture f left outer join gateway g  on g.id = f.gateway_id WHERE f.id = d.id and floor_id = " + floorId;
			System.out.println("query - " + query);
			rs1 = stmt.executeQuery(query);
			while(rs1.next()) {
				Device device = new Device();
				device.setId(rs1.getLong(1));
				device.setName(rs1.getString(2));
				device.setLocX(rs1.getFloat(3));
				device.setLocY(rs1.getFloat(4));
				device.setMacAddress(rs1.getString(5));
				device.setType(rs1.getString(6));
				device.setVersion(rs1.getString(7));
				device.setHardwareVersion(rs1.getString(8));
				device.setFloorId(floorId);
				device.setIsHopper(rs1.getInt(9));
				device.setLastConnectivity(rs1.getTimestamp(10));
				device.setChannel(rs1.getShort(11));
				device.setNetworkId(rs1.getInt(12));
				device.setState(rs1.getString(13));
				device.setMotionGroups(rs1.getString(14));
				device.setSwitchGroups(rs1.getString(15));
				deviceList.add(device);
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeResultSet(rs1);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return deviceList;
		
	} //end of method getDevices
	
	public List<Facility> getChildFacilitiesByFacilityId(long facilityId){

		List<Facility> facilityList = sessionFactory.getCurrentSession().createCriteria(Facility.class)
				.add(Restrictions.eq("parentId", facilityId)).list();
		if(facilityList != null && facilityList.size() > 0) {
			return facilityList;
		} else {
			return null;
		}

	}  

	public List<Facility> getSiblingFacilitiesByFacilityId(long facilityId){

		Long parentFacilityId = loadFacilityById(facilityId).getParentId();
		List<Facility> facilityList = sessionFactory.getCurrentSession().createCriteria(Facility.class)
				.add(Restrictions.eq("parentId", parentFacilityId)).list();
		if(facilityList != null && facilityList.size() > 0) {
			return facilityList;
		} else {
			return null;
		}

	}
	
	public Object saveObject(Object o) {
        sessionFactory.getCurrentSession().saveOrUpdate(o);
        return o;
    }
	
	public void editFacility(Facility facility) {
        sessionFactory.getCurrentSession().update(facility);
    }
	
	 @SuppressWarnings("unchecked")
	    public Object getObject(Class clazz, Serializable id) {
	        // sessionFactory.getCurrentSession().clear();
	        Object o = sessionFactory.getCurrentSession().get(clazz, id);

	        if (o == null) {
	            throw new ObjectRetrievalFailureException(clazz, id);
	        }

	        return o;
	    }
	
	@SuppressWarnings("unchecked")
    public void removeObject(Class clazz, Serializable id) {
        sessionFactory.getCurrentSession().delete(getObject(clazz, id));
    }
			
} //end of class FacilityeDao
