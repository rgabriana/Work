package com.emscloud.types;

public enum RoleType {
    Admin, //    
    SystemAdmin,
    SupportAdmin;

    public String getName() {
        return this.name();
    }
}
