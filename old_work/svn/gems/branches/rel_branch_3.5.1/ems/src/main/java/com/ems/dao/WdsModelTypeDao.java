package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("wdsModelTypeDao")
@Transactional(propagation = Propagation.REQUIRED)
public class WdsModelTypeDao extends BaseDaoHibernate {

}
