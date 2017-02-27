package com.ems.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.server.ServerMain;
import com.ems.vo.MasterGemsSetting;

@Service("communicatorManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CommunicatorManager {

	

	public void setConfiguration(MasterGemsSetting masterGemsSetting) {
		CommunicatorServices communicator = CommunicatorServices.getInstance() ;
		communicator.setConfiguration(masterGemsSetting);
		
	}

}
