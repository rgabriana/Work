package com.ems.security.exception;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Sameer Surjikar
 *
 */
public class EmsUnkownSecurityException extends AuthenticationException{
	
		public EmsUnkownSecurityException(String msg) {
			super(msg);		
		}
}



