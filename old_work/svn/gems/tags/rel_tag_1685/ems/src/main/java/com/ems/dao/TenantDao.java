package com.ems.dao;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Tenant;

@Repository("tenantDao")
@Transactional(propagation = Propagation.REQUIRED)
public class TenantDao extends BaseDaoHibernate {

    public List<Tenant> getAllTenants() {
        List<Tenant> results = null;
        String hsql = "from Tenant t order by name";
        Query q = getSession().createQuery(hsql.toString());
        results = q.list();
        return results;
    }

    public void save(Tenant tenant) {
        this.saveObject(tenant);
    }
}
