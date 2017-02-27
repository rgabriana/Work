/**
 * 
 */
package com.ems.utils;

import java.util.Arrays;

/**
 * Fires external process
 * @author yogesh
 * 
 */
public class ExecuteExternalProgram extends Thread {
	private String[] m_args;
	private int iStatus = -1;
	private String procError = "";

	public ExecuteExternalProgram(String[] args) {
		this.m_args = args;
	}

	public void run() {
		Process pingProcess = null;
		try {
			System.out.println("HAVC DEBUGGING IN SHELL ROOM TEMP--- > " +  Arrays.toString(m_args));
			pingProcess = Runtime.getRuntime().exec(m_args);
			pingProcess.waitFor();
			iStatus = pingProcess.exitValue();
		} catch (Exception e) {
			procError = e.getMessage();
		} finally {
			if (pingProcess != null) {
				pingProcess.destroy();
			}
		}
	}

	/**
	 * @return the iStatus
	 */
	public int getiStatus() {
		return iStatus;
	}

	/**
	 * @param iStatus
	 *            the iStatus to set
	 */
	public void setiStatus(int iStatus) {
		this.iStatus = iStatus;
	}

	/**
	 * @return the procError
	 */
	public String getProcError() {
		return procError;
	}

	/**
	 * @param strError
	 *            the procError to set
	 */
	public void setProcError(String procError) {
		this.procError = procError;
	}
}
