package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ApplicationConfigurationDao;
import com.ems.dao.SystemCleanUpDao;

@Service("systemCleanUpManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemCleanUpManager {

	
	
	@Resource
    private SystemCleanUpDao systemCleanUpDao;
	
	@Resource
	private FixtureManager fixtureManager;
	
	
	
	
	public void resetAllFixtureGroupSyncFlag()
	{
		fixtureManager.resetAllFixtureGroupSyncFlag() ;
	}
}
