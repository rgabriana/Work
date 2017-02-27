package com.ems.mvc.util;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ems.model.EmsUserAudit;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.EmsUserAuditManager;

@Component("userAuditLoggerUtil")
public class UserAuditLoggerUtil {
	
	@Resource
	EmsAuthenticationContext emsAuthContext;
	@Resource
	EmsUserAuditManager emsUserAuditManager;
	
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
			EmsUserAudit emsUserAudit = new EmsUserAudit(null, emsAuthContext.getUserId(), emsAuthContext.getUserName(),
														message, actionType, new Date(), ip);
			emsUserAuditManager.save(emsUserAudit);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String message, String actionType, String ipAddress){
		try {
			EmsUserAudit emsUserAudit = new EmsUserAudit(null, emsAuthContext.getUserId(), emsAuthContext.getUserName(),
														message, actionType, new Date(), ipAddress);
			emsUserAuditManager.save(emsUserAudit);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String message, String actionType, String ipAddress, String userName, Long userId){
		try {
			EmsUserAudit emsUserAudit = new EmsUserAudit(null, userId, userName,
														message, actionType, new Date(), ipAddress);
			emsUserAuditManager.save(emsUserAudit);
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
