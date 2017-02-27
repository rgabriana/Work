/**
 * 
 */
package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.HVACDevice;

/**
 * @author yogesh
 * 
 */
@Repository("hvacDevicesDao")
@Transactional(propagation = Propagation.REQUIRED)
public class HVACDevicesDao extends BaseDaoHibernate {
	private static final Logger logger = Logger.getLogger("HVACLogger");

	public List<HVACDevice> loadHVACDevicesByFloor(String facility, Long facilityId) {
		Session session = getSession();
		List<HVACDevice> oList = new ArrayList<HVACDevice>();
		Criteria oCriteria = session.createCriteria(HVACDevice.class);
        if (facility.equalsIgnoreCase("campus")) {
			oCriteria.add(Restrictions.eq("campusId", facilityId));
			oList = oCriteria.list();
        } else if (facility.equalsIgnoreCase("building")) {
			oCriteria.add(Restrictions.eq("buildingId", facilityId));
			oList = oCriteria.list();
        } else if (facility.equalsIgnoreCase("floor")) {
			oCriteria.add(Restrictions.eq("floorId", facilityId));
			oList = oCriteria.list();
        } else if (facility.equalsIgnoreCase("area")) {
			oCriteria.add(Restrictions.eq("areaId", facilityId));
			oList = oCriteria.list();
        } else {
			oList = oCriteria.list();
        }
		return oList;

	}

	public void updateGatewayPosition(HVACDevice hvDevice) {
        Session session = getSession();
        HVACDevice hvacDevice = (HVACDevice) session.get(HVACDevice.class, hvDevice.getId());
        hvacDevice.setXaxis(hvDevice.getXaxis());
        hvacDevice.setYaxis(hvDevice.getYaxis());
	}

	@SuppressWarnings("unchecked")
	public HVACDevice loadHvacByUserName(String hvacname) {
		Session session = getSession();
		List<HVACDevice> oList = new ArrayList<HVACDevice>();
		Criteria oCriteria = session.createCriteria(HVACDevice.class);
		oCriteria.add(Restrictions.eq("name", hvacname));
		oList = oCriteria.list();
		if (oList != null && !oList.isEmpty()) {
			HVACDevice hvac = (HVACDevice) oList.get(0);
            return hvac;
        }
        return null;
	}

	@SuppressWarnings("unchecked")
	public HVACDevice loadHvacById(Long hvacId) {
		Session session = getSession();
		List<HVACDevice> oList = new ArrayList<HVACDevice>();
		Criteria oCriteria = session.createCriteria(HVACDevice.class);
		oCriteria.add(Restrictions.eq("id", hvacId));
		oList = oCriteria.list();
		if (oList != null && !oList.isEmpty()) {
			HVACDevice hvac = (HVACDevice) oList.get(0);
            return hvac;
        }
        return null;
	}
}
