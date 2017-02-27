package com.emscloud.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.PlanMapDao;
import com.emscloud.model.PlanMap;



@Service("planMapManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlanMapManager {
	
	@Resource
	PlanMapDao planMapDao;
	
	public PlanMap getPlanById(Long plan_id) {
		return planMapDao.getPlanById(plan_id);
	}
	
	public PlanMap saveOrUpdatePlanMap(PlanMap planMap) {
		return (PlanMap)planMapDao.saveObject(planMap);
	}
	
	

}
