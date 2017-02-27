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
    LocatorDevice,
    Plugload;
    
    public String getName() {
        return this.toString();
    }
}
