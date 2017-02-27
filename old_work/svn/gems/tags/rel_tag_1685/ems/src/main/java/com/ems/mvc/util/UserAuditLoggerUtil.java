package com.ems.mvc.util;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

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
			ip = ((WebAuthenticationDetails)SecurityContextHolder.getContext().getAuthentication().getDetails()).getRemoteAddress();
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
}
