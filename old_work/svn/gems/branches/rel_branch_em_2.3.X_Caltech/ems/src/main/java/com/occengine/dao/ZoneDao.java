package com.occengine.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BaseDaoHibernate;
import com.occengine.model.Zone;
import com.occengine.utils.ArgumentUtils;

/**
 *
 * @author pankaj kumar chauhan
 *
 */
@Repository("zoneDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ZoneDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger("ZoneLogger");
	
	@SuppressWarnings("unchecked")
	public List<Zone> getAllZones() {
		
		Session session = getSession();
		List<Zone> zoneList = session.createCriteria(Zone.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(zoneList)) {
			return zoneList;
		} else {
			return null;
		}
		
	} //end of method getAllZones
	
	/**
	 * get the sensor by id
	 *
	 * @param id
	 * @return the sensor by id
	 */
	public Zone getZoneById(Long id) {
		
		Session session = getSession();
		Zone zone = (Zone) session.createCriteria(Zone.class)
				.add(Restrictions.eq("id", id)).uniqueResult();
		return zone;

	} // end of method getZoneById
	
	public void zoneOccStatus(int hbEnlFailureTolerance, int unOccPeriod, Date startTime, Date endTime, Date hbTime,
			int hbFailedSensorsPercentage) {
		
		Connection con = null;
    CallableStatement calSt = null;
    try {
        con = getSession().connection();
        con.setAutoCommit(false);
     
        String query = "{ call occ_status(" + hbEnlFailureTolerance + ", " + unOccPeriod + ", '" + 
        		new Timestamp(startTime.getTime()) + "', '" + new Timestamp(endTime.getTime()) + "', '" +
        		new Timestamp(hbTime.getTime()) +"', " + hbFailedSensorsPercentage + ") }";
        //System.out.println("query -- " + query);
        calSt = con.prepareCall(query);
        calSt.execute();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    
	} //end of method zoneOccStatus
	
	public void updateBmsClientInfo(long zoneId, Date hbTime, int setbackValue) {
		
		Session session = getSession();
		Zone zone = (Zone) session.load(Zone.class, zoneId);
		zone.setBmsClientLastHbTime(hbTime);
		zone.setLastBmsClientSetback(setbackValue);
		
	} //end of method updateBmsClientInfo

} //end of class ZoneDao
