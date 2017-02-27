package com.ems.security.exception;

public class EmsValidationException extends Exception {

	public EmsValidationException(String msg){
		super(msg);
	}

	public EmsValidationException(String msg, Throwable e){
		super(msg, e);
	}
}
