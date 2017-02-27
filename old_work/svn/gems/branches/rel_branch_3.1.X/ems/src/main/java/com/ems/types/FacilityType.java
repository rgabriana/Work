package com.ems.types;

public enum FacilityType {
    ROOT, //
    COMPANY, //
    CAMPUS, //
    BUILDING, //
    FLOOR, //
    AREA, //
    FIXTURE,
    GROUP, //Added by Nitin
    TEMPLATE;
    
    public String getName() {
        return this.toString();
    }

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }

}
