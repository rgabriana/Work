package com.emscloud.security.exception;

public class EcloudValidationException extends Exception {

	public EcloudValidationException(String msg){
		super(msg);
	}

	public EcloudValidationException(String msg, Throwable e){
		super(msg, e);
	}
}
