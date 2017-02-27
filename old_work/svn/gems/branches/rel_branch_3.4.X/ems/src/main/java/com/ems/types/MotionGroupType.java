/**
 * 
 */
package com.ems.types;

/**
 * @author yogesh
 * 
 */
public enum MotionGroupType {
	ACT_AND_REPORT(0), ACT_ONLY(1), REPORT_ONLY(2);

	private int type;

	private MotionGroupType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
