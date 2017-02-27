/**
 * enLighted Inc @ 2011
 */
package com.ems.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.EmsAudit;
import com.ems.server.ServerConstants;
import com.ems.server.util.ServerUtil;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;

/**
 * @author yogesh
 * 
 */
@Repository("emsAuditDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmsAuditDao extends BaseDaoHibernate {
    static final Logger logger = Logger.getLogger("AuditLogger");

    // public static final String AUDIT_CONTRACTOR = "Select new EmsAudit(a.id,"
    // + "a.txnId," + "a.deviceId," + "a.deviceType,"
    // + "a.attempts," + "a.action," + "a.startTime," + "a.endTime,"
    // + "a.status," + "a.comments," + "a.deviceName)";

    public EmsAudit update(EmsAudit emsaudit) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<EmsAudit> loadAllFixtureAuditlogs() {
        Session session = getSession();
        Calendar oNow = Calendar.getInstance();
        oNow.set(Calendar.MINUTE, -5);
        List<EmsAudit> emsAuditList = (List<EmsAudit>) session.createCriteria(EmsAudit.class)
                .add(Restrictions.eq("deviceType", ServerConstants.DEVICE_FIXTURE))
                .add(Restrictions.ge("startTime", DateUtil.formatDate(oNow.getTime(), "yyyy-MM-dd HH:mm:ss"))).list();
        if (!ArgumentUtils.isNullOrEmpty(emsAuditList)) {
            return emsAuditList;
        } else {
            return null;
        }

        // try {
        // Session s = getSession();
        // List<EmsAudit> results = null;
        // Calendar oNow = Calendar.getInstance();
        // oNow.set(Calendar.MINUTE, -5);
        // String hsql = AUDIT_CONTRACTOR + " from EmsAudit a where a.deviceType=" + ServerConstants.DEVICE_FIXTURE
        // + " AND a.startTime >= '" + DateUtil.formatDate(oNow.getTime(), "yyyy-MM-dd HH:mm:ss") + "')";
        // Query q = s.createQuery(hsql.toString());
        // results = q.list();
        // if (!ArgumentUtils.isNullOrEmpty(results)) {
        // return results;
        // }
        // } catch (HibernateException hbe) {
        // logger.error("Error in loading data", hbe.fillInStackTrace());
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public List<EmsAudit> loadAllGatewayAuditlogs() {
        // TODO Auto-generated method stub
        return null;
    }

    public EmsAudit insertAuditRecord(long txnId, int deviceType, Date oStartDate, Date oEndDate) {
        Session session = getSession();
        EmsAudit oEmsAudit = new EmsAudit();
        oEmsAudit.setTxnId(new Long(txnId));
        oEmsAudit.setStartTime(oStartDate);
        oEmsAudit.setEndTime(oEndDate);
        oEmsAudit.setDeviceType(new Integer(deviceType));
        oEmsAudit.setStatus(ServerConstants.AUDIT_SCHEDULED_STATUS);
        session.saveOrUpdate(oEmsAudit);
        return oEmsAudit;
    }

    public void insertAuditRecord(long txnId, Long deviceId, String deviceName, int deviceType, int msgType) {
        Calendar oNow = Calendar.getInstance();
        EmsAudit oEmsAudit = new EmsAudit();
        oEmsAudit.setTxnId(new Long(txnId));
        oEmsAudit.setStartTime(oNow.getTime());
        oEmsAudit.setDeviceName(deviceName);
        oEmsAudit.setDeviceId(deviceId);
        oEmsAudit.setDeviceType(new Integer(deviceType));
        oEmsAudit.setAction(ServerUtil.getCommandString(msgType));
        oEmsAudit.setStatus(ServerConstants.AUDIT_SCHEDULED_STATUS);
        oEmsAudit.setComments(Integer.toHexString(msgType));
        getSession().saveOrUpdate(oEmsAudit);
    }

    public void updateAuditRecord(long txnId, long deviceId, int attempts, String status) {

        Calendar oNow = Calendar.getInstance();
        // try {
        String queryStr = "UPDATE EmsAudit SET attempts = :attempts, status = :status "
                + " WHERE txnId = :txnId AND deviceId = :deviceId";
        Query q = getSession().createQuery(queryStr);
        q.setInteger("attempts", attempts);
        q.setString("status", status);
        q.setLong("txnId", txnId);
        q.setLong("deviceId", deviceId);
        q.executeUpdate();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }

    }

    public void updateAuditRecord(long txnId, int attempts, int status) {
        Session session = getSession();
        List<EmsAudit> emsAuditList = session.createCriteria(EmsAudit.class).add(Restrictions.eq("txnId", txnId))
                .addOrder(Order.desc("startTime")).list();

        if (!ArgumentUtils.isNullOrEmpty(emsAuditList)) {
            Calendar oNow = Calendar.getInstance();
            for (EmsAudit oEmsAudit : emsAuditList) {
                if (status == 0) {
                    oEmsAudit.setAttempts(attempts);
                    oEmsAudit.setEndTime(oNow.getTime());
                    oEmsAudit.setStatus(ServerConstants.AUDIT_SUCCESS_STATUS);
                } else {
                    oEmsAudit.setAttempts(attempts);
                    oEmsAudit.setEndTime(oNow.getTime());
                    oEmsAudit.setStatus(ServerConstants.AUDIT_FAIL_STATUS);
                }
            }
        }

        // Calendar oNow = Calendar.getInstance();
        // try {
        // List<EmsAudit> results = null;
        // String hsql = AUDIT_CONTRACTOR + " from EmsAudit a where a.txnId=? order by startTime desc)";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, new Long(txnId));
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // EmsAudit oEmsAudit = results.get(0);
        // if (oEmsAudit != null) {
        // // logger.debug("Updating status: " + status + ", " + oEmsAudit.toString());
        // if (status == 0) {
        // oEmsAudit.setAttempts(attempts);
        // oEmsAudit.setEndTime(oNow.getTime());
        // oEmsAudit.setStatus(ServerConstants.AUDIT_SUCCESS_STATUS);
        // } else {
        // oEmsAudit.setAttempts(attempts);
        // oEmsAudit.setEndTime(oNow.getTime());
        // oEmsAudit.setStatus(ServerConstants.AUDIT_FAIL_STATUS);
        // }
        // getSession().saveOrUpdate(oEmsAudit);
        // }
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
    }

    public void updateAuditRecord(long txnId, long deviceId, int attempts, int status) {
        Calendar oNow = Calendar.getInstance();
        String queryStr = "UPDATE EmsAudit SET attempts = :attempts, status = :status, endTime = :endTime "
                + " WHERE txnId = :txnId AND deviceId = :deviceId";
        Query q = getSession().createQuery(queryStr);
        q.setInteger("attempts", attempts);
        if (status == 0)
            q.setString("status", ServerConstants.AUDIT_SUCCESS_STATUS);
        else
            q.setString("status", ServerConstants.AUDIT_FAIL_STATUS);
        q.setTimestamp("endTime", oNow.getTime());
        q.setLong("txnId", txnId);
        q.setLong("deviceId", deviceId);
        q.executeUpdate();
    }

    public EmsAudit getAuditRecord(Date fromDate) {
        Session session = getSession();
        List<EmsAudit> emsAuditList = session.createCriteria(EmsAudit.class)
                .add(Restrictions.eq("startTime", DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm:ss"))).list();
        if (!ArgumentUtils.isNullOrEmpty(emsAuditList)) {
            return emsAuditList.get(0);
        }
        return null;

        // try {
        // Session s = getSession();
        // List<EmsAudit> results = null;
        // String hsql = AUDIT_CONTRACTOR + " from EmsAudit a where a.startTime='"
        // + DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm:ss") + "')";
        // Query q = s.createQuery(hsql.toString());
        // results = q.list();
        // if (!ArgumentUtils.isNullOrEmpty(results)) {
        // return results.get(0);
        // }
        // } catch (HibernateException hbe) {
        // logger.error("Error in loading data", hbe.fillInStackTrace());
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public void updateDRStatus(Date currentTime) {
        SQLQuery query = getSession()
                .createSQLQuery(
                        "update EmsAudit e set e.status = :status WHERE e.deviceType = 2 AND e.status = :activeStatus AND e.endTime < :endTime");
        query.setString("status", ServerConstants.DR_STATUS_FINISHED);
        query.setDate("endTime", currentTime);
        query.setString("activeStatus", ServerConstants.DR_STATUS_ACTIVE);
        query.executeUpdate();
    }

}
