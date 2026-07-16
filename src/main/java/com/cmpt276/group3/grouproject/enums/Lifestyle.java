package com.cmpt276.group3.grouproject.enums;

public enum Lifestyle {
    BUSY("Busy"),
    BALANCED("Balanced"),
    RELAXED("Relaxed"),
    VERY_ACTIVE("Very active");

    private final String displayName;

    Lifestyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
