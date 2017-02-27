package com.enlightedinc.hvac.dao;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.model.Sensor;
import com.enlightedinc.hvac.model.Zone;
import com.enlightedinc.hvac.utils.DateUtil;


@Repository("zonesDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class ZonesDao extends BaseDaoHibernate{

static final Logger logger = Logger.getLogger(ZonesDao.class.getName());
	
    @Resource
    SessionFactory sessionFactory;
    
    @SuppressWarnings("unchecked")
	public List<Zone> getAllZones(){
    	List<Zone> zonesList = null;
    	Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(Zone.class);
    	zonesList =  criteria.list();
    	return zonesList;
    }

	@SuppressWarnings("unchecked")
	public List<Sensor> getAllSensorsForZone(Long zoneId) {
	     String zoneSensorQuery = "Select zoneSensor.sensor from ZonesSensor zoneSensor where zoneSensor.zone.id = :zoneId";
	     Query query = sessionFactory.getCurrentSession().createQuery(zoneSensorQuery);
	     query.setParameter("zoneId", zoneId);
	     List<Sensor> sensorList = (List<Sensor>)query.list();
	     return sensorList;
    }

	public void dissociateSensorFromZone(Long zoneId, Long sensorId){ 
    	String dissociateQuery = "delete from ZonesSensor zd where zd.zone.id = :zoneId and zd.sensor.id = :sensorId";
	    Query query = sessionFactory.getCurrentSession().createQuery(dissociateQuery);
	    query.setParameter("zoneId", zoneId);
	    query.setParameter("sensorId", sensorId);
    	query.executeUpdate();	    
    }
	
	@SuppressWarnings("unchecked")
	public Integer getTimeSinceLastOccupancyByZone(Long id) {
		Integer minId = -1;
		try {
			String hsql = "select min(s.last_occupancy_seen)  from sensor s, zones_sensor zs " +
					"where zs.sensor_id = s.id and zs.zone_id = " + id;
			Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object> results = q.list();
			if (results != null && !results.isEmpty() && results.get(0) != null) {
	            minId = Integer.parseInt(results.get(0).toString());
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return minId;
	}
	
	@SuppressWarnings("unchecked")
	public Integer getOutageStatus(Long zoneId) {
		Integer output = 0;
		try {
			String hsql = "select 1  from sensor s, zones_sensor zs " +
					"where zs.sensor_id = s.id and s.outage_flag is true and zs.zone_id = " + zoneId;
			Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object> results = q.list();
			if (results != null && !results.isEmpty() && results.get(0) != null) {
	            output = Integer.parseInt(results.get(0).toString());
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return output;
	}
	
	
	@SuppressWarnings("unchecked")
	public Integer getAvgDimLevel(Long zoneId) {
		Integer output = -1;
		try {
			String hsql = "select round(avg(s.current_dim_level))  from sensor s, zones_sensor zs " +
					"where zs.sensor_id = s.id and zs.zone_id = " + zoneId;
			Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object> results = q.list();
			if (results != null && !results.isEmpty() && results.get(0) != null) {
	            output = Integer.parseInt(results.get(0).toString());
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return output;
	}
	
	@SuppressWarnings("unchecked")
	public Integer getAvgTemperature(Long zoneId) {
		Integer output = 0;
		try {
			String hsql = "select round(avg(s.avg_temperature))  from sensor s, zones_sensor zs " +
					"where zs.sensor_id = s.id and zs.zone_id = " + zoneId;
			Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object> results = q.list();
			if (results != null && !results.isEmpty() && results.get(0) != null) {
	            output = Integer.parseInt(results.get(0).toString());
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return output;
	}
	
	@SuppressWarnings("unchecked")
	public Integer getTotalPower(Long zoneId) {
		Integer output = 0;
		Calendar startDateCal = Calendar.getInstance();
		Calendar endDateCal = Calendar.getInstance();
		int minute = startDateCal.get(Calendar.MINUTE);
		startDateCal.add(Calendar.MINUTE, -15 - (1*(minute%15)));
		endDateCal.add(Calendar.MINUTE, -1*(minute%15));
		try {
			String hsql = "select round(sum(avg_fixture_power)) as total_power from " +
					"(select avg(sh.power_used) as avg_fixture_power, sh.mac_address " +
					"from sensor_history sh, zones_sensor zs, sensor s " +
					"where zs.zone_id = " + zoneId + 
					" and zs.sensor_id = s.id " + 
					" and s.mac_address = sh.mac_address " +
					" and sh.zero_bucket != 1 " +
					" and sh.capture_at >'" + DateUtil.formatDate(startDateCal.getTime(), "yyyy-MM-dd HH:mm:ss") + "'" +
					" and sh.capture_at <= '"  + DateUtil.formatDate(endDateCal.getTime(), "yyyy-MM-dd HH:mm:ss") + "'" +
					"group by sh.mac_address) as avg_power";
			Query q = getSession().createSQLQuery(hsql.toString());
	        List<Object> results = q.list();
			if (results != null && !results.isEmpty() && results.get(0) != null) {
	            output = Integer.parseInt(results.get(0).toString());
			}
		} catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return output;
	}

}
