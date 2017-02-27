package com.communicator.util;

import java.util.Date;

import com.communication.types.DatabaseState;
import com.communicator.type.SyncTaskStatus;

public class SyncTasks {
	
	private Long lastWalId;
	private String filename;
	private Date creationTime;
	private String action;
	private int failedAttemps;
	private String checksum;
	private SyncTaskStatus status;

	
	public Long getLastWalId() {
		return lastWalId;
	}
	public void setLastWalId(Long lastWalId) {
		this.lastWalId = lastWalId;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public int getFailedAttemps() {
		return failedAttemps;
	}
	public void setFailedAttemps(int failedAttemps) {
		this.failedAttemps = failedAttemps;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	public SyncTaskStatus getStatus() {
		return status;
	}
	public void setStatus(SyncTaskStatus status) {
		this.status = status;
	}
}
