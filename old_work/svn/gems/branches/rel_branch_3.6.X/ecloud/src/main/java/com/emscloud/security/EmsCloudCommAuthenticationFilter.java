package com.emscloud.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import com.communication.template.CloudConnectionTemplate;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.SystemConfigurationManager;

@SuppressWarnings("deprecation")
public class EmsCloudCommAuthenticationFilter extends AnonymousAuthenticationFilter{
	
	@Resource
	EmInstanceManager		emInstanceManager;
	@Resource
	SystemConfigurationManager sysConfigManager;
	
	private Boolean cloudMode = null;
	
	private Authentication authenticated = null;
	
	@Override
	protected Authentication createAuthentication(HttpServletRequest request) {
		Authentication auth = super.createAuthentication(request);
		boolean addRole = false;
		
		if(cloudMode == null) {
			SystemConfiguration sysConfig = sysConfigManager.loadConfigByName("cloud.mode");
			if(sysConfig != null) {
				cloudMode = "true".equals(sysConfig.getValue());
			}
		}
		if(!cloudMode) {
			String mac = request.getHeader("macid");
			String key = request.getHeader("key");
			if(mac != null && key != null) {
				try {
					String secretKey = emInstanceManager.loadKeyByMac(mac);
					if(secretKey != null && CloudConnectionTemplate.getSSLAuthKey(secretKey, mac).equals(key)) {
						addRole = true;
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		else {
			addRole = true;
		}
			
		if(addRole) {
			if(authenticated == null ) {
				GrantedAuthority role = new GrantedAuthorityImpl("ROLE_USER");
				List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
				roles.add(role);
				authenticated = new AnonymousAuthenticationToken("enlightedKey", "mac", roles);
			}
			auth = authenticated;
		}
		return auth;
	}
	

}