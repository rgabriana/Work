package com.ems.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.LdapSettings;
import com.ems.model.SystemConfiguration;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsUnkownSecurityException;
import com.ems.security.exception.FacilityNotAssignedException;
import com.ems.security.exception.SSLCertificateNotFoundException;
import com.ems.security.util.PasswordUtils;
import com.ems.server.ServerMain;
import com.ems.service.FacilityTreeManager;
import com.ems.service.LdapAuthenticationManager;
import com.ems.service.LdapSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.AuthenticationType;
import com.ems.types.RoleType;
import com.ems.types.TenantStatus;
import com.ems.types.UserAuditActionType;
import com.ems.types.UserStatus;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;

@Transactional(propagation = Propagation.REQUIRED)
public class EmsauthenticationProvider implements AuthenticationProvider {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource
	UserManager userManager;

	@Resource
	PasswordEncoder passwordEncoder;
	
	@Resource
	PasswordEncoder shaPasswordEncoder;

	@Resource
	SystemConfigurationManager systemConfigurationManager;

	@Resource
	FacilityTreeManager facilityTreeManager;
	@Resource
	LdapSettingsManager ldapSettingsManager;
	@Resource
	LdapAuthenticationManager ldapAuthenticationManager;
	String ldapEmail;
	
	@Autowired
	private HttpServletRequest request;


	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		
		String ipAddress = null;
		if(request != null) {
			ipAddress = request.getHeader("x-forwarded-for");
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
			}
		}
		if (ipAddress == null) {
			ipAddress = "";
		}
		
		AuthenticationType authenticationType = AuthenticationType.DATABASE;
		ArrayList<LdapSettings> ldapSettings = new ArrayList<LdapSettings>();

		SystemConfiguration authTypeConfig = systemConfigurationManager
				.loadConfigByName("auth.auth_type");
		if (authTypeConfig != null) {
			authenticationType = AuthenticationType.valueOf(authTypeConfig
					.getValue());
		}

		User user = null;

		if (authenticationType == AuthenticationType.LDAP
				&& (!"admin".equals(authentication.getName()))) {
			// Authenticate against Ldap
			ldapSettings.add(ldapSettingsManager.loadById(1l));
			if (!authenticateAgainstLdap(authentication, ldapSettings)) {

				throw new BadCredentialsException("Credential is not correct");
			}
			
			if (ldapEmail != null) {
				user = userManager.loadUserByUserName(ldapEmail);
			
			} else {
				throw new BadCredentialsException(
						" Mail id not found on Ldap server.");
			}
		} else {
			// Authenticate against Database
			user = userManager.loadUserByUserName(authentication.getName());
			if (user != null) {
				
				String storedPassword = null;
				
				String encodedPassword = null;
				
				Boolean authStatus = false;
						
				if("admin".equals(user.getEmail())) {
					
					storedPassword = null;
					//salt encoded password to be used for "admin" for which authentication is against text file
					encodedPassword = null;
					
					try {
						storedPassword = PasswordUtils.extractPassword()[0];
						encodedPassword = PasswordUtils.generateDigest(authentication.getCredentials().toString());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
									
				
					authStatus = false;
				
					while ((storedPassword) != null)  {
							authStatus = storedPassword.trim().equals(encodedPassword);
							break;
					}
					
					if (!authStatus) {
						throw new BadCredentialsException("Password is not correct");
					}
				}
				else if(RoleType.Admin == user.getRole().getRoleType()){
					storedPassword = null;
					//salt encoded password to be used for "admin" for which authentication is against text file
					encodedPassword = null;
					
					try {
						storedPassword = PasswordUtils.extractAdminTypeUserPassword(user.getEmail())[0];
						encodedPassword = PasswordUtils.generateAdminTypeUserDigest(authentication.getCredentials().toString(),user.getEmail());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
									
				
					authStatus = false;
				
					while ((storedPassword) != null)  {
							authStatus = storedPassword.trim().equals(encodedPassword);
							break;
					}
					
					if (!authStatus) {
						throw new BadCredentialsException("Password is not correct");
					}
				}
				else {
					
					encodedPassword = passwordEncoder.encodePassword(
							authentication.getCredentials().toString(), null);
					if (!encodedPassword.equals(user.getPassword())) {
						throw new BadCredentialsException("Password is not correct");
					}
				}

			} else {
				throw new BadCredentialsException("Credential is not correct");
			}
		}

		// Let's check if the user belongs to a tenant and tenant is not active
		// or
		// do not have any facility assigned.
		if (user != null) {

			if (user.getStatus() == UserStatus.INACTIVE) {
				throw new DisabledException("Account is inactive");
			}

			if (user.getTenant() != null) {
				Tenant tenant = user.getTenant();

				if (tenant.getStatus() == TenantStatus.INACTIVE) {
					throw new DisabledException(
							"Tenant is inactive. Please contact Administrator");
				}
			}

			if (user.getRole().getRoleType() != RoleType.Admin) {
				if (facilityTreeManager
						.loadFacilityHierarchyForUser(user.getId())
						.getTreeNodeList().isEmpty()) {
					throw new FacilityNotAssignedException(
							"No facility assigned to User. Please contact Administrator");
				}
			}
			
		} else {
			throw new AuthenticationCredentialsNotFoundException(
					"User not Found");
		}

		Authentication authenticated = null;

		EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(user);
		authenticated = new UsernamePasswordAuthenticationToken(
				authenticatedUser, null, authenticatedUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authenticated);

		userAuditLoggerUtil.log("User " + user.getEmail() + " logged in",
				UserAuditActionType.Login.getName(), ipAddress);
		return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		return true;
	}

	private boolean authenticateAgainstLdap(Authentication authentication,
			ArrayList<LdapSettings> ldapSettings) {
		try{
		Iterator<LdapSettings> serverListIterator = ldapSettings.iterator();
		String host;
		int port;
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		String passwordEncryptionType;
		int version = LDAPConnection.LDAP_V3;
		String baseDn = null;
		// Full qualified dn name pass with the bind fuction to get
		// authenticated. This is generated from other parameter
		// we acquire from LdapSetting.
		ArrayList<String> baseDnWithUserName = new ArrayList<String>();
		// Used when anonymous access is not granted by ldap server. This is
		// mostly needed with AD server
		// to get "cn" value which need to be binded with the authentication
		// call in AD server.
		String anonymousBaseDn;
		String anonymousPassword;
		LDAPConnection cnConn = null;

		while (serverListIterator.hasNext()) {
			LdapSettings ldapObject = serverListIterator.next();
			host = ldapObject.getServer();
			port = ldapObject.getPort();
			passwordEncryptionType = ldapObject.getPasswordEncrypType();
			anonymousBaseDn = ldapObject.getNonAnonymousDn();
			anonymousPassword = ldapObject.getNonAnonymousPassword();
			baseDn = ldapObject.getBaseDns();

			// Prepare dn for authentication depending on the server we are
			// communicating with (AD/Apache)
			if (ldapObject.getUserAttribute().equals("sAMAccountName")|| ldapObject.getUserAttribute().equals("uid")) {
				// If SSL enabled we need the Ldap connection object to be able
				// to handle SSL connection with AD server
				if (ldapObject.isTls()) {
					/* A JSSE Security provider must be configured */
					Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
					// check for certificate being present.
					File certifcateFile = new File(ServerMain.getInstance()
							.getTomcatLocation() + "../../Enlighted/cacerts");
					if (!certifcateFile.exists()) {
						throw new SSLCertificateNotFoundException(
								"LDAP SSL Certiicate not found. Please contact Administrator.");
					}
					// set the certificate to truststore.
					System.setProperty("javax.net.ssl.trustStore", ServerMain
							.getInstance().getTomcatLocation()
							+ "../../Enlighted/cacerts");
					// Set the socket factory for this SSL connection only
					LDAPJSSESecureSocketFactory ssf = new LDAPJSSESecureSocketFactory();
					cnConn = new LDAPConnection(ssf);
				} else {
					// if not ssl use simple Ldap connection
					cnConn = new LDAPConnection();
				}
				// collect cn and email address for person who wants to login.
				String temp;
				String cnValue = ldapAuthenticationManager.searchCn(version,
						cnConn, host, port, username, baseDn,
						ldapObject.getUserAttribute(), anonymousBaseDn,
						anonymousPassword, ldapObject.isAllowAnonymous());
				ldapEmail = ldapAuthenticationManager.getLdapUserEmail(version,
						cnConn, host, port, username, baseDn,
						ldapObject.getUserAttribute(), anonymousBaseDn,
						anonymousPassword, ldapObject.isAllowAnonymous());
				temp = "cn=" + cnValue + ",";
				String baseTemp[] = baseDn.split(":");
				for (int i = 0; i < baseTemp.length; i++)
					baseDnWithUserName.add(temp + baseTemp[i]);
			} else {
				throw new DisabledException(
						"User Attribute in Ldap Setting page is not correct contact your Admin");
			}

			// we need to split multiple base dns and make multiple dn to
			// authenticate against it.
			Iterator<String> it = baseDnWithUserName.iterator();
			while (it.hasNext()) {
				String name = it.next();
				// Check whether it is SSL or simple communication . Communicate
				// accordingly
				if (ldapObject.isTls()) {
					if (!ldapAuthenticationManager.SSLBindAuthentication(
							version, cnConn, host, port, name, password)) {
						continue;
					} else
						return true;
				} else {
					LDAPConnection simpleConn = new LDAPConnection();
					if (!ldapAuthenticationManager.simpleBindAuthentication(
							version, simpleConn, host, port, name, password)) {
						continue;
					} else
						return true;
				}
			}
		}
		}
		catch(Exception ex)
		{   ex.printStackTrace();
			throw new EmsUnkownSecurityException(
					"Unknown Security Exception. Please Contact Administrator.");
		}
		return false;
	}

}
