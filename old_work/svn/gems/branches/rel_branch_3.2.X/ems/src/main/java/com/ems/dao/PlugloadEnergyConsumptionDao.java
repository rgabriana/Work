package com.ems.dao;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("plugloadEnergyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadEnergyConsumptionDao extends BaseDaoHibernate{
	
	
	
}
