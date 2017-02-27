/**
 * enLighted Inc @ 2011
 */
package com.ems.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.EmsAuditDao;
import com.ems.model.EmsAudit;

/**
 * @author yogesh
 * 
 */
@Service("emsAuditService")
@Transactional(propagation = Propagation.REQUIRED)
public class EmsAuditService {

    @Resource
    private EmsAuditDao emsAuditDao;

    public EmsAudit save(EmsAudit emsaudit) {
        return (EmsAudit) emsAuditDao.saveObject(emsaudit);
    }

    public EmsAudit update(EmsAudit emsaudit) {
        return (EmsAudit) emsAuditDao.update(emsaudit);
    }

    public List<EmsAudit> loadAllFixtureAuditlogs() {
        return emsAuditDao.loadAllFixtureAuditlogs();
    }

    public List<EmsAudit> loadAllGatewayAuditlogs() {
        return emsAuditDao.loadAllGatewayAuditlogs();
    }

    public void insertAuditRecord(long txnId, Long deviceId, String deviceName, int deviceType, int msgType) {
        emsAuditDao.insertAuditRecord(txnId, deviceId, deviceName, deviceType, msgType);
    }

    public void updateAuditRecord(long txnId, int attempts, int status) {
        emsAuditDao.updateAuditRecord(txnId, attempts, status);
    }

    public void updateAuditRecord(long txnId, long deviceId, int attempts, int status) {
        emsAuditDao.updateAuditRecord(txnId, deviceId, attempts, status);
    }

    public void updateAuditRecord(long txnId, long deviceId, int attempts, String status) {
        emsAuditDao.updateAuditRecord(txnId, deviceId, attempts, status);
    }

    public EmsAudit insertAuditRecord(long txnId, int deviceType, Date oStartDate, Date oEndDate) {
        return emsAuditDao.insertAuditRecord(txnId, deviceType, oStartDate, oEndDate);
    }

    public EmsAudit getAuditRecord(Date fromDate) {
        return emsAuditDao.getAuditRecord(fromDate);
    }

    public void updateDRStatus(Date currentTime) {
        emsAuditDao.updateDRStatus(currentTime);
    }
}
