package com.emscloud.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import com.emscloud.api.util.Request;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.SystemConfigurationManager;

@SuppressWarnings("deprecation")
public class ApiAuthenticationFilter extends AnonymousAuthenticationFilter{
	
	@Resource
	EmInstanceManager		emInstanceManager;
	@Resource
	SystemConfigurationManager sysConfigManager;
	@Resource
	Request apiRequest;
	
	Random r = new Random();
	
	private Boolean cloudMode = null;
	
	private Authentication authenticated = null;
	
	@Override
	protected Authentication createAuthentication(HttpServletRequest request) {
		Authentication auth = super.createAuthentication(request);
		boolean addRole = true;
		
		/*if(cloudMode == null) {
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
		}*/
			
		if(addRole) {
			if(authenticated == null ) {
				GrantedAuthority role = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
				//GrantedAuthority role1 = new GrantedAuthorityImpl("ROLE_USER");
				List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
				roles.add(role);
				//roles.add(role1);
				authenticated = new AnonymousAuthenticationToken("enlightedKey", "mac", roles);
			}
			apiRequest.setTransactionId(r.nextLong());
			auth = authenticated;
		}
		return auth;
	}
	

}
