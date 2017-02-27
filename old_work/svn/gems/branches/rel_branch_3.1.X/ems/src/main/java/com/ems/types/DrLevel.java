/**
 * 
 */
package com.ems.types;

/**
 * @author sreedhar.kamishetti
 *
 */
public enum DrLevel {

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

