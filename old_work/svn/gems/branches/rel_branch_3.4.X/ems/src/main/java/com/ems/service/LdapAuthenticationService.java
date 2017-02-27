package com.ems.service;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

/**
 * @author Sameer Surjikar
 * 
 */
@Service("ldapAuthenticationService")
@Transactional(propagation = Propagation.REQUIRED)
public class LdapAuthenticationService {

	 static final Logger logger = Logger.getLogger("LDAPLog");

	/**
	 * Does anonymous authentication with the Ldap server
	 * 
	 * @param conn
	 * @param host
	 * @param port
	 * @return success/failure for Ldap authentication
	 */
	public boolean anonymousAuthentication(LDAPConnection conn, String host,
			int port) {
		try {
			// connect to the server
			conn.connect(host, port);
			if (conn.isBound()) {
				logger.info("Anonymous authentication successfull");
				return true;
			} else {
				logger.info("Anonymous authentication not successfull");
				return false;
			}
		} catch (LDAPException e) {

			logger.error("Error: " + e.toString());
			return false;

		} finally {
			// disconnect with the server
			try {
				conn.disconnect();
			} catch (LDAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Does simple bind authentication with the Ldap server
	 * 
	 * @param conn
	 * @param host
	 * @param port
	 * @return success/failure for Ldap authentication
	 */
	public boolean simpleBindAuthentication(int version, LDAPConnection conn,
			String host, int port, String dn, String passwd) {
		try {
			logger.info("Simple bind Authntication started");
			// connect to the server
			conn.connect(host, port);
			// authenticate to the server with the connection method
			try {
				conn.bind(version, dn, passwd.getBytes("UTF8"));
			} catch (UnsupportedEncodingException u) {
				throw new LDAPException("UTF8 Invalid Encoding",
						LDAPException.LOCAL_ERROR, (String) null, u);
			}
			if (conn.isBound()) {
				logger.info("Simple bind Authntication sucessfull");
				return true;
			} else {
				logger.info("Simple bind Authntication not sucessfull");
				return false;
			}
		}

		catch (LDAPException e) {
			logger.error("Error: " + e.toString());
			return false;
		} finally {
			// disconnect with the server
			try {
				conn.disconnect();
			} catch (LDAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Does ssl bind authentication with the Ldap server
	 * 
	 * @param conn
	 * @param host
	 * @param port
	 * @return success/failure for Ldap authentication
	 */
	public boolean SSLBindAuthentication(int version, LDAPConnection conn ,String host, int SSLPort,
			String dn, String passwd) {
		
      
		try {
			logger.info("SSL bind authentication");

			// connect to the server
			conn.connect(host, SSLPort);
			// authenticate to the server with the connection method

			try {
				conn.bind(version, dn, passwd.getBytes("UTF8"));
			} catch (UnsupportedEncodingException u) {
				throw new LDAPException("UTF8 Invalid Encoding",

				LDAPException.LOCAL_ERROR,

				(String) null, u);
			}
			if (conn.isBound()) {
				logger.info("SSL bind Authntication sucessfull");
				return true;
			} else {
				logger.info("SSL bind Authntication not sucessfull");
				return false;
			}
		} catch (LDAPException e) {

			logger.error("Error: " + e.toString());
			return false;
		} finally {
			// disconnect with the server
			try {
				conn.disconnect();
			} catch (LDAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Mostly used with AD Ldap server. To first get the cn value and call the
	 * authentication function.
	 * 
	 * @param conn
	 * @param host
	 * @param port
	 * @param userName
	 *            :- User trying to login
	 * @param dn
	 *            :- anonymous dn
	 * @param passswd
	 *            : anonymous passwd
	 * @return success/failure for Ldap authentication
	 */
	public String searchCn( int version , LDAPConnection conn, String host, int port , String userName, String baseDn, String searchattr,String dn, String passwd , boolean isanonymousacess ) {
			String cnValue = null ;
	        try {
	        		if(!isanonymousacess)
	        		{
	        			// connect to the server
	        			conn.connect( host, port );
	        			// authenticate to the server with the connection method
	        			try {
	        				conn.bind( version, dn, passwd.getBytes("UTF8") );
	        					
	        				} catch (UnsupportedEncodingException u){
	        					throw new LDAPException( "UTF8 Invalid Encoding",
	                                         LDAPException.LOCAL_ERROR,
	                                         (String)null, u);
	        				} 
	        			}
	        		else
	        		{ 
	        			 conn.connect( host, port );
	        			
	        		}
	            if(conn.isBound()||conn.isConnected() )
	            {
	            	
	            	LDAPSearchResults searchResults = conn.search(
	            			baseDn,
	            	        LDAPConnection.SCOPE_ONE,
	            	        searchattr + "=" + userName,  // username came from the user trying to login
	            	        null,
	            	        false);
	            	LDAPEntry entry = searchResults.next();
	            	if (entry != null) {  // the username is valid, lets pull out the CN from the attributes
	            	  
	            	    LDAPAttributeSet attrSet = entry.getAttributeSet();
	            	    Iterator allAttrs = attrSet.iterator();
	            	    while (allAttrs.hasNext()) {
	            	        LDAPAttribute attr = (LDAPAttribute)allAttrs.next();
	            	        String attrName = attr.getName();
	            	        if (attrName.equalsIgnoreCase("cn")) {  // we got the CN
	            	            cnValue = (String) attr.getStringValues().nextElement();
	            	         // disconnect with the server
	            	        	try {
	            					conn.disconnect();
	            				} catch (LDAPException e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}
	            	            logger.info("Search  successfull");
	            	            return cnValue ;
	            	            
	            	        } else {
	            	            continue;
	            	        }
	            	    }

	            	
	            }
	            	 return cnValue ;
	            }
	            else
	            {
	            	logger.info("Search not successfull , Anonymous acess failed");
	            	return cnValue ;
	            }
	        }
	        catch( LDAPException e ) {

	        	logger.error( "Error: " + e.toString() );
	            return cnValue;

	        }finally
	        {
	        	 // disconnect with the server
	        	try {
					conn.disconnect();
				} catch (LDAPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
	        }
	       
}
	
	public String getLdapUserEmail(int version , LDAPConnection conn, String host, int port , String userName, String baseDn, String searchattr,String dn, String passwd , boolean isanonymousacess )
	{
		String cnValue = null ;
        try {
        		if(!isanonymousacess)
        		{
        			// connect to the server
        			conn.connect( host, port );
        			// authenticate to the server with the connection method
        			try {
        				conn.bind( version, dn, passwd.getBytes("UTF8") );
        				} catch (UnsupportedEncodingException u){
        					throw new LDAPException( "UTF8 Invalid Encoding",
                                         LDAPException.LOCAL_ERROR,
                                         (String)null, u);
        				} 
        			}
        		else
        		{
        			
        			 conn.connect( host, port );
        		
        		}
            if(conn.isBound()||conn.isConnected() )
            {
            	
            	LDAPSearchResults searchResults = conn.search(
            			baseDn,
            	        LDAPConnection.SCOPE_ONE,
            	        searchattr + "=" + userName,  // username came from the user trying to login
            	        null,
            	        false);
            	LDAPEntry entry = searchResults.next();
            	if (entry != null) {  // the username is valid, lets pull out the CN from the attributes
            	  
            	    LDAPAttributeSet attrSet = entry.getAttributeSet();
            	    Iterator allAttrs = attrSet.iterator();
            	    while (allAttrs.hasNext()) {
            	        LDAPAttribute attr = (LDAPAttribute)allAttrs.next();
            	        String attrName = attr.getName();
            	        if (attrName.equalsIgnoreCase("mail")) {  // we got the CN
            	            cnValue = (String) attr.getStringValues().nextElement();
            	         // disconnect with the server
            	        	try {
            					conn.disconnect();
            				} catch (LDAPException e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            				}
            	            logger.info("Search  successfull");
            	            return cnValue ;
            	            
            	        } else {
            	            continue;
            	        }
            	    }

            	
            }
            	 return cnValue ;
            }
            else
            {
            	logger.info("Search not successfull , Anonymous acess failed");
            	return cnValue ;
            }
        }
        catch( LDAPException e ) {

        	logger.error( "Error: " + e.toString() );
            return cnValue;

        }finally
        {
        	 // disconnect with the server
        	try {
				conn.disconnect();
			} catch (LDAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
	}
}
