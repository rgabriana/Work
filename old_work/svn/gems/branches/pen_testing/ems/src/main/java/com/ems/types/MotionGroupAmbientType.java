/**
 * 
 */
package com.ems.types;

/**
 * @author yogesh
 *
 */
public enum MotionGroupAmbientType {
	ALWAYS(0), DARK(1), DIM(2), NORMAL(3), BRIGHT(4);

	private int type;

	private MotionGroupAmbientType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
