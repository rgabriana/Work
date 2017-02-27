/**
 * 
 */
package com.ems.server.ssl;

/**
 * @author Sreedhar
 *
 */
public interface SSLConstants {

  public static final int GW_SSL_PORT = 995;
  
  public static String TLS_PROTOCOL_VERSION = "TLSv1";
	
  public static String[] TLS_CIPHER_SUITES = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
    	
} //end of interface SSLInterface
