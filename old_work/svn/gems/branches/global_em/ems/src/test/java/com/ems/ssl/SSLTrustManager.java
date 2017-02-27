/**
 * 
 */
package com.ems.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * @author Sreedhar
 *
 */
public class SSLTrustManager implements X509TrustManager {
  
  /**
   * 
   */
  public SSLTrustManager() {
    // TODO Auto-generated constructor stub
  }
  
  public X509Certificate[] getAcceptedIssuers() {
    return null; 
  }
  
  public void checkClientTrusted(X509Certificate[] certs, String authType) { 
  }
  
  public void checkServerTrusted(X509Certificate[] certs, String authType) {
  }
  
} //end of class SSLTrustManager
