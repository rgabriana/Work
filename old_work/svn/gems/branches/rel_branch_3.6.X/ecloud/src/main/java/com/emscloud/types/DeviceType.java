/**
 * 
 */
package com.emscloud.types;

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
