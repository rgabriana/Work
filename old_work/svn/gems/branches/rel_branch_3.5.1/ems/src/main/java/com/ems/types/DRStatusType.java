package com.ems.types;

public enum DRStatusType {
    Active, //
    //Inactive, //
    Completed, //
    Cancelled, //
    Scheduled,
    Far,
    Near;
    //Failed; //
    public String getName() {
        return this.name();
    }
}

