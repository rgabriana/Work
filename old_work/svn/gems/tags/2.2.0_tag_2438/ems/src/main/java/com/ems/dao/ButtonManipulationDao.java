package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("buttonManipulationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ButtonManipulationDao extends BaseDaoHibernate {

}
