package com.emscloud.types;

public enum AuthenticationType {
    DATABASE, //
    LDAP;

    public String getName() {
        return this.name();
    }
}
