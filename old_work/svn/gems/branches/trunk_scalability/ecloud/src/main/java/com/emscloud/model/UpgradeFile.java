package com.emscloud.model;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class UpgradeFile {

	private String creationDate;
	private String upgradeFileName;
	private int version = 0;
	private String upgradeFileSize;
	private CommonsMultipartFile fileData;
	
	public CommonsMultipartFile getFileData() {
		return fileData;
	}
	public void setFileData(CommonsMultipartFile fileData) {
		this.fileData = fileData;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public String getUpgradeFileName() {
		return upgradeFileName;
	}
	public void setUpgradeFileName(String upgradeFileName) {
		this.upgradeFileName = upgradeFileName;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getUpgradeFileSize() {
		return upgradeFileSize;
	}
	public void setUpgradeFileSize(String upgradeFileSize) {
		this.upgradeFileSize = upgradeFileSize;
	}
}
