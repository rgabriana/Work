package com.emscloud.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.EmInstance;



@Service("certificateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CertificateManager {
	
	@Resource
	EmInstanceManager emInstanceManager;
	
	public String getTSLocation()
	{		
		return "/etc/enlighted/CA/ssl/pfx/enlighted.ts";
	}
	
	public String getKSLocation(Long emInstId){
		
		String ksPath = "/etc/enlighted/CA/ssl/pfx/";
		
		EmInstance emInst = emInstanceManager.loadEmInstanceById(emInstId);
		String ksFileName = "em_" + emInst.getCustomer().getId() + "_" +  emInstId + ".pfx";		
		String pathWithFileName = ksPath + ksFileName;
		
		return pathWithFileName;
	}
	
public String getKSFileName(Long emInstId){
		
		EmInstance emInst = emInstanceManager.loadEmInstanceById(emInstId);
		String ksFileName = "em_" + emInst.getCustomer().getId() + "_" +  emInstId + ".pfx";		
				
		return ksFileName;
	}
}
