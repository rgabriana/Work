package com.emscloud.types;

public enum FacilityType {
    ROOT, //
    CUSTOMER,
    COMPANY, //
    CAMPUS, //
    BUILDING, //
    FLOOR, //
    AREA, //
    FIXTURE,
    GEMS,//Added by sharad
    GROUP; //Added by Nitin
    
    public String getName() {
        return this.toString();
    }

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }

}
