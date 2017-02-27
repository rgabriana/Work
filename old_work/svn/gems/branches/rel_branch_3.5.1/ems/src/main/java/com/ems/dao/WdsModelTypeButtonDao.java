package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("wdsModelTypeButtonDao")
@Transactional(propagation = Propagation.REQUIRED)
public class WdsModelTypeButtonDao extends BaseDaoHibernate {

}
