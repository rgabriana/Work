package com.communication.types;

public enum DataPullRequestStateType {
	
	Queued,
	Processing,
	Successful,
	Failed,
	Deleted,
	Cancelled
	;
	
	public String getName() {
        return this.toString();
    }

}
