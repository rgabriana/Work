package com.emscloud.types;

public enum RoleType {
    Admin, //    
    SystemAdmin,
    SupportAdmin,
    ThirdPartySupportAdmin,
    SPPA;

    public String getName() {
        return this.name();
    }
}
