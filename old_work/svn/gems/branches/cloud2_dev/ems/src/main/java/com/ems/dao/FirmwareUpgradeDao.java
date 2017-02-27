package com.ems.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FirmwareUpgrade;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.server.ServerConstants;
import com.ems.utils.DateUtil;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("firmwareUpgradeDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FirmwareUpgradeDao extends BaseDaoHibernate {

    // public static final String FIRMWARE_CONTRACTOR = "Select new FirmwareUpgrade(fu.id," +
    // "fu.user.id, " +
    // "fu.user.firstName, " +
    // "fu.fileName, " +
    // "fu.version, " +
    // "fu.upgradeOn, " +
    // "fu.deviceType)";

    public FirmwareUpgrade loadFirmwareUpgradeByDeviceType(Integer deviceType) {
        Session session = getSession();
        FirmwareUpgrade firmwareUpgrade = (FirmwareUpgrade) session.createCriteria(FirmwareUpgrade.class)
                .add(Restrictions.eq("deviceType", deviceType)).setMaxResults(1).uniqueResult();
        return firmwareUpgrade;

        // FirmwareUpgrade fmUpgrade = null;
        // try {
        // String hsql = FIRMWARE_CONTRACTOR + " From FirmwareUpgrade fu where fu.deviceType = ?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, deviceType);
        // q.setMaxResults(1);
        // fmUpgrade = (FirmwareUpgrade) q.uniqueResult();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return fmUpgrade;
    }

    public List<FirmwareUpgrade> getFirmWareUpgradeList() {
        return loadAll(FirmwareUpgrade.class);
    }

    public void updateDeviceStatus(Long jobId, Long deviceId, String status) {

        Query query = getSession().createQuery(
                "Update ImageUpgradeDeviceStatus set "
                        + "status = :status where jobId = :jobId and deviceId = :deviceId");
        query.setLong("jobId", jobId);
        query.setLong("deviceId", deviceId);
        query.setString("status", status);
        query.executeUpdate();

    } // end of method updateDeviceStatus
    
    public void startDeviceUpgrade(Long jobId, Long[] deviceIds) {

        String hsql = "Update ImageUpgradeDeviceStatus set startTime = :startTime, "
                + "status = :status where jobId = :jobId and device_id in (:deviceIds)";
        log.debug("start device upgrade query -- " + hsql);
        Query query = getSession().createQuery(hsql);
        query.setLong("jobId", jobId);
        query.setTimestamp("startTime", new Date());
        query.setString("status", ServerConstants.IMG_UP_STATUS_INPROGRESS);
        query.setParameterList("deviceIds", deviceIds).executeUpdate();

    } // end of method startDeviceUpgrade

    public void finishDeviceUpgrade(Long jobId, Long deviceId, String status, int noOfAttempts, String desc, String newVersion) {

        Query query = getSession().createQuery(
                "Update ImageUpgradeDeviceStatus set " + "status = :status, endTime = :endTime, description = :desc, "
                        + "noOfAttempts = :noOfAtts, new_version = :newVersion where jobId = :jobId and deviceId = :deviceId");
        query.setLong("jobId", jobId);
        query.setLong("deviceId", deviceId);
        query.setString("status", status);
        query.setTimestamp("endTime", new Date());
        query.setString("desc", desc);
        query.setInteger("noOfAtts", noOfAttempts);
        query.setString("newVersion", newVersion);
        query.executeUpdate();

    } // end of method finishDeviceUpgrade

	public List<ImageUpgradeDeviceStatus> loadDeviceStatus(Date fromDate,Date toDate) {
		List<ImageUpgradeDeviceStatus> oList = new ArrayList<ImageUpgradeDeviceStatus>();
		
		String hsql = "from ImageUpgradeDeviceStatus where startTime > '"+  DateUtil.formatDate(fromDate,"yyyy-MM-dd HH:mm:ss") + "' AND startTime < '" + DateUtil.formatDate(toDate,"yyyy-MM-dd HH:mm:ss") + "' ORDER BY start_time ASC";
        Query q = getSession().createQuery(hsql.toString());              
        oList = q.list();
        return oList;
	}

}
