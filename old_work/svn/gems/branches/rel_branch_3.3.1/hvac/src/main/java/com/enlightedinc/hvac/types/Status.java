package com.enlightedinc.hvac.types;

public enum Status {

    ACTIVE("ACTIVE"), //
    INACTIVE("INACTIVE");

    private String label;

    private Status(String value) {
        setLabel(value);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
