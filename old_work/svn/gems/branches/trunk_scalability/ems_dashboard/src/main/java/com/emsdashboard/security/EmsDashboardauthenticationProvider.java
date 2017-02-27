package com.emsdashboard.security;

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


import com.emsdashboard.model.User;

import com.emsdashboard.service.UserManager;
import com.emsdashboard.types.AuthenticationType;
import com.emsdashboard.types.RoleType;
import com.emsdashboard.types.Status;





public class EmsDashboardauthenticationProvider implements AuthenticationProvider {
	 @Resource
	    UserManager userManager;

	    @Resource
	    PasswordEncoder passwordEncoder;

	 

	    private AuthenticationType authenticationType = AuthenticationType.DATABASE;

	    private String ldapUrl = null;
	    private String ldapAuthType = "simple";
	    private String ldapSecurityPrincipal = null;
	    private boolean allowNonEmsLdapUser = false;


	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		 User user = userManager.loadUserByUserName(authentication.getName());
        Authentication authenticated = null;

        // If we are in LDAP mode and we want to allow the non ems user, then let's create a user here
        if (user == null && authenticationType == AuthenticationType.LDAP && allowNonEmsLdapUser) {
            user = new User();
            user.setEmail(authentication.getName());
            user.setStatus(Status.A);
        }

        if (user != null) {

            if (user.getStatus() == Status.I) {
                throw new DisabledException("Account is inactive");
            }

            if (authenticationType == AuthenticationType.LDAP && (!"admin".equals(authentication.getName()))) {
                authenticateAgainstLdap(authentication);
            } else {
                String encodedPassword = passwordEncoder.encodePassword(authentication.getCredentials().toString(),
                        null);
                if (!encodedPassword.equals(user.getPassword())) {
                    throw new BadCredentialsException("Password is not correct");
                }
            }
            EmsDashBoardAuthenticatedUser authenticatedUser = new  EmsDashBoardAuthenticatedUser(user);
            authenticated = new UsernamePasswordAuthenticationToken(authenticatedUser, null,
                    authenticatedUser.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticated);
        } else {
            throw new AuthenticationCredentialsNotFoundException("User not Found");
        }
        return authenticated;
	}

	@Override
	public boolean supports(Class<? extends Object> arg0) {
		// TODO Auto-generated method stub
		return true;
	}
	 private void authenticateAgainstLdap(Authentication authentication) {

	        Hashtable env = new Hashtable(11);
	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, ldapUrl);
	        env.put(Context.SECURITY_AUTHENTICATION, this.ldapAuthType);
	        String securityPrincipal = ldapSecurityPrincipal.replace("{0}", authentication.getName());
	        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
	        env.put(Context.SECURITY_CREDENTIALS, authentication.getCredentials().toString());

	        DirContext ctx = null;

	        try {
	            ctx = new InitialDirContext(env);
	            ctx.close();
	        } catch (NamingException e) {
	            /**
	             * For providing the right message, the right exception need to be thrown here.
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
