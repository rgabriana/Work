package com.occengine.dao;

import java.util.List;

import org.apache.log4j.Logger;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BaseDaoHibernate;
import com.occengine.model.Sensor;
import com.occengine.model.SensorRGL;
import com.occengine.utils.ArgumentUtils;

/**
 *
 * @author pankaj kumar chauhan
 *
 */
@Repository("sensorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SensorDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger("SensorLogger");
	
	/**
	 * Loads all the sensor belonging to an zone
	 *
	 * @param id
	 * @return com.ems.model.Sensor collection
	 */
	@SuppressWarnings("unchecked")
	public List<Sensor> loadSensorsByZoneId(Long id) {
		
		Session session = getSession();
		List<Sensor> sensorList = session.createCriteria(Sensor.class).createAlias("zone", "zone", Criteria.LEFT_JOIN)
				.add(Restrictions.eq("zone.id", id)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(sensorList)) {
			return sensorList;
		} else {
			return null;
		}
		
	}
	
	/**
	 * Loads all the rgl sensor belonging to an zone
	 *
	 * @param id
	 * @return com.ems.model.SensorRGL collection
	 */
	@SuppressWarnings("unchecked")
	public List<SensorRGL> loadRGLSensorsByZoneId(Long id) {
		
		Session session = getSession();
		List<SensorRGL> sensorList = session.createCriteria(SensorRGL.class).createAlias("zone", "zone", Criteria.LEFT_JOIN)
				.add(Restrictions.eq("zone.id", id)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(sensorList)) {
			return sensorList;
		} else {
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Sensor> getAllSensors() {
		
		Session session = getSession();
		List<Sensor> sensorList = session.createCriteria(Sensor.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(sensorList)) {
			return sensorList;
		} else {
			return null;
		}
		
	}
	
	/**
	 * get the sensor by id
	 *
	 * @param id
	 * @return the sensor by id
	 */
	public Sensor getSensorById(Long id) {
		
		Session session = getSession();
		Sensor sensor = (Sensor) session.createCriteria(Sensor.class)
				.add(Restrictions.eq("id", id)).uniqueResult();
		return sensor;

	} // end of method getSensorById
	
	/**
	 * get the sensor by mac
	 *
	 * @param mac
	 * @return the sensor by mac
	 */
	public Sensor getSensorByMac(String mac) {
		
		Session session = getSession();
		Sensor sensor = (Sensor) session.createCriteria(Sensor.class)
				.add(Restrictions.eq("mac", mac)).uniqueResult();
		//System.out.println("mac from db -- " + sensor.getMac());
		return sensor;

	} // end of method getSensorByMac
	
	public void update(Sensor sensor) {
  
		Session session = getSession();
		//System.out.println("temp -- " + sensor.getAvgTemperature());
    session.update(sensor);
    
	} //end of method update

} //end of class SensorDao
