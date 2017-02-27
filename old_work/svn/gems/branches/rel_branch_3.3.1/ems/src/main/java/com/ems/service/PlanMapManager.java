package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.PlanMapDao;
import com.ems.model.PlanMap;

/**
 * 
 * @author Abhishek Sinha
 * 
 */
@Service("planMapManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlanMapManager {

    @Resource
    private PlanMapDao planMapDao;

    public PlanMap save(PlanMap planMap) {
        if (planMap.getId() != null && planMap.getId() == 0) {
            planMap.setId(null);
        }
        return (PlanMap) planMapDao.saveObject(planMap);
    }

    public PlanMap update(PlanMap planMap) {
        return (PlanMap) planMapDao.saveObject(planMap);
    }

    public PlanMap loadPlanMapById(Long id) {
        return planMapDao.loadPlanMapById(id);
    }
}
