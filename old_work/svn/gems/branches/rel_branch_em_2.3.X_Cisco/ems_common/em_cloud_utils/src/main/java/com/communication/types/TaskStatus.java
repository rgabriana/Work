package com.communication.types;

public enum TaskStatus {
	
	SCHEDULED,
	IN_PROGRESS,
	FAILED,
	SUCCESS,
	CANCELED;
	
	public String getName()
	{
		return this.toString();
	}

}
