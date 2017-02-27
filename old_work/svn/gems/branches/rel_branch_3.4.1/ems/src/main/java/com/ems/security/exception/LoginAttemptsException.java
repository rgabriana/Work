package com.ems.security.exception;

import org.springframework.security.core.AuthenticationException;

public class LoginAttemptsException extends AuthenticationException {

	public LoginAttemptsException(String msg) {
		super(msg);		
	}
}
