package com.enlightedportal.types;

public enum AuthenticationType {
    DATABASE, //
    LDAP;

    public String getName() {
        return this.name();
    }
}
