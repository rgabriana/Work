/**
 * 
 */
package com.ems.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.InventoryDevice;

/**
 * @author EMS
 * 
 */
@Repository("inventoryDeviceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class InventoryDeviceDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger("InventoryDeviceDaoImpl");

    public InventoryDevice getInventoryDeviceBysnapAddr(String snapAddr) {

        // TODO Auto-generated method stub
        return (InventoryDevice) getSession().createCriteria(InventoryDevice.class)
                .add(Restrictions.eq("snapAddr", snapAddr)).uniqueResult();

    }

    public InventoryDevice getInventoryDeviceByMacAddr(String macAddr) {

        // TODO Auto-generated method stub
        return (InventoryDevice) getSession().createCriteria(InventoryDevice.class)
                .add(Restrictions.eq("macAddr", macAddr)).uniqueResult();

    }

    public void addInventoryDevice(InventoryDevice device) {

        logger.debug("Add Inventory device");
        logger.debug("Snap Addr:" + device.getSnapAddr());
        logger.debug("Network Id: " + device.getNetworkId());
        logger.debug("Fixture Name: " + device.getDeviceName());
        logger.debug("Mac Addr: " + device.getMacAddr());
        logger.debug("Version: " + device.getVersion());
        logger.debug("Discovered Time:" + device.getDiscoveredTime());
        logger.debug("Floor Id: " + device.getFloorId());
        logger.debug("Status: " + device.getStatus());

        getSession().saveOrUpdate(device);

    } // end of method addInventoryDevice

    /**
     * Load all InventoryDevice associated with given floor
     * 
     * @param id
     *            floor id
     * @return Collection of InventoryDevice
     */
    @SuppressWarnings("unchecked")
    public List<InventoryDevice> loadInventoryDeviceByFloorId(Long id) {
        try {
            logger.debug("Floor Id:" + id);
            List<InventoryDevice> results = null;
            String hsql = " from InventoryDevice i where i.floorId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                for (InventoryDevice inventoryDevice : results) {
                    logger.debug("Id:" + inventoryDevice.getDeviceName() + "Mac:" + inventoryDevice.getMacAddr()
                            + "Status:" + inventoryDevice.getStatus());
                }
                return results;
            } else {
                logger.debug("No Fixture found for Floor Id:" + id);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Load all InventoryDevice associated with given building
     * 
     * @param id
     *            building id
     * @return Collection of InventoryDevice
     */
    @SuppressWarnings("unchecked")
    public List<InventoryDevice> loadInventoryDeviceByBuildingId(Long id) {
        try {
            List<InventoryDevice> results = null;
            String hsql = "from InventoryDevice i where i.floorId "
                    + "in(Select f.id from Floor f where f.building.id=?)";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Load all InventoryDevice associated with given campus
     * 
     * @param id
     *            campus id
     * @return Collection of InventoryDevice
     */
    @SuppressWarnings("unchecked")
    public List<InventoryDevice> loadInventoryDeviceByCampusId(Long id) {
        try {
            List<InventoryDevice> results = null;
            String hsql = "from InventoryDevice i where i.floorId "
                    + "in(Select f.id from Floor f where f.building.campus.id=?)";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Load all InventoryDevice
     * 
     * @return Collection of InventoryDevice
     */
    @SuppressWarnings("unchecked")
    public List<InventoryDevice> loadAllInventoryDevice() {
        try {
            List<InventoryDevice> results = null;
            String hsql = "from InventoryDevice i)";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Load all InventoryDevice by type
     * 
     * @return Collection of InventoryDevice
     */
    @SuppressWarnings("unchecked")
    public List<InventoryDevice> loadAllInventoryDeviceByType(Integer deviceType) {
        try {
            List<InventoryDevice> results = null;
            String hsql = "from InventoryDevice i where i.deviceType = ?)";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, deviceType);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public void deleteInventoryDevices(long gwId) {

        SQLQuery query = getSession().createSQLQuery("delete from InventoryDevice where " + "gwId = :gwId");
        query.setLong("gwId", gwId);
        query.executeUpdate();

    } // end of method deleteUnplacedFixtures

    public void deleteInventoryDeviceByMacAddress(String macAddress) {

        SQLQuery query = getSession()
                .createSQLQuery("delete from InventoryDevice where " + "mac_address = :macAddress");
        query.setString("macAddress", macAddress);
        query.executeUpdate();

    } // end of method deleteCommissioned devices from inventory.

} // end of class InventoryDaoImpl
