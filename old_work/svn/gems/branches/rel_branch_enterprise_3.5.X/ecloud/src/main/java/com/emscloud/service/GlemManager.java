package com.emscloud.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.adaptor.Adapter;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.types.GlemModeType;

@Service("glemManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GlemManager {

	@Resource(name = "gemAdapter")
	Adapter gemAdapter;

	@Resource(name = "emAdapter")
	Adapter emAdapter;

	@Resource
	SystemConfigurationManager sysConfigManager;

	public GlemManager() {
		
	}
	
	public int getGLEMMode() {
		SystemConfiguration sysConfig = sysConfigManager.loadConfigByName("cloud.mode");
		if(sysConfig != null) {
			if ("true".equals(sysConfig.getValue())) {
				// ecloud mode
				return GlemModeType.ECLOUD.getMode();
			}
		}
		// Direct EM mode
		return GlemModeType.UEM.getMode();
	}
	
	public Adapter getAdapter() {
		if (getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			return gemAdapter;
		} else {
			return emAdapter;
		}
	}


}
