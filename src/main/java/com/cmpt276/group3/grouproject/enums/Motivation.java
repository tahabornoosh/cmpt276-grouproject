package com.cmpt276.group3.grouproject.enums;

public enum Motivation {
    ACADEMIC_SUCCESS("Academic success"),
    CAREER_SUCCESS("Career success"),
    PERSONAL_GROWTH("Personal growth"),
    ENJOYING_LIFE("Enjoying life");

    private final String displayName;

    Motivation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
