/**
 * 
 */
package com.ems.types;

/**
 * @author sreedhar.kamishetti
 *
 */
public enum DrLevel {

	HOLIDAY(16), //this is not a dr override. It is of type override where type is 1 and override is 0.
	SPECIAL(4),
	HIGH(1),
	MODERATE(2),
	LOW(3);
	
	private final int value;
	
	DrLevel(int i) {
		value = i;
	}
	
	public int value() {
        return value;
    }
	
	public String getName() {
		return this.name();
	}
} //end of enumeration DrLevel

