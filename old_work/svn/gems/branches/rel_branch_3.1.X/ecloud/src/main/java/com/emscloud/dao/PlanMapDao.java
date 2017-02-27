/**
 * 
 */
package com.emscloud.dao;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.PlanMap;



/**
 * @author Admin
 *
 */
@Repository("planmapDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class PlanMapDao  extends BaseDaoHibernate{
	
    public PlanMap getPlanById(Long plan_id) {
            String hsql = "from PlanMap pm where pm.id=?";
            Query q = this.getSession().createQuery(hsql.toString());
            q.setParameter(0, plan_id);
            if (q.list() == null || q.list().size() == 0) return null;
            return (PlanMap)q.list().get(0);
    }
}
