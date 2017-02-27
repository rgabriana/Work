package com.ems.types;

public enum RoleType {
    Admin, //
    Auditor, //
    Employee, //
    FacilitiesAdmin, //
    TenantAdmin,
    Bacnet;

    public String getName() {
        return this.name();
    }
}
