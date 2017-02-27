package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.PlanMap;

@Repository("planMapDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlanMapDao extends BaseDaoHibernate {

    public PlanMap loadPlanMapById(Long id) {
        Criteria criteria = getSession().createCriteria(PlanMap.class);
        criteria.add(Restrictions.eq("id", id));
        List<PlanMap> list = criteria.list();
        if (list != null) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
