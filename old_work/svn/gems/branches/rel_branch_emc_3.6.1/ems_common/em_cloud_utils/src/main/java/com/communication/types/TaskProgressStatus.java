package com.communication.types;

public enum TaskProgressStatus {
	
	//upgrade progress status 
	ImageDownloadRequested,
	ImageDownloadSuccess,
	UpgradingEM,
	EMUpgradeSuccess,
	EMUpgradeFailed,
	TSCertDownloadRequested,
	TSCertDownloadSuccess,
	KSCertDownloadRequested,
	KSCertDownloadSuccess,
	KSCertAuthFailure,
	CertificateSyncSuccess,
	CertificateSyncFailed,
	
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
