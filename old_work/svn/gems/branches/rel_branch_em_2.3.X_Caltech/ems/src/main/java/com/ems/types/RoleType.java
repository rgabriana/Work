package com.ems.types;

public enum RoleType {
    Admin, //
    Auditor, //
    Employee, //
    FacilitiesAdmin, //
    TenantAdmin;

    public String getName() {
        return this.name();
    }
}
