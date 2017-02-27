package com.communicator.type;

public enum SyncTaskStatus {
	
	Success,
	Failed,
	Queued,
	InProgress
	;
	
	public String getName() {
		return this.toString();
	}


}
