package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.TenantDao;
import com.ems.model.Tenant;

@Service("tenantManager")
@Transactional(propagation = Propagation.REQUIRED)
public class TenantManager {

    @Resource
    private TenantDao tenantDao;

    public List<Tenant> getAllTenants() {
        return tenantDao.getAllTenants();
    }

    public void save(Tenant tenant) {
        tenantDao.save(tenant);
    }

    public Tenant get(Long tenantId) {
        return (Tenant) tenantDao.getObject(Tenant.class, tenantId);
    }

}
