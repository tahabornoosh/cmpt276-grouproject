package com.cmpt276.group3.grouproject.enums;

public enum Campus {
    BURNABY("Burnaby"),
    SURREY("Surrey"),
    VANCOUVER("Vancouver"),
    MOSTLY_ONLINE("Mostly online");

    private final String displayName;

    Campus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
