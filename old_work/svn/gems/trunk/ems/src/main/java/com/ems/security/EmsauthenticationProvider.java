package com.ems.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

import com.ems.hvac.utils.CryptographyUtil;
import com.ems.model.LdapSettings;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.SystemConfiguration;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsUnkownSecurityException;
import com.ems.security.exception.FacilityNotAssignedException;
import com.ems.security.exception.ForgotPasswordException;
import com.ems.security.exception.ForgotPasswordGenericException;
import com.ems.security.exception.LoginAttemptsException;
import com.ems.security.exception.SSLCertificateNotFoundException;
import com.ems.security.util.PasswordUtils;
import com.ems.server.ServerMain;
import com.ems.service.FacilityTreeManager;
import com.ems.service.LdapAuthenticationManager;
import com.ems.service.LdapSettingsManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.AuthenticationType;
import com.ems.types.NetworkType;
import com.ems.types.RoleType;
import com.ems.types.TenantStatus;
import com.ems.types.UserAuditActionType;
import com.ems.types.UserStatus;
import com.ems.util.Constants;
import com.ems.utils.DateUtil;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;

@Transactional(propagation = Propagation.REQUIRED)
public class EmsauthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = Logger.getLogger(EmsauthenticationProvider.class);
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
	
	@Resource 
	NetworkSettingsManager networkSettingsManager;
	
	@Autowired
	private HttpServletRequest request;

	@Autowired
    protected MessageSource messageSource;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		
		String ipAddress = null;
		boolean isForgotPassRequest = false;
		User user = null;
		try {
			if(request != null) {
				ipAddress = request.getHeader("x-forwarded-for");
				if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
					ipAddress = request.getRemoteAddr();
				}
				isForgotPassRequest = !StringUtils.isEmpty(request.getParameter(Constants.FORGOT_REQ_ATTRIBUTE));
			}
			if (ipAddress == null) {
				ipAddress = "";
			}
			request.getSession().setAttribute(Constants.FORGOT_REQ_ATTRIBUTE, isForgotPassRequest);
			AuthenticationType authenticationType = AuthenticationType.DATABASE;
			ArrayList<LdapSettings> ldapSettings = new ArrayList<LdapSettings>();

			SystemConfiguration authTypeConfig = systemConfigurationManager
					.loadConfigByName("auth.auth_type");
			if (authTypeConfig != null) {
				authenticationType = AuthenticationType.valueOf(authTypeConfig
						.getValue());
			}

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
					
					if(!isForgotPassRequest && user.isUserLocked()){
						throw new LoginAttemptsException("You have exceeded max no of login attempts. Please contact Administrator.");
					}
					if(isForgotPassRequest && isToBypassRequest(authentication, user)){
						return afterSuccessfulLogin(ipAddress, user);
					}
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
							//updateLoginAttempts(user, user.getNoOfLoginAttempts() + 1);
							throw new LoginAttemptsException("Password is not correct");
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
								//updateLoginAttempts(user, user.getNoOfLoginAttempts() + 1);
								throw new LoginAttemptsException("Password is not correct");
						}
					}
					else {
						encodedPassword = passwordEncoder.encodePassword(
								authentication.getCredentials().toString(), null);
						if (!encodedPassword.equals(user.getPassword())) {
								//updateLoginAttempts(user, user.getNoOfLoginAttempts() + 1);
								throw new LoginAttemptsException("Password is not correct");
						}
					}

				} else {
					//throw new BadCredentialsException("Credential is not correct");
					throw new LoginAttemptsException("Credential is not correct");
				}
			}

			// Let's check if the user belongs to a tenant and tenant is not active
			// or
			// do not have any facility assigned.
			if (user != null) {

				if (user.getStatus() == UserStatus.INACTIVE || (user.getRole().getRoleType() == RoleType.Bacnet) ) {
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
//				throw new AuthenticationCredentialsNotFoundException(
//						"User not Found");
				throw new LoginAttemptsException("User not found");
			}

			Authentication authenticated = afterSuccessfulLogin(ipAddress, user);
			return authenticated;
		} catch (AuthenticationException e) {
			//Delay the response as this is unsuccessful login attempt..
			final String defaultDelayInMillSec = messageSource.getMessage(Constants.DEFAULT_DELAY_LOGIN_UNSUCCESS,
					null, LocaleContextHolder.getLocale());
			Long defaultDelayTimeInMillis = Long.parseLong(defaultDelayInMillSec); 
			try {
				Thread.sleep(defaultDelayTimeInMillis);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(user != null && ( user.isAdmin() || user.isSuperAdmin()) && (user.getNoOfLoginAttempts() >= (Constants.MAX_LOGIN_ATTEMPTS - 1)) ){
				defaultDelayTimeInMillis = ((Double)(Math.pow(defaultDelayTimeInMillis/1000,user.getNoOfLoginAttempts() + 1 ))).longValue();
				request.getSession().setAttribute(Constants.DEFAULT_DELAY_ADMIN_ATTRIB, defaultDelayTimeInMillis);
			}
			if(isForgotPassRequest){
				if(user == null){
					request.getSession().removeAttribute("userId");
					throw e;
				}else{
					request.getSession().setAttribute("userId", user.getId());
					if(e instanceof ForgotPasswordException){
						throw new ForgotPasswordException("",e);
					}else{
						throw new ForgotPasswordGenericException("",e);
					}
				}
			}
			
			if(user == null){
				request.getSession().removeAttribute("userId");
				throw e;
			}else if(( e instanceof FacilityNotAssignedException || e instanceof DisabledException)){
				request.getSession().setAttribute("userId", user.getId());
				throw e;
			}else if(!user.isUserLocked()){
				request.getSession().setAttribute("userId", user.getId());
				throw new LoginAttemptsException("Incorrect Login");
			}else{
				request.getSession().setAttribute("userId", user.getId());	
			}
			throw e;
		}
	}

	private boolean isToBypassRequest(Authentication authentication, User user) {
		String tempPassword = null;
		String keyGenTime = null;
		String keyGenTimeZone = null;
		//Check if the request is after forgot password by checking the column value identifier_forgot_password with null
		final boolean isForgotPasswordLoginRequest = !StringUtils.isEmpty(user.getForgotPasswordIdentifier()); 
		if(isForgotPasswordLoginRequest){
			//Check the password from the supp key - extract time t2 and match it with the identifier
			String inputPassword = authentication.getCredentials().toString();
			try {
				inputPassword = CryptographyUtil.getDecryptedString(Constants.EM_PVT_FILE, inputPassword);
				final String[] passWithDate = inputPassword.split(Constants.FORGOT_PASS_TEMP_SPLITTER);
				tempPassword = passWithDate[0];
				final String keyGenString[] = passWithDate[1].split(Constants.FORGOT_PASS_TIMEZONE_SPLITTER);
				keyGenTime = keyGenString[0];
				keyGenTimeZone = keyGenString[1];
				if(StringUtils.isEmpty(keyGenTime) || StringUtils.isEmpty(keyGenTimeZone)){
					throw new Exception("ERROR occured after password retrieved from support. Please contact Administrator");
				}
				
				String nimCorporate = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.Corporate.getName());
				String corporateMapping=Constants.NETWK_INTERFACE_ETH0;
				if(nimCorporate != null){
					corporateMapping = nimCorporate;
				}				
				String storedPassword = user.getForgotPasswordIdentifier()+  ServerMain.getInstance().getMacAddressByInterfaceName(corporateMapping);
				
				if(storedPassword.equals(tempPassword)){
					final Date d = new Date();
					final SimpleDateFormat formatterDateFormat = new SimpleDateFormat(Constants.FORGOT_DATE_PATTERN);
					final String referenceDateStr = DateUtil.getTimeInSpecifiedTimeZone(formatterDateFormat.format(d), TimeZone.getDefault().getID(), TimeZone.getDefault().getID(), Constants.FORGOT_DATE_PATTERN);
					//add one day to keyGenTime and check it with the current time. if it is less than referenceDateStr ie.e. result is -1 then raise error password expired
					final Date keyGenDate = formatterDateFormat.parse(keyGenTime);
					final Date dateToChk = DateUtil.addDays(keyGenDate, 1);
					final String dateToChkStr = DateUtil.getTimeInSpecifiedTimeZone(formatterDateFormat.format(dateToChk), keyGenTimeZone, keyGenTimeZone, Constants.FORGOT_DATE_PATTERN);
					final int result = DateUtil.compareDate(dateToChkStr, keyGenTimeZone, referenceDateStr, TimeZone.getDefault().getID(), Constants.FORGOT_DATE_PATTERN);
					if(result  < 0){
						final String expiredMsg = messageSource.getMessage(Constants.FORGOT_PASS_EXPIRED,
								null, LocaleContextHolder.getLocale());
						throw new ForgotPasswordException(expiredMsg);
					}
					return true;
				}else{
					if(user.isUserLocked()){
						throw new LoginAttemptsException("You have exceeded max no of login attempts. Please contact Administrator.");
					}else{
						return false;
					}
				}
				
			} catch (Exception e) {
				logger.error("ERROR OCCURED DURING LOGIN:", e);
				if( !(e instanceof ForgotPasswordException) ){
					throw new LoginAttemptsException("Bad credentials. Please contact Administrator.");
				}else{
					throw new ForgotPasswordException("",e);
				}
			}
		}
		return false;
	}

	private Authentication afterSuccessfulLogin(String ipAddress, User user) {
		Authentication authenticated = null;

		EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(user);
		authenticated = new UsernamePasswordAuthenticationToken(
				authenticatedUser, null, authenticatedUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authenticated);

		//Update user table.. Reset the noOfLoginAttempts
		updateLoginAttempts(user, 0);
		userAuditLoggerUtil.log("User " + user.getEmail() + " logged in",
				UserAuditActionType.Login.getName(), ipAddress);
		return authenticated;
	}
	
	private void updateLoginAttempts(final User user, final long noOfLoginAttempts){
		user.setNoOfLoginAttempts(noOfLoginAttempts);
		user.setUnlockTime(null);
		//userManager.save(user);
		//userManager.updateUserDetailsInNewTransaction(Constants.NO_LOGIN_ATTEMPT_COLUMN, String.valueOf(noOfLoginAttempts), String.valueOf(user.getId()));
		//userManager.updateLoginAttempts(noOfLoginAttempts, user.getId());
		userManager.save(user);
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
