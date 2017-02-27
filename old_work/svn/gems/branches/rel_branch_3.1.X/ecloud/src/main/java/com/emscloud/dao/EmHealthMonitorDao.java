package com.emscloud.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Repository("emHealthMonitorDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmHealthMonitorDao extends BaseDaoHibernate {

}
