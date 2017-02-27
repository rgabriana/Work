package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("buttonMapDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ButtonMapDao extends BaseDaoHibernate {

}
