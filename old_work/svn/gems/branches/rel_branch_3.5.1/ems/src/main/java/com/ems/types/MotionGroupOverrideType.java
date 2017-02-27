/**
 * 
 */
package com.ems.types;

/**
 * @author yogesh
 *
 */
public enum MotionGroupOverrideType {
	SU(0), EM(1);
	
	private int type;

	private MotionGroupOverrideType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
