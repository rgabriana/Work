package com.emscloud.aop;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import com.emscloud.service.FacilityTreeManager;

/**
 * Aspect is weaved around the methods marked with InvalidateFacilityTreeCache annotation and results in cleaning of the
 * facility tree cache.
 *  
 */
@Aspect
public class InvalidateFacilityTreeCacheAspect {

    @Resource
    FacilityTreeManager facilityTreeManager;

    @After("@annotation(com.emscloud.annotation.InvalidateFacilityTreeCache)")
    public void refreshTreeCache() {
        facilityTreeManager.clearCache();
    }
}
