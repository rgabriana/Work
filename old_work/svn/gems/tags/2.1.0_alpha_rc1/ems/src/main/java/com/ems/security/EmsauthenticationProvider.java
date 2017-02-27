package com.ems.security;

import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.AuthenticationType;
import com.ems.types.RoleType;
import com.ems.types.UserStatus;

public class EmsauthenticationProvider implements AuthenticationProvider {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource
	UserManager userManager;

	@Resource
	PasswordEncoder passwordEncoder;

	@Resource
	SystemConfigurationManager systemConfigurationManager;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		AuthenticationType authenticationType = AuthenticationType.DATABASE;

		String ldapUrl = null;
		String ldapAuthType = "simple";
		String ldapSecurityPrincipal = null;
		boolean allowNonEmsLdapUser = false;

		SystemConfiguration authTypeConfig = systemConfigurationManager
				.loadConfigByName("auth.auth_type");
		if (authTypeConfig != null) {
			authenticationType = AuthenticationType.valueOf(authTypeConfig
					.getValue());
		}

		if (authenticationType == AuthenticationType.LDAP) {
			SystemConfiguration ldapUrlConfig = systemConfigurationManager
					.loadConfigByName("auth.ldap_url");
			if (ldapUrlConfig != null) {
				ldapUrl = ldapUrlConfig.getValue();
			}

			SystemConfiguration ldapAuthTypeConfig = systemConfigurationManager
					.loadConfigByName("auth.ldap_auth_type");
			if (ldapAuthTypeConfig != null) {
				ldapAuthType = ldapAuthTypeConfig.getValue();
			}

			SystemConfiguration ldapSecurityPrincipalConfig = systemConfigurationManager
					.loadConfigByName("auth.ldap_security_principal");
			if (ldapSecurityPrincipalConfig != null) {
				ldapSecurityPrincipal = ldapSecurityPrincipalConfig.getValue();
			}

			SystemConfiguration allowNonEmsLdapUserConfig = systemConfigurationManager
					.loadConfigByName("auth.ldap_allow_non_ems_users");
			if (allowNonEmsLdapUserConfig != null) {
				if ("true".equals(allowNonEmsLdapUserConfig.getValue())) {
					allowNonEmsLdapUser = true;
				}
			}
		}
		
		
		User user = userManager.loadUserByUserName(authentication.getName());
		Authentication authenticated = null;

		// If we are in LDAP mode and we want to allow the non ems user, then
		// let's create a user here
		if (user == null && authenticationType == AuthenticationType.LDAP
				&& allowNonEmsLdapUser) {
			user = new User();
			user.setEmail(authentication.getName());
			user.setRole(userManager.getRole(RoleType.Employee));
			user.setStatus(UserStatus.ACTIVE);
		}

		if (user != null) {

			if (user.getStatus() == UserStatus.INACTIVE) {
				throw new DisabledException("Account is inactive");
			}

			if (authenticationType == AuthenticationType.LDAP
					&& (!"admin".equals(authentication.getName()))) {
				authenticateAgainstLdap(authentication, ldapUrl, ldapAuthType,
						ldapSecurityPrincipal);
			} else {
				String encodedPassword = passwordEncoder.encodePassword(
						authentication.getCredentials().toString(), null);
				if (!encodedPassword.equals(user.getPassword())) {
					throw new BadCredentialsException("Password is not correct");
				}
			}
			EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
					user);
			authenticated = new UsernamePasswordAuthenticationToken(
					authenticatedUser, null, authenticatedUser.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authenticated);
		} else {
			throw new AuthenticationCredentialsNotFoundException(
					"User not Found");
		}
		userAuditLoggerUtil.log("User logged in: " + user.getEmail());
		return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		return true;
	}

	private void authenticateAgainstLdap(Authentication authentication,
			String ldapUrl, String ldapAuthType, String ldapSecurityPrincipal) {

		Hashtable env = new Hashtable(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, ldapAuthType);
		String securityPrincipal = ldapSecurityPrincipal.replace("{0}",
				authentication.getName());
		env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
		env.put(Context.SECURITY_CREDENTIALS, authentication.getCredentials()
				.toString());

		DirContext ctx = null;

		try {
			ctx = new InitialDirContext(env);
			ctx.close();
		} catch (NamingException e) {
			/**
			 * For providing the right message, the right exception need to be
			 * thrown here.
			 */
			throw new BadCredentialsException("could not connect using LDAP");
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (Exception ex) {

			}
		}

	}
}
