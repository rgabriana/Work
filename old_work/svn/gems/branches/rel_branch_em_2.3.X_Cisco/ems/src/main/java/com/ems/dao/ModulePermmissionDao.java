package com.ems.dao;

import java.util.List;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("modulePermmissionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ModulePermmissionDao extends BaseDaoHibernate {

    public List<String[]> loadModulePermissionByRoleId(Long roleId) {

        SQLQuery sqlQuery = getSession()
                .createSQLQuery(
                        " select m.name as module_name,pd.name as permission_name from module m inner join module_permission mp on m.id=mp.module_id inner join permission_details pd on mp.permission=pd.id where mp.role_id="
                                + roleId + "");
        List<String[]> list = sqlQuery.list();
        if (list != null) {
            return list;
        } else {
            return null;
        }
    }

}
