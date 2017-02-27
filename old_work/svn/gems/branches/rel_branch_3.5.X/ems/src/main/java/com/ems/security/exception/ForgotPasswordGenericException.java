package com.ems.security.exception;

import org.springframework.security.core.AuthenticationException;

public class ForgotPasswordGenericException extends AuthenticationException {

	public ForgotPasswordGenericException(String msg){
		super(msg);
	}
	
	public ForgotPasswordGenericException(String msg, Throwable e){
		super(msg, e);
	}
}

