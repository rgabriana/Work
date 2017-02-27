package com.emscloud.types;

public enum BillStatus {

    ACTIVE, //
    INACTIVE,
    OBSOLETE;

    private String status;
    
    public String getStatus() {
        return status;
    }

    public void setLabel(String status) {
        this.status = status;
    }
}
