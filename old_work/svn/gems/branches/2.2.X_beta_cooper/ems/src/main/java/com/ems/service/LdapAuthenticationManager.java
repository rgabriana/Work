package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novell.ldap.LDAPConnection;



/**
 * @author Sameer Surjikar
 *
 */
@Service("ldapAuthenticationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class LdapAuthenticationManager {

	@Resource
    private LdapAuthenticationService ldapAuthenticationService;
	
	public boolean anonymousAuthentication( LDAPConnection conn, String host, int port ) {
		return ldapAuthenticationService.anonymousAuthentication(conn, host, port) ;
	}
	public boolean simpleBindAuthentication(  int version, LDAPConnection conn, String host, int port,String dn, String passwd ) {
		return ldapAuthenticationService.simpleBindAuthentication(version, conn, host, port, dn, passwd) ;
	}
	public boolean SSLBindAuthentication( int version, LDAPConnection conn, String host, int SSLPort, String dn, String passwd ) {
     
		return ldapAuthenticationService.SSLBindAuthentication(version, conn ,host, SSLPort, dn, passwd);
	}
	public String searchCn( int version , LDAPConnection conn, String host, int port , String userName, String baseDn,String searchattr, String dn, String passwd , boolean isanonymousacess )
	{
		return ldapAuthenticationService.searchCn(version, conn, host, port, userName, baseDn, searchattr, dn, passwd, isanonymousacess);
	}
	public String getLdapUserEmail(int version , LDAPConnection conn, String host, int port , String userName, String baseDn, String searchattr,String dn, String passwd , boolean isanonymousacess )
	{
		return ldapAuthenticationService.getLdapUserEmail(version, conn, host, port, userName, baseDn, searchattr, dn, passwd, isanonymousacess);
	}
}
