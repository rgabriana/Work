/**
 * 
 */
package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.ems.model.Device;
import com.ems.model.Wds;


/**
 * @author shilpa
 * 
 */
@Repository("deviceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DeviceDao extends BaseDaoHibernate {

    public Device getDeviceBySnapAddress(String snapAddress) {
        Session session = getSession();
        Device device = (Device) session.createCriteria(Device.class).add(Restrictions.eq("macAddress", snapAddress))
                .uniqueResult();
        return device;
    }

    public void update(Device device) {
        Session session = getSession();
        session.update(device);
    }

}
