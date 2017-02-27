/**
 * 
 */
package com.ems.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author Sameer Surjikar
 *
 */
public class SSLCertificateNotFoundException extends AuthenticationException {
	public SSLCertificateNotFoundException(String msg) {
		super(msg);		
	}
}
