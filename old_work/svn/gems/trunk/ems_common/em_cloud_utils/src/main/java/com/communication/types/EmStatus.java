package com.communication.types;

public enum EmStatus {
    CALL_HOME, 
    SPPA;
    
    
    public String getName() {
        return this.toString();
    }

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }
}