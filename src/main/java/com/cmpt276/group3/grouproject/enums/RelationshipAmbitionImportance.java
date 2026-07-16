package com.cmpt276.group3.grouproject.enums;

public enum RelationshipAmbitionImportance {
    VERY_IMPORTANT("Very important"),
    SOMEWHAT_IMPORTANT("Somewhat important"),
    NOT_MAIN_FOCUS("Not a main focus"),
    FIGURING_OUT("I’m still figuring it out");

    private final String displayName;

    RelationshipAmbitionImportance(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
