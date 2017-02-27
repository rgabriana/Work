package com.ems.aop;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import com.ems.service.GroupManager;

/**
 * Aspect is weaved around the methods marked with InvalidateProfileTreeCache annotation and results in cleaning of the
 * profile tree cache.
 * 
 * @author lalit
 * 
 */
@Aspect
public class InvalidateProfileTreeCacheAspect {

    @Resource
    GroupManager groupManager;

    @After("@annotation(com.ems.annotaion.InvalidateProfileTreeCache)")
    public void refreshTreeCache() {
    	groupManager.inValidateProfilesTreeCache();
    }
}
