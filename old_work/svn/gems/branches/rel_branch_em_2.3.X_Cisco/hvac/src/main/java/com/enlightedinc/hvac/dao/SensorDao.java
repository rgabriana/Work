package com.enlightedinc.hvac.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.hvac.model.Sensor;

@Repository("sensorDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class SensorDao extends BaseDaoHibernate{

	public Sensor getSensorFromMac(String macAddress) {
	    Sensor sensor = null;
	    Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(Sensor.class);
    	criteria.add(Restrictions.eq("macAddress", macAddress));
    	sensor = (Sensor)criteria.uniqueResult();
    	return sensor;
    }
	
    @SuppressWarnings("unchecked")
	public List<Sensor> getAllSensors(){
    	List<Sensor> sensorsList = null;
    	Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(Sensor.class);
    	sensorsList =  criteria.list();
    	return sensorsList;
    }
    
    public Integer getDimLevel(Long sensorId) {
    	Session session = sessionFactory.getCurrentSession();
    	Sensor sensor = (Sensor) session.get(Sensor.class, sensorId);
    	if(sensor != null) {
    		return sensor.getCurrentDimLevel();
    	}
    	else {
    		return -1;
    	}
    }
    
    public Integer getTimeSinceLastOccupancy(Long sensorId) {
    	Session session = sessionFactory.getCurrentSession();
    	Sensor sensor = (Sensor) session.get(Sensor.class, sensorId);
    	if(sensor != null) {
    		return sensor.getLastOccupancySeen();
    	}
    	else {
    		return -1;
    	}
    }
    
    public Sensor getSensorById(Long id) {
    	Session session = sessionFactory.getCurrentSession();
    	Sensor sensor = (Sensor) session.get(Sensor.class, id);
    	return sensor;
    }

}
