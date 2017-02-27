/**
 * 
 */
package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FixtureCalibrationMap;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.LampCalibrationConfiguration;
import com.ems.utils.ArgumentUtils;

/**
 * @author yogesh
 * 
 */
@Repository("fixtureCalibrationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureCalibrationDao extends BaseDaoHibernate {

    public FixtureLampCalibration getFixtureCalibrationMapByFixtureId(Long fixtureId) {
        Session session = getSession();
        List<FixtureLampCalibration> flcList = session.createCriteria(FixtureLampCalibration.class)
                .add(Restrictions.eq("fixtureId", fixtureId)).list();
        if (!ArgumentUtils.isNullOrEmpty(flcList)) {
            return flcList.get(0);
        } else {
            return null;
        }
    }

    public LampCalibrationConfiguration getCalibrationConfiguration() {
        Session session = getSession();
        List<LampCalibrationConfiguration> configurationList = session.createCriteria(
                LampCalibrationConfiguration.class).list();
        if (!ArgumentUtils.isNullOrEmpty(configurationList)) {
            return configurationList.get(0);
        } else {
            return null;
        }
    }

    public void update(FixtureCalibrationMap fcm) {
        Session session = getSession();
        session.update(fcm);
    }
    
    public void update(FixtureLampCalibration flc) {
        Session session = getSession();
        session.update(flc);
    }

    public void updateLampCalibrationConfiguration(LampCalibrationConfiguration lcm) {
        Session session = getSession();
        session.update(lcm);
    }
    
    public void updateFixtureCalibrationMap(List<FixtureCalibrationMap> fixtureCalibrationMap,Long fixtureId) {
    	FixtureLampCalibration flc= getFixtureCalibrationMapByFixtureId(fixtureId);
    	for(FixtureCalibrationMap fcm: fixtureCalibrationMap) {
    		Session session = getSession();
    		FixtureCalibrationMap fixtCal = (FixtureCalibrationMap) session.createCriteria(FixtureCalibrationMap.class)
                    .add(Restrictions.eq("fixtureLampCalibration.id", flc.getId()))
                    .add(Restrictions.eq("volt", fcm.getVolt()))
                    .uniqueResult();
    		if(fcm.getEnabled() != null && !fixtCal.getEnabled().equals(fcm.getEnabled())) {
    			fixtCal.setEnabled(fcm.getEnabled());
    			session.update(fixtCal);
    		}
    	}
    }
    @SuppressWarnings("unchecked")
	public List<FixtureCalibrationMap> getAllFixtureVoltPowersFromId(Long fixtureId) {
    	FixtureLampCalibration fcm= getFixtureCalibrationMapByFixtureId(fixtureId);
		List<FixtureCalibrationMap> fixtureVoltPowers = new ArrayList<FixtureCalibrationMap>();
		fixtureVoltPowers = getSession().createCriteria(FixtureCalibrationMap.class)
							.add(Restrictions.eq("fixtureLampCalibration.id", fcm.getId()))
							.addOrder(Order.asc("volt"))
							.list();
		return fixtureVoltPowers;
	}
    
    public void deleteFixtureCurve(Long fixtureId) {
    	Long fixtureLampCalibrationId=(long) 0;
    	FixtureLampCalibration fixtureLampCalibration = getFixtureCalibrationMapByFixtureId(fixtureId);
    	if(fixtureLampCalibration!=null)
    	{
	    	fixtureLampCalibrationId = fixtureLampCalibration.getId();
	    	
	    	String hsql = "delete from fixture_calibration_map where fixture_lamp_calibration_id="+fixtureLampCalibrationId;
	    	Query q = getSession().createSQLQuery(hsql.toString());
			q.executeUpdate();
			
			hsql = "delete from FixtureLampCalibration where fixtureId=?";
	        q = getSession().createQuery(hsql.toString());
	        q.setParameter(0, fixtureId);
			q.executeUpdate();
    	}
	}
}
