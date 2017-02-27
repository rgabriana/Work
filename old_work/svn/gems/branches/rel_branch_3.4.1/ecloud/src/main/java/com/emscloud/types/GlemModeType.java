/**
 * 
 */
package com.emscloud.types;

/**
 * @author yogesh
 * 
 */
public enum GlemModeType {
	ECLOUD(0), UEM(1);

	private int iMode;

	private GlemModeType(int mode) {
		setMode(mode);
	}

	public int getMode() {
		return iMode;
	}

	public void setMode(int mode) {
		this.iMode = mode;
	}

}
