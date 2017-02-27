package com.emscloud.util;

import java.util.Hashtable;

public enum OccupancyTypeEnum {
    LAST_30_DAYS("Last 30 days", 30), QTD("QTD", 90), YTD("YTD", 365);
    private String name;
    private int cvalue = 0;

    private OccupancyTypeEnum(String name, int value) {
        this.name = name;
        this.cvalue = value;
        OccTypeEnumDir.getEnumDir().put(name, this);
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public int getNoOfDays() {
        return cvalue;
    }

    @Override
    public String toString() {
        return name;
    }

    public static final OccupancyTypeEnum getEnumFromName(String name) {
        OccupancyTypeEnum result = OccTypeEnumDir.getEnumDir().get(name);
        if (result != null)
            return result;
        if (name == null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException("No enum const " + OccupancyTypeEnum.class + "." + name);
    }

}

class OccTypeEnumDir {
    private final static Hashtable<String, OccupancyTypeEnum> enumDir = new Hashtable<String, OccupancyTypeEnum>();

    public static Hashtable<String, OccupancyTypeEnum> getEnumDir() {
        return enumDir;
    }
}
