package com.ems.model;

import org.springframework.web.multipart.commons.CommonsMultipartFile;


/**
 * Model for populating the list of backup files.
 * @author Sharad M
 *
 */
public class BackUpFile {

	private String creationDate;
	private String backupfileName;
	private String path;
	private String backupfileSize;
	private String filepath;
	private CommonsMultipartFile fileData;
	
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getBackupfileName() {
		return backupfileName;
	}
	public void setBackupfileName(String backupfileName) {
		this.backupfileName = backupfileName;
	}
	public String getBackupfileSize() {
		return backupfileSize;
	}
	public void setBackupfileSize(String backupfileSize) {
		this.backupfileSize = backupfileSize;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public CommonsMultipartFile getFileData()  {
	    return fileData;
	}
	public void setFileData(CommonsMultipartFile fileData)  {
	    this.fileData = fileData;
	}
	
}
