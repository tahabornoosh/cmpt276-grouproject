package com.cmpt276.group3.grouproject.enums;

public enum RelationshipGoal {
    LONG_TERM("Long-term relationship"),
    CASUAL_DATING("Casual dating"),
    OPEN_TO_POSSIBILITIES("Open to possibilities"),
    NOT_SURE("Not sure yet");

    private final String displayName;

    RelationshipGoal(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
