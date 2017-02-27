package com.communication.types;

public enum TaskProgressStatus {
	
	ImageDownloadRequested,
	ImageDownloadSuccess,
	UpgradingEM,
	EMUpgradeSuccess,
	EMUpgradeFailed;
	
	public String getName()
	{
		return this.toString();
	}

}
