package com.cmpt276.group3.grouproject.enums;

public enum Availability {
    MORNINGS("Mornings"),
    AFTERNOONS("Afternoons"),
    EVENINGS("Evenings"),
    WEEKENDS("Weekends");

    private final String displayName;

    Availability(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
