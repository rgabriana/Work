package com.ems.mvc.util;

import java.util.Date;

import javax.annotation.Resource;

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
		EmsUserAudit emsUserAudit = new EmsUserAudit(null, emsAuthContext.getUserId(), emsAuthContext.getUserName(),
													message, actionType, new Date());
		emsUserAuditManager.save(emsUserAudit); 
	}
}
