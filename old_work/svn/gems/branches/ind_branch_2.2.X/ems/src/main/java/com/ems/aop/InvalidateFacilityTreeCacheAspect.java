package com.ems.aop;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import com.ems.service.FacilityTreeManager;

/**
 * Aspect is weaved around the methods marked with InvalidateFacilityTreeCache annotation and results in cleaning of the
 * facility tree cache.
 * 
 * @author lalit
 * 
 */
@Aspect
public class InvalidateFacilityTreeCacheAspect {

    @Resource
    FacilityTreeManager facilityTreeManager;

    @After("@annotation(com.ems.annotaion.InvalidateFacilityTreeCache)")
    public void refreshTreeCache() {
        facilityTreeManager.inValidateFacilitiesTreeCache();
    }
}
