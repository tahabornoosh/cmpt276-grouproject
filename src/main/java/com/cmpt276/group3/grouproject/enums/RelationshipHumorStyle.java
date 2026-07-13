package com.cmpt276.group3.grouproject.enums;

public enum RelationshipHumorStyle {
    SARCASTIC("Sarcastic"),
    GOOFY("Goofy"),
    DRY("Dry"),
    LIGHT_FRIENDLY("Light and friendly");

    private final String displayName;

    RelationshipHumorStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
