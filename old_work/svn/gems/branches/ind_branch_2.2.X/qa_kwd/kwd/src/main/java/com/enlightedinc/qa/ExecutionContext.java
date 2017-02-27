package com.enlightedinc.qa;

public class ExecutionContext {

	String serverUrl;
	String browserType;
	
	public ExecutionContext(String serverUrl, String browserType) {
		super();
		this.serverUrl = serverUrl;
		this.browserType = browserType;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getBrowserType() {
		return browserType;
	}
}
