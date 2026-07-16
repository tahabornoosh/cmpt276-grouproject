package com.cmpt276.group3.grouproject.enums;

public enum RelationshipCareStyle {
    WORDS("Words"),
    QUALITY_TIME("Quality time"),
    HELPING("Helping with things"),
    THOUGHTFUL_ACTIONS("Small thoughtful actions");

    private final String displayName;

    RelationshipCareStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
