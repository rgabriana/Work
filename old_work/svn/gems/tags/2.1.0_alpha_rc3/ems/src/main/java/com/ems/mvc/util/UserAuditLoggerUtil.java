package com.ems.mvc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ems.security.EmsAuthenticationContext;

@Component("userAuditLoggerUtil")
public class UserAuditLoggerUtil {

	private static Logger userAuditLogger = Logger.getLogger("UserAuditLog");
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss z");
	
	@Resource
	EmsAuthenticationContext emsAuthContext;
	
	public void log(String message){
		Date now = new Date();
		String currentTime = format.format(now);
		String logMessage = emsAuthContext.getUserName() +"(" +emsAuthContext.getUserId() + ")" +"," + currentTime + "," + message;
		userAuditLogger.info(logMessage);
	}
}
