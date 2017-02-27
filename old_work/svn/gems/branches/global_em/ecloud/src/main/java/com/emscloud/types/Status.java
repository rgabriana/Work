package com.emscloud.types;

public enum Status {

    A("ACTIVE"), //
    I("INACTIVE");

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
