package com.ems.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.ButtonManipulation;

@Repository("buttonManipulationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ButtonManipulationDao extends BaseDaoHibernate {
	
	public ButtonManipulation getButtonManipulationByButtonMapId(Long id) {
        Session session = getSession();
        ButtonManipulation buttonManipulation = (ButtonManipulation) session.createCriteria(ButtonManipulation.class).add(Restrictions.eq("buttonMapId", id)).
        		setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
        return buttonManipulation;
	}
}
