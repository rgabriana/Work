package com.emscloud.util;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.emscloud.model.CloudUserAudit;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CloudUserAuditManager;


@Component("cloudAuditLoggerUtil")
public class CloudAuditLoggerUtil {
	
	@Resource
	EmsAuthenticationContext emsAuthContext;
	
	@Resource
	CloudUserAuditManager cloudUserAuditManager;
	
	public void log(String message, String actionType){
		String ip = null;
		try {
			ip = getIp();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
	    	if(ip == null) {
	    		ip = "";
	    	}
	    	CloudUserAudit cloudUserAudit = new CloudUserAudit(null, emsAuthContext.getUserId(), emsAuthContext.getUserName(),
														message, actionType, new Date(), ip);
	    	cloudUserAuditManager.save(cloudUserAudit);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String message, String actionType, String ipAddress){
		try {
			CloudUserAudit cloudUserAudit = new CloudUserAudit(null, emsAuthContext.getUserId(), emsAuthContext.getUserName(),
														message, actionType, new Date(), ipAddress);
			cloudUserAuditManager.save(cloudUserAudit);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String message, String actionType, String ipAddress, String userName, Long userId){
		try {
			CloudUserAudit cloudUserAudit = new CloudUserAudit(null, userId, userName,
														message, actionType, new Date(), ipAddress);
			cloudUserAuditManager.save(cloudUserAudit);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getIp() {
		ServletRequestAttributes reqAttr = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		String ip = reqAttr.getRequest().getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = reqAttr.getRequest().getRemoteAddr();
		}
		return ip;
	}
}
