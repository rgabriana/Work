package com.communication.types;

public enum TaskProgressStatus {
	
	//upgrade progress status 
	ImageDownloadRequested,
	ImageDownloadSuccess,
	UpgradingEM,
	EMUpgradeSuccess,
	EMUpgradeFailed,
	
	//Log upload status
	LogUploadRequested,
	LogUploadInProgress,
	LogUploadSuccess,
	LogUploadFailed;
	
	
	public String getName()
	{
		return this.toString();
	}

}
