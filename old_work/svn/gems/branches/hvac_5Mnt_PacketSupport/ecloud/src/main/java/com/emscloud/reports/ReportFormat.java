package com.emscloud.reports;

public enum ReportFormat {

	DOC("doc", "application/msword"), //
	PDF("pdf", "application/pdf"), //
	TEXT("text", "application/text");

	private String extension;

	private String contentType;

	private ReportFormat(String extension, String contentType) {
		this.extension = extension;
		this.contentType = contentType;
	}

	public String getExtension() {
		return extension;
	}

	public String getContentType() {
		return contentType;
	}
	
	

}
