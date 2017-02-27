package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.TenantLocations;

@Repository("tenantLocationsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class TenantLocationsDao extends BaseDaoHibernate {

    public void save(TenantLocations tenantLocations) {
        this.saveObject(tenantLocations);
    }

}
