package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("systemCleanUpDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemCleanUpDao extends BaseDaoHibernate { 

}
