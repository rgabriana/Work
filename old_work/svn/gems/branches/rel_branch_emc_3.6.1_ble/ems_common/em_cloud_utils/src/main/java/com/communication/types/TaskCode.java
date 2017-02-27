package com.communication.types;

public enum TaskCode {
	
	UPGRADE,
	CERTIFICATE_SYNC,
	LOG_UPLOAD;
	
	public String getName()
	{
		return this.toString();
	}

}
