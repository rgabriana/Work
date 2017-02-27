/**
 * 
 */
package com.ems.types;

/**
 * @author yogesh
 *
 */
public enum DeviceType {
    Fixture,
    Gateway,
    WDS,
    LocatorDevice;
    
    public String getName() {
        return this.toString();
    }
}
