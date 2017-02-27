/**
 * 
 */
package com.ems.types;

/**
 * @author yogesh
 * 
 */
public enum GGroupType {
    ProfileGroup(1), MotionGroup(2), DrGroup(3), SwitchGroup(4);

    private int id;

    private GGroupType(int id) {
        this.id = id;
    }

    public String getName() {
        return this.toString();
    }

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
