package com.enlightedinc.hvac.dao;

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

}
