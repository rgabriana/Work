package com.enlightedportal.utils;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.enlightedportal.model.UserAudit;
import com.enlightedportal.service.UserAuditManager;
import com.enlightedportal.types.UserAuditActionType;

@Component("userAuditLoggerUtil")
public class UserAuditLoggerUtil {

	@Resource
	private UserAuditManager userAuditManager;

	public void log(UserAuditActionType actionType, String message) {
		String ipAddress = null;
		try {
			ipAddress = ((WebAuthenticationDetails) SecurityContextHolder.getContext()
					.getAuthentication().getDetails()).getRemoteAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(ipAddress==null)
		{
			ipAddress="" ;
		}
		UserAudit userAudit = new UserAudit();
		userAudit.setLogTime(new Date());
		userAudit.setActionType(actionType.getName());
		userAudit.setUserName("Admin");
		userAudit.setIpAddress(ipAddress);
		userAudit.setDescription(message);
		userAuditManager.save(userAudit);
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
