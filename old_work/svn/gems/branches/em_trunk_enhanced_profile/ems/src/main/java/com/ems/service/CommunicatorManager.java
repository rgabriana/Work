package com.ems.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.vo.MasterGemsSetting;

@Service("communicatorManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CommunicatorManager {

	

	public void setConfiguration(MasterGemsSetting masterGemsSetting) {
		CommunicatorServices communicator = CommunicatorServices.getInstance() ;
		communicator.setConfiguration(masterGemsSetting);
		
	}
	public MasterGemsSetting loadConfiguration()
	{
		CommunicatorServices communicator = CommunicatorServices.getInstance() ;
		return communicator.loadConfiguration();
	}
}
