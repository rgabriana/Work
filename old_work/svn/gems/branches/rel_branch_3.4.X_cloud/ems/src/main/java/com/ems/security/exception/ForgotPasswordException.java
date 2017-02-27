package com.ems.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * To identify from where the exception has been arised and accordingly route the flow in security xml
 * @author admin
 *
 */
public class ForgotPasswordException extends AuthenticationException {

	public ForgotPasswordException(String msg){
		super(msg);
	}
	
	public ForgotPasswordException(String msg, Throwable e){
		super(msg, e);
	}

}
