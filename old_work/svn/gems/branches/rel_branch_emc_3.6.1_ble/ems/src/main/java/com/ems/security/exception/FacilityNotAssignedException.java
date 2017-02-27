package com.ems.security.exception;

import org.springframework.security.core.AuthenticationException;

public class FacilityNotAssignedException extends AuthenticationException {

	public FacilityNotAssignedException(String msg) {
		super(msg);		
	}

}
